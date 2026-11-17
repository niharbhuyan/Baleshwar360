package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.daily.DailyBalasorePulse
import com.example.data.daily.DailyUpdateEngine
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.FirestoreService
import com.example.data.local.AppDatabase
import com.example.data.local.ChandipurTideEntity
import com.example.data.local.HotspotEntity
import com.example.data.local.ItineraryItemEntity
import com.example.ui.components.CityEssentialsRechartsWidget
import com.example.ui.components.CulturalInsightsRoomCard
import com.example.ui.components.EmergencySosDashboardBanner
import com.example.ui.components.HotspotOfflineHighResGalleryCarousel
import com.example.ui.components.BalasoreOpenWeatherDashboard
import com.example.ui.components.HotspotEmbeddedMap
import com.example.ui.components.LocalWeather
import com.example.ui.components.PersonalizedDragDropItineraryCard
import com.example.ui.components.PersonalizedRecommendationsCard
import com.example.data.recommendation.BalasoreRecommendationEngine
import com.example.ui.features.admin.ContentModerationRepository
import com.example.ui.features.admin.ReviewModerationBadgeAndFlagButton
import com.example.data.local.NewsArticleEntity
import com.example.data.local.ReviewEntity
import com.example.data.local.UserEntity
import com.example.data.model.AppLanguage
import com.example.data.model.BalasoreForecastDay
import com.example.data.model.BalasoreForecastHour
import com.example.data.model.BalasoreWeatherAlert
import com.example.data.model.DailyBalasoreIdiom
import com.example.data.model.DrdoAdvisory
import com.example.data.model.FavoriteNewsCloudItem
import com.example.data.model.FeedbackReport
import com.example.data.model.Hotspot
import com.example.data.model.LiteraryTrailPoint
import com.example.data.model.NewsArticle
import com.example.data.model.TempleRitualInfo
import com.example.data.model.TidalClockData
import com.example.data.model.TourismBookmarkCloudItem
import com.example.data.model.TourismReviewCloudEntity
import com.example.data.model.UserWeatherPreferences
import com.example.data.model.WeatherInfo
import com.example.data.remote.EmergencyAlertDto
import com.example.data.remote.OpenWeatherCurrentResponse
import com.example.data.remote.OpenWeatherDailyForecast
import com.example.data.remote.OpenWeatherThreeDayForecast
import com.example.util.BalasoreHapticHelper
import com.example.data.repository.BalasoreRepository
import com.example.ui.components.BALASORE_TOURIST_LOCATIONS
import com.example.ui.components.BalasoreExploreMapSection
import com.example.ui.components.BalasoreOpenWeatherSummaryWidget
import com.example.ui.components.BalasoreSevenDayWeatherTrendsRechartsCard
import com.example.ui.components.BaleshwarEventCalendarCard
import com.example.ui.components.ChandipurTidalAnalyticsDashboard
import com.example.ui.components.CoastalWellnessAndCommunityPulseCard
import com.example.ui.components.FeedbackFormCard
import com.example.ui.components.HotspotSwipeablePhotoGallery
import com.example.ui.components.OfflineModeStorageDashboardCard
import com.example.ui.components.OpenWeatherMapDashboardCardComponent
import com.example.ui.components.SAMPLE_BALASORE_BUS_ARRIVALS
import com.example.ui.components.SafeWalkTideCountdownAndProactiveAlertCard
import com.example.ui.components.VoiceSearchControls
import com.example.ui.components.VoiceSearchLanguage
import com.example.ui.components.VoiceSearchSection
import com.example.ui.components.matchesBilingualSearchQuery
import com.example.ui.features.coastal.DailyCoastalCleanupTrackerCard
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoBlueLight
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.ui.viewmodel.UniqueFeatureSheetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TourismScreen(
    hotspots: List<Hotspot> = BalasoreRepository.hotspots,
    templeRitualInfo: TempleRitualInfo = BalasoreRepository.templeRitualInfo,
    literaryTrailPoints: List<LiteraryTrailPoint> = BalasoreRepository.literaryTrailPoints,
    dailyIdiom: DailyBalasoreIdiom = BalasoreRepository.dailyIdiom,
    selectedCategory: String = "All",
    searchQuery: String = "",
    language: AppLanguage = com.example.ui.components.LocalAppLanguage.current.currentLanguage,
    favoriteIds: Set<String> = emptySet(),
    weather: WeatherInfo = BalasoreRepository.weather,
    forecastDays: List<BalasoreForecastDay> = BalasoreRepository.forecast3Days,
    weatherAlerts: List<BalasoreWeatherAlert> = emptyList(),
    isFahrenheit: Boolean = false,
    selectedForecastIndex: Int = 0,
    onCategorySelected: (String) -> Unit = {},
    onSearchChanged: (String) -> Unit = {},
    onToggleFavorite: (String) -> Unit = {},
    onToggleTempUnit: () -> Unit = {},
    onSelectForecastDay: (Int) -> Unit = {},
    onAcknowledgeAlert: (String) -> Unit = {},
    onNavigateToWeather: () -> Unit = {},
    onNavigateToSpecialFeatures: () -> Unit = {},
    onOpenFeatureSheet: (UniqueFeatureSheetType) -> Unit = {},
    itineraryItems: List<ItineraryItemEntity> = emptyList(),
    liveEmergencyAlerts: List<EmergencyAlertDto> = emptyList(),
    dismissedAlertIds: Set<String> = emptySet(),
    onDismissAlert: (String) -> Unit = {},
    onAddToItinerary: (Hotspot, Int, String, String) -> Unit = { _, _, _, _ -> },
    onRemoveFromItinerary: (Long) -> Unit = {},
    onClearItinerary: () -> Unit = {},
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    onRefreshDailyPulse: () -> Unit = {},
    onRefreshWeather: () -> Unit = {},
    openWeatherCurrent: OpenWeatherCurrentResponse? = null,
    openWeatherThreeDayForecast: List<OpenWeatherThreeDayForecast> = emptyList(),
    openWeatherSevenDayForecast: List<OpenWeatherDailyForecast> = emptyList(),
    isOpenWeatherLoading: Boolean = false,
    onRefreshOpenWeather: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestoreService = remember { FirestoreService(context) }
    val authManager = remember { FirebaseAuthManager(context) }
    val reviewDao = remember { AppDatabase.getInstance(context).reviewDao() }

    val activeSessionUser by authManager.activeSessionUser.collectAsStateWithLifecycle()
    val currentUser by authManager.currentUser.collectAsStateWithLifecycle()
    val activeUid = currentUser?.uid ?: activeSessionUser?.id
    val activeUserName = activeSessionUser?.fullName ?: currentUser?.displayName ?: "Balasore Explorer"

    var visitedSpotIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var ratingDialogSpot by remember { mutableStateOf<Hotspot?>(null) }
    var selectedDetailHotspot by remember { mutableStateOf<Hotspot?>(null) }

    val allStoredReviews by reviewDao.getAllReviews().collectAsStateWithLifecycle(initialValue = emptyList())
    val isOdia = language == AppLanguage.ODIA || com.example.ui.components.LocalAppLanguage.current.isOdia

    var localSearchQuery by remember(searchQuery) { mutableStateOf(searchQuery) }
    var voiceSearchLanguage by remember(isOdia) {
        mutableStateOf(if (isOdia) VoiceSearchLanguage.ODIA else VoiceSearchLanguage.ENGLISH)
    }

    val osmPreCacheStats by com.example.data.maps.BalasoreOfflineTileCacheManager.preCacheStats.collectAsStateWithLifecycle()
    val downloadedHotspotIds by com.example.ui.components.BalasoreOfflineAccessManager.downloadedHotspotIdsFlow.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        com.example.ui.components.BalasoreOfflineAccessManager.ensureInitialized(context)
        val loadedVisited = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            firestoreService.getVisitedSpotIds()
        }
        visitedSpotIds = loadedVisited
        kotlinx.coroutines.delay(1800L)
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            com.example.data.maps.BalasoreOfflineTileCacheManager.preWarmBalasoreCoastalTiles(context.applicationContext ?: context)
        }
    }

    val filteredHotspots = remember(hotspots, selectedCategory, localSearchQuery) {
        hotspots.filter { spot ->
            val categoryMatch = selectedCategory.equals("All", ignoreCase = true) ||
                selectedCategory.isBlank() ||
                spot.category.contains(selectedCategory, ignoreCase = true)
            val searchMatch = matchesBilingualSearchQuery(
                localSearchQuery,
                spot.name,
                spot.odiaName,
                spot.category,
                spot.location,
                spot.description
            )
            categoryMatch && searchMatch
        }
    }

    var isDashboardPullRefreshing by remember { mutableStateOf(false) }
    val effectiveDashboardRefreshing = isOpenWeatherLoading || isDashboardPullRefreshing
    val dashboardPullRefreshState = rememberPullToRefreshState()
    val triggerDashboardPullRefresh: () -> Unit = {
        if (!isDashboardPullRefreshing) {
            isDashboardPullRefreshing = true
            onRefreshOpenWeather()
            onRefreshWeather()
            onRefreshDailyPulse()
            coroutineScope.launch {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    runCatching {
                        com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullAndAutoPublishAllSections(
                            context = context,
                            forceNetworkRss = true
                        )
                    }
                    try {
                        com.example.ui.components.BalasoreCommunityForumRepository.syncPendingForumPostsWithCloud(context)
                    } catch (_: Throwable) {}
                    try {
                        com.example.data.sync.SyncManager.triggerImmediateEssentialSync(context)
                    } catch (_: Throwable) {}
                }
                kotlinx.coroutines.delay(650L)
                isDashboardPullRefreshing = false
                BalasoreHapticHelper.vibrateSuccess(context)
                Toast.makeText(
                    context,
                    if (isOdia) "✓ ସଦ୍ୟତମ ଖବର, ପାଣିପାଗ ଓ କମ୍ୟୁନିଟି ତଥ୍ୟ ଅପଡେଟ୍ ହୋଇଛି"
                    else "✓ Pulled latest news, weather & community updates from network",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = effectiveDashboardRefreshing,
        onRefresh = triggerDashboardPullRefresh,
        state = dashboardPullRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = dashboardPullRefreshState,
                isRefreshing = effectiveDashboardRefreshing,
                containerColor = MaterialTheme.colorScheme.surface,
                color = OceanBlue,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .testTag("home_dashboard_pull_to_refresh_indicator")
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("home_dashboard_pull_to_refresh_box")
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("tourism_main_lazy_column"),
        contentPadding = PaddingValues(bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hero Banner with Quick Map, Event Calendar, Offline Mode, Auth & Weather Actions
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱର ୩୬୦ ପର୍ଯ୍ୟଟନ ଓ ଐତିହ୍ୟ" else "Explore Baleshwar 360 Hotspots",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                            Text(
                                text = "${weather.tempCelsius}°C • ${weather.condition} • ${weather.tideStatus}",
                                fontSize = 12.sp,
                                color = BentoSlate700
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFF0FDF4),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Text(
                                text = "✓ ${visitedSpotIds.size}/${hotspots.size} Visited",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.OSM_OFFLINE_TOURISM_MAP) },
                            modifier = Modifier.testTag("open_osm_map_sheet_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Map, contentDescription = "Open Interactive Map", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isOdia) "ଇଣ୍ଟରାକ୍ଟିଭ୍ ମ୍ୟାପ୍" else "Full Screen Map", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFeatureSheet(UniqueFeatureSheetType.AR_LANDMARK_CAMERA)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0369A1)),
                            modifier = Modifier.testTag("hero_open_ar_landmark_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isOdia) "📷 AR ଐତିହ୍ୟ" else "📷 AR Landmarks", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFeatureSheet(UniqueFeatureSheetType.ODIA_FESTIVALS)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                            modifier = Modifier.testTag("hero_open_event_calendar_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isOdia) "🗓️ ପର୍ବ କ୍ୟାଲେଣ୍ଡର" else "🗓️ Event Calendar", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            modifier = Modifier.testTag("hero_open_civic_eye_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isOdia) "📸 ସିଭିକ୍ ଆଇ (ଫଟୋ ରିପୋର୍ଟ)" else "📸 Civic Eye (Report Issue)", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFeatureSheet(UniqueFeatureSheetType.OFFICIAL_TOURISM_EVENT_POSTER_SCANNER)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                            modifier = Modifier.testTag("hero_open_qr_scanner_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isOdia) "📷 QR ସ୍କାନର (ML Kit)" else "📷 Scan Event/Venue QR", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFeatureSheet(UniqueFeatureSheetType.OFFLINE_MODE_MANAGER)
                            },
                            modifier = Modifier.testTag("hero_open_offline_manager_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isOdia) "⚡ ଅଫଲାଇନ୍ ମୋଡ୍" else "⚡ Offline Mode", fontSize = 12.sp)
                        }
                        Button(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFeatureSheet(UniqueFeatureSheetType.COMMUNITY_FORUM)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
                            modifier = Modifier.testTag("hero_open_community_forum_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isOdia) "💬 କମ୍ୟୁନିଟି ଫୋରମ୍" else "💬 Community Forum", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFeatureSheet(UniqueFeatureSheetType.USER_PROFILE_AUTH)
                            },
                            modifier = Modifier.testTag("open_user_profile_hub_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = if (activeUid != null) "👤 $activeUserName" else (if (isOdia) "ଲଗ୍-ଇନ୍ / ପ୍ରୋଫାଇଲ୍" else "Login / Profile"),
                                fontSize = 12.sp
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFeatureSheet(UniqueFeatureSheetType.MY_CIVIC_REPORTS)
                            },
                            modifier = Modifier.testTag("hero_open_my_civic_reports_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isOdia) "📸 ସିଭିକ୍ ରିପୋର୍ଟ ଓ ଗ୍ୟାଲେରୀ" else "📸 Civic Reports & Gallery", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFeatureSheet(UniqueFeatureSheetType.PUSH_NOTIFICATION_CENTER)
                            },
                            modifier = Modifier.testTag("hero_open_push_notifications_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isOdia) "🔔 ପୁଶ୍ ଆଲର୍ଟ" else "🔔 Push Alerts", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFeatureSheet(UniqueFeatureSheetType.ADMIN_MODERATOR_PANEL)
                            },
                            modifier = Modifier.testTag("hero_open_admin_panel_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isOdia) "🛡️ ଆଡମିନ୍ ପ୍ୟାନେଲ୍" else "🛡️ Admin Panel", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = {
                                val playStoreUrl = "https://play.google.com/store/apps/details?id=com.niharsales.baleshwar360"
                                val shareBody = "Explore Baleshwar 360 — Odisha's Smart Coastal, Tide, Weather, Transit & Heritage Companion! Download on Google Play: $playStoreUrl"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, "Share Baleshwar 360")
                                    putExtra(Intent.EXTRA_TEXT, shareBody)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share Baleshwar 360"))
                            },
                            modifier = Modifier.testTag("hero_share_baleshwar_app_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Baleshwar 360", modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isOdia) "Baleshwar 360 ସେୟାର୍" else "Share Baleshwar 360", fontSize = 12.sp)
                        }
                        OutlinedButton(
                            onClick = onNavigateToWeather,
                            modifier = Modifier.testTag("navigate_weather_tab_btn"),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(if (isOdia) "ଜୁଆର ଓ ପାଣିପାଗ" else "Tide & Weather", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // 1-Brief. Gemini API 'Daily News Brief' 3-Bullet Highlight Card on Main Dashboard
        item {
            var briefSummary by remember {
                mutableStateOf<String>(com.example.data.notification.DailyNewsDigestManager.getLastDigestSummary(context))
            }
            var isGeneratingBrief by remember { mutableStateOf(false) }

            LaunchedEffect(Unit) {
                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    val refreshed = com.example.data.notification.DailyNewsDigestManager.generateAndDispatchDigest(
                        context = context,
                        candidateArticles = BalasoreRepository.newsArticles,
                        postNotification = false
                    )
                    briefSummary = "${refreshed.summaryEnglish}\n\n🇮🇳 ଓଡ଼ିଆ ସାରାଂଶ:\n${refreshed.summaryOdia}"
                }
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_daily_news_brief_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, OceanBlue.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "✨ ଦૈନିକ ଖବର ସଂକ୍ଷିପ୍ତ (Daily News Brief • Gemini AI)" else "✨ Daily News Brief • 3-Bullet Balasore Highlight",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = OceanBlue
                            )
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱରର ଶୀର୍ଷ ୩ଟି ଟ୍ରେଣ୍ଡିଂ ଖବରର AI ସାରାଂଶ" else "Gemini AI 3-bullet summary of top trending news from Balasore",
                                fontSize = 11.5.sp,
                                color = BentoSlate600
                            )
                        }
                        OutlinedButton(
                            onClick = {
                                if (!isGeneratingBrief) {
                                    isGeneratingBrief = true
                                    coroutineScope.launch {
                                        val fresh = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                                            com.example.data.notification.DailyNewsDigestManager.generateAndDispatchDigest(
                                                context = context,
                                                candidateArticles = BalasoreRepository.newsArticles,
                                                postNotification = false
                                            )
                                        }
                                        briefSummary = "${fresh.summaryEnglish}\n\n🇮🇳 ଓଡ଼ିଆ ସାରାଂଶ:\n${fresh.summaryOdia}"
                                        isGeneratingBrief = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                            modifier = Modifier.testTag("home_daily_news_brief_refresh_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Daily News Brief", modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isGeneratingBrief) "..." else (if (isOdia) "ରିଫ୍ରେସ୍" else "Refresh AI"),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = OceanBlue.copy(alpha = 0.06f),
                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.16f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("home_daily_news_brief_bullets_box")
                    ) {
                        Text(
                            text = briefSummary,
                            fontSize = 12.5.sp,
                            lineHeight = 18.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            }
        }

        // 1-QR. Community QR Scanner (ML Kit) Banner Card for Official Tourism & Event Posters
        item {
            com.example.ui.components.CommunityQrScannerBannerCard(
                language = language,
                onOpenScanner = {
                    BalasoreHapticHelper.vibrateButtonClick(context)
                    onOpenFeatureSheet(UniqueFeatureSheetType.OFFICIAL_TOURISM_EVENT_POSTER_SCANNER)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 1-Rec. Personalized Recommendation Engine Card (Analyzes Viewed Articles, Saved Locations & Category Affinities)
        item {
            PersonalizedRecommendationsCard(
                language = language,
                externalSavedSpotIds = favoriteIds,
                externalVisitedSpotIds = visitedSpotIds,
                onOpenHotspot = { spot ->
                    BalasoreRecommendationEngine.recordLocationViewed(context, spot)
                    selectedDetailHotspot = spot
                },
                onToggleSaveHotspot = { spot ->
                    onToggleFavorite(spot.id)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 1A. Mini Weather Forecast Card, 7-Day OpenWeatherMap Forecast & 7-Day Temperature Trends Chart (Offline Room Cache)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BalasoreOpenWeatherSummaryWidget(
                    currentWeather = openWeatherCurrent,
                    isLoading = isOpenWeatherLoading,
                    language = language,
                    isFahrenheit = isFahrenheit,
                    onToggleTempUnit = onToggleTempUnit,
                    onRefresh = onRefreshOpenWeather,
                    onOpenDetails = onNavigateToWeather,
                    modifier = Modifier.testTag("home_mini_weather_warning_card")
                )

                com.example.ui.components.LocalWeatherDashboard(
                    language = language,
                    initialFahrenheit = isFahrenheit,
                    onToggleTempUnit = { onToggleTempUnit() },
                    onOpenFullForecast = onNavigateToWeather
                )

                OpenWeatherMapDashboardCardComponent(
                    currentWeather = openWeatherCurrent,
                    sevenDayForecast = openWeatherSevenDayForecast,
                    isLoading = isOpenWeatherLoading,
                    language = language,
                    isFahrenheit = isFahrenheit,
                    onToggleTempUnit = onToggleTempUnit,
                    onRefresh = onRefreshOpenWeather,
                    onOpenFullScreen = onNavigateToWeather,
                    onConfigureApiKey = onNavigateToWeather
                )

                BalasoreSevenDayWeatherTrendsRechartsCard(
                    language = language,
                    isFahrenheit = isFahrenheit
                )

                ChandipurTidalAnalyticsDashboard(
                    language = language
                )
            }
        }

        // 1A-Bus. Real-Time Bus Tracking Component (Balasore Bus Stand ↔ Remuna & Major Hubs ETA List View)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_realtime_bus_tracker_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.2.dp, OceanBlue.copy(alpha = 0.28f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "🚌 ଲାଇଭ୍ ବସ୍ ଟ୍ରାକିଂ (ବାଲେଶ୍ୱର ବସ୍ ଷ୍ଟାଣ୍ଡ ↔ ରେମୁଣା)" else "🚌 Real-Time Bus ETA (Bus Stand ↔ Remuna & Hubs)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = OceanBlue
                            )
                            Text(
                                text = if (isOdia) "ପ୍ରମୁଖ ରୁଟ୍ ଏବଂ ଆନୁମାନିକ ପହଞ୍ଚିବା ସମୟ (ETA)" else "Live public transport routes & estimated arrival times",
                                fontSize = 11.5.sp,
                                color = BentoSlate600
                            )
                        }
                        OutlinedButton(
                            onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.LOCAL_TRANSPORT) },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("open_full_bus_tracker_screen_btn")
                        ) {
                            Text(
                                text = if (isOdia) "ସମ୍ପୂର୍ଣ୍ଣ ତାଲିକା" else "Full Screen",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    SAMPLE_BALASORE_BUS_ARRIVALS.take(4).forEach { bus ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = OceanBlue.copy(alpha = 0.06f),
                            border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.18f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.LOCAL_TRANSPORT) }
                                .testTag("home_bus_eta_row_${bus.busId}")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${bus.routeNumber} • ${if (isOdia) bus.routeNameOd else bus.routeNameEn}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                    Text(
                                        text = "${bus.origin} ➔ ${bus.destination} • Next: ${bus.nextStop}",
                                        fontSize = 11.sp,
                                        color = BentoSlate700
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldGreen.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "ETA ${bus.arrivalMinutes} min",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 11.sp,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 1B. Auto-Updating Baleshwar Event & Festival Calendar Section
        item {
            BaleshwarEventCalendarCard(
                language = language,
                onOpenFullCalendar = { onOpenFeatureSheet(UniqueFeatureSheetType.ODIA_FESTIVALS) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 1C. Offline Mode, Intelligent Room Caching & Storage Footprint Dashboard Card
        item {
            OfflineModeStorageDashboardCard(
                hotspots = hotspots,
                language = language,
                onOpenOfflineManagerSheet = { onOpenFeatureSheet(UniqueFeatureSheetType.OFFLINE_MODE_MANAGER) },
                onOpenOfflineMapTilesSheet = { onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_OFFLINE_MAP_TILES) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 1D. Chandipur 'Safe Walk' Countdown Timer + Low Tide Proactive Alert + Compact Weather + Daily Odia Proverb
        item {
            SafeWalkTideCountdownAndProactiveAlertCard(
                weather = weather,
                dailyIdiom = dailyIdiom,
                language = language,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 1E. High-Visibility One-Tap 'Emergency SOS' Button (Immediate 112 Call + GPS Coordinates SMS)
        item {
            EmergencySosDashboardBanner(
                language = language,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 1F. High-Contrast 'City Essentials' Recharts Dashboard Widget (Weather, Bus Arrival, Tide Status & Safe Walking Countdown)
        item {
            CityEssentialsRechartsWidget(
                weather = weather,
                language = language,
                lastKnownTide = null,
                onOpenBusSchedule = { onOpenFeatureSheet(UniqueFeatureSheetType.LOCAL_TRANSPORT) },
                onOpenRiverMonitor = onNavigateToWeather,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 1G. 'Cultural Insights' Section (Panchalingeswar Daily Darshan Timings + Daily Odia Astrology & Trivia Cached in Room DB)
        item {
            CulturalInsightsRoomCard(
                language = language,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 1H. 'Personalized Tourism Itinerary' (Drag-and-Drop / Reorderable Schedule with Google Maps Travel Times)
        item {
            PersonalizedDragDropItineraryCard(
                allHotspots = hotspots,
                bookmarkedHotspotIds = favoriteIds,
                language = language,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 2. Interactive Google Maps & Vector Map Section with Clickable Hotspot Markers, Details, Navigation & Sharing
        item {
            BalasoreExploreMapSection(
                language = language,
                onOpenFullScreenMap = { onOpenFeatureSheet(UniqueFeatureSheetType.OSM_OFFLINE_TOURISM_MAP) }
            )
        }

        // 3. Daily Coastal Cleanup Habit Tracker with Recharts Monthly Bar Chart & 'Coastal Guardian' Badge
        item {
            DailyCoastalCleanupTrackerCard(
                language = language,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 3B. Coastal Wellness & Hydration Tracker + Community Pulse Poll (Recharts) + Coastal Fishing Trends (D3)
        item {
            CoastalWellnessAndCommunityPulseCard(
                language = language,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 3C. Offline Tourism Hotspots Download Banner & Balasore Community Forum Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                com.example.ui.components.BalasoreOfflineDownloadBannerCard(
                    mode = "TOURISM",
                    language = language,
                    hotspots = hotspots,
                    onOpenOfflineManager = {
                        onOpenFeatureSheet(UniqueFeatureSheetType.OFFLINE_MODE_MANAGER)
                    }
                )

                com.example.ui.components.BalasoreCommunityForumCard(
                    language = language,
                    initialCategory = "All",
                    onOpenFullForumSheet = {
                        onOpenFeatureSheet(UniqueFeatureSheetType.COMMUNITY_FORUM)
                    }
                )

                com.example.ui.components.BalasoreLocalBusTrackerBannerCard(
                    language = language,
                    onOpenFullTracker = {
                        onOpenFeatureSheet(UniqueFeatureSheetType.LOCAL_BUS_TRACKER)
                    }
                )
            }
        }

        // 4. Section Header & Bilingual Voice-to-Text Search Bar (English & Odia) for Tourism Hotspots
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOdia) "ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ (ଭ୍ରମଣ, ରେଟିଂ ଓ ସେୟାର୍)" else "Tourism Hotspots (Visit, Rate & Share)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "${filteredHotspots.size}/${hotspots.size} Spots • ${osmPreCacheStats.memoryCachedTiles} OSM Tiles Pre-Cached",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanBlue,
                        modifier = Modifier.testTag("tourism_osm_precache_badge")
                    )
                }

                // Voice-to-Text & Bilingual Search Input Field (English & Odia)
                OutlinedTextField(
                    value = localSearchQuery,
                    onValueChange = { updated ->
                        localSearchQuery = updated
                        onSearchChanged(updated)
                    },
                    placeholder = {
                        Text(
                            text = if (isOdia) {
                                "ଇଂରାଜୀ କିମ୍ବା ଓଡ଼ିଆରେ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ଖୋଜନ୍ତୁ (Speak or type)..."
                            } else {
                                "Search or speak in English / ଓଡ଼ିଆ (e.g., Chandipur, ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର)..."
                            },
                            fontSize = 12.5.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Tourism Hotspots",
                            tint = OceanBlue
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            if (localSearchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        localSearchQuery = ""
                                        onSearchChanged("")
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("tourism_clear_search_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear Tourism Search",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            VoiceSearchControls(
                                section = VoiceSearchSection.TOURISM,
                                isOdiaUi = isOdia,
                                selectedVoiceLang = voiceSearchLanguage,
                                onVoiceLangChanged = { voiceSearchLanguage = it },
                                onVoiceResult = { spokenText ->
                                    localSearchQuery = spokenText
                                    onSearchChanged(spokenText)
                                },
                                tagPrefix = "tourism"
                            )
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tourism_search_text_field")
                )

                // Category filter chips
                val categories = listOf("All", "Beach", "Temple", "Heritage", "Wildlife")
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { cat ->
                        val catLabel = if (isOdia) {
                            when (cat) {
                                "All" -> "ସମସ୍ତ (All)"
                                "Beach" -> "ବେଳାଭୂମି (Beach)"
                                "Temple" -> "ମନ୍ଦିର (Temple)"
                                "Heritage" -> "ଐତିହ୍ୟ (Heritage)"
                                "Wildlife" -> "ବନ୍ୟପ୍ରାଣୀ (Wildlife)"
                                else -> cat
                            }
                        } else {
                            cat
                        }
                        FilterChip(
                            selected = selectedCategory.equals(cat, ignoreCase = true) ||
                                (cat == "All" && selectedCategory.isBlank()),
                            onClick = { onCategorySelected(cat) },
                            label = {
                                Text(
                                    text = catLabel,
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            modifier = Modifier.testTag("tourism_category_chip_${cat.lowercase()}")
                        )
                    }
                }
            }
        }

        // 5. Hotspot Cards with Swipeable Image Gallery, Visited toggle, Rating & Review, Navigation, Bookmark, and Share
        items(filteredHotspots, key = { it.id }) { spot ->
            val isVisited = visitedSpotIds.contains(spot.id)
            val isFavorite = favoriteIds.contains(spot.id)
            val spotReviews = remember(allStoredReviews, spot.id) {
                allStoredReviews.filter { it.targetType == "HOTSPOT" && it.targetId == spot.id }
            }
            val displayRating = remember(spotReviews, spot.rating) {
                if (spotReviews.isNotEmpty()) {
                    val avg = spotReviews.map { it.rating }.average()
                    String.format(Locale.US, "%.1f (%d)", avg, spotReviews.size)
                } else {
                    String.format(Locale.US, "%.1f", spot.rating)
                }
            }

            val mappedLocation = remember(spot.id, spot.name) {
                BALASORE_TOURIST_LOCATIONS.firstOrNull {
                    it.id == spot.id || it.nameEn.contains(spot.name.take(8), ignoreCase = true)
                }
            }
            val spotLat = if (spot.latitude != 21.4934 || spot.id == "fakir_mohan_smruti") spot.latitude else (mappedLocation?.latitude ?: 21.4934)
            val spotLng = if (spot.longitude != 86.9135 || spot.id == "fakir_mohan_smruti") spot.longitude else (mappedLocation?.longitude ?: 86.9135)
            var isExpandedCard by remember(spot.id) { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { selectedDetailHotspot = spot }
                    .testTag("tourism_hotspot_${spot.id}"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (isVisited) Color(0xFF86EFAC) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Swipeable Multi-Photo Gallery for each Tourism Hotspot
                    HotspotSwipeablePhotoGallery(
                        spot = spot,
                        isOdia = isOdia,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isOdia) spot.odiaName else spot.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                if (isVisited) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFDCFCE7)
                                    ) {
                                        Text(
                                            text = if (isOdia) "✓ ଭ୍ରମଣ ହୋଇଛି" else "✓ Visited",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF166534),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // User Ratings & Category Row
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = AmberGold.copy(alpha = 0.15f)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "★★★★★",
                                            fontSize = 10.sp,
                                            color = AmberGold,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "$displayRating (${spot.reviewCount + spotReviews.size} reviews)",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoSlate800
                                        )
                                    }
                                }
                                Text(
                                    text = "• ${spot.category} • ${spot.distanceKm} km",
                                    fontSize = 11.5.sp,
                                    color = OceanBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // Operating Hours Badge
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BentoBlueLight.copy(alpha = 0.65f)
                            ) {
                                Text(
                                    text = "🕒 ${if (isOdia) "ଖୋଲିବା ସମୟ" else "Operating Hours"}: ${spot.effectiveOperatingHours}",
                                    fontSize = 11.sp,
                                    color = OceanBlueDark,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Row {
                            IconButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateToggle(context)
                                    onToggleFavorite(spot.id)
                                    if (!isFavorite && activeUid != null) {
                                        coroutineScope.launch {
                                            firestoreService.saveCloudBookmark(
                                                userId = activeUid,
                                                item = TourismBookmarkCloudItem(
                                                    spotId = spot.id,
                                                    userId = activeUid,
                                                    spotName = spot.name,
                                                    category = spot.category,
                                                    location = spot.location,
                                                    rating = spot.rating.toDouble()
                                                )
                                            )
                                        }
                                    }
                                },
                                modifier = Modifier.testTag("favorite_hotspot_btn_${spot.id}")
                            ) {
                                Icon(
                                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                    contentDescription = "Favorite Hotspot",
                                    tint = if (isFavorite) Color(0xFFE11D48) else BentoSlate600
                                )
                            }

                            IconButton(
                                onClick = {
                                    val shareText = buildString {
                                        append("📍 ${spot.name} (${spot.category}) — ⭐ $displayRating\n")
                                        append("${spot.effectiveDetailedDescription}\n")
                                        if (spot.historicalSignificance.isNotBlank()) {
                                            append("🏛️ History: ${spot.historicalSignificance}\n")
                                        }
                                        append("🕒 Hours: ${spot.effectiveOperatingHours}\n")
                                        append("🗺️ Navigate: https://maps.google.com/?q=$spotLat,$spotLng")
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Visit ${spot.name} in Balasore")
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share ${spot.name}"))
                                },
                                modifier = Modifier.testTag("share_hotspot_btn_${spot.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Hotspot",
                                    tint = OceanBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    // Detailed Description
                    Text(
                        text = spot.effectiveDetailedDescription,
                        fontSize = 13.sp,
                        color = BentoSlate700,
                        lineHeight = 18.sp
                    )

                    // Historical Significance Section
                    if (spot.historicalSignificance.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isOdia) "🏛️ ଐତିହାସିକ ମହତ୍ତ୍ୱ (Historical Significance)" else "🏛️ Historical & Cultural Significance",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFB45309)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = spot.historicalSignificance,
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F),
                                    lineHeight = 16.5.sp
                                )
                            }
                        }
                    }

                    // Embedded Map & GPS Navigation inside Hotspot Card
                    Spacer(modifier = Modifier.height(10.dp))
                    HotspotEmbeddedMap(
                        latitude = spotLat,
                        longitude = spotLng,
                        spotName = spot.name,
                        spotCategory = spot.category,
                        distanceKm = spot.distanceKm,
                        language = language,
                        onOpenFullDistrictMap = {
                            onOpenFeatureSheet(UniqueFeatureSheetType.TOURISM_INTERACTIVE_MAP)
                        }
                    )

                    // Show latest user review snippet if available (excluding moderator-rejected items)
                    val visibleSpotReviews = spotReviews.filterNot {
                        ContentModerationRepository.isContentRejected("HOTSPOT", spot.id, it.comment)
                    }
                    if (visibleSpotReviews.isNotEmpty()) {
                        val latest = visibleSpotReviews.first()
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = "★".repeat(latest.rating), color = AmberGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "\"${latest.comment}\" — ${latest.userName}",
                                        fontSize = 11.5.sp,
                                        color = BentoSlate700,
                                        maxLines = 1
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                ReviewModerationBadgeAndFlagButton(
                                    targetType = "HOTSPOT",
                                    targetId = spot.id,
                                    targetTitle = spot.name,
                                    authorName = latest.userName,
                                    rating = latest.rating,
                                    commentText = latest.comment,
                                    isOdia = isOdia,
                                    tagSuffix = "spot_${spot.id}"
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Mark as Visited button
                        FilterChip(
                            selected = isVisited,
                            onClick = {
                                coroutineScope.launch {
                                    visitedSpotIds = firestoreService.toggleVisitedSpot(
                                        userId = activeUid,
                                        spotId = spot.id,
                                        spotName = spot.name,
                                        category = spot.category,
                                        location = spot.location
                                    )
                                    val msg = if (visitedSpotIds.contains(spot.id)) {
                                        "Marked ${spot.name} as Visited!"
                                    } else {
                                        "Removed ${spot.name} from Visited list"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = {
                                Text(
                                    text = if (isVisited) (if (isOdia) "✓ ଭ୍ରମଣ ହୋଇଛି" else "✓ Visited") else (if (isOdia) "ଭ୍ରମଣ ଚିହ୍ନଟ" else "Mark Visited"),
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            modifier = Modifier.testTag("mark_visited_hotspot_btn_${spot.id}")
                        )

                        // Download Hotspot for Offline Viewing button
                        val isSpotDownloaded = downloadedHotspotIds.contains(spot.id)
                        FilterChip(
                            selected = isSpotDownloaded,
                            onClick = {
                                BalasoreHapticHelper.vibrateToggle(context)
                                coroutineScope.launch {
                                    val savedNow = com.example.ui.components.BalasoreOfflineAccessManager.toggleDownloadHotspot(
                                        context = context,
                                        hotspot = spot
                                    )
                                    val msg = if (savedNow) {
                                        "Downloaded ${spot.name} to Room DB for offline viewing!"
                                    } else {
                                        "Removed ${spot.name} from offline downloads"
                                    }
                                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                                }
                            },
                            label = {
                                Text(
                                    text = if (isSpotDownloaded) {
                                        if (isOdia) "✓ ଅଫଲାଇନ୍ ସେଭ୍" else "✓ Offline Saved"
                                    } else {
                                        if (isOdia) "⬇ ଅଫଲାଇନ୍ ଡାଉନଲୋଡ୍" else "⬇ Download Offline"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            modifier = Modifier.testTag("hotspot_offline_download_btn_${spot.id}")
                        )

                        // Rate & Review button
                        OutlinedButton(
                            onClick = { ratingDialogSpot = spot },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("rate_hotspot_btn_${spot.id}")
                        ) {
                            Icon(Icons.Default.Star, contentDescription = null, tint = AmberGold, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isOdia) "ରେଟିଂ ଦିଅନ୍ତୁ" else "Rate (${spotReviews.size})", fontSize = 11.5.sp)
                        }

                        // View Details & Share button
                        OutlinedButton(
                            onClick = { selectedDetailHotspot = spot },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("view_hotspot_details_btn_${spot.id}")
                        ) {
                            Icon(Icons.Default.Info, contentDescription = "View Hotspot Details", tint = OceanBlue, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isOdia) "ବିବରଣୀ" else "Details", fontSize = 11.5.sp)
                        }

                        // Navigate button
                        Button(
                            onClick = {
                                val gmmIntentUri = Uri.parse("google.navigation:q=$spotLat,$spotLng")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                                    setPackage("com.google.android.apps.maps")
                                }
                                try {
                                    context.startActivity(mapIntent)
                                } catch (_: Exception) {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, Uri.parse("geo:$spotLat,$spotLng?q=${Uri.encode(spot.name)}"))
                                    )
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            modifier = Modifier.testTag("navigate_hotspot_btn_${spot.id}")
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (isOdia) "ଦିଗନିର୍ଦ୍ଦେଶ" else "Navigate", fontSize = 11.5.sp)
                        }
                    }
                }
            }
        }

        // 5B. Traveler Photos, Reviews & Insider Tips UGC Hub + Moderation Queue
        item {
            com.example.ui.components.TourismCommunityUgcHubCard(
                language = language,
                activeUserId = activeUid,
                activeUserName = activeUserName,
                onOpenFullModeratorSheet = {
                    onOpenFeatureSheet(UniqueFeatureSheetType.ADMIN_MODERATOR_PANEL)
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 6. Stored Feedback & Ratings Analytics Summary Card
        item {
            FeedbackAnalyticsSummaryCard(
                allReviews = allStoredReviews,
                isOdia = isOdia,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        // 7. General App Feedback & Rating Form Card (persisted to Room & Cloud Firestore for analysis)
        item {
            FeedbackFormCard(
                language = language,
                modifier = Modifier.padding(horizontal = 16.dp),
                onSubmittedSuccessfully = { report ->
                    coroutineScope.launch {
                        // Store in Room ReviewDao for local analytics alongside Firestore 'feedback' collection
                        reviewDao.insertReview(
                            ReviewEntity(
                                targetType = "APP_FEEDBACK",
                                targetId = "balasore_app_general",
                                targetTitle = report.title.ifBlank { "Baleshwar 360 App Feedback" },
                                userId = activeUid ?: "citizen_user",
                                userName = report.userName.ifBlank { activeUserName },
                                rating = report.rating,
                                comment = report.description.ifBlank { "Rated ${report.rating} stars" },
                                timestamp = System.currentTimeMillis()
                            )
                        )
                    }
                }
            )
        }
    }
    }

    // Hotspot Rating & Review Modal Dialog
    if (ratingDialogSpot != null) {
        val spot = ratingDialogSpot!!
        val spotReviews = allStoredReviews.filter {
            it.targetType == "HOTSPOT" &&
                it.targetId == spot.id &&
                !ContentModerationRepository.isContentRejected("HOTSPOT", spot.id, it.comment)
        }
        HotspotRatingDialog(
            spot = spot,
            existingReviews = spotReviews,
            defaultUserName = activeUserName,
            isOdia = isOdia,
            onDismiss = { ratingDialogSpot = null },
            onSubmit = { stars, comment, reviewerName, travelTip, tipCategory, photoUris ->
                coroutineScope.launch {
                    val finalName = reviewerName.ifBlank { activeUserName }
                    val combinedComment = if (travelTip.isNotBlank()) {
                        "$comment | Tip ($tipCategory): $travelTip"
                    } else {
                        comment
                    }
                    val ugcCreated = com.example.ui.components.TourismUgcModerationRepository.submitUgc(
                        spotId = spot.id,
                        spotName = spot.name,
                        userId = activeUid ?: "local_explorer",
                        authorName = finalName,
                        rating = stars,
                        reviewText = comment,
                        travelTip = travelTip,
                        tipCategory = tipCategory,
                        photoUris = photoUris
                    )
                    val insertedId = reviewDao.insertReview(
                        ReviewEntity(
                            targetType = "HOTSPOT",
                            targetId = spot.id,
                            targetTitle = spot.name,
                            userId = activeUid ?: "local_explorer",
                            userName = finalName,
                            rating = stars,
                            comment = combinedComment,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    ContentModerationRepository.registerSubmittedReview(
                        context = context,
                        reviewDbId = insertedId,
                        targetType = "HOTSPOT",
                        targetId = spot.id,
                        targetTitle = spot.name,
                        userId = activeUid ?: "local_explorer",
                        userName = finalName,
                        rating = stars,
                        comment = combinedComment
                    )
                    firestoreService.submitTourismReview(
                        TourismReviewCloudEntity(
                            reviewId = ugcCreated.id,
                            spotId = spot.id,
                            spotName = spot.name,
                            userId = activeUid ?: "local_explorer",
                            userDisplayName = finalName,
                            rating = stars,
                            reviewText = comment,
                            travelTip = travelTip,
                            tipCategory = tipCategory,
                            photoUris = photoUris,
                            moderationStatus = ugcCreated.moderationStatus.name
                        )
                    )
                    Toast.makeText(context, "Thank you for rating ${spot.name} ($stars★)! Submitted for moderation.", Toast.LENGTH_SHORT).show()
                    ratingDialogSpot = null
                }
            }
        )
    }

    // Tourism Hotspot Details View Dialog with Intent.ACTION_SEND 'Share' Icon Button
    if (selectedDetailHotspot != null) {
        val detailSpot = selectedDetailHotspot!!
        LaunchedEffect(detailSpot.id) {
            BalasoreRecommendationEngine.recordLocationViewed(context, detailSpot)
        }
        AlertDialog(
            onDismissRequest = { selectedDetailHotspot = null },
            modifier = Modifier.testTag("tourism_hotspot_details_dialog"),
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOdia) detailSpot.odiaName else detailSpot.name,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 17.sp
                        )
                        Text(
                            text = "${detailSpot.category} • ⭐ ${detailSpot.rating} (${detailSpot.reviewCount} reviews) • ${detailSpot.distanceKm} km",
                            fontSize = 12.sp,
                            color = OceanBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    IconButton(
                        onClick = {
                            val shareText = buildString {
                                append("📍 ${detailSpot.name} (${detailSpot.odiaName})\n")
                                append("Category: ${detailSpot.category} | Rating: ⭐ ${detailSpot.rating}\n")
                                append("Location: ${detailSpot.location} | Timings: ${detailSpot.effectiveOperatingHours}\n\n")
                                append("${detailSpot.effectiveDetailedDescription}\n\n")
                                if (detailSpot.historicalSignificance.isNotBlank()) {
                                    append("🏛️ Historical Significance: ${detailSpot.historicalSignificance}")
                                }
                            }
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, detailSpot.name)
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(
                                Intent.createChooser(sendIntent, "Share ${detailSpot.name}")
                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            )
                        },
                        modifier = Modifier.testTag("hotspot_details_share_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Hotspot Details",
                            tint = OceanBlue
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HotspotOfflineHighResGalleryCarousel(
                        spotName = detailSpot.name,
                        spotCategory = detailSpot.category,
                        isOdia = isOdia
                    )
                    Text(
                        text = detailSpot.effectiveDetailedDescription,
                        fontSize = 13.sp,
                        color = BentoSlate700,
                        lineHeight = 18.sp
                    )
                    if (detailSpot.historicalSignificance.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFFFBEB),
                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "🏛️ Historical Significance",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFB45309)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = detailSpot.historicalSignificance,
                                    fontSize = 12.sp,
                                    color = Color(0xFF78350F)
                                )
                            }
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = OceanBlue.copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("📍 Location: ${detailSpot.location}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            Text("🕒 Operating Hours: ${detailSpot.effectiveOperatingHours}", fontSize = 12.sp, color = BentoSlate700)
                        }
                    }
                    HotspotEmbeddedMap(
                        latitude = detailSpot.latitude,
                        longitude = detailSpot.longitude,
                        spotName = detailSpot.name,
                        spotCategory = detailSpot.category,
                        distanceKm = detailSpot.distanceKm,
                        language = language
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val shareText = "${detailSpot.name}\n\n${detailSpot.description}\n📍 ${detailSpot.location} (${detailSpot.timing})"
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, detailSpot.name)
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(
                            Intent.createChooser(sendIntent, "Share ${detailSpot.name}")
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    modifier = Modifier.testTag("hotspot_details_share_action_btn")
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Share Hotspot", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isOdia) "ସେୟାର୍ କରନ୍ତୁ" else "Share Hotspot")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { selectedDetailHotspot = null },
                    modifier = Modifier.testTag("close_hotspot_details_dialog_btn")
                ) {
                    Text(if (isOdia) "ବନ୍ଦ କରନ୍ତୁ" else "Close")
                }
            }
        )
    }
}

@Composable
private fun FeedbackAnalyticsSummaryCard(
    allReviews: List<ReviewEntity>,
    isOdia: Boolean,
    modifier: Modifier = Modifier
) {
    val hotspotReviews = remember(allReviews) { allReviews.filter { it.targetType == "HOTSPOT" } }
    val newsReviews = remember(allReviews) { allReviews.filter { it.targetType == "NEWS" } }
    val appFeedbacks = remember(allReviews) { allReviews.filter { it.targetType == "APP_FEEDBACK" } }

    val overallAvg = remember(allReviews) {
        if (allReviews.isNotEmpty()) allReviews.map { it.rating }.average() else 4.8
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("feedback_analytics_summary_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Analytics, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ରେଟିଂ ଓ ମତାମତ ବିଶ୍ଳେଷଣ (Analytics)" else "Ratings & Feedback Analytics Hub",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isOdia) "ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ, ଖବର ଓ ଆପ୍ ମତାମତ ତଥ୍ୟ" else "Stored ratings across Hotspots, News & General App Feedback",
                            fontSize = 11.sp,
                            color = BentoSlate600
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7)
                ) {
                    Text(
                        text = String.format(Locale.US, "★ %.1f Avg (%d)", overallAvg, allReviews.size),
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 12.sp,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AnalyticsMetricPill(
                    title = if (isOdia) "ପର୍ଯ୍ୟଟନ ରେଟିଂ" else "Hotspot Ratings",
                    count = hotspotReviews.size,
                    avg = if (hotspotReviews.isNotEmpty()) hotspotReviews.map { it.rating }.average() else 4.8,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsMetricPill(
                    title = if (isOdia) "ଖବର ରେଟିଂ" else "News Ratings",
                    count = newsReviews.size,
                    avg = if (newsReviews.isNotEmpty()) newsReviews.map { it.rating }.average() else 4.7,
                    modifier = Modifier.weight(1f)
                )
                AnalyticsMetricPill(
                    title = if (isOdia) "ଆପ୍ ମତାମତ" else "App Feedback",
                    count = appFeedbacks.size,
                    avg = if (appFeedbacks.isNotEmpty()) appFeedbacks.map { it.rating }.average() else 4.9,
                    modifier = Modifier.weight(1f)
                )
            }

            if (allReviews.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Text(
                    text = if (isOdia) "ସାମ୍ପ୍ରତିକ ସଂରକ୍ଷିତ ମତାମତ:" else "Recent Stored Ratings & Feedback:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate700
                )
                allReviews
                    .filterNot { ContentModerationRepository.isContentRejected(it.targetType, it.targetId, it.comment) }
                    .take(3)
                    .forEachIndexed { idx, rev ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "[${rev.targetType}] ${rev.targetTitle}",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp,
                                    maxLines = 1
                                )
                                Text(
                                    text = "\"${rev.comment}\" — ${rev.userName}",
                                    fontSize = 11.sp,
                                    color = BentoSlate600,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            ReviewModerationBadgeAndFlagButton(
                                targetType = rev.targetType,
                                targetId = rev.targetId,
                                targetTitle = rev.targetTitle,
                                authorName = rev.userName,
                                rating = rev.rating,
                                commentText = rev.comment,
                                isOdia = isOdia,
                                tagSuffix = "analytics_$idx"
                            )
                        }
                    }
            }
        }
    }
}

@Composable
private fun AnalyticsMetricPill(
    title: String,
    count: Int,
    avg: Double,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = title, fontSize = 10.5.sp, color = BentoSlate600, fontWeight = FontWeight.Medium)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = String.format(Locale.US, "%.1f★ (%d)", avg, count),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp,
                color = OceanBlue
            )
        }
    }
}

@Composable
private fun HotspotRatingDialog(
    spot: Hotspot,
    existingReviews: List<ReviewEntity>,
    defaultUserName: String,
    isOdia: Boolean,
    onDismiss: () -> Unit,
    onSubmit: (stars: Int, comment: String, reviewerName: String, travelTip: String, tipCategory: String, photoUris: List<String>) -> Unit
) {
    var selectedStars by remember { mutableIntStateOf(5) }
    var comment by remember { mutableStateOf("") }
    var travelTip by remember { mutableStateOf("") }
    var tipCategory by remember { mutableStateOf("Best Time to Visit") }
    var attachedPhotos by remember { mutableStateOf<List<String>>(emptyList()) }
    var reviewerName by remember { mutableStateOf(defaultUserName) }

    val dialogPhotoLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachedPhotos = (attachedPhotos + uri.toString()).distinct()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = if (isOdia) "${spot.odiaName} କୁ ରେଟିଂ ଓ ଫଟୋ ଦିଅନ୍ତୁ" else "Rate, Review & Share Tip: ${spot.name}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "${spot.category} • ${spot.location}",
                    fontSize = 12.sp,
                    color = OceanBlue
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    for (s in 1..5) {
                        IconButton(
                            onClick = { selectedStars = s },
                            modifier = Modifier.testTag("hotspot_dialog_star_$s")
                        ) {
                            Icon(
                                imageVector = if (s <= selectedStars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                contentDescription = "$s Stars",
                                tint = if (s <= selectedStars) AmberGold else BentoSlate600,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = reviewerName,
                    onValueChange = { reviewerName = it },
                    label = { Text(if (isOdia) "ଆପଣଙ୍କ ନାମ" else "Your Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hotspot_review_name_input")
                )

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text(if (isOdia) "ଆପଣଙ୍କ ଅନୁଭୂତି (Review)" else "Write your attraction review...") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hotspot_review_comment_input")
                )

                OutlinedTextField(
                    value = travelTip,
                    onValueChange = { travelTip = it },
                    label = { Text(if (isOdia) "ଭ୍ରମଣ ଟିପ୍ସ (Optional Travel Tip)" else "Share an Insider Tip (e.g., tide, parking, prasad)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("hotspot_review_tip_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            dialogPhotoLauncher.launch(
                                androidx.activity.result.PickVisualMediaRequest(
                                    androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                )
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("hotspot_dialog_upload_photo_btn")
                    ) {
                        Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (attachedPhotos.isEmpty()) "📷 Upload Photo" else "✓ ${attachedPhotos.size} Photo(s)",
                            fontSize = 11.sp
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            attachedPhotos = (attachedPhotos + "content://balasore360/tourism_photos/${spot.id}_snap.jpg").distinct()
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("hotspot_dialog_sample_photo_btn")
                    ) {
                        Text("+ Snap", fontSize = 11.sp)
                    }
                }

                if (existingReviews.isNotEmpty()) {
                    Text(
                        text = "Recent Reviews (${existingReviews.size}):",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    existingReviews.take(3).forEachIndexed { idx, r ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "★${r.rating} ${r.userName}: ${r.comment}",
                                fontSize = 11.sp,
                                color = BentoSlate700,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            ReviewModerationBadgeAndFlagButton(
                                targetType = "HOTSPOT",
                                targetId = spot.id,
                                targetTitle = spot.name,
                                authorName = r.userName,
                                rating = r.rating,
                                commentText = r.comment,
                                isOdia = isOdia,
                                tagSuffix = "dialog_${spot.id}_$idx"
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmit(
                        selectedStars,
                        comment.ifBlank { "Great visit to ${spot.name}!" },
                        reviewerName,
                        travelTip,
                        tipCategory,
                        attachedPhotos
                    )
                },
                modifier = Modifier.testTag("submit_hotspot_review_btn")
            ) {
                Text(if (isOdia) "ସବମିଟ୍ କରନ୍ତୁ" else "Submit for Moderation")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(if (isOdia) "ବାତିଲ୍" else "Cancel")
            }
        }
    )
}

@Composable
fun WeatherScreen(
    weather: WeatherInfo = BalasoreRepository.weather,
    forecastDays: List<BalasoreForecastDay> = BalasoreRepository.forecast3Days,
    hourlyForecast: List<BalasoreForecastHour> = emptyList(),
    isWeatherLoading: Boolean = false,
    isFahrenheit: Boolean = false,
    onRefreshWeather: () -> Unit = {},
    onToggleUnit: () -> Unit = {},
    tidalClock: TidalClockData = BalasoreRepository.tidalClock,
    drdoAdvisories: List<DrdoAdvisory> = BalasoreRepository.drdoAdvisories,
    language: AppLanguage = com.example.ui.components.LocalAppLanguage.current.currentLanguage,
    lastKnownTide: ChandipurTideEntity? = null,
    openWeatherSevenDayForecast: List<OpenWeatherDailyForecast> = emptyList(),
    openWeatherCurrent: OpenWeatherCurrentResponse? = null,
    isOpenWeatherLoading: Boolean = false,
    onRefreshOpenWeather: () -> Unit = {},
    onOpenFeatureSheet: (UniqueFeatureSheetType) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestoreService = remember { FirestoreService(context) }
    val authManager = remember { FirebaseAuthManager(context) }
    val activeSessionUser by authManager.activeSessionUser.collectAsStateWithLifecycle()
    val currentUser by authManager.currentUser.collectAsStateWithLifecycle()
    val activeUid = currentUser?.uid ?: activeSessionUser?.id ?: "local_weather_user"

    var weatherPrefs by remember { mutableStateOf(UserWeatherPreferences(userId = activeUid)) }
    val isOdia = language == AppLanguage.ODIA || com.example.ui.components.LocalAppLanguage.current.isOdia

    LaunchedEffect(activeUid) {
        val fetched = firestoreService.fetchWeatherPreferences(activeUid).getOrNull()
        if (fetched != null) {
            weatherPrefs = fetched
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Premier OpenWeatherMap Live Weather & Severe Alerts Dashboard Component
        item {
            com.example.ui.components.LocalWeatherDashboard(
                language = language,
                initialFahrenheit = isFahrenheit,
                onToggleTempUnit = { onToggleUnit() },
                onOpenFullForecast = { onOpenFeatureSheet(UniqueFeatureSheetType.OPEN_WEATHER_MAP) }
            )
        }

        // 1B. Balasore OpenWeather Dashboard
        item {
            BalasoreOpenWeatherDashboard(
                language = language,
                onOpenFullForecast = { onOpenFeatureSheet(UniqueFeatureSheetType.OPEN_WEATHER_MAP) }
            )
        }

        // 2. Real-Time Local Weather Composable (Temperature, Conditions & 3-Day Forecast)
        item {
            LocalWeather(
                language = language,
                onOpenFullForecast = { onOpenFeatureSheet(UniqueFeatureSheetType.OPEN_WEATHER_MAP) }
            )
        }

        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "ଚାନ୍ଦିପୁର ଜୁଆର ଓ ପାଣିପାଗ" else "Balasore Weather & Chandipur Tide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        IconButton(onClick = {
                            onRefreshWeather()
                            onRefreshOpenWeather()
                        }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Weather")
                        }
                    }
                    Text(
                        text = "${weather.tempCelsius}°C • ${weather.condition} • Humidity ${weather.humidity}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOdia) {
                            "ଜୁଆର ସ୍ଥିତି: ${tidalClock.currentPhase} (${tidalClock.distanceRecededKm} କି.ମି. ଭଟ୍ଟା)"
                        } else {
                            "Tide Phase: ${tidalClock.currentPhase} (Receded ${tidalClock.distanceRecededKm} km)"
                        },
                        fontSize = 12.5.sp,
                        color = BentoSlate700
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.OFFLINE_TIDE_DASHBOARD) }) {
                            Text(if (isOdia) "୩୦-ଦିନିଆ ଅଫଲାଇନ୍ ଜୁଆର ସାରଣୀ" else "30-Day Offline Tide Dashboard")
                        }
                        OutlinedButton(onClick = onToggleUnit) {
                            Text(if (isFahrenheit) "°F" else "°C")
                        }
                    }
                }
            }
        }

        // Custom Weather Alert Preferences Card (Customize Cyclone, High Tide, Heat & Rainfall alerts)
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_alert_preferences_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = OceanBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOdia) "କଷ୍ଟମ୍ ପାଣିପାଗ ସତର୍କତା ପସନ୍ଦ" else "Customize Weather Alert Preferences",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    WeatherPrefToggleRow(
                        label = if (isOdia) "ବାତ୍ୟା ଓ ଝଡ଼ ସତର୍କତା (IMD Cyclone)" else "Bay of Bengal Cyclone & Gale Alerts",
                        checked = weatherPrefs.notifyCyclone,
                        onCheckedChange = {
                            weatherPrefs = weatherPrefs.copy(notifyCyclone = it)
                            coroutineScope.launch { firestoreService.saveWeatherPreferences(activeUid, weatherPrefs) }
                        },
                        testTag = "weather_screen_pref_cyclone"
                    )
                    WeatherPrefToggleRow(
                        label = if (isOdia) "ଚାନ୍ଦିପୁର ଉଚ୍ଚ ଜୁଆର ସତର୍କତା" else "Chandipur High Tide Inflow Warnings",
                        checked = weatherPrefs.notifyHighTide,
                        onCheckedChange = {
                            weatherPrefs = weatherPrefs.copy(notifyHighTide = it)
                            coroutineScope.launch { firestoreService.saveWeatherPreferences(activeUid, weatherPrefs) }
                        },
                        testTag = "weather_screen_pref_tide"
                    )
                    WeatherPrefToggleRow(
                        label = if (isOdia) "ଭାରୀ ବର୍ଷା ଓ ସୁବର୍ଣ୍ଣରେଖା ବନ୍ୟା" else "Heavy Rainfall & Subarnarekha Flood Alerts",
                        checked = weatherPrefs.notifyRainfall,
                        onCheckedChange = {
                            weatherPrefs = weatherPrefs.copy(notifyRainfall = it)
                            coroutineScope.launch { firestoreService.saveWeatherPreferences(activeUid, weatherPrefs) }
                        },
                        testTag = "weather_screen_pref_rainfall"
                    )
                }
            }
        }

        // 7-Day Recharts Weather & Tidal Forecast Visualization
        item {
            BalasoreSevenDayWeatherTrendsRechartsCard(
                language = language,
                isFahrenheit = isFahrenheit
            )
        }

        // 7-Day Chandipur Tidal Analytics Recharts Dashboard (Simulated Hydrographic API)
        item {
            ChandipurTidalAnalyticsDashboard(
                language = language
            )
        }

        items(forecastDays, key = { it.id }) { day ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("${day.weatherIcon} ${day.dayLabel}", fontWeight = FontWeight.Bold)
                        Text(day.condition, fontSize = 12.sp, color = BentoSlate700)
                    }
                    Text("${day.highTempC}° / ${day.lowTempC}°C", fontWeight = FontWeight.Bold, color = OceanBlue)
                }
            }
        }
    }
}

@Composable
private fun WeatherPrefToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 12.5.sp, modifier = Modifier.weight(1f))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(testTag)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDetailScreen(
    article: NewsArticle,
    onNavigateBack: () -> Unit,
    onToggleBookmark: () -> Unit,
    onToggleNightMode: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestoreService = remember { FirestoreService(context) }
    val authManager = remember { FirebaseAuthManager(context) }
    val reviewDao = remember { AppDatabase.getInstance(context).reviewDao() }

    val activeSessionUser by authManager.activeSessionUser.collectAsStateWithLifecycle()
    val currentUser by authManager.currentUser.collectAsStateWithLifecycle()
    val activeUid = currentUser?.uid ?: activeSessionUser?.id ?: "local_reader"
    val activeUserName = activeSessionUser?.fullName ?: currentUser?.displayName ?: "Balasore Reader"

    val articleReviews by reviewDao.getReviewsForTarget("NEWS", article.id)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedStars by remember { mutableIntStateOf(5) }
    var readerComment by remember { mutableStateOf("") }
    var isSavedFavorite by remember(article.isBookmarked) { mutableStateOf(article.isBookmarked) }

    var viewCount by remember { mutableIntStateOf(firestoreService.getArticleViewCountSync(article.id)) }

    LaunchedEffect(article.id) {
        BalasoreRecommendationEngine.recordArticleViewed(context, article)
        viewCount = firestoreService.incrementArticleViewCount(
            articleId = article.id,
            title = article.title,
            category = article.category
        )
    }

    val scrollState = rememberScrollState()
    val rawProgress = if (scrollState.maxValue > 0) {
        (scrollState.value.toFloat() / scrollState.maxValue.toFloat()).coerceIn(0f, 1f)
    } else {
        1f
    }
    val animatedProgress by animateFloatAsState(targetValue = rawProgress, label = "reading_progress")

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text(article.category, maxLines = 1, fontSize = 15.sp) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack, modifier = Modifier.testTag("news_detail_back_button")) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                val previousBookmarked = isSavedFavorite
                                isSavedFavorite = !isSavedFavorite
                                onToggleBookmark()
                                coroutineScope.launch {
                                    try {
                                        val localDb = com.example.data.local.AppDatabase.getInstance(context)
                                        val articleLongId = article.id.toLongOrNull()
                                        if (articleLongId != null) {
                                            localDb.newsDao().updateBookmark(articleLongId, isSavedFavorite)
                                        }
                                        com.example.data.repository.SavedBookmarkRepository(localDb.savedBookmarkDao())
                                            .toggleNewsArticle(article, previousBookmarked)
                                    } catch (_: Throwable) {}
                                    if (isSavedFavorite) {
                                        firestoreService.saveFavoriteNews(
                                            userId = activeUid,
                                            item = FavoriteNewsCloudItem(
                                                newsId = article.id,
                                                userId = activeUid,
                                                title = article.title,
                                                category = article.category,
                                                source = article.source,
                                                publishedAt = article.timeAgo
                                            )
                                        )
                                        Toast.makeText(context, "Article saved to local database for offline access!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        firestoreService.removeFavoriteNews(activeUid, article.id)
                                        Toast.makeText(context, "Removed from saved bookmarks", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.testTag("news_detail_bookmark_button")
                        ) {
                            Icon(
                                imageVector = if (isSavedFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark Article",
                                tint = if (isSavedFavorite) OceanBlue else MaterialTheme.colorScheme.onSurface
                            )
                        }
                        IconButton(
                            onClick = {
                                val articleLink = "https://baleshwar360.in/news/${article.id}"
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_SUBJECT, article.title)
                                    putExtra(Intent.EXTRA_TEXT, "📰 ${article.title}\n${article.snippet}\n🔗 Read more: $articleLink\n— Shared via Baleshwar 360")
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Share News Article"))
                            },
                            modifier = Modifier.testTag("news_detail_share_button")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Article")
                        }
                        IconButton(onClick = onToggleNightMode) {
                            Icon(Icons.Default.DarkMode, contentDescription = "Toggle Reading Mode")
                        }
                    }
                )
                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .testTag("news_reading_progress_bar"),
                    color = OceanBlue
                )
            }
        },
        modifier = modifier
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            val isOdia = com.example.ui.components.LocalAppLanguage.current.isOdia
            Text(
                text = if (isOdia && article.odiaTitle.isNotBlank()) article.odiaTitle else article.title,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
            if (!isOdia && article.odiaTitle.isNotBlank()) {
                Text(text = article.odiaTitle, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = OceanBlue)
            } else if (isOdia && article.title.isNotBlank()) {
                Text(text = article.title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = OceanBlue)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "${article.source} • ${article.timeAgo}", fontSize = 12.sp, color = BentoSlate700)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = OceanBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = if (isOdia) "🔥 $viewCount ପାଠକ (Trending)" else "🔥 $viewCount Views",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Prominent Share Button in News Article Detail View
            Button(
                onClick = {
                    val articleLink = "https://baleshwar360.in/news/${article.id}"
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_SUBJECT, article.title)
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "📰 ${article.title}\n${article.snippet}\n🔗 Link: $articleLink\n— Shared via Baleshwar 360"
                        )
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Share Article Headline & Link"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("news_detail_share_headline_btn")
            ) {
                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOdia) "ଖବର ଶିରୋନାମା ଓ ଲିଙ୍କ୍ ସେୟାର୍ କରନ୍ତୁ (Share)" else "Share Article Headline & Link",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Text(
                text = if (isOdia && article.odiaContent.isNotBlank()) {
                    article.odiaContent
                } else {
                    article.content.ifBlank { article.snippet }
                },
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
            if (!isOdia && article.odiaContent.isNotBlank()) {
                Text(
                    text = article.odiaContent,
                    fontSize = 14.5.sp,
                    lineHeight = 22.sp,
                    color = BentoSlate700
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            // News Article Rating & Reader Feedback Card (Stored in Room & Firestore for Analysis)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("news_article_rating_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.RateReview, contentDescription = null, tint = OceanBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Rate & Review this News Article",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                        if (articleReviews.isNotEmpty()) {
                            val avg = articleReviews.map { it.rating }.average()
                            Text(
                                text = String.format(Locale.US, "★ %.1f (%d)", avg, articleReviews.size),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = AmberGold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        for (s in 1..5) {
                            IconButton(
                                onClick = { selectedStars = s },
                                modifier = Modifier.testTag("news_rating_star_$s")
                            ) {
                                Icon(
                                    imageVector = if (s <= selectedStars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "$s Stars",
                                    tint = if (s <= selectedStars) AmberGold else BentoSlate600,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = readerComment,
                        onValueChange = { readerComment = it },
                        label = { Text("Add your feedback on this news report...") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("news_rating_comment_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            val finalComment = readerComment.ifBlank { "Rated this news report $selectedStars stars." }
                            coroutineScope.launch {
                                val revId = reviewDao.insertReview(
                                    ReviewEntity(
                                        targetType = "NEWS",
                                        targetId = article.id,
                                        targetTitle = article.title,
                                        userId = activeUid,
                                        userName = activeUserName,
                                        rating = selectedStars,
                                        comment = finalComment,
                                        timestamp = System.currentTimeMillis()
                                    )
                                )
                                ContentModerationRepository.registerSubmittedReview(
                                    context = context,
                                    reviewDbId = revId,
                                    targetType = "NEWS",
                                    targetId = article.id,
                                    targetTitle = article.title,
                                    userId = activeUid,
                                    userName = activeUserName,
                                    rating = selectedStars,
                                    comment = finalComment
                                )
                                firestoreService.submitFeedback(
                                    FeedbackReport(
                                        userName = activeUserName,
                                        locality = "Balasore",
                                        category = "News Article Rating",
                                        title = "News [${article.id}]: ${article.title.take(45)}",
                                        description = finalComment,
                                        rating = selectedStars
                                    )
                                )
                                readerComment = ""
                                Toast.makeText(context, "News rating ($selectedStars★) submitted for moderation & analysis!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("submit_news_rating_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submit News Rating ($selectedStars★)", fontWeight = FontWeight.Bold)
                    }

                    val visibleArticleReviews = articleReviews.filterNot {
                        ContentModerationRepository.isContentRejected("NEWS", article.id, it.comment)
                    }
                    if (visibleArticleReviews.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Reader Ratings & Comments (${visibleArticleReviews.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        visibleArticleReviews.forEachIndexed { idx, rev ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("${rev.userName} (${rev.rating}★)", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                                    Text(rev.comment, fontSize = 11.5.sp, color = BentoSlate700)
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                ReviewModerationBadgeAndFlagButton(
                                    targetType = "NEWS",
                                    targetId = article.id,
                                    targetTitle = article.title,
                                    authorName = rev.userName,
                                    rating = rev.rating,
                                    commentText = rev.comment,
                                    isOdia = false,
                                    tagSuffix = "news_${article.id}_$idx"
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsDetailScreen(
    article: NewsArticleEntity,
    currentUser: UserEntity? = null,
    reviewsFlow: Flow<List<ReviewEntity>> = emptyFlow(),
    onBack: () -> Unit = {},
    onSubmitReview: (Int, String, String) -> Unit = { _, _, _ -> },
    onLoginPrompt: () -> Unit = {},
    onToggleBookmark: () -> Unit = {},
    onShare: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    NewsDetailScreen(
        article = NewsArticle(
            id = article.id.toString(),
            title = article.title,
            odiaTitle = "",
            snippet = article.summary,
            odiaSnippet = "",
            category = article.category,
            timeAgo = article.publishedAt,
            source = article.source,
            isBookmarked = article.isBookmarked,
            content = article.content
        ),
        onNavigateBack = onBack,
        onToggleBookmark = onToggleBookmark,
        modifier = modifier
    )
}

@Composable
fun HotspotsMapView(
    hotspots: List<HotspotEntity> = emptyList(),
    selectedHotspot: HotspotEntity? = null,
    onHotspotSelected: (HotspotEntity) -> Unit = {},
    onNavigateToDetail: (String) -> Unit = {},
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ପର୍ଯ୍ୟଟନ ମାନଚିତ୍ର" else "Balasore Tourism Hotspots Map",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        BALASORE_TOURIST_LOCATIONS.forEach { spot ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToDetail(spot.id) },
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("${spot.highlightEmoji} ${if (language == AppLanguage.ODIA) spot.nameOr else spot.nameEn}", fontWeight = FontWeight.Bold)
                    Text("${spot.categoryEn} • ${spot.baseDistanceKm} km • ⭐ ${spot.rating}", fontSize = 12.sp, color = OceanBlue)
                    Text(spot.shortDescEn, fontSize = 12.sp, color = BentoSlate700)
                }
            }
        }
    }
}

@Composable
fun BalasoreBusStandScheduleScreen(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ବସ୍ ଷ୍ଟାଣ୍ଡ ସମୟ ସାରଣୀ" else "Balasore OSRTC & Private Bus Stand Schedule",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        BalasoreRepository.transitSchedules.forEach { item ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.routeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${item.origin} ➔ ${item.destination}", fontSize = 12.sp, color = BentoSlate700)
                    }
                    Text(item.time, fontWeight = FontWeight.SemiBold, color = OceanBlue, fontSize = 12.sp)
                }
            }
        }
    }
}
