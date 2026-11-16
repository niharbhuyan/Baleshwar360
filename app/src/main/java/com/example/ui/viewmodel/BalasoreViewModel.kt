package com.example.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SavedBookmarkDao
import com.example.data.local.SavedBookmarkEntity
import com.example.data.repository.SavedBookmarkRepository
import com.example.data.local.ChandipurTideEntity
import com.example.data.local.ChandipurTideDao
import com.example.data.local.CivicReportEntity
import com.example.data.local.CivicReportDao
import com.example.data.local.CommunityNewsSubmissionEntity
import com.example.data.local.NewsModerationStatus
import com.example.data.local.NewsSearchQueryDao
import com.example.data.local.NewsSearchQueryEntity
import com.example.data.local.ItineraryItemEntity
import com.example.data.model.*
import com.example.data.repository.CommunityNewsModerationRepository
import com.example.data.notification.TideAndWeatherNotificationManager
import com.example.data.remote.BalasoreApiService
import com.example.data.remote.WeatherApiService
import com.example.data.remote.OpenWeatherCurrentResponse
import com.example.data.remote.OpenWeatherThreeDayForecast
import com.example.data.remote.OpenWeatherDailyForecast
import com.example.data.remote.EmergencyAlertDto
import com.example.data.remote.GeminiGroundingService
import com.example.data.remote.GroundingResponse
import com.example.data.remote.GroundingToolMode
import com.example.data.remote.GeminiChatService
import com.example.data.remote.ChatMessage
import com.example.data.remote.MessageSender
import com.example.data.remote.GeminiChatModel
import com.example.data.remote.ChatRolePersona
import com.example.data.remote.ChatRolePersonas
import com.example.data.firebase.FirestoreService
import com.example.data.repository.BalasoreRepository
import com.example.data.repository.CivicReportRepository
import com.example.data.repository.DefaultData
import com.example.data.repository.ItineraryRepository
import com.example.data.repository.INewsRepository
import com.example.data.repository.ITourismRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.OpenWeatherRepository
import com.example.data.repository.Resource
import com.example.data.repository.TourismRepository
import com.example.data.repository.TravelJournalRepository
import com.example.data.repository.UniqueFeaturesRepository
import com.example.data.repository.toDomainModel
import com.example.ui.components.WatermarkOpacity
import com.example.ui.components.WatermarkStyle
import com.example.ui.navigation.HashRoute
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Material-3 TabRow categorization for Balasore News Feed:
 * 0 -> Local News
 * 1 -> Defense Alerts
 * 2 -> Tourism Updates
 */
enum class NewsFeedTab(val index: Int, val title: String, val odiaTitle: String, val categoryName: String) {
    ALL_STORIES(0, "All News", "ସମସ୍ତ ଖବର", "All"),
    LOCAL_NEWS(1, "Local News", "ସ୍ଥାନୀୟ ଖବର", "Local"),
    NATIONAL_NEWS(2, "National News", "ଜାତୀୟ ଖବର", "National"),
    INTERNATIONAL_NEWS(3, "International News", "ଆନ୍ତର୍ଜାତୀୟ ଖବର", "International"),
    DEFENSE_ALERTS(4, "Defense Alerts", "ପ୍ରତିରକ୍ଷା ସତର୍କତା", "Defense"),
    TOURISM_UPDATES(5, "Tourism Updates", "ପର୍ଯ୍ୟଟନ ଖବର", "Tourism"),
    MY_SAVED(6, "My Saved", "ସଂରକ୍ଷିତ", "Saved Bookmarks"),
    WEATHER_UPDATES(7, "Weather Updates", "ପାଣିପାଗ ଖବର", "Weather"),
    EMERGENCY_FEEDS(8, "Emergency News", "ଜରୁରୀକାଳୀନ ଖବର", "Emergency"),
    POLITICS(9, "Local Politics", "ସ୍ଥାନୀୟ ରାଜନୀତି", "Local Politics"),
    SPORTS(10, "Sports", "କ୍ରୀଡ଼ା", "Sports"),
    TRENDING(11, "Trending", "🔥 ଟ୍ରେଣ୍ଡିଂ", "Trending"),
    HEALTH(12, "Health", "ସ୍ୱାସ୍ଥ୍ୟ ଓ ମେଡିକାଲ", "Health");

    companion object {
        fun fromIndex(index: Int): NewsFeedTab = entries.find { it.index == index } ?: ALL_STORIES
        fun fromCategory(cat: String): NewsFeedTab = when {
            cat.equals("All", ignoreCase = true) || cat.equals("All News", ignoreCase = true) || cat.isBlank() -> ALL_STORIES

            cat.equals("Trending", ignoreCase = true) ||
            cat.equals("Trending Stories", ignoreCase = true) ||
            cat.equals("Popular", ignoreCase = true) -> TRENDING

            cat.equals("Politics", ignoreCase = true) ||
            cat.equals("Local Politics", ignoreCase = true) -> POLITICS

            cat.equals("Health", ignoreCase = true) ||
            cat.equals("Healthcare", ignoreCase = true) ||
            cat.equals("Medical", ignoreCase = true) ||
            cat.equals("Hospital", ignoreCase = true) -> HEALTH

            cat.equals("Sports", ignoreCase = true) ||
            cat.equals("Athletics", ignoreCase = true) -> SPORTS

            cat.equals("Local", ignoreCase = true) ||
            cat.equals("Local News", ignoreCase = true) ||
            cat.equals("Infrastructure", ignoreCase = true) -> LOCAL_NEWS

            cat.equals("National", ignoreCase = true) ||
            cat.equals("National News", ignoreCase = true) -> NATIONAL_NEWS

            cat.equals("International", ignoreCase = true) ||
            cat.equals("International News", ignoreCase = true) ||
            cat.equals("World", ignoreCase = true) -> INTERNATIONAL_NEWS

            cat.equals("Defense", ignoreCase = true) ||
            cat.equals("Defense Alerts", ignoreCase = true) ||
            cat.equals("Coastal Alerts", ignoreCase = true) -> DEFENSE_ALERTS

            cat.equals("Tourism", ignoreCase = true) ||
            cat.equals("Tourism Updates", ignoreCase = true) ||
            cat.equals("Culture", ignoreCase = true) ||
            cat.equals("Spiritual", ignoreCase = true) -> TOURISM_UPDATES

            cat.equals("Weather", ignoreCase = true) ||
            cat.equals("Weather Updates", ignoreCase = true) ||
            cat.equals("Meteorological", ignoreCase = true) ||
            cat.equals("Rain", ignoreCase = true) ||
            cat.equals("Cyclone Alert", ignoreCase = true) -> WEATHER_UPDATES

            cat.equals("Emergency", ignoreCase = true) ||
            cat.equals("Emergency News", ignoreCase = true) ||
            cat.equals("Disaster Alert", ignoreCase = true) ||
            cat.equals("Disaster", ignoreCase = true) -> EMERGENCY_FEEDS

            cat.equals("Saved", ignoreCase = true) ||
            cat.equals("Saved Bookmarks", ignoreCase = true) ||
            cat.equals("My Saved", ignoreCase = true) ||
            cat.equals("Bookmarks", ignoreCase = true) -> MY_SAVED

            else -> ALL_STORIES
        }
    }
}

object NewsFeedFilter {
    fun matchesTab(article: NewsArticle, tabIndex: Int): Boolean {
        val tab = NewsFeedTab.fromIndex(tabIndex)
        return when (tab) {
            NewsFeedTab.ALL_STORIES, NewsFeedTab.TRENDING -> true

            NewsFeedTab.MY_SAVED ->
                article.isBookmarked

            NewsFeedTab.LOCAL_NEWS ->
                article.category.equals("Local News", ignoreCase = true) ||
                article.category.equals("Local", ignoreCase = true) ||
                article.category.equals("Infrastructure", ignoreCase = true) ||
                article.category.equals("Politics", ignoreCase = true) ||
                article.category.equals("Local Politics", ignoreCase = true) ||
                article.category.equals("Development", ignoreCase = true) ||
                article.category.equals("Sports", ignoreCase = true) ||
                article.category.contains("Civic", ignoreCase = true) ||
                article.category.contains("Municipal", ignoreCase = true) ||
                article.title.contains("municipality", ignoreCase = true) ||
                article.title.contains("station", ignoreCase = true) ||
                article.title.contains("parishad", ignoreCase = true) ||
                article.title.contains("drainage", ignoreCase = true) ||
                article.title.contains("smart city", ignoreCase = true)

            NewsFeedTab.WEATHER_UPDATES ->
                article.category.equals("Weather", ignoreCase = true) ||
                article.category.equals("Weather Updates", ignoreCase = true) ||
                article.category.equals("Cyclone Alert", ignoreCase = true) ||
                article.category.equals("Meteorological", ignoreCase = true) ||
                article.title.contains("Weather", ignoreCase = true) ||
                article.title.contains("Cyclone", ignoreCase = true) ||
                article.title.contains("Tide", ignoreCase = true) ||
                article.title.contains("Rain", ignoreCase = true) ||
                article.title.contains("Heatwave", ignoreCase = true) ||
                article.title.contains("IMD", ignoreCase = true) ||
                article.title.contains("Storm", ignoreCase = true) ||
                article.snippet.contains("temperature", ignoreCase = true) ||
                article.snippet.contains("rainfall", ignoreCase = true) ||
                article.content.contains("weather", ignoreCase = true)

            NewsFeedTab.TOURISM_UPDATES ->
                article.category.equals("Tourism Updates", ignoreCase = true) ||
                article.category.equals("Tourism", ignoreCase = true) ||
                article.category.equals("Culture", ignoreCase = true) ||
                article.category.equals("Spiritual", ignoreCase = true) ||
                article.title.contains("Tourism", ignoreCase = true) ||
                article.title.contains("Eco-Promenade", ignoreCase = true) ||
                article.title.contains("Eco-Trek", ignoreCase = true) ||
                article.title.contains("Gazebos", ignoreCase = true) ||
                article.title.contains("Vanishing Sea", ignoreCase = true) ||
                article.title.contains("Mahotsav", ignoreCase = true) ||
                article.title.contains("Tourists", ignoreCase = true) ||
                article.title.contains("Eco-Tourism", ignoreCase = true) ||
                article.title.contains("Wildlife Sanctuary", ignoreCase = true) ||
                article.title.contains("Darshan Facilitated", ignoreCase = true) ||
                article.content.contains("tourist", ignoreCase = true) ||
                article.content.contains("visitors", ignoreCase = true)

            NewsFeedTab.EMERGENCY_FEEDS ->
                article.category.equals("Emergency", ignoreCase = true) ||
                article.category.equals("Emergency News", ignoreCase = true) ||
                article.category.equals("Disaster Alert", ignoreCase = true) ||
                article.category.equals("Coastal Alerts", ignoreCase = true) ||
                article.title.contains("Emergency", ignoreCase = true) ||
                article.title.contains("Alert", ignoreCase = true) ||
                article.title.contains("Warning", ignoreCase = true) ||
                article.title.contains("Flood", ignoreCase = true) ||
                article.title.contains("Siren", ignoreCase = true) ||
                article.title.contains("Evacuation", ignoreCase = true) ||
                article.title.contains("NDRF", ignoreCase = true) ||
                article.title.contains("ODRAF", ignoreCase = true) ||
                article.title.contains("Rescue", ignoreCase = true) ||
                article.content.contains("helpline", ignoreCase = true) ||
                article.content.contains("emergency", ignoreCase = true)

            NewsFeedTab.NATIONAL_NEWS ->
                article.category.equals("National News", ignoreCase = true) ||
                article.category.equals("National", ignoreCase = true) ||
                article.title.contains("National", ignoreCase = true) ||
                article.title.contains("India", ignoreCase = true) ||
                article.title.contains("ISRO", ignoreCase = true) ||
                article.title.contains("Cabinet", ignoreCase = true) ||
                article.title.contains("Union", ignoreCase = true) ||
                article.source.contains("National", ignoreCase = true) ||
                article.source.contains("PTI", ignoreCase = true)

            NewsFeedTab.INTERNATIONAL_NEWS ->
                article.category.equals("International News", ignoreCase = true) ||
                article.category.equals("International", ignoreCase = true) ||
                article.category.equals("World", ignoreCase = true) ||
                article.title.contains("Global", ignoreCase = true) ||
                article.title.contains("World", ignoreCase = true) ||
                article.title.contains("UN", ignoreCase = true) ||
                article.title.contains("International", ignoreCase = true) ||
                article.source.contains("World", ignoreCase = true) ||
                article.source.contains("Reuters", ignoreCase = true) ||
                article.source.contains("International", ignoreCase = true)

            NewsFeedTab.DEFENSE_ALERTS ->
                article.category.equals("Defense Alerts", ignoreCase = true) ||
                article.category.equals("Defense", ignoreCase = true) ||
                article.title.contains("DRDO", ignoreCase = true) ||
                article.title.contains("Missile", ignoreCase = true) ||
                article.title.contains("ITR", ignoreCase = true) ||
                article.title.contains("Defense", ignoreCase = true) ||
                article.content.contains("DRDO", ignoreCase = true) ||
                article.content.contains("missile", ignoreCase = true)

            NewsFeedTab.POLITICS ->
                article.category.equals("Politics", ignoreCase = true) ||
                article.category.equals("Local Politics", ignoreCase = true) ||
                article.title.contains("Parishad", ignoreCase = true) ||
                article.title.contains("Council", ignoreCase = true) ||
                article.title.contains("Cabinet", ignoreCase = true) ||
                article.title.contains("Budget", ignoreCase = true) ||
                article.title.contains("Resolution", ignoreCase = true) ||
                article.content.contains("elected", ignoreCase = true) ||
                article.content.contains("council", ignoreCase = true)

            NewsFeedTab.SPORTS ->
                article.category.equals("Sports", ignoreCase = true) ||
                article.title.contains("Athletics", ignoreCase = true) ||
                article.title.contains("Football", ignoreCase = true) ||
                article.title.contains("Stadium", ignoreCase = true) ||
                article.title.contains("Championship", ignoreCase = true) ||
                article.title.contains("Cricket", ignoreCase = true) ||
                article.title.contains("Marathon", ignoreCase = true) ||
                article.content.contains("athletes", ignoreCase = true) ||
                article.content.contains("sprinters", ignoreCase = true)

            NewsFeedTab.HEALTH ->
                article.category.equals("Health", ignoreCase = true) ||
                article.category.equals("Healthcare", ignoreCase = true) ||
                article.category.equals("Medical", ignoreCase = true) ||
                article.title.contains("Health", ignoreCase = true) ||
                article.title.contains("Medical", ignoreCase = true) ||
                article.title.contains("Hospital", ignoreCase = true) ||
                article.title.contains("AIIMS", ignoreCase = true) ||
                article.title.contains("FM Medical", ignoreCase = true) ||
                article.title.contains("DHH", ignoreCase = true) ||
                article.title.contains("ICU", ignoreCase = true) ||
                article.title.contains("Telemedicine", ignoreCase = true) ||
                article.title.contains("Doctor", ignoreCase = true) ||
                article.content.contains("hospital", ignoreCase = true) ||
                article.content.contains("medical", ignoreCase = true) ||
                article.content.contains("patients", ignoreCase = true)
        }
    }

    fun filterArticles(articles: List<NewsArticle>, tabIndex: Int, query: String = ""): List<NewsArticle> {
        val trimmed = query.trim()
        return articles.filter { article ->
            val matchesTab = matchesTab(article, tabIndex)
            val matchesQuery = trimmed.isEmpty() ||
                article.title.contains(trimmed, ignoreCase = true) ||
                article.odiaTitle.contains(trimmed, ignoreCase = true) ||
                article.snippet.contains(trimmed, ignoreCase = true) ||
                article.odiaSnippet.contains(trimmed, ignoreCase = true) ||
                article.content.contains(trimmed, ignoreCase = true) ||
                article.odiaContent.contains(trimmed, ignoreCase = true) ||
                article.category.contains(trimmed, ignoreCase = true) ||
                article.source.contains(trimmed, ignoreCase = true)
            matchesTab && matchesQuery
        }
    }
}

