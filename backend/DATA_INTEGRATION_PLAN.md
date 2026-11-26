# Balasore 360 — Tourism Schema & Automated Daily Data Integration Plan

## 1. Tourism Module Database Schema

The tourism module is modeled across both **SQLite / PostgreSQL (`backend/schema.sql`)** and **Cloud Firestore (`firebase-blueprint.json` + `firestore.rules`)** to support offline-first caching, interactive map clustering/bounding-box queries, and bilingual (English + Odia) content.

### Relational Tables (`backend/schema.sql`)
1. **`tourism_hotspots`** (Primary Landmark Entity)
   - `spot_id` (`TEXT PRIMARY KEY`), `slug` (`TEXT UNIQUE`), `region_code` (`FK -> regions.region_code`)
   - `name_en`, `name_or`, `short_description`, `description`
   - `category` (`BEACH | TEMPLE | WILDLIFE | HERITAGE | ESTUARY | HILL_SHRINE | WATERFALL`)
   - `hero_image_url` (`https://`, `http://`, or bundled `drawable://`)
   - `latitude` (`REAL`, `-90..90`), `longitude` (`REAL`, `-180..180`), `map_marker_color`, `address`, `distance_from_bls_km`
   - `best_season`, `visiting_hours`, `entry_fee_inr`, `rating` (`0..5`), `is_featured`
   - Spatial & filter indexes: `idx_tourism_geo (latitude, longitude)` and `idx_tourism_category_rating (category, rating DESC)`
2. **`tourism_hotspot_images`** (1-to-Many Media Gallery)
   - `image_id` (`TEXT PRIMARY KEY`), `spot_id` (`FK -> tourism_hotspots.spot_id ON DELETE CASCADE`)
   - `image_url`, `caption_en`, `caption_or`, `credit`, `display_order`, `is_primary`

### Interactive Map API & GeoJSON Output (`backend/scheduler.py`)
- `GET /api/balasore/tourism/hotspots?category=BEACH&min_lat=21.2&max_lat=21.9&min_lon=86.5&max_lon=87.5`
- Add `&format=geojson` to return a standard RFC 7946 `FeatureCollection` directly consumable by Google Maps, MapLibre, or Leaflet.

---

## 2. Reliable APIs for Balasore News & Weather Forecasting

### A. Weather Forecasting & Coastal Severe Alerts
| Priority | Provider | Coverage & Rationale | Auth / Cost |
|---|---|---|---|
| **Primary** | **OpenWeatherMap API** (`api.openweathermap.org/data/2.5/weather` & One Call 3.0) | Reliable real-time temperature, wind gusts, 3-hour precipitation, pressure, and severe weather bulletins across Balasore Sadar (`21.4934, 86.9335`), Chandipur (`21.4416, 87.0149`), Jaleswar (`21.7975, 87.2146`), Soro, and Nilagiri. | `OPENWEATHERMAP_API_KEY` (Free tier: 1,000 calls/day) |
| **Zero-Key Fallback** | **Open-Meteo WMO API** (`api.open-meteo.com/v1/forecast`) | Official WMO meteorological grid with hourly precipitation, gusts, and weather codes—ensures uninterrupted daily updates even when an API key is unavailable or rate-limited. | Keyless / Free for non-commercial |
| **Derived Local Hazard Rules** | **Balasore Alert Engine** (`backend/aggregators.py`) | Evaluates Bay of Bengal gale winds (`>= 50 km/h`), Subarnarekha/Budhabalanga flood rainfall (`>= 30 mm/3h`), Odisha heatwaves (`>= 40°C`), and Kalbaishakhi thunderstorms to emit bilingual Odia/English alerts. | Built-in |

### B. Local Balasore News Feeds
| Priority | Source | Coverage & Rationale | Auth / Cost |
|---|---|---|---|
| **Primary** | **Google News Regional RSS** (`en-IN` & `or` Odia queries) | Aggregates real-time Balasore, Chandipur, ITR DRDO, Subarnarekha flood, and district administration reports from Sambad, OdishaTV, Pragativadi, and national bureaus. | Keyless HTTPS RSS |
| **Optional Supplement** | **NewsAPI / GNews** (`newsapi.org/v2/everything`) | Adds structured full-text articles, author attribution, and verified `urlToImage` thumbnails for Balasore queries. | `NEWS_API_KEY` (Optional) |

---

## 3. Automated Daily Backend Service Architecture

Implemented in `backend/`:
1. **Scheduler (`backend/scheduler.py`)**: Runs `BalasoreDailyScheduler` daily at `06:00 IST` (`Asia/Kolkata`) via `APScheduler` `CronTrigger` (with an automatic zero-dependency Python thread fallback and `--run-once` flag for Linux cron / Cloud Scheduler).
2. **Ingestion & Normalization (`backend/aggregators.py`)**:
   - Enforces a strict outbound HTTPS host allowlist (`ALLOWED_EXTERNAL_HOSTS`) and 2 MB response size cap.
   - Blocks DTD/XXE XML payloads, strips HTML tags, validates `https://`/`http://` URLs, filters strictly for Balasore district keywords/blocks, classifies categories (`Weather`, `Defense`, `Civic`, `Heritage`, `Tourism`, `Agriculture`, `Local`), and deduplicates via SHA-256 hashes.
3. **Dual Persistence (`backend/database.py`)**:
   - Writes atomically to local SQLite (`backend/data/balasore_realtime.db`) with parameterized queries, expires stale alerts (`expires_at_ms < now`), and logs every sync in `aggregation_runs`.
   - Optionally syncs latest news, weather snapshots, and severe alerts to the Android app's Cloud Firestore database (`ai-studio-android-balasore-011eecca-ad37-48d7-9ed9-0ca2c072c765`) when `FIREBASE_SERVICE_ACCOUNT_PATH` is configured.
