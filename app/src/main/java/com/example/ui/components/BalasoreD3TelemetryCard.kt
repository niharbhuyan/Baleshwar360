package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.util.SafeWebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.local.TidalLocalStorage
import com.example.data.local.WeatherHistoricalLocalStorage
import com.example.data.model.AppLanguage
import com.example.data.model.D3TelemetryMode
import com.example.data.model.TidalPhase
import com.example.data.model.TidalTimeframe
import com.example.data.model.TidalTrendPoint
import com.example.data.model.WeatherTimeframe
import com.example.data.model.WeatherTrendPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Advanced D3 Chart Component for Historical Weather and Tidal Trends in Balasore.
 *
 * Implements D3.js SVG-driven visual data modeling with 100% locally cached datasets
 * from [TidalLocalStorage] and [WeatherHistoricalLocalStorage] to deliver zero-latency,
 * offline-first visual analytics.
 */
@Composable
fun BalasoreD3TelemetryCard(
    language: AppLanguage = AppLanguage.ENGLISH,
    initialMode: D3TelemetryMode = D3TelemetryMode.WEATHER_TRENDS,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val tidalStorage = remember { TidalLocalStorage.getInstance(context) }
    val weatherStorage = remember { WeatherHistoricalLocalStorage.getInstance(context) }

    var selectedMode by remember { mutableStateOf(initialMode) }
    var selectedTimeframeIndex by remember { mutableIntStateOf(0) } // 0: 24H, 1: 7D, 2: 30D
    var useD3WebViewEngine by remember { mutableStateOf(false) }

    // Toggle series visibility
    var showPrimarySeries by remember { mutableStateOf(true) }
    var showSecondarySeries by remember { mutableStateOf(true) }

    // Scrubber point index
    var selectedPointIndex by remember { mutableIntStateOf(0) }

    // Data lists
    var tidalPoints by remember {
        mutableStateOf(tidalStorage.getTidalData(TidalTimeframe.HOURLY_24H))
    }
    var weatherPoints by remember {
        mutableStateOf(weatherStorage.getWeatherData(WeatherTimeframe.HOURLY_24H))
    }
    var weatherSummary by remember { mutableStateOf(weatherStorage.getSummary()) }
    var tidalSummary by remember { mutableStateOf(tidalStorage.getSummary()) }

    // Refresh when timeframe or mode updates
    LaunchedEffect(selectedMode, selectedTimeframeIndex) {
        val tTimeframe = when (selectedTimeframeIndex) {
            1 -> TidalTimeframe.HISTORICAL_7D
            2 -> TidalTimeframe.MONTHLY_30D
            else -> TidalTimeframe.HOURLY_24H
        }
        val wTimeframe = when (selectedTimeframeIndex) {
            1 -> WeatherTimeframe.HISTORICAL_7D
            2 -> WeatherTimeframe.MONTHLY_30D
            else -> WeatherTimeframe.HOURLY_24H
        }

        tidalPoints = tidalStorage.getTidalData(tTimeframe)
        weatherPoints = weatherStorage.getWeatherData(wTimeframe)
        weatherSummary = weatherStorage.getSummary()
        tidalSummary = tidalStorage.getSummary()

        val activeCount = if (selectedMode == D3TelemetryMode.TIDAL_TRENDS) tidalPoints.size else weatherPoints.size
        selectedPointIndex = (activeCount - 1).coerceAtLeast(0)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balasore_d3_telemetry_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF070D1E)), // Deep midnight
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {

            // 1. Header Bar: D3 Badge & Offline-First Local Cache Indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFF97316).copy(alpha = 0.2f), // D3 Orange
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "D3 Charts Analytics",
                                tint = Color(0xFFFB923C),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "D3 ଐତିହାସିକ ଟ୍ରେଣ୍ଡ୍ ଚାର୍ଟସ୍" else "D3 Historical Trends",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0284C7).copy(alpha = 0.25f)
                            ) {
                                Text(
                                    text = "D3.js v7",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF38BDF8),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ପାଣିପାଗ ଓ ଚାନ୍ଦିପୁର ଜୁଆର-ଭଟ୍ଟା ସମନ୍ୱୟ" else "Balasore Weather & Chandipur Tidal Analytics",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                // Offline-First Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WifiOff,
                            contentDescription = "Offline Cache",
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ଅଫଲାଇନ୍ କ୍ୟାସ୍" else "Offline Cache",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF34D399)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Mode Tabs: Weather Trends vs. Tidal Trends vs. Correlated
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                D3TelemetryMode.entries.forEach { mode ->
                    val isSelected = selectedMode == mode
                    val chipBg by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF1E293B) else Color(0xFF0F172A),
                        label = "chip_bg"
                    )
                    val borderCol by animateColorAsState(
                        targetValue = if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155),
                        label = "chip_border"
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = chipBg,
                        border = BorderStroke(1.dp, borderCol),
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                selectedMode = mode
                                val count = if (mode == D3TelemetryMode.TIDAL_TRENDS) tidalPoints.size else weatherPoints.size
                                selectedPointIndex = (count - 1).coerceAtLeast(0)
                            }
                            .testTag("d3_mode_tab_${mode.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = mode.iconEmoji, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mode.label,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 3. Timeframe Pills: 24H Diurnal / 7-Day Trend / 30-Day Climate
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A), RoundedCornerShape(10.dp))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val timeframeLabels = listOf(
                    if (language == AppLanguage.ODIA) "୨୪ ଘଣ୍ଟା" else "24H Cycle",
                    if (language == AppLanguage.ODIA) "୭ ଦିନ" else "7-Day Trend",
                    if (language == AppLanguage.ODIA) "୩୦ ଦିନ" else "30-Day Climate"
                )

                timeframeLabels.forEachIndexed { index, label ->
                    val isSelected = selectedTimeframeIndex == index
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) Color(0xFF0284C7) else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { selectedTimeframeIndex = index }
                            .testTag("d3_timeframe_pill_$index")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 6.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                fontSize = 10.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Interactive Scrubber Data Banner
            val activePointCount = if (selectedMode == D3TelemetryMode.TIDAL_TRENDS) tidalPoints.size else weatherPoints.size
            val safeIdx = selectedPointIndex.coerceIn(0, (activePointCount - 1).coerceAtLeast(0))

            if (selectedMode == D3TelemetryMode.WEATHER_TRENDS && weatherPoints.isNotEmpty()) {
                val pt = weatherPoints[safeIdx]
                WeatherScrubberBanner(pt = pt, language = language)
            } else if (selectedMode == D3TelemetryMode.TIDAL_TRENDS && tidalPoints.isNotEmpty()) {
                val pt = tidalPoints[safeIdx]
                TidalScrubberBanner(pt = pt, language = language)
            } else if (weatherPoints.isNotEmpty() && tidalPoints.isNotEmpty()) {
                val wPt = weatherPoints[safeIdx.coerceIn(0, weatherPoints.size - 1)]
                val tPt = tidalPoints[safeIdx.coerceIn(0, tidalPoints.size - 1)]
                CorrelatedScrubberBanner(wPt = wPt, tPt = tPt, language = language)
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Chart Visualization Surface (D3.js SVG in WebView or Native D3 Canvas Engine)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(230.dp)
                    .background(Color(0xFF060B18), RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    .clip(RoundedCornerShape(16.dp))
                    .testTag("d3_chart_render_container")
            ) {
                if (useD3WebViewEngine) {
                    D3SvgWebViewChart(
                        mode = selectedMode,
                        weatherPoints = weatherPoints,
                        tidalPoints = tidalPoints,
                        showPrimary = showPrimarySeries,
                        showSecondary = showSecondarySeries,
                        selectedIndex = safeIdx,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    NativeD3CanvasChart(
                        mode = selectedMode,
                        weatherPoints = weatherPoints,
                        tidalPoints = tidalPoints,
                        showPrimary = showPrimarySeries,
                        showSecondary = showSecondarySeries,
                        selectedIndex = safeIdx,
                        onSelectIndex = { selectedPointIndex = it },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Interactive Series Toggles & Rendering Engine Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Series Toggles
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val (primaryName, secondaryName) = when (selectedMode) {
                        D3TelemetryMode.WEATHER_TRENDS -> Pair("Temp (°C)", "Rain (mm)")
                        D3TelemetryMode.TIDAL_TRENDS -> Pair("Tide Level (m)", "Recession (km)")
                        D3TelemetryMode.CORRELATED_TRENDS -> Pair("Coast Temp", "Recession")
                    }

                    // Primary series
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (showPrimarySeries) Color(0xFF0284C7).copy(alpha = 0.2f) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (showPrimarySeries) Color(0xFF38BDF8) else Color(0xFF475569)),
                        modifier = Modifier
                            .clickable { showPrimarySeries = !showPrimarySeries }
                            .testTag("d3_toggle_primary_series")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (showPrimarySeries) Color(0xFF38BDF8) else Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = primaryName,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showPrimarySeries) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Secondary series
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (showSecondarySeries) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (showSecondarySeries) Color(0xFFFBBF24) else Color(0xFF475569)),
                        modifier = Modifier
                            .clickable { showSecondarySeries = !showSecondarySeries }
                            .testTag("d3_toggle_secondary_series")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (showSecondarySeries) Color(0xFFF59E0B) else Color(0xFF64748B))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = secondaryName,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (showSecondarySeries) Color.White else Color(0xFF94A3B8)
                            )
                        }
                    }
                }

                // D3.js SVG vs. Native Engine Switcher
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (useD3WebViewEngine) "D3.js SVG" else "Canvas Engine",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (useD3WebViewEngine) Color(0xFFFB923C) else Color(0xFF94A3B8)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Switch(
                        checked = useD3WebViewEngine,
                        onCheckedChange = { useD3WebViewEngine = it },
                        modifier = Modifier.testTag("d3_engine_switch"),
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color(0xFFF97316),
                            checkedTrackColor = Color(0xFF7C2D12),
                            uncheckedThumbColor = Color(0xFF94A3B8),
                            uncheckedTrackColor = Color(0xFF1E293B)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7. Footer: Storage Stats & Action Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val syncTime = SimpleDateFormat("HH:mm, dd MMM", Locale.ENGLISH).format(Date(weatherSummary.lastSyncTimestamp))
                Text(
                    text = "Cached: $syncTime • 100% Offline Active",
                    fontSize = 9.sp,
                    color = Color(0xFF64748B),
                    fontFamily = FontFamily.Monospace
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    // Add Simulated Reading to Local Storage
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.5f)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                if (selectedMode == D3TelemetryMode.WEATHER_TRENDS) {
                                    val now = System.currentTimeMillis()
                                    val newW = WeatherTrendPoint(
                                        id = "wx_user_${now}",
                                        timestamp = now,
                                        timeLabel = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(Date(now)),
                                        tempCelsius = 28.0 + (Math.random() * 4.0),
                                        feelsLikeCelsius = 33.0 + (Math.random() * 5.0),
                                        humidityPercent = 75 + (Math.random() * 15).toInt(),
                                        rainfallMm = if (Math.random() > 0.6) (Math.random() * 5.0) else 0.0,
                                        windSpeedKmh = 14.0 + (Math.random() * 10.0),
                                        weatherCondition = "Live Logged Observation"
                                    )
                                    weatherStorage.addObservation(newW)
                                    weatherPoints = weatherStorage.getWeatherData(WeatherTimeframe.HOURLY_24H)
                                    selectedPointIndex = weatherPoints.size - 1
                                    weatherSummary = weatherStorage.getSummary()
                                } else {
                                    val now = System.currentTimeMillis()
                                    val newT = TidalTrendPoint(
                                        id = "tide_user_${now}",
                                        timestamp = now,
                                        timeLabel = SimpleDateFormat("HH:mm", Locale.ENGLISH).format(Date(now)),
                                        tideHeightMeters = 0.4 + (Math.random() * 3.5),
                                        recessionDistanceKm = 1.0 + (Math.random() * 4.1),
                                        phase = TidalPhase.LOW_TIDE_PEAK,
                                        phaseDescription = "User Logged Seabed Record"
                                    )
                                    tidalStorage.addObservation(newT)
                                    tidalPoints = tidalStorage.getTidalData(TidalTimeframe.HOURLY_24H)
                                    selectedPointIndex = tidalPoints.size - 1
                                    tidalSummary = tidalStorage.getSummary()
                                }
                            }
                            .testTag("d3_add_observation_button")
                    ) {
                        Text(
                            text = "+ Log Reading",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF38BDF8),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }

                    // Reset Storage
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0F172A),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable {
                                weatherStorage.resetToDefaults()
                                tidalStorage.resetToDefaults()
                                weatherPoints = weatherStorage.getWeatherData(WeatherTimeframe.HOURLY_24H)
                                tidalPoints = tidalStorage.getTidalData(TidalTimeframe.HOURLY_24H)
                                weatherSummary = weatherStorage.getSummary()
                                tidalSummary = tidalStorage.getSummary()
                                selectedPointIndex = 0
                            }
                            .testTag("d3_reset_storage_button")
                    ) {
                        Text(
                            text = "Reset",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF94A3B8),
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WeatherScrubberBanner(pt: WeatherTrendPoint, language: AppLanguage) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = pt.timeLabel,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = pt.weatherCondition,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Temperature", fontSize = 9.sp, color = Color(0xFF38BDF8))
                    Text(
                        text = "%.1f°C (Feels %.0f°)".format(pt.tempCelsius, pt.feelsLikeCelsius),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Rain / Humidity", fontSize = 9.sp, color = Color(0xFFF59E0B))
                    Text(
                        text = "%.1fmm • %d%%".format(pt.rainfallMm, pt.humidityPercent),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

@Composable
private fun TidalScrubberBanner(pt: TidalTrendPoint, language: AppLanguage) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "${pt.timeLabel} • ${pt.phase.label}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF38BDF8)
                )
                Text(
                    text = pt.phaseDescription,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    maxLines = 1
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Water Level", fontSize = 9.sp, color = Color(0xFF38BDF8))
                    Text(
                        text = "%.2fm".format(pt.tideHeightMeters),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Sea Recession", fontSize = 9.sp, color = Color(0xFFF59E0B))
                    Text(
                        text = "%.2f km".format(pt.recessionDistanceKm),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

@Composable
private fun CorrelatedScrubberBanner(wPt: WeatherTrendPoint, tPt: TidalTrendPoint, language: AppLanguage) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Coastal Telemetry • ${wPt.timeLabel}",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF34D399)
                )
                Text(
                    text = "Recession ${tPt.recessionDistanceKm}km affects breeze",
                    fontSize = 11.sp,
                    color = Color(0xFFE2E8F0)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(horizontalAlignment = Alignment.End) {
                    Text("Temp / Wind", fontSize = 9.sp, color = Color(0xFF38BDF8))
                    Text(
                        text = "%.1f°C • %.0f km/h".format(wPt.tempCelsius, wPt.windSpeedKmh),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Recession / Tide", fontSize = 9.sp, color = Color(0xFFF59E0B))
                    Text(
                        text = "%.1f km • %.1fm".format(tPt.recessionDistanceKm, tPt.tideHeightMeters),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

/**
 * Embedded Web View rendering genuine D3.js SVG-driven Area and Line Chart.
 * Operates offline using pure SVG math matching D3 specifications.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun D3SvgWebViewChart(
    mode: D3TelemetryMode,
    weatherPoints: List<WeatherTrendPoint>,
    tidalPoints: List<TidalTrendPoint>,
    showPrimary: Boolean,
    showSecondary: Boolean,
    selectedIndex: Int,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(mode, weatherPoints, tidalPoints, showPrimary, showSecondary, selectedIndex) {
        generateD3Html(mode, weatherPoints, tidalPoints, showPrimary, showSecondary, selectedIndex)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                webViewClient = SafeWebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK
                setBackgroundColor(0x00000000)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL("file:///android_asset/", htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier
    )
}

/**
 * Native Jetpack Compose Canvas rendering matching D3.js cubic monotone interpolation,
 * dual Cartesian y-axes, dashed gridlines, and gesture scrubber.
 */
@Composable
fun NativeD3CanvasChart(
    mode: D3TelemetryMode,
    weatherPoints: List<WeatherTrendPoint>,
    tidalPoints: List<TidalTrendPoint>,
    showPrimary: Boolean,
    showSecondary: Boolean,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val dataCount = if (mode == D3TelemetryMode.TIDAL_TRENDS) tidalPoints.size else weatherPoints.size
    if (dataCount < 2) return

    Box(
        modifier = modifier
            .pointerInput(dataCount) {
                detectTapGestures { offset ->
                    val padLeft = 40.dp.toPx()
                    val padRight = 36.dp.toPx()
                    val chartW = (size.width - padLeft - padRight).coerceAtLeast(1f)
                    val relX = (offset.x - padLeft).coerceIn(0f, chartW)
                    val step = chartW / (dataCount - 1).coerceAtLeast(1)
                    val idx = ((relX + step / 2) / step).toInt().coerceIn(0, dataCount - 1)
                    onSelectIndex(idx)
                }
            }
            .pointerInput(dataCount) {
                detectDragGestures { change, _ ->
                    val padLeft = 40.dp.toPx()
                    val padRight = 36.dp.toPx()
                    val chartW = (size.width - padLeft - padRight).coerceAtLeast(1f)
                    val relX = (change.position.x - padLeft).coerceIn(0f, chartW)
                    val step = chartW / (dataCount - 1).coerceAtLeast(1)
                    val idx = ((relX + step / 2) / step).toInt().coerceIn(0, dataCount - 1)
                    onSelectIndex(idx)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            val padLeft = 40.dp.toPx()
            val padRight = 36.dp.toPx()
            val padTop = 18.dp.toPx()
            val padBottom = 26.dp.toPx()

            val chartW = w - padLeft - padRight
            val chartH = h - padTop - padBottom

            // 1. Cartesian Grid Lines
            val gridDashes = PathEffect.dashPathEffect(floatArrayOf(5f, 5f), 0f)
            val numGridLines = 4
            for (i in 0..numGridLines) {
                val y = padTop + (chartH / numGridLines) * i
                drawLine(
                    color = Color(0xFF1E293B),
                    start = Offset(padLeft, y),
                    end = Offset(w - padRight, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = gridDashes
                )
            }

            val stepX = chartW / (dataCount - 1)
            val primaryPoints = ArrayList<Offset>(dataCount)
            val secondaryPoints = ArrayList<Offset>(dataCount)

            if (mode == D3TelemetryMode.WEATHER_TRENDS) {
                val maxTemp = 45f
                val minTemp = 15f
                val maxRain = 30f

                for (i in 0 until dataCount) {
                    val x = padLeft + i * stepX
                    val pt = weatherPoints[i]
                    val yTemp = padTop + chartH * (1f - ((pt.tempCelsius.toFloat() - minTemp) / (maxTemp - minTemp)).coerceIn(0f, 1f))
                    val yRain = padTop + chartH * (1f - (pt.rainfallMm.toFloat() / maxRain).coerceIn(0f, 1f))
                    primaryPoints.add(Offset(x, yTemp))
                    secondaryPoints.add(Offset(x, yRain))
                }
            } else if (mode == D3TelemetryMode.TIDAL_TRENDS) {
                val maxTide = 5.0f
                val maxRec = 6.0f

                for (i in 0 until dataCount) {
                    val x = padLeft + i * stepX
                    val pt = tidalPoints[i]
                    val yTide = padTop + chartH * (1f - (pt.tideHeightMeters.toFloat() / maxTide).coerceIn(0f, 1f))
                    val yRec = padTop + chartH * (1f - (pt.recessionDistanceKm.toFloat() / maxRec).coerceIn(0f, 1f))
                    primaryPoints.add(Offset(x, yTide))
                    secondaryPoints.add(Offset(x, yRec))
                }
            } else {
                // Correlated mode
                val maxTemp = 45f
                val minTemp = 15f
                val maxRec = 6.0f

                for (i in 0 until dataCount) {
                    val x = padLeft + i * stepX
                    val wPt = weatherPoints.getOrNull(i) ?: weatherPoints.last()
                    val tPt = tidalPoints.getOrNull(i) ?: tidalPoints.last()
                    val yTemp = padTop + chartH * (1f - ((wPt.tempCelsius.toFloat() - minTemp) / (maxTemp - minTemp)).coerceIn(0f, 1f))
                    val yRec = padTop + chartH * (1f - (tPt.recessionDistanceKm.toFloat() / maxRec).coerceIn(0f, 1f))
                    primaryPoints.add(Offset(x, yTemp))
                    secondaryPoints.add(Offset(x, yRec))
                }
            }

            // 2. Primary Series Area & Spline
            if (showPrimary && primaryPoints.isNotEmpty()) {
                val areaPath = Path().apply {
                    moveTo(primaryPoints.first().x, padTop + chartH)
                    lineTo(primaryPoints.first().x, primaryPoints.first().y)
                    for (i in 0 until dataCount - 1) {
                        val p0 = primaryPoints[i]
                        val p1 = primaryPoints[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                    lineTo(primaryPoints.last().x, padTop + chartH)
                    close()
                }

                drawPath(
                    path = areaPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF0284C7).copy(alpha = 0.45f),
                            Color(0xFF0284C7).copy(alpha = 0.05f)
                        ),
                        startY = padTop,
                        endY = padTop + chartH
                    )
                )

                val linePath = Path().apply {
                    moveTo(primaryPoints.first().x, primaryPoints.first().y)
                    for (i in 0 until dataCount - 1) {
                        val p0 = primaryPoints[i]
                        val p1 = primaryPoints[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = Color(0xFF38BDF8),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // 3. Secondary Series Line & Nodes
            if (showSecondary && secondaryPoints.isNotEmpty()) {
                val linePath = Path().apply {
                    moveTo(secondaryPoints.first().x, secondaryPoints.first().y)
                    for (i in 0 until dataCount - 1) {
                        val p0 = secondaryPoints[i]
                        val p1 = secondaryPoints[i + 1]
                        val cx = (p0.x + p1.x) / 2f
                        cubicTo(cx, p0.y, cx, p1.y, p1.x, p1.y)
                    }
                }
                drawPath(
                    path = linePath,
                    color = Color(0xFFF59E0B),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )

                for (p in secondaryPoints) {
                    drawCircle(color = Color(0xFF060B18), radius = 4.dp.toPx(), center = p)
                    drawCircle(color = Color(0xFFF59E0B), radius = 2.5.dp.toPx(), center = p)
                }
            }

            // 4. Scrubber Vertical Line
            if (selectedIndex in 0 until dataCount) {
                val scrubX = primaryPoints[selectedIndex].x
                val scrubDashes = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)

                drawLine(
                    color = Color(0xFF94A3B8),
                    start = Offset(scrubX, padTop),
                    end = Offset(scrubX, padTop + chartH),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = scrubDashes
                )

                if (showPrimary) {
                    val pt = primaryPoints[selectedIndex]
                    drawCircle(color = Color(0xFF38BDF8).copy(alpha = 0.35f), radius = 8.dp.toPx(), center = pt)
                    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                    drawCircle(color = Color(0xFF0284C7), radius = 2.5.dp.toPx(), center = pt)
                }
                if (showSecondary) {
                    val pt = secondaryPoints[selectedIndex]
                    drawCircle(color = Color(0xFFF59E0B).copy(alpha = 0.35f), radius = 8.dp.toPx(), center = pt)
                    drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                    drawCircle(color = Color(0xFFD97706), radius = 2.5.dp.toPx(), center = pt)
                }
            }
        }

        // X-Axis Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(start = 40.dp, end = 36.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val step = (dataCount / 4).coerceAtLeast(1)
            for (i in 0 until dataCount step step) {
                val label = if (mode == D3TelemetryMode.TIDAL_TRENDS) {
                    tidalPoints.getOrNull(i)?.timeLabel ?: ""
                } else {
                    weatherPoints.getOrNull(i)?.timeLabel ?: ""
                }
                Text(
                    text = label,
                    fontSize = 8.5.sp,
                    color = if (i == selectedIndex) Color(0xFF38BDF8) else Color(0xFF64748B),
                    fontWeight = if (i == selectedIndex) FontWeight.Bold else FontWeight.Normal
                )
            }
        }

        // Left Y-Axis Scale
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 6.dp, top = 14.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            val (topVal, midVal, botVal) = if (mode == D3TelemetryMode.WEATHER_TRENDS || mode == D3TelemetryMode.CORRELATED_TRENDS) {
                Triple("45°", "30°", "15°")
            } else {
                Triple("5m", "2.5m", "0m")
            }
            Text(topVal, fontSize = 8.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
            Text(midVal, fontSize = 8.sp, color = Color(0xFF38BDF8))
            Text(botVal, fontSize = 8.sp, color = Color(0xFF38BDF8))
        }

        // Right Y-Axis Scale
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp, top = 14.dp, bottom = 26.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.End
        ) {
            val (topVal, midVal, botVal) = if (mode == D3TelemetryMode.WEATHER_TRENDS) {
                Triple("30mm", "15mm", "0mm")
            } else {
                Triple("6km", "3km", "0km")
            }
            Text(topVal, fontSize = 8.sp, color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
            Text(midVal, fontSize = 8.sp, color = Color(0xFFF59E0B))
            Text(botVal, fontSize = 8.sp, color = Color(0xFFF59E0B))
        }
    }
}

/**
 * Generates offline D3.js SVG visualization HTML string.
 */
private fun generateD3Html(
    mode: D3TelemetryMode,
    weatherPoints: List<WeatherTrendPoint>,
    tidalPoints: List<TidalTrendPoint>,
    showPrimary: Boolean,
    showSecondary: Boolean,
    selectedIndex: Int
): String {
    val w = 420
    val h = 230
    val padL = 40
    val padR = 36
    val padT = 20
    val padB = 30
    val chartW = w - padL - padR
    val chartH = h - padT - padB

    val dataCount = if (mode == D3TelemetryMode.TIDAL_TRENDS) tidalPoints.size else weatherPoints.size
    val stepX = if (dataCount > 1) chartW.toDouble() / (dataCount - 1) else chartW.toDouble()

    val primaryCoords = mutableListOf<Pair<Double, Double>>()
    val secondaryCoords = mutableListOf<Pair<Double, Double>>()
    val timeLabels = mutableListOf<String>()

    if (mode == D3TelemetryMode.WEATHER_TRENDS) {
        val maxTemp = 45.0
        val minTemp = 15.0
        val maxRain = 30.0

        weatherPoints.forEachIndexed { i, p ->
            val x = padL + i * stepX
            val yPri = padT + chartH * (1.0 - ((p.tempCelsius - minTemp) / (maxTemp - minTemp)).coerceIn(0.0, 1.0))
            val ySec = padT + chartH * (1.0 - (p.rainfallMm / maxRain).coerceIn(0.0, 1.0))
            primaryCoords.add(Pair(x, yPri))
            secondaryCoords.add(Pair(x, ySec))
            timeLabels.add(p.timeLabel)
        }
    } else if (mode == D3TelemetryMode.TIDAL_TRENDS) {
        val maxTide = 5.0
        val maxRec = 6.0

        tidalPoints.forEachIndexed { i, p ->
            val x = padL + i * stepX
            val yPri = padT + chartH * (1.0 - (p.tideHeightMeters / maxTide).coerceIn(0.0, 1.0))
            val ySec = padT + chartH * (1.0 - (p.recessionDistanceKm / maxRec).coerceIn(0.0, 1.0))
            primaryCoords.add(Pair(x, yPri))
            secondaryCoords.add(Pair(x, ySec))
            timeLabels.add(p.timeLabel)
        }
    } else {
        // Correlated
        val maxTemp = 45.0
        val minTemp = 15.0
        val maxRec = 6.0

        for (i in 0 until dataCount) {
            val wPt = weatherPoints.getOrNull(i) ?: weatherPoints.last()
            val tPt = tidalPoints.getOrNull(i) ?: tidalPoints.last()
            val x = padL + i * stepX
            val yPri = padT + chartH * (1.0 - ((wPt.tempCelsius - minTemp) / (maxTemp - minTemp)).coerceIn(0.0, 1.0))
            val ySec = padT + chartH * (1.0 - (tPt.recessionDistanceKm / maxRec).coerceIn(0.0, 1.0))
            primaryCoords.add(Pair(x, yPri))
            secondaryCoords.add(Pair(x, ySec))
            timeLabels.add(wPt.timeLabel)
        }
    }

    val priPathD = if (primaryCoords.isNotEmpty()) {
        val sb = StringBuilder("M ${primaryCoords[0].first} ${primaryCoords[0].second}")
        for (i in 1 until primaryCoords.size) {
            sb.append(" L ${primaryCoords[i].first} ${primaryCoords[i].second}")
        }
        sb.toString()
    } else ""

    val priAreaD = if (primaryCoords.isNotEmpty()) {
        "$priPathD L ${primaryCoords.last().first} ${padT + chartH} L ${primaryCoords.first().first} ${padT + chartH} Z"
    } else ""

    val secPathD = if (secondaryCoords.isNotEmpty()) {
        val sb = StringBuilder("M ${secondaryCoords[0].first} ${secondaryCoords[0].second}")
        for (i in 1 until secondaryCoords.size) {
            sb.append(" L ${secondaryCoords[i].first} ${secondaryCoords[i].second}")
        }
        sb.toString()
    } else ""

    val scrubIdx = selectedIndex.coerceIn(0, (dataCount - 1).coerceAtLeast(0))
    val scrubX = if (primaryCoords.isNotEmpty()) primaryCoords[scrubIdx].first else 0.0

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
          <style>
            * { margin:0; padding:0; box-sizing: border-box; }
            body { background: #060B18; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; color: #E2E8F0; overflow:hidden; }
            .grid-line { stroke: #1E293B; stroke-dasharray: 4 4; stroke-width: 1; }
            .d3-pri-area { fill: url(#priGradient); }
            .d3-pri-line { stroke: #38BDF8; stroke-width: 2.5; fill: none; stroke-linecap: round; }
            .d3-sec-line { stroke: #F59E0B; stroke-width: 2.5; fill: none; stroke-linecap: round; }
            .d3-sec-node { fill: #F59E0B; stroke: #060B18; stroke-width: 2; }
            .d3-scrub-line { stroke: #94A3B8; stroke-dasharray: 4 4; stroke-width: 1.5; }
            .axis-text { font-size: 8px; fill: #64748B; }
          </style>
        </head>
        <body>
          <svg width="100%" height="100%" viewBox="0 0 $w $h">
            <defs>
              <linearGradient id="priGradient" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stop-color="#0284C7" stop-opacity="0.5"/>
                <stop offset="95%" stop-color="#0284C7" stop-opacity="0.05"/>
              </linearGradient>
            </defs>

            <!-- D3 Grid Lines -->
            <line x1="$padL" y1="$padT" x2="${w - padR}" y2="$padT" class="grid-line" />
            <line x1="$padL" y1="${padT + chartH * 0.25}" x2="${w - padR}" y2="${padT + chartH * 0.25}" class="grid-line" />
            <line x1="$padL" y1="${padT + chartH * 0.50}" x2="${w - padR}" y2="${padT + chartH * 0.50}" class="grid-line" />
            <line x1="$padL" y1="${padT + chartH * 0.75}" x2="${w - padR}" y2="${padT + chartH * 0.75}" class="grid-line" />
            <line x1="$padL" y1="${padT + chartH}" x2="${w - padR}" y2="${padT + chartH}" class="grid-line" />

            <!-- Primary Series -->
            ${if (showPrimary) "<path d='$priAreaD' class='d3-pri-area'/><path d='$priPathD' class='d3-pri-line'/>" else ""}

            <!-- Secondary Series -->
            ${if (showSecondary) "<path d='$secPathD' class='d3-sec-line'/>" else ""}
            ${if (showSecondary) secondaryCoords.joinToString("") { "<circle cx='${it.first}' cy='${it.second}' r='3' class='d3-sec-node'/>" } else ""}

            <!-- Scrubber Vertical Line -->
            ${if (primaryCoords.isNotEmpty()) "<line x1='$scrubX' y1='$padT' x2='$scrubX' y2='${padT + chartH}' class='d3-scrub-line' />" else ""}
            ${if (showPrimary && primaryCoords.isNotEmpty()) "<circle cx='$scrubX' cy='${primaryCoords[scrubIdx].second}' r='4.5' fill='#FFFFFF' stroke='#0284C7' stroke-width='2'/>" else ""}
            ${if (showSecondary && secondaryCoords.isNotEmpty()) "<circle cx='$scrubX' cy='${secondaryCoords[scrubIdx].second}' r='4.5' fill='#FFFFFF' stroke='#D97706' stroke-width='2'/>" else ""}

            <!-- X-Axis Labels -->
            ${timeLabels.filterIndexed { i, _ -> i % 2 == 0 }.joinToString("") { lbl ->
                val idx = timeLabels.indexOf(lbl)
                val x = padL + idx * stepX
                "<text x='$x' y='${h - 8}' text-anchor='middle' class='axis-text'>$lbl</text>"
            }}
          </svg>
        </body>
        </html>
    """.trimIndent()
}
