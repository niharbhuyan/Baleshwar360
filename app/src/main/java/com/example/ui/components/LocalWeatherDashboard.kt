package com.example.ui.components

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.BuildConfig
import com.example.data.model.AlertSeverity
import com.example.data.model.AppLanguage
import com.example.data.model.BalasoreWeatherAlert
import com.example.data.model.BalasoreZone
import com.example.data.notification.TideAndWeatherNotificationManager
import com.example.data.remote.OpenWeatherCurrentResponse
import com.example.data.remote.OpenWeatherDailyForecast
import com.example.data.repository.OpenWeatherRepository
import com.example.data.service.BalasoreOpenWeatherAlertService
import com.example.ui.theme.*
import com.example.util.BalasoreHapticHelper
import com.example.util.ShareHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * LocalWeatherDashboard Composable:
 * A dedicated Compose UI component for the Balasore local weather dashboard that fetches and displays:
 * 1. Real-time weather alerts (IMD / OpenWeatherMap coastal & district hazards with actionable safety steps)
 * 2. A live 5-day weather forecast with interactive 3-hour telemetry slots
 * 3. Current micro-climate conditions using BuildConfig.OPENWEATHERMAP_API_KEY configured in build.gradle.kts.
 */
@Composable
fun LocalWeatherDashboard(
    language: AppLanguage = AppLanguage.ENGLISH,
    initialFahrenheit: Boolean = false,
    onToggleTempUnit: ((Boolean) -> Unit)? = null,
    onOpenFullForecast: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA
    val repository = remember { OpenWeatherRepository(context) }

    // Read the OpenWeatherMap API key injected by app/build.gradle.kts
    val openWeatherApiKey = remember { BuildConfig.OPENWEATHERMAP_API_KEY.trim() }
    val isLiveKeyConfigured = remember(openWeatherApiKey) {
        repository.isApiKeyConfigured(openWeatherApiKey)
    }

    var selectedNode by remember { mutableStateOf(BALASORE_NODES_LIST.first()) }
    var isFahrenheit by remember(initialFahrenheit) { mutableStateOf(initialFahrenheit) }
    var isLoading by remember { mutableStateOf(true) }
    var currentWeather by remember { mutableStateOf<OpenWeatherCurrentResponse?>(null) }
    var realTimeAlerts by remember { mutableStateOf<List<BalasoreWeatherAlert>>(emptyList()) }
    var fiveDayForecast by remember { mutableStateOf<List<OpenWeatherDailyForecast>>(emptyList()) }
    var selectedAlertSeverity by remember { mutableStateOf<AlertSeverity?>(null) }
    var expandedAlertId by remember { mutableStateOf<String?>(null) }
    var selectedDayIndex by remember { mutableIntStateOf(0) }
    var lastUpdatedLabel by remember { mutableStateOf("") }

    var refreshRotationTarget by remember { mutableFloatStateOf(0f) }
    val animatedRefreshRotation by animateFloatAsState(
        targetValue = refreshRotationTarget,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "local_weather_dashboard_refresh_rotation"
    )

    fun formatTemperature(tempCelsius: Double?): String {
        val c = tempCelsius ?: 29.0
        return if (isFahrenheit) {
            "${((c * 9.0 / 5.0) + 32.0).roundToInt()}°F"
        } else {
            "${c.roundToInt()}°C"
        }
    }

    val fetchDashboardTelemetry: (Boolean) -> Unit = { isManualRefresh ->
        if (isManualRefresh) {
            refreshRotationTarget += 360f
            BalasoreHapticHelper.vibrateButtonClick(context)
        }
        isLoading = true
        coroutineScope.launch {
            try {
                val currentResult = repository.fetchCurrentWeatherForNode(
                    lat = selectedNode.lat,
                    lon = selectedNode.lon,
                    apiKey = openWeatherApiKey
                )
                currentWeather = currentResult.getOrElse {
                    repository.getCuratedBalasoreWeatherFallback()
                }

                val fetchedAlerts = BalasoreOpenWeatherAlertService.fetchLocationWeatherAlerts(
                    context = context,
                    apiKey = openWeatherApiKey,
                    forceRefresh = isManualRefresh
                )
                realTimeAlerts = fetchedAlerts
                if (expandedAlertId == null) {
                    expandedAlertId = fetchedAlerts.firstOrNull()?.id
                }

                val forecastResult = repository.fetchFiveDayForecast(
                    lat = selectedNode.lat,
                    lon = selectedNode.lon,
                    apiKey = openWeatherApiKey
                )
                fiveDayForecast = forecastResult.getOrElse {
                    repository.getCuratedBalasoreFiveDayForecastFallback()
                }.take(5)

                val formatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                lastUpdatedLabel = formatter.format(Date())

                if (isManualRefresh) {
                    BalasoreHapticHelper.vibrateSuccess(context)
                }
            } catch (_: Exception) {
                currentWeather = repository.getCuratedBalasoreWeatherFallback()
                realTimeAlerts = BalasoreOpenWeatherAlertService.fetchLocationWeatherAlerts(
                    context = context,
                    apiKey = openWeatherApiKey,
                    forceRefresh = false
                )
                fiveDayForecast = repository.getCuratedBalasoreFiveDayForecastFallback().take(5)
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(selectedNode, openWeatherApiKey) {
        fetchDashboardTelemetry(false)
    }

    val filteredAlerts = remember(realTimeAlerts, selectedNode, selectedAlertSeverity) {
        realTimeAlerts.filter { alert ->
            val matchesNode = alert.affectedZones.contains(selectedNode.zone) ||
                alert.affectedZones.contains(BalasoreZone.DISTRICT_WIDE) ||
                selectedNode.zone == BalasoreZone.BALASORE_TOWN
            val matchesSeverity = selectedAlertSeverity == null || alert.severity == selectedAlertSeverity
            matchesNode && matchesSeverity
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("local_weather_dashboard_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, OceanBlue.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ================= 1. TOP HEADER & CONTROLS =================
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
                                    colors = listOf(OceanBlue, OceanBlueDark)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Thunderstorm,
                            contentDescription = "Local Weather Dashboard",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ସ୍ଥାନୀୟ ପାଣିପାଗ ଓ ୫-ଦିନିଆ ଡ୍ୟାସବୋର୍ଡ" else "Local Weather & Alerts Dashboard",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOdia)
                                "${selectedNode.nameOr} • OpenWeatherMap ଲାଇଭ୍"
                            else
                                "${selectedNode.nameEn} (${selectedNode.lat}°N, ${selectedNode.lon}°E)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // °C / °F Unit Toggle
                    Surface(
                        onClick = {
                            BalasoreHapticHelper.vibrateToggle(context)
                            isFahrenheit = !isFahrenheit
                            onToggleTempUnit?.invoke(isFahrenheit)
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = OceanBlue.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("local_weather_dashboard_unit_toggle")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isFahrenheit) "°F" else "°C",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = OceanBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Toggle Celsius or Fahrenheit",
                                tint = OceanBlue,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }

                    // Manual Refresh Button
                    IconButton(
                        onClick = { fetchDashboardTelemetry(true) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("local_weather_dashboard_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Weather & Alerts",
                            tint = OceanBlue,
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(animatedRefreshRotation)
                        )
                    }
                }
            }

            // ================= 2. MICRO-CLIMATE ZONE SELECTOR & API STATUS =================
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("local_weather_dashboard_node_selector")
                ) {
                    items(BALASORE_NODES_LIST, key = { it.id }) { node ->
                        val isSelected = selectedNode.id == node.id
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedNode = node
                            },
                            label = {
                                Text(
                                    text = if (isOdia) node.nameOr else node.nameEn,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            } else null,
                            modifier = Modifier.testTag("local_weather_node_chip_${node.id}")
                        )
                    }
                }

                // API Key Telemetry Status Bar
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isLiveKeyConfigured) Color(0xFFF0FDF4) else BentoBlueLight,
                    border = BorderStroke(
                        1.dp,
                        if (isLiveKeyConfigured) Color(0xFFBBF7D0) else OceanBlue.copy(alpha = 0.25f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("local_weather_dashboard_api_badge")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isLiveKeyConfigured) EmeraldGreen else OceanBlue)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLiveKeyConfigured) {
                                    if (isOdia) "OpenWeatherMap API ସଂଯୁକ୍ତ (BuildConfig)" else "OpenWeatherMap Live API Active (BuildConfig)"
                                } else {
                                    if (isOdia) "OpenWeatherMap ବାଲେଶ୍ୱର ଉପକୂଳ ଷ୍ଟ୍ରିମ୍" else "OpenWeatherMap Balasore Telemetry Stream"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isLiveKeyConfigured) Color(0xFF15803D) else BentoBlueText
                            )
                        }
                        if (lastUpdatedLabel.isNotBlank()) {
                            Text(
                                text = if (isOdia) "ଅଦ୍ୟତନ: $lastUpdatedLabel" else "Updated $lastUpdatedLabel",
                                fontSize = 10.5.sp,
                                color = BentoSlate600
                            )
                        }
                    }
                }
            }

            // ================= 3. CURRENT WEATHER HERO SUMMARY =================
            val weather = currentWeather
            if (weather != null) {
                val condition = weather.weather.firstOrNull()
                val tempC = weather.main?.temp ?: 29.5
                val feelsLikeC = weather.main?.feelsLike ?: 32.0
                val minC = weather.main?.tempMin ?: 25.0
                val maxC = weather.main?.tempMax ?: 32.5
                val humidity = weather.main?.humidity ?: 74
                val windKmh = ((weather.wind?.speed ?: 4.5) * 3.6).roundToInt()
                val pressure = weather.main?.pressure ?: 1008
                val visibilityKm = ((weather.visibility ?: 10000) / 1000.0).roundToInt()
                val iconCode = condition?.icon ?: "02d"
                val descText = if (isOdia) {
                    translateConditionToOdia(condition?.description)
                } else {
                    (condition?.description ?: "Pleasant coastal breeze").replaceFirstChar {
                        if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString()
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF0F172A),
                                    Color(0xFF0369A1),
                                    Color(0xFF0284C7)
                                )
                            )
                        )
                        .padding(16.dp)
                        .testTag("local_weather_dashboard_current_hero")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = formatTemperature(tempC),
                                    fontSize = 42.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.testTag("local_weather_dashboard_current_temp")
                                )
                                Text(
                                    text = descText,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFBAE6FD)
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isOdia)
                                        "ଅନୁଭୂତ ${formatTemperature(feelsLikeC)} • ସର୍ବোଚ୍ଚ ${formatTemperature(maxC)} / ସର୍ବନିମ୍ନ ${formatTemperature(minC)}"
                                    else
                                        "Feels like ${formatTemperature(feelsLikeC)} • H: ${formatTemperature(maxC)} / L: ${formatTemperature(minC)}",
                                    fontSize = 11.5.sp,
                                    color = Color.White.copy(alpha = 0.85f)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(Color.White.copy(alpha = 0.14f)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("https://openweathermap.org/img/wn/$iconCode@2x.png")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = descText,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        // Compact Atmospheric Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            DashboardAtmosphericPill(
                                icon = Icons.Default.WaterDrop,
                                label = if (isOdia) "ଆର୍ଦ୍ରତା" else "Humidity",
                                value = "$humidity%",
                                modifier = Modifier.weight(1f)
                            )
                            DashboardAtmosphericPill(
                                icon = Icons.Default.Air,
                                label = if (isOdia) "ପବନ" else "Wind",
                                value = "$windKmh km/h",
                                modifier = Modifier.weight(1f)
                            )
                            DashboardAtmosphericPill(
                                icon = Icons.Default.Speed,
                                label = if (isOdia) "ଚାପ" else "Pressure",
                                value = "$pressure hPa",
                                modifier = Modifier.weight(1f)
                            )
                            DashboardAtmosphericPill(
                                icon = Icons.Default.Visibility,
                                label = if (isOdia) "ଦୃଶ୍ୟମାନ" else "Visibility",
                                value = "$visibilityKm km",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // ================= 4. REAL-TIME WEATHER ALERTS SECTION =================
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                border = BorderStroke(
                    1.dp,
                    if (filteredAlerts.any { it.severity == AlertSeverity.WARNING || it.severity == AlertSeverity.CYCLONE_ALERT })
                        Color(0xFFEF4444)
                    else
                        Color(0xFF334155)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("local_weather_dashboard_alerts_section")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.WarningAmber,
                                contentDescription = "Real-Time Weather Alerts",
                                tint = Color(0xFFF59E0B),
                                modifier = Modifier.size(22.dp)
                            )
                            Column {
                                Text(
                                    text = if (isOdia) "ରିଅଲ୍-ଟାଇମ୍ ପାଣିପାଗ ସତର୍କତା" else "Real-Time Weather Alerts",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isOdia)
                                        "OpenWeatherMap ଓ IMD ଉପକୂଳ ସୁରକ୍ଷା ବୁଲେଟିନ୍"
                                    else
                                        "Live OpenWeatherMap & IMD Coastal Advisories",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = if (filteredAlerts.isNotEmpty()) Color(0xFFDC2626) else Color(0xFF16A34A)
                        ) {
                            Text(
                                text = if (isOdia) "${filteredAlerts.size} ସକ୍ରିୟ" else "${filteredAlerts.size} ACTIVE",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Severity Filter Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AlertSeverityChip(
                            label = if (isOdia) "ସମସ୍ତ (${realTimeAlerts.size})" else "All (${realTimeAlerts.size})",
                            selected = selectedAlertSeverity == null,
                            selectedColor = OceanBlue,
                            onClick = { selectedAlertSeverity = null },
                            testTag = "local_weather_alert_filter_all"
                        )
                        AlertSeverityChip(
                            label = if (isOdia) "ଝଡ଼ ଚେତାବନୀ" else "Storm Warning",
                            selected = selectedAlertSeverity == AlertSeverity.WARNING,
                            selectedColor = Color(0xFFDC2626),
                            onClick = {
                                selectedAlertSeverity =
                                    if (selectedAlertSeverity == AlertSeverity.WARNING) null else AlertSeverity.WARNING
                            },
                            testTag = "local_weather_alert_filter_warning"
                        )
                        AlertSeverityChip(
                            label = if (isOdia) "ସମୁଦ୍ର ଜୁଆର" else "High Tide",
                            selected = selectedAlertSeverity == AlertSeverity.HIGH_TIDE,
                            selectedColor = Color(0xFF0284C7),
                            onClick = {
                                selectedAlertSeverity =
                                    if (selectedAlertSeverity == AlertSeverity.HIGH_TIDE) null else AlertSeverity.HIGH_TIDE
                            },
                            testTag = "local_weather_alert_filter_tide"
                        )
                        AlertSeverityChip(
                            label = if (isOdia) "ମତ୍ସ୍ୟଜୀବୀ ସୂଚନା" else "Marine Advisory",
                            selected = selectedAlertSeverity == AlertSeverity.ADVISORY,
                            selectedColor = Color(0xFFD97706),
                            onClick = {
                                selectedAlertSeverity =
                                    if (selectedAlertSeverity == AlertSeverity.ADVISORY) null else AlertSeverity.ADVISORY
                            },
                            testTag = "local_weather_alert_filter_advisory"
                        )
                    }

                    if (filteredAlerts.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF1E293B),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (isOdia)
                                    "ବର୍ତ୍ତମାନ କୌଣସି ଜରୁରୀ ପାଣିପାଗ ଚେତାବନୀ ନାହିଁ। ଉପକୂଳ ପାଗ ସ୍ୱାଭାବିକ ଅଛି।"
                                else
                                    "No severe weather alerts matching this filter for ${selectedNode.nameEn}.",
                                fontSize = 12.sp,
                                color = Color(0xFFCBD5E1),
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    } else {
                        filteredAlerts.forEach { alert ->
                            val isExpanded = expandedAlertId == alert.id
                            val borderAccent = when (alert.severity) {
                                AlertSeverity.CYCLONE_ALERT, AlertSeverity.WARNING -> Color(0xFFEF4444)
                                AlertSeverity.HIGH_TIDE -> Color(0xFF38BDF8)
                                else -> Color(0xFFF59E0B)
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color(0xFF1E293B),
                                border = BorderStroke(1.dp, borderAccent.copy(alpha = 0.65f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        BalasoreHapticHelper.vibrateButtonClick(context)
                                        expandedAlertId = if (isExpanded) null else alert.id
                                    }
                                    .testTag("local_weather_dashboard_alert_item_${alert.id}")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Text(text = alert.category.emoji, fontSize = 18.sp)
                                            Column {
                                                Text(
                                                    text = if (isOdia) alert.odiaTitle else alert.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    color = Color.White
                                                )
                                                Text(
                                                    text = "${alert.severity.levelName} • ${alert.issuingAuthority}",
                                                    fontSize = 10.5.sp,
                                                    color = borderAccent,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                        Icon(
                                            imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                            contentDescription = if (isExpanded) "Collapse Alert" else "Expand Alert",
                                            tint = Color(0xFF94A3B8),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Text(
                                        text = alert.summary,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE2E8F0),
                                        lineHeight = 16.sp
                                    )

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
                                                text = alert.detailedDescription,
                                                fontSize = 11.5.sp,
                                                color = Color(0xFF94A3B8),
                                                lineHeight = 16.sp
                                            )

                                            if (alert.actionableInstructions.isNotEmpty()) {
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
                                                            text = if (isOdia) "🛡️ ସୁରକ୍ଷା ପଦକ୍ଷେପ:" else "🛡️ Recommended Safety Actions:",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = Color(0xFFFBBF24)
                                                        )
                                                        alert.actionableInstructions.forEach { step ->
                                                            Text(
                                                                text = "• $step",
                                                                fontSize = 11.sp,
                                                                color = Color(0xFFF1F5F9)
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                OutlinedButton(
                                                    onClick = { ShareHelper.shareWeatherAlert(context, alert) },
                                                    border = BorderStroke(1.dp, Color(0xFF64748B)),
                                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(if (isOdia) "ସେୟାର୍" else "Share Alert", fontSize = 11.sp)
                                                }

                                                Button(
                                                    onClick = {
                                                        TideAndWeatherNotificationManager.showLocationWeatherAlertNotification(context, alert)
                                                        Toast.makeText(
                                                            context,
                                                            if (isOdia) "ସତର୍କତା ବିଜ୍ଞପ୍ତି ପଠାଗଲା" else "Weather alert notification sent",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                                    shape = RoundedCornerShape(8.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.weight(1f)
                                                ) {
                                                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(14.dp))
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(if (isOdia) "ଆଲର୍ଟ ବିଜ୍ଞପ୍ତି" else "Push Alert", fontSize = 11.sp)
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

            // ================= 5. 5-DAY OPENWEATHERMAP FORECAST SECTION =================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("local_weather_dashboard_5day_forecast_section"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "5-Day Forecast",
                            tint = OceanBlue,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = if (isOdia) "୫-ଦିନିଆ ପାଣିପାଗ ପୂର୍ବାନୁମାନ (OpenWeatherMap)" else "5-Day OpenWeatherMap Forecast",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isOdia)
                                    "ଦିନ ଚୟନ କରି ୩-ଘଣ୍ଟିଆ ତାପମାତ୍ରା ଓ ବର୍ଷା ସମ୍ଭାବନା ଦେଖନ୍ତୁ"
                                else
                                    "Tap any day to inspect 3-hour coastal forecast intervals",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (onOpenFullForecast != null) {
                        TextButton(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenFullForecast()
                            },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.testTag("local_weather_dashboard_open_full_btn")
                        ) {
                            Text(
                                text = if (isOdia) "ସମ୍ପୂର୍ଣ୍ଣ ଦେଖନ୍ତୁ" else "Full Radar",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // 5-Day Forecast Cards Strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    fiveDayForecast.take(5).forEachIndexed { index, day ->
                        val isSelected = selectedDayIndex == index
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) OceanBlue.copy(alpha = 0.12f) else BentoSlate50,
                            border = BorderStroke(
                                width = if (isSelected) 1.6.dp else 1.dp,
                                color = if (isSelected) OceanBlue else BentoBorder
                            ),
                            modifier = Modifier
                                .width(116.dp)
                                .clickable {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    selectedDayIndex = index
                                }
                                .testTag("local_weather_dashboard_5day_item_$index")
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = if (isOdia) day.odiaDayName else day.dayName,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) OceanBlueDark else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = day.dateLabel,
                                    fontSize = 10.sp,
                                    color = BentoSlate500,
                                    fontWeight = FontWeight.Medium
                                )

                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data("https://openweathermap.org/img/wn/${day.iconCode}@2x.png")
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = day.conditionDesc,
                                    modifier = Modifier.size(40.dp)
                                )

                                Text(
                                    text = if (isOdia) translateConditionToOdia(day.conditionDesc) else day.conditionMain,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = formatTemperature(day.tempMax),
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = CoralOrange
                                    )
                                    Text(
                                        text = "/",
                                        fontSize = 10.sp,
                                        color = BentoSlate400
                                    )
                                    Text(
                                        text = formatTemperature(day.tempMin),
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = BentoSlate600
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = OceanBlue.copy(alpha = 0.12f)
                                ) {
                                    Text(
                                        text = "☔ ${day.popPercent}% • 💧 ${day.avgHumidity}%",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OceanBlueDark,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Selected Day 3-Hour Interval Breakdown
                val selectedDay = fiveDayForecast.getOrNull(selectedDayIndex) ?: fiveDayForecast.firstOrNull()
                if (selectedDay != null && selectedDay.hourlySlots.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = BentoSlate50,
                        border = BorderStroke(1.dp, BentoBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("local_weather_dashboard_hourly_slots_row")
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (isOdia)
                                        "⏱️ ${selectedDay.odiaDayName} (${selectedDay.dateLabel}) • ୩-ଘଣ୍ଟିଆ ପୂର୍ବାନୁମାନ"
                                    else
                                        "⏱️ ${selectedDay.dayName} (${selectedDay.dateLabel}) • 3-Hour Forecast Slots",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlueDark
                                )
                                Text(
                                    text = if (isOdia)
                                        "ପବନ ${selectedDay.windSpeedKmh.roundToInt()} km/h"
                                    else
                                        "Wind ${selectedDay.windSpeedKmh.roundToInt()} km/h",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoSlate600
                                )
                            }

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                items(selectedDay.hourlySlots.take(6)) { slot ->
                                    val slotTime = slot.dtTxt?.substringAfter(" ")?.take(5) ?: "12:00"
                                    val slotPop = ((slot.pop ?: 0.15) * 100).roundToInt()
                                    val slotIcon = slot.weather.firstOrNull()?.icon ?: selectedDay.iconCode
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = MaterialTheme.colorScheme.surface,
                                        border = BorderStroke(1.dp, BentoBorder),
                                        modifier = Modifier.width(68.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                        ) {
                                            Text(
                                                text = slotTime,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = BentoSlate600
                                            )
                                            AsyncImage(
                                                model = ImageRequest.Builder(context)
                                                    .data("https://openweathermap.org/img/wn/$slotIcon.png")
                                                    .crossfade(true)
                                                    .build(),
                                                contentDescription = null,
                                                modifier = Modifier.size(26.dp)
                                            )
                                            Text(
                                                text = formatTemperature(slot.main?.temp ?: selectedDay.tempMax),
                                                fontSize = 11.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = BentoSlate900
                                            )
                                            Text(
                                                text = "💧 $slotPop%",
                                                fontSize = 9.5.sp,
                                                color = OceanBlue
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
private fun DashboardAtmosphericPill(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White.copy(alpha = 0.12f),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFBAE6FD),
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = value,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 9.5.sp,
                color = Color.White.copy(alpha = 0.75f),
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AlertSeverityChip(
    label: String,
    selected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) selectedColor else Color(0xFF1E293B),
        border = BorderStroke(
            1.dp,
            if (selected) Color.White.copy(alpha = 0.4f) else Color(0xFF334155)
        ),
        modifier = Modifier.testTag(testTag)
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
