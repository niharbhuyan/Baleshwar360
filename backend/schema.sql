-- ============================================================================
-- Baleshwar 360 — Balasore Region Real-Time News & Weather Alert Database Schema
-- Compatible with SQLite 3.35+ (and easily portable to PostgreSQL 14+)
-- ============================================================================

PRAGMA foreign_keys = ON;
PRAGMA journal_mode = WAL;

-- 1. Balasore Administrative Blocks & Coastal Monitoring Zones
CREATE TABLE IF NOT EXISTS regions (
    region_code     TEXT PRIMARY KEY,
    name_en         TEXT NOT NULL,
    name_or         TEXT NOT NULL,
    zone_type       TEXT NOT NULL CHECK (zone_type IN ('URBAN', 'COASTAL', 'RIVER_BASIN', 'BLOCK', 'HILL_FOREST')),
    latitude        REAL NOT NULL CHECK (latitude BETWEEN -90.0 AND 90.0),
    longitude       REAL NOT NULL CHECK (longitude BETWEEN -180.0 AND 180.0),
    river_basin     TEXT,
    created_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now'))
);

-- Seed canonical Balasore district blocks & coastal/river monitoring stations
INSERT OR IGNORE INTO regions (region_code, name_en, name_or, zone_type, latitude, longitude, river_basin) VALUES
    ('BLS_SADAR',     'Balasore Sadar',  'ବାଲେଶ୍ୱର ସଦର',   'URBAN',       21.4934, 86.9335, 'Budhabalanga'),
    ('CHANDIPUR',     'Chandipur Coast', 'ଚାନ୍ଦିପୁର ବେଳାଭୂମି', 'COASTAL',   21.4416, 87.0149, 'Budhabalanga'),
    ('REMUNA',        'Remuna',          'ରେମୁଣା',          'BLOCK',       21.5290, 86.8712, 'Budhabalanga'),
    ('SORO',          'Soro',            'ସୋରୋ',            'BLOCK',       21.2882, 86.6915, 'Kansabansa'),
    ('JALESWAR',      'Jaleswar',        'ଜଳେଶ୍ୱର',         'RIVER_BASIN', 21.7975, 87.2146, 'Subarnarekha'),
    ('BASTA',         'Basta',           'ବସ୍ତା',           'RIVER_BASIN', 21.6897, 87.0642, 'Jalaka'),
    ('NILAGIRI',      'Nilagiri',        'ନୀଳଗିରି',         'HILL_FOREST', 21.4596, 86.7689, 'Sono'),
    ('BHOGRAI',       'Bhograi',         'ଭୋଗରାଇ',          'COASTAL',     21.7510, 87.3865, 'Subarnarekha'),
    ('BALIAPAL',      'Baliapal',        'ବାଲିଆପାଳ',        'COASTAL',     21.6486, 87.2836, 'Subarnarekha'),
    ('BALARAMAGADI',  'Balaramgadi',     'ବଳରାମଗଡ଼ି',        'COASTAL',     21.4712, 87.0531, 'Budhabalanga');

-- 2. Aggregated Real-Time Balasore News Articles
CREATE TABLE IF NOT EXISTS news_articles (
    id              TEXT PRIMARY KEY,
    content_hash    TEXT NOT NULL UNIQUE,
    region_code     TEXT NOT NULL DEFAULT 'BLS_SADAR' REFERENCES regions(region_code),
    title           TEXT NOT NULL CHECK (length(trim(title)) > 0 AND length(title) <= 300),
    title_or        TEXT,
    summary         TEXT NOT NULL CHECK (length(summary) <= 600),
    content         TEXT NOT NULL CHECK (length(content) <= 10000),
    category        TEXT NOT NULL CHECK (category IN ('Local', 'Weather', 'Defense', 'Civic', 'Tourism', 'Heritage', 'Agriculture')),
    source          TEXT NOT NULL CHECK (length(source) <= 100),
    author          TEXT NOT NULL DEFAULT 'Balasore News Desk',
    source_url      TEXT NOT NULL CHECK (source_url LIKE 'https://%' OR source_url LIKE 'http://%'),
    image_url       TEXT,
    is_breaking     INTEGER NOT NULL DEFAULT 0 CHECK (is_breaking IN (0, 1)),
    priority        INTEGER NOT NULL DEFAULT 3 CHECK (priority BETWEEN 1 AND 5),
    published_at_ms INTEGER NOT NULL,
    fetched_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    updated_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now'))
);

