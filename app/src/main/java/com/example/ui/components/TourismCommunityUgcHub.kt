package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.FirestoreService
import com.example.data.local.AppDatabase
import com.example.data.local.ReviewEntity
import com.example.data.model.AppLanguage
import com.example.data.model.TourismReviewCloudEntity
import com.example.data.recommendation.BalasoreRecommendationEngine
import com.example.data.repository.BalasoreRepository
import com.example.ui.features.admin.ContentModerationRepository
import com.example.ui.features.admin.ModerationStatus
import com.example.ui.theme.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class TourismUgcSubmissionItem(
    val id: String = "ugc_${UUID.randomUUID().toString().take(8)}",
    val spotId: String,
    val spotName: String,
    val userId: String,
    val authorName: String,
    val rating: Int,
    val reviewText: String,
    val travelTip: String,
    val tipCategory: String, // "Best Time to Visit", "Transport & Parking", "Food & Prasad", "Safety & Tide Tip", "Hidden Photo Spot"
    val photoUris: List<String> = emptyList(),
    val photoCaption: String = "",
    val visitSeason: String = "Winter",
    val moderationStatus: ModerationStatus = ModerationStatus.PENDING,
    val moderationNote: String = "Awaiting moderator verification",
    val helpfulCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Manages User-Generated Content (UGC) for the Balasore Tourism section:
 * - Traveler Photo Uploads, Attraction Reviews, and Insider Tips
 * - Automated pre-screening + Editorial Moderation Queue (PENDING -> APPROVED / FLAGGED / REJECTED)
 */
object TourismUgcModerationRepository {

    val TIP_CATEGORIES = listOf(
        "Best Time to Visit",
        "Transport & Parking",
        "Food & Prasad",
        "Safety & Tide Tip",
        "Hidden Photo Spot"
    )

    val SAMPLE_PHOTO_PRESETS = listOf(
        "content://balasore360/tourism_photos/chandipur_low_tide_sunset.jpg" to "🌅 Chandipur Low-Tide Sunset",
        "content://balasore360/tourism_photos/panchalingeswar_hill_stream.jpg" to "🏞️ Panchalingeswar Perennial Stream",
        "content://balasore360/tourism_photos/khirachora_gopinath_prasad.jpg" to "🛕 Remuna Khirachora Gopinath",
        "content://balasore360/tourism_photos/kuldiha_canopy_trail.jpg" to "🦌 Kuldiha Forest Canopy Trail",
        "content://balasore360/tourism_photos/talasari_red_crab_coast.jpg" to "🦀 Talasari Red Crab Shoreline"
    )

    private val UNSAFE_KEYWORDS = listOf(
        "scam", "unregulated atv", "spam", "click unknown", "fake", "illegal"
    )

    private val _submissions = MutableStateFlow(defaultSeededTourismUgc())
    val submissions: StateFlow<List<TourismUgcSubmissionItem>> = _submissions.asStateFlow()

    private fun defaultSeededTourismUgc(): List<TourismUgcSubmissionItem> {
        val now = System.currentTimeMillis()
        return listOf(
            TourismUgcSubmissionItem(
                id = "ugc_bls_01",
                spotId = "1",
                spotName = "Chandipur-on-Sea",
                userId = "usr_explorer_11",
                authorName = "Ananya Mohapatra",
                rating = 5,
                reviewText = "Walking 3 km into the receding Bay of Bengal seabed at dawn is magical. You can spot red ghost crabs and horseshoe crabs along the ripples!",
                travelTip = "Check the low-tide chart before stepping onto the seabed and return 90 minutes before high-tide reversal.",
                tipCategory = "Safety & Tide Tip",
                photoUris = listOf("content://balasore360/tourism_photos/chandipur_low_tide_sunset.jpg"),
                photoCaption = "Golden hour reflection on the receding seabed at Chandipur",
                visitSeason = "Winter",
                moderationStatus = ModerationStatus.APPROVED,
                moderationNote = "Verified coastal safety tip & authentic visitor photo.",
                helpfulCount = 28,
                createdAt = now - 3600_000L * 8
            ),
            TourismUgcSubmissionItem(
                id = "ugc_bls_02",
                spotId = "3",
                spotName = "Khirachora Gopinatha Temple",
                userId = "usr_devotee_22",
                authorName = "Subrat Kumar Das",
                rating = 5,
                reviewText = "Historical Vaishnavite shrine in Remuna with peaceful courtyard and divine darshan.",
                travelTip = "Pre-book the famous Amruta Keli Khira Bhoga counter ticket before 11:00 AM to avoid festival queues.",
                tipCategory = "Food & Prasad",
                photoUris = listOf("content://balasore360/tourism_photos/khirachora_gopinath_prasad.jpg"),
                photoCaption = "Morning darshan queue and traditional Remuna Khira Prasad complex",
                visitSeason = "Festive Season",
                moderationStatus = ModerationStatus.APPROVED,
                moderationNote = "Verified temple visitor tip.",
                helpfulCount = 19,
                createdAt = now - 3600_000L * 14
            ),
            TourismUgcSubmissionItem(
                id = "ugc_bls_03",
                spotId = "2",
                spotName = "Panchalingeswar Shrine",
                userId = "usr_trekker_07",
                authorName = "Debasis Panda",
                rating = 5,
                reviewText = "Lush green Nilagiri hills and crystal-clear stream flowing over the five Shiva Lingams.",
                travelTip = "Arrive between 6:00 AM and 8:30 AM for peaceful darshan and carry non-slip sandals for the wet stone steps.",
                tipCategory = "Best Time to Visit",
                photoUris = listOf("content://balasore360/tourism_photos/panchalingeswar_hill_stream.jpg"),
                photoCaption = "Perennial hill stream over the shrine steps",
                visitSeason = "Winter",
                moderationStatus = ModerationStatus.PENDING,
                moderationNote = "Awaiting moderator verification of uploaded photo & tip.",
                helpfulCount = 6,
                createdAt = now - 3600_000L * 2
            ),
            TourismUgcSubmissionItem(
                id = "ugc_bls_04",
                spotId = "4",
                spotName = "Kuldiha Wildlife Sanctuary",
                userId = "usr_wildlife_04",
                authorName = "Priyanka Rout",
                rating = 4,
                reviewText = "Spotted giant squirrels, hornbills, and elephant herds near Rishia Dam watchtower.",
                travelTip = "Obtain entry permit at Jadachua Forest Gate before 7:30 AM; high-clearance SUVs are best for the forest track.",
                tipCategory = "Transport & Parking",
                photoUris = listOf("content://balasore360/tourism_photos/kuldiha_canopy_trail.jpg"),
                photoCaption = "Morning mist over Rishia Dam eco-watchtower",
                visitSeason = "Winter",
                moderationStatus = ModerationStatus.APPROVED,
                moderationNote = "Verified eco-tourism permit guidance.",
                helpfulCount = 15,
                createdAt = now - 3600_000L * 20
            )
        )
    }

    fun submitUgc(
        spotId: String,
        spotName: String,
        userId: String,
        authorName: String,
        rating: Int,
        reviewText: String,
        travelTip: String,
        tipCategory: String,
        photoUris: List<String>,
        photoCaption: String = "",
        visitSeason: String = "Winter"
    ): TourismUgcSubmissionItem {
        val combinedText = "$reviewText $travelTip $photoCaption".lowercase()
        val isAutoFlagged = UNSAFE_KEYWORDS.any { combinedText.contains(it) }
        val initialStatus = if (isAutoFlagged) ModerationStatus.FLAGGED else ModerationStatus.PENDING
        val initialNote = if (isAutoFlagged) {
            "Auto-flagged by safety filter: requires manual moderator inspection"
        } else {
            "Submitted to moderation queue (PENDING editorial & photo verification)"
        }

        val item = TourismUgcSubmissionItem(
            spotId = spotId,
            spotName = spotName,
            userId = userId,
            authorName = authorName.ifBlank { "Balasore Explorer" },
            rating = rating.coerceIn(1, 5),
            reviewText = reviewText.trim(),
            travelTip = travelTip.trim(),
            tipCategory = tipCategory,
            photoUris = photoUris,
            photoCaption = photoCaption.trim(),
            visitSeason = visitSeason,
            moderationStatus = initialStatus,
            moderationNote = initialNote,
            helpfulCount = 1
        )
        _submissions.value = listOf(item) + _submissions.value
        return item
    }

    fun moderateSubmission(
        itemId: String,
        newStatus: ModerationStatus,
        moderatorNote: String = ""
    ): TourismUgcSubmissionItem? {
        var updatedItem: TourismUgcSubmissionItem? = null
        _submissions.value = _submissions.value.map { current ->
            if (current.id == itemId) {
                val next = current.copy(
                    moderationStatus = newStatus,
                    moderationNote = moderatorNote.ifBlank {
                        when (newStatus) {
                            ModerationStatus.APPROVED -> "Approved & published by tourism moderator"
                            ModerationStatus.REJECTED -> "Rejected: did not meet community tourism guidelines"
                            ModerationStatus.FLAGGED -> "Flagged for safety/accuracy review"
                            ModerationStatus.PENDING -> "Returned to pending queue"
                        }
                    }
                )
                updatedItem = next
                next
            } else {
                current
            }
        }
        return updatedItem
    }

    fun markHelpful(itemId: String) {
        _submissions.value = _submissions.value.map {
            if (it.id == itemId) it.copy(helpfulCount = it.helpfulCount + 1) else it
        }
    }

    fun getApprovedForSpot(spotId: String? = null): List<TourismUgcSubmissionItem> {
        return _submissions.value.filter {
            it.moderationStatus == ModerationStatus.APPROVED &&
                (spotId == null || spotId == "ALL" || it.spotId == spotId)
        }
    }

    fun getPendingAndFlaggedQueue(): List<TourismUgcSubmissionItem> {
        return _submissions.value.filter {
            it.moderationStatus == ModerationStatus.PENDING || it.moderationStatus == ModerationStatus.FLAGGED
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TourismCommunityUgcHubCard(
    language: AppLanguage = AppLanguage.ENGLISH,
    activeUserId: String? = null,
    activeUserName: String = "Balasore Explorer",
    onOpenFullModeratorSheet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA
    val allSubmissions by TourismUgcModerationRepository.submissions.collectAsStateWithLifecycle()

    val firestoreService = remember { FirestoreService(context) }
    val reviewDao = remember { AppDatabase.getInstance(context).reviewDao() }

    // 0 = Community Feed (Approved), 1 = Upload Photo, Review & Tip, 2 = Moderation Queue
    var activeTab by remember { mutableIntStateOf(0) }
    var selectedSpotFilter by remember { mutableStateOf("ALL") }

    // Form State for Tab 1 (Submit Photo + Review + Travel Tip)
    val hotspots = remember { BalasoreRepository.hotspots }
    var formSpot by remember { mutableStateOf(hotspots.first()) }
    var formRating by remember { mutableIntStateOf(5) }
    var formAuthorName by remember(activeUserName) { mutableStateOf(activeUserName) }
    var formReviewText by remember { mutableStateOf("") }
    var formTravelTip by remember { mutableStateOf("") }
    var formTipCategory by remember { mutableStateOf(TourismUgcModerationRepository.TIP_CATEGORIES.first()) }
    var formVisitSeason by remember { mutableStateOf("Winter") }
    var formPhotoCaption by remember { mutableStateOf("") }
    var uploadedPhotoUris by remember { mutableStateOf<List<String>>(emptyList()) }

    // Zero-permission Android Photo Picker (Play Policy compliant)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            uploadedPhotoUris = (uploadedPhotoUris + uri.toString()).distinct()
            Toast.makeText(context, "Photo attached for ${formSpot.name}!", Toast.LENGTH_SHORT).show()
        }
    }

    val approvedItems = remember(allSubmissions, selectedSpotFilter) {
        allSubmissions.filter {
            it.moderationStatus == ModerationStatus.APPROVED &&
                (selectedSpotFilter == "ALL" || it.spotId == selectedSpotFilter)
        }
    }

    val moderationQueueItems = remember(allSubmissions) {
        allSubmissions.filter {
            it.moderationStatus == ModerationStatus.PENDING ||
                it.moderationStatus == ModerationStatus.FLAGGED
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tourism_community_ugc_hub_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, OceanBlue.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(OceanBlue, EmeraldGreen)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoCamera,
                            contentDescription = "Tourism UGC Hub",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia)
                                "📸 ପର୍ଯ୍ୟଟক ଫଟୋ, ସମୀକ୍ଷା ଓ ଭ୍ରମଣ ଟିপ୍ସ"
                            else
                                "📸 Traveler Photos, Reviews & Insider Tips",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isOdia)
                                "ସ୍ଥାନୀୟ ଆକର୍ଷଣ ପାଇଁ ଫଟୋ ଅପଲୋଡ୍, ଟିପ୍ସ ସେୟାର୍ ଓ ମଡରେସନ୍ ସିଷ୍ଟମ୍"
                            else
                                "Upload attraction photos, share local tips & moderated community reviews",
                            fontSize = 11.sp,
                            color = BentoSlate600
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Text(
                        text = "${moderationQueueItems.size} Pending",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFB45309),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // 3-Mode Tab Bar: Community Feed | Upload Photo/Review/Tip | Moderation Queue
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(4.dp)
            ) {
                val tabs = listOf(
                    0 to (if (isOdia) "🌟 ଫିଡ୍ (${approvedItems.size})" else "🌟 Verified (${approvedItems.size})"),
                    1 to (if (isOdia) "➕ ଫଟୋ ଓ ଟିପ୍ସ ଦିଅନ୍ତୁ" else "➕ Share Photo & Tip"),
                    2 to (if (isOdia) "🛡️ ମଡରେସନ୍ (${moderationQueueItems.size})" else "🛡️ Moderate (${moderationQueueItems.size})")
                )
                tabs.forEach { (idx, label) ->
                    val isSelected = activeTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) OceanBlue else Color.Transparent)
                            .clickable { activeTab = idx }
                            .padding(vertical = 8.dp)
                            .testTag("tourism_ugc_tab_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            when (activeTab) {
                0 -> {
                    // ================= TAB 0: APPROVED TRAVELER PHOTOS, REVIEWS & TIPS =================
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        item {
                            FilterChip(
                                selected = selectedSpotFilter == "ALL",
                                onClick = { selectedSpotFilter = "ALL" },
                                label = { Text(if (isOdia) "ସମସ୍ତ ସ୍ଥାନ" else "All Attractions", fontSize = 11.sp) }
                            )
                        }
                        items(hotspots.take(6)) { spot ->
                            FilterChip(
                                selected = selectedSpotFilter == spot.id,
                                onClick = { selectedSpotFilter = spot.id },
                                label = { Text(spot.name.substringBefore(" "), fontSize = 11.sp) }
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        approvedItems.forEach { ugc ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.18f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tourism_ugc_approved_card_${ugc.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Text(
                                                    text = "📍 ${ugc.spotName}",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 13.sp,
                                                    color = OceanBlue
                                                )
                                                Surface(
                                                    shape = RoundedCornerShape(6.dp),
                                                    color = Color(0xFFDCFCE7)
                                                ) {
                                                    Text(
                                                        text = "✓ Verified",
                                                        fontSize = 9.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color(0xFF166534),
                                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "By ${ugc.authorName} • ${ugc.visitSeason} • ${"★".repeat(ugc.rating)}",
                                                fontSize = 11.sp,
                                                color = AmberGold,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }

                                        OutlinedButton(
                                            onClick = { TourismUgcModerationRepository.markHelpful(ugc.id) },
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("tourism_ugc_helpful_btn_${ugc.id}")
                                        ) {
                                            Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(13.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Helpful (${ugc.helpfulCount})", fontSize = 10.5.sp)
                                        }
                                    }

                                    // Uploaded Photo Strip
                                    if (ugc.photoUris.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFE0F2FE),
                                            border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.PhotoLibrary,
                                                    contentDescription = null,
                                                    tint = OceanBlue,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "📷 ${ugc.photoUris.size} Traveler Photo(s) Attached",
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 11.5.sp,
                                                        color = OceanBlueDark
                                                    )
                                                    if (ugc.photoCaption.isNotBlank()) {
                                                        Text(
                                                            text = ugc.photoCaption,
                                                            fontSize = 11.sp,
                                                            color = BentoSlate700
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    // Review Text
                                    if (ugc.reviewText.isNotBlank()) {
                                        Text(
                                            text = ugc.reviewText,
                                            fontSize = 12.5.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }

                                    // Highlighted Insider Tip Box
                                    if (ugc.travelTip.isNotBlank()) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFFFFFBEB),
                                            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(10.dp),
                                                verticalAlignment = Alignment.Top
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Lightbulb,
                                                    contentDescription = null,
                                                    tint = Color(0xFFD97706),
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Column {
                                                    Text(
                                                        text = "💡 Insider Tip (${ugc.tipCategory}):",
                                                        fontWeight = FontWeight.ExtraBold,
                                                        fontSize = 11.sp,
                                                        color = Color(0xFFB45309)
                                                    )
                                                    Text(
                                                        text = ugc.travelTip,
                                                        fontSize = 11.5.sp,
                                                        color = Color(0xFF78350F)
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

                1 -> {
                    // ================= TAB 1: UPLOAD PHOTO, WRITE REVIEW & SHARE INSIDER TIP =================
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.testTag("tourism_ugc_submission_form")
                    ) {
                        Text(
                            text = if (isOdia) "୧. ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ବାଛନ୍ତୁ:" else "1. Select Balasore Attraction:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(hotspots.take(6)) { spot ->
                                FilterChip(
                                    selected = formSpot.id == spot.id,
                                    onClick = { formSpot = spot },
                                    label = { Text(spot.name, fontSize = 11.sp) },
                                    modifier = Modifier.testTag("ugc_select_spot_chip_${spot.id}")
                                )
                            }
                        }

                        // Star Rating + Season Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                for (s in 1..5) {
                                    IconButton(
                                        onClick = { formRating = s },
                                        modifier = Modifier
                                            .size(34.dp)
                                            .testTag("ugc_star_btn_$s")
                                    ) {
                                        Icon(
                                            imageVector = if (s <= formRating) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                            contentDescription = "$s Stars",
                                            tint = if (s <= formRating) AmberGold else BentoSlate600
                                        )
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                listOf("Winter", "Monsoon", "Summer").forEach { season ->
                                    FilterChip(
                                        selected = formVisitSeason == season,
                                        onClick = { formVisitSeason = season },
                                        label = { Text(season, fontSize = 10.sp) }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = formAuthorName,
                            onValueChange = { formAuthorName = it },
                            label = { Text(if (isOdia) "ଆପଣଙ୍କ ନାମ" else "Your Name") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ugc_author_name_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        OutlinedTextField(
                            value = formReviewText,
                            onValueChange = { formReviewText = it },
                            label = { Text(if (isOdia) "ଆପଣଙ୍କ ଭ୍ରମଣ ଅନୁଭୂତି (Review)" else "Write Your Attraction Review") },
                            placeholder = { Text("Share what you loved about ${formSpot.name}...") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ugc_review_text_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Insider Tip Category & Input
                        Text(
                            text = if (isOdia) "୨. ଭ୍ରମଣ ଟିପ୍ସ ବିଭାଗ (Tip Category):" else "2. Share an Insider Travel Tip:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(TourismUgcModerationRepository.TIP_CATEGORIES) { cat ->
                                FilterChip(
                                    selected = formTipCategory == cat,
                                    onClick = { formTipCategory = cat },
                                    label = { Text(cat, fontSize = 10.5.sp) },
                                    modifier = Modifier.testTag("ugc_tip_category_chip_${cat.take(8).lowercase()}")
                                )
                            }
                        }

                        OutlinedTextField(
                            value = formTravelTip,
                            onValueChange = { formTravelTip = it },
                            label = { Text(if (isOdia) "ଭ୍ରମଣକାରୀଙ୍କ ପାଇଁ ଟିପ୍ସ (Travel Tip)" else "Helpful Tip for Fellow Visitors ($formTipCategory)") },
                            placeholder = { Text("e.g., Best tide hour, parking gate, or prasad timing...") },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ugc_travel_tip_input"),
                            shape = RoundedCornerShape(12.dp)
                        )

                        // Upload Photo Section (Android Photo Picker + Quick Preset)
                        Text(
                            text = if (isOdia) "୩. ଆକର୍ଷଣର ଫଟୋ ଅପଲୋଡ୍ କରନ୍ତୁ:" else "3. Upload Attraction Photos:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    photoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ugc_pick_photo_gallery_btn")
                            ) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOdia) "ଗ୍ୟାଲେରୀରୁ ଫଟୋ" else "Upload Photo", fontSize = 11.5.sp)
                            }

                            OutlinedButton(
                                onClick = {
                                    val preset = TourismUgcModerationRepository.SAMPLE_PHOTO_PRESETS.first()
                                    uploadedPhotoUris = (uploadedPhotoUris + preset.first).distinct()
                                    if (formPhotoCaption.isBlank()) {
                                        formPhotoCaption = preset.second
                                    }
                                    Toast.makeText(context, "Attached ${preset.second}!", Toast.LENGTH_SHORT).show()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ugc_attach_sample_photo_btn")
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOdia) "କ୍ୟାମେରା ସ୍ନାପ୍" else "Attach Spot Snap", fontSize = 11.5.sp)
                            }
                        }

                        if (uploadedPhotoUris.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFFF0FDF4),
                                border = BorderStroke(1.dp, Color(0xFF86EFAC)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "✓ ${uploadedPhotoUris.size} Photo(s) Attached Ready for Upload",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534)
                                    )
                                    TextButton(onClick = { uploadedPhotoUris = emptyList() }) {
                                        Text("Clear", fontSize = 11.sp)
                                    }
                                }
                            }
                        }

                        Button(
                            onClick = {
                                if (formReviewText.isBlank() && formTravelTip.isBlank()) {
                                    Toast.makeText(context, "Please write a review or travel tip.", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }
                                val created = TourismUgcModerationRepository.submitUgc(
                                    spotId = formSpot.id,
                                    spotName = formSpot.name,
                                    userId = activeUserId ?: "local_explorer",
                                    authorName = formAuthorName,
                                    rating = formRating,
                                    reviewText = formReviewText,
                                    travelTip = formTravelTip,
                                    tipCategory = formTipCategory,
                                    photoUris = uploadedPhotoUris,
                                    photoCaption = formPhotoCaption,
                                    visitSeason = formVisitSeason
                                )
                                // Update behavioral recommendation engine profile
                                BalasoreRecommendationEngine.recordLocationViewed(context, formSpot)

                                coroutineScope.launch {
                                    val combinedComment = buildString {
                                        if (formReviewText.isNotBlank()) append(formReviewText.trim())
                                        if (formTravelTip.isNotBlank()) {
                                            if (isNotEmpty()) append(" | ")
                                            append("Tip ($formTipCategory): ${formTravelTip.trim()}")
                                        }
                                    }
                                    val dbId = reviewDao.insertReview(
                                        ReviewEntity(
                                            targetType = "HOTSPOT",
                                            targetId = formSpot.id,
                                            targetTitle = formSpot.name,
                                            userId = activeUserId ?: "local_explorer",
                                            userName = created.authorName,
                                            rating = formRating,
                                            comment = combinedComment,
                                            timestamp = created.createdAt
                                        )
                                    )
                                    ContentModerationRepository.registerSubmittedReview(
                                        context = context,
                                        reviewDbId = dbId,
                                        targetType = "HOTSPOT",
                                        targetId = formSpot.id,
                                        targetTitle = formSpot.name,
                                        userId = activeUserId ?: "local_explorer",
                                        userName = created.authorName,
                                        rating = formRating,
                                        comment = combinedComment
                                    )
                                    firestoreService.submitTourismReview(
                                        TourismReviewCloudEntity(
                                            reviewId = created.id,
                                            spotId = formSpot.id,
                                            spotName = formSpot.name,
                                            userId = activeUserId ?: "local_explorer",
                                            userDisplayName = created.authorName,
                                            rating = formRating,
                                            reviewText = formReviewText,
                                            travelTip = formTravelTip,
                                            tipCategory = formTipCategory,
                                            photoUris = uploadedPhotoUris,
                                            visitSeason = formVisitSeason,
                                            moderationStatus = created.moderationStatus.name
                                        )
                                    )
                                }

                                formReviewText = ""
                                formTravelTip = ""
                                formPhotoCaption = ""
                                uploadedPhotoUris = emptyList()
                                activeTab = 2 // Switch to Moderation Queue so user can see/verify moderation workflow
                                Toast.makeText(
                                    context,
                                    "Submitted for ${formSpot.name}! Queued in Moderation (${created.moderationStatus.name}).",
                                    Toast.LENGTH_LONG
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("ugc_submit_review_tip_btn")
                        ) {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOdia) "ସମୀକ୍ଷା ଓ ମଡରେସନ୍ ପାଇଁ ପଠାନ୍ତୁ" else "Submit Photo, Review & Tip for Moderation",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            )
                        }
                    }
                }

                else -> {
                    // ================= TAB 2: TOURISM UGC MODERATION SYSTEM =================
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.testTag("tourism_ugc_moderation_queue_panel")
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isOdia) "🛡️ ପର୍ଯ୍ୟଟନ କଣ୍ଟେଣ୍ଟ ମଡରେସନ୍ ସିଷ୍ଟମ୍" else "🛡️ Tourism UGC Moderation Queue",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Verify traveler photos, attraction reviews & safety tips before publishing",
                                        fontSize = 11.sp,
                                        color = Color(0xFFBAE6FD)
                                    )
                                }
                                TextButton(onClick = onOpenFullModeratorSheet) {
                                    Text("Full Console →", color = Color(0xFFFDE68A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        if (moderationQueueItems.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFF0FDF4),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "✓ All submitted traveler photos, reviews & tips have been moderated!",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        } else {
                            moderationQueueItems.forEach { item ->
                                val isFlagged = item.moderationStatus == ModerationStatus.FLAGGED
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = if (isFlagged) Color(0xFFFEF2F2) else Color(0xFFFFFBEB),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isFlagged) Color(0xFFFCA5A5) else Color(0xFFFDE68A)
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("tourism_ugc_mod_item_${item.id}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "📍 ${item.spotName} • By ${item.authorName}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 12.5.sp
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isFlagged) Color(0xFFDC2626) else Color(0xFFD97706)
                                            ) {
                                                Text(
                                                    text = item.moderationStatus.name,
                                                    fontSize = 9.5.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        if (item.photoUris.isNotEmpty()) {
                                            Text(
                                                text = "📷 ${item.photoUris.size} Photo(s) Attached ${if (item.photoCaption.isNotBlank()) "• ${item.photoCaption}" else ""}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = OceanBlue
                                            )
                                        }
                                        if (item.reviewText.isNotBlank()) {
                                            Text(
                                                text = "Review (${item.rating}★): \"${item.reviewText}\"",
                                                fontSize = 12.sp,
                                                color = BentoSlate800
                                            )
                                        }
                                        if (item.travelTip.isNotBlank()) {
                                            Text(
                                                text = "💡 Tip (${item.tipCategory}): \"${item.travelTip}\"",
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Color(0xFF92400E)
                                            )
                                        }
                                        Text(
                                            text = "Status Note: ${item.moderationNote}",
                                            fontSize = 10.5.sp,
                                            color = BentoSlate600
                                        )

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Button(
                                                onClick = {
                                                    TourismUgcModerationRepository.moderateSubmission(
                                                        itemId = item.id,
                                                        newStatus = ModerationStatus.APPROVED,
                                                        moderatorNote = "Verified & approved for public Tourism feed"
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        "Approved & published ${item.authorName}'s post for ${item.spotName}!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("ugc_mod_approve_btn_${item.id}")
                                            ) {
                                                Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Approve & Publish", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }

                                            OutlinedButton(
                                                onClick = {
                                                    TourismUgcModerationRepository.moderateSubmission(
                                                        itemId = item.id,
                                                        newStatus = ModerationStatus.REJECTED,
                                                        moderatorNote = "Rejected by moderator for quality/policy violation"
                                                    )
                                                    Toast.makeText(
                                                        context,
                                                        "Rejected submission for ${item.spotName}.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                },
                                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                                                shape = RoundedCornerShape(8.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .testTag("ugc_mod_reject_btn_${item.id}")
                                            ) {
                                                Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp))
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text("Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
