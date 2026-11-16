package com.example.ui.features.journal

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.TravelJournalEntity
import com.example.data.repository.TravelJournalRepository
import com.example.ui.theme.OceanBlue
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Pre-defined Balasore local tourism spots with accurate geographical coordinates
 * for instant offline auto-completion.
 */
data class TourismSpotCoordinate(
    val id: String,
    val nameEn: String,
    val nameOr: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val colorTag: Long
)

val BALASORE_TOURISM_SPOTS = listOf(
    TourismSpotCoordinate("spot_chandipur", "Chandipur Beach", "ଚାନ୍ଦିପୁର ବେଳାଭୂମି", "Beach", 21.4700, 87.0135, 0xFF0284C7),
    TourismSpotCoordinate("spot_remuna", "Khirachora Gopinath Temple", "କ୍ଷୀରଚୋରା ଗୋପୀନାଥ, ରେମୁଣା", "Temple", 21.5284, 86.8722, 0xFFD97706),
    TourismSpotCoordinate("spot_panchalingeswar", "Panchalingeswar Shrine", "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପୀଠ", "Nature & Spiritual", 21.4333, 86.7167, 0xFF059669),
    TourismSpotCoordinate("spot_talasari", "Talasari Beach", "ତାଳସାରୀ ବେଳାଭୂମି", "Beach & Estuary", 21.5972, 87.4589, 0xFF0284C7),
    TourismSpotCoordinate("spot_kasafal", "Kasafal Beach & Harbor", "କସାଫଳ ବେଳାଭୂମି", "Beach & Harbor", 21.4878, 87.0867, 0xFF0284C7),
    TourismSpotCoordinate("spot_kuldiha", "Kuldiha Wildlife Sanctuary", "କୁଲଡିହା ଅଭୟାରଣ୍ୟ", "Wildlife", 21.4167, 86.6333, 0xFF059669),
    TourismSpotCoordinate("spot_emami", "Emami Jagannath Temple", "ଇମାମି ଜଗନ୍ନାଥ ମନ୍ଦିର", "Temple", 21.5120, 86.8850, 0xFFD97706),
    TourismSpotCoordinate("spot_nilagiri", "Nilagiri Rajbati & Fort", "ନୀଳଗିରି ରାଜବାଟୀ", "Heritage", 21.4583, 86.7667, 0xFF7C3AED),
    TourismSpotCoordinate("spot_balaramgadi", "Balaramgadi Fishing Harbor", "ବଳରାମଗଡ଼ି ମୁହାଣ", "Harbor", 21.4650, 87.0450, 0xFF0284C7),
    TourismSpotCoordinate("spot_raibania", "Raibania Medieval Fort", "ରାଇବଣିଆ ଦୁର୍ଗ", "Heritage", 21.9210, 87.1850, 0xFF7C3AED),
    TourismSpotCoordinate("spot_bichitrapur", "Bichitrapur Mangrove Reserve", "ବିଚିତ୍ରପୁର ହେନ୍ତାଳବନ", "Eco-Tourism", 21.6120, 87.4850, 0xFF059669),
    TourismSpotCoordinate("spot_bhusandeswar", "Bhusandeswar Shiva Temple", "ଭୂଷଣ୍ଡେଶ୍ୱର ମନ୍ଦିର", "Temple", 21.6500, 87.2800, 0xFFD97706)
)

