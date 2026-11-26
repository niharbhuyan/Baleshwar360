"""Real-time Balasore news and weather alert aggregation services."""

from __future__ import annotations

import email.utils
import hashlib
import html
import json
import logging
import re
import ssl
import time
import urllib.error
import urllib.parse
import urllib.request
import xml.etree.ElementTree as ET
from dataclasses import dataclass
from typing import Any, Dict, List, Optional, Tuple

from config import (
    ALLOWED_EXTERNAL_HOSTS,
    BALASORE_KEYWORDS,
    BALASORE_STATIONS,
    BackendConfig,
)
from database import (
    BalasoreDatabase,
    FirestoreSyncAdapter,
    NewsArticleRecord,
    WeatherAlertRecord,
    WeatherSnapshotRecord,
)

logger = logging.getLogger("balasore.aggregators")

MAX_HTTP_RESPONSE_BYTES = 2 * 1024 * 1024  # 2 MB cap per external payload
HTML_TAG_RE = re.compile(r"<[^>]+>")
WHITESPACE_RE = re.compile(r"\s+")
VALID_ID_RE = re.compile(r"[^a-zA-Z0-9_\-.]")


def sanitize_text(raw: Optional[str], max_len: int) -> str:
    """Strip HTML tags, unescape entities, collapse whitespace, and enforce length cap."""
    if not raw:
        return ""
    cleaned = HTML_TAG_RE.sub(" ", raw)
    cleaned = html.unescape(cleaned)
    cleaned = WHITESPACE_RE.sub(" ", cleaned).strip()
    return cleaned[:max_len]


def validate_safe_url(url: Optional[str], allow_any_public_host: bool = True) -> Optional[str]:
    """Validate URL scheme (http/https only) and reject local/private hosts."""
    if not url:
        return None
    trimmed = url.strip()
    parsed = urllib.parse.urlparse(trimmed)
    if parsed.scheme not in {"https", "http"}:
        return None
    hostname = (parsed.hostname or "").lower()
    if not hostname or hostname in {"localhost", "127.0.0.1", "0.0.0.0", "::1", "169.254.169.254"}:
        return None
    if hostname.startswith(("10.", "192.168.", "172.16.", "172.17.", "172.18.", "172.19.", "172.2", "172.30.", "172.31.")):
        return None
    if not allow_any_public_host and hostname not in ALLOWED_EXTERNAL_HOSTS:
        return None
    return trimmed[:1000]


def fetch_allowlisted_bytes(url: str, timeout_seconds: int = 15) -> bytes:
    """Perform a hardened HTTPS GET request strictly restricted to allowlisted hosts."""
    parsed = urllib.parse.urlparse(url)
    if parsed.scheme != "https":
        raise ValueError(f"Only HTTPS outbound requests are allowed: {parsed.scheme}")
    hostname = (parsed.hostname or "").lower()
    if hostname not in ALLOWED_EXTERNAL_HOSTS:
        raise ValueError(f"Outbound host is not in allowlist: {hostname}")

    req = urllib.request.Request(
        url,
        headers={
            "User-Agent": "Baleshwar360Aggregator/1.0 (+https://baleshwar360.local)",
            "Accept": "application/json, application/rss+xml, application/xml, text/xml",
        },
        method="GET",
    )
    ssl_ctx = ssl.create_default_context()
    with urllib.request.urlopen(req, timeout=timeout_seconds, context=ssl_ctx) as resp:
        data = resp.read(MAX_HTTP_RESPONSE_BYTES + 1)
        if len(data) > MAX_HTTP_RESPONSE_BYTES:
            raise ValueError("Remote response exceeded 2 MB safety limit")
        return data


def is_balasore_relevant(text: str) -> bool:
    lowered = text.lower()
    return any(kw in lowered for kw in BALASORE_KEYWORDS)


