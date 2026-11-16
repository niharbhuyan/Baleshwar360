package com.example.data.model

enum class FeedbackType(
    val labelEn: String,
    val labelOdia: String,
    val iconEmoji: String = "💬"
) {
    SUGGESTION("Suggestion", "ପରାମର୍ଶ", "💡"),
    REPORT("Issue Report", "ସମସ୍ୟା ରିପୋର୍ଟ", "🚨"),
    BUG_REPORT("Bug Report", "ତ୍ରୁଟି ରିପୋର୍ଟ", "🐞"),
    FEATURE_REQUEST("Feature Request", "ନୂତନ ବୈଶିଷ୍ଟ୍ୟ", "✨"),
    CONTENT_CORRECTION("Data Update", "ତଥ୍ୟ ସଂଶୋଧନ", "📝"),
    GENERAL_PRAISE("Appreciation", "ପ୍ରଶଂସା", "🙌");

    fun getDisplayName(language: AppLanguage): String =
        if (language == AppLanguage.ODIA) labelOdia else labelEn
}

data class FeedbackReport(
    val id: String = "fb_${System.currentTimeMillis()}",
    val type: FeedbackType = FeedbackType.SUGGESTION,
    val category: String = "General",
    val categoryArea: String = category,
    val title: String = "",
    val description: String = "",
    val userName: String = "Balasore Citizen",
    val contactInfo: String = "",
    val contactEmailOrPhone: String = contactInfo,
    val locality: String = "Balasore Town",
    val rating: Int = 5,
    val timestamp: Long = System.currentTimeMillis(),
    val timestampMillis: Long = timestamp,
    val status: String = "Submitted"
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "type" to type.name,
        "category" to category,
        "categoryArea" to categoryArea,
        "title" to title,
        "description" to description,
        "userName" to userName,
        "contactInfo" to contactInfo,
        "contactEmailOrPhone" to contactEmailOrPhone,
        "locality" to locality,
        "rating" to rating,
        "timestamp" to timestamp,
        "timestampMillis" to timestampMillis,
        "status" to status
    )

    companion object {
        fun fromFirestoreMap(map: Map<String, Any?>): FeedbackReport {
            val typeStr = map["type"] as? String ?: FeedbackType.SUGGESTION.name
            val resolvedType = runCatching { FeedbackType.valueOf(typeStr) }.getOrDefault(FeedbackType.SUGGESTION)
            val cat = map["category"] as? String ?: (map["categoryArea"] as? String ?: "General")
            val contact = map["contactInfo"] as? String ?: (map["contactEmailOrPhone"] as? String ?: "")
            val ts = (map["timestamp"] as? Number)?.toLong()
                ?: (map["timestampMillis"] as? Number)?.toLong()
                ?: System.currentTimeMillis()
            return FeedbackReport(
                id = map["id"] as? String ?: "fb_$ts",
                type = resolvedType,
                category = cat,
                categoryArea = map["categoryArea"] as? String ?: cat,
                title = map["title"] as? String ?: "",
                description = map["description"] as? String ?: "",
                userName = map["userName"] as? String ?: "Balasore Citizen",
                contactInfo = contact,
                contactEmailOrPhone = map["contactEmailOrPhone"] as? String ?: contact,
                locality = map["locality"] as? String ?: "Balasore Town",
                rating = (map["rating"] as? Number)?.toInt() ?: 5,
                timestamp = ts,
                timestampMillis = ts,
                status = map["status"] as? String ?: "Submitted"
            )
        }
    }
}

data class HeritageAudioStory(
    val id: String,
    val titleEn: String = "",
    val titleOd: String = "",
    val monumentEn: String = "",
    val monumentOd: String = "",
    val era: String = "",
    val distanceKm: Double = 0.0,
    val narrationScriptEn: String = "",
    val narrationScriptOd: String = "",
    val lyriaMusicPrompt: String = "",
    val suggestedAudioTrackName: String = "",
    val durationSeconds: Int = 150,
    val historicalSignificance: String = "",
    val title: String = titleEn,
    val odiaTitle: String = titleOd,
    val narrator: String = "Balasore Heritage Guide",
    val locationTag: String = monumentEn,
    val summary: String = historicalSignificance,
    val fullScriptEn: String = narrationScriptEn,
    val fullScriptOdia: String = narrationScriptOd,
    val ambientSoundDesc: String = lyriaMusicPrompt
)

