package com.example.ui.features.map

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.HotspotEntity
import java.util.Locale

data class HotspotPinPhoto(
    @DrawableRes val drawableRes: Int,
    val caption: String
)

/**
 * Resolves curated offline photography for Balasore tourism hotspot pins so every
 * map pin card and full detail screen renders rich imagery even with unstable coastal signal.
 */
object BalasoreHotspotMediaResolver {

    @DrawableRes
    fun primaryDrawableFor(spotId: String, category: String = "", name: String = ""): Int {
        return galleryFor(spotId, category, name).first().drawableRes
    }

    fun galleryFor(spotId: String, category: String = "", name: String = ""): List<HotspotPinPhoto> {
        val key = "$spotId $category $name".lowercase(Locale.US)
        return when {
            key.contains("chandipur") || key.contains("talasari") || key.contains("beach") || key.contains("balaramgadi") -> listOf(
                HotspotPinPhoto(R.drawable.img_chandipur_beach, "Vanishing Sea Shoreline"),
                HotspotPinPhoto(R.drawable.img_feature_graphic, "Bay of Bengal Horizon"),
                HotspotPinPhoto(R.drawable.img_temple_balasore, "Nearby Coastal Heritage")
            )
            key.contains("remuna") || key.contains("khirachora") || key.contains("emami") || key.contains("temple") || key.contains("chandaneswar") || key.contains("panchalingeswar") -> listOf(
                HotspotPinPhoto(R.drawable.img_temple_balasore, "Sacred Kalinga Shrine"),
                HotspotPinPhoto(R.drawable.img_feature_graphic, "Balasore Spiritual Circuit"),
                HotspotPinPhoto(R.drawable.img_chandipur_beach, "Coastal District View")
            )
            else -> listOf(
                HotspotPinPhoto(R.drawable.img_feature_graphic, "Balasore Scenic Reserve"),
                HotspotPinPhoto(R.drawable.img_chandipur_beach, "Coastal Gateway"),
                HotspotPinPhoto(R.drawable.img_temple_balasore, "District Heritage")
            )
        }
    }
}

/**
 * Rich interactive card displayed when tapping a tourism hotspot pin on the Balasore map.
 * Displays a horizontal photo gallery strip, category & distance pills, short description,
 * visiting metadata, and a prominent "View More Details" button opening the full spot screen.
 */
@Composable
fun TourismHotspotPinCard(
    spot: HotspotEntity,
    isOdia: Boolean,
    onToggleFavorite: () -> Unit,
    onViewMoreDetails: () -> Unit,
    onFocusCallout: () -> Unit,
    onOpenReviews: () -> Unit,
    onDirections: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val pinStyle = TourismPinCategory.resolve(spot.category, spot.name)
    val accent = Color(pinStyle.primaryColor)
    val photos = remember(spot.id, spot.category, spot.name) {
        BalasoreHotspotMediaResolver.galleryFor(spot.id, spot.category, spot.name)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp)
            .testTag("tourism_osm_selected_hotspot_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Swipeable Hotspot Photo Gallery Strip with Overlay Controls
            Box(modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(end = 4.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("tourism_pin_image_gallery")
                ) {
                    itemsIndexed(photos) { idx, photo ->
                        Box(
                            modifier = Modifier
                                .width(if (idx == 0) 232.dp else 148.dp)
                                .height(116.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable(onClick = onViewMoreDetails)
                                .testTag("tourism_pin_photo_$idx")
                        ) {
                            Image(
                                painter = painterResource(id = photo.drawableRes),
                                contentDescription = "${spot.name} — ${photo.caption}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            colors = listOf(
                                                Color.Transparent,
                                                Color.Black.copy(alpha = 0.58f)
                                            )
                                        )
                                    )
                            )
                            Text(
                                text = photo.caption,
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                // Category & Photo Count Badge Top-Left
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = accent
                    ) {
                        Text(
                            text = if (isOdia) pinStyle.labelOr else pinStyle.labelEn,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color.Black.copy(alpha = 0.62f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                            Text(
                                text = "${photos.size}",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Floating Bookmark & Dismiss Buttons Top-Right
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ) {
                        IconButton(
                            onClick = onToggleFavorite,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("tourism_osm_favorite_btn")
                        ) {
                            Icon(
                                imageVector = if (spot.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (spot.isFavorite) "Remove from Saved Hotspots" else "Save Hotspot",
                                tint = if (spot.isFavorite) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f)
                    ) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("tourism_osm_dismiss_card_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Hotspot Card",
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // 2. Title, Odia Subtitle, Distance & Short Description
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOdia && spot.odiaName.isNotBlank()) spot.odiaName else spot.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (spot.distanceKmFromBls > 0) "${spot.distanceKmFromBls} km" else "Town",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = accent
                    )
                }

                if (spot.odiaName.isNotBlank() && !isOdia) {
                    Text(
                        text = spot.odiaName,
                        style = MaterialTheme.typography.labelMedium,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = spot.shortDescription.ifBlank { spot.fullDescription },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 3. Compact Visiting Info Strip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onFocusCallout)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = spot.timings.ifBlank { "6:00 AM - 6:00 PM" },
                            style = MaterialTheme.typography.labelSmall,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = accent.copy(alpha = 0.12f),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${spot.bestTimeToVisit.ifBlank { "Oct - Mar" }} • ${spot.entryFee.ifBlank { "Free" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = accent,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    )
                }
            }

            // 4. Primary "View More Details" CTA + Directions + Reviews
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onViewMoreDetails,
                    colors = ButtonDefaults.buttonColors(containerColor = accent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1.25f)
                        .height(48.dp)
                        .testTag("tourism_map_view_details_btn")
                ) {
                    Text(
                        text = if (isOdia) "ଅଧିକ ବିବରଣୀ ଦେଖନ୍ତୁ" else stringResource(R.string.tourism_pin_view_more_details),
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.width(5.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                }

                OutlinedButton(
                    onClick = onDirections,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.55f)),
                    contentPadding = PaddingValues(horizontal = 10.dp),
                    modifier = Modifier
                        .weight(0.85f)
                        .height(48.dp)
                        .testTag("tourism_osm_directions_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.osm_tourism_directions),
                        fontSize = 12.sp,
                        color = accent,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = onOpenReviews,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("tourism_osm_reviews_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = stringResource(R.string.osm_tourism_reviews),
                        tint = accent
                    )
                }
            }
        }
    }
}
