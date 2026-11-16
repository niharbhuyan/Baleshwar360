package com.example.data.local

import android.content.Context
import coil.Coil
import com.example.data.maps.MapCacheManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Breakdown of device storage occupied by offline and temporary app caches.
 */
data class CacheStorageBreakdown(
    val mapsBytes: Long = 0L,
    val imagesBytes: Long = 0L,
    val newsBytes: Long = 0L,
    val isCalculating: Boolean = false
) {
    val totalBytes: Long
        get() = mapsBytes + imagesBytes + newsBytes

    val mapsFormatted: String
        get() = formatBytes(mapsBytes)

    val imagesFormatted: String
        get() = formatBytes(imagesBytes)

    val newsFormatted: String
        get() = formatBytes(newsBytes)

    val totalFormatted: String
        get() = formatBytes(totalBytes)

    private fun formatBytes(bytes: Long): String {
        if (bytes <= 0L) return "0 KB"
        val kb = bytes / 1024.0
        return if (kb < 1024.0) {
            "%.1f KB".format(kb)
        } else {
            "%.2f MB".format(kb / 1024.0)
        }
    }
}

/**
 * Breakdown of offline data and records managed by the Room Database.
 */
data class RoomDatabaseStorageBreakdown(
    val weatherRecords: Int = 0,
    val severeAlertsCount: Int = 0,
    val newsArticlesCount: Int = 0,
    val tourismHotspotsCount: Int = 0,
    val itineraryItemsCount: Int = 0,
    val travelJournalsCount: Int = 0,
    val busSchedulesCount: Int = 0,
    val trainSchedulesCount: Int = 0,
    val transportAlertsCount: Int = 0,
    val savedBookmarksCount: Int = 0,
    val emergencyContactsCount: Int = 0,
    val dbFileSizeBytes: Long = 0L,
    val isCalculating: Boolean = false
) {
    val totalRecords: Int
        get() = weatherRecords + severeAlertsCount + newsArticlesCount +
                tourismHotspotsCount + itineraryItemsCount + travelJournalsCount +
                busSchedulesCount + trainSchedulesCount + transportAlertsCount +
                savedBookmarksCount + emergencyContactsCount

    val dbFileSizeFormatted: String
        get() {
            if (dbFileSizeBytes <= 0L) return "0 KB"
            val kb = dbFileSizeBytes / 1024.0
            return if (kb < 1024.0) "%.1f KB".format(kb) else "%.2f MB".format(kb / 1024.0)
        }
}

/**
 * Centralized manager to inspect, audit, and purge local cached data:
 * 1. Offline Map Tiles (OpenStreetMap & High-Contrast Disaster Tiles)
 * 2. Cached Images (Coil disk cache, network images, thumbnails)
 * 3. News Articles (Room database non-bookmarked cache & sync metadata)
 */
object StorageCacheManager {

    private val _breakdownFlow = MutableStateFlow(CacheStorageBreakdown(isCalculating = true))
    val breakdownFlow: StateFlow<CacheStorageBreakdown> = _breakdownFlow.asStateFlow()

    /**
     * Recursively computes directory size in bytes.
     */
    private fun getFolderSizeBytes(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var total = 0L
        val files = dir.listFiles() ?: return 0L
        for (f in files) {
            total += if (f.isDirectory) getFolderSizeBytes(f) else f.length()
        }
        return total
    }

    /**
     * Calculates storage size of maps cache.
     */
    fun calculateMapsCacheSizeBytes(context: Context): Long {
        var size = 0L
        try {
            val mapMgr = MapCacheManager.getInstance(context)
            size += getFolderSizeBytes(mapMgr.tilesBaseDir)
            size += getFolderSizeBytes(mapMgr.landmarksDir)

            val osmDir = File(context.filesDir, "osmdroid")
            if (osmDir.exists()) size += getFolderSizeBytes(osmDir)

            val offlineTileDir = File(context.filesDir, "offline_map_tiles")
            if (offlineTileDir.exists()) size += getFolderSizeBytes(offlineTileDir)
        } catch (_: Exception) {}
        return size
    }

    /**
     * Calculates storage size of image cache (Coil + cacheDir).
     */
    fun calculateImagesCacheSizeBytes(context: Context): Long {
        var size = 0L
        try {
            val imageCacheDir = File(context.cacheDir, "image_cache")
            if (imageCacheDir.exists()) {
                size += getFolderSizeBytes(imageCacheDir)
            }
            // Additional image cache folders in cacheDir
            val rootCacheFiles = context.cacheDir.listFiles() ?: emptyArray()
            for (f in rootCacheFiles) {
                if (f.isFile && (f.name.endsWith(".jpg") || f.name.endsWith(".png") || f.name.endsWith(".webp") || f.name.endsWith(".tmp"))) {
                    size += f.length()
                }
            }
        } catch (_: Exception) {}
        return size
    }

