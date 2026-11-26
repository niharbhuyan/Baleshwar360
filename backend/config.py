"""Configuration and regional constants for the Balasore 360 backend aggregator."""

from __future__ import annotations

import os
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, List, Tuple

BACKEND_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = BACKEND_DIR.parent
DEFAULT_DB_PATH = BACKEND_DIR / "data" / "balasore_realtime.db"
SCHEMA_PATH = BACKEND_DIR / "schema.sql"

# Strict outbound HTTPS host allowlist to prevent SSRF on external fetches.
ALLOWED_EXTERNAL_HOSTS = frozenset(
    {
        "news.google.com",
        "api.openweathermap.org",
        "api.open-meteo.com",
        "newsapi.org",
        "odishatv.in",
        "sambadenglish.com",
        "pragativadi.com",
        "pib.gov.in",
    }
)

# Canonical Balasore district monitoring coordinates
BALASORE_STATIONS: Dict[str, Tuple[str, float, float]] = {
    "BLS_SADAR": ("Balasore Sadar", 21.4934, 86.9335),
    "CHANDIPUR": ("Chandipur Coast", 21.4416, 87.0149),
    "JALESWAR": ("Jaleswar (Subarnarekha)", 21.7975, 87.2146),
    "SORO": ("Soro", 21.2882, 86.6915),
    "NILAGIRI": ("Nilagiri", 21.4596, 86.7689),
}

# Hyper-local Balasore keyword matchers for filtering regional news feeds
BALASORE_KEYWORDS: Tuple[str, ...] = (
    "balasore",
    "baleshwar",
    "chandipur",
    "remuna",
    "soro",
    "jaleswar",
    "basta",
    "nilagiri",
    "bhograi",
    "baliapal",
    "bahanaga",
    "khaira",
    "oupada",
    "simulia",
    "balaramgadi",
    "subarnarekha",
    "budhabalanga",
    "panchalingeswar",
    "khirachora",
    "kuldiha",
    "itr chandipur",
    "drdo chandipur",
    "fakir mohan",
    "talasari",
    "chandaneswar",
    "bhusandheswar",
    "sahadevkhunta",
    "motiganj",
    "ବାଲେଶ୍ୱର",
    "ଚାନ୍ଦିପୁର",
    "ରେମୁଣା",
    "ସୋରୋ",
    "ଜଳେଶ୍ୱର",
)

# Default HTTPS RSS feeds focused on Balasore & coastal North Odisha
DEFAULT_NEWS_RSS_FEEDS: Tuple[Tuple[str, str], ...] = (
    (
        "Google News Balasore",
        "https://news.google.com/rss/search?q=Balasore+OR+Baleshwar+OR+Chandipur+Odisha&hl=en-IN&gl=IN&ceid=IN:en",
    ),
    (
        "Google News Balasore Weather & DRDO",
        "https://news.google.com/rss/search?q=(Balasore+cyclone+OR+Subarnarekha+flood+OR+ITR+Chandipur)&hl=en-IN&gl=IN&ceid=IN:en",
    ),
    (
        "Google News Balasore Odia",
        "https://news.google.com/rss/search?q=%E0%AC%AC%E0%AC%BE%E0%AC%B2%E0%AD%87%E0%AC%B6%E0%AD%8D%E0%AC%B5%E0%AC%B0&hl=or&gl=IN&ceid=IN:or",
    ),
)


def _load_dotenv_if_present() -> None:
    """Load local .env file when available without overriding exported env vars."""
    env_file = PROJECT_ROOT / ".env"
    if not env_file.is_file():
        return
    try:
        for raw_line in env_file.read_text(encoding="utf-8").splitlines():
            line = raw_line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            key = key.strip()
            value = value.strip().strip('"').strip("'")
            if key and key not in os.environ and value and not value.startswith("YOUR_") and not value.startswith("MY_"):
                os.environ[key] = value
    except OSError:
        return


def resolve_db_path(raw_path: str | None) -> Path:
    """Resolve database path safely within the project workspace or explicit absolute data dir."""
    cleaned = (raw_path or "").strip()
    if not cleaned:
        DEFAULT_DB_PATH.parent.mkdir(parents=True, exist_ok=True)
        return DEFAULT_DB_PATH
    candidate = Path(cleaned).expanduser()
    if not candidate.is_absolute():
        candidate = (PROJECT_ROOT / candidate).resolve()
    else:
        candidate = candidate.resolve()
    candidate.parent.mkdir(parents=True, exist_ok=True)
    return candidate


