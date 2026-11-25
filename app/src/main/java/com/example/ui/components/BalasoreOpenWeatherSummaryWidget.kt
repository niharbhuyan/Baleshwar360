package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.AlertSeverity
import com.example.data.model.AppLanguage
import com.example.data.model.BalasoreWeatherAlert
import com.example.data.remote.OpenWeatherCurrentResponse
import com.example.data.remote.OpenWeatherDailyForecast
import com.example.data.repository.OpenWeatherRepository
import com.example.data.repository.SevereWeatherAlertRepository
import com.example.data.service.BalasoreOpenWeatherAlertService
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate400
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.OceanBlue
import com.example.util.ShareHelper
import com.example.widget.BalasoreWeatherWidgetProvider
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Balasore District Micro-Climate Node definition for the Weather Forecast Dashboard Widget.
 */
data class BalasoreWidgetDistrictNode(
    val id: String,
    val nameEn: String,
    val nameOr: String,
    val zoneTypeEn: String,
    val zoneTypeOr: String,
    val lat: Double,
    val lon: Double
)

private val BALASORE_WIDGET_DISTRICT_NODES = listOf(
    BalasoreWidgetDistrictNode(
        id = "balasore_town",
        nameEn = "Balasore Town HQ",
        nameOr = "ବାଲେଶ୍ୱର ସହର",
        zoneTypeEn = "Fakir Mohan Golai • District HQ",
        zoneTypeOr = "ଫକିର ମୋହନ ଗୋଲେଇ • ଜିଲ୍ଲା ମୁଖ୍ୟାଳୟ",
        lat = 21.4934,
        lon = 86.9135
    ),
    BalasoreWidgetDistrictNode(
        id = "chandipur_coast",
        nameEn = "Chandipur Coast",
        nameOr = "ଚାନ୍ଦିପୁର ଉପକୂଳ",
        zoneTypeEn = "ITR Range • Intertidal Bay Zone",
        zoneTypeOr = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି • ସମୁଦ୍ର ତଟ",
        lat = 21.4448,
        lon = 87.0149
    ),
    BalasoreWidgetDistrictNode(
        id = "talasari_subarnarekha",
        nameEn = "Talasari & Bhograi",
        nameOr = "ତାଳସାରୀ ଓ ଭୋଗରାଇ",
        zoneTypeEn = "North Balasore • Subarnarekha Mouth",
        zoneTypeOr = "ଉତ୍ତର ବାଲେଶ୍ୱର • ସୁବର୍ଣ୍ଣରେଖା ମୁହାଣ",
        lat = 21.6225,
        lon = 87.4058
    ),
    BalasoreWidgetDistrictNode(
        id = "nilagiri_hills",
        nameEn = "Nilagiri Foothills",
        nameOr = "ନୀଳଗିରି ପାହାଡ଼",
        zoneTypeEn = "Kuldiha Reserve • Panchalingeswar",
        zoneTypeOr = "କୁଲଡ଼ିହା ଅଭୟାରଣ୍ୟ • ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର",
        lat = 21.4601,
        lon = 86.7745
    ),
    BalasoreWidgetDistrictNode(
        id = "kasafal_bahanaga",
        nameEn = "Kasafal & Bahanaga",
        nameOr = "କଷାଫଳ ଓ ବାହାନଗା",
        zoneTypeEn = "Budhabalanga Estuary • Fishing Jetty",
        zoneTypeOr = "ବୁଢ଼ାବଳଙ୍ଗ ମୁହାଣ • ମାଛଧରା ଜେଟି",
        lat = 21.5220,
        lon = 87.1210
    )
)

/**
 * Weather Forecast Dashboard Widget for the Balasore district powered by the OpenWeatherMap API.
 * Displays:
 * 1. Balasore District micro-climate node selector (Balasore Town, Chandipur Coast, Talasari, Nilagiri, Kasafal).
 * 2. Current conditions for Balasore in Celsius / Fahrenheit with instant toggle, remote condition icon,
 *    feels-like temperature, humidity %, wind speed, barometric pressure, and visibility.
 * 3. 5-Day / 3-Hour OpenWeatherMap Forecast Strip specifically for the Balasore district.
 * 4. Real-Time Severe Weather Alerts for the Balasore district (`BalasoreOpenWeatherAlertService`)
 *    with severity badges (`CYCLONE_ALERT`, `WARNING`, `HIGH_TIDE`, `ADVISORY`), affected Balasore zones,
 *    expandable safety instructions, one-tap social sharing, and Room database alert history.
 * 5. Direct synchronization with the Android Home Screen Widget (`BalasoreWeatherWidgetProvider`).
 */
