package com.example.ui.components

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.BuildConfig
import com.example.data.model.AppLanguage
import com.example.data.remote.OpenWeatherCondition
import com.example.data.remote.OpenWeatherCurrentResponse
import com.example.data.remote.OpenWeatherForecastItem
import com.example.data.remote.OpenWeatherThreeDayForecast
import com.example.data.repository.OpenWeatherRepository
import com.example.ui.theme.*
import com.example.util.BalasoreHapticHelper
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * LocalWeather Composable:
 * Fetches and displays real-time weather information for Balasore, Odisha
 * using the OpenWeatherMap API (or resilient local cache/fallback),
 * including current temperature, conditions, atmospheric telemetry, and a 3-day forecast.
 */
@Composable
fun LocalWeather(
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier,
    onOpenFullForecast: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA
    val repository = remember { OpenWeatherRepository(context) }

    // API Key from BuildConfig / .env
    val configuredApiKey = remember { BuildConfig.OPENWEATHERMAP_API_KEY.trim() }
    val isLiveApi = remember(configuredApiKey) {
        repository.isApiKeyConfigured(configuredApiKey)
    }

    // Weather States
    var currentWeather by remember { mutableStateOf<OpenWeatherCurrentResponse?>(null) }
    var threeDayForecast by remember { mutableStateOf<List<OpenWeatherThreeDayForecast>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isFahrenheit by remember { mutableStateOf(false) }
    var expandedDayIndex by remember { mutableStateOf<Int?>(null) }
    var lastUpdatedTime by remember { mutableStateOf<String>("") }

    // Refresh animation rotation angle
    var rotationAngle by remember { mutableFloatStateOf(0f) }
    val animatedRotation by animateFloatAsState(
        targetValue = rotationAngle,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "weather_refresh_rotation"
    )

    // Data loader function
    val loadWeatherData: (Boolean) -> Unit = { isManualRefresh ->
        if (isManualRefresh) {
            rotationAngle += 360f
            BalasoreHapticHelper.vibrateButtonClick(context)
        }
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val weatherRes = repository.fetchCurrentWeather(configuredApiKey)
                val forecastRes = repository.fetchThreeDayForecast(configuredApiKey)

                currentWeather = weatherRes.getOrElse { repository.getCuratedBalasoreWeatherFallback() }
                threeDayForecast = forecastRes.getOrElse { repository.getCuratedBalasoreThreeDayForecastFallback() }

                val timeFormatter = SimpleDateFormat("hh:mm a", Locale.getDefault())
                lastUpdatedTime = timeFormatter.format(Date())

                if (isManualRefresh) {
                    BalasoreHapticHelper.vibrateSuccess(context)
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage
                currentWeather = repository.getCuratedBalasoreWeatherFallback()
                threeDayForecast = repository.getCuratedBalasoreThreeDayForecastFallback()
            } finally {
                isLoading = false
            }
        }
    }

    // Initial load
    LaunchedEffect(Unit) {
        loadWeatherData(false)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("local_weather_container"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize()
        ) {
            // 1. Header Row: Location Title & Action Controls
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(OceanBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = OceanBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ବାଲେଶ୍ୱର ପାଣିପାଗ" else "Balasore Local Weather",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOdia) "ଚାନ୍ଦିପୁର ଉପକୂଳ • ୨୧.୪୯°N, ୮୬.୯୧°E" else "Chandipur Coast • 21.49°N, 86.91°E",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Controls: °C/°F unit toggle & Refresh button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Unit Toggle Pill
                    Surface(
                        onClick = {
                            BalasoreHapticHelper.vibrateToggle(context)
                            isFahrenheit = !isFahrenheit
                        },
                        shape = RoundedCornerShape(12.dp),
                        color = OceanBlue.copy(alpha = 0.1f),
                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .testTag("local_weather_unit_toggle")
                            .height(36.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp)
                        ) {
                            Text(
                                text = if (isFahrenheit) "°F" else "°C",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.SwapHoriz,
                                contentDescription = "Toggle Temperature Unit",
                                tint = OceanBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Refresh Button with animated rotation
                    IconButton(
                        onClick = { loadWeatherData(true) },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("local_weather_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Weather",
                            tint = OceanBlue,
                            modifier = Modifier
                                .size(22.dp)
                                .rotate(animatedRotation)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Telemetry Status Badge
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isLiveApi) EmeraldGreen.copy(alpha = 0.1f)
                        else BentoBlueLight
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (isLiveApi) EmeraldGreen else OceanBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isLiveApi) {
                            if (isOdia) "ଓପନହ୍ୱେଦରମ୍ୟାପ୍ ଲାଇଭ୍ ଟେଲିମେଟ୍ରି" else "OpenWeatherMap Live Telemetry"
                        } else {
                            if (isOdia) "ବାଲେଶ୍ୱର ପାଣିପାଗ କ୍ୟାସ୍ ସୂଚନା" else "Balasore Coastal Weather Cache"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isLiveApi) EmeraldGreen else BentoBlueText
                    )
                }

                if (lastUpdatedTime.isNotBlank()) {
                    Text(
                        text = if (isOdia) "ଅଦ୍ୟତନ: $lastUpdatedTime" else "Updated: $lastUpdatedTime",
                        style = MaterialTheme.typography.labelSmall,
                        color = BentoSlate600
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Current Weather Hero Card
            val current = currentWeather
            if (current != null) {
                CurrentWeatherHeroSection(
                    current = current,
                    isFahrenheit = isFahrenheit,
                    isOdia = isOdia,
                    context = context
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Atmospheric Metrics Grid
                AtmosphericMetricsRow(
                    current = current,
                    isOdia = isOdia
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(32.dp),
                        color = OceanBlue
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. 3-Day Forecast Section
            Text(
                text = if (isOdia) "୩-ଦିନିଆ ପାଣିପାଗ ପୂର୍ବାନୁମାନ" else "3-Day Coastal Forecast",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("local_weather_3day_forecast")
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (threeDayForecast.isNotEmpty()) {
                threeDayForecast.take(3).forEachIndexed { index, forecastDay ->
                    ThreeDayForecastItemCard(
                        forecast = forecastDay,
                        isExpanded = expandedDayIndex == index,
                        onToggleExpand = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            expandedDayIndex = if (expandedDayIndex == index) null else index
                        },
                        isFahrenheit = isFahrenheit,
                        isOdia = isOdia,
                        context = context
                    )
                    if (index < 2) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // 6. Footer Button (if full forecast handler is provided)
            if (onOpenFullForecast != null) {
                Spacer(modifier = Modifier.height(14.dp))
                OutlinedButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        onOpenFullForecast()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("local_weather_full_radar_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, OceanBlue)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = if (isOdia) "ସମ୍ପୂର୍ଣ୍ଣ ରାଡାର ଓ ବିସ୍ତୃତ ପୂର୍ବାନୁମାନ ଦେଖନ୍ତୁ" else "View Radar & Detailed Trends",
                            fontWeight = FontWeight.SemiBold,
                            color = OceanBlue,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Current Weather Hero Section displaying temperature, condition text, and icon
 */
@Composable
private fun CurrentWeatherHeroSection(
    current: OpenWeatherCurrentResponse,
    isFahrenheit: Boolean,
    isOdia: Boolean,
    context: Context
) {
    val tempC = current.main?.temp ?: 29.5
    val feelsC = current.main?.feelsLike ?: 32.0
    val tempMinC = current.main?.tempMin ?: 25.0
    val tempMaxC = current.main?.tempMax ?: 32.5
    val condition = current.weather.firstOrNull()
    val conditionMain = condition?.main ?: "Clouds"
    val conditionDesc = condition?.description ?: "scattered clouds"
    val iconCode = condition?.icon ?: "02d"

    val displayTemp = formatWeatherTemp(tempC, isFahrenheit)
    val displayFeels = formatWeatherTemp(feelsC, isFahrenheit)
    val displayMin = formatWeatherTemp(tempMinC, isFahrenheit)
    val displayMax = formatWeatherTemp(tempMaxC, isFahrenheit)

    val localizedCondition = if (isOdia) {
        translateConditionToOdia(conditionDesc)
    } else {
        conditionDesc.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        OceanBlue.copy(alpha = 0.15f),
                        CoastalSkyBlue.copy(alpha = 0.08f)
                    )
                )
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Temperature & Condition Text
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = displayTemp,
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag("local_weather_temp")
                )

                Text(
                    text = localizedCondition,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = OceanBlueDark,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.testTag("local_weather_condition")
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isOdia) "ଅନୁଭୂତ: $displayFeels" else "Feels like: $displayFeels",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.testTag("local_weather_feels_like")
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "•  H: $displayMax  L: $displayMin",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = BentoSlate600
                    )
                }
            }

            // Weather Icon Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data("https://openweathermap.org/img/wn/$iconCode@2x.png")
                        .crossfade(true)
                        .build(),
                    contentDescription = conditionDesc,
                    modifier = Modifier.size(68.dp)
                )
            }
        }
    }
}

/**
 * Atmospheric Metrics Row: Humidity, Wind, Pressure, Visibility
 */
@Composable
private fun AtmosphericMetricsRow(
    current: OpenWeatherCurrentResponse,
    isOdia: Boolean
) {
    val humidity = current.main?.humidity ?: 74
    val windSpeed = current.wind?.speed ?: 4.6
    val windKmh = (windSpeed * 3.6).roundToInt()
    val pressure = current.main?.pressure ?: 1008
    val visibilityKm = ((current.visibility ?: 10000) / 1000.0).roundToInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WeatherMetricCard(
            icon = Icons.Default.WaterDrop,
            label = if (isOdia) "ଆର୍ଦ୍ରତା" else "Humidity",
            value = "$humidity%",
            tint = OceanBlue,
            modifier = Modifier
                .weight(1f)
                .testTag("local_weather_humidity")
        )
        WeatherMetricCard(
            icon = Icons.Default.Air,
            label = if (isOdia) "ପବନ" else "Wind",
            value = "$windKmh km/h",
            tint = CoastalSkyBlue,
            modifier = Modifier
                .weight(1f)
                .testTag("local_weather_wind")
        )
        WeatherMetricCard(
            icon = Icons.Default.Speed,
            label = if (isOdia) "ଚାପ" else "Pressure",
            value = "$pressure hPa",
            tint = AmberGold,
            modifier = Modifier
                .weight(1f)
                .testTag("local_weather_pressure")
        )
        WeatherMetricCard(
            icon = Icons.Default.Visibility,
            label = if (isOdia) "ଦୃଶ୍ୟମାନ" else "Visibility",
            value = "$visibilityKm km",
            tint = EmeraldGreen,
            modifier = Modifier
                .weight(1f)
                .testTag("local_weather_visibility")
        )
    }
}

@Composable
private fun WeatherMetricCard(
    icon: ImageVector,
    label: String,
    value: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = BentoSlate50,
        border = BorderStroke(1.dp, BentoBorder),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = BentoSlate900,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = BentoSlate500,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 3-Day Forecast Item with expandable 3-hour slots
 */
@Composable
private fun ThreeDayForecastItemCard(
    forecast: OpenWeatherThreeDayForecast,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    isFahrenheit: Boolean,
    isOdia: Boolean,
    context: Context
) {
    val dayTitle = if (isOdia) forecast.odiaDayName else forecast.dayName
    val maxTemp = formatWeatherTemp(forecast.tempMax, isFahrenheit)
    val minTemp = formatWeatherTemp(forecast.tempMin, isFahrenheit)
    val conditionLabel = if (isOdia) translateConditionToOdia(forecast.conditionDesc) else forecast.conditionMain

    Surface(
        onClick = onToggleExpand,
        shape = RoundedCornerShape(16.dp),
        color = BentoSlate50,
        border = BorderStroke(1.dp, if (isExpanded) OceanBlue.copy(alpha = 0.5f) else BentoBorder),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forecast_day_${forecast.dayIndex}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Day label & date
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.2f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(OceanBlue.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${forecast.dayIndex + 1}",
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue,
                            fontSize = 14.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = dayTitle,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = forecast.dateLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSlate500
                        )
                    }
                }

                // Weather Icon & Condition
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1.3f),
                    horizontalArrangement = Arrangement.Center
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data("https://openweathermap.org/img/wn/${forecast.iconCode}.png")
                            .crossfade(true)
                            .build(),
                        contentDescription = forecast.conditionDesc,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = conditionLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (forecast.popPercent > 0) {
                            Text(
                                text = "☔ ${forecast.popPercent}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = OceanBlue
                            )
                        }
                    }
                }

                // High / Low temp & Expand icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.weight(1f)
                ) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = maxTemp,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = minTemp,
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSlate500
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = BentoSlate500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Expandable Hourly breakdown
            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    HorizontalDivider(color = BentoBorder, thickness = 0.5.dp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isOdia) "ଘଣ୍ଟିକା ପୂର୍ବାନୁମାନ:" else "3-Hour Telemetry Slots:",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate600
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (forecast.hourlySlots.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(forecast.hourlySlots) { slot ->
                                HourlySlotPill(
                                    slot = slot,
                                    isFahrenheit = isFahrenheit,
                                    context = context
                                )
                            }
                        }
                    } else {
                        Text(
                            text = if (isOdia) "ଆନୁମାନିକ ହାରାହାରି ଆର୍ଦ୍ରତା ${forecast.avgHumidity}% • ପବନ ${forecast.windSpeedKmh.roundToInt()} km/h"
                            else "Avg Humidity ${forecast.avgHumidity}% • Wind Speed ${forecast.windSpeedKmh.roundToInt()} km/h",
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSlate600
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HourlySlotPill(
    slot: OpenWeatherForecastItem,
    isFahrenheit: Boolean,
    context: Context
) {
    val timeLabel = slot.dtTxt?.substringAfter(" ")?.take(5) ?: "--:--"
    val tempC = slot.main?.temp ?: 28.0
    val formattedTemp = formatWeatherTemp(tempC, isFahrenheit)
    val icon = slot.weather.firstOrNull()?.icon ?: "02d"

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, BentoBorder),
        modifier = Modifier.width(64.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = timeLabel,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                color = BentoSlate600
            )
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data("https://openweathermap.org/img/wn/$icon.png")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = formattedTemp,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = BentoSlate900
            )
        }
    }
}

