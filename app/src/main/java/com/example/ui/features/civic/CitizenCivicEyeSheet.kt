package com.example.ui.features.civic

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.draw.scale
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Traffic
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import com.example.data.municipal.BalasoreMunicipalWards
import com.example.data.municipal.BalasoreWard
import com.example.data.municipal.MunicipalZone
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.example.data.local.CivicReportEntity
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoBlueLight
import com.example.ui.theme.BentoCardWhite
import com.example.ui.theme.BentoPrimaryBlue
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate400
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import kotlin.random.Random

data class CivicIssue(
    val id: String,
    val title: String,
    val odiaTitle: String,
    val category: String,
    val location: String,
    val odiaLocation: String,
    val description: String,
    val status: String, // "PENDING", "SUBMITTED", "IN_PROGRESS", "RESOLVED"
    val assignedDepartment: String,
    val reportedTime: String,
    val upvotes: Int,
    val isUpvotedByUser: Boolean = false,
    val trackingId: String,
    val reportedTimestampMs: Long = System.currentTimeMillis() - 2 * 3600_000L,
    val latitude: Double = 21.4925,
    val longitude: Double = 86.9312,
    val hasPhotoAttached: Boolean = true,
    val photoUri: String? = "preset://pothole_ot_road"
)

/**
 * Formats a Unix epoch timestamp (ms) into a human-readable relative string
 * (e.g., 'Just now', '15 mins ago', '2 hours ago', 'Yesterday', '3 days ago')
 * so citizens can immediately gauge the freshness of infrastructure issues.
 */
fun formatCivicRelativeTimestamp(
    timestampMs: Long,
    language: AppLanguage = AppLanguage.ENGLISH,
    nowMs: Long = System.currentTimeMillis()
): String {
    val diffMs = (nowMs - timestampMs).coerceAtLeast(0L)
    val minutes = diffMs / 60_000L
    val hours = diffMs / 3_600_000L
    val days = diffMs / 86_400_000L

    return if (language == AppLanguage.ODIA) {
        when {
            minutes < 1L -> "ଏଇমাত্র (Just now)"
            minutes < 60L -> "$minutes ମିନିଟ୍ ପୂର୍ବରୁ ($minutes mins ago)"
            hours == 1L -> "୧ ଘଣ୍ଟା ପୂର୍ବରୁ (1 hour ago)"
            hours < 24L -> "$hours ଘଣ୍ଟା ପୂର୍ବରୁ ($hours hours ago)"
            days == 1L -> "ଗତକାଲି (Yesterday)"
            days < 7L -> "$days ଦିନ ପୂର୍ବରୁ ($days days ago)"
            else -> "${days / 7} ସପ୍ତାହ ପୂର୍ବରୁ (${days / 7}w ago)"
        }
    } else {
        when {
            minutes < 1L -> "Just now"
            minutes == 1L -> "1 min ago"
            minutes < 60L -> "$minutes mins ago"
            hours == 1L -> "1 hour ago"
            hours < 24L -> "$hours hours ago"
            days == 1L -> "Yesterday"
            days < 7L -> "$days days ago"
            else -> "${days / 7} weeks ago"
        }
    }
}

/**
 * Visual Progress Stepper Component for the Citizen Civic Eye report details view.
 * Clearly indicates the 3 resolution stages:
 *   Stage 1: Pending (Logged & queued for inspection)
 *   Stage 2: In-Progress (Field engineer / municipal crew assigned)
 *   Stage 3: Resolved (Verified & closed in ward registry)
 */
@Composable
fun CivicResolutionProgressStepper(
    status: String,
    language: AppLanguage,
    trackingId: String = "",
    modifier: Modifier = Modifier
) {
    val normalizedStatus = when (status.uppercase()) {
        "RESOLVED", "COMPLETED", "CLOSED" -> "RESOLVED"
        "IN_PROGRESS", "IN-PROGRESS", "ACTIVE", "ASSIGNED" -> "IN_PROGRESS"
        else -> "PENDING"
    }
    val currentStageIndex = when (normalizedStatus) {
        "RESOLVED" -> 2
        "IN_PROGRESS" -> 1
        else -> 0
    }

    data class ResolutionStageInfo(
        val key: String,
        val titleEn: String,
        val titleOd: String,
        val subtitleEn: String,
        val subtitleOd: String,
        val activeColor: Color
    )

    val stages = listOf(
        ResolutionStageInfo(
            key = "PENDING",
            titleEn = "Pending",
            titleOd = "ବକେୟା (Pending)",
            subtitleEn = "Logged in Ward Desk",
            subtitleOd = "ୱାର୍ଡ ଡେସ୍କରେ ପଞ୍ଜୀକୃତ",
            activeColor = Color(0xFFD97706)
        ),
        ResolutionStageInfo(
            key = "IN_PROGRESS",
            titleEn = "In-Progress",
            titleOd = "ଚାଲୁଅଛି (In-Progress)",
            subtitleEn = "Field Crew Dispatched",
            subtitleOd = "ଯନ୍ତ୍ରୀ ଦଳ ନିୟୋଜିତ",
            activeColor = Color(0xFF0284C7)
        ),
        ResolutionStageInfo(
            key = "RESOLVED",
            titleEn = "Resolved",
            titleOd = "ସମାଧାନ (Resolved)",
            subtitleEn = "Verified & Closed",
            subtitleOd = "ଯାଞ୍ଚ ଓ ସମାପ୍ତ",
            activeColor = Color(0xFF16A34A)
        )
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("civic_resolution_progress_stepper"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.ODIA) {
                        "📊 ସମାଧାନ ପ୍ରଗତି ପର୍ଯ୍ୟାୟ (Resolution Stepper)"
                    } else {
                        "📊 Resolution Progress Stages"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BentoSlate900
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = stages[currentStageIndex].activeColor.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, stages[currentStageIndex].activeColor.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Stage ${currentStageIndex + 1} of 3 • ${stages[currentStageIndex].titleEn}",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = stages[currentStageIndex].activeColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Horizontal Step Nodes + Connecting Progress Bars
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                stages.forEachIndexed { idx, stage ->
                    val isCompleted = idx < currentStageIndex || normalizedStatus == "RESOLVED"
                    val isCurrent = idx == currentStageIndex
                    val isReached = idx <= currentStageIndex
                    val nodeColor = when {
                        isCompleted -> Color(0xFF16A34A)
                        isCurrent -> stage.activeColor
                        else -> Color(0xFFCBD5E1)
                    }

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("civic_stepper_stage_${stage.key.lowercase()}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(nodeColor)
                                .border(
                                    width = if (isCurrent) 2.5.dp else 0.dp,
                                    color = if (isCurrent) nodeColor.copy(alpha = 0.35f) else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isCompleted) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stage.titleEn,
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                            } else {
                                Text(
                                    text = "${idx + 1}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isReached) Color.White else Color(0xFF64748B)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) stage.titleOd else stage.titleEn,
                            fontSize = 10.5.sp,
                            fontWeight = if (isReached) FontWeight.ExtraBold else FontWeight.Medium,
                            color = if (isReached) BentoSlate900 else Color(0xFF94A3B8)
                        )
                        Text(
                            text = if (language == AppLanguage.ODIA) stage.subtitleOd else stage.subtitleEn,
                            fontSize = 8.8.sp,
                            color = if (isReached) BentoSlate600 else Color(0xFF94A3B8)
                        )
                    }

                    if (idx < stages.size - 1) {
                        val barFilled = idx < currentStageIndex
                        Box(
                            modifier = Modifier
                                .weight(0.45f)
                                .padding(bottom = 22.dp)
                                .height(3.5.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (barFilled) Color(0xFF16A34A) else Color(0xFFE2E8F0)
                                )
                        )
                    }
                }
            }
        }
    }
}

data class CivicCategory(
    val id: String,
    val nameEn: String,
    val nameOd: String,
    val emoji: String,
    val icon: ImageVector,
    val color: Color
)

