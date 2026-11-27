package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.ui.viewmodel.UniqueFeatureSheetType

private enum class QuickHubMenuCategory(
    val id: String,
    val labelEn: String,
    val labelOd: String,
    val emoji: String
) {
    ALL("ALL", "All Menus", "ସମସ୍ତ ମେନୁ", "🌟"),
    CIVIC_NEWS("CIVIC_NEWS", "Civic & News", "ନାଗରିକ ଓ ଖବର", "🏛️"),
    COASTAL_WEATHER("COASTAL_WEATHER", "Coastal & Weather", "ଉପକୂଳ ଓ ପାଣିପାଗ", "🌊"),
    TRANSIT_SOS("TRANSIT_SOS", "Transit & SOS", "ପରିବହନ ଓ SOS", "🚌"),
    CULTURE_SYSTEM("CULTURE_SYSTEM", "Culture & System", "ସଂସ୍କୃତି ଓ ସିଷ୍ଟମ୍", "🎭")
}

private data class QuickHubSubMenuItem(
    val id: String,
    val category: QuickHubMenuCategory,
    val titleEn: String,
    val titleOd: String,
    val subtitleEn: String,
    val subtitleOd: String,
    val badgeEn: String,
    val icon: ImageVector,
    val iconTint: Color,
    val lightBg: Color,
    val lightBorder: Color,
    val testTag: String,
    val onClickAction: () -> Unit
)

