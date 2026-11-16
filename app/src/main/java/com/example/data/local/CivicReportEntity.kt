package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a citizen's reported civic issue in Balasore district.
 * Persisted locally in SQLite (Room) with optional photo capture and a 'sync-when-online' queue
 * for 100% offline resilience, category filtering, status progression, and automated municipal updates.
 */
@Entity(tableName = "civic_reports")
data class CivicReportEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val wardLocation: String,
    val description: String,
    val status: String = "PENDING", // "DRAFT", "PENDING", "IN_PROGRESS", "RESOLVED"
    val urgency: String = "NORMAL", // "LOW", "NORMAL", "MEDIUM", "HIGH", "CRITICAL", "EMERGENCY"
    val assignedDepartment: String = "Balasore Municipality Grievance Cell",
    val reportedTimestamp: Long = System.currentTimeMillis(),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val hasPhotoAttached: Boolean = false,
    val resolutionNotes: String = "Report logged in local municipal queue. Awaiting field inspection.",
    val photoUri: String? = null,
    val latitude: Double? = 21.4934,
    val longitude: Double? = 86.9135,
    val geoAddress: String? = "Balasore, Odisha",
    val upvotesCount: Int = 0,
    val syncStatus: String = "SYNCED" // "QUEUED_OFFLINE", "SYNCING", "SYNCED"
) {
    /**
     * True if this report is waiting in the local Room 'sync-when-online' queue.
     */
    val isQueuedForSync: Boolean
        get() = syncStatus.equals("QUEUED_OFFLINE", ignoreCase = true) ||
            status.equals("DRAFT", ignoreCase = true)

    /**
     * Unified category matcher supporting all Civic Eye category filters:
     * 'Potholes', 'Lighting', 'Water Supply', 'Waterlogging', 'Waste', 'Road', and 'ALL'.
     */
    fun matchesCategoryFilter(filterKey: String): Boolean {
        val norm = filterKey.trim().uppercase()
        if (norm.isEmpty() || norm == "ALL" || norm == "ALL CATEGORIES") return true
        return when (norm) {
            "POTHOLE", "POTHOLES" ->
                category.contains("Pothole", ignoreCase = true) ||
                    title.contains("Pothole", ignoreCase = true)
            "LIGHTING", "ELECTRICITY", "STREETLIGHT", "STREET LIGHT OUTAGES" ->
                category.contains("Light", ignoreCase = true) ||
                    category.contains("Electric", ignoreCase = true) ||
                    category.contains("Lamp", ignoreCase = true) ||
                    title.contains("Streetlight", ignoreCase = true) ||
                    title.contains("Light", ignoreCase = true)
            "WATER_SUPPLY", "WATER SUPPLY", "WATER_LEAK", "WATER LEAK", "WATER" ->
                (category.contains("Water", ignoreCase = true) && !category.contains("Waterlog", ignoreCase = true)) ||
                    category.contains("Supply", ignoreCase = true) ||
                    category.contains("Leak", ignoreCase = true) ||
                    category.contains("Pipeline", ignoreCase = true)
            "WATERLOGGING", "DRAINAGE" ->
                category.contains("Waterlog", ignoreCase = true) ||
                    category.contains("Drain", ignoreCase = true)
            "WASTE", "GARBAGE", "SANITATION" ->
                category.contains("Garbage", ignoreCase = true) ||
                    category.contains("Waste", ignoreCase = true) ||
                    category.contains("Sanitation", ignoreCase = true)
            "ROAD", "DAMAGED ROAD" ->
                category.contains("Road", ignoreCase = true) ||
                    category.contains("Culvert", ignoreCase = true)
            else ->
                category.contains(filterKey, ignoreCase = true)
        }
    }
}
