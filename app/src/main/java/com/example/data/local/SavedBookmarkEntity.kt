package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Room Database entity representing user-saved bookmarks across Balasore 360,
 * specifically news articles and emergency directory contact numbers for fast offline access.
 */
@Entity(
    tableName = "saved_bookmarks",
    indices = [
        Index("itemType"),
        Index("originalId"),
        Index("timestamp")
    ]
)
data class SavedBookmarkEntity(
    @PrimaryKey
    val id: String, // Unique key, e.g. "news_101" or "emergency_police_112"
    val itemType: String, // "NEWS" or "EMERGENCY"
    val originalId: String, // Original ID of the news article or emergency contact
    val title: String,
    val odiaTitle: String = "",
    val subtitle: String = "",
    val odiaSubtitle: String = "",
    val category: String = "",
    val phoneNumber: String? = null, // Specific to emergency contacts for 1-tap dialing
    val sourceOrDepartment: String = "",
    val imageUrl: String? = null,
    val content: String = "",
    val odiaContent: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