def detect_balasore_region(text: str) -> str:
    lowered = text.lower()
    mapping = (
        ("chandipur", "CHANDIPUR"),
        ("itr ", "CHANDIPUR"),
        ("drdo", "CHANDIPUR"),
        ("balaramgadi", "BALARAMAGADI"),
        ("remuna", "REMUNA"),
        ("khirachora", "REMUNA"),
        ("soro", "SORO"),
        ("jaleswar", "JALESWAR"),
        ("subarnarekha", "JALESWAR"),
        ("basta", "BASTA"),
        ("nilagiri", "NILAGIRI"),
        ("panchalingeswar", "NILAGIRI"),
        ("kuldiha", "NILAGIRI"),
        ("bhograi", "BHOGRAI"),
        ("talasari", "BHOGRAI"),
        ("chandaneswar", "BHOGRAI"),
        ("baliapal", "BALIAPAL"),
    )
    for token, region_code in mapping:
        if token in lowered:
            return region_code
    return "BLS_SADAR"


def classify_news_category(text: str) -> Tuple[str, bool, int]:
    """Return (category, is_breaking, priority) for a Balasore headline/summary."""
    lowered = text.lower()
    if any(w in lowered for w in ("cyclone", "flood", "red alert", "imd", "thunderstorm", "heatwave", "tide", "subarnarekha", "budhabalanga", "heavy rain")):
        is_breaking = any(w in lowered for w in ("red alert", "cyclone", "evacuation", "danger level", "flash flood"))
        return "Weather", is_breaking, 1 if is_breaking else 2
    if any(w in lowered for w in ("drdo", "missile", "itr chandipur", "defense", "defence", "test fire", "coastal advisory")):
        return "Defense", "test" in lowered or "alert" in lowered, 2
    if any(w in lowered for w in ("temple", "khirachora", "panchalingeswar", "chandaneswar", "festival", "rath yatra", "darshan")):
        return "Heritage", False, 3
    if any(w in lowered for w in ("beach", "tourism", "kuldiha", "talasari", "wildlife", "safari", "eco-tourism")):
        return "Tourism", False, 3
    if any(w in lowered for w in ("paddy", "mandi", "farmer", "fisherman", "balaramgadi", "fisheries", "crop")):
        return "Agriculture", False, 3
    if any(w in lowered for w in ("municipality", "road", "drainage", "electricity", "water supply", "collector", "hospital", "railway", "train", "nh-16")):
        return "Civic", False, 2
    return "Local", False, 3


def parse_rss_timestamp_ms(pub_date: Optional[str]) -> int:
    if pub_date:
        try:
            dt = email.utils.parsedate_to_datetime(pub_date)
            if dt is not None:
                return int(dt.timestamp() * 1000)
        except (TypeError, ValueError):
            pass
    return int(time.time() * 1000)


def parse_rss_xml_safely(xml_bytes: bytes, source_label: str, max_items: int = 20) -> List[NewsArticleRecord]:
    """Parse RSS XML while rejecting DTD/entity declarations (XXE defense)."""
    if b"<!DOCTYPE" in xml_bytes or b"<!ENTITY" in xml_bytes:
        raise ValueError("Rejected XML payload containing DTD or ENTITY declarations")

    root = ET.fromstring(xml_bytes)
    items = root.findall(".//item")
    records: List[NewsArticleRecord] = []

    for item in items[:max_items]:
        title = sanitize_text(item.findtext("title"), 300)
        description = sanitize_text(item.findtext("description"), 600)
        link = validate_safe_url(item.findtext("link"))
        pub_date = item.findtext("pubDate")
        source_el = item.find("source")
        source_name = sanitize_text(source_el.text if source_el is not None else source_label, 100) or source_label

        if not title or not link:
            continue

        combined = f"{title} {description}"
        if not is_balasore_relevant(combined):
            continue

        category, is_breaking, priority = classify_news_category(combined)
        region_code = detect_balasore_region(combined)
        content_hash = hashlib.sha256(f"{title.lower()}|{link}".encode("utf-8")).hexdigest()
        article_id = f"bls_news_{content_hash[:16]}"
        summary = description if description else f"Balasore regional update from {source_name}: {title}"
        published_ms = parse_rss_timestamp_ms(pub_date)

        is_odia = any("\u0b00" <= ch <= "\u0b7f" for ch in title)
        records.append(
            NewsArticleRecord(
                id=article_id,
                content_hash=content_hash,
                region_code=region_code,
                title=title,
                title_or=title if is_odia else None,
                summary=summary[:600],
                content=f"{summary}\n\nSource: {source_name} ({link})"[:10000],
                category=category,
                source=source_name,
                author=source_name,
                source_url=link,
                image_url=None,
                is_breaking=is_breaking,
                priority=priority,
                published_at_ms=published_ms,
            )
        )
    return records


