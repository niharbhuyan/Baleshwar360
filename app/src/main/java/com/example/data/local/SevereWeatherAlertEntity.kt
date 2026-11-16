package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing severe weather alerts received for Balasore.
 * Stored locally to maintain an offline history of hazardous weather events.
 */
@Entity(tableName = "severe_weather_alerts")
data class SevereWeatherAlertEntity(
    @PrimaryKey val id: String,
    val title: String,
    val odiaTitle: String,
    val summary: String,
    val severity: String,
    val affectedZones: String,
    val issuingAuthority: String,
    val validFromMillis: Long,
    val validUntilMillis: Long,
    val receivedAtMillis: Long = System.currentTimeMillis()
)