/**
 * Helper to convert temperature to °C or °F formatted string
 */
fun formatWeatherTemp(celsius: Double?, isFahrenheit: Boolean): String {
    val temp = celsius ?: 28.0
    return if (isFahrenheit) {
        val f = (temp * 9.0 / 5.0) + 32.0
        "${f.roundToInt()}°F"
    } else {
        "${temp.roundToInt()}°C"
    }
}

/**
 * Odia translation for OpenWeatherMap condition descriptions
 */
fun translateConditionToOdia(descEn: String?): String {
    if (descEn == null) return "ନିର୍ମଳ ପାଣିପାଗ"
    val lower = descEn.lowercase()
    return when {
        lower.contains("thunderstorm") -> "ଝଡ଼ବର୍ଷା ସହିତ ବଜ୍ରପାତ"
        lower.contains("heavy rain") -> "ପ୍ରବଳ ଉପକୂଳ ବର୍ଷା"
        lower.contains("moderate rain") -> "ମଧ୍ୟମ ବର୍ଷା"
        lower.contains("light rain") || lower.contains("drizzle") -> "ହାଲୁକା ବୃଷ୍ଟିପାତ"
        lower.contains("rain") || lower.contains("shower") -> "ଉପକୂଳ ବୃଷ୍ଟିପାତ"
        lower.contains("clear") -> "ନିର୍ମଳ ଆକାଶ ଓ ସୂର୍ଯ୍ୟୋଦୟ"
        lower.contains("few clouds") -> "ସ୍ୱଳ୍ପ ମେଘାଚ୍ଛନ୍ନ"
        lower.contains("scattered clouds") -> "ଛିନ୍ନଛତ୍ର ମେଘ"
        lower.contains("broken clouds") || lower.contains("overcast") -> "ଘନ ମେଘାଚ୍ଛନ୍ନ ଆକାଶ"
        lower.contains("mist") || lower.contains("fog") || lower.contains("haze") -> "କୁହୁଡ଼ିଆ ସାମୁଦ୍ରିକ ପାଗ"
        else -> descEn.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }
}

/**
 * Backward compatibility alias for LocalWeather composable
 */
@Composable
fun LocalWeatherComposable(
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier,
    onOpenFullForecast: (() -> Unit)? = null
) {
    LocalWeather(
        language = language,
        modifier = modifier,
        onOpenFullForecast = onOpenFullForecast
    )
}