class BalasoreNewsAggregator:
    """Fetches and normalizes live Balasore news from allowlisted RSS and optional NewsAPI."""

    def __init__(self, config: BackendConfig) -> None:
        self.config = config

    def fetch_articles(self) -> Tuple[List[NewsArticleRecord], List[str]]:
        articles: Dict[str, NewsArticleRecord] = {}
        errors: List[str] = []

        for source_label, feed_url in self.config.rss_feeds:
            try:
                raw_xml = fetch_allowlisted_bytes(feed_url, timeout_seconds=self.config.http_timeout_seconds)
                parsed = parse_rss_xml_safely(raw_xml, source_label, max_items=self.config.max_news_per_feed)
                for record in parsed:
                    articles[record.content_hash] = record
            except Exception as exc:
                msg = f"RSS fetch failed ({source_label}): {exc}"
                logger.warning(msg)
                errors.append(msg)

        if self.config.news_api_key:
            try:
                for record in self._fetch_newsapi_balasore():
                    articles[record.content_hash] = record
            except Exception as exc:
                msg = f"NewsAPI fetch failed: {exc}"
                logger.warning(msg)
                errors.append(msg)

        return list(articles.values()), errors

    def _fetch_newsapi_balasore(self) -> List[NewsArticleRecord]:
        query = urllib.parse.urlencode(
            {
                "q": "Balasore OR Baleshwar OR Chandipur OR Subarnarekha",
                "language": "en",
                "sortBy": "publishedAt",
                "pageSize": 20,
                "apiKey": self.config.news_api_key,
            }
        )
        url = f"https://newsapi.org/v2/everything?{query}"
        payload = json.loads(fetch_allowlisted_bytes(url, timeout_seconds=self.config.http_timeout_seconds).decode("utf-8"))

        records: List[NewsArticleRecord] = []
        for item in payload.get("articles", []):
            title = sanitize_text(item.get("title"), 300)
            summary = sanitize_text(item.get("description") or title, 600)
            content = sanitize_text(item.get("content") or summary, 10000)
            link = validate_safe_url(item.get("url"))
            image_url = validate_safe_url(item.get("urlToImage"))
            source_name = sanitize_text((item.get("source") or {}).get("name") or "NewsAPI", 100)
            author = sanitize_text(item.get("author") or source_name, 100)

            if not title or not link or not is_balasore_relevant(f"{title} {summary}"):
                continue

            category, is_breaking, priority = classify_news_category(f"{title} {summary}")
            region_code = detect_balasore_region(f"{title} {summary}")
            content_hash = hashlib.sha256(f"{title.lower()}|{link}".encode("utf-8")).hexdigest()
            records.append(
                NewsArticleRecord(
                    id=f"bls_news_{content_hash[:16]}",
                    content_hash=content_hash,
                    region_code=region_code,
                    title=title,
                    title_or=None,
                    summary=summary,
                    content=content,
                    category=category,
                    source=source_name,
                    author=author,
                    source_url=link,
                    image_url=image_url,
                    is_breaking=is_breaking,
                    priority=priority,
                    published_at_ms=int(time.time() * 1000),
                )
            )
        return records


