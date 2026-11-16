package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.heightIn
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.example.data.local.CivicReportEntity
import com.example.data.maps.BalasoreOsmOfflineTileProvider
import com.example.data.maps.CivicMarkerCluster
import com.example.data.maps.clusterCivicReports
import com.example.data.maps.createClusterMarkerDrawable
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoPrimaryBlue
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import android.content.Intent
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import android.view.MotionEvent
import org.osmdroid.views.overlay.Polyline
import com.example.util.BalasoreHapticHelper
import kotlin.math.roundToInt
import java.util.Locale

/**
 * Visual synchronization status representing report state relative to the Firestore cloud backend.
 */
enum class CivicSyncStatus(
    val labelEn: String,
    val labelOd: String,
    val badgeBg: Color,
    val badgeFg: Color,
    val iconEmoji: String,
    val syncDetailEn: String
) {
    DRAFT(
        labelEn = "Draft",
        labelOd = "ଡ୍ରାଫ୍ଟ",
        badgeBg = Color(0xFFFEF3C7),
        badgeFg = Color(0xFFD97706),
        iconEmoji = "📝",
        syncDetailEn = "Saved locally on device • Not synced to cloud"
    ),
    PENDING(
        labelEn = "Pending",
        labelOd = "ବକେୟା",
        badgeBg = Color(0xFFEFF6FF),
        badgeFg = Color(0xFF1D4ED8),
        iconEmoji = "⏳",
        syncDetailEn = "Queued in Room DB • Firestore sync pending"
    ),
    UPLOADED(
        labelEn = "Uploaded",
        labelOd = "ଅପଲୋଡ୍",
        badgeBg = Color(0xFFDCFCE7),
        badgeFg = Color(0xFF15803D),
        iconEmoji = "☁️",
        syncDetailEn = "Synchronized with Firestore backend ✓"
    )
}

/**
 * Derives the visual sync indicator for any civic report entity.
 */
fun getCivicSyncStatus(report: CivicReportEntity): CivicSyncStatus {
    return when {
        report.status.equals("DRAFT", ignoreCase = true) -> CivicSyncStatus.DRAFT
        report.status.equals("PENDING", ignoreCase = true) &&
                !report.resolutionNotes.contains("Synced to Firestore", ignoreCase = true) &&
                !report.resolutionNotes.contains("Auto-synced", ignoreCase = true) -> CivicSyncStatus.PENDING
        else -> CivicSyncStatus.UPLOADED
    }
}

/**
 * Calculates straight-line distance in meters using Haversine formula.
 */
fun calculateDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val earthRadius = 6371000.0 // meters
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return earthRadius * c
}

/**
 * Formats distance into clean human-readable meters or kilometers string.
 */
fun formatProximityDistance(meters: Double): String {
    return if (meters < 1000) {
        "${meters.toInt()}m away"
    } else {
        String.format(Locale.US, "%.1f km away", meters / 1000.0)
    }
}

/**
 * Public Amenity marker model for the Civic Issue Map Layer Toggle
 * (Drinking Water Stations & Public Toilets color-coded by verified maintenance status).
 */
data class BalasorePublicAmenityMarker(
    val id: String,
    val nameEn: String,
    val nameOd: String,
    val amenityType: String, // "DRINKING_WATER" or "PUBLIC_TOILET"
    val wardLocation: String,
    val latitude: Double,
    val longitude: Double,
    val maintenanceStatus: String, // "VERIFIED_OPERATIONAL", "SCHEDULED_MAINTENANCE", "NEEDS_REPAIR"
    val lastVerifiedNoteEn: String,
    val lastVerifiedNoteOd: String,
    val caretakerHelpline: String
)

val BALASORE_PUBLIC_AMENITIES_LAYER = listOf(
    BalasorePublicAmenityMarker(
        id = "amenity_water_station_square",
        nameEn = "WATCO Sujal 24x7 Drinking Water ATM • Station Square",
        nameOd = "ସୁଜଳ ୨୪x୭ ବିଶୁଦ୍ଧ ପାନୀୟ ଜଳ କେନ୍ଦ୍ର • ଷ୍ଟେସନ ଛକ",
        amenityType = "DRINKING_WATER",
        wardLocation = "Ward 14 • Railway Station & Bus Stand",
        latitude = 21.5004,
        longitude = 86.9201,
        maintenanceStatus = "VERIFIED_OPERATIONAL",
        lastVerifiedNoteEn = "UV & RO Filtration Verified Today • TDS 48 ppm • Chilled Tap Active",
        lastVerifiedNoteOd = "ଆଜି ଯାଞ୍ଚ ହୋଇଛି • ବିଶୁଦ୍ଧ ଥଣ୍ଡା ପାନୀୟ ଜଳ ଉପଲବ୍ଧ (TDS 48 ppm)",
        caretakerHelpline = "1800-345-6783"
    ),
    BalasorePublicAmenityMarker(
        id = "amenity_toilet_station_sulabh",
        nameEn = "Sulabh Smart Public Toilet Complex • Sahadevkhunta",
        nameOd = "ସୁଲଭ ସ୍ମାର୍ଟ ସାର୍ବଜନୀନ ଶୌଚାଳୟ • ସହଦେବଖୁଣ୍ଟା",
        amenityType = "PUBLIC_TOILET",
        wardLocation = "Ward 4 • Sahadevkhunta Bus Stand",
        latitude = 21.4988,
        longitude = 86.9230,
        maintenanceStatus = "VERIFIED_OPERATIONAL",
        lastVerifiedNoteEn = "Sanitized 40 mins ago • Water supply & disability ramp verified operational",
        lastVerifiedNoteOd = "୪୦ ମିନିଟ୍ ପୂର୍ବରୁ ସଫା ହୋଇଛି • ଜଳ ଓ ଦିବ୍ୟାଙ୍ଗ ରାମ୍ପ୍ ସୁବିଧା ଉପଲବ୍ଧ",
        caretakerHelpline = "06782-262044"
    ),
    BalasorePublicAmenityMarker(
        id = "amenity_water_cinema_chhak",
        nameEn = "Sujal Drink-From-Tap Water Kiosk • Cinema Chhak",
        nameOd = "ସୁଜଳ ପାନୀୟ ଜଳ କିଓସ୍କ • ସିନେମା ଛକ",
        amenityType = "DRINKING_WATER",
        wardLocation = "Ward 12 • Cinema Chhak Market",
        latitude = 21.4912,
        longitude = 86.9288,
        maintenanceStatus = "VERIFIED_OPERATIONAL",
        lastVerifiedNoteEn = "Chlorination & pressure verified by WATCO mobile lab • All 4 taps active",
        lastVerifiedNoteOd = "ୱାটକୋ (WATCO) ଦ୍ୱାରା ଜଳ ମାନ ପରୀକ୍ଷିତ • ୪ଟି ଟ୍ୟାପ୍ ସଚଳ ଅଛି",
        caretakerHelpline = "1800-345-6783"
    ),
    BalasorePublicAmenityMarker(
        id = "amenity_toilet_fmmch_gate",
        nameEn = "BMC Modular Bio-Toilet • FMMCH Hospital Gate",
        nameOd = "ପୌରପାଳିକା ବାୟୋ-ଶୌଚାଳୟ • ମେଡିକାଲ କଲେଜ ଗେଟ୍",
        amenityType = "PUBLIC_TOILET",
        wardLocation = "Ward 8 • Hospital Road",
        latitude = 21.4926,
        longitude = 86.9254,
        maintenanceStatus = "SCHEDULED_MAINTENANCE",
        lastVerifiedNoteEn = "Scheduled afternoon steam-cleaning & soap refill in progress (Reopens in 20 mins)",
        lastVerifiedNoteOd = "ନିୟମିତ ସଫେଇ ଓ ରକ୍ଷଣାବେକ୍ଷଣ ଚାଲିଛି (୨୦ ମିନିଟରେ ଖୋଲିବ)",
        caretakerHelpline = "06782-262115"
    ),
    BalasorePublicAmenityMarker(
        id = "amenity_water_nuabazar_mandi",
        nameEn = "Public RO Drinking Water Post • Nuabazar Sabzi Mandi",
        nameOd = "ସାର୍ବଜନୀନ ଆରଓ ପାନୀୟ ଜଳ କେନ୍ଦ୍ର • ନୂଆବଜାର ମଣ୍ଡି",
        amenityType = "DRINKING_WATER",
        wardLocation = "Ward 8 • Nuabazar Market",
        latitude = 21.4958,
        longitude = 86.9272,
        maintenanceStatus = "SCHEDULED_MAINTENANCE",
        lastVerifiedNoteEn = "RO membrane cartridge replacement underway by municipal technician",
        lastVerifiedNoteOd = "ଆରଓ ଫିଲ୍ଟର୍ ବଦଳାଯାଉଛି • ରକ୍ଷଣାବେକ୍ଷଣ ଚାଲୁଅଛି",
        caretakerHelpline = "1800-345-6783"
    ),
    BalasorePublicAmenityMarker(
        id = "amenity_toilet_remuna_golei",
        nameEn = "Highway Commuter Public Restroom • Remuna Golei",
        nameOd = "ଜାତୀୟ ରାଜପଥ ଯାତ୍ରୀ ଶୌଚାଳୟ • ରେମୁଣା ଗୋଲେଇ",
        amenityType = "PUBLIC_TOILET",
        wardLocation = "Ward 29 • Remuna Golei NH-16",
        latitude = 21.5280,
        longitude = 86.8760,
        maintenanceStatus = "NEEDS_REPAIR",
        lastVerifiedNoteEn = "Overhead pump valve fault reported • BMC plumbing team dispatched for repair",
        lastVerifiedNoteOd = "ପାଣି ପମ୍ପ ମରାମତି ପାଇଁ ପୌରପାଳିକା ଟିମ୍ ନିୟୋଜିତ",
        caretakerHelpline = "06782-262044"
    )
)

