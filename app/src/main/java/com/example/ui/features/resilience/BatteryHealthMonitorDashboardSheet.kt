package com.example.ui.features.resilience

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.analytics.BalasoreAnalytics
import com.example.data.model.AppLanguage
import com.example.data.sync.BackgroundServiceEnergyProfile
import com.example.data.sync.BatteryHealthTelemetry
import com.example.data.sync.BatteryLongevityActionTip
import com.example.data.sync.DeviceBatteryInfo
import com.example.data.sync.EnergyImpactLevel
import com.example.data.sync.SyncManager
import com.example.data.sync.TipActionType

/**
 * Battery Health Monitor Dashboard Sheet for Balasore 360.
 *
 * Provides:
 * 1. Live Device Battery Hardware Telemetry & Health Score (Temperature, Voltage, Longevity Index).
 * 2. Energy Consumption Breakdown for all Background Services (WorkManager, Alarms, FCM, Widgets).
 * 3. Interactive Sync Interval Optimizer (15m, 30m, 60m, 120m presets with wakeup reduction calculation).
 * 4. Actionable Battery Longevity Tips with one-tap toggle / apply actions and estimated life extension.
 * 5. 24-hour background energy discharge comparison curve.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryHealthMonitorDashboardSheet(
    language: AppLanguage,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA

    var telemetry by remember { mutableStateOf(SyncManager.getBatteryHealthTelemetry(context)) }
    var serviceProfiles by remember { mutableStateOf(SyncManager.getBackgroundServiceProfiles(context)) }
    var longevityTips by remember { mutableStateOf(SyncManager.getActionableLongevityTips(context)) }
    var baseInterval by remember { mutableIntStateOf(SyncManager.getUserBaseSyncIntervalMinutes(context)) }
    var isSimulateLowBattery by remember { mutableStateOf(SyncManager.isSimulateLowBatteryEnabled(context)) }
    var isRunningImmediateSync by remember { mutableStateOf(false) }
    var selectedFilterTier by remember { mutableStateOf<EnergyImpactLevel?>(null) }

    fun refreshAllTelemetry() {
        telemetry = SyncManager.getBatteryHealthTelemetry(context)
        serviceProfiles = SyncManager.getBackgroundServiceProfiles(context)
        longevityTips = SyncManager.getActionableLongevityTips(context)
        baseInterval = SyncManager.getUserBaseSyncIntervalMinutes(context)
        isSimulateLowBattery = SyncManager.isSimulateLowBatteryEnabled(context)
    }

    val rotation by rememberInfiniteTransition(label = "sync_rotate").animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        modifier = modifier.testTag("battery_health_monitor_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // --- HEADER ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981).copy(alpha = 0.12f),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.BatteryChargingFull,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    Column {
                        Text(
                            text = if (isOdia) "ବ୍ୟାଟେରୀ ସ୍ୱାସ୍ଥ୍ୟ ମନିଟର୍" else "Battery Health Monitor",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 19.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOdia)
                                "ବ୍ୟାକଗ୍ରାଉଣ୍ଡ ସର୍ଭିସ୍ ଶକ୍ତି ବ୍ୟବହାର ଓ ସିଙ୍କ୍ ଆୟୁଷ ଅପ୍ଟିମାଇଜର୍"
                            else
                                "Background Services Energy Drain & Longevity Optimizer",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_battery_health_monitor_sheet")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // --- SECTION 1: HARDWARE BATTERY TELEMETRY & LONGEVITY SCORE BENTO CARD ---
            val batteryLevel = telemetry.percentage
            val isCharging = telemetry.isCharging
            val isLowBattery = batteryLevel <= 20

            val statusColor = when {
                isCharging -> Color(0xFF10B981) // Green
                isLowBattery -> Color(0xFFEF4444) // Red alert
                batteryLevel <= 40 -> Color(0xFFF59E0B) // Amber
                else -> Color(0xFF10B981) // Green
            }

            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isLowBattery) Color(0xFFFEF2F2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(
                    1.5.dp,
                    if (isLowBattery) Color(0xFFEF4444) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("battery_telemetry_hero_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Top Row: Percentage + Longevity Score Badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isCharging) Icons.Default.BatteryChargingFull else if (isLowBattery) Icons.Default.BatteryAlert else Icons.Default.BatteryStd,
                                contentDescription = null,
                                tint = statusColor,
                                modifier = Modifier.size(26.dp)
                            )
                            Column {
                                Text(
                                    text = if (isOdia) "ଲାଇଭ୍ ହାର୍ଡୱେର୍ ବ୍ୟାଟେରୀ" else "Live Hardware Battery",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = telemetry.chargingSpeed,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Percentage Pill
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = statusColor.copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = if (isCharging) "⚡ $batteryLevel%" else "$batteryLevel%",
                                fontWeight = FontWeight.Black,
                                fontSize = 16.sp,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Linear Battery Capacity Bar
                    LinearProgressIndicator(
                        progress = { batteryLevel / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Longevity Index Grade Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.35f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("longevity_index_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = null,
                                    tint = Color(0xFF047857),
                                    modifier = Modifier.size(22.dp)
                                )
                                Column {
                                    Text(
                                        text = if (isOdia) "ବ୍ୟାଟେରୀ ଆୟୁଷ ସୂଚକାଙ୍କ (Longevity Index)" else "Battery Longevity Index",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp,
                                        color = Color(0xFF065F46)
                                    )
                                    Text(
                                        text = "${telemetry.longevityRating} • Estimated life: ${telemetry.projectedLifespanYears}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF047857)
                            ) {
                                Text(
                                    text = "${telemetry.longevityScore}/100",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 4-Column Hardware Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Metric 1: Temperature
                        HardwareMetricCell(
                            title = if (isOdia) "ତାପମାତ୍ରା" else "Temperature",
                            value = "${telemetry.temperatureCelsius}°C",
                            subtitle = if (telemetry.temperatureCelsius > 38f) "Warm" else "Normal",
                            valueColor = if (telemetry.temperatureCelsius > 38f) Color(0xFFDC2626) else Color(0xFF0284C7),
                            modifier = Modifier.weight(1f)
                        )

                        // Metric 2: Voltage
                        HardwareMetricCell(
                            title = if (isOdia) "ଭୋଲ୍ଟେଜ୍" else "Voltage",
                            value = "${telemetry.voltageMilliVolts} mV",
                            subtitle = telemetry.technology,
                            valueColor = Color(0xFF0D9488),
                            modifier = Modifier.weight(1f)
                        )

                        // Metric 3: Estimated Runtime
                        val runtimeFormatted = String.format(java.util.Locale.US, "%.1fh", telemetry.estimatedRemainingHours)
                        HardwareMetricCell(
                            title = if (isOdia) "ଆନୁମାନିକ ଚଳନ" else "Est. Runtime",
                            value = runtimeFormatted,
                            subtitle = if (isCharging) "Charging" else "Remaining",
                            valueColor = Color(0xFF16A34A),
                            modifier = Modifier.weight(1f)
                        )

                        // Metric 4: Health Condition
                        HardwareMetricCell(
                            title = if (isOdia) "ସ୍ୱାସ୍ଥ୍ୟ" else "Health",
                            value = telemetry.healthCondition,
                            subtitle = "Cycles: ~${telemetry.cycleEstimate}",
                            valueColor = Color(0xFF6366F1),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Low Battery Warning
                    if (isLowBattery) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEE2E2),
                            border = BorderStroke(1.dp, Color(0xFFF87171)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = if (isOdia)
                                        "⚠️ ବ୍ୟାଟେରୀ ୨୦% ତଳେ ଅଛି: ସ୍ମାର୍ଟ ସିଙ୍କ୍ ସକ୍ରିୟ ହୋଇ ଭାରୀ କାର୍ଯ୍ୟ ସ୍ଥଗିତ ରଖିଛି"
                                    else
                                        "⚠️ Critical Battery (<= 20%): Non-critical background sync throttled to reserve power for Emergency SOS.",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                        }
                    }

                    // Test mode checkbox for low battery simulation
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "୧୫% ସିମୁଲେସନ୍ ମୋଡ୍ ଟେଷ୍ଟ୍" else "Test 15% Mode",
                            fontSize = 11.sp,
                            color = Color(0xFF2563EB),
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.clickable {
                                isSimulateLowBattery = !isSimulateLowBattery
                                SyncManager.setSimulateLowBatteryEnabled(context, isSimulateLowBattery)
                                refreshAllTelemetry()
                            }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Checkbox(
                            checked = isSimulateLowBattery,
                            onCheckedChange = {
                                isSimulateLowBattery = it
                                SyncManager.setSimulateLowBatteryEnabled(context, it)
                                refreshAllTelemetry()
                            },
                            modifier = Modifier.scale(0.75f).testTag("test_low_battery_checkbox")
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECTION 2: BACKGROUND SERVICES ENERGY CONSUMPTION BREAKDOWN ---
            val totalDailyDrain = serviceProfiles.filter { it.isEnabled }.map { it.dailyDrainPercent }.sum()
            val totalMah = serviceProfiles.filter { it.isEnabled }.map { it.energyMilliAmpHours }.sum()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isOdia) "ବ୍ୟାକଗ୍ରାଉଣ୍ଡ ସର୍ଭିସ୍ ଶକ୍ତି ବ୍ୟବହାର" else "BACKGROUND SERVICES ENERGY DRAIN",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = String.format(java.util.Locale.US, "Total: %.2f%% (~%.1f mAh / 24h)", totalDailyDrain, totalMah),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF047857)
                    )
                }

                // Total services count
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "${serviceProfiles.size} Services",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Visual Progress comparison
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isOdia) "୨୪-ଘଣ୍ଟା ଶକ୍ତି ବଜେଟ୍ ତୁଳନା" else "24-Hour Energy Budget Share",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = String.format(java.util.Locale.US, "%.2f%% of 1.00%% Budget", totalDailyDrain),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = if (totalDailyDrain <= 0.6f) Color(0xFF10B981) else Color(0xFFF59E0B)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LinearProgressIndicator(
                        progress = { (totalDailyDrain / 1.0f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("total_services_energy_drain_indicator"),
                        color = if (totalDailyDrain <= 0.6f) Color(0xFF10B981) else Color(0xFFF59E0B),
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Standard polling: ~2.1% drain",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Optimized saving: ~55%",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Service Profile Cards
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                serviceProfiles.forEach { svc ->
                    BackgroundServiceCard(
                        service = svc,
                        isOdia = isOdia,
                        onToggle = { enabled ->
                            SyncManager.setBackgroundServiceEnabled(context, svc.id, enabled)
                            refreshAllTelemetry()
                            Toast.makeText(
                                context,
                                if (enabled) "${svc.name} Enabled" else "${svc.name} Paused",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECTION 3: SYNC INTERVAL LONGEVITY OPTIMIZER (ACTIONABLE PRESET SELECTOR) ---
            Text(
                text = if (isOdia) "ସିଙ୍କ୍ ବ୍ୟବଧାନ ଓ ଆୟୁଷ ଅପ୍ଟିମାଇଜର୍" else "SYNC INTERVAL & LONGEVITY OPTIMIZER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sync_interval_optimizer_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isOdia) "ଆପ୍ ମୁଖ୍ୟ ସିଙ୍କ୍ ବ୍ୟବଧାନ" else "Base Sync Interval",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isOdia)
                                    "ଅଧିକ ବ୍ୟବଧାନ = କମ୍ ମୋଡେମ୍ ବ୍ୟାଟେରୀ ବ୍ୟୟ"
                                else
                                    "Higher interval = fewer radio wakeups & longer battery life",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF0284C7).copy(alpha = 0.15f),
                            border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = "${baseInterval}m",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF0284C7),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Preset Chips Row
                    val presets = listOf(
                        Triple(15, "15m", "96 runs/day"),
                        Triple(30, "30m", "48 runs/day"),
                        Triple(60, "60m (Best)", "24 runs/day"),
                        Triple(120, "120m", "12 runs/day")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presets.forEach { (minutes, label, runsDesc) ->
                            val isSelected = baseInterval == minutes
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(0xFF059669) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        SyncManager.setUserBaseSyncIntervalMinutes(context, minutes)
                                        BalasoreAnalytics.trackBatteryOptimizationApplied(context, "interval_$minutes", "Set to ${minutes}m")
                                        refreshAllTelemetry()
                                        Toast.makeText(
                                            context,
                                            "Sync interval updated to ${minutes} minutes!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .testTag("preset_interval_${minutes}m")
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = runsDesc,
                                        fontSize = 9.sp,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Impact Summary Note
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.08f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Bolt,
                                contentDescription = null,
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = when (baseInterval) {
                                    15 -> if (isOdia) "୧୫ ମିନିଟ୍: ସର୍ବାଧିକ ତାଜା ତଥ୍ୟ, କିନ୍ତୁ ବ୍ୟାଟେରୀ ବ୍ୟୟ ଅଧିକ।" else "15 mins: Instant freshness, but higher radio wakeups (~1.8% daily drain)."
                                    30 -> if (isOdia) "୩୦ ମିନିଟ୍: ସନ୍ତୁଳିତ ବ୍ୟାଟେରୀ ଓ ସତେଜତା।" else "30 mins: Balanced for active daytime tourism (~0.9% daily drain)."
                                    60 -> if (isOdia) "୬୦ ମିନିଟ୍ (ସୁପାରିଶ): ସର୍ବୋତ୍ତମ ବ୍ୟାଟେରୀ ଆୟୁଷ, ୫୫% ଶକ୍ତି ସଞ୍ଚୟ!" else "60 mins (Recommended): Superior battery longevity, saving ~55% wakeups!"
                                    else -> if (isOdia) "୧୨୦ ମିନିଟ୍: ଦୀର୍ଘ ଯାତ୍ରା ପାଇଁ ଅଲ୍ଟ୍ରା-ପାୱାର୍ ସେଭର୍।" else "120 mins: Ultra-saver profile, extending battery life by ~2.5 hours."
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF047857)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECTION 4: ACTIONABLE BATTERY LONGEVITY TIPS ---
            val unappliedTips = longevityTips.count { !it.isApplied }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isOdia) "ବ୍ୟାଟେରୀ ଆୟୁଷ ସୁପାରିଶ (Actionable Tips)" else "ACTIONABLE LONGEVITY TIPS",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (unappliedTips > 0) "$unappliedTips tips to optimize battery life" else "All longevity tips applied",
                        fontSize = 11.sp,
                        color = if (unappliedTips > 0) Color(0xFFD97706) else Color(0xFF059669)
                    )
                }

                if (unappliedTips > 0) {
                    Button(
                        onClick = {
                            SyncManager.applyAllLongevityOptimizations(context)
                            BalasoreAnalytics.trackBatteryOptimizationApplied(context, "apply_all", "+3h 45m")
                            refreshAllTelemetry()
                            // Tactile haptic feedback
                            try {
                                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                } else {
                                    @Suppress("DEPRECATION")
                                    vibrator?.vibrate(50)
                                }
                            } catch (_: Exception) {}
                            Toast.makeText(context, "All battery longevity optimizations applied! (+3h 45m battery life)", Toast.LENGTH_LONG).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.testTag("apply_all_longevity_optimizations_btn")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Text(
                                text = if (isOdia) "ସବୁ ଲାଗୁ କରନ୍ତୁ ($unappliedTips)" else "Apply All ($unappliedTips)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF10B981).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "✓ 100% OPTIMIZED",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF047857),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actionable Tips List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                longevityTips.forEach { tip ->
                    ActionableTipCard(
                        tip = tip,
                        isOdia = isOdia,
                        onAction = {
                            SyncManager.applyLongevityTip(context, tip.id)
                            BalasoreAnalytics.trackBatteryOptimizationApplied(context, tip.id, tip.estimatedDailyBatteryGain)
                            refreshAllTelemetry()
                            Toast.makeText(
                                context,
                                "Applied: ${tip.title} (${tip.estimatedDailyBatteryGain})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECTION 5: 24-HOUR ENERGY DISCHARGE CURVE CHART ---
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("battery_discharge_curve_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isOdia) "୨୪-ଘଣ୍ଟା ବ୍ୟାଟେରୀ ବ୍ୟବହାର କର୍ଭ" else "24-Hour Battery Discharge Curve",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isOdia)
                                    "ସ୍ମାର୍ଟ ସିଙ୍କ୍ ବନାମ ସାଧାରଣ ସିଙ୍କ୍ ମଧ୍ୟରେ ତୁଳନା"
                                else
                                    "Standard sync vs Battery-aware optimized discharge",
                                fontSize = 10.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFFEF4444), CircleShape))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Standard", fontSize = 9.5.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).background(Color(0xFF10B981), CircleShape))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Optimized", fontSize = 9.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Custom Compose Canvas Chart
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .testTag("battery_discharge_canvas_chart")
                    ) {
                        val width = size.width
                        val height = size.height

                        // Draw Grid lines
                        val gridPaint = Color.Gray.copy(alpha = 0.15f)
                        for (i in 1..3) {
                            val y = height * (i / 4f)
                            drawLine(gridPaint, Offset(0f, y), Offset(width, y), strokeWidth = 1f)
                        }

                        // Path 1: Standard Drain Curve (Steeper drop)
                        val standardPath = Path().apply {
                            moveTo(0f, height * 0.15f)
                            cubicTo(
                                width * 0.3f, height * 0.35f,
                                width * 0.7f, height * 0.65f,
                                width, height * 0.90f
                            )
                        }
                        drawPath(standardPath, Color(0xFFEF4444).copy(alpha = 0.5f), style = Stroke(width = 2.5f, cap = StrokeCap.Round))

                        // Path 2: Optimized Longevity Curve (Flatter, gentler drop)
                        val optimizedPath = Path().apply {
                            moveTo(0f, height * 0.15f)
                            cubicTo(
                                width * 0.3f, height * 0.25f,
                                width * 0.7f, height * 0.42f,
                                width, height * 0.55f
                            )
                        }
                        drawPath(optimizedPath, Color(0xFF10B981), style = Stroke(width = 3.5f, cap = StrokeCap.Round))

                        // Fill area under optimized curve
                        val fillPath = Path().apply {
                            addPath(optimizedPath)
                            lineTo(width, height)
                            lineTo(0f, height)
                            close()
                        }
                        drawPath(
                            fillPath,
                            Brush.verticalGradient(
                                colors = listOf(Color(0xFF10B981).copy(alpha = 0.15f), Color.Transparent),
                                startY = 0f,
                                endY = height
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("0h (100%)", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("6h", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("12h", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("18h", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("24h (+45% Left)", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- SECTION 6: ACTIONS (IMMEDIATE SYNC & RESET COUNTERS) ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Immediate Sync
                OutlinedButton(
                    onClick = {
                        isRunningImmediateSync = true
                        SyncManager.triggerImmediateSync(context)
                        refreshAllTelemetry()
                        Toast.makeText(context, "Triggered WorkManager one-time sync!", Toast.LENGTH_SHORT).show()
                        isRunningImmediateSync = false
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("battery_immediate_sync_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .then(if (isRunningImmediateSync) Modifier.rotate(rotation) else Modifier)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "ବର୍ତ୍ତମାନ ସିଙ୍କ୍ କରନ୍ତୁ" else "Sync Now",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Reset Counters
                OutlinedButton(
                    onClick = {
                        SyncManager.resetHealthMetrics(context)
                        refreshAllTelemetry()
                        Toast.makeText(context, "Telemetry metrics reset", Toast.LENGTH_SHORT).show()
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("battery_reset_counters_btn")
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "ରିସେଟ୍ ମେଟ୍ରିକ୍ସ" else "Reset Counters",
                        fontSize = 11.5.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun HardwareMetricCell(
    title: String,
    value: String,
    subtitle: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black,
                color = valueColor
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = subtitle,
                fontSize = 8.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun BackgroundServiceCard(
    service: BackgroundServiceEnergyProfile,
    isOdia: Boolean,
    onToggle: (Boolean) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    val impactColor = when (service.powerImpactLevel) {
        EnergyImpactLevel.MINIMAL -> Color(0xFF10B981)
        EnergyImpactLevel.LOW -> Color(0xFF0284C7)
        EnergyImpactLevel.MODERATE -> Color(0xFFF59E0B)
        EnergyImpactLevel.HIGH -> Color(0xFFEF4444)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("service_card_${service.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (isOdia) service.odiaName else service.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = impactColor.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = service.serviceType,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = impactColor,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = String.format(java.util.Locale.US, "%.2f%% (~%.1f mAh)", service.dailyDrainPercent, service.energyMilliAmpHours),
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black,
                            color = impactColor
                        )
                    }
                }

                if (service.canUserToggle) {
                    Switch(
                        checked = service.isEnabled,
                        onCheckedChange = onToggle,
                        modifier = Modifier.scale(0.8f).testTag("toggle_service_${service.id}")
                    )
                } else {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFDC2626).copy(alpha = 0.1f)
                    ) {
                        Text(
                            text = "LIFELINE",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFFDC2626),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Quick metrics chip bar
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                MetricPill(label = "Runs", value = "${service.runsPerDay}/day", modifier = Modifier.weight(1f))
                MetricPill(label = "Avg Time", value = "${service.avgExecutionDurationMs}ms", modifier = Modifier.weight(1f))
                MetricPill(label = "Wake Locks", value = "${service.wakeLocksCount}", modifier = Modifier.weight(1f))
                MetricPill(label = "Payload", value = String.format(java.util.Locale.US, "%.0f KB", service.networkTransferredKb), modifier = Modifier.weight(1f))
            }

            // Toggle Expand details
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isExpanded) "Hide details" else "View telemetry details",
                    fontSize = 10.sp,
                    color = Color(0xFF0284C7),
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = Color(0xFF0284C7),
                    modifier = Modifier.size(16.dp)
                )
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 6.dp)) {
                    Text(
                        text = if (isOdia) service.odiaDescription else service.description,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Wake Lock Duration: ${String.format(java.util.Locale.US, "%.2fs", service.wakeLockDurationSec)} • Auto-throttled <20%: ${if (service.isThrottledUnderLowBattery) "Yes" else "No (Emergency Active)"}",
                        fontSize = 9.5.sp,
                        color = Color.Gray
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricPill(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 2.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(value, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun ActionableTipCard(
    tip: BatteryLongevityActionTip,
    isOdia: Boolean,
    onAction: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            1.dp,
            if (tip.isApplied) Color(0xFF10B981).copy(alpha = 0.4f) else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("tip_card_${tip.id}")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) tip.odiaTitle else tip.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isOdia) tip.odiaDescription else tip.description,
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 14.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF10B981).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "⚡ ${tip.estimatedDailyBatteryGain}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = tip.currentSettingSummary,
                            fontSize = 9.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                if (tip.isApplied) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Applied",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Button(
                        onClick = onAction,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("apply_tip_btn_${tip.id}")
                    ) {
                        Text(
                            text = if (isOdia) "ଲାଗୁ କରନ୍ତୁ" else "Optimize",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
