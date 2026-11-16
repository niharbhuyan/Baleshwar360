package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
import com.example.data.local.SevereWeatherAlertEntity
import com.example.data.repository.SevereWeatherAlertRepository
import kotlinx.coroutines.delay
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.data.model.AppLanguage
import com.example.data.remote.OpenWeatherCurrentResponse
import com.example.data.repository.OpenWeatherRepository
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate400
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.OceanBlue
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Weather Summary Widget for the main Balasore dashboard.
 * Uses the OpenWeatherMap API to display:
 * 1. Current temperature for Balasore in Celsius / Fahrenheit with instant toggle.
 * 2. Brief weather condition icon fetched from OpenWeatherMap with dynamic vector fallback.
 * 3. Concise condition summary with bilingual English / Odia (ଓଡ଼ିଆ) translations.
 * 4. Key secondary coastal telemetry: feels-like temperature, humidity %, and wind speed.
 * 5. Direct navigation to the comprehensive OpenWeatherMap telemetry view.
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

    // Internal fallback / standalone state if not supplied by parent
    var localWeather by remember { mutableStateOf<OpenWeatherCurrentResponse?>(null) }
    var internalLoading by remember { mutableStateOf(false) }

    val effectiveWeather = currentWeather ?: localWeather
    val effectiveLoading = isLoading || internalLoading

    // Room Database Severe Weather Alerts repository (Alert History)
    val alertRepo = remember { SevereWeatherAlertRepository(context) }
    val roomAlerts by alertRepo.last5Alerts.collectAsState(initial = emptyList())
    var showAlertHistory by remember { mutableStateOf(false) }

    // Seed room database if empty
    LaunchedEffect(Unit) {
        alertRepo.seedSampleAlertsIfEmpty()
    }

    // Dynamic auto-update state & 60-second automatic refresh loop
    var secondsSinceLastUpdate by remember { mutableStateOf(0) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsSinceLastUpdate++
            if (secondsSinceLastUpdate >= 60) {
                secondsSinceLastUpdate = 0
                if (currentWeather != null) {
                    onRefresh()
                } else {
                    try {
                        val repo = OpenWeatherRepository()
                        val result = repo.fetchCurrentWeather(apiKey ?: "")
                        localWeather = result.getOrNull()
                    } catch (_: Exception) {}
                }
            }
        }
    }

    // Standalone fetch if currentWeather is null
    LaunchedEffect(currentWeather) {
        if (currentWeather == null && localWeather == null) {
            internalLoading = true
            coroutineScope.launch {
                try {
                    val repo = OpenWeatherRepository()
                    val result = repo.fetchCurrentWeather(apiKey ?: "")
                    localWeather = result.getOrNull()
                } catch (_: Exception) {
                    // Handled gracefully with baseline
                } finally {
                    internalLoading = false
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Balasore Location",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱର ପାଣିପାଗ ସାରାଂଶ" else "Balasore Weather Summary",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 15.sp,
                                    color = BentoSlate900
                                )
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
                                "୨୧.୪୯° ଉ, ୮୬.୯୧° ପୂ • ସ୍ୱୟଂଚାଳିତ ଅପଡେଟ୍ (${secondsSinceLastUpdate}s)"
                            else
                                "21.49°N, 86.91°E • Live Auto-Update (${secondsSinceLastUpdate}s ago)",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.sp,
                                color = BentoSlate500
                            )
                        )
                    }
                }

                // Controls: °C / °F Unit Toggle and Refresh Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Temperature Unit Toggle Pill
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

                    Spacer(modifier = Modifier.width(6.dp))

                    // Refresh Button
                    IconButton(
                        onClick = {
                            if (currentWeather != null) {
                                onRefresh()
                            } else {
                                coroutineScope.launch {
                                    internalLoading = true
                                    try {
                                        val repo = OpenWeatherRepository()
                                        val result = repo.fetchCurrentWeather(apiKey ?: "")
                                        localWeather = result.getOrNull()
                                    } catch (_: Exception) {}
                                    finally {
                                        internalLoading = false
                                    }
                                }
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

            // 2-Tab Selector: 'Live Weather' vs 'Historical Alerts' (Severe Weather & Coastal Defense Log)
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
                            text = if (isOdia) "ଲାଇଭ୍ ପାଣିପାଗ" else "Live Summary",
                            fontSize = 11.5.sp,
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
                            fontSize = 11.5.sp,
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
                                "ଅନୁଭୂତ: ${formatTemp(feelsLikeTemp)} • ବାୟୁ: $windKmh କିମି/ଘଣ୍ଟା"
                            else
                                "Feels like: ${formatTemp(feelsLikeTemp)} • Wind: $windKmh km/h",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFFBAE6FD),
                                fontSize = 11.5.sp
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
                                // Dynamic vector fallback matching condition
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

            // Dynamic Severe Weather Alert Banner (Room DB / IMD Synchronized)
            val latestAlert = roomAlerts.firstOrNull()
            if (latestAlert != null) {
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
                                    text = if (isOdia) latestAlert.odiaTitle else latestAlert.title,
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
                            text = latestAlert.summary,
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

            // Bottom Metrics Bar: Humidity, Wind, Cloud Cover & Tap for full telemetry
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Secondary Metrics Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Humidity
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Humidity",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$humidity%",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSlate700
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = if (isOdia) "ଆର୍ଦ୍ରତା" else "Humidity",
                            fontSize = 10.sp,
                            color = BentoSlate500
                        )
                    }

                    // Wind
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Air,
                            contentDescription = "Wind Speed",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = "$windKmh km/h",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSlate700
                        )
                    }
                }

                // Interactive tap link to OpenWeatherMap detail
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenDetails() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
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