/**
 * Interactive 'Civic Issues Map' screen powered by OSMDroid.
 *
 * Features:
 * 1. Visualizes civic issue reports (Draft, Pending, Uploaded) as color-coded pins on an offline-ready map.
 * 2. Visual status indicator ('Pending', 'Uploaded', 'Draft') on each item indicating Firestore sync status.
 * 3. 'Civic Eye' gallery viewer displaying captured photos of reported potholes and waterlogging alongside pin details.
 * 4. Proximity radar calculating distance from citizen's location with nearest-first sorting.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivicIssuesMapScreen(
    language: AppLanguage = AppLanguage.ENGLISH,
    draftReports: List<CivicReportEntity> = emptyList(),
    userLatitude: Double = 21.4925,
    userLongitude: Double = 86.9312,
    onSubmitDraft: ((String) -> Unit)? = null,
    onEditDraft: ((CivicReportEntity) -> Unit)? = null,
    onDeleteDraft: ((String) -> Unit)? = null,
    onSnapNewDraft: (() -> Unit)? = null,
    onUpvoteReport: ((CivicReportEntity) -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var selectedReport by remember { mutableStateOf<CivicReportEntity?>(null) }
    var selectedCluster by remember { mutableStateOf<CivicMarkerCluster?>(null) }
    var isClusteringEnabled by remember { mutableStateOf(true) }
    var currentMapZoom by remember { mutableStateOf(13.2) }
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // ALL, DRAFT, PENDING, UPLOADED
    var selectedSortOrder by remember { mutableStateOf("PROXIMITY") } // PROXIMITY, URGENCY, RECENT
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    // Distance Ruler Tool States (Draw line between 2 points to measure real-world distance in km)
    var isRulerMode by remember { mutableStateOf(false) }
    var rulerPointA by remember { mutableStateOf<GeoPoint?>(null) }
    var rulerPointB by remember { mutableStateOf<GeoPoint?>(null) }

    // Gallery Viewer and Lightbox States
    var showGalleryViewer by remember { mutableStateOf(false) }
    var lightboxPhotoReport by remember { mutableStateOf<CivicReportEntity?>(null) }

    // Enhancements: Hazard Heatmap, Wayfinding HUD, Public Amenities Layer, and Impact Dialog States
    var isHeatmapEnabled by remember { mutableStateOf(false) }
    var showPublicAmenitiesLayer by remember { mutableStateOf(true) }
    var selectedPublicAmenity by remember { mutableStateOf<BalasorePublicAmenityMarker?>(null) }
    var navigatingToReport by remember { mutableStateOf<CivicReportEntity?>(null) }
    var showImpactStatsDialog by remember { mutableStateOf(false) }
    var showWardsDirectorySheet by remember { mutableStateOf(false) }
    var selectedWardFilter by remember { mutableStateOf<Int?>(null) }

    // Intercept hardware back button
    BackHandler {
        when {
            isRulerMode -> {
                isRulerMode = false
                rulerPointA = null
                rulerPointB = null
            }
            showWardsDirectorySheet -> showWardsDirectorySheet = false
            showImpactStatsDialog -> showImpactStatsDialog = false
            navigatingToReport != null -> navigatingToReport = null
            lightboxPhotoReport != null -> lightboxPhotoReport = null
            showGalleryViewer -> showGalleryViewer = false
            selectedReport != null -> selectedReport = null
            selectedCluster != null -> selectedCluster = null
            selectedWardFilter != null -> selectedWardFilter = null
            else -> onClose()
        }
    }

    // Initialize OSMDroid Configuration
    remember {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Offline-ready tile provider using applicationContext to prevent Activity BroadcastReceiver leaks
    val tileProvider = remember { BalasoreOsmOfflineTileProvider(context.applicationContext ?: context) }
    val isOfflineOnly by tileProvider.isOfflineOnlyMode.collectAsState()

    // Default seeded reports guaranteeing rich pothole & drainage pins
    val resolvedReports = remember(draftReports) {
        if (draftReports.isNotEmpty()) {
            draftReports
        } else {
            val now = System.currentTimeMillis()
            listOf(
                CivicReportEntity(
                    id = "BLS-DRAFT-2026-1042",
                    title = "Dangerous crater pothole cluster near Phandi Chhak",
                    category = "Road Pothole",
                    wardLocation = "Ward 14 - OT Road / Phandi Chhak",
                    description = "Snapped photo of 20cm asphalt depth crater. Poses skid hazard for two-wheelers during monsoon.",
                    status = "DRAFT",
                    urgency = "HIGH",
                    assignedDepartment = "PWD & Balasore Municipality Road Div",
                    reportedTimestamp = now - (45 * 60 * 1000L),
                    lastUpdatedTimestamp = now - (45 * 60 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://pothole_ot_road",
                    latitude = 21.4925,
                    longitude = 86.9312,
                    geoAddress = "OT Road, Phandi Chhak, Ward 14, Balasore",
                    resolutionNotes = "Local draft recorded via Civic Eye camera. Ready for submission."
                ),
                CivicReportEntity(
                    id = "BLS-CIVIC-2026-4821",
                    title = "Deep pothole cluster on OT Road Phandi Chhak",
                    category = "Road Pothole",
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
                    longitude = 86.9320,
                    geoAddress = "OT Road, Phandi Chhak, Balasore",
                    resolutionNotes = "Synced to Firestore. Road contractor dispatched with bitumen batch."
                ),
                CivicReportEntity(
                    id = "BLS-DRAFT-2026-1043",
                    title = "Waterlogging slush puddle near OT Road market",
                    category = "Drain Clog / Waterlogging",
                    wardLocation = "Ward 14 - OT Road / Phandi Chhak",
                    description = "Muddy slush accumulation in front of commercial shops due to broken curb channel.",
                    status = "DRAFT",
                    urgency = "HIGH",
                    assignedDepartment = "BMC Drainage Wing",
                    reportedTimestamp = now - (35 * 60 * 1000L),
                    lastUpdatedTimestamp = now - (35 * 60 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://drain_cinema_chhak",
                    latitude = 21.4928,
                    longitude = 86.9316,
                    geoAddress = "OT Road Near Phandi Chhak, Ward 14, Balasore",
                    resolutionNotes = "Local draft recorded via Civic Eye camera. High-density cluster with adjacent craters."
                ),
                CivicReportEntity(
                    id = "BLS-DRAFT-2026-1088",
                    title = "Monsoon storm drain clog & road waterlogging",
                    category = "Drain Clog / Waterlogging",
                    wardLocation = "Ward 12 - Cinema Chhak",
                    description = "Plastic blockage in culvert causing rain overflow onto merchant storefronts.",
                    status = "DRAFT",
                    urgency = "CRITICAL",
                    assignedDepartment = "Drainage Desiltation Taskforce",
                    reportedTimestamp = now - (20 * 60 * 1000L),
                    lastUpdatedTimestamp = now - (20 * 60 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://drain_cinema_chhak",
                    latitude = 21.4910,
                    longitude = 86.9265,
                    geoAddress = "Cinema Chhak, Ward 12, Balasore",
                    resolutionNotes = "Local draft recorded via Civic Eye GPS telemetry."
                ),
                CivicReportEntity(
                    id = "BLS-CIVIC-2026-1089",
                    title = "Deep asphalt gouge near Cinema Chhak junction",
                    category = "Road Pothole",
                    wardLocation = "Ward 12 - Cinema Chhak",
                    description = "Pothole filled with rainwater right at theater turn, creating blind spot risk.",
                    status = "PENDING",
                    urgency = "HIGH",
                    assignedDepartment = "PWD Road Div",
                    reportedTimestamp = now - (55 * 60 * 1000L),
                    lastUpdatedTimestamp = now - (55 * 60 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://pothole_ot_road",
                    latitude = 21.4914,
                    longitude = 86.9268,
                    geoAddress = "Cinema Chhak Junction, Balasore",
                    resolutionNotes = "Queued in Room DB for municipal maintenance upload."
                ),
                CivicReportEntity(
                    id = "BLS-CIVIC-2026-3914",
                    title = "Solid waste accumulation near Nuabazar Sabzi Mandi",
                    category = "Garbage Heap / Waste",
                    wardLocation = "Ward 4 - Sahadevkhunta",
                    description = "Vegetable waste spillover blocking drainage channels and road passage.",
                    status = "PENDING",
                    urgency = "NORMAL",
                    assignedDepartment = "BMC Sanitation Wing",
                    reportedTimestamp = now - (90 * 60 * 1000L),
                    lastUpdatedTimestamp = now - (90 * 60 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://drain_cinema_chhak",
                    latitude = 21.4975,
                    longitude = 86.9250,
                    geoAddress = "Nuabazar Market, Sahadevkhunta, Balasore",
                    resolutionNotes = "Submitted to municipal grievance queue. Awaiting cloud synchronization."
                ),
                CivicReportEntity(
                    id = "BLS-DRAFT-2026-1104",
                    title = "Waterlogged approach road near Sahadevkhunta Bus Stand",
                    category = "Drain Clog / Waterlogging",
                    wardLocation = "Ward 4 - Sahadevkhunta",
                    description = "Pedestrians unable to access interstate bus bays without wading through water.",
                    status = "DRAFT",
                    urgency = "HIGH",
                    assignedDepartment = "Drainage Desiltation Taskforce",
                    reportedTimestamp = now - (15 * 60 * 1000L),
                    lastUpdatedTimestamp = now - (15 * 60 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://drain_cinema_chhak",
                    latitude = 21.4981,
                    longitude = 86.9240,
                    geoAddress = "Sahadevkhunta Bus Stand Area, Ward 4, Balasore",
                    resolutionNotes = "Local draft recorded via Civic Eye GPS telemetry."
                ),
                CivicReportEntity(
                    id = "BLS-CIVIC-2026-2105",
                    title = "Solar streetlights not turning on along FM University Road",
                    category = "Faulty Streetlight",
                    wardLocation = "Ward 1 - Azimabad",
                    description = "Dark stretch at night causing safety concern for evening student commuters.",
                    status = "RESOLVED",
                    urgency = "NORMAL",
                    assignedDepartment = "TPNODL & Balasore Municipal Electrical Div",
                    reportedTimestamp = now - (24 * 3600 * 1000L),
                    lastUpdatedTimestamp = now - (2 * 3600 * 1000L),
                    hasPhotoAttached = false,
                    photoUri = null,
                    latitude = 21.5034,
                    longitude = 86.9180,
                    geoAddress = "FM University Approach Road, Ward 1, Balasore",
                    resolutionNotes = "Synced to Firestore. Charge controller replaced. Tested and functional."
                )
            )
        }
    }

    // Filter and sort
    val filteredReports = remember(resolvedReports, selectedCategoryFilter, selectedStatusFilter, selectedSortOrder, selectedWardFilter) {
        val categoryFiltered = when (selectedCategoryFilter) {
            "POTHOLE" -> resolvedReports.filter { it.category.contains("Pothole", ignoreCase = true) }
            "DRAINAGE" -> resolvedReports.filter { it.category.contains("Drain", ignoreCase = true) || it.category.contains("Water", ignoreCase = true) }
            "STREETLIGHT" -> resolvedReports.filter { it.category.contains("Streetlight", ignoreCase = true) || it.category.contains("Light", ignoreCase = true) }
            "GARBAGE" -> resolvedReports.filter { it.category.contains("Garbage", ignoreCase = true) || it.category.contains("Waste", ignoreCase = true) }
            else -> resolvedReports
        }

        val statusFiltered = when (selectedStatusFilter) {
            "DRAFT" -> categoryFiltered.filter { getCivicSyncStatus(it) == CivicSyncStatus.DRAFT }
            "PENDING" -> categoryFiltered.filter { getCivicSyncStatus(it) == CivicSyncStatus.PENDING }
            "UPLOADED" -> categoryFiltered.filter { getCivicSyncStatus(it) == CivicSyncStatus.UPLOADED }
            else -> categoryFiltered
        }

        val wardFiltered = if (selectedWardFilter != null) {
            statusFiltered.filter { it.wardLocation.contains("Ward $selectedWardFilter", ignoreCase = true) }
        } else {
            statusFiltered
        }

        when (selectedSortOrder) {
            "PROXIMITY" -> wardFiltered.sortedBy { report ->
                val rLat = report.latitude ?: userLatitude
                val rLng = report.longitude ?: userLongitude
                calculateDistanceMeters(userLatitude, userLongitude, rLat, rLng)
            }
            "URGENCY" -> wardFiltered.sortedByDescending {
                when (it.urgency) {
                    "CRITICAL" -> 3
                    "HIGH" -> 2
                    else -> 1
                }
            }
            else -> statusFiltered.sortedByDescending { it.reportedTimestamp }
        }
    }

    // Reports that have photos attached (specifically focusing on potholes and waterlogging)
    val photoReports = remember(resolvedReports) {
        resolvedReports.filter { it.hasPhotoAttached && !it.photoUri.isNullOrBlank() }
    }

    // Centroid-based proximity marker clusters calculated dynamically based on currentMapZoom
    val clusters = remember(filteredReports, currentMapZoom, isClusteringEnabled) {
        clusterCivicReports(
            reports = filteredReports,
            zoomLevel = currentMapZoom,
            userLat = userLatitude,
            userLng = userLongitude,
            clusteringEnabled = isClusteringEnabled
        )
    }

    // Lifecycle Observer for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewInstance?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewInstance?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapViewInstance?.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                mapViewInstance?.onPause()
                mapViewInstance?.onDetach()
                tileProvider.detach()
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (language == AppLanguage.ODIA) "ସିଭିକ୍ ଆଇ: ନାଗରିକ ସମସ୍ୟା ମ୍ୟାପ୍" else "Civic Issues Map",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = BentoSlate900
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981))
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = "${filteredReports.size} Issues (${clusters.size} Pins) • ${photoReports.size} Photos",
                                fontSize = 10.5.sp,
                                color = BentoSlate700
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("civic_map_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BentoSlate900
                        )
                    }
                },
                actions = {
                    // Balasore All 31 Municipal Wards Directory Button
                    IconButton(
                        onClick = { showWardsDirectorySheet = true },
                        modifier = Modifier.testTag("open_wards_directory_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationCity,
                            contentDescription = "All 31 Wards Directory",
                            tint = Color(0xFF0F172A)
                        )
                    }

                    // Civic Impact & Karma Stats Button
                    IconButton(
                        onClick = { showImpactStatsDialog = true },
                        modifier = Modifier.testTag("open_civic_impact_stats_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Impact & Karma Stats",
                            tint = Color(0xFFD97706)
                        )
                    }

                    // Civic Eye Photo Gallery Button
                    IconButton(
                        onClick = { showGalleryViewer = true },
                        modifier = Modifier.testTag("open_civic_gallery_top_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = "Photo Gallery",
                            tint = Color(0xFF0284C7)
                        )
                    }

                    // Distance Ruler Tool Button
                    IconButton(
                        onClick = {
                            isRulerMode = !isRulerMode
                            if (!isRulerMode) {
                                rulerPointA = null
                                rulerPointB = null
                            }
                            BalasoreHapticHelper.vibrateMarkerTap(context)
                            Toast.makeText(
                                context,
                                if (isRulerMode) "📏 Distance Ruler Active: Tap 2 points on map to measure" else "Distance ruler closed",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.testTag("civic_map_distance_ruler_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Directions,
                            contentDescription = "Distance Ruler Tool",
                            tint = if (isRulerMode) Color(0xFFEA580C) else BentoPrimaryBlue
                        )
                    }

                    // Recenter to user location
                    IconButton(
                        onClick = {
                            mapViewInstance?.controller?.apply {
                                setZoom(13.5)
                                animateTo(GeoPoint(userLatitude, userLongitude))
                            }
                            Toast.makeText(context, "Centered on your location (Balasore)", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("civic_map_recenter_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.GpsFixed,
                            contentDescription = "Recenter",
                            tint = BentoPrimaryBlue
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // OSMDroid Map Host with Marker Clustering
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("civic_issues_osm_map"),
                factory = { ctx ->
                    MapView(ctx, tileProvider).apply {
                        mapViewInstance = this
                        setTileSource(tileProvider.tileSource)
                        setUseDataConnection(!isOfflineOnly)
                        setMultiTouchControls(true)
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                        com.example.data.maps.BalasoreOsmDarkStyleHelper.applyMapColorStyle(this, true)

                        // Center on Balasore OT Road area
                        controller.setZoom(currentMapZoom)
                        controller.setCenter(GeoPoint(userLatitude, userLongitude))

                        // Dynamic zoom listener to recompute marker clusters
                        addMapListener(object : MapListener {
                            override fun onScroll(event: ScrollEvent?): Boolean = false
                            override fun onZoom(event: ZoomEvent?): Boolean {
                                event?.zoomLevel?.let { newZoom ->
                                    currentMapZoom = newZoom
                                }
                                return false
                            }
                        })

                        // Overlays: Compass & ScaleBar
                        val compassOverlay = CompassOverlay(ctx, InternalCompassOrientationProvider(ctx), this)
                        compassOverlay.enableCompass()
                        overlays.add(compassOverlay)

                        val scaleBarOverlay = ScaleBarOverlay(this)
                        scaleBarOverlay.setCentred(true)
                        overlays.add(scaleBarOverlay)

                        updateCivicMapPins(
                            mapView = this,
                            clusters = clusters,
                            userLat = userLatitude,
                            userLng = userLongitude,
                            context = ctx,
                            language = language,
                            isHeatmapEnabled = isHeatmapEnabled,
                            showPublicAmenitiesLayer = showPublicAmenitiesLayer,
                            publicAmenities = BALASORE_PUBLIC_AMENITIES_LAYER,
                            onPublicAmenityClick = { amenity ->
                                selectedReport = null
                                selectedCluster = null
                                selectedPublicAmenity = amenity
                                controller.animateTo(GeoPoint(amenity.latitude, amenity.longitude))
                            },
                            navigatingReport = navigatingToReport,
                            isRulerMode = isRulerMode,
                            rulerPointA = rulerPointA,
                            rulerPointB = rulerPointB,
                            onRulerTap = { geo ->
                                if (rulerPointA == null) {
                                    rulerPointA = geo
                                    rulerPointB = null
                                } else if (rulerPointB == null) {
                                    rulerPointB = geo
                                } else {
                                    rulerPointA = geo
                                    rulerPointB = null
                                }
                                BalasoreHapticHelper.vibrateMarkerTap(ctx)
                            },
                            onClusterClick = { cluster ->
                                selectedReport = null
                                selectedPublicAmenity = null
                                selectedCluster = cluster
                                if (cluster.size > 1) {
                                    try {
                                        zoomToBoundingBox(cluster.getBoundingBox(), true)
                                    } catch (_: Exception) {
                                        controller.animateTo(cluster.centerGeoPoint)
                                        controller.zoomIn()
                                    }
                                }
                            },
                            onReportClick = { report ->
                                selectedCluster = null
                                selectedPublicAmenity = null
                                selectedReport = report
                                controller.animateTo(GeoPoint(report.latitude ?: userLatitude, report.longitude ?: userLongitude))
                            }
                        )
                    }
                },
                update = { map ->
                    map.setUseDataConnection(!isOfflineOnly)
                    com.example.data.maps.BalasoreOsmDarkStyleHelper.applyMapColorStyle(map, true)
                    updateCivicMapPins(
                        mapView = map,
                        clusters = clusters,
                        userLat = userLatitude,
                        userLng = userLongitude,
                        context = context,
                        language = language,
                        isHeatmapEnabled = isHeatmapEnabled,
                        showPublicAmenitiesLayer = showPublicAmenitiesLayer,
                        publicAmenities = BALASORE_PUBLIC_AMENITIES_LAYER,
                        onPublicAmenityClick = { amenity ->
                            selectedReport = null
                            selectedCluster = null
                            selectedPublicAmenity = amenity
                            map.controller.animateTo(GeoPoint(amenity.latitude, amenity.longitude))
                        },
                        navigatingReport = navigatingToReport,
                        isRulerMode = isRulerMode,
                        rulerPointA = rulerPointA,
                        rulerPointB = rulerPointB,
                        onRulerTap = { geo ->
                            if (rulerPointA == null) {
                                rulerPointA = geo
                                rulerPointB = null
                            } else if (rulerPointB == null) {
                                rulerPointB = geo
                            } else {
                                rulerPointA = geo
                                rulerPointB = null
                            }
                            BalasoreHapticHelper.vibrateMarkerTap(context)
                        },
                        onClusterClick = { cluster ->
                            selectedReport = null
                            selectedPublicAmenity = null
                            selectedCluster = cluster
                            if (cluster.size > 1) {
                                try {
                                    map.zoomToBoundingBox(cluster.getBoundingBox(), true)
                                } catch (_: Exception) {
                                    map.controller.animateTo(cluster.centerGeoPoint)
                                    map.controller.zoomIn()
                                }
                            }
                        },
                        onReportClick = { report ->
                            selectedCluster = null
                            selectedPublicAmenity = null
                            selectedReport = report
                            map.controller.animateTo(GeoPoint(report.latitude ?: userLatitude, report.longitude ?: userLongitude))
                        }
                    )
                    map.invalidate()
                }
            )

            // Top Filter Bars: Clustering Toggle, Heatmap Toggle, Status Indicators & Categories
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White.copy(alpha = 0.96f))
                        .padding(vertical = 4.dp)
                ) {
                    // Row 0: MapView Settings Toggle Switch ('Live Online Map' vs 'Cached Offline Map')
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 3.dp)
                            .testTag("civic_map_view_settings_toggle_card"),
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOfflineOnly) Color(0xFFECFDF5) else Color(0xFFF0F9FF),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isOfflineOnly) Color(0xFF34D399) else Color(0xFF38BDF8)
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOfflineOnly) "Cached Offline Map Mode" else "Live Online Map Mode",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = if (isOfflineOnly) {
                                        "Zero data usage • Cached Balasore ward tiles active"
                                    } else {
                                        "Live online tile streaming • Toggle to save data"
                                    },
                                    fontSize = 10.sp,
                                    color = if (isOfflineOnly) Color(0xFF065F46) else Color(0xFF0369A1),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Switch(
                                checked = isOfflineOnly,
                                onCheckedChange = { newMode ->
                                    tileProvider.setOfflineOnly(newMode)
                                    mapViewInstance?.setUseDataConnection(!newMode)
                                    mapViewInstance?.invalidate()
                                    Toast.makeText(
                                        context,
                                        if (newMode) "Switched to Cached Offline Map Mode" else "Switched to Live Online Map Mode",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Color(0xFF10B981),
                                    uncheckedThumbColor = Color.White,
                                    uncheckedTrackColor = BentoPrimaryBlue
                                ),
                                modifier = Modifier.testTag("civic_map_view_settings_mode_switch")
                            )
                        }
                    }

                    // Row 1: Clustering Toggle, Hazard Heatmap Toggle & Visual Status Filter Chips ('Pending', 'Uploaded', 'Draft')
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val statusChips = listOf(
                            "ALL" to ("All (${resolvedReports.size})"),
                            "DRAFT" to ("📝 Draft (${resolvedReports.count { getCivicSyncStatus(it) == CivicSyncStatus.DRAFT }})"),
                            "PENDING" to ("⏳ Pending (${resolvedReports.count { getCivicSyncStatus(it) == CivicSyncStatus.PENDING }})"),
                            "UPLOADED" to ("☁️ Uploaded (${resolvedReports.count { getCivicSyncStatus(it) == CivicSyncStatus.UPLOADED }})")
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Marker Clustering Switch Chip
                            item {
                                FilterChip(
                                    selected = isClusteringEnabled,
                                    onClick = { isClusteringEnabled = !isClusteringEnabled },
                                    label = {
                                        Text(
                                            text = if (isClusteringEnabled) "⚡ Clustered (${clusters.count { it.size > 1 }} clusters)" else "📍 Unclustered",
                                            fontSize = 11.sp,
                                            fontWeight = if (isClusteringEnabled) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF6D28D9),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = BentoSlate800
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("civic_map_clustering_toggle")
                                )
                            }

                            // Hazard Radar Heatmap Toggle Chip
                            item {
                                FilterChip(
                                    selected = isHeatmapEnabled,
                                    onClick = { isHeatmapEnabled = !isHeatmapEnabled },
                                    label = {
                                        Text(
                                            text = if (isHeatmapEnabled) "🔥 Risk Heatmap ON" else "🔥 Risk Heatmap",
                                            fontSize = 11.sp,
                                            fontWeight = if (isHeatmapEnabled) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFDC2626),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = BentoSlate800
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("civic_map_heatmap_toggle")
                                )
                            }

                            // Public Amenities Layer Toggle Chip (Drinking Water & Public Toilets color-coded by Maintenance Status)
                            item {
                                FilterChip(
                                    selected = showPublicAmenitiesLayer,
                                    onClick = {
                                        showPublicAmenitiesLayer = !showPublicAmenitiesLayer
                                        if (!showPublicAmenitiesLayer) selectedPublicAmenity = null
                                    },
                                    label = {
                                        Text(
                                            text = if (showPublicAmenitiesLayer) {
                                                if (language == AppLanguage.ODIA) "🚰🚻 ପାନୀୟ ଜଳ ଓ ଶୌଚାଳୟ ON (${BALASORE_PUBLIC_AMENITIES_LAYER.size})"
                                                else "🚰🚻 Public Amenities ON (${BALASORE_PUBLIC_AMENITIES_LAYER.size})"
                                            } else {
                                                if (language == AppLanguage.ODIA) "🚰🚻 ପାନୀୟ ଜଳ ଓ ଶୌଚାଳୟ"
                                                else "🚰🚻 Public Amenities Layer"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = if (showPublicAmenitiesLayer) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0D9488),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = BentoSlate800
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("civic_map_public_amenities_toggle")
                                )
                            }

                            // All 31 Municipal Wards Selector Chip
                            item {
                                FilterChip(
                                    selected = selectedWardFilter != null,
                                    onClick = {
                                        if (selectedWardFilter != null) {
                                            selectedWardFilter = null
                                        } else {
                                            showWardsDirectorySheet = true
                                        }
                                    },
                                    label = {
                                        Text(
                                            text = if (selectedWardFilter != null) "🏛️ Ward $selectedWardFilter ✕" else "🏛️ Wards (31)",
                                            fontSize = 11.sp,
                                            fontWeight = if (selectedWardFilter != null) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFF0F172A),
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = BentoSlate800
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("civic_map_wards_directory_toggle")
                                )
                            }

                            items(statusChips) { (key, label) ->
                                val isSel = selectedStatusFilter == key
                                FilterChip(
                                    selected = isSel,
                                    onClick = { selectedStatusFilter = key },
                                    label = { Text(text = label, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when (key) {
                                            "DRAFT" -> Color(0xFFD97706)
                                            "PENDING" -> Color(0xFF2563EB)
                                            "UPLOADED" -> Color(0xFF15803D)
                                            else -> BentoPrimaryBlue
                                        },
                                        selectedLabelColor = Color.White,
                                        containerColor = Color(0xFFF1F5F9),
                                        labelColor = BentoSlate800
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }

                    // Row 2: Category Chips (Pothole & Waterlogging focus)
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val categories = listOf(
                            "ALL" to (if (language == AppLanguage.ODIA) "ସବୁ ବର୍ଗ" else "All Types"),
                            "POTHOLE" to (if (language == AppLanguage.ODIA) "🕳️ ରାସ୍ତା ଖାଲ" else "🕳️ Potholes"),
                            "DRAINAGE" to (if (language == AppLanguage.ODIA) "🌊 ଜଳବନ୍ଦୀ / ଡ୍ରେନ୍" else "🌊 Drainage & Water"),
                            "STREETLIGHT" to (if (language == AppLanguage.ODIA) "💡 ଲାଇଟ୍" else "💡 Lights"),
                            "GARBAGE" to (if (language == AppLanguage.ODIA) "🗑️ ଆବର୍ଜନା" else "🗑️ Waste")
                        )
                        items(categories) { (key, label) ->
                            val isSel = selectedCategoryFilter == key
                            FilterChip(
                                selected = isSel,
                                onClick = { selectedCategoryFilter = key },
                                label = { Text(text = label, fontSize = 10.5.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0F172A),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = BentoSlate700
                                ),
                                border = BorderStroke(1.dp, if (isSel) Color(0xFF0F172A) else BentoSlate200),
                                shape = RoundedCornerShape(6.dp)
                            )
                        }
                    }

                    // Row 3: Verified Maintenance Status Legend & Quick-Pick Amenity Pills when Public Amenities Layer is Active
                    if (showPublicAmenitiesLayer) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7),
                                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                ) {
                                    Text(
                                        text = "🟢 Verified Clean",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            item {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF3C7),
                                    border = BorderStroke(1.dp, Color(0xFFFDE68A))
                                ) {
                                    Text(
                                        text = "🟡 Maintenance",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            item {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEE2E2),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA))
                                ) {
                                    Text(
                                        text = "🔴 Needs Repair",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB91C1C),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            items(BALASORE_PUBLIC_AMENITIES_LAYER, key = { it.id }) { amenity ->
                                val statusDot = when (amenity.maintenanceStatus) {
                                    "VERIFIED_OPERATIONAL" -> "🟢"
                                    "SCHEDULED_MAINTENANCE" -> "🟡"
                                    else -> "🔴"
                                }
                                val typeEmoji = if (amenity.amenityType == "DRINKING_WATER") "🚰" else "🚻"
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (selectedPublicAmenity?.id == amenity.id) Color(0xFF0D9488) else Color(0xFFF0FDFA),
                                    border = BorderStroke(1.dp, Color(0xFF5EEAD4)),
                                    modifier = Modifier
                                        .clickable {
                                            selectedReport = null
                                            selectedCluster = null
                                            selectedPublicAmenity = amenity
                                            mapViewInstance?.controller?.apply {
                                                setZoom(15.5)
                                                animateTo(GeoPoint(amenity.latitude, amenity.longitude))
                                            }
                                        }
                                        .testTag("civic_map_amenity_pill_${amenity.id}")
                                ) {
                                    Text(
                                        text = "$statusDot $typeEmoji ${amenity.nameEn.substringBefore("•").trim()}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selectedPublicAmenity?.id == amenity.id) Color.White else Color(0xFF0F766E),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Active Turn-by-Turn Wayfinding & Direction HUD
                AnimatedVisibility(
                    visible = navigatingToReport != null,
                    enter = fadeIn() + slideInVertically(),
                    exit = fadeOut() + slideOutVertically(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    if (navigatingToReport != null) {
                        val target = navigatingToReport!!
                        val distM = calculateDistanceMeters(
                            userLatitude,
                            userLongitude,
                            target.latitude ?: userLatitude,
                            target.longitude ?: userLongitude
                        )
                        val distStr = formatProximityDistance(distM)
                        val carMin = ((distM / 600.0) + 1).toInt()
                        val autoMin = ((distM / 450.0) + 1).toInt()
                        val walkMin = ((distM / 80.0) + 1).toInt()

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = Color(0xFF0F172A),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.5.dp, Color(0xFF0284C7)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_navigation_hud")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
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
                                            color = Color(0xFF0284C7),
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    imageVector = Icons.Default.Navigation,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            }
                                        }
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                text = if (language == AppLanguage.ODIA) "ସକ୍ରିୟ ଦିଗ ନିର୍ଣ୍ଣୟ ରୁଟ୍" else "Active Wayfinding Route",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF38BDF8)
                                            )
                                            Text(
                                                text = target.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }

                                    IconButton(
                                        onClick = { navigatingToReport = null },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Exit Route",
                                            tint = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(8.dp))

                                // Proximity & ETA Options
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("📍 $distStr", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🚗 Car ~$carMin m", fontSize = 10.sp, color = Color(0xFF93C5FD))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🛺 Auto ~$autoMin m", fontSize = 10.sp, color = Color(0xFFFDE047))
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF1E293B)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🚶 Walk ~$walkMin m", fontSize = 10.sp, color = Color(0xFF86EFAC))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Floating Controls on Right
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Open Civic Eye Photo Gallery
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0284C7),
                    shadowElevation = 5.dp,
                    modifier = Modifier
                        .size(44.dp)
                        .clickable { showGalleryViewer = true }
                        .testTag("floating_gallery_fab")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.Collections, contentDescription = "Gallery", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }

                // Zoom In
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { mapViewInstance?.controller?.zoomIn() }
                        .testTag("civic_map_zoom_in")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = BentoSlate800)
                    }
                }

                // Zoom Out
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable { mapViewInstance?.controller?.zoomOut() }
                        .testTag("civic_map_zoom_out")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = BentoSlate800)
                    }
                }

                // Recenter
                Surface(
                    shape = CircleShape,
                    color = BentoPrimaryBlue,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(40.dp)
                        .clickable {
                            mapViewInstance?.controller?.apply {
                                setZoom(14.0)
                                animateTo(GeoPoint(userLatitude, userLongitude))
                            }
                        }
                        .testTag("civic_map_recenter_proximity_btn")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.MyLocation, contentDescription = "My Proximity", tint = Color.White, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Bottom Section: Either Selected Pin Detail Card, Selected Cluster Detail Card, OR Horizontal Carousel of Nearby Issues
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                if (selectedReport != null) {
                    // Detailed Pin Card showing photo evidence, Firestore sync status, and actions
                    CivicPinDetailCard(
                        report = selectedReport!!,
                        userLat = userLatitude,
                        userLng = userLongitude,
                        language = language,
                        onOpenGallery = { showGalleryViewer = true },
                        onPhotoClick = { lightboxPhotoReport = selectedReport },
                        onNavigate = {
                            navigatingToReport = selectedReport
                            selectedReport = null
                            mapViewInstance?.controller?.apply {
                                setZoom(15.0)
                                animateTo(GeoPoint(navigatingToReport!!.latitude ?: userLatitude, navigatingToReport!!.longitude ?: userLongitude))
                            }
                            Toast.makeText(context, "Wayfinding route activated!", Toast.LENGTH_SHORT).show()
                        },
                        onShare = {
                            val rep = selectedReport!!
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                val text = "🏛️ [Baleshwar 360 - Civic Eye Report]\n" +
                                    "Title: ${rep.title}\n" +
                                    "Category: ${rep.category}\n" +
                                    "Ward/Location: ${rep.wardLocation}\n" +
                                    "GPS: ${rep.latitude}, ${rep.longitude}\n" +
                                    "Status: ${getCivicSyncStatus(rep).labelEn}\n" +
                                    "Notes: ${rep.resolutionNotes}\n" +
                                    "Dispatched via Baleshwar 360 Civic Eye"
                                putExtra(Intent.EXTRA_SUBJECT, "Civic Grievance: ${rep.title}")
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Forward Civic Grievance"))
                        },
                        onSubmitDraft = {
                            onSubmitDraft?.invoke(selectedReport!!.id)
                            Toast.makeText(context, "Submitted & Synchronized with Firestore queue!", Toast.LENGTH_SHORT).show()
                            selectedReport = selectedReport!!.copy(
                                status = "PENDING",
                                resolutionNotes = "Submitted to municipal queue. Synced to Firestore."
                            )
                        },
                        onEditDraft = {
                            onEditDraft?.invoke(selectedReport!!)
                        },
                        onDismiss = { selectedReport = null }
                    )
                } else if (selectedCluster != null && selectedCluster!!.size > 1) {
                    // Detailed High-Density Cluster Card showing aggregated counts and all issues
                    CivicClusterDetailCard(
                        cluster = selectedCluster!!,
                        userLat = userLatitude,
                        userLng = userLongitude,
                        language = language,
                        onReportClick = { report ->
                            selectedCluster = null
                            selectedReport = report
                            mapViewInstance?.controller?.animateTo(GeoPoint(report.latitude ?: userLatitude, report.longitude ?: userLongitude))
                        },
                        onZoomInToDisperse = {
                            mapViewInstance?.apply {
                                try {
                                    zoomToBoundingBox(selectedCluster!!.getBoundingBox(), true)
                                } catch (_: Exception) {
                                    controller.animateTo(selectedCluster!!.centerGeoPoint)
                                    controller.zoomIn()
                                }
                            }
                        },
                        onDismiss = { selectedCluster = null }
                    )
                } else {
                    // Nearby Issues Proximity Carousel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "📍 ଆଖପାଖ ସମସ୍ୟା (${filteredReports.size})" else "📍 Nearby Issues (${filteredReports.size})",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "• Tap to focus",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Gallery quick shortcut
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE)),
                                    modifier = Modifier
                                        .clickable { showGalleryViewer = true }
                                        .testTag("carousel_open_gallery_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("📸", fontSize = 11.sp)
                                        Spacer(Modifier.width(3.dp))
                                        Text("Photos (${photoReports.size})", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BentoPrimaryBlue)
                                    }
                                }

                                if (onSnapNewDraft != null) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier
                                            .clickable { onSnapNewDraft() }
                                            .testTag("civic_map_snap_draft_fab")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                            Spacer(Modifier.width(4.dp))
                                            Text("+ Snap", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        }
                                    }
                                }
                            }
                        }

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(filteredReports, key = { it.id }) { report ->
                                val rLat = report.latitude ?: userLatitude
                                val rLng = report.longitude ?: userLongitude
                                val distanceMeters = calculateDistanceMeters(userLatitude, userLongitude, rLat, rLng)
                                val distanceText = formatProximityDistance(distanceMeters)

                                NearbyReportMiniCard(
                                    report = report,
                                    distanceText = distanceText,
                                    onClick = {
                                        selectedReport = report
                                        mapViewInstance?.controller?.apply {
                                            setZoom(15.0)
                                            animateTo(GeoPoint(rLat, rLng))
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Selected Public Amenity Detail Card (Color-Coded by Verified Maintenance Status)
            AnimatedVisibility(
                visible = selectedPublicAmenity != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 14.dp, vertical = 18.dp)
            ) {
                selectedPublicAmenity?.let { amenity ->
                    val (statusLabel, statusBg, statusFg) = when (amenity.maintenanceStatus) {
                        "VERIFIED_OPERATIONAL" -> Triple(
                            if (language == AppLanguage.ODIA) "ଯାଞ୍ଚ ହୋଇଛି • ସଚଳ" else "VERIFIED OPERATIONAL",
                            Color(0xFFDCFCE7),
                            Color(0xFF15803D)
                        )
                        "SCHEDULED_MAINTENANCE" -> Triple(
                            if (language == AppLanguage.ODIA) "ନିୟମିତ ରକ୍ଷଣାବେକ୍ଷଣ" else "SCHEDULED MAINTENANCE",
                            Color(0xFFFEF3C7),
                            Color(0xFFB45309)
                        )
                        else -> Triple(
                            if (language == AppLanguage.ODIA) "ମରାମତି ଆବଶ୍ୟକ" else "NEEDS REPAIR",
                            Color(0xFFFEE2E2),
                            Color(0xFFB91C1C)
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("civic_public_amenity_detail_card"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(1.5.dp, statusFg)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statusBg
                                ) {
                                    Text(
                                        text = "${if (amenity.amenityType == "DRINKING_WATER") "🚰 DRINKING WATER" else "🚻 PUBLIC TOILET"} • $statusLabel",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = statusFg,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                IconButton(
                                    onClick = { selectedPublicAmenity = null },
                                    modifier = Modifier.size(26.dp)
                                ) {
                                    Icon(Icons.Default.Clear, contentDescription = "Close Amenity Card", tint = BentoSlate600)
                                }
                            }
                            Text(
                                text = if (language == AppLanguage.ODIA) amenity.nameOd else amenity.nameEn,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = BentoSlate900
                            )
                            Text(
                                text = "📍 ${amenity.wardLocation} • ${formatProximityDistance(calculateDistanceMeters(userLatitude, userLongitude, amenity.latitude, amenity.longitude))}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F766E)
                            )
                            Text(
                                text = if (language == AppLanguage.ODIA) amenity.lastVerifiedNoteOd else amenity.lastVerifiedNoteEn,
                                fontSize = 11.5.sp,
                                color = BentoSlate700
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        val uri = android.net.Uri.parse("google.navigation:q=${amenity.latitude},${amenity.longitude}&mode=w")
                                        val navIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                            setPackage("com.google.android.apps.maps")
                                        }
                                        runCatching { context.startActivity(navIntent) }.onFailure {
                                            runCatching {
                                                context.startActivity(
                                                    Intent(
                                                        Intent.ACTION_VIEW,
                                                        android.net.Uri.parse("geo:${amenity.latitude},${amenity.longitude}?q=${android.net.Uri.encode(amenity.nameEn)}")
                                                    )
                                                )
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.DirectionsWalk, contentDescription = null, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (language == AppLanguage.ODIA) "ଚାଲି ଚାଲି ଯାଆନ୍ତୁ" else "Walk to Amenity", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = {
                                        runCatching {
                                            context.startActivity(
                                                Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:${amenity.caretakerHelpline}"))
                                            )
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, statusFg),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("📞 Caretaker (${amenity.caretakerHelpline})", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = statusFg)
                                }
                            }
                        }
                    }
                }
            }

            // Distance Ruler Floating HUD
            AnimatedVisibility(
                visible = isRulerMode,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    border = BorderStroke(1.5.dp, Color(0xFF38BDF8)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("civic_distance_ruler_hud")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("📏", fontSize = 18.sp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଦୂରତା ମାପକ (Distance Ruler)" else "Civic Distance Ruler",
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                            IconButton(
                                onClick = {
                                    isRulerMode = false
                                    rulerPointA = null
                                    rulerPointB = null
                                },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Clear, contentDescription = "Close Ruler", tint = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        if (rulerPointA == null) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ପ୍ରଥମ ବିନ୍ଦୁ (A) ସ୍ଥିର କରିବାକୁ ମ୍ୟାପ୍‌ରେ ଟ୍ୟାପ୍ କରନ୍ତୁ" else "📍 Step 1: Tap anywhere on map to place Point A (Start).",
                                color = Color(0xFF94A3B8),
                                fontSize = 12.sp
                            )
                        } else if (rulerPointB == null) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ବିନ୍ଦୁ A ସ୍ଥିର ହେଲା। ଦ୍ୱିତୀୟ ବିନ୍ଦୁ (B) ପାଇଁ ଅନ୍ୟ ସ୍ଥାନରେ ଟ୍ୟାପ୍ କରନ୍ତୁ" else "🎯 Step 2: Point A set. Tap another location to place Point B (End).",
                                color = Color(0xFFFBBF24),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        } else {
                            val distMeters = calculateDistanceMeters(rulerPointA!!.latitude, rulerPointA!!.longitude, rulerPointB!!.latitude, rulerPointB!!.longitude)
                            val distKm = distMeters / 1000.0
                            val walkMin = kotlin.math.max(1, (distMeters / 80.0).roundToInt())
                            val driveMin = kotlin.math.max(1, (distMeters / 500.0).roundToInt())
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = String.format(Locale.US, "%.2f km", distKm),
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF38BDF8)
                                    )
                                    Text(
                                        text = "${distMeters.roundToInt()} m straight-line • ~${walkMin}m walk • ~${driveMin}m drive",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFFE2E8F0)
                                    )
                                }
                                OutlinedButton(
                                    onClick = {
                                        rulerPointA = null
                                        rulerPointB = null
                                    },
                                    border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text("Reset", color = Color(0xFF38BDF8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Civic Eye Gallery Viewer
    if (showGalleryViewer) {
        CivicEyeGalleryViewerSheet(
            reports = resolvedReports,
            userLat = userLatitude,
            userLng = userLongitude,
            language = language,
            onLocateOnMap = { report ->
                showGalleryViewer = false
                selectedReport = report
                val rLat = report.latitude ?: userLatitude
                val rLng = report.longitude ?: userLongitude
                mapViewInstance?.controller?.apply {
                    setZoom(15.5)
                    animateTo(GeoPoint(rLat, rLng))
                }
            },
            onPhotoClick = { report ->
                lightboxPhotoReport = report
            },
            onUpvoteReport = { report ->
                onUpvoteReport?.invoke(report)
                BalasoreHapticHelper.vibrateSuccess(context)
                Toast.makeText(context, "Upvoted report! Increased community visibility.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { showGalleryViewer = false }
        )
    }

    // Full-screen Lightbox Dialog for high-resolution photo examination
    if (lightboxPhotoReport != null) {
        CivicPhotoLightboxDialog(
            report = lightboxPhotoReport!!,
            userLat = userLatitude,
            userLng = userLongitude,
            language = language,
            onUpvoteReport = { report ->
                onUpvoteReport?.invoke(report)
                BalasoreHapticHelper.vibrateSuccess(context)
                Toast.makeText(context, "Upvoted report! Increased community visibility.", Toast.LENGTH_SHORT).show()
            },
            onDismiss = { lightboxPhotoReport = null }
        )
    }

    // Civic Eye SLA Impact & Citizen Karma Dialog
    if (showImpactStatsDialog) {
        CivicEyeImpactDialog(
            language = language,
            onOpenWardsDirectory = {
                showImpactStatsDialog = false
                showWardsDirectorySheet = true
            },
            onDismiss = { showImpactStatsDialog = false }
        )
    }

    // Municipal Wards Directory Full-Screen Dialog (All 31 Balasore Municipality Wards)
    if (showWardsDirectorySheet) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showWardsDirectorySheet = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                com.example.ui.features.municipal.BalasoreWardsDirectorySheet(
                    language = language,
                    onLocateWardOnMap = { ward ->
                        showWardsDirectorySheet = false
                        selectedWardFilter = ward.wardNumber
                        mapViewInstance?.controller?.apply {
                            setZoom(15.5)
                            animateTo(GeoPoint(ward.latitude, ward.longitude))
                        }
                        Toast.makeText(context, "Centered map on ${ward.formattedDisplayNameEn}", Toast.LENGTH_SHORT).show()
                    },
                    onReportCivicIssueInWard = { ward ->
                        showWardsDirectorySheet = false
                        onSnapNewDraft?.invoke()
                    },
                    onClose = { showWardsDirectorySheet = false }
                )
            }
        }
    }
}

/**
 * Overlay for detecting map taps to place ruler markers when Distance Ruler mode is active.
 */
