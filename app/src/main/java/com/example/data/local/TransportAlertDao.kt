package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TransportAlertDao {
    @Query("SELECT * FROM transport_alerts ORDER BY timestamp DESC")
    fun getAllAlerts(): Flow<List<TransportAlertEntity>>

    @Query("SELECT * FROM transport_alerts WHERE isAcknowledged = 0 ORDER BY timestamp DESC")
    fun getActiveDelayAlerts(): Flow<List<TransportAlertEntity>>

    @Query("SELECT * FROM transport_alerts WHERE alertId = :alertId LIMIT 1")
    suspend fun getAlertById(alertId: String): TransportAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: TransportAlertEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<TransportAlertEntity>)

    @Query("UPDATE transport_alerts SET isAcknowledged = 1 WHERE alertId = :alertId")
    suspend fun acknowledgeAlert(alertId: String)

    @Query("DELETE FROM transport_alerts WHERE alertId = :alertId")
    suspend fun deleteAlertById(alertId: String)

    @Query("DELETE FROM transport_alerts")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM transport_alerts")
    suspend fun getCount(): Int
}
