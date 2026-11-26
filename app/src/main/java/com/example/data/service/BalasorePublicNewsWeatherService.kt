package com.example.data.service

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.CacheMetadataDao
import com.example.data.local.CacheSyncMetadataEntity
import com.example.data.local.DailyNewsWeatherUpdateDao
import com.example.data.local.DailyNewsWeatherUpdateEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.local.NewsDao
import com.example.data.local.WeatherCacheEntity
import com.example.data.local.WeatherDao
import com.example.data.remote.OpenMeteoResponse
import com.example.data.remote.WeatherApiService
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.abs

@JsonClass(generateAdapter = true)
data class DailyPublicNewsItem(
    val id: String,
    val title: String,
    val source: String,
    val category: String,
    val publishedAt: String,
    val url: String
)

data class DailyNewsWeatherFeed(
    val dateKey: String,
    val regionName: String,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val maxTempC: Double,
    val minTempC: Double,
    val humidityPct: Int,
    val windSpeedKmh: Double,
    val precipitationMm: Double,
    val weatherCode: Int,
    val weatherSummary: String,
    val weatherAlertLevel: String,
    val weatherAlertMessage: String,
    val weatherSource: String,
    val newsSource: String,
    val newsItems: List<DailyPublicNewsItem>,
    val fetchedAtMs: Long,
    val isFromOfflineCache: Boolean = false
)

/**
 * Unified service layer for Balasore integrating public Weather (Open-Meteo API) and
 * public Regional News (Google News RSS for Balasore / Chandipur / Baleshwar) APIs.
 *
 * Persists daily updates idempotently in Room ([DailyNewsWeatherUpdateDao]) while also
 * refreshing [WeatherDao] and [NewsDao] so the frontend dashboard stays fast and offline-ready.
 */
