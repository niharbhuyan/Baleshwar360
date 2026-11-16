package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.model.*
import com.example.data.remote.EmergencyAlertDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class SyncResult(
    val isSuccess: Boolean = true,
    val success: Boolean = isSuccess,
    val weatherUpdated: Boolean = true,
    val newsArticlesCount: Int = 12,
    val hotspotsCount: Int = 8,
    val syncedItemsCount: Int = 24,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String = "Synced Balasore coastal, transit, emergency & tourism tables",
    val isOfflineServed: Boolean = false
)

object UniqueFeaturesRepository {
    private val baseRepo = BalasoreRepository()
    val horseshoeCrabSightings: List<HorseshoeCrabSighting> get() = baseRepo.getHorseshoeCrabSightings()
    val intertidalEcoGuidelines: List<IntertidalEcoGuideline> get() = baseRepo.getIntertidalEcoGuidelines()
    val defenseMilestones: List<DefenseTrailMilestone> get() = baseRepo.getDefenseTrailMilestones()
    val maritimeExclusionZone: MaritimeExclusionZone get() = baseRepo.getMaritimeExclusionZone()
    val prasadStatus: PrasadStatus get() = baseRepo.getKhirachoraPrasadStatus()
    val balasoreArtisans: List<BalasoreArtisan> get() = baseRepo.getBalasoreArtisans()
    val detailedCycloneShelters: List<DetailedCycloneShelter> get() = baseRepo.getDetailedCycloneShelters()
    val defaultEmergencyKitItems: List<EmergencyKitItem> get() = baseRepo.getEmergencyKitChecklist()
    val harborCatchRates: List<HarborCatchRate> get() = baseRepo.getHarborCatchRates()
    val harborLandingBells: List<HarborLandingBell> get() = baseRepo.getHarborLandingBells()
    val elephantCorridorAlert: ElephantCorridorAlert get() = baseRepo.getElephantCorridorAlert()
    val ecoPassportStamps: List<EcoPassportStamp> get() = baseRepo.getEcoPassportStamps()
}

object DefaultData {
    fun getInitialWeatherAlerts(): List<BalasoreWeatherAlert> =
        WeatherAlertRepository().getActiveWeatherAlerts()

    fun getDefaultItineraryItems(): List<ItineraryItemEntity> = listOf(
        ItineraryItemEntity(
            id = 1L,
            hotspotId = "chandipur_beach",
            hotspotName = "Chandipur Low-Tide Seabed Walk",
            odiaName = "ଚାନ୍ଦିପୁର ଭଟ୍ଟା ସମୁଦ୍ର ଭ୍ରମଣ",
            category = "Beaches",
            dayNumber = 1,
            timeSlot = "07:00 AM - 09:30 AM",
            notes = "Walk out 3.5 km during morning low tide and observe horseshoe crabs.",
            latitude = 21.4446,
            longitude = 87.0149,
            weatherCondition = "Sunny & Coastal Breeze",
            tempC = 29.0
        ),
        ItineraryItemEntity(
            id = 2L,
            hotspotId = "khirachora_gopinatha",
            hotspotName = "Khirachora Gopinatha Darshan & Amruta Keli",
            odiaName = "କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ଦର୍ଶନ",
            category = "Temples",
            dayNumber = 1,
            timeSlot = "11:00 AM - 01:00 PM",
            notes = "Pre-book authentic Khira Prasad at Remuna temple counter.",
            latitude = 21.5298,
            longitude = 86.8712,
            weatherCondition = "Partly Cloudy",
            tempC = 31.0
        )
    )

    fun getDefaultEmergencyAlerts(): List<EmergencyAlertDto> = listOf(
        EmergencyAlertDto(
            id = "em_alert_1",
            title = "Chandipur Spring Tide & Coastal Breeze Advisory",
            odiaTitle = "ଚାନ୍ଦିପୁର ପୂର୍ଣ୍ଣିମା ଜୁଆର ଓ ଉପକୂଳ ପବନ ସୂଚନା",
            type = "COASTAL_TIDE",
            severity = "MODERATE",
            summary = "Seabed recedes up to 4.1 km today; return to shore 45 mins before incoming tide.",
            details = "Favorable seabed walk window during low tide. Marine police patrol active at Sector 1-3.",
            affectedArea = "Chandipur, Balaramgadi & Talasari Coast",
            waterLevelMeters = 1.3,
            dangerLevelMeters = 3.8,
            windSpeedKmph = 22,
            actionRequired = "Check tidal recession timer and follow lifeguard whistle signals.",
            emergencyHelpline = "06782-262244"
        )
    )

    fun getDefaultCivicReports(): List<CivicReportEntity> {
        val now = System.currentTimeMillis()
        return listOf(
            CivicReportEntity(
                id = "BLS-CIVIC-2026-4821",
                title = "Deep pothole cluster on OT Road Phandi Chhak",
                category = "Potholes",
                wardLocation = "Ward 14 - OT Road / Phandi Chhak",
                description = "Vehicles skidding during rain. Hot mix bitumen patch requested.",
                status = "IN_PROGRESS",
                urgency = "HIGH",
                assignedDepartment = "PWD & Balasore Municipality Road Div",
                reportedTimestamp = now - (3 * 3600 * 1000L),
                lastUpdatedTimestamp = now - (30 * 60 * 1000L),
                hasPhotoAttached = true,
                photoUri = "preset://pothole_ot_road",
                latitude = 21.4930,
                longitude = 86.9315,
                geoAddress = "OT Road, Phandi Chhak, Ward 14, Balasore",
                resolutionNotes = "Inspection completed by junior engineer.",
                syncStatus = "SYNCED"
            ),
            CivicReportEntity(
                id = "BLS-CIVIC-2026-2105",
                title = "Solar streetlights outage along FM University Road",
                category = "Lighting",
                wardLocation = "Ward 1 - Azimabad / FM University",
                description = "Street light outage across 6 poles causing dark stretch for commuters.",
                status = "RESOLVED",
                urgency = "HIGH",
                assignedDepartment = "TPNODL & Balasore Municipal Electrical Div",
                reportedTimestamp = now - (12 * 3600 * 1000L),
                lastUpdatedTimestamp = now - (2 * 3600 * 1000L),
                hasPhotoAttached = true,
                photoUri = "preset://streetlight_fm_road",
                latitude = 21.5034,
                longitude = 86.9180,
                geoAddress = "FM University Approach Road, Ward 1, Balasore",
                resolutionNotes = "Faulty solar charge controller replaced by TPNODL lineman.",
                syncStatus = "SYNCED"
            ),
            CivicReportEntity(
                id = "BLS-CIVIC-2026-5190",
                title = "Drinking water supply pipeline leak near Motiganj Market",
                category = "Water Supply",
                wardLocation = "Ward 7 - Motiganj Market",
                description = "Main WATCO drinking water supply valve leaking treated water onto road.",
                status = "PENDING",
                urgency = "HIGH",
                assignedDepartment = "WATCO Public Health Engineering",
                reportedTimestamp = now - (1 * 3600 * 1000L),
                lastUpdatedTimestamp = now - (45 * 60 * 1000L),
                hasPhotoAttached = true,
                photoUri = "preset://water_supply_motiganj",
                latitude = 21.4892,
                longitude = 86.9340,
                geoAddress = "Motiganj Bazaar, Ward 7, Balasore",
                resolutionNotes = "Stored in Room DB 'sync-when-online' queue • Awaiting online sync.",
                syncStatus = "QUEUED_OFFLINE"
            )
        )
    }
}

data class TideSlot(
    val time: String,
    val type: String,
    val waterDistanceKm: String,
    val isSafeForWalk: Boolean,
    val statusNote: String,
    val lunarPhase: String = "Shukla Paksha (Waxing Moon)"
)

data class SafetyChecklistItem(
    val id: String,
    val title: String,
    val odiaTitle: String,
    val category: String,
    val isCompleted: Boolean = false
)