class CivicRulerOverlay(
    private val onMapTapped: (GeoPoint) -> Unit
) : Overlay() {
    override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
        if (mapView == null || e == null) return false
        val geo = mapView.projection.fromPixels(e.x.toInt(), e.y.toInt()) as? GeoPoint
        if (geo != null) {
            onMapTapped(geo)
            return true
        }
        return false
    }
}

private fun createRulerPinDrawable(context: Context, label: String, colorInt: Long): BitmapDrawable {
    val bitmap = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = colorInt.toInt()
        style = Paint.Style.FILL
    }
    canvas.drawCircle(36f, 36f, 32f, paint)
    paint.color = android.graphics.Color.WHITE
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 4f
    canvas.drawCircle(36f, 36f, 32f, paint)

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = android.graphics.Color.WHITE
        textSize = 34f
        textAlign = Paint.Align.CENTER
        typeface = android.graphics.Typeface.DEFAULT_BOLD
    }
    val yPos = (36f - ((textPaint.descent() + textPaint.ascent()) / 2))
    canvas.drawText(label, 36f, yPos, textPaint)
    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Updates OSMDroid map markers for all civic reports and clusters with distinct category icons,
 * cluster counts, sync state badges, risk heatmaps, active route overlays, and distance ruler tool.
 */
