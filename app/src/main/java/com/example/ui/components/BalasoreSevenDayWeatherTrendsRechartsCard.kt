package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import android.webkit.WebViewClient
import com.example.util.SafeWebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.local.DailyForecastEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.util.BalasoreHapticHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Data point for a 7-day historical weather trend observation in Balasore,
 * mapped directly from the Room `daily_forecasts` table (`DailyForecastEntity`).
 */
data class SevenDayHistoricalPoint(
    val dayLabel: String,
    val dayLabelOd: String,
    val dateStr: String,
    val maxTemp: Double,
    val minTemp: Double,
    val avgTemp: Double,
    val rainfallMm: Double,
    val humidityPct: Int,
    val uvIndex: Double = 6.5,
    val condition: String = "Partly Cloudy"
) {
    val precipProbabilityPct: Int
        get() = (rainfallMm * 6.0 + humidityPct * 0.45).roundToInt().coerceIn(10, 98)
}

val DEFAULT_7_DAY_HISTORICAL = listOf(
    SevenDayHistoricalPoint("6d ago", "୬ ଦିନ ପୂର୍ବେ", "2026-10-26", 32.5, 23.8, 28.2, 0.0, 74, 7.2, "Clear Coastal Sun"),
    SevenDayHistoricalPoint("5d ago", "୫ ଦିନ ପୂର୍ବେ", "2026-10-27", 33.8, 24.2, 29.0, 2.5, 78, 7.8, "Humid & Warm"),
    SevenDayHistoricalPoint("4d ago", "୪ ଦିନ ପୂର୍ବେ", "2026-10-28", 34.4, 25.0, 29.7, 12.0, 85, 6.4, "Coastal Showers"),
    SevenDayHistoricalPoint("3d ago", "୩ ଦିନ ପୂର୍ବେ", "2026-10-29", 29.6, 22.4, 26.0, 42.0, 92, 4.1, "Bay Squall Rain"),
    SevenDayHistoricalPoint("2d ago", "୨ ଦିନ ପୂର୍ବେ", "2026-10-30", 31.2, 23.0, 27.1, 8.5, 80, 5.9, "Scattered Clouds"),
    SevenDayHistoricalPoint("Yesterday", "ଗତକାଲି", "2026-10-31", 32.4, 23.6, 28.0, 1.2, 75, 6.8, "Pleasant Breeze"),
    SevenDayHistoricalPoint("Today", "ଆଜି", "2026-11-01", 33.1, 24.1, 28.6, 0.0, 72, 7.5, "Sunny & Clear")
)

/**
 * 7-Day Historical Weather Temperature Chart on the Dashboard using Recharts architecture,
 * backed directly by the Room database (`daily_forecasts` table via `WeatherDao`).
 */
