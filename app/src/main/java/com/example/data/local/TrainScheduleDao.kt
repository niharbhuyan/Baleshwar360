package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Balasore Railway Station train schedules stored in Room.
 */
@Dao
interface TrainScheduleDao {

    @Query("SELECT * FROM balasore_train_schedules ORDER BY scheduledArrival ASC")
    fun getAllTrainSchedules(): Flow<List<TrainScheduleEntity>>

    @Query("SELECT * FROM balasore_train_schedules ORDER BY scheduledArrival ASC")
    suspend fun getAllTrainSchedulesSync(): List<TrainScheduleEntity>

    @Query("SELECT * FROM balasore_train_schedules WHERE trainNumber = :trainNumber LIMIT 1")
    suspend fun getTrainByNumber(trainNumber: String): TrainScheduleEntity?

    @Query("SELECT * FROM balasore_train_schedules WHERE delayMinutes > 0 ORDER BY delayMinutes DESC")
    fun getDelayedTrains(): Flow<List<TrainScheduleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainSchedules(trains: List<TrainScheduleEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrainSchedule(train: TrainScheduleEntity)

    @Update
    suspend fun updateTrainSchedule(train: TrainScheduleEntity)

    @Query("""
        UPDATE balasore_train_schedules 
        SET delayMinutes = :delayMinutes, 
            estimatedArrival = :estimatedArrival, 
            platform = :platform, 
            status = :status, 
            lastUpdatedMillis = :lastUpdatedMillis 
        WHERE trainNumber = :trainNumber
    """)
    suspend fun updateDelay(
        trainNumber: String,
        delayMinutes: Int,
        estimatedArrival: String,
        platform: String,
        status: String,
        lastUpdatedMillis: Long
    )

    @Query("UPDATE balasore_train_schedules SET isTracked = :isTracked WHERE trainNumber = :trainNumber")
    suspend fun updateTrackedStatus(trainNumber: String, isTracked: Boolean)

    @Query("DELETE FROM balasore_train_schedules")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM balasore_train_schedules")
    suspend fun getCount(): Int
}