def derive_balasore_weather_alerts(snapshot: WeatherSnapshotRecord) -> List[WeatherAlertRecord]:
    """Evaluate Balasore-specific coastal, cyclone, heatwave, and river basin flood thresholds."""
    alerts: List[WeatherAlertRecord] = []
    now_ms = snapshot.observed_at_ms
    expires_ms = now_ms + 12 * 60 * 60 * 1000  # 12-hour default alert validity
    day_bucket = time.strftime("%Y%m%d", time.gmtime(now_ms / 1000.0))

    def make_alert(
        event_type: str,
        severity: str,
        headline: str,
        headline_or: str,
        description: str,
        affected_blocks: List[str],
    ) -> WeatherAlertRecord:
        alert_hash = hashlib.sha256(f"{snapshot.region_code}|{event_type}|{day_bucket}".encode("utf-8")).hexdigest()
        safe_id = VALID_ID_RE.sub("_", f"alert_{snapshot.region_code.lower()}_{event_type.lower()}_{day_bucket}")
        return WeatherAlertRecord(
            id=safe_id[:120],
            alert_hash=alert_hash,
            region_code=snapshot.region_code,
            event_type=event_type,
            severity=severity,
            headline=headline,
            headline_or=headline_or,
            description=description,
            affected_blocks=affected_blocks,
            source=f"{snapshot.provider} / Balasore Weather Engine",
            starts_at_ms=now_ms,
            expires_at_ms=expires_ms,
            is_active=True,
        )

    # 1. Cyclone / Coastal Gale Winds (Bay of Bengal)
    if snapshot.wind_speed_kmh >= 50.0:
        sev = "EMERGENCY" if snapshot.wind_speed_kmh >= 75.0 else "WARNING"
        alerts.append(
            make_alert(
                event_type="CYCLONE" if snapshot.wind_speed_kmh >= 62.0 else "COASTAL_WIND",
                severity=sev,
                headline=f"High Coastal Wind Alert ({snapshot.wind_speed_kmh:.0f} km/h) across Balasore Coast",
                headline_or=f"ବାଲେଶ୍ୱର ଉପକୂଳରେ ପ୍ରବଳ ପବନ ସତର୍କତା ({snapshot.wind_speed_kmh:.0f} କିମି/ଘଣ୍ଟା)",
                description=(
                    f"Sustained winds of {snapshot.wind_speed_kmh:.1f} km/h recorded at {snapshot.region_code}. "
                    "Fishermen at Balaramgadi, Chandipur, and Talasari are advised not to venture into the Bay of Bengal."
                ),
                affected_blocks=["Chandipur", "Balaramgadi", "Bhograi", "Baliapal", "Balasore Sadar"],
            )
        )

    # 2. Heavy Monsoon Rainfall & Subarnarekha / Budhabalanga Flood Risk
    if snapshot.rain_mm_3h >= 30.0:
        sev = "EMERGENCY" if snapshot.rain_mm_3h >= 65.0 else "WARNING"
        alerts.append(
            make_alert(
                event_type="FLOOD" if snapshot.rain_mm_3h >= 50.0 else "HEAVY_RAIN",
                severity=sev,
                headline=f"Heavy Rainfall & River Basin Alert ({snapshot.rain_mm_3h:.1f} mm/3h) in Balasore",
                headline_or="ବାଲେଶ୍ୱରରେ ପ୍ରବଳ ବର୍ଷା ଓ ବନ୍ୟା ସତର୍କତା",
                description=(
                    f"Accumulated rainfall of {snapshot.rain_mm_3h:.1f} mm detected. Monitor Subarnarekha (Rajghat) "
                    "and Budhabalanga (NH-5) water levels; low-lying wards in Jaleswar, Basta, and Remuna should stay alert."
                ),
                affected_blocks=["Jaleswar", "Basta", "Bhograi", "Baliapal", "Remuna", "Balasore Sadar"],
            )
        )

    # 3. Severe Odisha Summer Heatwave
    if snapshot.temperature_c >= 40.0 or snapshot.feels_like_c >= 44.0:
        sev = "WARNING" if snapshot.temperature_c >= 42.0 else "WATCH"
        alerts.append(
            make_alert(
                event_type="HEATWAVE",
                severity=sev,
                headline=f"Balasore Heatwave Advisory: {snapshot.temperature_c:.1f}°C (Feels like {snapshot.feels_like_c:.1f}°C)",
                headline_or=f"ବାଲେଶ୍ୱର ଗ୍ରୀଷ୍ମ ପ୍ରବାହ ସତର୍କତା: {snapshot.temperature_c:.1f}°C",
                description=(
                    "Peak heat stress expected between 11:00 AM and 3:30 PM across Soro, Nilagiri, Remuna, and Balasore Town. "
                    "Avoid direct sun exposure and stay hydrated."
                ),
                affected_blocks=["Balasore Sadar", "Remuna", "Soro", "Nilagiri", "Basta"],
            )
        )

    # 4. Thunderstorm / Kalbaishakhi Lightning Watch
    if "thunder" in snapshot.condition_summary.lower() or snapshot.weather_code in {"95", "96", "99", "Thunderstorm"}:
        alerts.append(
            make_alert(
                event_type="THUNDERSTORM",
                severity="WARNING",
                headline="Thunderstorm & Lightning Watch for Balasore District",
                headline_or="ବାଲେଶ୍ୱର ଜିଲ୍ଲାରେ ଘଡ଼ଘଡ଼ି ଓ ବଜ୍ରପାତ ସତର୍କତା",
                description=(
                    "Active convective thunderstorm cells detected over North Odisha. Seek indoor shelter away from open fields and trees."
                ),
                affected_blocks=["Balasore Sadar", "Remuna", "Soro", "Nilagiri", "Jaleswar"],
            )
        )

    return alerts


