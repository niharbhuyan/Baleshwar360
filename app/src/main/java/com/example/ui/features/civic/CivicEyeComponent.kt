package com.example.ui.features.civic

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.compose.ui.graphics.asImageBitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import coil.compose.AsyncImage
import com.example.data.local.CivicReportEntity
import com.example.data.model.AppLanguage
import com.example.data.model.FirestoreUserReport
import com.example.util.BalasoreHapticHelper
import com.example.util.ShareHelper
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Civic Eye Component: Allows citizens of Balasore to report and monitor infrastructure issues
 * like potholes and waterlogging, persisting real-time records directly to Firebase Firestore
 * and local Room database with full offline-first resilience.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivicEyeComponent(
    language: AppLanguage = AppLanguage.ENGLISH,
    myReports: List<CivicReportEntity> = emptyList(),
    firestoreReports: List<FirestoreUserReport> = emptyList(),
    isFirestoreLoading: Boolean = false,
    onRefreshFirestore: () -> Unit = {},
    onSubmitReport: (
        title: String,
        category: String,
        wardLocation: String,
        description: String,
        urgency: String,
        hasPhoto: Boolean,
        photoUri: String?,
        latitude: Double?,
        longitude: Double?,
        geoAddress: String?,
        userId: String,
        userName: String
    ) -> Unit,
    onAdvanceStatus: (String) -> Unit = {},
    onDeleteReport: (String) -> Unit = {},
    onUpvoteReport: (String) -> Unit = {},
    onQueueReportOffline: ((
        title: String,
        category: String,
        wardLocation: String,
        description: String,
        urgency: String,
        hasPhoto: Boolean,
        photoUri: String?
    ) -> Unit)? = null,
    onFlushSyncQueue: (() -> Int)? = null,
    onClose: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA

    var selectedTabIndex by remember { mutableIntStateOf(0) } // 0: Submit Report, 1: Live Feed (List View), 2: Map Pins
    var reportCategory by remember { mutableStateOf("Potholes") }
    var issueTitle by remember { mutableStateOf("") }
    var selectedWard by remember { mutableStateOf("Ward 14 - OT Road / Phandi Chhak") }
    var issueDescription by remember { mutableStateOf("") }
    var selectedUrgency by remember { mutableStateOf("HIGH") }
    var attachedPhotoUri by remember { mutableStateOf<String?>(null) }
    var attachedPhotoBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }
    var submissionSuccessId by remember { mutableStateOf<String?>(null) }
    var queueWhenOnlineMode by remember { mutableStateOf(false) }

    // Search & Filter in Feed
    var searchQuery by remember { mutableStateOf("") }
    var feedStatusFilter by remember { mutableStateOf("ALL") }
    var feedCategoryFilter by remember { mutableStateOf("ALL") }
    var feedUrgencyFilter by remember { mutableStateOf("ALL") }
    var showFeedFilterSheet by remember { mutableStateOf(false) }

    val queuedSyncReports = remember(myReports) {
        myReports.filter { it.isQueuedForSync }
    }

    // Combined Room + Firestore Reports across all tabs
    val allCombinedReports = remember(myReports, firestoreReports) {
        val firestoreAsEntities = firestoreReports.map { f ->
            CivicReportEntity(
                id = f.id,
                title = f.title,
                category = when (f.category) {
                    "pothole" -> "Road Pothole"
                    "waterlogging" -> "Drain Clog / Waterlogging"
                    "road" -> "Damaged Culvert / Road"
                    "garbage" -> "Garbage Heap / Waste"
                    "electricity" -> "Faulty Streetlight"
                    "water" -> "Drinking Water Leak"
                    else -> "Civic Issue"
                },
                wardLocation = f.wardLocation,
                description = f.description,
                status = when (f.status.uppercase()) {
                    "RESOLVED" -> "RESOLVED"
                    "IN_PROGRESS" -> "IN_PROGRESS"
                    else -> "PENDING"
                },
                urgency = f.urgency,
                assignedDepartment = "Balasore Municipality Grievance Cell",
                reportedTimestamp = f.reportedTimestamp,
                lastUpdatedTimestamp = f.lastUpdatedTimestamp,
                hasPhotoAttached = !f.photoUrl.isNullOrBlank(),
                photoUri = f.photoUrl,
                latitude = f.latitude,
                longitude = f.longitude,
                geoAddress = f.wardLocation,
                resolutionNotes = f.resolutionNotes ?: "Filed via Civic Eye."
            )
        }
        (myReports + firestoreAsEntities).distinctBy { it.id }.sortedByDescending { it.reportedTimestamp }
    }

    // Localities & Wards in Balasore
    val wardList = remember {
        listOf(
            "Ward 14 - OT Road / Phandi Chhak",
            "Ward 12 - Cinema Chhak",
            "Ward 4 - Sahadevkhunta Bus Stand",
            "Ward 1 - Azimabad / FM University",
            "Ward 7 - Motiganj Market",
            "Ward 9 - Station Bazaar",
            "Ward 18 - Sovarampur",
            "Ward 21 - Proof Road / Chandipur Link",
            "Chandipur Coastal Beach Road",
            "Remuna Golei / Khirachora Temple Area",
            "Soro Municipality Main Road",
            "Jaleswar Town Market",
            "Nilagiri Sub-division / College Road"
        )
    }

    // Camera / Single-Photo Evidence Launchers (Full-resolution FileProvider TakePicture + Photo Picker)
    var pendingCameraPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var pendingCameraPhotoFile by remember { mutableStateOf<File?>(null) }
    var cameraPermissionDeniedMessage by remember { mutableStateOf<String?>(null) }

    val createCivicPhotoOutput: () -> Pair<File, Uri>? = {
        runCatching {
            val photoDir = File(context.cacheDir, "civic_eye_photos").apply { mkdirs() }
            val photoFile = File(photoDir, "civic_issue_${System.currentTimeMillis()}.jpg")
            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                photoFile
            )
            photoFile to contentUri
        }.getOrNull()
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            cameraPermissionDeniedMessage = null
            attachedPhotoUri = uri.toString()
            try {
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    attachedPhotoBitmap = BitmapFactory.decodeStream(stream)
                }
            } catch (_: Exception) {
                attachedPhotoBitmap = null
            }
        }
    }

    val cameraCaptureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { saved ->
        val capturedUri = pendingCameraPhotoUri
        val capturedFile = pendingCameraPhotoFile
        if (saved && capturedUri != null) {
            cameraPermissionDeniedMessage = null
            // Enforce single-photo attachment by replacing any previously attached photo
            attachedPhotoUri = capturedUri.toString()
            attachedPhotoBitmap = runCatching {
                if (capturedFile != null && capturedFile.exists()) {
                    BitmapFactory.decodeFile(capturedFile.absolutePath)
                } else {
                    context.contentResolver.openInputStream(capturedUri)?.use { BitmapFactory.decodeStream(it) }
                }
            }.getOrNull()
            Toast.makeText(
                context,
                if (isOdia) "ଫଟୋ ସଂଲଗ୍ନ ହୋଇଛି!" else "Photo captured and attached to report!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            runCatching { capturedFile?.delete() }
        }
        pendingCameraPhotoUri = null
        pendingCameraPhotoFile = null
    }

    val launchCameraCapture: () -> Unit = {
        val output = createCivicPhotoOutput()
        if (output != null) {
            pendingCameraPhotoFile = output.first
            pendingCameraPhotoUri = output.second
            cameraCaptureLauncher.launch(output.second)
        } else {
            Toast.makeText(
                context,
                if (isOdia) "କ୍ୟାମେରା ଫାଇଲ୍ ପ୍ରସ୍ତୁତ ହୋଇପାରିଲା ନାହିଁ" else "Could not prepare camera capture file.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            cameraPermissionDeniedMessage = null
            launchCameraCapture()
        } else {
            cameraPermissionDeniedMessage = if (isOdia) {
                "କ୍ୟାମେରା ଅନୁମତି ନାକଚ ହୋଇଛି। ଆପଣ ଗ୍ୟାଲେରୀରୁ ଫଟୋ ବାଛିପାରିବେ କିମ୍ବା ସେଟିଂସରେ କ୍ୟାମେରା ଅନ୍ କରନ୍ତୁ।"
            } else {
                "Camera permission denied. Enable camera access in Settings or choose a photo from gallery."
            }
            Toast.makeText(
                context,
                if (isOdia) "କ୍ୟାମେରା ଅନୁମତି ଆବଶ୍ୟକ" else "Camera permission is required to take a photo",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val requestCameraAndCapture: () -> Unit = {
        BalasoreHapticHelper.vibrateButtonClick(context)
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCameraCapture()
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("civic_eye_component")
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
        // TOP HEADER
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 3.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Civic Eye",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isOdia) "ନାଗରିକ ଦୃଷ୍ଟି (Civic Eye)" else "Civic Eye",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    color = Color(0xFFDCFCE7),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "ROOM DB • SYNC QUEUE",
                                        color = Color(0xFF15803D),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isOdia) "ଖାଲ-ଖମା, ଷ୍ଟ୍ରିଟ୍ ଲାଇଟ୍ ଓ ଜଳ ଯୋଗାଣ ସମସ୍ୟା • Room DB ସିଙ୍କ୍" else "Potholes, lighting & water supply • Room DB sync-when-online queue",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    if (onClose != null) {
                        IconButton(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onClose()
                            },
                            modifier = Modifier.testTag("civic_eye_close_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // PRIMARY NAVIGATION TABS
                PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            selectedTabIndex = 0
                        },
                        text = {
                            Text(
                                text = if (isOdia) "ସମସ୍ୟା ଦାଖଲ" else "Submit Report",
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("civic_eye_tab_submit")
                    )

                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            selectedTabIndex = 1
                            onRefreshFirestore()
                        },
                        text = {
                            Text(
                                text = if (isOdia) "ଫିଡ୍ (${allCombinedReports.size})" else "Live Feed (${allCombinedReports.size})",
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.CloudQueue, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("civic_eye_tab_feed")
                    )

                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            selectedTabIndex = 2
                            onRefreshFirestore()
                        },
                        text = {
                            Text(
                                text = if (isOdia) "ମାନଚିତ୍ର ପିନ୍" else "Map Pins",
                                fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        icon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        modifier = Modifier.testTag("civic_eye_tab_map")
                    )
                }
            }
        }

        // CONTENT SECTION
        if (selectedTabIndex == 0) {
            // SUBMIT FORM TAB
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                contentPadding = PaddingValues(top = 14.dp, bottom = 32.dp)
            ) {
                // SUCCESS BANNER IF JUST SUBMITTED
                if (submissionSuccessId != null) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(24.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isOdia) "ରିପୋର୍ଟ ସଫଳତାର ସହ ଦାଖଲ ହୋଇଛି!" else "Report Submitted & Synced!",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF15803D)
                                    )
                                    Text(
                                        text = "Tracking ID: $submissionSuccessId • Saved in Room & synced to Firestore.",
                                        fontSize = 11.5.sp,
                                        color = Color(0xFF166534)
                                    )
                                }
                                TextButton(
                                    onClick = {
                                        selectedTabIndex = 1
                                        submissionSuccessId = null
                                    }
                                ) {
                                    Text("View in Feed", fontSize = 12.sp, color = Color(0xFF15803D), fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                // 0. ROOM DB & SYNC-WHEN-ONLINE QUEUE CONTROL CARD
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (queueWhenOnlineMode) Color(0xFFFFFBEB) else Color(0xFFF0F9FF),
                        border = BorderStroke(
                            1.dp,
                            if (queueWhenOnlineMode) Color(0xFFF59E0B) else Color(0xFF38BDF8)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("civic_eye_sync_queue_banner")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = if (queueWhenOnlineMode) Icons.Default.CloudQueue else Icons.Default.Storage,
                                        contentDescription = "Room Database Queue",
                                        tint = if (queueWhenOnlineMode) Color(0xFFD97706) else Color(0xFF0284C7),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isOdia) {
                                                "Room DB 'Sync-When-Online' କ୍ୟୁ (${queuedSyncReports.size} ବକେୟା)"
                                            } else {
                                                "Room DB Sync-When-Online Queue (${queuedSyncReports.size} Queued)"
                                            },
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 12.5.sp,
                                            color = Color(0xFF0F172A)
                                        )
                                        Text(
                                            text = if (queueWhenOnlineMode) {
                                                "Offline Queue Active: Reports & photos save to Room SQLite and sync when online"
                                            } else {
                                                "${myReports.size} reports stored in Room DB • Toggle to queue offline before syncing"
                                            },
                                            fontSize = 11.sp,
                                            color = Color(0xFF475569)
                                        )
                                    }
                                }
                                Switch(
                                    checked = queueWhenOnlineMode,
                                    onCheckedChange = { queueWhenOnlineMode = it },
                                    modifier = Modifier.testTag("civic_eye_offline_queue_switch")
                                )
                            }

                            if (queuedSyncReports.isNotEmpty()) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "⏳ ${queuedSyncReports.size} report(s) waiting in Room 'sync-when-online' queue",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.weight(1f)
                                    )
                                    Button(
                                        onClick = {
                                            BalasoreHapticHelper.vibrateSuccess(context)
                                            val count = onFlushSyncQueue?.invoke() ?: queuedSyncReports.size
                                            onRefreshFirestore()
                                            Toast.makeText(
                                                context,
                                                "Synced $count queued Room DB report(s) to cloud!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                        modifier = Modifier.testTag("civic_eye_sync_queue_now_btn")
                                    ) {
                                        Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Sync Queue Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }

                // 1. SELECT CATEGORY CHIPS
                item {
                    Text(
                        text = if (isOdia) "୧. ସମସ୍ୟା ପ୍ରକାର ବାଛନ୍ତୁ" else "1. Select Issue Category",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    val categories = listOf(
                        "Potholes" to "🕳️ Potholes",
                        "Lighting" to "💡 Lighting / Streetlight Outage",
                        "Water Supply" to "🚰 Water Supply / Pipeline Leak",
                        "Drain Clog / Waterlogging" to "🌊 Waterlogging / Drain",
                        "Garbage Heap / Waste" to "🗑️ Waste & Sanitation",
                        "Damaged Culvert / Road" to "🚧 Damaged Road"
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { (catKey, catLabel) ->
                            val isSelected = reportCategory == catKey
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    reportCategory = catKey
                                },
                                label = { Text(catLabel, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }

                // 2. QUICK SMART PROMPTS
                item {
                    Text(
                        text = if (isOdia) "ଦ୍ରୁତ ଉଦାହରଣ (Quick Fill)" else "Quick Suggestion Prompts",
                        style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val prompts = listOf(
                            "Deep pothole cluster on OT Road" to ("Road Pothole" to "Deep crater on OT Road causing two-wheelers to skid during monsoon."),
                            "Heavy waterlogging near Cinema Chhak" to ("Drain Clog / Waterlogging" to "Monsoon runoff overflowing drain onto storefronts and blocking road."),
                            "Uncollected garbage Nuabazar Mandi" to ("Garbage Heap / Waste" to "Solid waste spillover blocking pedestrian passage."),
                            "Streetlights dark near FM College" to ("Faulty Streetlight" to "Streetlights not turning on for last 3 nights.")
                        )
                        prompts.forEach { (label, details) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .clickable {
                                        BalasoreHapticHelper.vibrateButtonClick(context)
                                        issueTitle = label
                                        reportCategory = details.first
                                        issueDescription = details.second
                                    }
                            ) {
                                Text(
                                    text = "✍️ $label",
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }

                // 3. TITLE INPUT
                item {
                    OutlinedTextField(
                        value = issueTitle,
                        onValueChange = { issueTitle = it },
                        label = { Text(if (isOdia) "ସମସ୍ୟାର ଶୀର୍ଷକ" else "Issue Headline / Title *") },
                        placeholder = { Text("e.g. Deep crater pothole near Phandi Chhak") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("civic_eye_input_title"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                }

                // 4. WARD & LOCALITY PICKER
                item {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedWard,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(if (isOdia) "ୱାର୍ଡ୍ / ସ୍ଥାନ ଚୟନ" else "Ward / Locality in Balasore *") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                                .testTag("civic_eye_input_ward"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            wardList.forEach { ward ->
                                DropdownMenuItem(
                                    text = { Text(ward, fontSize = 13.sp) },
                                    onClick = {
                                        selectedWard = ward
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // 5. DESCRIPTION TEXT FIELD
                item {
                    OutlinedTextField(
                        value = issueDescription,
                        onValueChange = { issueDescription = it },
                        label = { Text(if (isOdia) "ସମ୍ପୂର୍ଣ୍ଣ ବିବରଣୀ" else "Detailed Description *") },
                        placeholder = { Text("Describe road dimensions, flooding depth, safety hazards for pedestrians or vehicles...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                            .testTag("civic_eye_input_desc"),
                        shape = RoundedCornerShape(12.dp),
                        maxLines = 5
                    )
                }

                // 6. URGENCY LEVEL
                item {
                    Text(
                        text = if (isOdia) "ଜରୁରୀ ସ୍ତର (Urgency Level)" else "Urgency Level",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val urgencies = listOf(
                            "LOW" to ("🟢 Low" to Color(0xFF16A34A)),
                            "MEDIUM" to ("🟡 Medium" to Color(0xFFD97706)),
                            "HIGH" to ("🟠 High" to Color(0xFFEA580C)),
                            "EMERGENCY" to ("🔴 Emergency" to Color(0xFFDC2626))
                        )
                        urgencies.forEach { (level, pair) ->
                            val isSelected = selectedUrgency == level
                            val (label, color) = pair
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) color.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) color else MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        BalasoreHapticHelper.vibrateButtonClick(context)
                                        selectedUrgency = level
                                    }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }

                // 7. GPS LOCATION TELEMETRY TILE
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "GPS Coordinates (Balasore)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Lat: 21.4934° N • Lng: 86.9135° E • Ward 14 Municipal Grid",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "ACQUIRED",
                                    color = Color(0xFF15803D),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }

                // 8. SINGLE-PHOTO CAMERA EVIDENCE CAPTURE (Pothole / Drainage Issue)
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "ଫଟୋ ପ୍ରମାଣ (୧ଟି ଫଟୋ ସଂଲଗ୍ନ କରନ୍ତୁ)" else "Photo Evidence (Attach 1 Photo)",
                                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = if (isOdia) "ଖାଲଖମା କିମ୍ବା ନାଳ ସମସ୍ୟାର ଫଟୋ କ୍ୟାମେରାରେ ଉଠାନ୍ତୁ" else "Capture a clear photo of the pothole or drainage issue before submitting",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (attachedPhotoUri != null) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "1 / 1 Photo",
                                    color = Color(0xFF15803D),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    if (!cameraPermissionDeniedMessage.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .testTag("civic_eye_camera_permission_banner")
                        ) {
                            Text(
                                text = cameraPermissionDeniedMessage ?: "",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    if (attachedPhotoBitmap != null || attachedPhotoUri != null) {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_eye_attached_photo_card")
                        ) {
                            Column {
                                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                                    if (attachedPhotoBitmap != null) {
                                        androidx.compose.foundation.Image(
                                            bitmap = attachedPhotoBitmap!!.asImageBitmap(),
                                            contentDescription = "Attached civic issue photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        AsyncImage(
                                            model = attachedPhotoUri,
                                            contentDescription = "Attached civic issue photo",
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    Surface(
                                        color = Color.Black.copy(alpha = 0.65f),
                                        shape = RoundedCornerShape(bottomStart = 8.dp),
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        IconButton(
                                            onClick = {
                                                attachedPhotoBitmap = null
                                                attachedPhotoUri = null
                                            },
                                            modifier = Modifier
                                                .size(48.dp)
                                                .testTag("civic_eye_remove_photo_btn")
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Remove Photo", tint = Color.White, modifier = Modifier.size(20.dp))
                                        }
                                    }

                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(topEnd = 8.dp),
                                        modifier = Modifier.align(Alignment.BottomStart)
                                    ) {
                                        Text(
                                            text = "📍 $selectedWard",
                                            color = Color.White,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = requestCameraAndCapture,
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("civic_eye_retake_photo_btn"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = "Retake photo", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isOdia) "ପୁନଃ ଫଟୋ ଉଠାନ୍ତୁ" else "Retake Photo", fontSize = 12.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            BalasoreHapticHelper.vibrateButtonClick(context)
                                            attachedPhotoBitmap = null
                                            attachedPhotoUri = null
                                        },
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 48.dp)
                                            .testTag("civic_eye_clear_photo_btn"),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Icon(Icons.Default.Delete, contentDescription = "Remove attached photo", modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(if (isOdia) "ଫଟୋ ହଟାନ୍ତୁ" else "Remove", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = requestCameraAndCapture,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("civic_eye_camera_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo with Camera", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOdia) "କ୍ୟାମେରା ଫଟୋ" else "Take Photo", fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("civic_eye_gallery_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Image, contentDescription = "Choose Photo from Gallery", modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOdia) "ଗ୍ୟାଲେରୀରୁ ବାଛନ୍ତୁ" else "Choose Photo", fontSize = 12.5.sp)
                            }
                        }
                    }
                }

                // 9. SUBMIT BUTTON (SUPPORTS DIRECT ROOM+CLOUD OR ROOM 'SYNC-WHEN-ONLINE' QUEUE)
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = {
                            if (issueTitle.isBlank()) {
                                Toast.makeText(context, if (isOdia) "ଦୟାକରି ଶୀର୍ଷକ ଲେଖନ୍ତୁ" else "Please enter a title for the issue", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            isSubmitting = true
                            val generatedId = "BLS-CIVIC-2026-${1000 + kotlin.random.Random.nextInt(9000)}"
                            val hasPhoto = attachedPhotoUri != null || attachedPhotoBitmap != null

                            if (queueWhenOnlineMode && onQueueReportOffline != null) {
                                onQueueReportOffline(
                                    issueTitle,
                                    reportCategory,
                                    selectedWard,
                                    issueDescription.ifBlank { "Reported via Civic Eye (Queued for Sync-When-Online)" },
                                    selectedUrgency,
                                    hasPhoto,
                                    attachedPhotoUri
                                )
                            } else {
                                onSubmitReport(
                                    issueTitle,
                                    reportCategory,
                                    selectedWard,
                                    issueDescription.ifBlank { "Reported via Civic Eye" },
                                    selectedUrgency,
                                    hasPhoto,
                                    attachedPhotoUri,
                                    21.4934,
                                    86.9135,
                                    selectedWard,
                                    "citizen_user",
                                    "Baleswari Citizen"
                                )
                            }

                            isSubmitting = false
                            submissionSuccessId = generatedId
                            issueTitle = ""
                            issueDescription = ""
                            attachedPhotoBitmap = null
                            attachedPhotoUri = null
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("civic_eye_submit_btn"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (queueWhenOnlineMode) Color(0xFFD97706) else Color(0xFF0284C7)
                        ),
                        enabled = !isSubmitting
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (queueWhenOnlineMode) Icons.Default.CloudQueue else Icons.Default.Send,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (queueWhenOnlineMode) {
                                    if (isOdia) "Room DB 'Sync-When-Online' କ୍ୟୁରେ ସେଭ୍ କରନ୍ତୁ" else "Save to Room DB (Sync-When-Online Queue)"
                                } else {
                                    if (isOdia) "Room DB ଓ କ୍ଲାଉଡ୍ କୁ ଦାଖଲ କରନ୍ତୁ" else "Submit Report (Room DB & Cloud Sync)"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        } else if (selectedTabIndex == 1) {
            // LIVE FEED TAB
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                // SEARCH & CLOUD SYNC BAR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search potholes, drainage, wards...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                }
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("civic_eye_feed_search"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    // Quick Switch to Map Pin View
                    Surface(
                        color = Color(0xFF0284C7).copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .height(50.dp)
                            .clickable {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedTabIndex = 2
                            }
                            .testTag("civic_eye_switch_to_map_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Place, contentDescription = "Map Pins", tint = Color(0xFF0284C7), modifier = Modifier.size(18.dp))
                            Text(if (isOdia) "ମାନଚିତ୍ର" else "Map", color = Color(0xFF0284C7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Filter Bottom Sheet button
                    val hasActiveFeedFilters = feedCategoryFilter != "ALL" || feedStatusFilter != "ALL" || feedUrgencyFilter != "ALL"
                    Surface(
                        color = if (hasActiveFeedFilters) Color(0xFF0284C7) else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, if (hasActiveFeedFilters) Color(0xFF0284C7) else MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .size(50.dp)
                            .clickable {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                showFeedFilterSheet = !showFeedFilterSheet
                            }
                            .testTag("civic_eye_feed_filter_sheet_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = "Filters",
                                tint = if (hasActiveFeedFilters) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            onRefreshFirestore()
                        },
                        modifier = Modifier
                            .size(50.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("civic_eye_refresh_feed_btn")
                    ) {
                        if (isFirestoreLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.Sync, contentDescription = "Sync", modifier = Modifier.size(20.dp))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ROOM DB SYNC-WHEN-ONLINE QUEUE BAR IN LIST VIEW
                if (queuedSyncReports.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("civic_feed_sync_queue_banner")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudQueue,
                                    contentDescription = "Sync Queue",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOdia) {
                                        "${queuedSyncReports.size} ରିପୋର୍ଟ Room DB 'Sync-When-Online' କ୍ୟୁରେ ଅଛି"
                                    } else {
                                        "${queuedSyncReports.size} report(s) in Room DB 'Sync-When-Online' queue"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                            }
                            TextButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateSuccess(context)
                                    val count = onFlushSyncQueue?.invoke() ?: queuedSyncReports.size
                                    onRefreshFirestore()
                                    Toast.makeText(
                                        context,
                                        "Synced $count queued report(s) from Room DB!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                modifier = Modifier.testTag("civic_feed_sync_now_btn")
                            ) {
                                Text("Sync Now →", fontSize = 11.5.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF0284C7))
                            }
                        }
                    }
                }

                // CATEGORY FILTER CHIPS FOR LIST VIEW ('Potholes', 'Lighting', 'Water Supply', 'Waterlogging', 'Waste')
                val potholeCount = remember(allCombinedReports) { allCombinedReports.count { it.matchesCategoryFilter("POTHOLE") } }
                val lightingCount = remember(allCombinedReports) { allCombinedReports.count { it.matchesCategoryFilter("LIGHTING") } }
                val waterSupplyCount = remember(allCombinedReports) { allCombinedReports.count { it.matchesCategoryFilter("WATER_SUPPLY") } }
                val waterloggingCount = remember(allCombinedReports) { allCombinedReports.count { it.matchesCategoryFilter("WATERLOGGING") } }
                val wasteCount = remember(allCombinedReports) { allCombinedReports.count { it.matchesCategoryFilter("WASTE") } }

                val feedCategories = listOf(
                    "ALL" to (if (isOdia) "ସବୁ (${allCombinedReports.size})" else "All (${allCombinedReports.size})"),
                    "POTHOLE" to (if (isOdia) "🕳️ ଖାଲ-ଖମା ($potholeCount)" else "🕳️ Potholes ($potholeCount)"),
                    "LIGHTING" to (if (isOdia) "💡 ଷ୍ଟ୍ରିଟ୍ ଲାଇଟ୍ ($lightingCount)" else "💡 Lighting ($lightingCount)"),
                    "WATER_SUPPLY" to (if (isOdia) "🚰 ଜଳ ଯୋଗାଣ ($waterSupplyCount)" else "🚰 Water Supply ($waterSupplyCount)"),
                    "WATERLOGGING" to (if (isOdia) "💧 ଜଳବନ୍ଦୀ ($waterloggingCount)" else "💧 Waterlogging ($waterloggingCount)"),
                    "WASTE" to (if (isOdia) "🗑️ ଆବର୍ଜନା ($wasteCount)" else "🗑️ Waste ($wasteCount)"),
                    "ELECTRICITY" to (if (isOdia) "⚡ ବିଦ୍ୟୁତ୍" else "⚡ Electricity"),
                    "ROAD" to (if (isOdia) "🚧 ରାସ୍ତା" else "🚧 Damaged Road")
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    feedCategories.forEach { (catKey, label) ->
                        val isSelected = feedCategoryFilter == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                feedCategoryFilter = catKey
                            },
                            label = {
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            modifier = Modifier.testTag("civic_feed_cat_${catKey.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // STATUS & SYNC QUEUE FILTER CHIPS
                val statusFilters = listOf("ALL", "SYNC_QUEUE", "PENDING", "IN_PROGRESS", "RESOLVED")
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusFilters.forEach { status ->
                        val isSelected = feedStatusFilter == status
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                feedStatusFilter = status
                            },
                            label = {
                                Text(
                                    text = when (status) {
                                        "SYNC_QUEUE" -> "⏳ Sync Queue (${queuedSyncReports.size})"
                                        "PENDING" -> "Submitted"
                                        "IN_PROGRESS" -> "In Progress"
                                        "RESOLVED" -> "Resolved"
                                        else -> "All Status"
                                    },
                                    fontSize = 11.sp
                                )
                            },
                            modifier = Modifier.testTag("civic_feed_status_${status.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // FILTERED LIST
                val combinedReports = remember(allCombinedReports, searchQuery, feedStatusFilter, feedCategoryFilter, feedUrgencyFilter) {
                    allCombinedReports.filter { report ->
                        val matchesSearch = searchQuery.isBlank() ||
                                report.title.contains(searchQuery, ignoreCase = true) ||
                                report.wardLocation.contains(searchQuery, ignoreCase = true) ||
                                report.category.contains(searchQuery, ignoreCase = true)

                        val matchesStatus = when (feedStatusFilter) {
                            "ALL" -> true
                            "SYNC_QUEUE" -> report.isQueuedForSync
                            "PENDING" -> report.status.equals("PENDING", ignoreCase = true) ||
                                report.status.equals("SUBMITTED", ignoreCase = true) ||
                                report.status.equals("DRAFT", ignoreCase = true)
                            else -> report.status.equals(feedStatusFilter, ignoreCase = true)
                        }

                        val matchesCategory = report.matchesCategoryFilter(feedCategoryFilter)

                        val matchesUrgency = when (feedUrgencyFilter) {
                            "ALL" -> true
                            else -> report.urgency.equals(feedUrgencyFilter, ignoreCase = true)
                        }

                        matchesSearch && matchesStatus && matchesCategory && matchesUrgency
                    }
                }

                if (combinedReports.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        contentAlignment = Alignment.TopCenter
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(text = "🔍", fontSize = 42.sp)
                            Text(
                                text = if (isOdia) "କୌଣସି ରିପୋର୍ଟ ମିଳିଲା ନାହିଁ" else "No matching reports found",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Try clearing search filters or submit a new report.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Button(
                                onClick = { selectedTabIndex = 0 },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Submit New Report")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 24.dp)
                    ) {
                        items(combinedReports, key = { it.id }) { report ->
                            CivicReportCardItem(
                                report = report,
                                onAdvanceStatus = { onAdvanceStatus(report.id) },
                                onDelete = { onDeleteReport(report.id) },
                                onUpvote = { onUpvoteReport(report.id) }
                            )
                        }
                    }
                }
            }
        } else {
            // TAB 2: INTERACTIVE MAP PIN VIEW
            CivicEyeMapPinView(
                reports = allCombinedReports,
                language = language,
                onUpvoteReport = onUpvoteReport,
                onAdvanceStatus = onAdvanceStatus,
                modifier = Modifier.fillMaxSize()
            )
        }
    }

        // Live Feed Filter Bottom Sheet
        val feedMatchingCount = remember(allCombinedReports, feedCategoryFilter, feedStatusFilter, feedUrgencyFilter) {
            allCombinedReports.count { report ->
                val matchesCategory = report.matchesCategoryFilter(feedCategoryFilter)
                val matchesStatus = when (feedStatusFilter) {
                    "ALL" -> true
                    "SYNC_QUEUE" -> report.isQueuedForSync
                    "PENDING" -> report.status.equals("PENDING", ignoreCase = true) ||
                        report.status.equals("SUBMITTED", ignoreCase = true) ||
                        report.status.equals("DRAFT", ignoreCase = true)
                    else -> report.status.equals(feedStatusFilter, ignoreCase = true)
                }
                val matchesUrgency = when (feedUrgencyFilter) {
                    "ALL" -> true
                    else -> report.urgency.equals(feedUrgencyFilter, ignoreCase = true)
                }
                matchesCategory && matchesStatus && matchesUrgency
            }
        }

        CivicFilterBottomSheet(
            visible = showFeedFilterSheet,
            onDismiss = { showFeedFilterSheet = false },
            selectedCategory = feedCategoryFilter,
            onSelectCategory = { feedCategoryFilter = it },
            selectedStatus = feedStatusFilter,
            onSelectStatus = { feedStatusFilter = it },
            selectedUrgency = feedUrgencyFilter,
            onSelectUrgency = { feedUrgencyFilter = it },
            matchingCount = feedMatchingCount,
            onResetFilters = {
                feedCategoryFilter = "ALL"
                feedStatusFilter = "ALL"
                feedUrgencyFilter = "ALL"
            },
            language = language,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
private fun CivicReportCardItem(
    report: CivicReportEntity,
    onAdvanceStatus: () -> Unit,
    onDelete: () -> Unit,
    onUpvote: () -> Unit
) {
    val context = LocalContext.current
    var upvoteCount by remember(report.id) { mutableIntStateOf(report.upvotesCount) }

    val statusColor = when (report.status.uppercase()) {
        "RESOLVED" -> Color(0xFF16A34A) // Green
        "IN_PROGRESS" -> Color(0xFF0284C7) // Blue
        else -> Color(0xFFD97706) // Amber/Pending
    }

    val urgencyColor = when (report.urgency.uppercase()) {
        "CRITICAL", "EMERGENCY" -> Color(0xFFDC2626)
        "HIGH" -> Color(0xFFEA580C)
        "MEDIUM" -> Color(0xFFD97706)
        else -> Color(0xFF16A34A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("civic_report_card_${report.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // TOP ROW: ID, Category & Urgency Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = report.id,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Surface(
                        color = urgencyColor.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = report.urgency,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = urgencyColor,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    // ROOM DB SYNC QUEUE STATUS BADGE
                    Surface(
                        color = if (report.isQueuedForSync) Color(0xFFFEF3C7) else Color(0xFFE0F2FE),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = if (report.isQueuedForSync) "⏳ QUEUED FOR SYNC" else "✓ ROOM DB SYNCED",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (report.isQueuedForSync) Color(0xFFB45309) else Color(0xFF0369A1),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }

                // STATUS PILL
                Surface(
                    color = statusColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(statusColor)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (report.status.uppercase()) {
                                "RESOLVED" -> "RESOLVED"
                                "IN_PROGRESS" -> "IN PROGRESS"
                                else -> "SUBMITTED"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // TITLE
            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            // DESCRIPTION
            Text(
                text = report.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(8.dp))

            // WARD & LOCATION
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = report.wardLocation,
                    fontSize = 11.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
            }

            // PHOTO THUMBNAIL IF PRESENT
            if (report.hasPhotoAttached && !report.photoUri.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                ) {
                    AsyncImage(
                        model = report.photoUri,
                        contentDescription = "Photo Evidence",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            // RESOLUTION NOTES IF IN_PROGRESS OR RESOLVED
            if (!report.resolutionNotes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = report.resolutionNotes,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // BOTTOM ACTIONS: Upvote, Share, Advance Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // UPVOTE / ENDORSE
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier.clickable {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            upvoteCount++
                            onUpvote()
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ThumbUp, contentDescription = "Upvote", tint = Color(0xFF0284C7), modifier = Modifier.size(13.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Upvote ($upvoteCount)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0284C7)
                            )
                        }
                    }

                    // SHARE
                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Civic Eye Alert: ${report.title} at ${report.wardLocation}. Status: ${report.status}. Track on Baleshwar 360 app.")
                            }
                            val chooser = Intent.createChooser(sendIntent, "Share Civic Eye Alert").apply {
                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            }
                            context.startActivity(chooser)
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                    }
                }

                // ADVANCE STATUS (Municipal Progression Simulation)
                TextButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        onAdvanceStatus()
                    },
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (report.status == "RESOLVED") "Marked Resolved ✓" else "Advance Status →",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (report.status == "RESOLVED") Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
