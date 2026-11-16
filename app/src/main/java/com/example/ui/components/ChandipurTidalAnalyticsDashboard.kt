package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.AppDatabase
import com.example.data.local.ChandipurTideEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Data model representing a single day's Chandipur sea recession pattern
 * over the upcoming 7-day forecast window.
 */
data class ChandipurTidalDayPattern(
    val dayIndex: Int,
    val dayLabelEn: String,
    val dayLabelOr: String,
    val dateLabel: String,
    val maxRecessionKm: Double,
    val minRecessionKm: Double,
    val highTideSurgeMeters: Double,
    val lowTideDepthMeters: Double,
    val lowTideTime: String,
    val highTideTime: String,
    val safeWalkWindowMins: Int,
    val lunarPhaseEn: String,
    val lunarPhaseOr: String,
    val safetyStatus: String
)

/**
 * Simulated Hydrographic & Coastal Telemetry API Service for Chandipur-on-Sea.
 * Fetches 7-day sea recession patterns (0.0 km – 5.2 km vanishing sea phenomenon)
 * and persists snapshots into Room (`ChandipurTideDao`).
 */
object ChandipurTidalAnalyticsApiService {

    private var fetchCounter = 0

    fun getDefaultSevenDayTidalPatterns(variationOffsetKm: Double = 0.0): List<ChandipurTidalDayPattern> {
        val cal = Calendar.getInstance()
        val dateFmt = SimpleDateFormat("MMM dd", Locale.US)
        val dayFmt = SimpleDateFormat("EEE", Locale.US)

        val baseRecessionsKm = listOf(4.8, 5.1, 4.9, 4.5, 4.7, 5.2, 5.0)
        val baseHighTideMeters = listOf(3.1, 3.4, 3.2, 2.8, 3.0, 3.5, 3.3)
        val baseLowTideMeters = listOf(0.4, 0.3, 0.4, 0.6, 0.5, 0.2, 0.3)
        val lowTimes = listOf(
            "10:15 AM • 10:42 PM",
            "11:02 AM • 11:28 PM",
            "11:48 AM • 11:58 PM",
            "12:34 PM • 01:05 AM",
            "01:18 PM • 01:49 AM",
            "02:04 PM • 02:36 AM",
            "02:50 PM • 03:22 AM"
        )
        val highTimes = listOf(
            "04:05 AM • 04:32 PM",
            "04:52 AM • 05:18 PM",
            "05:38 AM • 06:04 PM",
            "06:24 AM • 06:50 PM",
            "07:12 AM • 07:38 PM",
            "07:58 AM • 08:25 PM",
            "08:45 AM • 09:12 PM"
        )
        val safeWalkWindows = listOf(165, 180, 170, 145, 155, 190, 175)
        val lunarEn = listOf(
            "Waxing Gibbous",
            "Near Full Moon",
            "Full Moon (Purnima)",
            "Waning Gibbous",
            "Waning Gibbous",
            "Spring Tide Peak",
            "Coastal Equilibrium"
        )
        val lunarOr = listOf(
            "ଶୁକ୍ଳ ପକ୍ଷ ଏକାଦଶୀ",
            "ଶୁକ୍ଳ ଚତୁର୍ଦ୍ଦଶୀ",
            "ପୂର୍ଣ୍ଣିମା ଜୁଆର",
            "କୃଷ୍ଣ ପ୍ରତିପଦା",
            "କୃଷ୍ଣ ଦ୍ୱିତୀୟା",
            "ମହା ଭଟ୍ଟା ସୁଯୋଗ",
            "ସନ୍ତୁଳିତ ଉପକୂଳ ଜୁଆର"
        )

        return (0 until 7).map { idx ->
            val dayCal = (cal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, idx) }
            val dateStr = dateFmt.format(dayCal.time)
            val weekDay = dayFmt.format(dayCal.time)
            val labelEn = when (idx) {
                0 -> "Today"
                1 -> "Tomorrow"
                else -> weekDay
            }
            val labelOr = when (idx) {
                0 -> "ଆଜି"
                1 -> "ଆସନ୍ତାକାଲି"
                else -> "ଦିନ ${idx + 1}"
            }
            val adjustedRecession = ((baseRecessionsKm[idx] + variationOffsetKm).coerceIn(3.6, 5.4) * 10.0).roundToInt() / 10.0

            ChandipurTidalDayPattern(
                dayIndex = idx,
                dayLabelEn = labelEn,
                dayLabelOr = labelOr,
                dateLabel = dateStr,
                maxRecessionKm = adjustedRecession,
                minRecessionKm = 0.3,
                highTideSurgeMeters = baseHighTideMeters[idx],
                lowTideDepthMeters = baseLowTideMeters[idx],
                lowTideTime = lowTimes[idx],
                highTideTime = highTimes[idx],
                safeWalkWindowMins = safeWalkWindows[idx],
                lunarPhaseEn = lunarEn[idx],
                lunarPhaseOr = lunarOr[idx],
                safetyStatus = if (adjustedRecession >= 4.8) "OPTIMAL_SEABED_WALK" else "SAFE_WINDOW"
            )
        }
    }

    /**
     * Simulates an asynchronous network API call to the Chandipur Coastal Hydrographic Telemetry endpoint.
     */
    suspend fun fetchNextSevenDaysTidalPatterns(
        forceRefresh: Boolean = false,
        simulatedDelayMs: Long = 180L
    ): List<ChandipurTidalDayPattern> {
        if (simulatedDelayMs > 0) {
            delay(simulatedDelayMs)
        }
        if (forceRefresh) {
            fetchCounter = (fetchCounter + 1) % 3
        }
        val delta = when (fetchCounter) {
            1 -> 0.1
            2 -> -0.1
            else -> 0.0
        }
        return getDefaultSevenDayTidalPatterns(variationOffsetKm = delta)
    }

    /**
     * Fetches the 7-day Chandipur tidal recession forecast and caches it into Room (`ChandipurTideDao`).
     */
    suspend fun fetchAndCacheSevenDayPatterns(
        context: Context,
        forceRefresh: Boolean = false,
        simulatedDelayMs: Long = 180L
    ): List<ChandipurTidalDayPattern> {
        val patterns = fetchNextSevenDaysTidalPatterns(
            forceRefresh = forceRefresh,
            simulatedDelayMs = simulatedDelayMs
        )
        try {
            val tideDao = AppDatabase.getInstance(context).chandipurTideDao()
            val entities = patterns.map { day ->
                ChandipurTideEntity(
                    id = if (day.dayIndex == 0) "chandipur_tide_current" else "chandipur_tide_day_${day.dayIndex}",
                    date = day.dateLabel,
                    lowTideTime = day.lowTideTime,
                    highTideTime = day.highTideTime,
                    nextHighTideTime = day.highTideTime.substringAfter("• ").trim(),
                    recededDistanceKm = day.maxRecessionKm,
                    currentWaterLevelMeters = day.lowTideDepthMeters,
                    tideState = "LOW_TIDE",
                    safeWalkStatus = "SAFE_WALK",
                    safeWalkMinutesRemaining = day.safeWalkWindowMins,
                    lunarCondition = day.lunarPhaseEn,
                    tidalForecastSummary = "Sea recedes ${day.maxRecessionKm} km • Safe walk window ${day.safeWalkWindowMins} mins",
                    isOfflineCached = true,
                    lastFetchedTimestamp = System.currentTimeMillis()
                )
            }
            tideDao.insertTides(entities)
        } catch (_: Exception) {
        }
        return patterns
    }
}

