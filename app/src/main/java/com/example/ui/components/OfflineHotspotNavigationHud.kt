package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WifiOff
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.maps.OfflineHotspotRoute
import com.example.data.maps.OfflineNavMode
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import org.osmdroid.util.GeoPoint

/**
 * Top & Bottom HUD for Offline Vector Navigation over OSMDroid.
 * Delivers turn-by-turn routing, transit mode switching, and quick hotspot launcher
 * when mobile internet is unavailable.
 */
@Composable
fun OfflineHotspotNavigationHud(
    activeRoute: OfflineHotspotRoute?,
    language: AppLanguage,
    currentStepIndex: Int,
    onStepChange: (Int) -> Unit,
    onFocusPoint: (GeoPoint) -> Unit,
    onExitNavigation: () -> Unit,
    onSelectHotspot: (TouristLocationItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedNavMode by remember { mutableStateOf(OfflineNavMode.CAR) }

    Box(modifier = modifier) {
        if (activeRoute != null) {
            val stepCount = activeRoute.steps.size
            val safeStepIndex = currentStepIndex.coerceIn(0, (stepCount - 1).coerceAtLeast(0))
            val currentStep = activeRoute.steps.getOrNull(safeStepIndex)

            // Top Turn-by-Turn Maneuver Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter)
                    .testTag("offline_nav_turn_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // Midnight slate
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
                border = BorderStroke(1.5.dp, Color(0xFF0284C7))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Row 1: Maneuver Icon + Step Instruction + Close Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFF0284C7).copy(alpha = 0.25f),
                            modifier = Modifier.size(46.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = currentStep?.maneuverIcon ?: "➡️",
                                    fontSize = 24.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଅଫଲାଇନ୍ ନେଭିଗେସନ୍" else "OFFLINE VECTOR NAVIGATION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF38BDF8),
                                    letterSpacing = 0.5.sp
                                )
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreen)
                                )
                            }
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    currentStep?.instructionOr ?: activeRoute.destinationNameOr
                                } else {
                                    currentStep?.instructionEn ?: activeRoute.destinationNameEn
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (currentStep != null && currentStep.distanceMeters > 0) {
                                val distText = if (currentStep.distanceMeters >= 1000) {
                                    String.format("%.1f km", currentStep.distanceMeters / 1000.0)
                                } else {
                                    "${currentStep.distanceMeters} m"
                                }
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଆଗାମୀ ମୋଡ଼: $distText ରେ" else "In $distText",
                                    fontSize = 11.5.sp,
                                    color = Color(0xFFCBD5E1)
                                )
                            }
                        }

                        IconButton(
                            onClick = onExitNavigation,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("exit_offline_nav_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Exit Navigation",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Distance & ETA Metrics + Mode Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val etaMinutes = when (selectedNavMode) {
                            OfflineNavMode.CAR -> activeRoute.estimatedMinutesCar
                            OfflineNavMode.AUTO -> activeRoute.estimatedMinutesAuto
                            OfflineNavMode.WALK -> activeRoute.estimatedMinutesWalk
                        }

                        Column {
                            Row(verticalAlignment = Alignment.Bottom) {
                                Text(
                                    text = "$etaMinutes min",
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = EmeraldGreen
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = "(${String.format("%.1f", activeRoute.totalDistanceKm)} km)",
                                    fontSize = 13.sp,
                                    color = Color(0xFF94A3B8),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "Bearing: ${activeRoute.bearingDegrees.toInt()}° • Zero Data Needed",
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                        }

                        // Mode toggle pills
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            OfflineNavMode.entries.forEach { mode ->
                                val isSelected = selectedNavMode == mode
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                                    modifier = Modifier
                                        .clickable { selectedNavMode = mode }
                                        .padding(2.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = mode.emoji, fontSize = 13.sp)
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: Step Navigation Controls (Prev, Next, Recenter Step)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (safeStepIndex > 0) {
                                        val newIdx = safeStepIndex - 1
                                        onStepChange(newIdx)
                                        activeRoute.steps.getOrNull(newIdx)?.let { onFocusPoint(it.point) }
                                    }
                                },
                                enabled = safeStepIndex > 0,
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, Color(0xFF334155))
                            ) {
                                Icon(imageVector = Icons.Default.FastRewind, contentDescription = "Previous", tint = Color.White, modifier = Modifier.size(14.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(text = "Prev", fontSize = 11.sp, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    if (safeStepIndex < stepCount - 1) {
                                        val newIdx = safeStepIndex + 1
                                        onStepChange(newIdx)
                                        activeRoute.steps.getOrNull(newIdx)?.let { onFocusPoint(it.point) }
                                    } else {
                                        Toast.makeText(context, "You have reached ${activeRoute.destinationNameEn}!", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                            ) {
                                Text(
                                    text = if (safeStepIndex < stepCount - 1) "Next Step" else "Arrived",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(Modifier.width(4.dp))
                                Icon(imageVector = Icons.Default.FastForward, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }

                        Text(
                            text = "Step ${safeStepIndex + 1} of $stepCount",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        } else {
            // Quick Hotspot Route Launcher Tray (Bottom of Map)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 12.dp)
            ) {
                // Offline vector guarantee badge
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(bottom = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A).copy(alpha = 0.92f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.WifiOff,
                        contentDescription = null,
                        tint = EmeraldGreen,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଅଫଲାଇନ୍ ଭେକ୍ଟର ମ୍ୟାପ୍ ଓ ନେଭିଗେସନ୍ ସକ୍ରିୟ" else "Offline Vector Map & Navigation Active",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                // Horizontal Hotspot Carousel for 1-Tap Offline Route Guidance
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(BALASORE_TOURIST_LOCATIONS) { spot ->
                        Card(
                            modifier = Modifier
                                .clickable { onSelectHotspot(spot) }
                                .testTag("quick_nav_spot_${spot.id}"),
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = spot.highlightEmoji, fontSize = 18.sp)
                                Spacer(Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) spot.nameOr else spot.nameEn.substringBefore("(").trim(),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlate900,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Navigation,
                                            contentDescription = null,
                                            tint = OceanBlue,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(Modifier.width(2.dp))
                                        Text(
                                            text = "${spot.baseDistanceKm} km • Navigate",
                                            fontSize = 10.sp,
                                            color = OceanBlue,
                                            fontWeight = FontWeight.SemiBold
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
}
