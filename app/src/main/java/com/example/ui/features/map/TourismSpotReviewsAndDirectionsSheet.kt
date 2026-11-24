package com.example.ui.features.map

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.firebase.FirestoreService
import com.example.data.local.AppDatabase
import com.example.data.local.ReviewEntity
import com.example.data.model.AppLanguage
import com.example.data.model.TourismReviewCloudEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class DirectionsTravelMode(
    val googleNavMode: String,
    val webMode: String,
    val labelEn: String,
    val labelOr: String,
    val avgSpeedKmh: Double
) {
    DRIVING("d", "driving", "Drive", "କାର/ଅଟୋ", 36.0),
    TRANSIT("r", "transit", "Bus / Transit", "ବସ୍ / ଟ୍ରେନ୍", 24.0),
    WALKING("w", "walking", "Walk", "ଚାଲିବା", 4.8)
}

data class TourismMapReviewDisplayItem(
    val id: String,
    val authorName: String,
    val authorLocality: String,
    val rating: Int,
    val comment: String,
    val travelTip: String = "",
    val timestamp: Long,
    val isCloudSynced: Boolean = false
)

data class TourismReviewSummaryStats(
    val averageRating: Double,
    val totalCount: Int,
    val starCounts: Map<Int, Int>
)

/**
 * Pure helper utilities for Tourism Map directions launching, ETA estimation,
 * and rating summary aggregation.
 */
object TourismMapDirectionsHelper {

    const val BALASORE_STATION_LAT = 21.4934
    const val BALASORE_STATION_LNG = 86.9135

    fun calculateDistanceKm(
        originLat: Double,
        originLng: Double,
        destLat: Double,
        destLng: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(destLat - originLat)
        val dLon = Math.toRadians(destLng - originLng)
        val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(originLat)) *
            cos(Math.toRadians(destLat)) *
            sin(dLon / 2).pow(2.0)
        return 2.0 * earthRadiusKm * asin(sqrt(a.coerceIn(0.0, 1.0)))
    }

    fun estimateMinutes(distanceKm: Double, mode: DirectionsTravelMode): Int {
        if (distanceKm <= 0.05) return 1
        // Apply road-winding multiplier (1.28x) over great-circle distance
        val roadDistanceKm = distanceKm * 1.28
        return ((roadDistanceKm / mode.avgSpeedKmh) * 60.0).roundToInt().coerceAtLeast(2)
    }

    fun buildGoogleNavigationUri(
        destLat: Double,
        destLng: Double,
        mode: DirectionsTravelMode
    ): String {
        return "google.navigation:q=$destLat,$destLng&mode=${mode.googleNavMode}"
    }

    fun buildWebDirectionsUrl(
        destLat: Double,
        destLng: Double,
        mode: DirectionsTravelMode
    ): String {
        return "https://www.google.com/maps/dir/?api=1&destination=$destLat,$destLng&travelmode=${mode.webMode}"
    }

    fun buildGeoDirectionsUri(
        spotName: String,
        destLat: Double,
        destLng: Double
    ): String {
        val encodedLabel = java.net.URLEncoder.encode(spotName, "UTF-8").replace("+", "%20")
        return "geo:$destLat,$destLng?q=$destLat,$destLng($encodedLabel)"
    }

    fun launchDirections(
        context: Context,
        spotName: String,
        destLat: Double,
        destLng: Double,
        mode: DirectionsTravelMode = DirectionsTravelMode.DRIVING
    ) {
        val googleNavIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(buildGoogleNavigationUri(destLat, destLng, mode))
        ).apply {
            setPackage("com.google.android.apps.maps")
        }
        try {
            context.startActivity(googleNavIntent)
            return
        } catch (_: Exception) {
            // Fall back to default map application via generic geo: URI
        }

        val geoIntent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse(buildGeoDirectionsUri(spotName, destLat, destLng))
        )
        try {
            context.startActivity(geoIntent)
            return
        } catch (_: Exception) {
            // Fall back to browser Google Maps directions URL
        }

        try {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse(buildWebDirectionsUrl(destLat, destLng, mode))
                )
            )
        } catch (_: Exception) {
            Toast.makeText(
                context,
                "No navigation or browser app available on this device.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun summarizeReviews(
        reviews: List<TourismMapReviewDisplayItem>,
        baselineRating: Double = 4.6
    ): TourismReviewSummaryStats {
        val counts = (1..5).associateWith { star ->
            reviews.count { it.rating.coerceIn(1, 5) == star }
        }
        val avg = if (reviews.isNotEmpty()) {
            reviews.map { it.rating.coerceIn(1, 5) }.average()
        } else {
            baselineRating.coerceIn(1.0, 5.0)
        }
        return TourismReviewSummaryStats(
            averageRating = (avg * 10.0).roundToInt() / 10.0,
            totalCount = reviews.size,
            starCounts = counts
        )
    }
}