/**
 * Offline Travel Journal feature built on Android Room Database.
 * Allows users to record notes, capture or select photos (via camera or photo picker),
 * and record precise coordinates of local tourism spots for complete offline access.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelJournalSheet(
    journalRepo: TravelJournalRepository?,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf("All") }

    val rawJournals by (journalRepo?.allJournals?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList<TravelJournalEntity>()) })

    val filteredJournals = remember(rawJournals, searchQuery, selectedCategoryFilter) {
        rawJournals.filter { journal ->
            val matchesCategory = selectedCategoryFilter == "All" ||
                    journal.category.contains(selectedCategoryFilter, ignoreCase = true)
            val matchesQuery = searchQuery.isBlank() ||
                    journal.title.contains(searchQuery, ignoreCase = true) ||
                    journal.locationName.contains(searchQuery, ignoreCase = true) ||
                    journal.notes.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    var showAddDialog by remember { mutableStateOf(false) }

    // Dialog state
    var newTitle by remember { mutableStateOf("") }
    var newLocation by remember { mutableStateOf(BALASORE_TOURISM_SPOTS.first().nameEn) }
    var newCategory by remember { mutableStateOf(BALASORE_TOURISM_SPOTS.first().category) }
    var newRating by remember { mutableIntStateOf(5) }
    var newNotes by remember { mutableStateOf("") }
    var newLatitude by remember { mutableDoubleStateOf(BALASORE_TOURISM_SPOTS.first().latitude) }
    var newLongitude by remember { mutableDoubleStateOf(BALASORE_TOURISM_SPOTS.first().longitude) }
    var newSpotId by remember { mutableStateOf<String?>(BALASORE_TOURISM_SPOTS.first().id) }
    var newPhotoPath by remember { mutableStateOf<String?>(null) }
    var showSpotDropdown by remember { mutableStateOf(false) }

    // Utilities to save photo to app's internal files directory for 100% offline persistence
    fun saveBitmapLocally(bitmap: Bitmap): String {
        val dir = File(context.filesDir, "travel_journal_photos").apply { mkdirs() }
        val file = File(dir, "journal_cam_${System.currentTimeMillis()}.jpg")
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        return file.absolutePath
    }

    fun saveUriLocally(sourceUri: Uri): String? {
        return try {
            val dir = File(context.filesDir, "travel_journal_photos").apply { mkdirs() }
            val file = File(dir, "journal_pick_${System.currentTimeMillis()}.jpg")
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (_: Exception) {
            null
        }
    }

    // Camera Launcher via TakePicturePreview
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val path = saveBitmapLocally(bitmap)
            newPhotoPath = path
            Toast.makeText(context, "Photo captured and stored offline in Room", Toast.LENGTH_SHORT).show()
        }
    }

    // Camera Permission Launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Camera permission needed to take journal photo", Toast.LENGTH_SHORT).show()
        }
    }

    // Photo Picker Launcher (zero-permission media picker)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = saveUriLocally(uri)
            newPhotoPath = path
            Toast.makeText(context, "Photo saved offline for travel entry", Toast.LENGTH_SHORT).show()
        }
    }

    // GPS Location auto-detection
    fun detectCurrentGps() {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val loc = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            if (loc != null) {
                newLatitude = loc.latitude
                newLongitude = loc.longitude
                Toast.makeText(context, "GPS coordinates captured: ${loc.latitude.take(7)}, ${loc.longitude.take(7)}", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Defaulted to current Balasore coordinates", Toast.LENGTH_SHORT).show()
            }
        } catch (_: Exception) {
            Toast.makeText(context, "Using Balasore coordinates", Toast.LENGTH_SHORT).show()
        }
    }

    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("travel_journal_sheet")
    ) {
        // 1. Header Row
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
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF0284C7), Color(0xFF0369A1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Book,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Travel Journal & Moments",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF10B981))
                        ) {
                            Text(
                                text = "ROOM DB OFFLINE",
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = "ଅଫଲାଇନ୍ ଭ୍ରମଣ ଡାଏରୀ, ଫଟୋ ଓ ସ୍ଥାନୀୟ କୋର୍ଡିନେଟ୍ସ",
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp
                        )
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFE0F2FE)
            ) {
                Text(
                    text = "${filteredJournals.size} Memories",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0369A1)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Action Toolbar: Add Memory with Camera & Share
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    newTitle = ""
                    newNotes = ""
                    newPhotoPath = null
                    val defaultSpot = BALASORE_TOURISM_SPOTS.first()
                    newLocation = defaultSpot.nameEn
                    newCategory = defaultSpot.category
                    newLatitude = defaultSpot.latitude
                    newLongitude = defaultSpot.longitude
                    newSpotId = defaultSpot.id
                    showAddDialog = true
                },
                modifier = Modifier
                    .weight(1.2f)
                    .height(40.dp)
                    .testTag("add_journal_memory_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AddAPhoto, null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Memory (Camera)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    if (rawJournals.isNotEmpty()) {
                        val shareText = buildString {
                            append("🗺️ Baleshwar 360 - Offline Travel Journal\n\n")
                            rawJournals.forEach { j ->
                                append("📍 ${j.title} (${j.locationName})\n")
                                append("🌐 Coordinates: ${j.latitude}° N, ${j.longitude}° E\n")
                                append("⭐ Rating: ${"★".repeat(j.rating)}\n")
                                append("📝 Notes: ${j.notes}\n")
                                if (!j.photoUri.isNullOrBlank()) {
                                    append("📸 Photo Stored Locally: Available Offline\n")
                                }
                                append("\n")
                            }
                            append("Saved offline in Room Database via Baleshwar 360 App")
                        }
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "My Balasore Travel Journal")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        context.startActivity(Intent.createChooser(intent, "Share Travel Scrapbook"))
                    } else {
                        Toast.makeText(context, "No memories recorded yet", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .weight(0.9f)
                    .height(40.dp)
                    .testTag("share_travel_journal_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Share, null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Share Trip", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Search & Category Filters
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by spot, title, or notes...", fontSize = 12.5.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(18.dp))
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .testTag("journal_search_input"),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            val categories = listOf("All", "Beach", "Temple", "Wildlife", "Heritage", "Nature", "Harbor")
            items(categories) { cat ->
                val isSelected = selectedCategoryFilter == cat
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategoryFilter = cat },
                    label = { Text(cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(10.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OceanBlue.copy(alpha = 0.15f),
                        selectedLabelColor = OceanBlue
                    ),
                    border = BorderStroke(1.dp, if (isSelected) OceanBlue else Color(0xFFE2E8F0)),
                    modifier = Modifier.height(30.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 4. Journal Entries List (from Room Database)
        if (filteredJournals.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = Color(0xFFCBD5E1),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No journal entries match your filter" else "No travel moments recorded yet",
                        style = MaterialTheme.typography.bodyMedium.copy(color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                    )
                    Text(
                        text = "Take a photo and save coordinates of local Balasore tourism spots!",
                        style = MaterialTheme.typography.bodySmall.copy(color = Color(0xFF94A3B8), fontSize = 11.5.sp)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f, fill = false)
                    .testTag("travel_journal_entries_list")
            ) {
                items(filteredJournals, key = { it.id }) { journal ->
                    JournalEntryCard(
                        journal = journal,
                        dateFormat = dateFormat,
                        onDelete = {
                            scope.launch {
                                journalRepo?.deleteJournalById(journal.id)
                                Toast.makeText(context, "Memory deleted from Room DB", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onOpenMap = {
                            val uri = Uri.parse("geo:${journal.latitude},${journal.longitude}?q=${journal.latitude},${journal.longitude}(${Uri.encode(journal.locationName)})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                            try {
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                                Toast.makeText(context, "Coordinates: ${journal.latitude}° N, ${journal.longitude}° E", Toast.LENGTH_LONG).show()
                            }
                        },
                        onShare = {
                            val shareText = "📍 ${journal.title} at ${journal.locationName}\n" +
                                    "🌐 Coordinates: ${journal.latitude}° N, ${journal.longitude}° E\n" +
                                    "⭐ Rating: ${"★".repeat(journal.rating)}\n" +
                                    "📝 Notes: ${journal.notes}\n" +
                                    "- Captured in Baleshwar 360 Travel Journal"
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Memory"))
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Button(
            onClick = onClose,
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp)
                .testTag("done_travel_journal_btn"),
            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Done with Travel Journal", fontWeight = FontWeight.Bold)
        }
    }

    // =========================================================================
    // 5. Add Memory Dialog with Camera, Spot Selector, Coordinates & Notes
    // =========================================================================
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, tint = OceanBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Travel Journal Entry", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Memory Title
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Memory Title") },
                        placeholder = { Text("e.g. Walking on the Dry Seabed") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_input_title"),
                        singleLine = true
                    )

                    // Spot Selector: Quick Local Tourism Spot Selector
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select Tourism Spot & Coordinates:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF334155)
                            )
                            TextButton(
                                onClick = { detectCurrentGps() },
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(13.dp), tint = OceanBlue)
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("Current GPS", fontSize = 11.sp, color = OceanBlue, fontWeight = FontWeight.Bold)
                            }
                        }

                        // Tourism Spot Horizontal Chips
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(BALASORE_TOURISM_SPOTS) { spot ->
                                val isSelected = newSpotId == spot.id
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) OceanBlue else Color(0xFFF1F5F9),
                                    border = BorderStroke(1.dp, if (isSelected) OceanBlue else Color(0xFFCBD5E1)),
                                    modifier = Modifier
                                        .clickable {
                                            newSpotId = spot.id
                                            newLocation = spot.nameEn
                                            newCategory = spot.category
                                            newLatitude = spot.latitude
                                            newLongitude = spot.longitude
                                        }
                                        .testTag("spot_chip_${spot.id}")
                                ) {
                                    Text(
                                        text = spot.nameEn,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) Color.White else Color(0xFF334155),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Selected Coordinates Pill (saved in Room for offline access)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PinDrop, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "${newLocation} • ${newCategory}",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF15803D)
                                    )
                                    Text(
                                        text = "📍 Lat: ${newLatitude.take(7)}° N, Lng: ${newLongitude.take(7)}° E (Offline)",
                                        fontSize = 10.sp,
                                        color = Color(0xFF16A34A)
                                    )
                                }
                            }
                        }
                    }

                    // Photo via Camera / Gallery
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📸 Photo (Camera & Offline Storage):",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF334155)
                                )

                                if (newPhotoPath != null) {
                                    TextButton(
                                        onClick = { newPhotoPath = null },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Text("Remove", fontSize = 11.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            if (newPhotoPath != null) {
                                // Photo Preview Thumbnail
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color(0xFFE2E8F0))
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(File(newPhotoPath!!))
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Journal Photo",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(bottomStart = 8.dp),
                                        color = Color.Black.copy(alpha = 0.6f),
                                        modifier = Modifier.align(Alignment.TopEnd)
                                    ) {
                                        Text(
                                            text = "SAVED OFFLINE",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            } else {
                                // Buttons: Capture with Camera & Pick Photo
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .testTag("launch_camera_photo_btn")
                                    ) {
                                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Take Photo", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            photoPickerLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(36.dp)
                                            .testTag("pick_gallery_photo_btn")
                                    ) {
                                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Pick Image", fontSize = 11.sp)
                                    }
                                }
                            }
                        }
                    }

                    // Personal Impressions & Notes
                    OutlinedTextField(
                        value = newNotes,
                        onValueChange = { newNotes = it },
                        label = { Text("Personal Impressions & Notes") },
                        placeholder = { Text("Describe what you saw, felt, or tasted at this spot...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("journal_input_notes"),
                        minLines = 3,
                        maxLines = 5
                    )

                    // Rating Stars
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Rating (1-5):", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Row {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = { newRating = star },
                                    modifier = Modifier.size(30.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = if (star <= newRating) Color(0xFFF59E0B) else Color(0xFFCBD5E1),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            scope.launch {
                                journalRepo?.addJournal(
                                    title = newTitle.trim(),
                                    locationName = newLocation.trim(),
                                    category = newCategory,
                                    rating = newRating,
                                    notes = newNotes.trim(),
                                    photoUri = newPhotoPath,
                                    latitude = newLatitude,
                                    longitude = newLongitude,
                                    spotId = newSpotId,
                                    colorTag = 0xFF0284C7
                                )
                                Toast.makeText(context, "Memory saved to offline Room Database", Toast.LENGTH_SHORT).show()
                            }
                            newTitle = ""
                            newNotes = ""
                            newPhotoPath = null
                            showAddDialog = false
                        } else {
                            Toast.makeText(context, "Please enter a title for this memory", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    modifier = Modifier.testTag("save_journal_entry_btn")
                ) {
                    Text("Save to Room DB")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Visual card displaying a saved travel journal entry with photo, coordinates, and notes.
 */