def _discover_firestore_database_id() -> str:
    """Read the provisioned Firestore named database ID automatically from firebase_applet_config.xml."""
    xml_path = PROJECT_ROOT / "app" / "src" / "main" / "res" / "values" / "firebase_applet_config.xml"
    if xml_path.is_file():
        try:
            import re
            match = re.search(r'name="firestore_database_id"[^>]*>([^<]+)<', xml_path.read_text(encoding="utf-8"))
            if match and match.group(1).strip():
                return match.group(1).strip()
        except OSError:
            pass
    return "ai-studio-android-balasore-011eecca-ad37-48d7-9ed9-0ca2c072c765"


def _discover_firebase_project_id() -> str:
    """Read Firebase project_id automatically from app/google-services.json."""
    gs_path = PROJECT_ROOT / "app" / "google-services.json"
    if gs_path.is_file():
        try:
            import json
            data = json.loads(gs_path.read_text(encoding="utf-8"))
            pid = (data.get("project_info") or {}).get("project_id", "").strip()
            if pid and pid != "remixed-project-id":
                return pid
        except (OSError, ValueError):
            pass
    return ""


def resolve_firebase_service_account_path(raw_path: str | None) -> str:
    """Auto-resolve Firebase service account JSON from explicit env or GOOGLE_APPLICATION_CREDENTIALS."""
    candidates = [
        (raw_path or "").strip(),
        os.getenv("GOOGLE_APPLICATION_CREDENTIALS", "").strip(),
    ]
    for item in candidates:
        if item and not item.startswith("YOUR_") and Path(item).expanduser().is_file():
            return str(Path(item).expanduser().resolve())
    return ""


@dataclass(frozen=True)
class BackendConfig:
    db_path: Path
    openweathermap_api_key: str
    news_api_key: str
    firebase_service_account_path: str
    firestore_database_id: str
    firebase_project_id: str = ""
    timezone: str = "Asia/Kolkata"
    daily_hour: int = 6
    daily_minute: int = 0
    http_timeout_seconds: int = 15
    max_news_per_feed: int = 20
    rss_feeds: Tuple[Tuple[str, str], ...] = field(default_factory=lambda: DEFAULT_NEWS_RSS_FEEDS)

    @classmethod
    def from_env(cls) -> "BackendConfig":
        _load_dotenv_if_present()
        owm_key = os.getenv("OPENWEATHERMAP_API_KEY", "").strip()
        if owm_key in {"YOUR_OPENWEATHERMAP_API_KEY", "REPLACE_ME"}:
            owm_key = ""

        news_key = (os.getenv("NEWS_API_KEY") or os.getenv("NEWSAPI_KEY") or "").strip()
        if news_key in {"YOUR_NEWS_API_KEY", "REPLACE_ME"}:
            news_key = ""

        hour = int(os.getenv("DAILY_SYNC_HOUR_IST", "6"))
        minute = int(os.getenv("DAILY_SYNC_MINUTE_IST", "0"))
        hour = max(0, min(23, hour))
        minute = max(0, min(59, minute))

        default_db_id = _discover_firestore_database_id()
        default_project_id = _discover_firebase_project_id()

        return cls(
            db_path=resolve_db_path(os.getenv("BALASORE_DB_PATH")),
            openweathermap_api_key=owm_key,
            news_api_key=news_key,
            firebase_service_account_path=resolve_firebase_service_account_path(
                os.getenv("FIREBASE_SERVICE_ACCOUNT_PATH")
            ),
            firestore_database_id=os.getenv("FIRESTORE_DATABASE_ID", default_db_id).strip() or default_db_id,
            firebase_project_id=os.getenv("FIREBASE_PROJECT_ID", default_project_id).strip() or default_project_id,
            timezone=os.getenv("BALASORE_TIMEZONE", "Asia/Kolkata").strip() or "Asia/Kolkata",
            daily_hour=hour,
            daily_minute=minute,
        )