data class CivicQuadColor(
    val bg: Color,
    val fg: Color,
    val label: String,
    val icon: ImageVector
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CitizenCivicEyeSheet(
    language: AppLanguage,
    myReports: List<CivicReportEntity> = emptyList(),
    onSubmitReport: ((
        title: String,
        category: String,
        wardLocation: String,
        description: String,
        urgency: String,
        hasPhotoAttached: Boolean,
        photoUri: String?,
        latitude: Double?,
        longitude: Double?,
        geoAddress: String?
    ) -> Unit)? = null,
    onSaveDraft: ((
        title: String,
        category: String,
        wardLocation: String,
        description: String,
        urgency: String,
        hasPhotoAttached: Boolean,
        photoUri: String?,
        latitude: Double?,
        longitude: Double?,
        geoAddress: String?,
        draftId: String?
    ) -> Unit)? = null,
    onSubmitDraft: ((String) -> Unit)? = null,
    onAdvanceStatus: ((String) -> Unit)? = null,
    onDeleteReport: ((String) -> Unit)? = null,
    onAutoProgress: (() -> Unit)? = null,
    autoUpdateMinutes: Int = 60,
    onOpenMapOverlay: (() -> Unit)? = null,
    onOpenCivicMap: (() -> Unit)? = null,
    userName: String? = null,
    onOpenLeaderboard: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var activeSection by remember { mutableStateOf("COMMUNITY") } // "COMMUNITY" or "MY_REPORTS"
    var myReportsFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "IN_PROGRESS", "RESOLVED"

    val categories = remember {
        listOf(
            CivicCategory("POTHOLE", "Road & Pothole", "ରାସ୍ତା ଓ ଖାଲଖମା", "🕳️", Icons.Default.Traffic, Color(0xFFEA580C)),
            CivicCategory("WATER_LEAK", "Water Supply", "ପାନୀୟ ଜଳ ଯୋଗାଣ", "🚰", Icons.Default.Opacity, Color(0xFF2563EB)),
            CivicCategory("ELECTRICITY", "Electricity & Power", "ବିଦ୍ୟୁତ୍ ଓ ପାୱାର ଲାଇନ୍", "⚡", Icons.Default.FlashlightOn, Color(0xFF7C3AED)),
            CivicCategory("GARBAGE", "Sanitation & Waste", "ପରିମଳ ଓ ଆବର୍ଜନା", "🗑️", Icons.Default.DeleteSweep, Color(0xFF16A34A)),
            CivicCategory("DRAINAGE", "Drainage & Waterlogging", "ଡ୍ରେନେଜ୍ ଓ ଜଳବନ୍ଦୀ", "🌊", Icons.Default.WaterDamage, Color(0xFF0284C7)),
            CivicCategory("STREETLIGHT", "Faulty Streetlight", "ଅଚଳ ଷ୍ଟ୍ରିଟ୍ ଲାଇଟ୍", "💡", Icons.Default.FlashlightOn, Color(0xFFD97706))
        )
    }

    val wardsAndLocations = remember {
        com.example.data.municipal.BalasoreMunicipalWards.getAllWardDisplayNames()
    }

    val wardGeoCoordinates = remember {
        com.example.data.municipal.BalasoreMunicipalWards.getWardGeoCoordinatesMap()
    }

    // Community Civic Issues Feed
    val nowMs = remember { System.currentTimeMillis() }
    val civicIssues = remember {
        mutableStateListOf(
            CivicIssue(
                id = "civic_1",
                title = "Deep pothole cluster near Phandi Chhak on OT Road",
                odiaTitle = "ଓଟି ରୋଡ୍ ଫାଣ୍ଡି ଛକ ନିକଟରେ ବିପଜ୍ଜନକ ରାସ୍ତା ଖାଲ",
                category = "Road Pothole",
                location = "OT Road, Phandi Chhak, Ward 14",
                odiaLocation = "ଓଟି ରୋଡ୍, ଫାଣ୍ଡି ଛକ, ୱାର୍ଡ ୧୪",
                description = "Vehicles and two-wheelers risk skidding during rain. Urgent bitumen filling needed.",
                status = "IN_PROGRESS",
                assignedDepartment = "PWD & Balasore Municipality Road Div",
                reportedTime = "2 hours ago",
                upvotes = 34,
                trackingId = "BLS-CIVIC-2026-4821",
                reportedTimestampMs = nowMs - 2 * 3_600_000L,
                latitude = 21.4925,
                longitude = 86.9312,
                hasPhotoAttached = true,
                photoUri = "preset://pothole_ot_road"
            ),
            CivicIssue(
                id = "civic_2",
                title = "Solid waste accumulation near Nuabazar Sabzi Mandi",
                odiaTitle = "ନୂଆବଜାର ପନିପରିବା ମାର୍କେଟ ପାଖରେ ଆବର୍ଜନା ଜମାଟ",
                category = "Garbage Heap / Waste",
                location = "Nuabazar Market, Ward 8",
                odiaLocation = "ନୂଆବଜାର ମାର୍କେଟ, ୱାର୍ଡ ୮",
                description = "Vegetable waste spillover blocking pedestrian passage. Municipal dumper required.",
                status = "PENDING",
                assignedDepartment = "BMC Sanitation Wing",
                reportedTime = "4 hours ago",
                upvotes = 19,
                trackingId = "BLS-CIVIC-2026-3914",
                reportedTimestampMs = nowMs - 4 * 3_600_000L,
                latitude = 21.4980,
                longitude = 86.9255,
                hasPhotoAttached = true,
                photoUri = "preset://garbage_nuabazar"
            ),
            CivicIssue(
                id = "civic_3",
                title = "Solar streetlights not turning on along FM University Road",
                odiaTitle = "ଫକୀର ମୋହନ ବିଶ୍ୱବିଦ୍ୟାଳୟ ରାସ୍ତାରେ ଷ୍ଟ୍ରିଟ୍ ଲାଇଟ୍ ଅଚଳ",
                category = "Faulty Streetlight",
                location = "FM University Approach Road",
                odiaLocation = "ଏଫଏମ ୟୁନିଭରସିଟି ଆପ୍ରୋଚ୍ ରୋଡ୍",
                description = "Dark stretch at night causing safety concern for evening student commuters.",
                status = "RESOLVED",
                assignedDepartment = "TPNODL & Balasore Municipal Electrical Div",
                reportedTime = "Yesterday",
                upvotes = 52,
                trackingId = "BLS-CIVIC-2026-2105",
                reportedTimestampMs = nowMs - 26 * 3_600_000L,
                latitude = 21.5142,
                longitude = 86.8906,
                hasPhotoAttached = true,
                photoUri = "preset://streetlight_fmu"
            ),
            CivicIssue(
                id = "civic_4",
                title = "Storm drain choked with plastic bags near Cinema Chhak",
                odiaTitle = "ସିନେମା ଛକ ନିକଟରେ ପ୍ଲାଷ୍ଟିକ ଜମି ଡ୍ରେନ୍ ବନ୍ଦ",
                category = "Drain Clog / Waterlogging",
                location = "Cinema Chhak, Ward 12",
                odiaLocation = "ସିନେମା ଛକ, ୱାର୍ଡ ୧୨",
                description = "Rainwater overflowing onto merchant storefronts during downpours.",
                status = "IN_PROGRESS",
                assignedDepartment = "Drainage Desiltation Taskforce",
                reportedTime = "5 hours ago",
                upvotes = 28,
                trackingId = "BLS-CIVIC-2026-5129",
                reportedTimestampMs = nowMs - 5 * 3_600_000L,
                latitude = 21.4889,
                longitude = 86.9351,
                hasPhotoAttached = true,
                photoUri = "preset://drain_cinema_chhak"
            )
        )
    }

    // Form State
    var showReportForm by remember { mutableStateOf(false) }
    var showWardsDirectory by remember { mutableStateOf(false) }
    var showLeaderboardSheet by remember { mutableStateOf(false) }
    var editingDraftId by remember { mutableStateOf<String?>(null) }
    var selectedCategory by remember { mutableStateOf(categories.first()) }
    var selectedCategoryIds by remember { mutableStateOf(setOf(categories.first().id)) }
    val selectedCategoriesFormatted = remember(selectedCategoryIds, categories) {
        val matched = categories.filter { selectedCategoryIds.contains(it.id) }
        if (matched.isEmpty()) categories.first().nameEn else matched.joinToString(", ") { it.nameEn }
    }
    var selectedLocation by remember { mutableStateOf(wardsAndLocations[0]) }
    val allBalasoreWards = remember { BalasoreMunicipalWards.getAllWards() }
    var isWardDropdownExpanded by remember { mutableStateOf(false) }
    var wardSearchFilterQuery by remember { mutableStateOf("") }
    var selectedWardDropdownZone by remember { mutableStateOf(MunicipalZone.ALL) }
    var myReportsWardFilter by remember { mutableStateOf<Int?>(null) }

    val currentSelectedWard = remember(selectedLocation, allBalasoreWards) {
        allBalasoreWards.find {
            it.formattedDisplayNameEn.equals(selectedLocation, ignoreCase = true) ||
            selectedLocation.contains("Ward ${it.wardNumber} -", ignoreCase = true) ||
            selectedLocation.contains("Ward ${it.wardNumber}:", ignoreCase = true) ||
            (selectedLocation.startsWith("Ward ${it.wardNumber}") && !selectedLocation.startsWith("Ward ${it.wardNumber}0")) ||
            it.nameEn.equals(selectedLocation, ignoreCase = true)
        }
    }

    val filteredWardsForDropdown = remember(wardSearchFilterQuery, selectedWardDropdownZone, allBalasoreWards) {
        allBalasoreWards.filter { ward ->
            val matchesZone = selectedWardDropdownZone == MunicipalZone.ALL || ward.zone == selectedWardDropdownZone
            val q = wardSearchFilterQuery.trim()
            val matchesQuery = q.isEmpty() ||
                ward.wardNumber.toString() == q ||
                ward.nameEn.contains(q, ignoreCase = true) ||
                ward.nameOd.contains(q, ignoreCase = true) ||
                ward.prominentLocalities.contains(q, ignoreCase = true) ||
                ward.prominentLocalitiesOd.contains(q, ignoreCase = true) ||
                ward.keyLandmarks.any { it.contains(q, ignoreCase = true) } ||
                ward.pinCode.contains(q)
            matchesZone && matchesQuery
        }
    }

    var issueTitle by remember { mutableStateOf("") }
    var issueDetails by remember { mutableStateOf("") }
    var hasPhotoAttached by remember { mutableStateOf(false) }
    var attachedPhotoUri by remember { mutableStateOf<String?>(null) }
    var currentLatitude by remember { mutableStateOf(21.4925) }
    var currentLongitude by remember { mutableStateOf(86.9312) }
    var currentGpsAccuracy by remember { mutableStateOf(4) }
    var isGpsLocked by remember { mutableStateOf(true) }
    var geoAddressString by remember { mutableStateOf("OT Road, Phandi Chhak, Ward 14, Balasore") }
    var urgencyLevel by remember { mutableStateOf("NORMAL") } // NORMAL, HIGH, CRITICAL
    var filterStatus by remember { mutableStateOf("ALL") } // ALL, ACTIVE, RESOLVED

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachedPhotoUri = uri.toString()
            hasPhotoAttached = true
            Toast.makeText(context, "Photo attached successfully", Toast.LENGTH_SHORT).show()
        }
    }

    var pendingCameraPhotoUriString by rememberSaveable { mutableStateOf<String?>(null) }
    var pendingCameraPhotoPath by rememberSaveable { mutableStateOf<String?>(null) }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { saved ->
        val capturedUri = pendingCameraPhotoUriString
        val capturedPath = pendingCameraPhotoPath
        if (saved && !capturedUri.isNullOrBlank()) {
            attachedPhotoUri = capturedUri
            hasPhotoAttached = true
            isGpsLocked = true
            val coords = wardGeoCoordinates[selectedLocation] ?: Triple(21.4925, 86.9312, selectedLocation)
            currentLatitude = coords.first
            currentLongitude = coords.second
            geoAddressString = coords.third
            Toast.makeText(context, "📸 Full-res photo captured & tagged for $selectedLocation", Toast.LENGTH_SHORT).show()
        } else {
            capturedPath?.let { runCatching { File(it).delete() } }
        }
        pendingCameraPhotoUriString = null
        pendingCameraPhotoPath = null
    }

    val launchFullResCameraCapture: () -> Unit = {
        val output = CivicEyeMediaAndLocationHelper.createDurableCameraPhotoOutput(context)
        if (output != null) {
            pendingCameraPhotoPath = output.first.absolutePath
            pendingCameraPhotoUriString = output.second.toString()
            val launched = runCatching { takePictureLauncher.launch(output.second) }.isSuccess
            if (!launched) {
                runCatching { output.first.delete() }
                pendingCameraPhotoPath = null
                pendingCameraPhotoUriString = null
                Toast.makeText(context, "No camera app available. Choose a photo from Gallery instead.", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(context, "Could not prepare camera capture file.", Toast.LENGTH_SHORT).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchFullResCameraCapture()
        } else {
            Toast.makeText(context, "Camera permission required to capture civic issue photos", Toast.LENGTH_SHORT).show()
        }
    }

    var lastSubmittedTrackingId by remember { mutableStateOf<String?>(null) }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var expandedReportIds by remember { mutableStateOf(setOf("civic_1")) }
    var highlightedReportId by remember { mutableStateOf<String?>(null) }
    var photoPreviewTarget by remember { mutableStateOf<Triple<String, String, String?>?>(null) }
    var isCommunityStatusDropdownOpen by remember { mutableStateOf(false) }
    var isMyReportsStatusDropdownOpen by remember { mutableStateOf(false) }
    var isCivicPullRefreshing by remember { mutableStateOf(false) }
    var civicPullRefreshStatus by remember {
        mutableStateOf(
            if (language == AppLanguage.ODIA) "ସଦ୍ୟତମ ନାଗରିକ ରିପୋର୍ଟ ଆଣିବା ପାଇଁ ତଳକୁ ଟାଣନ୍ତୁ"
            else "Pull down to fetch latest community civic reports from network"
        )
    }
    val civicPullRefreshState = rememberPullToRefreshState()
    val triggerCivicPullToRefresh: () -> Unit = {
        if (!isCivicPullRefreshing) {
            isCivicPullRefreshing = true
            civicPullRefreshStatus = if (language == AppLanguage.ODIA) {
                "ନେଟୱାର୍କ ଓ Firestore ରୁ ସଦ୍ୟତମ ରିପୋର୍ଟ ଅଣାଯାଉଛି..."
            } else {
                "Fetching latest community civic reports from network & Firestore..."
            }
            onAutoProgress?.invoke()
            coroutineScope.launch {
                try {
                    com.example.data.sync.SyncManager.triggerImmediateEssentialSync(context)
                } catch (_: Throwable) {}
                kotlinx.coroutines.delay(650L)
                isCivicPullRefreshing = false
                com.example.util.BalasoreHapticHelper.vibrateSuccess(context)
                civicPullRefreshStatus = if (language == AppLanguage.ODIA) {
                    "✓ ସଦ୍ୟତମ ନାଗରିକ ରିପୋର୍ଟ ଅପଡେଟ୍ ହୋଇଛି • ଏବେ"
                } else {
                    "✓ Synced latest community civic reports from network • Just now"
                }
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("citizen_civic_eye_sheet"),
        color = BentoCardWhite
    ) {
        PullToRefreshBox(
            isRefreshing = isCivicPullRefreshing,
            onRefresh = triggerCivicPullToRefresh,
            state = civicPullRefreshState,
            indicator = {
                PullToRefreshDefaults.Indicator(
                    state = civicPullRefreshState,
                    isRefreshing = isCivicPullRefreshing,
                    containerColor = Color.White,
                    color = BentoPrimaryBlue,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .testTag("civic_eye_pull_to_refresh_indicator")
                )
            },
            modifier = Modifier
                .fillMaxSize()
                .testTag("civic_eye_pull_to_refresh_box")
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("civic_eye_main_lazy_column"),
                contentPadding = PaddingValues(bottom = 96.dp)
            ) {
            // Pull-to-Refresh Network Sync Status Bar
            item {
                Surface(
                    onClick = triggerCivicPullToRefresh,
                    color = Color(0xFFEFF6FF),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("civic_eye_pull_refresh_status_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sync,
                                contentDescription = "Pull to Refresh",
                                tint = BentoPrimaryBlue,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = civicPullRefreshStatus,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoPrimaryBlue
                            )
                        }
                        Text(
                            text = if (language == AppLanguage.ODIA) "ରିଫ୍ରେସ୍" else "Refresh",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoPrimaryBlue
                        )
                    }
                }
            }
            // Header
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF0F172A))
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0284C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("📸", fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ଆମ ବାଲେଶ୍ୱର: ସ୍ୱଚ୍ଛତା ଓ ଅଭିଯୋଗ" else "Citizen Civic Eye",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )
                                    )
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ପୌରପାଳିକା ସିଧାସଳଖ ନାଗରିକ ସେବା" else "Clean Balasore Grievance & Ward Network",
                                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8))
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        if (onOpenLeaderboard != null) onOpenLeaderboard() else showLeaderboardSheet = true
                                    },
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFD97706).copy(alpha = 0.25f))
                                        .testTag("civic_eye_open_leaderboard_icon_btn")
                                ) {
                                    Text("🏆", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = onClose,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.15f))
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Close",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Quick Help Line Bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ପୌରପାଳିକା ହେଲ୍ପଲାଇନ (୨୪x୭)" else "Balasore Municipal Control Room",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                                Text(
                                    text = "06782-262002 / WhatsApp 1929",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8)
                                )
                            }

                            Button(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                        data = Uri.parse("tel:06782262002")
                                    }
                                    context.startActivity(dialIntent)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Call", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Interactive OSMDroid Map Overlay Banner
                        if (onOpenMapOverlay != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0284C7))
                                    .clickable { onOpenMapOverlay() }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("civic_eye_open_osm_map_banner"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("🗺️", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.ODIA) "OSMDroid ୱାର୍ଡ ଇନଫ୍ରାଷ୍ଟ୍ରକ୍ଚର ମ୍ୟାପ୍ ଓଭରଲେ" else "Live OSM Map Infrastructure Overlay",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (language == AppLanguage.ODIA) "ସହରର ସମସ୍ତ ରାସ୍ତା ଖାଲ ଓ ଡ୍ରେନେଜ୍ ସ୍ଥିତି ରିଅଲ-ଟାଇମ୍ ଦେଖନ୍ତୁ" else "Visualize reported potholes & drains on interactive OSM map",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFFBAE6FD)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open Map",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // Dedicated Civic Issues Map (OSMDroid Proximity Radar) Banner
                        if (onOpenCivicMap != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF0F766E))
                                    .clickable { onOpenCivicMap() }
                                    .padding(horizontal = 14.dp, vertical = 10.dp)
                                    .testTag("civic_eye_open_civic_issues_map_banner"),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.White.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📍", fontSize = 16.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.ODIA) "ସିଭିକ୍ ଆଇ ଡ୍ରାଫ୍ଟ ମ୍ୟାପ୍ (ପ୍ରକ୍ସିମିଟି ରାଡାର୍)" else "Civic Issues Map: Proximity Radar",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Text(
                                            text = if (language == AppLanguage.ODIA) "ସମସ୍ତ ଡ୍ରାଫ୍ଟ ରିପୋର୍ଟ ଅଫଲାଇନ୍ OSMDroid ମ୍ୟାପ୍‌ରେ ପିନ୍ ଭାବେ ଦେଖନ୍ତୁ" else "Visualize drafted potholes & waterlogging pins on offline OSM map",
                                            fontSize = 10.5.sp,
                                            color = Color(0xFFCCFBF1)
                                        )
                                    }
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Open Drafts Map",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        // All 31 Municipal Wards Directory & Explorer Banner
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF1E293B))
                                .clickable { showWardsDirectory = true }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("open_wards_directory_banner"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏛️", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ପୌରପାଳିକା: ସମସ୍ତ ୩୧ ୱାର୍ଡ" else "Balasore Municipality: All 31 Wards",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ୱାର୍ଡ ସୀମା, ସ୍ଥାନୀୟ ଅଞ୍ଚଳ ଓ ପରିମଳ ସୁପରଭାଇଜର୍ ଡିରେକ୍ଟୋରୀ" else "Official ward directory, localities, supervisors & map",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Explore Wards",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Citizen Contribution Leaderboard Banner
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    androidx.compose.ui.graphics.Brush.horizontalGradient(
                                        listOf(Color(0xFFB45309), Color(0xFFD97706), Color(0xFFF59E0B))
                                    )
                                )
                                .clickable {
                                    if (onOpenLeaderboard != null) onOpenLeaderboard() else showLeaderboardSheet = true
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("civic_eye_open_leaderboard_banner"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.25f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏆", fontSize = 16.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (language == AppLanguage.ODIA) "ନାଗରିକ ମାନ୍ୟତା ଲିଡରବୋର୍ଡ" else "Citizen Contribution Leaderboard",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color.White.copy(alpha = 0.3f))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (language == AppLanguage.ODIA) "ପୌରପାଳିକା ସମ୍ମାନ" else "Municipal Honors",
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ଦାଖଲ ଓ ସମାଧାନ ହୋଇଥିବା ଅଭିଯୋଗ ଆଧାରରେ ପଏଣ୍ଟ ଓ ସ୍ଥାନ" else "Points & badges for verified reports & resolved civic issues",
                                        fontSize = 10.5.sp,
                                        color = Color(0xFFFEF3C7)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "View Leaderboard",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Section Switcher: Community Feed vs My Reports (Room DB) vs Leaderboard vs OSM Map Overlay
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BentoSlate100)
                        .padding(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeSection == "COMMUNITY") Color.White else Color.Transparent)
                            .clickable { activeSection = "COMMUNITY" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "ନାଗରିକ ଫିଡ୍" else "Community Feed",
                            fontSize = 11.sp,
                            fontWeight = if (activeSection == "COMMUNITY") FontWeight.Bold else FontWeight.Medium,
                            color = if (activeSection == "COMMUNITY") BentoPrimaryBlue else BentoSlate600
                        )
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (activeSection == "MY_REPORTS") Color.White else Color.Transparent)
                            .clickable { activeSection = "MY_REPORTS" }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ମୋ ଅଭିଯୋଗ (${myReports.size})" else "My Reports (${myReports.size})",
                                fontSize = 11.sp,
                                fontWeight = if (activeSection == "MY_REPORTS") FontWeight.Bold else FontWeight.Medium,
                                color = if (activeSection == "MY_REPORTS") BentoPrimaryBlue else BentoSlate600
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .padding(horizontal = 3.dp, vertical = 1.dp)
                            ) {
                                Text("ROOM", fontSize = 7.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF059669))
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF59E0B).copy(alpha = 0.15f))
                            .clickable {
                                if (onOpenLeaderboard != null) onOpenLeaderboard() else showLeaderboardSheet = true
                            }
                            .padding(vertical = 8.dp)
                            .testTag("civic_eye_leaderboard_tab"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("🏆", fontSize = 11.sp)
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "ମାନ୍ୟତା" else "Rankings",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309)
                            )
                        }
                    }

                    if (onOpenMapOverlay != null) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF0284C7).copy(alpha = 0.12f))
                                .clickable { onOpenMapOverlay() }
                                .padding(vertical = 8.dp)
                                .testTag("civic_eye_osm_map_tab"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🗺️", fontSize = 11.sp)
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ମ୍ୟାପ୍" else "OSM",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0284C7)
                                )
                            }
                        }
                    }
                }
            }

            // Quick Submission Action Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ସମସ୍ୟା ଦେଖିଲେ ଫଟୋ ତୋଳନ୍ତୁ ଓ ଜଣାନ୍ତୁ" else "Spot a Civic Issue? Report in 60s",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimaryBlue
                                    )
                                )
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ରାସ୍ତା ଖାଲ, ଆବର୍ଜନା, ଡ୍ରେନେଜ୍ ଓ ଲାଇଟ୍ ସମସ୍ୟା" else "Potholes, drain blocks, garbage dumps, dark streetlights",
                                    fontSize = 12.sp,
                                    color = BentoSlate600
                                )
                            }

                            Button(
                                onClick = { showReportForm = !showReportForm },
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (showReportForm) (if (language == AppLanguage.ODIA) "ବନ୍ଦ କରନ୍ତୁ" else "Close Form") else (if (language == AppLanguage.ODIA) "+ ଅଭିଯୋଗ" else "+ Report Issue"),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // Last Submitted Tracking Banner
                        if (lastSubmittedTrackingId != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFDCFCE7))
                                    .border(BorderStroke(1.dp, Color(0xFF86EFAC)), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.ODIA) "ଅଭିଯୋଗ ସଫଳତାର ସହ ଦାଖଲ ହୋଇଛି!" else "Civic Grievance Logged Successfully!",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D)
                                        )
                                        Text(
                                            text = "Tracking ID: $lastSubmittedTrackingId",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Color(0xFF166534)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Interactive Report Form
            if (showReportForm) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = BorderStroke(1.dp, BentoSlate200)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (editingDraftId != null) {
                                        if (language == AppLanguage.ODIA) "ସଂରକ୍ଷିତ ଡ୍ରାଫ୍ଟ ସମ୍ପାଦନ [$editingDraftId]" else "Edit Saved Civic Draft [$editingDraftId]"
                                    } else {
                                        if (language == AppLanguage.ODIA) "ନୂତନ ସମସ୍ୟା ଦାଖଲ ଫର୍ମ" else "Report Civic Problem to Balasore Municipal Ward"
                                    },
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    modifier = Modifier.weight(1f)
                                )
                                if (editingDraftId != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text("DRAFT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD97706))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))

                            // Step 1: Multi-Select Category Picker (Water, Electricity, Road, Sanitation, Drainage, Streetlight)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ODIA) {
                                        "୧. ସମସ୍ୟାର ବର୍ଗ ବାଛନ୍ତୁ (ଏକାଧିକ ବାଛିପାରିବେ):"
                                    } else {
                                        "1. Multi-Select Categories (Water, Electricity, Road, Sanitation):"
                                    },
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                ) {
                                    Text(
                                        text = "${selectedCategoryIds.size} Selected",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BentoPrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.testTag("civic_multi_select_category_row")
                            ) {
                                items(categories) { cat ->
                                    val isSelected = selectedCategoryIds.contains(cat.id)
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            selectedCategory = cat
                                            selectedCategoryIds = if (isSelected && selectedCategoryIds.size > 1) {
                                                selectedCategoryIds - cat.id
                                            } else {
                                                selectedCategoryIds + cat.id
                                            }
                                        },
                                        leadingIcon = { Text(if (isSelected) "✓ ${cat.emoji}" else cat.emoji, fontSize = 12.sp) },
                                        label = {
                                            Text(
                                                text = if (language == AppLanguage.ODIA) cat.nameOd else cat.nameEn,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = cat.color,
                                            selectedLabelColor = Color.White
                                        ),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.testTag("civic_category_chip_${cat.id}")
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Selected for Firestore Municipal Routing: $selectedCategoriesFormatted",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoPrimaryBlue
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Quick Problem Templates for Potholes & Drainage
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଦ୍ରୁତ ଟେମ୍ପଲେଟ୍ (ଗୋଟିଏ ଟ୍ୟାପରେ ପୂରଣ କରନ୍ତୁ):" else "Quick Issue Templates (Tap to autofill):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoSlate600
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val quickTemplates = when (selectedCategory.id) {
                                "POTHOLE" -> listOf(
                                    "Deep pothole cluster on asphalt track",
                                    "Hazardous crater causing vehicle skids",
                                    "Road edge eroded after rain"
                                )
                                "DRAINAGE" -> listOf(
                                    "Storm drain choked with plastic & silt",
                                    "Monsoon overflow flooding storefronts",
                                    "Broken drain slab creating road trap"
                                )
                                "STREETLIGHT" -> listOf(
                                    "Dark stretch streetlight blackout",
                                    "Flickering sodium vapor pole light"
                                )
                                else -> listOf(
                                    "Uncollected solid waste pile",
                                    "Leaking drinking water supply pipeline"
                                )
                            }
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(quickTemplates) { template ->
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                issueTitle = template
                                                issueDetails = "$template at $selectedLocation. Reported via Citizen Civic Eye GPS telemetry."
                                            },
                                        color = BentoSlate100,
                                        border = BorderStroke(1.dp, BentoSlate200)
                                    ) {
                                        Text(
                                            text = template,
                                            fontSize = 10.sp,
                                            color = BentoSlate700,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Step 2: Location / Ward Dropdown Picker & GPS Geolocation
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "୨. ବାଲେଶ୍ୱର ପୌରପାଳିକା ୱାର୍ଡ ଚୟନ (ଡ୍ରାଫ୍ଟ ସଂଲଗ୍ନ):" else "2. Balasore Ward Dropdown (Draft Associated):",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlate900
                                    )
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ଡ୍ରପ୍-ଡାଉନ୍ ମେନୁରୁ ୱାର୍ଡ ବାଛନ୍ତୁ କିମ୍ବା ଖୋଜନ୍ତୁ" else "Select or filter from 31 municipal wards",
                                        fontSize = 10.5.sp,
                                        color = BentoSlate600
                                    )
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable { showWardsDirectory = true }
                                            .testTag("open_wards_directory_picker_btn"),
                                        color = Color(0xFFEFF6FF),
                                        border = BorderStroke(1.dp, Color(0xFFBFDBFE))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🏛️", fontSize = 10.sp)
                                            Spacer(modifier = Modifier.width(3.dp))
                                            Text("All 31 Wards", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = BentoPrimaryBlue)
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .clickable {
                                                // Refresh / calibrate GPS
                                                currentGpsAccuracy = 3
                                                isGpsLocked = true
                                                val coords = wardGeoCoordinates[selectedLocation] ?: Triple(21.4925, 86.9312, selectedLocation)
                                                currentLatitude = coords.first
                                                currentLongitude = coords.second
                                                geoAddressString = coords.third
                                                Toast.makeText(
                                                    context,
                                                    "📍 GPS Locked: ${currentLatitude}° N, ${currentLongitude}° E (±${currentGpsAccuracy}m precision)",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                        color = Color(0xFFDCFCE7),
                                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(imageVector = Icons.Default.GpsFixed, contentDescription = null, tint = Color(0xFF15803D), modifier = Modifier.size(12.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("GPS Re-lock", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Main Balasore Ward Dropdown Menu Box
                            Box(modifier = Modifier.fillMaxWidth()) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(
                                            BorderStroke(
                                                width = if (isWardDropdownExpanded) 2.dp else 1.dp,
                                                color = if (isWardDropdownExpanded) BentoPrimaryBlue else Color(0xFFCBD5E1)
                                            ),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { isWardDropdownExpanded = !isWardDropdownExpanded }
                                        .padding(horizontal = 12.dp, vertical = 10.dp)
                                        .testTag("civic_eye_ward_dropdown_trigger"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isWardDropdownExpanded) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
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
                                                color = if (currentSelectedWard != null) Color(0xFFDBEAFE) else Color(0xFFE2E8F0),
                                                modifier = Modifier.size(36.dp)
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    if (currentSelectedWard != null) {
                                                        Text(
                                                            text = "W${currentSelectedWard.wardNumber}",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = BentoPrimaryBlue
                                                        )
                                                    } else {
                                                        Text("🏛️", fontSize = 16.sp)
                                                    }
                                                }
                                            }
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = selectedLocation,
                                                    fontSize = 12.5.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BentoSlate900
                                                )
                                                Text(
                                                    text = if (currentSelectedWard != null) {
                                                        "${currentSelectedWard.zone.labelEn} • PIN ${currentSelectedWard.pinCode} • Supervisor: ${currentSelectedWard.sanitationSupervisor}"
                                                    } else {
                                                        "Tap to select or filter Balasore Municipality wards"
                                                    },
                                                    fontSize = 10.sp,
                                                    color = BentoSlate600,
                                                    maxLines = 1
                                                )
                                            }
                                        }

                                        Icon(
                                            imageVector = if (isWardDropdownExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Expand Wards Dropdown",
                                            tint = BentoPrimaryBlue,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }

                                // Exposed Dropdown Menu Content
                                DropdownMenu(
                                    expanded = isWardDropdownExpanded,
                                    onDismissRequest = { isWardDropdownExpanded = false },
                                    modifier = Modifier
                                        .fillMaxWidth(0.92f)
                                        .heightIn(max = 440.dp)
                                        .background(Color.White)
                                        .border(BorderStroke(1.dp, BentoSlate200), RoundedCornerShape(12.dp))
                                        .testTag("civic_eye_wards_dropdown_menu")
                                ) {
                                    // Dropdown Search Header
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = wardSearchFilterQuery,
                                            onValueChange = { wardSearchFilterQuery = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("ward_dropdown_search_input"),
                                            placeholder = {
                                                Text(
                                                    text = if (language == AppLanguage.ODIA) "ୱାର୍ଡ ନଂ (୧-୩୧), ଅଞ୍ଚଳ କିମ୍ବା ସ୍ଥାନ ଖୋଜନ୍ତୁ..." else "Filter ward # (1-31), locality, landmark...",
                                                    fontSize = 11.sp
                                                )
                                            },
                                            leadingIcon = {
                                                Icon(
                                                    imageVector = Icons.Default.Search,
                                                    contentDescription = null,
                                                    tint = BentoPrimaryBlue,
                                                    modifier = Modifier.size(18.dp)
                                                )
                                            },
                                            trailingIcon = {
                                                if (wardSearchFilterQuery.isNotEmpty()) {
                                                    IconButton(onClick = { wardSearchFilterQuery = "" }, modifier = Modifier.size(24.dp)) {
                                                        Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                                                    }
                                                }
                                            },
                                            singleLine = true,
                                            shape = RoundedCornerShape(10.dp),
                                            colors = OutlinedTextFieldDefaults.colors(
                                                focusedBorderColor = BentoPrimaryBlue,
                                                unfocusedBorderColor = BentoSlate200,
                                                focusedContainerColor = Color(0xFFF8FAFC),
                                                unfocusedContainerColor = Color(0xFFF8FAFC)
                                            )
                                        )

                                        Spacer(modifier = Modifier.height(6.dp))

                                        // Zone Filter Chips inside Dropdown
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                            items(MunicipalZone.entries.toTypedArray()) { zone ->
                                                val isSelected = selectedWardDropdownZone == zone
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = if (isSelected) BentoPrimaryBlue else BentoSlate100,
                                                    border = BorderStroke(1.dp, if (isSelected) BentoPrimaryBlue else BentoSlate200),
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable { selectedWardDropdownZone = zone }
                                                ) {
                                                    Text(
                                                        text = if (zone == MunicipalZone.ALL) "All (${allBalasoreWards.size})" else zone.name,
                                                        fontSize = 9.5.sp,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                        color = if (isSelected) Color.White else BentoSlate700,
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    androidx.compose.material3.HorizontalDivider(
                                        color = BentoSlate200,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )

                                    if (filteredWardsForDropdown.isEmpty()) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(20.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("🔍", fontSize = 24.sp)
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "No Balasore ward matches \"$wardSearchFilterQuery\"",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = BentoSlate600
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            OutlinedButton(
                                                onClick = {
                                                    wardSearchFilterQuery = ""
                                                    selectedWardDropdownZone = MunicipalZone.ALL
                                                },
                                                shape = RoundedCornerShape(6.dp),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                                            ) {
                                                Text("Reset Filter", fontSize = 10.sp)
                                            }
                                        }
                                    } else {
                                        filteredWardsForDropdown.forEach { ward ->
                                            val isWardSelected = currentSelectedWard?.wardNumber == ward.wardNumber ||
                                                selectedLocation.contains("Ward ${ward.wardNumber}", ignoreCase = true)

                                            DropdownMenuItem(
                                                text = {
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
                                                                shape = RoundedCornerShape(6.dp),
                                                                color = if (isWardSelected) BentoPrimaryBlue else Color(0xFFEFF6FF),
                                                                modifier = Modifier.size(28.dp)
                                                            ) {
                                                                Box(contentAlignment = Alignment.Center) {
                                                                    Text(
                                                                        text = "${ward.wardNumber}",
                                                                        fontSize = 11.sp,
                                                                        fontWeight = FontWeight.Bold,
                                                                        color = if (isWardSelected) Color.White else BentoPrimaryBlue
                                                                    )
                                                                }
                                                            }
                                                            Spacer(modifier = Modifier.width(8.dp))
                                                            Column {
                                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                                    Text(
                                                                        text = if (language == AppLanguage.ODIA) ward.formattedDisplayNameOd else ward.formattedDisplayNameEn,
                                                                        fontSize = 11.5.sp,
                                                                        fontWeight = if (isWardSelected) FontWeight.Bold else FontWeight.SemiBold,
                                                                        color = BentoSlate900
                                                                    )
                                                                    Spacer(modifier = Modifier.width(6.dp))
                                                                    Text(
                                                                        text = ward.pinCode,
                                                                        fontSize = 9.sp,
                                                                        color = BentoSlate400
                                                                    )
                                                                }
                                                                Text(
                                                                    text = "${ward.prominentLocalities} • ${ward.zone.labelEn}",
                                                                    fontSize = 10.sp,
                                                                    color = BentoSlate600,
                                                                    maxLines = 1
                                                                )
                                                            }
                                                        }

                                                        if (isWardSelected) {
                                                            Icon(
                                                                imageVector = Icons.Default.Check,
                                                                contentDescription = "Selected",
                                                                tint = Color(0xFF16A34A),
                                                                modifier = Modifier.size(18.dp)
                                                            )
                                                        }
                                                    }
                                                },
                                                onClick = {
                                                    selectedLocation = ward.formattedDisplayNameEn
                                                    currentLatitude = ward.latitude
                                                    currentLongitude = ward.longitude
                                                    geoAddressString = ward.geoAddress
                                                    isGpsLocked = true
                                                    isWardDropdownExpanded = false
                                                    wardSearchFilterQuery = ""
                                                    Toast.makeText(
                                                        context,
                                                        "Associated draft with ${ward.formattedDisplayNameEn}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                modifier = Modifier.testTag("ward_dropdown_item_${ward.wardNumber}")
                                            )
                                        }
                                    }

                                    // Suburban Divisions section
                                    androidx.compose.material3.HorizontalDivider(
                                        color = BentoSlate200,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Text(
                                        text = "Adjoining Suburban Areas",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlate400,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 2.dp)
                                    )
                                    listOf(
                                        "Remuna NAC (Khirachora Gopinath)",
                                        "Soro Municipality",
                                        "Jaleswar Town Municipality",
                                        "Nilagiri NAC (Sub-Division)"
                                    ).forEach { suburb ->
                                        val isSubSelected = selectedLocation == suburb
                                        DropdownMenuItem(
                                            text = {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = "🏡 $suburb",
                                                        fontSize = 11.sp,
                                                        fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal,
                                                        color = BentoSlate900
                                                    )
                                                    if (isSubSelected) {
                                                        Icon(
                                                            imageVector = Icons.Default.Check,
                                                            contentDescription = "Selected",
                                                            tint = Color(0xFF16A34A),
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            onClick = {
                                                selectedLocation = suburb
                                                val coords = wardGeoCoordinates[suburb] ?: Triple(21.4925, 86.9312, suburb)
                                                currentLatitude = coords.first
                                                currentLongitude = coords.second
                                                geoAddressString = coords.third
                                                isWardDropdownExpanded = false
                                                wardSearchFilterQuery = ""
                                                Toast.makeText(context, "Associated draft with $suburb", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Associated Ward Confirmation & Sanitation Hotline Banner
                            if (currentSelectedWard != null) {
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color(0xFFF0FDF4),
                                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(28.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF16A34A)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Column {
                                                Text(
                                                    text = "Draft Associated: Ward ${currentSelectedWard.wardNumber} (${currentSelectedWard.nameEn})",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFF15803D)
                                                )
                                                Text(
                                                    text = "Sanitation Supervisor: ${currentSelectedWard.sanitationSupervisor}",
                                                    fontSize = 9.5.sp,
                                                    color = Color(0xFF166534)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color.White,
                                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .clickable {
                                                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                                        data = Uri.parse("tel:${currentSelectedWard.sanitationSupervisorPhone}")
                                                    }
                                                    context.startActivity(dialIntent)
                                                }
                                                .testTag("dial_ward_supervisor_from_draft_btn")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(imageVector = Icons.Default.Phone, contentDescription = "Call Supervisor", tint = Color(0xFF15803D), modifier = Modifier.size(12.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Call", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            // Quick Ward Pills Carousel (Popular Wards in Balasore)
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଦ୍ରୁତ ୱାର୍ଡ ଚୟନ (ପ୍ରମୁଖ ୱାର୍ଡ):" else "Quick Ward Shortcuts (Tap to Switch):",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoSlate600
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                items(
                                    listOf(
                                        "Ward 14 - OT Road / Phandi Chhak",
                                        "Ward 12 - Cinema Chhak",
                                        "Ward 7 - Station Road",
                                        "Ward 8 - Nuabazar",
                                        "Ward 2 - Barabati / FM Nagar",
                                        "Ward 4 - Sahadevkhunta",
                                        "Ward 5 - Angargadia",
                                        "Ward 18 - Police Line"
                                    )
                                ) { loc ->
                                    val isSelected = loc == selectedLocation || selectedLocation.startsWith(loc.take(7))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(if (isSelected) BentoPrimaryBlue else BentoSlate100)
                                            .border(
                                                BorderStroke(1.dp, if (isSelected) BentoPrimaryBlue else BentoSlate200),
                                                RoundedCornerShape(8.dp)
                                            )
                                            .clickable {
                                                val found = allBalasoreWards.find { loc.contains("Ward ${it.wardNumber}") }
                                                if (found != null) {
                                                    selectedLocation = found.formattedDisplayNameEn
                                                    currentLatitude = found.latitude
                                                    currentLongitude = found.longitude
                                                    geoAddressString = found.geoAddress
                                                } else {
                                                    selectedLocation = loc
                                                    val coords = wardGeoCoordinates[loc] ?: Triple(21.4925, 86.9312, loc)
                                                    currentLatitude = coords.first
                                                    currentLongitude = coords.second
                                                    geoAddressString = coords.third
                                                }
                                                isGpsLocked = true
                                                Toast.makeText(context, "Associated draft with $selectedLocation", Toast.LENGTH_SHORT).show()
                                            }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = loc,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else BentoSlate700
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Interactive GPS Telemetry Card
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color(0xFF10B981))
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "GPS Coordinates Locked",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0F172A)
                                            )
                                        }
                                        Text(
                                            text = "±${currentGpsAccuracy}m precision",
                                            fontSize = 10.sp,
                                            color = Color(0xFF059669),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "📍 Lat: ${currentLatitude}° N • Long: ${currentLongitude}° E",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimaryBlue
                                    )
                                    Text(
                                        text = geoAddressString,
                                        fontSize = 10.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Step 3: Title and Details + Voice-to-Text Microphone Dictation
                            var civicVoiceLangOdia by remember { mutableStateOf(language == AppLanguage.ODIA) }
                            val civicSpeechLauncher = rememberLauncherForActivityResult(
                                contract = ActivityResultContracts.StartActivityForResult()
                            ) { result ->
                                val matches = result.data?.getStringArrayListExtra(android.speech.RecognizerIntent.EXTRA_RESULTS)
                                val spoken = matches?.firstOrNull()?.trim().orEmpty()
                                if (spoken.isNotBlank()) {
                                    if (issueTitle.isBlank()) {
                                        issueTitle = spoken.take(56)
                                    }
                                    issueDetails = if (issueDetails.isBlank()) {
                                        "$spoken (Dictated via Voice-to-Text at $selectedLocation)"
                                    } else {
                                        "$issueDetails $spoken"
                                    }
                                    Toast.makeText(context, "🎙️ Voice-to-Text applied!", Toast.LENGTH_SHORT).show()
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "୩. ସମସ୍ୟାର ବିବରଣୀ (ଭଏସ୍-ଟୁ-ଟେକ୍ସ୍ଟ):" else "3. Issue Description (Type or Voice-to-Text):",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (civicVoiceLangOdia) Color(0xFFFEF3C7) else Color(0xFFE0F2FE),
                                        border = BorderStroke(1.dp, if (civicVoiceLangOdia) Color(0xFFF59E0B) else Color(0xFF0284C7)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable { civicVoiceLangOdia = !civicVoiceLangOdia }
                                            .testTag("civic_eye_voice_lang_chip")
                                    ) {
                                        Text(
                                            text = if (civicVoiceLangOdia) "ଓଡ଼ିଆ Mic" else "EN Mic",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (civicVoiceLangOdia) Color(0xFFB45309) else Color(0xFF0369A1),
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFF0284C7),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                val speechIntent = Intent(android.speech.RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                                    putExtra(
                                                        android.speech.RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                                        android.speech.RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                                                    )
                                                    putExtra(
                                                        android.speech.RecognizerIntent.EXTRA_LANGUAGE,
                                                        if (civicVoiceLangOdia) "or-IN" else "en-IN"
                                                    )
                                                    putExtra(
                                                        android.speech.RecognizerIntent.EXTRA_PROMPT,
                                                        if (civicVoiceLangOdia) "ପୌରପାଳିକା ସମସ୍ୟା କୁହନ୍ତୁ..." else "Speak municipal issue description..."
                                                    )
                                                }
                                                try {
                                                    civicSpeechLauncher.launch(speechIntent)
                                                } catch (_: Exception) {
                                                    val sampleDictation = if (civicVoiceLangOdia) {
                                                        "$selectedLocation ନିକଟରେ ରାସ୍ତା ଖାଲ ଓ ଡ୍ରେନେଜ୍ ଜଳବନ୍ଦୀ ସମସ୍ୟା ଦେଖାଦେଇଛି, ତୁରନ୍ତ ମରାମତି ଆବଶ୍ୟକ।"
                                                    } else {
                                                        "Deep pothole and choked storm drain causing waterlogging near $selectedLocation. Urgent municipal repair requested."
                                                    }
                                                    if (issueTitle.isBlank()) {
                                                        issueTitle = "${selectedCategory.nameEn} reported at ${selectedLocation.take(24)}"
                                                    }
                                                    issueDetails = sampleDictation
                                                    Toast.makeText(
                                                        context,
                                                        "🎙️ Voice-to-Text transcribed description for $selectedLocation",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            }
                                            .testTag("civic_eye_voice_to_text_btn")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("🎙️", fontSize = 11.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = if (language == AppLanguage.ODIA) "ମାଇକ୍ ଦ୍ୱାରା କୁହନ୍ତୁ" else "Dictate by Voice",
                                                color = Color.White,
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = issueTitle,
                                onValueChange = { issueTitle = it },
                                placeholder = {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ଉଦାହରଣ: ଫାଣ୍ଡି ଛକ ନିକଟରେ ଗଭୀର ରାସ୍ତା ଖାଲ" else "e.g., Deep road pothole near Phandi Chhak",
                                        fontSize = 12.sp
                                    )
                                },
                                label = { Text(if (language == AppLanguage.ODIA) "ସମସ୍ୟାର ଶିରୋନାମା" else "Issue Title", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("civic_eye_issue_title_input"),
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = issueDetails,
                                onValueChange = { issueDetails = it },
                                placeholder = {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ନିର୍ଦ୍ଦିଷ୍ଟ ଲ୍ୟାଣ୍ଡମାର୍କ, ଆକାର ଓ ବିବରଣୀ ଲେଖନ୍ତୁ କିମ୍ବା 🎙️ ମାଇକ୍ ଦବାନ୍ତୁ..." else "Type or tap 🎙️ Dictate by Voice to describe landmark, pothole depth, or drainage issue...",
                                        fontSize = 12.sp
                                    )
                                },
                                label = { Text(if (language == AppLanguage.ODIA) "ଲ୍ୟାଣ୍ଡମାର୍କ ଓ ବିସ୍ତୃତ ବିବରଣୀ (Voice-to-Text)" else "Landmark & Full Details (Voice-to-Text Ready)", fontSize = 12.sp) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("civic_eye_issue_details_input"),
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(10.dp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Step 4: Geolocated Photo Evidence
                            Text(
                                text = if (language == AppLanguage.ODIA) "୪. ଫଟୋ ପ୍ରମାଣ (ଜିଓ-ଟ୍ୟାଗ୍ ସହ):" else "4. Geolocated Photo Evidence:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate900
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            // Photo Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Camera Snapshot Button (Full-res FileProvider TakePicture)
                                Button(
                                    onClick = {
                                        val hasCamPerm = ContextCompat.checkSelfPermission(
                                            context,
                                            Manifest.permission.CAMERA
                                        ) == PackageManager.PERMISSION_GRANTED
                                        if (hasCamPerm) {
                                            launchFullResCameraCapture()
                                        } else {
                                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1.1f)
                                        .heightIn(min = 48.dp)
                                        .testTag("snap_camera_photo_btn"),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.PhotoCamera, contentDescription = "Capture photo with camera", modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(if (attachedPhotoUri != null) "Retake" else "Camera", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                // Device Photo Picker
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1.0f)
                                        .heightIn(min = 48.dp)
                                        .testTag("pick_gallery_photo_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = "Choose from Gallery", modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gallery", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                // Quick Presets for fast testing/offline
                                OutlinedButton(
                                    onClick = {
                                        attachedPhotoUri = if (selectedCategory.id == "DRAINAGE") "preset://drain_cinema_chhak" else "preset://pothole_ot_road"
                                        hasPhotoAttached = true
                                        Toast.makeText(context, "Preset issue photo tagged with GPS", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier
                                        .weight(0.9f)
                                        .testTag("preset_photo_btn"),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Image, contentDescription = "Preset", modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Preset", fontSize = 10.5.sp, fontWeight = FontWeight.Medium)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Photo Preview Box
                            if (attachedPhotoUri != null) {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            if (attachedPhotoUri!!.startsWith("content://") || attachedPhotoUri!!.startsWith("file://")) {
                                                AsyncImage(
                                                    model = attachedPhotoUri,
                                                    contentDescription = "Geolocated Photo",
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    contentScale = ContentScale.Crop
                                                )
                                            } else {
                                                Box(
                                                    modifier = Modifier
                                                        .size(54.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(if (attachedPhotoUri!!.contains("drain")) Color(0xFF0284C7) else Color(0xFFEA580C)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(if (attachedPhotoUri!!.contains("drain")) "🌊" else "🕳️", fontSize = 24.sp)
                                                }
                                            }

                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Photo Geotagged ✓",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF166534)
                                                    )
                                                }
                                                Text(
                                                    text = "📍 ${currentLatitude}° N, ${currentLongitude}° E",
                                                    fontSize = 10.sp,
                                                    color = Color(0xFF15803D),
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                                Text(
                                                    text = selectedLocation,
                                                    fontSize = 9.sp,
                                                    color = Color(0xFF166534)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                attachedPhotoUri = null
                                                hasPhotoAttached = false
                                                Toast.makeText(context, "Photo removed", Toast.LENGTH_SHORT).show()
                                            },
                                            modifier = Modifier.size(30.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Remove photo", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                                        }
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .border(BorderStroke(1.dp, BentoSlate200), RoundedCornerShape(8.dp))
                                        .padding(10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "No photo attached yet. Attach a photo above to provide evidence.",
                                        fontSize = 11.sp,
                                        color = BentoSlate600
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Urgency Selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଜରୁରୀ ସ୍ତର:" else "Urgency Level:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )

                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("NORMAL" to "Normal", "HIGH" to "High", "CRITICAL" to "⚠️ Critical").forEach { (urgKey, urgLabel) ->
                                        val isSel = urgencyLevel == urgKey
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(
                                                    if (isSel) {
                                                        when (urgKey) {
                                                            "CRITICAL" -> Color(0xFFDC2626)
                                                            "HIGH" -> Color(0xFFEA580C)
                                                            else -> BentoPrimaryBlue
                                                        }
                                                    } else BentoSlate100
                                                )
                                                .clickable { urgencyLevel = urgKey }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = urgLabel,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSel) Color.White else BentoSlate700
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Save as Local Draft Button (Room DB Persistence)
                            OutlinedButton(
                                onClick = {
                                    val finalTitle = if (issueTitle.isNotBlank()) issueTitle else "$selectedCategoriesFormatted near $selectedLocation (Draft)"
                                    val finalDesc = if (issueDetails.isNotBlank()) issueDetails else "Local draft recorded for later submission."
                                    onSaveDraft?.invoke(
                                        finalTitle,
                                        selectedCategoriesFormatted,
                                        selectedLocation,
                                        finalDesc,
                                        urgencyLevel,
                                        hasPhotoAttached,
                                        attachedPhotoUri,
                                        currentLatitude,
                                        currentLongitude,
                                        geoAddressString,
                                        editingDraftId
                                    )
                                    val draftMsg = if (editingDraftId != null) "Draft updated in Room DB" else "Saved as local draft in Room DB"
                                    Toast.makeText(context, draftMsg, Toast.LENGTH_SHORT).show()
                                    issueTitle = ""
                                    issueDetails = ""
                                    attachedPhotoUri = null
                                    hasPhotoAttached = false
                                    editingDraftId = null
                                    showReportForm = false
                                    activeSection = "MY_REPORTS"
                                    myReportsFilter = "DRAFTS"
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("save_as_local_draft_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFD97706)),
                                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Storage, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଲୋକାଲ୍ ଡ୍ରାଫ୍ଟ ଭାବେ ସଂରକ୍ଷଣ କରନ୍ତୁ (Room DB)" else "Save as Local Draft (Room DB)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Submit Button with Spring-Based Animation
                            val submitBtnScale by animateFloatAsState(
                                targetValue = if (issueTitle.isNotBlank()) 1.0f else 0.97f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                ),
                                label = "submit_civic_spring_scale"
                            )
                            Button(
                                onClick = {
                                    if (issueTitle.isBlank()) {
                                        Toast.makeText(context, "Please enter an issue title", Toast.LENGTH_SHORT).show()
                                        return@Button
                                    }
                                    val randomIdNum = Random.nextInt(1000, 9999)
                                    val newTrackId = "BLS-CIVIC-2026-$randomIdNum"
                                    val selectedCatNames = categories.filter { selectedCategoryIds.contains(it.id) }.map { it.nameEn }.ifEmpty { listOf(selectedCategory.nameEn) }

                                    val newIssue = CivicIssue(
                                        id = "civic_${System.currentTimeMillis()}",
                                        title = issueTitle,
                                        odiaTitle = issueTitle,
                                        category = selectedCategoriesFormatted,
                                        location = selectedLocation,
                                        odiaLocation = selectedLocation,
                                        description = if (issueDetails.isNotBlank()) issueDetails else "Reported by citizen via Baleshwar 360",
                                        status = "PENDING",
                                        assignedDepartment = "Ward Sanitary & Municipal Taskforce",
                                        reportedTime = "Just now",
                                        upvotes = 1,
                                        trackingId = newTrackId,
                                        reportedTimestampMs = System.currentTimeMillis(),
                                        latitude = currentLatitude,
                                        longitude = currentLongitude,
                                        hasPhotoAttached = hasPhotoAttached,
                                        photoUri = attachedPhotoUri
                                    )
                                    onSubmitReport?.invoke(
                                        issueTitle,
                                        selectedCategoriesFormatted,
                                        selectedLocation,
                                        if (issueDetails.isNotBlank()) issueDetails else "Reported by citizen via Baleshwar 360",
                                        urgencyLevel,
                                        hasPhotoAttached,
                                        attachedPhotoUri,
                                        currentLatitude,
                                        currentLongitude,
                                        geoAddressString
                                    )
                                    // Also organize multi-select categories in Firestore civic_reports collection
                                    runCatching {
                                        val firestorePayload = hashMapOf(
                                            "trackingId" to newTrackId,
                                            "title" to issueTitle,
                                            "category" to selectedCategoriesFormatted,
                                            "categories" to selectedCatNames,
                                            "categoryIds" to selectedCategoryIds.toList(),
                                            "wardLocation" to selectedLocation,
                                            "description" to (if (issueDetails.isNotBlank()) issueDetails else "Reported via Baleshwar 360"),
                                            "urgency" to urgencyLevel,
                                            "status" to "SUBMITTED",
                                            "latitude" to currentLatitude,
                                            "longitude" to currentLongitude,
                                            "timestamp" to System.currentTimeMillis()
                                        )
                                        com.google.firebase.firestore.FirebaseFirestore.getInstance(
                                            context.getString(com.example.R.string.firestore_database_id)
                                        )
                                            .collection("civic_reports")
                                            .document(newTrackId)
                                            .set(firestorePayload)
                                    }
                                    if (editingDraftId != null) {
                                        onDeleteReport?.invoke(editingDraftId!!)
                                        editingDraftId = null
                                    }
                                    civicIssues.add(0, newIssue)
                                    lastSubmittedTrackingId = newTrackId
                                    issueTitle = ""
                                    issueDetails = ""
                                    attachedPhotoUri = null
                                    hasPhotoAttached = false
                                    showReportForm = false
                                    activeSection = "MY_REPORTS"
                                    myReportsFilter = "PENDING"
                                    Toast.makeText(context, "Logged grievance $newTrackId with Balasore Municipality (Room DB)", Toast.LENGTH_LONG).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .scale(submitBtnScale)
                                    .testTag("submit_civic_report_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଅଭିଯୋଗ ଦାଖଲ କରନ୍ତୁ (Room DB)" else "Submit Grievance to Municipality (Room DB)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (activeSection == "MY_REPORTS") {
                // My Reports Section (Offline Room Database Tracking)
                item {
                    val myDrafts = myReports.count { it.status == "DRAFT" }
                    val myPending = myReports.count { it.status == "PENDING" }
                    val myProgress = myReports.count { it.status == "IN_PROGRESS" }
                    val myResolved = myReports.count { it.status == "RESOLVED" }

                    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        // Telemetry Sync Banner
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF1F5F9))
                                .border(BorderStroke(1.dp, BentoSlate200), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981))
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ଲୋକାଲ୍ Room DB ସକ୍ରିୟ" else "Local Room DB Active",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                }
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ସ୍ୱୟଂକ୍ରିୟ ସିଙ୍କ୍ ପ୍ରତି $autoUpdateMinutes ମିନିଟରେ" else "Auto-updates active every ${autoUpdateMinutes}m",
                                    fontSize = 10.sp,
                                    color = Color(0xFF64748B)
                                )
                            }

                            Button(
                                onClick = {
                                    onAutoProgress?.invoke()
                                    Toast.makeText(context, "Synced status with Balasore Municipality", Toast.LENGTH_SHORT).show()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryBlue),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (language == AppLanguage.ODIA) "ସିଙ୍କ୍" else "Sync", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Status Dropdown Filter + Filter Chips for My Reports
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ମୋ ଅଭିଯୋଗ ସ୍ଥିତି:" else "Filter by Status:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate900
                            )

                            Box {
                                Surface(
                                    onClick = { isMyReportsStatusDropdownOpen = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, BentoPrimaryBlue.copy(alpha = 0.4f)),
                                    modifier = Modifier.testTag("my_reports_status_dropdown_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (myReportsFilter) {
                                                "PENDING" -> "Pending ($myPending)"
                                                "IN_PROGRESS" -> "In-Progress ($myProgress)"
                                                "RESOLVED" -> "Resolved ($myResolved)"
                                                "DRAFTS" -> "Drafts ($myDrafts)"
                                                else -> "All (${myReports.size})"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoPrimaryBlue
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = if (isMyReportsStatusDropdownOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Status Filter Dropdown",
                                            tint = BentoPrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isMyReportsStatusDropdownOpen,
                                    onDismissRequest = { isMyReportsStatusDropdownOpen = false }
                                ) {
                                    listOf(
                                        "ALL" to "All (${myReports.size})",
                                        "PENDING" to "Pending ($myPending)",
                                        "IN_PROGRESS" to "In-Progress ($myProgress)",
                                        "RESOLVED" to "Resolved ($myResolved)",
                                        "DRAFTS" to "Drafts ($myDrafts)"
                                    ).forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = { Text(label, fontSize = 12.sp, fontWeight = if (myReportsFilter == key) FontWeight.Bold else FontWeight.Normal) },
                                            onClick = {
                                                myReportsFilter = key
                                                isMyReportsStatusDropdownOpen = false
                                            }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            items(
                                listOf(
                                    "ALL" to (if (language == AppLanguage.ODIA) "ସମସ୍ତ (${myReports.size})" else "All (${myReports.size})"),
                                    "DRAFTS" to (if (language == AppLanguage.ODIA) "ଡ୍ରାଫ୍ଟ ($myDrafts)" else "Drafts ($myDrafts)"),
                                    "PENDING" to (if (language == AppLanguage.ODIA) "ବକେୟା ($myPending)" else "Pending ($myPending)"),
                                    "IN_PROGRESS" to (if (language == AppLanguage.ODIA) "ଚାଲୁ ($myProgress)" else "In-Progress ($myProgress)"),
                                    "RESOLVED" to (if (language == AppLanguage.ODIA) "ସମାହିତ ($myResolved)" else "Resolved ($myResolved)")
                                )
                            ) { (key, label) ->
                                val isSel = myReportsFilter == key
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) BentoPrimaryBlue else BentoSlate100)
                                        .clickable { myReportsFilter = key }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else BentoSlate700
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Ward Filter in My Reports
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ୱାର୍ଡ ଫିଲ୍ଟର୍:" else "Filter by Ward:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate700
                            )

                            if (myReportsWardFilter != null) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF2F2),
                                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { myReportsWardFilter = null }
                                        .testTag("clear_ward_filter_btn")
                                ) {
                                    Text(
                                        text = "Ward $myReportsWardFilter ✕ Clear",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFDC2626),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            item {
                                val isAll = myReportsWardFilter == null
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isAll) BentoPrimaryBlue else BentoSlate100)
                                        .clickable { myReportsWardFilter = null }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "All Wards (31)",
                                        fontSize = 10.sp,
                                        fontWeight = if (isAll) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isAll) Color.White else BentoSlate700
                                    )
                                }
                            }
                            items(allBalasoreWards) { ward ->
                                val isSel = myReportsWardFilter == ward.wardNumber
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) BentoPrimaryBlue else BentoSlate100)
                                        .clickable {
                                            myReportsWardFilter = if (isSel) null else ward.wardNumber
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "W${ward.wardNumber}: ${ward.nameEn}",
                                        fontSize = 10.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else BentoSlate700
                                    )
                                }
                            }
                        }
                    }
                }

                val myFilteredReports = myReports.filter { report ->
                    val matchesStatus = when (myReportsFilter) {
                        "DRAFTS" -> report.status == "DRAFT"
                        "PENDING" -> report.status == "PENDING"
                        "IN_PROGRESS" -> report.status == "IN_PROGRESS"
                        "RESOLVED" -> report.status == "RESOLVED"
                        else -> true
                    }
                    val matchesWard = myReportsWardFilter == null ||
                        report.wardLocation.contains("Ward ${myReportsWardFilter} ", ignoreCase = true) ||
                        report.wardLocation.contains("Ward ${myReportsWardFilter}:", ignoreCase = true) ||
                        report.wardLocation.contains("Ward ${myReportsWardFilter}-", ignoreCase = true) ||
                        report.wardLocation.endsWith("Ward ${myReportsWardFilter}", ignoreCase = true)
                    matchesStatus && matchesWard
                }

                if (myFilteredReports.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📋", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଏହି ସ୍ଥିତିରେ କୌଣସି ଅଭିଯୋଗ ନାହିଁ" else "No Reports in this Status Category",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate700
                                )
                            }
                        }
                    }
                } else {
                    items(myFilteredReports, key = { it.id }) { report ->
                        MyCivicReportItemRow(
                            report = report,
                            language = language,
                            isExpanded = expandedReportIds.contains(report.id) || highlightedReportId == report.id,
                            onToggleExpand = {
                                expandedReportIds = if (expandedReportIds.contains(report.id)) {
                                    expandedReportIds - report.id
                                } else {
                                    expandedReportIds + report.id
                                }
                            },
                            onViewPhoto = {
                                photoPreviewTarget = Triple(report.title, report.geoAddress ?: report.wardLocation, report.photoUri)
                            },
                            onAdvanceStatus = { onAdvanceStatus?.invoke(report.id) },
                            onSubmitDraft = {
                                onSubmitDraft?.invoke(report.id)
                                Toast.makeText(context, "Draft submitted to Balasore Municipal Desk", Toast.LENGTH_SHORT).show()
                            },
                            onEditDraft = {
                                editingDraftId = report.id
                                issueTitle = report.title.removeSuffix(" (Draft)")
                                issueDetails = if (report.description.startsWith("Local draft recorded")) "" else report.description
                                selectedLocation = report.wardLocation
                                selectedCategory = categories.find { it.nameEn == report.category } ?: categories.first()
                                urgencyLevel = report.urgency
                                hasPhotoAttached = report.hasPhotoAttached
                                attachedPhotoUri = report.photoUri
                                currentLatitude = report.latitude ?: 21.4925
                                currentLongitude = report.longitude ?: 86.9312
                                geoAddressString = report.geoAddress ?: "Balasore Municipal Ward"
                                showReportForm = true
                                Toast.makeText(context, "Loaded draft into form for editing", Toast.LENGTH_SHORT).show()
                            },
                            onDelete = { onDeleteReport?.invoke(report.id) },
                            onShare = {
                                val sendIntent = Intent().apply {
                                    action = Intent.ACTION_SEND
                                    putExtra(
                                        Intent.EXTRA_TEXT,
                                        "Balasore Civic Issue [${report.id}]: ${report.title} at ${report.wardLocation}. Status: ${report.status}. Notes: ${report.resolutionNotes}. Track on Baleshwar 360 app."
                                    )
                                    type = "text/plain"
                                }
                                context.startActivity(Intent.createChooser(sendIntent, null))
                            }
                        )
                    }
                }
            } else {
                // Community Civic Issues Feed
                val filteredIssues = civicIssues.filter { issue ->
                    when (filterStatus) {
                        "PENDING" -> issue.status == "PENDING" || issue.status == "SUBMITTED"
                        "IN_PROGRESS", "ACTIVE" -> issue.status == "IN_PROGRESS"
                        "RESOLVED" -> issue.status == "RESOLVED"
                        else -> true
                    }
                }

                // Interactive Map Component with Pins to Jump to Report in the List
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("civic_eye_interactive_pin_map_card"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                        border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("🗺️", fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.ODIA) {
                                                "ବାଲେଶ୍ୱର ସିଭିକ୍ ଆଇ ରିପୋର୍ଟ ପିନ୍ ମ୍ୟାପ୍"
                                            } else {
                                                "Balasore Civic Report Pin Map"
                                            },
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BentoSlate900
                                        )
                                        Text(
                                            text = if (language == AppLanguage.ODIA) {
                                                "ଯେକୌଣସି ପିନ୍ ଟ୍ୟାପ୍ କରି ତାଲିକାରେ ଥିବା ରିପୋର୍ଟକୁ ଯାଆନ୍ତୁ"
                                            } else {
                                                "Tap any geographical pin to jump to that report in the list"
                                            },
                                            fontSize = 10.sp,
                                            color = BentoSlate600
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = BentoPrimaryBlue.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "${filteredIssues.size} Pins",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimaryBlue,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Geographical Map Grid Canvas with Interactive Pins
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(118.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(Color(0xFFE0F2FE))
                                    .border(BorderStroke(1.dp, Color(0xFF7DD3FC)), RoundedCornerShape(10.dp))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "Balasore Municipal Grid (21.48°N–21.52°N, 86.89°E–86.94°E)",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0369A1),
                                    modifier = Modifier.align(Alignment.TopStart)
                                )

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.Center)
                                        .padding(top = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    filteredIssues.forEachIndexed { idx, pinIssue ->
                                        val isPinHighlighted = highlightedReportId == pinIssue.id
                                        val pinColor = when (pinIssue.status) {
                                            "RESOLVED" -> Color(0xFF16A34A)
                                            "IN_PROGRESS" -> Color(0xFF0284C7)
                                            else -> Color(0xFFD97706)
                                        }
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(if (isPinHighlighted) Color.White else Color.White.copy(alpha = 0.75f))
                                                .border(
                                                    width = if (isPinHighlighted) 1.5.dp else 1.dp,
                                                    color = if (isPinHighlighted) pinColor else Color.Transparent,
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                                .clickable {
                                                    highlightedReportId = pinIssue.id
                                                    expandedReportIds = expandedReportIds + pinIssue.id
                                                    coroutineScope.launch {
                                                        // Header + switcher + quick card + map + filter header = ~5 items before list
                                                        val targetIdx = (5 + idx).coerceAtLeast(0)
                                                        lazyListState.animateScrollToItem(targetIdx)
                                                    }
                                                }
                                                .padding(horizontal = 7.dp, vertical = 5.dp)
                                                .testTag("civic_map_pin_${pinIssue.id}")
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(22.dp)
                                                    .clip(CircleShape)
                                                    .background(pinColor),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.LocationOn,
                                                    contentDescription = pinIssue.location,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = pinIssue.location.substringBefore(",").take(12),
                                                fontSize = 8.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoSlate900,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = formatCivicRelativeTimestamp(pinIssue.reportedTimestampMs, AppLanguage.ENGLISH),
                                                fontSize = 7.5.sp,
                                                color = pinColor,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ସାମ୍ପ୍ରତିକ ନାଗରିକ ଅଭିଯୋଗ ଟ୍ରାକର୍" else "Active Community Civic Tracker",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = BentoSlate900)
                            )

                            // Status Dropdown Filter (All, Pending, In-Progress, Resolved)
                            Box {
                                Surface(
                                    onClick = { isCommunityStatusDropdownOpen = true },
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEFF6FF),
                                    border = BorderStroke(1.dp, BentoPrimaryBlue.copy(alpha = 0.4f)),
                                    modifier = Modifier.testTag("civic_status_dropdown_filter_btn")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = when (filterStatus) {
                                                "PENDING" -> "Pending"
                                                "IN_PROGRESS", "ACTIVE" -> "In-Progress"
                                                "RESOLVED" -> "Resolved"
                                                else -> "All Statuses"
                                            },
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoPrimaryBlue
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Icon(
                                            imageVector = if (isCommunityStatusDropdownOpen) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                                            contentDescription = "Filter by Status",
                                            tint = BentoPrimaryBlue,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = isCommunityStatusDropdownOpen,
                                    onDismissRequest = { isCommunityStatusDropdownOpen = false }
                                ) {
                                    listOf(
                                        "ALL" to "All",
                                        "PENDING" to "Pending",
                                        "IN_PROGRESS" to "In-Progress",
                                        "RESOLVED" to "Resolved"
                                    ).forEach { (key, label) ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = label,
                                                    fontSize = 12.sp,
                                                    fontWeight = if (filterStatus == key) FontWeight.Bold else FontWeight.Normal
                                                )
                                            },
                                            onClick = {
                                                filterStatus = key
                                                isCommunityStatusDropdownOpen = false
                                            },
                                            modifier = Modifier.testTag("civic_status_dropdown_item_${key.lowercase()}")
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(
                                "ALL" to "All",
                                "PENDING" to "Pending",
                                "IN_PROGRESS" to "In-Progress",
                                "RESOLVED" to "Resolved"
                            ).forEach { (key, label) ->
                                val isSel = filterStatus == key
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (isSel) BentoPrimaryBlue else BentoSlate100)
                                        .clickable { filterStatus = key }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                        .testTag("civic_status_chip_${key.lowercase()}")
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSel) Color.White else BentoSlate700
                                    )
                                }
                            }
                        }
                    }
                }

                items(filteredIssues, key = { it.id }) { issue ->
                    CivicIssueCard(
                        issue = issue,
                        language = language,
                        isExpanded = expandedReportIds.contains(issue.id) || highlightedReportId == issue.id,
                        onToggleExpand = {
                            expandedReportIds = if (expandedReportIds.contains(issue.id)) {
                                expandedReportIds - issue.id
                            } else {
                                expandedReportIds + issue.id
                            }
                        },
                        onViewPhoto = {
                            photoPreviewTarget = Triple(issue.title, issue.location, issue.photoUri)
                        },
                        onUpvote = {
                            val index = civicIssues.indexOfFirst { it.id == issue.id }
                            if (index != -1) {
                                val current = civicIssues[index]
                                civicIssues[index] = current.copy(
                                    upvotes = if (current.isUpvotedByUser) current.upvotes - 1 else current.upvotes + 1,
                                    isUpvotedByUser = !current.isUpvotedByUser
                                )
                            }
                        },
                        onShare = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Balasore Civic Issue [${issue.trackingId}]: ${issue.title} at ${issue.location}. Status: ${issue.status}. Track on Baleshwar 360 app."
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }
                    )
                }
            }
            }

            // Floating Action Button (FAB) to open the New Infrastructure Report Form
            ExtendedFloatingActionButton(
                onClick = {
                    showReportForm = true
                    coroutineScope.launch {
                        lazyListState.animateScrollToItem(2)
                    }
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Submit New Civic Report",
                        tint = Color.White
                    )
                },
                text = {
                    Text(
                        text = if (language == AppLanguage.ODIA) "+ ନୂତନ ରିପୋର୍ଟ" else "Report Issue",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                },
                containerColor = BentoPrimaryBlue,
                contentColor = Color.White,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .testTag("civic_eye_new_report_fab")
            )
        }

    if (photoPreviewTarget != null) {
        val (previewTitle, previewLocation, previewUri) = photoPreviewTarget!!
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { photoPreviewTarget = null },
            title = {
                Text(
                    text = if (language == AppLanguage.ODIA) "📷 ଜିଓ-ଟ୍ୟାଗ୍ ଫଟୋ ପ୍ରମାଣ" else "📷 Geotagged Report Photo Evidence",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(text = previewTitle, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = BentoSlate900)
                    Text(text = "📍 $previewLocation", fontSize = 11.5.sp, color = BentoSlate600)
                    if (previewUri != null && (previewUri.startsWith("content://") || previewUri.startsWith("file://"))) {
                        AsyncImage(
                            model = previewUri,
                            contentDescription = previewTitle,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFE0F2FE))
                                .border(BorderStroke(1.dp, Color(0xFF7DD3FC)), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📸", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Verified GPS Field Evidence Snapshot",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1)
                                )
                                Text(
                                    text = previewLocation,
                                    fontSize = 10.5.sp,
                                    color = BentoSlate600
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { photoPreviewTarget = null },
                    colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryBlue)
                ) {
                    Text(if (language == AppLanguage.ODIA) "ବନ୍ଦ କରନ୍ତୁ" else "Close Preview")
                }
            }
        )
    }

    if (showWardsDirectory) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showWardsDirectory = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                com.example.ui.features.municipal.BalasoreWardsDirectorySheet(
                    language = language,
                    onLocateWardOnMap = { ward ->
                        showWardsDirectory = false
                        onOpenCivicMap?.invoke()
                    },
                    onReportCivicIssueInWard = { ward ->
                        showWardsDirectory = false
                        selectedLocation = ward.formattedDisplayNameEn
                        val coords = wardGeoCoordinates[selectedLocation] ?: Triple(ward.latitude, ward.longitude, ward.geoAddress)
                        currentLatitude = coords.first
                        currentLongitude = coords.second
                        geoAddressString = coords.third
                        showReportForm = true
                    },
                    onClose = { showWardsDirectory = false }
                )
            }
        }
    }

    if (showLeaderboardSheet) {
        androidx.compose.ui.window.Dialog(
            onDismissRequest = { showLeaderboardSheet = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            androidx.compose.material3.Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.White
            ) {
                CitizenContributionLeaderboardSheet(
                    language = language,
                    myReports = myReports,
                    userName = userName,
                    onOpenCivicEyeForm = {
                        showLeaderboardSheet = false
                        showReportForm = true
                    },
                    onClose = { showLeaderboardSheet = false }
                )
            }
        }
    }
}
}