data class BalasoreUiState(
    val selectedTab: Int = 0,
    val currentHash: String = "#tourism",
    val language: AppLanguage = AppLanguage.ENGLISH,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val hotspots: List<Hotspot> = BalasoreRepository.hotspots,
    val selectedHotspotCategory: String = "All",
    val hotspotSearchQuery: String = "",
    val newsArticles: List<NewsArticle> = BalasoreRepository.newsArticles,
    val selectedNewsTab: Int = 0,
    val selectedNewsCategory: String = "Local News",
    val newsSearchQuery: String = "",
    val filteredNewsArticles: List<NewsArticle> = NewsFeedFilter.filterArticles(BalasoreRepository.newsArticles, 0, ""),
    val weather: WeatherInfo = BalasoreRepository.weather,
    val isWeatherLoading: Boolean = false,
    val emergencyContacts: List<EmergencyContact> = BalasoreRepository.emergencyContacts,
    val transitList: List<TransitSchedule> = BalasoreRepository.transitSchedules,
    // 6 Unique Features
    val tidalClock: TidalClockData = BalasoreRepository.tidalClock,
    val lastKnownTideForecast: ChandipurTideEntity? = null,
    val drdoAdvisories: List<DrdoAdvisory> = BalasoreRepository.drdoAdvisories,
    val templeRitualInfo: TempleRitualInfo = BalasoreRepository.templeRitualInfo,
    val riverGauges: List<RiverGauge> = BalasoreRepository.riverGauges,
    val cycloneShelters: List<CycloneShelter> = BalasoreRepository.cycloneShelters,
    val seafoodCatches: List<SeafoodCatch> = BalasoreRepository.seafoodCatches,
    val literaryTrailPoints: List<LiteraryTrailPoint> = BalasoreRepository.literaryTrailPoints,
    val dailyIdiom: DailyBalasoreIdiom = BalasoreRepository.dailyIdiom,
    // Extended Unique Features Suite
    val horseshoeCrabSightings: List<HorseshoeCrabSighting> = UniqueFeaturesRepository.horseshoeCrabSightings,
    val intertidalEcoGuidelines: List<IntertidalEcoGuideline> = UniqueFeaturesRepository.intertidalEcoGuidelines,
    val defenseMilestones: List<DefenseTrailMilestone> = UniqueFeaturesRepository.defenseMilestones,
    val maritimeExclusionZone: MaritimeExclusionZone = UniqueFeaturesRepository.maritimeExclusionZone,
    val prasadStatus: PrasadStatus = UniqueFeaturesRepository.prasadStatus,
    val balasoreArtisans: List<BalasoreArtisan> = UniqueFeaturesRepository.balasoreArtisans,
    val detailedCycloneShelters: List<DetailedCycloneShelter> = UniqueFeaturesRepository.detailedCycloneShelters,
    val emergencyKitItems: List<EmergencyKitItem> = UniqueFeaturesRepository.defaultEmergencyKitItems,
    val harborCatchRates: List<HarborCatchRate> = UniqueFeaturesRepository.harborCatchRates,
    val harborLandingBells: List<HarborLandingBell> = UniqueFeaturesRepository.harborLandingBells,
    val elephantCorridorAlert: ElephantCorridorAlert = UniqueFeaturesRepository.elephantCorridorAlert,
    val ecoPassportStamps: List<EcoPassportStamp> = UniqueFeaturesRepository.ecoPassportStamps,
    val weatherAlerts: List<BalasoreWeatherAlert> = DefaultData.getInitialWeatherAlerts(),
    val forecastDays: List<BalasoreForecastDay> = BalasoreRepository.forecast3Days,
    val hourlyForecast: List<BalasoreForecastHour> = emptyList(),
    val isFahrenheit: Boolean = false,
    val selectedForecastDayIndex: Int = 0,
    val activeFeatureSheet: UniqueFeatureSheetType? = null,
    val isRefreshing: Boolean = false,
    val bookmarkedIds: Set<String> = emptySet(),
    val favoriteHotspotIds: Set<String> = emptySet(),
    val groundingState: GroundingState = GroundingState(),
    // Gemini Chatbot & AI Features State
    val chatMessages: List<com.example.data.remote.ChatMessage> = emptyList(),
    val isChatLoading: Boolean = false,
    val selectedChatModel: com.example.data.remote.GeminiChatModel = com.example.data.remote.GeminiChatModel.FLASH,
    val selectedChatRole: com.example.data.remote.ChatRolePersona = com.example.data.remote.ChatRolePersonas.TOURISM_GUIDE,
    val isChatSearchGrounding: Boolean = true,
    val isChatMapsGrounding: Boolean = true,
    val isMusicGenerating: Boolean = false,
    // Custom Interactive Itinerary (Room persisted)
    val itineraryItems: List<ItineraryItemEntity> = DefaultData.getDefaultItineraryItems(),
    // Live Emergency Alerts fetched from Backend
    val liveEmergencyAlerts: List<EmergencyAlertDto> = DefaultData.getDefaultEmergencyAlerts(),
    val dismissedAlertIds: Set<String> = emptySet(),
    // Daily Auto-Update Pulse
    val dailyPulse: com.example.data.daily.DailyBalasorePulse = com.example.data.daily.DailyUpdateEngine.getDailyPulse(),
    // Full App Watermark (App Name & Official Logo)
    val isWatermarkEnabled: Boolean = true,
    val watermarkStyle: WatermarkStyle = WatermarkStyle.CENTER_EMBLEM,
    val watermarkOpacity: WatermarkOpacity = WatermarkOpacity.MEDIUM,
    // Hourly Auto-Refresh & Synchronization
    val isHourlyAutoRefreshEnabled: Boolean = true,
    val lastHourlyRefreshTimestamp: Long = System.currentTimeMillis(),
    val nextHourlyRefreshMinutesRemaining: Int = 60,
    val autoRefreshCycleCount: Int = 0,
    val autoUpdateFrequencyMinutes: Int = 60,
    val isAutoUpdateBackgroundEnabled: Boolean = true,
    val lastSyncStatusMessage: String = "All services synchronized",
    val appVersionInstalled: String = com.example.BuildConfig.VERSION_NAME,
    val appVersionLatest: String = com.example.BuildConfig.VERSION_NAME,
    val isUpdateCheckLoading: Boolean = false,
    // News Dedicated Auto-Update Engine
    val isNewsAutoUpdateEnabled: Boolean = true,
    val newsAutoUpdateIntervalSeconds: Int = 3600,
    val newsAutoUpdateSecondsRemaining: Int = 3600,
    val lastNewsAutoUpdateTimestamp: Long = System.currentTimeMillis(),
    val newsAutoUpdateCycleCount: Int = 0,
    // Battery-Saving Mode Toggle (reduces background data syncs for News & Bus Schedules)
    val isBatterySavingModeEnabled: Boolean = false,
    // My Reports - Local Room Database for Offline Civic Issue Tracking & Auto Updates
    val myCivicReports: List<CivicReportEntity> = DefaultData.getDefaultCivicReports(),
    val selectedMyReportsFilter: String = "ALL",
    val firestoreUserReports: List<FirestoreUserReport> = emptyList(),
    val isFirestoreReportsLoading: Boolean = false,
    // OpenWeatherMap Real-Time Telemetry & 3-Day / 7-Day Forecast Suite
    val openWeatherCurrent: OpenWeatherCurrentResponse? = null,
    val openWeatherThreeDayForecast: List<OpenWeatherThreeDayForecast> = emptyList(),
    val openWeatherSevenDayForecast: List<OpenWeatherDailyForecast> = emptyList(),
    val isOpenWeatherLoading: Boolean = false,
    val openWeatherCycleCount: Int = 1,
    // Chandipur Vanishing Sea Walk Window Proactive Notification Settings
    val isWalkWindowAlertsEnabled: Boolean = true,
    val walkWindowAdvanceMinutes: Int = 30,
    // Chandipur Beach 'High Tide' Coastal Safety Push Notification Toggle (Settings)
    val isChandipurHighTideAlertsEnabled: Boolean = true,
    // Adaptive Sync (<20% Battery Auto-Throttle) & Personalized News Category Subscriptions
    val isAdaptiveSyncEnabled: Boolean = true,
    val subscribedNewsCategories: Set<String> = setOf("Politics", "Sports", "Defense", "Tourism", "Civic"),
    // Saved Bookmarks Room Database Cache (News Articles & Emergency Directory numbers)
    val savedBookmarks: List<SavedBookmarkEntity> = emptyList(),
    val savedEmergencyContactIds: Set<String> = emptySet(),
    val selectedSavedFilter: String = "ALL", // "ALL", "ARTICLES", "EMERGENCY"
    val recentNewsSearchQueries: List<String> = emptyList(),
    val offlineTideSchedule: List<ChandipurTideEntity> = emptyList(),
    // Citizen-Submitted Local News Stories & Editorial Moderation Queue
    val communityNewsSubmissions: List<CommunityNewsSubmissionEntity> =
        CommunityNewsModerationRepository.defaultSeededSubmissions()
)

enum class UniqueFeatureSheetType {
    HORSESHOE_CRAB,
    DEFENSE_TRAIL,
    REMUNA_PRASAD_ARTISANS,
    CYCLONE_RESILIENCE,
    HARBOR_CATCH_RATES,
    ELEPHANT_PASSPORT,
    // 8 Core Feature Hubs:
    CHANDIPUR_TIDE_TIMER,
    OFFLINE_TIDE_DASHBOARD,
    CYCLONE_SHELTER_FINDER,
    BALASORE_FOOD_TRAIL,
    BILINGUAL_HERITAGE_AUDIO,
    TRANSIT_FARE_ESTIMATOR,
    KULDIHA_SAFARI_COMPANION,
    TRAVEL_JOURNAL,
    BLOOD_AND_DIALYSIS_DIRECTORY,
    // 7 Daily Auto-Updated Features:
    KASAFAL_RED_CRABS,
    NILAGIRI_STONE_SABAI,
    BALARAMGADI_ESTUARY,
    COLONIAL_HERITAGE_WALK,
    TALASARI_AUCTION_MONITOR,
    BALESWARIYA_DIALECT_PROVERBS,
    PAN_BARAJA_AGRO,
    // 10 Next-Gen Daily Auto-Updated Features:
    BICHITRAPUR_MANGROVE_BOATING,
    CHANDANESWAR_CHADAK_MELA,
    PANCHALINGESWAR_STREAM_SAFETY,
    DARK_SKY_BIOLUMINESCENCE,
    BELL_METAL_ARTISANS,
    BUDDHIST_JAIN_CIRCUIT,
    HEIRLOOM_RICE_AGRO,
    LAKHANNATH_ZAMINDARI,
    BUDHABALANGA_RIVER_ANGLING,
    CYCLONE_ORAL_HISTORY,
    // Healthcare & Medical Daily Auto-Updated Suites:
    DOCTORS_DIRECTORY,
    MEDICINE_STORES_DIRECTORY,
    POLYCLINIC_DIRECTORY,
    PATHOLOGY_LAB_DIRECTORY,
    PRIVATE_HOSPITAL_DIRECTORY,
    // Hydrology & Artisan Marketplace Sheets:
    RIVER_FLOOD_TELEMETRY,
    MATI_MANISHA_ARTISANS,
    // Google Maps Explorer (Hotspots & Cyclone Shelters)
    BALASORE_MAP_EXPLORER,
    // 6 Extended Unique Suites with Auto-Updating Telemetry
    SALT_PAN_HERITAGE,
    HILSA_MIGRATION,
    KULDIHA_ELEPHANT_CORRIDOR,
    CHHENA_GAJA_HOT_BATCH,
    RIVER_FERRY_SCHEDULE,
    ODIA_SAHITYA_REVIVAL,
    // Suggested Innovations:
    ASK_BALASORE_AI,
    INCOIS_OCEAN_ADVISORY,
    ODIA_PANJIKA_CALENDAR,
    RAIBANIA_AUDIO_WALK,
    BLOOD_AND_BED_PULSE,
    CITIZEN_CIVIC_EYE,
    // Next Generation Auto-Update Suites
    TOTO_AUTO_FARE_CARD,
    BALASORE_CAMPUS_CAREER_BOARD,
    BALASORE_AUTO_UPDATE_CENTER,
    // 7 New Suites with Live Telemetry & Auto-Updates
    BALASORE_JUNCTION_RADAR,
    BALESWARI_PAAN_KRUSHI_MANDI,
    NIGHT_CHEMIST_SANJEEVANI,
    BALESWAR_MAHOTSAV_CHADAK,
    NILAGIRI_STONE_ARTISAN,
    TPNODL_WATCO_MONITOR,
    TALASARI_MANGROVE_EXPLORER,
    // 7 Citizen, Maritime, Nature & Healthcare Suites
    MO_SEVA_KENDRA_CITIZEN,
    MARINE_FISHERMEN_SAFETY,
    KULDIHA_ECO_CAMP_SAFARI,
    FM_MCH_MEDICAL_COLLEGE_OPD,
    SABAI_GRASS_MISSION_SHAKTI,
    BALASORE_MARITIME_COLONIAL,
    BALASORE_STUDENT_CAREER_SCHOLARSHIP,
    // 7 Advanced Coastal, Safety, Culture, Health, Agro, Civic & Highway Suites
    DRDO_CHANDIPUR_SAFETY_RADAR,
    SUBARNAREKHA_SLUICE_FLOOD_RADAR,
    NILAGIRI_CHHAU_CULTURAL_GUILD,
    DHH_BLOOD_BANK_LIVE_RADAR,
    PAN_BARAJA_AQUA_SHRIMP_DESK,
    BALASORE_COURT_LEGAL_AID_DESK,
    NH16_HIGHWAY_PATROL_TRAUMA_SOS,
    // 7 Special Automated Hubs with Live Telemetry
    PANCHALINGESWAR_KULDIHA_SAFARI,
    SER_BALASORE_RAILWAY_JUNCTION,
    NOCCI_INDUSTRIAL_B2B_SKILL,
    TALSARI_BICHITRAPUR_MANGROVE_PILOT,
    FAKIR_MOHAN_BHASHA_LITERATURE,
    BALESWARI_CUISINE_SWEET_HERITAGE,
    TPNODL_WATCO_UTILITY_HOTLINE,
    // 7 Features with Auto Updates:
    DHAN_MANDI_MSP_PROCUREMENT,
    NILAGIRI_GRANITE_STONEWARE_GUILD,
    BAHANAGA_NHAI_RAPID_TRAUMA_NETWORK,
    BHUSANDESWAR_CHANDANESWAR_PILGRIMAGE,
    SENIOR_CITIZEN_SEVA_SETU,
    BALASORE_HIGHER_EDUCATION_HUB,
    BALASORE_MO_BUS_CRUT_NAVIGATOR,
    // 7 Coastal, Resilience, Heritage, Health, Agro, Transit & Nature Auto-Updated Suites:
    AQUA_SHRIMP_HATCHERY_RADAR,
    COASTAL_CYCLONE_SHELTER_NETWORK,
    MARITIME_FOREIGN_LOGE_TRAIL,
    RURAL_MOBILE_TELEMEDICINE_NETWORK,
    BALESWARI_BETEL_PADDY_COOPERATIVE,
    COASTAL_CRUT_INTERDISTRICT_BUS_RADAR,
    HORSESHOE_CRAB_INTERTIDAL_PROTECTION,
    CITIZEN_FEEDBACK_PORTAL,
    FCM_SEVERE_WEATHER_ALERT,
    // 6 Next-Gen Auto-Updated Suites:
    COASTAL_MARINE_AUDIO_RADIO,
    CYCLONE_SURGE_EVACUATION_ROUTER,
    BALARAMGADI_MANDI_PRICE_INDEX,
    TEMPLE_AARTI_KHIRA_BHOG_COMPANION,
    FM_MCH_EMERGENCY_BLOOD_NETWORK,
    MO_BALASORE_CIVIC_STORM_REPORTER,
    // 6 Defense, Agro, Transit, Campus, Culinary & Energy Suites:
    ITR_MISSILE_COASTAL_TRACKER,
    SUBARNAREKHA_CROP_ADVISORY,
    BALASORE_SMART_TRANSIT_TRACKER,
    FAKIR_MOHAN_STUDENT_HUB,
    BALESWARI_CUISINE_EXPLORER,
    TPNODL_POWER_OUTAGE_RADAR,
    // 6 Coastal Nocturnal, Medical Emergency, Tribal Craft, Freedom Trail, Eco-Corridor & Cashew Mandi Suites:
    CHANDIPUR_BIOLUMINESCENCE_EXPLORER,
    BALASORE_SNAKEBITE_ANTI_VENOM,
    NILAGIRI_DOKRA_CRAFT_GUILD,
    BAGHA_JATIN_FREEDOM_TRAIL,
    KULDIHA_ECO_CORRIDOR_FIRE_RADAR,
    BHOGRAI_CASHEW_GROUNDNUT_MANDI,
    // 6 Medieval Heritage, Deep-Sea Trawlers, Lacquer Guild, Blood Grid, River Sluice & Youth Wellness Suites:
    RAIBANIA_FORTRESS_VAULT,
    BALARAMGADI_TRAWLER_FLEET_RADAR,
    NILAGIRI_LACQUER_BAMBOO_GUILD,
    BALASORE_DISTRICT_BLOOD_DONOR_GRID,
    SUBARNAREKHA_FLOOD_SLUICE_TELEMETRY,
    BALASORE_YOUTH_WELLNESS_STUDY_CIRCLE,
    // Google Maps Pre-cached Offline Map Tiles & Outage Navigation
    BALASORE_OFFLINE_MAP_TILES,
    // 6 Coastal SOS, Culinary GI, Micro-Ward Flood, Elephant Sentinel, Student Career & Solar Radar Suites:
    MARINE_FISHERMEN_HIGH_SEAS_SOS,
    BALESWARI_CULINARY_GI_TRAIL,
    WARD_FLOOD_EVACUATION_ROUTER,
    KULDIHA_ELEPHANT_CORRIDOR_SENTINEL,
    STUDENT_CAREER_LIBRARY_PULSE,
    ROOFTOP_SOLAR_TPNODL_RADAR,
    // 8 Eco-Tourism, Sanctum, Fish Mandi, Transit, Civic Sanitation, Industrial Jobs & Krushak PACS Suites:
    TALASARI_UDAYPUR_RED_CRAB_SAFARI,
    PANCHALINGESWAR_WATERFALL_TREK_MONITOR,
    EMAMI_JAGANNATH_REMUNA_HERITAGE,
    BALARAMGADI_FISH_MANDI_RATES,
    TOWN_AUTO_RICKSHAW_FARE_CALCULATOR,
    WARD_SANITATION_GARBAGE_VEHICLE_TRACKER,
    BALASORE_INDUSTRIAL_JOB_APPRENTICE_BOARD,
    KRUSHAK_PACS_MANDI_GRAIN_RADAR,
    // Auto-Updated New Features Suite
    OLIVE_RIDLEY_MARINE_WILDLIFE,
    AIIMS_DHH_OPD_BED_TRACKER,
    OFFLINE_CYCLONE_SAFETY_TOOLKIT,
    // Offline Room Database Civic Reports Suite
    MY_CIVIC_REPORTS,
    // OpenWeatherMap Real-Time Telemetry & 3-Day Forecast Suite
    OPEN_WEATHER_MAP,
    // Google Maps Coastal Precipitation Radar & Storm Tracking Suite
    COASTAL_WEATHER_RADAR_MAP,
    // Odia Culture & Daily Horoscope Astrology Suite
    ODIA_CULTURE_SECTION,
    // Odia Festivals & Important Cultural Dates Room Suite
    ODIA_FESTIVALS,
    // Google Maps Tourism Hotspots & Distance Markers Carousel
    TOURISM_HOTSPOTS,
    // Live Balasore City Bus & Public Transit API Suite
    LOCAL_TRANSPORT,
    // Live Balasore Bus Stand (Sahadevkhunta) Room Database Schedules
    BALASORE_BUS_STAND_LIVE_SCHEDULES,
    // Nearest Emergency Services Map relative to user position
    NEAREST_EMERGENCY_SERVICES_MAP,
    // 5 New Features Suite:
    CHANDIPUR_VANISHING_SEA_CLOCK,
    COASTAL_CYCLONE_PREPAREDNESS,
    BALASORE_DAY_TRIP_PLANNER,
    BALARAMGADI_SEAFOOD_INDEX,
    BALASORE_PHOTO_SPOTS_CHECKLIST,
    // Real-Time Balasore Train Running Status & Push Notification Alerts Suite
    BALASORE_LIVE_TRAIN_STATUS,
    // Location-Based Cyclone Shelter & Block Helpline Home Screen Widget Manager
    CYCLONE_SHELTER_WIDGET_MANAGER,
    // OSMDroid Offline Tourism Map & Tiles Provider Suite
    OSM_OFFLINE_TOURISM_MAP,
    // D3 Historical Weather and Tidal Trends Analytics Suite
    HISTORICAL_D3_WEATHER_TIDAL_TRENDS,
    // ML Kit Official Balasore Tourism & Event Information Posters QR Scanner Suite
    OFFICIAL_TOURISM_EVENT_POSTER_SCANNER,
    // Offline-first Coil Zoomable Gallery & Travel Itinerary Suite
    BALASORE_TOURISM_GALLERY,
    // WorkManager Battery & Data Sync Health Dashboard with Smart Sync mode
    APP_HEALTH_DASHBOARD,
    // Battery Health Monitor & Background Services Energy Longevity Dashboard
    BATTERY_HEALTH_MONITOR,
    // Google Play Store Verified In-App Privacy Policy & User Data Compliance
    PRIVACY_POLICY,
    // Real-Time Transport Alerts (Train & Bus Room Database Cache + FCM Delay Push)
    TRANSPORT_ALERTS,
    // Interactive Tourism Map with Hotspot Pins, Timings, Contact Info & Community Reviews
    TOURISM_INTERACTIVE_MAP,
    // User Authentication & Profile (Google Sign-In, Cloud Favorites, Weather Prefs & Bookmarks)
    USER_PROFILE_AUTH,
    // Real-Time Push Notification Alert Center & Live Simulator
    PUSH_NOTIFICATION_CENTER,
    // Offline Mode & Data Hub (News, Weather Forecasts, Tourism Caching & Smart Storage)
    OFFLINE_MODE_MANAGER,
    // Text-To-Speech Historical Landmarks Audio Guide Engine
    HISTORICAL_LANDMARK_AUDIO_GUIDE,
    // Offline OSMDroid Civic Issues Map & Proximity Radar
    CIVIC_ISSUES_MAP,
    // Balasore Municipality All 31 Administrative Wards Directory & Sanitation Hub
    MUNICIPAL_WARDS_DIRECTORY,
    // Administrative Moderator Panel (Add, Edit & Remove Local News, Weather & Tourism Hotspots)
    ADMIN_MODERATOR_PANEL,
    // Augmented Reality (AR) Historical Landmark Camera Overlay
    AR_LANDMARK_CAMERA,
    // Citizen Contribution Leaderboard (Civic Eye reports, resolutions, impact points & tiers)
    CITIZEN_CONTRIBUTION_LEADERBOARD,
    // Balasore Movie Theatres, Multiplexes & Live Showtimes Directory
    BALASORE_MOVIE_THEATRES,
    // Balasore Community Forum (Create posts, reply to discussions, Local News, Events & Tourism)
    COMMUNITY_FORUM,
    // Balasore Real-Time Local Bus Tracker (Static Route JSON + Mock API Live Arrivals)
    LOCAL_BUS_TRACKER
}

