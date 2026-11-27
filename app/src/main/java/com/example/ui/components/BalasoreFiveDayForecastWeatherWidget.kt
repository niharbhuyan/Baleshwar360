package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.model.BalasoreForecastDay
import com.example.data.model.WeatherInfo
import com.example.data.remote.OpenWeatherDailyForecast
import com.example.ui.theme.LocalAppTheme
import com.example.util.BalasoreHapticHelper
import kotlin.math.roundToInt

/**
 * Dashboard Weather Summary Widget displaying a 5-day temperature forecast dual-area chart
 * (High & Low temperature curves, dashed Cartesian grid, Y-axis ticks, interactive scrub/tap
 * tooltip, and 5-day KPI metrics) built natively in Jetpack Compose Canvas.
 */
@Composable
fun BalasoreFiveDayForecastWeatherWidget(
    openWeatherForecast: List<OpenWeatherDailyForecast> = emptyList(),
    fallbackForecastDays: List<BalasoreForecastDay> = emptyList(),
    currentWeather: WeatherInfo? = null,
    language: AppLanguage = AppLanguage.ENGLISH,
    isFahrenheit: Boolean = false,
    onToggleTempUnit: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenFullWeather: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalAppTheme.current.isDark
    val isOdia = language == AppLanguage.ODIA

    val forecastPoints = remember(openWeatherForecast, fallbackForecastDays, currentWeather) {
        FiveDayForecastChartHelper.buildFiveDayPoints(
            openWeatherForecast = openWeatherForecast,
            fallbackForecastDays = fallbackForecastDays,
            currentWeather = currentWeather
        )
    }
    val summary = remember(forecastPoints) {
        FiveDayForecastChartHelper.summarize(forecastPoints)
    }

    var selectedDayIndex by remember(forecastPoints.size) { mutableIntStateOf(0) }
    val safeIndex = selectedDayIndex.coerceIn(0, (forecastPoints.size - 1).coerceAtLeast(0))
    val activeDay = forecastPoints.getOrNull(safeIndex)

    var animateChart by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animateChart = true }
    val chartProgress by animateFloatAsState(
        targetValue = if (animateChart) 1f else 0f,
        animationSpec = tween(durationMillis = 650),
        label = "five_day_forecast_reveal"
    )

    val highColor = Color(0xFFEA580C)
    val lowColor = Color(0xFF0284C7)
    val avgColor = Color(0xFF0D9488)
    val unitSuffix = if (isFahrenheit) "°F" else "°C"

    val headerGradient = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
            )
        } else {
            listOf(
                Color(0xFFE0F2FE),
                Color(0xFFFFF7ED)
            )
        }
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier
            .fillMaxWidth()
            .testTag("dashboard_five_day_weather_widget")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Atmospheric Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(headerGradient)
                    .padding(start = 16.dp, end = 8.dp, top = 14.dp, bottom = 12.dp),
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
                        color = highColor.copy(alpha = 0.16f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Thermostat,
                                contentDescription = null,
                                tint = highColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Column {
                        Text(
                            text = if (isOdia) "୫ ଦିନିଆ ତାପମାତ୍ରା ପୂର୍ବାନୁମାନ" else stringResource(R.string.weather_five_day_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = if (isOdia) {
                                "ବାଲେଶ୍ୱର • ଦୈନିକ ସର୍ବାଧିକ ଓ ସର୍ବନିମ୍ନ ତାପମାତ୍ରା"
                            } else {
                                stringResource(R.string.weather_five_day_subtitle)
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onToggleTempUnit()
                            }
                            .testTag("five_day_forecast_unit_toggle")
                    ) {
                        Text(
                            text = unitSuffix,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            onRefresh()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("five_day_forecast_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.weather_five_day_refresh_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            onOpenFullWeather()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("five_day_forecast_open_details_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                            contentDescription = stringResource(R.string.weather_five_day_open_full_cd),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 2. 5-Day Summary KPI Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ForecastSummaryKpiPill(
                        label = stringResource(R.string.weather_five_day_high_label),
                        value = FiveDayForecastChartHelper.formatTempWhole(summary.peakHighC, isFahrenheit),
                        subtext = summary.peakDayLabel,
                        accent = highColor,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    ForecastSummaryKpiPill(
                        label = stringResource(R.string.weather_five_day_low_label),
                        value = FiveDayForecastChartHelper.formatTempWhole(summary.lowestLowC, isFahrenheit),
                        subtext = "5-day min",
                        accent = lowColor,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                    ForecastSummaryKpiPill(
                        label = stringResource(R.string.weather_five_day_avg_label),
                        value = FiveDayForecastChartHelper.formatTempWhole(summary.fiveDayAvgC, isFahrenheit),
                        subtext = "${summary.maxRainChancePct}% max rain",
                        accent = avgColor,
                        isDark = isDark,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 3. Recharts-Style Legend Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ChartLegendDot(
                            color = highColor,
                            label = stringResource(R.string.weather_five_day_legend_high)
                        )
                        ChartLegendDot(
                            color = lowColor,
                            label = stringResource(R.string.weather_five_day_legend_low)
                        )
                    }
                    activeDay?.let { day ->
                        Text(
                            text = "${if (isOdia) day.dayShortOd else day.dayShortEn} • ${day.dateLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 4. Interactive Recharts-Style Dual-Area Temperature Chart Canvas
                FiveDayTemperatureAreaChartCanvas(
                    points = forecastPoints,
                    isFahrenheit = isFahrenheit,
                    selectedIndex = safeIndex,
                    revealProgress = chartProgress,
                    highColor = highColor,
                    lowColor = lowColor,
                    isDark = isDark,
                    onSelectIndex = { newIdx ->
                        if (newIdx != selectedDayIndex) {
                            BalasoreHapticHelper.vibrateMarkerTap(context)
                            selectedDayIndex = newIdx
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp)
                        .testTag("five_day_forecast_chart_canvas")
                )

                // 5. Clickable 5-Day X-Axis Strip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    forecastPoints.forEachIndexed { idx, day ->
                        val isSelected = idx == safeIndex
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                            },
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.outlineVariant
                                }
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 56.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    selectedDayIndex = idx
                                }
                                .testTag("five_day_forecast_day_pill_$idx")
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    text = if (isOdia) day.dayShortOd else day.dayShortEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = weatherConditionIcon(day.condition, day.rainChancePct),
                                    contentDescription = day.condition,
                                    tint = if (day.rainChancePct >= 45) lowColor else highColor,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${FiveDayForecastChartHelper.convertTemp(day.highTempC, isFahrenheit).roundToInt()}° / ${FiveDayForecastChartHelper.convertTemp(day.lowTempC, isFahrenheit).roundToInt()}°",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                // 6. Active Day Tooltip Detail Strip
                activeDay?.let { day ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("five_day_forecast_tooltip_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${if (isOdia) day.dayShortOd else day.dayShortEn} (${day.dateLabel}) • ${day.condition}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "High ${FiveDayForecastChartHelper.formatTempWhole(day.highTempC, isFahrenheit)}  •  Low ${FiveDayForecastChartHelper.formatTempWhole(day.lowTempC, isFahrenheit)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = highColor
                                )
                            }

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                TooltipStatChip(
                                    icon = Icons.Default.WaterDrop,
                                    value = "${day.rainChancePct}%",
                                    tint = lowColor
                                )
                                TooltipStatChip(
                                    icon = Icons.Default.Air,
                                    value = "${day.windKmh.roundToInt()} km/h",
                                    tint = avgColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FiveDayTemperatureAreaChartCanvas(
    points: List<FiveDayForecastPoint>,
    isFahrenheit: Boolean,
    selectedIndex: Int,
    revealProgress: Float,
    highColor: Color,
    lowColor: Color,
    isDark: Boolean,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val textMeasurer = rememberTextMeasurer()
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.55f else 0.85f)
    val axisTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    val dotHaloBg = MaterialTheme.colorScheme.surface
    val cursorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)

    val (minY, maxY) = remember(points, isFahrenheit) {
        FiveDayForecastChartHelper.computeYAxisBounds(points, isFahrenheit)
    }

    val highValues = remember(points, isFahrenheit) {
        points.map { FiveDayForecastChartHelper.convertTemp(it.highTempC, isFahrenheit).toFloat() }
    }
    val lowValues = remember(points, isFahrenheit) {
        points.map { FiveDayForecastChartHelper.convertTemp(it.lowTempC, isFahrenheit).toFloat() }
    }

    Canvas(
        modifier = modifier
            .pointerInput(points.size) {
                detectTapGestures { offset ->
                    val count = points.size.coerceAtLeast(1)
                    val leftPad = 42.dp.toPx()
                    val rightPad = 16.dp.toPx()
                    val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                    val fraction = ((offset.x - leftPad) / plotW).coerceIn(0f, 1f)
                    val idx = (fraction * (count - 1)).roundToInt().coerceIn(0, count - 1)
                    onSelectIndex(idx)
                }
            }
            .pointerInput(points.size) {
                detectHorizontalDragGestures { change, _ ->
                    val count = points.size.coerceAtLeast(1)
                    val leftPad = 42.dp.toPx()
                    val rightPad = 16.dp.toPx()
                    val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                    val fraction = ((change.position.x - leftPad) / plotW).coerceIn(0f, 1f)
                    val idx = (fraction * (count - 1)).roundToInt().coerceIn(0, count - 1)
                    onSelectIndex(idx)
                }
            }
    ) {
        if (points.isEmpty()) return@Canvas

        val leftPad = 40.dp.toPx()
        val rightPad = 16.dp.toPx()
        val topPad = 16.dp.toPx()
        val bottomPad = 24.dp.toPx()
        val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
        val plotH = (size.height - topPad - bottomPad).coerceAtLeast(1f)
        val ySpan = (maxY - minY).coerceAtLeast(1f)

        val axisStyle = TextStyle(
            color = axisTextColor,
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )

        // 1. Dashed Recharts CartesianGrid + Y-Axis Temperature Labels
        val gridSteps = 4
        for (step in 0..gridSteps) {
            val fraction = step.toFloat() / gridSteps.toFloat()
            val y = topPad + plotH * fraction
            val tickValue = (maxY - ySpan * fraction).roundToInt()

            drawLine(
                color = gridColor,
                start = Offset(leftPad, y),
                end = Offset(leftPad + plotW, y),
                strokeWidth = 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
            )

            val measured = textMeasurer.measure("$tickValue°", style = axisStyle)
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = (leftPad - measured.size.width - 6.dp.toPx()).coerceAtLeast(0f),
                    y = y - measured.size.height / 2f
                )
            )
        }

        val count = points.size
        fun toX(index: Int): Float {
            return if (count <= 1) leftPad + plotW / 2f
            else leftPad + (plotW * index.toFloat() / (count - 1).toFloat())
        }

        fun toY(value: Float): Float {
            val normalized = ((value - minY) / ySpan).coerceIn(0f, 1f)
            val animatedNormalized = normalized * revealProgress
            return topPad + plotH * (1f - animatedNormalized)
        }

        val highOffsets = highValues.mapIndexed { idx, v -> Offset(toX(idx), toY(v)) }
        val lowOffsets = lowValues.mapIndexed { idx, v -> Offset(toX(idx), toY(v)) }
        val baselineY = topPad + plotH

        fun buildSmoothLinePath(coords: List<Offset>): Path {
            return Path().apply {
                if (coords.isEmpty()) return@apply
                moveTo(coords.first().x, coords.first().y)
                for (i in 0 until coords.size - 1) {
                    val p0 = coords[i]
                    val p1 = coords[i + 1]
                    val midX = (p0.x + p1.x) / 2f
                    cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                }
            }
        }

        // 2. Smooth Area Fills (High & Low)
        val highLinePath = buildSmoothLinePath(highOffsets)
        val highAreaPath = Path().apply {
            addPath(highLinePath)
            lineTo(highOffsets.last().x, baselineY)
            lineTo(highOffsets.first().x, baselineY)
            close()
        }
        drawPath(
            path = highAreaPath,
            brush = Brush.verticalGradient(
                colors = listOf(highColor.copy(alpha = 0.28f), highColor.copy(alpha = 0.02f)),
                startY = topPad,
                endY = baselineY
            )
        )

        val lowLinePath = buildSmoothLinePath(lowOffsets)
        val lowAreaPath = Path().apply {
            addPath(lowLinePath)
            lineTo(lowOffsets.last().x, baselineY)
            lineTo(lowOffsets.first().x, baselineY)
            close()
        }
        drawPath(
            path = lowAreaPath,
            brush = Brush.verticalGradient(
                colors = listOf(lowColor.copy(alpha = 0.24f), lowColor.copy(alpha = 0.02f)),
                startY = topPad,
                endY = baselineY
            )
        )

        // 3. Recharts Vertical Cursor Reference Line on Selected Day
        val activeX = toX(selectedIndex.coerceIn(0, count - 1))
        drawLine(
            color = cursorColor,
            start = Offset(activeX, topPad),
            end = Offset(activeX, baselineY),
            strokeWidth = 1.5.dp.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
        )

        // 4. Crisp High & Low Temperature Curve Strokes
        drawPath(
            path = highLinePath,
            color = highColor,
            style = Stroke(
                width = 3.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )
        drawPath(
            path = lowLinePath,
            color = lowColor,
            style = Stroke(
                width = 2.6.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        // 5. Data Point Nodes + Value Callouts on Active Day
        highOffsets.forEachIndexed { idx, pt ->
            val isSelected = idx == selectedIndex
            if (isSelected) {
                drawCircle(color = highColor.copy(alpha = 0.22f), radius = 10.dp.toPx(), center = pt)
            }
            drawCircle(color = dotHaloBg, radius = (if (isSelected) 6.dp else 4.5.dp).toPx(), center = pt)
            drawCircle(color = highColor, radius = (if (isSelected) 4.2.dp else 3.dp).toPx(), center = pt)

            if (isSelected) {
                val label = "${highValues[idx].roundToInt()}°"
                val measured = textMeasurer.measure(
                    label,
                    style = TextStyle(color = highColor, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
                )
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        x = (pt.x - measured.size.width / 2f).coerceIn(leftPad, size.width - measured.size.width),
                        y = (pt.y - measured.size.height - 6.dp.toPx()).coerceAtLeast(0f)
                    )
                )
            }
        }

        lowOffsets.forEachIndexed { idx, pt ->
            val isSelected = idx == selectedIndex
            if (isSelected) {
                drawCircle(color = lowColor.copy(alpha = 0.22f), radius = 9.dp.toPx(), center = pt)
            }
            drawCircle(color = dotHaloBg, radius = (if (isSelected) 5.5.dp else 4.dp).toPx(), center = pt)
            drawCircle(color = lowColor, radius = (if (isSelected) 3.8.dp else 2.8.dp).toPx(), center = pt)

            if (isSelected) {
                val label = "${lowValues[idx].roundToInt()}°"
                val measured = textMeasurer.measure(
                    label,
                    style = TextStyle(color = lowColor, fontSize = 10.5.sp, fontWeight = FontWeight.ExtraBold)
                )
                drawText(
                    textLayoutResult = measured,
                    topLeft = Offset(
                        x = (pt.x - measured.size.width / 2f).coerceIn(leftPad, size.width - measured.size.width),
                        y = (pt.y + 6.dp.toPx()).coerceAtMost(baselineY - measured.size.height)
                    )
                )
            }
        }

        // 6. X-Axis Day Labels below baseline
        points.forEachIndexed { idx, day ->
            val measured = textMeasurer.measure(
                day.dayShortEn,
                style = axisStyle.copy(
                    fontWeight = if (idx == selectedIndex) FontWeight.ExtraBold else FontWeight.Medium,
                    color = if (idx == selectedIndex) highColor else axisTextColor
                )
            )
            drawText(
                textLayoutResult = measured,
                topLeft = Offset(
                    x = (toX(idx) - measured.size.width / 2f).coerceIn(leftPad - 4.dp.toPx(), size.width - measured.size.width),
                    y = baselineY + 6.dp.toPx()
                )
            )
        }
    }
}

@Composable
private fun ForecastSummaryKpiPill(
    label: String,
    value: String,
    subtext: String,
    accent: Color,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = accent.copy(alpha = if (isDark) 0.16f else 0.09f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = accent,
                maxLines = 1
            )
            Text(
                text = subtext,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChartLegendDot(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TooltipStatChip(
    icon: ImageVector,
    value: String,
    tint: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun weatherConditionIcon(condition: String, rainChancePct: Int): ImageVector {
    val c = condition.lowercase()
    return when {
        rainChancePct >= 50 || c.contains("rain") || c.contains("shower") || c.contains("storm") ->
            Icons.Default.WaterDrop
        c.contains("cloud") || c.contains("overcast") || c.contains("mist") ->
            Icons.Default.Cloud
        else -> Icons.Default.WbSunny
    }
}