def compute_flood_risk(rain_mm_3h: float) -> str:
    if rain_mm_3h >= 65.0:
        return "DANGER"
    if rain_mm_3h >= 35.0:
        return "WARNING"
    if rain_mm_3h >= 15.0:
        return "WATCH"
    return "NORMAL"


class BalasoreWeatherAggregator:
    """Collects live Balasore weather observations and severe weather alerts."""

    WMO_CODE_LABELS: Dict[int, str] = {
        0: "Clear Sky",
        1: "Mainly Clear",
        2: "Partly Cloudy",
        3: "Overcast",
        45: "Coastal Fog",
        51: "Light Drizzle",
        61: "Slight Rain",
        63: "Moderate Rain",
        65: "Heavy Rain",
        80: "Rain Showers",
        82: "Violent Rain Showers",
        95: "Thunderstorm",
        96: "Thunderstorm with Hail",
        99: "Severe Thunderstorm",
    }

    def __init__(self, config: BackendConfig) -> None:
        self.config = config

    def fetch_weather_and_alerts(self) -> Tuple[List[WeatherSnapshotRecord], List[WeatherAlertRecord], List[str]]:
        snapshots: List[WeatherSnapshotRecord] = []
        alerts: Dict[str, WeatherAlertRecord] = {}
        errors: List[str] = []

        for region_code, (station_name, lat, lon) in BALASORE_STATIONS.items():
            try:
                if self.config.openweathermap_api_key:
                    snap = self._fetch_openweathermap(region_code, lat, lon)
                else:
                    snap = self._fetch_open_meteo(region_code, lat, lon)
                snapshots.append(snap)
                for derived in derive_balasore_weather_alerts(snap):
                    alerts[derived.alert_hash] = derived
            except Exception as exc:
                msg = f"Weather fetch failed ({station_name}): {exc}"
                logger.warning(msg)
                errors.append(msg)

        return snapshots, list(alerts.values()), errors

    def _fetch_openweathermap(self, region_code: str, lat: float, lon: float) -> WeatherSnapshotRecord:
        query = urllib.parse.urlencode(
            {
                "lat": f"{lat:.4f}",
                "lon": f"{lon:.4f}",
                "appid": self.config.openweathermap_api_key,
                "units": "metric",
            }
        )
        url = f"https://api.openweathermap.org/data/2.5/weather?{query}"
        data = json.loads(fetch_allowlisted_bytes(url, timeout_seconds=self.config.http_timeout_seconds).decode("utf-8"))

        main = data.get("main") or {}
        wind = data.get("wind") or {}
        rain = data.get("rain") or {}
        weather_list = data.get("weather") or [{}]
        weather_primary = weather_list[0]

        temp_c = float(main.get("temp", 28.0))
        feels_c = float(main.get("feels_like", temp_c))
        humidity = int(main.get("humidity", 70))
        wind_kmh = float(wind.get("speed", 0.0)) * 3.6
        gust_kmh = float(wind["gust"]) * 3.6 if "gust" in wind else None
        pressure = int(main["pressure"]) if "pressure" in main else None
        rain_3h = float(rain.get("3h", rain.get("1h", 0.0) * 3.0))
        cond = sanitize_text(weather_primary.get("description") or weather_primary.get("main") or "Clear", 200).title()
        w_code = sanitize_text(str(weather_primary.get("main") or weather_primary.get("id") or "Clear"), 40)
        obs_ms = int(data.get("dt", time.time())) * 1000

        tide_note = (
            "Chandipur Bay of Bengal tide active — verify 30-min safe beach walk window."
            if region_code == "CHANDIPUR"
            else None
        )

        return WeatherSnapshotRecord(
            id=f"snap_{region_code.lower()}_{obs_ms}",
            region_code=region_code,
            provider="OpenWeatherMap",
            temperature_c=round(temp_c, 1),
            feels_like_c=round(feels_c, 1),
            humidity_pct=humidity,
            wind_speed_kmh=round(wind_kmh, 1),
            wind_gust_kmh=round(gust_kmh, 1) if gust_kmh is not None else None,
            pressure_hpa=pressure,
            rain_mm_3h=round(rain_3h, 1),
            weather_code=w_code,
            condition_summary=cond,
            flood_risk_level=compute_flood_risk(rain_3h),
            tide_advisory=tide_note,
            observed_at_ms=obs_ms,
        )

    def _fetch_open_meteo(self, region_code: str, lat: float, lon: float) -> WeatherSnapshotRecord:
        query = urllib.parse.urlencode(
            {
                "latitude": f"{lat:.4f}",
                "longitude": f"{lon:.4f}",
                "current": "temperature_2m,relative_humidity_2m,apparent_temperature,precipitation,weather_code,surface_pressure,wind_speed_10m,wind_gusts_10m",
                "timezone": "Asia/Kolkata",
            }
        )
        url = f"https://api.open-meteo.com/v1/forecast?{query}"
        data = json.loads(fetch_allowlisted_bytes(url, timeout_seconds=self.config.http_timeout_seconds).decode("utf-8"))
        current = data.get("current") or {}

        temp_c = float(current.get("temperature_2m", 28.0))
        feels_c = float(current.get("apparent_temperature", temp_c))
        humidity = int(current.get("relative_humidity_2m", 70))
        wind_kmh = float(current.get("wind_speed_10m", 0.0))
        gust_kmh = float(current["wind_gusts_10m"]) if current.get("wind_gusts_10m") is not None else None
        pressure = int(round(float(current["surface_pressure"]))) if current.get("surface_pressure") is not None else None
        rain_3h = float(current.get("precipitation", 0.0)) * 3.0
        code_int = int(current.get("weather_code", 0))
        cond = self.WMO_CODE_LABELS.get(code_int, f"WMO Condition {code_int}")
        obs_ms = int(time.time() * 1000)

        tide_note = (
            "Chandipur intertidal zone active — verify low-tide window before sea-bed walk."
            if region_code == "CHANDIPUR"
            else None
        )

        return WeatherSnapshotRecord(
            id=f"snap_{region_code.lower()}_{obs_ms}",
            region_code=region_code,
            provider="Open-Meteo",
            temperature_c=round(temp_c, 1),
            feels_like_c=round(feels_c, 1),
            humidity_pct=humidity,
            wind_speed_kmh=round(wind_kmh, 1),
            wind_gust_kmh=round(gust_kmh, 1) if gust_kmh is not None else None,
            pressure_hpa=pressure,
            rain_mm_3h=round(rain_3h, 1),
            weather_code=str(code_int),
            condition_summary=cond,
            flood_risk_level=compute_flood_risk(rain_3h),
            tide_advisory=tide_note,
            observed_at_ms=obs_ms,
        )


