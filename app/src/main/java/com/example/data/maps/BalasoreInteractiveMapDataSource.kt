package com.example.data.maps

import com.example.ui.components.BALASORE_TOURIST_LOCATIONS
import com.example.ui.components.TouristLocationItem
import com.example.ui.components.calculateHaversineDistanceKm

/**
 * Geotagged local news story point displayed on the Balasore Interactive Map
 * (Google Maps SDK & Offline District Vector Map).
 */
data class BalasoreGeoNewsLocation(
    val id: String,
    val headlineEn: String,
    val headlineOr: String,
    val summaryEn: String,
    val summaryOr: String,
    val locationNameEn: String,
    val locationNameOr: String,
    val wardOrBlock: String,
    val category: String,
    val sourceReporter: String,
    val publishedTimeAgo: String,
    val latitude: Double,
    val longitude: Double,
    val isBreaking: Boolean,
    val emoji: String,
    val relatedEmergencyPhone: String = "112"
)

/**
 * Geospatial weather alert area (polygon/circle zone + center marker) displayed
 * on the Balasore Interactive Map (Google Maps CircleOptions & Vector Map Radar Zone).
 */
data class BalasoreWeatherAlertZone(
    val id: String,
    val alertTitleEn: String,
    val alertTitleOr: String,
    val zoneNameEn: String,
    val zoneNameOr: String,
    val severity: String, // "CRITICAL", "SEVERE", "MODERATE", "ADVISORY"
    val hazardType: String,
    val centerLat: Double,
    val centerLng: Double,
    val radiusMeters: Double,
    val windSpeedKmh: Int,
    val expectedRainfallMm: Int,
    val imdAdvisoryEn: String,
    val imdAdvisoryOr: String,
    val affectedAreas: List<String>,
    val nearestShelterName: String,
    val nearestShelterLat: Double,
    val nearestShelterLng: Double,
    val nearestShelterPhone: String,
    val validUntil: String,
    val emoji: String
)

/**
 * Canonical data source for the Balasore Interactive Multi-Layer Map:
 * 1. Geotagged News Locations across Balasore District
 * 2. Geospatial Weather Alert Areas (Cyclone Surge, River Flood, Lightning Squall, Urban Rain)
 * 3. Tourist Hotspots (linked with [BALASORE_TOURIST_LOCATIONS])
 */
object BalasoreInteractiveMapDataSource {

