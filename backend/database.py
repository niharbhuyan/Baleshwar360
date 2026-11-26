"""Parameterized SQLite storage layer and optional Cloud Firestore sync for Balasore 360."""

from __future__ import annotations

import datetime as dt
import json
import logging
import sqlite3
import time
import uuid
from contextlib import contextmanager
from dataclasses import dataclass
from pathlib import Path
from typing import Any, Dict, Iterable, Iterator, List, Optional

from config import SCHEMA_PATH

logger = logging.getLogger("balasore.database")


@dataclass(frozen=True)
class NewsArticleRecord:
    id: str
    content_hash: str
    region_code: str
    title: str
    title_or: Optional[str]
    summary: str
    content: str
    category: str
    source: str
    author: str
    source_url: str
    image_url: Optional[str]
    is_breaking: bool
    priority: int
    published_at_ms: int


@dataclass(frozen=True)
class WeatherSnapshotRecord:
    id: str
    region_code: str
    provider: str
    temperature_c: float
    feels_like_c: float
    humidity_pct: int
    wind_speed_kmh: float
    wind_gust_kmh: Optional[float]
    pressure_hpa: Optional[int]
    rain_mm_3h: float
    weather_code: str
    condition_summary: str
    flood_risk_level: str
    tide_advisory: Optional[str]
    observed_at_ms: int


@dataclass(frozen=True)
class WeatherAlertRecord:
    id: str
    alert_hash: str
    region_code: str
    event_type: str
    severity: str
    headline: str
    headline_or: Optional[str]
    description: str
    affected_blocks: List[str]
    source: str
    starts_at_ms: int
    expires_at_ms: int
    is_active: bool = True


