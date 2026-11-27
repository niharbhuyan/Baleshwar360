package com.example.data.repository

import com.example.data.local.CacheMetadataDao
import com.example.data.local.CacheSyncMetadataEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.local.NewsDao
import com.example.data.remote.BalasoreApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Repository interface defining news data operations.
 */
interface INewsRepository {
    val allNews: Flow<List<NewsArticleEntity>>
    val breakingNews: Flow<List<NewsArticleEntity>>
    fun getUnifiedNewsFlow(forceRefresh: Boolean = false): Flow<Resource<List<NewsArticleEntity>>>
    fun getNewsByCategory(category: String): Flow<List<NewsArticleEntity>>
    fun getTrendingNews(): Flow<List<NewsArticleEntity>>
    fun getNewsByCategoryAndSearch(category: String, query: String): Flow<List<NewsArticleEntity>>
    fun getBookmarkedNews(): Flow<List<NewsArticleEntity>>
    fun searchNews(query: String): Flow<List<NewsArticleEntity>>
    fun getArticleById(id: Long): Flow<NewsArticleEntity?>
    suspend fun isCacheOlderThan24Hours(): Boolean
    suspend fun refreshNews(): Result<Int>
    suspend fun toggleBookmark(articleId: Long, currentBookmarked: Boolean)
    suspend fun insertArticle(article: NewsArticleEntity): Long
    suspend fun updateArticle(article: NewsArticleEntity): Int
    suspend fun deleteArticle(article: NewsArticleEntity): Int
    suspend fun deleteArticleById(id: Long): Int
    suspend fun refreshTrendingFromFirestore(
        firestoreService: com.example.data.firebase.FirestoreService?
    ): Result<Int> = Result.success(0)
}

/**
 * Repository implementing offline-first caching for local Balasore district news.
 * Abstracts the Room [NewsDao] and Retrofit [BalasoreApiService].
 */