CREATE INDEX IF NOT EXISTS idx_news_published ON news_articles (published_at_ms DESC);
CREATE INDEX IF NOT EXISTS idx_news_category_breaking ON news_articles (category, is_breaking, published_at_ms DESC);
CREATE INDEX IF NOT EXISTS idx_news_region ON news_articles (region_code, published_at_ms DESC);

-- 3. Balasore District Weather Snapshots (Hourly / Daily Telemetry)
CREATE TABLE IF NOT EXISTS weather_snapshots (
    id                TEXT PRIMARY KEY,
    region_code       TEXT NOT NULL REFERENCES regions(region_code),
    provider          TEXT NOT NULL,
    temperature_c     REAL NOT NULL,
    feels_like_c      REAL NOT NULL,
    humidity_pct      INTEGER NOT NULL CHECK (humidity_pct BETWEEN 0 AND 100),
    wind_speed_kmh    REAL NOT NULL CHECK (wind_speed_kmh >= 0.0),
    wind_gust_kmh     REAL,
    pressure_hpa      INTEGER,
    rain_mm_3h        REAL NOT NULL DEFAULT 0.0 CHECK (rain_mm_3h >= 0.0),
    weather_code      TEXT NOT NULL,
    condition_summary TEXT NOT NULL,
    flood_risk_level  TEXT NOT NULL DEFAULT 'NORMAL' CHECK (flood_risk_level IN ('NORMAL', 'WATCH', 'WARNING', 'DANGER')),
    tide_advisory     TEXT,
    observed_at_ms    INTEGER NOT NULL,
    created_at        TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now'))
);

CREATE INDEX IF NOT EXISTS idx_weather_region_observed ON weather_snapshots (region_code, observed_at_ms DESC);

-- 4. Active & Historical Severe Weather Alerts for Balasore
CREATE TABLE IF NOT EXISTS weather_alerts (
    id              TEXT PRIMARY KEY,
    alert_hash      TEXT NOT NULL UNIQUE,
    region_code     TEXT NOT NULL DEFAULT 'BLS_SADAR' REFERENCES regions(region_code),
    event_type      TEXT NOT NULL CHECK (event_type IN ('CYCLONE', 'FLOOD', 'HEATWAVE', 'HEAVY_RAIN', 'THUNDERSTORM', 'HIGH_TIDE', 'COASTAL_WIND', 'ADVISORY')),
    severity        TEXT NOT NULL CHECK (severity IN ('INFO', 'ADVISORY', 'WATCH', 'WARNING', 'EMERGENCY')),
    headline        TEXT NOT NULL CHECK (length(trim(headline)) > 0 AND length(headline) <= 240),
    headline_or     TEXT,
    description     TEXT NOT NULL CHECK (length(description) <= 2000),
    affected_blocks TEXT NOT NULL, -- JSON array string of affected Balasore blocks
    source          TEXT NOT NULL,
    starts_at_ms    INTEGER NOT NULL,
    expires_at_ms   INTEGER NOT NULL,
    is_active       INTEGER NOT NULL DEFAULT 1 CHECK (is_active IN (0, 1)),
    created_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    updated_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now'))
);

CREATE INDEX IF NOT EXISTS idx_alerts_active_severity ON weather_alerts (is_active, severity, expires_at_ms DESC);
CREATE INDEX IF NOT EXISTS idx_alerts_region ON weather_alerts (region_code, starts_at_ms DESC);

