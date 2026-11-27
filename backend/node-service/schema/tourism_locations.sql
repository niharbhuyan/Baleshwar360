-- Balasore (Baleshwar) Tourist Locations Relational Database Schema
-- Compatible with PostgreSQL 15+ and SQLite 3.35+
-- Mirrors the Android Room `hotspots` table and Firestore `tourism_hotspots` collection.

CREATE TABLE IF NOT EXISTS balasore_tourist_locations (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(160) NOT NULL,
    odia_name VARCHAR(200) NOT NULL,
    category VARCHAR(32) NOT NULL CHECK (category IN ('BEACH', 'TEMPLE', 'NATURE', 'HERITAGE')),
    subcategory VARCHAR(80) NOT NULL,
    short_description TEXT NOT NULL,
    detailed_description TEXT NOT NULL,
    historical_significance TEXT,
    latitude DOUBLE PRECISION NOT NULL CHECK (latitude BETWEEN -90.0 AND 90.0),
    longitude DOUBLE PRECISION NOT NULL CHECK (longitude BETWEEN -180.0 AND 180.0),
    distance_from_town_km NUMERIC(5, 2) NOT NULL DEFAULT 0.0,
    address VARCHAR(240) NOT NULL,
    timings VARCHAR(80) NOT NULL DEFAULT '06:00 AM - 08:00 PM',
    entry_fee VARCHAR(64) NOT NULL DEFAULT 'Free',
    best_season VARCHAR(80) NOT NULL DEFAULT 'October - March',
    nearest_transit_stop VARCHAR(140),
    rating NUMERIC(2, 1) NOT NULL DEFAULT 4.5 CHECK (rating BETWEEN 1.0 AND 5.0),
    review_count INTEGER NOT NULL DEFAULT 0,
    image_url TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_balasore_tourist_category
    ON balasore_tourist_locations (category);

CREATE INDEX IF NOT EXISTS idx_balasore_tourist_coords
    ON balasore_tourist_locations (latitude, longitude);

CREATE INDEX IF NOT EXISTS idx_balasore_tourist_rating
    ON balasore_tourist_locations (rating DESC);

-- Canonical Seed Records for Balasore District Attractions
INSERT INTO balasore_tourist_locations (
    id, name, odia_name, category, subcategory, short_description, detailed_description,
    historical_significance, latitude, longitude, distance_from_town_km, address,
    timings, entry_fee, best_season, nearest_transit_stop, rating, review_count
) VALUES
(
    'chandipur_beach',
    'Chandipur-on-Sea (Vanishing Beach)',
    'ଚାନ୍ଦିପୁର ବେଳାଭୂମି',
    'BEACH',
    'Beach & Coast',
    'World-famous vanishing sea beach where the Bay of Bengal recedes up to 5 km twice daily.',
    'Chandipur-on-Sea offers a rare intertidal phenomenon where shallow waters ebb 1 to 5 km every tide cycle, revealing red ghost crabs and living-fossil horseshoe crabs.',
    'Historically linked to Balaramgadi maritime trade and modern DRDO Integrated Test Range (ITR).',
    21.4442, 87.0147, 16.0, 'Chandipur, Balasore, Odisha 756025',
    '05:30 AM - 08:30 PM', 'Free', 'October - March', 'Chandipur Panthanivas Bus Stop', 4.9, 2480
),
(
    'talasari_beach',
    'Talasari Beach & Subarnarekha Estuary',
    'ତାଳସାରୀ ବେଳାଭୂମି',
    'BEACH',
    'Estuary Beach',
    'Palm-fringed serene beach at the Subarnarekha river mouth with boat rides across calm tidal creeks.',
    'Named after rows of palm trees (Tala Sari), this tranquil coastal stretch meets the Subarnarekha estuary with golden sandbars and artisan fishing boats.',
    'Traditional northernmost coastal fishing harbour of Odisha.',
    21.6085, 87.4659, 88.0, 'Talasari, Bhograi, Balasore, Odisha 756085',
    '06:00 AM - 07:00 PM', 'Free (Boat ₹50)', 'October - February', 'Chandaneshwar Bus Stand', 4.8, 1640
),
(
    'khirachora_gopinath',
    'Khirachora Gopinatha Temple, Remuna',
    'କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର, ରେମୁଣା',
    'TEMPLE',
    'Heritage Temple',
    '13th-century Vaishnavite shrine renowned for sacred Amruta Keli condensed milk bhog.',
    'Built in black chlorite stone by King Narasingha Deva I, the temple is celebrated across eastern India for earthen-pot Khira Prasad and timeless Kalinga stone carving.',
    'Visited by Sri Chaitanya Mahaprabhu in 1510 CE following Saint Madhavendra Puri.',
    21.5292, 86.8711, 9.0, 'Remuna, Balasore, Odisha 756019',
    '05:30 AM - 09:00 PM', 'Free Darshan', 'All Year', 'Remuna Golei / Temple Bazar', 4.9, 3120
),
(
    'emami_jagannath',
    'Emami Jagannath Temple, Balgopalpur',
    'ଇମାମୀ ଜଗନ୍ନାଥ ମନ୍ଦିର',
    'TEMPLE',
    'Architecture & Temple',
    'Grand Konark Sun-Chariot style Jagannath temple featuring 16 carved stone wheels and manicured gardens.',
    'Constructed in classic Kalinga temple architecture with 16 hand-sculpted stone chariot wheels, 7 celestial horses, and tranquil 10-acre gardens.',
    'Built by traditional Odisha stone artisans following classical Shilpa Shastra.',
    21.4981, 86.8624, 11.0, 'Balgopalpur, Remuna, Balasore, Odisha 756020',
    '06:00 AM - 08:30 PM', 'Free', 'All Year', 'Balgopalpur Industrial Square', 4.8, 1890
),
(
    'panchalingeswar_temple',
    'Panchalingeswar Hill Shrine',
    'ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଶୈବ ପୀଠ',
    'TEMPLE',
    'Hill Shrine & Stream',
    'Five natural Shiva Lingams bathed continuously by a cool mountain stream in the Nilagiri hills.',
    'Nestled atop lush Eastern Ghats hills near Kuldiha forest, devotees climb 263 stone steps to touch five submerged Lingams under a perennial hill spring.',
    'Ancient Shaivite pilgrimage seat patronized by the Nilagiri royal lineage.',
    21.4253, 86.7441, 30.0, 'Panchalingeswar, Nilagiri, Balasore, Odisha 756040',
    '06:00 AM - 06:30 PM', 'Free', 'October - March', 'Panchalingeswar Panthanivas Gate', 4.9, 2750
),
(
    'kuldiha_wildlife_sanctuary',
    'Kuldiha Wildlife Sanctuary',
    'କୁଳଡ଼ିହା ଅଭୟାରଣ୍ୟ',
    'NATURE',
    'Wildlife Sanctuary & Forest',
    '272 sq km dense Sal forest sanctuary home to Asian elephants, gaur, hornbills, and forest watchtowers.',
    'Connected to Similipal Biosphere Reserve via the Sukhupada-Nato hill corridor, Kuldiha features Jadachua waterhole watchtowers, eco-cottages, and guided jungle safaris.',
    'Declared a protected Odisha Wildlife Sanctuary in 1984.',
    21.4196, 86.6203, 40.0, 'Kuldiha Forest Range, Nilagiri, Balasore, Odisha',
    '06:30 AM - 04:30 PM', '₹100 Entry + Permit', 'November - April', 'Nilagiri Forest Checkgate', 4.8, 1420
),
(
    'nilagiri_jagannath_palace',
    'Nilagiri Palace & Heritage Fort Precinct',
    'ନୀଳଗିରି ରାଜବାଟୀ ଓ ଐତିହ୍ୟ',
    'HERITAGE',
    'Royal Heritage & Fort',
    'Historic princely estate architecture and century-old Jagannath temple precinct at the foothills of Nilagiri.',
    'Showcases the architectural legacy of the erstwhile Nilagiri princely state with colonnaded courtyards, traditional Rath Yatra corridors, and nearby hill viewpoints.',
    'Seat of the historic Nilagiri royal dynasty integrated into Odisha in 1948.',
    21.4622, 86.7715, 22.0, 'Nilagiri Town, Balasore, Odisha 756040',
    '07:00 AM - 06:30 PM', 'Free', 'October - March', 'Nilagiri Bus Stand', 4.6, 890
),
(
    'bhusandeswar_temple',
    'Bhusandeswar Shiva Temple',
    'ଭୂଷଣ୍ଡେଶ୍ୱର ଶିବ ମନ୍ଦିର',
    'HERITAGE',
    'Ancient Megalithic Shrine',
    'Home to one of Asia''s largest monolithic black-granite Shiva Lingams on the Subarnarekha coast.',
    'Standing 12 feet high and 14 feet in circumference, this monumental ancient granite Lingam draws pilgrims during Maha Shivaratri and coastal heritage travelers.',
    'Linked in regional folklore to Ravana''s Lanka journey and ancient Kalinga Shaivism.',
    21.6625, 87.3812, 92.0, 'Bhograi, Balasore, Odisha 756085',
    '05:30 AM - 08:30 PM', 'Free', 'October - March', 'Jaleswar - Bhograi Bus Stop', 4.7, 1180
);
