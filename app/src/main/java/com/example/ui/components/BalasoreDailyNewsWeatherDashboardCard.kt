package com.example.ui.components

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Air
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.model.AppLanguage
import com.example.data.service.BalasorePublicNewsWeatherService
import com.example.data.service.DailyNewsWeatherFeed
import com.example.data.service.DailyPublicNewsItem
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Frontend dashboard component that fetches daily Balasore weather & news updates via
 * [BalasorePublicNewsWeatherService] (Open-Meteo Public API + Google News RSS), stores them
 * in the Room `daily_news_weather_updates` table, and displays today's live feed plus
 * recent cached daily snapshots.
 */
@Composable
fun BalasoreDailyNewsWeatherDashboardCard(
    language: AppLanguage,
    onOpenFullWeather: () -> Unit,
    onOpenNewsArticleFallback: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA
    val service = remember(context) { BalasorePublicNewsWeatherService.fromContext(context) }

    val recentFeeds by service.observeRecentDailyFeeds(limit = 7)
        .collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedDateKey by remember { mutableStateOf<String?>(null) }
    var isSyncing by remember { mutableStateOf(false) }
    var syncStatusNote by remember { mutableStateOf<String?>(null) }

    val triggerDailyFetch: (Boolean) -> Unit = { forceRefresh ->
        coroutineScope.launch {
            isSyncing = true
            val result = service.fetchAndSyncDailyUpdates(forceRefresh = forceRefresh)
            result.onSuccess { feed ->
                selectedDateKey = feed.dateKey
                syncStatusNote = if (feed.isFromOfflineCache) {
                    if (isOdia) "ଅଫଲାଇନ୍ ଡାଟାବେସ୍ କ୍ୟାସ୍" else "Offline Room Cache"
                } else {
                    if (isOdia) "ଲାଇଭ୍ ପବ୍ଲିକ୍ API ସିଙ୍କ୍" else "Live Public API Sync"
                }
            }.onFailure {
                syncStatusNote = if (isOdia) "କ୍ୟାସ୍ ଡାଟା ପ୍ରଦର୍ଶିତ" else "Showing Cached Snapshot"
            }
            isSyncing = false
        }
    }

    LaunchedEffect(service) {
        triggerDailyFetch(false)
    }

    val activeFeed: DailyNewsWeatherFeed? = remember(recentFeeds, selectedDateKey) {
        recentFeeds.find { it.dateKey == selectedDateKey } ?: recentFeeds.firstOrNull()
    }

    val alertAccent = when (activeFeed?.weatherAlertLevel) {
        "CYCLONE_ALERT" -> Color(0xFFDC2626)
        "WARNING" -> Color(0xFFEA580C)
        "ADVISORY" -> Color(0xFFD97706)
        else -> EmeraldGreen
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_daily_news_weather_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
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
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(alertAccent)
                        )
                        Text(
                            text = syncStatusNote ?: stringResource(R.string.daily_updates_source_badge),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (isOdia) "ବାଲେଶ୍ୱର ଦୈନିକ ଖବର ଓ ପାଣିପାଗ" else stringResource(R.string.daily_updates_card_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                IconButton(
                    onClick = { triggerDailyFetch(true) },
                    enabled = !isSyncing,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("dashboard_daily_updates_refresh_btn")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = OceanBlue
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.daily_updates_refresh_cd),
                            tint = OceanBlue
                        )
                    }
                }
            }

            // Stored Daily Snapshots History Selector (when > 1 day saved in Room)
            if (recentFeeds.size > 1) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    contentPadding = PaddingValues(vertical = 2.dp)
                ) {
                    items(recentFeeds, key = { it.dateKey }) { snap ->
                        val selected = snap.dateKey == activeFeed?.dateKey
                        FilterChip(
                            selected = selected,
                            onClick = { selectedDateKey = snap.dateKey },
                            label = {
                                Text(
                                    text = formatSnapshotDateLabel(snap.dateKey),
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OceanBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("daily_snapshot_chip_${snap.dateKey}")
                        )
                    }
                }
            }

            if (activeFeed == null) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = OceanBlue
                        )
                        Text(
                            text = if (isOdia) "ବାଲେଶ୍ୱର ଲାଇଭ୍ ପାଣିପାଗ ଓ ଖବର ଅଣାଯାଉଛି..." else stringResource(R.string.daily_updates_loading),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Live Weather Summary Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    OceanBlueDark,
                                    OceanBlue
                                )
                            )
                        )
                        .clickable(onClick = onOpenFullWeather)
                        .padding(14.dp)
                        .testTag("dashboard_daily_weather_banner")
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = if (activeFeed.weatherCode <= 2) Icons.Default.WbSunny else Icons.Default.Cloud,
                                    contentDescription = activeFeed.weatherSummary,
                                    tint = Color(0xFFFDE047),
                                    modifier = Modifier.size(32.dp)
                                )
                                Column {
                                    Text(
                                        text = "${activeFeed.temperatureC.roundToInt()}°C • ${activeFeed.weatherSummary}",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "H: ${activeFeed.maxTempC.roundToInt()}°  L: ${activeFeed.minTempC.roundToInt()}° • ${activeFeed.weatherSource}",
                                        color = Color.White.copy(alpha = 0.85f),
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(999.dp),
                                color = alertAccent
                            ) {
                                Text(
                                    text = activeFeed.weatherAlertLevel.replace("_", " "),
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WaterDrop,
                                    contentDescription = null,
                                    tint = Color(0xFFBAE6FD),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${activeFeed.humidityPct}% RH",
                                    color = Color.White.copy(alpha = 0.92f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Air,
                                    contentDescription = null,
                                    tint = Color(0xFFBAE6FD),
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "${activeFeed.windSpeedKmh.roundToInt()} km/h",
                                    color = Color.White.copy(alpha = 0.92f),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            Spacer(Modifier.weight(1f))
                            Text(
                                text = formatSyncedTime(activeFeed.fetchedAtMs),
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }

                        Text(
                            text = activeFeed.weatherAlertMessage,
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Daily Balasore Public News Headlines
                AnimatedVisibility(visible = activeFeed.newsItems.isNotEmpty(), enter = fadeIn()) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                                    imageVector = Icons.Default.Newspaper,
                                    contentDescription = null,
                                    tint = OceanBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isOdia) "ଆଜିର ବାଲେଶ୍ୱର ମୁଖ୍ୟ ଖବର (${activeFeed.newsItems.size})" else "Daily Balasore Headlines (${activeFeed.newsItems.size})",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            TextButton(
                                onClick = onOpenNewsArticleFallback,
                                modifier = Modifier.testTag("dashboard_daily_updates_all_news_btn")
                            ) {
                                Text(
                                    text = if (isOdia) "ସମସ୍ତ ଖବର" else "More",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        activeFeed.newsItems.take(3).forEachIndexed { idx, newsItem ->
                            DailyPublicHeadlineRow(
                                item = newsItem,
                                index = idx,
                                onClick = {
                                    if (newsItem.url.startsWith("https://", ignoreCase = true) ||
                                        newsItem.url.startsWith("http://", ignoreCase = true)
                                    ) {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.url)))
                                        }.onFailure {
                                            Toast.makeText(context, newsItem.title, Toast.LENGTH_SHORT).show()
                                        }
                                    } else {
                                        onOpenNewsArticleFallback()
                                    }
                                }
                            )
                            if (idx < minOf(2, activeFeed.newsItems.lastIndex)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailyPublicHeadlineRow(
    item: DailyPublicNewsItem,
    index: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp, horizontal = 4.dp)
            .testTag("dashboard_daily_news_item_$index"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = OceanBlue.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = item.category,
                        color = OceanBlue,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "${item.source} • ${item.publishedAt}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = item.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (item.url.isNotBlank()) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                contentDescription = "Open article",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

private fun formatSnapshotDateLabel(dateKey: String): String {
    return runCatching {
        val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val outFmt = SimpleDateFormat("dd MMM", Locale.US)
        val parsed = inFmt.parse(dateKey) ?: return dateKey
        outFmt.format(parsed)
    }.getOrDefault(dateKey)
}

private fun formatSyncedTime(timestampMs: Long): String {
    return runCatching {
        SimpleDateFormat("hh:mm a", Locale.US).format(Date(timestampMs))
    }.getOrDefault("Synced")
}