/**
 * 'Tidal Analytics' Dashboard Component using Recharts-style visualization to chart
 * Chandipur sea recession patterns over the next 7 days, powered by `ChandipurTidalAnalyticsApiService`.
 */
@Composable
fun ChandipurTidalAnalyticsDashboard(
    language: AppLanguage = LocalAppLanguage.current.currentLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val appLanguageState = LocalAppLanguage.current
    val isOdia = language == AppLanguage.ODIA || appLanguageState.isOdia

    var tidalPatterns by remember {
        mutableStateOf(ChandipurTidalAnalyticsApiService.getDefaultSevenDayTidalPatterns())
    }
    var isFetchingApi by remember { mutableStateOf(false) }
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    var chartViewMode by remember { mutableStateOf("AREA") } // "AREA" or "BAR"
    var lastApiSyncLabel by remember {
        mutableStateOf(SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date()))
    }

    LaunchedEffect(Unit) {
        isFetchingApi = true
        tidalPatterns = ChandipurTidalAnalyticsApiService.fetchAndCacheSevenDayPatterns(
            context = context,
            forceRefresh = false,
            simulatedDelayMs = 120L
        )
        lastApiSyncLabel = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
        isFetchingApi = false
    }

    val activeDay = tidalPatterns.getOrElse(selectedDayIndex.coerceIn(0, (tidalPatterns.size - 1).coerceAtLeast(0))) {
        tidalPatterns.first()
    }
    val peakRecessionKm = remember(tidalPatterns) {
        tidalPatterns.maxOfOrNull { it.maxRecessionKm } ?: 5.2
    }
    val avgSafeWalkMins = remember(tidalPatterns) {
        if (tidalPatterns.isNotEmpty()) tidalPatterns.map { it.safeWalkWindowMins }.average().roundToInt() else 168
    }

    val chartProgress by animateFloatAsState(
        targetValue = if (isFetchingApi) 0.65f else 1f,
        animationSpec = tween(durationMillis = 450),
        label = "tidal_recharts_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("tidal_analytics_dashboard_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, OceanBlue.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Recharts Badge & Simulated API Refresh Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE0F2FE),
                        border = BorderStroke(1.dp, Color(0xFF7DD3FC)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Waves,
                                contentDescription = "Chandipur Tidal Analytics",
                                tint = OceanBlueDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isOdia) "ଚାନ୍ଦିପୁର ଜୁଆର ଆନାଲିଟିକ୍ସ (୭-ଦିନ)" else "Chandipur Tidal Analytics (7-Day)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.sp,
                                color = BentoSlate900
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFEDE9FE),
                                border = BorderStroke(1.dp, Color(0xFFDDD6FE))
                            ) {
                                Text(
                                    text = "Recharts",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF6D28D9),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isOdia)
                                "ସମୁଦ୍ର ଭଟ୍ଟା ଦୂରତା (କି.ମି.) • API ସିଙ୍କ୍: $lastApiSyncLabel"
                            else
                                "7-Day Sea Recession Patterns (km) • Simulated API Synced $lastApiSyncLabel",
                            fontSize = 11.sp,
                            color = BentoSlate600
                        )
                    }
                }

                IconButton(
                    onClick = {
                        coroutineScope.launch {
                            isFetchingApi = true
                            tidalPatterns = ChandipurTidalAnalyticsApiService.fetchAndCacheSevenDayPatterns(
                                context = context,
                                forceRefresh = true,
                                simulatedDelayMs = 180L
                            )
                            lastApiSyncLabel = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
                            isFetchingApi = false
                            Toast.makeText(
                                context,
                                if (isOdia) "୭-ଦିନିଆ ଚାନ୍ଦିପୁର ଜୁଆର ଆନାଲିଟିକ୍ସ ଅପଡେଟ୍ ହେଲା!"
                                else "Fetched 7-Day Chandipur Tidal Recession Patterns from API!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("tidal_analytics_refresh_btn")
                ) {
                    if (isFetchingApi) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(18.dp)
                                .testTag("tidal_analytics_loading_indicator"),
                            strokeWidth = 2.dp,
                            color = OceanBlue
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Tidal Analytics API",
                            tint = OceanBlue
                        )
                    }
                }
            }

            // Summary KPI Strip: Peak Recession, Mean Safe Walk Window, Chart Mode Switch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0F9FF),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = if (isOdia) "ସର୍ବାଧିକ ଭଟ୍ଟା ଦୂରତା" else "7-Day Peak Recession",
                            fontSize = 10.sp,
                            color = Color(0xFF0369A1),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$peakRecessionKm km offshore",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0C4A6E)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Text(
                            text = if (isOdia) "ସୁରକ୍ଷିତ ପଦଯାତ୍ରା ସମୟ" else "Avg Safe Walk Window",
                            fontSize = 10.sp,
                            color = Color(0xFF047857),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$avgSafeWalkMins mins / tide",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                // Chart Mode Switch: Recharts Area vs Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BentoSlate100,
                    border = BorderStroke(1.dp, BentoSlate200),
                    modifier = Modifier
                        .clickable {
                            chartViewMode = if (chartViewMode == "AREA") "BAR" else "AREA"
                        }
                        .testTag("tidal_analytics_chart_mode_toggle")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isOdia) "ଚାର୍ଟ ମୋଡ୍" else "Chart Mode",
                            fontSize = 9.5.sp,
                            color = BentoSlate600,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (chartViewMode == "AREA") "📈 Area Curve" else "📊 Dual Bar",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlueDark
                        )
                    }
                }
            }

            // Recharts Interactive 7-Day Sea Recession Visualization Canvas
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tidal_analytics_recharts_container")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Legend Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(OceanBlue)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOdia) "ଭଟ୍ଟା ଦୂରତା (km)" else "Sea Recession (km)",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate700
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(EmeraldGreen)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOdia) "ଜୁଆର ଉଚ୍ଚତା (m)" else "High Tide Surge (m)",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate700
                                )
                            }
                        }
                        Text(
                            text = if (isOdia) "ଦିନ ଚୟନ ପାଇଁ ଟ୍ୟାପ୍ କରନ୍ତୁ" else "Tap point to inspect",
                            fontSize = 10.sp,
                            color = BentoSlate600
                        )
                    }

                    // Recharts Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp)
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("tidal_analytics_recharts_canvas")
                                .pointerInput(tidalPatterns) {
                                    detectTapGestures { tapOffset ->
                                        if (tidalPatterns.isNotEmpty() && size.width > 0) {
                                            val slotWidth = size.width.toFloat() / tidalPatterns.size
                                            val tappedIdx = (tapOffset.x / slotWidth).toInt()
                                                .coerceIn(0, tidalPatterns.size - 1)
                                            selectedDayIndex = tappedIdx
                                        }
                                    }
                                }
                        ) {
                            val width = size.width
                            val height = size.height
                            val padTop = 18.dp.toPx()
                            val padBottom = 22.dp.toPx()
                            val padHorizontal = 18.dp.toPx()
                            val usableWidth = (width - padHorizontal * 2).coerceAtLeast(1f)
                            val usableHeight = (height - padTop - padBottom).coerceAtLeast(1f)
                            val maxScaleKm = 5.6f

                            // Horizontal dashed gridlines (Recharts CartesianGrid style)
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                            for (gridStep in 0..3) {
                                val y = padTop + usableHeight * (gridStep / 3f)
                                drawLine(
                                    color = Color(0xFFCBD5E1),
                                    start = Offset(padHorizontal, y),
                                    end = Offset(width - padHorizontal, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = dashEffect
                                )
                            }

                            if (tidalPatterns.isNotEmpty()) {
                                val stepX = if (tidalPatterns.size > 1) {
                                    usableWidth / (tidalPatterns.size - 1)
                                } else {
                                    usableWidth
                                }

                                val recessionPoints = tidalPatterns.mapIndexed { idx, item ->
                                    val x = padHorizontal + idx * stepX
                                    val normalized = ((item.maxRecessionKm.toFloat() / maxScaleKm) * chartProgress).coerceIn(0.05f, 1f)
                                    val y = padTop + usableHeight * (1f - normalized)
                                    Offset(x, y)
                                }

                                val tideSurgePoints = tidalPatterns.mapIndexed { idx, item ->
                                    val x = padHorizontal + idx * stepX
                                    val normalized = ((item.highTideSurgeMeters.toFloat() / maxScaleKm) * chartProgress).coerceIn(0.05f, 1f)
                                    val y = padTop + usableHeight * (1f - normalized)
                                    Offset(x, y)
                                }

                                if (chartViewMode == "BAR") {
                                    // Dual Bar Chart Mode
                                    val barWidth = (stepX * 0.28f).coerceAtLeast(6.dp.toPx())
                                    tidalPatterns.forEachIndexed { idx, _ ->
                                        val recPt = recessionPoints[idx]
                                        val surgePt = tideSurgePoints[idx]
                                        val bottomY = padTop + usableHeight
                                        val isSelected = idx == selectedDayIndex

                                        drawRoundRect(
                                            color = if (isSelected) OceanBlueDark else OceanBlue,
                                            topLeft = Offset(recPt.x - barWidth - 2f, recPt.y),
                                            size = Size(barWidth, (bottomY - recPt.y).coerceAtLeast(4f)),
                                            cornerRadius = CornerRadius(6f, 6f)
                                        )
                                        drawRoundRect(
                                            color = if (isSelected) Color(0xFF047857) else EmeraldGreen,
                                            topLeft = Offset(surgePt.x + 2f, surgePt.y),
                                            size = Size(barWidth, (bottomY - surgePt.y).coerceAtLeast(4f)),
                                            cornerRadius = CornerRadius(6f, 6f)
                                        )
                                    }
                                } else {
                                    // Recharts AreaChart + LineChart Mode
                                    val linePath = Path().apply {
                                        recessionPoints.forEachIndexed { index, pt ->
                                            if (index == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                                        }
                                    }
                                    val areaPath = Path().apply {
                                        addPath(linePath)
                                        lineTo(recessionPoints.last().x, padTop + usableHeight)
                                        lineTo(recessionPoints.first().x, padTop + usableHeight)
                                        close()
                                    }
                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                OceanBlue.copy(alpha = 0.35f),
                                                OceanBlue.copy(alpha = 0.04f)
                                            ),
                                            startY = padTop,
                                            endY = padTop + usableHeight
                                        )
                                    )
                                    drawPath(
                                        path = linePath,
                                        color = OceanBlue,
                                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )

                                    // Secondary High Tide Surge Curve
                                    val surgePath = Path().apply {
                                        tideSurgePoints.forEachIndexed { index, pt ->
                                            if (index == 0) moveTo(pt.x, pt.y) else lineTo(pt.x, pt.y)
                                        }
                                    }
                                    drawPath(
                                        path = surgePath,
                                        color = EmeraldGreen,
                                        style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                                    )

                                    // Active Day Vertical Reference Line & Data Dots
                                    recessionPoints.forEachIndexed { idx, pt ->
                                        val isSelected = idx == selectedDayIndex
                                        if (isSelected) {
                                            drawLine(
                                                color = AmberGold,
                                                start = Offset(pt.x, padTop),
                                                end = Offset(pt.x, padTop + usableHeight),
                                                strokeWidth = 1.5.dp.toPx(),
                                                pathEffect = dashEffect
                                            )
                                        }
                                        drawCircle(
                                            color = Color.White,
                                            radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
                                            center = pt
                                        )
                                        drawCircle(
                                            color = if (isSelected) OceanBlueDark else OceanBlue,
                                            radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                                            center = pt
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // 7-Day Interactive X-Axis Selector Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        tidalPatterns.forEachIndexed { idx, day ->
                            val isSelected = selectedDayIndex == idx
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) OceanBlue else Color.White,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) OceanBlue else Color(0xFFCBD5E1)
                                ),
                                modifier = Modifier
                                    .clickable { selectedDayIndex = idx }
                                    .testTag("tidal_analytics_day_chip_$idx")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = if (isOdia) day.dayLabelOr else day.dayLabelEn,
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isSelected) Color.White else BentoSlate900
                                    )
                                    Text(
                                        text = "${day.maxRecessionKm} km",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(0xFFE0F2FE) else OceanBlue
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Selected Day Detailed Tidal Recession Telemetry Card
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF0F9FF),
                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tidal_analytics_selected_day_detail_card")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia)
                                "🌊 ${activeDay.dayLabelOr} (${activeDay.dateLabel}) • ${activeDay.lunarPhaseOr}"
                            else
                                "🌊 ${activeDay.dayLabelEn} (${activeDay.dateLabel}) • ${activeDay.lunarPhaseEn}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF0C4A6E)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "Recedes ${activeDay.maxRecessionKm} km",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = if (isOdia)
                            "ଭଟ୍ଟା ସମୟ: ${activeDay.lowTideTime} • ଜୁଆର ସମୟ: ${activeDay.highTideTime}"
                        else
                            "Low Tide (Recession): ${activeDay.lowTideTime} • High Tide: ${activeDay.highTideTime}",
                        fontSize = 11.sp,
                        color = Color(0xFF0369A1),
                        fontWeight = FontWeight.SemiBold
                    )

                    Text(
                        text = if (isOdia)
                            "ସୁରକ୍ଷିତ ବେଳାଭୂମି ଭ୍ରମଣ: ${activeDay.safeWalkWindowMins} ମିନିଟ୍ • ଜୁଆର ଉଚ୍ଚତା: ${activeDay.highTideSurgeMeters}m"
                        else
                            "Safe Seabed Walk Window: ${activeDay.safeWalkWindowMins} mins • High Tide Surge: ${activeDay.highTideSurgeMeters}m (Low: ${activeDay.lowTideDepthMeters}m)",
                        fontSize = 11.sp,
                        color = Color(0xFF0C4A6E)
                    )
                }
            }
        }
    }
}
