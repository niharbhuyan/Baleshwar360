package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room Database entity representing a citizen's vote on a local civic initiative
 * in Balasore (e.g. park improvements, event scheduling, coastal preservation).
 * Persisted locally for instant offline voting and synced to Firestore when online.
 */
@Entity(tableName = "citizen_votes")
data class CitizenVoteEntity(
    @PrimaryKey
    val pollId: String,
    val optionId: String,
    val optionLabel: String = "",
    val votedAtTimestamp: Long = System.currentTimeMillis(),
    val syncedToCloud: Boolean = false,
    val userId: String? = null,
    val initiativeCategory: String = "CIVIC"
)
