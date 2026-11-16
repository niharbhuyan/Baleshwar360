package com.example

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.automirrored.outlined.Article
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Emergency
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Emergency
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import com.example.ui.features.emergency.NearestEmergencyServicesMapSheet
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.VerifiedUser
import com.example.ui.components.BalasoreQuickHubSheet
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.viewmodel.ThemeMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.admob.AdMobManager
import com.example.data.model.AppLanguage
import com.example.ui.screens.EssentialsScreen
import com.example.ui.screens.HotspotsMapView
import com.example.ui.screens.NewsScreen
import com.example.ui.screens.SpecialFeaturesScreen
import com.example.ui.screens.TourismScreen
import com.example.ui.screens.CivicIssuesMapScreen
import com.example.ui.screens.WeatherScreen
import com.example.ui.screens.OpenWeatherMapScreen
import com.example.ui.screens.CoastalWeatherRadarMapView
import com.example.ui.screens.OdiaCultureScreen
import com.example.ui.screens.OdiaFestivalsScreen
import com.example.ui.screens.TourismHotspotsSheet
import com.example.ui.screens.BalasoreOsmTourismMapView
import com.example.ui.components.BalasoreD3TelemetryCard
import com.example.ui.components.BalasorePamphletScannerSheet
import com.example.data.pamphlet.PamphletCategory
import com.example.data.model.D3TelemetryMode
import com.example.ui.screens.LocalTransportScreen
import com.example.ui.screens.BalasoreBusStandScheduleScreen
import com.example.ui.components.AppSettingsSheet
import com.example.ui.components.PersistentEmergencySosOverlay
import com.example.ui.theme.AppThemeProvider
import com.example.ui.theme.Balasore360Theme
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate400
import com.example.ui.theme.BentoSlate50
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.ui.viewmodel.BalasoreViewModel
import com.example.ui.viewmodel.UniqueFeatureSheetType
import com.example.ui.components.AppWatermarkOverlay
import com.example.ui.components.CycloneResilienceSheet
import com.example.ui.components.DefenseTrailSheet
import com.example.ui.components.ElephantPassportSheet
import com.example.ui.features.civic.CitizenCivicEyeSheet
import com.example.ui.features.civic.CivicEyeComponent
import com.example.ui.features.civic.MyCivicReportsSheet
import com.example.ui.components.HarborCatchRatesSheet
import com.example.ui.components.HorseshoeCrabSheet
import com.example.ui.components.RemunaPrasadArtisansSheet
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.example.util.BalasoreHapticHelper
import com.example.ui.features.coastal.ChandipurTideTimerSheet
import com.example.ui.features.coastal.ChandipurOfflineTideDashboardSheet
import com.example.ui.features.coastal.CycloneShelterFinderSheet
import com.example.ui.features.heritage.BalasoreFoodTrailSheet
import com.example.ui.features.heritage.HeritageAudioSnippetsSheet
import com.example.ui.features.transit.TransitFareEstimatorSheet
import com.example.ui.features.transit.KuldihaSafariCompanionSheet
import com.example.ui.features.journal.TravelJournalSheet
import com.example.ui.features.emergency.BloodAndDialysisDirectorySheet
import com.example.ui.features.coastal.KasafalRedCrabsSheet
import com.example.ui.features.artisans.NilagiriStoneSabaiSheet
import com.example.ui.features.coastal.BalaramgadiEstuarySheet
import com.example.ui.features.heritage.ColonialHeritageWalkSheet
import com.example.ui.features.coastal.TalasariAuctionMonitorSheet
import com.example.ui.features.heritage.BaleswariyaDialectProverbsSheet
import com.example.ui.features.agro.PanBarajaAgroSheet
import com.example.ui.features.coastal.BichitrapurMangroveSheet
import com.example.ui.features.heritage.ChandaneswarChadakSheet
import com.example.ui.features.nature.PanchalingeswarStreamSheet
import com.example.ui.features.coastal.DarkSkyBioluminescenceSheet
import com.example.ui.features.artisans.BellMetalArtisansSheet
import com.example.ui.features.heritage.BuddhistJainCircuitSheet
import com.example.ui.features.agro.HeirloomRiceAgroSheet
import com.example.ui.features.heritage.LakhannathZamindariSheet
import com.example.ui.features.coastal.BudhabalangaRiverAnglingSheet
import com.example.ui.features.resilience.CycloneOralHistorySheet
import com.example.ui.features.health.DoctorsDirectorySheet
import com.example.ui.features.health.MedicineStoresDirectorySheet
import com.example.ui.features.health.PolyclinicDirectorySheet
import com.example.ui.features.health.PathologyLabDirectorySheet
import com.example.ui.features.health.PrivateHospitalDirectorySheet
import com.example.ui.features.coastal.RiverFloodTelemetrySheet
import com.example.ui.features.artisans.LacquerCraftArtisansSheet
import com.example.ui.features.coastal.SaltPanHeritageSheet
import com.example.ui.features.coastal.HilsaMigrationSheet
import com.example.ui.features.nature.KuldihaElephantCorridorSheet
import com.example.ui.features.heritage.ChhenaGajaHotBatchSheet
import com.example.ui.features.transit.RiverFerryScheduleSheet
import com.example.ui.features.heritage.OdiaSahityaRevivalSheet
import com.example.ui.features.ai.AskBalasoreChatbotSheet
import com.example.ui.features.coastal.IncoisSeaAdvisorySheet
import com.example.ui.features.heritage.BaleswarPanjikaCalendarSheet
import com.example.ui.features.heritage.RaibaniaAudioStoryWalkSheet
import com.example.ui.features.emergency.BloodDonorBedPulseSheet
import com.example.ui.features.transit.BalasoreTotoFareCardSheet
import com.example.ui.features.civic.CampusCareerBoardSheet
import com.example.ui.features.resilience.AutoUpdateCenterSheet
import com.example.ui.features.transit.BalasoreJunctionRadarSheet
import com.example.ui.features.agro.BaleswariPaanKrushiMandiSheet
import com.example.ui.features.health.NightChemistSanjeevaniSheet
import com.example.ui.features.heritage.BaleswarMahotsavChadakSheet
import com.example.ui.features.artisans.NilagiriStoneArtisanSheet
import com.example.ui.features.civic.TpnodlWatcoMonitorSheet
import com.example.ui.features.coastal.TalasariMangroveExplorerSheet
import com.example.ui.features.civic.MoSevaKendraCitizenSheet
import com.example.ui.features.coastal.MarineFishermenSafetySheet
import com.example.ui.features.nature.KuldihaEcoCampSafariSheet
import com.example.ui.features.health.FmMchMedicalCollegeOpdSheet
import com.example.ui.features.artisans.SabaiGrassMissionShaktiSheet
import com.example.ui.features.heritage.BalasoreMaritimeColonialSheet
import com.example.ui.features.civic.BalasoreStudentCareerScholarshipSheet
import com.example.ui.features.coastal.DrdoChandipurSafetyRadarSheet
import com.example.ui.features.nature.SubarnarekhaSluiceFloodRadarSheet
import com.example.ui.features.artisans.NilagiriChhauCulturalGuildSheet
import com.example.ui.features.health.DhhBloodBankLiveRadarSheet
import com.example.ui.features.agro.PanBarajaAquaShrimpSheet
import com.example.ui.features.civic.BalasoreCourtLegalAidSheet
import com.example.ui.features.transit.Nh16HighwayPatrolTraumaSheet
import com.example.ui.features.nature.PanchalingeswarKuldihaSafariSheet
import com.example.ui.features.transit.SerBalasoreRailwayJunctionSheet
import com.example.ui.features.civic.NocciIndustrialB2bSkillSheet
import com.example.ui.features.coastal.TalsariBichitrapurMangrovePilotSheet
import com.example.ui.features.heritage.FakirMohanBhashaLiteratureSheet
import com.example.ui.features.heritage.BaleswariCuisineSweetHeritageSheet
import com.example.ui.features.civic.TpnodlWatcoUtilityHotlineSheet
import com.example.ui.components.LanguageToggleSwitch
import com.example.ui.components.LanguageSwitcher
import com.example.ui.components.LocalAppLanguage
import com.example.ui.components.ProvideAppLanguage
import com.example.ui.components.TopBarPersistentLanguageBar
import com.example.ui.util.AppStrings
import com.example.ui.features.agro.DhanMandiMspProcurementSheet
import com.example.ui.features.artisans.NilagiriGraniteStonewareGuildSheet
import com.example.ui.features.emergency.BahanagaNhaiRapidTraumaNetworkSheet
import com.example.ui.features.heritage.BhusandeswarChandaneswarPilgrimageSheet
import com.example.ui.features.civic.SeniorCitizenSevaSetuSheet
import com.example.ui.features.civic.BalasoreHigherEducationHubSheet
import com.example.ui.features.transit.BalasoreMoBusCrutNavigatorSheet
import com.example.ui.features.coastal.AquaShrimpHatcheryRadarSheet
import com.example.ui.features.resilience.CoastalCycloneShelterNetworkSheet
import com.example.ui.features.heritage.MaritimeForeignLogeTrailSheet
import com.example.ui.features.health.RuralMobileTelemedicineNetworkSheet
import com.example.ui.features.agro.BaleswariBetelPaddyCooperativeSheet
import com.example.ui.features.transit.CoastalCrutInterdistrictBusRadarSheet
import com.example.ui.features.nature.HorseshoeCrabIntertidalProtectionSheet
import com.example.ui.features.civic.CitizenFeedbackSheet
import com.example.ui.features.emergency.FcmSevereWeatherAlertSheet
import com.example.ui.features.coastal.CoastalMarineAudioRadioSheet
import com.example.ui.features.coastal.CycloneSurgeEvacuationRouterSheet
import com.example.ui.features.agro.BalaramgadiMandiPriceIndexSheet
import com.example.ui.features.heritage.BalasoreTemplePilgrimageCompanionSheet
import com.example.ui.features.emergency.FmMchEmergencyBloodNetworkSheet
import com.example.ui.features.civic.MoBalasoreCivicStormReporterSheet
import com.example.ui.features.coastal.ItrChandipurMissileTrackerSheet
import com.example.ui.features.agro.SubarnarekhaCropAdvisorySheet
import com.example.ui.features.transit.BalasoreSmartTransitTrackerSheet
import com.example.ui.features.education.FakirMohanStudentHubSheet
import com.example.ui.features.heritage.BaleswariCuisineExplorerSheet
import com.example.ui.features.resilience.TpnodlPowerOutageRadarSheet
import com.example.ui.features.coastal.ChandipurBioluminescenceExplorerSheet
import com.example.ui.features.emergency.BalasoreSnakebiteAntiVenomSheet
import com.example.ui.features.artisans.NilagiriDokraCraftGuildSheet
import com.example.ui.features.heritage.BaghaJatinFreedomTrailSheet
import com.example.ui.features.nature.KuldihaEcoCorridorFireRadarSheet
import com.example.ui.features.agro.BhograiCashewGroundnutMandiSheet
import com.example.ui.features.heritage.RaibaniaMedievalFortressVaultSheet
import com.example.ui.features.coastal.BalaramgadiDeepSeaTrawlerFleetSheet
import com.example.ui.features.artisans.NilagiriLacquerBambooGuildSheet
import com.example.ui.features.health.BalasoreDistrictBloodDonorGridSheet
import com.example.ui.features.coastal.SubarnarekhaFloodSluiceTelemetrySheet
import com.example.ui.features.civic.BalasoreYouthWellnessStudyCircleSheet
import com.example.ui.features.maps.BalasoreOfflineMapTileSheet
import com.example.ui.features.coastal.MarineFishermenHighSeasSosSheet
import com.example.ui.features.heritage.BaleswariGiCulinaryTrailSheet
import com.example.ui.features.resilience.MicroWardFloodEvacuationSheet
import com.example.ui.features.nature.KuldihaElephantCorridorSentinelSheet
import com.example.ui.features.civic.StudentCareerLibraryPulseSheet
import com.example.ui.features.resilience.RooftopSolarTpnodlRadarSheet
import com.example.ui.features.coastal.TalasariRedCrabSafariSheet
import com.example.ui.features.nature.PanchalingeswarWaterfallTrekSheet
import com.example.ui.features.heritage.EmamiJagannathRemunaHeritageSheet
import com.example.ui.features.coastal.BalaramgadiFishMandiSheet
import com.example.ui.features.transit.TownAutoFareCalculatorSheet
import com.example.ui.features.civic.WardSanitationVehicleTrackerSheet
import com.example.ui.features.education.BalasoreIndustrialJobApprenticeSheet
import com.example.ui.features.agro.KrushakPacsMandiGrainSheet
import com.example.ui.features.nature.OliveRidleyMarineWildlifeSheet
import com.example.ui.features.health.AiimsDhhOpdBedTrackerSheet
import com.example.ui.features.resilience.OfflineCycloneSafetyToolkitSheet
import com.example.ui.features.coastal.ChandipurVanishingSeaClockSheet
import com.example.ui.features.coastal.CoastalCyclonePreparednessSheet
import com.example.ui.features.heritage.BalasoreDayTripPlannerSheet
import com.example.ui.features.coastal.BalaramgadiSeafoodIndexSheet
import com.example.ui.features.nature.BalasorePhotoSpotsChecklistSheet
import com.example.ui.features.transit.BalasoreLiveTrainStatusSheet
import com.example.ui.features.emergency.BalasoreShelterWidgetManagerSheet
import com.example.data.fcm.FcmManager
import com.example.data.fcm.BalasoreNotificationHelper
import android.content.Intent
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import com.example.data.emergency.EmergencySosManager