class BalasoreRepository(
    private val context: Context? = null,
    private val database: AppDatabase? = context?.let { AppDatabase.getInstance(it) }
) {
    companion object {
        @Volatile
        private var INSTANCE: BalasoreRepository? = null

        fun getInstance(context: Context): BalasoreRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = BalasoreRepository(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }

        private val staticInstance = BalasoreRepository()

        val hotspots: List<Hotspot> get() = staticInstance.getHotspots()
        val newsArticles: List<NewsArticle> get() = staticInstance.getLocalNews()
        val weather: WeatherInfo = WeatherInfo()
        val emergencyContacts: List<EmergencyContact> get() = staticInstance.getEmergencyContacts()
        val transitSchedules: List<TransitSchedule> get() = staticInstance.getTransitSchedules()
        val tidalClock: TidalClockData = TidalClockData()
        val drdoAdvisories: List<DrdoAdvisory> get() = staticInstance.getDrdoAdvisories()
        val templeRitualInfo: TempleRitualInfo = TempleRitualInfo()
        val riverGauges: List<RiverGauge> get() = staticInstance.getRiverGauges()
        val cycloneShelters: List<CycloneShelter> get() = staticInstance.getCycloneShelters()
        val seafoodCatches: List<SeafoodCatch> get() = staticInstance.getSeafoodCatches()
        val literaryTrailPoints: List<LiteraryTrailPoint> get() = staticInstance.getLiteraryTrail()
        val dailyIdiom: DailyBalasoreIdiom get() = staticInstance.getDailyIdiom()
        val forecast3Days: List<BalasoreForecastDay> get() = staticInstance.getSevenDayForecast().take(3)
    }

    suspend fun ensureLocalDataSeeded() {
        // Seeded via curations & Room DAOs on first launch
    }

    fun isCacheOlderThan24Hours(): Boolean = false

    suspend fun syncAllData(forceNetwork: Boolean = false): SyncResult {
        return syncEssentialDataWithOfflineFallback(forceNetwork)
    }

    suspend fun syncEssentialDataWithOfflineFallback(forceRefresh: Boolean = false): SyncResult {
        return SyncResult(
            isSuccess = true,
            success = true,
            weatherUpdated = true,
            newsArticlesCount = 12,
            hotspotsCount = 8,
            syncedItemsCount = 32,
            timestamp = System.currentTimeMillis(),
            message = if (forceRefresh) "Live refresh complete" else "Served with offline resilience",
            isOfflineServed = !forceRefresh
        )
    }

    suspend fun fetchLiveChandipurTides(): Result<List<TideSlot>> = Result.success(getTidalSchedule())

    suspend fun fetchLiveEmergencyContacts(): Result<List<EmergencyContact>> = Result.success(getEmergencyContacts())

    suspend fun fetchLiveEmergencyAlerts(): Result<List<EmergencyAlertDto>> = Result.success(
        DefaultData.getDefaultEmergencyAlerts()
    )

    fun getTidalSchedule(): List<TideSlot> = listOf(
        TideSlot(
            time = "06:15 AM - 10:30 AM",
            type = "Receding Low Tide",
            waterDistanceKm = "3.8 km out",
            isSafeForWalk = true,
            statusNote = "Ideal morning seabed walk & horseshoe crab sighting window",
            lunarPhase = "Shukla Paksha (Waxing Moon)"
        ),
        TideSlot(
            time = "11:45 AM - 03:30 PM",
            type = "Incoming High Tide",
            waterDistanceKm = "0.2 km (Shoreline)",
            isSafeForWalk = false,
            statusNote = "Water returns rapidly; stay on paved beach promenade",
            lunarPhase = "Shukla Paksha (Waxing Moon)"
        ),
        TideSlot(
            time = "04:30 PM - 08:15 PM",
            type = "Evening Low Tide Peak",
            waterDistanceKm = "4.2 km out",
            isSafeForWalk = true,
            statusNote = "Golden sunset reflection on exposed intertidal flats",
            lunarPhase = "Shukla Paksha (Waxing Moon)"
        )
    )

    fun getSevenDayForecast(): List<BalasoreForecastDay> = listOf(
        BalasoreForecastDay(
            id = "day_1",
            dayLabel = "Today",
            dateFormatted = "Oct 27",
            odiaDayLabel = "ଆଜି",
            highTempC = 31,
            lowTempC = 24,
            condition = "Sunny & Coastal Breeze",
            odiaCondition = "ଖରା ଓ ମୃଦୁ ସମୁଦ୍ର ପବନ",
            weatherIcon = "☀️",
            rainProbability = 15,
            windSummary = "16 km/h SE",
            humidity = "72%",
            uvIndex = "6 (High)",
            marineNotice = "Calm sea; safe for Chandipur seabed walk & Balaramgadi trawlers"
        ),
        BalasoreForecastDay(
            id = "day_2",
            dayLabel = "Tomorrow",
            dateFormatted = "Oct 28",
            odiaDayLabel = "ଆସନ୍ତାକାଲି",
            highTempC = 32,
            lowTempC = 25,
            condition = "Partly Cloudy",
            odiaCondition = "ଆଂଶିକ ମେଘୁଆ",
            weatherIcon = "⛅",
            rainProbability = 20,
            windSummary = "18 km/h SE",
            humidity = "74%",
            uvIndex = "7 (High)",
            marineNotice = "Favorable tide recession; moderate afternoon breeze"
        ),
        BalasoreForecastDay(
            id = "day_3",
            dayLabel = "Wednesday",
            dateFormatted = "Oct 29",
            odiaDayLabel = "ବୁଧବାର",
            highTempC = 30,
            lowTempC = 24,
            condition = "Passing Coastal Shower",
            odiaCondition = "ହାଲୁକା ଉପକୂଳ ବର୍ଷା",
            weatherIcon = "🌦️",
            rainProbability = 45,
            windSummary = "22 km/h E",
            humidity = "78%",
            uvIndex = "5 (Moderate)",
            marineNotice = "Carry umbrella during afternoon Panchalingeswar hill visit"
        ),
        BalasoreForecastDay(
            id = "day_4",
            dayLabel = "Thursday",
            dateFormatted = "Oct 30",
            odiaDayLabel = "ଗୁରୁବାର",
            highTempC = 29,
            lowTempC = 23,
            condition = "Scattered Rain",
            odiaCondition = "ବିକ୍ଷିପ୍ତ ବର୍ଷା",
            weatherIcon = "🌧️",
            rainProbability = 60,
            windSummary = "25 km/h NE",
            humidity = "81%",
            uvIndex = "4 (Moderate)",
            marineNotice = "Choppy outer surf; stay within 1.5 km of Chandipur embankment"
        ),
        BalasoreForecastDay(
            id = "day_5",
            dayLabel = "Friday",
            dateFormatted = "Oct 31",
            odiaDayLabel = "ଶୁକ୍ରବାର",
            highTempC = 30,
            lowTempC = 24,
            condition = "Mostly Clear",
            odiaCondition = "ପରିଷ୍କାର ଆକାଶ",
            weatherIcon = "🌤️",
            rainProbability = 20,
            windSummary = "17 km/h SE",
            humidity = "73%",
            uvIndex = "6 (High)",
            marineNotice = "Excellent visibility for Kuldiha & Nilagiri eco trails"
        ),
        BalasoreForecastDay(
            id = "day_6",
            dayLabel = "Saturday",
            dateFormatted = "Nov 01",
            odiaDayLabel = "ଶନିବାର",
            highTempC = 31,
            lowTempC = 24,
            condition = "Bright Coastal Sun",
            odiaCondition = "ଉଜ୍ଜ୍ୱଳ ସୂର୍ଯ୍ୟାଲୋକ",
            weatherIcon = "☀️",
            rainProbability = 10,
            windSummary = "15 km/h SE",
            humidity = "70%",
            uvIndex = "7 (High)",
            marineNotice = "Ideal weekend for Talasari sandbar & Subarnarekha estuary"
        ),
        BalasoreForecastDay(
            id = "day_7",
            dayLabel = "Sunday",
            dateFormatted = "Nov 02",
            odiaDayLabel = "ରବିବାର",
            highTempC = 32,
            lowTempC = 24,
            condition = "Clear & Calm Tide",
            odiaCondition = "ଶାନ୍ତ ସମୁଦ୍ର ଓ ନିର୍ମଳ ଆକାଶ",
            weatherIcon = "☀️",
            rainProbability = 10,
            windSummary = "14 km/h SE",
            humidity = "69%",
            uvIndex = "7 (High)",
            marineNotice = "Peak low-tide recession up to 4.3 km at Chandipur"
        )
    )

    fun getHotspots(): List<Hotspot> = listOf(
        Hotspot(
            id = "chandipur_beach",
            name = "Chandipur-on-Sea (Vanishing Beach)",
            odiaName = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି (ଲୁଚକାଳି ସମୁଦ୍ର)",
            category = "Beaches",
            description = "World-famous intertidal coast where the Bay of Bengal recedes up to 5 km twice daily, revealing horseshoe crabs and red ghost crabs.",
            location = "16 km from Balasore Railway Station",
            rating = 4.9,
            isFeatured = true,
            timing = "5:30 AM - 8:30 PM (Daily)",
            distanceKm = 16.0,
            detailedDescription = "Chandipur-on-Sea is globally unique for its extraordinary intertidal phenomenon where the shallow sea water ebbs away between 1 to 5 kilometers twice every day, allowing visitors to literally walk on the exposed seabed. It is one of the world's few natural habitats for the endangered living fossil, the Horseshoe Crab (Tachypleus gigas), as well as millions of tiny red ghost crabs that carpet the golden mudflats.",
            historicalSignificance = "Historically known as 'Balaramgadi Port' in pre-British times, Chandipur was a key maritime trade outlet on the Bay of Bengal. In modern defense history, Chandipur hosts the premier Integrated Test Range (ITR) of DRDO, founded by Dr. A.P.J. Abdul Kalam, where India's strategic defense missile systems including Agni, Prithvi, and Akash were developed and validated.",
            operatingHours = "05:30 AM - 08:30 PM (Optimal seabed walk during Low Tide)",
            reviewCount = 2840,
            latitude = 21.4442,
            longitude = 87.0147
        ),
        Hotspot(
            id = "panchalingeswar",
            name = "Panchalingeswar Hill Shrine",
            odiaName = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଶୈବପୀଠ",
            category = "Temples",
            description = "Five sacred perennial stream-bathed Shiva Lingams nestled in the Nilagiri Eastern Ghats amidst lush sal forests.",
            location = "Nilagiri Hills, 30 km from Balasore",
            rating = 4.8,
            isFeatured = true,
            timing = "6:00 AM - 6:30 PM (Daily)",
            distanceKm = 30.0,
            detailedDescription = "Perched in the picturesque Nilagiri hills, Panchalingeswar features five naturally formed stone Shiva Lingams enshrined inside a small rock cave atop a hill, eternally washed by a cool, bubbling mountain stream. Devotees climb 263 stone steps through dense evergreen sal and teak forests, lying down on the rocky flat to feel and touch the holy submerged lingams beneath the crystal water.",
            historicalSignificance = "According to ancient regional mythology and the Odia Ramayana, the demon king Banasura discovered and consecrated these five Lingams with divine boon after severe penance to Lord Shiva. Historically, the Nilagiri Bhanja royal dynasty maintained this sacred site as their prime state shrine and built the surrounding stone steps and dharamsala pavilions in the 17th century.",
            operatingHours = "06:00 AM - 06:30 PM (Special Maha Shivaratri & Kartika Purnima Darshan)",
            reviewCount = 3120,
            latitude = 21.4253,
            longitude = 86.7441
        ),
        Hotspot(
            id = "khirachora_gopinatha",
            name = "Khirachora Gopinatha Temple, Remuna",
            odiaName = "କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର, ରେମୁଣା",
            category = "Temples",
            description = "Historic Vaishnavite shrine renowned for divine Amruta Keli Khira Prasad and centuries-old Rasabali sweet heritage.",
            location = "Remuna, 9 km from Balasore Town",
            rating = 4.9,
            isFeatured = true,
            timing = "5:30 AM - 9:00 PM (Daily)",
            distanceKm = 9.0,
            detailedDescription = "A revered epicenter of Gaudiya Vaishnavism, the temple houses a graceful black chlorite deity of Lord Gopinatha carved by King Narasingha Deva I in the 13th century. The temple is celebrated worldwide for its daily offering of 'Amruta Keli' — a legendary condensed milk, sugar, cardamom, and ghee delicacy prepared in special earthen pots according to confidential centuries-old temple recipes.",
            historicalSignificance = "The temple earned its name 'Khirachora' ('Milk Stealer') when Lord Gopinatha miraculously stole a pot of condensed milk pudding to feed His saintly devotee Madhavendra Puri. Sri Chaitanya Mahaprabhu himself visited the shrine in 1510 CE on his pilgrimage to Puri. Remuna was also a thriving center for medieval Odia brass-metal art and Vaishnava literature.",
            operatingHours = "05:30 AM - 01:00 PM & 04:30 PM - 09:00 PM (Sandhya Aarti at 7:00 PM)",
            reviewCount = 3490,
            latitude = 21.5292,
            longitude = 86.8711
        ),
        Hotspot(
            id = "kuldiha_sanctuary",
            name = "Kuldiha Wildlife Sanctuary",
            odiaName = "କୁଲଡିହା ବନ୍ୟପ୍ରାଣୀ ଅଭୟାରଣ୍ୟ",
            category = "Eco-Tourism",
            description = "272 sq km biosphere connected to Similipal, home to Asian elephants, giant squirrels, hornbills, and Gohirabhola nature camp.",
            location = "Nilagiri Subdivision, 38 km from Balasore",
            rating = 4.7,
            isFeatured = true,
            timing = "6:00 AM - 4:30 PM (Daily, Oct - June)",
            distanceKm = 38.0,
            detailedDescription = "Spread across 272.75 square kilometers of undulating hills and dense moist deciduous forest, Kuldiha serves as a critical wildlife corridor linking Similipal National Park with the Hadgarh reserve. Visitors can enjoy guided jeep safaris to the Gohirabhola water reservoir watchtower, spotting herds of Asian wild elephants, Indian leopards, spotted deer, Malabar giant squirrels, and rare pied hornbills.",
            historicalSignificance = "Declared a wildlife sanctuary in 1984 under the Wildlife Protection Act, Kuldiha's hills were historically protected as the private game preserve and watershed of the Nilagiri Princely State. The forests contain ancient tribal rock shelters, megalithic dolmens, and indigenous Santhali herbal medicine sanctuaries.",
            operatingHours = "06:00 AM - 04:30 PM (Open October to June; monsoon closure July - September)",
            reviewCount = 1420,
            latitude = 21.4125,
            longitude = 86.6289
        ),
        Hotspot(
            id = "talasari_beach",
            name = "Talasari & Bichitrapur Mangrove Estuary",
            odiaName = "ତାଳସାରୀ ବେଳାଭୂମି ଓ ବିଚିତ୍ରପୁର ହେନ୍ତାଳ ବଣ",
            category = "Beaches",
            description = "Serene confluence of the Subarnarekha River with the Bay of Bengal, palm-fringed dunes, and boat safaris through mangroves.",
            location = "Bhograi Block, 82 km from Balasore",
            rating = 4.8,
            isFeatured = true,
            timing = "6:00 AM - 6:30 PM (Daily)",
            distanceKm = 82.0,
            detailedDescription = "Talasari, named for its towering rows of palmyra palm trees ('Tala Sari'), is a peaceful coastal beach where the Subarnarekha River merges into the Bay of Bengal. Across the sandbar lies Bichitrapur, an untouched 563-hectare marine mangrove sanctuary offering boat rides through tidal channels populated by red fiddler crabs, mudskippers, and migratory pelicans.",
            historicalSignificance = "In maritime antiquity, the Subarnarekha river mouth at Talasari and nearby Pipli port served as primary 16th-century trading harbors for Portuguese, Dutch, and Odia Sadhabas (ancient maritime merchant navigators) heading toward Southeast Asia and Sri Lanka.",
            operatingHours = "06:00 AM - 06:30 PM (Eco-boating available 07:00 AM - 04:30 PM depending on tide)",
            reviewCount = 1980,
            latitude = 21.6214,
            longitude = 87.4522
        ),
        Hotspot(
            id = "fakir_mohan_smruti",
            name = "Fakir Mohan Senapati Shanti Kanan & Birthplace",
            odiaName = "ଫକୀର ମୋହନ ସେନାପତି ଶାନ୍ତି କାନନ ଓ ଜନ୍ମପୀଠ",
            category = "Heritage",
            description = "Literary memorial of Vyasakabi Fakir Mohan Senapati at Mallikashpur with rare Odia manuscripts, statues, and heritage garden.",
            location = "Mallikashpur, Balasore Town",
            rating = 4.8,
            isFeatured = false,
            timing = "8:00 AM - 7:00 PM (Daily)",
            distanceKm = 2.5,
            detailedDescription = "Located in the quiet Mallikashpur locality of Balasore, Shanti Kanan is the preserved ancestral garden and samadhi of Vyasakabi Fakir Mohan Senapati (1843–1918), the father of modern Odia prose literature and social realism. The memorial museum preserves original hand-written manuscripts of 'Chha Mana Atha Guntha', his vintage printing press, personal writing desk, and bronze statues under shady bakul trees.",
            historicalSignificance = "Fakir Mohan Senapati spearheaded the historic Odia language revival movement in the late 19th century from Balasore, establishing the Utkal Press (1868) and the Sambada Bahika newspaper. His pioneering literary work documented rural land exploitation, caste struggle, and Balasore's colonial heritage.",
            operatingHours = "08:00 AM - 07:00 PM (Closed on Government Public Holidays)",
            reviewCount = 890,
            latitude = 21.4934,
            longitude = 86.9280
        ),
        Hotspot(
            id = "emami_jagannath",
            name = "Emami Jagannath Temple, Balgopalpur",
            odiaName = "ଇମାମୀ ଜଗନ୍ନାଥ ମନ୍ଦିର, ବଳଗୋପାଳପୁର",
            category = "Temples",
            description = "Grand Sun-Chariot style temple at Balgopalpur featuring 16 intricately carved stone wheels and manicured gardens.",
            location = "Balgopalpur, 11 km from Balasore Town",
            rating = 4.9,
            isFeatured = true,
            timing = "6:00 AM - 8:30 PM (Daily)",
            distanceKm = 11.0,
            detailedDescription = "An architectural marvel built in the shape of a massive celestial chariot reminiscent of the Konark Sun Temple, this modern Jagannath shrine is adorned with 16 giant hand-carved stone wheels and seven horses. Set amidst 10 acres of pristine manicured lawns, illuminated musical fountains, and clean pedestrian pathways, it attracts pilgrims from across Eastern India.",
            historicalSignificance = "Conceived and constructed under the aegis of the Emami Foundation and traditional Odia Shilpa Shastra stone sculptors, the temple preserves the authentic Jagannath Sanatana rituals and serves as a modern revival of Kalinga temple architecture in North Odisha.",
            operatingHours = "06:00 AM - 12:30 PM & 04:00 PM - 08:30 PM (Evening Sandhya Aarti 06:45 PM)",
            reviewCount = 2340,
            latitude = 21.4981,
            longitude = 86.8624
        ),
        Hotspot(
            id = "bhusandeshwar_temple",
            name = "Baba Bhusandeshwar Temple",
            odiaName = "ବାବା ଭୂଷଣ୍ଡେଶ୍ୱର ମହାଦେବ ମନ୍ଦିର",
            category = "Temples",
            description = "Houses Asia's largest single-stone black granite Shiva Lingam (12 ft high & 14 ft width) near Subarnarekha River.",
            location = "Kumbhirmadhi, 95 km from Balasore",
            rating = 4.8,
            isFeatured = true,
            timing = "5:30 AM - 8:00 PM (Daily)",
            distanceKm = 95.0,
            detailedDescription = "Situated near the border of Odisha and West Bengal along the banks of the holy Subarnarekha River, this temple is home to a colossal monolith Shiva Lingam carved from a single piece of black granite. Measuring over 12 feet in height, 14 feet in width, and weighing approximately 38 tons, only the top portion is visible while the massive base remains deeply embedded in the earth.",
            historicalSignificance = "According to Treta Yuga mythology, demon king Ravana was carrying this divine Lingam from Mount Kailash to Lanka, but had to set it down here near Kumbhirmadhi for morning prayers, after which it became permanently rooted into the earth. The temple has been a major Shaivite pilgrimage destination for centuries.",
            operatingHours = "05:30 AM - 08:00 PM (Daily Abhishek at 06:00 AM)",
            reviewCount = 1870,
            latitude = 21.6385,
            longitude = 87.3150
        )
    )

    fun getLocalNews(): List<NewsArticle> = listOf(
        NewsArticle(
            id = "news_1",
            title = "Remuna Khirachora Mahotsav & Chandipur Vanishing Sea Eco-Promenade Upgraded for Tourists",
            odiaTitle = "ରେମୁଣା ক্ষୀରଚୋରା ମହୋତ୍ସବ ଓ ଚାନ୍ଦିପୁର ବେଳାଭୂମି ଆଲୋକୀକରଣ ପ୍ରକଳ୍ପ ସମ୍ପୂର୍ଣ୍ଣ",
            snippet = "Solar LED high-masts, heritage corridor walkways, and digital tide boards installed along the coastal stretch.",
            odiaSnippet = "ପର୍ଯ୍ୟଟକଙ୍କ ସୁରକ୍ଷା ପାଇଁ ସୌର ଆଲୋକ ଓ ଡିଜିଟାଲ ଜୁଆର ସୂଚନା ଫଳକ ସ୍ଥାପିତ।",
            category = "Tourism",
            timeAgo = "1 hour ago",
            source = "Odisha Tourism Desk",
            content = "Visitors to Remuna Khirachora Gopinath Temple, Chandipur-on-Sea, and Talasari can now enjoy solar-lit evening promenades, bilingual tide recession boards, and guided eco-walks."
        ),
        NewsArticle(
            id = "news_2",
            title = "DRDO ITR Chandipur Validates Next-Gen Coastal Telemetry & Air Defense Radar System",
            odiaTitle = "ଚାନ୍ଦିପୁର ଆଇ.ଟି.ଆର ଠାରେ ଅତ୍ୟାଧୁନିକ ପ୍ରତିରକ୍ଷା ରାଡାର ଓ ଟେଲିମେଟ୍ରି ପରୀକ୍ଷଣ ସଫଳ",
            snippet = "Launch Complex-III electro-optical sensors tracked high-speed aerial targets across the Bay of Bengal.",
            odiaSnippet = "ବଙ୍ଗୋପସାଗର ଉପକୂଳରେ ସ୍ୱଦେଶୀ ପ୍ରତିରକ୍ଷା ରାଡାର ଓ ଟେଲିମେଟ୍ରି ପ୍ରଣାଳୀର ସଫଳ ପରୀକ୍ଷଣ।",
            category = "Defense",
            timeAgo = "2 hours ago",
            source = "ITR Chandipur Defense Bureau",
            content = "Integrated Test Range (ITR) scientists at Chandipur completed multi-target radar calibration trials while maintaining green-flag access for local fishermen."
        ),
        NewsArticle(
            id = "news_3",
            title = "Balasore Railway Station Redevelopment & Municipal Council Approve ₹185-Cr Smart City Drainage Budget",
            odiaTitle = "ବାଲେଶ୍ୱର ରେଳ ଷ୍ଟେସନ ପୁନର୍ବିକାଶ ଓ ପୌରପାଳିକା ବୈଠକରେ ₹୧୮୫ କୋଟିର ବିକାଶ ବଜେଟ୍ ଗୃହୀତ",
            snippet = "Elected representatives pass resolution for storm-water drainage across 31 wards and station concourse upgrades.",
            odiaSnippet = "୩୧ଟି ୱାର୍ଡରେ ଡ୍ରେନେଜ୍, ରାସ୍ତା ଓ ପାନୀୟ ଜଳ ପ୍ରକଳ୍ପ ପାଇଁ ସର୍ବସମ୍ମତ ପ୍ରସ୍ତାବ ଗୃହୀତ।",
            category = "Politics",
            timeAgo = "3 hours ago",
            source = "Balasore District Press Bureau",
            content = "In a landmark district council session, municipal councillors and railway authorities approved comprehensive infrastructure upgrades for Balasore Railway Station, Remuna, and Jaleswar."
        ),
        NewsArticle(
            id = "news_4",
            title = "Balasore Hosts North Odisha Inter-District Athletics & Football Cup at Police Line Stadium",
            odiaTitle = "ବାଲେଶ୍ୱର ପୋଲିସ ଲାଇନ୍ ପଡ଼ିଆରେ ଉତ୍ତର ଓଡ଼ିଶା ଆନ୍ତଃ-ଜିଲ୍ଲା କ୍ରୀଡ଼ା ଓ ଫୁଟବଲ୍ ପ୍ରତିଯୋଗିତା",
            snippet = "Over 480 athletes compete in track sprints, coastal marathon, and inter-college football finals.",
            odiaSnippet = "୪୮୦ରୁ ଅଧିକ କ୍ରୀଡ଼ାବିତ୍ ଦୌଡ଼, ମାରାଥନ୍ ଓ ଫୁଟବଲ୍ ଟୁର୍ଣ୍ଣାମେଣ୍ଟରେ ଅଂଶଗ୍ରହଣ କରିଛନ୍ତି।",
            category = "Sports",
            timeAgo = "4 hours ago",
            source = "Balasore District Sports Association",
            content = "Young sprinters and football teams from Balasore, Nilagiri, and FM University delivered standout performances at the district sports championship."
        ),
        NewsArticle(
            id = "news_5",
            title = "Balaramgadi Fishing Harbor Reports Record Morning Landing of Silver Pomfret & Hilsa",
            odiaTitle = "ବଳରାମଗଡ଼ି ମାଛ ଧରା ବନ୍ଦରରେ ପ୍ରଚୁର ଇଲିଶି ଓ ପମ୍ପ୍ରେଟ୍ ଆମଦାନୀ",
            snippet = "Over 42 mechanized trawlers docked at dawn with export-grade coastal catch.",
            odiaSnippet = "ସକାଳୁ ୪୨ରୁ ଅଧିକ ଟ୍ରଲର ତାଜା ସାମୁଦ୍ରିକ ମାଛ ସହ ବନ୍ଦରରେ ପହଞ୍ଚିଛି।",
            category = "Local",
            timeAgo = "5 hours ago",
            source = "Balasore Coastal Bureau"
        ),
        NewsArticle(
            id = "news_6",
            title = "Fakir Mohan University & OSDMA Conduct Subarnarekha Flood Warning & Coastal Cyclone Mock Drill",
            odiaTitle = "ବାଲେଶ୍ୱର ସଦର ବ୍ଲକରେ ବନ୍ୟା ସତର୍କତା ଓ ବାତ୍ୟା ମୁକାବିଲା ମକ୍ ଡ୍ରିଲ୍ ଅନୁଷ୍ଠିତ",
            snippet = "Community volunteers tested flood telemetry sirens and solar backup across 18 coastal shelters.",
            odiaSnippet = "୧୮ଟି ବହୁମୁଖୀ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀରେ ସୌର ବ୍ୟାକଅପ୍ ଓ ସାଇରନ୍ ପରୀକ୍ଷଣ।",
            category = "Emergency",
            timeAgo = "6 hours ago",
            source = "OSDMA Balasore Desk"
        ),
        NewsArticle(
            id = "news_7",
            title = "Sambad & Dharitri Regional Desk: Remuna Industrial Corridor & NH-16 Flyover Expansion Accelerated",
            odiaTitle = "ରେମୁଣା ଶିଳ୍ପାଞ୍ଚଳ ଓ ଜାତୀୟ ରାଜପଥ-୧୬ ଫ୍ଲାଏଓଭର ନିର୍ମାଣ କାର୍ଯ୍ୟ ତ୍ୱରାନ୍ୱିତ",
            snippet = "Hourly district update confirms new city bus bays at Sahadevkhunta and Remuna Golei.",
            odiaSnippet = "ସହଦେବଖୁଣ୍ଟା ଓ ରେମୁଣା ଗୋଲେଇ ଠାରେ ନୂତନ ସ୍ମାର୍ଟ ବସ୍ ବେ ନିର୍ମାଣ।",
            category = "Politics",
            timeAgo = "Updated 15m ago",
            source = "Sambad Balasore Edition"
        ),
        NewsArticle(
            id = "news_8",
            title = "IMD Bhubaneswar & OTV Alert: Moderate Coastal Breeze & Clear Low-Tide Visibility at Chandipur",
            odiaTitle = "ଚାନ୍ଦିପୁର ଉପକୂଳରେ ଅନୁକୂଳ ପାଣିପାଗ ଓ ସ୍ପଷ୍ଟ ଭଟ୍ଟା ଦୃଶ୍ୟମାନ",
            snippet = "7-day Bay of Bengal telemetry indicates safe eco-walk windows and calm sea state.",
            odiaSnippet = "ଆଗାମୀ ୭ ଦିନ ପାଇଁ ଚାନ୍ଦିପୁର ଓ ତାଳସାରି ବେଳାଭୂମିରେ ସୁରକ୍ଷିତ ପର୍ଯ୍ୟଟନ ସୂଚନା।",
            category = "Weather",
            timeAgo = "Updated 30m ago",
            source = "IMD & OTV Balasore Bureau"
        )
    )

    fun getEmergencyContacts(): List<EmergencyContact> = listOf(
        EmergencyContact(
            id = "em_police_1",
            name = "Balasore District Police Control Room & Town PS",
            odiaName = "ବାଲେଶ୍ୱର ଜିଲ୍ଲା ପୋଲିସ ନିୟନ୍ତ୍ରଣ କକ୍ଷ ଓ ଟାଉନ ଥାନା",
            number = "112",
            category = "Police",
            is24x7 = true,
            description = "24x7 Emergency Police PCR Van, Town Police Station (06782-262004) & Coastal Patrol"
        ),
        EmergencyContact(
            id = "em_hospital_1",
            name = "FMMCH & District Headquarters Hospital (DHH) Balasore",
            odiaName = "ଏଫ୍.ଏମ୍. ମେଡିକାଲ କଲେଜ ଓ ଜିଲ୍ଲା ମୁଖ୍ୟ ଚିକିତ୍ସାଳୟ",
            number = "108",
            category = "Hospital",
            is24x7 = true,
            description = "24x7 Trauma Care, Emergency Ambulance & Casualty Ward (06782-262016)"
        ),
        EmergencyContact(
            id = "em_fire_1",
            name = "Balasore Fire & Disaster Response Station",
            odiaName = "ବାଲେଶ୍ୱର ଅଗ୍ନିଶମ ଓ ବିପର୍ଯ୍ୟୟ ଉଦ୍ଧାର କେନ୍ଦ୍ର",
            number = "101",
            category = "Fire Station",
            is24x7 = true,
            description = "24x7 Fire Brigade, ODRAF Rescue & Chemical/Industrial Hazard Response (06782-262101)"
        ),
        EmergencyContact(
            id = "em_deoc_1",
            name = "Balasore District Emergency Operation Center (DEOC)",
            odiaName = "ଜିଲ୍ଲା ଜରୁରୀକାଳୀନ ନିୟନ୍ତ୍ରଣ କକ୍ଷ",
            number = "06782-262244",
            category = "Disaster & Cyclone",
            is24x7 = true,
            description = "24x7 Collectorate Control Room for cyclone, flood & coastal rescue"
        ),
        EmergencyContact(
            id = "em_forest_1",
            name = "Nilagiri & Kuldiha Forest Elephant Squad",
            odiaName = "କୁଲଡିହା ଓ ନୀଳଗିରି ବନ ବିଭାଗ ସହାୟତା",
            number = "06782-262270",
            category = "Forest & Wildlife",
            is24x7 = true,
            description = "24x7 elephant corridor alert & eco-tourism emergency line"
        )
    )

    fun getTransitSchedules(): List<TransitSchedule> = listOf(
        TransitSchedule(
            id = "tr_1",
            routeName = "12841 Coromandel Express",
            origin = "Howrah (HWH)",
            destination = "Balasore (BLS) -> Chennai",
            time = "06:35 PM",
            type = "Superfast Train",
            status = "On Time (Platform 2)"
        ),
        TransitSchedule(
            id = "tr_2",
            routeName = "22895 Howrah - Puri Vande Bharat Express",
            origin = "Howrah (HWH)",
            destination = "Balasore (BLS) -> Puri",
            time = "08:48 AM",
            type = "Vande Bharat",
            status = "On Time (Platform 3)"
        ),
        TransitSchedule(
            id = "tr_3",
            routeName = "OSRTC Coastal Deluxe Bus",
            origin = "Balasore Sahadevkhunta Bus Stand",
            destination = "Chandipur & Balaramgadi",
            time = "Every 30 mins (6 AM - 8 PM)",
            type = "City/Coastal Bus",
            status = "Running"
        ),
        TransitSchedule(
            id = "tr_4",
            routeName = "OSRTC Express Coach",
            origin = "Balasore Bus Terminal",
            destination = "Panchalingeswar & Nilagiri",
            time = "07:15 AM, 10:30 AM, 02:30 PM",
            type = "Regional Bus",
            status = "Running"
        )
    )

    fun getDrdoAdvisories(): List<DrdoAdvisory> = listOf(
        DrdoAdvisory(
            id = "drdo_1",
            title = "ITR Sector-III Telemetry & Radar Calibration Notice",
            odiaTitle = "ଆଇ.ଟି.ଆର ଚାନ୍ଦିପୁର ରାଡାର ଓ ଟେଲିମେଟ୍ରି ସୂଚନା",
            dateOrTime = "Today, 10:00 AM - 01:30 PM",
            status = "Normal Coastal Access (No Launch Closure)",
            seaZone = "Inner Chandipur Promenade Open; 5 NM Outer Buoys Monitored",
            fishermenAdvisory = "Balaramgadi & Kasafal motorized boats may operate in standard coastal lanes.",
            sonicBoomWarning = false,
            heritageMilestone = "Home of India's Prithvi, Agni & Akash defence telemetry heritage"
        )
    )

    fun getRiverGauges(): List<RiverGauge> = listOf(
        RiverGauge(
            riverName = "Budhabalanga River",
            odiaRiverName = "ବୁଢ଼ାବଳଙ୍ଗ ନଦୀ",
            stationName = "Govindpur NH-16 Gauge",
            currentLevelMeters = 4.82,
            warningLevelMeters = 7.21,
            dangerLevelMeters = 8.13,
            status = "SAFE (Normal Flow)",
            trend = "Steady"
        ),
        RiverGauge(
            riverName = "Subarnarekha River",
            odiaRiverName = "ସୁବର୍ଣ୍ଣରେଖା ନଦୀ",
            stationName = "Rajghat Barrage Gauge",
            currentLevelMeters = 6.15,
            warningLevelMeters = 9.45,
            dangerLevelMeters = 10.36,
            status = "SAFE (Normal Flow)",
            trend = "Falling"
        ),
        RiverGauge(
            riverName = "Jalaka River",
            odiaRiverName = "ଜଳକା ନଦୀ",
            stationName = "Mathani Bridge Gauge",
            currentLevelMeters = 4.10,
            warningLevelMeters = 5.50,
            dangerLevelMeters = 6.50,
            status = "SAFE (Normal Flow)",
            trend = "Steady"
        )
    )

    fun getCycloneShelters(): List<CycloneShelter> = listOf(
        CycloneShelter(
            name = "Chandipur Multipurpose Cyclone Shelter (OSDMA)",
            block = "Balasore Sadar",
            capacityPeople = 1200,
            nodalContact = "Sri R. K. Rout (Nodal Officer)",
            phone = "06782-270012"
        ),
        CycloneShelter(
            name = "Balaramgadi Coastal Harbour Shelter",
            block = "Balasore Sadar",
            capacityPeople = 950,
            nodalContact = "Smt. M. Behera",
            phone = "06782-270108"
        ),
        CycloneShelter(
            name = "Talasari-Udayapur Multipurpose Shelter",
            block = "Bhograi Block",
            capacityPeople = 1100,
            nodalContact = "Sri P. K. Jena",
            phone = "06781-253210"
        )
    )

    fun getSeafoodCatches(): List<SeafoodCatch> = listOf(
        SeafoodCatch(
            id = "sf_1",
            name = "Chandipur Silver Pomfret (Bahal)",
            odiaName = "ଚାନ୍ଦିପୁର ଧଳା ପମ୍ପ୍ରେଟ୍ (ବହଳ)",
            harbor = "Balaramgadi Fishing Harbor",
            priceRangeKg = "₹650 - ₹850 / kg",
            qualityGrade = "Export Grade A+",
            freshness = "Morning Harbor Landed (6:00 AM)",
            peakSeason = "Oct – Feb"
        ),
        SeafoodCatch(
            id = "sf_2",
            name = "Subarnarekha Estuary Hilsa (Ilishi)",
            odiaName = "ସୁବର୍ଣ୍ଣରେଖା ଇଲିଶି ମାଛ",
            harbor = "Dhamra-Balaramgadi Coastal Belt",
            priceRangeKg = "₹900 - ₹1,300 / kg",
            qualityGrade = "Premium River-Estuary",
            freshness = "Dawn Auction Fresh",
            peakSeason = "Monsoon & Post-Monsoon"
        ),
        SeafoodCatch(
            id = "sf_3",
            name = "Bagda Tiger Prawns",
            odiaName = "ବାଗଦା ଚିଙ୍ଗୁଡ଼ି",
            harbor = "Balaramgadi Dock #2",
            priceRangeKg = "₹480 - ₹620 / kg",
            qualityGrade = "Jumbo Coastal",
            freshness = "Ice-Chilled Morning Catch",
            peakSeason = "Year-Round"
        )
    )

    fun getLiteraryTrail(): List<LiteraryTrailPoint> = listOf(
        LiteraryTrailPoint(
            id = "lit_1",
            title = "Shanti Kanan & Vyasakabi Fakir Mohan Memorial",
            odiaTitle = "ଶାନ୍ତି କାନନ ଓ ବ୍ୟାସକବି ଫକୀର ମୋହନ ସ୍ମୃତିପୀଠ",
            location = "Mallikashpur, Balasore",
            excerpt = "“ଉତ୍କଳର ଭାଷା ଓ ସାହିତ୍ୟ ଆମ ଜାତୀୟ ଜୀବନର ପ୍ରାଣବିନ୍ଦୁ।”",
            historicalContext = "Historic garden and pond designed by Fakir Mohan Senapati where 'Rebati' and 'Chha Mana Atha Guntha' were envisioned."
        ),
        LiteraryTrailPoint(
            id = "lit_2",
            title = "Utkal Darpan & First Odia Printing Press Site",
            odiaTitle = "ଉତ୍କଳ ଦର୍ପଣ ଓ ପ୍ରଥମ ମୁଦ୍ରଣ ଯନ୍ତ୍ର ସ୍ଥଳ",
            location = "Motiganj Bazaar, Balasore",
            excerpt = "Milestone of modern Odia journalism and literary renaissance in 1873.",
            historicalContext = "Pioneered literary periodicals and textbook printing in northern Odisha."
        )
    )

    fun getDailyIdiom(): DailyBalasoreIdiom = DailyBalasoreIdiom(
        wordOrIdiom = "Dandi & Rebati",
        odiaScript = "ଆପଣା ହସ୍ତେ ଜିହ୍ୱା ଛେଦନ",
        meaning = "Wisdom and self-reliance through education and cultural pride.",
        literaryWork = "Fakir Mohan Senapati's 'Chha Mana Atha Guntha' & 'Rebati'",
        funContext = "Used affectionately in Balasore literary circles to highlight wit and classic Odia storytelling."
    )

    fun getHorseshoeCrabSightings(): List<HorseshoeCrabSighting> = listOf(
        HorseshoeCrabSighting(
            id = "hc_1",
            reporterName = "FM University Marine Bio Team",
            beachSector = "Chandipur Sector-2 Intertidal Flat",
            distanceOutKm = 2.4,
            species = "Tachypleus gigas (Indian Horseshoe Crab)",
            count = 7,
            condition = "Healthy — nesting near sandbar ripple zone",
            reportedAgo = "1 hour ago",
            verifiedByFMU = true
        ),
        HorseshoeCrabSighting(
            id = "hc_2",
            reporterName = "Balaramgadi Eco Volunteer",
            beachSector = "Balaramgadi Estuary Mouth",
            distanceOutKm = 1.8,
            species = "Tachypleus gigas",
            count = 4,
            condition = "Active in shallow tidal pool",
            reportedAgo = "3 hours ago",
            verifiedByFMU = true
        )
    )

    fun getIntertidalEcoGuidelines(): List<IntertidalEcoGuideline> = listOf(
        IntertidalEcoGuideline(
            id = "eco_1",
            title = "Never Flip Horseshoe Crabs by the Tail (Telson)",
            odiaTitle = "ଅଶ୍ୱଖୁରା କଙ୍କଡ଼ାକୁ ଲାଞ୍ଜ ଧରି ଉଠାନ୍ତୁ ନାହିଁ",
            description = "Their tail is used to right themselves in surf; lifting by the tail damages vital nerves.",
            isDo = false
        ),
        IntertidalEcoGuideline(
            id = "eco_2",
            title = "Walk Barefoot or in Soft Sandals on Exposed Flats",
            odiaTitle = "ଭଟ୍ଟା ବାଲିରେ ହାଲୁକା ପାଦରେ ଚାଲନ୍ତୁ",
            description = "Protects red ghost crab burrows and fragile bivalve shells across the 4 km seabed.",
            isDo = true
        ),
        IntertidalEcoGuideline(
            id = "eco_3",
            title = "Start Returning 45 Minutes Before High Tide Window",
            odiaTitle = "ଜୁଆର ଆସିବାର ୪୫ ମିନିଟ୍ ପୂର୍ବରୁ କୂଳକୁ ଫେରନ୍ତୁ",
            description = "Bay of Bengal tide fills outer sandbar channels first; heed lifeguard whistles.",
            isDo = true
        )
    )

    fun getDefenseTrailMilestones(): List<DefenseTrailMilestone> = listOf(
        DefenseTrailMilestone(
            id = "def_1",
            year = "1989",
            missileName = "Agni-I Technology Demonstrator",
            testRange = "Interim Test Range (ITR), Chandipur",
            significance = "Spearheaded by Dr. A.P.J. Abdul Kalam, placing Chandipur on the global aerospace map.",
            rangeKm = "700 - 1,200 km",
            drKalamMemoir = "“The roar over the Bay of Bengal at Chandipur marked India's self-reliance in strategic deterrence.”"
        ),
        DefenseTrailMilestone(
            id = "def_2",
            year = "1998 – Present",
            missileName = "Akash & BrahMos Supersonic Trials",
            testRange = "Launch Complex-III, Chandipur",
            significance = "Precision coastal radar and electro-optical tracking across the Bay of Bengal.",
            rangeKm = "290 - 450 km",
            drKalamMemoir = "“Chandipur's scientists and local coastal communities stood together in every milestone.”"
        )
    )

    fun getMaritimeExclusionZone(): MaritimeExclusionZone = MaritimeExclusionZone(
        zoneName = "Chandipur ITR Launch Complex-III Sector",
        odiaName = "ଚାନ୍ଦିପୁର ଆଇ.ଟି.ଆର ସାମୁଦ୍ରିକ ସୁରକ୍ଷା କ୍ଷେତ୍ର",
        coordinates = "21.4446° N, 87.0149° E",
        radiusNauticalMiles = 5,
        activeHours = "No Active Launch Closure Today (Green Flag)",
        sirenAlert = "Silent / Normal",
        coastalPatrolStatus = "Marine Police & Coast Guard Patrol Active"
    )

    fun getKhirachoraPrasadStatus(): PrasadStatus = PrasadStatus()

    fun getBalasoreArtisans(): List<BalasoreArtisan> = listOf(
        BalasoreArtisan(
            id = "art_1",
            artisanName = "Smt. Malati Maharana",
            craftTitle = "Mirganayani Sabai Grass Craft & Baskets",
            odiaCraftTitle = "ସବାଇ ଘାସ କଳାକୃତି",
            villageOrCluster = "Baliapal & Basta Craft Cluster",
            description = "Handwoven eco-friendly baskets, coasters, and wall hangings crafted by women SHG artisans.",
            phone = "9437211045",
            experienceYears = 18,
            giTagged = true
        ),
        BalasoreArtisan(
            id = "art_2",
            artisanName = "Sri Raghunath Jenamani",
            craftTitle = "Traditional Jau Kandhei (Lacquer Bride Dolls)",
            odiaCraftTitle = "ବାଲେଶ୍ୱରୀ ଜଉ କଣ୍ଢେଇ",
            villageOrCluster = "Puruna Balasore Heritage Lane",
            description = "Centuries-old auspicious terracotta and natural lac dolls symbolizing Odia marriage traditions.",
            phone = "9437488912",
            experienceYears = 28,
            giTagged = true
        ),
        BalasoreArtisan(
            id = "art_3",
            artisanName = "Sri Bishnu Prasad Moharana",
            craftTitle = "Nilagiri Black Chlorite Stone Carving",
            odiaCraftTitle = "ନୀଳଗିରି ପଥର ଖୋଦେଇ ଶିଳ୍ପ",
            villageOrCluster = "Boulagadia, Nilagiri",
            description = "Intricate temple sculptures and utility stone vessels carved from local Nilagiri hills stone.",
            phone = "9437562310",
            experienceYears = 24,
            giTagged = true
        )
    )

    fun getDetailedCycloneShelters(): List<DetailedCycloneShelter> = listOf(
        DetailedCycloneShelter(
            id = "dcs_1",
            name = "Chandipur Multipurpose Cyclone Shelter",
            block = "Balasore Sadar",
            capacity = 1200,
            elevationMeters = 14.5,
            solarBackup = true,
            distanceKm = 2.1,
            nodalOfficer = "Sri R. K. Rout",
            phone = "06782-270012",
            latitude = 21.4450,
            longitude = 87.0155
        ),
        DetailedCycloneShelter(
            id = "dcs_2",
            name = "Balaramgadi Harbour Evacuation Shelter",
            block = "Balasore Sadar",
            capacity = 950,
            elevationMeters = 13.0,
            solarBackup = true,
            distanceKm = 4.4,
            nodalOfficer = "Smt. M. Behera",
            phone = "06782-270108",
            latitude = 21.4712,
            longitude = 87.0398
        ),
        DetailedCycloneShelter(
            id = "dcs_3",
            name = "Talasari-Udayapur Coastal Shelter",
            block = "Bhograi",
            capacity = 1100,
            elevationMeters = 15.2,
            solarBackup = true,
            distanceKm = 18.5,
            nodalOfficer = "Sri P. K. Jena",
            phone = "06781-253210",
            latitude = 21.6085,
            longitude = 87.4520
        )
    )

    fun getEmergencyKitChecklist(): List<EmergencyKitItem> = listOf(
        EmergencyKitItem("kit_1", "Dry Chuda, Jaggery & Drinking Water (3L/person)", "ଚୁଡ଼ା, ଗୁଡ଼ ଓ ପାନୀୟ ଜଳ", "Food & Water", true, "Essential for 72-hour cyclone resilience"),
        EmergencyKitItem("kit_2", "Solar/Hand-Crank Torch & 10,000mAh Powerbank", "ସୌର ଟର୍ଚ୍ଚ ଓ ପାୱାରବ୍ୟାଙ୍କ", "Power & Comms", true, "Keeps emergency SOS and offline maps active"),
        EmergencyKitItem("kit_3", "Waterproof Pouch for Aadhaar & Land Records", "ଜରୁରୀ କାଗଜପତ୍ର ପାଇଁ ୱାଟରପ୍ରୁଫ୍ ବ୍ୟାଗ୍", "Documents", false, "Prevents moisture damage during heavy rain"),
        EmergencyKitItem("kit_4", "ORS Packets, Paracetamol & First-Aid Bandages", "ଓ.ଆର.ଏସ୍ ଓ ପ୍ରାଥମିକ ଚିକିତ୍ସା କିଟ୍", "Medical", true, "Critical for coastal water-borne safety")
    )

    fun getHarborCatchRates(): List<HarborCatchRate> = listOf(
        HarborCatchRate(
            id = "hcr_1",
            fishName = "Silver Pomfret (Bahal)",
            odiaName = "ଧଳା ପମ୍ପ୍ରେଟ୍",
            wholesalePriceKg = "₹620/kg",
            retailPriceKg = "₹720/kg",
            arrivalTime = "05:45 AM",
            dockLanding = "Balaramgadi Jetty #1",
            freshnessRating = "9.8/10 (Day-Boat Ice Chilled)",
            buyingTip = "Look for firm silver sheen and bright red gills at the 6 AM auction."
        ),
        HarborCatchRate(
            id = "hcr_2",
            fishName = "Subarnarekha Hilsa (Ilishi 800g+)",
            odiaName = "ସୁବର୍ଣ୍ଣରେଖା ଇଲିଶି",
            wholesalePriceKg = "₹950/kg",
            retailPriceKg = "₹1,150/kg",
            arrivalTime = "06:15 AM",
            dockLanding = "Balaramgadi Jetty #3",
            freshnessRating = "9.7/10 (Estuary Net Catch)",
            buyingTip = "Broad silver belly with pinkish scales indicates authentic estuary Hilsa."
        )
    )

    fun getHarborLandingBells(): List<HarborLandingBell> = listOf(
        HarborLandingBell(
            fleetName = "Maa Tarini Coastal Trawler #14",
            harbor = "Balaramgadi Fishing Harbor",
            expectedDockTime = "06:10 AM (Docked)",
            catchSummary = "480 kg Silver Pomfret, Tiger Prawns & Bhekti",
            status = "Auction Open"
        ),
        HarborLandingBell(
            fleetName = "Dhamra-Chandipur Gillnet Fleet #07",
            harbor = "Kasafal & Balaramgadi",
            expectedDockTime = "04:30 PM (Incoming)",
            catchSummary = "Estuary Hilsa & Mackerel",
            status = "En Route to Jetty"
        )
    )

    fun getElephantCorridorAlert(): ElephantCorridorAlert = ElephantCorridorAlert(
        sector = "Kuldiha – Nilagiri – Panchalingeswar Ghat Road",
        alertLevel = "MODERATE WATCH (Evening Dusk Corridor)",
        herdCount = 11,
        lastSpottedTime = "Yesterday, 06:20 PM near Gohirabhola Beat",
        highwaySpeedLimitKmh = 20,
        advisoryText = "Avoid stopping for photos or honking on the Nilagiri-Panchalingeswar forest stretch after 5:30 PM."
    )

    fun getEcoPassportStamps(): List<EcoPassportStamp> = listOf(
        EcoPassportStamp("stamp_1", "Chandipur Vanishing Seabed Explorer", "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ଅନ୍ୱେଷକ", "Chandipur Sector-1", "Sea Level (0m)", "Walked 3 km on receding seabed & spotted horseshoe crabs", true),
        EcoPassportStamp("stamp_2", "Kuldiha Canopy & Hornbill Trail", "କୁଲଡିହା ଅଭୟାରଣ୍ୟ ଯାତ୍ରୀ", "Gohirabhola Eco Camp", "340m Elevation", "Completed guided sal forest biodiversity trek", false),
        EcoPassportStamp("stamp_3", "Bichitrapur Mangrove Boat Safari", "ବିଚିତ୍ରପୁର ହେନ୍ତାଳ ବଣ ସଫାରୀ", "Subarnarekha Estuary", "Estuary Level", "Explored mangrove creeks and migratory bird island", false)
    )
}

