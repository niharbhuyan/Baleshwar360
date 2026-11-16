package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Database entity representing cached local train schedules for
 * Balasore Railway Station (BLS) on the South Eastern Railway mainline.
 */
@Entity(
    tableName = "balasore_train_schedules",
    indices = [
        Index("trainNumber"),
        Index("destination"),
        Index("status"),
        Index("delayMinutes")
    ]
)
data class TrainScheduleEntity(
    @PrimaryKey
    val trainNumber: String,
    val trainNameEn: String,
    val trainNameOr: String,
    val trainType: String,
    val origin: String,
    val destination: String,
    val scheduledArrival: String,
    val estimatedArrival: String,
    val scheduledDeparture: String,
    val estimatedDeparture: String,
    val delayMinutes: Int = 0,
    val platform: String = "PF 1",
    val status: String = "ON_TIME",
    val currentLocation: String = "",
    val currentLocationOr: String = "",
    val speedKmh: Int = 0,
    val isTracked: Boolean = false,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
