package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AppLanguage
import com.example.data.notification.ChandipurLocationTideHazardManager
import com.example.data.notification.ChandipurLocationTideHazardManager.HazardLevel
import com.example.data.notification.ChandipurLocationTideHazardManager.UserZone
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import kotlinx.coroutines.launch

/**
 * Interactive UI Component providing location-based proximity safety warnings
 * for Chandipur Beach during dangerous high tide periods.
 */
@Composable
fun ChandipurLocationTideHazardCard(
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val coroutineScope = rememberCoroutineScope()

    var isAlertsEnabled by remember {
        mutableStateOf(ChandipurLocationTideHazardManager.isProximityAlertsEnabled(context))
    }
    var selectedRadiusKm by remember {
        mutableStateOf(ChandipurLocationTideHazardManager.getProximityRadiusKm(context))
    }
    var hazardStatus by remember {
        mutableStateOf(ChandipurLocationTideHazardManager.evaluateLocationTideHazard(context))
    }
    var isSimulatingBeachProximity by remember { mutableStateOf(false) }

    // Location Permission launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            Toast.makeText(context, if (isOdia) "ସ୍ଥାନ ଅନୁମତି ମିଳିଲା! ସ୍ଥିତି ଯାଞ୍ଚ ହେଉଛି..." else "Location granted! Evaluating proximity...", Toast.LENGTH_SHORT).show()
            hazardStatus = ChandipurLocationTideHazardManager.evaluateLocationTideHazard(context)
        } else {
            Toast.makeText(context, if (isOdia) "ଚାନ୍ଦିପୁର ଜୁଆର ଚେତାବନୀ ପାଇଁ ସ୍ଥାନ ଅନୁମତି ଆବଶ୍ୟକ" else "Location required for Chandipur proximity alerts", Toast.LENGTH_LONG).show()
        }
    }

    // High tide pulse transition for DANGER mode
    val infiniteTransition = rememberInfiniteTransition(label = "HazardPulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseAlpha"
    )

    val badgeColor = when (hazardStatus.hazardLevel) {
        HazardLevel.DANGER -> Color(0xFFEF4444)
        HazardLevel.CAUTION -> Color(0xFFF59E0B)
        HazardLevel.SAFE -> EmeraldGreen
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("chandipur_location_hazard_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (hazardStatus.hazardLevel == HazardLevel.DANGER) {
                Color(0xFFFEF2F2)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            width = if (hazardStatus.hazardLevel == HazardLevel.DANGER) 2.dp else 1.dp,
            color = if (hazardStatus.hazardLevel == HazardLevel.DANGER) {
                Color(0xFFEF4444).copy(alpha = pulseAlpha)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (hazardStatus.hazardLevel == HazardLevel.DANGER) {
                            Color(0xFFEF4444).copy(alpha = 0.2f)
                        } else {
                            OceanBlue.copy(alpha = 0.15f)
                        },
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (hazardStatus.hazardLevel == HazardLevel.DANGER) Icons.Default.Warning else Icons.Default.MyLocation,
                                contentDescription = "Location Hazard Icon",
                                tint = if (hazardStatus.hazardLevel == HazardLevel.DANGER) Color(0xFFDC2626) else OceanBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = if (isOdia) "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ସ୍ଥାନୀୟ ଜୁଆର ସତର୍କତା" else "Chandipur Beach Proximity Radar",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = if (isOdia) "ଜିପିଏସ ଭିତ୍ତିକ ଉଚ୍ଚ ଜୁଆର ବିପଦ ସୂଚକ" else "GPS-guided high tide coastal safety alerts",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Live status pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = badgeColor,
                            modifier = Modifier.size(8.dp)
                        ) {}
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOdia) hazardStatus.hazardLevel.titleOd else hazardStatus.hazardLevel.titleEn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = badgeColor
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Main Status Display Banner
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (hazardStatus.hazardLevel == HazardLevel.DANGER) {
                    Color(0xFFFEE2E2)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                },
                border = BorderStroke(
                    1.dp,
                    if (hazardStatus.hazardLevel == HazardLevel.DANGER) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "ବର୍ତ୍ତମାନର ସ୍ଥାନ ଓ ଦୂରତା:" else "Current Shoreline Proximity:",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format(java.util.Locale.US, "%.1f", hazardStatus.distanceKm)} km from Chandipur Beach",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hazardStatus.distanceKm <= 5.0) Color(0xFFDC2626) else MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = if (isOdia) hazardStatus.odiaAdvisoryMessage else hazardStatus.advisoryMessage,
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        fontWeight = if (hazardStatus.hazardLevel == HazardLevel.DANGER) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (hazardStatus.hazardLevel == HazardLevel.DANGER) Color(0xFF991B1B) else MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tidal Telemetry Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Stat 1: Next High Tide
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = if (isOdia) "ଜୁଆର ଆସିବାକୁ ବାକି" else "Next High Tide",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${hazardStatus.minutesUntilPeakHighTide} mins",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        // Stat 2: Surge Wave Velocity
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = if (isOdia) "ଜୁଆର ବେଗ" else "Surge Velocity",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${hazardStatus.surgeSpeedMs} m/s",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFEA580C)
                                )
                            }
                        }

                        // Stat 3: User Zone
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(
                                    text = if (isOdia) "ଜୋନ" else "Current Zone",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = if (isOdia) hazardStatus.userZone.labelOd else hazardStatus.userZone.labelEn,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Proximity Alert Toggle Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) "ସ୍ୱୟଂକ୍ରିୟ ଜିପିଏସ ସତର୍କତା ବଟନ" else "GPS Proximity Safety Alerts",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isOdia) "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ପାଖରେ ଥିବା ବେଳେ ଜୁଆର ମାଡ଼ି ଆସିଲେ ଚେତାବନୀ ଦିଏ" else "Sends high-priority siren & notification when near beach during high tide",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = isAlertsEnabled,
                    onCheckedChange = { enabled ->
                        isAlertsEnabled = enabled
                        ChandipurLocationTideHazardManager.setProximityAlertsEnabled(context, enabled)
                        if (enabled) {
                            // Check permission
                            val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            if (!fineGranted) {
                                locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                            }
                        }
                    },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = OceanBlue
                    ),
                    modifier = Modifier.testTag("proximity_alert_switch")
                )
            }

            // Hazard Radius Threshold Selector
            AnimatedVisibility(visible = isAlertsEnabled) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = if (isOdia) "ବିପଦ ସତର୍କତା ଦୂରତା ସୀମା (କିଲୋମିଟର):" else "Proximity Alert Radius:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(2.0 to "2 km (Seabed only)", 5.0 to "5 km (Beach & Town)", 10.0 to "10 km (Wider Coastal)").forEach { (radius, label) ->
                            val isSelected = selectedRadiusKm == radius
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.clickable {
                                    selectedRadiusKm = radius
                                    ChandipurLocationTideHazardManager.setProximityRadiusKm(context, radius)
                                    hazardStatus = ChandipurLocationTideHazardManager.evaluateLocationTideHazard(context)
                                }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Button 1: Evaluate Location Now
                OutlinedButton(
                    onClick = {
                        val fineGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                        if (!fineGranted) {
                            locationPermissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
                        } else {
                            hazardStatus = ChandipurLocationTideHazardManager.evaluateLocationTideHazard(context)
                            Toast.makeText(context, if (isOdia) "ସ୍ଥିତି ସତେଜ ହେଲା: ${String.format(java.util.Locale.US, "%.1f", hazardStatus.distanceKm)} କି.ମି." else "Location checked: ${String.format(java.util.Locale.US, "%.1f", hazardStatus.distanceKm)} km from beach", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("evaluate_location_btn"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "ସ୍ଥାନ ଯାଞ୍ଚ କରନ୍ତୁ" else "Check Location",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Button 2: Simulate Danger Alert (Test Location on Seabed)
                Button(
                    onClick = {
                        isSimulatingBeachProximity = true
                        // Simulate coordinates directly on Chandipur intertidal sands
                        hazardStatus = ChandipurLocationTideHazardManager.evaluateLocationTideHazard(
                            context = context,
                            userLat = ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LAT + 0.005,
                            userLng = ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LNG + 0.010,
                            forceNotification = true
                        )
                        Toast.makeText(
                            context,
                            "🚨 Triggered High Tide Proximity Warning for Chandipur Beach!",
                            Toast.LENGTH_LONG
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("test_simulate_hazard_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "ବିପଦ ସତର୍କତା ଟେଷ୍ଟ" else "Test Beach Alert",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Quick Marine Police Emergency Call
            if (hazardStatus.hazardLevel == HazardLevel.DANGER) {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ChandipurLocationTideHazardManager.CHANDIPUR_MARINE_POLICE_PHONE}"))
                        context.startActivity(callIntent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("call_marine_police_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = "Emergency Call",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Call Chandipur Marine Police: ${ChandipurLocationTideHazardManager.CHANDIPUR_MARINE_POLICE_PHONE}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}
