package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Database entity representing live bus schedules departing from
 * Balasore bus stands (Sahadevkhunta Central Bus Stand & Old Bus Stand).
 */
@Entity(
    tableName = "balasore_bus_schedules",
    indices = [
        Index("destination"),
        Index("departureMinutesOfDay"),
        Index("busStandName"),
        Index("busType"),
        Index("status")
    ]
)
data class BusScheduleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val busNumber: String,
    val serviceName: String,
    val busStandName: String = "Sahadevkhunta Central Bus Stand",
    val destination: String,
    val destinationOdia: String = "",
    val departureTime: String,
    val departureMinutesOfDay: Int,
    val arrivalTime: String,
    val estimatedDuration: String = "3h 30m",
    val busType: String,
    val bayPlatform: String,
    val fareInr: Int,
    val viaRoute: String,
    val status: String = "ON_TIME",
    val frequency: String = "Daily",
    val contactPhone: String = "06782-262066",
    val isLiveActive: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)