    val BALASORE_GEO_NEWS_LOCATIONS: List<BalasoreGeoNewsLocation> = listOf(
        BalasoreGeoNewsLocation(
            id = "news_loc_chandipur_seawall",
            headlineEn = "OSDMA & Coastal Engineering Deploy Smart Tide Sensors Along Chandipur Beach",
            headlineOr = "ଚାନ୍ଦିପୁର ବେଳାଭୂମିରେ ସ୍ମାର୍ଟ ଜୁଆର ସେନ୍ସର ଓ ସୁରକ୍ଷା ବ୍ୟବସ୍ଥା ସ୍ଥାପିତ",
            summaryEn = "Six solar-powered acoustic tide & swell telemetry beacons have been activated across the 4-km receding intertidal zone at Chandipur to alert tourists 25 minutes before incoming bore tides.",
            summaryOr = "ପର୍ଯ୍ୟଟକଙ୍କ ସୁରକ୍ଷା ପାଇଁ ଚାନ୍ଦିପୁର ସମୁଦ୍ର କୂଳରେ ୬ଟି ସୌରଚାଳିତ ଜୁଆର ସୂଚନା ଯନ୍ତ୍ର କାର୍ଯ୍ୟକ୍ଷମ ହୋଇଛି।",
            locationNameEn = "Chandipur Intertidal Coast",
            locationNameOr = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
            wardOrBlock = "Sadar Block • Coastal Sector 2",
            category = "Coastal & Safety",
            sourceReporter = "Baleshwar Coastal Bureau",
            publishedTimeAgo = "18 mins ago",
            latitude = 21.4695,
            longitude = 87.0185,
            isBreaking = true,
            emoji = "🌊",
            relatedEmergencyPhone = "06782-270033"
        ),
        BalasoreGeoNewsLocation(
            id = "news_loc_station_amrit_bharat",
            headlineEn = "Balasore Railway Station Platform 1 & Foot-Overbridge Expansion Enters Final Phase",
            headlineOr = "ବାଲେଶ୍ୱର ରେଳ ଷ୍ଟେସନ ପ୍ଲାଟଫର୍ମ-୧ ଓ ନୂତନ ଫୁଟ୍ ଓଭରବ୍ରିଜ୍ କାର୍ଯ୍ୟ ଚୂଡ଼ାନ୍ତ ପର୍ଯ୍ୟାୟରେ",
            summaryEn = "South Eastern Railway engineers completed structural testing of the 12-meter wide passenger concourse, adding escalators, digital coach guidance boards, and a dedicated Mo Bus feeder bay outside Sahadevkhunta exit.",
            summaryOr = "ବାଲେଶ୍ୱର ଷ୍ଟେସନରେ ଯାତ୍ରୀଙ୍କ ସୁବିଧା ପାଇଁ ନୂତନ ଏସ୍କାଲେଟର ଓ ଡିଜିଟାଲ କୋଚ୍ ସୂଚନା ବୋର୍ଡ ସ୍ଥାପନ କରାଯାଇଛି।",
            locationNameEn = "Balasore Railway Station (BLS)",
            locationNameOr = "ବାଲେଶ୍ୱର ରେଳ ଷ୍ଟେସନ",
            wardOrBlock = "Ward 14 • Sahadevkhunta",
            category = "Transit & Infrastructure",
            sourceReporter = "SER Railway Desk",
            publishedTimeAgo = "42 mins ago",
            latitude = 21.5008,
            longitude = 86.9197,
            isBreaking = true,
            emoji = "🚆",
            relatedEmergencyPhone = "139"
        ),
        BalasoreGeoNewsLocation(
            id = "news_loc_fmmch_trauma_wing",
            headlineEn = "Fakir Mohan Medical College & AIIMS Satellite Unit Launch 24x7 Heatstroke & Snakebite ICU",
            headlineOr = "ଫକିର ମୋହନ ମେଡିକାଲ କଲେଜରେ ୨୪ ଘଣ୍ଟିଆ ସର୍ପଦଂଶନ ଓ ଜରୁରୀକାଳୀନ ଆଇସିୟୁ ଆରମ୍ଭ",
            summaryEn = "Equipped with 40 specialized critical-care beds and rapid polyvalent anti-snake venom reserves, the new emergency wing serves patients across Remuna, Nilagiri, Soro, and Jaleswar.",
            summaryOr = "ବାଲେଶ୍ୱର ଜିଲ୍ଲା ମୁଖ୍ୟ ଚିକିତ୍ସାଳୟ ଓ ମେଡିକାଲ କଲେଜରେ ୪୦ ଶଯ୍ୟା ବିଶିଷ୍ଟ ନୂତନ ଜରୁରୀକାଳୀନ ୱାର୍ଡ ଖୋଲିଛି।",
            locationNameEn = "FMMCH Medical Campus, Balasore",
            locationNameOr = "ଏଫ୍.ଏମ୍. ମେଡିକାଲ କଲେଜ",
            wardOrBlock = "Ward 8 • Hospital Road",
            category = "Health & Medical",
            sourceReporter = "District Health Correspondent",
            publishedTimeAgo = "1 hr ago",
            latitude = 21.4922,
            longitude = 86.9248,
            isBreaking = false,
            emoji = "🏥",
            relatedEmergencyPhone = "108"
        ),
        BalasoreGeoNewsLocation(
            id = "news_loc_remuna_golei_flyover",
            headlineEn = "Smart LED Solar Streetlights & CCTV Traffic Grid Activated at Remuna Golei NH-16 Junction",
            headlineOr = "ରେମୁଣା ଗୋଲେଇ ଜାତୀୟ ରାଜପଥ-୧୬ ଛକରେ ସ୍ମାର୍ଟ ଟ୍ରାଫିକ୍ ସିସିଟିଭି ଓ ସୌର ଆଲୋକୀକରଣ",
            summaryEn = "Over 140 high-lumen LED poles and automated number-plate recognition cameras are now operational between Remuna Golei and Kuruda Bypass to curb night highway accidents.",
            summaryOr = "ରେମୁଣା ଗୋଲେଇ ଠାରୁ କୁରୁଡ଼ା ବାଇପାସ୍ ପର୍ଯ୍ୟନ୍ତ ନୂତନ ଏଲଇଡି ଷ୍ଟ୍ରିଟ୍ ଲାଇଟ୍ ଓ ଟ୍ରାଫିକ୍ କ୍ୟାମେରା ଲଗାଯାଇଛି।",
            locationNameEn = "Remuna Golei NH-16 Corridor",
            locationNameOr = "ରେମୁଣା ଗୋଲେଇ ଛକ",
            wardOrBlock = "Remuna NAC • Industrial Zone",
            category = "Smart City & Civic",
            sourceReporter = "NH-16 Civic Desk",
            publishedTimeAgo = "2 hrs ago",
            latitude = 21.5292,
            longitude = 86.8744,
            isBreaking = false,
            emoji = "🛣️",
            relatedEmergencyPhone = "1033"
        ),
        BalasoreGeoNewsLocation(
            id = "news_loc_balaramgadi_harbour",
            headlineEn = "Balaramgadi Marine Fishing Harbour Records Bumper Hilsa & Pomfret Landing After Ban Period",
            headlineOr = "ବଳରାମଗଡ଼ି ମାଛ ଧରା ବନ୍ଦରରେ ପ୍ରଚୁର ଇଲିଶି ଓ ପମ୍ପ୍ଲେଟ୍ ମାଛ ଆମଦାନୀ",
            summaryEn = "More than 210 deep-sea mechanized trawlers returned to the Budhabalanga river mouth jetty today, boosting cold-storage dispatches to Kolkata, Bhubaneswar, and local Baleshwar markets.",
            summaryOr = "ବଳରାମଗଡ଼ି ମୁହାଣରେ ୨୧୦ରୁ ଅଧିକ ଟ୍ରଲର ଫେରିବା ପରେ ମାଛ ବଜାରରେ ଚଳଚଞ୍ଚଳ ବୃଦ୍ଧି ପାଇଛି।",
            locationNameEn = "Balaramgadi Fishing Harbour",
            locationNameOr = "ବଳରାମଗଡ଼ି ମାଛ ବନ୍ଦର",
            wardOrBlock = "Chandipur Coast • Jetty Road",
            category = "Marine & Economy",
            sourceReporter = "Marine Fisheries Desk",
            publishedTimeAgo = "3 hrs ago",
            latitude = 21.4785,
            longitude = 87.0320,
            isBreaking = false,
            emoji = "⛴️",
            relatedEmergencyPhone = "06782-270108"
        ),
        BalasoreGeoNewsLocation(
            id = "news_loc_kuldiha_elephant_corridor",
            headlineEn = "Forest Department Deploys AI Thermal Cameras Along Kuldiha-Nilagiri Elephant Corridor",
            headlineOr = "କୁଲଡିହା-ନୀଳଗିରି ହାତୀ କରିଡରରେ ଏଆଇ ଥର୍ମାଲ୍ କ୍ୟାମେରା ଓ ସୌର ବାଡ଼ ସ୍ଥାପନ",
            summaryEn = "Early-warning SMS and siren alerts are now broadcast to 14 fringe villages near Panchalingeswar and Kuldiha sanctuary whenever wild elephant herds approach agricultural fields.",
            summaryOr = "କୁଲଡିହା অভୟାରଣ୍ୟ ନିକଟବର୍ତ୍ତୀ ୧୪ଟି ଗ୍ରାମରେ ହାତୀ ଚଳାଚଳ ସୂଚନା ପାଇଁ ସ୍ୱୟଂଚାଳିତ ଆଲାର୍ମ ବ୍ୟବସ୍ଥା ହୋଇଛି।",
            locationNameEn = "Kuldiha & Panchalingeswar Foothills",
            locationNameOr = "କୁଲଡିହା-ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର",
            wardOrBlock = "Nilagiri Subdivision",
            category = "Wildlife & Eco",
            sourceReporter = "Nilagiri Forest Beat",
            publishedTimeAgo = "4 hrs ago",
            latitude = 21.4116,
            longitude = 86.7214,
            isBreaking = false,
            emoji = "🐘",
            relatedEmergencyPhone = "1800-345-7158"
        ),
        BalasoreGeoNewsLocation(
            id = "news_loc_bhograi_subarnarekha_embankment",
            headlineEn = "Water Resources Dept Reinforces 6.5-Km Stone Spur Embankment Along Subarnarekha in Bhograi",
            headlineOr = "ଭୋଗରାଇ ସୁବର୍ଣ୍ଣରେଖା ନଦୀ ବନ୍ଧର ୬.୫ କି.ମି. ପଥର ପ୍ୟାକିଂ କାର୍ଯ୍ୟ ସମ୍ପୂର୍ଣ୍ଣ",
            summaryEn = "Ahead of seasonal monsoon inflows from Jharkhand's Galudih barrage, flood sluice gates and high-capacity dewatering pumps have been stationed near Talasari and Jaleswar.",
            summaryOr = "ସୁବର୍ଣ୍ଣରେଖା ବନ୍ୟା ନିୟନ୍ତ୍ରଣ ପାଇଁ ଭୋଗରାଇ ଓ ଜଳେଶ୍ୱର ଅଞ୍ଚଳରେ ନଦୀବନ୍ଧ ସୁଦୃଢ଼ୀକରଣ କରାଯାଇଛି।",
            locationNameEn = "Subarnarekha Estuary & Bhograi",
            locationNameOr = "ଭୋଗରାଇ ଓ ସୁବର୍ଣ୍ଣରେଖା ମୁହାଣ",
            wardOrBlock = "Bhograi Block • Talasari Belt",
            category = "River & Flood Control",
            sourceReporter = "North Odisha Irrigation Desk",
            publishedTimeAgo = "5 hrs ago",
            latitude = 21.6085,
            longitude = 87.2150,
            isBreaking = false,
            emoji = "🌉",
            relatedEmergencyPhone = "06781-253210"
        ),
        BalasoreGeoNewsLocation(
            id = "news_loc_soro_agri_mandi",
            headlineEn = "Digital E-NAM Paddy & Betel Leaf Auction Terminal Inaugurated at Soro Regulated Market",
            headlineOr = "ସୋର ନିୟନ୍ତ୍ରିତ ବଜାର କମିଟିରେ ଡିଜିଟାଲ ଧାନ ଓ ପାନ ନିଲାମ କେନ୍ଦ୍ର ଉଦଘାଟିତ",
            summaryEn = "Over 4,200 registered farmers from Soro, Bahanaga, and Simulia blocks can now access real-time electronic weighing and direct bank transfers within 24 hours of harvest sale.",
            summaryOr = "ସୋର, ବାହାନଗା ଓ ସିମୁଳିଆ ବ୍ଲକର ଚାଷୀଙ୍କ ପାଇଁ ଇ-ନାମ୍ ଡିଜିଟାଲ ମଣ୍ଡି ସେବା ଆରମ୍ଭ ହୋଇଛି।",
            locationNameEn = "Soro Krushak Mandi Corridor",
            locationNameOr = "ସୋର କୃଷକ ମଣ୍ଡି",
            wardOrBlock = "Soro NAC • NH-16 South",
            category = "Agriculture & Mandi",
            sourceReporter = "Krushak Bazar Bureau",
            publishedTimeAgo = "6 hrs ago",
            latitude = 21.2882,
            longitude = 86.6895,
            isBreaking = false,
            emoji = "🌾",
            relatedEmergencyPhone = "155333"
        )
    )