@Composable
fun BalasoreOpenWeatherSummaryWidget(
    currentWeather: OpenWeatherCurrentResponse? = null,
    isLoading: Boolean = false,
    language: AppLanguage = AppLanguage.ENGLISH,
    isFahrenheit: Boolean = false,
    apiKey: String? = null,
    onToggleTempUnit: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenDetails: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA

    var selectedNode by remember { mutableStateOf(BALASORE_WIDGET_DISTRICT_NODES.first()) }
    var localWeather by remember { mutableStateOf<OpenWeatherCurrentResponse?>(null) }
    var fiveDayForecast by remember { mutableStateOf<List<OpenWeatherDailyForecast>>(emptyList()) }
    var liveDistrictAlerts by remember { mutableStateOf<List<BalasoreWeatherAlert>>(emptyList()) }
    var internalLoading by remember { mutableStateOf(false) }
    var showSafetyInstructions by remember { mutableStateOf(false) }

    val effectiveWeather = if (selectedNode.id == "balasore_town" && currentWeather != null && localWeather == null) {
        currentWeather
    } else {
        localWeather ?: currentWeather
    }
    val effectiveLoading = isLoading || internalLoading

    // Room Database Severe Weather Alerts repository (Alert History)
    val alertRepo = remember { SevereWeatherAlertRepository(context) }
    val roomAlerts by alertRepo.last5Alerts.collectAsState(initial = emptyList())
    var showAlertHistory by remember { mutableStateOf(false) }

    // Seed Room database if empty
    LaunchedEffect(Unit) {
        alertRepo.seedSampleAlertsIfEmpty()
    }

    // Core loader that fetches OpenWeatherMap current conditions, 5-day forecast, and Balasore severe alerts
    fun refreshNodeTelemetry(node: BalasoreWidgetDistrictNode, forceRefreshAlerts: Boolean = false) {
        coroutineScope.launch {
            internalLoading = true
            try {
                val repo = OpenWeatherRepository(context)
                val weatherRes = repo.fetchCurrentWeatherForNode(
                    lat = node.lat,
                    lon = node.lon,
                    apiKey = apiKey ?: ""
                )
                weatherRes.getOrNull()?.let { localWeather = it }

                val forecastRes = repo.fetchFiveDayForecast(
                    lat = node.lat,
                    lon = node.lon,
                    apiKey = apiKey ?: ""
                )
                forecastRes.getOrNull()?.let { fiveDayForecast = it }

                val alerts = BalasoreOpenWeatherAlertService.fetchLocationWeatherAlerts(
                    context = context,
                    apiKey = apiKey,
                    forceRefresh = forceRefreshAlerts
                )
                liveDistrictAlerts = alerts

                // Sync to Android Home Screen Widget (BalasoreWeatherWidgetProvider)
                val w = weatherRes.getOrNull() ?: effectiveWeather
                val tempInt = w?.main?.temp?.roundToInt() ?: 31
                val feelsInt = w?.main?.feelsLike?.roundToInt() ?: 34
                val humInt = w?.main?.humidity ?: 74
                val windKmhInt = ((w?.wind?.speed ?: 4.2) * 3.6).roundToInt()
                val pressInt = w?.main?.pressure ?: 1008
                val condText = w?.weather?.firstOrNull()?.description
                    ?.replaceFirstChar { it.uppercase() } ?: "Partly Cloudy"
                val topAlert = alerts.firstOrNull()
                val alertSummary = if (topAlert != null) {
                    "⚠️ ${topAlert.severity.levelName}: ${topAlert.title} (${node.nameEn})"
                } else {
                    "✅ All Clear in ${node.nameEn} • Normal Balasore Coastal Conditions"
                }

                BalasoreWeatherWidgetProvider.saveWidgetData(
                    context = context,
                    tempC = tempInt,
                    conditionDesc = "$condText • ${node.nameEn}",
                    windInfo = "💨 Wind: $windKmhInt km/h • $pressInt hPa",
                    alertText = alertSummary,
                    hasAlert = topAlert != null && topAlert.severity != AlertSeverity.SAFE,
                    feelsLikeC = feelsInt,
                    humidity = humInt
                )
                BalasoreWeatherWidgetProvider.updateAllWidgets(context)
            } catch (_: Exception) {
                // Resilient fallback already handled in OpenWeatherRepository
            } finally {
                internalLoading = false
            }
        }
    }

    // Fetch whenever selected Balasore district node changes
    LaunchedEffect(selectedNode.id) {
        refreshNodeTelemetry(selectedNode, forceRefreshAlerts = false)
    }

    // Dynamic auto-update state & 60-second automatic refresh loop
    var secondsSinceLastUpdate by remember { mutableStateOf(0) }
    LaunchedEffect(selectedNode.id) {
        secondsSinceLastUpdate = 0
        while (true) {
            delay(1000)
            secondsSinceLastUpdate++
            if (secondsSinceLastUpdate >= 60) {
                secondsSinceLastUpdate = 0
                refreshNodeTelemetry(selectedNode, forceRefreshAlerts = true)
                if (currentWeather != null) {
                    onRefresh()
                }
            }
        }
    }

    // Refresh animation spin
    val infiniteTransition = rememberInfiniteTransition(label = "owm_summary_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "owm_summary_spin_angle"
    )

    // Helper functions for formatting temperature and weather condition
    fun formatTemp(celsius: Double?): String {
        if (celsius == null) return "--°"
        return if (isFahrenheit) {
            val f = (celsius * 1.8 + 32).roundToInt()
            "$f°F"
        } else {
            val c = celsius.roundToInt()
            "$c°C"
        }
    }

    val primaryCondition = effectiveWeather?.weather?.firstOrNull()
    val conditionTitle = primaryCondition?.main ?: "Clear"
    val conditionDesc = primaryCondition?.description?.replaceFirstChar { it.uppercase() } ?: "Clear Sky"
    val iconCode = primaryCondition?.icon ?: "01d"
    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"

    // Map weather condition to vector icon and atmospheric colors
    val (vectorIcon: ImageVector, iconTint: Color, gradientColors: List<Color>) = when {
        iconCode.startsWith("11") || conditionTitle.contains("Thunder", true) -> {
            Triple(
                Icons.Default.Thunderstorm,
                Color(0xFFFBBF24),
                listOf(Color(0xFF312E81), Color(0xFF1E1B4B))
            )
        }
        iconCode.startsWith("09") || iconCode.startsWith("10") || conditionTitle.contains("Rain", true) -> {
            Triple(
                Icons.Default.WaterDrop,
                Color(0xFF38BDF8),
                listOf(Color(0xFF0369A1), Color(0xFF0C4A6E))
            )
        }
        iconCode.startsWith("02") || iconCode.startsWith("03") || iconCode.startsWith("04") || conditionTitle.contains("Cloud", true) -> {
            Triple(
                Icons.Default.Cloud,
                Color(0xFFBAE6FD),
                listOf(Color(0xFF0284C7), Color(0xFF0369A1))
            )
        }
        iconCode.startsWith("50") || conditionTitle.contains("Mist", true) || conditionTitle.contains("Haze", true) -> {
            Triple(
                Icons.Default.Air,
                Color(0xFFE2E8F0),
                listOf(Color(0xFF475569), Color(0xFF334155))
            )
        }
        else -> {
            Triple(
                Icons.Default.WbSunny,
                Color(0xFFFDE047),
                listOf(Color(0xFF0284C7), Color(0xFF075985))
            )
        }
    }

    val localizedConditionText = if (isOdia) {
        when {
            conditionDesc.contains("Thunderstorm", ignoreCase = true) -> "ବଜ୍ରପାତ ଓ ଝଡ଼ବର୍ଷା"
            conditionDesc.contains("Heavy Rain", ignoreCase = true) -> "ପ୍ରବଳ ବର୍ଷା"
            conditionDesc.contains("Rain", ignoreCase = true) || conditionDesc.contains("Drizzle", ignoreCase = true) -> "ବର୍ଷା / ଝିପିଝିପି"
            conditionDesc.contains("Few Clouds", ignoreCase = true) -> "ସ୍ୱଳ୍ପ ମେଘାଚ୍ଛନ୍ନ"
            conditionDesc.contains("Scattered Clouds", ignoreCase = true) -> "ଖଣ୍ଡବିଖଣ୍ଡିତ ମେଘ"
            conditionDesc.contains("Broken Clouds", ignoreCase = true) || conditionDesc.contains("Overcast", ignoreCase = true) -> "ମେଘାଚ୍ଛନ୍ନ ଆକାଶ"
            conditionDesc.contains("Mist", ignoreCase = true) || conditionDesc.contains("Haze", ignoreCase = true) || conditionDesc.contains("Fog", ignoreCase = true) -> "କୁହୁଡ଼ିଆ ପାଗ"
            conditionDesc.contains("Clear", ignoreCase = true) -> "ନିର୍ମଳ ଆକାଶ"
            else -> conditionDesc
        }
    } else {
        conditionDesc
    }

    val rawTemp = effectiveWeather?.main?.temp ?: 29.0
    val feelsLikeTemp = effectiveWeather?.main?.feelsLike ?: (rawTemp + 2.5)
    val humidity = effectiveWeather?.main?.humidity ?: 74
    val pressureHpa = effectiveWeather?.main?.pressure ?: 1008
    val visibilityKm = ((effectiveWeather?.visibility ?: 10000) / 1000.0)
    val windSpeedMps = effectiveWeather?.wind?.speed ?: 3.8
    val windKmh = (windSpeedMps * 3.6).roundToInt()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(22.dp))
            .clickable { onOpenDetails() }
            .testTag("open_weather_summary_widget"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoSlate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize()
        ) {
            // Top Row: Location, OpenWeatherMap Tag & Controls
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
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Balasore District Location",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱର ଜିଲ୍ଲା ପାଣିପାଗ ଓ ପୂର୍ବାନୁମାନ" else "Balasore Weather Forecast Widget",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 14.5.sp,
                                    color = BentoSlate900
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // OWM Live Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7),
                                border = BorderStroke(0.5.dp, Color(0xFF86EFAC))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF16A34A))
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "OWM API",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF166534)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isOdia)
                                "${selectedNode.nameOr} (${selectedNode.zoneTypeOr}) • ${secondsSinceLastUpdate}s ପୂର୍ବେ"
                            else
                                "${selectedNode.nameEn} (${String.format(Locale.US, "%.2f°N, %.2f°E", selectedNode.lat, selectedNode.lon)}) • ${secondsSinceLastUpdate}s ago",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 10.5.sp,
                                color = BentoSlate500
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Controls: °C / °F Unit Toggle and Refresh Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .clickable { onToggleTempUnit() }
                            .testTag("open_weather_temp_unit_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "°C",
                                fontSize = 11.sp,
                                fontWeight = if (!isFahrenheit) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (!isFahrenheit) OceanBlue else BentoSlate400
                            )
                            Text(
                                text = " | ",
                                fontSize = 10.sp,
                                color = BentoSlate400
                            )
                            Text(
                                text = "°F",
                                fontSize = 11.sp,
                                fontWeight = if (isFahrenheit) FontWeight.ExtraBold else FontWeight.Normal,
                                color = if (isFahrenheit) OceanBlue else BentoSlate400
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = {
                            secondsSinceLastUpdate = 0
                            refreshNodeTelemetry(selectedNode, forceRefreshAlerts = true)
                            if (currentWeather != null) {
                                onRefresh()
                            }
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("open_weather_refresh_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Balasore Weather",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier
                                .size(17.dp)
                                .rotate(if (effectiveLoading) spinAngle else 0f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Balasore District Micro-Climate Node Selector Strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .testTag("weather_widget_node_selector_row"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BALASORE_WIDGET_DISTRICT_NODES.forEach { node ->
                    val isSelected = node.id == selectedNode.id
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFFF1F5F9),
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF0284C7) else Color(0xFFE2E8F0)
                        ),
                        modifier = Modifier
                            .clickable { selectedNode = node }
                            .testTag("weather_widget_node_${node.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = if (isSelected) Color.White else Color(0xFF0284C7),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOdia) node.nameOr else node.nameEn,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                color = if (isSelected) Color.White else BentoSlate700
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2-Tab Selector: 'Live Forecast & Alerts' vs 'Historical Alerts'
            val activeAlertCount = liveDistrictAlerts.size
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .padding(4.dp)
                    .testTag("weather_widget_tab_row"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = if (!showAlertHistory) OceanBlue else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showAlertHistory = false }
                        .testTag("weather_widget_live_summary_tab")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = if (!showAlertHistory) Color.White else BentoSlate700,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isOdia) "ଲାଇଭ୍ ପୂର୍ବାନୁମାନ ଓ ସତର୍କତା ($activeAlertCount)" else "Live Forecast & Alerts ($activeAlertCount)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (!showAlertHistory) Color.White else BentoSlate700
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(9.dp),
                    color = if (showAlertHistory) Color(0xFFDC2626) else Color.Transparent,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { showAlertHistory = true }
                        .testTag("weather_widget_historical_alerts_tab")
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 7.dp, horizontal = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = if (showAlertHistory) Color.White else Color(0xFFDC2626),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isOdia) "ଐତିହାସିକ ସତର୍କତା (${roomAlerts.size})" else "Historical Alerts (${roomAlerts.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (showAlertHistory) Color.White else Color(0xFFDC2626)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Main Highlight Container with Gradient, Current Temp & Weather Condition Icon
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Brush.horizontalGradient(gradientColors))
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Column: Large Current Temperature & Condition Text
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.testTag("open_weather_temperature_row")
                        ) {
                            Text(
                                text = formatTemp(rawTemp),
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 34.sp
                                ),
                                modifier = Modifier.testTag("open_weather_current_temperature")
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "($conditionTitle)",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = Color(0xFFE0F2FE),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                modifier = Modifier.padding(bottom = 6.dp)
                            )
                        }

                        // Brief Weather Condition & Localized Subtext
                        Text(
                            text = localizedConditionText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.5.sp
                            ),
                            modifier = Modifier.testTag("open_weather_condition_desc")
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = if (isOdia)
                                "ଅନୁଭୂତ: ${formatTemp(feelsLikeTemp)} • ବାୟୁ: $windKmh କିମି/ଘଣ୍ଟା • ଚାପ: $pressureHpa hPa"
                            else
                                "Feels like: ${formatTemp(feelsLikeTemp)} • Wind: $windKmh km/h • $pressureHpa hPa",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFBAE6FD),
                                fontSize = 11.sp
                            )
                        )
                    }

                    // Right Side: Brief Weather Condition Icon with OpenWeatherMap remote art & vector fallback
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
                            .testTag("open_weather_condition_icon"),
                        contentAlignment = Alignment.Center
                    ) {
                        SubcomposeAsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(iconUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = "OpenWeather condition icon: $conditionTitle",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(54.dp),
                            loading = {
                                Box(contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                }
                            },
                            error = {
                                Icon(
                                    imageVector = vectorIcon,
                                    contentDescription = conditionTitle,
                                    tint = iconTint,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5-Day OpenWeatherMap Forecast Strip for Balasore District
            val displayForecast = remember(fiveDayForecast) {
                fiveDayForecast.ifEmpty {
                    OpenWeatherRepository().getCuratedBalasoreFiveDayForecastFallback()
                }
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF8FAFC))
                    .border(BorderStroke(1.dp, Color(0xFFE2E8F0)), RoundedCornerShape(14.dp))
                    .padding(10.dp)
                    .testTag("weather_widget_five_day_forecast_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "5-Day Balasore Forecast",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isOdia) "୫-ଦିନିଆ ବାଲେଶ୍ୱର ପାଣିପାଗ ପୂର୍ବାନୁମାନ" else "5-Day Balasore District Forecast",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoSlate900
                        )
                    }
                    Text(
                        text = if (isOdia) "OpenWeather 2.5" else "OWM 5-Day / 3-Hr",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0284C7)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .testTag("weather_widget_five_day_forecast_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    displayForecast.take(5).forEach { day ->
                        val dayIcon = when {
                            day.conditionMain.contains("Thunder", true) -> Icons.Default.Thunderstorm
                            day.conditionMain.contains("Rain", true) -> Icons.Default.WaterDrop
                            day.conditionMain.contains("Cloud", true) -> Icons.Default.Cloud
                            else -> Icons.Default.WbSunny
                        }
                        val dayTint = when {
                            day.conditionMain.contains("Thunder", true) -> Color(0xFFD97706)
                            day.conditionMain.contains("Rain", true) -> Color(0xFF0284C7)
                            day.conditionMain.contains("Cloud", true) -> Color(0xFF475569)
                            else -> Color(0xFFEAB308)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                            modifier = Modifier.width(96.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = if (isOdia) day.odiaDayName else day.dayName,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoSlate900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Icon(
                                    imageVector = dayIcon,
                                    contentDescription = day.conditionMain,
                                    tint = dayTint,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = "${formatTemp(day.tempMax)} / ${formatTemp(day.tempMin)}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = "💧 ${day.popPercent}% • ${day.conditionMain}",
                                    fontSize = 9.sp,
                                    color = BentoSlate500,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Live Balasore District Severe Weather Alert Banner (from BalasoreOpenWeatherAlertService + Room DB fallback)
            val primaryLiveAlert = liveDistrictAlerts.firstOrNull()
            val latestRoomAlert = roomAlerts.firstOrNull()

            if (primaryLiveAlert != null) {
                val isCritical = primaryLiveAlert.severity == AlertSeverity.CYCLONE_ALERT ||
                    primaryLiveAlert.severity == AlertSeverity.WARNING ||
                    primaryLiveAlert.severity.isUrgent
                val alertBg = if (isCritical) Color(0xFFFEF2F2) else Color(0xFFFFFBEB)
                val alertBorder = if (isCritical) Color(0xFFFECACA) else Color(0xFFFDE68A)
                val alertAccent = if (isCritical) Color(0xFFDC2626) else Color(0xFFD97706)

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = alertBg,
                    border = BorderStroke(1.dp, alertBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_weather_severe_alert_banner")
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Severe Weather Alert",
                                    tint = alertAccent,
                                    modifier = Modifier.size(17.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = alertAccent
                                ) {
                                    Text(
                                        text = if (isOdia) primaryLiveAlert.severity.odiaLabel else primaryLiveAlert.severity.levelName.uppercase(),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOdia && primaryLiveAlert.odiaTitle.isNotBlank()) primaryLiveAlert.odiaTitle else primaryLiveAlert.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF7F1D1D),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Share Severe Weather Alert Button
                                IconButton(
                                    onClick = {
                                        ShareHelper.shareWeatherAlert(
                                            context = context,
                                            alert = primaryLiveAlert,
                                            validityRemainingText = primaryLiveAlert.getFormattedTimeRemaining()
                                        )
                                    },
                                    modifier = Modifier
                                        .size(26.dp)
                                        .testTag("share_severe_weather_alert_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Balasore Severe Weather Alert",
                                        tint = alertAccent,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(4.dp))

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = alertAccent,
                                    modifier = Modifier
                                        .clickable { showAlertHistory = !showAlertHistory }
                                        .testTag("toggle_weather_alert_history_button")
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "Alert History",
                                            tint = Color.White,
                                            modifier = Modifier.size(11.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (isOdia) "ଇତିହାସ (${roomAlerts.size})" else "History (${roomAlerts.size})",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        Text(
                            text = primaryLiveAlert.summary,
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D),
                            lineHeight = 15.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Affected Balasore District Zones & Safety Steps Toggle
                        val formattedZones = primaryLiveAlert.affectedZones
                            .take(3)
                            .joinToString(", ") { if (isOdia) it.odiaName else it.zoneName }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📍 Zones: $formattedZones",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF991B1B),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .clickable { showSafetyInstructions = !showSafetyInstructions }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                                    .testTag("toggle_alert_safety_steps_button")
                            ) {
                                Text(
                                    text = if (isOdia) "ସୁରକ୍ଷା ପଦକ୍ଷେପ" else "Safety Steps",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = alertAccent
                                )
                                Icon(
                                    imageVector = if (showSafetyInstructions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = null,
                                    tint = alertAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }

                        AnimatedVisibility(visible = showSafetyInstructions) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White.copy(alpha = 0.85f))
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = "🛡️ ${primaryLiveAlert.issuingAuthority} • ${primaryLiveAlert.emergencyContact}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7F1D1D)
                                )
                                primaryLiveAlert.actionableInstructions.take(3).forEach { step ->
                                    Text(
                                        text = "• $step",
                                        fontSize = 10.sp,
                                        color = BentoSlate700,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else if (latestRoomAlert != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_weather_severe_alert_banner")
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Severe Weather Alert",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOdia) latestRoomAlert.odiaTitle else latestRoomAlert.title,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDC2626),
                                modifier = Modifier
                                    .clickable { showAlertHistory = !showAlertHistory }
                                    .testTag("toggle_weather_alert_history_button")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Alert History",
                                        tint = Color.White,
                                        modifier = Modifier.size(11.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = if (isOdia) "ଇତିହାସ (${roomAlerts.size})" else "History (${roomAlerts.size})",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = latestRoomAlert.summary,
                            fontSize = 11.sp,
                            color = Color(0xFF7F1D1D),
                            lineHeight = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Normal Weather Conditions",
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱର ଓ ଚାନ୍ଦିପୁରରେ ପାଣିପାଗ ସ୍ୱାଭାବିକ ରହିଛି" else "All Clear: Normal Weather for Balasore & Coast",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF166534),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        if (roomAlerts.isNotEmpty()) {
                            Text(
                                text = if (isOdia) "ଇତିହାସ" else "History",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .clickable { showAlertHistory = !showAlertHistory }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Expandable Alert History Tab / Card (Room Database last 5 alerts)
            AnimatedVisibility(visible = showAlertHistory) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(BorderStroke(1.dp, BentoSlate200), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                        .testTag("open_weather_alert_history_list")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "ସତର୍କତା ଇତିହାସ (ପୂର୍ବବର୍ତ୍ତୀ ୫ ଟି ରେକର୍ଡ)" else "Severe Alert History (Last 5 Records)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate700
                        )
                        Icon(
                            imageVector = Icons.Default.ExpandLess,
                            contentDescription = "Close History",
                            tint = BentoSlate500,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { showAlertHistory = false }
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    val timeFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }
                    roomAlerts.take(5).forEachIndexed { idx, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = "${idx + 1}.",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate500,
                                modifier = Modifier.width(16.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOdia) item.odiaTitle else item.title,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = "${timeFormatter.format(Date(item.receivedAtMillis))} • ${item.affectedZones}",
                                    fontSize = 9.5.sp,
                                    color = BentoSlate500
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Metrics Bar: Humidity, Wind, Visibility, Home Widget Sync & Details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.horizontalScroll(rememberScrollState()).weight(1f)
                ) {
                    // Humidity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Humidity",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$humidity%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSlate700
                        )
                    }

                    // Wind
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = "Wind Speed",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$windKmh km/h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSlate700
                        )
                    }

                    // Visibility
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "Visibility",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f km", visibilityKm),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSlate700
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Sync Home Screen Widget Action Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFE0F2FE),
                        border = BorderStroke(0.5.dp, Color(0xFF7DD3FC)),
                        modifier = Modifier
                            .clickable {
                                refreshNodeTelemetry(selectedNode, forceRefreshAlerts = true)
                                BalasoreWeatherWidgetProvider.triggerUpdate(context)
                                Toast.makeText(
                                    context,
                                    if (isOdia) "ହୋମ୍ ସ୍କ୍ରିନ୍ ପାଣିପାଗ ୱିଜେଟ୍ ଅପଡେଟ୍ ହେଲା!"
                                    else "Balasore Home Screen Weather Widget synced!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .testTag("sync_home_weather_widget_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Widgets,
                                contentDescription = "Sync Home Screen Widget",
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(11.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isOdia) "ୱିଜେଟ୍ ସିଙ୍କ୍" else "Sync Widget",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Interactive tap link to OpenWeatherMap detail
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenDetails() }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isOdia) "ସବିଶେଷ" else "Details",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = "Open OpenWeatherMap Telemetry",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Convenient alias matching the naming convention requested.
 */
@Composable
fun OpenWeatherSummaryWidget(
    currentWeather: OpenWeatherCurrentResponse? = null,
    isLoading: Boolean = false,
    language: AppLanguage = AppLanguage.ENGLISH,
    isFahrenheit: Boolean = false,
    apiKey: String? = null,
    onToggleTempUnit: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenDetails: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BalasoreOpenWeatherSummaryWidget(
        currentWeather = currentWeather,
        isLoading = isLoading,
        language = language,
        isFahrenheit = isFahrenheit,
        apiKey = apiKey,
        onToggleTempUnit = onToggleTempUnit,
        onRefresh = onRefresh,
        onOpenDetails = onOpenDetails,
        modifier = modifier
    )
}