-- 5. Daily Background Aggregation Run Audit Log
CREATE TABLE IF NOT EXISTS aggregation_runs (
    run_id            TEXT PRIMARY KEY,
    trigger_type      TEXT NOT NULL CHECK (trigger_type IN ('SCHEDULED_DAILY', 'MANUAL', 'STARTUP')),
    started_at_ms     INTEGER NOT NULL,
    finished_at_ms    INTEGER,
    status            TEXT NOT NULL CHECK (status IN ('RUNNING', 'SUCCESS', 'PARTIAL', 'FAILED')),
    news_upserted     INTEGER NOT NULL DEFAULT 0,
    snapshots_saved   INTEGER NOT NULL DEFAULT 0,
    alerts_upserted   INTEGER NOT NULL DEFAULT 0,
    alerts_expired    INTEGER NOT NULL DEFAULT 0,
    firestore_synced  INTEGER NOT NULL DEFAULT 0 CHECK (firestore_synced IN (0, 1)),
    error_summary     TEXT
);

CREATE INDEX IF NOT EXISTS idx_runs_started ON aggregation_runs (started_at_ms DESC);

-- 6. User-Searched Balasore News Topics Ranked by Frequency
CREATE TABLE IF NOT EXISTS news_topic_searches (
    topic_key           TEXT PRIMARY KEY,
    topic_label         TEXT NOT NULL CHECK (length(trim(topic_label)) > 0 AND length(topic_label) <= 80),
    category            TEXT NOT NULL DEFAULT 'Local' CHECK (category IN ('Local', 'Weather', 'Defense', 'Civic', 'Tourism', 'Heritage', 'Agriculture')),
    search_count        INTEGER NOT NULL DEFAULT 1 CHECK (search_count >= 1),
    last_searched_at_ms INTEGER NOT NULL,
    created_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    updated_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now'))
);

CREATE INDEX IF NOT EXISTS idx_topic_searches_rank ON news_topic_searches (search_count DESC, last_searched_at_ms DESC);

-- 6b. Daily Search Frequency Buckets per Balasore News Topic (7-Day Trend Line Chart)
CREATE TABLE IF NOT EXISTS news_topic_daily_counts (
    topic_key           TEXT NOT NULL REFERENCES news_topic_searches(topic_key) ON DELETE CASCADE,
    search_date         TEXT NOT NULL CHECK (length(search_date) = 10),
    search_count        INTEGER NOT NULL DEFAULT 1 CHECK (search_count >= 1),
    updated_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    PRIMARY KEY (topic_key, search_date)
);

CREATE INDEX IF NOT EXISTS idx_topic_daily_date ON news_topic_daily_counts (topic_key, search_date DESC);

-- 7. Balasore Tourism Hotspots (Interactive Map & Discovery Module)
CREATE TABLE IF NOT EXISTS tourism_hotspots (
    spot_id           TEXT PRIMARY KEY,
    slug              TEXT NOT NULL UNIQUE,
    region_code       TEXT NOT NULL REFERENCES regions(region_code),
    name_en           TEXT NOT NULL CHECK (length(trim(name_en)) > 0 AND length(name_en) <= 150),
    name_or           TEXT NOT NULL CHECK (length(trim(name_or)) > 0 AND length(name_or) <= 150),
    short_description TEXT NOT NULL CHECK (length(short_description) <= 300),
    description       TEXT NOT NULL CHECK (length(description) <= 4000),
    category          TEXT NOT NULL CHECK (category IN ('BEACH', 'TEMPLE', 'WILDLIFE', 'HERITAGE', 'ESTUARY', 'HILL_SHRINE', 'WATERFALL')),
    hero_image_url    TEXT NOT NULL CHECK (hero_image_url LIKE 'https://%' OR hero_image_url LIKE 'http://%' OR hero_image_url LIKE 'drawable://%'),
    latitude          REAL NOT NULL CHECK (latitude BETWEEN -90.0 AND 90.0),
    longitude         REAL NOT NULL CHECK (longitude BETWEEN -180.0 AND 180.0),
    address           TEXT NOT NULL CHECK (length(address) <= 300),
    distance_from_bls_km REAL CHECK (distance_from_bls_km >= 0.0),
    best_season       TEXT NOT NULL DEFAULT 'October - March',
    visiting_hours    TEXT NOT NULL DEFAULT '06:00 AM - 07:00 PM',
    entry_fee_inr     INTEGER NOT NULL DEFAULT 0 CHECK (entry_fee_inr >= 0),
    rating            REAL NOT NULL DEFAULT 4.5 CHECK (rating BETWEEN 0.0 AND 5.0),
    map_marker_color  TEXT NOT NULL DEFAULT '#0284c7',
    is_featured       INTEGER NOT NULL DEFAULT 0 CHECK (is_featured IN (0, 1)),
    created_at        TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now')),
    updated_at        TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%SZ', 'now'))
);

