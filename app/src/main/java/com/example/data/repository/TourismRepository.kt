package com.example.data.repository

import com.example.data.local.CacheMetadataDao
import com.example.data.local.CacheSyncMetadataEntity
import com.example.data.local.HotspotDao
import com.example.data.local.HotspotEntity
import com.example.data.model.Hotspot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Remote data source abstraction for fetching live Balasore tourism hotspots.
 * Enables clean offline-first unit testing with JUnit and Mockito.
 */
interface ITourismRemoteDataSource {
    suspend fun fetchRemoteHotspots(): List<HotspotEntity>
}

/**
 * Default remote data source that maps official Balasore district tourism hotspots
 * into [HotspotEntity] records for local Room persistence.
 */
class DefaultTourismRemoteDataSource : ITourismRemoteDataSource {
    override suspend fun fetchRemoteHotspots(): List<HotspotEntity> {
        val now = System.currentTimeMillis()
        return BalasoreRepository.hotspots.map { hotspot ->
            hotspot.toEntity(timestamp = now)
        }
    }
}

/**
 * Extension mapper converting UI/Domain [Hotspot] to Room [HotspotEntity].
 */
fun Hotspot.toEntity(
    isFavorite: Boolean = false,
    timestamp: Long = System.currentTimeMillis()
): HotspotEntity {
    return HotspotEntity(
        id = id,
        name = name,
        odiaName = odiaName,
        hindiName = name,
        category = category,
        shortDescription = description,
        fullDescription = effectiveDetailedDescription,
        highlights = location,
        distanceKmFromBls = distanceKm.toInt(),
        bestTimeToVisit = "October to March",
        timings = effectiveOperatingHours,
        entryFee = "Free / Nominal Entry",
        specialty = historicalSignificance.ifBlank { location },
        localTip = "Best visited during morning or golden hour in Balasore.",
        latitude = latitude,
        longitude = longitude,
        isFavorite = isFavorite,
        timestamp = timestamp
    )
}

/**
 * Extension mapper converting Room [HotspotEntity] to UI/Domain [Hotspot].
 */
fun HotspotEntity.toDomainModel(): Hotspot {
    return Hotspot(
        id = id,
        name = name,
        odiaName = odiaName,
        category = category,
        description = shortDescription.ifBlank { fullDescription },
        location = highlights.ifBlank { "Balasore District, Odisha" },
        rating = 4.8,
        isFeatured = isFavorite || distanceKmFromBls <= 15,
        timing = timings.ifBlank { "6:00 AM - 6:00 PM" },
        distanceKm = distanceKmFromBls.toDouble(),
        detailedDescription = fullDescription,
        historicalSignificance = specialty,
        operatingHours = timings,
        latitude = latitude,
        longitude = longitude
    )
}

/**
 * Repository interface defining offline-first tourism hotspot operations.
 */
interface ITourismRepository {
    val allHotspots: Flow<List<HotspotEntity>>
    val favoriteHotspots: Flow<List<HotspotEntity>>
    fun getUnifiedHotspotsFlow(forceRefresh: Boolean = false): Flow<Resource<List<HotspotEntity>>>
    fun getHotspotsByCategory(category: String): Flow<List<HotspotEntity>>
    fun searchHotspots(query: String): Flow<List<HotspotEntity>>
    fun getHotspotById(id: String): Flow<HotspotEntity?>
    suspend fun getHotspotByIdSync(id: String): HotspotEntity?
    suspend fun isCacheOlderThan24Hours(): Boolean
    suspend fun refreshHotspots(): Result<Int>
    suspend fun toggleFavorite(hotspotId: String, currentFavorite: Boolean)
    suspend fun insertHotspot(hotspot: HotspotEntity)
    suspend fun insertHotspots(hotspots: List<HotspotEntity>)
    suspend fun updateHotspot(hotspot: HotspotEntity): Int
    suspend fun deleteHotspot(hotspot: HotspotEntity): Int
    suspend fun deleteHotspotById(id: String): Int
}

/**
 * Offline-first repository for Balasore Tourism Hotspots backed by Room [HotspotDao],
 * [CacheMetadataDao], and [ITourismRemoteDataSource].
 */
