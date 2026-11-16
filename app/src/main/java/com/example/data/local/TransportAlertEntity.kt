package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Database entity representing real-time and historical transport delay alerts
 * for Balasore trains and buses received via Firebase Cloud Messaging or local telemetry.
 */
@Entity(
    tableName = "transport_alerts",
    indices = [
        Index("transportType"),
        Index("identifier"),
        Index("timestamp")
    ]
)
data class TransportAlertEntity(
    @PrimaryKey
    val alertId: String,
    val transportType: String, // "TRAIN" or "BUS"
    val identifier: String, // e.g. "12821" or "OD-01-A-4421"
    val serviceName: String, // e.g. "Dhauli Express" or "OSRTC Express to Cuttack"
    val delayMinutes: Int,
    val scheduledTime: String,
    val estimatedTime: String,
    val platformOrBay: String,
    val reason: String,
    val reasonOdia: String = "",
    val severity: String = "WARNING", // "INFO", "WARNING", "SEVERE"
    val timestamp: Long = System.currentTimeMillis(),
    val isAcknowledged: Boolean = false,
    val fcmMessageId: String? = null
)
