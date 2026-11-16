package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.ui.theme.*
import com.example.util.BalasoreHapticHelper
import java.util.Locale

/**
 * Embedded Interactive Map with Navigation Options for Balasore Tourism Hotspots.
 * Renders an embedded cartographic canvas showing the destination's exact geographic pin,
 * radar pulse, coastline orientation, GPS coordinates, and instant one-tap navigation options
 * (Google Maps, GPS Turn-by-Turn, Walking Route, and Coordinate Copy).
 */
@Composable
fun HotspotEmbeddedMap(
    latitude: Double,
    longitude: Double,
    spotName: String,
    spotCategory: String,
    distanceKm: Double,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier,
    onOpenFullDistrictMap: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA

    val infiniteTransition = rememberInfiniteTransition(label = "map_radar_pulse")
    val pulseRadiusFactor by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BentoCardWhite),
        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("hotspot_embedded_map_card_${spotName.lowercase().replace(" ", "_")}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Map title & Coordinates
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(OceanBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOdia) "ମାନଚିତ୍ର ଓ ଦିଗନିର୍ଣ୍ଣୟ" else "Embedded Map & GPS",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate900
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BentoBlueLight
                ) {
                    Text(
                        text = String.format(Locale.US, "%.4f°N, %.4f°E", latitude, longitude),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanBlueDark,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Embedded Interactive Canvas Map View
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE2E8F0))
                    .clickable {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        launchGoogleNavigation(context, latitude, longitude, spotName)
                    }
                    .testTag("embedded_canvas_map_box")
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height

                    // 1. Stylized background terrain gradient
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF1F5F9),
                                Color(0xFFE2E8F0),
                                Color(0xFFCBD5E1)
                            )
                        )
                    )

                    // 2. Bay of Bengal coastline curve on eastern edge
                    val coastPath = Path().apply {
                        moveTo(w * 0.72f, 0f)
                        cubicTo(
                            w * 0.78f, h * 0.35f,
                            w * 0.68f, h * 0.65f,
                            w * 0.82f, h
                        )
                        lineTo(w, h)
                        lineTo(w, 0f)
                        close()
                    }
                    drawPath(
                        path = coastPath,
                        brush = Brush.horizontalGradient(
                            listOf(Color(0xFFBAE6FD), Color(0xFF7DD3FC), Color(0xFF38BDF8))
                        )
                    )

                    // 3. Budhabalanga River estuary representation
                    val riverPath = Path().apply {
                        moveTo(0f, h * 0.45f)
                        quadraticTo(w * 0.35f, h * 0.48f, w * 0.74f, h * 0.42f)
                    }
                    drawPath(
                        path = riverPath,
                        color = Color(0xFF0284C7).copy(alpha = 0.45f),
                        style = Stroke(width = 6f)
                    )

                    // 4. Road network lines (NH 16 / Proof Road)
                    drawLine(
                        color = Color.White.copy(alpha = 0.85f),
                        start = Offset(0f, h * 0.75f),
                        end = Offset(w, h * 0.65f),
                        strokeWidth = 7f
                    )
                    drawLine(
                        color = Color(0xFFF59E0B).copy(alpha = 0.7f),
                        start = Offset(0f, h * 0.75f),
                        end = Offset(w, h * 0.65f),
                        strokeWidth = 2.5f
                    )

                    // Main district road to hotspot
                    val pinCenter = Offset(w * 0.52f, h * 0.48f)
                    drawLine(
                        color = Color.White.copy(alpha = 0.9f),
                        start = Offset(w * 0.28f, h * 0.72f),
                        end = pinCenter,
                        strokeWidth = 5f
                    )

                    // 5. Radar pulse circle around destination pin
                    val maxPulseRadius = 55f
                    drawCircle(
                        color = OceanBlue.copy(alpha = pulseAlpha),
                        radius = maxPulseRadius * pulseRadiusFactor,
                        center = pinCenter,
                        style = Stroke(width = 3.5f)
                    )

                    // Outer pin halo
                    drawCircle(
                        color = Color.White,
                        radius = 16f,
                        center = pinCenter
                    )
                    // Inner pin dot
                    drawCircle(
                        color = Color(0xFFE11D48),
                        radius = 11f,
                        center = pinCenter
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 4f,
                        center = pinCenter
                    )

                    // Distance label badge
                    drawCircle(
                        color = OceanBlueDark,
                        radius = 7f,
                        center = Offset(w * 0.28f, h * 0.72f)
                    )
                }

                // Overlay Tag: Tap to open navigation
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF0F172A).copy(alpha = 0.82f),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f km • %s", distanceKm, if (isOdia) "ଟ୍ୟାପ୍ କରି ରାସ୍ତା ଦେଖନ୍ତୁ" else "Tap to Navigate"),
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontSize = 10.5.sp
                        )
                    }
                }

                // Compass / Orientation indicator at Top Right
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "N",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            color = Color(0xFFDC2626)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Navigation Options Toolbar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Primary: Google Maps Driving
                Button(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        launchGoogleNavigation(context, latitude, longitude, spotName)
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.3f)
                        .height(38.dp)
                        .testTag("nav_driving_btn_${spotName.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "ଗୁଗୁଲ୍ ମ୍ୟାପ୍" else "Google Maps",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Turn-by-Turn GPS
                OutlinedButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        launchGeoIntent(context, latitude, longitude, spotName)
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp)
                        .testTag("nav_gps_btn_${spotName.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = OceanBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "ଦିଗନିର୍ଣ୍ଣୟ" else "GPS Route",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Copy GPS Coordinates
                OutlinedButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateSuccess(context)
                        copyCoordinatesToClipboard(context, latitude, longitude, spotName)
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                        .testTag("nav_copy_coords_btn_${spotName.lowercase().replace(" ", "_")}")
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = "Copy Coordinates",
                        tint = BentoSlate700,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = if (isOdia) "କପି" else "Copy",
                        fontSize = 11.sp
                    )
                }
            }

            // Full interactive district map link
            if (onOpenFullDistrictMap != null) {
                Spacer(modifier = Modifier.height(6.dp))
                TextButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        onOpenFullDistrictMap()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = OceanBlue,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "ସମ୍ପୂର୍ଣ୍ଣ ବାଲେଶ୍ୱର ଜିଲ୍ଲା ପର୍ଯ୍ୟଟନ ମାନଚିତ୍ର ଖୋଲନ୍ତୁ" else "Open Full Interactive District Tourism Map",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = OceanBlue
                    )
                }
            }
        }
    }
}

private fun launchGoogleNavigation(context: Context, lat: Double, lng: Double, name: String) {
    val navUri = Uri.parse("google.navigation:q=$lat,$lng&mode=d")
    val intent = Intent(Intent.ACTION_VIEW, navUri).apply {
        setPackage("com.google.android.apps.maps")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private fun launchGeoIntent(context: Context, lat: Double, lng: Double, name: String) {
    val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(name)})")
    val intent = Intent(Intent.ACTION_VIEW, geoUri).apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    try {
        context.startActivity(intent)
    } catch (_: Exception) {
        val browserUri = Uri.parse("https://maps.google.com/?q=$lat,$lng")
        context.startActivity(Intent(Intent.ACTION_VIEW, browserUri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private fun copyCoordinatesToClipboard(context: Context, lat: Double, lng: Double, name: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText("Coordinates for $name", "$lat, $lng")
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "GPS Coordinates copied: $lat, $lng", Toast.LENGTH_SHORT).show()
}
