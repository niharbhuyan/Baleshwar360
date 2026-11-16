package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Room Data Access Object for severe weather alert history in Balasore.
 */
@Dao
interface SevereWeatherAlertDao {

    /**
     * Fetches the last 5 severe weather alerts received for Balasore, ordered chronologically descending.
     */
    @Query("SELECT * FROM severe_weather_alerts ORDER BY receivedAtMillis DESC LIMIT 5")
    fun getLast5Alerts(): Flow<List<SevereWeatherAlertEntity>>

    @Query("SELECT * FROM severe_weather_alerts ORDER BY receivedAtMillis DESC")
    fun getAllAlerts(): Flow<List<SevereWeatherAlertEntity>>

    @Query("SELECT * FROM severe_weather_alerts ORDER BY receivedAtMillis DESC")
    suspend fun getAllAlertsSync(): List<SevereWeatherAlertEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<SevereWeatherAlertEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: SevereWeatherAlertEntity)

    @Query("DELETE FROM severe_weather_alerts")
    suspend fun clearAlerts()

    @Query("SELECT COUNT(*) FROM severe_weather_alerts")
    suspend fun getAlertCount(): Int
}