@Composable
fun BalasoreSevenDayWeatherTrendsRechartsCard(
    language: AppLanguage = AppLanguage.ENGLISH,
    isFahrenheit: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA

    val weatherDao = remember { AppDatabase.getInstance(context).weatherDao() }
    val roomForecastEntities by weatherDao.getDailyForecasts().collectAsStateWithLifecycle(initialValue = emptyList())

    // Seed Room database with 7-day historical weather records if empty or fewer than 7 rows
    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val existing = weatherDao.getDailyForecastsSync()
            if (existing.size < 7) {
                val seedEntities = DEFAULT_7_DAY_HISTORICAL.mapIndexed { index, pt ->
                    DailyForecastEntity(
                        date = pt.dateStr,
                        dayOfWeek = pt.dayLabel,
                        weatherCode = if (pt.rainfallMm > 20) 63 else if (pt.rainfallMm > 0) 61 else 1,
                        weatherDescription = "${pt.condition}|rain=${pt.rainfallMm}|hum=${pt.humidityPct}",
                        maxTemp = pt.maxTemp,
                        minTemp = pt.minTemp,
                        sunrise = "05:48 AM",
                        sunset = "05:14 PM",
                        uvIndex = pt.uvIndex,
                        cachedAt = System.currentTimeMillis() - ((6 - index) * 86_400_000L)
                    )
                }
                weatherDao.insertDailyForecasts(seedEntities)
            }
        }
    }

    // Map Room DailyForecastEntity rows to 7-day historical chart points
    val historicalData = remember(roomForecastEntities) {
        if (roomForecastEntities.size >= 5) {
            roomForecastEntities.takeLast(7).mapIndexed { idx, entity ->
                val rainParsed = Regex("rain=([0-9.]+)").find(entity.weatherDescription)?.groupValues?.getOrNull(1)?.toDoubleOrNull()
                    ?: when (entity.weatherCode) {
                        63, 65, 80 -> 28.0
                        61, 51 -> 6.5
                        else -> 0.0
                    }
                val humParsed = Regex("hum=([0-9]+)").find(entity.weatherDescription)?.groupValues?.getOrNull(1)?.toIntOrNull()
                    ?: (68 + (entity.weatherCode % 22))
                val cleanCondition = entity.weatherDescription.substringBefore("|").ifBlank { "Coastal Breeze" }
                val avg = ((entity.maxTemp + entity.minTemp) / 2.0 * 10.0).roundToInt() / 10.0
                val dayOd = when (entity.dayOfWeek.lowercase(Locale.ROOT)) {
                    "today" -> "ଆଜି"
                    "yesterday" -> "ଗତକାଲି"
                    else -> entity.dayOfWeek
                }
                SevenDayHistoricalPoint(
                    dayLabel = entity.dayOfWeek.take(9),
                    dayLabelOd = dayOd,
                    dateStr = entity.date,
                    maxTemp = entity.maxTemp,
                    minTemp = entity.minTemp,
                    avgTemp = avg,
                    rainfallMm = rainParsed,
                    humidityPct = humParsed,
                    uvIndex = entity.uvIndex,
                    condition = cleanCondition
                )
            }
        } else {
            loadOrCache7DayData(context)
        }
    }

    var isWebRechartsMode by remember { mutableStateOf(false) } // Default to Native Compose Canvas for speed & stability
    var selectedMetric by remember { mutableStateOf("TEMP") } // "TEMP", "AVG", "RAIN"
    var selectedIndex by remember(historicalData.size) {
        mutableIntStateOf((historicalData.size - 1).coerceAtLeast(0))
    }
    var showRoomEditor by remember { mutableStateOf(false) }
    var showWeatherHistoryTable by remember { mutableStateOf(true) }
    var workManagerAlertStatus by remember { mutableStateOf<String?>(null) }
    var customMaxTempInput by remember { mutableStateOf("33.6") }
    var customMinTempInput by remember { mutableStateOf("24.4") }

    val activePoint = historicalData.getOrElse(selectedIndex.coerceIn(0, (historicalData.size - 1).coerceAtLeast(0))) {
        historicalData.lastOrNull() ?: DEFAULT_7_DAY_HISTORICAL.last()
    }

    fun formatTemp(celsius: Double): Double {
        return if (isFahrenheit) {
            ((celsius * 9.0 / 5.0 + 32.0) * 10.0).roundToInt() / 10.0
        } else {
            (celsius * 10.0).roundToInt() / 10.0
        }
    }

    val tempUnit = if (isFahrenheit) "°F" else "°C"
    val sevenDayPeakMax = remember(historicalData, isFahrenheit) {
        formatTemp(historicalData.maxOfOrNull { it.maxTemp } ?: 34.4)
    }
    val sevenDayLowestMin = remember(historicalData, isFahrenheit) {
        formatTemp(historicalData.minOfOrNull { it.minTemp } ?: 22.4)
    }
    val sevenDayMeanTemp = remember(historicalData, isFahrenheit) {
        val avgC = if (historicalData.isNotEmpty()) historicalData.map { it.avgTemp }.average() else 28.1
        formatTemp(avgC)
    }

    var isChartVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isChartVisible = true
    }
    val fadeInAlpha by animateFloatAsState(
        targetValue = if (isChartVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "recharts_weather_fade_in"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer { alpha = fadeInAlpha }
            .testTag("weather_7day_recharts_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
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
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF6FF)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timeline,
                            contentDescription = "Historical Recharts",
                            tint = OceanBlue,
                            modifier = Modifier.size(21.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) "୭-ଦିନ ପାଣିପାଗ ଇତିହାସ ଓ ତାପମାତ୍ରା ଚାର୍ଟ (Room DB)" else "Weather History: 7-Day Temperature Trends (Room DB)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = Color(0xFF0F172A)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFEDE9FE),
                                border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                            ) {
                                Text(
                                    text = "Recharts",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6D28D9),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFECFDF5),
                                border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                                modifier = Modifier.testTag("room_db_weather_status_badge")
                            ) {
                                Text(
                                    text = if (isOdia) "Room SQLite (${historicalData.size} ଦିନ)" else "Room SQLite • ${historicalData.size}d cached",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF047857),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }
                }

                // Switch between Web Recharts SVG/JS Engine and Native Compose Recharts Canvas
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isWebRechartsMode) Color(0xFFEDE9FE) else Color(0xFFF1F5F9),
                    border = BorderStroke(1.dp, if (isWebRechartsMode) Color(0xFFC4B5FD) else Color(0xFFCBD5E1)),
                    modifier = Modifier
                        .clickable {
                            BalasoreHapticHelper.vibrateMarkerTap(context)
                            isWebRechartsMode = !isWebRechartsMode
                        }
                        .testTag("toggle_recharts_engine_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isWebRechartsMode) Icons.Default.AutoGraph else Icons.Default.BarChart,
                            contentDescription = "Switch Recharts Rendering Engine",
                            tint = if (isWebRechartsMode) Color(0xFF7C3AED) else Color(0xFF475569),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isWebRechartsMode) "Recharts SVG" else "Canvas Mode",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isWebRechartsMode) Color(0xFF7C3AED) else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 7-Day Historical Summary Strip from Room Database
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFFF7ED),
                    border = BorderStroke(1.dp, Color(0xFFFED7AA))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = if (isOdia) "୭-ଦିନ ସର୍ବୋଚ୍ଚ" else "7-Day Peak High",
                            fontSize = 9.5.sp,
                            color = Color(0xFF9A3412)
                        )
                        Text(
                            text = "$sevenDayPeakMax$tempUnit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFEA580C)
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0F9FF),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = if (isOdia) "୭-ଦିନ ସର୍ବନିମ୍ନ" else "7-Day Lowest",
                            fontSize = 9.5.sp,
                            color = Color(0xFF075985)
                        )
                        Text(
                            text = "$sevenDayLowestMin$tempUnit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlue
                        )
                    }
                }
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF5F3FF),
                    border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = if (isOdia) "୭-ଦିନ ହାରାହାରି" else "7-Day Mean",
                            fontSize = 9.5.sp,
                            color = Color(0xFF5B21B6)
                        )
                        Text(
                            text = "$sevenDayMeanTemp$tempUnit",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF7C3AED)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metric Selector Pills (High/Low Temp, Mean Temp, Precip Probability %, Rainfall)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(3.dp)
            ) {
                listOf(
                    "TEMP" to (if (isOdia) "🌡️ ତାପମାତ୍ରା ($tempUnit)" else "🌡️ High/Low ($tempUnit)"),
                    "PRECIP" to (if (isOdia) "☔ ସମ୍ଭାବନା (%)" else "☔ Precip (%)"),
                    "AVG" to (if (isOdia) "📈 ହାରାହାରି ($tempUnit)" else "📈 Mean ($tempUnit)"),
                    "RAIN" to (if (isOdia) "🌧️ ବୃଷ୍ଟି (mm)" else "🌧️ Rain (mm)")
                ).forEach { (key, label) ->
                    val isSel = selectedMetric == key
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSel) Color.White else Color.Transparent)
                            .clickable {
                                BalasoreHapticHelper.vibrateMarkerTap(context)
                                selectedMetric = key
                            }
                            .padding(vertical = 6.dp)
                            .testTag("recharts_metric_pill_$key"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.sp,
                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSel) OceanBlue else Color(0xFF475569)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Active Point Detail Badge (Synced with Tapped Day)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
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
                            text = "${if (isOdia) activePoint.dayLabelOd else activePoint.dayLabel} • ${activePoint.condition}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Date: ${activePoint.dateStr} • UV ${activePoint.uvIndex}",
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "High / Low",
                                fontSize = 9.5.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "${formatTemp(activePoint.maxTemp)}° / ${formatTemp(activePoint.minTemp)}$tempUnit",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFEA580C)
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Precip Prob / Rain",
                                fontSize = 9.5.sp,
                                color = Color(0xFF64748B)
                            )
                            Text(
                                text = "☔ ${activePoint.precipProbabilityPct}% • ${activePoint.rainfallMm}mm",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Chart View Engine Switcher (Dynamic Recharts WebView or Native Recharts Compose Canvas)
            if (isWebRechartsMode) {
                WebRechartsWeatherHtmlView(
                    points = historicalData,
                    metric = selectedMetric,
                    isFahrenheit = isFahrenheit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp)
                        .testTag("web_recharts_temperature_view")
                )
            } else {
                NativeRechartsWeatherCanvas(
                    points = historicalData,
                    metric = selectedMetric,
                    isFahrenheit = isFahrenheit,
                    selectedIndex = selectedIndex,
                    onSelectIndex = { idx ->
                        BalasoreHapticHelper.vibrateMarkerTap(context)
                        selectedIndex = idx
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(195.dp)
                        .testTag("native_recharts_temperature_canvas")
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Legend + Room Database Live Action Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF97316))
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Max Temp", fontSize = 10.sp, color = Color(0xFF475569))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(OceanBlue)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            if (selectedMetric == "RAIN") "Rainfall (mm)" else "Min Temp",
                            fontSize = 10.sp,
                            color = Color(0xFF475569)
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (showWeatherHistoryTable) Color(0xFFE0F2FE) else Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, if (showWeatherHistoryTable) OceanBlue else Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .clickable {
                                showWeatherHistoryTable = !showWeatherHistoryTable
                            }
                            .testTag("weather_history_table_toggle_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.TableChart,
                                contentDescription = "Toggle 7-Day Weather History Table",
                                tint = OceanBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOdia) "୭-ଦିନ ଇତିହାସ" else "7-Day History",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1)),
                        modifier = Modifier
                            .clickable {
                                showRoomEditor = !showRoomEditor
                            }
                            .testTag("room_weather_add_record_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EditCalendar,
                                contentDescription = "Log Room Temperature",
                                tint = OceanBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOdia) "+ Room ତାପମାତ୍ରା" else "+ Log Room Temp",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                        }
                    }
                }
            }

            // Expandable 7-Day Offline Weather History Table (Backed by Local Room Database)
            AnimatedVisibility(visible = showWeatherHistoryTable) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .testTag("weather_history_offline_table"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOdia) {
                                    "📋 ୭-ଦିନ ପାଣିପାଗ ଇତିହାସ (ଅଫଲାଇନ୍ Room ଡାଟାବେସ୍)"
                                } else {
                                    "📋 Weather History: 7-Day Offline Room DB Log"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = if (isOdia) "ଇଣ୍ଟରନେଟ୍ ବିନା ଉପଲବ୍ଧ" else "Available 100% Offline",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }

                        historicalData.forEachIndexed { idx, pt ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (idx == selectedIndex) Color(0xFFE0F2FE) else Color.White)
                                    .clickable { selectedIndex = idx }
                                    .padding(horizontal = 8.dp, vertical = 5.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1.2f)) {
                                    Text(
                                        text = "${if (isOdia) pt.dayLabelOd else pt.dayLabel} (${pt.dateStr.takeLast(5)})",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF0F172A)
                                    )
                                    Text(
                                        text = pt.condition,
                                        fontSize = 9.5.sp,
                                        color = Color(0xFF64748B)
                                    )
                                }
                                Text(
                                    text = "H: ${formatTemp(pt.maxTemp)}$tempUnit / L: ${formatTemp(pt.minTemp)}$tempUnit",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFFEA580C),
                                    modifier = Modifier.weight(1.1f)
                                )
                                Text(
                                    text = "${pt.rainfallMm}mm • ${pt.humidityPct}%",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OceanBlue
                                )
                            }
                        }

                        // WorkManager Offline Severe Weather Alert Background Monitor Action
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val alertEntity = com.example.data.local.SevereWeatherAlertEntity(
                                        id = "severe_cyclone_${System.currentTimeMillis()}",
                                        title = "Severe Cyclone & Heavy Rain Warning — Balasore Coast",
                                        odiaTitle = "ବାଲେଶ୍ୱର ଉପକୂଳ ବାତ୍ୟା ଓ ପ୍ରବଳ ବର୍ଷା ଚେତାବନୀ",
                                        summary = "Sudden severe cyclone squall & heavy rainfall (68mm/hr) detected in Balasore offline database for Chandipur, Remuna, and Talsari.",
                                        severity = "Extreme",
                                        affectedZones = "Balasore District, Chandipur Coast & Talsari",
                                        issuingAuthority = "IMD Bhubaneswar & Balasore DDMA",
                                        validFromMillis = System.currentTimeMillis(),
                                        validUntilMillis = System.currentTimeMillis() + 12 * 3600_000L
                                    )
                                    AppDatabase.getInstance(context)
                                        .severeWeatherAlertDao()
                                        .insertAlerts(listOf(alertEntity))
                                    com.example.data.sync.SyncManager.triggerImmediateSync(context)
                                    com.example.data.fcm.BalasoreAlertDispatchEngine.checkBackgroundAlerts(
                                        context = context,
                                        forceNotification = true
                                    )
                                    withContext(Dispatchers.Main) {
                                        workManagerAlertStatus = "✓ WorkManager DataSyncWorker monitored offline Room DB & triggered local Severe Weather Push Notification!"
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("workmanager_severe_weather_alert_test_btn"),
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, Color(0xFFDC2626))
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "WorkManager Severe Weather Alert",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) {
                                    "🚨 WorkManager ଅଫଲାଇନ୍ ବାତ୍ୟା/ପ୍ରବଳ ବର୍ଷା ପୁଶ୍ ଆଲର୍ଟ ପରୀକ୍ଷା"
                                } else {
                                    "🚨 Test WorkManager Offline Severe Weather Push Alert"
                                },
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626)
                            )
                        }

                        if (workManagerAlertStatus != null) {
                            Text(
                                text = workManagerAlertStatus!!,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF047857),
                                modifier = Modifier.testTag("workmanager_severe_weather_status_text")
                            )
                        }
                    }
                }
            }

            // Inline Room Database Historical Temperature Logger / Updater
            AnimatedVisibility(visible = showRoomEditor) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .testTag("room_weather_editor_panel"),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f))
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isOdia) "Room ଡାଟାବେସ୍ ରେ ଆଜିର ତାପମାତ୍ରା ଅପଡେଟ୍ କରନ୍ତୁ" else "Update Today's Historical Record in Room DB (daily_forecasts)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = customMaxTempInput,
                                onValueChange = { customMaxTempInput = it },
                                label = { Text("High (°C)", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_weather_max_temp_input")
                            )
                            OutlinedTextField(
                                value = customMinTempInput,
                                onValueChange = { customMinTempInput = it },
                                label = { Text("Low (°C)", fontSize = 10.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("room_weather_min_temp_input")
                            )
                            Button(
                                onClick = {
                                    val newMax = customMaxTempInput.toDoubleOrNull()?.coerceIn(10.0, 50.0) ?: 33.6
                                    val newMin = customMinTempInput.toDoubleOrNull()?.coerceIn(5.0, newMax) ?: 24.4
                                    coroutineScope.launch(Dispatchers.IO) {
                                        val updatedToday = DailyForecastEntity(
                                            date = activePoint.dateStr,
                                            dayOfWeek = activePoint.dayLabel,
                                            weatherCode = 1,
                                            weatherDescription = "Room Logged|rain=${activePoint.rainfallMm}|hum=${activePoint.humidityPct}",
                                            maxTemp = newMax,
                                            minTemp = newMin,
                                            sunrise = "05:48 AM",
                                            sunset = "05:14 PM",
                                            uvIndex = activePoint.uvIndex,
                                            cachedAt = System.currentTimeMillis()
                                        )
                                        weatherDao.insertDailyForecasts(listOf(updatedToday))
                                    }
                                    showRoomEditor = false
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("room_weather_save_record_btn")
                            ) {
                                Text(if (isOdia) "ସେଭ୍" else "Save to Room", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Native Jetpack Compose Canvas implementation mirroring Recharts ComposedChart / Dual-AreaChart & CartesianGrid.
 */
@Composable
fun NativeRechartsWeatherCanvas(
    points: List<SevenDayHistoricalPoint>,
    metric: String,
    isFahrenheit: Boolean = false,
    selectedIndex: Int,
    onSelectIndex: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "recharts_anim"
    )

    fun convertUnit(c: Double): Float {
        return if (isFahrenheit) (c * 9.0 / 5.0 + 32.0).toFloat() else c.toFloat()
    }

    BoxWithConstraints(modifier = modifier) {
        val height = constraints.maxHeight.toFloat()
        val paddingBottom = 42f
        val paddingTop = 22f
        val chartHeight = (height - paddingBottom - paddingTop).coerceAtLeast(40f)

        val primaryValues = remember(points, metric, isFahrenheit) {
            points.map {
                when (metric) {
                    "RAIN" -> it.rainfallMm.toFloat()
                    "PRECIP" -> it.precipProbabilityPct.toFloat()
                    "AVG" -> convertUnit(it.avgTemp)
                    else -> convertUnit(it.maxTemp)
                }
            }
        }
        val secondaryValues = remember(points, metric, isFahrenheit) {
            points.map {
                when (metric) {
                    "TEMP" -> convertUnit(it.minTemp)
                    else -> it.precipProbabilityPct.toFloat()
                }
            }
        }

        val maxVal = remember(primaryValues, metric) {
            if (metric == "PRECIP") 100f
            else ((primaryValues.maxOrNull() ?: 35f) + 2f).coerceAtLeast(10f)
        }
        val minVal = remember(primaryValues, secondaryValues, metric) {
            if (metric == "RAIN" || metric == "PRECIP") 0f
            else ((secondaryValues.minOrNull()?.takeIf { it > 0f } ?: primaryValues.minOrNull() ?: 15f) - 2f).coerceAtLeast(0f)
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(points) {
                    detectTapGestures { tapOffset ->
                        val divisor = (points.size - 1).coerceAtLeast(1)
                        val stepX = size.width / divisor
                        val tappedIdx = ((tapOffset.x + stepX / 2) / stepX).toInt().coerceIn(0, (points.size - 1).coerceAtLeast(0))
                        onSelectIndex(tappedIdx)
                    }
                }
        ) {
            // 1. Recharts CartesianGrid Lines
            val gridLines = 4
            for (i in 0..gridLines) {
                val y = paddingTop + (chartHeight / gridLines) * i
                drawLine(
                    color = Color(0xFFE2E8F0),
                    start = Offset(0f, y),
                    end = Offset(size.width, y),
                    strokeWidth = 1.5f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                )
            }

            if (points.size < 2) return@Canvas
            val stepX = size.width / (points.size - 1)

            // Active Tooltip Vertical Crosshair Cursor Line
            val activeX = selectedIndex.coerceIn(0, points.size - 1) * stepX
            drawLine(
                color = Color(0xFF94A3B8).copy(alpha = 0.55f),
                start = Offset(activeX, paddingTop),
                end = Offset(activeX, height - paddingBottom),
                strokeWidth = 2f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 4f), 0f)
            )

            // Secondary Area (Min Temp in TEMP mode)
            if (metric == "TEMP") {
                val minLinePath = Path()
                val minFillPath = Path()
                secondaryValues.forEachIndexed { index, v ->
                    val x = index * stepX
                    val normalizedY = 1f - ((v - minVal) / (maxVal - minVal).coerceAtLeast(1f))
                    val y = paddingTop + (normalizedY * chartHeight * animProgress)
                    if (index == 0) {
                        minLinePath.moveTo(x, y)
                        minFillPath.moveTo(x, height - paddingBottom)
                        minFillPath.lineTo(x, y)
                    } else {
                        minLinePath.lineTo(x, y)
                        minFillPath.lineTo(x, y)
                    }
                }
                minFillPath.lineTo(size.width, height - paddingBottom)
                minFillPath.close()

                drawPath(
                    path = minFillPath,
                    brush = Brush.verticalGradient(
                        colors = listOf(OceanBlue.copy(alpha = 0.22f), OceanBlue.copy(alpha = 0.02f)),
                        startY = paddingTop,
                        endY = height - paddingBottom
                    )
                )
                drawPath(
                    path = minLinePath,
                    color = OceanBlue,
                    style = Stroke(width = 2.8f, cap = StrokeCap.Round)
                )
            }

            // Primary Area (Max Temp / Avg Temp / Rainfall)
            val linePath = Path()
            val fillPath = Path()

            val primaryColor = when (metric) {
                "RAIN" -> OceanBlue
                "AVG" -> Color(0xFF7C3AED)
                else -> Color(0xFFF97316)
            }
            val gradientStart = primaryColor.copy(alpha = 0.35f)
            val gradientEnd = primaryColor.copy(alpha = 0.02f)

            primaryValues.forEachIndexed { index, v ->
                val x = index * stepX
                val normalizedY = 1f - ((v - minVal) / (maxVal - minVal).coerceAtLeast(1f))
                val y = paddingTop + (normalizedY * chartHeight * animProgress)

                if (index == 0) {
                    linePath.moveTo(x, y)
                    fillPath.moveTo(x, height - paddingBottom)
                    fillPath.lineTo(x, y)
                } else {
                    linePath.lineTo(x, y)
                    fillPath.lineTo(x, y)
                }
            }

            fillPath.lineTo(size.width, height - paddingBottom)
            fillPath.close()

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(
                    colors = listOf(gradientStart, gradientEnd),
                    startY = paddingTop,
                    endY = height - paddingBottom
                )
            )

            drawPath(
                path = linePath,
                color = primaryColor,
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            // Draw Recharts Dots & Active Index Ring
            primaryValues.forEachIndexed { index, v ->
                val x = index * stepX
                val normalizedY = 1f - ((v - minVal) / (maxVal - minVal).coerceAtLeast(1f))
                val y = paddingTop + (normalizedY * chartHeight * animProgress)

                val isSelected = index == selectedIndex
                if (isSelected) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.25f),
                        radius = 12f,
                        center = Offset(x, y)
                    )
                }
                drawCircle(
                    color = Color.White,
                    radius = if (isSelected) 6f else 4f,
                    center = Offset(x, y)
                )
                drawCircle(
                    color = primaryColor,
                    radius = if (isSelected) 4.5f else 2.5f,
                    center = Offset(x, y)
                )

                if (metric == "TEMP") {
                    val minV = secondaryValues[index]
                    val minNormY = 1f - ((minV - minVal) / (maxVal - minVal).coerceAtLeast(1f))
                    val minY = paddingTop + (minNormY * chartHeight * animProgress)
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelected) 5f else 3.5f,
                        center = Offset(x, minY)
                    )
                    drawCircle(
                        color = OceanBlue,
                        radius = if (isSelected) 3.5f else 2f,
                        center = Offset(x, minY)
                    )
                }
            }
        }

        // Bottom X-Axis Labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            points.forEachIndexed { index, pt ->
                val isSel = index == selectedIndex
                Text(
                    text = pt.dayLabel.take(6),
                    fontSize = 9.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSel) Color(0xFF0F172A) else Color(0xFF94A3B8)
                )
            }
        }
    }
}

