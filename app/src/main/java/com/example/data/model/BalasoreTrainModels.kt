package com.example.data.model

/**
 * Categories of train classification for Balasore Junction (BLS).
 */
enum class TrainType(val labelEn: String, val labelOr: String) {
    VANDE_BHARAT("Vande Bharat Express", "ବନ୍ଦେ ଭାରତ ଏକ୍ସପ୍ରେସ୍"),
    SUPERFAST("Superfast Express", "ସୁପରଫାଷ୍ଟ ଏକ୍ସପ୍ରେସ୍"),
    EXPRESS_MAIL("Mail / Express", "ମେଲ୍ / ଏକ୍ସପ୍ରେସ୍"),
    JAN_SHATABDI("Jan Shatabdi Express", "ଜନ ଶତାବ୍ଦୀ ଏକ୍ସପ୍ରେସ୍"),
    MEMU_PASSENGER("MEMU Local Passenger", "ମେମୁ ଲୋକାଲ୍ ପାସେଞ୍ଜର")
}

/**
 * Status category describing current operating state relative to Balasore station.
 */
enum class TrainStatusCategory(val labelEn: String, val labelOr: String) {
    ON_TIME("On Time", "ସମୟାନୁବର୍ତ୍ତୀ"),
    DELAYED("Delayed", "ବିଳମ୍ବିତ"),
    APPROACHING("Approaching Balasore", "ବାଲେଶ୍ୱର ନିକଟବର୍ତ୍ତୀ"),
    AT_STATION("At Platform", "ପ୍ଲାଟଫର୍ମରେ ଉପସ୍ଥିତ"),
    DEPARTED("Departed", "ପ୍ରସ୍ଥାନ କରିସାରିଛି")
}

/**
 * Movement orientation of train relative to Balasore.
 */
enum class TrainMovement(val labelEn: String, val labelOr: String) {
    ALL("All Movements", "ସମସ୍ତ ଚଳାଚଳ"),
    ARRIVALS("Arrivals", "ଆଗମନ"),
    DEPARTURES("Departures", "ପ୍ରସ୍ଥାନ"),
    TRACKED_ONLY("My Tracked", "ମୋର ଚିହ୍ନିତ ଟ୍ରେନ୍")
}

/**
 * Real-time train status data model for Balasore Railway Station (BLS).
 */
data class TrainRunningStatus(
    val trainNumber: String,
    val trainNameEn: String,
    val trainNameOr: String,
    val type: TrainType,
    val originCode: String,
    val originNameEn: String,
    val originNameOr: String,
    val destinationCode: String,
    val destinationNameEn: String,
    val destinationNameOr: String,
    val scheduledArrival: String,
    val estimatedArrival: String,
    val scheduledDeparture: String,
    val estimatedDeparture: String,
    val delayMinutes: Int = 0,
    val platform: String = "PF 1",
    val isPlatformChanged: Boolean = false,
    val currentLocation: String,
    val currentLocationOr: String,
    val speedKmh: Int = 0,
    val distanceToBalasoreKm: Double = 0.0,
    val statusCategory: TrainStatusCategory,
    val nextStationName: String,
    val coachComposition: String = "Engine • Gen • SL • 3A • 2A • 1A",
    val runningDays: String = "Daily",
    val haltsBalasoreMins: Int = 2,
    val isTerminatingAtBalasore: Boolean = false
)

/**
 * User-configurable push notification settings for a specific train.
 */
data class TrainNotificationPreference(
    val trainNumber: String,
    val isTrackingEnabled: Boolean = true,
    val notifyOnApproaching: Boolean = true,
    val notifyOnDelay: Boolean = true,
    val notifyOnPlatformChange: Boolean = true,
    val notifyOnArrival: Boolean = true,
    val advanceMinutes: Int = 15,
    val lastNotifiedStatus: String = "",
    val lastNotifiedTimestamp: Long = 0L
)