    val BALASORE_WEATHER_ALERT_ZONES: List<BalasoreWeatherAlertZone> = listOf(
        BalasoreWeatherAlertZone(
            id = "wx_zone_chandipur_surge",
            alertTitleEn = "Bay of Bengal High Tidal Surge & Coastal Squall Warning",
            alertTitleOr = "ବଙ୍ଗୋପସାଗର ଉଚ୍ଚ ଜୁଆର ଓ ଉପକୂଳବର୍ତ୍ତୀ ঝଡ଼ ପବନ ସତର୍କତା",
            zoneNameEn = "Chandipur–Balaramgadi Coastal Belt",
            zoneNameOr = "ଚାନ୍ଦିପୁର-ବଳରାମଗଡ଼ି ଉପକୂଳ ଅଞ୍ଚଳ",
            severity = "CRITICAL",
            hazardType = "Tidal Surge & Gale Winds",
            centerLat = 21.4695,
            centerLng = 87.0185,
            radiusMeters = 5200.0,
            windSpeedKmh = 68,
            expectedRainfallMm = 85,
            imdAdvisoryEn = "Sea conditions rough to very rough with 2.8m swell waves. Tourists must vacate the receding seabed 45 mins before flood tide. Mechanized trawlers advised not to venture into deep sea.",
            imdAdvisoryOr = "ସମୁଦ୍ର ଅଶାନ୍ତ ରହିବ ଏବଂ ୨.୮ ମିଟର ଉଚ୍ଚ ଜୁଆର ଆସିବାର ସମ୍ଭାବନା ଅଛି। ମৎସ୍ୟଜୀବୀ ଓ ପର୍ଯ୍ୟଟକମାନଙ୍କୁ ସତର୍କ ରହିବାକୁ ପରାମର୍ଶ।",
            affectedAreas = listOf("Chandipur Beach", "Balaramgadi Harbour", "Parikhi", "Dublagadi"),
            nearestShelterName = "Chandipur Multipurpose Cyclone Shelter (OSDMA)",
            nearestShelterLat = 21.4450,
            nearestShelterLng = 87.0155,
            nearestShelterPhone = "06782-270012",
            validUntil = "Valid next 12 hrs • IMD Bhubaneswar Bulletin",
            emoji = "🌀"
        ),
        BalasoreWeatherAlertZone(
            id = "wx_zone_subarnarekha_flood",
            alertTitleEn = "Subarnarekha & Budhabalanga Low-Lying Basin Flood Watch",
            alertTitleOr = "ସୁବର୍ଣ୍ଣରେଖା ଓ ବୁଢ଼ାବଳଙ୍ଗ ତଳିଆ ଅଞ୍ଚଳ ବନ୍ୟା ସତର୍କ ସୂଚନା",
            zoneNameEn = "Bhograi–Talasari–Estuary Delta Zone",
            zoneNameOr = "ଭୋଗରାଇ-ତାଳସାରୀ ମୁହାଣ ଅଞ୍ଚଳ",
            severity = "SEVERE",
            hazardType = "River Inflow & Estuarine Swell",
            centerLat = 21.5950,
            centerLng = 87.1850,
            radiusMeters = 6000.0,
            windSpeedKmh = 54,
            expectedRainfallMm = 110,
            imdAdvisoryEn = "Heavy upper-catchment rainfall coupled with high astronomical tide may cause waterlogging in low-lying riverine gram panchayats. Keep emergency kits ready and monitor Rajghat gauge levels.",
            imdAdvisoryOr = "ଉପରମୁଣ୍ଡରେ ପ୍ରବଳ ବର୍ଷା ଯୋଗୁଁ ନଦୀ ଜଳସ୍ତର ବୃଦ୍ଧି ପାଉଛି। ନଦୀ କୂଳିଆ ଗ୍ରାମବାସୀ ସତର୍କ ରୁହନ୍ତୁ।",
            affectedAreas = listOf("Talasari", "Udayapur", "Kirtania", "Bhograi Block", "Jaleswar River Belt"),
            nearestShelterName = "Talasari-Udayapur Multipurpose Cyclone Shelter",
            nearestShelterLat = 21.6085,
            nearestShelterLng = 87.2150,
            nearestShelterPhone = "06781-253210",
            validUntil = "Valid next 24 hrs • CWC & OSDMA Flood Cell",
            emoji = "🌊"
        ),
        BalasoreWeatherAlertZone(
            id = "wx_zone_nilagiri_lightning",
            alertTitleEn = "Kalbaisakhi Thunderstorm & Cloud-to-Ground Lightning Alert",
            alertTitleOr = "କାଳବୈଶାଖୀ ଘଡ଼ଘଡ଼ି ଓ ବଜ୍ରପାତ ସତର୍କ ସୂଚନା (ନୀଳଗିରି-କୁଲଡିହା)",
            zoneNameEn = "Nilagiri Hills & Panchalingeswar Forest Belt",
            zoneNameOr = "ନୀଳଗିରି ଓ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପାହାଡ଼ ଅଞ୍ଚଳ",
            severity = "SEVERE",
            hazardType = "Lightning & Gusty Downburst",
            centerLat = 21.4589,
            centerLng = 86.7656,
            radiusMeters = 5500.0,
            windSpeedKmh = 58,
            expectedRainfallMm = 64,
            imdAdvisoryEn = "Intense convective cloud cells detected over Nilagiri hills. Avoid trekking on slippery rock streams at Panchalingeswar waterfall and take shelter inside concrete buildings during lightning.",
            imdAdvisoryOr = "ନୀଳଗିରି ଓ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଅଞ୍ଚଳରେ ବଜ୍ରପାତ ସହ ବର୍ଷା ସମ୍ଭାବନା। ପାହାଡ଼ ଚଢ଼ିବା ଓ ଗଛ ତଳେ ରହିବାରୁ ଦୂରେଇ ରୁହନ୍ତୁ।",
            affectedAreas = listOf("Panchalingeswar", "Nilagiri Town", "Kuldiha Gate", "Sajanagarh"),
            nearestShelterName = "Nilagiri Subdivision Emergency Relief Center",
            nearestShelterLat = 21.4610,
            nearestShelterLng = 86.7720,
            nearestShelterPhone = "06782-233224",
            validUntil = "Valid next 6 hrs • Damini Lightning Radar",
            emoji = "⛈️"
        ),
        BalasoreWeatherAlertZone(
            id = "wx_zone_town_urban_rain",
            alertTitleEn = "Heavy Urban Downpour & Municipal Ward Drainage Advisory",
            alertTitleOr = "ବାଲେଶ୍ୱର ପୌରପାଳିକା ପ୍ରବଳ ବର୍ଷା ଓ ଜଳବନ୍ଦୀ ସତର୍କତା",
            zoneNameEn = "Balasore Town (Wards 1–31 & Station Road)",
            zoneNameOr = "ବାଲେଶ୍ୱର ସହର ଓ ଷ୍ଟେସନ ଛକ",
            severity = "MODERATE",
            hazardType = "Intense Spell & Urban Waterlogging",
            centerLat = 21.4934,
            centerLng = 86.9135,
            radiusMeters = 4200.0,
            windSpeedKmh = 42,
            expectedRainfallMm = 52,
            imdAdvisoryEn = "Short-duration intense rain spell (30-50 mm/hr) expected across central Balasore. Temporary waterlogging possible near Cinema Chhak, Sahadevkhunta underpass, and Motiganj.",
            imdAdvisoryOr = "ବାଲେଶ୍ୱର ସହରରେ ଘଣ୍ଟାପ୍ରତି ୩୦-୫୦ ମି.ମି. ବର୍ଷା ସମ୍ଭାବନା। ତଳିଆ ରାସ୍ତାରେ ଯାତାୟାତ ବେଳେ ସାବଧାନ ରୁହନ୍ତୁ।",
            affectedAreas = listOf("Sahadevkhunta", "Cinema Chhak", "Fakir Mohan Golei", "Motiganj", "ITR Chhak"),
            nearestShelterName = "Sahadevkhunta Municipal Community Relief Hall",
            nearestShelterLat = 21.4988,
            nearestShelterLng = 86.9310,
            nearestShelterPhone = "06782-262040",
            validUntil = "Valid next 8 hrs • Balasore Municipality Control Room",
            emoji = "🌧️"
        ),
        BalasoreWeatherAlertZone(
            id = "wx_zone_remuna_highway_fog",
            alertTitleEn = "NH-16 Crosswind & Low-Visibility Squall Advisory",
            alertTitleOr = "ଜାତୀୟ ରାଜପଥ-୧୬ ପବନ ଓ କମ୍ ଦୃଶ୍ୟମାନତା ସତର୍କତା",
            zoneNameEn = "Remuna–Kuruda–Soro Highway Corridor",
            zoneNameOr = "ରେମୁଣା-କୁରୁଡ଼ା-ସୋର ରାଜପଥ",
            severity = "MODERATE",
            hazardType = "Highway Crosswind & Rain Curtains",
            centerLat = 21.5292,
            centerLng = 86.8744,
            radiusMeters = 4500.0,
            windSpeedKmh = 46,
            expectedRainfallMm = 38,
            imdAdvisoryEn = "Sudden rain curtains reducing highway visibility below 400 meters along NH-16 between Remuna Golei and Bahanaga. Heavy vehicles and two-wheelers advised to use hazard lights.",
            imdAdvisoryOr = "ଜାତୀୟ ରାଜପଥ-୧୬ ରେ ବର୍ଷା ଓ ପବନ ଯୋଗୁଁ ଦୃଶ୍ୟମାନତା କମିପାରେ। ଗାଡ଼ି ଚାଳകମାନେ ହେଡଲାଇଟ୍ ବ୍ୟବହାର କରନ୍ତୁ।",
            affectedAreas = listOf("Remuna Golei", "Kuruda Bypass", "Khantapada", "Bahanaga"),
            nearestShelterName = "Remuna Block Disaster Response Shelter",
            nearestShelterLat = 21.5310,
            nearestShelterLng = 86.8760,
            nearestShelterPhone = "06782-245210",
            validUntil = "Valid next 6 hrs • NHAI & Traffic Control",
            emoji = "🌬️"
        )
    )

