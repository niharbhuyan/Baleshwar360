package com.example.ui.features.coastal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChandipurTideEntity
import com.example.data.model.AppLanguage
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

enum class OfflineTrendChartMode {
    TEMPERATURE,
    TIDAL
}

private data class OfflineChartPoint(
    val dayLabel: String,
    val primaryValue: Float,
    val secondaryValue: Float,
    val primaryDisplay: String,
    val secondaryDisplay: String,
    val note: String
)

/**
 * Interactive Compose Canvas area/line chart (Recharts-inspired visual style) embedded in
 * Balasore's offline weather & tide views. Lets users switch between 7-day Historical Temperature
 * trends (°C) and Chandipur Tidal Trends (meters & seabed recession km), with tap-to-inspect tooltips.
 */
@Composable
fun BalasoreOfflineWeatherTideChartCard(
    tideSchedule: List<ChandipurTideEntity> = emptyList(),
    language: AppLanguage = AppLanguage.ENGLISH,
    initialMode: OfflineTrendChartMode = OfflineTrendChartMode.TIDAL,
    modifier: Modifier = Modifier
) {
    val isOdia = language == AppLanguage.ODIA
    var mode by remember { mutableStateOf(initialMode) }
    var selectedIndex by remember(mode) { mutableIntStateOf(0) }

    val temperaturePoints = remember {
        listOf(
            OfflineChartPoint("D-6", 31.4f, 24.6f, "31.4°C High", "24.6°C Low", "Coastal breeze • 74% RH"),
            OfflineChartPoint("D-5", 32.1f, 25.0f, "32.1°C High", "25.0°C Low", "Partly cloudy • Clear evening"),
            OfflineChartPoint("D-4", 33.0f, 25.8f, "33.0°C High", "25.8°C Low", "Warm afternoon • UV 8"),
            OfflineChartPoint("D-3", 30.2f, 24.1f, "30.2°C High", "24.1°C Low", "Bay shower • 14mm rain"),
            OfflineChartPoint("D-2", 29.6f, 23.8f, "29.6°C High", "23.8°C Low", "Cool sea breeze • AQI Good"),
            OfflineChartPoint("D-1", 31.8f, 24.9f, "31.8°C High", "24.9°C Low", "Humid • Moderate swells"),
            OfflineChartPoint("Today", 32.5f, 25.3f, "32.5°C High", "25.3°C Low", "Cached offline trend ready")
        )
    }

    val tidalPoints = remember(tideSchedule) {
        if (tideSchedule.size >= 7) {
            tideSchedule.take(7).mapIndexed { idx, entry ->
                val highLevel = (entry.currentWaterLevelMeters.toFloat() + 1.4f).coerceIn(2.1f, 4.6f)
                val lowLevel = (entry.currentWaterLevelMeters.toFloat() * 0.45f).coerceIn(0.4f, 1.8f)
                val label = "D+${idx + 1}"
                OfflineChartPoint(
                    dayLabel = label,
                    primaryValue = highLevel,
                    secondaryValue = lowLevel,
                    primaryDisplay = String.format(Locale.US, "%.2fm High Tide", highLevel),
                    secondaryDisplay = String.format(Locale.US, "%.1f km Recede", entry.recededDistanceKm),
                    note = if (entry.safeWalkStatus.contains("SAFE", ignoreCase = true)) {
                        "Safe Walk Window (${entry.safeWalkMinutesRemaining}m)"
                    } else {
                        "High Swell Caution • Return Early"
                    }
                )
            }
        } else {
            listOf(
                OfflineChartPoint("D+1", 3.2f, 0.7f, "3.20m High Tide", "4.6 km Recede", "Safe Walk Window (145m)"),
                OfflineChartPoint("D+2", 3.6f, 0.9f, "3.60m High Tide", "4.3 km Recede", "Safe Walk Window (130m)"),
                OfflineChartPoint("D+3", 4.1f, 1.1f, "4.10m High Tide", "3.4 km Recede", "Spring Tide Swell • Watch"),
                OfflineChartPoint("D+4", 4.3f, 1.2f, "4.30m High Tide", "3.1 km Recede", "Warning Zone • Rapid Ingress"),
                OfflineChartPoint("D+5", 3.7f, 0.8f, "3.70m High Tide", "4.2 km Recede", "Safe Walk Window (125m)"),
                OfflineChartPoint("D+6", 3.3f, 0.7f, "3.30m High Tide", "4.7 km Recede", "Safe Walk Window (150m)"),
                OfflineChartPoint("D+7", 3.1f, 0.6f, "3.10m High Tide", "4.9 km Recede", "Optimal Vanishing Sea Walk")
            )
        }
    }

    val points = if (mode == OfflineTrendChartMode.TEMPERATURE) temperaturePoints else tidalPoints
    val safeSelectedIndex = selectedIndex.coerceIn(0, points.lastIndex)
    val selectedPoint = points[safeSelectedIndex]

    val primaryColor = if (mode == OfflineTrendChartMode.TEMPERATURE) {
        Color(0xFFEA580C)
    } else {
        Color(0xFF0284C7)
    }
    val secondaryColor = if (mode == OfflineTrendChartMode.TEMPERATURE) {
        Color(0xFF2563EB)
    } else {
        Color(0xFF0D9488)
    }

    val minVal = remember(points, mode) {
        if (mode == OfflineTrendChartMode.TEMPERATURE) 20f else 0f
    }
    val maxVal = remember(points, mode) {
        if (mode == OfflineTrendChartMode.TEMPERATURE) 38f else 5f
    }
    val unitSuffix = if (mode == OfflineTrendChartMode.TEMPERATURE) "°C" else "m"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_weather_tide_trend_chart_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header + Segmented Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) {
                            "ବାଲେଶ୍ୱର ଅଫଲାଇନ୍ ପାଣିପାଗ ଓ ଜୁଆର ଚାର୍ଟ"
                        } else {
                            "Balasore Offline Trends Chart"
                        },
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = if (mode == OfflineTrendChartMode.TEMPERATURE) {
                            "7-Day Historical Temperature (°C) • Tap any point"
                        } else {
                            "7-Day Chandipur Tidal Height (m) • Tap any point"
                        },
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = mode == OfflineTrendChartMode.TEMPERATURE,
                    onClick = { mode = OfflineTrendChartMode.TEMPERATURE },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.DeviceThermostat,
                            contentDescription = "Historical Temperature Chart",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (isOdia) "ତାପମାତ୍ରା (°C)" else "Temperature (°C)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFFFEDD5),
                        selectedLabelColor = Color(0xFF9A3412),
                        selectedLeadingIconColor = Color(0xFFEA580C)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("offline_chart_tab_temperature")
                )

                FilterChip(
                    selected = mode == OfflineTrendChartMode.TIDAL,
                    onClick = { mode = OfflineTrendChartMode.TIDAL },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Waves,
                            contentDescription = "Tidal Trends Chart",
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = if (isOdia) "ଜୁଆର ଟ୍ରେଣ୍ଡ (m)" else "Tidal Trends (m)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFE0F2FE),
                        selectedLabelColor = Color(0xFF075985),
                        selectedLeadingIconColor = Color(0xFF0284C7)
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("offline_chart_tab_tidal")
                )
            }

            // Interactive Tooltip Card for Selected Data Point
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = primaryColor.copy(alpha = 0.08f),
                border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_chart_tooltip")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${selectedPoint.dayLabel} • ${selectedPoint.primaryDisplay}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.5.sp,
                            color = primaryColor
                        )
                        Text(
                            text = selectedPoint.note,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = secondaryColor.copy(alpha = 0.14f)
                    ) {
                        Text(
                            text = selectedPoint.secondaryDisplay,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = secondaryColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Responsive Canvas Area + Dual-Line Chart
            val gridLineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f)
            val surfaceBg = MaterialTheme.colorScheme.surface

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Y-Axis Ticks
                Column(
                    modifier = Modifier
                        .width(36.dp)
                        .height(152.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    val midVal = ((maxVal + minVal) / 2f).roundToInt()
                    Text("${maxVal.roundToInt()}$unitSuffix", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("$midVal$unitSuffix", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("${minVal.roundToInt()}$unitSuffix", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.width(6.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(points) {
                                detectTapGestures { offset ->
                                    if (points.size > 1 && size.width > 0) {
                                        val padX = 16f
                                        val usableW = (size.width - padX * 2f).coerceAtLeast(1f)
                                        val stepX = usableW / (points.size - 1)
                                        var nearest = 0
                                        var minDist = Float.MAX_VALUE
                                        for (i in points.indices) {
                                            val px = padX + i * stepX
                                            val d = abs(offset.x - px)
                                            if (d < minDist) {
                                                minDist = d
                                                nearest = i
                                            }
                                        }
                                        selectedIndex = nearest
                                    }
                                }
                            }
                            .testTag("offline_trend_canvas")
                    ) {
                        val padX = 16.dp.toPx()
                        val padTop = 12.dp.toPx()
                        val padBottom = 14.dp.toPx()
                        val chartW = (size.width - padX * 2f).coerceAtLeast(1f)
                        val chartH = (size.height - padTop - padBottom).coerceAtLeast(1f)
                        val span = (maxVal - minVal).coerceAtLeast(1f)

                        fun xFor(index: Int): Float =
                            if (points.size <= 1) padX + chartW / 2f
                            else padX + (index.toFloat() / (points.size - 1)) * chartW

                        fun yFor(value: Float): Float {
                            val norm = ((value - minVal) / span).coerceIn(0f, 1f)
                            return padTop + (1f - norm) * chartH
                        }

                        // Dashed Horizontal Gridlines
                        val dash = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        listOf(0f, 0.5f, 1f).forEach { frac ->
                            val y = padTop + frac * chartH
                            drawLine(
                                color = gridLineColor,
                                start = Offset(padX, y),
                                end = Offset(padX + chartW, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = dash
                            )
                        }

                        val primaryCoords = points.mapIndexed { i, p -> Offset(xFor(i), yFor(p.primaryValue)) }
                        val secondaryCoords = points.mapIndexed { i, p -> Offset(xFor(i), yFor(p.secondaryValue)) }

                        // Smooth Cubic Bezier Primary Path + Gradient Area Fill
                        val primaryLine = Path().apply {
                            primaryCoords.forEachIndexed { i, pt ->
                                if (i == 0) moveTo(pt.x, pt.y)
                                else {
                                    val prev = primaryCoords[i - 1]
                                    val midX = (prev.x + pt.x) / 2f
                                    cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                                }
                            }
                        }

                        val primaryArea = Path().apply {
                            addPath(primaryLine)
                            lineTo(primaryCoords.last().x, padTop + chartH)
                            lineTo(primaryCoords.first().x, padTop + chartH)
                            close()
                        }

                        drawPath(
                            path = primaryArea,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.28f),
                                    primaryColor.copy(alpha = 0.02f)
                                ),
                                startY = padTop,
                                endY = padTop + chartH
                            )
                        )

                        drawPath(
                            path = primaryLine,
                            color = primaryColor,
                            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                        )

                        // Smooth Secondary Series Path (Low Temp / Low Tide)
                        val secondaryLine = Path().apply {
                            secondaryCoords.forEachIndexed { i, pt ->
                                if (i == 0) moveTo(pt.x, pt.y)
                                else {
                                    val prev = secondaryCoords[i - 1]
                                    val midX = (prev.x + pt.x) / 2f
                                    cubicTo(midX, prev.y, midX, pt.y, pt.x, pt.y)
                                }
                            }
                        }
                        drawPath(
                            path = secondaryLine,
                            color = secondaryColor,
                            style = Stroke(
                                width = 2.dp.toPx(),
                                cap = StrokeCap.Round,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                            )
                        )

                        // Selected Vertical Reference Line
                        val activePt = primaryCoords[safeSelectedIndex]
                        drawLine(
                            color = primaryColor.copy(alpha = 0.45f),
                            start = Offset(activePt.x, padTop),
                            end = Offset(activePt.x, padTop + chartH),
                            strokeWidth = 1.5.dp.toPx()
                        )

                        // Data Point Dots
                        primaryCoords.forEachIndexed { i, pt ->
                            val isSel = i == safeSelectedIndex
                            drawCircle(
                                color = surfaceBg,
                                radius = if (isSel) 7.dp.toPx() else 4.5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = if (isSel) 5.dp.toPx() else 3.dp.toPx(),
                                center = pt
                            )
                        }
                    }
                }
            }

            // X-Axis Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 42.dp, end = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                points.forEachIndexed { idx, pt ->
                    Text(
                        text = pt.dayLabel,
                        fontSize = 10.sp,
                        fontWeight = if (idx == safeSelectedIndex) FontWeight.Bold else FontWeight.Normal,
                        color = if (idx == safeSelectedIndex) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