import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import com.example.data.sync.SyncManager
import com.example.util.PlayStoreUpdateManager
import com.example.util.UpdateUIState
import com.example.ui.navigation.HashRoute
import com.example.ui.navigation.HashRouter
import com.example.ui.navigation.rememberHashRouter
import com.example.ui.features.profile.ProfileScreen

class MainActivity : ComponentActivity() {

    private val viewModel: BalasoreViewModel by viewModels()
    private var updateManager: PlayStoreUpdateManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        try {
            updateManager = PlayStoreUpdateManager(this)
        } catch (t: Throwable) {
            Log.w("MainActivity", "PlayStoreUpdateManager safe fallback: ${t.message}")
        }

        // Handle notification deep links (Severe Weather & Coastal Flood push clicks)
        handleNotificationIntent(intent)

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            LaunchedEffect(Unit) {
                val appCtx = applicationContext
                kotlinx.coroutines.delay(450L)
                try {
                    viewModel.initDatabase(appCtx)
                } catch (t: Throwable) {
                    Log.e("MainActivity", "Safe initDatabase catch: ${t.message}")
                }

                kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                    // Crash-proof AdMob initialization
                    try {
                        AdMobManager.initialize(appCtx)
                    } catch (t: Throwable) {
                        Log.e("MainActivity", "Safe AdMob init catch: ${t.message}")
                    }

                    // Initialize Firebase Analytics
                    try {
                        com.example.data.analytics.BalasoreAnalytics.initialize(appCtx)
                        com.example.data.analytics.BalasoreAnalytics.trackScreenView(appCtx, "MainActivity", "MainActivity")
                    } catch (t: Throwable) {
                        Log.w("MainActivity", "Analytics initialization notice: ${t.message}")
                    }

                    // Schedule WorkManager periodic data sync, hourly multi-source news syndication, essential data background sync, and cache maintenance
                    try {
                        SyncManager.schedulePeriodicSync(appCtx)
                        SyncManager.scheduleEssentialDataSync(appCtx)
                        SyncManager.triggerImmediateEssentialSync(appCtx)
                        SyncManager.startProactiveBatteryMonitoring(appCtx)
                        com.example.data.sync.HourlyMultiSourceNewsSyndicator.scheduleHourlyAutoPull(appCtx)
                    } catch (t: Throwable) {
                        Log.w("MainActivity", "SyncManager schedulePeriodicSync fallback: ${t.message}")
                    }

                    // Initialize Chandipur High Tide & Weather Local Notification WorkManager
                    try {
                        com.example.data.notification.TideAndWeatherNotificationManager.createNotificationChannels(appCtx)
                        com.example.data.notification.TideAndWeatherNotificationManager.schedulePeriodicTideAndWeatherChecks(appCtx, intervalMinutes = 15)
                        com.example.data.notification.ChandipurTidalTelemetryWorker.schedulePeriodic(appCtx, intervalMinutes = 15)
                        com.example.data.service.ChandipurSafeWalkService.startService(appCtx)
                        com.example.data.service.ChandipurLunarTideService.schedulePreHighTideAlerts(appCtx)
                        com.example.widget.ChandipurSafeWalkWidgetProvider.updateAllWidgets(appCtx)
                        com.example.widget.BalasoreSosWidgetProvider.updateAllWidgets(appCtx)
                        com.example.widget.BalasoreFloodGaugeWidgetProvider.updateAllWidgets(appCtx)
                        com.example.data.notification.DailyNewsDigestManager.createNotificationChannel(appCtx)
                        if (com.example.data.notification.DailyNewsDigestManager.isDigestEnabled(appCtx)) {
                            com.example.data.notification.DailyNewsDigestManager.scheduleDaily8AmDigest(appCtx)
                        }
                    } catch (t: Throwable) {
                        Log.w("MainActivity", "TideAndWeatherNotificationManager init notice: ${t.message}")
                    }

                    // Initialize Firebase Cloud Messaging & Local Notification Channels
                    try {
                        FcmManager.initialize(appCtx)
                        com.example.data.notification.LocalAlertNotificationManager.createNotificationChannels(appCtx)
                    } catch (t: Throwable) {
                        Log.w("MainActivity", "FcmManager & LocalAlert initialization notice: ${t.message}")
                    }

                    // Initialize Google ML Kit Barcode Scanning Module
                    try {
                        com.example.data.pamphlet.BalasoreQrCodeScannerManager.checkAndInstallMlKitModules(appCtx)
                    } catch (t: Throwable) {
                        Log.w("MainActivity", "ML Kit module init fallback: ${t.message}")
                    }
                }
            }
            AppThemeProvider(
                themeMode = uiState.themeMode,
                onThemeModeChange = { viewModel.setThemeMode(it) }
            ) {
                ProvideAppLanguage(
                    currentLanguage = uiState.language,
                    onLanguageChange = { viewModel.setLanguage(it) }
                ) {
                    BalasoreApp(
                        viewModel = viewModel,
                        updateManager = updateManager
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateManager?.checkPendingUpdateCompletion()
    }

    override fun onDestroy() {
        super.onDestroy()
        updateManager?.cleanup()
        try {
            SyncManager.stopProactiveBatteryMonitoring(applicationContext)
        } catch (_: Exception) {}
    }

    private fun handleNotificationIntent(intent: Intent?) {
        if (intent == null) return
        val targetTab = intent.getStringExtra(BalasoreNotificationHelper.EXTRA_TARGET_TAB)
            ?: intent.getStringExtra("target_tab")
        val featureSheet = intent.getStringExtra("open_feature_sheet")

        if (intent.action == "com.example.action.OPEN_CYCLONE_SHELTERS" || featureSheet == "CYCLONE_SHELTERS") {
            viewModel.selectTab(3)
            viewModel.openFeatureSheet(UniqueFeatureSheetType.COASTAL_CYCLONE_SHELTER_NETWORK)
            return
        }

        if (intent.action == "com.example.action.OPEN_CHANDIPUR_TIDES" ||
            intent.action == com.example.widget.ChandipurSafeWalkWidgetProvider.ACTION_OPEN_CHANDIPUR_TIDES ||
            featureSheet == "CHANDIPUR_TIDE" || intent.hasExtra("walk_window_alert") || intent.hasExtra("tide_alert")) {
            viewModel.selectTab(3)
            viewModel.openFeatureSheet(UniqueFeatureSheetType.CHANDIPUR_TIDE_TIMER)
            return
        }

        if (featureSheet == "OPEN_WEATHER" || intent.hasExtra("open_weather_alert_id")) {
            viewModel.selectTab(3)
            viewModel.openFeatureSheet(UniqueFeatureSheetType.OPEN_WEATHER_MAP)
            return
        }

        if (featureSheet == "TRANSPORT_ALERTS" || intent.hasExtra("highlight_transport_number") || targetTab == "TRANSPORT") {
            viewModel.selectTab(3)
            viewModel.openFeatureSheet(UniqueFeatureSheetType.TRANSPORT_ALERTS)
            return
        }

        if (intent.action == "com.example.action.SEARCH_NEWS" || targetTab == "NEWS") {
            viewModel.selectTab(2)
            val category = intent.getStringExtra("news_category")
            if (!category.isNullOrBlank()) {
                viewModel.setNewsCategory(category)
            }
            return
        }

        if (intent.action == "com.example.action.REPORT_CIVIC_ISSUE" || featureSheet == "CITIZEN_CIVIC_EYE") {
            viewModel.openFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
            return
        }

        if (intent.action == com.example.widget.BalasoreSosWidgetProvider.ACTION_TRIGGER_WIDGET_SOS_MESSAGE ||
            intent.getBooleanExtra("trigger_sos_emergency_sms", false)
        ) {
            viewModel.selectTab(4)
            EmergencySosManager.triggerSos(
                context = this,
                customContacts = EmergencySosManager.getSavedSosContacts(this)
            ) { result ->
                Toast.makeText(
                    this,
                    "SOS Alert Dispatched (${result.locationDescription}) to ${result.smsRecipients.size} contacts",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        // Handle App & Web Deep Links (baleshwar360:// and https://baleshwar360.app)
        val dataUri = intent.data
        if (dataUri != null) {
            val pathOrHost = "${dataUri.host ?: ""}/${dataUri.path?.trimStart('/') ?: ""}".lowercase()
            when {
                pathOrHost.contains("news") -> {
                    viewModel.selectTab(2)
                    return
                }
                pathOrHost.contains("feature") -> {
                    val slug = dataUri.lastPathSegment?.uppercase()?.replace("-", "_") ?: ""
                    if (slug.contains("MOVIE_THEATRE", ignoreCase = true) || slug.contains("CINEMA", ignoreCase = true)) {
                        viewModel.openFeatureSheet(UniqueFeatureSheetType.BALASORE_MOVIE_THEATRES)
                        return
                    }
                    val matchedSheet = UniqueFeatureSheetType.entries.firstOrNull {
                        it.name.equals(slug, ignoreCase = true) || slug.contains(it.name, ignoreCase = true)
                    }
                    if (matchedSheet != null) {
                        viewModel.openFeatureSheet(matchedSheet)
                    } else {
                        viewModel.selectTab(0)
                    }
                    return
                }
            }
        }

        if (intent.action == com.example.widget.BalasoreFloodGaugeWidgetProvider.ACTION_OPEN_VOICE_EVACUATION ||
            intent.action == "com.example.action.EMERGENCY_CONTACTS" ||
            targetTab == "ESSENTIALS" ||
            targetTab == "EMERGENCY"
        ) {
            viewModel.selectTab(4)
            return
        }

        when (targetTab) {
            "WEATHER" -> {
                viewModel.selectTab(3)
                viewModel.openFeatureSheet(UniqueFeatureSheetType.FCM_SEVERE_WEATHER_ALERT)
            }
            "NEWS" -> {
                viewModel.selectTab(2)
            }
            "TOURISM" -> {
                viewModel.selectTab(1)
                val spotId = intent.getStringExtra("spot_id")
                when {
                    spotId?.contains("kuldiha", ignoreCase = true) == true -> {
                        viewModel.openFeatureSheet(UniqueFeatureSheetType.KULDIHA_SAFARI_COMPANION)
                    }
                    spotId?.contains("chandipur", ignoreCase = true) == true -> {
                        viewModel.openFeatureSheet(UniqueFeatureSheetType.CHANDIPUR_TIDE_TIMER)
                    }
                    spotId?.contains("remuna", ignoreCase = true) == true -> {
                        viewModel.openFeatureSheet(UniqueFeatureSheetType.REMUNA_PRASAD_ARTISANS)
                    }
                }
            }
            "ESSENTIALS" -> {
                viewModel.selectTab(4)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreApp(
    viewModel: BalasoreViewModel,
    updateManager: PlayStoreUpdateManager? = null
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val updateState = updateManager?.updateState?.collectAsState()?.value ?: UpdateUIState.Idle
    var showSettingsSheet by remember { mutableStateOf(false) }
    var showQuickHubSheet by remember { mutableStateOf(false) }

    val hashRouter = rememberHashRouter(
        initialRoute = HashRoute.fromTabIndex(uiState.selectedTab)
    ) { route ->
        if (uiState.selectedTab != route.tabIndex) {
            viewModel.selectTab(route.tabIndex)
        }
    }

    LaunchedEffect(uiState.selectedTab) {
        if (uiState.selectedTab in listOf(0, 2, 3, 5)) {
            val targetRoute = HashRoute.fromTabIndex(uiState.selectedTab)
            if (hashRouter.currentRoute != targetRoute) {
                hashRouter.navigate(targetRoute)
            }
        }
    }

    // Activity Result Launcher for Google Play In-App Updates
    val updateLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode != Activity.RESULT_OK) {
            Log.w("BalasoreApp", "Update flow failed or cancelled: ${result.resultCode}")
        }
    }

    // Emergency 112 Call Launcher using CALL_PHONE permission
    val emergencyCallLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            EmergencySosManager.makeDirectEmergencyCall(context, "112")
        } else {
            Toast.makeText(
                context,
                if (uiState.language == AppLanguage.ODIA) "କଲ୍ ଅନୁମତି ଆବଶ୍ୟକ। ଡାଏଲର୍ ଖୋଲାଯାଉଛି..." else "CALL_PHONE permission required. Opening dialer for 112...",
                Toast.LENGTH_SHORT
            ).show()
            EmergencySosManager.dialPoliceControlRoom(context, "112")
        }
    }

    val triggerEmergency112Call = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            EmergencySosManager.makeDirectEmergencyCall(context, "112")
        } else {
            emergencyCallLauncher.launch(Manifest.permission.CALL_PHONE)
        }
    }

    // Auto-check for updates on app launch
    LaunchedEffect(updateManager) {
        updateManager?.checkForUpdates(launcher = updateLauncher, autoStartFlexible = true)
    }

    // Analytics tracking for screen views & feature engagements
    LaunchedEffect(uiState.selectedTab) {
        val screenName = when (uiState.selectedTab) {
            0 -> "TourismHotspotsScreen"
            1 -> "SpecialFeaturesScreen"
            2 -> "NewsScreen"
            3 -> "WeatherScreen"
            4 -> "EssentialsScreen"
            5 -> "ProfileScreen"
            else -> "Screen_${uiState.selectedTab}"
        }
        com.example.data.analytics.BalasoreAnalytics.trackScreenView(context, screenName, screenName)
    }

    LaunchedEffect(uiState.activeFeatureSheet) {
        val sheet = uiState.activeFeatureSheet
        if (sheet != null) {
            com.example.data.analytics.BalasoreAnalytics.trackScreenView(context, "Sheet_${sheet.name}", sheet.name)
            com.example.data.analytics.BalasoreAnalytics.trackFeatureEngagement(context, sheet.name, "sheet_opened")
        }
    }

    // System Back Press handling: dismiss active bottom sheet first, then pop router history, then switch back to Explore/Tourism tab
    BackHandler(enabled = showQuickHubSheet || showSettingsSheet || uiState.activeFeatureSheet != null || hashRouter.canGoBack() || uiState.selectedTab != 0) {
        if (showQuickHubSheet) {
            showQuickHubSheet = false
        } else if (showSettingsSheet) {
            showSettingsSheet = false
        } else if (uiState.activeFeatureSheet != null) {
            viewModel.closeFeatureSheet()
        } else if (hashRouter.canGoBack()) {
            hashRouter.pop()
        } else if (uiState.selectedTab != 0) {
            hashRouter.navigate(HashRoute.Tourism)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("balasore_main_scaffold"),
        topBar = {
            val languageProvider = LocalAppLanguage.current
            val activeLang = languageProvider.currentLanguage
            Column(modifier = Modifier.fillMaxWidth()) {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Brand Logo Icon Emblem
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(
                                        Brush.linearGradient(
                                            listOf(OceanBlue, OceanBlueDark, Color(0xFF0F172A))
                                        )
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "🌊",
                                    fontSize = 16.sp
                                )
                            }

                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = AppStrings.appName(activeLang),
                                        style = MaterialTheme.typography.titleLarge.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 16.5.sp,
                                            color = if (com.example.ui.theme.LocalAppTheme.current.isDark) Color(0xFFE0F2FE) else OceanBlueDark
                                        ),
                                        maxLines = 1,
                                        modifier = Modifier.testTag("top_bar_app_title")
                                    )

                                    // Live pulse indicator dot that opens Auto-Update Center
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (uiState.isRefreshing) Color(0xFFE0F2FE) else EmeraldGreen.copy(alpha = 0.12f),
                                        border = BorderStroke(1.dp, if (uiState.isRefreshing) Color(0xFF38BDF8) else EmeraldGreen.copy(alpha = 0.4f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(12.dp))
                                            .clickable { viewModel.openFeatureSheet(UniqueFeatureSheetType.BALASORE_AUTO_UPDATE_CENTER) }
                                            .testTag("top_bar_live_pill")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(if (uiState.isHourlyAutoRefreshEnabled) EmeraldGreen else Color(0xFFEF4444))
                                            )
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text(
                                                text = if (languageProvider.isOdia) "ଲାଇଭ୍" else "LIVE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (uiState.isHourlyAutoRefreshEnabled) EmeraldGreen else Color(0xFFEF4444)
                                            )
                                        }
                                    }
                                }

                                Text(
                                    text = if (activeLang == AppLanguage.ODIA) "ବାଲେଶ୍ୱର • ସ୍ମାର୍ଟ ଉପକୂଳ ସୂଚନା" else "${AppStrings.appName(activeLang)} • ${activeLang.nativeLabel}",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    ),
                                    maxLines = 1,
                                    modifier = Modifier.testTag("top_bar_app_subtitle")
                                )
                            }
                        }
                    },
                    actions = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            // Dynamic State-Managed Language Switcher (Segmented pill: EN | ଓଡ଼ିଆ)
                            LanguageSwitcher(
                                currentLanguage = activeLang,
                                onLanguageChanged = {
                                    BalasoreHapticHelper.vibrateToggle(context)
                                    languageProvider.onLanguageChange(it)
                                },
                                compact = true
                            )

                            // Theme Mode Toggle Button
                            ThemeTogglePill(
                                currentMode = uiState.themeMode,
                                onCycleTheme = {
                                    BalasoreHapticHelper.vibrateToggle(context)
                                    viewModel.cycleThemeMode()
                                }
                            )

                            // Push Notification Alert Center button
                            IconButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.PUSH_NOTIFICATION_CENTER)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.15f))
                                    .testTag("top_bar_push_center_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Push Notification Alerts",
                                    tint = Color(0xFF0284C7),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Offline Mode & Storage Budget Manager button
                            IconButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.OFFLINE_MODE_MANAGER)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0D9488).copy(alpha = 0.15f))
                                    .testTag("top_bar_offline_mode_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDownload,
                                    contentDescription = "Offline Mode & Storage Cache Manager",
                                    tint = Color(0xFF0D9488),
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // User Profile & Authentication Hub button
                            IconButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    hashRouter.navigate(HashRoute.Profile)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f))
                                    .testTag("top_bar_user_profile_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = "User Profile & Cloud Hub",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Administrative Moderator Panel button
                            IconButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.ADMIN_MODERATOR_PANEL)
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF7C3AED).copy(alpha = 0.16f))
                                    .testTag("top_bar_admin_panel_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VerifiedUser,
                                    contentDescription = "Administrative Moderator Panel",
                                    tint = Color(0xFF7C3AED),
                                    modifier = Modifier.size(19.dp)
                                )
                            }

                            // Quick Hub Command Center button
                            IconButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    showQuickHubSheet = true
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f))
                                    .testTag("top_bar_quick_hub_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GridView,
                                    contentDescription = "Quick Hub Utilities",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Application Settings button (opens AppSettingsSheet with Battery-Saving Mode toggle)
                            IconButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    showSettingsSheet = true
                                },
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (uiState.isBatterySavingModeEnabled) Color(0xFFDCFCE7)
                                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    )
                                    .testTag("top_bar_settings_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Application Settings & Battery-Saving Mode",
                                    tint = if (uiState.isBatterySavingModeEnabled) Color(0xFF15803D) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )

                // Persistent Language Selection Bar (English / ଓଡ଼ିଆ) in the Top Navigation Bar
                TopBarPersistentLanguageBar(
                    currentLanguage = activeLang,
                    onLanguageSelected = { selectedLang ->
                        BalasoreHapticHelper.vibrateToggle(context)
                        languageProvider.onLanguageChange(selectedLang)
                    }
                )

                // TIER 1: Rearranged Baleshwar 360° Primary Command Menu Strip (Scrollable & Prioritized)
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("balasore_360_quick_command_strip"),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val quickItems = listOf(
                                Triple(
                                    "quick_strip_360_hub_btn",
                                    if (activeLang == AppLanguage.ODIA) "✨ ୩୬୦° ହବ୍" else "✨ 360° Hub",
                                    Color(0xFF0D9488) to (uiState.selectedTab == 1)
                                ) to {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    viewModel.selectTab(1)
                                },
                                Triple(
                                    "quick_strip_interactive_map_btn",
                                    if (activeLang == AppLanguage.ODIA) "🗺️ ୩୬୦° ମାନଚିତ୍ର" else "🗺️ 360° Map",
                                    Color(0xFF0284C7) to (uiState.activeFeatureSheet == UniqueFeatureSheetType.TOURISM_INTERACTIVE_MAP)
                                ) to {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.TOURISM_INTERACTIVE_MAP)
                                },
                                Triple(
                                    "quick_strip_trending_news_btn",
                                    if (activeLang == AppLanguage.ODIA) "🔥 ଟ୍ରେଣ୍ଡିଂ" else "🔥 Trending",
                                    Color(0xFF0284C7) to (uiState.selectedTab == 2 && uiState.selectedNewsCategory.equals("Trending", ignoreCase = true))
                                ) to {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    viewModel.selectTab(2)
                                    viewModel.setNewsCategory("Trending")
                                },
                                Triple(
                                    "quick_strip_community_forum_btn",
                                    if (activeLang == AppLanguage.ODIA) "💬 ଫୋରମ୍" else "💬 Forum",
                                    Color(0xFF7C3AED) to (uiState.activeFeatureSheet == UniqueFeatureSheetType.COMMUNITY_FORUM)
                                ) to {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.COMMUNITY_FORUM)
                                },
                                Triple(
                                    "quick_strip_civic_reports_btn",
                                    if (activeLang == AppLanguage.ODIA) "📸 ସିଭିକ୍ ଗ୍ୟାଲେରୀ" else "📸 Civic Gallery",
                                    Color(0xFFEA580C) to (uiState.activeFeatureSheet == UniqueFeatureSheetType.MY_CIVIC_REPORTS)
                                ) to {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.MY_CIVIC_REPORTS)
                                },
                                Triple(
                                    "quick_strip_local_bus_tracker_btn",
                                    if (activeLang == AppLanguage.ODIA) "🚌 ବସ୍ ଟ୍ରାକର" else "🚌 Bus Live",
                                    Color(0xFF0284C7) to (uiState.activeFeatureSheet == UniqueFeatureSheetType.LOCAL_BUS_TRACKER)
                                ) to {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.LOCAL_BUS_TRACKER)
                                },
                                Triple(
                                    "quick_strip_sos_essentials_btn",
                                    if (activeLang == AppLanguage.ODIA) "🚨 SOS ଓ ବସ୍" else "🚨 Quick SOS & Bus",
                                    Color(0xFFDC2626) to (uiState.selectedTab == 4)
                                ) to {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    viewModel.selectTab(4)
                                },
                                Triple(
                                    "quick_strip_all_menus_btn",
                                    if (activeLang == AppLanguage.ODIA) "☰ ସମସ୍ତ ମେନୁ" else "☰ All Menus",
                                    Color(0xFF4F46E5) to showQuickHubSheet
                                ) to {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    showQuickHubSheet = true
                                }
                            )

                            quickItems.forEach { (meta, onClickAction) ->
                                val (tag, label, colorAndActive) = meta
                                val (accentColor, isActive) = colorAndActive
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { onClickAction() }
                                        .testTag(tag),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isActive) accentColor else accentColor.copy(alpha = 0.12f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isActive) accentColor else accentColor.copy(alpha = 0.35f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isActive) Color.White else accentColor,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // TIER 2: Context-Aware Dynamic Sub-Menu Bar for the Active Section
                        val contextualSubMenus: List<Triple<String, String, () -> Unit>> = when (uiState.selectedTab) {
                            0 -> listOf(
                                Triple("submenu_tourism_all", if (activeLang == AppLanguage.ODIA) "🌐 ସମସ୍ତ ସ୍ଥଳୀ" else "🌐 All Spots") {
                                    viewModel.setHotspotCategory("All")
                                },
                                Triple("submenu_tourism_beach", if (activeLang == AppLanguage.ODIA) "🏖️ ବେଳାଭୂମି" else "🏖️ Beaches") {
                                    viewModel.setHotspotCategory("Beach")
                                },
                                Triple("submenu_tourism_temple", if (activeLang == AppLanguage.ODIA) "🛕 ମନ୍ଦିର" else "🛕 Temples") {
                                    viewModel.setHotspotCategory("Temple")
                                },
                                Triple("submenu_tourism_wildlife", if (activeLang == AppLanguage.ODIA) "🐘 ବନ୍ୟପ୍ରାଣୀ" else "🐘 Wildlife") {
                                    viewModel.setHotspotCategory("Wildlife")
                                },
                                Triple("submenu_tourism_heritage", if (activeLang == AppLanguage.ODIA) "🏰 ଐତିହ୍ୟ" else "🏰 Heritage") {
                                    viewModel.setHotspotCategory("Heritage")
                                },
                                Triple("submenu_tourism_map", if (activeLang == AppLanguage.ODIA) "🗺️ ପର୍ଯ୍ୟଟନ ମାନଚିତ୍ର" else "🗺️ Hotspot Map") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.TOURISM_INTERACTIVE_MAP)
                                },
                                Triple("submenu_tourism_ar", if (activeLang == AppLanguage.ODIA) "🥽 AR କ୍ୟାମେରା" else "🥽 AR Wayfinder") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.AR_LANDMARK_CAMERA)
                                }
                            )
                            1 -> listOf(
                                Triple("submenu_hub_tide", if (activeLang == AppLanguage.ODIA) "🌊 ଚାନ୍ଦିପୁର ଜୁଆର" else "🌊 Chandipur Tide") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.CHANDIPUR_TIDE_TIMER)
                                },
                                Triple("submenu_hub_civic", if (activeLang == AppLanguage.ODIA) "🏛️ ସମସ୍ୟା ଜଣାନ୍ତୁ" else "🏛️ Civic Eye") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
                                },
                                Triple("submenu_hub_bus", if (activeLang == AppLanguage.ODIA) "🚌 ବସ୍ ଓ ଭଡ଼ା" else "🚌 Bus & Fare") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.LOCAL_BUS_TRACKER)
                                },
                                Triple("submenu_hub_train", if (activeLang == AppLanguage.ODIA) "🚆 ଲାଇଭ୍ ଟ୍ରେନ୍" else "🚆 Train Status") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.BALASORE_LIVE_TRAIN_STATUS)
                                },
                                Triple("submenu_hub_culture", if (activeLang == AppLanguage.ODIA) "🎭 ଓଡ଼ିଆ ସଂସ୍କୃତି" else "🎭 Odia Culture") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.ODIA_CULTURE_SECTION)
                                },
                                Triple("submenu_hub_qr", if (activeLang == AppLanguage.ODIA) "📷 QR ସ୍କାନର୍" else "📷 QR Scanner") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.OFFICIAL_TOURISM_EVENT_POSTER_SCANNER)
                                }
                            )
                            2 -> listOf(
                                Triple("submenu_news_all", if (activeLang == AppLanguage.ODIA) "📰 ସମସ୍ତ ଖବର" else "📰 All News") {
                                    viewModel.setNewsCategory("All")
                                },
                                Triple("submenu_news_gov_notices", if (activeLang == AppLanguage.ODIA) "🏛️ ସରକାରୀ ସୂଚନା" else "🏛️ Government Notices") {
                                    viewModel.setNewsCategory("Government Notices")
                                },
                                Triple("submenu_news_local_events", if (activeLang == AppLanguage.ODIA) "🎭 ସ୍ଥାନୀୟ କାର୍ଯ୍ୟକ୍ରମ" else "🎭 Local Events") {
                                    viewModel.setNewsCategory("Local Events")
                                },
                                Triple("submenu_news_community_highlights", if (activeLang == AppLanguage.ODIA) "🌟 ଗୋଷ୍ଠୀ ବିଶେଷ" else "🌟 Community Highlights") {
                                    viewModel.setNewsCategory("Community Highlights")
                                },
                                Triple("submenu_news_local", if (activeLang == AppLanguage.ODIA) "🏙️ ସ୍ଥାନୀୟ" else "🏙️ Local") {
                                    viewModel.setNewsCategory("Local")
                                },
                                Triple("submenu_news_defense", if (activeLang == AppLanguage.ODIA) "🛡️ ପ୍ରତିରକ୍ଷା" else "🛡️ Defense") {
                                    viewModel.setNewsCategory("Defense")
                                },
                                Triple("submenu_news_tourism", if (activeLang == AppLanguage.ODIA) "🏖️ ପର୍ଯ୍ୟଟନ" else "🏖️ Tourism") {
                                    viewModel.setNewsCategory("Tourism")
                                },
                                Triple("submenu_news_saved", if (activeLang == AppLanguage.ODIA) "🔖 ସଂରକ୍ଷିତ" else "🔖 Saved") {
                                    viewModel.setNewsCategory("Saved Bookmarks")
                                },
                                Triple("submenu_news_moderate", if (activeLang == AppLanguage.ODIA) "🛡️ ମଡରେସନ୍" else "🛡️ Moderate") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.ADMIN_MODERATOR_PANEL)
                                }
                            )
                            3 -> listOf(
                                Triple("submenu_wx_openweather", if (activeLang == AppLanguage.ODIA) "🌦️ ଲାଇଭ୍ ପାଣିପାଗ" else "🌦️ OpenWeather & AQI") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.OPEN_WEATHER_MAP)
                                },
                                Triple("submenu_wx_tide", if (activeLang == AppLanguage.ODIA) "🌊 ଜୁଆର ଘଣ୍ଟା" else "🌊 Tide Clock") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.CHANDIPUR_TIDE_TIMER)
                                },
                                Triple("submenu_wx_radar", if (activeLang == AppLanguage.ODIA) "📡 ରାଡାର୍ ମାନଚିତ୍ର" else "📡 Weather Radar") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.COASTAL_WEATHER_RADAR_MAP)
                                },
                                Triple("submenu_wx_shelters", if (activeLang == AppLanguage.ODIA) "🏠 ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ" else "🏠 Cyclone Shelters") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.COASTAL_CYCLONE_SHELTER_NETWORK)
                                },
                                Triple("submenu_wx_flood", if (activeLang == AppLanguage.ODIA) "🌊 ବନ୍ୟା ସୂଚନା" else "🌊 River Flood Gauge") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.RIVER_FLOOD_TELEMETRY)
                                }
                            )
                            4 -> listOf(
                                Triple("submenu_sos_map", if (activeLang == AppLanguage.ODIA) "🏥 ଜରୁରୀ ମାନଚିତ୍ର" else "🏥 Emergency Map") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.NEAREST_EMERGENCY_SERVICES_MAP)
                                },
                                Triple("submenu_sos_bus", if (activeLang == AppLanguage.ODIA) "🚌 ବସ୍ ଭଡ଼ା" else "🚌 Bus Fare Calc") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.LOCAL_BUS_TRACKER)
                                },
                                Triple("submenu_sos_train", if (activeLang == AppLanguage.ODIA) "🚆 ରେଳ ଷ୍ଟାଟସ୍" else "🚆 Train Radar") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.BALASORE_LIVE_TRAIN_STATUS)
                                },
                                Triple("submenu_sos_blood", if (activeLang == AppLanguage.ODIA) "🩸 ରକ୍ତ ଭଣ୍ଡାର" else "🩸 Blood & Hospitals") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.BLOOD_AND_DIALYSIS_DIRECTORY)
                                },
                                Triple("submenu_sos_offline", if (activeLang == AppLanguage.ODIA) "📥 ଅଫଲାଇନ୍ ହବ୍" else "📥 Offline Cache") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.OFFLINE_MODE_MANAGER)
                                }
                            )
                            else -> listOf(
                                Triple("submenu_profile_civic", if (activeLang == AppLanguage.ODIA) "📸 ସିଭିକ୍ ଆଇ" else "📸 Civic Eye") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
                                },
                                Triple("submenu_profile_reports", if (activeLang == AppLanguage.ODIA) "📋 ମୋ ଅଭିଯୋଗ" else "📋 My Reports") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.MY_CIVIC_REPORTS)
                                },
                                Triple("submenu_profile_offline", if (activeLang == AppLanguage.ODIA) "📥 ଅଫଲାଇନ୍ ପ୍ୟାକ୍" else "📥 Offline Pack") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.OFFLINE_MODE_MANAGER)
                                },
                                Triple("submenu_profile_alerts", if (activeLang == AppLanguage.ODIA) "🔔 ଆଲର୍ଟ ସେଣ୍ଟର୍" else "🔔 Alert Center") {
                                    viewModel.openFeatureSheet(UniqueFeatureSheetType.PUSH_NOTIFICATION_CENTER)
                                },
                                Triple("submenu_profile_settings", if (activeLang == AppLanguage.ODIA) "⚙️ ସେଟିଂସ୍" else "⚙️ Settings & Theme") {
                                    showSettingsSheet = true
                                }
                            )
                        }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .horizontalScroll(rememberScrollState())
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                                .testTag("balasore_contextual_submenu_bar"),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (activeLang == AppLanguage.ODIA) "ସବ୍-ମେନୁ:" else "Sub-Menu:",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 2.dp)
                            )
                            contextualSubMenus.forEach { (subTag, subLabel, subAction) ->
                                Surface(
                                    onClick = {
                                        BalasoreHapticHelper.vibrateButtonClick(context)
                                        subAction()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = MaterialTheme.colorScheme.surface,
                                    border = BorderStroke(0.8.dp, MaterialTheme.colorScheme.outlineVariant),
                                    modifier = Modifier.testTag(subTag)
                                ) {
                                    Text(
                                        text = subLabel,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                modifier = Modifier.testTag("balasore_bottom_nav"),
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                HashRoute.bottomNavRoutes.forEach { route ->
                    val isSelected = uiState.selectedTab == route.tabIndex
                    val tag = when (route) {
                        HashRoute.News -> "bottom_nav_news"
                        HashRoute.Weather -> "bottom_nav_weather"
                        HashRoute.Tourism -> "bottom_nav_tourism"
                        HashRoute.Profile -> "bottom_nav_profile"
                    }
                    NavigationBarItem(
                        modifier = Modifier.testTag(tag),
                        selected = isSelected,
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            hashRouter.navigate(route)
                        },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) route.selectedIcon else route.unselectedIcon,
                                contentDescription = route.titleEnglish
                            )
                        },
                        label = {
                            Text(
                                text = route.localizedTitle(uiState.language),
                                fontSize = 10.5.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    )
                }

                // 5th Bottom Navigation Destination: 360° Smart Hub & All Sub-Menus
                val isHubSelected = uiState.selectedTab == 1
                NavigationBarItem(
                    modifier = Modifier.testTag("bottom_nav_360_hub"),
                    selected = isHubSelected,
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        viewModel.selectTab(1)
                    },
                    icon = {
                        Icon(
                            imageVector = if (isHubSelected) Icons.Default.AutoAwesome else Icons.Outlined.AutoAwesome,
                            contentDescription = "360 Hub"
                        )
                    },
                    label = {
                        Text(
                            text = if (uiState.language == AppLanguage.ODIA) "୩୬୦° ହବ୍" else "360° Hub",
                            fontSize = 10.5.sp,
                            maxLines = 1
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                )
            }
        },
        floatingActionButton = {
            // Camera launcher for quick 'Report Civic Issue' photo capture + Firestore/Room geo-tagged upload
            val civicQuickCameraLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.TakePicturePreview()
            ) { bitmap: android.graphics.Bitmap? ->
                val activeZone = "Ward 14 - OT Road / Station Square (21.4942°N, 86.9317°E)"
                val reportTitle = if (bitmap != null) {
                    "Geo-Tagged Camera Report: Pothole & Drainage Hazard ($activeZone)"
                } else {
                    "Quick Civic Report: Road / Drainage Issue ($activeZone)"
                }
                viewModel.submitCivicReport(
                    title = reportTitle,
                    category = "Road Pothole",
                    wardLocation = activeZone,
                    description = "Captured via Floating 'Report Civic Issue' Camera in High-Incident Zone (OT Road / Station Square).",
                    urgency = "HIGH",
                    hasPhotoAttached = true
                )
                Toast.makeText(
                    context,
                    "📸 Geo-tagged photo uploaded to Firestore 'Civic Reports' (Status: Pending)!",
                    Toast.LENGTH_LONG
                ).show()
                viewModel.openFeatureSheet(UniqueFeatureSheetType.MY_CIVIC_REPORTS)
            }

            // Pulse animation for floating 'Report Civic Issue' button in high-incident zones
            val civicPulseTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "civic_fab_pulse")
            val civicPulseScale by civicPulseTransition.animateFloat(
                initialValue = 1.0f,
                targetValue = 1.08f,
                animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                    animation = androidx.compose.animation.core.tween(
                        durationMillis = 850,
                        easing = androidx.compose.animation.core.FastOutSlowInEasing
                    ),
                    repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                ),
                label = "civic_fab_scale"
            )

            if (uiState.selectedTab == 0) {
                // Home Screen: Emergency 112 1-tap Call FAB + Pulsing 'Report Civic Issue' Camera FAB + Ask AI FAB
                val fabInfiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "emergency_fab_pulse")
                val fabPulseScale by fabInfiniteTransition.animateFloat(
                    initialValue = 1.0f,
                    targetValue = 1.05f,
                    animationSpec = androidx.compose.animation.core.infiniteRepeatable(
                        animation = androidx.compose.animation.core.tween(
                            durationMillis = 1100,
                            easing = androidx.compose.animation.core.FastOutSlowInEasing
                        ),
                        repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
                    ),
                    label = "emergency_fab_scale"
                )
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            try {
                                civicQuickCameraLauncher.launch(null)
                            } catch (_: Exception) {
                                viewModel.openFeatureSheet(UniqueFeatureSheetType.MY_CIVIC_REPORTS)
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Report Civic Issue Camera",
                                tint = Color.White
                            )
                        },
                        text = {
                            Text(
                                text = if (uiState.language == AppLanguage.ODIA)
                                    "📸 ସମସ୍ୟା ଜଣାନ୍ତୁ (ହାଇ-ଜୋନ୍)"
                                else
                                    "Report Civic Issue • High-Zone",
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        },
                        containerColor = Color(0xFFEA580C),
                        contentColor = Color.White,
                        modifier = Modifier
                            .scale(civicPulseScale)
                            .testTag("fab_report_civic_issue")
                    )

                    ExtendedFloatingActionButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateEmergencySos(context)
                            triggerEmergency112Call()
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Baleshwar Emergency Helpline 112",
                                tint = Color.White
                            )
                        },
                        text = {
                            Text(
                                text = when (uiState.language) {
                                    AppLanguage.ODIA -> "୧୧୨ ଜରୁରୀକାଳୀନ କଲ୍"
                                    AppLanguage.HINDI -> "११२ आपातकालीन कॉल"
                                    AppLanguage.BENGALI -> "১১২ জরুরি কল"
                                    AppLanguage.TELUGU -> "112 అత్యవసర కాల్"
                                    AppLanguage.TAMIL -> "112 அவசர அழைப்பு"
                                    AppLanguage.SANTALI -> "᱑᱑᱒ ᱟᱹᱰᱤ ᱞᱟᱹᱠᱛᱤ ᱠᱚᱞ"
                                    else -> "Call 112 Emergency"
                                },
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 13.sp
                            )
                        },
                        containerColor = Color(0xFFDC2626),
                        contentColor = Color.White,
                        modifier = Modifier
                            .scale(fabPulseScale)
                            .testTag("fab_emergency_call_112")
                    )

                    ExtendedFloatingActionButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            viewModel.openFeatureSheet(UniqueFeatureSheetType.ASK_BALASORE_AI)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Ask Baleshwar AI",
                                tint = Color.White
                            )
                        },
                        text = {
                            Text(
                                text = AppStrings.askAiButton(uiState.language),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        },
                        containerColor = Color(0xFF0284C7),
                        modifier = Modifier.testTag("fab_ask_balasore_ai")
                    )
                }
            } else {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            viewModel.openFeatureSheet(UniqueFeatureSheetType.MY_CIVIC_REPORTS)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = "Report Civic Issue",
                                tint = Color.White
                            )
                        },
                        text = {
                            Text(
                                text = if (uiState.language == AppLanguage.ODIA) "ସମସ୍ୟା ଜଣାନ୍ତୁ" else "Report Civic Issue",
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        },
                        containerColor = Color(0xFFEA580C),
                        modifier = Modifier
                            .scale(civicPulseScale)
                            .testTag("fab_report_civic_issue")
                    )

                    ExtendedFloatingActionButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            viewModel.openFeatureSheet(UniqueFeatureSheetType.ASK_BALASORE_AI)
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Ask Baleshwar AI",
                                tint = Color.White
                            )
                        },
                        text = {
                            Text(
                                text = AppStrings.askAiButton(uiState.language),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        },
                        containerColor = Color(0xFF0284C7),
                        modifier = Modifier.testTag("fab_ask_balasore_ai")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Prominent banner when flexible background update has finished downloading
            if (updateState is UpdateUIState.Downloaded) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SystemUpdate,
                                contentDescription = null,
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "Update Ready to Install",
                                    style = MaterialTheme.typography.labelLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                )
                                Text(
                                    text = "Restart app to apply the newest features.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = Color(0xFF15803D),
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                        Button(
                            onClick = { updateManager?.completeUpdate() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF166534)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("Restart", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (uiState.selectedTab) {
                        0 -> TourismScreen(
                            hotspots = uiState.hotspots,
                            templeRitualInfo = uiState.templeRitualInfo,
                            literaryTrailPoints = uiState.literaryTrailPoints,
                            dailyIdiom = uiState.dailyIdiom,
                            selectedCategory = uiState.selectedHotspotCategory,
                            searchQuery = uiState.hotspotSearchQuery,
                            language = uiState.language,
                            favoriteIds = uiState.favoriteHotspotIds,
                            weather = uiState.weather,
                            forecastDays = uiState.forecastDays,
                            weatherAlerts = uiState.weatherAlerts,
                            isFahrenheit = uiState.isFahrenheit,
                            selectedForecastIndex = uiState.selectedForecastDayIndex,
                            onCategorySelected = { viewModel.setHotspotCategory(it) },
                            onSearchChanged = { viewModel.setHotspotSearchQuery(it) },
                            onToggleFavorite = { viewModel.toggleFavoriteHotspot(it) },
                            onToggleTempUnit = { viewModel.toggleTemperatureUnit() },
                            onSelectForecastDay = { viewModel.selectForecastDay(it) },
                            onAcknowledgeAlert = {
                                BalasoreHapticHelper.vibrateEmergencyAlertAck(context)
                                viewModel.acknowledgeAlert(it)
                            },
                            onNavigateToWeather = { viewModel.selectTab(3) },
                            onNavigateToSpecialFeatures = { viewModel.selectTab(1) },
                            onOpenFeatureSheet = { viewModel.openFeatureSheet(it) },
                            itineraryItems = uiState.itineraryItems,
                            liveEmergencyAlerts = uiState.liveEmergencyAlerts,
                            dismissedAlertIds = uiState.dismissedAlertIds,
                            onDismissAlert = {
                                BalasoreHapticHelper.vibrateEmergencyAlertAck(context)
                                viewModel.dismissEmergencyAlert(it)
                            },
                            onAddToItinerary = { spot, day, time, notes ->
                                viewModel.addToItinerary(spot, day, time, notes)
                            },
                            onRemoveFromItinerary = { viewModel.removeFromItinerary(it) },
                            onClearItinerary = { viewModel.clearItinerary() },
                            dailyPulse = uiState.dailyPulse,
                            onRefreshDailyPulse = { viewModel.refreshDailyPulse() },
                            onRefreshWeather = { viewModel.fetchRealTimeWeather() },
                            openWeatherCurrent = uiState.openWeatherCurrent,
                            openWeatherThreeDayForecast = uiState.openWeatherThreeDayForecast,
                            openWeatherSevenDayForecast = uiState.openWeatherSevenDayForecast,
                            isOpenWeatherLoading = uiState.isOpenWeatherLoading,
                            onRefreshOpenWeather = { viewModel.fetchOpenWeatherData(true) }
                        )
                        1 -> SpecialFeaturesScreen(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            newsArticles = uiState.newsArticles,
                            onRefreshDailyPulse = { viewModel.refreshDailyPulse() },
                            onOpenFeatureSheet = { viewModel.openFeatureSheet(it) },
                            onLanguageSelected = { viewModel.setLanguage(it) }
                        )
                        2 -> NewsScreen(
                            articles = uiState.newsArticles,
                            filteredArticles = uiState.filteredNewsArticles,
                            selectedTab = uiState.selectedNewsTab,
                            onTabSelected = { viewModel.selectNewsTab(it) },
                            riverGauges = uiState.riverGauges,
                            cycloneShelters = uiState.cycloneShelters,
                            selectedCategory = uiState.selectedNewsCategory,
                            searchQuery = uiState.newsSearchQuery,
                            language = uiState.language,
                            bookmarkedIds = uiState.bookmarkedIds,
                            isRefreshing = uiState.isRefreshing,
                            onCategorySelected = { viewModel.setNewsCategory(it) },
                            onSearchQueryChanged = { viewModel.setNewsSearchQuery(it) },
                            onToggleBookmark = { viewModel.toggleBookmark(it) },
                            onRefresh = { viewModel.refreshAll() },
                            onOpenFeatureSheet = { viewModel.openFeatureSheet(it) },
                            themeMode = uiState.themeMode,
                            onToggleNightMode = { viewModel.toggleHighContrastNightMode() },
                            isAutoUpdateEnabled = uiState.isNewsAutoUpdateEnabled,
                            autoUpdateIntervalSeconds = uiState.newsAutoUpdateIntervalSeconds,
                            onToggleAutoUpdate = { viewModel.setNewsAutoUpdateEnabled(it) },
                            onChangeAutoUpdateInterval = { viewModel.setNewsAutoUpdateInterval(it) },
                            savedBookmarks = uiState.savedBookmarks,
                            onRemoveSavedBookmark = { viewModel.removeSavedBookmark(it) },
                            onCallEmergency = { number ->
                                val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$number"))
                                context.startActivity(dialIntent)
                            },
                            recentSearchQueries = uiState.recentNewsSearchQueries,
                            onSaveSearchQuery = { viewModel.saveNewsSearchQuery(it) },
                            onDeleteSearchQuery = { viewModel.deleteNewsSearchQuery(it) },
                            onClearSearchQueries = { viewModel.clearAllNewsSearchQueries() },
                            communityNewsSubmissions = uiState.communityNewsSubmissions,
                            onSubmitCommunityNewsStory = { title, odiaTitle, snippet, content, category, ward, author, contact, sourceType, photoUri, isBreaking ->
                                viewModel.submitCommunityNewsStory(
                                    title = title,
                                    odiaTitle = odiaTitle,
                                    snippet = snippet,
                                    content = content,
                                    category = category,
                                    locationWard = ward,
                                    authorName = author,
                                    authorContact = contact,
                                    sourceVerificationType = sourceType,
                                    photoUri = photoUri,
                                    isBreakingCandidate = isBreaking
                                )
                            },
                            onApproveCommunityNewsSubmission = { subId, note, eTitle, eOdia, eSnippet, eContent, eCat ->
                                viewModel.approveCommunityNewsSubmission(
                                    submissionId = subId,
                                    moderatorNote = note,
                                    editedTitle = eTitle,
                                    editedOdiaTitle = eOdia,
                                    editedSnippet = eSnippet,
                                    editedContent = eContent,
                                    editedCategory = eCat
                                )
                            },
                            onRejectCommunityNewsSubmission = { subId, note ->
                                viewModel.rejectCommunityNewsSubmission(subId, note)
                            },
                            onRequestRevisionCommunityNewsSubmission = { subId, note ->
                                viewModel.requestRevisionCommunityNewsSubmission(subId, note)
                            },
                            onResubmitCommunityNewsStory = { subId, title, odiaTitle, snippet, content, category, ward ->
                                viewModel.resubmitCommunityNewsStory(
                                    submissionId = subId,
                                    title = title,
                                    odiaTitle = odiaTitle,
                                    snippet = snippet,
                                    content = content,
                                    category = category,
                                    locationWard = ward
                                )
                            },
                            onApproveAllPendingCommunityNews = {
                                viewModel.approveAllPendingCommunityNews()
                            },
                            onDeleteCommunityNewsSubmission = { subId ->
                                viewModel.deleteCommunityNewsSubmission(subId)
                            }
                        )
                        3 -> WeatherScreen(
                            weather = uiState.weather,
                            forecastDays = uiState.forecastDays,
                            hourlyForecast = uiState.hourlyForecast,
                            isWeatherLoading = uiState.isWeatherLoading,
                            isFahrenheit = uiState.isFahrenheit,
                            onRefreshWeather = { viewModel.fetchRealTimeWeather() },
                            onToggleUnit = { viewModel.toggleTemperatureUnit() },
                            tidalClock = uiState.tidalClock,
                            drdoAdvisories = uiState.drdoAdvisories,
                            language = uiState.language,
                            lastKnownTide = uiState.lastKnownTideForecast,
                            openWeatherSevenDayForecast = uiState.openWeatherSevenDayForecast,
                            openWeatherCurrent = uiState.openWeatherCurrent,
                            isOpenWeatherLoading = uiState.isOpenWeatherLoading,
                            onRefreshOpenWeather = { viewModel.fetchOpenWeatherData(true) },
                            onOpenFeatureSheet = { viewModel.openFeatureSheet(it) }
                        )
                        4 -> EssentialsScreen(
                            emergencyContacts = uiState.emergencyContacts,
                            transitList = uiState.transitList,
                            seafoodCatches = uiState.seafoodCatches,
                            language = uiState.language,
                            updateState = updateState,
                            themeMode = uiState.themeMode,
                            onThemeModeChange = { viewModel.setThemeMode(it) },
                            isWatermarkEnabled = uiState.isWatermarkEnabled,
                            watermarkStyle = uiState.watermarkStyle,
                            watermarkOpacity = uiState.watermarkOpacity,
                            onWatermarkToggle = { viewModel.setWatermarkEnabled(it) },
                            onWatermarkStyleChange = { viewModel.setWatermarkStyle(it) },
                            onWatermarkOpacityChange = { viewModel.setWatermarkOpacity(it) },
                            isHourlyAutoRefreshEnabled = uiState.isHourlyAutoRefreshEnabled,
                            lastHourlyRefreshTimestamp = uiState.lastHourlyRefreshTimestamp,
                            nextHourlyRefreshMinutesRemaining = uiState.nextHourlyRefreshMinutesRemaining,
                            autoRefreshCycleCount = uiState.autoRefreshCycleCount,
                            onHourlyAutoRefreshToggle = { viewModel.setHourlyAutoRefreshEnabled(it) },
                            onTriggerManualHourlyRefresh = {
                                viewModel.triggerHourlyAutoRefresh()
                                // Also trigger WorkManager background sync cache
                                SyncManager.triggerImmediateSync(context)
                            },
                            onCheckForUpdates = { updateManager?.checkForUpdates(updateLauncher, autoStartFlexible = false) },
                            onTriggerUpdate = { updateManager?.startUpdate(updateLauncher) },
                            onCompleteUpdate = { updateManager?.completeUpdate() },
                            onLanguageChange = { viewModel.setLanguage(it) },
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onOpenFeatureSheet = { viewModel.openFeatureSheet(it) },
                            savedEmergencyContactIds = uiState.savedEmergencyContactIds,
                            onToggleFavoriteContact = { viewModel.toggleBookmarkEmergencyContact(it) },
                            isBatterySavingModeEnabled = uiState.isBatterySavingModeEnabled,
                            onBatterySavingModeToggle = { viewModel.setBatterySavingModeEnabled(it) }
                        )
                        5 -> ProfileScreen(
                            language = uiState.language,
                            onOpenHotspot = { spotId ->
                                viewModel.setHotspotSearchQuery(spotId)
                                hashRouter.navigate(HashRoute.Tourism)
                            },
                            onOpenAdminPanel = {
                                showSettingsSheet = true
                            },
                            onNavigateToTab = { targetTab ->
                                hashRouter.navigate(HashRoute.fromTabIndex(targetTab))
                            },
                            onOpenCivicEye = {
                                viewModel.openFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
                            }
                        )
                    }

                    // Full App Watermark Overlay (Displays App Name & Logo across entire app)
                    AppWatermarkOverlay(
                        enabled = uiState.isWatermarkEnabled,
                        style = uiState.watermarkStyle,
                        opacity = uiState.watermarkOpacity
                    )

                    // Persistent Emergency SOS Quick-Action Overlay with 1-Tap 112 Call, GPS SMS, and Voice Trigger
                    PersistentEmergencySosOverlay(
                        language = uiState.language,
                        onTriggerSosCall = { triggerEmergency112Call() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 16.dp)
                    )
                }
            }

            // Active Unique Feature Modal Sheet
            when (uiState.activeFeatureSheet) {
                UniqueFeatureSheetType.HORSESHOE_CRAB -> {
                    HorseshoeCrabSheet(
                        sightings = uiState.horseshoeCrabSightings,
                        guidelines = uiState.intertidalEcoGuidelines,
                        onLogSighting = { reporter, sector, species, count ->
                            viewModel.logHorseshoeCrabSighting(reporter, sector, species, count)
                        },
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.DEFENSE_TRAIL -> {
                    DefenseTrailSheet(
                        milestones = uiState.defenseMilestones,
                        exclusionZone = uiState.maritimeExclusionZone,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.REMUNA_PRASAD_ARTISANS -> {
                    RemunaPrasadArtisansSheet(
                        prasadStatus = uiState.prasadStatus,
                        artisans = uiState.balasoreArtisans,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.CYCLONE_RESILIENCE -> {
                    CycloneResilienceSheet(
                        shelters = uiState.detailedCycloneShelters,
                        kitItems = uiState.emergencyKitItems,
                        onToggleKitItem = { viewModel.toggleEmergencyKitItem(it) },
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.HARBOR_CATCH_RATES -> {
                    HarborCatchRatesSheet(
                        catchRates = uiState.harborCatchRates,
                        landingBells = uiState.harborLandingBells,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.ELEPHANT_PASSPORT -> {
                    ElephantPassportSheet(
                        corridorAlert = uiState.elephantCorridorAlert,
                        passportStamps = uiState.ecoPassportStamps,
                        onToggleStamp = { viewModel.toggleEcoPassportStamp(it) },
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.CHANDIPUR_TIDE_TIMER -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        ChandipurTideTimerSheet(
                            onClose = { viewModel.closeFeatureSheet() },
                            lastKnownTide = uiState.lastKnownTideForecast,
                            language = uiState.language,
                            onOpenOfflineDashboard = {
                                viewModel.openFeatureSheet(UniqueFeatureSheetType.OFFLINE_TIDE_DASHBOARD)
                            }
                        )
                    }
                }
                UniqueFeatureSheetType.OFFLINE_TIDE_DASHBOARD -> {
                    ChandipurOfflineTideDashboardSheet(
                        language = uiState.language,
                        tideSchedule = uiState.offlineTideSchedule,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.CYCLONE_SHELTER_FINDER -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        CycloneShelterFinderSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALASORE_FOOD_TRAIL -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BalasoreFoodTrailSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BILINGUAL_HERITAGE_AUDIO,
                UniqueFeatureSheetType.HISTORICAL_LANDMARK_AUDIO_GUIDE -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        com.example.ui.features.heritage.BalasoreAudioGuideSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.TRANSIT_FARE_ESTIMATOR -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        TransitFareEstimatorSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.KULDIHA_SAFARI_COMPANION -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        KuldihaSafariCompanionSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.TRAVEL_JOURNAL -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        TravelJournalSheet(
                            journalRepo = viewModel.travelJournalRepo,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BLOOD_AND_DIALYSIS_DIRECTORY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BloodAndDialysisDirectorySheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.KASAFAL_RED_CRABS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        KasafalRedCrabsSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.NILAGIRI_STONE_SABAI -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        NilagiriStoneSabaiSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALARAMGADI_ESTUARY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BalaramgadiEstuarySheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.COLONIAL_HERITAGE_WALK -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        ColonialHeritageWalkSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.TALASARI_AUCTION_MONITOR -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        TalasariAuctionMonitorSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALESWARIYA_DIALECT_PROVERBS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BaleswariyaDialectProverbsSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.PAN_BARAJA_AGRO -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        PanBarajaAgroSheet(
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BICHITRAPUR_MANGROVE_BOATING -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BichitrapurMangroveSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.CHANDANESWAR_CHADAK_MELA -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        ChandaneswarChadakSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.PANCHALINGESWAR_STREAM_SAFETY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        PanchalingeswarStreamSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.DARK_SKY_BIOLUMINESCENCE -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        DarkSkyBioluminescenceSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BELL_METAL_ARTISANS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BellMetalArtisansSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BUDDHIST_JAIN_CIRCUIT -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BuddhistJainCircuitSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.HEIRLOOM_RICE_AGRO -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        HeirloomRiceAgroSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.LAKHANNATH_ZAMINDARI -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        LakhannathZamindariSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BUDHABALANGA_RIVER_ANGLING -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BudhabalangaRiverAnglingSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.CYCLONE_ORAL_HISTORY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        CycloneOralHistorySheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.DOCTORS_DIRECTORY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        DoctorsDirectorySheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.MEDICINE_STORES_DIRECTORY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        MedicineStoresDirectorySheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.POLYCLINIC_DIRECTORY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        PolyclinicDirectorySheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.PATHOLOGY_LAB_DIRECTORY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        PathologyLabDirectorySheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.PRIVATE_HOSPITAL_DIRECTORY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        PrivateHospitalDirectorySheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.RIVER_FLOOD_TELEMETRY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        RiverFloodTelemetrySheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.MATI_MANISHA_ARTISANS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        LacquerCraftArtisansSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALASORE_MAP_EXPLORER -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        HotspotsMapView(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.SALT_PAN_HERITAGE -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        SaltPanHeritageSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.HILSA_MIGRATION -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        HilsaMigrationSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.KULDIHA_ELEPHANT_CORRIDOR -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        KuldihaElephantCorridorSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.CHHENA_GAJA_HOT_BATCH -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        ChhenaGajaHotBatchSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.RIVER_FERRY_SCHEDULE -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        RiverFerryScheduleSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.ODIA_SAHITYA_REVIVAL -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        OdiaSahityaRevivalSheet(
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.ASK_BALASORE_AI -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        AskBalasoreChatbotSheet(
                            chatMessages = uiState.chatMessages,
                            isLoading = uiState.isChatLoading,
                            selectedModel = uiState.selectedChatModel,
                            selectedRole = uiState.selectedChatRole,
                            isSearchGroundingEnabled = uiState.isChatSearchGrounding,
                            isMapsGroundingEnabled = uiState.isChatMapsGrounding,
                            language = uiState.language,
                            onSendMessage = { viewModel.sendChatMessage(it) },
                            onSelectModel = { viewModel.setChatModel(it) },
                            onSelectRole = { viewModel.setChatRole(it) },
                            onToggleSearchGrounding = { viewModel.toggleChatSearchGrounding() },
                            onToggleMapsGrounding = { viewModel.toggleChatMapsGrounding() },
                            onClearChat = { viewModel.clearChat() },
                            onGenerateMusic = { prompt -> viewModel.generateLyriaMusic(prompt, isShortClip = true) },
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.INCOIS_OCEAN_ADVISORY -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        IncoisSeaAdvisorySheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.ODIA_PANJIKA_CALENDAR -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BaleswarPanjikaCalendarSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.RAIBANIA_AUDIO_WALK -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        RaibaniaAudioStoryWalkSheet(
                            language = uiState.language,
                            onGenerateLyriaTrack = { prompt, isClip ->
                                viewModel.generateLyriaMusic(prompt, isClip)
                            },
                            isMusicGenerating = uiState.isMusicGenerating,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BLOOD_AND_BED_PULSE -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BloodDonorBedPulseSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.CITIZEN_CIVIC_EYE -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        CivicEyeComponent(
                            language = uiState.language,
                            myReports = uiState.myCivicReports,
                            firestoreReports = uiState.firestoreUserReports,
                            isFirestoreLoading = uiState.isFirestoreReportsLoading,
                            onRefreshFirestore = { viewModel.fetchFirestoreUserReports() },
                            onSubmitReport = { title, cat, ward, desc, urgency, photo, photoUri, lat, lng, addr, uid, uname ->
                                viewModel.submitCivicEyeReport(
                                    title = title,
                                    category = cat,
                                    wardLocation = ward,
                                    description = desc,
                                    urgency = urgency,
                                    hasPhotoAttached = photo,
                                    photoUri = photoUri,
                                    latitude = lat,
                                    longitude = lng,
                                    geoAddress = addr,
                                    userId = uid,
                                    userName = uname
                                )
                            },
                            onAdvanceStatus = { id -> viewModel.advanceCivicReportStatus(id) },
                            onDeleteReport = { id -> viewModel.deleteCivicReport(id) },
                            onUpvoteReport = { id -> viewModel.upvoteCivicReport(id) },
                            onQueueReportOffline = { title, cat, ward, desc, urgency, photo, photoUri ->
                                viewModel.submitCivicEyeReport(
                                    title = title,
                                    category = cat,
                                    wardLocation = ward,
                                    description = desc,
                                    urgency = urgency,
                                    hasPhotoAttached = photo,
                                    photoUri = photoUri,
                                    queueForSyncWhenOnline = true
                                )
                            },
                            onFlushSyncQueue = { viewModel.flushCivicSyncWhenOnlineQueue() },
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.MY_CIVIC_REPORTS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        MyCivicReportsSheet(
                            language = uiState.language,
                            myReports = uiState.myCivicReports,
                            selectedFilter = uiState.selectedMyReportsFilter,
                            onFilterChange = { filter -> viewModel.setMyReportsFilter(filter) },
                            onSubmitReport = { title, cat, ward, desc, urgency, photo ->
                                viewModel.submitCivicReport(title, cat, ward, desc, urgency, photo)
                            },
                            onAdvanceStatus = { id -> viewModel.advanceCivicReportStatus(id) },
                            onDeleteReport = { id -> viewModel.deleteCivicReport(id) },
                            onAutoProgress = { viewModel.autoProgressCivicReports() },
                            autoUpdateMinutes = uiState.autoUpdateFrequencyMinutes,
                            onOpenLeaderboard = {
                                viewModel.closeFeatureSheet()
                                viewModel.openFeatureSheet(UniqueFeatureSheetType.CITIZEN_CONTRIBUTION_LEADERBOARD)
                            },
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.CITIZEN_CONTRIBUTION_LEADERBOARD -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        com.example.ui.features.civic.CitizenContributionLeaderboardSheet(
                            language = uiState.language,
                            myReports = uiState.myCivicReports,
                            userName = "Baleswari Citizen",
                            onOpenCivicEyeForm = {
                                viewModel.closeFeatureSheet()
                                viewModel.openFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
                            },
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.OPEN_WEATHER_MAP -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        OpenWeatherMapScreen(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.COASTAL_WEATHER_RADAR_MAP -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CoastalWeatherRadarMapView(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.ODIA_CULTURE_SECTION -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        OdiaCultureScreen(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.ODIA_FESTIVALS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        OdiaFestivalsScreen(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.TOURISM_HOTSPOTS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TourismHotspotsSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.LOCAL_TRANSPORT -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LocalTransportScreen(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.TOTO_AUTO_FARE_CARD -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BalasoreTotoFareCardSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALASORE_CAMPUS_CAREER_BOARD -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        CampusCareerBoardSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALASORE_AUTO_UPDATE_CENTER -> {
                    AutoUpdateCenterSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        isRefreshing = uiState.isRefreshing,
                        isHourlyAutoRefreshEnabled = uiState.isHourlyAutoRefreshEnabled,
                        autoUpdateFrequencyMinutes = uiState.autoUpdateFrequencyMinutes,
                        nextHourlyRefreshMinutesRemaining = uiState.nextHourlyRefreshMinutesRemaining,
                        autoRefreshCycleCount = uiState.autoRefreshCycleCount,
                        lastSyncStatusMessage = uiState.lastSyncStatusMessage,
                        appVersionInstalled = uiState.appVersionInstalled,
                        appVersionLatest = uiState.appVersionLatest,
                        isUpdateCheckLoading = uiState.isUpdateCheckLoading,
                        onForceSyncAll = { viewModel.forceSyncAllData() },
                        onToggleAutoRefresh = { viewModel.setHourlyAutoRefreshEnabled(it) },
                        onSetFrequency = { viewModel.setAutoUpdateFrequency(it) },
                        onCheckForUpdates = { viewModel.checkForAppUpdates() },
                        onClose = { viewModel.closeFeatureSheet() },
                        onOpenFeatureSheet = { viewModel.openFeatureSheet(it) }
                    )
                }
                UniqueFeatureSheetType.BALASORE_JUNCTION_RADAR -> {
                    BalasoreJunctionRadarSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALESWARI_PAAN_KRUSHI_MANDI -> {
                    BaleswariPaanKrushiMandiSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.NIGHT_CHEMIST_SANJEEVANI -> {
                    NightChemistSanjeevaniSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALESWAR_MAHOTSAV_CHADAK -> {
                    BaleswarMahotsavChadakSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.NILAGIRI_STONE_ARTISAN -> {
                    NilagiriStoneArtisanSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.TPNODL_WATCO_MONITOR -> {
                    TpnodlWatcoMonitorSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.TALASARI_MANGROVE_EXPLORER -> {
                    TalasariMangroveExplorerSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.MO_SEVA_KENDRA_CITIZEN -> {
                    MoSevaKendraCitizenSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.MARINE_FISHERMEN_SAFETY -> {
                    MarineFishermenSafetySheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.KULDIHA_ECO_CAMP_SAFARI -> {
                    KuldihaEcoCampSafariSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.FM_MCH_MEDICAL_COLLEGE_OPD -> {
                    FmMchMedicalCollegeOpdSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.SABAI_GRASS_MISSION_SHAKTI -> {
                    SabaiGrassMissionShaktiSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_MARITIME_COLONIAL -> {
                    BalasoreMaritimeColonialSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_STUDENT_CAREER_SCHOLARSHIP -> {
                    BalasoreStudentCareerScholarshipSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.DRDO_CHANDIPUR_SAFETY_RADAR -> {
                    DrdoChandipurSafetyRadarSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.SUBARNAREKHA_SLUICE_FLOOD_RADAR -> {
                    SubarnarekhaSluiceFloodRadarSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.NILAGIRI_CHHAU_CULTURAL_GUILD -> {
                    NilagiriChhauCulturalGuildSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.DHH_BLOOD_BANK_LIVE_RADAR -> {
                    DhhBloodBankLiveRadarSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.PAN_BARAJA_AQUA_SHRIMP_DESK -> {
                    PanBarajaAquaShrimpSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_COURT_LEGAL_AID_DESK -> {
                    BalasoreCourtLegalAidSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.NH16_HIGHWAY_PATROL_TRAUMA_SOS -> {
                    Nh16HighwayPatrolTraumaSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.PANCHALINGESWAR_KULDIHA_SAFARI -> {
                    PanchalingeswarKuldihaSafariSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.SER_BALASORE_RAILWAY_JUNCTION -> {
                    SerBalasoreRailwayJunctionSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.NOCCI_INDUSTRIAL_B2B_SKILL -> {
                    NocciIndustrialB2bSkillSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.TALSARI_BICHITRAPUR_MANGROVE_PILOT -> {
                    TalsariBichitrapurMangrovePilotSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.FAKIR_MOHAN_BHASHA_LITERATURE -> {
                    FakirMohanBhashaLiteratureSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALESWARI_CUISINE_SWEET_HERITAGE -> {
                    BaleswariCuisineSweetHeritageSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.TPNODL_WATCO_UTILITY_HOTLINE -> {
                    TpnodlWatcoUtilityHotlineSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.DHAN_MANDI_MSP_PROCUREMENT -> {
                    DhanMandiMspProcurementSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.NILAGIRI_GRANITE_STONEWARE_GUILD -> {
                    NilagiriGraniteStonewareGuildSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BAHANAGA_NHAI_RAPID_TRAUMA_NETWORK -> {
                    BahanagaNhaiRapidTraumaNetworkSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BHUSANDESWAR_CHANDANESWAR_PILGRIMAGE -> {
                    BhusandeswarChandaneswarPilgrimageSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.SENIOR_CITIZEN_SEVA_SETU -> {
                    SeniorCitizenSevaSetuSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_HIGHER_EDUCATION_HUB -> {
                    BalasoreHigherEducationHubSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_MO_BUS_CRUT_NAVIGATOR -> {
                    BalasoreMoBusCrutNavigatorSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.AQUA_SHRIMP_HATCHERY_RADAR -> {
                    AquaShrimpHatcheryRadarSheet(
                        dailyPulse = uiState.dailyPulse,
                        language = uiState.language,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.COASTAL_CYCLONE_SHELTER_NETWORK -> {
                    CoastalCycloneShelterNetworkSheet(
                        dailyPulse = uiState.dailyPulse,
                        language = uiState.language,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.MARITIME_FOREIGN_LOGE_TRAIL -> {
                    MaritimeForeignLogeTrailSheet(
                        dailyPulse = uiState.dailyPulse,
                        language = uiState.language,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.RURAL_MOBILE_TELEMEDICINE_NETWORK -> {
                    RuralMobileTelemedicineNetworkSheet(
                        dailyPulse = uiState.dailyPulse,
                        language = uiState.language,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALESWARI_BETEL_PADDY_COOPERATIVE -> {
                    BaleswariBetelPaddyCooperativeSheet(
                        dailyPulse = uiState.dailyPulse,
                        language = uiState.language,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.COASTAL_CRUT_INTERDISTRICT_BUS_RADAR -> {
                    CoastalCrutInterdistrictBusRadarSheet(
                        dailyPulse = uiState.dailyPulse,
                        language = uiState.language,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.HORSESHOE_CRAB_INTERTIDAL_PROTECTION -> {
                    HorseshoeCrabIntertidalProtectionSheet(
                        dailyPulse = uiState.dailyPulse,
                        language = uiState.language,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.CITIZEN_FEEDBACK_PORTAL -> {
                    CitizenFeedbackSheet(
                        language = uiState.language,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.FCM_SEVERE_WEATHER_ALERT -> {
                    FcmSevereWeatherAlertSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.COASTAL_MARINE_AUDIO_RADIO -> {
                    CoastalMarineAudioRadioSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.CYCLONE_SURGE_EVACUATION_ROUTER -> {
                    CycloneSurgeEvacuationRouterSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALARAMGADI_MANDI_PRICE_INDEX -> {
                    BalaramgadiMandiPriceIndexSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.TEMPLE_AARTI_KHIRA_BHOG_COMPANION -> {
                    BalasoreTemplePilgrimageCompanionSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.FM_MCH_EMERGENCY_BLOOD_NETWORK -> {
                    FmMchEmergencyBloodNetworkSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.MO_BALASORE_CIVIC_STORM_REPORTER -> {
                    MoBalasoreCivicStormReporterSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.ITR_MISSILE_COASTAL_TRACKER -> {
                    ItrChandipurMissileTrackerSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.SUBARNAREKHA_CROP_ADVISORY -> {
                    SubarnarekhaCropAdvisorySheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_SMART_TRANSIT_TRACKER -> {
                    BalasoreSmartTransitTrackerSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.FAKIR_MOHAN_STUDENT_HUB -> {
                    FakirMohanStudentHubSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALESWARI_CUISINE_EXPLORER -> {
                    BaleswariCuisineExplorerSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.TPNODL_POWER_OUTAGE_RADAR -> {
                    TpnodlPowerOutageRadarSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.CHANDIPUR_BIOLUMINESCENCE_EXPLORER -> {
                    ChandipurBioluminescenceExplorerSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_SNAKEBITE_ANTI_VENOM -> {
                    BalasoreSnakebiteAntiVenomSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.NILAGIRI_DOKRA_CRAFT_GUILD -> {
                    NilagiriDokraCraftGuildSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BAGHA_JATIN_FREEDOM_TRAIL -> {
                    BaghaJatinFreedomTrailSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.KULDIHA_ECO_CORRIDOR_FIRE_RADAR -> {
                    KuldihaEcoCorridorFireRadarSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BHOGRAI_CASHEW_GROUNDNUT_MANDI -> {
                    BhograiCashewGroundnutMandiSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onDismiss = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.RAIBANIA_FORTRESS_VAULT -> {
                    RaibaniaMedievalFortressVaultSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALARAMGADI_TRAWLER_FLEET_RADAR -> {
                    BalaramgadiDeepSeaTrawlerFleetSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.NILAGIRI_LACQUER_BAMBOO_GUILD -> {
                    NilagiriLacquerBambooGuildSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_DISTRICT_BLOOD_DONOR_GRID -> {
                    BalasoreDistrictBloodDonorGridSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.SUBARNAREKHA_FLOOD_SLUICE_TELEMETRY -> {
                    SubarnarekhaFloodSluiceTelemetrySheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_YOUTH_WELLNESS_STUDY_CIRCLE -> {
                    BalasoreYouthWellnessStudyCircleSheet(
                        language = uiState.language,
                        dailyPulse = uiState.dailyPulse,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_OFFLINE_MAP_TILES -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BalasoreOfflineMapTileSheet(
                            language = uiState.language,
                            onDismiss = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.MARINE_FISHERMEN_HIGH_SEAS_SOS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        MarineFishermenHighSeasSosSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALESWARI_CULINARY_GI_TRAIL -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BaleswariGiCulinaryTrailSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.WARD_FLOOD_EVACUATION_ROUTER -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        MicroWardFloodEvacuationSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.KULDIHA_ELEPHANT_CORRIDOR_SENTINEL -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        KuldihaElephantCorridorSentinelSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.STUDENT_CAREER_LIBRARY_PULSE -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        StudentCareerLibraryPulseSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.ROOFTOP_SOLAR_TPNODL_RADAR -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        RooftopSolarTpnodlRadarSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.TALASARI_UDAYPUR_RED_CRAB_SAFARI -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        TalasariRedCrabSafariSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.PANCHALINGESWAR_WATERFALL_TREK_MONITOR -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        PanchalingeswarWaterfallTrekSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.EMAMI_JAGANNATH_REMUNA_HERITAGE -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        EmamiJagannathRemunaHeritageSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALARAMGADI_FISH_MANDI_RATES -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BalaramgadiFishMandiSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.TOWN_AUTO_RICKSHAW_FARE_CALCULATOR -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        TownAutoFareCalculatorSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.WARD_SANITATION_GARBAGE_VEHICLE_TRACKER -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        WardSanitationVehicleTrackerSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALASORE_INDUSTRIAL_JOB_APPRENTICE_BOARD -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        BalasoreIndustrialJobApprenticeSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.KRUSHAK_PACS_MANDI_GRAIN_RADAR -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        KrushakPacsMandiGrainSheet(
                            dailyPulse = uiState.dailyPulse,
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.OLIVE_RIDLEY_MARINE_WILDLIFE -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        OliveRidleyMarineWildlifeSheet(
                            language = uiState.language,
                            dailyPulse = uiState.dailyPulse,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.AIIMS_DHH_OPD_BED_TRACKER -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        AiimsDhhOpdBedTrackerSheet(
                            language = uiState.language,
                            dailyPulse = uiState.dailyPulse,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.OFFLINE_CYCLONE_SAFETY_TOOLKIT -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                    ) {
                        OfflineCycloneSafetyToolkitSheet(
                            language = uiState.language,
                            dailyPulse = uiState.dailyPulse,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.NEAREST_EMERGENCY_SERVICES_MAP -> {
                    NearestEmergencyServicesMapSheet(
                        onDismiss = { viewModel.closeFeatureSheet() },
                        language = uiState.language
                    )
                }
                UniqueFeatureSheetType.BALASORE_BUS_STAND_LIVE_SCHEDULES -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        BalasoreBusStandScheduleScreen(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.CHANDIPUR_VANISHING_SEA_CLOCK -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        ChandipurVanishingSeaClockSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.COASTAL_CYCLONE_PREPAREDNESS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CoastalCyclonePreparednessSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALASORE_DAY_TRIP_PLANNER -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        BalasoreDayTripPlannerSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALARAMGADI_SEAFOOD_INDEX -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        BalaramgadiSeafoodIndexSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALASORE_PHOTO_SPOTS_CHECKLIST -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        BalasorePhotoSpotsChecklistSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.BALASORE_LIVE_TRAIN_STATUS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        BalasoreLiveTrainStatusSheet(
                            language = uiState.language,
                            onClose = { viewModel.closeFeatureSheet() }
                        )
                    }
                }
                UniqueFeatureSheetType.CYCLONE_SHELTER_WIDGET_MANAGER -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        BalasoreShelterWidgetManagerSheet(
                            language = uiState.language,
                            onDismiss = { viewModel.closeFeatureSheet() },
                            onOpenShelterNetwork = {
                                viewModel.openFeatureSheet(UniqueFeatureSheetType.COASTAL_CYCLONE_SHELTER_NETWORK)
                            }
                        )
                    }
                }
                UniqueFeatureSheetType.OSM_OFFLINE_TOURISM_MAP -> {
                    BalasoreOsmTourismMapView(
                        language = uiState.language,
                        civicReports = uiState.myCivicReports,
                        onReportStatusChange = { id -> viewModel.advanceCivicReportStatus(id) },
                        onAddReportClick = {
                            viewModel.closeFeatureSheet()
                            viewModel.openFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
                        },
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.CIVIC_ISSUES_MAP -> {
                    CivicIssuesMapScreen(
                        language = uiState.language,
                        draftReports = uiState.myCivicReports,
                        onSubmitDraft = { draftId ->
                            viewModel.submitCivicReportDraft(draftId)
                        },
                        onEditDraft = { draft ->
                            viewModel.closeFeatureSheet()
                            viewModel.openFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
                        },
                        onDeleteDraft = { draftId ->
                            viewModel.deleteCivicReport(draftId)
                        },
                        onSnapNewDraft = {
                            viewModel.closeFeatureSheet()
                            viewModel.openFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
                        },
                        onUpvoteReport = { report ->
                            viewModel.upvoteCivicReport(report.id)
                        },
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.HISTORICAL_D3_WEATHER_TIDAL_TRENDS -> {
                    ModalBottomSheet(
                        onDismissRequest = { viewModel.closeFeatureSheet() },
                        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
                        modifier = Modifier.fillMaxSize(),
                        containerColor = Color(0xFF070D1E)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            BalasoreD3TelemetryCard(
                                language = uiState.language,
                                initialMode = D3TelemetryMode.WEATHER_TRENDS
                            )
                        }
                    }
                }
                UniqueFeatureSheetType.OFFICIAL_TOURISM_EVENT_POSTER_SCANNER -> {
                    BalasorePamphletScannerSheet(
                        language = uiState.language,
                        initialPamphletCategory = PamphletCategory.EVENT_POSTER,
                        onDismiss = { viewModel.closeFeatureSheet() },
                        onOpenEventCalendar = { viewModel.openFeatureSheet(UniqueFeatureSheetType.ODIA_FESTIVALS) }
                    )
                }
                UniqueFeatureSheetType.BALASORE_TOURISM_GALLERY -> {
                    com.example.ui.screens.BalasoreTourismGallerySheet(
                        language = uiState.language,
                        itineraryItems = uiState.itineraryItems,
                        onAddToItinerary = { hotspot, day, timeSlot, notes ->
                            viewModel.addToItinerary(hotspot, day, timeSlot, notes)
                        },
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BATTERY_HEALTH_MONITOR,
                UniqueFeatureSheetType.APP_HEALTH_DASHBOARD -> {
                    com.example.ui.features.resilience.BatteryHealthMonitorDashboardSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.PRIVACY_POLICY -> {
                    com.example.ui.screens.PrivacyPolicySheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.TRANSPORT_ALERTS -> {
                    val activity = androidx.compose.ui.platform.LocalContext.current as? android.app.Activity
                    com.example.ui.features.transit.TransportAlertsSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() },
                        highlightTransportNumber = activity?.intent?.getStringExtra("highlight_transport_number")
                    )
                }
                UniqueFeatureSheetType.PUSH_NOTIFICATION_CENTER -> {
                    com.example.ui.features.notifications.PushNotificationCenterSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.USER_PROFILE_AUTH -> {
                    com.example.ui.features.auth.UserProfileAuthSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() },
                        onOpenHotspot = {
                            viewModel.openFeatureSheet(UniqueFeatureSheetType.TOURISM_INTERACTIVE_MAP)
                        },
                        onOpenAdminPanel = {
                            viewModel.openFeatureSheet(UniqueFeatureSheetType.ADMIN_MODERATOR_PANEL)
                        }
                    )
                }
                UniqueFeatureSheetType.ADMIN_MODERATOR_PANEL -> {
                    com.example.ui.features.admin.AdminModeratorPanelSheet(
                        language = uiState.language,
                        newsArticles = uiState.newsArticles,
                        weatherInfo = uiState.weather,
                        weatherAlerts = uiState.weatherAlerts,
                        hotspots = uiState.hotspots,
                        onSaveNewsArticle = { viewModel.adminSaveNewsArticle(it) },
                        onDeleteNewsArticle = { viewModel.adminDeleteNewsArticle(it) },
                        onUpdateWeatherInfo = { viewModel.adminUpdateWeatherInfo(it) },
                        onSaveWeatherAlert = { viewModel.adminSaveWeatherAlert(it) },
                        onDeleteWeatherAlert = { viewModel.adminDeleteWeatherAlert(it) },
                        onSaveHotspot = { viewModel.adminSaveHotspot(it) },
                        onDeleteHotspot = { viewModel.adminDeleteHotspot(it) },
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.TOURISM_INTERACTIVE_MAP -> {
                    com.example.ui.features.map.BalasoreTourismInteractiveMapSheet(
                        language = uiState.language,
                        hotspots = uiState.hotspots,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.AR_LANDMARK_CAMERA -> {
                    com.example.ui.features.ar.ArLandmarkCameraSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.OFFLINE_MODE_MANAGER -> {
                    com.example.ui.features.offline.OfflineModeManagerSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.MUNICIPAL_WARDS_DIRECTORY -> {
                    com.example.ui.features.municipal.BalasoreWardsDirectorySheet(
                        language = uiState.language,
                        onLocateWardOnMap = { ward ->
                            viewModel.closeFeatureSheet()
                            viewModel.openFeatureSheet(UniqueFeatureSheetType.CIVIC_ISSUES_MAP)
                        },
                        onReportCivicIssueInWard = { ward ->
                            viewModel.closeFeatureSheet()
                            viewModel.openFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
                        },
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.BALASORE_MOVIE_THEATRES -> {
                    com.example.ui.components.BalasoreMovieTheatresSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.COMMUNITY_FORUM -> {
                    com.example.ui.components.BalasoreCommunityForumSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                UniqueFeatureSheetType.LOCAL_BUS_TRACKER -> {
                    com.example.ui.components.BalasoreLocalBusTrackerSheet(
                        language = uiState.language,
                        onClose = { viewModel.closeFeatureSheet() }
                    )
                }
                null -> {}
            }
        }

        // Baleshwar 360° Categorized Mega-Menu & Sub-Menu Directory Sheet
        if (showQuickHubSheet) {
            BalasoreQuickHubSheet(
                language = uiState.language,
                isAutoRefreshActive = uiState.isHourlyAutoRefreshEnabled,
                onOpenFeatureSheet = { sheetType ->
                    showQuickHubSheet = false
                    viewModel.openFeatureSheet(sheetType)
                },
                onOpenSettings = {
                    showQuickHubSheet = false
                    showSettingsSheet = true
                },
                onDismiss = { showQuickHubSheet = false },
                selectedTab = uiState.selectedTab,
                onSelectPrimaryTab = { targetTab ->
                    showQuickHubSheet = false
                    viewModel.selectTab(targetTab)
                }
            )
        }

        // Global Settings Menu with Dark Mode Toggle, Night Emergency Options, Chandipur High Tide Alerts & Battery-Saving Mode (Persisted)
        if (showSettingsSheet) {
            AppSettingsSheet(
                themeMode = uiState.themeMode,
                onThemeModeChange = { viewModel.setThemeMode(it) },
                language = uiState.language,
                onLanguageChange = { viewModel.setLanguage(it) },
                isAutoRefreshEnabled = uiState.isHourlyAutoRefreshEnabled,
                onToggleAutoRefresh = { viewModel.setHourlyAutoRefreshEnabled(it) },
                onOpenAppHealth = {
                    showSettingsSheet = false
                    viewModel.openFeatureSheet(UniqueFeatureSheetType.APP_HEALTH_DASHBOARD)
                },
                onOpenPrivacyPolicy = {
                    showSettingsSheet = false
                    viewModel.openFeatureSheet(UniqueFeatureSheetType.PRIVACY_POLICY)
                },
                onDismiss = { showSettingsSheet = false },
                isBatterySavingModeEnabled = uiState.isBatterySavingModeEnabled,
                onToggleBatterySavingMode = { viewModel.setBatterySavingModeEnabled(it) },
                isChandipurHighTideAlertsEnabled = uiState.isChandipurHighTideAlertsEnabled,
                onToggleChandipurHighTideAlerts = { viewModel.setChandipurHighTideAlertsEnabled(it) },
                tideForecast = uiState.lastKnownTideForecast,
                onTestChandipurHighTidePush = {
                    viewModel.triggerChandipurHighTideAlertNow()
                    Toast.makeText(
                        context,
                        if (uiState.language == com.example.data.model.AppLanguage.ODIA) {
                            "ଚାନ୍ଦିପୁର ଉଚ୍ଚ ଜୁଆର ସତର୍କତା ପ୍ରେରଣ ହେଲା!"
                        } else {
                            "Chandipur Beach High Tide alert dispatched!"
                        },
                        Toast.LENGTH_SHORT
                    ).show()
                },
                isAdaptiveSyncEnabled = uiState.isAdaptiveSyncEnabled,
                onToggleAdaptiveSync = { viewModel.setAdaptiveSyncEnabled(it) },
                subscribedNewsCategories = uiState.subscribedNewsCategories,
                onToggleNewsCategorySubscription = { viewModel.toggleSubscribedNewsCategory(it) },
                onTestCategoryPushAlert = { cat ->
                    viewModel.triggerSubscribedCategoryAlertNow(cat)
                    Toast.makeText(
                        context,
                        "Firestore push alert dispatched for '$cat'!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }
}

@Composable
fun LanguagePill(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 10.sp
            )
        )
    }
}

@Composable
fun ThemeTogglePill(
    currentMode: ThemeMode,
    onCycleTheme: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDarkActive = com.example.ui.theme.LocalAppTheme.current.isDark
    val (icon, tint, label) = when (currentMode) {
        ThemeMode.SYSTEM -> Triple(
            Icons.Default.BrightnessAuto,
            MaterialTheme.colorScheme.primary,
            "Auto"
        )
        ThemeMode.LIGHT -> Triple(
            Icons.Default.LightMode,
            Color(0xFFD97706),
            "Light"
        )
        ThemeMode.DARK -> Triple(
            Icons.Default.DarkMode,
            Color(0xFF38BDF8),
            "Dark"
        )
        ThemeMode.HIGH_CONTRAST_DARK -> Triple(
            Icons.Default.NightlightRound,
            Color(0xFFFBBF24),
            "Night"
        )
    }

    Surface(
        onClick = onCycleTheme,
        shape = RoundedCornerShape(20.dp),
        color = if (isDarkActive) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(
            1.dp,
            if (isDarkActive) Color(0xFF38BDF8).copy(alpha = 0.65f) else MaterialTheme.colorScheme.outlineVariant
        ),
        tonalElevation = 1.dp,
        modifier = modifier
            .testTag("theme_toggle_button")
            .height(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = "Theme mode: $label (Tap to switch)",
                tint = tint,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkActive) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}
