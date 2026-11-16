package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.R
import com.example.data.local.ItineraryItemEntity
import com.example.data.model.AppLanguage
import com.example.data.model.Hotspot
import com.example.ui.components.BALASORE_TOURIST_LOCATIONS
import com.example.ui.components.TouristLocationItem
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import kotlin.math.roundToInt

/**
 * Data item representing a hotspot with its bundled offline photo drawable.
 */
data class GalleryHotspotPhoto(
    val item: TouristLocationItem,
    val localDrawableRes: Int,
    val captionEn: String,
    val captionOr: String
)

/**
 * Curated offline gallery items mapping Balasore hotspots to high-quality local drawables.
 */
val BALASORE_GALLERY_PHOTOS = listOf(
    GalleryHotspotPhoto(
        item = BALASORE_TOURIST_LOCATIONS.first { it.id == "chandipur_beach" },
        localDrawableRes = R.drawable.img_chandipur_beach,
        captionEn = "Low tide recession revealing the golden seabed up to 5 km into the Bay of Bengal.",
        captionOr = "ଭଟ୍ଟା ସମୟରେ ୫ କିଲୋମିଟର ପଛକୁ ହଟିଯାଇଥିବା ସୁନେଲି ସମୁଦ୍ର ଶଯ୍ୟା।"
    ),
    GalleryHotspotPhoto(
        item = BALASORE_TOURIST_LOCATIONS.first { it.id == "khirachora_gopinath" },
        localDrawableRes = R.drawable.img_temple_balasore,
        captionEn = "Ancient 12th-century sanctum of Khirachora Gopinatha in Remuna, home of Amruta Keli.",
        captionOr = "ରେମୁଣାର ପ୍ରସିଦ୍ଧ ଦ୍ୱାଦଶ ଶତାବ୍ଦୀର କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ଜୀଉଙ୍କ ପବିତ୍ର ମନ୍ଦିର।"
    ),
    GalleryHotspotPhoto(
        item = BALASORE_TOURIST_LOCATIONS.first { it.id == "emami_jagannath" },
        localDrawableRes = R.drawable.img_temple_balasore,
        captionEn = "Carved Kalinga stone architecture and sprawling gardens of Januganj Jagannath temple.",
        captionOr = "ବାଲେଶ୍ୱର ଜାନୁଗଞ୍ଜର କଳିଙ୍ଗ ଶୈଳୀର ପ୍ରସ୍ତର ନିର୍ମିତ ଶ୍ରୀ ଜଗନ୍ନାଥ ମନ୍ଦିର।"
    ),
    GalleryHotspotPhoto(
        item = BALASORE_TOURIST_LOCATIONS.first { it.id == "panchalingeswar_temple" },
        localDrawableRes = R.drawable.img_feature_graphic,
        captionEn = "Perennial hill stream flowing over five self-manifested Shiva lingams in Nilagiri range.",
        captionOr = "ନୀଳଗିରି ପାହାଡ଼ ଝରଣାରେ ନିରନ୍ତର ସ୍ନାନ କରୁଥିବା ପାଞ୍ଚଟି ପ୍ରାକୃତିକ ଶିବଲିଙ୍ଗ।"
    ),
    GalleryHotspotPhoto(
        item = BALASORE_TOURIST_LOCATIONS.first { it.id == "talasari_beach" },
        localDrawableRes = R.drawable.img_chandipur_beach,
        captionEn = "Tranquil sandy expanse fringed by casuarina groves and thousands of red ghost crabs.",
        captionOr = "ଝାଉଁବଣ ଘେରା ନିରୋଳା ବେଳାଭୂମି ଓ ଲାଲ୍ କଙ୍କଡ଼ାର ଅଦ୍ଭୁତ ସମାଗମ।"
    ),
    GalleryHotspotPhoto(
        item = BALASORE_TOURIST_LOCATIONS.first { it.id == "kuldiha_wildlife" },
        localDrawableRes = R.drawable.img_feature_graphic,
        captionEn = "Dense sal and deciduous forest sanctuary sheltering herds of wild Asian elephants.",
        captionOr = "ହାତୀ ଅଭୟାରଣ୍ୟ କୁଳଡିହାର ଘଞ୍ଚ ଶାଳ ଜଙ୍ଗଲ ଓ ପ୍ରାକୃତିକ ସୌନ୍ଦର୍ଯ୍ୟ।"
    )
)