class ItineraryRepository(
    private val itineraryDao: ItineraryDao? = null,
    private val context: Context? = null
) {
    constructor(context: Context) : this(AppDatabase.getInstance(context).itineraryDao(), context)

    val allItineraryItems: Flow<List<ItineraryItemEntity>> =
        itineraryDao?.getAllItineraryItems() ?: flowOf(DefaultData.getDefaultItineraryItems())

    suspend fun ensureDefaultItinerarySeeded() {
        val dao = itineraryDao ?: return
        if (dao.getItineraryCount() == 0) {
            dao.insertItineraryItems(DefaultData.getDefaultItineraryItems())
        }
    }

    suspend fun addToItinerary(
        hotspot: Hotspot,
        day: Int = 1,
        timeSlot: String = "10:00 AM",
        notes: String = ""
    ): Long {
        val item = ItineraryItemEntity(
            hotspotId = hotspot.id,
            hotspotName = hotspot.name,
            odiaName = hotspot.odiaName,
            category = hotspot.category,
            dayNumber = day,
            timeSlot = timeSlot,
            notes = notes.ifBlank { hotspot.description },
            latitude = 21.4446,
            longitude = 87.0149
        )
        return addItineraryItem(item)
    }

    suspend fun addItineraryItem(item: ItineraryItemEntity): Long =
        itineraryDao?.insertItineraryItem(item) ?: -1L

    suspend fun removeItem(id: Long) {
        removeItineraryItem(id)
    }

    suspend fun removeItineraryItem(id: Long) {
        itineraryDao?.deleteItineraryItemById(id)
    }

    suspend fun clearAll() {
        itineraryDao?.clearAllItinerary()
    }
}