CREATE INDEX IF NOT EXISTS idx_tourism_geo ON tourism_hotspots (latitude, longitude);
CREATE INDEX IF NOT EXISTS idx_tourism_category_rating ON tourism_hotspots (category, rating DESC);
CREATE INDEX IF NOT EXISTS idx_tourism_region ON tourism_hotspots (region_code, is_featured DESC);

-- 8. Tourism Hotspot Image Gallery
CREATE TABLE IF NOT EXISTS tourism_hotspot_images (
    image_id          TEXT PRIMARY KEY,
    spot_id           TEXT NOT NULL REFERENCES tourism_hotspots(spot_id) ON DELETE CASCADE,
    image_url         TEXT NOT NULL CHECK (image_url LIKE 'https://%' OR image_url LIKE 'http://%' OR image_url LIKE 'drawable://%'),
    caption_en        TEXT NOT NULL CHECK (length(caption_en) <= 240),
    caption_or        TEXT,
    credit            TEXT DEFAULT 'Balasore 360 Curated Media',
    display_order     INTEGER NOT NULL DEFAULT 0 CHECK (display_order >= 0),
    is_primary        INTEGER NOT NULL DEFAULT 0 CHECK (is_primary IN (0, 1))
);

CREATE INDEX IF NOT EXISTS idx_tourism_images_spot ON tourism_hotspot_images (spot_id, display_order ASC);