class BalasorePublicNewsWeatherService(
    private val dailyUpdateDao: DailyNewsWeatherUpdateDao,
    private val weatherDao: WeatherDao,
    private val newsDao: NewsDao,
    private val cacheMetadataDao: CacheMetadataDao,
    private val weatherApiService: WeatherApiService = WeatherApiService.create(),
    private val httpClient: OkHttpClient = defaultHttpClient()
) {
    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val newsListAdapter = moshi.adapter<List<DailyPublicNewsItem>>(
        Types.newParameterizedType(List::class.java, DailyPublicNewsItem::class.java)
    )

    companion object {
        private const val TAG = "BalasorePublicService"
        const val BALASORE_LAT = 21.4934
        const val BALASORE_LON = 86.9335
        private const val GOOGLE_NEWS_BALASORE_RSS =
            "https://news.google.com/rss/search?q=Balasore+OR+Chandipur+OR+Baleshwar+Odisha&hl=en-IN&gl=IN&ceid=IN:en"
        private const val NEWSAPI_BALASORE_URL =
            "https://newsapi.org/v2/everything?q=(Balasore%20OR%20Baleshwar%20OR%20Chandipur)%20AND%20Odisha&language=en&sortBy=publishedAt&pageSize=8"
        private const val STALE_THRESHOLD_MS = 6 * 60 * 60 * 1000L // 6 hours within the same day
        private const val MAX_STORED_HEADLINES = 8

        fun fromContext(context: Context): BalasorePublicNewsWeatherService {
            val db = AppDatabase.getInstance(context.applicationContext)
            return BalasorePublicNewsWeatherService(
                dailyUpdateDao = db.dailyNewsWeatherUpdateDao(),
                weatherDao = db.weatherDao(),
                newsDao = db.newsDao(),
                cacheMetadataDao = db.cacheMetadataDao()
            )
        }

        private fun defaultHttpClient(): OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        fun todayDateKey(): String {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }
            return sdf.format(Date())
        }

        private fun cutoffDateKey(daysAgo: Int): String {
            val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata"))
            cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }
            return sdf.format(cal.time)
        }
    }

    /**
     * Reactive stream of the latest daily Balasore weather & news snapshot stored in Room.
     */
    fun observeLatestDailyFeed(): Flow<DailyNewsWeatherFeed?> =
        dailyUpdateDao.observeLatestDailyUpdate().map { entity -> entity?.toFeed(isOffline = false) }

    /**
     * Reactive stream of recent daily snapshots (up to 7 days) stored in Room.
     */
    fun observeRecentDailyFeeds(limit: Int = 7): Flow<List<DailyNewsWeatherFeed>> =
        dailyUpdateDao.observeRecentDailyUpdates(limit).map { rows ->
            rows.map { it.toFeed(isOffline = false) }
        }

    /**
     * Fetches fresh public weather (Open-Meteo) and Balasore regional news (Google News RSS),
     * merges them into today's [DailyNewsWeatherUpdateEntity], persists to Room, and returns
     * the live [DailyNewsWeatherFeed]. Falls back cleanly to cached Room data when offline.
     */
    suspend fun fetchAndSyncDailyUpdates(forceRefresh: Boolean = false): Result<DailyNewsWeatherFeed> =
        withContext(Dispatchers.IO) {
            val today = todayDateKey()
            val existingToday = dailyUpdateDao.getDailyUpdateByDate(today)
            val isFreshToday = existingToday != null &&
                (System.currentTimeMillis() - existingToday.fetchedAtMs) < STALE_THRESHOLD_MS

            if (!forceRefresh && isFreshToday && existingToday != null) {
                return@withContext Result.success(existingToday.toFeed(isOffline = false))
            }

            try {
                val (weatherResult, newsResult) = coroutineScope {
                    val weatherDeferred = async { runCatching { fetchPublicOpenMeteoWeather() } }
                    val newsDeferred = async { runCatching { fetchBalasoreNewsWithAutoKey() } }
                    weatherDeferred.await() to newsDeferred.await()
                }

                val liveWeather = weatherResult.getOrNull()
                val newsFetchPair = newsResult.getOrNull()
                val liveNews = newsFetchPair?.first.orEmpty()
                val liveNewsSourceLabel = newsFetchPair?.second ?: "Local Room News Cache"

                if (liveWeather == null && liveNews.isEmpty()) {
                    val cached = existingToday ?: dailyUpdateDao.getRecentDailyUpdatesSync(1).firstOrNull()
                    if (cached != null) {
                        return@withContext Result.success(cached.toFeed(isOffline = true))
                    }
                    // Bootstrap once from local Room weather/news caches if available
                    val bootstrapped = buildFromExistingRoomCaches(today)
                    dailyUpdateDao.upsertDailyUpdate(bootstrapped)
                    return@withContext Result.success(bootstrapped.toFeed(isOffline = true))
                }

                val fallbackWeather = weatherDao.getWeatherCacheSync()
                val tempC = liveWeather?.current?.temperature ?: fallbackWeather?.temperature ?: 29.5
                val feelsLikeC = liveWeather?.current?.apparentTemperature ?: fallbackWeather?.apparentTemperature ?: (tempC + 2.0)
                val maxTempC = liveWeather?.daily?.temperatureMax?.firstOrNull() ?: fallbackWeather?.maxTemp ?: (tempC + 3.5)
                val minTempC = liveWeather?.daily?.temperatureMin?.firstOrNull() ?: fallbackWeather?.minTemp ?: (tempC - 5.0)
                val humidityPct = liveWeather?.current?.relativeHumidity ?: fallbackWeather?.humidity ?: 72
                val windKmh = liveWeather?.current?.windSpeed ?: fallbackWeather?.windSpeed ?: 14.0
                val gustsKmh = liveWeather?.current?.windGusts ?: fallbackWeather?.windGusts ?: (windKmh + 6.0)
                val precipMm = liveWeather?.current?.precipitation ?: 0.0
                val wmoCode = liveWeather?.current?.weatherCode ?: fallbackWeather?.weatherCode ?: 1
                val weatherSummary = describeWmoCode(wmoCode)
                val (alertLevel, alertMessage) = deriveCoastalAlert(tempC, windKmh, gustsKmh, precipMm, wmoCode)

                val effectiveNews = if (liveNews.isNotEmpty()) {
                    liveNews.take(MAX_STORED_HEADLINES)
                } else {
                    existingToday?.let { decodeNewsJson(it.topNewsJson) }
                        ?.takeIf { it.isNotEmpty() }
                        ?: loadNewsFromRoomFallback()
                }

                val nowMs = System.currentTimeMillis()
                val snapshot = DailyNewsWeatherUpdateEntity(
                    dateKey = today,
                    regionName = "Balasore, Odisha",
                    latitude = BALASORE_LAT,
                    longitude = BALASORE_LON,
                    temperatureC = tempC,
                    feelsLikeC = feelsLikeC,
                    maxTempC = maxTempC,
                    minTempC = minTempC,
                    humidityPct = humidityPct,
                    windSpeedKmh = windKmh,
                    precipitationMm = precipMm,
                    weatherCode = wmoCode,
                    weatherSummary = weatherSummary,
                    weatherAlertLevel = alertLevel,
                    weatherAlertMessage = alertMessage,
                    weatherSource = if (liveWeather != null) "Open-Meteo Public API" else "Cached Telemetry",
                    headlineCount = effectiveNews.size,
                    topNewsJson = encodeNewsJson(effectiveNews),
                    newsSource = if (liveNews.isNotEmpty()) liveNewsSourceLabel else "Local Room News Cache",
                    fetchedAtMs = nowMs
                )

                dailyUpdateDao.upsertDailyUpdate(snapshot)
                dailyUpdateDao.pruneOlderThan(cutoffDateKey(30))

                // Keep core WeatherCacheEntity & NewsArticleEntity synced for the rest of the app
                if (liveWeather != null) {
                    val existingCache = weatherDao.getWeatherCacheSync()
                    weatherDao.insertOrUpdateWeather(
                        WeatherCacheEntity(
                            id = 1,
                            temperature = tempC,
                            apparentTemperature = feelsLikeC,
                            weatherCode = wmoCode,
                            weatherDescription = weatherSummary,
                            windSpeed = windKmh,
                            windGusts = gustsKmh,
                            humidity = humidityPct,
                            alertLevel = alertLevel,
                            alertTitle = when (alertLevel) {
                                "CYCLONE_ALERT" -> "Balasore Coastal Gale Alert"
                                "WARNING" -> "Balasore Severe Weather Warning"
                                "ADVISORY" -> "Balasore Coastal Weather Advisory"
                                else -> "Balasore Weather Normal"
                            },
                            alertMessage = alertMessage,
                            tideState = existingCache?.tideState ?: "RECEDING",
                            tideDescription = existingCache?.tideDescription ?: "Check Chandipur Tide Clock before seabed walk.",
                            sunrise = liveWeather.daily?.sunrise?.firstOrNull()?.substringAfter("T") ?: (existingCache?.sunrise ?: "05:52"),
                            sunset = liveWeather.daily?.sunset?.firstOrNull()?.substringAfter("T") ?: (existingCache?.sunset ?: "17:12"),
                            uvIndex = liveWeather.daily?.uvIndexMax?.firstOrNull() ?: (existingCache?.uvIndex ?: 6.0),
                            maxTemp = maxTempC,
                            minTemp = minTempC,
                            lastUpdated = nowMs
                        )
                    )
                }

                if (liveNews.isNotEmpty()) {
                    val bookmarkedIds = runCatching {
                        newsDao.getBookmarkedNewsSync().map { it.id }.toSet()
                    }.getOrDefault(emptySet())
                    val articleEntities = liveNews.take(MAX_STORED_HEADLINES).mapIndexed { idx, item ->
                        val stableId = 900_000L + (abs(item.title.lowercase(Locale.US).hashCode()) % 90_000L)
                        NewsArticleEntity(
                            id = stableId,
                            title = item.title,
                            summary = "${item.source} • ${item.category} update for Balasore district.",
                            content = "${item.title}\n\nSource: ${item.source}\nPublished: ${item.publishedAt}\nLink: ${item.url}",
                            category = item.category,
                            source = item.source,
                            publishedAt = item.publishedAt,
                            timestamp = nowMs - idx * 60_000L,
                            isBreaking = idx == 0 && (item.category == "Weather" || item.title.contains("Alert", ignoreCase = true)),
                            isBookmarked = bookmarkedIds.contains(stableId)
                        )
                    }
                    newsDao.insertArticles(articleEntities)
                }

                cacheMetadataDao.insertOrUpdate(
                    CacheSyncMetadataEntity(
                        cacheKey = "DAILY_PUBLIC_FEED",
                        lastSyncedAt = nowMs,
                        itemCount = effectiveNews.size + 1,
                        status = if (liveWeather != null || liveNews.isNotEmpty()) "FRESH" else "CACHED",
                        cacheLabel = "Public News & Weather APIs",
                        details = "${snapshot.weatherSource} • ${snapshot.newsSource}"
                    )
                )

                Result.success(snapshot.toFeed(isOffline = liveWeather == null && liveNews.isEmpty()))
            } catch (e: Exception) {
                Log.w(TAG, "Public API sync notice: ${e.message}")
                val fallback = existingToday ?: dailyUpdateDao.getRecentDailyUpdatesSync(1).firstOrNull()
                if (fallback != null) {
                    Result.success(fallback.toFeed(isOffline = true))
                } else {
                    val bootstrapped = buildFromExistingRoomCaches(today)
                    dailyUpdateDao.upsertDailyUpdate(bootstrapped)
                    Result.success(bootstrapped.toFeed(isOffline = true))
                }
            }
        }

    private suspend fun fetchPublicOpenMeteoWeather(): OpenMeteoResponse {
        return weatherApiService.getBalasoreForecast(
            latitude = BALASORE_LAT,
            longitude = BALASORE_LON
        )
    }

    /**
     * Automatically uses NewsAPI when `NEWS_API_KEY` is configured via AI Studio Secrets
     * (injected into `BuildConfig.NEWS_API_KEY`), and merges or falls back seamlessly to
     * keyless Google News Public RSS so Balasore headlines always load automatically.
     */
    private fun fetchBalasoreNewsWithAutoKey(): Pair<List<DailyPublicNewsItem>, String> {
        val newsApiKey = com.example.data.config.BalasoreApiKeys.newsApiKey()
        val apiItems = if (newsApiKey.isNotBlank()) {
            runCatching { fetchNewsApiArticles(newsApiKey) }.getOrDefault(emptyList())
        } else {
            emptyList()
        }
        val rssItems = runCatching { fetchPublicBalasoreNewsRss() }.getOrDefault(emptyList())

        val seenTitles = mutableSetOf<String>()
        val merged = (apiItems + rssItems).filter { item ->
            val key = item.title.lowercase(Locale.US).replace(Regex("[^a-z0-9]"), "")
            key.isNotBlank() && seenTitles.add(key)
        }.take(MAX_STORED_HEADLINES)

        val sourceLabel = when {
            apiItems.isNotEmpty() && rssItems.isNotEmpty() -> "NewsAPI + Google News RSS"
            apiItems.isNotEmpty() -> "NewsAPI (Auto-Configured)"
            rssItems.isNotEmpty() -> "Google News Public RSS"
            else -> "Local Room News Cache"
        }
        return merged to sourceLabel
    }

    private fun fetchNewsApiArticles(apiKey: String): List<DailyPublicNewsItem> {
        val request = Request.Builder()
            .url(NEWSAPI_BALASORE_URL)
            .header("X-Api-Key", apiKey)
            .header("User-Agent", "Baleshwar360-Android/1.0")
            .header("Accept", "application/json")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("NewsAPI returned HTTP ${response.code}")
            }
            val body = response.body?.string().orEmpty()
            if (body.isBlank()) return emptyList()
            val root = org.json.JSONObject(body)
            val articles = root.optJSONArray("articles") ?: return emptyList()
            val out = mutableListOf<DailyPublicNewsItem>()
            for (i in 0 until articles.length()) {
                val obj = articles.optJSONObject(i) ?: continue
                val title = obj.optString("title").trim()
                if (title.isBlank() || title.equals("[Removed]", ignoreCase = true)) continue
                val sourceName = obj.optJSONObject("source")?.optString("name")?.trim()
                    ?.ifBlank { "NewsAPI" } ?: "NewsAPI"
                val url = obj.optString("url").trim()
                val publishedAt = obj.optString("publishedAt").trim().take(10).ifBlank { "Today" }
                val safeUrl = url.takeIf {
                    it.startsWith("https://", ignoreCase = true) || it.startsWith("http://", ignoreCase = true)
                } ?: continue
                out.add(
                    DailyPublicNewsItem(
                        id = "newsapi_${abs(title.hashCode())}",
                        title = title.take(180),
                        source = sourceName.take(60),
                        category = classifyNewsCategory(title),
                        publishedAt = publishedAt,
                        url = safeUrl
                    )
                )
                if (out.size >= MAX_STORED_HEADLINES) break
            }
            return out
        }
    }

    private fun fetchPublicBalasoreNewsRss(): List<DailyPublicNewsItem> {
        val request = Request.Builder()
            .url(GOOGLE_NEWS_BALASORE_RSS)
            .header("User-Agent", "Baleshwar360-Android/1.0")
            .header("Accept", "application/rss+xml, application/xml, text/xml")
            .get()
            .build()

        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IllegalStateException("News RSS returned HTTP ${response.code}")
            }
            val xml = response.body?.string().orEmpty()
            if (xml.isBlank()) return emptyList()
            return parseRssItemsSafely(xml)
        }
    }

    private fun parseRssItemsSafely(xml: String): List<DailyPublicNewsItem> {
        val items = mutableListOf<DailyPublicNewsItem>()
        val factory = XmlPullParserFactory.newInstance().apply {
            isNamespaceAware = false
            runCatching { setFeature("http://xml.org/sax/features/external-general-entities", false) }
            runCatching { setFeature("http://xml.org/sax/features/external-parameter-entities", false) }
        }
        val parser = factory.newPullParser()
        parser.setInput(StringReader(xml))

        var insideItem = false
        var currentTitle = ""
        var currentLink = ""
        var currentPubDate = ""
        var currentSource = ""

        var eventType = parser.eventType
        while (eventType != XmlPullParser.END_DOCUMENT && items.size < MAX_STORED_HEADLINES) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name.lowercase(Locale.US)) {
                        "item" -> {
                            insideItem = true
                            currentTitle = ""
                            currentLink = ""
                            currentPubDate = ""
                            currentSource = ""
                        }
                        "title" -> if (insideItem) currentTitle = readNextTextSafe(parser)
                        "link" -> if (insideItem) currentLink = readNextTextSafe(parser)
                        "pubdate" -> if (insideItem) currentPubDate = readNextTextSafe(parser)
                        "source" -> if (insideItem) currentSource = readNextTextSafe(parser)
                    }
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name.equals("item", ignoreCase = true) && insideItem) {
                        insideItem = false
                        val sanitizedTitle = sanitizePlainText(currentTitle).substringBeforeLast(" - ").trim()
                        val safeUrl = currentLink.trim().takeIf {
                            it.startsWith("https://", ignoreCase = true) || it.startsWith("http://", ignoreCase = true)
                        }
                        if (sanitizedTitle.isNotBlank() && safeUrl != null) {
                            val resolvedSource = sanitizePlainText(currentSource).ifBlank { "Balasore Regional Desk" }
                            items.add(
                                DailyPublicNewsItem(
                                    id = "pub_${abs(sanitizedTitle.hashCode())}",
                                    title = sanitizedTitle.take(180),
                                    source = resolvedSource.take(60),
                                    category = classifyNewsCategory(sanitizedTitle),
                                    publishedAt = formatRssDate(currentPubDate),
                                    url = safeUrl
                                )
                            )
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        return items
    }

    private fun readNextTextSafe(parser: XmlPullParser): String {
        return runCatching {
            if (parser.next() == XmlPullParser.TEXT) parser.text.orEmpty() else ""
        }.getOrDefault("")
    }

    private fun sanitizePlainText(raw: String): String {
        return raw.replace(Regex("<[^>]*>"), " ")
            .replace("&amp;", "&")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace(Regex("\\s+"), " ")
            .trim()
    }

    private fun formatRssDate(rawDate: String): String {
        if (rawDate.isBlank()) return "Today"
        return runCatching {
            val inputFmt = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH)
            val parsed = inputFmt.parse(rawDate.trim()) ?: return "Today"
            val outFmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("Asia/Kolkata")
            }
            outFmt.format(parsed)
        }.getOrDefault("Today")
    }

    private fun classifyNewsCategory(title: String): String {
        val lower = title.lowercase(Locale.US)
        return when {
            listOf("cyclone", "rain", "imd", "flood", "weather", "tide", "subarnarekha", "heat").any { lower.contains(it) } -> "Weather"
            listOf("drdo", "missile", "itr", "defense", "defence").any { lower.contains(it) } -> "Defense"
            listOf("beach", "chandipur", "talasari", "kuldiha", "tourism", "temple", "remuna").any { lower.contains(it) } -> "Tourism"
            listOf("train", "railway", "bus", "nh-16", "hospital", "school", "police", "civic").any { lower.contains(it) } -> "Civic"
            else -> "Local"
        }
    }

    private fun describeWmoCode(code: Int): String = when (code) {
        0 -> "Clear Coastal Sky"
        1, 2 -> "Partly Cloudy"
        3 -> "Overcast Sky"
        45, 48 -> "Coastal Fog & Mist"
        51, 53, 55 -> "Light Drizzle"
        61, 63 -> "Moderate Rain"
        65, 80, 81, 82 -> "Heavy Rain Showers"
        95, 96, 99 -> "Thunderstorm & Squall"
        else -> "Warm & Humid"
    }

    private fun deriveCoastalAlert(
        tempC: Double,
        windKmh: Double,
        gustsKmh: Double,
        precipMm: Double,
        wmoCode: Int
    ): Pair<String, String> = when {
        windKmh >= 55.0 || gustsKmh >= 70.0 -> "CYCLONE_ALERT" to "High coastal winds (${windKmh.toInt()} km/h) along Chandipur & Balaramgadi coast."
        wmoCode >= 95 || precipMm >= 15.0 -> "WARNING" to "Heavy rain / thunderstorm cell detected over Balasore district."
        tempC >= 39.0 -> "ADVISORY" to "Elevated coastal heat (${tempC.toInt()}°C); stay hydrated during midday."
        windKmh >= 30.0 -> "ADVISORY" to "Breezy Bay of Bengal conditions; follow Chandipur low-tide advisories."
        else -> "NORMAL" to "Favorable coastal weather across Balasore, Chandipur & Remuna."
    }

    private suspend fun loadNewsFromRoomFallback(): List<DailyPublicNewsItem> {
        return newsDao.getAllArticlesSync()
            .sortedByDescending { it.timestamp }
            .take(5)
            .map { art ->
                DailyPublicNewsItem(
                    id = "room_${art.id}",
                    title = art.title,
                    source = art.source,
                    category = art.category,
                    publishedAt = art.publishedAt,
                    url = ""
                )
            }
    }

    private suspend fun buildFromExistingRoomCaches(dateKey: String): DailyNewsWeatherUpdateEntity {
        val weather = weatherDao.getWeatherCacheSync()
        val roomNews = loadNewsFromRoomFallback()
        return DailyNewsWeatherUpdateEntity(
            dateKey = dateKey,
            regionName = "Balasore, Odisha",
            temperatureC = weather?.temperature ?: 29.0,
            feelsLikeC = weather?.apparentTemperature ?: 31.0,
            maxTempC = weather?.maxTemp ?: 32.5,
            minTempC = weather?.minTemp ?: 24.0,
            humidityPct = weather?.humidity ?: 74,
            windSpeedKmh = weather?.windSpeed ?: 13.5,
            precipitationMm = 0.0,
            weatherCode = weather?.weatherCode ?: 1,
            weatherSummary = weather?.weatherDescription ?: "Partly Cloudy",
            weatherAlertLevel = weather?.alertLevel ?: "NORMAL",
            weatherAlertMessage = weather?.alertMessage ?: "Cached coastal telemetry active.",
            weatherSource = "Local Room Weather Cache",
            headlineCount = roomNews.size,
            topNewsJson = encodeNewsJson(roomNews),
            newsSource = "Local Room News Cache",
            fetchedAtMs = weather?.lastUpdated ?: System.currentTimeMillis()
        )
    }

    private fun encodeNewsJson(items: List<DailyPublicNewsItem>): String {
        return runCatching { newsListAdapter.toJson(items) }.getOrDefault("[]")
    }

    private fun decodeNewsJson(rawJson: String): List<DailyPublicNewsItem> {
        if (rawJson.isBlank()) return emptyList()
        return runCatching { newsListAdapter.fromJson(rawJson).orEmpty() }.getOrDefault(emptyList())
    }

    private fun DailyNewsWeatherUpdateEntity.toFeed(isOffline: Boolean): DailyNewsWeatherFeed {
        return DailyNewsWeatherFeed(
            dateKey = dateKey,
            regionName = regionName,
            temperatureC = temperatureC,
            feelsLikeC = feelsLikeC,
            maxTempC = maxTempC,
            minTempC = minTempC,
            humidityPct = humidityPct,
            windSpeedKmh = windSpeedKmh,
            precipitationMm = precipitationMm,
            weatherCode = weatherCode,
            weatherSummary = weatherSummary,
            weatherAlertLevel = weatherAlertLevel,
            weatherAlertMessage = weatherAlertMessage,
            weatherSource = weatherSource,
            newsSource = newsSource,
            newsItems = decodeNewsJson(topNewsJson),
            fetchedAtMs = fetchedAtMs,
            isFromOfflineCache = isOffline
        )
    }
}