@Composable
fun CivicIssueCard(
    issue: CivicIssue,
    language: AppLanguage,
    isExpanded: Boolean = true,
    onToggleExpand: () -> Unit = {},
    onViewPhoto: () -> Unit = {},
    onUpvote: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val relativeTimeText = remember(issue.reportedTimestampMs, issue.reportedTime, language) {
        if (issue.reportedTime.isNotBlank() && issue.reportedTimestampMs <= 0L) {
            issue.reportedTime
        } else {
            formatCivicRelativeTimestamp(issue.reportedTimestampMs, language)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onToggleExpand() }
            .testTag("civic_issue_card_${issue.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, BentoSlate100)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Top Row: Category + Relative Timestamp Badge + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = issue.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryBlue
                        )
                    }

                    // Human-Readable Freshness Timestamp Pill (e.g., '2 hours ago')
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.testTag("civic_issue_relative_timestamp_${issue.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = BentoSlate600,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = relativeTimeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoSlate700
                            )
                        }
                    }
                }

                val (statusBg, statusFg, statusLabel) = when (issue.status) {
                    "RESOLVED" -> Triple(Color(0xFFDCFCE7), Color(0xFF15803D), "RESOLVED")
                    "IN_PROGRESS" -> Triple(Color(0xFFFEF3C7), Color(0xFFB45309), "IN-PROGRESS")
                    else -> Triple(Color(0xFFDBEAFE), Color(0xFF1D4ED8), "PENDING")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = statusFg
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = if (language == AppLanguage.ODIA) issue.odiaTitle else issue.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate900,
                    lineHeight = 20.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Location & Freshness Timestamp Row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = BentoSlate400,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = if (language == AppLanguage.ODIA) issue.odiaLocation else issue.location,
                    fontSize = 11.sp,
                    color = BentoSlate600,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "• $relativeTimeText",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BentoPrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Description
            Text(
                text = issue.description,
                style = MaterialTheme.typography.bodySmall.copy(color = BentoSlate700, lineHeight = 18.sp)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Visual Progress Stepper (Pending -> In-Progress -> Resolved)
            CivicResolutionProgressStepper(
                status = issue.status,
                language = language,
                trackingId = issue.trackingId
            )

            // Expandable Summary Details View (Timestamp, Location Description, & 'View Photo' Button)
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, BentoSlate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("civic_issue_expanded_details_${issue.id}")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🕒 Reported: $relativeTimeText (${issue.reportedTime})",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate700
                                )
                                Text(
                                    text = issue.trackingId,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoPrimaryBlue
                                )
                            }
                            Text(
                                text = "📍 Location: ${issue.location} (GPS: ${issue.latitude}° N, ${issue.longitude}° E)",
                                fontSize = 10.5.sp,
                                color = BentoSlate600
                            )
                            Text(
                                text = "🏛️ Assigned Dept: ${issue.assignedDepartment}",
                                fontSize = 10.sp,
                                color = BentoSlate600,
                                fontWeight = FontWeight.Medium
                            )

                            if (issue.hasPhotoAttached) {
                                OutlinedButton(
                                    onClick = onViewPhoto,
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.testTag("civic_view_photo_btn_${issue.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = "View Photo",
                                        tint = BentoPrimaryBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ଫଟୋ ଦେଖନ୍ତୁ (View Photo)" else "View Photo",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoPrimaryBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Upvote / Endorse (with Spring Animation) & Share on Social Media
            val cardSpringScale by animateFloatAsState(
                targetValue = if (issue.isUpvotedByUser) 1.03f else 1.0f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "civic_upvote_spring"
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .scale(cardSpringScale)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (issue.isUpvotedByUser) Color(0xFFEFF6FF) else BentoSlate100)
                        .clickable { onUpvote() }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Upvote",
                        tint = if (issue.isUpvotedByUser) BentoPrimaryBlue else BentoSlate600,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${issue.upvotes} Citizens Agree",
                        fontSize = 11.sp,
                        fontWeight = if (issue.isUpvotedByUser) FontWeight.Bold else FontWeight.Medium,
                        color = if (issue.isUpvotedByUser) BentoPrimaryBlue else BentoSlate700
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE0F2FE))
                        .clickable { onShare() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("civic_issue_share_btn_${issue.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Issue on Social Media",
                        tint = Color(0xFF0284C7),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (language == AppLanguage.ODIA) "ସୋସିଆଲ୍ ସେୟାର୍" else "Share Issue",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }
            }
        }
    }
}