    /**
     * Returns all tourist hotspots from [BALASORE_TOURIST_LOCATIONS].
     */
    fun getTouristHotspots(): List<TouristLocationItem> = BALASORE_TOURIST_LOCATIONS

    /**
     * Checks whether a given coordinate lies inside the radius of a [BalasoreWeatherAlertZone].
     */
    fun isPointInsideAlertZone(lat: Double, lng: Double, zone: BalasoreWeatherAlertZone): Boolean {
        val distanceKm = calculateHaversineDistanceKm(lat, lng, zone.centerLat, zone.centerLng)
        return (distanceKm * 1000.0) <= zone.radiusMeters
    }

    /**
     * Finds the nearest geotagged news location to the given coordinate.
     */
    fun findNearestNewsLocation(lat: Double, lng: Double): BalasoreGeoNewsLocation {
        return BALASORE_GEO_NEWS_LOCATIONS.minByOrNull { news ->
            calculateHaversineDistanceKm(lat, lng, news.latitude, news.longitude)
        } ?: BALASORE_GEO_NEWS_LOCATIONS.first()
    }

    /**
     * Finds the weather alert zone containing or nearest to the given coordinate.
     */
    fun findNearestWeatherAlertZone(lat: Double, lng: Double): BalasoreWeatherAlertZone {
        return BALASORE_WEATHER_ALERT_ZONES.minByOrNull { zone ->
            calculateHaversineDistanceKm(lat, lng, zone.centerLat, zone.centerLng)
        } ?: BALASORE_WEATHER_ALERT_ZONES.first()
    }
}
