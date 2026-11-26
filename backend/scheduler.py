"""Daily scheduled background worker for Balasore 360 news & weather alert aggregation.

Supports:
  1. APScheduler BackgroundScheduler with CronTrigger (Asia/Kolkata daily schedule)
  2. Automatic stdlib threading fallback when APScheduler is not installed
  3. One-shot execution (--run-once) for Linux cron, systemd timers, or Cloud Scheduler
  4. Optional read-only local JSON feed server (--serve-port 8090) for testing/inspection

Examples:
  python3 backend/scheduler.py --run-once
  python3 backend/scheduler.py --hour 6 --minute 0
  python3 backend/scheduler.py --hour 6 --minute 0 --serve-port 8090
"""

from __future__ import annotations

import argparse
import datetime as dt
import json
import logging
import signal
import sys
import threading
import urllib.parse
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
from typing import Optional
from zoneinfo import ZoneInfo

# Ensure backend directory is importable whether run from project root or backend/
sys.path.insert(0, str(Path(__file__).resolve().parent))

from aggregators import AggregationSummary, BalasoreAggregationService
from config import BackendConfig
from database import BalasoreDatabase

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(name)s: %(message)s",
)
logger = logging.getLogger("balasore.scheduler")


def seconds_until_next_daily_run(hour: int, minute: int, timezone_name: str, now: Optional[dt.datetime] = None) -> float:
    """Calculate seconds until the next daily HH:MM occurrence in the configured timezone."""
    tz = ZoneInfo(timezone_name)
    current = now.astimezone(tz) if now else dt.datetime.now(tz)
    target = current.replace(hour=hour, minute=minute, second=0, microsecond=0)
    if target <= current:
        target = target + dt.timedelta(days=1)
    return max(1.0, (target - current).total_seconds())


class BalasoreDailyScheduler:
    """Runs the Balasore news & weather aggregation job automatically every day."""

    def __init__(
        self,
        service: BalasoreAggregationService,
        hour: int = 6,
        minute: int = 0,
        timezone_name: str = "Asia/Kolkata",
    ) -> None:
        self.service = service
        self.hour = max(0, min(23, hour))
        self.minute = max(0, min(59, minute))
        self.timezone_name = timezone_name
        self._stop_event = threading.Event()
        self._apscheduler = None
        self._fallback_thread: Optional[threading.Thread] = None

    def execute_job(self, trigger_type: str = "SCHEDULED_DAILY") -> AggregationSummary:
        logger.info("Starting Balasore aggregation job (trigger=%s)...", trigger_type)
        summary = self.service.run_aggregation(trigger_type=trigger_type)
        logger.info(
            "Aggregation complete [run_id=%s status=%s news=%d snapshots=%d alerts=%d expired=%d firestore=%s]",
            summary.run_id,
            summary.status,
            summary.news_upserted,
            summary.snapshots_saved,
            summary.alerts_upserted,
            summary.alerts_expired,
            summary.firestore_synced,
        )
        return summary

    def start(self, run_immediately: bool = False) -> str:
        """Start the daily background scheduler. Returns backend engine name."""
        if run_immediately:
            self.execute_job(trigger_type="STARTUP")

        try:
            from apscheduler.schedulers.background import BackgroundScheduler
            from apscheduler.triggers.cron import CronTrigger

            scheduler = BackgroundScheduler(timezone=ZoneInfo(self.timezone_name))
            scheduler.add_job(
                func=self.execute_job,
                trigger=CronTrigger(hour=self.hour, minute=self.minute, timezone=ZoneInfo(self.timezone_name)),
                id="balasore_daily_news_weather_sync",
                name="Balasore Daily Real-Time News & Weather Alert Sync",
                replace_existing=True,
                misfire_grace_time=3600,
                coalesce=True,
                max_instances=1,
            )
            scheduler.start()
            self._apscheduler = scheduler
            logger.info(
                "APScheduler started: daily sync scheduled at %02d:%02d (%s)",
                self.hour,
                self.minute,
                self.timezone_name,
            )
            return "apscheduler"
        except ImportError:
            logger.info("APScheduler not installed; starting built-in Python daily scheduler thread.")
            self._fallback_thread = threading.Thread(
                target=self._stdlib_daily_loop,
                name="balasore-daily-scheduler",
                daemon=True,
            )
            self._fallback_thread.start()
            return "stdlib-thread"

    def _stdlib_daily_loop(self) -> None:
        while not self._stop_event.is_set():
            wait_seconds = seconds_until_next_daily_run(self.hour, self.minute, self.timezone_name)
            logger.info(
                "Next Balasore daily sync in %.0f seconds (scheduled %02d:%02d %s)",
                wait_seconds,
                self.hour,
                self.minute,
                self.timezone_name,
            )
            if self._stop_event.wait(timeout=wait_seconds):
                break
            try:
                self.execute_job(trigger_type="SCHEDULED_DAILY")
            except Exception:
                logger.exception("Scheduled Balasore daily sync failed; will retry on next daily window.")

    def stop(self) -> None:
        self._stop_event.set()
        if self._apscheduler is not None:
            self._apscheduler.shutdown(wait=False)
            self._apscheduler = None
        if self._fallback_thread is not None and self._fallback_thread.is_alive():
            self._fallback_thread.join(timeout=2.0)