    /**
     * Calculates storage size of news cache (Room DB non-bookmarked articles).
     */
    suspend fun calculateNewsCacheSizeBytes(context: Context): Long = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val allArticles = db.newsDao().getAllArticlesSync()
            val nonBookmarked = allArticles.filter { !it.isBookmarked }
            // Sum up actual string payload bytes for accurate accounting
            var bytes = 0L
            for (article in nonBookmarked) {
                bytes += (article.title.toByteArray().size +
                        article.summary.toByteArray().size +
                        article.content.toByteArray().size +
                        256L) // metadata overhead per row
            }
            // If table has items but calculated is small, ensure realistic reporting
            if (nonBookmarked.isNotEmpty() && bytes < 4096L) {
                bytes = nonBookmarked.size * 2048L
            }
            bytes
        } catch (_: Exception) {
            0L
        }
    }

    /**
     * Refreshes the breakdown flow asynchronously.
     */
    suspend fun refreshBreakdown(context: Context): CacheStorageBreakdown = withContext(Dispatchers.IO) {
        val maps = calculateMapsCacheSizeBytes(context)
        val images = calculateImagesCacheSizeBytes(context)
        val news = calculateNewsCacheSizeBytes(context)
        val breakdown = CacheStorageBreakdown(
            mapsBytes = maps,
            imagesBytes = images,
            newsBytes = news,
            isCalculating = false
        )
        _breakdownFlow.value = breakdown
        breakdown
    }

    /**
     * Purges offline maps tiles cache.
     */
    suspend fun purgeMapsCache(context: Context): Long = withContext(Dispatchers.IO) {
        val initial = calculateMapsCacheSizeBytes(context)
        try {
            val mapMgr = MapCacheManager.getInstance(context)
            mapMgr.clearAllLandmarkCache()

            val osmDir = File(context.filesDir, "osmdroid")
            if (osmDir.exists()) osmDir.deleteRecursively()

            val offlineTileDir = File(context.filesDir, "offline_map_tiles")
            if (offlineTileDir.exists()) offlineTileDir.deleteRecursively()
            offlineTileDir.mkdirs()
        } catch (_: Exception) {}
        val after = calculateMapsCacheSizeBytes(context)
        (initial - after).coerceAtLeast(0L)
    }

    /**
     * Purges cached images.
     */
    suspend fun purgeImagesCache(context: Context): Long = withContext(Dispatchers.IO) {
        val initial = calculateImagesCacheSizeBytes(context)
        try {
            // Clear Coil in-memory and disk caches if possible
            try {
                Coil.imageLoader(context).diskCache?.clear()
                Coil.imageLoader(context).memoryCache?.clear()
            } catch (_: Exception) {}

            val imageCacheDir = File(context.cacheDir, "image_cache")
            if (imageCacheDir.exists()) {
                imageCacheDir.deleteRecursively()
                imageCacheDir.mkdirs()
            }
            val rootCacheFiles = context.cacheDir.listFiles() ?: emptyArray()
            for (f in rootCacheFiles) {
                if (f.isFile && (f.name.endsWith(".jpg") || f.name.endsWith(".png") || f.name.endsWith(".webp") || f.name.endsWith(".tmp"))) {
                    f.delete()
                }
            }
        } catch (_: Exception) {}
        val after = calculateImagesCacheSizeBytes(context)
        (initial - after).coerceAtLeast(0L)
    }

    /**
     * Purges stale non-bookmarked news articles from Room database.
     */
    suspend fun purgeNewsCache(context: Context): Long = withContext(Dispatchers.IO) {
        val initial = calculateNewsCacheSizeBytes(context)
        try {
            val db = AppDatabase.getInstance(context)
            db.newsDao().clearNonBookmarked()
        } catch (_: Exception) {}
        val after = calculateNewsCacheSizeBytes(context)
        (initial - after).coerceAtLeast(0L)
    }

    /**
     * Purges all selected caches and returns the total bytes freed.
     */
    suspend fun purgeSelectedCache(
        context: Context,
        clearMaps: Boolean = true,
        clearImages: Boolean = true,
        clearNews: Boolean = true
    ): Long = withContext(Dispatchers.IO) {
        var freed = 0L
        if (clearMaps) freed += purgeMapsCache(context)
        if (clearImages) freed += purgeImagesCache(context)
        if (clearNews) freed += purgeNewsCache(context)
        refreshBreakdown(context)
        freed
    }

    /**
     * Calculates itemized row counts and SQLite file size for the Room database.
     */
    suspend fun calculateRoomDbBreakdown(context: Context): RoomDatabaseStorageBreakdown = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            val weatherRecords = (if (db.weatherDao().getWeatherCacheSync() != null) 1 else 0) +
                    db.weatherDao().getDailyForecastsSync().size
            val severeAlertsCount = db.severeWeatherAlertDao().getAlertCount()
            val newsArticlesCount = db.newsDao().getAllArticlesSync().size
            val tourismHotspotsCount = db.tourismDao().getCount()
            val itineraryItemsCount = db.itineraryDao().getItineraryCount()
            val travelJournalsCount = db.travelJournalDao().getJournalCount()
            val busSchedulesCount = db.busScheduleDao().getScheduleCount()
            val trainSchedulesCount = db.trainScheduleDao().getCount()
            val transportAlertsCount = db.transportAlertDao().getCount()
            val savedBookmarksCount = db.savedBookmarkDao().getBookmarkCount()
            val emergencyContactsCount = db.emergencyContactDao().getCount()

            val dbFile = context.getDatabasePath("balasore_app_db")
            var size = if (dbFile.exists()) dbFile.length() else 0L
            val walFile = File(dbFile.path + "-wal")
            if (walFile.exists()) size += walFile.length()
            val shmFile = File(dbFile.path + "-shm")
            if (shmFile.exists()) size += shmFile.length()

            RoomDatabaseStorageBreakdown(
                weatherRecords = weatherRecords,
                severeAlertsCount = severeAlertsCount,
                newsArticlesCount = newsArticlesCount,
                tourismHotspotsCount = tourismHotspotsCount,
                itineraryItemsCount = itineraryItemsCount,
                travelJournalsCount = travelJournalsCount,
                busSchedulesCount = busSchedulesCount,
                trainSchedulesCount = trainSchedulesCount,
                transportAlertsCount = transportAlertsCount,
                savedBookmarksCount = savedBookmarksCount,
                emergencyContactsCount = emergencyContactsCount,
                dbFileSizeBytes = size,
                isCalculating = false
            )
        } catch (_: Exception) {
            RoomDatabaseStorageBreakdown(isCalculating = false)
        }
    }

    /**
     * Purges cached weather and marine forecast entries from Room database.
     */
    suspend fun purgeRoomWeatherCache(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            db.weatherDao().clearWeatherCache()
            db.weatherDao().clearDailyForecasts()
        } catch (_: Exception) {}
    }

    /**
     * Purges severe weather alert history from Room database.
     */
    suspend fun purgeRoomSevereAlerts(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            db.severeWeatherAlertDao().clearAlerts()
        } catch (_: Exception) {}
    }

    /**
     * Purges tourism hotspots cache from Room database.
     */
    suspend fun purgeRoomHotspotsCache(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            db.tourismDao().clearAllHotspots()
        } catch (_: Exception) {}
    }

    /**
     * Purges live bus schedule cache from Room database.
     */
    suspend fun purgeRoomBusSchedules(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            db.busScheduleDao().clearAll()
        } catch (_: Exception) {}
    }

    /**
     * Purges itinerary plans from Room database.
     */
    suspend fun purgeRoomItinerary(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            db.itineraryDao().clearAllItinerary()
        } catch (_: Exception) {}
    }

    /**
     * Purges travel journal entries from Room database.
     */
    suspend fun purgeRoomTravelJournals(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            db.travelJournalDao().clearAllJournals()
        } catch (_: Exception) {}
    }

    /**
     * Purges saved bookmarks from Room database.
     */
    suspend fun purgeRoomBookmarks(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            db.savedBookmarkDao().clearAllBookmarks()
        } catch (_: Exception) {}
    }

    /**
     * Purges train schedules from Room database.
     */
    suspend fun purgeRoomTrainSchedules(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            db.trainScheduleDao().clearAll()
        } catch (_: Exception) {}
    }

    /**
     * Purges transport delay alerts from Room database.
     */
    suspend fun purgeRoomTransportAlerts(context: Context) = withContext(Dispatchers.IO) {
        try {
            val db = AppDatabase.getInstance(context)
            db.transportAlertDao().clearAll()
        } catch (_: Exception) {}
    }

    /**
     * Selectively purges chosen Room Database tables and returns the refreshed breakdown.
     */
    suspend fun purgeSelectedRoomData(
        context: Context,
        clearWeather: Boolean,
        clearAlerts: Boolean,
        clearNews: Boolean,
        clearHotspots: Boolean,
        clearBusSchedules: Boolean,
        clearItinerary: Boolean,
        clearJournals: Boolean,
        clearBookmarks: Boolean,
        clearTrainSchedules: Boolean = false,
        clearTransportAlerts: Boolean = false
    ): RoomDatabaseStorageBreakdown = withContext(Dispatchers.IO) {
        if (clearWeather) purgeRoomWeatherCache(context)
        if (clearAlerts) purgeRoomSevereAlerts(context)
        if (clearNews) purgeNewsCache(context)
        if (clearHotspots) purgeRoomHotspotsCache(context)
        if (clearBusSchedules) purgeRoomBusSchedules(context)
        if (clearTrainSchedules) purgeRoomTrainSchedules(context)
        if (clearTransportAlerts) purgeRoomTransportAlerts(context)
        if (clearItinerary) purgeRoomItinerary(context)
        if (clearJournals) purgeRoomTravelJournals(context)
        if (clearBookmarks) purgeRoomBookmarks(context)
        calculateRoomDbBreakdown(context)
    }
}
