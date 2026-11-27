package com.example.data.repository

import android.util.Log
import com.example.data.config.BalasoreApiKeys
import com.example.data.local.NewsArticleEntity
import com.example.data.model.NewsArticle
import com.example.data.remote.NewsApiOrgArticleDto
import com.example.data.remote.NewsApiOrgService
import com.example.data.service.DailyPublicNewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.abs

/**
 * Automatic NewsAPI v2 client powered by `BuildConfig.NEWS_API_KEY` (configured via AI Studio Secrets).
 *
 * When `NEWS_API_KEY` is configured, fetches live Balasore/Odisha local news, India national
 * headlines, and international stories with thumbnails and full descriptions. When unconfigured
 * or rate-limited, returns an empty result gracefully so keyless Google News RSS and Room cache
 * continue uninterrupted.
 */
class NewsApiOrgClient(
    private val service: NewsApiOrgService = NewsApiOrgService.create(),
    private val apiKeyProvider: () -> String = { BalasoreApiKeys.newsApiKey() }
) {
    companion object {
        private const val TAG = "NewsApiOrgClient"
        const val QUERY_BALASORE_LOCAL = "(Balasore OR Baleshwar OR Chandipur OR Odisha)"
        const val QUERY_INDIA_NATIONAL = "(India AND (ISRO OR Railways OR Odisha OR IMD OR Infrastructure))"
        const val QUERY_WORLD_GLOBAL = "(Ocean OR Climate OR Science OR Technology)"
        private const val NEWSAPI_ENTITY_ID_BASE = 820_000L
        private const val NEWSAPI_ENTITY_ID_MOD = 70_000L

        fun sanitizeSafeHttpUrl(raw: String?): String? {
            val trimmed = raw?.trim().orEmpty()
            return trimmed.takeIf {
                it.startsWith("https://", ignoreCase = true) ||
                    it.startsWith("http://", ignoreCase = true)
            }
        }

        fun formatPublishedAt(rawIso: String?): String {
            val clean = rawIso?.trim().orEmpty()
            if (clean.isBlank()) return "Today"
            val patterns = listOf(
                "yyyy-MM-dd'T'HH:mm:ss'Z'",
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
                "yyyy-MM-dd'T'HH:mm:ssXXX"
            )
            for (pattern in patterns) {
                val parsed = runCatching {
                    SimpleDateFormat(pattern, Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("UTC")
                    }.parse(clean)
                }.getOrNull()
                if (parsed != null) {
                    val outFmt = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).apply {
                        timeZone = TimeZone.getTimeZone("Asia/Kolkata")
                    }
                    return outFmt.format(parsed)
                }
            }
            return clean.take(10).ifBlank { "Today" }
        }

        fun classifyCategory(title: String, fallbackSection: String = "Local"): String {
            val lower = title.lowercase(Locale.US)
            return when {
                fallbackSection.equals("National News", ignoreCase = true) -> "National News"
                fallbackSection.equals("International News", ignoreCase = true) -> "International News"
                listOf("cyclone", "rain", "imd", "flood", "weather", "tide", "subarnarekha", "heat").any { lower.contains(it) } -> "Weather"
                listOf("drdo", "missile", "itr", "defense", "defence").any { lower.contains(it) } -> "Defense"
                listOf("beach", "chandipur", "talasari", "kuldiha", "tourism", "temple", "remuna").any { lower.contains(it) } -> "Tourism"
                listOf("hospital", "aiims", "medical", "health", "dengue", "vaccine").any { lower.contains(it) } -> "Health"
                listOf("train", "railway", "bus", "nh-16", "school", "police", "civic", "municipality").any { lower.contains(it) } -> "Civic"
                else -> fallbackSection
            }
        }

        fun computeStableArticleId(title: String): Long {
            val normalized = title.trim().lowercase(Locale.US)
            return NEWSAPI_ENTITY_ID_BASE + (abs(normalized.hashCode().toLong()) % NEWSAPI_ENTITY_ID_MOD)
        }

        fun mapToDailyPublicNewsItem(dto: NewsApiOrgArticleDto): DailyPublicNewsItem? {
            val title = dto.title?.trim().orEmpty()
            if (title.isBlank() || title.equals("[Removed]", ignoreCase = true)) return null
            val safeUrl = sanitizeSafeHttpUrl(dto.url) ?: return null
            val sourceName = dto.source?.name?.trim()?.ifBlank { "NewsAPI" } ?: "NewsAPI"
            return DailyPublicNewsItem(
                id = "newsapi_${abs(title.hashCode())}",
                title = title.take(180),
                source = sourceName.take(60),
                category = classifyCategory(title, fallbackSection = "Local"),
                publishedAt = formatPublishedAt(dto.publishedAt),
                url = safeUrl
            )
        }

        fun mapToNewsArticle(
            dto: NewsApiOrgArticleDto,
            sectionCategory: String
        ): NewsArticle? {
            val title = dto.title?.trim().orEmpty()
            if (title.length < 8 || title.equals("[Removed]", ignoreCase = true)) return null
            val safeUrl = sanitizeSafeHttpUrl(dto.url) ?: return null
            val sourceName = dto.source?.name?.trim()?.ifBlank { "NewsAPI" } ?: "NewsAPI"
            val description = dto.description?.trim().orEmpty()
                .replace(Regex("<[^>]*>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            val cleanedBody = dto.content?.trim().orEmpty()
                .replace(Regex("\\[\\+\\d+ chars]"), "")
                .replace(Regex("<[^>]*>"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            val snippet = description.ifBlank {
                cleanedBody.ifBlank { "Live report from $sourceName via NewsAPI." }
            }.take(220)
            val fullContent = buildString {
                if (cleanedBody.isNotBlank()) {
                    append(cleanedBody)
                } else {
                    append(snippet)
                }
                val author = dto.author?.trim().orEmpty()
                if (author.isNotBlank() && !author.startsWith("http", ignoreCase = true)) {
                    append("\n\nAuthor: ")
                    append(author.take(80))
                }
                append("\nSource: ")
                append(sourceName.take(60))
                append("\nRead full article: ")
                append(safeUrl)
            }
            val stableId = computeStableArticleId(title).toString()
            val formattedTime = formatPublishedAt(dto.publishedAt)
            return NewsArticle(
                id = stableId,
                title = title.take(180),
                odiaTitle = title.take(180),
                snippet = snippet,
                odiaSnippet = snippet,
                category = classifyCategory(title, fallbackSection = sectionCategory),
                timeAgo = "NewsAPI • $formattedTime",
                source = "$sourceName (NewsAPI Live)",
                isBookmarked = false,
                content = fullContent,
                odiaContent = fullContent
            )
        }

        fun mapToNewsArticleEntity(
            dto: NewsApiOrgArticleDto,
            sectionCategory: String,
            index: Int,
            nowMs: Long,
            bookmarkedIds: Set<Long>,
            bookmarkedTitles: Set<String>
        ): NewsArticleEntity? {
            val mapped = mapToNewsArticle(dto, sectionCategory) ?: return null
            val id = mapped.id.toLongOrNull() ?: computeStableArticleId(mapped.title)
            val isBookmarked = bookmarkedIds.contains(id) ||
                bookmarkedTitles.contains(mapped.title.trim().lowercase(Locale.US))
            val safeImage = sanitizeSafeHttpUrl(dto.urlToImage)
            val isBreaking = index == 0 && (
                mapped.category.equals("Weather", ignoreCase = true) ||
                    mapped.title.contains("Alert", ignoreCase = true) ||
                    mapped.title.contains("Cyclone", ignoreCase = true) ||
                    mapped.title.contains("Breaking", ignoreCase = true)
                )
            return NewsArticleEntity(
                id = id,
                title = mapped.title,
                summary = mapped.snippet,
                content = mapped.content,
                category = mapped.category,
                source = mapped.source,
                publishedAt = mapped.timeAgo,
                timestamp = nowMs - (index * 45_000L),
                isBreaking = isBreaking,
                imageUrl = safeImage,
                isBookmarked = isBookmarked
            )
        }
    }

    fun resolveApiKey(explicitKey: String? = null): String {
        val explicit = BalasoreApiKeys.sanitize(explicitKey)
        if (explicit.isNotBlank()) return explicit
        return BalasoreApiKeys.sanitize(apiKeyProvider())
    }

    fun isConfigured(explicitKey: String? = null): Boolean = resolveApiKey(explicitKey).isNotBlank()

    /**
     * Fetches Balasore/Odisha local headlines for the Dashboard Daily Public Feed card.
     */
    suspend fun fetchBalasoreDailyNews(
        apiKeyOverride: String? = null,
        pageSize: Int = 8
    ): Result<List<DailyPublicNewsItem>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(apiKeyOverride)
        if (apiKey.isBlank()) return@withContext Result.success(emptyList())

        runCatching {
            val response = service.searchEverything(
                query = QUERY_BALASORE_LOCAL,
                language = "en",
                sortBy = "publishedAt",
                pageSize = pageSize.coerceIn(1, 20),
                apiKey = apiKey
            )
            if (response.status != null && !response.status.equals("ok", ignoreCase = true)) {
                throw IllegalStateException("NewsAPI error: ${response.code ?: response.message ?: response.status}")
            }
            response.articles
                .mapNotNull(::mapToDailyPublicNewsItem)
                .distinctBy { it.title.lowercase(Locale.US) }
                .take(pageSize)
        }.onFailure { err ->
            Log.w(TAG, "NewsAPI daily fetch notice: ${err.message}")
        }
    }

    /**
     * Fetches live Local, National, and International news articles from NewsAPI in parallel
     * for [com.example.data.sync.HourlyMultiSourceNewsSyndicator].
     */
    suspend fun fetchMultiSectionNews(
        apiKeyOverride: String? = null,
        nowMs: Long = System.currentTimeMillis()
    ): Result<Pair<List<NewsArticle>, Map<String, String>>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(apiKeyOverride)
        if (apiKey.isBlank()) return@withContext Result.success(emptyList<NewsArticle>() to emptyMap())

        runCatching {
            val (localDtos, nationalDtos, internationalDtos) = coroutineScope {
                val localJob = async {
                    runCatching {
                        service.searchEverything(
                            query = QUERY_BALASORE_LOCAL,
                            language = "en",
                            sortBy = "publishedAt",
                            pageSize = 6,
                            apiKey = apiKey
                        ).articles
                    }.getOrDefault(emptyList())
                }
                val nationalJob = async {
                    runCatching {
                        val top = service.getTopHeadlines(
                            country = "in",
                            pageSize = 5,
                            apiKey = apiKey
                        ).articles
                        if (top.isNotEmpty()) top else {
                            service.searchEverything(
                                query = QUERY_INDIA_NATIONAL,
                                language = "en",
                                sortBy = "publishedAt",
                                pageSize = 5,
                                apiKey = apiKey
                            ).articles
                        }
                    }.getOrDefault(emptyList())
                }
                val internationalJob = async {
                    runCatching {
                        service.searchEverything(
                            query = QUERY_WORLD_GLOBAL,
                            language = "en",
                            sortBy = "publishedAt",
                            pageSize = 5,
                            apiKey = apiKey
                        ).articles
                    }.getOrDefault(emptyList())
                }
                Triple(localJob.await(), nationalJob.await(), internationalJob.await())
            }

            val imageUrlsById = mutableMapOf<String, String>()
            val allArticles = mutableListOf<NewsArticle>()

            fun collectSection(dtos: List<NewsApiOrgArticleDto>, section: String, limit: Int) {
                var added = 0
                for (dto in dtos) {
                    val mapped = mapToNewsArticle(dto, section) ?: continue
                    sanitizeSafeHttpUrl(dto.urlToImage)?.let { imageUrlsById[mapped.id] = it }
                    allArticles.add(mapped)
                    added++
                    if (added >= limit) break
                }
            }

            collectSection(localDtos, "Local", 6)
            collectSection(nationalDtos, "National News", 5)
            collectSection(internationalDtos, "International News", 5)

            val deduped = allArticles.distinctBy { it.title.trim().lowercase(Locale.US) }
            deduped to imageUrlsById
        }.onFailure { err ->
            Log.w(TAG, "NewsAPI multi-section fetch notice: ${err.message}")
        }
    }

    /**
     * Fetches Room-ready [NewsArticleEntity] rows directly from NewsAPI while preserving user bookmarks.
     */
    suspend fun fetchNewsArticleEntities(
        bookmarkedIds: Set<Long> = emptySet(),
        bookmarkedTitles: Set<String> = emptySet(),
        apiKeyOverride: String? = null,
        nowMs: Long = System.currentTimeMillis()
    ): Result<List<NewsArticleEntity>> = withContext(Dispatchers.IO) {
        val apiKey = resolveApiKey(apiKeyOverride)
        if (apiKey.isBlank()) return@withContext Result.success(emptyList())

        runCatching {
            val response = service.searchEverything(
                query = QUERY_BALASORE_LOCAL,
                language = "en",
                sortBy = "publishedAt",
                pageSize = 10,
                apiKey = apiKey
            )
            if (response.status != null && !response.status.equals("ok", ignoreCase = true)) {
                throw IllegalStateException("NewsAPI error: ${response.code ?: response.message ?: response.status}")
            }
            response.articles.mapIndexedNotNull { idx, dto ->
                mapToNewsArticleEntity(
                    dto = dto,
                    sectionCategory = "Local",
                    index = idx,
                    nowMs = nowMs,
                    bookmarkedIds = bookmarkedIds,
                    bookmarkedTitles = bookmarkedTitles
                )
            }.distinctBy { it.id }
        }.onFailure { err ->
            Log.w(TAG, "NewsAPI entity fetch notice: ${err.message}")
        }
    }
}