-- Seed flagship Balasore tourism hotspots with verified coordinates for interactive maps
INSERT OR IGNORE INTO tourism_hotspots (
    spot_id, slug, region_code, name_en, name_or, short_description, description,
    category, hero_image_url, latitude, longitude, address, distance_from_bls_km,
    best_season, visiting_hours, entry_fee_inr, rating, map_marker_color, is_featured
) VALUES
    (
        'spot_chandipur_beach',
        'chandipur-vanishing-beach',
        'CHANDIPUR',
        'Chandipur Beach (Vanishing Sea)',
        'ଚାନ୍ଦିପୁର ବେଳାଭୂମି',
        'Iconic Bay of Bengal beach where the sea recedes up to 5 km twice daily during low tide.',
        'Chandipur-on-Sea is famous worldwide for its vanishing tide phenomenon, exposing miles of flat coastal seabed inhabited by red ghost crabs and endangered horseshoe crabs. Visitors can walk along the recessed seafloor safely during low-tide windows.',
        'BEACH',
        'drawable://hero_chandipur_beach',
        21.4416,
        87.0149,
        'Chandipur Sea Beach, Balasore, Odisha 756025',
        16.0,
        'October - March',
        'Open 24 Hours (Safe Walk: Low Tide Window)',
        0,
        4.8,
        '#0284c7',
        1
    ),
    (
        'spot_remuna_khirachora',
        'remuna-khirachora-gopinatha-temple',
        'REMUNA',
        'Khirachora Gopinatha Temple, Remuna',
        'କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର, ରେମୁଣା',
        'Historic Vaishnavite shrine in Remuna renowned for its sacred Amruta Keli condensed-milk bhog.',
        'Built in the 13th century under King Langula Narasingha Deva, the Khirachora Gopinatha shrine in Remuna holds deep spiritual significance in Gaudiya Vaishnavism and is celebrated across Odisha for its evening Amruta Keli khira prasad.',
        'TEMPLE',
        'drawable://hero_remuna_temple',
        21.5290,
        86.8712,
        'Remuna Town, Balasore District, Odisha 756019',
        9.0,
        'Year-round (Peak: Kartika & Chandan Yatra)',
        '05:30 AM - 01:00 PM, 04:00 PM - 09:00 PM',
        0,
        4.9,
        '#d97706',
        1
    ),
    (
        'spot_panchalingeswar',
        'panchalingeswar-hill-shrine',
        'NILAGIRI',
        'Panchalingeswar Temple',
        'ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପୀଠ',
        'Five perennial spring-washed Shiva Lingas nestled in the Nilagiri hills beside Kuldiha forests.',
        'Perched atop the scenic Nilagiri hillocks, devotees lie flat on smooth rock beside a crystal-clear mountain stream to touch the five sacred Shiva Lingas submerged in flowing spring water.',
        'HILL_SHRINE',
        'drawable://hero_panchalingeswar',
        21.4184,
        86.7266,
        'Panchalingeswar, Nilagiri, Balasore, Odisha 756040',
        29.0,
        'October - February',
        '06:00 AM - 06:30 PM',
        0,
        4.8,
        '#7c3aed',
        1
    ),
    (
        'spot_kuldiha_sanctuary',
        'kuldiha-wildlife-sanctuary',
        'NILAGIRI',
        'Kuldiha Wildlife Sanctuary',
        'କୁଳଡିହା ଅଭୟାରଣ୍ୟ',
        '272 sq km forested sanctuary home to Asian elephants, gaur, giant squirrels, and hill birds.',
        'Linked to Similipal via the Sukhupada and Nato hills corridor, Kuldiha offers forest watchtowers, eco-cottages at Jadachua and Gohirabhola, and guided jungle safaris.',
        'WILDLIFE',
        'drawable://hero_kuldiha_sanctuary',
        21.4258,
        86.6472,
        'Kuldiha Forest Range, Nilagiri, Balasore, Odisha 756040',
        38.0,
        'November - April',
        '06:00 AM - 04:00 PM (Permit Required)',
        100,
        4.7,
        '#15803d',
        1
    ),
    (
        'spot_talasari_beach',
        'talasari-beach-bhograi',
        'BHOGRAI',
        'Talasari Beach',
        'ତାଳସାରୀ ବେଳାଭୂମି',
        'Tranquil palm-fringed shoreline at the Subarnarekha estuary with calm waters and red-crab sands.',
        'Located at the northern coastal tip of Balasore in Bhograi block, Talasari offers tranquil sunrise views, boat crossings over tidal creeks, and fresh coastal seafood.',
        'BEACH',
        'drawable://hero_talasari_beach',
        21.6083,
        87.4694,
        'Talasari, Bhograi, Balasore, Odisha 756085',
        84.0,
        'October - March',
        'Open 24 Hours',
        0,
        4.6,
        '#0ea5e9',
        1
    ),
    (
        'spot_chandaneswar_temple',
        'chandaneswar-shiva-temple',
        'BHOGRAI',
        'Chandaneswar Temple',
        'ଚନ୍ଦନେଶ୍ୱର ଶିବ ମନ୍ଦିର',
        'Revered Lord Shiva shrine near the Odisha-West Bengal border famous for Maha Bishuba Sankranti.',
        'A major Shaivite pilgrimage center in Bhograi block attracting lakhs of devotees during the annual Chadak Mela in April.',
        'TEMPLE',
        'drawable://hero_chandaneswar_temple',
        21.6305,
        87.4508,
        'Chandaneswar, Bhograi, Balasore, Odisha 756085',
        82.0,
        'Year-round (Peak: April Chadak Mela)',
        '05:00 AM - 09:00 PM',
        0,
        4.7,
        '#ea580c',
        0
    ),
    (
        'spot_emami_jagannath',
        'emami-jagannath-temple-balgopalpur',
        'REMUNA',
        'Emami Jagannath Temple',
        'ଇମାମୀ ଜଗନ୍ନାଥ ମନ୍ଦିର',
        'Monumental Kalinga-style stone Jagannath temple near Remuna crafted by master Odisha sculptors.',
        'Constructed with intricately carved red sandstone modeled on classical Puri and Konark architecture, surrounded by manicured gardens near Balgopalpur.',
        'HERITAGE',
        'drawable://hero_emami_jagannath',
        21.5476,
        86.9061,
        'Balgopalpur, Remuna, Balasore, Odisha 756020',
        12.0,
        'Year-round',
        '06:00 AM - 12:30 PM, 04:00 PM - 08:30 PM',
        0,
        4.8,
        '#b45309',
        1
    ),
    (
        'spot_balaramgadi_estuary',
        'balaramgadi-budhabalanga-estuary',
        'BALARAMAGADI',
        'Balaramgadi Estuary & Fishing Harbor',
        'ବଳରାମଗଡ଼ି ମୁହାଣ',
        'Confluence of the Budhabalanga River and Bay of Bengal with Balasore''s liveliest morning fish harbor.',
        'Just 2 km from Chandipur Beach, Balaramgadi is where the Budhabalanga meets the sea—renowned for sunrise trawler auctions, mangrove views, and horseshoe crab habitats.',
        'ESTUARY',
        'drawable://hero_balaramgadi',
        21.4712,
        87.0531,
        'Balaramgadi Fishing Harbor, Balasore, Odisha 756025',
        18.0,
        'October - March',
        '05:30 AM - 06:00 PM',
        0,
        4.5,
        '#0f766e',
        0
    );