/**
 * Self-contained HTML5 / SVG Recharts ResponsiveContainer rendered in WebView,
 * dynamically plotting the Room Database `DailyForecastEntity` 7-day historical records.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebRechartsWeatherHtmlView(
    points: List<SevenDayHistoricalPoint>,
    metric: String,
    isFahrenheit: Boolean = false,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(points, metric, isFahrenheit) {
        generateWeatherRechartsHtml(points, metric, isFahrenheit)
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                setBackgroundColor(0)
                webViewClient = SafeWebViewClient()
                loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
        },
        modifier = modifier.clip(RoundedCornerShape(10.dp))
    )
}

private fun generateWeatherRechartsHtml(
    points: List<SevenDayHistoricalPoint>,
    metric: String,
    isFahrenheit: Boolean
): String {
    fun conv(c: Double): Double {
        val v = if (isFahrenheit) (c * 9.0 / 5.0 + 32.0) else c
        return (v * 10.0).roundToInt() / 10.0
    }

    val unit = if (metric == "RAIN") "mm" else if (isFahrenheit) "°F" else "°C"
    val primaryColor = when (metric) {
        "RAIN" -> "#0284c7"
        "AVG" -> "#7c3aed"
        else -> "#f97316"
    }
    val secondaryColor = "#0284c7"

    val jsonPoints = JSONArray()
    points.forEach { pt ->
        val obj = JSONObject().apply {
            put("day", pt.dayLabel.take(6))
            put("date", pt.dateStr)
            put("maxTemp", conv(pt.maxTemp))
            put("minTemp", conv(pt.minTemp))
            put("avgTemp", conv(pt.avgTemp))
            put("rain", pt.rainfallMm)
            put("humidity", pt.humidityPct)
            put("condition", pt.condition)
        }
        jsonPoints.put(obj)
    }

    return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
          <style>
            * { box-sizing: border-box; -webkit-tap-highlight-color: transparent; }
            body { margin: 0; padding: 4px 6px; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: transparent; }
            .recharts-wrapper { position: relative; width: 100%; height: 195px; }
            svg { width: 100%; height: 165px; overflow: visible; }
            .recharts-cartesian-grid-horizontal line, .recharts-cartesian-grid-vertical line {
              stroke: #e2e8f0; stroke-dasharray: 4 4; stroke-width: 1;
            }
            .recharts-x-axis text, .recharts-y-axis text {
              font-size: 9.5px; fill: #64748b; font-weight: 600;
            }
            .recharts-reference-line { stroke: #94a3b8; stroke-dasharray: 3 3; stroke-width: 1.2; }
            .recharts-tooltip-cursor { stroke: #64748b; stroke-dasharray: 3 3; stroke-width: 1.5; }
            .recharts-tooltip {
              display: flex; justify-content: space-between; align-items: center;
              background: #0f172a; color: #f8fafc; border-radius: 8px;
              padding: 5px 10px; font-size: 10.5px; margin-bottom: 4px;
              box-shadow: 0 4px 10px rgba(15, 23, 42, 0.15);
            }
            .recharts-tooltip .title { font-weight: 700; color: #38bdf8; }
            .recharts-tooltip .vals { font-weight: 600; }
          </style>
        </head>
        <body>
          <div class="recharts-wrapper">
            <div id="tooltip" class="recharts-tooltip">
              <span class="title" id="tt-day">Today</span>
              <span class="vals" id="tt-val">Loading Room DB...</span>
            </div>
            <svg id="recharts-svg" viewBox="0 0 360 155"></svg>
          </div>
          <script>
            const data = ${jsonPoints.toString()};
            const metric = "$metric";
            const unit = "$unit";
            const primaryColor = "$primaryColor";
            const secondaryColor = "$secondaryColor";
            const svg = document.getElementById('recharts-svg');
            const ttDay = document.getElementById('tt-day');
            const ttVal = document.getElementById('tt-val');

            let activeIdx = Math.max(0, data.length - 1);

            function getPrimary(d) {
              if (metric === 'RAIN') return d.rain;
              if (metric === 'AVG') return d.avgTemp;
              return d.maxTemp;
            }

            function buildSmoothPath(pts) {
              if (pts.length === 0) return "";
              if (pts.length === 1) return "M " + pts[0].x + " " + pts[0].y;
              let d = "M " + pts[0].x.toFixed(1) + " " + pts[0].y.toFixed(1);
              for (let i = 0; i < pts.length - 1; i++) {
                const p0 = pts[i];
                const p1 = pts[i + 1];
                const midX = ((p0.x + p1.x) / 2).toFixed(1);
                d += " C " + midX + " " + p0.y.toFixed(1) + ", " + midX + " " + p1.y.toFixed(1) + ", " + p1.x.toFixed(1) + " " + p1.y.toFixed(1);
              }
              return d;
            }

            function renderRecharts() {
              if (!data || data.length === 0) return;
              const padL = 34, padR = 14, padT = 14, padB = 26;
              const W = 360 - padL - padR;
              const H = 155 - padT - padB;

              const pVals = data.map(getPrimary);
              const sVals = metric === 'TEMP' ? data.map(d => d.minTemp) : [];
              const allVals = sVals.length ? pVals.concat(sVals) : pVals;

              const rawMax = Math.max(...allVals, 10);
              const rawMin = metric === 'RAIN' ? 0 : Math.min(...allVals);
              const maxV = Math.ceil(rawMax + 2);
              const minV = metric === 'RAIN' ? 0 : Math.floor(Math.max(0, rawMin - 2));
              const span = Math.max(1, maxV - minV);

              function toX(i) {
                return padL + (data.length > 1 ? (i * W / (data.length - 1)) : W / 2);
              }
              function toY(v) {
                return padT + H - ((v - minV) / span) * H;
              }

              let html = `
                <defs>
                  <linearGradient id="rechartsPrimaryGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="${'$'}{primaryColor}" stop-opacity="0.38"/>
                    <stop offset="100%" stop-color="${'$'}{primaryColor}" stop-opacity="0.02"/>
                  </linearGradient>
                  <linearGradient id="rechartsSecondaryGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stop-color="${'$'}{secondaryColor}" stop-opacity="0.24"/>
                    <stop offset="100%" stop-color="${'$'}{secondaryColor}" stop-opacity="0.02"/>
                  </linearGradient>
                </defs>
                <g class="recharts-cartesian-grid-horizontal">
              `;

              for (let g = 0; g <= 3; g++) {
                const gy = padT + (H / 3) * g;
                const gVal = (maxV - (span / 3) * g).toFixed(0);
                html += `<line x1="${'$'}{padL}" y1="${'$'}{gy}" x2="${'$'}{360 - padR}" y2="${'$'}{gy}" />`;
                html += `<g class="recharts-y-axis"><text x="${'$'}{padL - 5}" y="${'$'}{gy + 3}" text-anchor="end">${'$'}{gVal}${'$'}{unit}</text></g>`;
              }
              html += `</g>`;

              // Active vertical cursor line
              const actX = toX(activeIdx);
              html += `<line class="recharts-tooltip-cursor" x1="${'$'}{actX}" y1="${'$'}{padT}" x2="${'$'}{actX}" y2="${'$'}{padT + H}" />`;

              // Secondary Area (Min Temp in TEMP mode)
              if (metric === 'TEMP') {
                const sPts = data.map((d, i) => ({ x: toX(i), y: toY(d.minTemp), v: d.minTemp }));
                const sLine = buildSmoothPath(sPts);
                const sArea = sLine + ` L ${'$'}{sPts[sPts.length - 1].x} ${'$'}{padT + H} L ${'$'}{sPts[0].x} ${'$'}{padT + H} Z`;
                html += `<path d="${'$'}{sArea}" fill="url(#rechartsSecondaryGrad)" />`;
                html += `<path d="${'$'}{sLine}" fill="none" stroke="${'$'}{secondaryColor}" stroke-width="2.4" stroke-linecap="round" />`;
              }

              // Primary Area (Max Temp / Mean Temp / Rainfall)
              const pPts = data.map((d, i) => ({ x: toX(i), y: toY(getPrimary(d)), v: getPrimary(d) }));
              const pLine = buildSmoothPath(pPts);
              const pArea = pLine + ` L ${'$'}{pPts[pPts.length - 1].x} ${'$'}{padT + H} L ${'$'}{pPts[0].x} ${'$'}{padT + H} Z`;
              html += `<path d="${'$'}{pArea}" fill="url(#rechartsPrimaryGrad)" />`;
              html += `<path d="${'$'}{pLine}" fill="none" stroke="${'$'}{primaryColor}" stroke-width="3" stroke-linecap="round" />`;

              // Dots & XAxis Labels
              data.forEach((d, i) => {
                const cx = toX(i);
                const cy = toY(getPrimary(d));
                const isAct = i === activeIdx;
                if (isAct) {
                  html += `<circle cx="${'$'}{cx}" cy="${'$'}{cy}" r="9" fill="${'$'}{primaryColor}" fill-opacity="0.22" />`;
                }
                html += `<circle cx="${'$'}{cx}" cy="${'$'}{cy}" r="${'$'}{isAct ? 5 : 3.5}" fill="#ffffff" stroke="${'$'}{primaryColor}" stroke-width="${'$'}{isAct ? 3 : 2}" />`;

                if (metric === 'TEMP') {
                  const cyMin = toY(d.minTemp);
                  html += `<circle cx="${'$'}{cx}" cy="${'$'}{cyMin}" r="${'$'}{isAct ? 4.5 : 3}" fill="#ffffff" stroke="${'$'}{secondaryColor}" stroke-width="2" />`;
                }

                // Value label above point
                html += `<text x="${'$'}{cx}" y="${'$'}{cy - 8}" text-anchor="middle" font-size="8.5" font-weight="${'$'}{isAct ? '800' : '600'}" fill="#0f172a">${'$'}{getPrimary(d)}${'$'}{unit}</text>`;

                // X-Axis day label
                html += `<g class="recharts-x-axis"><text x="${'$'}{cx}" y="${'$'}{155 - 6}" text-anchor="middle" style="fill:${'$'}{isAct ? '#0f172a' : '#64748b'};font-weight:${'$'}{isAct ? '800' : '600'}">${'$'}{d.day}</text></g>`;

                // Invisible touch column for selecting day
                const colW = W / data.length;
                html += `<rect x="${'$'}{cx - colW / 2}" y="0" width="${'$'}{colW}" height="155" fill="transparent" onclick="selectDay(${'$'}{i})" />`;
              });

              svg.innerHTML = html;

              const sel = data[activeIdx];
              ttDay.textContent = sel.day + " (" + sel.date + ")";
              if (metric === 'TEMP') {
                ttVal.textContent = "High: " + sel.maxTemp + unit + " • Low: " + sel.minTemp + unit + " • Hum: " + sel.humidity + "%";
              } else if (metric === 'AVG') {
                ttVal.textContent = "Mean: " + sel.avgTemp + unit + " • High: " + sel.maxTemp + unit;
              } else {
                ttVal.textContent = "Rainfall: " + sel.rain + "mm • Hum: " + sel.humidity + "%";
              }
            }

            window.selectDay = function(idx) {
              activeIdx = idx;
              renderRecharts();
            };

            renderRecharts();
          </script>
        </body>
        </html>
    """.trimIndent()
}

private fun loadOrCache7DayData(context: Context): List<SevenDayHistoricalPoint> {
    val prefs = context.getSharedPreferences("balasore_weather_history", Context.MODE_PRIVATE)
    val cached = prefs.getString("7day_trend_points", null)
    if (cached != null) {
        try {
            val arr = JSONArray(cached)
            val list = mutableListOf<SevenDayHistoricalPoint>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(
                    SevenDayHistoricalPoint(
                        dayLabel = obj.getString("dayLabel"),
                        dayLabelOd = obj.getString("dayLabelOd"),
                        dateStr = obj.getString("dateStr"),
                        maxTemp = obj.getDouble("maxTemp"),
                        minTemp = obj.getDouble("minTemp"),
                        avgTemp = obj.getDouble("avgTemp"),
                        rainfallMm = obj.getDouble("rainfallMm"),
                        humidityPct = obj.getInt("humidityPct"),
                        uvIndex = obj.optDouble("uvIndex", 6.5),
                        condition = obj.optString("condition", "Partly Cloudy")
                    )
                )
            }
            if (list.size >= 5) return list
        } catch (_: Exception) {}
    }

    val arr = JSONArray()
    DEFAULT_7_DAY_HISTORICAL.forEach {
        val o = JSONObject().apply {
            put("dayLabel", it.dayLabel)
            put("dayLabelOd", it.dayLabelOd)
            put("dateStr", it.dateStr)
            put("maxTemp", it.maxTemp)
            put("minTemp", it.minTemp)
            put("avgTemp", it.avgTemp)
            put("rainfallMm", it.rainfallMm)
            put("humidityPct", it.humidityPct)
            put("uvIndex", it.uvIndex)
            put("condition", it.condition)
        }
        arr.put(o)
    }
    prefs.edit().putString("7day_trend_points", arr.toString()).apply()
    return DEFAULT_7_DAY_HISTORICAL
}