/**
 * Interactive Modal Bottom Sheet for any Balasore tourism attraction tapped on the map:
 * - Multi-mode Turn-by-Turn Directions (Drive, Bus/Transit, Walk) with distance & ETA
 * - Live Rating Summary & Star Breakdown
 * - View existing community reviews (merged Room local + Firestore cloud reviews)
 * - Submit a new user review with 1-5 star rating, season tag, comment, and travel tip
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TourismSpotReviewsAndDirectionsSheet(
    spotId: String,
    spotName: String,
    spotCategory: String,
    spotLocation: String,
    spotLatitude: Double,
    spotLongitude: Double,
    baselineRating: Double = 4.6,
    language: AppLanguage = AppLanguage.ENGLISH,
    originLat: Double = TourismMapDirectionsHelper.BALASORE_STATION_LAT,
    originLng: Double = TourismMapDirectionsHelper.BALASORE_STATION_LNG,
    onStartOfflineRoute: (() -> Unit)? = null,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA

    val reviewDao = remember { AppDatabase.getInstance(context).reviewDao() }
    val firestoreService = remember { FirestoreService(context) }
    val authManager = remember { FirebaseAuthManager(context) }

    val activeSessionUser by authManager.activeSessionUser.collectAsStateWithLifecycle()
    val firebaseUser by authManager.currentUser.collectAsStateWithLifecycle()
    val localReviews by reviewDao.getReviewsForTarget("HOTSPOT", spotId)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var cloudReviews by remember(spotId) { mutableStateOf<List<TourismReviewCloudEntity>>(emptyList()) }
    var isLoadingCloud by remember(spotId) { mutableStateOf(true) }

    LaunchedEffect(spotId) {
        isLoadingCloud = true
        val res = firestoreService.fetchTourismReviewsForSpot(spotId)
        cloudReviews = res.getOrDefault(emptyList())
        isLoadingCloud = false
    }

    val mergedReviews = remember(localReviews, cloudReviews) {
        val localItems = localReviews.map { r ->
            val tipDelimiter = "\nTip: "
            val hasTip = r.comment.contains(tipDelimiter)
            val mainComment = if (hasTip) r.comment.substringBefore(tipDelimiter).trim() else r.comment.trim()
            val extractedTip = if (hasTip) r.comment.substringAfter(tipDelimiter).trim() else ""
            TourismMapReviewDisplayItem(
                id = "local_${r.id}",
                authorName = r.userName.ifBlank { "Balasore Traveler" },
                authorLocality = r.userLocality.ifBlank { "Balasore" },
                rating = r.rating.coerceIn(1, 5),
                comment = mainComment,
                travelTip = extractedTip,
                timestamp = r.timestamp,
                isCloudSynced = false
            )
        }
        val cloudItems = cloudReviews.map { c ->
            TourismMapReviewDisplayItem(
                id = "cloud_${c.reviewId}",
                authorName = c.userDisplayName.ifBlank { "Balasore Traveler" },
                authorLocality = c.visitSeason.ifBlank { "Balasore" },
                rating = c.rating.coerceIn(1, 5),
                comment = c.reviewText.trim(),
                travelTip = c.travelTip.trim(),
                timestamp = c.createdAt,
                isCloudSynced = true
            )
        }
        (localItems + cloudItems)
            .distinctBy { "${it.authorName.lowercase(Locale.ROOT)}|${it.comment.trim().lowercase(Locale.ROOT)}" }
            .sortedByDescending { it.timestamp }
    }

    val summaryStats = remember(mergedReviews, baselineRating) {
        TourismMapDirectionsHelper.summarizeReviews(mergedReviews, baselineRating)
    }

    val distanceKm = remember(originLat, originLng, spotLatitude, spotLongitude) {
        TourismMapDirectionsHelper.calculateDistanceKm(originLat, originLng, spotLatitude, spotLongitude)
    }

    // Review composer state
    var selectedStars by remember { mutableIntStateOf(5) }
    var reviewerName by remember(activeSessionUser, firebaseUser) {
        mutableStateOf(
            activeSessionUser?.fullName?.takeIf { it.isNotBlank() }
                ?: firebaseUser?.displayName?.takeIf { it.isNotBlank() }
                ?: ""
        )
    }
    var reviewComment by remember { mutableStateOf("") }
    var travelTip by remember { mutableStateOf("") }
    var visitSeason by remember { mutableStateOf("Winter") }
    var isSubmitting by remember { mutableStateOf(false) }
    var validationError by remember { mutableStateOf<String?>(null) }

    val seasons = listOf("Winter", "Monsoon", "Summer", "Spring")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("tourism_map_reviews_directions_sheet")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 680.dp)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header Banner with Attraction Info
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(OceanBlueDark, OceanBlue, Color(0xFF0EA5E9))
                            )
                        )
                        .padding(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = spotName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(3.dp))
                            Text(
                                text = "$spotCategory • $spotLocation • ${String.format(Locale.US, "%.1f km", distanceKm)}",
                                fontSize = 11.5.sp,
                                color = Color(0xFFE0F2FE),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("tourism_map_reviews_sheet_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Reviews & Directions",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // 2. Multi-Mode Directions Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tourism_map_directions_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Navigation,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOdia) "ଦିଗନିର୍ଦ୍ଦେଶ ଓ ଯାତ୍ରା ସମୟ" else "Get Turn-by-Turn Directions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "${String.format(Locale.US, "%.1f", distanceKm)} km",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DirectionsTravelMode.entries.forEach { mode ->
                                val etaMins = TourismMapDirectionsHelper.estimateMinutes(distanceKm, mode)
                                val icon = when (mode) {
                                    DirectionsTravelMode.DRIVING -> Icons.Default.DirectionsCar
                                    DirectionsTravelMode.TRANSIT -> Icons.Default.DirectionsTransit
                                    DirectionsTravelMode.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
                                }
                                OutlinedButton(
                                    onClick = {
                                        TourismMapDirectionsHelper.launchDirections(
                                            context = context,
                                            spotName = spotName,
                                            destLat = spotLatitude,
                                            destLng = spotLongitude,
                                            mode = mode
                                        )
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .minimumInteractiveComponentSize()
                                        .testTag("tourism_directions_mode_${mode.webMode}")
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = icon,
                                            contentDescription = mode.labelEn,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isOdia) mode.labelOr else mode.labelEn,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "~${etaMins}m",
                                            fontSize = 10.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }

                        if (onStartOfflineRoute != null) {
                            Button(
                                onClick = {
                                    onDismiss()
                                    onStartOfflineRoute()
                                },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .testTag("tourism_directions_offline_route_btn")
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isOdia) "ଅଫଲାଇନ୍ ଭେକ୍ଟର୍ ରୁଟ୍ ଦେଖନ୍ତୁ" else "Preview In-App Offline Route",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            // 3. Rating Breakdown Summary
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tourism_map_rating_summary_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = String.format(Locale.US, "%.1f", summaryStats.averageRating),
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Row {
                                val filledCount = summaryStats.averageRating.roundToInt().coerceIn(1, 5)
                                repeat(5) { idx ->
                                    Icon(
                                        imageVector = if (idx < filledCount) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = AmberGold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (summaryStats.totalCount == 0) {
                                    if (isOdia) "ପ୍ରଥମ ରିଭ୍ୟୁ ଦିଅନ୍ତୁ" else "No user reviews yet"
                                } else {
                                    "${summaryStats.totalCount} ${if (summaryStats.totalCount == 1) "review" else "reviews"}"
                                },
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            (5 downTo 1).forEach { star ->
                                val count = summaryStats.starCounts[star] ?: 0
                                val fraction = if (summaryStats.totalCount > 0) {
                                    count.toFloat() / summaryStats.totalCount.toFloat()
                                } else {
                                    0f
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "${star}★",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.width(22.dp)
                                    )
                                    LinearProgressIndicator(
                                        progress = { fraction },
                                        color = AmberGold,
                                        trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                    )
                                    Text(
                                        text = "$count",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.width(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Submit a User Review Card
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tourism_map_submit_review_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) "ଆପଣଙ୍କ ଅନୁଭୂତି ଓ ରିଭ୍ୟୁ ଲେଖନ୍ତୁ" else "Write a Review for $spotName",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        // Star Rating Selector (48dp touch targets)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            (1..5).forEach { star ->
                                IconButton(
                                    onClick = {
                                        selectedStars = star
                                        validationError = null
                                    },
                                    modifier = Modifier
                                        .minimumInteractiveComponentSize()
                                        .testTag("tourism_map_review_star_$star")
                                ) {
                                    Icon(
                                        imageVector = if (star <= selectedStars) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = "Rate $star stars",
                                        tint = if (star <= selectedStars) AmberGold else MaterialTheme.colorScheme.outline,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = reviewerName,
                            onValueChange = { reviewerName = it },
                            label = { Text(if (isOdia) "ଆପଣଙ୍କ ନାମ" else "Your Name") },
                            placeholder = { Text(if (isOdia) "ଉଦାହରଣ: ସୁମିତ୍ରା ଦାସ" else "e.g. Balasore Explorer") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tourism_map_review_name_input")
                        )

                        // Visit Season Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOdia) "ଋତୁ:" else "Visited in:",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            seasons.forEach { season ->
                                FilterChip(
                                    selected = visitSeason == season,
                                    onClick = { visitSeason = season },
                                    label = { Text(season, fontSize = 11.sp) }
                                )
                            }
                        }

                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = {
                                reviewComment = it
                                validationError = null
                            },
                            label = { Text(if (isOdia) "ଆପଣଙ୍କ ରିଭ୍ୟୁ" else "Your Review") },
                            placeholder = {
                                Text(
                                    if (isOdia) "ଏହି ସ୍ଥାନ କିପରି ଲାଗିଲା? ପରିବେଶ, ଯାତାୟାତ ଓ ସୁରକ୍ଷା ବିଷୟରେ ଲେଖନ୍ତୁ..."
                                    else "Share your experience, scenery, crowd level, or accessibility..."
                                )
                            },
                            minLines = 2,
                            maxLines = 4,
                            isError = validationError != null,
                            supportingText = validationError?.let { err -> { Text(err) } },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tourism_map_review_comment_input")
                        )

                        OutlinedTextField(
                            value = travelTip,
                            onValueChange = { travelTip = it },
                            label = { Text(if (isOdia) "ଭ୍ରମଣ ଟିପ୍ (ଇଚ୍ଛାଧୀନ)" else "Helpful Visitor Tip (Optional)") },
                            placeholder = { Text(if (isOdia) "ଉଦାହରଣ: ସୂର୍ଯ୍ୟୋଦୟ ସମୟରେ ଯିବା ଭଲ" else "e.g. Best visited at sunrise or low tide") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(Icons.Default.TipsAndUpdates, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tourism_map_review_tip_input")
                        )

                        Button(
                            onClick = {
                                val trimmedComment = reviewComment.trim()
                                if (trimmedComment.length < 4) {
                                    validationError = if (isOdia) {
                                        "ଦୟାକରି ଅତି କମ୍‌ରେ ୪ଟି ଅକ୍ଷରର ରିଭ୍ୟୁ ଲେଖନ୍ତୁ।"
                                    } else {
                                        "Please enter at least 4 characters for your review."
                                    }
                                    return@Button
                                }

                                isSubmitting = true
                                val finalAuthor = reviewerName.trim().ifBlank { "Balasore Visitor" }
                                val finalLocality = activeSessionUser?.locality?.ifBlank { "Balasore" } ?: "Balasore"
                                val finalUid = firebaseUser?.uid ?: activeSessionUser?.id ?: "local_${UUID.randomUUID().toString().take(8)}"
                                val finalTip = travelTip.trim()
                                val combinedComment = if (finalTip.isNotBlank()) {
                                    "$trimmedComment\nTip: $finalTip"
                                } else {
                                    trimmedComment
                                }

                                coroutineScope.launch {
                                    val now = System.currentTimeMillis()
                                    withContext(Dispatchers.IO) {
                                        reviewDao.insertReview(
                                            ReviewEntity(
                                                targetType = "HOTSPOT",
                                                targetId = spotId,
                                                targetTitle = spotName,
                                                userId = finalUid,
                                                userName = finalAuthor,
                                                userLocality = finalLocality,
                                                userAvatarUri = activeSessionUser?.avatarUri,
                                                rating = selectedStars,
                                                comment = combinedComment,
                                                timestamp = now
                                            )
                                        )
                                    }

                                    // Sync to Cloud Firestore when authenticated
                                    val cloudReview = TourismReviewCloudEntity(
                                        reviewId = "rev_${UUID.randomUUID().toString().replace("-", "").take(12)}",
                                        spotId = spotId,
                                        spotName = spotName,
                                        userId = finalUid,
                                        userDisplayName = finalAuthor,
                                        userPhotoUrl = activeSessionUser?.avatarUri,
                                        rating = selectedStars,
                                        reviewText = trimmedComment,
                                        travelTip = finalTip,
                                        visitSeason = visitSeason,
                                        moderationStatus = "APPROVED",
                                        createdAt = now
                                    )
                                    firestoreService.submitTourismReview(cloudReview)

                                    reviewComment = ""
                                    travelTip = ""
                                    selectedStars = 5
                                    isSubmitting = false
                                    Toast.makeText(
                                        context,
                                        if (isOdia) "ଆପଣଙ୍କ ରିଭ୍ୟୁ ସଫଳତାର ସହ ଦାଖଲ ହେଲା!" else "Review posted for $spotName!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            enabled = !isSubmitting,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("tourism_map_submit_review_btn")
                        ) {
                            if (isSubmitting) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = if (isOdia) "ରିଭ୍ୟୁ ଦାଖଲ କରନ୍ତୁ" else "Submit Review",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            )
                        }
                    }
                }
            }

            // 5. Community Reviews List Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOdia) "ପର୍ଯ୍ୟଟକଙ୍କ ରିଭ୍ୟୁ (${mergedReviews.size})" else "Visitor Reviews (${mergedReviews.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (isLoadingCloud) {
                        CircularProgressIndicator(
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (mergedReviews.isEmpty()) {
                item {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("tourism_map_reviews_empty_state")
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = if (isOdia) "ଏପର୍ଯ୍ୟନ୍ତ କୌଣସି ରିଭ୍ୟୁ ନାହିଁ" else "Be the first to review $spotName",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = if (isOdia) {
                                    "ଉପରେ ଥିବା ଫର୍ମରେ ଆପଣଙ୍କ ଅନୁଭୂତି ସେୟାର୍ କରି ଅନ୍ୟ ପର୍ଯ୍ୟଟକଙ୍କୁ ସାହାଯ୍ୟ କରନ୍ତୁ।"
                                } else {
                                    "Share your visit tips and rating above to help fellow Balasore explorers."
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(mergedReviews, key = { it.id }) { review ->
                    TourismMapReviewCard(review = review)
                }
            }
        }
    }
}

@Composable
private fun TourismMapReviewCard(review: TourismMapReviewDisplayItem) {
    val dateLabel = remember(review.timestamp) {
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(review.timestamp))
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tourism_map_review_item_${review.id}")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = review.authorName.firstOrNull()?.uppercase() ?: "B",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontSize = 14.sp
                        )
                    }
                    Column {
                        Text(
                            text = review.authorName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${review.authorLocality} • $dateLabel",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AmberGold.copy(alpha = 0.16f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "${review.rating} stars",
                            tint = AmberGold,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "${review.rating}.0",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))

            Text(
                text = review.comment,
                fontSize = 12.5.sp,
                lineHeight = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (review.travelTip.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGreen.copy(alpha = 0.10f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.TipsAndUpdates,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = review.travelTip,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = EmeraldGreen
                        )
                    }
                }
            }
        }
    }
}
