package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.maps.BalasoreOfflineMapManager
import com.example.data.maps.BalasoreOfflineTileProvider
import com.example.data.maps.BalasoreOsmOfflineTileProvider
import com.example.data.maps.MapCacheManager
import com.example.data.model.AppLanguage
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import kotlinx.coroutines.launch

/**
 * Interactive Dashboard UI Component for the Offline Vector & Raster Map Provider.
 * Allows users to:
 * 1. Toggle visibility of the map layer/preview.
 * 2. Switch between Online mode (CDN tile streaming) and Offline mode (zero-network synthesized vector raster tiles).
 * 3. Toggle High-Contrast Disaster Corridors mode.
 * 4. Inspect real-time storage metrics, pre-cached tiles count, and launch the full offline map viewer.
 */
@Composable
fun OfflineVectorRasterMapDashboardCard(
    language: AppLanguage,
    modifier: Modifier = Modifier,
    onOpenFullMap: () -> Unit = {},
    initialIsVisible: Boolean = true,
    initialIsOffline: Boolean = false
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val coroutineScope = rememberCoroutineScope()

    // Map Manager singletons
    val mapManager = remember { BalasoreOfflineMapManager.getInstance(context) }
    val cacheManager = remember { MapCacheManager.getInstance(context) }

    // Component States
    var isMapVisible by remember { mutableStateOf(initialIsVisible) }
    var isOfflineModeActive by remember { mutableStateOf(initialIsOffline) }
    var isHighContrastEmergency by remember { mutableStateOf(false) }
    var showSheltersLayer by remember { mutableStateOf(true) }
    var showCoastlineRecessionLayer by remember { mutableStateOf(true) }
    var showRoadCorridorsLayer by remember { mutableStateOf(true) }

    // Telemetry
    val totalStorageMb by remember {
        derivedStateOf { mapManager.getTotalStorageUsedMb() }
    }
    val totalTilesCount by remember {
        derivedStateOf { mapManager.getTotalDownloadedTiles() }
    }

    // Vibration helper
    val triggerHaptic = remember {
        {
            try {
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (vibrator != null && vibrator.hasVibrator()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(30)
                    }
                }
            } catch (_: Exception) {}
        }
    }

    // Card background color and border based on online/offline state
    val cardBackground = if (isOfflineModeActive) {
        Color(0xFF0F172A) // Slate 900 dark theme for offline disaster resiliency
    } else {
        MaterialTheme.colorScheme.surface
    }

    val cardBorderColor = if (isOfflineModeActive) {
        Color(0xFF10B981) // Emerald Green highlight for offline safety
    } else {
        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_vector_raster_map_dashboard_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground),
        border = BorderStroke(1.5.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ==========================================
            // HEADER: TITLE, BADGE & VISIBILITY TOGGLE
            // ==========================================
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isOfflineModeActive) Color(0xFF10B981).copy(alpha = 0.2f) else OceanBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isOfflineModeActive) Icons.Default.Layers else Icons.Default.Map,
                                contentDescription = "Offline Vector Raster Map Provider",
                                tint = if (isOfflineModeActive) Color(0xFF34D399) else OceanBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = if (isOdia) "ଅଫଲାଇନ୍ ଭେକ୍ଟର ରାଷ୍ଟର ମ୍ୟାପ୍" else "Vector Raster Map Provider",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp
                                ),
                                color = if (isOfflineModeActive) Color.White else MaterialTheme.colorScheme.onSurface
                            )

                            // Status Pill
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isOfflineModeActive) Color(0xFF065F46) else Color(0xFFDBEAFE)
                            ) {
                                Text(
                                    text = if (isOfflineModeActive) "OFFLINE RASTER" else "ONLINE CDN",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isOfflineModeActive) Color(0xFF6EE7B7) else Color(0xFF1E40AF),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isOdia)
                                "ସିନ୍ଥେଟିକ୍ ରାଷ୍ଟର ଟାଇଲ୍ସ • ଶୂନ୍ୟ ନେଟୱାର୍କ ନାଭିଗେସନ୍"
                            else
                                "On-the-fly vector raster synthesis • Zero network resilience",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = if (isOfflineModeActive) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                // Eye Toggle: Visibility Switch for Map Component
                IconButton(
                    onClick = {
                        triggerHaptic()
                        isMapVisible = !isMapVisible
                        Toast.makeText(
                            context,
                            if (isMapVisible) "Map provider visible" else "Map provider hidden",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.testTag("toggle_map_provider_visibility")
                ) {
                    Icon(
                        imageVector = if (isMapVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (isMapVisible) "Hide Map Provider" else "Show Map Provider",
                        tint = if (isMapVisible) (if (isOfflineModeActive) Color(0xFF34D399) else OceanBlue) else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // ==========================================
            // ONLINE / OFFLINE MODE SELECTOR (SEGMENTED)
            // ==========================================
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isOfflineModeActive) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, if (isOfflineModeActive) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_map_online_offline_mode")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Online Mode Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (!isOfflineModeActive) OceanBlue else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (isOfflineModeActive) {
                                    triggerHaptic()
                                    isOfflineModeActive = false
                                    Toast.makeText(context, "Switched to Online CDN Map Mode", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("btn_mode_online")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = if (!isOfflineModeActive) Color.White else (if (isOfflineModeActive) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) "ଅନଲାଇନ୍ ମୋଡ୍" else "Online Mode",
                                fontSize = 12.sp,
                                fontWeight = if (!isOfflineModeActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (!isOfflineModeActive) Color.White else (if (isOfflineModeActive) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant)
                            )
                        }
                    }

                    // Offline Mode Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isOfflineModeActive) Color(0xFF10B981) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (!isOfflineModeActive) {
                                    triggerHaptic()
                                    isOfflineModeActive = true
                                    Toast.makeText(context, "Switched to Autonomous Offline Vector Raster Mode", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("btn_mode_offline")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = if (isOfflineModeActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) "ଅଫଲାଇନ୍ ମୋଡ୍" else "Offline Mode",
                                fontSize = 12.sp,
                                fontWeight = if (isOfflineModeActive) FontWeight.Bold else FontWeight.Medium,
                                color = if (isOfflineModeActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // ==========================================
            // ANIMATED BODY: PREVIEW CANVAS & CONTROLS
            // ==========================================
            AnimatedVisibility(
                visible = isMapVisible,
                enter = expandVertically(animationSpec = tween(280, easing = FastOutSlowInEasing)) + fadeIn(),
                exit = shrinkVertically(animationSpec = tween(240, easing = FastOutSlowInEasing)) + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {

                    // Live Interactive Mini-Canvas Raster Map Preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isOfflineModeActive) (if (isHighContrastEmergency) Color.Black else Color(0xFF0F172A)) else Color(0xFFE2E8F0))
                            .border(
                                1.dp,
                                if (isOfflineModeActive) (if (isHighContrastEmergency) Color(0xFFFACC15) else Color(0xFF334155)) else Color(0xFFCBD5E1),
                                RoundedCornerShape(16.dp)
                            )
                            .clickable {
                                triggerHaptic()
                                onOpenFullMap()
                            }
                            .testTag("offline_vector_raster_canvas_preview")
                    ) {
                        // Drawing custom Balasore geographic features
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawBalasoreVectorRasterMap(
                                isOffline = isOfflineModeActive,
                                isHighContrast = isHighContrastEmergency,
                                showShelters = showSheltersLayer,
                                showCoastline = showCoastlineRecessionLayer,
                                showRoads = showRoadCorridorsLayer
                            )
                        }

                        // Top-left Engine Tag
                        Surface(
                            shape = RoundedCornerShape(bottomEnd = 10.dp),
                            color = (if (isOfflineModeActive) Color(0xFF10B981) else OceanBlue).copy(alpha = 0.9f),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = if (isOfflineModeActive)
                                    (if (isHighContrastEmergency) "EMERGENCY HIGH-CONTRAST RASTER" else "BALASORE SYNTHETIC RASTER (Z12-16)")
                                else
                                    "DYNAMIC VECTOR TILE STREAM",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Top-right Coordinates & Zoom Telemetry
                        Surface(
                            shape = RoundedCornerShape(bottomStart = 10.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier.align(Alignment.TopEnd)
                        ) {
                            Text(
                                text = "21.49° N, 86.93° E • Z14",
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        // Bottom-left Click-to-Expand Prompt
                        Surface(
                            shape = RoundedCornerShape(topEnd = 10.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier.align(Alignment.BottomStart)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Icon(Icons.Default.Fullscreen, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOdia) "ବିସ୍ତୃତ ମାନଚିତ୍ର ଖୋଲନ୍ତୁ ↗" else "Tap for Full Screen Map ↗",
                                    fontSize = 9.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Bottom-right Legend / Scale
                        Surface(
                            shape = RoundedCornerShape(topStart = 10.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            modifier = Modifier.align(Alignment.BottomEnd)
                        ) {
                            Text(
                                text = "Scale: 1:25,000",
                                fontSize = 8.5.sp,
                                color = Color(0xFFCBD5E1),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // ==========================================
                    // LAYER VISIBILITY & RASTER TOGGLES
                    // ==========================================
                    Text(
                        text = if (isOdia) "ମ୍ୟାପ୍ ଲେୟାର୍ ଓ ଭେକ୍ଟର ବିକଳ୍ପ" else "LAYER VISIBILITY & RASTER FILTERS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = if (isOfflineModeActive) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Layer Toggles Row (Chips)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cyclone Shelters Layer
                        FilterChip(
                            selected = showSheltersLayer,
                            onClick = {
                                triggerHaptic()
                                showSheltersLayer = !showSheltersLayer
                            },
                            label = { Text("Shelters", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = if (showSheltersLayer) Color(0xFFEF4444) else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("chip_layer_shelters")
                        )

                        // Coastline / Tidal Channel
                        FilterChip(
                            selected = showCoastlineRecessionLayer,
                            onClick = {
                                triggerHaptic()
                                showCoastlineRecessionLayer = !showCoastlineRecessionLayer
                            },
                            label = { Text("Coastline", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = if (showCoastlineRecessionLayer) Color(0xFF0284C7) else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("chip_layer_coastline")
                        )

                        // Road Corridors
                        FilterChip(
                            selected = showRoadCorridorsLayer,
                            onClick = {
                                triggerHaptic()
                                showRoadCorridorsLayer = !showRoadCorridorsLayer
                            },
                            label = { Text("Roads", fontSize = 11.sp) },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.AltRoute,
                                    contentDescription = null,
                                    tint = if (showRoadCorridorsLayer) Color(0xFFF59E0B) else Color.Gray,
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            modifier = Modifier.weight(1f).testTag("chip_layer_roads")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Emergency High-Contrast Switch
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOfflineModeActive) Color(0xFF1E293B) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, if (isOfflineModeActive) Color(0xFF334155) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Contrast,
                                    contentDescription = null,
                                    tint = if (isHighContrastEmergency) Color(0xFFFACC15) else (if (isOfflineModeActive) Color(0xFF94A3B8) else Color.Gray),
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isOdia) "ଜରୁରୀକାଳୀନ ଉଚ୍ଚ-କଣ୍ଟ୍ରାଷ୍ଟ ଭେକ୍ଟର ମୋଡ୍" else "Disaster High-Contrast Vector Mode",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = if (isOfflineModeActive) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (isOdia) "ବିଜୁଳି ବିଭ୍ରାଟ ସମୟରେ ତୀବ୍ର ଦୃଶ୍ୟମାନତା" else "Maximum readability during blackout evacuation",
                                        fontSize = 10.sp,
                                        color = if (isOfflineModeActive) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isHighContrastEmergency,
                                onCheckedChange = {
                                    triggerHaptic()
                                    isHighContrastEmergency = it
                                },
                                modifier = Modifier.testTag("switch_high_contrast_vector")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // ==========================================
                    // TELEMETRY STRIP & LAUNCH BUTTON
                    // ==========================================
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = if (isOfflineModeActive) Color(0xFF34D399) else OceanBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Tiles: $totalTilesCount • ${"%.1f".format(totalStorageMb)} MB cached",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isOfflineModeActive) Color(0xFFCBD5E1) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                triggerHaptic()
                                onOpenFullMap()
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isOfflineModeActive) Color(0xFF10B981) else OceanBlue
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("btn_open_full_offline_map")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Launch,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) "ମ୍ୟାପ୍ ଖୋଲନ୍ତୁ" else "Open Map",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Custom Canvas drawing function that synthesizes high-fidelity vector raster map elements:
 * - Coastline and Bay of Bengal tidal waters
 * - Budhabalanga river estuary
 * - NH-16 and OT Road corridors
 * - Multipurpose Cyclone Shelter markers with safe evacuation radiates
 */
private fun DrawScope.drawBalasoreVectorRasterMap(
    isOffline: Boolean,
    isHighContrast: Boolean,
    showShelters: Boolean,
    showCoastline: Boolean,
    showRoads: Boolean
) {
    val w = size.width
    val h = size.height

    // Background color
    val landColor = when {
        isHighContrast -> Color(0xFF111827)
        isOffline -> Color(0xFF1E293B)
        else -> Color(0xFFF1F5F9)
    }
    drawRect(color = landColor)

    // 1. Coastline & Bay of Bengal (right / east quadrant)
    if (showCoastline) {
        val waterColor = when {
            isHighContrast -> Color(0xFF0284C7)
            isOffline -> Color(0xFF0369A1)
            else -> Color(0xFFBAE6FD)
        }

        val seaPath = Path().apply {
            moveTo(w * 0.72f, 0f)
            cubicTo(
                w * 0.68f, h * 0.35f,
                w * 0.78f, h * 0.65f,
                w * 0.62f, h
            )
            lineTo(w, h)
            lineTo(w, 0f)
            close()
        }
        drawPath(path = seaPath, color = waterColor)

        // Chandipur Vanishing Sea Mudflat receding belt
        val mudflatColor = when {
            isHighContrast -> Color(0xFF0EA5E9).copy(alpha = 0.45f)
            isOffline -> Color(0xFF0284C7).copy(alpha = 0.35f)
            else -> Color(0xFFE0F2FE)
        }
        val mudflatPath = Path().apply {
            moveTo(w * 0.62f, h * 0.30f)
            cubicTo(
                w * 0.58f, h * 0.50f,
                w * 0.66f, h * 0.75f,
                w * 0.55f, h
            )
            lineTo(w * 0.62f, h)
            cubicTo(
                w * 0.78f, h * 0.65f,
                w * 0.68f, h * 0.35f,
                w * 0.72f, 0f
            )
            lineTo(w * 0.66f, 0f)
            close()
        }
        drawPath(path = mudflatPath, color = mudflatColor)

        // Budhabalanga River Estuary
        val riverColor = if (isHighContrast) Color(0xFF38BDF8) else Color(0xFF0284C7)
        val riverPath = Path().apply {
            moveTo(0f, h * 0.48f)
            cubicTo(
                w * 0.25f, h * 0.45f,
                w * 0.45f, h * 0.60f,
                w * 0.68f, h * 0.52f
            )
        }
        drawPath(
            path = riverPath,
            color = riverColor,
            style = Stroke(width = if (isHighContrast) 5f else 3.5f, cap = StrokeCap.Round)
        )
    }

    // 2. Trunk Road Corridors (NH-16 & OT Road)
    if (showRoads) {
        val nh16Color = if (isHighContrast) Color(0xFFFACC15) else Color(0xFFF59E0B)
        val otRoadColor = if (isHighContrast) Color(0xFFE2E8F0) else Color(0xFF94A3B8)

        // NH-16 (Highway from Southwest to Northeast)
        drawLine(
            color = nh16Color,
            start = Offset(0f, h * 0.85f),
            end = Offset(w * 0.65f, 0f),
            strokeWidth = if (isHighContrast) 6f else 4f,
            cap = StrokeCap.Round
        )

        // OT Road Trunk (Connecting Town to Chandipur)
        drawLine(
            color = otRoadColor,
            start = Offset(w * 0.32f, h * 0.48f),
            end = Offset(w * 0.72f, h * 0.42f),
            strokeWidth = if (isHighContrast) 4.5f else 3f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 6f)),
            cap = StrokeCap.Round
        )

        // Remuna Heritage Link
        drawLine(
            color = otRoadColor.copy(alpha = 0.8f),
            start = Offset(w * 0.32f, h * 0.48f),
            end = Offset(w * 0.18f, h * 0.22f),
            strokeWidth = 2.5f,
            cap = StrokeCap.Round
        )
    }

    // 3. Cyclone Shelter & Landmark Radar Blips
    if (showShelters) {
        val shelterBlips = listOf(
            Offset(w * 0.68f, h * 0.40f), // Chandipur Coastal MCS
            Offset(w * 0.64f, h * 0.75f), // Balaramgadi Port MCS
            Offset(w * 0.32f, h * 0.48f), // Balasore Central Core
            Offset(w * 0.18f, h * 0.22f)  // Remuna High School MCS
        )

        for (blip in shelterBlips) {
            // Radar pulse ring
            drawCircle(
                color = if (isHighContrast) Color(0xFFEF4444).copy(alpha = 0.35f) else Color(0xFFEF4444).copy(alpha = 0.2f),
                radius = 16f,
                center = blip
            )
            // Pin marker center
            drawCircle(
                color = Color(0xFFEF4444),
                radius = 5.5f,
                center = blip
            )
            drawCircle(
                color = Color.White,
                radius = 2f,
                center = blip
            )
        }
    }
}