@dataclass(frozen=True)
class AggregationSummary:
    run_id: str
    status: str
    news_upserted: int
    snapshots_saved: int
    alerts_upserted: int
    alerts_expired: int
    firestore_synced: bool
    errors: List[str]


class BalasoreAggregationService:
    """Orchestrates daily news + weather alert aggregation and persistence."""

    def __init__(
        self,
        config: BackendConfig,
        db: Optional[BalasoreDatabase] = None,
        news_aggregator: Optional[BalasoreNewsAggregator] = None,
        weather_aggregator: Optional[BalasoreWeatherAggregator] = None,
        firestore_sync: Optional[FirestoreSyncAdapter] = None,
    ) -> None:
        self.config = config
        self.db = db or BalasoreDatabase(config.db_path)
        self.news_aggregator = news_aggregator or BalasoreNewsAggregator(config)
        self.weather_aggregator = weather_aggregator or BalasoreWeatherAggregator(config)
        self.firestore_sync = firestore_sync or FirestoreSyncAdapter(
            config.firebase_service_account_path,
            config.firestore_database_id,
        )

    def run_aggregation(self, trigger_type: str = "SCHEDULED_DAILY") -> AggregationSummary:
        run_id = self.db.start_run(trigger_type=trigger_type)
        all_errors: List[str] = []
        news_upserted = 0
        snapshots_saved = 0
        alerts_upserted = 0
        alerts_expired = 0
        firestore_synced = False

        try:
            articles, news_errors = self.news_aggregator.fetch_articles()
            all_errors.extend(news_errors)
            if articles:
                news_upserted = self.db.upsert_news_articles(articles)

            snapshots, alerts, weather_errors = self.weather_aggregator.fetch_weather_and_alerts()
            all_errors.extend(weather_errors)
            if snapshots:
                snapshots_saved = self.db.insert_weather_snapshots(snapshots)
            if alerts:
                alerts_upserted = self.db.upsert_weather_alerts(alerts)

            alerts_expired = self.db.deactivate_expired_alerts()

            try:
                firestore_synced = self.firestore_sync.sync_all(articles, snapshots, alerts)
            except Exception as fs_exc:
                msg = f"Firestore sync skipped/failed: {fs_exc}"
                logger.warning(msg)
                all_errors.append(msg)

            if all_errors and (news_upserted > 0 or snapshots_saved > 0):
                status = "PARTIAL"
            elif all_errors and news_upserted == 0 and snapshots_saved == 0:
                status = "FAILED"
            else:
                status = "SUCCESS"

            self.db.finish_run(
                run_id=run_id,
                status=status,
                news_upserted=news_upserted,
                snapshots_saved=snapshots_saved,
                alerts_upserted=alerts_upserted,
                alerts_expired=alerts_expired,
                firestore_synced=firestore_synced,
                error_summary="; ".join(all_errors) if all_errors else None,
            )
            return AggregationSummary(
                run_id=run_id,
                status=status,
                news_upserted=news_upserted,
                snapshots_saved=snapshots_saved,
                alerts_upserted=alerts_upserted,
                alerts_expired=alerts_expired,
                firestore_synced=firestore_synced,
                errors=all_errors,
            )
        except Exception as exc:
            logger.exception("Unhandled aggregation error")
            self.db.finish_run(
                run_id=run_id,
                status="FAILED",
                news_upserted=news_upserted,
                snapshots_saved=snapshots_saved,
                alerts_upserted=alerts_upserted,
                alerts_expired=alerts_expired,
                firestore_synced=firestore_synced,
                error_summary=str(exc),
            )
            raise
