package com.example.data.model

/**
 * Cloud entity model for documents in the 'news' collection.
 * Holds real-time district headlines, coastal alerts, and emergency bulletins.
 */
data class FirestoreNewsEntity(
    val id: String = "",
    val title: String = "",
    val titleOr: String = "",
    val summary: String = "",
    val content: String = "",
    val category: String = "Local",
    val imageUrl: String? = null,
    val source: String = "District Administration Balasore",
    val isBreaking: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val author: String = "Balasore District Bureau",
    val priority: Int = 1
) {
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "id" to id,
            "title" to title,
            "titleOr" to titleOr.ifBlank { null },
            "summary" to summary,
            "content" to content,
            "category" to category,
            "imageUrl" to imageUrl?.takeIf { it.isNotBlank() },
            "source" to source,
            "isBreaking" to isBreaking,
            "timestamp" to timestamp,
            "author" to author,
            "priority" to priority
        )
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): FirestoreNewsEntity {
            return FirestoreNewsEntity(
                id = data["id"] as? String ?: "",
                title = data["title"] as? String ?: "",
                titleOr = data["titleOr"] as? String ?: "",
                summary = data["summary"] as? String ?: "",
                content = data["content"] as? String ?: "",
                category = data["category"] as? String ?: "Local",
                imageUrl = data["imageUrl"] as? String,
                source = data["source"] as? String ?: "District Administration Balasore",
                isBreaking = data["isBreaking"] as? Boolean ?: false,
                timestamp = (data["timestamp"] as? Number)?.toLong() ?: System.currentTimeMillis(),
                author = data["author"] as? String ?: "Balasore District Bureau",
                priority = (data["priority"] as? Number)?.toInt() ?: 1
            )
        }
    }
}

/**
 * Cloud entity model for documents in the 'emergency_contacts' collection.
 * Holds official 24/7 helplines, police, hospitals, and cyclone shelters.
 */
data class FirestoreEmergencyContactEntity(
    val id: String = "",
    val name: String = "",
    val odiaName: String = "",
    val category: String = "HOSPITAL",
    val phone: String = "",
    val alternatePhone: String? = null,
    val address: String = "",
    val odiaAddress: String = "",
    val latitude: Double = 21.4934,
    val longitude: Double = 86.9135,
    val is24x7: Boolean = true,
    val capacityOrBeds: String? = null,
    val priority: Int = 1,
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toFirestoreMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any?>(
            "id" to id,
            "name" to name,
            "odiaName" to odiaName.ifBlank { null },
            "category" to category,
            "phone" to phone,
            "alternatePhone" to alternatePhone?.takeIf { it.isNotBlank() },
            "address" to address,
            "odiaAddress" to odiaAddress.ifBlank { null },
            "latitude" to latitude,
            "longitude" to longitude,
            "is24x7" to is24x7,
            "capacityOrBeds" to capacityOrBeds?.takeIf { it.isNotBlank() },
            "priority" to priority,
            "updatedAt" to updatedAt
        )
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): FirestoreEmergencyContactEntity {
            return FirestoreEmergencyContactEntity(
                id = data["id"] as? String ?: "",
                name = data["name"] as? String ?: "",
                odiaName = data["odiaName"] as? String ?: "",
                category = data["category"] as? String ?: "HOSPITAL",
                phone = data["phone"] as? String ?: "",
                alternatePhone = data["alternatePhone"] as? String,
                address = data["address"] as? String ?: "",
                odiaAddress = data["odiaAddress"] as? String ?: "",
                latitude = (data["latitude"] as? Number)?.toDouble() ?: 21.4934,
                longitude = (data["longitude"] as? Number)?.toDouble() ?: 86.9135,
                is24x7 = data["is24x7"] as? Boolean ?: true,
                capacityOrBeds = data["capacityOrBeds"] as? String,
                priority = (data["priority"] as? Number)?.toInt() ?: 1,
                updatedAt = (data["updatedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

/**
 * Cloud entity model for documents in the 'user_reports' collection.
 * Holds citizen civic reports and community problem submissions.
 */
data class FirestoreUserReportEntity(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val title: String = "",
    val category: String = "road",
    val description: String = "",
    val wardLocation: String = "",
    val status: String = "SUBMITTED",
    val urgency: String = "MEDIUM",
    val latitude: Double = 21.4934,
    val longitude: Double = 86.9135,
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
            "photoUrl" to photoUrl?.takeIf { it.isNotBlank() },
            "reportedTimestamp" to reportedTimestamp,
            "lastUpdatedTimestamp" to lastUpdatedTimestamp,
            "resolutionNotes" to resolutionNotes?.takeIf { it.isNotBlank() }
        )
        @Suppress("UNCHECKED_CAST")
        return map.filterValues { it != null } as Map<String, Any>
    }

    companion object {
        fun fromFirestoreMap(data: Map<String, Any?>): FirestoreUserReportEntity {
            return FirestoreUserReportEntity(
                id = data["id"] as? String ?: "",
                userId = data["userId"] as? String ?: "",
                userName = data["userName"] as? String ?: "Citizen",
                title = data["title"] as? String ?: "",
                category = data["category"] as? String ?: "road",
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