data class BloodDonorContact(
    val id: String,
    val donorName: String = "",
    val bloodGroup: String = "",
    val locality: String = "",
    val phone: String = "",
    val lastDonatedDate: String = "2024-07-01",
    val availabilityStatus: String = "Available",
    val block: String = locality,
    val isAvailableNow: Boolean = true,
    val lastDonatedMonthsAgo: Int = 4,
    val verifiedClub: String = "Balasore Youth Blood Donor Seva",
    val fullName: String = donorName,
    val blockTown: String = locality,
    val contactPhone: String = phone
)

data class RareBloodAlert(
    val id: String,
    val patientNameOrBed: String = "",
    val bloodGroup: String = "",
    val unitsNeeded: Int = 1,
    val hospital: String = "",
    val contactPerson: String = "",
    val contactPhone: String = "",
    val postedTimeAgo: String = "Just now",
    val isUrgent: Boolean = true,
    val bloodGroupNeeded: String = bloodGroup,
    val hospitalName: String = hospital,
    val wardOrBed: String = patientNameOrBed,
    val unitsRequired: Int = unitsNeeded,
    val urgency: String = if (isUrgent) "CRITICAL" else "HIGH",
    val attendantPhone: String = contactPhone
)

data class HospitalBedPulse(
    val hospitalId: String = "",
    val name: String = "",
    val odiaName: String = "",
    val location: String = "",
    val totalIcuBeds: Int = 20,
    val availableIcuBeds: Int = 5,
    val totalOxygenBeds: Int = 100,
    val availableOxygenBeds: Int = 25,
    val dialysisSlotsTotal: Int = 10,
    val availableDialysisSlots: Int = 2,
    val emergencyContact: String = "108",
    val lastUpdatedMinutesAgo: Int = 10,
    val id: String = hospitalId,
    val hospitalName: String = name,
    val category: String = "Multi-Specialty",
    val generalBedsAvailable: Int = availableOxygenBeds,
    val icuBedsAvailable: Int = availableIcuBeds,
    val oxygenBedsAvailable: Int = availableOxygenBeds,
    val snakebiteAntiVenomStock: String = "Available (24x7)",
    val dialysisSlotsOpen: Int = availableDialysisSlots,
    val emergencyPhone: String = emergencyContact,
    val dutyDoctorStatus: String = "Casualty MO On-Duty"
)

enum class BatteryImpactTier(val label: String, val odiaLabel: String) {
    ULTRA_LOW("Ultra Low (<0.5%/hr)", "ଅତି କମ୍ ବ୍ୟାଟେରୀ ବ୍ୟୟ"),
    MINIMAL("Minimal (~0.8%/hr)", "ସର୍ବନିମ୍ନ ବ୍ୟାଟେରୀ ବ୍ୟୟ"),
    MODERATE("Moderate (~1.5%/hr)", "ମଧ୍ୟମ ବ୍ୟାଟେରୀ ବ୍ୟୟ"),
    ELEVATED("Elevated (>2.5%/hr)", "ଅଧିକ ବ୍ୟାଟେରୀ ବ୍ୟୟ")
}

data class BackgroundServiceHealthProfile(
    val serviceId: String,
    val serviceName: String,
    val odiaServiceName: String,
    val isEnabled: Boolean = true,
    val isCurrentlyThrottled: Boolean = false,
    val effectiveIntervalMinutes: Int = 30,
    val estimatedMahPerDay: Float = 8.5f,
    val lastRunTimestamp: Long = System.currentTimeMillis(),
    val statusSummary: String = "Optimized"
)

data class ActionableLongevityTip(
    val id: String,
    val title: String,
    val odiaTitle: String,
    val description: String,
    val estimatedExtraMinutes: Int = 35,
    val isApplied: Boolean = false,
    val actionLabel: String = "Optimize"
)

data class BatteryHealthMetrics(
    val batteryPercentage: Int = 85,
    val isCharging: Boolean = false,
    val isPowerSaveMode: Boolean = false,
    val isDozeModeActive: Boolean = false,
    val isScreenInteractive: Boolean = true,
    val batteryTemperatureCelsius: Float = 31.0f,
    val batteryVoltageMv: Int = 4100,
    val currentImpactTier: BatteryImpactTier = BatteryImpactTier.MINIMAL,
    val estimatedHourlyDrainPercent: Float = 0.8f,
    val networkCallsSavedToday: Int = 18,
    val throttledSyncsToday: Int = 5,
    val deferredJobsCount: Int = 2,
    val estimatedMahSavedToday: Int = 86,
    val activePollingIntervalMinutes: Int = 30,
    val baseUserPollingIntervalMinutes: Int = 30,
    val backgroundServices: List<BackgroundServiceHealthProfile> = emptyList(),
    val longevityTips: List<ActionableLongevityTip> = emptyList(),
    val adaptiveReasonBanner: String = "Adaptive Coastal Sync active"
)