private fun updateCivicMapPins(
    mapView: MapView,
    clusters: List<CivicMarkerCluster>,
    userLat: Double,
    userLng: Double,
    context: Context,
    language: AppLanguage,
    isHeatmapEnabled: Boolean,
    showPublicAmenitiesLayer: Boolean = true,
    publicAmenities: List<BalasorePublicAmenityMarker> = emptyList(),
    onPublicAmenityClick: ((BalasorePublicAmenityMarker) -> Unit)? = null,
    navigatingReport: CivicReportEntity?,
    isRulerMode: Boolean = false,
    rulerPointA: GeoPoint? = null,
    rulerPointB: GeoPoint? = null,
    onRulerTap: ((GeoPoint) -> Unit)? = null,
    onClusterClick: (CivicMarkerCluster) -> Unit,
    onReportClick: (CivicReportEntity) -> Unit
) {
    mapView.overlays.removeAll { it is Marker || it is CivicHeatmapOverlay || it is CivicRouteOverlay || it is Polyline || it is CivicRulerOverlay }

    // Distance Ruler Touch Handler
    if (isRulerMode && onRulerTap != null) {
        mapView.overlays.add(CivicRulerOverlay(onRulerTap))
    }

    // Ruler Point A & Point B markers and connecting Polyline
    if (rulerPointA != null) {
        val markerA = Marker(mapView).apply {
            position = rulerPointA
            title = "Ruler: Point A (Start)"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = createRulerPinDrawable(context, "A", 0xFF10B981)
        }
        mapView.overlays.add(markerA)
    }
    if (rulerPointB != null) {
        val markerB = Marker(mapView).apply {
            position = rulerPointB
            title = "Ruler: Point B (End)"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = createRulerPinDrawable(context, "B", 0xFFEF4444)
        }
        mapView.overlays.add(markerB)
    }
    if (rulerPointA != null && rulerPointB != null) {
        val rulerLine = Polyline(mapView).apply {
            addPoint(rulerPointA)
            addPoint(rulerPointB)
            outlinePaint.color = 0xFFF59E0B.toInt()
            outlinePaint.strokeWidth = 9f
            outlinePaint.style = Paint.Style.STROKE
            val dist = calculateDistanceMeters(rulerPointA.latitude, rulerPointA.longitude, rulerPointB.latitude, rulerPointB.longitude)
            title = "Distance: ${formatProximityDistance(dist)}"
        }
        mapView.overlays.add(rulerLine)
    }

    // 1. Heatmap Risk Zone Radar Overlay (if enabled)
    if (isHeatmapEnabled) {
        mapView.overlays.add(CivicHeatmapOverlay(clusters))
    }

    // 2. Active Vector Route Corridor Overlay (if navigating)
    if (navigatingReport != null) {
        mapView.overlays.add(
            CivicRouteOverlay(
                startLat = userLat,
                startLng = userLng,
                destLat = navigatingReport.latitude ?: userLat,
                destLng = navigatingReport.longitude ?: userLng
            )
        )
    }

    // 1. User Location Pin
    val userMarker = Marker(mapView).apply {
        position = GeoPoint(userLat, userLng)
        title = if (language == AppLanguage.ODIA) "ଆପଣଙ୍କ ସ୍ଥିତି" else "You Are Here"
        snippet = "Balasore OT Road • Center of Proximity Radar"
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
        icon = createUserLocationDrawable(context)
    }
    mapView.overlays.add(userMarker)

    // 2. Clustered or Individual Civic Issue Pins
    for (cluster in clusters) {
        if (cluster.isSingle) {
            val report = cluster.singleReport ?: continue
            val lat = report.latitude ?: userLat
            val lng = report.longitude ?: userLng
            val distMeters = calculateDistanceMeters(userLat, userLng, lat, lng)
            val distStr = formatProximityDistance(distMeters)
            val syncStatus = getCivicSyncStatus(report)

            val marker = Marker(mapView).apply {
                position = GeoPoint(lat, lng)
                title = "[${syncStatus.labelEn.uppercase()}] ${report.title}"
                snippet = "${report.category} • $distStr • ${syncStatus.syncDetailEn}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = createCivicMarkerDrawable(context, report, syncStatus)
                setOnMarkerClickListener { _, _ ->
                    onReportClick(report)
                    showInfoWindow()
                    true
                }
            }
            mapView.overlays.add(marker)
        } else {
            // High-Density Multi-Report Cluster Pin
            val marker = Marker(mapView).apply {
                position = cluster.centerGeoPoint
                title = "⚡ ${cluster.size} Clustered Issues"
                snippet = "Potholes: ${cluster.potholeCount} • Drains: ${cluster.drainageCount} • ${cluster.draftCount} Drafts, ${cluster.uploadedCount} Synced"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = createClusterMarkerDrawable(context, cluster)
                setOnMarkerClickListener { _, _ ->
                    onClusterClick(cluster)
                    showInfoWindow()
                    true
                }
            }
            mapView.overlays.add(marker)
        }
    }

    // 3. Public Amenities Layer Markers (Drinking Water Stations & Public Toilets Color-Coded by Maintenance Status)
    if (showPublicAmenitiesLayer) {
        for (amenity in publicAmenities) {
            val statusColorHex = when (amenity.maintenanceStatus) {
                "VERIFIED_OPERATIONAL" -> 0xFF16A34AL
                "SCHEDULED_MAINTENANCE" -> 0xFFD97706L
                else -> 0xFFDC2626L
            }
            val shortBadge = if (amenity.amenityType == "DRINKING_WATER") "W" else "T"
            val amenityMarker = Marker(mapView).apply {
                position = GeoPoint(amenity.latitude, amenity.longitude)
                title = if (language == AppLanguage.ODIA) amenity.nameOd else amenity.nameEn
                snippet = "${amenity.wardLocation} • ${amenity.lastVerifiedNoteEn}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = createRulerPinDrawable(context, shortBadge, statusColorHex)
                setOnMarkerClickListener { _, _ ->
                    onPublicAmenityClick?.invoke(amenity)
                    showInfoWindow()
                    true
                }
            }
            mapView.overlays.add(amenityMarker)
        }
    }
}

