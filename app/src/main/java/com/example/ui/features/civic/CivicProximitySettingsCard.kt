package com.example.ui.features.civic

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.notification.CivicProximityAlertManager
import com.example.util.BalasoreHapticHelper
import kotlinx.coroutines.launch

/**
 * Material 3 Settings card allowing users to toggle proximity-based notifications for nearby
 * 'Civic Eye' incidents, pick an alert radius (500m, 1 km, 2 km, 5 km), and trigger an immediate
 * neighborhood check with runtime location/notification permission handling.
 */
@Composable
fun CivicProximitySettingsCard(
    isEnabled: Boolean,
    radiusMeters: Int,
    language: AppLanguage,
    onToggleEnabled: (Boolean) -> Unit,
    onRadiusSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA

    var isScanning by remember { mutableStateOf(false) }
    var lastScanSummary by remember { mutableStateOf<String?>(null) }
    var permissionDeniedNote by remember { mutableStateOf<String?>(null) }

    val requiredPermissions = remember {
        buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.ACCESS_COARSE_LOCATION)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val locationGranted =
            (results[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (results[Manifest.permission.ACCESS_COARSE_LOCATION] == true) ||
                CivicProximityAlertManager.hasLocationPermission(context)

        if (locationGranted) {
            permissionDeniedNote = null
            onToggleEnabled(true)
            Toast.makeText(
                context,
                if (isOdia) {
                    "ନିକଟସ୍ଥ ସିଭିକ୍ ଆଇ ଆଲର୍ଟ ସକ୍ରିୟ ହେଲା (${CivicProximityAlertManager.formatRadiusLabel(radiusMeters)})"
                } else {
                    "Nearby Civic Eye alerts enabled (${CivicProximityAlertManager.formatRadiusLabel(radiusMeters)} radius)"
                },
                Toast.LENGTH_SHORT
            ).show()
        } else {
            permissionDeniedNote = context.getString(R.string.civic_proximity_permission_needed)
            onToggleEnabled(false)
        }
    }

    val handleToggle: (Boolean) -> Unit = { desired ->
        BalasoreHapticHelper.vibrateButtonClick(context)
        if (desired) {
            if (CivicProximityAlertManager.hasLocationPermission(context)) {
                permissionDeniedNote = null
                onToggleEnabled(true)
            } else {
                permissionLauncher.launch(requiredPermissions)
            }
        } else {
            permissionDeniedNote = null
            lastScanSummary = null
            onToggleEnabled(false)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("settings_civic_proximity_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isEnabled) {
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
            }
        ),
        border = BorderStroke(
            1.dp,
            if (isEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 10.dp),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (isEnabled) Icons.Default.NotificationsActive else Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.civic_proximity_title),
                        tint = if (isEnabled) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(20.dp)
                    )
                    Column {
                        Text(
                            text = if (isOdia) {
                                "ନିକଟସ୍ଥ ସିଭିକ୍ ଆଇ ପ୍ରକ୍ସିମିଟି ଆଲର୍ଟ"
                            } else {
                                stringResource(R.string.civic_proximity_title)
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isOdia) {
                                "ରିପୋର୍ଟ ହୋଇଥିବା ରାସ୍ତା ଖାଲ, ନାଳ କିମ୍ବା ଆବର୍ଜନା ଅଞ୍ଚଳରେ ପ୍ରବେଶ କଲେ ତୁରନ୍ତ ସତର୍କତା ପାଆନ୍ତୁ।"
                            } else {
                                stringResource(R.string.civic_proximity_subtitle)
                            },
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Switch(
                    checked = isEnabled,
                    onCheckedChange = handleToggle,
                    modifier = Modifier.testTag("settings_civic_proximity_switch")
                )
            }

            if (!permissionDeniedNote.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("settings_civic_proximity_permission_error")
                ) {
                    Text(
                        text = permissionDeniedNote.orEmpty(),
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isEnabled) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (isOdia) {
                            "ସତର୍କତା ଦୂରତା (Neighborhood Radius)"
                        } else {
                            stringResource(R.string.civic_proximity_radius_label)
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CivicProximityAlertManager.AVAILABLE_RADII_METERS.forEach { radius ->
                            val selected = radiusMeters == radius
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    onRadiusSelected(radius)
                                },
                                label = {
                                    Text(
                                        text = CivicProximityAlertManager.formatRadiusLabel(radius),
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("settings_civic_proximity_radius_$radius")
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            if (!CivicProximityAlertManager.hasLocationPermission(context)) {
                                permissionLauncher.launch(requiredPermissions)
                                return@OutlinedButton
                            }
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            coroutineScope.launch {
                                isScanning = true
                                val scan = CivicProximityAlertManager.scanNearbyIncidentsNow(
                                    context = context,
                                    forceNotify = true
                                )
                                isScanning = false
                                lastScanSummary = if (scan.nearbyIncidents.isNotEmpty()) {
                                    val closest = scan.nearbyIncidents.first()
                                    val dist = CivicProximityAlertManager.formatDistanceLabel(closest.distanceMeters)
                                    "${scan.nearbyIncidents.size} active issue(s) within ${CivicProximityAlertManager.formatRadiusLabel(scan.radiusMeters)} • Closest: ${closest.category} ($dist)"
                                } else {
                                    "No open Civic Eye issues within ${CivicProximityAlertManager.formatRadiusLabel(scan.radiusMeters)} of your location."
                                }
                            }
                        },
                        enabled = !isScanning,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 48.dp)
                            .testTag("settings_civic_proximity_scan_now_btn")
                    ) {
                        if (isScanning) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.civic_proximity_scanning),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = stringResource(R.string.civic_proximity_scan_now),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) {
                                    "ବର୍ତ୍ତମାନ ନିକଟସ୍ଥ ସମସ୍ୟା ଯାଞ୍ଚ କରନ୍ତୁ"
                                } else {
                                    stringResource(R.string.civic_proximity_scan_now)
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (!lastScanSummary.isNullOrBlank()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_civic_proximity_scan_result")
                        ) {
                            Text(
                                text = lastScanSummary.orEmpty(),
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