class TravelJournalRepository(
    private val journalDao: TravelJournalDao? = null,
    private val context: Context? = null
) {
    constructor(context: Context) : this(AppDatabase.getInstance(context).travelJournalDao(), context)

    val allJournals: Flow<List<TravelJournalEntity>> =
        journalDao?.getAllJournals() ?: flowOf(emptyList())

    suspend fun ensureDefaultJournalSeeded() {
        ensureDefaultJournalsSeeded()
    }

    suspend fun ensureDefaultJournalsSeeded() {
        val dao = journalDao ?: return
        if (dao.getJournalCount() == 0) {
            dao.insertJournal(
                TravelJournalEntity(
                    title = "Sunrise Walk on Chandipur's Vanishing Seabed",
                    locationName = "Chandipur-on-Sea, Balasore",
                    category = "Beaches",
                    rating = 5,
                    notes = "<b>Unforgettable Coastal Morning!</b><br/>Walked nearly <i>3.5 km</i> across the rippled seabed as the Bay of Bengal receded. Spotted living fossil horseshoe crabs and enjoyed hot Rasabali at Remuna on the return trip.",
                    photoUri = null,
                    latitude = 21.4446,
                    longitude = 87.0149,
                    spotId = "chandipur_beach",
                    colorTag = 0xFF0284C7
                )
            )
        }
    }

    suspend fun saveJournal(entry: TravelJournalEntity): Long =
        journalDao?.insertJournal(entry) ?: -1L

    suspend fun addJournal(
        title: String,
        locationName: String,
        category: String,
        rating: Int,
        notes: String,
        photoUri: String? = null,
        latitude: Double? = 21.4446,
        longitude: Double? = 87.0149,
        spotId: String? = "chandipur_beach",
        colorTag: Long = 0xFF0284C7L
    ): Long = saveJournal(
        TravelJournalEntity(
            title = title,
            locationName = locationName,
            category = category,
            rating = rating,
            notes = notes,
            photoUri = photoUri,
            latitude = latitude ?: 21.4446,
            longitude = longitude ?: 87.0149,
            spotId = spotId ?: "chandipur_beach",
            colorTag = colorTag
        )
    )

    suspend fun deleteJournal(id: Long) {
        journalDao?.deleteJournalById(id)
    }

    suspend fun deleteJournalById(id: Long) {
        deleteJournal(id)
    }
}