/**
 * Offline-first photo gallery component using Coil to display locally stored images of Balasore hotspots.
 *
 * Key features:
 * - Coil AsyncImage loading local drawables 100% offline.
 * - Zoom & pan gestures using Compose Foundation:
 *     - Pinch-to-zoom (1.0x to 4.0x)
 *     - Smooth pan when zoomed in
 *     - Double-tap to toggle between 1.0x and 2.5x zoom
 *     - Zoom controls (+, -, reset) with live percentage readout
 * - Custom swipe-to-dismiss gesture navigation:
 *     - Drag down or swipe right when unzoomed smoothly dismisses the gallery
 * - Edge-to-edge immersive presentation with translucent controls
 * - Personalized Travel Itinerary:
 *     - Users can select tourist hotspots from the gallery and save them to a daily schedule
 *     - Stored locally in the Room database
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreTourismGallerySheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    initialIndex: Int = 0,
    itineraryItems: List<ItineraryItemEntity> = emptyList(),
    onAddToItinerary: (Hotspot, Int, String, String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val density = LocalDensity.current
    val dismissThresholdPx = with(density) { 110.dp.toPx() }

    var selectedIndex by remember {
        mutableIntStateOf(initialIndex.coerceIn(0, BALASORE_GALLERY_PHOTOS.lastIndex))
    }
    val currentPhoto = BALASORE_GALLERY_PHOTOS[selectedIndex]

    // Zoom and pan state
    var scale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Swipe-to-dismiss gesture state
    var dragDismissOffsetY by remember { mutableFloatStateOf(0f) }
    var dragDismissOffsetX by remember { mutableFloatStateOf(0f) }

    // Toggle overlays visibility for full immersive edge-to-edge view
    var showOverlays by remember { mutableStateOf(true) }

    // Itinerary Dialog Sheet state
    var showItineraryDialog by remember { mutableStateOf(false) }

    // Reset zoom when switching images
    fun resetZoom() {
        scale = 1f
        panOffset = Offset.Zero
    }

    // Dismiss animation spring
    val animatedDismissOffsetY by animateFloatAsState(
        targetValue = dragDismissOffsetY,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dismiss_offset_y"
    )
    val animatedDismissOffsetX by animateFloatAsState(
        targetValue = dragDismissOffsetX,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "dismiss_offset_x"
    )

    // Check if current hotspot is already in itinerary
    val isAlreadyInItinerary = remember(itineraryItems, currentPhoto.item.id) {
        itineraryItems.any { it.hotspotId == currentPhoto.item.id }
    }
    val itineraryDaySaved = remember(itineraryItems, currentPhoto.item.id) {
        itineraryItems.find { it.hotspotId == currentPhoto.item.id }?.dayNumber
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF060B14))
            .offset {
                IntOffset(
                    animatedDismissOffsetX.roundToInt().coerceAtLeast(0),
                    animatedDismissOffsetY.roundToInt().coerceAtLeast(0)
                )
            }
            // Custom swipe-to-dismiss gesture navigation (active only when unzoomed)
            .pointerInput(scale) {
                if (scale <= 1.05f) {
                    detectVerticalDragGestures(
                        onDragEnd = {
                            if (dragDismissOffsetY > dismissThresholdPx) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onClose()
                            } else {
                                dragDismissOffsetY = 0f
                            }
                        },
                        onDragCancel = { dragDismissOffsetY = 0f },
                        onVerticalDrag = { change, dragAmount ->
                            if (dragAmount > 0 || dragDismissOffsetY > 0) {
                                change.consume()
                                dragDismissOffsetY = (dragDismissOffsetY + dragAmount).coerceAtLeast(0f)
                            }
                        }
                    )
                }
            }
            .pointerInput(scale) {
                if (scale <= 1.05f) {
                    detectHorizontalDragGestures(
                        onDragEnd = {
                            if (dragDismissOffsetX > dismissThresholdPx) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onClose()
                            } else {
                                dragDismissOffsetX = 0f
                            }
                        },
                        onDragCancel = { dragDismissOffsetX = 0f },
                        onHorizontalDrag = { change, dragAmount ->
                            if (dragAmount > 0 || dragDismissOffsetX > 0) {
                                change.consume()
                                dragDismissOffsetX = (dragDismissOffsetX + dragAmount).coerceAtLeast(0f)
                            }
                        }
                    )
                }
            }
            .testTag("balasore_tourism_gallery_sheet")
    ) {
        // Main Photo Display with Coil + Compose Foundation Transform Gestures
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        val newScale = (scale * zoom).coerceIn(1.0f, 4.0f)
                        scale = newScale
                        if (newScale > 1f) {
                            val maxPanX = (newScale - 1f) * 600f
                            val maxPanY = (newScale - 1f) * 800f
                            panOffset = Offset(
                                x = (panOffset.x + pan.x).coerceIn(-maxPanX, maxPanX),
                                y = (panOffset.y + pan.y).coerceIn(-maxPanY, maxPanY)
                            )
                        } else {
                            panOffset = Offset.Zero
                        }
                    }
                }
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (scale > 1.1f) {
                                resetZoom()
                            } else {
                                scale = 2.5f
                            }
                        },
                        onTap = {
                            showOverlays = !showOverlays
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(currentPhoto.localDrawableRes)
                    .crossfade(300)
                    .build(),
                contentDescription = currentPhoto.item.nameEn,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = panOffset.x,
                        translationY = panOffset.y
                    )
                    .testTag("gallery_zoomable_photo"),
                contentScale = ContentScale.Fit
            )
        }

        // Top App Bar / Overlays (Edge-to-Edge)
        AnimatedVisibility(
            visible = showOverlays,
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xCC000000),
                                Color(0x66000000),
                                Color.Transparent
                            )
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(42.dp)
                                .background(Color(0x66000000), CircleShape)
                                .testTag("gallery_close_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Close Gallery",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = if (language == AppLanguage.ODIA) currentPhoto.item.nameOr else currentPhoto.item.nameEn,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${selectedIndex + 1} / ${BALASORE_GALLERY_PHOTOS.size} • ${currentPhoto.item.categoryEn}",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    // Zoom Controls & Readout
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0x88000000),
                            border = BorderStroke(1.dp, Color(0x44FFFFFF))
                        ) {
                            Text(
                                text = "${(scale * 100).roundToInt()}%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                scale = (scale + 0.5f).coerceAtMost(4.0f)
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x66000000), CircleShape)
                                .testTag("gallery_zoom_in_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = "Zoom In",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                scale = (scale - 0.5f).coerceAtLeast(1.0f)
                                if (scale == 1.0f) panOffset = Offset.Zero
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(Color(0x66000000), CircleShape)
                                .testTag("gallery_zoom_out_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOut,
                                contentDescription = "Zoom Out",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        if (scale > 1.05f) {
                            IconButton(
                                onClick = {
                                    resetZoom()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(Color(0x66000000), CircleShape)
                                    .testTag("gallery_zoom_reset_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset Zoom",
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Bottom Controls: Hotspot Bento Details + Thumbnail Carousel (Edge-to-Edge)
        AnimatedVisibility(
            visible = showOverlays,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                Color(0xB3000000),
                                Color(0xF2060B14)
                            )
                        )
                    )
                    .navigationBarsPadding()
                    .padding(bottom = 12.dp)
            ) {
                // Hotspot Information Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("gallery_spot_info_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC0F172A)),
                    border = BorderStroke(1.dp, Color(0x3338BDF8))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = currentPhoto.item.highlightEmoji,
                                    fontSize = 20.sp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) currentPhoto.item.nameOr else currentPhoto.item.nameEn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "📍 ${currentPhoto.item.baseDistanceKm} km from Balasore • ⭐ ${currentPhoto.item.rating}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF38BDF8)
                                    )
                                }
                            }

                            if (isAlreadyInItinerary) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = EmeraldGreen.copy(alpha = 0.2f),
                                    border = BorderStroke(1.dp, EmeraldGreen)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Saved",
                                            tint = EmeraldGreen,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (language == AppLanguage.ODIA) "ଦିନ $itineraryDaySaved ରେ ଯୋଡ଼ା" else "Day $itineraryDaySaved",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = EmeraldGreen
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (language == AppLanguage.ODIA) currentPhoto.captionOr else currentPhoto.captionEn,
                            fontSize = 11.5.sp,
                            color = Color(0xFFCBD5E1),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons: Add to Itinerary & Directions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    showItineraryDialog = true
                                },
                                modifier = Modifier
                                    .weight(1.3f)
                                    .height(38.dp)
                                    .testTag("gallery_add_to_itinerary_btn"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isAlreadyInItinerary) EmeraldGreen else OceanBlue
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CalendarMonth,
                                    contentDescription = "Itinerary",
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isAlreadyInItinerary) {
                                        if (language == AppLanguage.ODIA) "କାର୍ଯ୍ୟସୂଚୀ ବଦଳାନ୍ତୁ" else "Modify Itinerary"
                                    } else {
                                        if (language == AppLanguage.ODIA) "+ କାର୍ଯ୍ୟସୂଚୀରେ ଯୋଡନ୍ତୁ" else "+ Add to Itinerary"
                                    },
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val geoUri = Uri.parse(
                                        "geo:${currentPhoto.item.latitude},${currentPhoto.item.longitude}?q=" +
                                                "${currentPhoto.item.latitude},${currentPhoto.item.longitude}(" +
                                                Uri.encode(currentPhoto.item.nameEn) + ")"
                                    )
                                    val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    }
                                    try {
                                        context.startActivity(intent)
                                    } catch (_: Exception) {
                                        val webIntent = Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("https://maps.google.com/?q=${currentPhoto.item.latitude},${currentPhoto.item.longitude}")
                                        )
                                        context.startActivity(webIntent)
                                    }
                                },
                                modifier = Modifier
                                    .weight(0.9f)
                                    .height(38.dp)
                                    .testTag("gallery_directions_btn"),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                border = BorderStroke(1.dp, Color(0x66FFFFFF)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Directions,
                                    contentDescription = "Directions",
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ମାର୍ଗ" else "Directions",
                                    fontSize = 11.5.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Horizontal Thumbnail Carousel Strip
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(BALASORE_GALLERY_PHOTOS) { index, photo ->
                        val isSelected = index == selectedIndex
                        Surface(
                            modifier = Modifier
                                .size(width = 62.dp, height = 48.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) Color(0xFF38BDF8) else Color(0x44FFFFFF),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (selectedIndex != index) {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedIndex = index
                                        resetZoom()
                                    }
                                }
                                .testTag("gallery_thumbnail_$index"),
                            color = Color(0xFF1E293B)
                        ) {
                            Box {
                                AsyncImage(
                                    model = photo.localDrawableRes,
                                    contentDescription = photo.item.nameEn,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0x3338BDF8))
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Add to Personalized Travel Itinerary Modal Bottom Sheet
        if (showItineraryDialog) {
            AddToItinerarySheet(
                item = currentPhoto.item,
                language = language,
                onDismiss = { showItineraryDialog = false },
                onConfirm = { day, timeSlot, notes ->
                    val hotspot = Hotspot(
                        id = currentPhoto.item.id,
                        name = currentPhoto.item.nameEn,
                        odiaName = currentPhoto.item.nameOr,
                        category = currentPhoto.item.categoryEn,
                        description = currentPhoto.item.shortDescEn,
                        location = "Balasore, Odisha",
                        rating = currentPhoto.item.rating,
                        isFeatured = true,
                        timing = currentPhoto.item.timings,
                        distanceKm = currentPhoto.item.baseDistanceKm
                    )
                    onAddToItinerary(hotspot, day, timeSlot, notes)
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    showItineraryDialog = false
                }
            )
        }
    }
}

/**
 * Bottom Sheet to schedule a tourist hotspot into the personalized daily travel itinerary.
 * Persists the plan into the local Room database offline.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddToItinerarySheet(
    item: TouristLocationItem,
    language: AppLanguage,
    onDismiss: () -> Unit,
    onConfirm: (day: Int, timeSlot: String, notes: String) -> Unit
) {
    var selectedDay by remember { mutableIntStateOf(1) }
    var selectedTimeSlot by remember { mutableStateOf("10:00 AM") }
    var customNotes by remember {
        mutableStateOf(
            if (item.id == "chandipur_beach") "Visit during vanishing sea low tide"
            else if (item.id == "khirachora_gopinath") "Collect Amruta Keli condensed milk bhog"
            else "Explore highlights & photography"
        )
    }

    val timeSlots = listOf("08:00 AM", "10:00 AM", "01:30 PM", "04:30 PM", "06:30 PM")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🗓️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.ODIA) "କାର୍ଯ୍ୟସୂଚୀରେ ସାମିଲ କରନ୍ତୁ" else "Add to Travel Itinerary",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900
                        )
                        Text(
                            text = if (language == AppLanguage.ODIA) item.nameOr else item.nameEn,
                            fontSize = 12.sp,
                            color = OceanBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = BentoSlate700)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Day Selector (Day 1, Day 2, Day 3)
            Text(
                text = if (language == AppLanguage.ODIA) "ଯାତ୍ରା ଦିନ ବାଛନ୍ତୁ:" else "Select Itinerary Day:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoSlate800
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (d in 1..3) {
                    FilterChip(
                        selected = selectedDay == d,
                        onClick = { selectedDay = d },
                        label = {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଦିନ $d" else "Day $d",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Time Slot Selector
            Text(
                text = if (language == AppLanguage.ODIA) "ସମୟ ସ୍ଲଟ୍:" else "Select Time Slot:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoSlate800
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(timeSlots.size) { idx ->
                    val slot = timeSlots[idx]
                    FilterChip(
                        selected = selectedTimeSlot == slot,
                        onClick = { selectedTimeSlot = slot },
                        label = { Text(slot, fontSize = 11.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0F172A),
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Custom Notes Field
            Text(
                text = if (language == AppLanguage.ODIA) "ସ୍ମରଣୀୟ ଟିପ୍ପଣୀ:" else "Personal Notes:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoSlate800
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = customNotes,
                onValueChange = { customNotes = it },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                placeholder = { Text("e.g. Bring camera for sunrise") }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Confirm Button
            Button(
                onClick = { onConfirm(selectedDay, selectedTimeSlot, customNotes) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("confirm_add_itinerary_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (language == AppLanguage.ODIA) "କାର୍ଯ୍ୟସୂଚୀ ସଞ୍ଚୟ କରନ୍ତୁ" else "Save to Itinerary (Room DB)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.5.sp
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}