class NewsRepository(
    private val newsDao: NewsDao,
    private val cacheMetadataDao: CacheMetadataDao? = null,
    private val apiService: BalasoreApiService = BalasoreApiService.create(),
    private val newsApiOrgClient: NewsApiOrgClient = NewsApiOrgClient()
) : INewsRepository {

    override val allNews: Flow<List<NewsArticleEntity>> = newsDao.getAllNews().distinctUntilChanged()

    override val breakingNews: Flow<List<NewsArticleEntity>> = newsDao.getBreakingNews().distinctUntilChanged()

    companion object {
        const val CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000L // 24 Hours
    }

    override suspend fun refreshTrendingFromFirestore(
        firestoreService: com.example.data.firebase.FirestoreService?
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val cloudTrending = firestoreService?.fetchLatestTrendingFromFirestore().orEmpty()
            if (cloudTrending.isNotEmpty()) {
                val bookmarkedIds = runCatching { newsDao.getBookmarkedNewsSync().map { it.id }.toSet() }.getOrDefault(emptySet())
                val entities = cloudTrending.mapIndexed { idx, art ->
                    val numericId = art.id.toLongOrNull() ?: (7900L + idx)
                    NewsArticleEntity(
                        id = numericId,
                        title = art.title,
                        summary = art.snippet,
                        content = art.content.ifBlank { art.snippet },
                        category = art.category,
                        source = art.source,
                        publishedAt = art.timeAgo,
                        timestamp = System.currentTimeMillis() - idx * 15_000L,
                        isBreaking = idx == 0,
                        isBookmarked = bookmarkedIds.contains(numericId)
                    )
                }
                newsDao.insertArticles(entities)
            }
            Result.success(com.example.data.firebase.FirestoreService.trendingViewCountsFlow.value.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isCacheOlderThan24Hours(): Boolean = withContext(Dispatchers.IO) {
        val latestTimestamp = newsDao.getLatestTimestamp() ?: 0L
        val metadataTime = cacheMetadataDao?.getMetadataSync("NEWS")?.lastSyncedAt ?: 0L
        val effectiveTime = maxOf(latestTimestamp, metadataTime)
        if (effectiveTime <= 0L) return@withContext true
        (System.currentTimeMillis() - effectiveTime) > CACHE_EXPIRATION_MS
    }

    /**
     * Unified Flow combining local Room cache with remote network fetching.
     * 1. Emits [Resource.Loading] with cached Room data immediately for offline responsiveness.
     * 2. If [forceRefresh], if cache is empty, OR if cache is older than 24 hours, automatically queries the network.
     * 3. On success, persists to Room and emits [Resource.Success].
     * 4. On network failure, smoothly emits [Resource.Success] with cached data if available,
     *    or [Resource.Error] with fallback cache so the UI never breaks.
     */
    override fun getUnifiedNewsFlow(forceRefresh: Boolean): Flow<Resource<List<NewsArticleEntity>>> = flow {
        // Step 1: Read local Room cache synchronously
        val hasLocalArticles = newsDao.getCount() > 0
        val isStale = isCacheOlderThan24Hours()

        emit(Resource.Loading())

        if (forceRefresh || !hasLocalArticles || isStale) {
            try {
                val response = apiService.getBalasoreNews()
                val dtos = response.articles.orEmpty()

                val bookmarkedArticles = newsDao.getBookmarkedNewsSync()
                val bookmarkedIds = bookmarkedArticles.map { it.id }.toSet()
                val bookmarkedTitles = bookmarkedArticles.map { it.title.trim().lowercase() }.toSet()

                val apiEntities = dtos.map { dto ->
                    val isBookmarked = (dto.id != null && bookmarkedIds.contains(dto.id)) ||
                            (dto.title != null && bookmarkedTitles.contains(dto.title.trim().lowercase()))
                    dto.toEntity(isBookmarked = isBookmarked)
                }

                // Pull Local, National & International news from external sources and publish directly without approval
                val syndicatedEntities = runCatching {
                    com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullAndAutoPublishAllSections(
                        context = null,
                        forceNetworkRss = true
                    )
                    com.example.data.sync.HourlyMultiSourceNewsSyndicator.pulledArticlesFlow.value.mapIndexed { idx, art ->
                        val numericId = art.id.toLongOrNull() ?: (7100L + idx)
                        val isBookmarked = bookmarkedIds.contains(numericId) ||
                            bookmarkedTitles.contains(art.title.trim().lowercase())
                        NewsArticleEntity(
                            id = numericId,
                            title = art.title,
                            summary = art.snippet,
                            content = art.content.ifBlank { art.snippet },
                            category = art.category,
                            source = art.source,
                            publishedAt = art.timeAgo,
                            timestamp = System.currentTimeMillis() - idx * 30_000L,
                            isBreaking = idx < 3,
                            isBookmarked = isBookmarked
                        )
                    }
                }.getOrDefault(emptyList())

                // Automatically fetch live NewsAPI articles when NEWS_API_KEY is configured via AI Studio Secrets
                val newsApiEntities = if (newsApiOrgClient.isConfigured()) {
                    newsApiOrgClient.fetchNewsArticleEntities(
                        bookmarkedIds = bookmarkedIds,
                        bookmarkedTitles = bookmarkedTitles
                    ).getOrDefault(emptyList())
                } else {
                    emptyList()
                }

                val freshEntities = (newsApiEntities + syndicatedEntities + apiEntities).distinctBy { it.id }

                if (freshEntities.isNotEmpty()) {
                    newsDao.insertArticles(freshEntities)
                }

                val sourceDetail = if (newsApiEntities.isNotEmpty()) {
                    "Auto-pulled via NewsAPI (NEWS_API_KEY) + Local, National & International feeds into Room."
                } else {
                    "Auto-pulled Local, National & International feeds directly into Room."
                }

                cacheMetadataDao?.insertOrUpdate(
                    CacheSyncMetadataEntity(
                        cacheKey = "NEWS",
                        lastSyncedAt = System.currentTimeMillis(),
                        itemCount = newsDao.getCount(),
                        status = "FRESH",
                        cacheLabel = "${newsDao.getCount()} Multi-Source News Articles",
                        details = sourceDetail
                    )
                )

                // Step 2: Emit success with fresh data from Room
                emit(Resource.Success(data = freshEntities, isOfflineCached = false))
            } catch (e: Exception) {
                // Fallback to multi-source syndicator + local Room cache
                runCatching {
                    val fallbackSyndicated = com.example.data.sync.HourlyMultiSourceNewsSyndicator.pulledArticlesFlow.value.mapIndexed { idx, art ->
                        NewsArticleEntity(
                            id = art.id.toLongOrNull() ?: (7100L + idx),
                            title = art.title,
                            summary = art.snippet,
                            content = art.content.ifBlank { art.snippet },
                            category = art.category,
                            source = art.source,
                            publishedAt = art.timeAgo,
                            timestamp = System.currentTimeMillis() - idx * 30_000L,
                            isBreaking = idx < 3,
                            isBookmarked = false
                        )
                    }
                    if (fallbackSyndicated.isNotEmpty()) {
                        newsDao.insertArticles(fallbackSyndicated)
                    }
                }
                cacheMetadataDao?.insertOrUpdate(
                    CacheSyncMetadataEntity(
                        cacheKey = "NEWS",
                        lastSyncedAt = System.currentTimeMillis(),
                        itemCount = newsDao.getCount(),
                        status = "OFFLINE",
                        cacheLabel = "Offline Room News Cache",
                        details = "Serving local cached news articles without active network."
                    )
                )
                emit(Resource.Success(data = emptyList(), isOfflineCached = true, message = "Serving from offline Room cache"))
            }
        } else {
            emit(Resource.Success(data = emptyList(), isOfflineCached = true))
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Reactive 'Trending' news stream combining Room articles with Firestore view counts ('news_article_views'),
     * sorted descending by view count to populate the 'Trending' tab with the most popular stories.
     */
    override fun getTrendingNews(): Flow<List<NewsArticleEntity>> {
        return kotlinx.coroutines.flow.combine(
            newsDao.getAllNews(),
            com.example.data.firebase.FirestoreService.trendingViewCountsFlow
        ) { articles, viewCounts ->
            articles.sortedByDescending { entity ->
                com.example.data.firebase.FirestoreService.getViewCountForId(
                    entity.id.toString(),
                    viewCounts
                )
            }
        }
    }

    override fun getNewsByCategory(category: String): Flow<List<NewsArticleEntity>> {
        val trimmed = category.trim()
        return when {
            trimmed.isEmpty() || trimmed.equals("All", ignoreCase = true) || trimmed.equals("All Stories", ignoreCase = true) ->
                newsDao.getAllNews()
            trimmed.equals("Trending", ignoreCase = true) || trimmed.equals("Trending Stories", ignoreCase = true) ->
                getTrendingNews()
            trimmed.equals("Saved", ignoreCase = true) || trimmed.equals("Saved Bookmarks", ignoreCase = true) || trimmed.equals("My Saved", ignoreCase = true) ->
                newsDao.getBookmarkedNews()
            else ->
                newsDao.getNewsByCategory(trimmed)
        }
    }

    override fun getNewsByCategoryAndSearch(category: String, query: String): Flow<List<NewsArticleEntity>> {
        val trimmedCategory = category.trim().ifEmpty { "All" }
        val trimmedQuery = query.trim()
        return when {
            trimmedCategory.equals("All", ignoreCase = true) && trimmedQuery.isEmpty() ->
                newsDao.getAllNews()
            trimmedCategory.equals("All", ignoreCase = true) ->
                newsDao.searchNews(trimmedQuery)
            (trimmedCategory.equals("Trending", ignoreCase = true) || trimmedCategory.equals("Trending Stories", ignoreCase = true)) && trimmedQuery.isEmpty() ->
                getTrendingNews()
            trimmedCategory.equals("Trending", ignoreCase = true) || trimmedCategory.equals("Trending Stories", ignoreCase = true) ->
                kotlinx.coroutines.flow.combine(
                    newsDao.searchNews(trimmedQuery),
                    com.example.data.firebase.FirestoreService.trendingViewCountsFlow
                ) { articles, viewCounts ->
                    articles.sortedByDescending { entity ->
                        com.example.data.firebase.FirestoreService.getViewCountForId(
                            entity.id.toString(),
                            viewCounts
                        )
                    }
                }
            trimmedQuery.isEmpty() ->
                getNewsByCategory(trimmedCategory)
            else ->
                newsDao.getNewsByCategoryAndSearch(trimmedCategory, trimmedQuery)
        }
    }

    override fun getBookmarkedNews(): Flow<List<NewsArticleEntity>> =
        newsDao.getBookmarkedNews()

    override fun searchNews(query: String): Flow<List<NewsArticleEntity>> =
        if (query.isBlank()) newsDao.getAllNews() else newsDao.searchNews(query.trim())

    override fun getArticleById(id: Long): Flow<NewsArticleEntity?> =
        newsDao.getArticleById(id)

    override suspend fun refreshNews(): Result<Int> = withContext(Dispatchers.IO) {
        val bookmarkedArticles = runCatching { newsDao.getBookmarkedNewsSync() }.getOrDefault(emptyList())
        val bookmarkedIds = bookmarkedArticles.map { it.id }.toSet()
        val bookmarkedTitles = bookmarkedArticles.map { it.title.trim().lowercase() }.toSet()

        // Always pull Local, National, and International feeds from external sources and publish directly without approval
        val syndicatedEntities = runCatching {
            com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullAndAutoPublishAllSections(
                context = null,
                forceNetworkRss = true
            )
            com.example.data.sync.HourlyMultiSourceNewsSyndicator.pulledArticlesFlow.value.mapIndexed { idx, art ->
                val numericId = art.id.toLongOrNull() ?: (7100L + idx)
                val isBookmarked = bookmarkedIds.contains(numericId) ||
                    bookmarkedTitles.contains(art.title.trim().lowercase())
                NewsArticleEntity(
                    id = numericId,
                    title = art.title,
                    summary = art.snippet,
                    content = art.content.ifBlank { art.snippet },
                    category = art.category,
                    source = art.source,
                    publishedAt = art.timeAgo,
                    timestamp = System.currentTimeMillis() - idx * 30_000L,
                    isBreaking = idx < 3,
                    isBookmarked = isBookmarked
                )
            }
        }.getOrDefault(emptyList())

        // Automatically fetch live NewsAPI articles when NEWS_API_KEY is configured via AI Studio Secrets
        val newsApiEntities = if (newsApiOrgClient.isConfigured()) {
            newsApiOrgClient.fetchNewsArticleEntities(
                bookmarkedIds = bookmarkedIds,
                bookmarkedTitles = bookmarkedTitles
            ).getOrDefault(emptyList())
        } else {
            emptyList()
        }

        val combinedPreEntities = (newsApiEntities + syndicatedEntities).distinctBy { it.id }
        if (combinedPreEntities.isNotEmpty()) {
            runCatching { newsDao.insertArticles(combinedPreEntities) }
        }

        try {
            val response = apiService.getBalasoreNews()
            val dtos = response.articles.orEmpty()

            val entities = dtos.map { dto ->
                val isBookmarked = (dto.id != null && bookmarkedIds.contains(dto.id)) ||
                        (dto.title != null && bookmarkedTitles.contains(dto.title.trim().lowercase()))
                dto.toEntity(isBookmarked = isBookmarked)
            }

            if (entities.isNotEmpty()) {
                newsDao.insertArticles(entities)
            }

            val totalCount = newsDao.getCount()
            val detailText = if (newsApiEntities.isNotEmpty()) {
                "Synced via NewsAPI (NEWS_API_KEY) + hourly Local, National & International feeds."
            } else {
                "Hourly auto-pulled Local, National & International feeds directly into Room."
            }
            cacheMetadataDao?.insertOrUpdate(
                CacheSyncMetadataEntity(
                    cacheKey = "NEWS",
                    lastSyncedAt = System.currentTimeMillis(),
                    itemCount = totalCount,
                    status = "FRESH",
                    cacheLabel = "$totalCount Multi-Source News Articles",
                    details = detailText
                )
            )
            Result.success(totalCount)
        } catch (e: Exception) {
            val count = newsDao.getCount()
            if (count > 0) {
                Result.success(count)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun toggleBookmark(articleId: Long, currentBookmarked: Boolean) = withContext(Dispatchers.IO) {
        newsDao.updateBookmark(articleId, !currentBookmarked)
    }

    override suspend fun insertArticle(article: NewsArticleEntity): Long = withContext(Dispatchers.IO) {
        newsDao.insertArticle(article)
    }

    override suspend fun updateArticle(article: NewsArticleEntity): Int = withContext(Dispatchers.IO) {
        newsDao.updateArticle(article)
    }

    override suspend fun deleteArticle(article: NewsArticleEntity): Int = withContext(Dispatchers.IO) {
        newsDao.deleteArticle(article)
    }

    override suspend fun deleteArticleById(id: Long): Int = withContext(Dispatchers.IO) {
        newsDao.deleteArticleById(id)
    }
}