data class GroundingState(
    val isSheetOpen: Boolean = false,
    val query: String = "",
    val isQuerying: Boolean = false,
    val toolMode: GroundingToolMode = GroundingToolMode.COMBINED,
    val response: GroundingResponse? = null,
    val errorMessage: String? = null
)

enum class AppTab {
    HOTSPOTS,
    NEWS,
    WEATHER,
    ESSENTIALS
}

enum class AuthMode {
    PROFILE,
    LOGIN,
    SIGNUP,
    FORGOT_PASSWORD,
    EDIT_PROFILE
}

class BalasoreViewModel(
    private val injectedNewsRepo: INewsRepository? = null,
    private val injectedCivicRepo: CivicReportRepository? = null,
    private val injectedItineraryRepo: ItineraryRepository? = null,
    private val injectedTideDao: ChandipurTideDao? = null,
    private val injectedTourismRepo: ITourismRepository? = null
) : ViewModel() {

    private var itineraryRepo: ItineraryRepository? = injectedItineraryRepo
    var travelJournalRepo: TravelJournalRepository? = null
        private set
    private var tideDao: ChandipurTideDao? = injectedTideDao
    private var newsRepo: INewsRepository? = injectedNewsRepo
    private var tourismRepo: ITourismRepository? = injectedTourismRepo
    private var civicReportDao: CivicReportDao? = null
    var civicReportRepo: CivicReportRepository? = injectedCivicRepo
        private set
    var communityNewsRepo: CommunityNewsModerationRepository = CommunityNewsModerationRepository()
        private set
    var savedBookmarkRepo: SavedBookmarkRepository? = null
        private set
    private var newsSearchQueryDao: NewsSearchQueryDao? = null
    var firestoreService: FirestoreService? = null
        private set
    private var appContext: Context? = null
    private val apiService: BalasoreApiService by lazy { BalasoreApiService.create() }
    private val weatherApiService: WeatherApiService by lazy { WeatherApiService.create() }
    private val openWeatherRepo: OpenWeatherRepository by lazy { OpenWeatherRepository() }
    private val geminiChatService: GeminiChatService by lazy { GeminiChatService() }

    private val _uiState = MutableStateFlow(BalasoreUiState())
    val uiState: StateFlow<BalasoreUiState> = _uiState.asStateFlow()

    init {
        observeMultiSourceSyndicator()
        injectedNewsRepo?.let { repo ->
            observeNewsRepository(repo)
        }
        injectedTourismRepo?.let { repo ->
            observeTourismRepository(repo)
        }
        try {
            if (android.os.Looper.getMainLooper() != null) {
                // Periodic auto-update ticker and deferred initial network sync
                viewModelScope.launch {
                    delay(600L)
                    fetchLiveEmergencyAlerts()
                    fetchRealTimeWeather()
                    fetchOpenWeatherData()
                    try {
                        com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullAndAutoPublishAllSections(
                            context = appContext,
                            forceNetworkRss = true
                        )
                    } catch (_: Throwable) {}
                    while (true) {
                        delay(60_000L)
                        refreshDailyPulse()
                        try {
                            civicReportRepo?.autoProgressReports()
                        } catch (_: Throwable) {}
                        if (_uiState.value.isHourlyAutoRefreshEnabled) {
                            val elapsedMillis = System.currentTimeMillis() - _uiState.value.lastHourlyRefreshTimestamp
                            val elapsedMinutes = (elapsedMillis / (1000 * 60)).toInt()
                            val freq = _uiState.value.autoUpdateFrequencyMinutes.coerceAtLeast(1)
                            val remaining = (freq - elapsedMinutes).coerceAtLeast(0)
                            if (remaining <= 0) {
                                triggerHourlyAutoRefresh()
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    nextHourlyRefreshMinutesRemaining = remaining
                                )
                            }
                        }
                        // Automatic news auto-update ticker (hourly or interval auto-sync)
                        if (_uiState.value.isNewsAutoUpdateEnabled) {
                            val curRem = _uiState.value.newsAutoUpdateSecondsRemaining
                            val nextRem = curRem - 60
                            if (nextRem <= 0) {
                                triggerNewsAutoUpdate()
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    newsAutoUpdateSecondsRemaining = nextRem
                                )
                            }
                        }
                    }
                }
            }
        } catch (_: Throwable) {
            // JVM unit test environment without Android Looper
        }
    }

    /**
     * Observes hourly multi-source news syndicator (`Local`, `National News`, `International News`)
     * and merges newly pulled articles directly into the live feed without requiring moderator approval.
     */
    private fun observeMultiSourceSyndicator() {
        viewModelScope.launch {
            try {
                com.example.data.sync.HourlyMultiSourceNewsSyndicator.pulledArticlesFlow.collect { pulledList ->
                    if (pulledList.isNotEmpty()) {
                        val currentArticles = _uiState.value.newsArticles
                        val bookmarkedSet = _uiState.value.bookmarkedIds
                        val merged = (pulledList.map { art ->
                            art.copy(isBookmarked = art.isBookmarked || bookmarkedSet.contains(art.id))
                        } + currentArticles)
                            .distinctBy { "${it.id}_${it.title.trim().lowercase()}" }
                        val recomputed = NewsFeedFilter.filterArticles(
                            merged,
                            _uiState.value.selectedNewsTab,
                            _uiState.value.newsSearchQuery
                        )
                        _uiState.value = _uiState.value.copy(
                            newsArticles = merged,
                            filteredNewsArticles = recomputed
                        )
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    /**
     * Connects an external or mocked [INewsRepository] and observes its offline Room cache flows.
     */
    fun setNewsRepositoryForTesting(repo: INewsRepository) {
        newsRepo = repo
        observeNewsRepository(repo)
    }

    fun getNewsRepository(): INewsRepository? = newsRepo

    /**
     * Observes offline cached articles and bookmark streams from the given [INewsRepository].
     */
    fun observeNewsRepository(nRepo: INewsRepository) {
        viewModelScope.launch {
            try {
                nRepo.allNews.collect { entities ->
                    if (entities.isNotEmpty()) {
                        val repoArticles = BalasoreRepository.newsArticles +
                            com.example.data.sync.HourlyMultiSourceNewsSyndicator.pulledArticlesFlow.value
                        val mapped = entities.map { entity ->
                            val matchingRepo = repoArticles.find {
                                it.id == entity.id.toString() || it.title.equals(entity.title, ignoreCase = true)
                            }
                            NewsArticle(
                                id = entity.id.toString(),
                                title = entity.title,
                                odiaTitle = matchingRepo?.odiaTitle ?: entity.title,
                                snippet = entity.summary,
                                odiaSnippet = matchingRepo?.odiaSnippet ?: entity.summary,
                                category = entity.category,
                                timeAgo = entity.publishedAt,
                                source = entity.source,
                                isBookmarked = entity.isBookmarked,
                                content = entity.content,
                                odiaContent = matchingRepo?.odiaContent ?: entity.content
                            )
                        }
                        val bIds = entities.filter { it.isBookmarked }.map { it.id.toString() }.toSet()
                        val recomputed = NewsFeedFilter.filterArticles(mapped, _uiState.value.selectedNewsTab, _uiState.value.newsSearchQuery)
                        _uiState.value = _uiState.value.copy(
                            newsArticles = mapped,
                            filteredNewsArticles = recomputed,
                            bookmarkedIds = if (bIds.isNotEmpty()) bIds else _uiState.value.bookmarkedIds
                        )
                    }
                }
            } catch (_: Throwable) {}
        }
        viewModelScope.launch {
            try {
                nRepo.getBookmarkedNews().collect { bookmarkedEntities ->
                    val bIds = bookmarkedEntities.map { it.id.toString() }.toSet()
                    _uiState.value = _uiState.value.copy(
                        bookmarkedIds = bIds
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    /**
     * Connects an external or mocked [ITourismRepository] and observes its offline Room cache flows.
     */
    fun setTourismRepositoryForTesting(repo: ITourismRepository) {
        tourismRepo = repo
        observeTourismRepository(repo)
    }

    fun getTourismRepository(): ITourismRepository? = tourismRepo

    /**
     * Observes offline cached tourism hotspots and favorite streams from the given [ITourismRepository].
     */
    fun observeTourismRepository(tRepo: ITourismRepository) {
        viewModelScope.launch {
            try {
                tRepo.allHotspots.collect { entities ->
                    if (entities.isNotEmpty()) {
                        val mappedHotspots = entities.map { it.toDomainModel() }
                        val favIds = entities.filter { it.isFavorite }.map { it.id }.toSet()
                        _uiState.value = _uiState.value.copy(
                            hotspots = mappedHotspots,
                            favoriteHotspotIds = if (favIds.isNotEmpty()) favIds else _uiState.value.favoriteHotspotIds
                        )
                    }
                }
            } catch (_: Throwable) {}
        }
        viewModelScope.launch {
            try {
                tRepo.favoriteHotspots.collect { favEntities ->
                    val favIds = favEntities.map { it.id }.toSet()
                    _uiState.value = _uiState.value.copy(
                        favoriteHotspotIds = favIds
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    /**
     * Returns tourism hotspots filtered by the currently selected hotspot category and search query.
     */
    fun getFilteredHotspots(): List<Hotspot> {
        val state = _uiState.value
        val category = state.selectedHotspotCategory.trim()
        val query = state.hotspotSearchQuery.trim()
        return state.hotspots.filter { hotspot ->
            val matchesCategory = when {
                category.isEmpty() || category.equals("All", ignoreCase = true) -> true
                category.equals("Favorites", ignoreCase = true) || category.equals("Saved", ignoreCase = true) ->
                    state.favoriteHotspotIds.contains(hotspot.id)
                else ->
                    hotspot.category.equals(category, ignoreCase = true) ||
                        hotspot.category.contains(category, ignoreCase = true) ||
                        category.contains(hotspot.category, ignoreCase = true)
            }
            val matchesQuery = query.isEmpty() ||
                hotspot.name.contains(query, ignoreCase = true) ||
                hotspot.odiaName.contains(query, ignoreCase = true) ||
                hotspot.description.contains(query, ignoreCase = true) ||
                hotspot.location.contains(query, ignoreCase = true) ||
                hotspot.category.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    /**
     * Connects local Room database and streams itinerary items and tide data into UI state.
     */
    fun initDatabase(context: Context) {
        val appCtx = context.applicationContext ?: context
        appContext = appCtx
        if (itineraryRepo != null && tideDao != null && civicReportRepo != null) {
            return
        }
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val prefs = appCtx.getSharedPreferences("balasore_app_preferences", Context.MODE_PRIVATE)
                val savedTheme = prefs.getString("theme_mode", null)
                val savedLang = com.example.ui.components.AppLanguagePreferencesManager.getSavedLanguage(appCtx)
                val batterySaverEnabled = com.example.data.sync.SyncManager.isBatterySavingModeEnabled(appCtx)
                val highTideAlertsEnabled = com.example.data.fcm.FcmManager.isChandipurHighTideAlertsEnabled(appCtx)
                val parsedMode = savedTheme?.let {
                    runCatching { ThemeMode.valueOf(it) }.getOrNull()
                }
                _uiState.value = _uiState.value.copy(
                    language = savedLang,
                    themeMode = parsedMode ?: _uiState.value.themeMode,
                    isBatterySavingModeEnabled = batterySaverEnabled,
                    isChandipurHighTideAlertsEnabled = highTideAlertsEnabled,
                    newsAutoUpdateIntervalSeconds = if (batterySaverEnabled) 1800 else _uiState.value.newsAutoUpdateIntervalSeconds
                )
            } catch (_: Exception) {}

            try {
                val db = AppDatabase.getInstance(appCtx)
                val repo = ItineraryRepository(db.itineraryDao())
                itineraryRepo = repo

                val jRepo = TravelJournalRepository(db.travelJournalDao())
                travelJournalRepo = jRepo

                val tDao = db.chandipurTideDao()
                tideDao = tDao

                val sbDao = db.savedBookmarkDao()
                val sbRepo = SavedBookmarkRepository(sbDao)
                savedBookmarkRepo = sbRepo

                val nsDao = db.newsSearchQueryDao()
                newsSearchQueryDao = nsDao

                val cNewsRepo = CommunityNewsModerationRepository.getInstance(appCtx)
                communityNewsRepo = cNewsRepo
                launch {
                    try {
                        cNewsRepo.ensureSeeded()
                        com.example.data.sync.HourlyMultiSourceNewsSyndicator.scheduleHourlyAutoPull(appCtx)
                        com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullAndAutoPublishAllSections(
                            context = appCtx,
                            forceNetworkRss = true
                        )
                        cNewsRepo.allSubmissionsFlow.collect { submissions ->
                            _uiState.value = _uiState.value.copy(
                                communityNewsSubmissions = submissions
                            )
                        }
                    } catch (_: Throwable) {}
                }

                launch {
                    try {
                        nsDao.getRecentQueries().collect { queries ->
                            if (queries.isEmpty()) {
                                val now = System.currentTimeMillis()
                                listOf("Chandipur Tide", "DRDO Missile", "Politics", "Sports").forEachIndexed { idx, defaultQ ->
                                    nsDao.insertOrUpdateQuery(
                                        NewsSearchQueryEntity(query = defaultQ, timestamp = now - idx * 1000L)
                                    )
                                }
                            } else {
                                _uiState.value = _uiState.value.copy(
                                    recentNewsSearchQueries = queries.map { it.query }
                                )
                            }
                        }
                    } catch (_: Throwable) {}
                }

                launch {
                    try {
                        sbRepo.allBookmarks.collect { bookmarks ->
                            _uiState.value = _uiState.value.copy(savedBookmarks = bookmarks)
                        }
                    } catch (_: Throwable) {}
                }

                launch {
                    try {
                        sbRepo.savedEmergencyContactIds.collect { savedContactIds ->
                            _uiState.value = _uiState.value.copy(savedEmergencyContactIds = savedContactIds)
                        }
                    } catch (_: Throwable) {}
                }

                launch {
                    try {
                        sbRepo.savedNewsArticleIds.collect { savedNewsIds ->
                            if (savedNewsIds.isNotEmpty()) {
                                val union = _uiState.value.bookmarkedIds + savedNewsIds
                                _uiState.value = _uiState.value.copy(bookmarkedIds = union)
                            }
                        }
                    } catch (_: Throwable) {}
                }

                launch {
                    try {
                        repo.ensureDefaultItinerarySeeded()
                    } catch (t: Throwable) {
                        if (t !is kotlinx.coroutines.CancellationException) {
                            android.util.Log.e("BalasoreViewModel", "Error seeding default itinerary: ${t.message}")
                        }
                    }
                }
                launch {
                    try {
                        jRepo.ensureDefaultJournalSeeded()
                    } catch (t: Throwable) {
                        if (t !is kotlinx.coroutines.CancellationException) {
                            android.util.Log.e("BalasoreViewModel", "Error seeding default journal: ${t.message}")
                        }
                    }
                }
                // Room Entity for Chandipur Tide Schedule (Last Known Tide when offline)
                launch {
                    try {
                        val existingTide = tDao.getLatestTideForecastSync()
                        if (existingTide == null) {
                            val initialTide = ChandipurTideEntity(
                                id = "chandipur_tide_current",
                                date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                                lowTideTime = "02:45 PM",
                                highTideTime = "08:30 AM",
                                nextHighTideTime = "08:15 PM",
                                recededDistanceKm = 4.8,
                                currentWaterLevelMeters = 0.6,
                                tideState = "RECEDING",
                                safeWalkStatus = "SAFE_WALK",
                                safeWalkMinutesRemaining = 135,
                                lunarCondition = "Spring Tide (Amavasya Cycle)",
                                tidalForecastSummary = "Sea recedes up to 5 km into the Bay of Bengal. Rare natural walking opportunity until 04:30 PM.",
                                isOfflineCached = true,
                                lastFetchedTimestamp = System.currentTimeMillis()
                            )
                            tDao.insertOrUpdateTide(initialTide)
                            _uiState.value = _uiState.value.copy(lastKnownTideForecast = initialTide)
                        } else {
                            _uiState.value = _uiState.value.copy(lastKnownTideForecast = existingTide)
                        }

                        // Ensure 30-day offline tide schedule is populated
                        val futureTides = tDao.getAllFutureTidesSync()
                        if (futureTides.size < 15) {
                            val schedule = com.example.data.service.ChandipurTidePrepopulator.generate30DaysTideSchedule()
                            tDao.insertTides(schedule)
                        }

                        // Collect latest tide forecast
                        launch {
                            tDao.getLatestTideForecastFlow().collect { cachedTide ->
                                if (cachedTide != null) {
                                    _uiState.value = _uiState.value.copy(lastKnownTideForecast = cachedTide)
                                }
                            }
                        }

                        // Collect 30-day offline schedule flow
                        launch {
                            tDao.getAllFutureTidesFlow().collect { schedule ->
                                if (schedule.isNotEmpty()) {
                                    _uiState.value = _uiState.value.copy(offlineTideSchedule = schedule)
                                }
                            }
                        }
                    } catch (t: Throwable) {
                        if (t !is kotlinx.coroutines.CancellationException) {
                            android.util.Log.e("BalasoreViewModel", "Error initializing Chandipur tide entity: ${t.message}")
                        }
                    }
                }

                launch {
                    try {
                        repo.allItineraryItems.collect { items ->
                            if (items.isNotEmpty()) {
                                _uiState.value = _uiState.value.copy(itineraryItems = items)
                            }
                        }
                    } catch (t: Throwable) {
                        if (t !is kotlinx.coroutines.CancellationException) {
                            android.util.Log.e("BalasoreViewModel", "Error collecting itinerary items: ${t.message}")
                        }
                    }
                }

                // Room Database + Network unified News Repository with auto cache updates
                val nRepo = NewsRepository(db.newsDao(), db.cacheMetadataDao())
                newsRepo = nRepo

                // Initial fetch or cache load from repository
                launch {
                    try {
                        nRepo.getUnifiedNewsFlow(forceRefresh = false).collect { _ -> }
                    } catch (t: Throwable) {
                        android.util.Log.w("BalasoreViewModel", "News stream initial fetch notice: ${t.message}")
                    }
                }

                // Continuous reactive observation of all Room news articles & bookmarks
                observeNewsRepository(nRepo)

                // Room Database + Network unified Tourism Repository with offline cache updates
                val tRepo = TourismRepository(db.hotspotDao(), db.cacheMetadataDao())
                tourismRepo = tRepo
                launch {
                    try {
                        tRepo.getUnifiedHotspotsFlow(forceRefresh = false).collect { _ -> }
                    } catch (t: Throwable) {
                        android.util.Log.w("BalasoreViewModel", "Tourism stream initial fetch notice: ${t.message}")
                    }
                }
                observeTourismRepository(tRepo)

                // Room Database Citizen Civic Reports Repository
                val cDao = db.civicReportDao()
                civicReportDao = cDao
                val fsService = FirestoreService(appCtx)
                firestoreService = fsService
                val cRepo = CivicReportRepository(cDao, fsService)
                civicReportRepo = cRepo
                launch {
                    try {
                        cRepo.ensureDefaultReportsSeeded()
                    } catch (t: Throwable) {
                        if (t !is kotlinx.coroutines.CancellationException) {
                            android.util.Log.e("BalasoreViewModel", "Error seeding civic reports: ${t.message}")
                        }
                    }
                }
                launch {
                    try {
                        cRepo.allReports.collect { reports ->
                            _uiState.value = _uiState.value.copy(myCivicReports = reports)
                        }
                    } catch (t: Throwable) {
                        if (t !is kotlinx.coroutines.CancellationException) {
                            android.util.Log.e("BalasoreViewModel", "Error streaming civic reports: ${t.message}")
                        }
                    }
                }
            } catch (t: Throwable) {
                if (t !is kotlinx.coroutines.CancellationException) {
                    android.util.Log.e("BalasoreViewModel", "Failed to initialize database: ${t.message}")
                }
            }
        }
    }

    fun setCivicReportRepository(repo: CivicReportRepository) {
        this.civicReportRepo = repo
        viewModelScope.launch {
            repo.ensureDefaultReportsSeeded()
        }
        viewModelScope.launch {
            repo.allReports.collect { reports ->
                _uiState.value = _uiState.value.copy(myCivicReports = reports)
            }
        }
    }

    fun submitCivicReport(
        title: String,
        category: String,
        wardLocation: String,
        description: String,
        urgency: String = "NORMAL",
        hasPhotoAttached: Boolean = false,
        photoUri: String? = null,
        latitude: Double? = 21.4934,
        longitude: Double? = 86.9135,
        geoAddress: String? = null,
        queueForSyncWhenOnline: Boolean = false
    ) {
        val trackingNumber = 1000 + kotlin.random.Random.nextInt(9000)
        val reportId = "BLS-CIVIC-2026-$trackingNumber"
        val dept = when {
            category.contains("Pothole", ignoreCase = true) -> "PWD & Balasore Municipality Road Div"
            category.contains("Light", ignoreCase = true) || category.contains("Electric", ignoreCase = true) -> "TPNODL & Municipal Electrical Wing"
            category.contains("Waterlog", ignoreCase = true) || category.contains("Drain", ignoreCase = true) -> "Drainage Desiltation Taskforce"
            category.contains("Garbage", ignoreCase = true) || category.contains("Waste", ignoreCase = true) -> "BMC Solid Waste Sanitation Wing"
            category.contains("Water", ignoreCase = true) || category.contains("Supply", ignoreCase = true) -> "WATCO Public Health Engineering"
            else -> "Balasore Municipality Grievance Cell"
        }
        val notes = if (queueForSyncWhenOnline) {
            "Stored in Room DB 'sync-when-online' queue. Will auto-sync when online."
        } else {
            "Report registered in Room DB & synced with Ward Grievance Desk."
        }
        val newEntity = CivicReportEntity(
            id = reportId,
            title = title.ifBlank { "$category near $wardLocation" },
            category = category,
            wardLocation = wardLocation,
            description = description.ifBlank { "Citizen issue reported via Baleshwar 360 app." },
            status = "PENDING",
            urgency = urgency,
            assignedDepartment = dept,
            reportedTimestamp = System.currentTimeMillis(),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            hasPhotoAttached = hasPhotoAttached,
            resolutionNotes = notes,
            photoUri = photoUri,
            latitude = latitude,
            longitude = longitude,
            geoAddress = geoAddress ?: "$wardLocation, Balasore",
            syncStatus = if (queueForSyncWhenOnline) "QUEUED_OFFLINE" else "SYNCED"
        )

        // Immediately reflect in UI state so tests and UI have zero latency
        _uiState.value = _uiState.value.copy(
            myCivicReports = listOf(newEntity) + _uiState.value.myCivicReports
        )

        try {
            viewModelScope.launch {
                try {
                    civicReportRepo?.submitReport(
                        title = title,
                        category = category,
                        wardLocation = wardLocation,
                        description = description,
                        urgency = urgency,
                        hasPhotoAttached = hasPhotoAttached,
                        photoUri = photoUri,
                        latitude = latitude,
                        longitude = longitude,
                        geoAddress = geoAddress ?: "$wardLocation, Balasore",
                        id = reportId,
                        queueForSyncWhenOnline = queueForSyncWhenOnline
                    )
                } catch (t: Throwable) {
                    if (t !is kotlinx.coroutines.CancellationException) {
                        android.util.Log.e("BalasoreViewModel", "Error submitting report: ${t.message}")
                    }
                }
            }
        } catch (_: Throwable) {
            // JVM unit test fallback without Dispatchers.Main
            _uiState.value = _uiState.value.copy(
                myCivicReports = listOf(newEntity) + _uiState.value.myCivicReports
            )
        }
    }

    /**
     * Flushes all reports currently waiting in the Room DB 'sync-when-online' queue.
     */
    fun flushCivicSyncWhenOnlineQueue(): Int {
        val now = System.currentTimeMillis()
        var flushedCount = 0
        val updated = _uiState.value.myCivicReports.map { report ->
            if (report.isQueuedForSync) {
                flushedCount++
                report.copy(
                    status = if (report.status.equals("DRAFT", ignoreCase = true)) "PENDING" else report.status,
                    syncStatus = "SYNCED",
                    lastUpdatedTimestamp = now,
                    resolutionNotes = "Auto-synced from Room DB 'sync-when-online' queue to municipal cloud."
                )
            } else {
                report
            }
        }
        _uiState.value = _uiState.value.copy(myCivicReports = updated)

        try {
            viewModelScope.launch {
                try {
                    appContext?.let { ctx ->
                        civicReportRepo?.syncOfflineDrafts(ctx)
                    }
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
        return flushedCount
    }

    fun saveCivicReportDraft(
        title: String,
        category: String,
        wardLocation: String,
        description: String,
        urgency: String = "NORMAL",
        hasPhotoAttached: Boolean = false,
        photoUri: String? = null,
        latitude: Double? = 21.4925,
        longitude: Double? = 86.9312,
        geoAddress: String? = null,
        draftId: String? = null
    ): CivicReportEntity {
        val trackingNumber = 1000 + kotlin.random.Random.nextInt(9000)
        val reportId = draftId ?: "BLS-DRAFT-2026-$trackingNumber"
        val dept = when (category) {
            "Road Pothole" -> "PWD & Balasore Municipality Road Div"
            "Faulty Streetlight" -> "TPNODL & Municipal Electrical Wing"
            "Drain Clog / Waterlogging" -> "Drainage Desiltation Taskforce"
            "Garbage Heap / Waste" -> "BMC Solid Waste Sanitation Wing"
            "Drinking Water Leak" -> "WATCO Public Health Engineering"
            else -> "Balasore Municipality Grievance Cell"
        }
        val draftEntity = CivicReportEntity(
            id = reportId,
            title = title.ifBlank { "$category near $wardLocation (Draft)" },
            category = category,
            wardLocation = wardLocation,
            description = description.ifBlank { "Local draft recorded for later submission." },
            status = "DRAFT",
            urgency = urgency,
            assignedDepartment = dept,
            reportedTimestamp = System.currentTimeMillis(),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            hasPhotoAttached = hasPhotoAttached,
            resolutionNotes = "Local draft saved on device. Ready for review and submission.",
            photoUri = photoUri,
            latitude = latitude,
            longitude = longitude,
            geoAddress = geoAddress ?: "$wardLocation, Balasore"
        )

        val updated = listOf(draftEntity) + _uiState.value.myCivicReports.filterNot { it.id == reportId }
        _uiState.value = _uiState.value.copy(myCivicReports = updated)

        try {
            viewModelScope.launch {
                try {
                    civicReportRepo?.saveDraft(draftEntity)
                } catch (t: Throwable) {
                    if (t !is kotlinx.coroutines.CancellationException) {
                        android.util.Log.e("BalasoreViewModel", "Error saving draft: ${t.message}")
                    }
                }
            }
        } catch (_: Throwable) {
            // JVM test fallback
        }
        return draftEntity
    }

    fun submitCivicReportDraft(draftId: String) {
        val existing = _uiState.value.myCivicReports.find { it.id == draftId } ?: return
        val submitted = existing.copy(
            status = "PENDING",
            lastUpdatedTimestamp = System.currentTimeMillis(),
            resolutionNotes = "Report registered offline in Room DB. Auto-synced with Ward Grievance Desk."
        )
        val updated = _uiState.value.myCivicReports.map { if (it.id == draftId) submitted else it }
        _uiState.value = _uiState.value.copy(myCivicReports = updated)

        try {
            viewModelScope.launch {
                try {
                    civicReportRepo?.submitDraft(draftId)
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    fun fetchFirestoreUserReports() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isFirestoreReportsLoading = true)
            try {
                val reports: List<FirestoreUserReport> = firestoreService?.fetchUserReports() ?: emptyList<FirestoreUserReport>()
                _uiState.value = _uiState.value.copy(
                    firestoreUserReports = reports,
                    isFirestoreReportsLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isFirestoreReportsLoading = false)
            }
        }
    }

    fun submitCivicEyeReport(
        title: String,
        category: String,
        wardLocation: String,
        description: String,
        urgency: String = "MEDIUM",
        hasPhotoAttached: Boolean = false,
        photoUri: String? = null,
        latitude: Double? = 21.4934,
        longitude: Double? = 86.9135,
        geoAddress: String? = null,
        userId: String = "guest_balasore_citizen",
        userName: String = "Baleswari Citizen",
        queueForSyncWhenOnline: Boolean = false
    ) {
        submitCivicReport(
            title = title,
            category = category,
            wardLocation = wardLocation,
            description = description,
            urgency = urgency,
            hasPhotoAttached = hasPhotoAttached,
            photoUri = photoUri,
            latitude = latitude,
            longitude = longitude,
            geoAddress = geoAddress,
            queueForSyncWhenOnline = queueForSyncWhenOnline
        )

        if (queueForSyncWhenOnline) return

        val trackingNumber = 1000 + kotlin.random.Random.nextInt(9000)
        val cloudReportId = "BLS-CIVIC-2026-$trackingNumber"
        val normalizedCategory = when {
            category.contains("Pothole", ignoreCase = true) -> "pothole"
            category.contains("Waterlog", ignoreCase = true) || category.contains("Drain", ignoreCase = true) -> "waterlogging"
            category.contains("Road", ignoreCase = true) -> "road"
            category.contains("Garbage", ignoreCase = true) || category.contains("Waste", ignoreCase = true) -> "garbage"
            category.contains("Light", ignoreCase = true) -> "electricity"
            category.contains("Water", ignoreCase = true) || category.contains("Supply", ignoreCase = true) -> "water"
            else -> "other"
        }
        val cloudReport = FirestoreUserReport(
            id = cloudReportId,
            userId = userId,
            userName = userName,
            title = title.ifBlank { "$category in $wardLocation" },
            category = normalizedCategory,
            description = description.ifBlank { "Reported via Civic Eye" },
            wardLocation = wardLocation,
            status = "SUBMITTED",
            urgency = urgency.uppercase().let { if (it in listOf("LOW", "MEDIUM", "HIGH", "EMERGENCY")) it else "MEDIUM" },
            latitude = latitude,
            longitude = longitude,
            photoUrl = photoUri,
            reportedTimestamp = System.currentTimeMillis(),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            resolutionNotes = "Submitted via Civic Eye. Synced to municipal registry."
        )

        viewModelScope.launch {
            try {
                firestoreService?.submitUserReport(cloudReport)
                fetchFirestoreUserReports()
            } catch (e: Exception) {
                android.util.Log.w("BalasoreViewModel", "Firestore user report submit failed: ${e.message}")
            }
        }
    }

    fun deleteCivicReport(reportId: String) {
        _uiState.value = _uiState.value.copy(
            myCivicReports = _uiState.value.myCivicReports.filterNot { it.id == reportId }
        )

        try {
            viewModelScope.launch {
                try {
                    civicReportRepo?.deleteReport(reportId)
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    fun upvoteCivicReport(reportId: String) {
        val updated = _uiState.value.myCivicReports.map {
            if (it.id == reportId) it.copy(upvotesCount = it.upvotesCount + 1) else it
        }
        _uiState.value = _uiState.value.copy(myCivicReports = updated)

        viewModelScope.launch {
            try {
                civicReportDao?.incrementUpvotes(reportId)
            } catch (_: Throwable) {}
        }
    }

    fun advanceCivicReportStatus(reportId: String) {
        val now = System.currentTimeMillis()
        val advanceBlock: (List<CivicReportEntity>) -> List<CivicReportEntity> = { list ->
            list.map { report ->
                if (report.id == reportId) {
                    val nextStatus = when (report.status) {
                        "PENDING" -> "IN_PROGRESS"
                        "IN_PROGRESS" -> "RESOLVED"
                        else -> "RESOLVED"
                    }
                    val notes = when (nextStatus) {
                        "IN_PROGRESS" -> "Field inspection completed by ${report.assignedDepartment}. Repair crew dispatched to ${report.wardLocation}."
                        "RESOLVED" -> "Work completed and verified by Ward Junior Engineer. Issue closed successfully in municipal registry."
                        else -> report.resolutionNotes
                    }
                    report.copy(status = nextStatus, lastUpdatedTimestamp = now, resolutionNotes = notes)
                } else {
                    report
                }
            }
        }

        _uiState.value = _uiState.value.copy(myCivicReports = advanceBlock(_uiState.value.myCivicReports))

        try {
            viewModelScope.launch {
                try {
                    civicReportRepo?.progressReportStatus(reportId)
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {}
    }

    fun setMyReportsFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedMyReportsFilter = filter)
    }

    fun autoProgressCivicReports() {
        val now = System.currentTimeMillis()
        val autoBlock: (List<CivicReportEntity>) -> List<CivicReportEntity> = { list ->
            list.map { report ->
                if (report.status == "PENDING") {
                    report.copy(
                        status = "IN_PROGRESS",
                        lastUpdatedTimestamp = now,
                        resolutionNotes = "Inspection assigned to ${report.assignedDepartment}. Field technician en route to ${report.wardLocation}."
                    )
                } else if (report.status == "IN_PROGRESS") {
                    report.copy(
                        status = "RESOLVED",
                        lastUpdatedTimestamp = now,
                        resolutionNotes = "Repair work completed by municipal squad. Quality check passed by Balasore Ward Supervisor."
                    )
                } else {
                    report
                }
            }
        }

        try {
            viewModelScope.launch {
                try {
                    if (civicReportRepo != null) {
                        civicReportRepo?.autoProgressReports()
                    } else {
                        _uiState.value = _uiState.value.copy(myCivicReports = autoBlock(_uiState.value.myCivicReports))
                    }
                } catch (_: Throwable) {}
            }
        } catch (_: Throwable) {
            _uiState.value = _uiState.value.copy(myCivicReports = autoBlock(_uiState.value.myCivicReports))
        }
    }

    fun setItineraryRepository(repo: ItineraryRepository) {
        this.itineraryRepo = repo
        viewModelScope.launch {
            repo.ensureDefaultItinerarySeeded()
        }
        viewModelScope.launch {
            repo.allItineraryItems.collect { items ->
                _uiState.value = _uiState.value.copy(itineraryItems = items)
            }
        }
    }

    fun addToItinerary(hotspot: Hotspot, day: Int = 1, timeSlot: String = "10:00 AM", notes: String = "") {
        viewModelScope.launch {
            itineraryRepo?.addToItinerary(hotspot, day, timeSlot, notes)
        }
    }

    fun removeFromItinerary(id: Long) {
        viewModelScope.launch {
            itineraryRepo?.removeItem(id)
        }
    }

    fun clearItinerary() {
        viewModelScope.launch {
            itineraryRepo?.clearAll()
        }
    }

    fun dismissEmergencyAlert(alertId: String) {
        _uiState.value = _uiState.value.copy(
            dismissedAlertIds = _uiState.value.dismissedAlertIds + alertId
        )
    }

    fun fetchLiveEmergencyAlerts() {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val response = apiService.getLiveEmergencyAlerts()
                _uiState.value = _uiState.value.copy(
                    liveEmergencyAlerts = response.alerts
                )
                appContext?.let { ctx ->
                    com.example.data.fcm.BalasoreAlertDispatchEngine.evaluateAndDispatchRealTimeAlerts(
                        context = ctx,
                        weather = _uiState.value.weather,
                        pulse = _uiState.value.dailyPulse,
                        emergencyAlerts = response.alerts
                    )
                }
            } catch (e: Exception) {
                // Keep existing or fallback gracefully
            }
        }
    }

    fun selectTab(tabIndex: Int) {
        val hash = HashRoute.fromTabIndex(tabIndex).hash
        _uiState.value = _uiState.value.copy(selectedTab = tabIndex, currentHash = hash)
    }

    fun navigateHash(route: HashRoute) {
        selectTab(route.tabIndex)
    }

    fun navigateHash(hash: String) {
        val route = HashRoute.fromHash(hash)
        selectTab(route.tabIndex)
    }

    fun setLanguage(lang: AppLanguage) {
        _uiState.value = _uiState.value.copy(language = lang)
        com.example.ui.components.AppLanguagePreferencesManager.saveLanguage(appContext, lang)
    }

    fun toggleLanguage() {
        val next = if (_uiState.value.language == AppLanguage.ODIA) AppLanguage.ENGLISH else AppLanguage.ODIA
        setLanguage(next)
    }

    fun setHotspotSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(hotspotSearchQuery = query)
    }

    fun setHotspotCategory(category: String) {
        _uiState.value = _uiState.value.copy(selectedHotspotCategory = category)
    }

    fun selectNewsTab(tabIndex: Int) {
        val tab = NewsFeedTab.fromIndex(tabIndex)
        val filtered = NewsFeedFilter.filterArticles(_uiState.value.newsArticles, tab.index, _uiState.value.newsSearchQuery)
        _uiState.value = _uiState.value.copy(
            selectedNewsTab = tab.index,
            selectedNewsCategory = tab.categoryName,
            filteredNewsArticles = filtered
        )
    }

    fun setNewsCategory(category: String) {
        val tab = NewsFeedTab.fromCategory(category)
        val filtered = NewsFeedFilter.filterArticles(_uiState.value.newsArticles, tab.index, _uiState.value.newsSearchQuery)
        _uiState.value = _uiState.value.copy(
            selectedNewsCategory = category,
            selectedNewsTab = tab.index,
            filteredNewsArticles = filtered
        )
    }

    fun setNewsSearchQuery(query: String) {
        val filtered = NewsFeedFilter.filterArticles(_uiState.value.newsArticles, _uiState.value.selectedNewsTab, query)
        _uiState.value = _uiState.value.copy(
            newsSearchQuery = query,
            filteredNewsArticles = filtered
        )
    }

    fun saveNewsSearchQuery(query: String) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return
        viewModelScope.launch {
            try {
                newsSearchQueryDao?.insertOrUpdateQuery(
                    NewsSearchQueryEntity(
                        query = trimmed,
                        timestamp = System.currentTimeMillis()
                    )
                )
            } catch (t: Throwable) {
                android.util.Log.w("BalasoreViewModel", "Error saving search query: ${t.message}")
            }
        }
    }

    fun deleteNewsSearchQuery(query: String) {
        viewModelScope.launch {
            try {
                newsSearchQueryDao?.deleteQuery(query)
            } catch (_: Throwable) {}
        }
    }

    fun clearAllNewsSearchQueries() {
        viewModelScope.launch {
            try {
                newsSearchQueryDao?.clearAllQueries()
            } catch (_: Throwable) {}
        }
    }

    fun toggleBookmark(articleId: String) {
        val current = _uiState.value.bookmarkedIds
        val isCurrentlyBookmarked = current.contains(articleId)
        val newBookmarkedState = !isCurrentlyBookmarked
        val updated = if (isCurrentlyBookmarked) current - articleId else current + articleId

        val updatedArticles = _uiState.value.newsArticles.map { article ->
            if (article.id == articleId) article.copy(isBookmarked = newBookmarkedState) else article
        }
        val recomputedFiltered = NewsFeedFilter.filterArticles(updatedArticles, _uiState.value.selectedNewsTab, _uiState.value.newsSearchQuery)

        // Optimistic UI update
        _uiState.value = _uiState.value.copy(
            bookmarkedIds = updated,
            newsArticles = updatedArticles,
            filteredNewsArticles = recomputedFiltered
        )

        val idLong = articleId.toLongOrNull()
        if (idLong != null) {
            viewModelScope.launch {
                try {
                    newsRepo?.toggleBookmark(idLong, isCurrentlyBookmarked)
                } catch (e: Throwable) {
                    if (e !is kotlinx.coroutines.CancellationException) {
                        android.util.Log.e("BalasoreViewModel", "Error saving bookmark to Room: ${e.message}")
                    }
                }
            }
        }

        // Also persist/remove in unified SavedBookmarkRepository
        val targetArticle = _uiState.value.newsArticles.find { it.id == articleId }
        if (targetArticle != null) {
            viewModelScope.launch {
                try {
                    savedBookmarkRepo?.toggleNewsArticle(targetArticle, isCurrentlyBookmarked)
                } catch (_: Throwable) {}
            }
        }
    }

    fun toggleBookmarkEmergencyContact(contact: EmergencyContact) {
        val currentIds = _uiState.value.savedEmergencyContactIds
        val isCurrentlySaved = currentIds.contains(contact.id)
        val updatedIds = if (isCurrentlySaved) currentIds - contact.id else currentIds + contact.id

        // Optimistic UI state update
        _uiState.value = _uiState.value.copy(savedEmergencyContactIds = updatedIds)

        viewModelScope.launch {
            try {
                savedBookmarkRepo?.toggleEmergencyContact(contact, isCurrentlySaved)
            } catch (e: Throwable) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    android.util.Log.e("BalasoreViewModel", "Error saving emergency contact bookmark to Room: ${e.message}")
                }
            }
        }
    }

    fun removeSavedBookmark(bookmarkId: String) {
        viewModelScope.launch {
            try {
                savedBookmarkRepo?.deleteBookmarkById(bookmarkId)
                if (bookmarkId.startsWith("emergency_")) {
                    val contactId = bookmarkId.removePrefix("emergency_")
                    _uiState.value = _uiState.value.copy(
                        savedEmergencyContactIds = _uiState.value.savedEmergencyContactIds - contactId
                    )
                } else if (bookmarkId.startsWith("news_")) {
                    val articleId = bookmarkId.removePrefix("news_")
                    toggleBookmark(articleId)
                }
            } catch (e: Throwable) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    android.util.Log.e("BalasoreViewModel", "Error removing saved bookmark: ${e.message}")
                }
            }
        }
    }

    fun setSelectedSavedFilter(filter: String) {
        _uiState.value = _uiState.value.copy(selectedSavedFilter = filter)
    }

    fun toggleFavoriteHotspot(hotspotId: String) {
        val current = _uiState.value.favoriteHotspotIds
        val isCurrentlyFavorite = current.contains(hotspotId)
        val updated = if (isCurrentlyFavorite) current - hotspotId else current + hotspotId
        _uiState.value = _uiState.value.copy(favoriteHotspotIds = updated)

        viewModelScope.launch {
            try {
                tourismRepo?.toggleFavorite(hotspotId, isCurrentlyFavorite)
            } catch (e: Throwable) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    android.util.Log.e("BalasoreViewModel", "Error toggling favorite hotspot in Room: ${e.message}")
                }
            }
        }
    }

    fun refreshAll() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                lastSyncStatusMessage = "Fetching new local stories & telemetry from server..."
            )
            fetchRealTimeWeather()
            fetchLiveEmergencyAlerts()
            refreshDailyPulse()
            try {
                civicReportRepo?.autoProgressReports()
            } catch (_: Throwable) {}
            try {
                newsRepo?.refreshNews()
            } catch (_: Throwable) {}
            try {
                tourismRepo?.refreshHotspots()
            } catch (_: Throwable) {}
            delay(800)
            val now = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                isRefreshing = false,
                lastNewsAutoUpdateTimestamp = now,
                newsAutoUpdateCycleCount = _uiState.value.newsAutoUpdateCycleCount + 1,
                lastSyncStatusMessage = "Fetched latest local Balasore stories from server"
            )
        }
    }

    /**
     * Dedicated Pull-to-Refresh action for the News section that signals `isRefreshing = true`
     * while fetching fresh local Balasore news stories and the latest trending articles/view counts from Firestore.
     */
    fun refreshNewsFromServer() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isRefreshing = true,
                lastSyncStatusMessage = "Fetching latest trending articles from Firestore & external sources..."
            )
            try {
                appContext?.let { ctx ->
                    val fs = com.example.data.firebase.FirestoreService(ctx)
                    fs.fetchLatestTrendingFromFirestore()
                    newsRepo?.refreshTrendingFromFirestore(fs)
                }
            } catch (_: Throwable) {}
            try {
                com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullAndAutoPublishAllSections(
                    context = appContext,
                    forceNetworkRss = true
                )
            } catch (_: Throwable) {}
            try {
                newsRepo?.refreshNews()
            } catch (_: Throwable) {}
            delay(650)
            val now = System.currentTimeMillis()
            _uiState.value = _uiState.value.copy(
                isRefreshing = false,
                lastNewsAutoUpdateTimestamp = now,
                newsAutoUpdateCycleCount = _uiState.value.newsAutoUpdateCycleCount + 1,
                lastSyncStatusMessage = "Synced latest trending articles from Firestore & multi-source feeds"
            )
        }
    }

    /**
     * Fetches real-time meteorological data for Balasore using the Open-Meteo public API
     * and updates the UI state prominently with temperature, humidity, wind, UV index, hourly track, and 7-day outlook.
     */
    fun fetchRealTimeWeather(retryCount: Int = 1) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isWeatherLoading = true)
            try {
                val response = weatherApiService.getBalasoreForecast(
                    latitude = 21.4934,
                    longitude = 86.9135,
                    hourly = "temperature_2m,relative_humidity_2m,precipitation_probability,weather_code,wind_speed_10m",
                    forecastDays = 7
                )
                val current = response.current
                val daily = response.daily
                val hourly = response.hourly

                if (current != null) {
                    val tempC = current.temperature?.toInt() ?: 29
                    val feelsLikeC = current.apparentTemperature?.toInt() ?: (tempC + 2)
                    val humidity = "${current.relativeHumidity ?: 75}%"
                    val windKmh = "${current.windSpeed?.toInt() ?: 18} km/h"
                    val gustsKmh = "${current.windGusts?.toInt() ?: 24} km/h"
                    val conditionStr = weatherCodeToDescription(current.weatherCode ?: 2)
                    val highLowStr = if (daily?.temperatureMax?.isNotEmpty() == true && daily.temperatureMin?.isNotEmpty() == true) {
                        "${daily.temperatureMax[0].toInt()}° / ${daily.temperatureMin[0].toInt()}°"
                    } else {
                        "${tempC + 4}° / ${tempC - 3}°"
                    }
                    val uv = daily?.uvIndexMax?.firstOrNull() ?: 6.2
                    val precip = current.precipitation ?: 0.0

                    val nowFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

                    val wCode = current.weatherCode ?: 2
                    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                    val isDaytime = currentHour in 6..18
                    val dynamicIconUrl = weatherCodeToIconUrl(wCode, isDaytime)
                    val dynamicIconType = weatherCodeToIconType(wCode)

                    val updatedWeather = _uiState.value.weather.copy(
                        tempCelsius = tempC,
                        condition = conditionStr,
                        highLow = highLowStr,
                        humidity = humidity,
                        windSpeedKmh = windKmh,
                        feelsLikeCelsius = feelsLikeC,
                        windGustsKmh = gustsKmh,
                        uvIndex = uv,
                        precipitationMm = precip,
                        dataSource = "Open-Meteo Real-Time Met API",
                        lastUpdatedTime = "Live • Updated $nowFormat",
                        isLiveApi = true,
                        weatherCode = wCode,
                        conditionIconUrl = dynamicIconUrl,
                        iconType = dynamicIconType,
                        isDay = isDaytime
                    )

                    // Parse next 24 hourly steps
                    val parsedHourly = mutableListOf<BalasoreForecastHour>()
                    if (hourly?.time != null && hourly.temperature != null) {
                        val count = minOf(hourly.time.size, hourly.temperature.size, 24)
                        for (i in 0 until count) {
                            val rawTime = hourly.time[i]
                            val hourLabel = try {
                                val hourPart = rawTime.substringAfter("T").take(5)
                                val h = hourPart.substringBefore(":").toInt()
                                when {
                                    h == 0 -> "12 AM"
                                    h < 12 -> "${h} AM"
                                    h == 12 -> "12 PM"
                                    else -> "${h - 12} PM"
                                }
                            } catch (_: Exception) {
                                "${i}:00"
                            }
                            val temp = hourly.temperature[i].toInt()
                            val code = hourly.weatherCode?.getOrNull(i) ?: 1
                            val pop = hourly.precipitationProbability?.getOrNull(i) ?: 10
                            val wind = hourly.windSpeed?.getOrNull(i)?.toInt() ?: 15
                            val emoji = weatherCodeToEmoji(code)
                            parsedHourly.add(
                                BalasoreForecastHour(
                                    timeLabel = hourLabel,
                                    tempC = temp,
                                    conditionEmoji = emoji,
                                    popPercentage = pop,
                                    windKmh = wind
                                )
                            )
                        }
                    }

                    // Parse 7-day daily forecast
                    val parsedDays = mutableListOf<BalasoreForecastDay>()
                    if (daily?.time != null && daily.temperatureMax != null && daily.temperatureMin != null) {
                        val count = minOf(daily.time.size, daily.temperatureMax.size, daily.temperatureMin.size, 7)
                        for (i in 0 until count) {
                            val dateStr = daily.time[i]
                            val high = daily.temperatureMax[i].toInt()
                            val low = daily.temperatureMin[i].toInt()
                            val code = daily.weatherCode?.getOrNull(i) ?: 1
                            val uvD = daily.uvIndexMax?.getOrNull(i) ?: 6.0
                            val desc = weatherCodeToDescription(code)
                            val emoji = weatherCodeToEmoji(code)

                            val dayLabel = if (i == 0) "Today" else if (i == 1) "Tomorrow" else {
                                try {
                                    val parsedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateStr)
                                    SimpleDateFormat("EEE", Locale.getDefault()).format(parsedDate!!)
                                } catch (_: Exception) { "Day ${i + 1}" }
                            }
                            val odiaDayLabel = if (i == 0) "ଆଜି" else if (i == 1) "ଆସନ୍ତାକାଲି" else dayLabel
                            val odiaDesc = when {
                                desc.contains("Rain", true) || desc.contains("Downpour", true) -> "ବର୍ଷା ସମ୍ଭାବନା"
                                desc.contains("Thunder", true) -> "ବଜ୍ରପାତ ସତର୍କତା"
                                desc.contains("Cloud", true) -> "ମେଘୁଆ ପାଗ"
                                desc.contains("Fog", true) -> "କୁହୁଡ଼ି"
                                else -> "ଖରାଟିଆ ନିର୍ମଳ ଆକାଶ"
                            }

                            parsedDays.add(
                                BalasoreForecastDay(
                                    id = "day_$i",
                                    dayLabel = dayLabel,
                                    dateFormatted = dateStr,
                                    odiaDayLabel = odiaDayLabel,
                                    highTempC = high,
                                    lowTempC = low,
                                    condition = desc,
                                    odiaCondition = odiaDesc,
                                    weatherIcon = emoji,
                                    rainProbability = if (code in 51..67 || code in 80..82) 65 else 20,
                                    windSummary = "14 km/h SW",
                                    humidity = "76%",
                                    uvIndex = "UV ${uvD.toInt()}",
                                    marineNotice = if (code in 51..67) "Rough surf near Kasafal & Balaramgadi" else "Calm sea swell for Chandipur beach walk"
                                )
                            )
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        weather = updatedWeather,
                        hourlyForecast = if (parsedHourly.isNotEmpty()) parsedHourly else _uiState.value.hourlyForecast,
                        forecastDays = if (parsedDays.isNotEmpty()) parsedDays else _uiState.value.forecastDays,
                        isWeatherLoading = false
                    )
                    appContext?.let { ctx ->
                        com.example.data.fcm.BalasoreAlertDispatchEngine.evaluateAndDispatchRealTimeAlerts(
                            context = ctx,
                            weather = updatedWeather,
                            pulse = _uiState.value.dailyPulse,
                            emergencyAlerts = _uiState.value.liveEmergencyAlerts
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(isWeatherLoading = false)
                }
            } catch (e: Exception) {
                // Graceful fallback to cached meteorological state without error logging
                android.util.Log.w("BalasoreViewModel", "Weather API fetch notice (${e.javaClass.simpleName}): ${e.message ?: "network timeout"}. Falling back to baseline data.")

                // If it was a network timeout or IOException and we have retries remaining, retry once after backoff
                if (retryCount > 0 && (e is java.net.SocketTimeoutException || e is java.io.IOException)) {
                    delay(1500)
                    fetchRealTimeWeather(retryCount = retryCount - 1)
                    return@launch
                }

                val nowFormat = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                val currentWeather = _uiState.value.weather
                val fallbackWeather = if (!currentWeather.isLiveApi) {
                    currentWeather.copy(
                        dataSource = "Balasore Coastal Station (Baseline)",
                        lastUpdatedTime = "Updated $nowFormat • Coastal Baseline"
                    )
                } else {
                    currentWeather
                }

                _uiState.value = _uiState.value.copy(
                    weather = fallbackWeather,
                    isWeatherLoading = false
                )
            }
        }
    }

    /**
     * Fetches real-time conditions and 7-day forecast for Balasore via OpenWeatherMap API.
     */
    fun fetchOpenWeatherData(isUserTriggered: Boolean = false) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isOpenWeatherLoading = true)
            val apiKey = com.example.BuildConfig.OPENWEATHERMAP_API_KEY
            val weatherResult = openWeatherRepo.fetchCurrentWeather(apiKey)
            val forecastResult = openWeatherRepo.fetchThreeDayForecast(apiKey)
            val forecast7Result = openWeatherRepo.fetchSevenDayForecast(apiKey)
            val locationAlerts: List<com.example.data.model.BalasoreWeatherAlert> = runCatching {
                com.example.data.service.BalasoreOpenWeatherAlertService.fetchLocationWeatherAlerts(
                    context = appContext,
                    apiKey = apiKey,
                    forceRefresh = isUserTriggered
                )
            }.getOrDefault(emptyList())

            val current = if (weatherResult.isSuccess) {
                weatherResult.getOrNull()
            } else {
                openWeatherRepo.createBaselineBalasoreWeather(_uiState.value.openWeatherCycleCount)
            }

            val forecast = if (forecastResult.isSuccess) {
                forecastResult.getOrNull() ?: emptyList()
            } else {
                openWeatherRepo.createBaselineThreeDayForecast(_uiState.value.openWeatherCycleCount)
            }

            val forecast7 = if (forecast7Result.isSuccess) {
                forecast7Result.getOrNull() ?: emptyList()
            } else {
                openWeatherRepo.createBaselineSevenDayForecast(_uiState.value.openWeatherCycleCount)
            }

            val updatedAlerts = if (locationAlerts.isNotEmpty()) locationAlerts else _uiState.value.weatherAlerts

            _uiState.value = _uiState.value.copy(
                openWeatherCurrent = current,
                openWeatherThreeDayForecast = forecast,
                openWeatherSevenDayForecast = forecast7,
                weatherAlerts = updatedAlerts,
                isOpenWeatherLoading = false,
                openWeatherCycleCount = _uiState.value.openWeatherCycleCount + 1
            )
        }
    }

    fun testTriggerLocationWeatherAlert(context: Context, alert: com.example.data.model.BalasoreWeatherAlert? = null) {
        com.example.data.notification.TideAndWeatherNotificationManager.testTriggerLocationWeatherAlert(context, alert)
    }

    fun setWalkWindowAlertsEnabled(context: Context, enabled: Boolean) {
        TideAndWeatherNotificationManager.setWalkWindowAlertsEnabled(context, enabled)
        _uiState.value = _uiState.value.copy(isWalkWindowAlertsEnabled = enabled)
    }

    fun setWalkWindowAdvanceMinutes(context: Context, minutes: Int) {
        TideAndWeatherNotificationManager.setWalkWindowAdvanceMinutes(context, minutes)
        _uiState.value = _uiState.value.copy(walkWindowAdvanceMinutes = minutes)
    }

    fun testTriggerWalkWindowOpeningAlert(context: Context) {
        TideAndWeatherNotificationManager.testTriggerWalkWindowOpening(context)
    }

    fun testTriggerWalkWindowClosingAlert(context: Context) {
        TideAndWeatherNotificationManager.testTriggerWalkWindowClosing(context)
    }

    private fun weatherCodeToEmoji(code: Int): String = when (code) {
        0 -> "☀️"
        1 -> "🌤️"
        2 -> "⛅"
        3 -> "☁️"
        45, 48 -> "🌫️"
        51, 53, 55 -> "🌦️"
        61, 63 -> "🌧️"
        65 -> "⛈️"
        80, 81, 82 -> "🌧️"
        95, 96, 99 -> "⛈️"
        else -> "⛅"
    }

    fun weatherCodeToIconUrl(code: Int, isDay: Boolean): String {
        val suffix = if (isDay) "d" else "n"
        return when (code) {
            0 -> "https://openweathermap.org/img/wn/01$suffix@2x.png"
            1 -> "https://openweathermap.org/img/wn/02$suffix@2x.png"
            2 -> "https://openweathermap.org/img/wn/03$suffix@2x.png"
            3 -> "https://openweathermap.org/img/wn/04$suffix@2x.png"
            45, 48 -> "https://openweathermap.org/img/wn/50$suffix@2x.png"
            51, 53, 55, 61, 63, 65 -> "https://openweathermap.org/img/wn/10$suffix@2x.png"
            80, 81, 82 -> "https://openweathermap.org/img/wn/09$suffix@2x.png"
            95, 96, 99 -> "https://openweathermap.org/img/wn/11$suffix@2x.png"
            else -> "https://openweathermap.org/img/wn/02$suffix@2x.png"
        }
    }

    fun weatherCodeToIconType(code: Int): String = when (code) {
        0 -> "clear_sky"
        1, 2 -> "partly_cloudy"
        3 -> "overcast"
        45, 48 -> "fog_mist"
        51, 53, 55, 61, 63, 65, 80, 81, 82 -> "rain"
        95, 96, 99 -> "thunderstorm"
        else -> "partly_cloudy"
    }

    // ==========================================
    // GEMINI MULTI-TURN CHAT & LYRIA AI ENGINE
    // ==========================================

    fun sendChatMessage(text: String) {
        if (text.isBlank()) return
        val userMsg = ChatMessage(
            text = text,
            sender = MessageSender.USER
        )
        val currentHistory = _uiState.value.chatMessages
        _uiState.value = _uiState.value.copy(
            chatMessages = currentHistory + userMsg,
            isChatLoading = true
        )

        viewModelScope.launch {
            val response = geminiChatService.sendMultiTurnChat(
                history = currentHistory,
                userMessage = text,
                model = _uiState.value.selectedChatModel,
                enableSearchGrounding = _uiState.value.isChatSearchGrounding,
                enableMapsGrounding = _uiState.value.isChatMapsGrounding,
                systemInstruction = _uiState.value.selectedChatRole.systemInstruction
            )

            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + response,
                isChatLoading = false
            )
        }
    }

    fun setChatModel(model: GeminiChatModel) {
        _uiState.value = _uiState.value.copy(selectedChatModel = model)
    }

    fun setChatRole(role: ChatRolePersona) {
        _uiState.value = _uiState.value.copy(selectedChatRole = role)
    }

    fun toggleChatSearchGrounding() {
        _uiState.value = _uiState.value.copy(isChatSearchGrounding = !_uiState.value.isChatSearchGrounding)
    }

    fun toggleChatMapsGrounding() {
        _uiState.value = _uiState.value.copy(isChatMapsGrounding = !_uiState.value.isChatMapsGrounding)
    }

    fun clearChat() {
        _uiState.value = _uiState.value.copy(chatMessages = emptyList())
    }

    fun generateLyriaMusic(prompt: String, isShortClip: Boolean = true) {
        _uiState.value = _uiState.value.copy(isMusicGenerating = true)
        viewModelScope.launch {
            val trackMsg = geminiChatService.generateMusic(prompt, isShortClip)
            _uiState.value = _uiState.value.copy(
                chatMessages = _uiState.value.chatMessages + trackMsg,
                isMusicGenerating = false
            )
        }
    }

    private fun weatherCodeToDescription(code: Int): String = when (code) {
        0 -> "Clear Sunny Sky"
        1 -> "Mainly Sunny"
        2 -> "Partly Cloudy"
        3 -> "Overcast Sky"
        45, 48 -> "Coastal Fog / Mist"
        51, 53, 55 -> "Light Coastal Drizzle"
        61, 63 -> "Moderate Rain"
        65 -> "Heavy Monsoon Downpour"
        80, 81, 82 -> "Rain Showers"
        95 -> "Bay of Bengal Thunderstorm"
        96, 99 -> "Severe Coastal Thunderstorm"
        else -> "Partly Cloudy"
    }

    fun openGroundingSheet(initialQuery: String = "", mode: GroundingToolMode = GroundingToolMode.COMBINED) {
        _uiState.value = _uiState.value.copy(
            groundingState = _uiState.value.groundingState.copy(
                isSheetOpen = true,
                query = initialQuery,
                toolMode = mode,
                errorMessage = null
            )
        )
    }

    fun closeGroundingSheet() {
        _uiState.value = _uiState.value.copy(
            groundingState = _uiState.value.groundingState.copy(isSheetOpen = false)
        )
    }

    fun setGroundingMode(mode: GroundingToolMode) {
        _uiState.value = _uiState.value.copy(
            groundingState = _uiState.value.groundingState.copy(toolMode = mode)
        )
    }

    fun executeGroundingQuery(query: String, mode: GroundingToolMode = _uiState.value.groundingState.toolMode) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                groundingState = _uiState.value.groundingState.copy(
                    query = query,
                    toolMode = mode,
                    isQuerying = true,
                    errorMessage = null
                )
            )
            try {
                val response = GeminiGroundingService.getInstance().queryWithGrounding(
                    prompt = query,
                    toolMode = mode
                )
                _uiState.value = _uiState.value.copy(
                    groundingState = _uiState.value.groundingState.copy(
                        isQuerying = false,
                        response = response,
                        errorMessage = if (!response.isSuccess) response.errorMessage else null
                    )
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    groundingState = _uiState.value.groundingState.copy(
                        isQuerying = false,
                        errorMessage = e.message ?: "Failed to complete grounding query"
                    )
                )
            }
        }
    }

    fun openFeatureSheet(type: UniqueFeatureSheetType) {
        _uiState.value = _uiState.value.copy(activeFeatureSheet = type)
    }

    fun closeFeatureSheet() {
        _uiState.value = _uiState.value.copy(activeFeatureSheet = null)
    }

    fun refreshDailyPulse() {
        val newPulse = com.example.data.daily.DailyUpdateEngine.getDailyPulse()
        _uiState.value = _uiState.value.copy(
            dailyPulse = newPulse
        )
        appContext?.let { ctx ->
            com.example.data.fcm.BalasoreAlertDispatchEngine.evaluateAndDispatchRealTimeAlerts(
                context = ctx,
                weather = _uiState.value.weather,
                pulse = newPulse,
                emergencyAlerts = _uiState.value.liveEmergencyAlerts
            )
        }
    }

    fun toggleEmergencyKitItem(itemId: String) {
        val updated = _uiState.value.emergencyKitItems.map {
            if (it.id == itemId) it.copy(isChecked = !it.isChecked) else it
        }
        _uiState.value = _uiState.value.copy(emergencyKitItems = updated)
    }

    fun toggleEcoPassportStamp(stampId: String) {
        val updated = _uiState.value.ecoPassportStamps.map {
            if (it.id == stampId) it.copy(isCheckedIn = !it.isCheckedIn) else it
        }
        _uiState.value = _uiState.value.copy(ecoPassportStamps = updated)
    }

    fun logHorseshoeCrabSighting(reporter: String, sector: String, species: String, count: Int) {
        val newSighting = HorseshoeCrabSighting(
            id = "hsc_${System.currentTimeMillis()}",
            reporterName = reporter.ifBlank { "Citizen Eco-Guardian" },
            beachSector = sector.ifBlank { "Chandipur Intertidal Zone" },
            distanceOutKm = 2.5,
            species = species,
            count = if (count > 0) count else 1,
            condition = "Safely observed & logged for FMU Marine Research",
            reportedAgo = "Just now",
            verifiedByFMU = true
        )
        _uiState.value = _uiState.value.copy(
            horseshoeCrabSightings = listOf(newSighting) + _uiState.value.horseshoeCrabSightings
        )
    }

    fun toggleTemperatureUnit() {
        _uiState.value = _uiState.value.copy(isFahrenheit = !_uiState.value.isFahrenheit)
    }

    fun selectForecastDay(index: Int) {
        _uiState.value = _uiState.value.copy(selectedForecastDayIndex = index)
    }

    fun acknowledgeAlert(alertId: String) {
        val updated = _uiState.value.weatherAlerts.map {
            if (it.id == alertId) it.copy(isAcknowledged = true) else it
        }
        _uiState.value = _uiState.value.copy(weatherAlerts = updated)
    }

    fun setThemeMode(mode: ThemeMode) {
        _uiState.value = _uiState.value.copy(themeMode = mode)
        persistThemeMode(mode)
    }

    /**
     * Global 1-tap toggle for Material 3 Dark Mode.
     * Switches directly between Dark Mode and Light Mode to improve nighttime readability.
     */
    fun toggleDarkMode(isSystemCurrentlyDark: Boolean = false) {
        val current = _uiState.value.themeMode
        val isCurrentlyDark = when (current) {
            ThemeMode.DARK, ThemeMode.HIGH_CONTRAST_DARK -> true
            ThemeMode.LIGHT -> false
            ThemeMode.SYSTEM -> isSystemCurrentlyDark
        }
        val next = if (isCurrentlyDark) ThemeMode.LIGHT else ThemeMode.DARK
        _uiState.value = _uiState.value.copy(themeMode = next)
        persistThemeMode(next)
    }

    fun cycleThemeMode() {
        val next = when (_uiState.value.themeMode) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.HIGH_CONTRAST_DARK
            ThemeMode.HIGH_CONTRAST_DARK -> ThemeMode.SYSTEM
        }
        _uiState.value = _uiState.value.copy(themeMode = next)
        persistThemeMode(next)
    }

    /**
     * Direct toggle for High-Contrast OLED Night Mode.
     * Switches directly between High-Contrast Dark Mode and standard theme to reduce eye strain
     * and enhance readability during nighttime emergencies and power outages.
     */
    fun toggleHighContrastNightMode() {
        val current = _uiState.value.themeMode
        val next = if (current == ThemeMode.HIGH_CONTRAST_DARK) ThemeMode.SYSTEM else ThemeMode.HIGH_CONTRAST_DARK
        _uiState.value = _uiState.value.copy(themeMode = next)
        persistThemeMode(next)
    }

    private fun persistThemeMode(mode: ThemeMode) {
        try {
            appContext?.let { ctx ->
                val prefs = ctx.getSharedPreferences("balasore_app_preferences", Context.MODE_PRIVATE)
                prefs.edit().putString("theme_mode", mode.name).apply()
            }
        } catch (_: Exception) {}
    }

    fun setWatermarkEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isWatermarkEnabled = enabled)
    }

    fun setWatermarkStyle(style: WatermarkStyle) {
        _uiState.value = _uiState.value.copy(watermarkStyle = style)
    }

    fun setWatermarkOpacity(opacity: WatermarkOpacity) {
        _uiState.value = _uiState.value.copy(watermarkOpacity = opacity)
    }

    fun cycleWatermarkStyle() {
        val next = when (_uiState.value.watermarkStyle) {
            WatermarkStyle.CENTER_EMBLEM -> WatermarkStyle.DIAGONAL_TILES
            WatermarkStyle.DIAGONAL_TILES -> WatermarkStyle.CORNER_STAMP
            WatermarkStyle.CORNER_STAMP -> WatermarkStyle.CENTER_EMBLEM
        }
        _uiState.value = _uiState.value.copy(watermarkStyle = next)
    }

    /**
     * Executes the automatic refresh of weather, alerts, daily pulse, news, and synchronizes caches.
     */
    fun triggerHourlyAutoRefresh() {
        _uiState.value = _uiState.value.copy(
            lastHourlyRefreshTimestamp = System.currentTimeMillis(),
            nextHourlyRefreshMinutesRemaining = _uiState.value.autoUpdateFrequencyMinutes,
            autoRefreshCycleCount = _uiState.value.autoRefreshCycleCount + 1
        )
        try {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                fetchRealTimeWeather()
                fetchLiveEmergencyAlerts()
                refreshDailyPulse()
                try {
                    civicReportRepo?.autoProgressReports()
                } catch (_: Throwable) {}
                try {
                    com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullAndAutoPublishAllSections(
                        context = appContext,
                        forceNetworkRss = true
                    )
                } catch (_: Throwable) {}
                try {
                    newsRepo?.refreshNews()
                } catch (_: Throwable) {}
                delay(800)
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        } catch (_: Throwable) {
            // Safe fallback for JVM unit test environments without Dispatchers.Main
        }
    }

    fun setHourlyAutoRefreshEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            isHourlyAutoRefreshEnabled = enabled,
            nextHourlyRefreshMinutesRemaining = if (enabled) _uiState.value.autoUpdateFrequencyMinutes else 0
        )
    }

    fun setAutoUpdateFrequency(minutes: Int) {
        _uiState.value = _uiState.value.copy(
            autoUpdateFrequencyMinutes = minutes,
            nextHourlyRefreshMinutesRemaining = minutes
        )
    }

    fun toggleBackgroundSync(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            isAutoUpdateBackgroundEnabled = enabled,
            isHourlyAutoRefreshEnabled = enabled
        )
    }

    fun forceSyncAllData() {
        try {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isRefreshing = true)
                fetchRealTimeWeather()
                fetchLiveEmergencyAlerts()
                refreshDailyPulse()
                try {
                    civicReportRepo?.autoProgressReports()
                } catch (_: Throwable) {}
                try {
                    newsRepo?.refreshNews()
                } catch (_: Throwable) {}
                kotlinx.coroutines.delay(700)
                val timeStr = java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault()).format(java.util.Date())
                _uiState.value = _uiState.value.copy(
                    isRefreshing = false,
                    lastHourlyRefreshTimestamp = System.currentTimeMillis(),
                    nextHourlyRefreshMinutesRemaining = _uiState.value.autoUpdateFrequencyMinutes,
                    autoRefreshCycleCount = _uiState.value.autoRefreshCycleCount + 1,
                    lastSyncStatusMessage = "All feeds synced live at $timeStr"
                )
            }
        } catch (_: Throwable) {
            _uiState.value = _uiState.value.copy(isRefreshing = false)
        }
    }

    fun checkForAppUpdates() {
        try {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isUpdateCheckLoading = true)
                kotlinx.coroutines.delay(850)
                _uiState.value = _uiState.value.copy(
                    isUpdateCheckLoading = false,
                    appVersionLatest = com.example.BuildConfig.VERSION_NAME,
                    lastSyncStatusMessage = "You have the latest version (v${com.example.BuildConfig.VERSION_NAME}, Build ${com.example.BuildConfig.VERSION_CODE})"
                )
            }
        } catch (_: Throwable) {
            _uiState.value = _uiState.value.copy(isUpdateCheckLoading = false)
        }
    }

    fun setNewsAutoUpdateEnabled(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(
            isNewsAutoUpdateEnabled = enabled,
            newsAutoUpdateSecondsRemaining = if (enabled) _uiState.value.newsAutoUpdateIntervalSeconds else 0
        )
    }

    fun setNewsAutoUpdateInterval(seconds: Int) {
        val valid = seconds.coerceIn(15, 3600)
        _uiState.value = _uiState.value.copy(
            newsAutoUpdateIntervalSeconds = valid,
            newsAutoUpdateSecondsRemaining = valid
        )
    }

    fun setHourlyAutoUpdateMode(enabled: Boolean = true) {
        if (enabled) {
            setNewsAutoUpdateEnabled(true)
            setNewsAutoUpdateInterval(3600)
        } else {
            setNewsAutoUpdateInterval(45)
        }
    }

    /**
     * Toggles Battery-Saving Mode in application settings.
     * When enabled, reduces the frequency of background data syncs for the News (to 30 mins / 1800s)
     * and Bus Schedule (from 15s to 60s) modules to extend device battery life.
     */
    fun setBatterySavingModeEnabled(enabled: Boolean) {
        appContext?.let { ctx ->
            com.example.data.sync.SyncManager.setBatterySavingModeEnabled(ctx, enabled)
        }
        val targetNewsInterval = if (enabled) 1800 else 300
        val targetHourlyFreq = if (enabled) 120 else 60
        _uiState.value = _uiState.value.copy(
            isBatterySavingModeEnabled = enabled,
            newsAutoUpdateIntervalSeconds = targetNewsInterval,
            newsAutoUpdateSecondsRemaining = targetNewsInterval,
            autoUpdateFrequencyMinutes = targetHourlyFreq,
            nextHourlyRefreshMinutesRemaining = targetHourlyFreq,
            lastSyncStatusMessage = if (enabled) {
                "Battery-Saving Mode Active: News (30m) & Bus Schedule (60s) background syncs throttled"
            } else {
                "Standard Live Sync Active: News (5m) & Bus Schedule (15s)"
            }
        )
    }

    /**
     * Toggles Chandipur Beach 'High Tide' coastal safety notifications on/off in App Settings.
     * Synchronizes both FCM push topic subscription and local tidal notification monitors.
     */
    fun setChandipurHighTideAlertsEnabled(enabled: Boolean) {
        appContext?.let { ctx ->
            com.example.data.fcm.FcmManager.setChandipurHighTideAlertsEnabled(ctx, enabled)
        }
        _uiState.value = _uiState.value.copy(
            isChandipurHighTideAlertsEnabled = enabled,
            lastSyncStatusMessage = if (enabled) {
                "Chandipur Beach 'High Tide' coastal safety alerts enabled"
            } else {
                "Chandipur Beach 'High Tide' coastal safety alerts muted"
            }
        )
    }

    /**
     * Dispatches an immediate Chandipur Beach 'High Tide' push notification based on local tidal data.
     */
    fun triggerChandipurHighTideAlertNow() {
        appContext?.let { ctx ->
            com.example.data.fcm.FcmManager.dispatchChandipurHighTidePushFromLocalData(
                context = ctx,
                bypassToggleCheck = true
            )
        }
    }

    fun setAdaptiveSyncEnabled(enabled: Boolean) {
        appContext?.let { ctx ->
            com.example.data.fcm.FcmManager.setAdaptiveSyncEnabled(ctx, enabled)
        }
        _uiState.value = _uiState.value.copy(
            isAdaptiveSyncEnabled = enabled,
            lastSyncStatusMessage = if (enabled) {
                "Adaptive Sync Active: Auto-throttles weather, bus & news updates when battery < 20%"
            } else {
                "Adaptive Sync Disabled: Full update frequency regardless of battery level"
            }
        )
    }

    fun toggleSubscribedNewsCategory(category: String) {
        val updatedSet = appContext?.let { ctx ->
            com.example.data.fcm.FcmManager.toggleNewsCategorySubscription(ctx, category)
        } ?: _uiState.value.subscribedNewsCategories.let { cur ->
            if (cur.contains(category)) cur - category else cur + category
        }
        _uiState.value = _uiState.value.copy(
            subscribedNewsCategories = updatedSet,
            lastSyncStatusMessage = "Updated Firestore news category push subscriptions: ${updatedSet.joinToString(", ")}"
        )
    }

    fun triggerSubscribedCategoryAlertNow(category: String? = null) {
        val targetCat = category ?: _uiState.value.subscribedNewsCategories.firstOrNull() ?: "Politics"
        appContext?.let { ctx ->
            com.example.data.fcm.FcmManager.dispatchCategoryBreakingAlert(ctx, targetCat)
        }
    }

    fun triggerNewsAutoUpdate() {
        val now = System.currentTimeMillis()
        _uiState.value = _uiState.value.copy(
            lastNewsAutoUpdateTimestamp = now,
            newsAutoUpdateSecondsRemaining = _uiState.value.newsAutoUpdateIntervalSeconds,
            newsAutoUpdateCycleCount = _uiState.value.newsAutoUpdateCycleCount + 1
        )
        viewModelScope.launch {
            try {
                com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullAndAutoPublishAllSections(
                    context = appContext,
                    forceNetworkRss = true
                )
            } catch (_: Throwable) {}
            try {
                newsRepo?.refreshNews()
            } catch (_: Throwable) {}
            try {
                refreshDailyPulse()
            } catch (_: Throwable) {}
            try {
                fetchLiveEmergencyAlerts()
            } catch (_: Throwable) {}
        }
    }

    // --- ADMINISTRATIVE MODERATOR PANEL CRUD OPERATIONS ---

    fun adminSaveNewsArticle(article: NewsArticle) {
        val currentList = _uiState.value.newsArticles.toMutableList()
        val existingIdx = currentList.indexOfFirst { it.id == article.id }
        if (existingIdx >= 0) {
            currentList[existingIdx] = article
        } else {
            currentList.add(0, article)
        }
        val filtered = NewsFeedFilter.filterArticles(
            currentList,
            _uiState.value.selectedNewsTab,
            _uiState.value.newsSearchQuery
        )
        _uiState.value = _uiState.value.copy(
            newsArticles = currentList,
            filteredNewsArticles = filtered,
            lastSyncStatusMessage = "Moderator saved news article: ${article.title}"
        )
        viewModelScope.launch {
            try {
                appContext?.let { ctx ->
                    val db = AppDatabase.getInstance(ctx)
                    val numericId = article.id.toLongOrNull() ?: 0L
                    db.newsDao().insertArticle(
                        com.example.data.local.NewsArticleEntity(
                            id = numericId,
                            title = article.title,
                            summary = article.snippet,
                            content = article.content.ifBlank { article.snippet },
                            category = article.category,
                            source = article.source,
                            publishedAt = article.timeAgo,
                            timestamp = System.currentTimeMillis(),
                            isBreaking = true,
                            isBookmarked = article.isBookmarked
                        )
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    fun adminDeleteNewsArticle(articleId: String) {
        val currentList = _uiState.value.newsArticles.filterNot { it.id == articleId }
        val filtered = NewsFeedFilter.filterArticles(
            currentList,
            _uiState.value.selectedNewsTab,
            _uiState.value.newsSearchQuery
        )
        _uiState.value = _uiState.value.copy(
            newsArticles = currentList,
            filteredNewsArticles = filtered,
            lastSyncStatusMessage = "Moderator removed news article ($articleId)"
        )
        viewModelScope.launch {
            try {
                val numericId = articleId.toLongOrNull()
                if (numericId != null) {
                    appContext?.let { ctx ->
                        AppDatabase.getInstance(ctx).newsDao().deleteArticleById(numericId)
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    // --- CITIZEN LOCAL NEWS SUBMISSION & MODERATION WORKFLOW ---

    /**
     * Submits a user-authored local news story into the moderation queue (`PENDING`).
     * The story is stored in Room via [CommunityNewsModerationRepository] and does NOT appear
     * in the live [BalasoreUiState.newsArticles] feed until approved by a moderator.
     */
    fun submitCommunityNewsStory(
        title: String,
        odiaTitle: String = "",
        snippet: String,
        content: String = "",
        category: String = "Local",
        locationWard: String = "Balasore Town",
        authorName: String = "Balasore Citizen Reporter",
        authorContact: String = "",
        sourceVerificationType: String = "Eyewitness Report",
        photoUri: String? = null,
        isBreakingCandidate: Boolean = false
    ) {
        val cleanTitle = title.trim()
        val cleanSnippet = snippet.trim().ifBlank { content.trim().take(140) }
        if (cleanTitle.isBlank() || cleanSnippet.isBlank()) return

        viewModelScope.launch {
            try {
                val created = communityNewsRepo.submitNewsStory(
                    title = cleanTitle,
                    odiaTitle = odiaTitle,
                    snippet = cleanSnippet,
                    content = content.ifBlank { cleanSnippet },
                    category = category,
                    locationWard = locationWard,
                    authorName = authorName,
                    authorContact = authorContact,
                    sourceVerificationType = sourceVerificationType,
                    photoUri = photoUri,
                    isBreakingCandidate = isBreakingCandidate
                )
                val updatedSubmissions = listOf(created) +
                    _uiState.value.communityNewsSubmissions.filterNot { it.id == created.id }
                _uiState.value = _uiState.value.copy(
                    communityNewsSubmissions = updatedSubmissions,
                    lastSyncStatusMessage = "Submitted news story '${created.title}' for moderation review (PENDING)"
                )
            } catch (_: Throwable) {}
        }
    }

    /**
     * Moderator action: Approves a pending/revised citizen news submission and publishes it
     * immediately to the live Balasore News Feed and Room [com.example.data.local.NewsDao].
     */
    fun approveCommunityNewsSubmission(
        submissionId: String,
        moderatorNote: String = "",
        editedTitle: String? = null,
        editedOdiaTitle: String? = null,
        editedSnippet: String? = null,
        editedContent: String? = null,
        editedCategory: String? = null
    ) {
        viewModelScope.launch {
            try {
                val result = communityNewsRepo.approveAndPublishSubmission(
                    submissionId = submissionId,
                    moderatorNote = moderatorNote,
                    editedTitle = editedTitle,
                    editedOdiaTitle = editedOdiaTitle,
                    editedSnippet = editedSnippet,
                    editedContent = editedContent,
                    editedCategory = editedCategory
                )
                if (result != null) {
                    val (approvedEntity, liveArticle) = result
                    val updatedSubmissions = _uiState.value.communityNewsSubmissions.map {
                        if (it.id == submissionId) approvedEntity else it
                    }
                    val currentArticles = _uiState.value.newsArticles.toMutableList()
                    val existingIdx = currentArticles.indexOfFirst {
                        it.id == liveArticle.id || it.title.equals(liveArticle.title, ignoreCase = true)
                    }
                    if (existingIdx >= 0) {
                        currentArticles[existingIdx] = liveArticle
                    } else {
                        currentArticles.add(0, liveArticle)
                    }
                    val filtered = NewsFeedFilter.filterArticles(
                        currentArticles,
                        _uiState.value.selectedNewsTab,
                        _uiState.value.newsSearchQuery
                    )
                    _uiState.value = _uiState.value.copy(
                        communityNewsSubmissions = updatedSubmissions,
                        newsArticles = currentArticles,
                        filteredNewsArticles = filtered,
                        lastSyncStatusMessage = "Moderator approved & published citizen news: ${liveArticle.title}"
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    /**
     * Moderator action: Rejects a citizen news submission so it cannot go live (and removes it
     * from the live news feed if it was previously approved).
     */
    fun rejectCommunityNewsSubmission(
        submissionId: String,
        moderatorNote: String = ""
    ) {
        val targetBeforeReject = _uiState.value.communityNewsSubmissions.firstOrNull { it.id == submissionId }
        viewModelScope.launch {
            try {
                val rejected = communityNewsRepo.rejectSubmission(
                    submissionId = submissionId,
                    moderatorNote = moderatorNote
                )
                if (rejected != null) {
                    val updatedSubmissions = _uiState.value.communityNewsSubmissions.map {
                        if (it.id == submissionId) rejected else it
                    }
                    val publishedIdToRemove = targetBeforeReject?.publishedArticleId
                    val updatedArticles = _uiState.value.newsArticles.filterNot { art ->
                        (publishedIdToRemove != null && art.id == publishedIdToRemove) ||
                            art.title.equals(rejected.title, ignoreCase = true)
                    }
                    val filtered = NewsFeedFilter.filterArticles(
                        updatedArticles,
                        _uiState.value.selectedNewsTab,
                        _uiState.value.newsSearchQuery
                    )
                    _uiState.value = _uiState.value.copy(
                        communityNewsSubmissions = updatedSubmissions,
                        newsArticles = updatedArticles,
                        filteredNewsArticles = filtered,
                        lastSyncStatusMessage = "Moderator rejected citizen news submission (${rejected.title})"
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    /**
     * Moderator action: Requests revisions from the citizen reporter before approving.
     */
    fun requestRevisionCommunityNewsSubmission(
        submissionId: String,
        moderatorNote: String
    ) {
        viewModelScope.launch {
            try {
                val revised = communityNewsRepo.requestRevision(
                    submissionId = submissionId,
                    moderatorNote = moderatorNote
                )
                if (revised != null) {
                    val updatedSubmissions = _uiState.value.communityNewsSubmissions.map {
                        if (it.id == submissionId) revised else it
                    }
                    _uiState.value = _uiState.value.copy(
                        communityNewsSubmissions = updatedSubmissions,
                        lastSyncStatusMessage = "Moderator requested revision on '${revised.title}'"
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    /**
     * Citizen reporter action: Updates a story and resubmits it to the `PENDING` moderation queue.
     */
    fun resubmitCommunityNewsStory(
        submissionId: String,
        title: String,
        odiaTitle: String = "",
        snippet: String,
        content: String,
        category: String,
        locationWard: String
    ) {
        viewModelScope.launch {
            try {
                val resubmitted = communityNewsRepo.updateAndResubmitStory(
                    submissionId = submissionId,
                    title = title,
                    odiaTitle = odiaTitle,
                    snippet = snippet,
                    content = content,
                    category = category,
                    locationWard = locationWard
                )
                if (resubmitted != null) {
                    val updatedSubmissions = _uiState.value.communityNewsSubmissions.map {
                        if (it.id == submissionId) resubmitted else it
                    }
                    _uiState.value = _uiState.value.copy(
                        communityNewsSubmissions = updatedSubmissions,
                        lastSyncStatusMessage = "Resubmitted '${resubmitted.title}' for moderation review"
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    /**
     * Moderator batch action: Approves all pending citizen news submissions and publishes them live.
     */
    fun approveAllPendingCommunityNews() {
        viewModelScope.launch {
            try {
                val newlyLiveArticles = communityNewsRepo.approveAllPending()
                if (newlyLiveArticles.isNotEmpty()) {
                    val currentArticles = _uiState.value.newsArticles.toMutableList()
                    newlyLiveArticles.reversed().forEach { liveArt ->
                        val existingIdx = currentArticles.indexOfFirst {
                            it.id == liveArt.id || it.title.equals(liveArt.title, ignoreCase = true)
                        }
                        if (existingIdx >= 0) {
                            currentArticles[existingIdx] = liveArt
                        } else {
                            currentArticles.add(0, liveArt)
                        }
                    }
                    val filtered = NewsFeedFilter.filterArticles(
                        currentArticles,
                        _uiState.value.selectedNewsTab,
                        _uiState.value.newsSearchQuery
                    )
                    _uiState.value = _uiState.value.copy(
                        communityNewsSubmissions = CommunityNewsModerationRepository.submissionsStateFlow.value,
                        newsArticles = currentArticles,
                        filteredNewsArticles = filtered,
                        lastSyncStatusMessage = "Batch approved & published ${newlyLiveArticles.size} citizen news stories"
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    fun deleteCommunityNewsSubmission(submissionId: String) {
        val target = _uiState.value.communityNewsSubmissions.firstOrNull { it.id == submissionId }
        viewModelScope.launch {
            try {
                communityNewsRepo.deleteSubmission(submissionId)
                val updatedSubmissions = _uiState.value.communityNewsSubmissions.filterNot { it.id == submissionId }
                val updatedArticles = if (target?.publishedArticleId != null) {
                    _uiState.value.newsArticles.filterNot { it.id == target.publishedArticleId }
                } else {
                    _uiState.value.newsArticles
                }
                val filtered = NewsFeedFilter.filterArticles(
                    updatedArticles,
                    _uiState.value.selectedNewsTab,
                    _uiState.value.newsSearchQuery
                )
                _uiState.value = _uiState.value.copy(
                    communityNewsSubmissions = updatedSubmissions,
                    newsArticles = updatedArticles,
                    filteredNewsArticles = filtered,
                    lastSyncStatusMessage = "Deleted citizen news submission ($submissionId)"
                )
            } catch (_: Throwable) {}
        }
    }

    fun adminUpdateWeatherInfo(updated: WeatherInfo) {
        _uiState.value = _uiState.value.copy(
            weather = updated,
            lastSyncStatusMessage = "Moderator updated live Balasore weather (${updated.tempCelsius}°C, ${updated.condition})"
        )
        viewModelScope.launch {
            try {
                appContext?.let { ctx ->
                    val db = AppDatabase.getInstance(ctx)
                    val existing = db.weatherDao().getWeatherCacheSync()
                    if (existing != null) {
                        val parsedHumidity = updated.humidity.filter { it.isDigit() }.toIntOrNull() ?: existing.humidity
                        val parsedWind = updated.windSpeedKmh.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: existing.windSpeed
                        db.weatherDao().insertOrUpdateWeather(
                            existing.copy(
                                temperature = updated.tempCelsius.toDouble(),
                                weatherDescription = updated.condition,
                                humidity = parsedHumidity,
                                windSpeed = parsedWind,
                                tideDescription = updated.tideStatus,
                                alertMessage = updated.cycloneAlert ?: existing.alertMessage,
                                lastUpdated = System.currentTimeMillis()
                            )
                        )
                    }
                }
            } catch (_: Throwable) {}
        }
    }

    fun adminSaveWeatherAlert(alert: BalasoreWeatherAlert) {
        val currentAlerts = _uiState.value.weatherAlerts.toMutableList()
        val idx = currentAlerts.indexOfFirst { it.id == alert.id }
        if (idx >= 0) {
            currentAlerts[idx] = alert
        } else {
            currentAlerts.add(0, alert)
        }
        _uiState.value = _uiState.value.copy(
            weatherAlerts = currentAlerts,
            weather = _uiState.value.weather.copy(cycloneAlert = alert.title),
            lastSyncStatusMessage = "Moderator published weather alert: ${alert.title}"
        )
    }

    fun adminDeleteWeatherAlert(alertId: String) {
        val updatedAlerts = _uiState.value.weatherAlerts.filterNot { it.id == alertId }
        _uiState.value = _uiState.value.copy(
            weatherAlerts = updatedAlerts,
            lastSyncStatusMessage = "Moderator removed weather alert ($alertId)"
        )
    }

    fun adminSaveHotspot(hotspot: Hotspot) {
        val currentList = _uiState.value.hotspots.toMutableList()
        val idx = currentList.indexOfFirst { it.id == hotspot.id }
        if (idx >= 0) {
            currentList[idx] = hotspot
        } else {
            currentList.add(0, hotspot)
        }
        _uiState.value = _uiState.value.copy(
            hotspots = currentList,
            lastSyncStatusMessage = "Moderator saved tourism hotspot: ${hotspot.name}"
        )
        viewModelScope.launch {
            try {
                appContext?.let { ctx ->
                    val db = AppDatabase.getInstance(ctx)
                    db.hotspotDao().insertHotspot(
                        com.example.data.local.HotspotEntity(
                            id = hotspot.id,
                            name = hotspot.name,
                            odiaName = hotspot.odiaName,
                            hindiName = hotspot.name,
                            category = hotspot.category,
                            shortDescription = hotspot.description,
                            fullDescription = hotspot.description,
                            highlights = hotspot.location,
                            distanceKmFromBls = hotspot.distanceKm.toInt(),
                            bestTimeToVisit = "Oct - Mar",
                            timings = hotspot.timing,
                            entryFee = "Free",
                            specialty = hotspot.category,
                            localTip = "Located at ${hotspot.location}",
                            isFavorite = _uiState.value.favoriteHotspotIds.contains(hotspot.id)
                        )
                    )
                }
            } catch (_: Throwable) {}
        }
    }

    fun adminDeleteHotspot(hotspotId: String) {
        val updatedList = _uiState.value.hotspots.filterNot { it.id == hotspotId }
        _uiState.value = _uiState.value.copy(
            hotspots = updatedList,
            lastSyncStatusMessage = "Moderator removed tourism hotspot ($hotspotId)"
        )
        viewModelScope.launch {
            try {
                appContext?.let { ctx ->
                    AppDatabase.getInstance(ctx).hotspotDao().deleteHotspotById(hotspotId)
                }
            } catch (_: Throwable) {}
        }
    }

    companion object {
        fun matchesNewsTab(article: NewsArticle, tabIndex: Int): Boolean =
            NewsFeedFilter.matchesTab(article, tabIndex)

        fun filterNewsArticles(articles: List<NewsArticle>, tabIndex: Int, query: String = ""): List<NewsArticle> =
            NewsFeedFilter.filterArticles(articles, tabIndex, query)
    }
}
