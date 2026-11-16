package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import com.example.util.ShareHelper
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AlertSeverity
import com.example.data.model.BalasoreWeatherAlert
import com.example.data.model.BalasoreZone
import com.example.data.notification.TideAndWeatherNotificationManager
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoAmberBg
import com.example.ui.theme.BentoAmberText
import com.example.ui.theme.BentoGreenBg
import com.example.ui.theme.BentoGreenText
import com.example.ui.theme.BentoPrimaryBlue
import com.example.ui.theme.BentoRedBg
import com.example.ui.theme.BentoRedText
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark

/**
 * Rich Material 3 Card component displaying location-based weather alerts for the Balasore region
 * fetched via OpenWeatherMap API and integrated with the app's local notification engine.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BalasoreLocationWeatherAlertCard(
    alerts: List<BalasoreWeatherAlert>,
    isOdia: Boolean = false,
    onTestNotification: ((BalasoreWeatherAlert) -> Unit)? = null,
    onRefresh: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedZone by remember { mutableStateOf<BalasoreZone?>(null) }
    var expandedAlertId by remember { mutableStateOf<String?>(alerts.firstOrNull()?.id) }
    var isNotificationEnabled by remember {
        mutableStateOf(TideAndWeatherNotificationManager.isWeatherAlertsEnabled(context))
    }

    val filteredAlerts = if (selectedZone == null) {
        alerts
    } else {
        alerts.filter { alert ->
            alert.affectedZones.contains(selectedZone) || alert.affectedZones.contains(BalasoreZone.DISTRICT_WIDE)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balasore_location_weather_alert_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF0F172A).copy(alpha = 0.95f) // Deep Navy Glass
        ),
        border = BorderStroke(1.dp, Color(0xFF334155).copy(alpha = 0.8f))
    ) {
        Column(
            modifier = Modifier
                .padding(18.dp)
                .animateContentSize()
        ) {
            // === Top Section: Header & Live Alert Count ===
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFFDC2626), Color(0xFFF97316))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Weather Alert Warning",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = if (isOdia) "ଆଞ୍ଚଳିକ ପାଣିପାଗ ସତର୍କତା" else "Location Weather Alerts",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                        Text(
                            text = if (isOdia) "ବାଲେଶ୍ୱର ଜିଲ୍ଲା ଓ ଉପକୂଳ ନିରୀକ୍ଷଣ" else "Balasore Region • OpenWeatherMap Live",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Active Count Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (alerts.any { it.severity.isUrgent }) Color(0xFF7F1D1D) else Color(0xFF1E293B),
                    border = BorderStroke(
                        1.dp,
                        if (alerts.any { it.severity.isUrgent }) Color(0xFFEF4444) else Color(0xFF475569)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (alerts.any { it.severity.isUrgent }) Color(0xFFEF4444) else Color(0xFF38BDF8))
                        )
                        Text(
                            text = "${filteredAlerts.size} ${if (isOdia) "ସତର୍କତା" else "Active"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // === Notification Integration Control Bar ===
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF1E293B).copy(alpha = 0.8f),
                border = BorderStroke(1.dp, Color(0xFF334155))
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
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = if (isNotificationEnabled) Icons.Default.NotificationsActive else Icons.Default.Notifications,
                            contentDescription = "Notification Status",
                            tint = if (isNotificationEnabled) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = if (isOdia) "ସତର୍କତା ବିଜ୍ଞପ୍ତି (Notification)" else "Push Notifications",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                text = if (isNotificationEnabled) {
                                    if (isOdia) "ସକ୍ରିୟ • ବିପଦ ସମୟରେ ତୁରନ୍ତ ସୂଚନା ମିଳିବ" else "Active • Immediate alerts for squalls & high tide"
                                } else {
                                    if (isOdia) "ନିଷ୍କ୍ରିୟ" else "Turn on for automatic background alerts"
                                },
                                fontSize = 10.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Switch(
                        checked = isNotificationEnabled,
                        onCheckedChange = { enabled ->
                            isNotificationEnabled = enabled
                            TideAndWeatherNotificationManager.setWeatherAlertsEnabled(context, enabled)
                            val message = if (enabled) {
                                if (isOdia) "ବାଲେଶ୍ୱର ପାଣିପାଗ ସତର୍କତା ବିଜ୍ଞପ୍ତି ସକ୍ରିୟ ହେଲା" else "Balasore weather alert notifications enabled"
                            } else {
                                if (isOdia) "ବିଜ୍ଞପ୍ତି ନିଷ୍କ୍ରିୟ ହେଲା" else "Weather alert notifications disabled"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF0284C7),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF334155)
                        ),
                        modifier = Modifier.testTag("weather_notification_switch")
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // === Balasore Geographic Sub-region Filter Chips ===
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // "All Balasore" Chip
                FilterChip(
                    selected = selectedZone == null,
                    onClick = { selectedZone = null },
                    label = {
                        Text(
                            text = if (isOdia) "ସମସ୍ତ ଅଞ୍ଚଳ" else "All Balasore",
                            fontSize = 11.sp,
                            fontWeight = if (selectedZone == null) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0284C7),
                        selectedLabelColor = Color.White,
                        containerColor = Color(0xFF1E293B),
                        labelColor = Color(0xFF94A3B8)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = selectedZone == null,
                        borderColor = Color(0xFF475569),
                        selectedBorderColor = Color(0xFF38BDF8)
                    )
                )

                BalasoreZone.values().filter { it != BalasoreZone.DISTRICT_WIDE }.forEach { zone ->
                    FilterChip(
                        selected = selectedZone == zone,
                        onClick = { selectedZone = if (selectedZone == zone) null else zone },
                        label = {
                            Text(
                                text = if (isOdia) zone.odiaName else zone.zoneName.substringBefore("&").trim(),
                                fontSize = 11.sp,
                                fontWeight = if (selectedZone == zone) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF0284C7),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color(0xFF94A3B8)
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedZone == zone,
                            borderColor = Color(0xFF475569),
                            selectedBorderColor = Color(0xFF38BDF8)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // === Alert Cards List ===
            if (filteredAlerts.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E293B).copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "☀️ " + if (isOdia) "କୌଣସି ବିପଜ୍ଜନକ ସତର୍କତା ନାହିଁ" else "No Severe Alerts for Selected Zone",
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (isOdia) "ପାଣିପାଗ ସ୍ୱାଭାବିକ ଏବଂ ଯାତାୟାତ ପାଇଁ ଅନୁକୂଳ ଅଛି।" else "Weather is normal and clear for travel across this sector.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    filteredAlerts.forEach { alert ->
                        val isExpanded = expandedAlertId == alert.id
                        AlertItemCard(
                            alert = alert,
                            isExpanded = isExpanded,
                            isOdia = isOdia,
                            onToggleExpand = {
                                expandedAlertId = if (isExpanded) null else alert.id
                            },
                            onTestNotification = {
                                onTestNotification?.invoke(alert) ?: run {
                                    TideAndWeatherNotificationManager.showLocationWeatherAlertNotification(context, alert)
                                    Toast.makeText(context, "Dispatched notification for: ${alert.title}", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Individual expandable weather alert item with rich typography, countdown badge,
 * actionable safety steps, and quick emergency action buttons.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AlertItemCard(
    alert: BalasoreWeatherAlert,
    isExpanded: Boolean,
    isOdia: Boolean,
    onToggleExpand: () -> Unit,
    onTestNotification: () -> Unit
) {
    val context = LocalContext.current

    val cardBorderColor = when (alert.severity) {
        AlertSeverity.CYCLONE_ALERT -> Color(0xFFEF4444)
        AlertSeverity.WARNING -> Color(0xFFF97316)
        AlertSeverity.ADVISORY -> Color(0xFFFBBF24)
        AlertSeverity.HIGH_TIDE -> Color(0xFF0284C7)
        AlertSeverity.SAFE -> Color(0xFF10B981)
    }

    val cardBgGradient = when (alert.severity) {
        AlertSeverity.CYCLONE_ALERT -> listOf(Color(0xFF450A0A), Color(0xFF18181B))
        AlertSeverity.WARNING -> listOf(Color(0xFF431407), Color(0xFF18181B))
        AlertSeverity.ADVISORY -> listOf(Color(0xFF451A03), Color(0xFF18181B))
        AlertSeverity.HIGH_TIDE -> listOf(Color(0xFF0C4A6E), Color(0xFF18181B))
        AlertSeverity.SAFE -> listOf(Color(0xFF064E3B), Color(0xFF18181B))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onToggleExpand() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, cardBorderColor.copy(alpha = 0.6f))
    ) {
        Box(
            modifier = Modifier
                .background(Brush.verticalGradient(cardBgGradient))
                .padding(14.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Header row: Category Icon + Title + Severity Pill + Expand Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = alert.category.emoji,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Severity Pill
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = alert.severity.badgeBg
                            ) {
                                Text(
                                    text = if (isOdia) alert.severity.odiaLabel else alert.severity.levelName,
                                    color = alert.severity.badgeText,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            // Time remaining badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color.Black.copy(alpha = 0.4f)
                            ) {
                                Text(
                                    text = alert.getFormattedTimeRemaining(),
                                    color = Color(0xFFE2E8F0),
                                    fontSize = 9.sp,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = if (isOdia) alert.odiaTitle else alert.title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    // Quick Share Button invoking Android's native ShareSheet
                    IconButton(
                        onClick = {
                            ShareHelper.shareWeatherAlert(context, alert, alert.getFormattedTimeRemaining())
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("share_weather_alert_btn_${alert.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Weather Alert via ShareSheet",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onToggleExpand,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = if (isExpanded) "Collapse Alert" else "Expand Alert",
                            tint = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Summary Text
                Text(
                    text = alert.summary,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                )

                // Affected Zones tags
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    alert.affectedZones.forEach { zone ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFF1E293B).copy(alpha = 0.7f),
                            border = BorderStroke(0.5.dp, Color(0xFF475569))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFF38BDF8),
                                    modifier = Modifier.size(10.dp)
                                )
                                Text(
                                    text = if (isOdia) zone.odiaName else zone.zoneName,
                                    fontSize = 10.sp,
                                    color = Color(0xFFE2E8F0)
                                )
                            }
                        }
                    }
                }

                // === Expandable Details & Actions ===
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        // Detailed Description
                        Text(
                            text = alert.detailedDescription,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actionable Instructions
                        if (alert.actionableInstructions.isNotEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color(0xFF0F172A).copy(alpha = 0.7f),
                                border = BorderStroke(0.5.dp, Color(0xFF334155))
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isOdia) "ସୁରକ୍ଷା ନିର୍ଦ୍ଦେଶାବଳୀ (Safety Guidance)" else "Actionable Safety Guidance",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp,
                                            color = Color.White
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    alert.actionableInstructions.forEach { step ->
                                        Row(
                                            modifier = Modifier.padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Text(
                                                text = "• ",
                                                color = Color(0xFF38BDF8),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = step,
                                                color = Color(0xFFE2E8F0),
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                        }

                        // Authority & Attribution
                        Text(
                            text = "Source: ${alert.issuingAuthority}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons: Call Emergency & Trigger Test Notification
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Emergency Call Button
                            OutlinedButton(
                                onClick = {
                                    val phone = alert.emergencyContact.filter { it.isDigit() || it == '-' }
                                    try {
                                        val intent = Intent(Intent.ACTION_DIAL).apply {
                                            data = Uri.parse("tel:$phone")
                                        }
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Dialer unavailable", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFEF4444)),
                                border = BorderStroke(1.dp, Color(0xFFEF4444).copy(alpha = 0.5f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("alert_emergency_call_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Phone,
                                    contentDescription = "Emergency Call",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOdia) "ଜରୁରୀ" else "Helpline",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Share Alert Button with Android ShareSheet
                            OutlinedButton(
                                onClick = {
                                    ShareHelper.shareWeatherAlert(context, alert, alert.getFormattedTimeRemaining())
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF38BDF8)),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.6f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("share_weather_alert_action_button_${alert.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Share Alert",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOdia) "ସେୟାର୍" else "Share",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            // Notification Send / Test Button
                            Button(
                                onClick = onTestNotification,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("alert_test_notification_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = "Trigger Notification",
                                    modifier = Modifier.size(14.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOdia) "ନୋଟିସ୍" else "Notify",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