class TourismRepository(
    private val hotspotDao: HotspotDao,
    private val cacheMetadataDao: CacheMetadataDao? = null,
    private val remoteDataSource: ITourismRemoteDataSource = DefaultTourismRemoteDataSource()
) : ITourismRepository {

    override val allHotspots: Flow<List<HotspotEntity>> =
        hotspotDao.getAllHotspots().distinctUntilChanged()

    override val favoriteHotspots: Flow<List<HotspotEntity>> =
        hotspotDao.getFavoriteHotspots().distinctUntilChanged()

    companion object {
        const val CACHE_EXPIRATION_MS = 24 * 60 * 60 * 1000L // 24 Hours
        const val CACHE_KEY_TOURISM = "TOURISM"
    }

    override suspend fun isCacheOlderThan24Hours(): Boolean = withContext(Dispatchers.IO) {
        val latestTimestamp = hotspotDao.getLatestTimestamp() ?: 0L
        val metadataTime = cacheMetadataDao?.getMetadataSync(CACHE_KEY_TOURISM)?.lastSyncedAt ?: 0L
        val effectiveTime = maxOf(latestTimestamp, metadataTime)
        if (effectiveTime <= 0L) return@withContext true
        (System.currentTimeMillis() - effectiveTime) > CACHE_EXPIRATION_MS
    }

    /**
     * Unified Flow combining local Room tourism hotspot cache with remote network synchronization:
     * 1. Emits [Resource.Loading] immediately.
     * 2. If [forceRefresh], if local cache is empty, or if cache is older than 24 hours, fetches remote hotspots,
     *    preserves user favorite flags, persists to Room, and emits [Resource.Success].
     * 3. On network failure, falls back gracefully to local Room cached hotspots.
     */
    override fun getUnifiedHotspotsFlow(forceRefresh: Boolean): Flow<Resource<List<HotspotEntity>>> = flow {
        val hasLocalHotspots = hotspotDao.getCount() > 0
        val isStale = isCacheOlderThan24Hours()

        emit(Resource.Loading())

        if (forceRefresh || !hasLocalHotspots || isStale) {
            try {
                val remoteList = remoteDataSource.fetchRemoteHotspots()
                val favoriteHotspots = hotspotDao.getFavoriteHotspotsSync()
                val favoriteIds = favoriteHotspots.map { it.id }.toSet()

                val mergedEntities = remoteList.map { entity ->
                    entity.copy(isFavorite = entity.isFavorite || favoriteIds.contains(entity.id))
                }

                if (mergedEntities.isNotEmpty()) {
                    hotspotDao.insertHotspots(mergedEntities)
                }

                val totalCount = hotspotDao.getCount()
                cacheMetadataDao?.insertOrUpdate(
                    CacheSyncMetadataEntity(
                        cacheKey = CACHE_KEY_TOURISM,
                        lastSyncedAt = System.currentTimeMillis(),
                        itemCount = totalCount,
                        status = "FRESH",
                        cacheLabel = "$totalCount Tourism Hotspots",
                        details = "Synchronized live and cached in Room database."
                    )
                )

                emit(Resource.Success(data = mergedEntities, isOfflineCached = false))
            } catch (e: Exception) {
                val cachedList = hotspotDao.getAllHotspotsSync()
                cacheMetadataDao?.insertOrUpdate(
                    CacheSyncMetadataEntity(
                        cacheKey = CACHE_KEY_TOURISM,
                        lastSyncedAt = System.currentTimeMillis(),
                        itemCount = cachedList.size,
                        status = "OFFLINE",
                        cacheLabel = "Offline Room Tourism Cache",
                        details = "Serving local cached tourism hotspots without network."
                    )
                )
                emit(
                    Resource.Success(
                        data = cachedList,
                        isOfflineCached = true,
                        message = "Serving tourism hotspots from offline Room cache"
                    )
                )
            }
        } else {
            val cachedList = hotspotDao.getAllHotspotsSync()
            emit(Resource.Success(data = cachedList, isOfflineCached = true))
        }
    }.flowOn(Dispatchers.IO)

    override fun getHotspotsByCategory(category: String): Flow<List<HotspotEntity>> {
        val trimmed = category.trim()
        return when {
            trimmed.isEmpty() || trimmed.equals("All", ignoreCase = true) ->
                hotspotDao.getAllHotspots()
            trimmed.equals("Favorites", ignoreCase = true) || trimmed.equals("Saved", ignoreCase = true) ->
                hotspotDao.getFavoriteHotspots()
            else ->
                hotspotDao.getHotspotsByCategory(trimmed)
        }
    }

    override fun searchHotspots(query: String): Flow<List<HotspotEntity>> =
        if (query.isBlank()) hotspotDao.getAllHotspots() else hotspotDao.searchHotspots(query.trim())

    override fun getHotspotById(id: String): Flow<HotspotEntity?> =
        hotspotDao.getHotspotById(id)

    override suspend fun getHotspotByIdSync(id: String): HotspotEntity? = withContext(Dispatchers.IO) {
        hotspotDao.getHotspotByIdSync(id)
    }

    override suspend fun refreshHotspots(): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val remoteList = remoteDataSource.fetchRemoteHotspots()
            val favoriteHotspots = hotspotDao.getFavoriteHotspotsSync()
            val favoriteIds = favoriteHotspots.map { it.id }.toSet()

            val mergedEntities = remoteList.map { entity ->
                entity.copy(isFavorite = entity.isFavorite || favoriteIds.contains(entity.id))
            }

            if (mergedEntities.isNotEmpty()) {
                hotspotDao.insertHotspots(mergedEntities)
            }

            val totalCount = hotspotDao.getCount()
            cacheMetadataDao?.insertOrUpdate(
                CacheSyncMetadataEntity(
                    cacheKey = CACHE_KEY_TOURISM,
                    lastSyncedAt = System.currentTimeMillis(),
                    itemCount = totalCount,
                    status = "FRESH",
                    cacheLabel = "$totalCount Tourism Hotspots",
                    details = "Refreshed and cached in Room database."
                )
            )
            Result.success(totalCount)
        } catch (e: Exception) {
            val cachedCount = hotspotDao.getCount()
            if (cachedCount > 0) {
                Result.success(cachedCount)
            } else {
                Result.failure(e)
            }
        }
    }

    override suspend fun toggleFavorite(hotspotId: String, currentFavorite: Boolean) = withContext(Dispatchers.IO) {
        hotspotDao.updateFavorite(hotspotId, !currentFavorite)
    }

    override suspend fun insertHotspot(hotspot: HotspotEntity) = withContext(Dispatchers.IO) {
        hotspotDao.insertHotspot(hotspot)
    }

    override suspend fun insertHotspots(hotspots: List<HotspotEntity>) = withContext(Dispatchers.IO) {
        hotspotDao.insertHotspots(hotspots)
    }

    override suspend fun updateHotspot(hotspot: HotspotEntity): Int = withContext(Dispatchers.IO) {
        hotspotDao.updateHotspot(hotspot)
    }

    override suspend fun deleteHotspot(hotspot: HotspotEntity): Int = withContext(Dispatchers.IO) {
        hotspotDao.deleteHotspot(hotspot)
    }

    override suspend fun deleteHotspotById(id: String): Int = withContext(Dispatchers.IO) {
        hotspotDao.deleteHotspotById(id)
    }
}