class BalasoreDatabase:
    """SQLite database wrapper using strictly parameterized SQL queries."""

    def __init__(self, db_path: Path, schema_path: Path = SCHEMA_PATH) -> None:
        self.db_path = db_path
        self.schema_path = schema_path
        self.db_path.parent.mkdir(parents=True, exist_ok=True)
        self.initialize_schema()

    @contextmanager
    def _connect(self) -> Iterator[sqlite3.Connection]:
        conn = sqlite3.connect(str(self.db_path), timeout=30.0)
        conn.row_factory = sqlite3.Row
        conn.execute("PRAGMA foreign_keys = ON;")
        try:
            yield conn
            conn.commit()
        except Exception:
            conn.rollback()
            raise
        finally:
            conn.close()

    def initialize_schema(self) -> None:
        schema_sql = self.schema_path.read_text(encoding="utf-8")
        with self._connect() as conn:
            conn.executescript(schema_sql)

    def start_run(self, trigger_type: str) -> str:
        run_id = f"run_{int(time.time())}_{uuid.uuid4().hex[:8]}"
        now_ms = int(time.time() * 1000)
        with self._connect() as conn:
            conn.execute(
                """
                INSERT INTO aggregation_runs (run_id, trigger_type, started_at_ms, status)
                VALUES (?, ?, ?, 'RUNNING')
                """,
                (run_id, trigger_type, now_ms),
            )
        return run_id

    def finish_run(
        self,
        run_id: str,
        status: str,
        news_upserted: int,
        snapshots_saved: int,
        alerts_upserted: int,
        alerts_expired: int,
        firestore_synced: bool,
        error_summary: Optional[str] = None,
    ) -> None:
        now_ms = int(time.time() * 1000)
        with self._connect() as conn:
            conn.execute(
                """
                UPDATE aggregation_runs
                SET finished_at_ms = ?,
                    status = ?,
                    news_upserted = ?,
                    snapshots_saved = ?,
                    alerts_upserted = ?,
                    alerts_expired = ?,
                    firestore_synced = ?,
                    error_summary = ?
                WHERE run_id = ?
                """,
                (
                    now_ms,
                    status,
                    news_upserted,
                    snapshots_saved,
                    alerts_upserted,
                    alerts_expired,
                    1 if firestore_synced else 0,
                    error_summary[:1000] if error_summary else None,
                    run_id,
                ),
            )

    def upsert_news_articles(self, articles: Iterable[NewsArticleRecord]) -> int:
        count = 0
        with self._connect() as conn:
            for item in articles:
                conn.execute(
                    """
                    INSERT INTO news_articles (
                        id, content_hash, region_code, title, title_or, summary, content,
                        category, source, author, source_url, image_url, is_breaking,
                        priority, published_at_ms
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(content_hash) DO UPDATE SET
                        title = excluded.title,
                        title_or = COALESCE(excluded.title_or, news_articles.title_or),
                        summary = excluded.summary,
                        content = excluded.content,
                        category = excluded.category,
                        is_breaking = excluded.is_breaking,
                        priority = excluded.priority,
                        updated_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now')
                    """,
                    (
                        item.id,
                        item.content_hash,
                        item.region_code,
                        item.title[:300],
                        item.title_or[:300] if item.title_or else None,
                        item.summary[:600],
                        item.content[:10000],
                        item.category,
                        item.source[:100],
                        item.author[:100],
                        item.source_url[:1000],
                        item.image_url[:1000] if item.image_url else None,
                        1 if item.is_breaking else 0,
                        max(1, min(5, item.priority)),
                        item.published_at_ms,
                    ),
                )
                count += 1
        return count

    def insert_weather_snapshots(self, snapshots: Iterable[WeatherSnapshotRecord]) -> int:
        count = 0
        with self._connect() as conn:
            for snap in snapshots:
                conn.execute(
                    """
                    INSERT OR REPLACE INTO weather_snapshots (
                        id, region_code, provider, temperature_c, feels_like_c,
                        humidity_pct, wind_speed_kmh, wind_gust_kmh, pressure_hpa,
                        rain_mm_3h, weather_code, condition_summary, flood_risk_level,
                        tide_advisory, observed_at_ms
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """,
                    (
                        snap.id,
                        snap.region_code,
                        snap.provider[:60],
                        snap.temperature_c,
                        snap.feels_like_c,
                        max(0, min(100, snap.humidity_pct)),
                        max(0.0, snap.wind_speed_kmh),
                        snap.wind_gust_kmh,
                        snap.pressure_hpa,
                        max(0.0, snap.rain_mm_3h),
                        snap.weather_code[:40],
                        snap.condition_summary[:200],
                        snap.flood_risk_level,
                        snap.tide_advisory[:300] if snap.tide_advisory else None,
                        snap.observed_at_ms,
                    ),
                )
                count += 1
        return count

    def upsert_weather_alerts(self, alerts: Iterable[WeatherAlertRecord]) -> int:
        count = 0
        with self._connect() as conn:
            for alert in alerts:
                blocks_json = json.dumps(alert.affected_blocks[:20], ensure_ascii=False)
                conn.execute(
                    """
                    INSERT INTO weather_alerts (
                        id, alert_hash, region_code, event_type, severity, headline,
                        headline_or, description, affected_blocks, source,
                        starts_at_ms, expires_at_ms, is_active
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    ON CONFLICT(alert_hash) DO UPDATE SET
                        severity = excluded.severity,
                        headline = excluded.headline,
                        headline_or = COALESCE(excluded.headline_or, weather_alerts.headline_or),
                        description = excluded.description,
                        affected_blocks = excluded.affected_blocks,
                        expires_at_ms = excluded.expires_at_ms,
                        is_active = excluded.is_active,
                        updated_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now')
                    """,
                    (
                        alert.id,
                        alert.alert_hash,
                        alert.region_code,
                        alert.event_type,
                        alert.severity,
                        alert.headline[:240],
                        alert.headline_or[:240] if alert.headline_or else None,
                        alert.description[:2000],
                        blocks_json,
                        alert.source[:100],
                        alert.starts_at_ms,
                        alert.expires_at_ms,
                        1 if alert.is_active else 0,
                    ),
                )
                count += 1
        return count

    def deactivate_expired_alerts(self, now_ms: Optional[int] = None) -> int:
        cutoff = now_ms if now_ms is not None else int(time.time() * 1000)
        with self._connect() as conn:
            cursor = conn.execute(
                """
                UPDATE weather_alerts
                SET is_active = 0,
                    updated_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now')
                WHERE is_active = 1 AND expires_at_ms < ?
                """,
                (cutoff,),
            )
            return cursor.rowcount

    def get_latest_news(
        self,
        category: Optional[str] = None,
        limit: int = 25,
    ) -> List[Dict[str, Any]]:
        safe_limit = max(1, min(100, limit))
        with self._connect() as conn:
            if category:
                rows = conn.execute(
                    """
                    SELECT id, region_code, title, title_or, summary, content, category,
                           source, author, source_url, image_url, is_breaking, priority, published_at_ms
                    FROM news_articles
                    WHERE category = ?
                    ORDER BY is_breaking DESC, published_at_ms DESC
                    LIMIT ?
                    """,
                    (category, safe_limit),
                ).fetchall()
            else:
                rows = conn.execute(
                    """
                    SELECT id, region_code, title, title_or, summary, content, category,
                           source, author, source_url, image_url, is_breaking, priority, published_at_ms
                    FROM news_articles
                    ORDER BY is_breaking DESC, published_at_ms DESC
                    LIMIT ?
                    """,
                    (safe_limit,),
                ).fetchall()
        return [dict(row) for row in rows]

    def get_active_weather_alerts(self, severity: Optional[str] = None) -> List[Dict[str, Any]]:
        with self._connect() as conn:
            if severity:
                rows = conn.execute(
                    """
                    SELECT id, region_code, event_type, severity, headline, headline_or,
                           description, affected_blocks, source, starts_at_ms, expires_at_ms
                    FROM weather_alerts
                    WHERE is_active = 1 AND severity = ?
                    ORDER BY starts_at_ms DESC
                    """,
                    (severity,),
                ).fetchall()
            else:
                rows = conn.execute(
                    """
                    SELECT id, region_code, event_type, severity, headline, headline_or,
                           description, affected_blocks, source, starts_at_ms, expires_at_ms
                    FROM weather_alerts
                    WHERE is_active = 1
                    ORDER BY starts_at_ms DESC
                    """,
                ).fetchall()
        results: List[Dict[str, Any]] = []
        for row in rows:
            item = dict(row)
            item["affected_blocks"] = json.loads(item["affected_blocks"])
            results.append(item)
        return results

    def get_latest_weather_snapshots(self) -> List[Dict[str, Any]]:
        with self._connect() as conn:
            rows = conn.execute(
                """
                SELECT ws.id, ws.region_code, r.name_en, r.name_or, ws.provider,
                       ws.temperature_c, ws.feels_like_c, ws.humidity_pct,
                       ws.wind_speed_kmh, ws.rain_mm_3h, ws.weather_code,
                       ws.condition_summary, ws.flood_risk_level, ws.tide_advisory,
                       ws.observed_at_ms
                FROM weather_snapshots ws
                INNER JOIN regions r ON r.region_code = ws.region_code
                INNER JOIN (
                    SELECT region_code, MAX(observed_at_ms) AS max_obs
                    FROM weather_snapshots
                    GROUP BY region_code
                ) latest ON latest.region_code = ws.region_code
                        AND latest.max_obs = ws.observed_at_ms
                ORDER BY ws.region_code ASC
                """
            ).fetchall()
        return [dict(row) for row in rows]

    def get_recent_runs(self, limit: int = 10) -> List[Dict[str, Any]]:
        safe_limit = max(1, min(50, limit))
        with self._connect() as conn:
            rows = conn.execute(
                """
                SELECT run_id, trigger_type, started_at_ms, finished_at_ms, status,
                       news_upserted, snapshots_saved, alerts_upserted, alerts_expired,
                       firestore_synced, error_summary
                FROM aggregation_runs
                ORDER BY started_at_ms DESC
                LIMIT ?
                """,
                (safe_limit,),
            ).fetchall()
        return [dict(row) for row in rows]

    @staticmethod
    def _infer_topic_category(topic_text: str) -> str:
        lowered = topic_text.lower()
        if any(w in lowered for w in ("cyclone", "flood", "rain", "weather", "tide", "heat", "subarnarekha", "budhabalanga", "imd")):
            return "Weather"
        if any(w in lowered for w in ("drdo", "missile", "itr", "defense", "defence", "chandipur range")):
            return "Defense"
        if any(w in lowered for w in ("temple", "khirachora", "panchalingeswar", "chandaneswar", "festival", "darshan")):
            return "Heritage"
        if any(w in lowered for w in ("beach", "chandipur", "talasari", "kuldiha", "tourism", "safari")):
            return "Tourism"
        if any(w in lowered for w in ("fish", "balaramgadi", "mandi", "paddy", "farmer", "market", "motiganj")):
            return "Agriculture"
        if any(w in lowered for w in ("train", "bus", "road", "hospital", "municipality", "water", "power", "civic")):
            return "Civic"
        return "Local"

    @staticmethod
    def _resolve_search_date(search_date: Optional[str] = None) -> dt.date:
        if search_date:
            try:
                return dt.date.fromisoformat(search_date.strip())
            except ValueError as exc:
                raise ValueError("search_date must be YYYY-MM-DD") from exc
        return dt.datetime.now(dt.timezone.utc).date()

    def record_news_topic_search(
        self,
        topic: str,
        category: Optional[str] = None,
        search_date: Optional[str] = None,
    ) -> Dict[str, Any]:
        cleaned = " ".join((topic or "").strip().split())
        if not cleaned:
            raise ValueError("Search topic must not be empty")
        if len(cleaned) > 80:
            cleaned = cleaned[:80].strip()

        topic_key = cleaned.lower()
        allowed_categories = {"Local", "Weather", "Defense", "Civic", "Tourism", "Heritage", "Agriculture"}
        resolved_category = category if category in allowed_categories else self._infer_topic_category(cleaned)
        now_ms = int(time.time() * 1000)
        day_iso = self._resolve_search_date(search_date).isoformat()

        with self._connect() as conn:
            conn.execute(
                """
                INSERT INTO news_topic_searches (
                    topic_key, topic_label, category, search_count, last_searched_at_ms
                ) VALUES (?, ?, ?, 1, ?)
                ON CONFLICT(topic_key) DO UPDATE SET
                    topic_label = excluded.topic_label,
                    category = excluded.category,
                    search_count = news_topic_searches.search_count + 1,
                    last_searched_at_ms = excluded.last_searched_at_ms,
                    updated_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now')
                """,
                (topic_key, cleaned, resolved_category, now_ms),
            )
            conn.execute(
                """
                INSERT INTO news_topic_daily_counts (
                    topic_key, search_date, search_count
                ) VALUES (?, ?, 1)
                ON CONFLICT(topic_key, search_date) DO UPDATE SET
                    search_count = news_topic_daily_counts.search_count + 1,
                    updated_at = strftime('%Y-%m-%dT%H:%M:%SZ', 'now')
                """,
                (topic_key, day_iso),
            )
            row = conn.execute(
                """
                SELECT topic_key, topic_label, category, search_count, last_searched_at_ms
                FROM news_topic_searches
                WHERE topic_key = ?
                """,
                (topic_key,),
            ).fetchone()
        return dict(row) if row is not None else {}

    def get_news_topic_trends(
        self,
        topic_key: str,
        days: int = 7,
        end_date: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        """Return an ordered N-day daily search frequency series (zero-filled) for a Balasore topic."""
        cleaned_key = " ".join((topic_key or "").strip().split()).lower()
        if not cleaned_key:
            raise ValueError("topic_key must not be empty")
        safe_days = max(1, min(30, int(days)))
        end_day = self._resolve_search_date(end_date)
        start_day = end_day - dt.timedelta(days=safe_days - 1)

        with self._connect() as conn:
            rows = conn.execute(
                """
                SELECT search_date, search_count
                FROM news_topic_daily_counts
                WHERE topic_key = ?
                  AND search_date BETWEEN ? AND ?
                ORDER BY search_date ASC
                """,
                (cleaned_key, start_day.isoformat(), end_day.isoformat()),
            ).fetchall()

        counts_by_day = {row["search_date"]: int(row["search_count"]) for row in rows}
        series: List[Dict[str, Any]] = []
        for offset in range(safe_days):
            day_obj = start_day + dt.timedelta(days=offset)
            iso_str = day_obj.isoformat()
            series.append(
                {
                    "date": iso_str,
                    "label": day_obj.strftime("%b %d"),
                    "searches": counts_by_day.get(iso_str, 0),
                }
            )
        return series

    def get_ranked_news_topics(self, limit: int = 10) -> List[Dict[str, Any]]:
        safe_limit = max(1, min(50, limit))
        end_day = dt.datetime.now(dt.timezone.utc).date()
        start_day = end_day - dt.timedelta(days=6)
        with self._connect() as conn:
            rows = conn.execute(
                """
                SELECT
                    t.topic_key,
                    t.topic_label,
                    t.category,
                    t.search_count,
                    t.last_searched_at_ms,
                    (
                        SELECT COUNT(*)
                        FROM news_articles n
                        WHERE lower(n.title) LIKE '%' || t.topic_key || '%'
                           OR lower(n.summary) LIKE '%' || t.topic_key || '%'
                    ) AS matching_articles
                FROM news_topic_searches t
                ORDER BY t.search_count DESC, t.last_searched_at_ms DESC
                LIMIT ?
                """,
                (safe_limit,),
            ).fetchall()

            daily_rows = conn.execute(
                """
                SELECT topic_key, search_date, search_count
                FROM news_topic_daily_counts
                WHERE search_date BETWEEN ? AND ?
                ORDER BY search_date ASC
                """,
                (start_day.isoformat(), end_day.isoformat()),
            ).fetchall()

        daily_map: Dict[str, Dict[str, int]] = {}
        for drow in daily_rows:
            daily_map.setdefault(drow["topic_key"], {})[drow["search_date"]] = int(drow["search_count"])

        ranked: List[Dict[str, Any]] = []
        for idx, row in enumerate(rows, start=1):
            item = dict(row)
            item["rank"] = idx
            item["daily_counts"] = daily_map.get(item["topic_key"], {})
            ranked.append(item)
        return ranked

    def delete_news_topic_search(self, topic_key: str) -> bool:
        cleaned_key = " ".join((topic_key or "").strip().split()).lower()
        if not cleaned_key:
            return False
        with self._connect() as conn:
            cursor = conn.execute(
                "DELETE FROM news_topic_searches WHERE topic_key = ?",
                (cleaned_key,),
            )
            return cursor.rowcount > 0

    def clear_news_topic_searches(self) -> int:
        with self._connect() as conn:
            cursor = conn.execute("DELETE FROM news_topic_searches")
            return cursor.rowcount

    def get_tourism_hotspots(
        self,
        category: Optional[str] = None,
        min_lat: Optional[float] = None,
        max_lat: Optional[float] = None,
        min_lon: Optional[float] = None,
        max_lon: Optional[float] = None,
        featured_only: bool = False,
    ) -> List[Dict[str, Any]]:
        """Query Balasore tourism hotspots with optional category and interactive map bounding-box filters."""
        clauses: List[str] = ["1 = 1"]
        params: List[Any] = []

        if category:
            clauses.append("h.category = ?")
            params.append(category)
        if featured_only:
            clauses.append("h.is_featured = 1")
        if None not in (min_lat, max_lat, min_lon, max_lon):
            clauses.append("h.latitude BETWEEN ? AND ? AND h.longitude BETWEEN ? AND ?")
            params.extend([float(min_lat), float(max_lat), float(min_lon), float(max_lon)])

        where_sql = " AND ".join(clauses)
        with self._connect() as conn:
            rows = conn.execute(
                f"""
                SELECT h.spot_id, h.slug, h.region_code, r.name_en AS region_name_en,
                       h.name_en, h.name_or, h.short_description, h.description,
                       h.category, h.hero_image_url, h.latitude, h.longitude,
                       h.address, h.distance_from_bls_km, h.best_season,
                       h.visiting_hours, h.entry_fee_inr, h.rating,
                       h.map_marker_color, h.is_featured
                FROM tourism_hotspots h
                INNER JOIN regions r ON r.region_code = h.region_code
                WHERE {where_sql}
                ORDER BY h.is_featured DESC, h.rating DESC, h.name_en ASC
                """,
                tuple(params),
            ).fetchall()

            image_rows = conn.execute(
                """
                SELECT image_id, spot_id, image_url, caption_en, caption_or,
                       credit, display_order, is_primary
                FROM tourism_hotspot_images
                ORDER BY spot_id ASC, display_order ASC
                """
            ).fetchall()

        images_by_spot: Dict[str, List[Dict[str, Any]]] = {}
        for img in image_rows:
            img_dict = dict(img)
            img_dict["is_primary"] = bool(img_dict["is_primary"])
            images_by_spot.setdefault(img_dict["spot_id"], []).append(img_dict)

        results: List[Dict[str, Any]] = []
        for row in rows:
            item = dict(row)
            item["is_featured"] = bool(item["is_featured"])
            item["coordinates"] = {"latitude": item["latitude"], "longitude": item["longitude"]}
            item["images"] = images_by_spot.get(item["spot_id"], [])
            results.append(item)
        return results

    def get_tourism_hotspots_geojson(
        self,
        category: Optional[str] = None,
        min_lat: Optional[float] = None,
        max_lat: Optional[float] = None,
        min_lon: Optional[float] = None,
        max_lon: Optional[float] = None,
    ) -> Dict[str, Any]:
        """Return Balasore tourism hotspots as RFC 7946 GeoJSON FeatureCollection for map layers."""
        hotspots = self.get_tourism_hotspots(
            category=category,
            min_lat=min_lat,
            max_lat=max_lat,
            min_lon=min_lon,
            max_lon=max_lon,
        )
        features = [
            {
                "type": "Feature",
                "id": spot["spot_id"],
                "geometry": {
                    "type": "Point",
                    "coordinates": [spot["longitude"], spot["latitude"]],
                },
                "properties": {
                    "spot_id": spot["spot_id"],
                    "name_en": spot["name_en"],
                    "name_or": spot["name_or"],
                    "short_description": spot["short_description"],
                    "category": spot["category"],
                    "hero_image_url": spot["hero_image_url"],
                    "rating": spot["rating"],
                    "best_season": spot["best_season"],
                    "visiting_hours": spot["visiting_hours"],
                    "map_marker_color": spot["map_marker_color"],
                    "is_featured": spot["is_featured"],
                },
            }
            for spot in hotspots
        ]
        return {"type": "FeatureCollection", "features": features}




class FirestoreSyncAdapter:
    """Optional dual-write adapter syncing aggregated records to the Android app's Cloud Firestore."""

    def __init__(self, service_account_path: str, database_id: str, project_id: str = "") -> None:
        self.service_account_path = service_account_path
        self.database_id = database_id
        self.project_id = project_id
        self._client: Any = None

    def _ensure_client(self) -> Any:
        if self._client is not None:
            return self._client

        try:
            import firebase_admin
            from firebase_admin import credentials, firestore
        except ImportError:
            logger.info("firebase-admin not installed; running in local SQLite-only mode.")
            return None

        if not firebase_admin._apps:
            if self.service_account_path:
                sa_path = Path(self.service_account_path).expanduser().resolve()
                if not sa_path.is_file():
                    logger.warning("Firebase service account file not found; skipping Firestore sync.")
                    return None
                cred = credentials.Certificate(str(sa_path))
                firebase_admin.initialize_app(cred)
            else:
                try:
                    cred = credentials.ApplicationDefault()
                    options = {"projectId": self.project_id} if self.project_id else None
                    firebase_admin.initialize_app(cred, options=options)
                except Exception as exc:
                    logger.info("No Firebase Admin credentials/ADC detected (%s); running in local SQLite mode.", exc)
                    return None

        try:
            self._client = firestore.client(database_id=self.database_id)
        except Exception as exc:
            logger.info("Firestore client unavailable (%s); continuing with local SQLite database.", exc)
            return None
        return self._client

    def sync_all(
        self,
        articles: List[NewsArticleRecord],
        snapshots: List[WeatherSnapshotRecord],
        alerts: List[WeatherAlertRecord],
    ) -> bool:
        client = self._ensure_client()
        if client is None:
            return False

        batch = client.batch()
        op_count = 0

        for article in articles[:100]:
            doc_ref = client.collection("news").document(article.id)
            payload: Dict[str, Any] = {
                "id": article.id,
                "title": article.title[:300],
                "summary": article.summary[:600],
                "content": article.content[:10000],
                "category": article.category[:50],
                "source": article.source[:100],
                "isBreaking": bool(article.is_breaking),
                "timestamp": int(article.published_at_ms),
                "author": article.author[:100],
                "priority": int(article.priority),
            }
            if article.title_or:
                payload["titleOr"] = article.title_or[:300]
            if article.image_url:
                payload["imageUrl"] = article.image_url[:1000]
            batch.set(doc_ref, payload, merge=True)
            op_count += 1

        for snap in snapshots:
            doc_ref = client.collection("weather_snapshots").document(snap.region_code)
            payload = {
                "regionCode": snap.region_code,
                "provider": snap.provider[:60],
                "temperatureC": float(snap.temperature_c),
                "feelsLikeC": float(snap.feels_like_c),
                "humidityPct": int(snap.humidity_pct),
                "windSpeedKmh": float(snap.wind_speed_kmh),
                "rainMm3h": float(snap.rain_mm_3h),
                "weatherCode": snap.weather_code[:40],
                "conditionSummary": snap.condition_summary[:200],
                "floodRiskLevel": snap.flood_risk_level,
                "observedAtMs": int(snap.observed_at_ms),
            }
            if snap.tide_advisory:
                payload["tideAdvisory"] = snap.tide_advisory[:300]
            batch.set(doc_ref, payload, merge=True)
            op_count += 1

        for alert in alerts:
            doc_ref = client.collection("weather_alerts").document(alert.id)
            payload = {
                "alertId": alert.id,
                "regionCode": alert.region_code,
                "eventType": alert.event_type,
                "severity": alert.severity,
                "headline": alert.headline[:240],
                "description": alert.description[:2000],
                "affectedBlocks": alert.affected_blocks[:20],
                "source": alert.source[:100],
                "startsAtMs": int(alert.starts_at_ms),
                "expiresAtMs": int(alert.expires_at_ms),
                "isActive": bool(alert.is_active),
                "updatedAtMs": int(time.time() * 1000),
            }
            if alert.headline_or:
                payload["headlineOr"] = alert.headline_or[:240]
            batch.set(doc_ref, payload, merge=True)
            op_count += 1

        if op_count > 0:
            batch.commit()
            logger.info("Synced %d aggregated documents to Firestore database %s", op_count, self.database_id)
        return True
