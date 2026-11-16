package com.example.data.model

data class UserWeatherPreferences(
    val userId: String = "",
    val notifyCyclone: Boolean = true,
    val notifyHighTide: Boolean = true,
    val notifySevereHeat: Boolean = true,
    val notifyRainfall: Boolean = true,
    val notifyLocalEvents: Boolean = true,
    val notifyTempleFestivals: Boolean = true,
    val notifyBreakingNews: Boolean = true,
    val notifyTourismHotspots: Boolean = true,
    val notifyDefenseAdvisories: Boolean = true,
    val notifyFloodAlerts: Boolean = true,
    val notifyKuldihaSafari: Boolean = true,
    val notifyPanchalingeswar: Boolean = true,
    val notifySafeTideWindow: Boolean = true,
    val highTideAlertHoursBefore: Int = 2,
    val minTempThreshold: Int = 12,
    val maxTempThreshold: Int = 38,
    val preferredNewsCategories: List<String> = listOf("Civic", "Weather", "Defense", "Tourism", "Culture"),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "userId" to userId,
            "notifyCyclone" to notifyCyclone,
            "notifyHighTide" to notifyHighTide,
            "notifySevereHeat" to notifySevereHeat,
            "notifyRainfall" to notifyRainfall,
            "notifyLocalEvents" to notifyLocalEvents,
            "notifyTempleFestivals" to notifyTempleFestivals,
            "notifyBreakingNews" to notifyBreakingNews,
            "notifyTourismHotspots" to notifyTourismHotspots,
            "notifyDefenseAdvisories" to notifyDefenseAdvisories,
            "notifyFloodAlerts" to notifyFloodAlerts,
            "notifyKuldihaSafari" to notifyKuldihaSafari,
            "notifyPanchalingeswar" to notifyPanchalingeswar,
            "notifySafeTideWindow" to notifySafeTideWindow,
            "highTideAlertHoursBefore" to highTideAlertHoursBefore,
            "minTempThreshold" to minTempThreshold,
            "maxTempThreshold" to maxTempThreshold,
            "preferredNewsCategories" to preferredNewsCategories,
            "updatedAt" to updatedAt
        )
        return map.filterValues { it != null }
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): UserWeatherPreferences {
            @Suppress("UNCHECKED_CAST")
            val newsCats = (data["preferredNewsCategories"] as? List<*>)
                ?.mapNotNull { it?.toString() }
                ?.takeIf { it.isNotEmpty() }
                ?: listOf("Civic", "Weather", "Defense", "Tourism", "Culture")

            return UserWeatherPreferences(
                userId = data["userId"] as? String ?: "",
                notifyCyclone = data["notifyCyclone"] as? Boolean ?: true,
                notifyHighTide = data["notifyHighTide"] as? Boolean ?: true,
                notifySevereHeat = data["notifySevereHeat"] as? Boolean ?: true,
                notifyRainfall = data["notifyRainfall"] as? Boolean ?: true,
                notifyLocalEvents = data["notifyLocalEvents"] as? Boolean ?: true,
                notifyTempleFestivals = data["notifyTempleFestivals"] as? Boolean ?: true,
                notifyBreakingNews = data["notifyBreakingNews"] as? Boolean ?: true,
                notifyTourismHotspots = data["notifyTourismHotspots"] as? Boolean ?: true,
                notifyDefenseAdvisories = data["notifyDefenseAdvisories"] as? Boolean ?: true,
                notifyFloodAlerts = data["notifyFloodAlerts"] as? Boolean ?: true,
                notifyKuldihaSafari = data["notifyKuldihaSafari"] as? Boolean ?: true,
                notifyPanchalingeswar = data["notifyPanchalingeswar"] as? Boolean ?: true,
                notifySafeTideWindow = data["notifySafeTideWindow"] as? Boolean ?: true,
                highTideAlertHoursBefore = (data["highTideAlertHoursBefore"] as? Number)?.toInt() ?: 2,
                minTempThreshold = (data["minTempThreshold"] as? Number)?.toInt() ?: 12,
                maxTempThreshold = (data["maxTempThreshold"] as? Number)?.toInt() ?: 38,
                preferredNewsCategories = newsCats,
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

data class TourismReviewCloudEntity(
    val reviewId: String = "",
    val spotId: String = "",
    val spotName: String = "",
    val userId: String = "",
    val userDisplayName: String = "",
    val userPhotoUrl: String? = null,
    val rating: Int = 5,
    val reviewText: String = "",
    val travelTip: String = "",
    val tipCategory: String = "Best Time to Visit",
    val photoUris: List<String> = emptyList(),
    val visitSeason: String = "Winter",
    val moderationStatus: String = "PENDING", // "PENDING", "APPROVED", "FLAGGED", "REJECTED"
    val moderatorNote: String = "",
    val helpfulVotes: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "reviewId" to reviewId,
            "spotId" to spotId,
            "spotName" to spotName,
            "userId" to userId,
            "userDisplayName" to userDisplayName,
            "userPhotoUrl" to userPhotoUrl,
            "rating" to rating,
            "reviewText" to reviewText,
            "travelTip" to travelTip,
            "tipCategory" to tipCategory,
            "photoUris" to photoUris,
            "visitSeason" to visitSeason,
            "moderationStatus" to moderationStatus,
            "moderatorNote" to moderatorNote,
            "helpfulVotes" to helpfulVotes,
            "createdAt" to createdAt
        )
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): TourismReviewCloudEntity {
            val photos = (data["photoUris"] as? List<*>)?.mapNotNull { it?.toString() } ?: emptyList()
            return TourismReviewCloudEntity(
                reviewId = data["reviewId"] as? String ?: "",
                spotId = data["spotId"] as? String ?: "",
                spotName = data["spotName"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                userDisplayName = data["userDisplayName"] as? String ?: "Balasore Visitor",
                userPhotoUrl = data["userPhotoUrl"] as? String,
                rating = (data["rating"] as? Number)?.toInt() ?: 5,
                reviewText = data["reviewText"] as? String ?: "",
                travelTip = data["travelTip"] as? String ?: "",
                tipCategory = data["tipCategory"] as? String ?: "Best Time to Visit",
                photoUris = photos,
                visitSeason = data["visitSeason"] as? String ?: "Winter",
                moderationStatus = data["moderationStatus"] as? String ?: "APPROVED",
                moderatorNote = data["moderatorNote"] as? String ?: "",
                helpfulVotes = (data["helpfulVotes"] as? Number)?.toInt() ?: 0,
                createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

data class TourismBookmarkCloudItem(
    val spotId: String = "",
    val userId: String = "",
    val spotName: String = "",
    val category: String = "",
    val location: String = "",
    val rating: Double = 4.5,
    val bookmarkedAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "spotId" to spotId,
            "userId" to userId,
            "spotName" to spotName,
            "category" to category,
            "location" to location,
            "rating" to rating,
            "bookmarkedAt" to bookmarkedAt
        )
        return map.filterValues { it != null }
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): TourismBookmarkCloudItem {
            return TourismBookmarkCloudItem(
                spotId = data["spotId"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                spotName = data["spotName"] as? String ?: "",
                category = data["category"] as? String ?: "Tourism",
                location = data["location"] as? String ?: "Balasore",
                rating = (data["rating"] as? Number)?.toDouble() ?: 4.5,
                bookmarkedAt = (data["bookmarkedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

data class FavoriteNewsCloudItem(
    val newsId: String = "",
    val userId: String = "",
    val title: String = "",
    val source: String = "",
    val category: String = "",
    val publishedAt: String = "",
    val url: String = "",
    val savedAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>(
            "newsId" to newsId,
            "userId" to userId,
            "title" to title,
            "source" to source,
            "category" to category,
            "publishedAt" to publishedAt,
            "url" to url,
            "savedAt" to savedAt
        )
        return map.filterValues { it != null }
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): FavoriteNewsCloudItem {
            return FavoriteNewsCloudItem(
                newsId = data["newsId"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                title = data["title"] as? String ?: "",
                source = data["source"] as? String ?: "Balasore District News",
                category = data["category"] as? String ?: "Civic",
                publishedAt = data["publishedAt"] as? String ?: "",
                url = data["url"] as? String ?: "",
                savedAt = (data["savedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Cloud model for citizen civic reports persisted in Firestore collection 'user_reports'
 * matching schema FirestoreUserReport in firebase-blueprint.json and firestore.rules.
 */
data class FirestoreUserReport(
    val id: String = "",
    val userId: String = "",
    val userName: String = "Balasore Citizen",
    val title: String = "",
    val category: String = "pothole", // "pothole", "waterlogging", "road", "drainage", "garbage", "electricity", "water", "other"
    val description: String = "",
    val wardLocation: String = "Balasore Municipality",
    val status: String = "SUBMITTED", // "SUBMITTED", "IN_PROGRESS", "RESOLVED", "REJECTED"
    val urgency: String = "MEDIUM", // "LOW", "MEDIUM", "HIGH", "EMERGENCY"
    val latitude: Double? = 21.4934,
    val longitude: Double? = 86.9135,
    val photoUrl: String? = null,
    val reportedTimestamp: Long = System.currentTimeMillis(),
    val lastUpdatedTimestamp: Long = System.currentTimeMillis(),
    val resolutionNotes: String? = null
) {
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "id" to id,
            "userId" to userId,
            "userName" to userName,
            "title" to title,
            "category" to category,
            "description" to description,
            "wardLocation" to wardLocation,
            "status" to status,
            "urgency" to urgency,
            "latitude" to latitude,
            "longitude" to longitude,
            "photoUrl" to photoUrl,
            "reportedTimestamp" to reportedTimestamp,
            "lastUpdatedTimestamp" to lastUpdatedTimestamp,
            "resolutionNotes" to resolutionNotes
        )
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): FirestoreUserReport {
            return FirestoreUserReport(
                id = data["id"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                userName = data["userName"] as? String ?: "Balasore Citizen",
                title = data["title"] as? String ?: "",
                category = data["category"] as? String ?: "pothole",
                description = data["description"] as? String ?: "",
                wardLocation = data["wardLocation"] as? String ?: "Balasore",
                status = data["status"] as? String ?: "SUBMITTED",
                urgency = data["urgency"] as? String ?: "MEDIUM",
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 21.4934,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: 86.9135,
                photoUrl = data["photoUrl"] as? String,
                reportedTimestamp = (data["reportedTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                lastUpdatedTimestamp = (data["lastUpdatedTimestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                resolutionNotes = data["resolutionNotes"] as? String
            )
        }
    }
}