INSERT OR IGNORE INTO tourism_hotspot_images (
    image_id, spot_id, image_url, caption_en, caption_or, display_order, is_primary
) VALUES
    ('img_chandipur_1', 'spot_chandipur_beach', 'drawable://hero_chandipur_beach', 'Low-tide vanishing seabed at Chandipur Beach', 'ଚାନ୍ଦିପୁର ବେଳାଭୂମିର ଭଟ୍ଟା ଦୃଶ୍ୟ', 0, 1),
    ('img_chandipur_2', 'spot_chandipur_beach', 'drawable://chandipur_casuarina_grove', 'Casuarina shoreline & sunset walk trail at Chandipur', 'ଝାଉଁବଣ ଓ ସୂର୍ଯ୍ୟାସ୍ତ ଦୃଶ୍ୟ', 1, 0),
    ('img_remuna_1', 'spot_remuna_khirachora', 'drawable://hero_remuna_temple', 'Khirachora Gopinatha Temple sanctum complex, Remuna', 'ରେମୁଣା କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର', 0, 1),
    ('img_remuna_2', 'spot_remuna_khirachora', 'drawable://remuna_amruta_keli', 'Sacred Amruta Keli khira bhog tradition at Remuna', 'ଅମୃତ କେଳି କ୍ଷୀର ଭୋଗ', 1, 0),
    ('img_panchalingeswar_1', 'spot_panchalingeswar', 'drawable://hero_panchalingeswar', 'Mountain stream flowing over the five Shiva Lingas', 'ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପାହାଡ଼ି ଝରଣା', 0, 1),
    ('img_kuldiha_1', 'spot_kuldiha_sanctuary', 'drawable://hero_kuldiha_sanctuary', 'Lush Sal forest trail inside Kuldiha Wildlife Sanctuary', 'କୁଳଡିହା ଅଭୟାରଣ୍ୟର ଶାଳ ବଣ', 0, 1),
    ('img_talasari_1', 'spot_talasari_beach', 'drawable://hero_talasari_beach', 'Palm-lined tidal creek and golden sands of Talasari', 'ତାଳସାରୀ ବେଳାଭୂମି', 0, 1),
    ('img_emami_1', 'spot_emami_jagannath', 'drawable://hero_emami_jagannath', 'Carved sandstone Kalinga shikhara at Emami Jagannath Temple', 'ଇମାମୀ ଜଗନ୍ନାଥ ମନ୍ଦିରର କାରୁକାର୍ଯ୍ୟ', 0, 1);


