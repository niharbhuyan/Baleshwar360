package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.AppLanguage
import com.example.data.model.BalasoreWeatherAlert
import com.example.data.model.BalasoreZone
import com.example.data.model.WeatherAlertCategory
import com.example.data.notification.TideAndWeatherNotificationManager
import com.example.data.remote.OdishaSeismicAlert
import com.example.data.remote.OdishaSeismicRepository
import com.example.data.remote.SeismicAlertLevel
import com.example.ui.theme.*
import com.example.util.BalasoreHapticHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Real-Time Seismic Activity & Tsunami Watch Card for the Odisha Coastal Belt & Bay of Bengal.
 * Fetches live earthquake telemetry from the public USGS GeoJSON API (`earthquake.usgs.gov`)
 * and evaluates impact for Balasore, Chandipur, Talasari, Dhamra, and Paradeep.
 */
@Composable
fun OdishaCoastalSeismicAlertsCard(
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA
    val repository = remember { OdishaSeismicRepository(context) }

    var seismicAlerts by remember { mutableStateOf<List<OdishaSeismicAlert>>(emptyList()) }
    var isLiveUsgsFeed by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedMinMagnitude by remember { mutableDoubleStateOf(0.0) }
    var expandedEventId by remember { mutableStateOf<String?>(null) }
    var lastUpdatedText by remember { mutableStateOf("") }

    var rotationTarget by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationTarget,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "seismic_refresh_spin"
    )

    val loadSeismicEvents: (Boolean) -> Unit = { forceRefresh ->
        if (forceRefresh) {
            rotationTarget += 360f
            BalasoreHapticHelper.vibrateButtonClick(context)
        }
        isLoading = true
        coroutineScope.launch {
            val (events, isLive) = repository.fetchOdishaCoastalSeismicAlerts(forceRefresh = forceRefresh)
            seismicAlerts = events
            isLiveUsgsFeed = isLive
            if (expandedEventId == null) {
                expandedEventId = events.firstOrNull()?.id
            }
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            lastUpdatedText = sdf.format(Date())
            isLoading = false
            if (forceRefresh) {
                BalasoreHapticHelper.vibrateSuccess(context)
            }
        }
    }

    LaunchedEffect(Unit) {
        loadSeismicEvents(false)
    }

    val filteredAlerts = remember(seismicAlerts, selectedMinMagnitude) {
        seismicAlerts.filter { it.magnitude >= selectedMinMagnitude }
    }

    val maxMagnitude = remember(seismicAlerts) {
        seismicAlerts.maxOfOrNull { it.magnitude } ?: 0.0
    }
    val closestDistanceKm = remember(seismicAlerts) {
        seismicAlerts.minOfOrNull { it.distanceFromBalasoreKm } ?: 0
    }
    val anyTsunamiWatch = remember(seismicAlerts) {
        seismicAlerts.any { it.tsunamiWatch }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("odisha_coastal_seismic_alerts_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A)
        ),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (anyTsunamiWatch || maxMagnitude >= 5.0) Color(0xFFEF4444) else Color(0xFF38BDF8).copy(alpha = 0.45f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ================= 1. HEADER & LIVE USGS STATUS =================
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
                                    colors = listOf(Color(0xFFF59E0B), Color(0xFFDC2626))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Seismic Monitor",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia)
                                "ଓଡ଼ିଶା ଉପକୂଳ ଭୂକମ୍ପ ଓ ସୁନାମି ସତର୍କତା"
                            else
                                "Odisha Coastal Belt Seismic Monitor",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Text(
                            text = if (isOdia)
                                "USGS ଭୂକମ୍ପ API • ବଙ୍ଗୋପସାଗର ଓ ବାଲେଶ୍ୱର ଉପକୂଳ"
                            else
                                "Public USGS Earthquake API • Bay of Bengal & Odisha",
                            fontSize = 11.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }

                IconButton(
                    onClick = { loadSeismicEvents(true) },
                    modifier = Modifier
                        .size(38.dp)
                        .testTag("seismic_alerts_refresh_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Refresh Seismic Telemetry",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier
                            .size(22.dp)
                            .rotate(animatedRotation)
                    )
                }
            }

            // Live API & Tsunami Status Strip
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, Color(0xFF334155)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("seismic_api_status_banner")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (anyTsunamiWatch) Color(0xFFEF4444) else Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (anyTsunamiWatch) {
                                if (isOdia) "ସୁନାମି ସତର୍କତା ସକ୍ରିୟ!" else "TSUNAMI WATCH ACTIVE"
                            } else {
                                if (isOdia) "USGS GeoJSON ଫିଡ୍ • ସୁନାମି ବିପଦ ନାହିଁ" else "USGS FDSNWS Feed • No Tsunami Threat"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (anyTsunamiWatch) Color(0xFFFCA5A5) else Color(0xFF6EE7B7)
                        )
                    }

                    if (lastUpdatedText.isNotBlank()) {
                        Text(
                            text = if (isOdia) "ଅଦ୍ୟତନ: $lastUpdatedText" else "Synced $lastUpdatedText",
                            fontSize = 10.5.sp,
                            color = Color(0xFF94A3B8)
                        )
                    }
                }
            }

            // ================= 2. TELEMETRY SUMMARY METRICS =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SeismicMetricTile(
                    label = if (isOdia) "ମୋଟ କମ୍ପନ" else "Events",
                    value = "${seismicAlerts.size}",
                    subtitle = if (isOdia) "ବଙ୍ଗୋପସାଗର" else "Bay Corridor",
                    accentColor = Color(0xFF38BDF8),
                    modifier = Modifier.weight(1f)
                )
                SeismicMetricTile(
                    label = if (isOdia) "ସର୍ବାଧିକ ତୀବ୍ରତା" else "Peak Mag",
                    value = "M ${String.format(Locale.US, "%.1f", maxMagnitude)}",
                    subtitle = if (isOdia) "ରିକ୍ଟର ସ୍କେଲ୍" else "Richter Scale",
                    accentColor = if (maxMagnitude >= 4.5) Color(0xFFF59E0B) else Color(0xFF10B981),
                    modifier = Modifier.weight(1f)
                )
                SeismicMetricTile(
                    label = if (isOdia) "ନିକଟତମ ଦୂରତା" else "Closest",
                    value = "$closestDistanceKm km",
                    subtitle = if (isOdia) "ବାଲେଶ୍ୱର ଠାରୁ" else "From Balasore",
                    accentColor = Color(0xFFA78BFA),
                    modifier = Modifier.weight(1f)
                )
            }

            // ================= 3. MAGNITUDE FILTER CHIPS =================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filterOptions = listOf(
                    0.0 to (if (isOdia) "ସମସ୍ତ (${seismicAlerts.size})" else "All (${seismicAlerts.size})"),
                    3.5 to (if (isOdia) "M 3.5+ କମ୍ପନ" else "M 3.5+ Tremors"),
                    4.5 to (if (isOdia) "M 4.5+ ମଧ୍ୟମ" else "M 4.5+ Moderate"),
                    5.0 to (if (isOdia) "M 5.0+ ପ୍ରବଳ" else "M 5.0+ Strong")
                )
                filterOptions.forEach { (minMag, label) ->
                    val selected = selectedMinMagnitude == minMag
                    Surface(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            selectedMinMagnitude = minMag
                        },
                        shape = RoundedCornerShape(999.dp),
                        color = if (selected) OceanBlue else Color(0xFF1E293B),
                        border = BorderStroke(
                            1.dp,
                            if (selected) Color(0xFF7DD3FC) else Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("seismic_filter_chip_${minMag.toInt()}")
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // ================= 4. SEISMIC ALERTS LIST =================
            if (isLoading && seismicAlerts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.size(30.dp)
                    )
                }
            } else if (filteredAlerts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isOdia)
                            "ଏହି ତୀବ୍ରତା ସୀମା ମଧ୍ୟରେ କୌଣସି ଭୂକମ୍ପ ରେକର୍ଡ ହୋଇନାହିଁ। ଓଡ଼ିଶା ଉପକୂଳ ସମ୍ପୂର୍ଣ୍ଣ ସୁରକ୍ଷିତ ଅଛି।"
                        else
                            "No seismic events above M$selectedMinMagnitude detected in the Odisha coastal corridor.",
                        fontSize = 12.sp,
                        color = Color(0xFFCBD5E1),
                        modifier = Modifier.padding(14.dp)
                    )
                }
            } else {
                filteredAlerts.forEach { event ->
                    val isExpanded = expandedEventId == event.id
                    val magBadgeColor = when {
                        event.magnitude >= 5.5 -> Color(0xFFDC2626)
                        event.magnitude >= 4.5 -> Color(0xFFD97706)
                        event.magnitude >= 3.5 -> Color(0xFF0284C7)
                        else -> Color(0xFF059669)
                    }

                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color(0xFF1E293B),
                        border = BorderStroke(
                            1.dp,
                            if (isExpanded) magBadgeColor.copy(alpha = 0.8f) else Color(0xFF334155)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                expandedEventId = if (isExpanded) null else event.id
                            }
                            .testTag("seismic_alert_item_${event.id}")
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Richter Magnitude Badge
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(magBadgeColor),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(
                                                text = String.format(Locale.US, "%.1f", event.magnitude),
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Black,
                                                color = Color.White
                                            )
                                            Text(
                                                text = "MAG",
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White.copy(alpha = 0.85f)
                                            )
                                        }
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (isOdia) event.placeOr else event.placeEn,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "${formatRelativeSeismicTime(event.timeMillis, isOdia)} • " +
                                                (if (isOdia) "ଗଭୀରତା ${event.depthKm} କି.ମି. • ଦୂରତା ${event.distanceFromBalasoreKm} କି.ମି."
                                                else "Depth ${event.depthKm} km • ${event.distanceFromBalasoreKm} km from Balasore"),
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Alert Level & Tsunami Status Pills
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = magBadgeColor.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (isOdia) event.alertLevel.labelOr else event.alertLevel.labelEn,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFE2E8F0),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (event.tsunamiWatch) Color(0xFFDC2626).copy(alpha = 0.25f) else Color(0xFF059669).copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = if (event.tsunamiWatch) {
                                            if (isOdia) "🌊 ସୁନାମି ସତର୍କତା" else "🌊 Tsunami Watch Active"
                                        } else {
                                            if (isOdia) "✓ ସୁନାମି ବିପଦ ନାହିଁ" else "✓ No Tsunami Threat"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (event.tsunamiWatch) Color(0xFFFCA5A5) else Color(0xFF6EE7B7),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }

                            // Expandable Coastal Impact & Safety Instructions
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = if (isOdia) event.coastalImpactSummaryOr else event.coastalImpactSummaryEn,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE2E8F0),
                                        lineHeight = 17.sp
                                    )

                                    val guidelines = if (isOdia) event.safetyGuidelinesOr else event.safetyGuidelinesEn
                                    if (guidelines.isNotEmpty()) {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF0F172A),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Text(
                                                    text = if (isOdia)
                                                        "🛡️ ଉପକୂଳ ନିରାପତ୍ତା ନିର୍ଦ୍ଦେଶାବଳୀ (OSDMA / INCOIS):"
                                                    else
                                                        "🛡️ Odisha Coastal Safety Protocol (OSDMA / INCOIS):",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color(0xFFFBBF24)
                                                )
                                                guidelines.forEach { tip ->
                                                    Text(
                                                        text = "• $tip",
                                                        fontSize = 11.sp,
                                                        color = Color(0xFFF1F5F9)
                                                    )
                                                }
                                            }
                                        }
                                    }

                                    Text(
                                        text = if (isOdia)
                                            "ସ୍ଥାନାଙ୍କ: ${event.latitude}°N, ${event.longitude}°E • ଉତ୍ସ: ${event.sourceAgency}"
                                        else
                                            "Epicenter: ${event.latitude}°N, ${event.longitude}°E • Source: ${event.sourceAgency}",
                                        fontSize = 10.sp,
                                        color = Color(0xFF94A3B8)
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = {
                                                val shareBody = buildString {
                                                    append("🌐 Baleshwar 360 Seismic Alert\n")
                                                    append("Magnitude: M${event.magnitude}\n")
                                                    append("Location: ${event.placeEn}\n")
                                                    append("Depth: ${event.depthKm} km (${event.distanceFromBalasoreKm} km from Balasore)\n")
                                                    append("Summary: ${event.coastalImpactSummaryEn}\n")
                                                    append("USGS Bulletin: ${event.usgsUrl}")
                                                }
                                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(Intent.EXTRA_SUBJECT, "Odisha Coastal Seismic Alert (M${event.magnitude})")
                                                    putExtra(Intent.EXTRA_TEXT, shareBody)
                                                }
                                                context.startActivity(Intent.createChooser(sendIntent, "Share Seismic Alert"))
                                            },
                                            border = BorderStroke(1.dp, Color(0xFF64748B)),
                                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("seismic_share_btn_${event.id}")
                                        ) {
                                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isOdia) "ସେୟାର୍" else "Share", fontSize = 11.sp)
                                        }

                                        Button(
                                            onClick = {
                                                val mappedAlert = BalasoreWeatherAlert(
                                                    id = event.id,
                                                    title = "Odisha Seismic Alert: M${event.magnitude} (${event.placeEn})",
                                                    odiaTitle = "ଭୂକମ୍ପ ସୂଚନା: M${event.magnitude} (${event.placeOr})",
                                                    hindiTitle = "ओडिशा भूकंप सूचना: M${event.magnitude}",
                                                    summary = event.coastalImpactSummaryEn,
                                                    detailedDescription = event.coastalImpactSummaryEn,
                                                    category = WeatherAlertCategory.TIDAL_SURGE,
                                                    severity = if (event.magnitude >= 5.0) AlertSeverity.WARNING else AlertSeverity.ADVISORY,
                                                    affectedZones = listOf(BalasoreZone.COASTAL_CHANDIPUR, BalasoreZone.BALASORE_TOWN),
                                                    issuingAuthority = event.sourceAgency,
                                                    validFromMillis = event.timeMillis,
                                                    validUntilMillis = System.currentTimeMillis() + 3600_000L,
                                                    actionableInstructions = event.safetyGuidelinesEn,
                                                    emergencyContact = "Balasore Emergency Control: 06782-262244"
                                                )
                                                TideAndWeatherNotificationManager.showLocationWeatherAlertNotification(context, mappedAlert)
                                                Toast.makeText(
                                                    context,
                                                    if (isOdia) "ଭୂକମ୍ପ ସତର୍କତା ବିଜ୍ଞପ୍ତି ପଠାଗଲା" else "Seismic alert notification dispatched",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = magBadgeColor),
                                            shape = RoundedCornerShape(8.dp),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("seismic_notify_btn_${event.id}")
                                        ) {
                                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(if (isOdia) "ଆଲର୍ଟ ବିଜ୍ଞପ୍ତି" else "Notify Alert", fontSize = 11.sp)
                                        }

                                        IconButton(
                                            onClick = {
                                                runCatching {
                                                    val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(event.usgsUrl))
                                                    context.startActivity(browserIntent)
                                                }
                                            },
                                            modifier = Modifier
                                                .size(36.dp)
                                                .testTag("seismic_open_usgs_url_${event.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.OpenInNew,
                                                contentDescription = "Open USGS Event Page",
                                                tint = Color(0xFF38BDF8),
                                                modifier = Modifier.size(18.dp)
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
}

@Composable
private fun SeismicMetricTile(
    label: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFF1E293B),
        border = BorderStroke(1.dp, Color(0xFF334155)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                maxLines = 1
            )
            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.ExtraBold,
                color = accentColor,
                maxLines = 1
            )
            Text(
                text = subtitle,
                fontSize = 9.5.sp,
                color = Color(0xFFCBD5E1),
                maxLines = 1
            )
        }
    }
}

private fun formatRelativeSeismicTime(timestampMs: Long, isOdia: Boolean): String {
    val diffMs = (System.currentTimeMillis() - timestampMs).coerceAtLeast(0L)
    val minutes = diffMs / (60 * 1000L)
    val hours = minutes / 60L
    val days = hours / 24L

    return when {
        minutes < 2 -> if (isOdia) "এইମାତ୍ର" else "Just now"
        minutes < 60 -> if (isOdia) "$minutes ମିନିଟ୍ ପୂର୍ବେ" else "$minutes mins ago"
        hours < 24 -> if (isOdia) "$hours ଘଣ୍ଟା ପୂର୍ବେ" else "$hours hours ago"
        else -> if (isOdia) "$days ଦିନ ପୂର୍ବେ" else "$days days ago"
    }
}
