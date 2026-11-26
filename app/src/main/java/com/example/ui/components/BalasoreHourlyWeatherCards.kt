package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Thunderstorm
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.remote.BalasoreHourlyForecastBundle
import com.example.data.remote.BalasoreHourlyForecastSlot
import com.example.data.repository.OpenWeatherRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Interactive Hourly Weather Forecast Cards integrated into the Balasore main dashboard.
 * Backed by the existing OpenWeatherMap `/data/2.5/forecast` integration via [OpenWeatherRepository].
 */
@Composable
fun BalasoreHourlyWeatherCardsSection(
    language: AppLanguage,
    onOpenFullWeather: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA
    val repository = remember(context) { OpenWeatherRepository(context.applicationContext) }

    var forecastBundle by remember {
        mutableStateOf(repository.getCuratedBalasoreHourlyForecastFallback())
    }
    var isRefreshing by remember { mutableStateOf(false) }
    var isFahrenheit by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableIntStateOf(0) }

    val loadHourlyForecast: () -> Unit = {
        coroutineScope.launch {
            isRefreshing = true
            val result = withContext(Dispatchers.IO) {
                repository.fetchHourlyForecast()
            }
            result.getOrNull()?.let { fresh ->
                if (fresh.slots.isNotEmpty()) {
                    forecastBundle = fresh
                    if (selectedIndex >= fresh.slots.size) selectedIndex = 0
                }
            }
            isRefreshing = false
        }
    }

    LaunchedEffect(Unit) {
        loadHourlyForecast()
    }

    val slots = forecastBundle.slots
    val selectedSlot = slots.getOrNull(selectedIndex)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_hourly_weather_section"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Atmospheric Coastal Sky Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF0C4A6E),
                                Color(0xFF0369A1),
                                Color(0xFF0284C7)
                            )
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.hourly_weather_card_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = if (forecastBundle.isLiveOpenWeather) {
                                    Color(0xFF10B981).copy(alpha = 0.25f)
                                } else {
                                    Color.White.copy(alpha = 0.18f)
                                }
                            ) {
                                Text(
                                    text = if (forecastBundle.isLiveOpenWeather) "LIVE OWM" else "OWM READY",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isOdia) {
                                "ବାଲେଶ୍ୱର (OpenWeatherMap) • ପ୍ରତି ଘଣ୍ଟାର ପୂର୍ବାନୁମାନ ଦେଖିବାକୁ କାର୍ଡ ଟ୍ୟାପ୍ କରନ୍ତୁ"
                            } else {
                                "${forecastBundle.cityName} • Tap any hour card for wind, humidity & rain details"
                            },
                            fontSize = 11.5.sp,
                            color = Color(0xFFE0F2FE),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // °C / °F Unit Toggle Pill
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .clip(RoundedCornerShape(999.dp))
                                .clickable { isFahrenheit = !isFahrenheit }
                                .testTag("hourly_weather_unit_toggle")
                        ) {
                            Text(
                                text = if (isFahrenheit) "°F" else "°C",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        // Refresh Button
                        IconButton(
                            onClick = { if (!isRefreshing) loadHourlyForecast() },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("hourly_weather_refresh_btn")
                        ) {
                            if (isRefreshing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Hourly Forecast",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }

            // Horizontal Scrollable Set of Interactive Hourly Cards
            LazyRow(
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hourly_weather_cards_row")
            ) {
                itemsIndexed(slots, key = { idx, slot -> "${slot.epochSeconds}_$idx" }) { index, slot ->
                    HourlyForecastTileCard(
                        slot = slot,
                        isSelected = index == selectedIndex,
                        isFahrenheit = isFahrenheit,
                        onClick = { selectedIndex = index },
                        modifier = Modifier.testTag("hourly_weather_card_$index")
                    )
                }
            }

            // Animated Expandable Detail Breakdown for Selected Hour
            AnimatedVisibility(
                visible = selectedSlot != null,
                enter = expandVertically(spring(stiffness = Spring.StiffnessMediumLow)) + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                selectedSlot?.let { slot ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
                            .testTag("hourly_weather_selected_detail"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${slot.timeLabel} • ${slot.conditionDescription}",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = formatTemp(slot.tempC, isFahrenheit),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF0284C7)
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    HourlyMetricPill(
                                        icon = Icons.Default.Thermostat,
                                        label = if (isOdia) "ଅନୁଭବ" else "Feels",
                                        value = formatTemp(slot.feelsLikeC, isFahrenheit),
                                        modifier = Modifier.weight(1f)
                                    )
                                    HourlyMetricPill(
                                        icon = Icons.Default.WaterDrop,
                                        label = if (isOdia) "ଆର୍ଦ୍ରତା" else "Humidity",
                                        value = "${slot.humidityPercent}%",
                                        modifier = Modifier.weight(1f)
                                    )
                                    HourlyMetricPill(
                                        icon = Icons.Default.Air,
                                        label = if (isOdia) "ପବନ" else "Wind",
                                        value = "${slot.windSpeedKmh.roundToInt()} km/h",
                                        modifier = Modifier.weight(1f)
                                    )
                                    HourlyMetricPill(
                                        icon = Icons.Default.Compress,
                                        label = if (isOdia) "ଚାପ" else "hPa",
                                        value = "${slot.pressureHpa}",
                                        modifier = Modifier.weight(1f)
                                    )
                                }

                                Text(
                                    text = if (isOdia) slot.advisoryOr else slot.advisoryEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(
                                onClick = onOpenFullWeather,
                                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF0284C7)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .testTag("hourly_weather_open_full_btn")
                            ) {
                                Text(
                                    text = stringResource(R.string.hourly_weather_open_full),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
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
private fun HourlyForecastTileCard(
    slot: BalasoreHourlyForecastSlot,
    isSelected: Boolean,
    isFahrenheit: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val containerColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        label = "hourlyCardBg"
    )
    val contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
    val subColor = if (isSelected) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = containerColor,
        tonalElevation = if (isSelected) 6.dp else 0.dp,
        shadowElevation = if (isSelected) 4.dp else 0.dp,
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) Color(0xFF38BDF8) else MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .width(92.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = slot.timeLabel,
                fontSize = 11.5.sp,
                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                color = contentColor,
                maxLines = 1
            )

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (isSelected) Color.White.copy(alpha = 0.2f)
                        else weatherConditionTint(slot.conditionMain).copy(alpha = 0.14f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = weatherConditionIcon(slot.conditionMain),
                    contentDescription = slot.conditionDescription,
                    tint = if (isSelected) Color.White else weatherConditionTint(slot.conditionMain),
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = formatTemp(slot.tempC, isFahrenheit),
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WaterDrop,
                    contentDescription = "Rain Chance",
                    tint = if (isSelected) Color(0xFFBAE6FD) else Color(0xFF0284C7),
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = "${slot.rainChancePercent}%",
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = subColor
                )
            }
        }
    }
}

@Composable
private fun HourlyMetricPill(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFF0284C7),
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = label,
                fontSize = 9.5.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

private fun formatTemp(tempC: Double, isFahrenheit: Boolean): String {
    val value = if (isFahrenheit) (tempC * 9.0 / 5.0) + 32.0 else tempC
    val unit = if (isFahrenheit) "°F" else "°C"
    return String.format(Locale.US, "%d%s", value.roundToInt(), unit)
}

private fun weatherConditionIcon(conditionMain: String): ImageVector {
    val normalized = conditionMain.lowercase(Locale.US)
    return when {
        normalized.contains("thunder") || normalized.contains("storm") -> Icons.Default.Thunderstorm
        normalized.contains("rain") || normalized.contains("drizzle") -> Icons.Default.Grain
        normalized.contains("clear") || normalized.contains("sun") -> Icons.Default.WbSunny
        else -> Icons.Default.Cloud
    }
}

private fun weatherConditionTint(conditionMain: String): Color {
    val normalized = conditionMain.lowercase(Locale.US)
    return when {
        normalized.contains("thunder") || normalized.contains("storm") -> Color(0xFFDC2626)
        normalized.contains("rain") || normalized.contains("drizzle") -> Color(0xFF0284C7)
        normalized.contains("clear") || normalized.contains("sun") -> Color(0xFFD97706)
        else -> Color(0xFF475569)
    }
}