@Composable
private fun JournalEntryCard(
    journal: TravelJournalEntity,
    dateFormat: SimpleDateFormat,
    onDelete: () -> Unit,
    onOpenMap: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("journal_card_${journal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Photo if available
            if (!journal.photoUri.isNullOrBlank()) {
                val imageFile = File(journal.photoUri)
                if (imageFile.exists()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imageFile)
                                .crossfade(true)
                                .build(),
                            contentDescription = journal.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            shape = RoundedCornerShape(bottomStart = 8.dp),
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = "OFFLINE PHOTO",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }

            // Title & Actions Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = journal.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(journal.colorTag).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = journal.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(journal.colorTag),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = journal.locationName,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "★".repeat(journal.rating),
                        color = Color(0xFFF59E0B),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onShare,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF64748B), modifier = Modifier.size(15.dp))
                    }

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(15.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Coordinates Badge & Map Navigation Button
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFFF1F5F9),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (journal.latitude != 0.0)
                                "Lat: ${journal.latitude.take(7)}° N, Lng: ${journal.longitude.take(7)}° E"
                            else
                                "Coordinates Saved Offline",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF334155)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = OceanBlue.copy(alpha = 0.12f),
                        modifier = Modifier.clickable { onOpenMap() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Map", fontSize = 10.sp, color = OceanBlue, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(2.dp))
                            Icon(Icons.Default.Navigation, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(10.dp))
                        }
                    }
                }
            }

            if (journal.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = journal.notes,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        lineHeight = 17.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateFormat.format(Date(journal.visitedDate)),
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                )

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.12f)
                ) {
                    Text(
                        text = "ROOM DB CACHED",
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857),
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }
    }
}

// Helper extension to format double coordinates safely
private fun Double.take(count: Int): String {
    val s = this.toString()
    return if (s.length > count) s.take(count) else s
}