@Composable
private fun MyCivicReportItemRow(
    report: CivicReportEntity,
    language: AppLanguage,
    isExpanded: Boolean = true,
    onToggleExpand: () -> Unit = {},
    onViewPhoto: () -> Unit = {},
    onAdvanceStatus: () -> Unit,
    onSubmitDraft: (() -> Unit)? = null,
    onEditDraft: (() -> Unit)? = null,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusBg, statusFg, statusLabel, statusIcon) = when (report.status) {
        "DRAFT" -> CivicQuadColor(Color(0xFFFEF3C7), Color(0xFFD97706), if (language == AppLanguage.ODIA) "ସ୍ଥାନୀୟ ଡ୍ରାଫ୍ଟ" else "LOCAL DRAFT", Icons.Default.Info)
        "RESOLVED" -> CivicQuadColor(Color(0xFFDCFCE7), Color(0xFF15803D), if (language == AppLanguage.ODIA) "ସମାହିତ" else "RESOLVED", Icons.Default.CheckCircle)
        "IN_PROGRESS" -> CivicQuadColor(Color(0xFFDBEAFE), Color(0xFF1D4ED8), if (language == AppLanguage.ODIA) "ଚାଲୁଅଛି" else "IN-PROGRESS", Icons.Default.Build)
        else -> CivicQuadColor(Color(0xFFFEF3C7), Color(0xFFB45309), if (language == AppLanguage.ODIA) "ବକେୟା" else "PENDING", Icons.Default.HourglassTop)
    }

    val relativeTimeText = remember(report.reportedTimestamp, language) {
        formatCivicRelativeTimestamp(report.reportedTimestamp, language)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onToggleExpand() }
            .testTag("my_civic_report_card_${report.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, BentoSlate200)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Category, Relative Freshness Timestamp & Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFEFF6FF))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = report.category,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryBlue
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Human-readable freshness timestamp badge (e.g., '2 hours ago')
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.testTag("civic_report_relative_timestamp_${report.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = BentoSlate600,
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = relativeTimeText,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoSlate700
                            )
                        }
                    }

                    if (report.urgency == "CRITICAL" || report.urgency == "HIGH") {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFEE2E2))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "⚠️ ${report.urgency}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = statusIcon, contentDescription = null, tint = statusFg, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = statusFg
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = report.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = BentoSlate900,
                lineHeight = 20.sp
            )

            // Location & Relative Timestamp
            Row(
                modifier = Modifier.padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(13.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.geoAddress ?: report.wardLocation,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF64748B)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• $relativeTimeText",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BentoPrimaryBlue
                )
            }

            // GPS Coordinates Chip & 'View Photo' Button
            if (report.latitude != null && report.longitude != null) {
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFF1F5F9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "📍 GPS: ${report.latitude}° N, ${report.longitude}° E",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF334155)
                        )
                    }
                    if (report.hasPhotoAttached) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFDCFCE7))
                                .clickable { onViewPhoto() }
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .testTag("civic_view_photo_btn_${report.id}")
                        ) {
                            Text(
                                text = "📷 View Photo",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF15803D)
                            )
                        }
                    }
                }
            }

            // Photo Preview if attached and expanded
            if (isExpanded && report.hasPhotoAttached && report.photoUri != null) {
                Spacer(modifier = Modifier.height(6.dp))
                if (report.photoUri.startsWith("content://") || report.photoUri.startsWith("file://")) {
                    AsyncImage(
                        model = report.photoUri,
                        contentDescription = "Civic Evidence Photo",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (report.category.contains("Drain")) Color(0xFFE0F2FE) else Color(0xFFFFEDD5))
                            .border(BorderStroke(1.dp, if (report.category.contains("Drain")) Color(0xFFBAE6FD) else Color(0xFFFED7AA)), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(if (report.category.contains("Drain")) "🌊" else "🕳️", fontSize = 20.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (report.category.contains("Drain")) "Waterlogged Drain Field Evidence Tagged" else "Asphalt Pothole Crater Evidence Tagged",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (report.category.contains("Drain")) Color(0xFF0369A1) else Color(0xFFC2410C)
                                )
                                Text(
                                    text = "Verified at Lat: ${report.latitude ?: 21.4925}° N, Long: ${report.longitude ?: 86.9312}° E",
                                    fontSize = 9.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }

            // Description
            if (report.description.isNotBlank()) {
                Text(
                    text = report.description,
                    fontSize = 12.sp,
                    color = BentoSlate700,
                    modifier = Modifier.padding(top = 6.dp),
                    lineHeight = 17.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Progress Stepper (Pending, In-Progress, Resolved) or Draft Indicator
            if (report.status == "DRAFT") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFFFBEB))
                        .border(BorderStroke(1.dp, Color(0xFFFDE68A)), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ଏହା ସ୍ଥାନୀୟ ଡ୍ରାଫ୍ଟ ଭାବେ ସଂରକ୍ଷିତ। ସମ୍ପାଦନ କରନ୍ତୁ କିମ୍ବା ବର୍ତ୍ତମାନ ଦାଖଲ କରନ୍ତୁ।" else "Saved locally as draft. Edit details or tap 'Submit Now' to transmit to Balasore Municipality.",
                            fontSize = 10.5.sp,
                            color = Color(0xFF92400E),
                            lineHeight = 15.sp
                        )
                    }
                }
            } else {
                CivicResolutionProgressStepper(
                    status = report.status,
                    language = language,
                    trackingId = report.id
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Assigned Department & Resolution Notes Box
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(BorderStroke(1.dp, BentoSlate100), RoundedCornerShape(8.dp))
                    .padding(10.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dept: ${report.assignedDepartment}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                        Text(
                            text = report.id,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoPrimaryBlue
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = report.resolutionNotes,
                        fontSize = 11.sp,
                        color = Color(0xFF334155),
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions
            if (report.status == "DRAFT") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Button(
                            onClick = { onSubmitDraft?.invoke() },
                            colors = ButtonDefaults.buttonColors(containerColor = BentoPrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("submit_draft_btn")
                        ) {
                            Icon(imageVector = Icons.Default.AddAlert, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "ବର୍ତ୍ତମାନ ଦାଖଲ" else "Submit Now",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { onEditDraft?.invoke() },
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("edit_draft_btn")
                        ) {
                            Icon(imageVector = Icons.Default.Build, contentDescription = null, modifier = Modifier.size(12.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "ସମ୍ପାଦନ" else "Edit Draft",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_draft_btn")
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Draft", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (report.status != "RESOLVED") {
                        OutlinedButton(
                            onClick = onAdvanceStatus,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = BentoPrimaryBlue),
                            border = BorderStroke(1.dp, BentoPrimaryBlue),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (report.status == "PENDING") "Advance Status" else "Mark Resolved",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "କାର୍ଯ୍ୟ ସମାପ୍ତ ✓" else "Closed in Registry ✓",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF16A34A)
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                        }
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