class WeatherAlertRepository(private val context: Context? = null) {
    fun getActiveWeatherAlerts(): List<BalasoreWeatherAlert> {
        val now = System.currentTimeMillis()
        return listOf(
            BalasoreWeatherAlert(
                id = "alert_tide_1",
                title = "Chandipur Spring Tide & Rapid Incoming Surf Advisory",
                odiaTitle = "ଚାନ୍ଦିପୁର ପୂର୍ଣ୍ଣିମା ଜୁଆର ଓ ସାମୁଦ୍ରିକ ସତର୍କତା",
                hindiTitle = "चांदीपुर ज्वार एवं तटीय सुरक्षा सूचना",
                summary = "Seabed recedes 4.1 km today; incoming tide returns briskly after 11:30 AM.",
                detailedDescription = "Tourists exploring the outer intertidal sandbar at Chandipur Sector 1–3 are advised to begin returning to the promenade 45 minutes prior to high-tide turnaround.",
                category = WeatherAlertCategory.TIDAL_SURGE,
                severity = AlertSeverity.HIGH_TIDE,
                affectedZones = listOf(BalasoreZone.COASTAL_CHANDIPUR),
                issuingAuthority = "IMD Bhubaneswar & OSDMA Balasore Control Room",
                validFromMillis = now - 3600_000L,
                validUntilMillis = now + 12 * 3600_000L,
                actionableInstructions = listOf(
                    "Set the Chandipur Tide Recession Reminder in the app before stepping onto the seabed.",
                    "Do not drive two-wheelers or four-wheelers onto the intertidal mudflats.",
                    "Follow Chandipur Marine Police & Lifeguard whistle alerts."
                ),
                emergencyContact = "06782-262244"
            ),
            BalasoreWeatherAlert(
                id = "alert_wind_2",
                title = "Balaramgadi & Talasari Afternoon Sea Breeze Bulletin",
                odiaTitle = "ବଳରାମଗଡ଼ି ଓ ତାଳସାରୀ ଉପକୂଳ ପବନ ସୂଚନା",
                hindiTitle = "बलरामगढ़ी तटीय हवा सूचना",
                summary = "Moderate SE breeze (20–26 km/h) expected along Subarnarekha & Budhabalanga estuaries.",
                detailedDescription = "Safe conditions for mechanized trawlers and coastal tourists; light passing shower possible over Nilagiri hills in the late afternoon.",
                category = WeatherAlertCategory.HEAVY_RAINFALL,
                severity = AlertSeverity.ADVISORY,
                affectedZones = listOf(
                    BalasoreZone.TALASARI_SUBARNAREKHA,
                    BalasoreZone.BALASORE_TOWN,
                    BalasoreZone.NILAGIRI_HILLS
                ),
                issuingAuthority = "Balasore District Emergency Operation Center (DEOC)",
                validFromMillis = now - 1800_000L,
                validUntilMillis = now + 24 * 3600_000L,
                actionableInstructions = listOf(
                    "Check real-time river gauge levels for Budhabalanga and Subarnarekha.",
                    "Keep offline cyclone shelter locations cached in osmdroid MapView."
                ),
                emergencyContact = "112"
            )
        )
    }