/**
 * Detailed bottom card shown when a multi-issue cluster marker is clicked.
 * Provides a breakdown of drafted and synced issues in this high-density area,
 * with quick-zoom disperse action and ability to view any individual issue.
 */
@Composable
fun CivicClusterDetailCard(
    cluster: CivicMarkerCluster,
    userLat: Double,
    userLng: Double,
    language: AppLanguage,
    onReportClick: (CivicReportEntity) -> Unit,
    onZoomInToDisperse: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("civic_cluster_detail_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, BentoSlate200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Cluster Title, High-Density badge, and Close Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFEDE9FE),
                        border = BorderStroke(1.dp, Color(0xFFC4B5FD))
                    ) {
                        Text(
                            text = "⚡ High-Density Cluster",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6D28D9),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "${cluster.size} Reports Overlapping",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate900
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", tint = BentoSlate600)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Status Breakdown Row ('Draft', 'Pending', 'Uploaded') & Disperse Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (cluster.draftCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CivicSyncStatus.DRAFT.badgeBg,
                        border = BorderStroke(1.dp, CivicSyncStatus.DRAFT.badgeFg.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "📝 ${cluster.draftCount} Draft",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CivicSyncStatus.DRAFT.badgeFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (cluster.pendingCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CivicSyncStatus.PENDING.badgeBg,
                        border = BorderStroke(1.dp, CivicSyncStatus.PENDING.badgeFg.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "⏳ ${cluster.pendingCount} Pending",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CivicSyncStatus.PENDING.badgeFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                if (cluster.uploadedCount > 0) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = CivicSyncStatus.UPLOADED.badgeBg,
                        border = BorderStroke(1.dp, CivicSyncStatus.UPLOADED.badgeFg.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "☁️ ${cluster.uploadedCount} Synced",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = CivicSyncStatus.UPLOADED.badgeFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(Modifier.weight(1f))

                // Zoom to Disperse Button
                Button(
                    onClick = onZoomInToDisperse,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier
                        .height(28.dp)
                        .testTag("cluster_zoom_disperse_btn")
                ) {
                    Icon(imageVector = Icons.Default.ZoomIn, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(Modifier.width(3.dp))
                    Text("Disperse", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Scrollable list of reports in this cluster
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 210.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(cluster.reports, key = { it.id }) { report ->
                    val rLat = report.latitude ?: userLat
                    val rLng = report.longitude ?: userLng
                    val distMeters = calculateDistanceMeters(userLat, userLng, rLat, rLng)
                    val distText = formatProximityDistance(distMeters)
                    val syncStatus = getCivicSyncStatus(report)

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, BentoSlate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onReportClick(report) }
                            .testTag("cluster_item_${report.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail / Icon
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                if (report.photoUri != null && (report.photoUri.startsWith("content://") || report.photoUri.startsWith("file://"))) {
                                    AsyncImage(
                                        model = report.photoUri,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                } else {
                                    Text(
                                        text = if (report.category.contains("Drain") || report.category.contains("Water")) "🌊" else "🕳️",
                                        fontSize = 18.sp
                                    )
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = report.title,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlate900,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = syncStatus.badgeBg
                                    ) {
                                        Text(
                                            text = syncStatus.labelEn,
                                            fontSize = 8.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = syncStatus.badgeFg,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }

                                Spacer(Modifier.height(2.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${report.category} • $distText",
                                        fontSize = 9.5.sp,
                                        color = BentoSlate600,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "Inspect →",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimaryBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Creates custom circular pin drawable with category emoji and visual sync indicator badge.
 */
private fun createCivicMarkerDrawable(
    context: Context,
    report: CivicReportEntity,
    syncStatus: CivicSyncStatus
): BitmapDrawable {
    val size = 110
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val (emoji, pinColor) = when {
        report.category.contains("Pothole", ignoreCase = true) || report.category.contains("Road", ignoreCase = true) ->
            "🕳️" to android.graphics.Color.rgb(234, 88, 12) // Orange
        report.category.contains("Water", ignoreCase = true) || report.category.contains("Drain", ignoreCase = true) ->
            "🌊" to android.graphics.Color.rgb(2, 132, 199) // Sky Blue
        report.category.contains("Light", ignoreCase = true) ->
            "💡" to android.graphics.Color.rgb(217, 119, 6) // Amber
        report.category.contains("Waste", ignoreCase = true) || report.category.contains("Garbage", ignoreCase = true) ->
            "🗑️" to android.graphics.Color.rgb(22, 163, 74) // Green
        else ->
            "⚠️" to android.graphics.Color.rgb(220, 38, 38)
    }

    // Shadow
    val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(70, 0, 0, 0)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(55f, 62f, 40f, shadowPaint)

    // Primary circle pin
    val bgPaint = Paint().apply {
        isAntiAlias = true
        color = pinColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(55f, 52f, 40f, bgPaint)

    // Inner white core
    val whitePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(55f, 52f, 29f, whitePaint)

    // Category emoji
    val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 34f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(emoji, 55f, 64f, textPaint)

    // Visual Status Pill on Top (Draft = Amber, Pending = Blue, Uploaded = Green)
    val badgePaint = Paint().apply {
        isAntiAlias = true
        color = when (syncStatus) {
            CivicSyncStatus.DRAFT -> android.graphics.Color.rgb(245, 158, 11)
            CivicSyncStatus.PENDING -> android.graphics.Color.rgb(37, 99, 235)
            CivicSyncStatus.UPLOADED -> android.graphics.Color.rgb(22, 163, 74)
        }
        style = Paint.Style.FILL
    }
    canvas.drawRoundRect(14f, 2f, 96f, 24f, 10f, 10f, badgePaint)

    val badgeTextPaint = Paint().apply {
        isAntiAlias = true
        textSize = 13.5f
        color = android.graphics.Color.WHITE
        textAlign = Paint.Align.CENTER
        isFakeBoldText = true
    }
    canvas.drawText(syncStatus.labelEn.uppercase(), 55f, 18.5f, badgeTextPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Creates distinct blue pulsing icon for the user's location.
 */
private fun createUserLocationDrawable(context: Context): BitmapDrawable {
    val size = 72
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val haloPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(60, 2, 132, 199)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(36f, 36f, 32f, haloPaint)

    val corePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.rgb(2, 132, 199)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(36f, 36f, 18f, corePaint)

    val whitePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(36f, 36f, 7f, whitePaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Detailed bottom card shown when a pin is selected on the map.
 * Displays photo evidence, Firestore visual status indicator, and municipal action triggers.
 */
@Composable
fun CivicPinDetailCard(
    report: CivicReportEntity,
    userLat: Double,
    userLng: Double,
    language: AppLanguage,
    onOpenGallery: () -> Unit,
    onPhotoClick: () -> Unit,
    onNavigate: () -> Unit = {},
    onShare: () -> Unit = {},
    onSubmitDraft: () -> Unit,
    onEditDraft: () -> Unit,
    onDismiss: () -> Unit
) {
    val rLat = report.latitude ?: userLat
    val rLng = report.longitude ?: userLng
    val distanceMeters = calculateDistanceMeters(userLat, userLng, rLat, rLng)
    val distanceText = formatProximityDistance(distanceMeters)
    val syncStatus = getCivicSyncStatus(report)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp)
            .testTag("civic_map_selected_draft_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        border = BorderStroke(1.dp, BentoSlate200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Visual Status Indicator Badge + Category + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Visual Status Indicator ('Pending', 'Uploaded', 'Draft')
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = syncStatus.badgeBg,
                        border = BorderStroke(1.dp, syncStatus.badgeFg.copy(alpha = 0.4f)),
                        modifier = Modifier.testTag("visual_status_indicator_${report.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(syncStatus.iconEmoji, fontSize = 11.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) syncStatus.labelOd else syncStatus.labelEn,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = syncStatus.badgeFg
                            )
                        }
                    }

                    Spacer(Modifier.width(6.dp))

                    // Category Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(report.category, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = BentoPrimaryBlue)
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", tint = BentoSlate600, modifier = Modifier.size(18.dp))
                }
            }

            Spacer(Modifier.height(8.dp))

            // Title
            Text(
                text = report.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoSlate900,
                lineHeight = 20.sp
            )

            // Firestore Sync Telemetry Details Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when (syncStatus) {
                        CivicSyncStatus.UPLOADED -> Icons.Default.CloudDone
                        CivicSyncStatus.PENDING -> Icons.Default.Sync
                        CivicSyncStatus.DRAFT -> Icons.Default.CloudUpload
                    },
                    contentDescription = null,
                    tint = syncStatus.badgeFg,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(5.dp))
                Text(
                    text = syncStatus.syncDetailEn,
                    fontSize = 11.sp,
                    color = syncStatus.badgeFg,
                    fontWeight = FontWeight.Medium
                )
            }

            // Proximity & Ward Address
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(BorderStroke(1.dp, BentoSlate200), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.NearMe,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(15.dp)
                )
                Spacer(Modifier.width(6.dp))
                Column {
                    Text(
                        text = "📍 $distanceText from your location",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate900
                    )
                    Text(
                        text = report.geoAddress ?: report.wardLocation,
                        fontSize = 10.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            // 'Civic Eye' Photo Evidence Thumbnail alongside pin details
            if (report.hasPhotoAttached && !report.photoUri.isNullOrBlank()) {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPhotoClick() }
                        .testTag("pin_photo_evidence_card"),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(modifier = Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                            if (report.photoUri.startsWith("content://") || report.photoUri.startsWith("file://")) {
                                AsyncImage(
                                    model = report.photoUri,
                                    contentDescription = "Civic Eye Photo",
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (report.category.contains("Drain")) Color(0xFF0284C7) else Color(0xFFEA580C)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(if (report.category.contains("Drain")) "🌊" else "🕳️", fontSize = 26.sp)
                                }
                            }

                            Spacer(Modifier.width(10.dp))

                            Column {
                                Text(
                                    text = "📸 Civic Eye Field Evidence",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = "Tap to enlarge photo lightbox",
                                    fontSize = 10.sp,
                                    color = Color(0xFF0284C7),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "GPS Tagged: ${report.latitude ?: 21.4925}° N, ${report.longitude ?: 86.9312}° E",
                                    fontSize = 9.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }

                        IconButton(
                            onClick = onOpenGallery,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Collections, contentDescription = "View in Gallery", tint = Color(0xFF0284C7))
                        }
                    }
                }
            }

            // Description
            if (report.description.isNotBlank()) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = report.description,
                    fontSize = 12.sp,
                    color = BentoSlate700,
                    lineHeight = 17.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.height(10.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (syncStatus == CivicSyncStatus.DRAFT) {
                    // Submit & Sync Button
                    Button(
                        onClick = onSubmitDraft,
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("submit_draft_from_map_btn"),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ଦାଖଲ ଓ ଅପଲୋଡ୍ କରନ୍ତୁ" else "Submit & Sync",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Edit Draft Button
                    OutlinedButton(
                        onClick = onEditDraft,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.0f)
                            .testTag("edit_draft_from_map_btn"),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(13.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ସମ୍ପାଦନ" else "Edit Draft",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                } else {
                    // Uploaded or Pending Report View
                    OutlinedButton(
                        onClick = onOpenGallery,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("view_in_gallery_btn"),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Collections, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ଫଟୋ ଗ୍ୟାଲେରୀରେ ଦେଖନ୍ତୁ" else "View in Gallery",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Secondary Row: Directions (Wayfinding HUD) and Share Issue (Grievance Forwarding)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Directions Button
                OutlinedButton(
                    onClick = onNavigate,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0284C7)),
                    border = BorderStroke(1.dp, Color(0xFF0284C7)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("pin_navigate_btn"),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଦିଗ ନିର୍ଣ୍ଣୟ" else "Directions",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Share / Escalate Grievance
                OutlinedButton(
                    onClick = onShare,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF15803D)),
                    border = BorderStroke(1.dp, Color(0xFF15803D)),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_pin_btn"),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (language == AppLanguage.ODIA) "ସେୟାର୍ କରନ୍ତୁ" else "Share Issue",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Small horizontal card for the bottom carousel.
 * Shows status indicator ('Draft', 'Pending', 'Uploaded') and distance.
 */
@Composable
fun NearbyReportMiniCard(
    report: CivicReportEntity,
    distanceText: String,
    onClick: () -> Unit
) {
    val syncStatus = getCivicSyncStatus(report)

    Surface(
        modifier = Modifier
            .width(230.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .testTag("nearby_report_mini_card_${report.id}"),
        color = Color.White,
        shadowElevation = 3.dp,
        border = BorderStroke(1.dp, BentoSlate200)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Top Row: Visual Status Indicator Badge ('Pending', 'Uploaded', 'Draft') + Distance
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = syncStatus.badgeBg,
                    border = BorderStroke(1.dp, syncStatus.badgeFg.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "${syncStatus.iconEmoji} ${syncStatus.labelEn.uppercase()}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = syncStatus.badgeFg,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Text(
                    text = distanceText,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF15803D)
                )
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text = report.title,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = BentoSlate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = report.wardLocation,
                fontSize = 10.sp,
                color = BentoSlate600,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = report.category,
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BentoPrimaryBlue
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (report.hasPhotoAttached) {
                        Text("📷 ", fontSize = 9.5.sp)
                    }
                    Text(
                        text = "Focus Pin →",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }
            }
        }
    }
}

/**
 * 'Civic Eye' Gallery Viewer:
 * Displays all captured photos of reported potholes and waterlogging in an interactive modal viewer.
 * Citizens can filter by issue type, view visual sync indicators, and tap any photo to locate its pin on the map.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivicEyeGalleryViewerSheet(
    reports: List<CivicReportEntity>,
    userLat: Double,
    userLng: Double,
    language: AppLanguage,
    onLocateOnMap: (CivicReportEntity) -> Unit,
    onPhotoClick: (CivicReportEntity) -> Unit,
    onUpvoteReport: ((CivicReportEntity) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var galleryCategoryFilter by remember { mutableStateOf("ALL") } // ALL, POTHOLE, DRAINAGE
    var galleryStatusFilter by remember { mutableStateOf("ALL") } // ALL, DRAFT, PENDING, UPLOADED

    // Filter reports with photos
    val photoReports = remember(reports, galleryCategoryFilter, galleryStatusFilter) {
        reports.filter { it.hasPhotoAttached && !it.photoUri.isNullOrBlank() }
            .filter { report ->
                when (galleryCategoryFilter) {
                    "POTHOLE" -> report.category.contains("Pothole", ignoreCase = true)
                    "DRAINAGE" -> report.category.contains("Drain", ignoreCase = true) || report.category.contains("Water", ignoreCase = true)
                    else -> true
                }
            }
            .filter { report ->
                when (galleryStatusFilter) {
                    "DRAFT" -> getCivicSyncStatus(report) == CivicSyncStatus.DRAFT
                    "PENDING" -> getCivicSyncStatus(report) == CivicSyncStatus.PENDING
                    "UPLOADED" -> getCivicSyncStatus(report) == CivicSyncStatus.UPLOADED
                    else -> true
                }
            }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("civic_eye_gallery_viewer_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ସିଭିକ୍ ଆଇ ଫଟୋ ଗ୍ୟାଲେରୀ" else "Civic Eye Evidence Gallery",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = BentoSlate900
                    )
                    Text(
                        text = if (language == AppLanguage.ODIA) "ରାସ୍ତା ଖାଲ ଓ ଜଳବନ୍ଦୀ ଫଟୋ ପ୍ରମାଣ ସଂଗ୍ରହ (${photoReports.size})" else "Captured Pothole & Drainage Field Photos (${photoReports.size})",
                        fontSize = 11.5.sp,
                        color = BentoSlate600
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", tint = BentoSlate800)
                }
            }

            Spacer(Modifier.height(10.dp))

            // Category Filter Tabs (All Photos, Potholes, Waterlogging)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(BentoSlate100)
                    .padding(3.dp)
            ) {
                listOf(
                    "ALL" to (if (language == AppLanguage.ODIA) "ସବୁ ଫଟୋ" else "All Photos"),
                    "POTHOLE" to (if (language == AppLanguage.ODIA) "🕳️ ରାସ୍ତା ଖାଲ" else "🕳️ Potholes"),
                    "DRAINAGE" to (if (language == AppLanguage.ODIA) "🌊 ଜଳବନ୍ଦୀ" else "🌊 Waterlogging")
                ).forEach { (key, title) ->
                    val isSel = galleryCategoryFilter == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) Color.White else Color.Transparent)
                            .clickable { galleryCategoryFilter = key }
                            .padding(vertical = 7.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            fontSize = 11.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) BentoPrimaryBlue else BentoSlate700
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Status Filter Row ('Draft', 'Pending', 'Uploaded')
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(
                    "ALL" to "All Status",
                    "DRAFT" to "📝 Drafts Only",
                    "PENDING" to "⏳ Pending Sync",
                    "UPLOADED" to "☁️ Uploaded to Firestore"
                ).forEach { (key, label) ->
                    val isSel = galleryStatusFilter == key
                    item {
                        FilterChip(
                            selected = isSel,
                            onClick = { galleryStatusFilter = key },
                            label = { Text(text = label, fontSize = 10.5.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0F172A),
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFFF1F5F9),
                                labelColor = BentoSlate800
                            ),
                            shape = RoundedCornerShape(6.dp)
                        )
                    }
                }
            }

            Spacer(Modifier.height(10.dp))

            if (photoReports.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📸", fontSize = 36.sp)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ଏହି ବର୍ଗରେ କୌଣସି ଫଟୋ ପ୍ରମାଣ ନାହିଁ" else "No photos found matching this filter",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate700
                        )
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(photoReports, key = { it.id }) { report ->
                        GalleryPhotoCard(
                            report = report,
                            userLat = userLat,
                            userLng = userLng,
                            language = language,
                            onPhotoClick = { onPhotoClick(report) },
                            onLocatePin = { onLocateOnMap(report) },
                            onUpvote = { onUpvoteReport?.invoke(report) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual card inside the Civic Eye Photo Gallery grid.
 */
@Composable
fun GalleryPhotoCard(
    report: CivicReportEntity,
    userLat: Double,
    userLng: Double,
    language: AppLanguage,
    onPhotoClick: () -> Unit,
    onLocatePin: () -> Unit,
    onUpvote: (() -> Unit)? = null
) {
    val rLat = report.latitude ?: userLat
    val rLng = report.longitude ?: userLng
    val distanceMeters = calculateDistanceMeters(userLat, userLng, rLat, rLng)
    val distanceText = formatProximityDistance(distanceMeters)
    val syncStatus = getCivicSyncStatus(report)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onPhotoClick() }
            .testTag("gallery_photo_item_${report.id}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BentoSlate200)
    ) {
        Column {
            // Photo Container with Visual Status Indicator Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(115.dp)
                    .background(Color(0xFFE2E8F0))
            ) {
                if (report.photoUri != null && (report.photoUri.startsWith("content://") || report.photoUri.startsWith("file://"))) {
                    AsyncImage(
                        model = report.photoUri,
                        contentDescription = "Pothole / Drainage Photo",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(if (report.category.contains("Drain")) Color(0xFF0284C7) else Color(0xFFEA580C)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (report.category.contains("Drain")) "🌊" else "🕳️", fontSize = 36.sp)
                    }
                }

                // Visual Status Indicator Pill ('Pending', 'Uploaded', 'Draft')
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = syncStatus.badgeBg,
                    border = BorderStroke(1.dp, syncStatus.badgeFg.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "${syncStatus.iconEmoji} ${syncStatus.labelEn.uppercase()}",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = syncStatus.badgeFg,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                // Proximity Distance Chip in bottom left of photo
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color.Black.copy(alpha = 0.65f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp)
                ) {
                    Text(
                        text = "📍 $distanceText",
                        fontSize = 9.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                    )
                }
            }

            // Body
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    text = report.title,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = report.wardLocation,
                    fontSize = 9.5.sp,
                    color = Color(0xFF64748B),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onUpvote != null) {
                        OutlinedButton(
                            onClick = onUpvote,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimaryBlue),
                            shape = RoundedCornerShape(6.dp),
                            border = BorderStroke(1.dp, BentoPrimaryBlue.copy(alpha = 0.5f)),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("gallery_upvote_btn_${report.id}")
                        ) {
                            Text("👍 ${report.upvotesCount}", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Locate on Map Action
                    Button(
                        onClick = onLocatePin,
                        colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryBlue),
                        shape = RoundedCornerShape(6.dp),
                        contentPadding = PaddingValues(horizontal = 6.dp, vertical = 4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("locate_pin_btn_${report.id}")
                    ) {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(11.dp))
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ମ୍ୟାପ୍" else "Locate",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Fullscreen Lightbox Dialog to view captured photos with details.
 */
@Composable
fun CivicPhotoLightboxDialog(
    report: CivicReportEntity,
    userLat: Double,
    userLng: Double,
    language: AppLanguage,
    onUpvoteReport: ((CivicReportEntity) -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val syncStatus = getCivicSyncStatus(report)
    val rLat = report.latitude ?: userLat
    val rLng = report.longitude ?: userLng
    val distanceMeters = calculateDistanceMeters(userLat, userLng, rLat, rLng)
    val distanceText = formatProximityDistance(distanceMeters)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black.copy(alpha = 0.94f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Lightbox Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = syncStatus.badgeBg
                    ) {
                        Text(
                            text = "${syncStatus.iconEmoji} ${syncStatus.labelEn.uppercase()}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = syncStatus.badgeFg,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(36.dp)
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                    ) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", tint = Color.White)
                    }
                }

                // Photo Center
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (report.photoUri != null && (report.photoUri.startsWith("content://") || report.photoUri.startsWith("file://"))) {
                        AsyncImage(
                            model = report.photoUri,
                            contentDescription = "Full evidence photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Fit
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (report.category.contains("Drain")) Color(0xFF0284C7) else Color(0xFFEA580C)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(if (report.category.contains("Drain")) "🌊" else "🕳️", fontSize = 80.sp)
                        }
                    }
                }

                // Bottom Details
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                    border = BorderStroke(1.dp, Color(0xFF334155))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = report.title,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "📍 $distanceText • ${report.wardLocation}",
                            fontSize = 11.sp,
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "GPS Coordinates: ${report.latitude ?: 21.4925}° N, ${report.longitude ?: 86.9312}° E",
                            fontSize = 10.sp,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = syncStatus.syncDetailEn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = syncStatus.badgeFg
                        )

                        if (onUpvoteReport != null) {
                            Spacer(Modifier.height(10.dp))
                            Button(
                                onClick = { onUpvoteReport(report) },
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryBlue),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("lightbox_upvote_btn_${report.id}")
                            ) {
                                Text("👍 Upvote & Support Report (${report.upvotesCount})", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom canvas overlay that renders soft translucent danger heatmaps around high-density
 * pothole and flood hazard clusters in Balasore.
 */
class CivicHeatmapOverlay(
    private val clusters: List<CivicMarkerCluster>
) : Overlay() {
    private val heatPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val strokePaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.STROKE
        strokeWidth = 3f
    }
    private val labelPaint = Paint().apply {
        isAntiAlias = true
        textSize = 28f
        isFakeBoldText = true
        textAlign = Paint.Align.CENTER
    }
    private val labelBgPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = android.graphics.Color.argb(210, 15, 23, 42)
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val projection = mapView.projection ?: return

        for (cluster in clusters) {
            val centerGeo = cluster.centerGeoPoint
            val screenPt = android.graphics.Point()
            projection.toPixels(centerGeo, screenPt)

            val isSevere = cluster.potholeCount >= 2 || cluster.drainageCount >= 2 || cluster.size >= 3
            val radius = if (cluster.size > 1) (55 + cluster.size * 16).toFloat() else 42f

            // Outer danger halo
            heatPaint.color = if (isSevere) {
                android.graphics.Color.argb(55, 239, 68, 68) // Red translucent
            } else {
                android.graphics.Color.argb(50, 245, 158, 11) // Amber translucent
            }
            canvas.drawCircle(screenPt.x.toFloat(), screenPt.y.toFloat(), radius * 1.5f, heatPaint)

            // Inner core zone
            heatPaint.color = if (isSevere) {
                android.graphics.Color.argb(90, 239, 68, 68)
            } else {
                android.graphics.Color.argb(80, 245, 158, 11)
            }
            canvas.drawCircle(screenPt.x.toFloat(), screenPt.y.toFloat(), radius, heatPaint)

            // Border ring
            strokePaint.color = if (isSevere) {
                android.graphics.Color.argb(160, 220, 38, 38)
            } else {
                android.graphics.Color.argb(160, 217, 119, 6)
            }
            canvas.drawCircle(screenPt.x.toFloat(), screenPt.y.toFloat(), radius, strokePaint)

            // Cluster hazard tag on screen
            if (cluster.size > 1) {
                val labelText = "⚠️ Risk Zone (${cluster.size} hazards)"
                val textWidth = labelPaint.measureText(labelText)
                val textY = screenPt.y.toFloat() - radius - 14f
                val rect = android.graphics.RectF(
                    screenPt.x.toFloat() - (textWidth / 2f) - 14f,
                    textY - 26f,
                    screenPt.x.toFloat() + (textWidth / 2f) + 14f,
                    textY + 10f
                )
                canvas.drawRoundRect(rect, 8f, 8f, labelBgPaint)

                labelPaint.color = android.graphics.Color.WHITE
                canvas.drawText(labelText, screenPt.x.toFloat(), textY, labelPaint)
            }
        }
    }
}

/**
 * Vector route overlay drawing an active offline path between citizen and selected civic issue.
 */
class CivicRouteOverlay(
    private val startLat: Double,
    private val startLng: Double,
    private val destLat: Double,
    private val destLng: Double
) : Overlay() {
    private val glowPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(90, 2, 132, 199)
        strokeWidth = 18f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val linePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.rgb(2, 132, 199)
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }
    private val dashPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        strokeWidth = 3f
        style = Paint.Style.STROKE
        pathEffect = android.graphics.DashPathEffect(floatArrayOf(15f, 15f), 0f)
    }
    private val pinPaint = Paint().apply {
        isAntiAlias = true
        style = Paint.Style.FILL
        color = android.graphics.Color.rgb(220, 38, 38)
    }

    override fun draw(canvas: Canvas, mapView: MapView, shadow: Boolean) {
        if (shadow) return
        val projection = mapView.projection ?: return

        val startPt = android.graphics.Point()
        val destPt = android.graphics.Point()
        projection.toPixels(GeoPoint(startLat, startLng), startPt)
        projection.toPixels(GeoPoint(destLat, destLng), destPt)

        val sx = startPt.x.toFloat()
        val sy = startPt.y.toFloat()
        val dx = destPt.x.toFloat()
        val dy = destPt.y.toFloat()

        // Outer glow corridor
        canvas.drawLine(sx, sy, dx, dy, glowPaint)
        // Core corridor
        canvas.drawLine(sx, sy, dx, dy, linePaint)
        // Center dash
        canvas.drawLine(sx, sy, dx, dy, dashPaint)

        // Midpoint waypoint indicator
        val midX = (sx + dx) / 2f
        val midY = (sy + dy) / 2f
        canvas.drawCircle(midX, midY, 8f, pinPaint)
    }
}

/**
 * Citizen Karma & Civic Eye SLA Impact Dialog displaying municipal resolution statistics,
 * citizen contribution XP, and ward repair leaderboards.
 */
@Composable
fun CivicEyeImpactDialog(
    language: AppLanguage,
    onOpenWardsDirectory: () -> Unit = {},
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .testTag("civic_eye_impact_dialog"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFFEF3C7),
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text("🏆", fontSize = 20.sp)
                            }
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ସିଭିକ୍ ଆଇ ଇମ୍ପାକ୍ଟ ଡ୍ୟାସବୋର୍ଡ" else "Civic Eye Impact & Karma",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate900
                            )
                            Text(
                                text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ପୌରପାଳିକା ସ୍ୱଚ୍ଛତା ରିପୋର୍ଟ" else "Balasore Municipal Resolution SLA",
                                fontSize = 10.5.sp,
                                color = BentoSlate600
                            )
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Close", tint = BentoSlate600)
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Citizen Karma Card
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଆପଣଙ୍କ ନାଗରିକ କର୍ମା" else "YOUR CITIZEN KARMA",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "480 XP • Level 4",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFF1E293B)
                            ) {
                                Text(
                                    text = "🎖️ Ward Vigilante",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { 0.72f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF38BDF8),
                            trackColor = Color(0xFF334155)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "72/100 XP to Level 5 (Civic Sentinel)",
                            fontSize = 9.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))

                // SLA & City Metrics 3-Col Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFFA7F3D0))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("84.2%", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                            Text("SLA Fixed", fontSize = 9.sp, color = Color(0xFF065F46))
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFEFF6FF),
                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("36 hrs", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = BentoPrimaryBlue)
                            Text("Avg Response", fontSize = 9.sp, color = Color(0xFF1E40AF))
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF2F2),
                        border = BorderStroke(1.dp, Color(0xFFFECACA))
                    ) {
                        Column(
                            modifier = Modifier.padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("18", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                            Text("Active Hazards", fontSize = 9.sp, color = Color(0xFF991B1B))
                        }
                    }
                }

                Spacer(Modifier.height(14.dp))

                // Ward Activity Leaderboard
                Text(
                    text = if (language == AppLanguage.ODIA) "ୱାର୍ଡ ଅନୁସାରେ ସମାଧାନ ତାଲିକା" else "Ward Resolution Leaderboard",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate900
                )
                Spacer(Modifier.height(6.dp))

                listOf(
                    Triple("🥇 Ward 14 (OT Road / Phandi)", "92% Resolved (46/50)", Color(0xFF059669)),
                    Triple("🥈 Ward 12 (Cinema Chhak)", "86% Resolved (37/43)", Color(0xFF2563EB)),
                    Triple("🥉 Ward 7 (Station Bazar)", "78% Resolved (25/32)", Color(0xFFD97706))
                ).forEach { (ward, stat, color) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ward, fontSize = 10.5.sp, fontWeight = FontWeight.Medium, color = BentoSlate800)
                        Text(stat, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                }

                Spacer(Modifier.height(14.dp))

                androidx.compose.material3.OutlinedButton(
                    onClick = onOpenWardsDirectory,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_all_31_wards_from_impact_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = BentoPrimaryBlue
                    ),
                    border = BorderStroke(1.dp, BentoPrimaryBlue)
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA) "🏛️ ସମସ୍ତ ୩୧ଟି ମ୍ୟୁନିସିପାଲିଟି ୱାର୍ଡ ଦେଖନ୍ତୁ" else "🏛️ Browse All 31 Municipal Wards",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryBlue)
                ) {
                    Text("Close Impact Report", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