/**
 * Rearranged & Enhanced Baleshwar 360° Mega-Menu & Sub-Menu Directory Sheet.
 * Provides:
 * 1. Primary Screen Navigation Switcher (Explore, 360° Hub, News, Weather, Services/SOS, Profile)
 * 2. Instant Bilingual Search Filter across all menus and sub-menus
 * 3. Categorized Sub-Menu Tabs (All, Civic & News, Coastal & Weather, Transit & SOS, Culture & System)
 * 4. Full Material 3 Dark Mode & Nighttime Readability adaptation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreQuickHubSheet(
    language: AppLanguage,
    isAutoRefreshActive: Boolean,
    onOpenFeatureSheet: (UniqueFeatureSheetType) -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
    selectedTab: Int = 0,
    onSelectPrimaryTab: ((Int) -> Unit)? = null
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val appTheme = LocalAppTheme.current
    val isDark = appTheme.isDark

    var selectedCategory by remember { mutableStateOf(QuickHubMenuCategory.ALL) }
    var searchQuery by remember { mutableStateOf("") }

    val allSubMenuItems = remember(isOdia) {
        listOf(
            // --- CIVIC & NEWS SUB-MENUS ---
            QuickHubSubMenuItem(
                id = "civic_eye",
                category = QuickHubMenuCategory.CIVIC_NEWS,
                titleEn = "Report Civic Issue",
                titleOd = "ନାଗରିକ ସମସ୍ୟା ଜଣାନ୍ତୁ",
                subtitleEn = "Multi-Category Picker & Geo-Tag",
                subtitleOd = "ୱାର୍ଡ ସମସ୍ୟା ଓ ଫଟୋ ଅپଲୋଡ୍",
                badgeEn = "CIVIC EYE",
                icon = Icons.Default.ReportProblem,
                iconTint = Color(0xFFEA580C),
                lightBg = Color(0xFFFFF7ED),
                lightBorder = Color(0xFFFED7AA),
                testTag = "quick_hub_civic_eye_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE)
                }
            ),
            QuickHubSubMenuItem(
                id = "civic_eye_layers_map",
                category = QuickHubMenuCategory.CIVIC_NEWS,
                titleEn = "Civic Eye Layers Map",
                titleOd = "ସିଭିକ୍ ଆଇ ଲେୟାର ମ୍ୟାପ୍",
                subtitleEn = "Toggle Potholes, Sanitation & Drainage",
                subtitleOd = "ରାସ୍ତା ଖାଲ ଓ ସ୍ୱଚ୍ଛତା ମ୍ୟାପ୍ ଲେୟାର",
                badgeEn = "OSM LAYERS",
                icon = Icons.Default.Layers,
                iconTint = Color(0xFF0284C7),
                lightBg = Color(0xFFE0F2FE),
                lightBorder = Color(0xFF7DD3FC),
                testTag = "quick_hub_civic_layers_map_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.CIVIC_EYE_LAYERS_MAP)
                }
            ),
            QuickHubSubMenuItem(
                id = "civic_eye_history",
                category = QuickHubMenuCategory.CIVIC_NEWS,
                titleEn = "Civic Eye History",
                titleOd = "ସିଭିକ୍ ଆଇ ରିପୋର୍ଟ ଇତିହାସ",
                subtitleEn = "Chronological log, timestamps & statuses",
                subtitleOd = "ସମୟ ଓ ସ୍ଥିତି ସହ ସମସ୍ତ ଅଭିଯୋଗ ତାଲିକା",
                badgeEn = "TIMELINE",
                icon = Icons.Default.History,
                iconTint = Color(0xFF0F766E),
                lightBg = Color(0xFFCCFBF1),
                lightBorder = Color(0xFF5EEAD4),
                testTag = "quick_hub_civic_history_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.CIVIC_EYE_HISTORY)
                }
            ),
            QuickHubSubMenuItem(
                id = "community_forum",
                category = QuickHubMenuCategory.CIVIC_NEWS,
                titleEn = "Community Forum",
                titleOd = "କମ୍ୟୁନିଟି ଫୋରମ୍",
                subtitleEn = "Discussions & Lost & Found Hub",
                subtitleOd = "ଆଲୋଚନା ଓ ହଜିବା-ମିଳିବା",
                badgeEn = "LOST & FOUND",
                icon = Icons.Default.Forum,
                iconTint = Color(0xFF7C3AED),
                lightBg = Color(0xFFF5F3FF),
                lightBorder = Color(0xFFDDD6FE),
                testTag = "quick_hub_community_forum_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.COMMUNITY_FORUM)
                }
            ),
            QuickHubSubMenuItem(
                id = "citizen_feedback",
                category = QuickHubMenuCategory.CIVIC_NEWS,
                titleEn = "Citizen Feedback",
                titleOd = "ନାଗରିକ ମତାମତ",
                subtitleEn = "Report & Track Municipal Issues",
                subtitleOd = "ଅଭିଯୋଗ ଓ ପରାମର୍ଶ",
                badgeEn = "PORTAL",
                icon = Icons.Default.Feedback,
                iconTint = Color(0xFF0369A1),
                lightBg = Color(0xFFE0F2FE),
                lightBorder = Color(0xFFBAE6FD),
                testTag = "top_bar_feedback_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.CITIZEN_FEEDBACK_PORTAL)
                }
            ),
            QuickHubSubMenuItem(
                id = "admin_moderation",
                category = QuickHubMenuCategory.CIVIC_NEWS,
                titleEn = "Moderator Panel",
                titleOd = "ମଡରେଟର୍ ପ୍ୟାନେଲ୍",
                subtitleEn = "Approve News, Reviews & Photos",
                subtitleOd = "ଖବର ଓ ପର୍ଯ୍ୟଟନ ସମୀକ୍ଷା ଯାଞ୍ଚ",
                badgeEn = "MODERATION",
                icon = Icons.Default.VerifiedUser,
                iconTint = Color(0xFF6D28D9),
                lightBg = Color(0xFFEDE9FE),
                lightBorder = Color(0xFFC4B5FD),
                testTag = "quick_hub_moderator_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.ADMIN_MODERATOR_PANEL)
                }
            ),

            // --- COASTAL & WEATHER SUB-MENUS ---
            QuickHubSubMenuItem(
                id = "interactive_map",
                category = QuickHubMenuCategory.COASTAL_WEATHER,
                titleEn = "Interactive 360° Map",
                titleOd = "ଇଣ୍ଟରାକ୍ଟିଭ୍ ମାନଚିତ୍ର",
                subtitleEn = "Hotspots, News, Weather & Amenities",
                subtitleOd = "ପର୍ଯ୍ୟଟନ, ଖବର ଓ ପାଣିପାଗ ମାନଚିତ୍ର",
                badgeEn = "MULTI-LAYER",
                icon = Icons.Default.Map,
                iconTint = OceanBlue,
                lightBg = Color(0xFFE0F2FE),
                lightBorder = Color(0xFFBAE6FD),
                testTag = "quick_hub_interactive_map_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.TOURISM_INTERACTIVE_MAP)
                }
            ),
            QuickHubSubMenuItem(
                id = "chandipur_tide",
                category = QuickHubMenuCategory.COASTAL_WEATHER,
                titleEn = "Chandipur Tide Clock",
                titleOd = "ଚାନ୍ଦିପୁର ଜୁଆର ଘଣ୍ଟା",
                subtitleEn = "Safe Sea-Walk Window & Siren",
                subtitleOd = "ସୁରକ୍ଷିତ ବେଳାଭୂମି ଚଲାପଥ ସମୟ",
                badgeEn = "LIVE TIDE",
                icon = Icons.Default.Water,
                iconTint = Color(0xFF0284C7),
                lightBg = Color(0xFFF0F9FF),
                lightBorder = Color(0xFF7DD3FC),
                testTag = "quick_hub_chandipur_tide_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.CHANDIPUR_TIDE_TIMER)
                }
            ),
            QuickHubSubMenuItem(
                id = "cyclone_shelters",
                category = QuickHubMenuCategory.COASTAL_WEATHER,
                titleEn = "Cyclone Shelters",
                titleOd = "ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ",
                subtitleEn = "Evacuation Routes & Block Helplines",
                subtitleOd = "ନିକଟତମ ଆଶ୍ରୟସ୍ଥଳୀ ଓ ହେଲ୍ପଲାଇନ୍",
                badgeEn = "RESILIENCE",
                icon = Icons.Default.Shield,
                iconTint = Color(0xFFDC2626),
                lightBg = Color(0xFFFEF2F2),
                lightBorder = Color(0xFFFECACA),
                testTag = "quick_hub_cyclone_shelters_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.COASTAL_CYCLONE_SHELTER_NETWORK)
                }
            ),
            QuickHubSubMenuItem(
                id = "push_alerts_center",
                category = QuickHubMenuCategory.COASTAL_WEATHER,
                titleEn = "Push Alert Center",
                titleOd = "ପୁସ୍ ଆଲର୍ଟ ସେଣ୍ଟର୍",
                subtitleEn = "Breaking News & Weather Warnings",
                subtitleOd = "ଜରୁରୀ ଖବର ଓ ପାଣିପାଗ ସତର୍କତା",
                badgeEn = "REAL-TIME",
                icon = Icons.Default.NotificationsActive,
                iconTint = Color(0xFFD97706),
                lightBg = Color(0xFFFFFBEB),
                lightBorder = Color(0xFFFDE68A),
                testTag = "quick_hub_push_alerts_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.PUSH_NOTIFICATION_CENTER)
                }
            ),

            // --- TRANSIT & SOS SUB-MENUS ---
            QuickHubSubMenuItem(
                id = "bus_fare_tracker",
                category = QuickHubMenuCategory.TRANSIT_SOS,
                titleEn = "Bus Tracker & Fare",
                titleOd = "ବସ୍ ଟ୍ରାକର ଓ ଭଡ଼ା",
                subtitleEn = "Route Fare Calculator & Live Stops",
                subtitleOd = "ସ୍ଥାନୀୟ ବସ୍ ଭଡ଼ା କାଲକୁଲେଟର",
                badgeEn = "FARE CALC",
                icon = Icons.Default.DirectionsBus,
                iconTint = Color(0xFF0284C7),
                lightBg = Color(0xFFE0F2FE),
                lightBorder = Color(0xFFBAE6FD),
                testTag = "quick_hub_bus_tracker_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.LOCAL_BUS_TRACKER)
                }
            ),
            QuickHubSubMenuItem(
                id = "live_train_status",
                category = QuickHubMenuCategory.TRANSIT_SOS,
                titleEn = "BLS Railway Radar",
                titleOd = "ବାଲେଶ୍ୱର ରେଳ ଷ୍ଟାଟସ୍",
                subtitleEn = "Live Train Platforms & Delays",
                subtitleOd = "ଲାଇଭ୍ ଟ୍ରେନ୍ ଆଗମନ ଓ ପ୍ଲାଟଫର୍ମ",
                badgeEn = "BLS JUNCTION",
                icon = Icons.Default.Train,
                iconTint = EmeraldGreen,
                lightBg = Color(0xFFF0FDF4),
                lightBorder = Color(0xFF86EFAC),
                testTag = "quick_hub_train_radar_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_LIVE_TRAIN_STATUS)
                }
            ),
            QuickHubSubMenuItem(
                id = "emergency_map",
                category = QuickHubMenuCategory.TRANSIT_SOS,
                titleEn = "Emergency & Hospitals",
                titleOd = "ଜରୁରୀ ଚିକିତ୍ସା ଓ ହସ୍ପିଟାଲ୍",
                subtitleEn = "FM Medical, Blood Bank & Police",
                subtitleOd = "ରକ୍ତ ଭଣ୍ଡାର, ଆମ୍ବୁଲାନ୍ସ ଓ ପୋଲିସ",
                badgeEn = "24x7 SOS",
                icon = Icons.Default.LocalHospital,
                iconTint = Color(0xFFDC2626),
                lightBg = Color(0xFFFEF2F2),
                lightBorder = Color(0xFFFECACA),
                testTag = "quick_hub_emergency_map_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.NEAREST_EMERGENCY_SERVICES_MAP)
                }
            ),
            QuickHubSubMenuItem(
                id = "offline_mode_pack",
                category = QuickHubMenuCategory.TRANSIT_SOS,
                titleEn = "Offline Cache Manager",
                titleOd = "ଅଫଲାଇନ୍ ଷ୍ଟୋରେଜ୍ ହବ୍",
                subtitleEn = "Saved News, Weather & Hotspots",
                subtitleOd = "ବିନା ଇଣ୍ଟରନେଟରେ ଖବର ଓ ପର୍ଯ୍ୟଟନ",
                badgeEn = "ROOM SQLITE",
                icon = Icons.Default.CloudDownload,
                iconTint = Color(0xFF0D9488),
                lightBg = Color(0xFFF0FDFA),
                lightBorder = Color(0xFF99F6E4),
                testTag = "quick_hub_offline_manager_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.OFFLINE_MODE_MANAGER)
                }
            ),

            // --- CULTURE & SYSTEM SUB-MENUS (Preserving Original TestTags) ---
            QuickHubSubMenuItem(
                id = "qr_scanner",
                category = QuickHubMenuCategory.CULTURE_SYSTEM,
                titleEn = "QR Scanner",
                titleOd = "QR ସ୍କାନର୍",
                subtitleEn = "Event & Tourism Posters",
                subtitleOd = "ପୋଷ୍ଟର ଓ ପାମ୍ପଲେଟ୍",
                badgeEn = "ML KIT",
                icon = Icons.Default.QrCodeScanner,
                iconTint = OceanBlue,
                lightBg = Color(0xFFE0F2FE),
                lightBorder = Color(0xFFBAE6FD),
                testTag = "top_bar_qr_scanner_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.OFFICIAL_TOURISM_EVENT_POSTER_SCANNER)
                }
            ),
            QuickHubSubMenuItem(
                id = "odia_culture",
                category = QuickHubMenuCategory.CULTURE_SYSTEM,
                titleEn = "Odia Culture",
                titleOd = "ଓଡ଼ିଆ ସଂସ୍କୃତି",
                subtitleEn = "Panjika, Chadak & Festivals",
                subtitleOd = "ପଞ୍ଜିକା ଓ ପର୍ବପର୍ବାଣି",
                badgeEn = "HERITAGE",
                icon = Icons.Default.Festival,
                iconTint = Color(0xFFB45309),
                lightBg = Color(0xFFFEF3C7),
                lightBorder = Color(0xFFFDE68A),
                testTag = "top_bar_culture_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.ODIA_CULTURE_SECTION)
                }
            ),
            QuickHubSubMenuItem(
                id = "app_health",
                category = QuickHubMenuCategory.CULTURE_SYSTEM,
                titleEn = "App Health & Battery",
                titleOd = "ଆପ୍ ସ୍ୱାସ୍ଥ୍ୟ",
                subtitleEn = "WorkManager & Diagnostics",
                subtitleOd = "ବ୍ୟାଟେରୀ ସେଭର୍ ମୋଡ୍",
                badgeEn = "DIAGNOSTICS",
                icon = Icons.Default.HealthAndSafety,
                iconTint = EmeraldGreen,
                lightBg = Color(0xFFDCFCE7),
                lightBorder = Color(0xFF86EFAC),
                testTag = "top_bar_app_health_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.APP_HEALTH_DASHBOARD)
                }
            ),
            QuickHubSubMenuItem(
                id = "auto_update_center",
                category = QuickHubMenuCategory.CULTURE_SYSTEM,
                titleEn = "Auto-Update Center",
                titleOd = "ଅଟୋ ଅପଡେଟ୍ ହବ୍",
                subtitleEn = "Sync Loops & Play Updates",
                subtitleOd = "ସିଙ୍କ୍ ଓ ଟେଲିମେଟ୍ରି",
                badgeEn = "SYNC ENGINE",
                icon = Icons.Default.Sync,
                iconTint = Color(0xFF7C3AED),
                lightBg = Color(0xFFEDE9FE),
                lightBorder = Color(0xFFDDD6FE),
                testTag = "top_bar_auto_update_btn",
                onClickAction = {
                    onDismiss()
                    onOpenFeatureSheet(UniqueFeatureSheetType.BALASORE_AUTO_UPDATE_CENTER)
                }
            ),
            QuickHubSubMenuItem(
                id = "settings_theme",
                category = QuickHubMenuCategory.CULTURE_SYSTEM,
                titleEn = "Settings & Theme",
                titleOd = "ସେଟିଙ୍ଗ୍ସ",
                subtitleEn = "Dark Mode, AMOLED & Audio",
                subtitleOd = "ଡାର୍କ ମୋଡ୍ ଓ ଭାଷା",
                badgeEn = "MATERIAL 3",
                icon = Icons.Default.Settings,
                iconTint = BentoSlate700,
                lightBg = BentoSlate100,
                lightBorder = BentoSlate200,
                testTag = "top_bar_settings_btn",
                onClickAction = {
                    onDismiss()
                    onOpenSettings()
                }
            )
        )
    }

    val filteredItems = remember(allSubMenuItems, selectedCategory, searchQuery) {
        allSubMenuItems.filter { item ->
            val matchesCat = selectedCategory == QuickHubMenuCategory.ALL || item.category == selectedCategory
            val q = searchQuery.trim().lowercase()
            val matchesQuery = q.isEmpty() ||
                item.titleEn.lowercase().contains(q) ||
                item.titleOd.lowercase().contains(q) ||
                item.subtitleEn.lowercase().contains(q) ||
                item.subtitleOd.lowercase().contains(q) ||
                item.badgeEn.lowercase().contains(q)
            matchesCat && matchesQuery
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = Modifier.testTag("balasore_quick_hub_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Executive Header Banner with Quick Dark Mode Toggle & Live Beacon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFF0F172A), OceanBlueDark, OceanBlue)
                        )
                    )
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Dashboard,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱର ୩୬୦° ମେନୁ ଓ ସବ୍-ମେନୁ" else "Baleshwar 360° Command Menu",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (isOdia) "ସମସ୍ତ ମୁଖ୍ୟ ବିଭାଗ, ସବ୍-ମେନୁ ଓ ସ୍ମାର୍ଟ ଟୁଲ୍ସ" else "Organized Main Sections, Sub-Menus & Utilities",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = Color(0xFFBAE6FD),
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick Dark Mode Toggle inside Mega-Menu Header
                        Surface(
                            onClick = { appTheme.onToggleDarkMode() },
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)),
                            modifier = Modifier.testTag("quick_hub_dark_mode_toggle_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                    contentDescription = "Toggle Dark Mode",
                                    tint = if (isDark) Color(0xFF38BDF8) else Color(0xFFFDE047),
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isDark) "DARK" else "LIGHT",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        // Live Beacon
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (isAutoRefreshActive) EmeraldGreen else Color(0xFFEF4444),
                                    modifier = Modifier.size(7.dp)
                                ) {}
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = if (isAutoRefreshActive) "LIVE" else "PAUSED",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // 2. Primary Navigation Menu Bar (Direct Jump to Any of the 6 Main Screens)
            if (onSelectPrimaryTab != null) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isOdia) "ମୁଖ୍ୟ ବିଭାଗ (MAIN NAVIGATION MENU)" else "PRIMARY NAVIGATION MENU",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.8.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val mainTabs = listOf(
                            Triple(0, if (isOdia) "🏖️ ପର୍ଯ୍ୟଟନ" else "🏖️ Explore", Icons.Default.Explore),
                            Triple(1, if (isOdia) "✨ ୩୬୦° ହବ୍" else "✨ 360° Hub", Icons.Default.AutoAwesome),
                            Triple(2, if (isOdia) "📰 ଖବର" else "📰 News", Icons.AutoMirrored.Filled.Article),
                            Triple(3, if (isOdia) "🌦️ ପାଣିପାଗ" else "🌦️ Weather", Icons.Default.Cloud),
                            Triple(4, if (isOdia) "🚨 ଜରୁରୀ ସେବା" else "🚨 SOS & Transit", Icons.Default.Emergency),
                            Triple(5, if (isOdia) "👤 ପ୍ରୋଫାଇଲ୍" else "👤 Profile", Icons.Default.Person)
                        )
                        mainTabs.forEach { (tabIdx, label, _) ->
                            val isActive = selectedTab == tabIdx
                            Surface(
                                onClick = {
                                    onDismiss()
                                    onSelectPrimaryTab(tabIdx)
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier.testTag("quick_hub_main_tab_$tabIdx")
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 3. Instant Sub-Menu Search & Category Filter Tabs
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isOdia) "ମେନୁ କିମ୍ବା ସବ୍-ମେନୁ ଖୋଜନ୍ତୁ (ଯଥା: ବସ୍, ମାନଚିତ୍ର, ଜୁଆର)..." else "Search menus & sub-menus (e.g. Map, Bus, Tide, QR, Forum)...",
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("quick_hub_search_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                QuickHubMenuCategory.entries.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = "${cat.emoji} ${if (isOdia) cat.labelOd else cat.labelEn}",
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("quick_hub_category_${cat.id.lowercase()}")
                    )
                }
            }

            // 4. Categorized Sub-Menu Grid (2 Columns per Row)
            Text(
                text = if (isOdia) {
                    "ସବ୍-ମେନୁ ଓ ସ୍ମାର୍ଟ ସୁବିଧାସମୂହ (${filteredItems.size})"
                } else {
                    "SUB-MENUS & SMART UTILITIES (${filteredItems.size})"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            filteredItems.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    rowItems.forEach { item ->
                        QuickHubTile(
                            title = if (isOdia) item.titleOd else item.titleEn,
                            subtitle = if (isOdia) item.subtitleOd else item.subtitleEn,
                            badge = item.badgeEn,
                            icon = item.icon,
                            iconTint = item.iconTint,
                            bgColor = if (isDark) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f) else item.lightBg,
                            borderColor = if (isDark) item.iconTint.copy(alpha = 0.45f) else item.lightBorder,
                            isDark = isDark,
                            customTag = item.testTag,
                            onClick = item.onClickAction,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 5. Emergency Helpline Callout Strip
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isDark) Color(0xFF450A0A) else Color(0xFFFEF2F2),
                border = BorderStroke(1.dp, if (isDark) Color(0xFFEF4444) else Color(0xFFFECACA)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        try {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:112")
                            }
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Dialer unavailable", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .testTag("quick_hub_emergency_112_btn")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isOdia) "୧୧୨ ଜରୁରୀକାଳୀନ ହେଲ୍ପଲାଇନ୍" else "Emergency 112 National Helpline",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color(0xFFFECACA) else Color(0xFF991B1B)
                                )
                            )
                            Text(
                                text = if (isOdia) "ପୋଲିସ, ଅଗ୍ନିଶମ ଓ ଆମ୍ବୁଲାନ୍ସ ତତ୍କାଳ ସହାୟତା" else "Instant 1-tap call for Police, Fire & Medical",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = if (isDark) Color(0xFFFCA5A5) else Color(0xFFB91C1C),
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDC2626)
                    ) {
                        Text(
                            text = "DIAL 112",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickHubTile(
    title: String,
    subtitle: String,
    badge: String,
    icon: ImageVector,
    iconTint: Color,
    bgColor: Color,
    borderColor: Color,
    isDark: Boolean,
    onClick: () -> Unit,
    customTag: String? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier
            .defaultMinSize(minHeight = 92.dp)
            .testTag(customTag ?: "quick_hub_tile_${title.lowercase().replace(" ", "_")}")
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (isDark) Color(0xFF0F172A) else Color.White.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isDark && iconTint == BentoSlate700) Color(0xFF38BDF8) else iconTint,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = iconTint.copy(alpha = if (isDark) 0.25f else 0.14f)
                ) {
                    Text(
                        text = badge,
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isDark && iconTint == BentoSlate700) Color(0xFFBAE6FD) else iconTint,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) Color.White else BentoSlate900,
                        fontSize = 13.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = if (isDark) Color(0xFFCBD5E1) else BentoSlate600,
                        fontSize = 10.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