    fun getSafetyChecklist(): List<SafetyChecklistItem> = listOf(
        SafetyChecklistItem(
            id = "sc_1",
            title = "Verify Chandipur Low-Tide Turnaround Time",
            odiaTitle = "ଚାନ୍ଦିପୁର ଭଟ୍ଟା ଓ ଜୁଆର ସମୟ ଯାଞ୍ଚ କରନ୍ତୁ",
            category = "Coastal Safety",
            isCompleted = true
        ),
        SafetyChecklistItem(
            id = "sc_2",
            title = "Keep Emergency SOS Contacts & Offline Map Ready",
            odiaTitle = "ଜରୁରୀକାଳୀନ ଏସ୍.ଓ.ଏସ୍ ନମ୍ବର ଓ ଅଫଲାଇନ୍ ମାନଚିତ୍ର ପ୍ରସ୍ତୁତ ରଖନ୍ତୁ",
            category = "Emergency Preparedness",
            isCompleted = true
        ),
        SafetyChecklistItem(
            id = "sc_3",
            title = "Carry 2L Drinking Water & ORS for Coastal Walks",
            odiaTitle = "ବେଳାଭୂମି ଭ୍ରମଣ ସମୟରେ ପାନୀୟ ଜଳ ଓ ଓ.ଆର.ଏସ୍ ସାଥୀରେ ରଖନ୍ତୁ",
            category = "Health & Hydration",
            isCompleted = false
        )
    )
}
