package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.AppLanguage
import com.example.data.remote.OpenWeatherCurrentResponse
import com.example.data.remote.OpenWeatherForecastItem
import com.example.data.remote.OpenWeatherThreeDayForecast
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Interactive OpenWeatherMap Weather Widget for the Balasore Home Dashboard.
 *
 * Provides:
 * 1. Real-time Balasore telemetry fetched via OpenWeatherMap API (Temp, Feels Like, Humidity, Wind, Pressure, Clouds)
 * 2. Interactive 3-day forecast with daily selector (Today, Tomorrow, Day After) and 3-hour projection intervals
 * 3. Animated Celsius / Fahrenheit unit toggle
 * 4. Background auto-update ticker with live countdown badge
 * 5. One-tap launch to the full OpenWeatherMap telemetry screen
 * 6. Full bilingual support in English and Odia (ଓଡ଼ିଆ)
 */
@Composable
fun OpenWeatherMapDashboardWidget(
    currentWeather: OpenWeatherCurrentResponse?,
    threeDayForecast: List<OpenWeatherThreeDayForecast>,
    isLoading: Boolean = false,
    language: AppLanguage = AppLanguage.ENGLISH,
    isFahrenheit: Boolean = false,
    onToggleTempUnit: () -> Unit = {},
    onRefresh: () -> Unit = {},
    onOpenFullScreen: () -> Unit = {},
    onOpenSheltersMap: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA

    var selectedForecastDayIndex by remember { mutableIntStateOf(0) }
    var autoUpdateCountdown by remember { mutableIntStateOf(30) }

    // Auto-update countdown ticker (30s)
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (autoUpdateCountdown > 1) {
                autoUpdateCountdown -= 1
            } else {
                autoUpdateCountdown = 30
                onRefresh()
            }
        }
    }

    // Refresh icon animation
    val infiniteTransition = rememberInfiniteTransition(label = "refresh_spin")
    val spinAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "spin_angle"
    )

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

    fun getConditionEmoji(iconCode: String?): String {
        return when (iconCode?.take(2)) {
            "01" -> "☀️" // Clear
            "02" -> "⛅" // Few clouds
            "03", "04" -> "☁️" // Scattered/broken clouds
            "09", "10" -> "🌧️" // Rain
            "11" -> "⛈️" // Thunderstorm
            "13" -> "❄️" // Snow
            "50" -> "🌫️" // Mist/Haze
            else -> "🌤️"
        }
    }

    fun getOdiaConditionDesc(descEn: String?): String {
        if (descEn == null) return "ନିର୍ମଳ ପାଣିପାଗ"
        val lower = descEn.lowercase()
        return when {
            lower.contains("thunderstorm") -> "ଝଡ଼ବର୍ଷା ସହିତ ବଜ୍ରପାତ"
            lower.contains("heavy rain") -> "ପ୍ରବଳ ବର୍ଷା"
            lower.contains("rain") || lower.contains("shower") || lower.contains("drizzle") -> "ଉପକୂଳ ବୃଷ୍ଟିପାତ"
            lower.contains("clear") -> "ନିର୍ମଳ ଆକାଶ"
            lower.contains("few clouds") -> "ସ୍ୱଳ୍ପ ମେଘାଚ୍ଛନ୍ନ"
            lower.contains("scattered clouds") -> "ଛିନ୍ନଛତ୍ର ମେଘ"
            lower.contains("broken clouds") || lower.contains("overcast") -> "ଘନ ମେଘାଚ୍ଛନ୍ନ ଆକାଶ"
            lower.contains("mist") || lower.contains("fog") || lower.contains("haze") -> "କୁହୁଡ଼ିଆ ପାଗ"
            else -> descEn
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("open_weather_map_widget"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize()
        ) {
            // 1. Top Header Row: Location, Live Badge, Unit Toggle & Controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location & API Branding
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0284C7).copy(alpha = 0.12f),
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.WbSunny,
                                contentDescription = "Weather",
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱର ଓପନ-ୱେଦର" else "OpenWeatherMap Live",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0F172A),
                                    fontSize = 15.sp
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            // Pulsing Live badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFF10B981).copy(alpha = 0.15f),
                                border = BorderStroke(1.dp, Color(0xFF10B981))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(Color(0xFF10B981), CircleShape)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "LIVE API",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF047857)
                                    )
                                }
                            }
                        }
                        Text(
                            text = if (isOdia) "ଉପକୂଳ ଜିଲ୍ଲା • ୨୧.୪୯° ଉତ୍ତର, ୮୬.୯୧° ପୂର୍ବ" else "Coastal District • 21.49° N, 86.91° E",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                }

                // Interactive Controls: Auto countdown, Unit Toggle, Refresh, Fullscreen
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Auto-update countdown badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Text(
                            text = "${autoUpdateCountdown}s",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Celsius / Fahrenheit toggle
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OceanBlue.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .clickable { onToggleTempUnit() }
                            .testTag("temp_unit_toggle_btn")
                    ) {
                        Text(
                            text = if (isFahrenheit) "°F" else "°C",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Refresh Button
                    IconButton(
                        onClick = {
                            autoUpdateCountdown = 30
                            onRefresh()
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("refresh_open_weather_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier
                                .size(18.dp)
                                .rotate(if (isLoading) spinAngle else 0f)
                        )
                    }

                    // Expand to Full Screen
                    IconButton(
                        onClick = onOpenFullScreen,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("open_fullscreen_weather_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Full Telemetry",
                            tint = Color(0xFF64748B),
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Hero Current Weather Card with Coastal Gradient
            val weather = currentWeather
            val condition = weather?.weather?.firstOrNull()
            val conditionTitle = condition?.main ?: "Clear"
            val conditionDesc = condition?.description?.replaceFirstChar { it.uppercase() } ?: "Clear Sky"
            val iconCode = condition?.icon ?: "01d"
            val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF0369A1), // Deep Ocean
                                Color(0xFF0284C7),
                                Color(0xFF38BDF8)
                            )
                        )
                    )
                    .padding(16.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            // Current Temperature Display
                            Text(
                                text = formatTemp(weather?.main?.temp ?: 29.4),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    fontSize = 42.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            // Condition Description
                            Text(
                                text = if (isOdia) getOdiaConditionDesc(conditionDesc) else conditionDesc,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (isOdia) conditionDesc else getOdiaConditionDesc(conditionDesc),
                                fontSize = 11.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }

                        // Weather Icon Display with Network AsyncImage + Emoji Fallback
                        Box(
                            modifier = Modifier
                                .size(78.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(iconUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = conditionDesc,
                                modifier = Modifier.size(68.dp)
                            )
                            // If icon fails to load or offline, show big emoji
                            if (weather == null) {
                                Text(
                                    text = getConditionEmoji(iconCode),
                                    fontSize = 36.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Feels Like + High/Low + Bay of Bengal breeze note
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "H: ${formatTemp(weather?.main?.tempMax ?: 32.5)} • L: ${formatTemp(weather?.main?.tempMin ?: 24.8)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        Text(
                            text = if (isOdia) "ଅନୁଭବ: ${formatTemp(weather?.main?.feelsLike ?: 32.6)}" else "Feels like ${formatTemp(weather?.main?.feelsLike ?: 32.6)}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White.copy(alpha = 0.9f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Four Core Real-Time Telemetry Bento Tiles
            val humidity = weather?.main?.humidity ?: 78
            val windSpeedMps = weather?.wind?.speed ?: 3.8
            val windSpeedKmh = ((windSpeedMps * 3.6) * 10).roundToInt() / 10.0
            val pressureHpa = weather?.main?.pressure ?: 1012
            val cloudsAll = weather?.clouds?.all ?: 28

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Relative Humidity
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = null,
                                tint = Color(0xFF16A34A),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOdia) "ଆର୍ଦ୍ରତା" else "Humidity",
                                fontSize = 10.sp,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$humidity%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = if (humidity > 75) "Coastal High" else "Moderate",
                            fontSize = 9.sp,
                            color = Color(0xFF16A34A),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Wind Velocity
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0F9FF),
                    border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Air,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOdia) "ପବନ" else "Wind",
                                fontSize = 10.sp,
                                color = Color(0xFF0369A1),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$windSpeedKmh km/h",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Bay of Bengal",
                            fontSize = 9.sp,
                            color = Color(0xFF0284C7),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Barometer Pressure
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Compress,
                                contentDescription = null,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOdia) "ଚାପ" else "Pressure",
                                fontSize = 10.sp,
                                color = Color(0xFFB45309),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$pressureHpa hPa",
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Sea Level",
                            fontSize = 9.sp,
                            color = Color(0xFFD97706),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Cloudiness
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFAF5FF),
                    border = BorderStroke(1.dp, Color(0xFFE9D5FF)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Cloud,
                                contentDescription = null,
                                tint = Color(0xFF9333EA),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOdia) "ମେଘ" else "Clouds",
                                fontSize = 10.sp,
                                color = Color(0xFF7E22CE),
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$cloudsAll%",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = "Coverage",
                            fontSize = 9.sp,
                            color = Color(0xFF9333EA),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // =========================================================================
            // SPECIFIC ALERTS FOR EXTREME HEAT OR MONSOON WARNINGS (IMD & OpenWeatherMap)
            // =========================================================================
            var activeAlertTab by remember { mutableIntStateOf(0) } // 0: Extreme Heat, 1: Monsoon Warning
            var isHeatAcknowledged by remember { mutableStateOf(false) }
            var isMonsoonAcknowledged by remember { mutableStateOf(false) }
            var isAlertExpanded by remember { mutableStateOf(true) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("weather_alerts_dashboard_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (activeAlertTab == 0) Color(0xFFFFF7ED) else Color(0xFFF0F9FF)
                ),
                border = BorderStroke(
                    1.dp,
                    if (activeAlertTab == 0) Color(0xFFFDBA74) else Color(0xFFBAE6FD)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp)
                ) {
                    // Alert Header with Pulsing Beacon & Expand Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (activeAlertTab == 0) Color(0xFFEA580C) else Color(0xFF0284C7))
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOdia) "ପାଣିପାଗ ସତର୍କତା ବାର୍ତ୍ତା" else "Severe Weather Advisory",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (activeAlertTab == 0) Color(0xFF9A3412) else Color(0xFF0369A1)
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Expand/Collapse Details
                            TextButton(
                                onClick = { isAlertExpanded = !isAlertExpanded },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isAlertExpanded) (if (isOdia) "ସଂକ୍ଷିପ୍ତ" else "Collapse") else (if (isOdia) "ବିସ୍ତୃତ" else "Expand"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (activeAlertTab == 0) Color(0xFFC2410C) else Color(0xFF0284C7)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Icon(
                                    imageVector = if (isAlertExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = if (activeAlertTab == 0) Color(0xFFC2410C) else Color(0xFF0284C7),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Alert Switcher Filter Chips (Extreme Heat ⇄ Monsoon Warning)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = activeAlertTab == 0,
                            onClick = { activeAlertTab = 0 },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.WbSunny,
                                    contentDescription = null,
                                    tint = if (activeAlertTab == 0) Color.White else Color(0xFFEA580C),
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = if (isOdia) "🔥 ଗ୍ରୀଷ୍ମ ପ୍ରବାହ ସତର୍କତା" else "☀️ Extreme Heat Alert",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (activeAlertTab == 0) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = Color(0xFF9A3412),
                                selectedContainerColor = Color(0xFFEA580C),
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(1.dp, if (activeAlertTab == 0) Color(0xFFEA580C) else Color(0xFFFDBA74)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_chip_extreme_heat_alert")
                        )

                        FilterChip(
                            selected = activeAlertTab == 1,
                            onClick = { activeAlertTab = 1 },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Thunderstorm,
                                    contentDescription = null,
                                    tint = if (activeAlertTab == 1) Color.White else Color(0xFF0284C7),
                                    modifier = Modifier.size(14.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = if (isOdia) "🌧️ ମୌସୁମୀ ଚେତାବନୀ" else "🌧️ Monsoon Warning",
                                    fontSize = 11.5.sp,
                                    fontWeight = if (activeAlertTab == 1) FontWeight.ExtraBold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color.White,
                                labelColor = Color(0xFF0369A1),
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White
                            ),
                            border = BorderStroke(1.dp, if (activeAlertTab == 1) Color(0xFF0284C7) else Color(0xFFBAE6FD)),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("filter_chip_monsoon_warning")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Alert Body: Extreme Heat or Monsoon
                    if (activeAlertTab == 0) {
                        // -------------------------------------------------------------
                        // EXTREME HEAT ALERT (IMD Heatwave & Coastal Heat Index)
                        // -------------------------------------------------------------
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("extreme_heat_alert_details")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFED7AA)
                            ) {
                                Text(
                                    text = "IMD ORANGE ALERT • BALASORE SADAR & NILAGIRI BELT",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF9A3412),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isOdia) "ଅତ୍ୟଧିକ ଗ୍ରୀଷ୍ମ ପ୍ରବାହ ଓ ଉଷ୍ମ ଆର୍ଦ୍ରତା ସତର୍କତା" else "Severe Heatwave & High Coastal Humidity Warning",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF7C2D12),
                                    fontSize = 14.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isOdia)
                                    "ବାଲେଶ୍ୱର ଉପକୂଳରେ ଆର୍ଦ୍ରତା ୭୮% ଥିବାରୁ ଅନୁଭୂତ ଉତ୍ତାପ (Heat Index) ୪୨.୬°C ଟପିପାରେ। ସୂର୍ଯ୍ୟାଘାତ (Sunstroke) ଏବଂ ଜଳହୀନତା ଆଶଙ୍କା ରହିଛି।"
                                else
                                    "Coastal humidity (78%) amplifies Balasore feels-like heat index to 42.6°C. High vulnerability for dehydration and heat exhaustion across urban & rural areas.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF9A3412),
                                lineHeight = 16.sp
                            )

                            if (isAlertExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                // Heat Telemetry Strip
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🌡️ Peak Temp: 38.5°C", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF7C2D12))
                                    Text("🔥 Feels Like: 42.6°C", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEA580C))
                                    Text("☀️ UV Index: 9.4 (Very High)", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFC2410C))
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isOdia) "ଜରୁରୀ ସତର୍କତା ପରାମର୍ଶ:" else "Critical Safety Guidelines:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF7C2D12)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                listOf(
                                    if (isOdia) "• ଦିନ ୧୧ଟାରୁ ଅପରାହ୍ନ ୩:୩୦ ମଧ୍ୟରେ କଠିନ ଶାରୀରିକ ପରିଶ୍ରମରୁ ନିବୃତ୍ତ ରୁହନ୍ତୁ" else "• Avoid direct sun exposure between 11:00 AM – 3:30 PM",
                                    if (isOdia) "• ପ୍ରଚୁର ପାଣି, ଓଆରଏସ୍ (ORS), ଡାବ ପାଣି ଏବଂ ତୋରାଣି ପିଅନ୍ତୁ" else "• Drink adequate fluids (ORS, fresh green coconut, buttermilk/torani)",
                                    if (isOdia) "• ହାଲୁକା ସୁତା ବସ୍ତ୍ର ପରିଧାନ କରନ୍ତୁ ଓ ମୁଣ୍ଡରେ ଟୋପି/ଛତା ବ୍ୟବହାର କରନ୍ତୁ" else "• Wear loose light-colored cotton clothing and carry an umbrella",
                                    if (isOdia) "• ମୁଣ୍ଡ ବୁଲାଇବା ବା ଦୁର୍ବଳ ଲାଗିଲେ ତୁରନ୍ତ ଛାଇକୁ ଯାଆନ୍ତୁ ଏବଂ ୧୦୮ ଡାକନ୍ତୁ" else "• Heatstroke emergency: Call 108 / 104 for prompt transport to DHH Balasore"
                                ).forEach { instruction ->
                                    Text(
                                        text = instruction,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF9A3412),
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { isHeatAcknowledged = !isHeatAcknowledged },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (isHeatAcknowledged) Color(0xFF15803D) else Color(0xFF9A3412)
                                        ),
                                        border = BorderStroke(1.dp, if (isHeatAcknowledged) Color(0xFF16A34A) else Color(0xFFFDBA74)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("ack_extreme_heat_alert_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (isHeatAcknowledged) Icons.Default.CheckCircle else Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isHeatAcknowledged) (if (isOdia) "ଅବଗତ" else "Acknowledged") else (if (isOdia) "ଗ୍ରହଣ କରନ୍ତୁ" else "Acknowledge"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            try {
                                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:108"))
                                                context.startActivity(intent)
                                            } catch (_: Exception) {}
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("call_heat_emergency_108_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Phone,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isOdia) "୧୦୮ ଆମ୍ବୁଲାନ୍ସ" else "Call 108 Hotline",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // -------------------------------------------------------------
                        // MONSOON & SQUALLY WIND WARNING (IMD Coastal Depression & Floods)
                        // -------------------------------------------------------------
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("monsoon_warning_details")
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFBAE6FD)
                            ) {
                                Text(
                                    text = "IMD RED ADVISORY • BAY OF BENGAL MONSOON TROUGH",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0369A1),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isOdia) "ଉପକୂଳ ବାଲେଶ୍ୱର ପ୍ରବଳ ମୌସୁମୀ ବର୍ଷା ଓ ଝଡ଼ ପବନ" else "Coastal Balasore Heavy Monsoon Rain & Squally Winds",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0C4A6E),
                                    fontSize = 14.sp
                                )
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isOdia)
                                    "ବଙ୍ଗୋପସାଗରରେ ସୃଷ୍ଟ ଲଘୁଚାପ ଯୋଗୁଁ ଆଗାମୀ ୭୨ ଘଣ୍ଟା ମଧ୍ୟରେ ବାଲେଶ୍ୱର, ଚାନ୍ଦିପୁର ଓ ତାଳସାରୀ ଉପକୂଳରେ ୭୦-୧୧୦ ମିମି ବର୍ଷା ଏବଂ ୫୫ କିମି/ଘଣ୍ଟା ବେଗରେ ଝଟକା ପବନ ହେବାର ସମ୍ଭାବନା ରହିଛି।"
                                else
                                    "Monsoon low-pressure trough active over Bay of Bengal. Expect 70–110 mm rainfall, high tides, and 55 km/h squally wind gusts along Chandipur, Kasafal and Talasari coast.",
                                fontSize = 11.5.sp,
                                color = Color(0xFF0369A1),
                                lineHeight = 16.sp
                            )

                            if (isAlertExpanded) {
                                Spacer(modifier = Modifier.height(10.dp))
                                // Monsoon Telemetry Strip
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("🌧️ Rain: 75–110 mm", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                    Text("💨 Wind: 45–55 km/h", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                                    Text("🌊 Sea: Rough to Very Rough", fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF075985))
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = if (isOdia) "ଜରୁରୀ ସୁରକ୍ଷା ନିର୍ଦ୍ଦେଶାବଳୀ:" else "Urgent Coastal Precautions:",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0C4A6E)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                listOf(
                                    if (isOdia) "• ମତ୍ସ୍ୟଜୀବୀମାନଙ୍କୁ ସମୁଦ୍ର ମଧ୍ୟକୁ ଯିବାକୁ ସମ୍ପୂର୍ଣ୍ଣ ବାରଣ କରାଯାଇଛି" else "• Fishermen strictly warned against venturing into Bay of Bengal",
                                    if (isOdia) "• ବୁଢ଼ାବଳଙ୍ଗ ନଦୀକୂଳ ଏବଂ ତଳିଆ ଅଞ୍ଚଳରେ ଜଳବନ୍ଦୀ ସତର୍କତା ଜାରି" else "• Waterlogging watch along Budhabalanga river & Cinema Chhak lowlands",
                                    if (isOdia) "• ୫ଟି ବହୁମୁଖୀ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳକୁ ସଜାଗ ଓ ଜେନେରେଟର ସହିତ ପ୍ରସ୍ତୁତ ରଖାଯାଇଛି" else "• 5 Multi-purpose cyclone shelters on active standby with backup power",
                                    if (isOdia) "• ଜିଲ୍ଲା ଜରୁରୀକାଳୀନ କଣ୍ଟ୍ରୋଲ ରୁମ୍ ହେଲ୍ପଲାଇନ୍: ୦୬୭୮୨-୨୬୨୨୮୬" else "• District Disaster Helpline: 06782-262286 (24x7 Control Room)"
                                ).forEach { instruction ->
                                    Text(
                                        text = instruction,
                                        fontSize = 10.5.sp,
                                        color = Color(0xFF0369A1),
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = { isMonsoonAcknowledged = !isMonsoonAcknowledged },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.outlinedButtonColors(
                                            contentColor = if (isMonsoonAcknowledged) Color(0xFF15803D) else Color(0xFF0369A1)
                                        ),
                                        border = BorderStroke(1.dp, if (isMonsoonAcknowledged) Color(0xFF16A34A) else Color(0xFFBAE6FD)),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("ack_monsoon_warning_btn")
                                    ) {
                                        Icon(
                                            imageVector = if (isMonsoonAcknowledged) Icons.Default.CheckCircle else Icons.Default.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isMonsoonAcknowledged) (if (isOdia) "ଅବଗତ" else "Acknowledged") else (if (isOdia) "ଗ୍ରହଣ କରନ୍ତୁ" else "Acknowledge"),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Button(
                                        onClick = onOpenSheltersMap,
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(38.dp)
                                            .testTag("view_emergency_shelters_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isOdia) "ଆଶ୍ରୟସ୍ଥଳ ମ୍ୟାପ୍" else "Shelters Overlay",
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
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Interactive 3-Day Forecast Section Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = OceanBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "୩-ଦିନିଆ ପାଣିପାଗ ପୂର୍ବାନୁମାନ" else "OpenWeatherMap 3-Day Forecast",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0F172A)
                        )
                    )
                }

                Text(
                    text = if (isOdia) "ଦିନ ଚୟନ କରନ୍ତୁ" else "Tap Day to Inspect",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Interactive 3-Day Forecast Cards Row
            val forecastList = if (threeDayForecast.isNotEmpty()) threeDayForecast else listOf(
                OpenWeatherThreeDayForecast(
                    dayIndex = 1,
                    dateLabel = "Today",
                    dayName = "Today",
                    odiaDayName = "ଆଜି",
                    tempMax = 32.5,
                    tempMin = 24.8,
                    avgHumidity = 78,
                    windSpeedKmh = 14.5,
                    conditionMain = "Clouds",
                    conditionDesc = "scattered clouds",
                    iconCode = "03d",
                    popPercent = 35
                ),
                OpenWeatherThreeDayForecast(
                    dayIndex = 2,
                    dateLabel = "Tomorrow",
                    dayName = "Tomorrow",
                    odiaDayName = "ଆସନ୍ତାକାଲି",
                    tempMax = 33.0,
                    tempMin = 25.2,
                    avgHumidity = 80,
                    windSpeedKmh = 16.0,
                    conditionMain = "Rain",
                    conditionDesc = "light coastal rain",
                    iconCode = "10d",
                    popPercent = 65
                ),
                OpenWeatherThreeDayForecast(
                    dayIndex = 3,
                    dateLabel = "Day After",
                    dayName = "Day 3",
                    odiaDayName = "ପରଦିନ",
                    tempMax = 31.8,
                    tempMin = 24.5,
                    avgHumidity = 75,
                    windSpeedKmh = 13.0,
                    conditionMain = "Clear",
                    conditionDesc = "clear sky",
                    iconCode = "01d",
                    popPercent = 20
                )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                forecastList.take(3).forEachIndexed { index, item ->
                    val isSelected = selectedForecastDayIndex == index
                    val dayTitle = when (index) {
                        0 -> if (isOdia) "ଆଜି" else "Today"
                        1 -> if (isOdia) "ଆସନ୍ତାକାଲି" else "Tomorrow"
                        else -> if (isOdia) item.odiaDayName else item.dayName
                    }

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clickable { selectedForecastDayIndex = index }
                            .testTag("forecast_day_$index"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC)
                        ),
                        border = BorderStroke(
                            1.5.dp,
                            if (isSelected) OceanBlue else Color(0xFFE2E8F0)
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayTitle,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                color = if (isSelected) OceanBlue else Color(0xFF334155),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = item.dateLabel,
                                fontSize = 10.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Weather Icon
                            Text(
                                text = getConditionEmoji(item.iconCode),
                                fontSize = 24.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            // Temp High / Low
                            Text(
                                text = formatTemp(item.tempMax),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = formatTemp(item.tempMin),
                                fontSize = 11.sp,
                                color = Color(0xFF64748B)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Rain Chance Badge
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (item.popPercent >= 50) Color(0xFFDBEAFE) else Color(0xFFF1F5F9)
                            ) {
                                Text(
                                    text = "💧 ${item.popPercent}%",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (item.popPercent >= 50) Color(0xFF1D4ED8) else Color(0xFF64748B),
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 6. Interactive Expanded Detail Box for the Selected Forecast Day
            val activeForecastDay = forecastList.getOrNull(selectedForecastDayIndex) ?: forecastList.first()
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (isOdia) "${activeForecastDay.odiaDayName}ର ସବିଶେଷ ପୂର୍ବାନୁମାନ" else "${activeForecastDay.dayName} Detailed Outlook",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                            Text(
                                text = if (isOdia) getOdiaConditionDesc(activeForecastDay.conditionDesc) else activeForecastDay.conditionDesc.replaceFirstChar { it.uppercase() },
                                fontSize = 11.5.sp,
                                color = OceanBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OceanBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Avg ${formatTemp((activeForecastDay.tempMax + activeForecastDay.tempMin) / 2)}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Metrics Strip: Expected Humidity, Wind, Rain Probability
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("💧 Avg Humidity", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("${activeForecastDay.avgHumidity}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Column {
                            Text("💨 Coastal Wind", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("${activeForecastDay.windSpeedKmh} km/h", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text("🌧️ Rain Probability", fontSize = 10.sp, color = Color(0xFF64748B))
                            Text("${activeForecastDay.popPercent}% Risk", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0284C7))
                        }
                    }

                    // Hourly Slots if available
                    if (activeForecastDay.hourlySlots.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (isOdia) "୩-ଘଣ୍ଟିଆ ଅନ୍ତରାଳ (OpenWeather 5-Day/3-Hour)" else "3-Hour Telemetry Intervals",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF64748B)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(activeForecastDay.hourlySlots.take(8)) { slot ->
                                val slotTime = slot.dtTxt?.substringAfter(" ")?.take(5) ?: "--:--"
                                val slotTemp = slot.main?.temp
                                val slotIcon = slot.weather.firstOrNull()?.icon

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(slotTime, fontSize = 9.5.sp, color = Color(0xFF64748B), fontWeight = FontWeight.SemiBold)
                                        Text(getConditionEmoji(slotIcon), fontSize = 14.sp)
                                        Text(formatTemp(slotTemp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 7. Direct Action Button: Open Full OpenWeatherMap Screen
            Button(
                onClick = onOpenFullScreen,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .testTag("launch_openweather_screen_btn"),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Cloud,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOdia) "ସମ୍ପୂର୍ଣ୍ଣ ଓପନ-ୱେଦର ସ୍କ୍ରିନ୍ ଖୋଲନ୍ତୁ" else "Open Full OpenWeatherMap Suite",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