def create_readonly_api_server(db: BalasoreDatabase, port: int) -> ThreadingHTTPServer:
    """Create a lightweight read-only local HTTP JSON server for inspecting aggregated Balasore data."""

    allowed_categories = {"Local", "Weather", "Defense", "Civic", "Tourism", "Heritage", "Agriculture"}
    allowed_severities = {"INFO", "ADVISORY", "WATCH", "WARNING", "EMERGENCY"}

    class BalasoreReadOnlyHandler(BaseHTTPRequestHandler):
        def _send_json(self, status_code: int, payload: object) -> None:
            body = json.dumps(payload, ensure_ascii=False).encode("utf-8")
            self.send_response(status_code)
            self.send_header("Content-Type", "application/json; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.send_header("X-Content-Type-Options", "nosniff")
            self.end_headers()
            self.wfile.write(body)

        def do_GET(self) -> None:  # noqa: N802
            parsed = urllib.parse.urlparse(self.path)
            params = urllib.parse.parse_qs(parsed.query)

            if parsed.path == "/api/health":
                self._send_json(200, {"status": "ok", "recentRuns": db.get_recent_runs(limit=5)})
                return

            if parsed.path == "/api/balasore/news":
                cat = (params.get("category") or [None])[0]
                if cat and cat not in allowed_categories:
                    self._send_json(400, {"error": "Invalid category parameter"})
                    return
                try:
                    limit = int((params.get("limit") or ["25"])[0])
                except ValueError:
                    self._send_json(400, {"error": "Invalid limit parameter"})
                    return
                self._send_json(200, {"articles": db.get_latest_news(category=cat, limit=limit)})
                return

            if parsed.path == "/api/balasore/weather":
                sev = (params.get("severity") or [None])[0]
                if sev and sev not in allowed_severities:
                    self._send_json(400, {"error": "Invalid severity parameter"})
                    return
                self._send_json(
                    200,
                    {
                        "snapshots": db.get_latest_weather_snapshots(),
                        "activeAlerts": db.get_active_weather_alerts(severity=sev),
                    },
                )
                return

            if parsed.path == "/api/balasore/topics/ranked":
                try:
                    limit = int((params.get("limit") or ["10"])[0])
                except ValueError:
                    self._send_json(400, {"error": "Invalid limit parameter"})
                    return
                self._send_json(200, {"topics": db.get_ranked_news_topics(limit=limit)})
                return

            if parsed.path == "/api/balasore/topics/trends":
                topic_key = " ".join(((params.get("topic_key") or [""])[0]).strip().split()).lower()
                if not topic_key:
                    self._send_json(400, {"error": "Missing non-empty topic_key parameter"})
                    return
                try:
                    days = int((params.get("days") or ["7"])[0])
                except ValueError:
                    self._send_json(400, {"error": "Invalid days parameter"})
                    return
                if not (1 <= days <= 30):
                    self._send_json(400, {"error": "days must be between 1 and 30"})
                    return
                self._send_json(
                    200,
                    {
                        "topic_key": topic_key,
                        "days": days,
                        "trend": db.get_news_topic_trends(topic_key=topic_key, days=days),
                    },
                )
                return

            if parsed.path == "/api/balasore/tourism/hotspots":
                allowed_tourism_cats = {"BEACH", "TEMPLE", "WILDLIFE", "HERITAGE", "ESTUARY", "HILL_SHRINE", "WATERFALL"}
                cat = (params.get("category") or [None])[0]
                if cat and cat not in allowed_tourism_cats:
                    self._send_json(400, {"error": "Invalid tourism category parameter"})
                    return
                bbox_keys = ("min_lat", "max_lat", "min_lon", "max_lon")
                bbox_vals: dict[str, Optional[float]] = {k: None for k in bbox_keys}
                provided_bbox = [k for k in bbox_keys if k in params]
                if provided_bbox and len(provided_bbox) != 4:
                    self._send_json(400, {"error": "Bounding box requires min_lat, max_lat, min_lon, and max_lon"})
                    return
                try:
                    for k in provided_bbox:
                        val = float(params[k][0])
                        if "lat" in k and not (-90.0 <= val <= 90.0):
                            raise ValueError("Latitude out of bounds")
                        if "lon" in k and not (-180.0 <= val <= 180.0):
                            raise ValueError("Longitude out of bounds")
                        bbox_vals[k] = val
                except ValueError:
                    self._send_json(400, {"error": "Invalid geographic coordinate bounds"})
                    return

                fmt = (params.get("format") or ["json"])[0].lower()
                if fmt == "geojson":
                    self._send_json(
                        200,
                        db.get_tourism_hotspots_geojson(
                            category=cat,
                            min_lat=bbox_vals["min_lat"],
                            max_lat=bbox_vals["max_lat"],
                            min_lon=bbox_vals["min_lon"],
                            max_lon=bbox_vals["max_lon"],
                        ),
                    )
                else:
                    self._send_json(
                        200,
                        {
                            "hotspots": db.get_tourism_hotspots(
                                category=cat,
                                min_lat=bbox_vals["min_lat"],
                                max_lat=bbox_vals["max_lat"],
                                min_lon=bbox_vals["min_lon"],
                                max_lon=bbox_vals["max_lon"],
                            )
                        },
                    )
                return

            self._send_json(404, {"error": "Not found"})

        def do_POST(self) -> None:  # noqa: N802
            parsed = urllib.parse.urlparse(self.path)
            if parsed.path != "/api/balasore/topics/search":
                self._send_json(404, {"error": "Not found"})
                return
            try:
                content_len = int(self.headers.get("Content-Length", "0"))
            except ValueError:
                self._send_json(400, {"error": "Invalid Content-Length"})
                return
            if content_len <= 0 or content_len > 2048:
                self._send_json(400, {"error": "Payload must be between 1 and 2048 bytes"})
                return
            try:
                body = json.loads(self.rfile.read(content_len).decode("utf-8"))
            except (UnicodeDecodeError, json.JSONDecodeError):
                self._send_json(400, {"error": "Invalid JSON body"})
                return
            topic = body.get("topic") if isinstance(body, dict) else None
            category = body.get("category") if isinstance(body, dict) else None
            if not isinstance(topic, str) or not topic.strip():
                self._send_json(400, {"error": "Missing non-empty topic string"})
                return
            if category is not None and (not isinstance(category, str) or category not in allowed_categories):
                category = None
            recorded = db.record_news_topic_search(topic=topic, category=category)
            self._send_json(200, {"recorded": recorded, "topics": db.get_ranked_news_topics(limit=10)})

        def do_DELETE(self) -> None:  # noqa: N802
            parsed = urllib.parse.urlparse(self.path)
            params = urllib.parse.parse_qs(parsed.query)
            if parsed.path != "/api/balasore/topics/search":
                self._send_json(404, {"error": "Not found"})
                return
            if (params.get("all") or [""])[0].lower() == "true":
                cleared = db.clear_news_topic_searches()
                self._send_json(200, {"cleared": cleared, "topics": []})
                return
            topic_key = (params.get("topic_key") or [""])[0]
            if not topic_key.strip():
                self._send_json(400, {"error": "Missing topic_key parameter"})
                return
            removed = db.delete_news_topic_search(topic_key)
            self._send_json(200, {"removed": removed, "topics": db.get_ranked_news_topics(limit=10)})

        def log_message(self, fmt: str, *args: object) -> None:
            logger.info("HTTP %s - " + fmt, self.address_string(), *args)

    return ThreadingHTTPServer(("127.0.0.1", port), BalasoreReadOnlyHandler)


def main() -> int:
    parser = argparse.ArgumentParser(description="Balasore 360 Daily News & Weather Alert Aggregator")
    parser.add_argument("--run-once", action="store_true", help="Execute a single aggregation sync and exit")
    parser.add_argument("--run-on-start", action="store_true", help="Run immediately before waiting for daily schedule")
    parser.add_argument("--hour", type=int, default=None, help="Daily run hour (0-23) in configured timezone")
    parser.add_argument("--minute", type=int, default=None, help="Daily run minute (0-59)")
    parser.add_argument("--serve-port", type=int, default=0, help="Optional localhost port for read-only JSON API")
    args = parser.parse_args()

    config = BackendConfig.from_env()
    db = BalasoreDatabase(config.db_path)
    service = BalasoreAggregationService(config=config, db=db)

    if args.run_once:
        summary = service.run_aggregation(trigger_type="MANUAL")
        print(json.dumps(summary.__dict__, indent=2, ensure_ascii=False))
        return 0 if summary.status in {"SUCCESS", "PARTIAL"} else 1

    hour = config.daily_hour if args.hour is None else args.hour
    minute = config.daily_minute if args.minute is None else args.minute
    scheduler = BalasoreDailyScheduler(
        service=service,
        hour=hour,
        minute=minute,
        timezone_name=config.timezone,
    )

    stop_signal = threading.Event()

    def _handle_shutdown(signum: int, _frame: object) -> None:
        logger.info("Received signal %d; shutting down Balasore scheduler...", signum)
        stop_signal.set()

    signal.signal(signal.SIGINT, _handle_shutdown)
    signal.signal(signal.SIGTERM, _handle_shutdown)

    scheduler.start(run_immediately=args.run_on_start)

    http_server: Optional[ThreadingHTTPServer] = None
    if args.serve_port > 0:
        http_server = create_readonly_api_server(db, args.serve_port)
        api_thread = threading.Thread(target=http_server.serve_forever, daemon=True)
        api_thread.start()
        logger.info("Read-only Balasore feed API listening on http://127.0.0.1:%d", args.serve_port)

    try:
        while not stop_signal.is_set():
            stop_signal.wait(timeout=1.0)
    finally:
        scheduler.stop()
        if http_server is not None:
            http_server.shutdown()
            http_server.server_close()

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
