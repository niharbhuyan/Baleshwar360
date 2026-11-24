package com.example.ui.features.offline

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.local.CacheSyncMetadataEntity
import com.example.data.local.DailyForecastEntity
import com.example.data.local.HotspotEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.local.WeatherCacheEntity
import com.example.data.model.AppLanguage
import com.example.data.repository.BalasoreRepository
import com.example.data.repository.OpenWeatherRepository
import com.example.ui.components.HotspotOfflineHighResGalleryCarousel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@Composable
fun OfflineModeManagerSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val newsDao = remember { db.newsDao() }
    val hotspotDao = remember { db.hotspotDao() }
    val weatherDao = remember { db.weatherDao() }
    val festivalDao = remember { db.odiaFestivalDao() }
    val metadataDao = remember { db.cacheMetadataDao() }
    val weatherRepository = remember { OpenWeatherRepository(context) }
    val isOdia = language == AppLanguage.ODIA

    val prefs = remember {
        context.getSharedPreferences("baleshwar_offline_mode_prefs", Context.MODE_PRIVATE)
    }
    val networkMonitor = remember { com.example.data.sync.NetworkMonitor(context) }
    val isDeviceOnline by networkMonitor.isOnline.collectAsStateWithLifecycle(
        initialValue = networkMonitor.isCurrentlyOnline()
    )
    var isOfflineModeSimulated by remember {
        mutableStateOf(prefs.getBoolean("simulate_offline_mode_active", false))
    }
    var offlinePackSynced by remember { mutableStateOf(true) }
    var syncProgress by remember { mutableFloatStateOf(1.0f) }
    var isBatterySaverSync by remember {
        mutableStateOf(prefs.getBoolean("battery_saver_sync_enabled", false))
    }
    var mapTileMb by remember {
        mutableFloatStateOf(prefs.getFloat("cached_osm_tiles_mb", 14.2f))
    }
    var selectedOfflineTab by remember { mutableIntStateOf(0) } // 0: Saved Hotspots, 1: Downloaded News, 2: Downloaded Weather, 3: Content & Storage Manager
    var selectedOfflineHotspot by remember { mutableStateOf<HotspotEntity?>(null) }
    var selectedOfflineNews by remember { mutableStateOf<NewsArticleEntity?>(null) }
    var showOnlyBookmarkedHotspots by remember { mutableStateOf(false) }

    val cachedNewsList: List<NewsArticleEntity> by newsDao.getAllNews().collectAsStateWithLifecycle(initialValue = emptyList())
    val cachedHotspotsList: List<HotspotEntity> by hotspotDao.getAllHotspots().collectAsStateWithLifecycle(initialValue = emptyList())
    val cachedWeather: WeatherCacheEntity? by weatherDao.getWeatherCache().collectAsStateWithLifecycle(initialValue = null)
    val cachedDailyForecasts: List<DailyForecastEntity> by weatherDao.getDailyForecasts().collectAsStateWithLifecycle(initialValue = emptyList())
    val cachedNewsCount by newsDao.getNewsCountFlow().collectAsStateWithLifecycle(initialValue = 12)
    val cachedHotspotsCount by hotspotDao.getHotspotsCountFlow().collectAsStateWithLifecycle(initialValue = 10)
    val cachedFestivals by festivalDao.getAllFestivalsFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    val downloadNewsPackOnly: suspend () -> Int = {
        val articles = BalasoreRepository.newsArticles.mapIndexed { idx, art ->
            NewsArticleEntity(
                id = (idx + 1).toLong(),
                title = art.title,
                summary = art.snippet,
                content = art.content.ifBlank { "${art.snippet}\n\n${art.odiaSnippet}\n\nFull offline report cached by Baleshwar 360 Local Bureau for uninterrupted reading without internet." },
                category = art.category,
                source = art.source,
                publishedAt = art.timeAgo,
                timestamp = System.currentTimeMillis() - idx * 1800_000L,
                isBreaking = idx == 0,
                isBookmarked = true
            )
        }
        newsDao.insertArticles(articles)
        metadataDao.insertOrUpdate(
            CacheSyncMetadataEntity(
                cacheKey = "NEWS",
                lastSyncedAt = System.currentTimeMillis(),
                itemCount = articles.size,
                status = "OFFLINE_READY",
                cacheLabel = "Latest Balasore News Pack",
                details = "${articles.size} News Articles cached for offline reading"
            )
        )
        articles.size
    }

    val downloadHotspotsPackOnly: suspend () -> Int = {
        val spots = BalasoreRepository.hotspots.mapIndexed { idx, spot ->
            HotspotEntity(
                id = spot.id,
                name = spot.name,
                odiaName = spot.odiaName,
                category = spot.category,
                shortDescription = spot.description,
                fullDescription = "${spot.description} Located at ${spot.location} (${spot.distanceKm} km from Balasore town). Cached with full offline visitor timings, entry guidelines, and high-res visual gallery.",
                highlights = "${spot.category}, ${spot.location}, Rating ★ ${spot.rating}",
                distanceKmFromBls = spot.distanceKm.toInt(),
                bestTimeToVisit = "October to March (6:00 AM - 6:00 PM)",
                timings = spot.timing,
                entryFee = "Free Public Entry",
                specialty = "${spot.category} Heritage & Local Attraction",
                localTip = "Carry water and check offline tide/darshan timings before departure.",
                latitude = 21.4934 + (idx * 0.012),
                longitude = 86.9325 + (idx * 0.015),
                isFavorite = true
            )
        }
        hotspotDao.insertHotspots(spots)
        metadataDao.insertOrUpdate(
            CacheSyncMetadataEntity(
                cacheKey = "TOURISM",
                lastSyncedAt = System.currentTimeMillis(),
                itemCount = spots.size,
                status = "OFFLINE_READY",
                cacheLabel = "Tourism Hotspots & Guides Pack",
                details = "${spots.size} Tourism Hotspots cached for offline viewing"
            )
        )
        spots.size
    }

    val downloadWeatherPackOnly: suspend () -> Int = {
        val liveOrFallbackWeather = weatherRepository.fetchCurrentWeather().getOrDefault(
            weatherRepository.getCuratedBalasoreWeatherFallback()
        )
        val liveOrFallbackForecast = weatherRepository.fetchThreeDayForecast().getOrDefault(
            weatherRepository.getCuratedBalasoreThreeDayForecastFallback()
        )
        val cond = liveOrFallbackWeather.weather.firstOrNull()
        val mainTemp = liveOrFallbackWeather.main?.temp ?: 29.5
        val feelsTemp = liveOrFallbackWeather.main?.feelsLike ?: 31.0
        val windSpd = liveOrFallbackWeather.wind?.speed ?: 4.5
        val windGst = liveOrFallbackWeather.wind?.gust ?: (windSpd * 1.25)
        weatherDao.insertOrUpdateWeather(
            WeatherCacheEntity(
                id = 1,
                temperature = mainTemp,
                apparentTemperature = feelsTemp,
                weatherCode = cond?.id ?: 800,
                weatherDescription = cond?.description ?: "Pleasant Balasore coastal breeze",
                windSpeed = (windSpd * 3.6 * 10).roundToInt() / 10.0,
                windGusts = (windGst * 3.6 * 10).roundToInt() / 10.0,
                humidity = liveOrFallbackWeather.main?.humidity ?: 74,
                alertLevel = "NORMAL",
                alertTitle = "Balasore Coastal & Chandipur Tide Advisory",
                alertMessage = "Clear coastal visibility; safe low-tide seabed walk window active.",
                tideState = "LOW_TIDE",
                tideDescription = "Sea receded ~4.8 km at Chandipur Beach",
                sunrise = "05:42 AM",
                sunset = "05:18 PM",
                uvIndex = 6.0,
                maxTemp = liveOrFallbackWeather.main?.tempMax ?: 31.2,
                minTemp = liveOrFallbackWeather.main?.tempMin ?: 24.5,
                lastUpdated = System.currentTimeMillis()
            )
        )
        val dailyEntities = liveOrFallbackForecast.take(3).map { d ->
            DailyForecastEntity(
                date = d.dateLabel,
                dayOfWeek = "${d.dayName} (${d.odiaDayName}) • Humidity ${d.avgHumidity}% • Wind ${d.windSpeedKmh.roundToInt()} km/h • Rain ${d.popPercent}%",
                weatherCode = if (d.conditionMain.contains("Rain", ignoreCase = true)) 500 else 800,
                weatherDescription = "${d.conditionMain} — ${d.conditionDesc}",
                maxTemp = d.tempMax,
                minTemp = d.tempMin,
                sunrise = "05:42 AM",
                sunset = "05:18 PM",
                uvIndex = 6.0,
                cachedAt = System.currentTimeMillis()
            )
        }
        weatherDao.clearDailyForecasts()
        weatherDao.insertDailyForecasts(dailyEntities)
        metadataDao.insertOrUpdate(
            CacheSyncMetadataEntity(
                cacheKey = "WEATHER",
                lastSyncedAt = System.currentTimeMillis(),
                itemCount = 1 + dailyEntities.size,
                status = "OFFLINE_READY",
                cacheLabel = "Balasore Live Weather & 3-Day Forecast Pack",
                details = "${mainTemp.roundToInt()}°C + ${dailyEntities.size}-Day Forecast cached"
            )
        )
        1 + dailyEntities.size
    }

    // Ensure Room has initial offline news, weather & tourism hotspots cached on first open
    LaunchedEffect(Unit) {
        if (newsDao.getCount() == 0) {
            downloadNewsPackOnly()
        }
        if (hotspotDao.getCount() == 0) {
            downloadHotspotsPackOnly()
        }
        if (weatherDao.getWeatherCacheSync() == null || weatherDao.getDailyForecastsSync().isEmpty()) {
            downloadWeatherPackOnly()
        }
    }

    val weatherSizeKb = remember(cachedWeather, cachedDailyForecasts.size) {
        if (cachedWeather != null) 42 + (cachedDailyForecasts.size * 16) else 0
    }

    val totalMb = remember(cachedNewsCount, cachedHotspotsCount, cachedFestivals.size, weatherSizeKb, mapTileMb) {
        ((cachedNewsCount * 28 + cachedHotspotsCount * 145 + cachedFestivals.size * 18 + weatherSizeKb) / 1024f) + mapTileMb
    }

    val downloadCriticalOfflineContent: () -> Unit = {
        coroutineScope.launch {
            syncProgress = 0.25f
            val newsSaved = downloadNewsPackOnly()
            syncProgress = 0.60f
            val spotsSaved = downloadHotspotsPackOnly()
            syncProgress = 0.85f
            val weatherSaved = downloadWeatherPackOnly()

            metadataDao.insertOrUpdate(
                CacheSyncMetadataEntity(
                    cacheKey = "CRITICAL_OFFLINE_PACK",
                    lastSyncedAt = System.currentTimeMillis(),
                    itemCount = newsSaved + spotsSaved + weatherSaved,
                    status = "OFFLINE_READY",
                    cacheLabel = "News, Weather & Tourism Hotspots Offline Pack",
                    details = "$newsSaved News + 3-Day Weather Forecast + $spotsSaved Hotspots"
                )
            )
            com.example.ui.components.BalasoreOfflineAccessManager.syncStateFromOfflineManager(
                context = context,
                newsIds = BalasoreRepository.newsArticles.map { it.id }.toSet(),
                hotspotIds = BalasoreRepository.hotspots.map { it.id }.toSet(),
                weatherCached = true,
                weatherSummary = "${cachedWeather?.temperature?.roundToInt() ?: 30}°C • 3-Day Forecast Cached",
                updateSyncTimestamp = true
            )
            offlinePackSynced = true
            syncProgress = 1.0f
            mapTileMb = 18.4f
            prefs.edit().putFloat("cached_osm_tiles_mb", 18.4f).apply()
            Toast.makeText(
                context,
                if (isOdia) "ସମସ୍ତ ସମ୍ବାଦ, ପାଣିପାଗ ଓ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ଅଫଲାଇନ୍ ପାଇଁ ଡାଉନଲୋଡ୍ ହେଲା!"
                else "Downloaded $newsSaved news articles, 3-day Balasore weather & $spotsSaved hotspots for offline access!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_mode_manager_sheet"),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
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
                        imageVector = if (isOfflineModeSimulated) Icons.Default.CloudOff else Icons.Default.CloudDone,
                        contentDescription = "Offline Storage Manager",
                        tint = if (isOfflineModeSimulated) Color(0xFFD97706) else MaterialTheme.colorScheme.primary
                    )
                    Column {
                        Text(
                            text = if (isOdia) "ଅଫଲାଇନ୍ ଆକ୍ସେସ୍ ଓ ଡାଉନଲୋଡ୍ ହବ୍" else "Offline Access & Download Hub",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isOfflineModeSimulated) {
                                if (isOdia) "ଅଫଲାଇନ୍ ମୋଡ୍ ସକ୍ରିୟ • ସ୍ଥାନୀୟ Room ଡାଟାବେସ୍ ବ୍ୟବହୃତ" else "Offline Mode Active • Reading from Room SQLite Cache"
                            } else {
                                if (isOdia) "ସମ୍ବାଦ ଓ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ବିନା ଇଣ୍ଟରନେଟରେ ପଢ଼ନ୍ତୁ" else "Download recent news & view saved hotspot details offline"
                            },
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_offline_manager_button")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Offline Manager")
                }
            }

            // Master Offline Mode Switch + One-Tap Download All Button
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = if (isOfflineModeSimulated) Color(0xFFFFFBEB) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
                border = BorderStroke(
                    1.dp,
                    if (isOfflineModeSimulated) Color(0xFFF59E0B) else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ),
                modifier = Modifier.fillMaxWidth()
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
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "ଅଫଲାଇନ୍ ମୋଡ୍ (Offline Access Mode)" else "Seamless Offline Access Mode",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isOfflineModeSimulated) Color(0xFFB45309) else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isOdia)
                                    "ଇଣ୍ଟରନେଟ୍ ନଥିଲେ ମଧ୍ୟ ଡାଉନଲୋଡ୍ ହୋଇଥିବା ଖବର ଓ ପର୍ଯ୍ୟଟନ ତଥ୍ୟ ଦେଖନ୍ତୁ"
                                else
                                    "Read downloaded news articles & saved hotspot guides with zero connectivity",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isOfflineModeSimulated,
                            onCheckedChange = { enabled ->
                                isOfflineModeSimulated = enabled
                                prefs.edit().putBoolean("simulate_offline_mode_active", enabled).apply()
                                Toast.makeText(
                                    context,
                                    if (enabled) "Offline Mode Enabled: Serving saved hotspots & downloaded news from Room"
                                    else "Online Sync Mode Restored",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.testTag("offline_sheet_master_switch")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = downloadCriticalOfflineContent,
                            modifier = Modifier
                                .weight(1.3f)
                                .height(48.dp)
                                .testTag("download_all_critical_offline_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.DownloadForOffline, contentDescription = null, modifier = Modifier.size(17.dp))
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) "ସବୁ ଡାଉନଲୋଡ୍ କରନ୍ତୁ" else "Download All Offline",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                if (!isDeviceOnline) {
                                    Toast.makeText(
                                        context,
                                        if (isOdia) "ଅପଡେଟ୍ କରିବାକୁ ଇଣ୍ଟରନେଟ୍ ସଂଯୋଗ କରନ୍ତୁ।"
                                        else "Device is offline. Connect to internet to refresh downloaded packs.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    downloadCriticalOfflineContent()
                                }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("offline_manager_refresh_online_btn"),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isDeviceOnline) Icons.Default.Refresh else Icons.Default.CloudOff,
                                contentDescription = "Refresh Online",
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isDeviceOnline) {
                                    if (isOdia) "ଅନଲାଇନ୍ ରିଫ୍ରେସ୍" else "Refresh Online"
                                } else {
                                    if (isOdia) "ଅଫଲାଇନ୍" else "Offline"
                                },
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Interactive Tab Row: Saved Hotspots | Downloaded News | Downloaded Weather | Manage & Storage
            ScrollableTabRow(
                selectedTabIndex = selectedOfflineTab,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Tab(
                    selected = selectedOfflineTab == 0,
                    onClick = { selectedOfflineTab = 0 },
                    modifier = Modifier.testTag("offline_tab_hotspots"),
                    text = {
                        Text(
                            text = if (isOdia) "ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ (${cachedHotspotsList.size})" else "Saved Hotspots (${cachedHotspotsList.size})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedOfflineTab == 1,
                    onClick = { selectedOfflineTab = 1 },
                    modifier = Modifier.testTag("offline_tab_news"),
                    text = {
                        Text(
                            text = if (isOdia) "ଡାଉନଲୋଡ୍ ଖବର (${cachedNewsList.size})" else "Downloaded News (${cachedNewsList.size})",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedOfflineTab == 2,
                    onClick = { selectedOfflineTab = 2 },
                    modifier = Modifier.testTag("offline_tab_weather"),
                    text = {
                        Text(
                            text = if (isOdia) "ପାଣିପାଗ (${cachedDailyForecasts.size} ଦିନ)" else "Offline Weather (${cachedDailyForecasts.size}d)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                Tab(
                    selected = selectedOfflineTab == 3,
                    onClick = { selectedOfflineTab = 3 },
                    modifier = Modifier.testTag("offline_tab_storage"),
                    text = {
                        Text(
                            text = if (isOdia) "ପରିଚାଳନା (${String.format(Locale.US, "%.1f", totalMb)} MB)" else "Manage (${String.format(Locale.US, "%.1f", totalMb)} MB)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
            }

            when (selectedOfflineTab) {
                0 -> {
                    // ================= TAB 0: SAVED TOURISM HOTSPOTS OFFLINE VIEWER =================
                    val displayedHotspots = remember(cachedHotspotsList, showOnlyBookmarkedHotspots) {
                        if (showOnlyBookmarkedHotspots) {
                            cachedHotspotsList.filter { it.isFavorite }
                        } else {
                            cachedHotspotsList
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "ଅଫଲାଇନ୍ ପର୍ଯ୍ୟଟନ ବିବରଣୀ (ଟ୍ୟାପ୍ କରି ଦେଖନ୍ତୁ)" else "Tap any saved hotspot to view full details offline:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        FilterChip(
                            selected = showOnlyBookmarkedHotspots,
                            onClick = { showOnlyBookmarkedHotspots = !showOnlyBookmarkedHotspots },
                            label = {
                                Text(
                                    text = if (showOnlyBookmarkedHotspots) "★ Saved Only" else "All Cached",
                                    fontSize = 10.5.sp
                                )
                            },
                            modifier = Modifier.testTag("offline_filter_saved_hotspots_chip")
                        )
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 290.dp)
                            .testTag("offline_saved_hotspots_list"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(displayedHotspots, key = { it.id }) { spot ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedOfflineHotspot = spot }
                                    .testTag("offline_hotspot_item_${spot.id}"),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = if (isOdia) spot.odiaName else spot.name,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.5.sp
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = "OFFLINE READY",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF166534),
                                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "${spot.category} • ${spot.distanceKmFromBls} km • ${spot.timings}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.primary,
                                            fontWeight = FontWeight.Medium
                                        )
                                        Text(
                                            text = spot.shortDescription,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    hotspotDao.updateFavorite(spot.id, !spot.isFavorite)
                                                    Toast.makeText(
                                                        context,
                                                        if (!spot.isFavorite) "Saved ${spot.name} for pinned offline access"
                                                        else "Unpinned ${spot.name}",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.testTag("offline_toggle_save_hotspot_${spot.id}")
                                        ) {
                                            Icon(
                                                imageVector = if (spot.isFavorite) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder,
                                                contentDescription = "Save Offline",
                                                tint = if (spot.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    hotspotDao.deleteHotspotById(spot.id)
                                                    Toast.makeText(
                                                        context,
                                                        "Removed ${spot.name} from offline storage",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            modifier = Modifier.testTag("offline_delete_hotspot_${spot.id}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Remove Offline Hotspot",
                                                tint = MaterialTheme.colorScheme.error
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // ================= TAB 1: DOWNLOADED RECENT NEWS, FULL-TEXT SEARCH & NEWS ARCHIVE =================
                    var offlineSearchQuery by remember { mutableStateOf("") }
                    var selectedOfflineCategory by remember { mutableStateOf("All") } // "All", "Politics", "Tourism", "Education", "Defense"
                    var isArchiveMode by remember { mutableStateOf(false) }
                    var selectedArchiveDate by remember { mutableStateOf<String?>(null) }

                    val offlineCategories = listOf("All", "Politics", "Tourism", "Education", "Defense")

                    // Date Formatter for News Archive
                    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

                    // Date groups for historical archive browsing
                    val groupedArchiveDates = remember(cachedNewsList) {
                        cachedNewsList
                            .map { dateFormat.format(Date(it.timestamp)) }
                            .distinct()
                    }

                    val filteredOfflineNews = remember(
                        cachedNewsList,
                        offlineSearchQuery,
                        selectedOfflineCategory,
                        isArchiveMode,
                        selectedArchiveDate
                    ) {
                        cachedNewsList.filter { art ->
                            val matchesCategory = when (selectedOfflineCategory) {
                                "All" -> true
                                "Politics" -> art.category.contains("Politics", ignoreCase = true) ||
                                        art.title.contains("Parishad", ignoreCase = true) ||
                                        art.title.contains("Council", ignoreCase = true)
                                "Tourism" -> art.category.contains("Tourism", ignoreCase = true) ||
                                        art.title.contains("Tourism", ignoreCase = true) ||
                                        art.title.contains("Beach", ignoreCase = true) ||
                                        art.title.contains("Temple", ignoreCase = true)
                                "Education" -> art.category.contains("Education", ignoreCase = true) ||
                                        art.title.contains("University", ignoreCase = true) ||
                                        art.title.contains("Campus", ignoreCase = true) ||
                                        art.title.contains("School", ignoreCase = true)
                                "Defense" -> art.category.contains("Defense", ignoreCase = true) ||
                                        art.title.contains("DRDO", ignoreCase = true) ||
                                        art.title.contains("Missile", ignoreCase = true) ||
                                        art.title.contains("ITR", ignoreCase = true)
                                else -> art.category.contains(selectedOfflineCategory, ignoreCase = true)
                            }

                            val matchesSearch = if (offlineSearchQuery.isBlank()) {
                                true
                            } else {
                                art.title.contains(offlineSearchQuery, ignoreCase = true) ||
                                        art.summary.contains(offlineSearchQuery, ignoreCase = true) ||
                                        art.content.contains(offlineSearchQuery, ignoreCase = true) ||
                                        art.category.contains(offlineSearchQuery, ignoreCase = true) ||
                                        art.source.contains(offlineSearchQuery, ignoreCase = true)
                            }

                            val matchesDate = if (!isArchiveMode || selectedArchiveDate == null) {
                                true
                            } else {
                                dateFormat.format(Date(art.timestamp)) == selectedArchiveDate
                            }

                            matchesCategory && matchesSearch && matchesDate
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOdia) "ଡାଉନଲୋଡ୍ ହୋଇଥିବା ଖବର (${filteredOfflineNews.size}/${cachedNewsList.size})"
                                else "Downloaded News (${filteredOfflineNews.size}/${cachedNewsList.size})",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            // Segmented Switch: Recent Feed vs News Archive by Date
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.testTag("offline_news_view_mode_toggle")
                            ) {
                                Row(
                                    modifier = Modifier.padding(2.dp),
                                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (!isArchiveMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier
                                            .clickable {
                                                isArchiveMode = false
                                                selectedArchiveDate = null
                                            }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("offline_view_recent_feed_btn")
                                    ) {
                                        Text(
                                            text = if (isOdia) "ସାମ୍ପ୍ରତିକ" else "Feed",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (!isArchiveMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isArchiveMode) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        modifier = Modifier
                                            .clickable { isArchiveMode = true }
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .testTag("offline_view_news_archive_btn")
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.CalendarMonth,
                                                contentDescription = null,
                                                modifier = Modifier.size(12.dp),
                                                tint = if (isArchiveMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                            Spacer(Modifier.width(3.dp))
                                            Text(
                                                text = if (isOdia) "ଆର୍କାଇଭ୍" else "Archive",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isArchiveMode) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Full-Text Search Input for Cached Database
                        OutlinedTextField(
                            value = offlineSearchQuery,
                            onValueChange = { offlineSearchQuery = it },
                            placeholder = {
                                Text(
                                    text = if (isOdia) "ଡାଉନଲୋଡ୍ ହୋଇଥିବା ଖବରରେ ଖୋଜନ୍ତୁ (Full-text)..." else "Full-text search in cached database...",
                                    fontSize = 11.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                if (offlineSearchQuery.isNotBlank()) {
                                    IconButton(onClick = { offlineSearchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear Search", modifier = Modifier.size(16.dp))
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("offline_full_text_search_input")
                        )

                        // Category Filter Toggles (Politics, Tourism, Education, Defense)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = if (isOdia) "ବିଭାଗ:" else "Category:",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            offlineCategories.forEach { cat ->
                                val isSelected = selectedOfflineCategory == cat
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedOfflineCategory = cat },
                                    label = {
                                        Text(
                                            text = when (cat) {
                                                "Politics" -> if (isOdia) "ରାଜନୀତି" else "Politics"
                                                "Tourism" -> if (isOdia) "ପର୍ଯ୍ୟଟନ" else "Tourism"
                                                "Education" -> if (isOdia) "ଶିକ୍ଷା" else "Education"
                                                "Defense" -> if (isOdia) "ପ୍ରତିରକ୍ଷା" else "Defense"
                                                else -> if (isOdia) "ସମସ୍ତ" else "All"
                                            },
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                        )
                                    },
                                    modifier = Modifier.testTag("offline_filter_category_${cat.lowercase()}")
                                )
                            }
                        }

                        // News Archive Date Selector Row (Visible when Archive Mode is active)
                        AnimatedVisibility(visible = isArchiveMode) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .testTag("offline_news_archive_date_selector")
                            ) {
                                Column(modifier = Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.History, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = if (isOdia) "📅 ତାରିଖ ଅନୁସାରେ ଖବର ଆର୍କାଇଭ୍ (Historical News):" else "📅 Historical News Archive (Browse by Date):",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (selectedArchiveDate == null) MaterialTheme.colorScheme.primary else Color.White,
                                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                            modifier = Modifier
                                                .clickable { selectedArchiveDate = null }
                                                .testTag("archive_date_all_btn")
                                        ) {
                                            Text(
                                                text = if (isOdia) "ସମସ୍ତ ତାରିଖ" else "All Dates",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (selectedArchiveDate == null) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                        groupedArchiveDates.take(4).forEach { dateStr ->
                                            val isDateSelected = selectedArchiveDate == dateStr
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = if (isDateSelected) MaterialTheme.colorScheme.primary else Color.White,
                                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                                                modifier = Modifier
                                                    .clickable { selectedArchiveDate = dateStr }
                                                    .testTag("archive_date_btn_$dateStr")
                                            ) {
                                                Text(
                                                    text = dateStr,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = if (isDateSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Filtered & Archived Downloaded News List
                        if (filteredOfflineNews.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 12.dp)
                                    .testTag("offline_news_empty_results")
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("🔍", fontSize = 24.sp)
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = if (isOdia) "କୌଣସି ଡାଉନଲୋଡ୍ ଖବର ମିଳିଲା ନାହିଁ" else "No cached news articles match your filter/query",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (isOdia) "ଅନ୍ୟ କିୱାର୍ଡ ଖୋଜନ୍ତୁ କିମ୍ବା ଫିଲ୍ଟର୍ ରିସେଟ୍ କରନ୍ତୁ" else "Try clearing the search query or switching category",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp)
                                    .testTag("offline_downloaded_news_list"),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(filteredOfflineNews, key = { it.id }) { article ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { selectedOfflineNews = article }
                                            .testTag("offline_news_item_${article.id}"),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        )
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Surface(
                                                        shape = RoundedCornerShape(6.dp),
                                                        color = MaterialTheme.colorScheme.primaryContainer
                                                    ) {
                                                        Text(
                                                            text = article.category,
                                                            fontSize = 9.5.sp,
                                                            fontWeight = FontWeight.Bold,
                                                            color = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = "${article.source} • ${dateFormat.format(Date(article.timestamp))}",
                                                        fontSize = 10.sp,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Spacer(Modifier.height(4.dp))
                                                Text(
                                                    text = article.title,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 13.sp,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = article.summary,
                                                    fontSize = 11.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            newsDao.updateBookmark(article.id, !article.isBookmarked)
                                                        }
                                                    },
                                                    modifier = Modifier.testTag("offline_toggle_bookmark_news_${article.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = if (article.isBookmarked) Icons.Default.DownloadDone else Icons.Default.Download,
                                                        contentDescription = "Saved Offline",
                                                        tint = if (article.isBookmarked) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            newsDao.deleteArticleById(article.id)
                                                            Toast.makeText(
                                                                context,
                                                                "Removed news article from offline cache",
                                                                Toast.LENGTH_SHORT
                                                            ).show()
                                                        }
                                                    },
                                                    modifier = Modifier.testTag("offline_delete_news_${article.id}")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.DeleteOutline,
                                                        contentDescription = "Delete Cached News",
                                                        tint = MaterialTheme.colorScheme.error
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

                2 -> {
                    // ================= TAB 2: DOWNLOADED WEATHER & 3-DAY BALASORE FORECAST =================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("offline_weather_tab_content"),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOdia) "ଡାଉନଲୋଡ୍ ହୋଇଥିବା ବାଲେଶ୍ୱର ପାଣିପାଗ ଓ ୩-ଦିନିଆ ପୂର୍ବାନୁମାନ"
                                else "Downloaded Balasore Weather & 3-Day Forecast",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            downloadWeatherPackOnly()
                                            Toast.makeText(
                                                context,
                                                "Downloaded latest Balasore weather & 3-day forecast for offline viewing!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(30.dp)
                                        .testTag("offline_download_weather_btn")
                                ) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(if (isOdia) "ଅପଡେଟ୍" else "Update", fontSize = 10.5.sp)
                                }
                                if (cachedWeather != null) {
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                weatherDao.clearWeatherCache()
                                                weatherDao.clearDailyForecasts()
                                                Toast.makeText(
                                                    context,
                                                    "Cleared downloaded weather snapshot",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(30.dp)
                                            .testTag("offline_delete_weather_btn")
                                    ) {
                                        Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(Modifier.width(3.dp))
                                        Text(if (isOdia) "ଡିଲିଟ୍" else "Remove", fontSize = 10.5.sp)
                                    }
                                }
                            }
                        }

                        if (cachedWeather == null && cachedDailyForecasts.isEmpty()) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("offline_weather_empty_state")
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(Icons.Default.CloudOff, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Text(
                                        text = if (isOdia) "କୌଣସି ଅଫଲାଇନ୍ ପାଣିପାଗ ତଥ୍ୟ ସେଭ୍ ହୋଇନାହିଁ"
                                        else "No offline weather snapshot saved yet",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.5.sp
                                    )
                                    Button(
                                        onClick = {
                                            coroutineScope.launch { downloadWeatherPackOnly() }
                                        }
                                    ) {
                                        Text(if (isOdia) "ପାଣିପାଗ ଡାଉନଲୋଡ୍ କରନ୍ତୁ" else "Download Balasore Weather Pack")
                                    }
                                }
                            }
                        } else {
                            cachedWeather?.let { w ->
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFF0F9FF),
                                    border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("offline_cached_current_weather_card")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(12.dp),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = "☀️ ${w.temperature.roundToInt()}°C (Feels ${w.apparentTemperature.roundToInt()}°C) • ${w.weatherDescription.replaceFirstChar { it.uppercase() }}",
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 13.5.sp,
                                                color = Color(0xFF0C4A6E)
                                            )
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = "OFFLINE SAVED",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF166534),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                        }
                                        Text(
                                            text = "Humidity: ${w.humidity}% • Wind: ${w.windSpeed} km/h • High/Low: ${w.maxTemp.roundToInt()}°/${w.minTemp.roundToInt()}°C",
                                            fontSize = 11.5.sp,
                                            color = Color(0xFF0369A1),
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = "🌊 Tide Snapshot: ${w.tideDescription} • Sunrise ${w.sunrise} / Sunset ${w.sunset}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF0C4A6E)
                                        )
                                    }
                                }
                            }

                            if (cachedDailyForecasts.isNotEmpty()) {
                                Text(
                                    text = if (isOdia) "📅 ୩-ଦିନିଆ ଅଫଲାଇନ୍ ପାଣିପାଗ ପୂର୍ବାନୁମାନ:" else "📅 Cached 3-Day Balasore Forecast:",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    cachedDailyForecasts.take(3).forEachIndexed { idx, day ->
                                        Surface(
                                            shape = RoundedCornerShape(12.dp),
                                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("offline_forecast_day_$idx")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(10.dp),
                                                verticalArrangement = Arrangement.spacedBy(3.dp)
                                            ) {
                                                Text(
                                                    text = day.date,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 11.5.sp,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                                Text(
                                                    text = "${day.maxTemp.roundToInt()}°C / ${day.minTemp.roundToInt()}°C",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = day.weatherDescription,
                                                    fontSize = 10.sp,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = day.dayOfWeek,
                                                    fontSize = 9.5.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                3 -> {
                    // ================= TAB 3: DOWNLOADED CONTENT MANAGER & STORAGE FOOTPRINT =================
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("offline_content_manager_panel"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        LinearProgressIndicator(
                            progress = { syncProgress },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text(
                            text = if (isOdia) "ଡାଉନଲୋଡ୍ ହୋଇଥିବା ବିଷୟବସ୍ତୁ ପରିଚାଳନା (Manage Downloaded Packs)"
                            else "Manage Downloaded Content Packs (${String.format(Locale.US, "%.1f", totalMb)} MB Total)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.5.sp
                        )

                        // Pack 1: Latest News Pack
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manage_pack_news_row")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isOdia) "📰 ସାମ୍ପ୍ରତିକ ଖବର ପ୍ୟାକ୍ (Latest News)" else "📰 Latest News Offline Pack",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "$cachedNewsCount articles cached (${cachedNewsCount * 28} KB)",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                downloadNewsPackOnly()
                                                Toast.makeText(context, "Updated Offline News Pack", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("manage_download_news_btn")
                                    ) {
                                        Text(if (isOdia) "ଡାଉନଲୋଡ୍" else "Update", fontSize = 10.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                newsDao.clearAll()
                                                Toast.makeText(context, "Removed Downloaded News Pack", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("manage_delete_news_btn")
                                    ) {
                                        Text(if (isOdia) "ଡିଲିଟ୍" else "Remove", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        // Pack 2: Balasore Weather & 3-Day Forecast Pack
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manage_pack_weather_row")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isOdia) "🌤️ ପାଣିପାଗ ଓ ୩-ଦିନିଆ ପୂର୍ବାନୁମାନ" else "🌤️ Weather & 3-Day Forecast Pack",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = if (cachedWeather != null)
                                            "Current (${cachedWeather?.temperature?.roundToInt()}°C) + ${cachedDailyForecasts.size}-Day Forecast ($weatherSizeKb KB)"
                                        else
                                            "Not cached (0 KB)",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                downloadWeatherPackOnly()
                                                Toast.makeText(context, "Updated Offline Weather Pack", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("manage_download_weather_btn")
                                    ) {
                                        Text(if (isOdia) "ଡାଉନଲୋଡ୍" else "Update", fontSize = 10.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                weatherDao.clearWeatherCache()
                                                weatherDao.clearDailyForecasts()
                                                Toast.makeText(context, "Removed Offline Weather Pack", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("manage_delete_weather_btn")
                                    ) {
                                        Text(if (isOdia) "ଡିଲିଟ୍" else "Remove", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        // Pack 3: Tourism Hotspots Pack
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("manage_pack_hotspots_row")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isOdia) "🏖️ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ପ୍ୟାକ୍ (Hotspots)" else "🏖️ Tourism Hotspots & Guides Pack",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = "$cachedHotspotsCount locations cached (${cachedHotspotsCount * 145} KB)",
                                        fontSize = 10.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                downloadHotspotsPackOnly()
                                                Toast.makeText(context, "Updated Offline Tourism Hotspots Pack", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("manage_download_hotspots_btn")
                                    ) {
                                        Text(if (isOdia) "ଡାଉନଲୋଡ୍" else "Update", fontSize = 10.sp)
                                    }
                                    OutlinedButton(
                                        onClick = {
                                            coroutineScope.launch {
                                                hotspotDao.clearAllHotspots()
                                                Toast.makeText(context, "Removed Offline Hotspots Pack", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                        modifier = Modifier
                                            .height(28.dp)
                                            .testTag("manage_delete_hotspots_btn")
                                    ) {
                                        Text(if (isOdia) "ଡିଲିଟ୍" else "Remove", fontSize = 10.sp)
                                    }
                                }
                            }
                        }

                        // Battery-Saving Mode Toggle
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BatteryChargingFull,
                                        contentDescription = "Battery Saving Mode",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = if (isOdia) "ବ୍ୟାଟେରୀ ସେଭିଂ ମୋଡ୍" else "Battery-Saving Sync Mode",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.5.sp
                                        )
                                        Text(
                                            text = "Reduces background data sync frequency for News & Bus Schedules",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = isBatterySaverSync,
                                    onCheckedChange = { enabled ->
                                        isBatterySaverSync = enabled
                                        prefs.edit().putBoolean("battery_saver_sync_enabled", enabled).apply()
                                    },
                                    modifier = Modifier.testTag("sheet_battery_saver_sync_switch")
                                )
                            }
                        }
                    }
                }
            }

            // Footer Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (offlinePackSynced) {
                        "Baleshwar 360 Offline Pack: Ready (${String.format(Locale.US, "%.1f", totalMb)} MB)"
                    } else {
                        "Offline Cache Cleared (0.0 MB)"
                    },
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            coroutineScope.launch {
                                newsDao.clearNonBookmarked()
                                hotspotDao.clearAllHotspots()
                                weatherDao.clearWeatherCache()
                                weatherDao.clearDailyForecasts()
                                metadataDao.clearAll()
                                com.example.ui.components.BalasoreOfflineAccessManager.syncStateFromOfflineManager(
                                    context = context,
                                    newsIds = emptySet(),
                                    hotspotIds = emptySet(),
                                    weatherCached = false,
                                    weatherSummary = "Not downloaded yet",
                                    updateSyncTimestamp = false
                                )
                                mapTileMb = 0.0f
                                offlinePackSynced = false
                                syncProgress = 0.0f
                                prefs.edit().putFloat("cached_osm_tiles_mb", 0.0f).apply()
                                Toast.makeText(
                                    context,
                                    "Cleared offline cached news, weather & tourism data!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.testTag("sheet_clear_all_offline_data_button")
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Clear All Offline Data")
                        Spacer(Modifier.width(4.dp))
                        Text(if (isOdia) "ସଫା କରନ୍ତୁ" else "Clear All")
                    }

                    Button(
                        onClick = downloadCriticalOfflineContent,
                        modifier = Modifier.testTag("refresh_offline_pack_button")
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Verify Offline Cache")
                        Spacer(Modifier.width(6.dp))
                        Text(if (isOdia) "ଅପଡେଟ୍" else "Verify Cache")
                    }
                }
            }
        }
    }

    // ================= OFFLINE HOTSPOT DETAIL DIALOG =================
    selectedOfflineHotspot?.let { hotspot ->
        Dialog(onDismissRequest = { selectedOfflineHotspot = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_hotspot_detail_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Text(
                                    text = "✓ AVAILABLE OFFLINE • ROOM SQLITE",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF166534),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = hotspot.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "${hotspot.odiaName} • ${hotspot.category}",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        IconButton(
                            onClick = { selectedOfflineHotspot = null },
                            modifier = Modifier.testTag("close_offline_hotspot_dialog_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    HotspotOfflineHighResGalleryCarousel(
                        spotName = hotspot.name,
                        spotCategory = hotspot.category,
                        isOdia = isOdia
                    )

                    Text(
                        text = hotspot.fullDescription,
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("⏰ Timings: ${hotspot.timings}", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                            Text("🎟️ Entry Fee: ${hotspot.entryFee}", fontSize = 11.5.sp)
                            Text("📍 Distance from Balasore: ${hotspot.distanceKmFromBls} km", fontSize = 11.5.sp)
                            Text(
                                "🧭 Offline GPS: ${String.format(Locale.US, "%.4f", hotspot.latitude)}° N, ${String.format(Locale.US, "%.4f", hotspot.longitude)}° E",
                                fontSize = 11.5.sp
                            )
                            Text("💡 Local Tip: ${hotspot.localTip}", fontSize = 11.5.sp, color = MaterialTheme.colorScheme.primary)
                        }
                    }

                    Button(
                        onClick = { selectedOfflineHotspot = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isOdia) "ବନ୍ଦ କରନ୍ତୁ" else "Done Reading Offline Guide")
                    }
                }
            }
        }
    }

    // ================= OFFLINE NEWS ARTICLE READER DIALOG =================
    selectedOfflineNews?.let { article ->
        Dialog(onDismissRequest = { selectedOfflineNews = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_news_reader_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "✓ OFFLINE ARTICLE • ${article.category.uppercase()}",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        IconButton(
                            onClick = { selectedOfflineNews = null },
                            modifier = Modifier.testTag("close_offline_news_dialog_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "${article.source} • ${article.publishedAt}",
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )

                    HorizontalDivider()

                    Text(
                        text = article.content.ifBlank { article.summary },
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Button(
                        onClick = { selectedOfflineNews = null },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(if (isOdia) "ବନ୍ଦ କରନ୍ତୁ" else "Close Offline Reader")
                    }
                }
            }
        }
    }
}
