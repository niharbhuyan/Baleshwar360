package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Balasore Bus Stand Live Schedules.
 */
@Dao
interface BusScheduleDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: BusScheduleEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<BusScheduleEntity>)

    @Update
    suspend fun updateSchedule(schedule: BusScheduleEntity)

    @Delete
    suspend fun deleteSchedule(schedule: BusScheduleEntity)

    @Query("SELECT * FROM balasore_bus_schedules ORDER BY departureMinutesOfDay ASC")
    fun getAllSchedules(): Flow<List<BusScheduleEntity>>

    @Query("SELECT * FROM balasore_bus_schedules ORDER BY departureMinutesOfDay ASC")
    suspend fun getAllSchedulesSync(): List<BusScheduleEntity>

    @Query("""
        SELECT * FROM balasore_bus_schedules
        WHERE (:destinationQuery IS NULL OR :destinationQuery = '' 
               OR destination LIKE '%' || :destinationQuery || '%' 
               OR destinationOdia LIKE '%' || :destinationQuery || '%' 
               OR serviceName LIKE '%' || :destinationQuery || '%'
               OR busNumber LIKE '%' || :destinationQuery || '%'
               OR viaRoute LIKE '%' || :destinationQuery || '%')
          AND (:minMinutes IS NULL OR departureMinutesOfDay >= :minMinutes)
          AND (:maxMinutes IS NULL OR departureMinutesOfDay <= :maxMinutes)
        ORDER BY departureMinutesOfDay ASC
    """)
    fun filterSchedules(
        destinationQuery: String?,
        minMinutes: Int?,
        maxMinutes: Int?
    ): Flow<List<BusScheduleEntity>>

    @Query("SELECT DISTINCT destination FROM balasore_bus_schedules ORDER BY destination ASC")
    fun getDistinctDestinations(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM balasore_bus_schedules")
    suspend fun getScheduleCount(): Int

    @Query("DELETE FROM balasore_bus_schedules")
    suspend fun clearAll()
}
