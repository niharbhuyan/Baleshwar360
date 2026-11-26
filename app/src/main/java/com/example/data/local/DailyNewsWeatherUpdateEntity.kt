package com.example.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room database schema storing daily aggregated Balasore news and weather snapshots
 * synced from public APIs (Open-Meteo Weather API & Google News Public RSS).
 *
 * Keyed by ISO-8601 calendar date ([dateKey], e.g. "2026-11-25") so each day retains
 * a single idempotent snapshot while preserving historical daily records for offline viewing.
 */
@Entity(
    tableName = "daily_news_weather_updates",
    indices = [
        Index(value = ["fetchedAtMs"]),
        Index(value = ["weatherAlertLevel"])
    ]
)
data class DailyNewsWeatherUpdateEntity(
    @PrimaryKey
    val dateKey: String,
    val regionName: String = "Balasore, Odisha",
    val latitude: Double = 21.4934,
    val longitude: Double = 86.9335,
    val temperatureC: Double,
    val feelsLikeC: Double,
    val maxTempC: Double,
    val minTempC: Double,
    val humidityPct: Int,
    val windSpeedKmh: Double,
    val precipitationMm: Double,
    val weatherCode: Int,
    val weatherSummary: String,
    val weatherAlertLevel: String, // "NORMAL", "ADVISORY", "WARNING", "CYCLONE_ALERT"
    val weatherAlertMessage: String,
    val weatherSource: String,
    val headlineCount: Int,
    val topNewsJson: String,
    val newsSource: String,
    val fetchedAtMs: Long = System.currentTimeMillis()
)

@Dao
interface DailyNewsWeatherUpdateDao {
    @Query("SELECT * FROM daily_news_weather_updates ORDER BY dateKey DESC LIMIT 1")
    fun observeLatestDailyUpdate(): Flow<DailyNewsWeatherUpdateEntity?>

    @Query("SELECT * FROM daily_news_weather_updates ORDER BY dateKey DESC LIMIT :limit")
    fun observeRecentDailyUpdates(limit: Int = 7): Flow<List<DailyNewsWeatherUpdateEntity>>

    @Query("SELECT * FROM daily_news_weather_updates WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getDailyUpdateByDate(dateKey: String): DailyNewsWeatherUpdateEntity?

    @Query("SELECT * FROM daily_news_weather_updates ORDER BY dateKey DESC LIMIT :limit")
    suspend fun getRecentDailyUpdatesSync(limit: Int = 7): List<DailyNewsWeatherUpdateEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyUpdate(snapshot: DailyNewsWeatherUpdateEntity)

    @Query("DELETE FROM daily_news_weather_updates WHERE dateKey < :cutoffDateKey")
    suspend fun pruneOlderThan(cutoffDateKey: String): Int
}
