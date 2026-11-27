package com.example.data.fcm

import android.content.Context
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.SevereWeatherAlertEntity
import com.example.data.notification.BalasoreSevereWeatherAlertWorker
import com.example.data.notification.SevereWeatherDetection
import com.example.data.remote.OpenWeatherAlertDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Outcome returned when [FcmSevereWeatherHandler] processes a severe weather alert
 * from OpenWeatherMap or an incoming Firebase Cloud Messaging payload.
 */
data class FcmSevereWeatherDispatchResult(
    val dispatched: Boolean,
    val reason: String,
    val fcmPayload: Map<String, String>,
    val alertId: String? = null
)

/**
 * Firebase Cloud Messaging handler bridging [BalasoreSevereWeatherAlertWorker] and
 * [BalasoreFirebaseMessagingService] for extreme weather alerts in Balasore.
 *
 * Responsibilities:
 * 1. Maintains FCM topic subscriptions for Balasore severe weather push updates.
 * 2. Classifies official OpenWeatherMap alerts and incoming FCM payloads for 'Severe' alerts.
 * 3. Builds a standardized FCM data payload, persists the alert into Room (`severe_weather_alerts`),
 *    and triggers a high-priority local push notification via [BalasoreNotificationHelper].
 * 4. Handles data-only FCM sync triggers by enqueueing an immediate OpenWeatherMap worker check.
 */
object FcmSevereWeatherHandler {

    private const val TAG = "FcmSevereWeatherHandler"
    const val TOPIC_BALASORE_SEVERE_WEATHER = "balasore_severe_weather"
    const val ACTION_OPENWEATHER_SEVERE_CHECK = "OPENWEATHER_SEVERE_CHECK"

    private const val PREFS_NAME = "balasore_fcm_severe_weather_handler_prefs"
    private const val KEY_LAST_FINGERPRINT = "last_fcm_severe_fingerprint"
    private const val KEY_LAST_DISPATCH_MS = "last_fcm_severe_dispatch_ms"
    private const val KEY_LAST_EVENT_TITLE = "last_fcm_severe_title"
    private const val KEY_LAST_SOURCE = "last_fcm_severe_source"
    private const val ALERT_COOLDOWN_MS = 3L * 60L * 60L * 1000L // 3 hours

    private val severeKeywords = listOf(
        "severe",
        "extreme",
        "cyclone",
        "red warning",
        "red alert",
        "orange warning",
        "thunderstorm",
        "squall",
        "kalbaisakhi",
        "tornado",
        "gale",
        "storm surge",
        "heavy rain",
        "flood",
        "heatwave",
        "heat wave"
    )

    /**
     * Ensures this device is subscribed to the Balasore severe weather FCM topics
     * whenever the user has weather alerts enabled.
     */
    fun ensureFcmTopicSubscription(context: Context) {
        if (!FcmManager.isWeatherAlertsEnabled(context)) return
        runCatching {
            FcmManager.subscribeToTopic(FcmManager.TOPIC_WEATHER_ALERTS)
            FcmManager.subscribeToTopic(TOPIC_BALASORE_SEVERE_WEATHER)
        }.onFailure { err ->
            Log.w(TAG, "Unable to refresh FCM severe weather topic subscription: ${err.message}")
        }
    }

    /**
     * Returns true if an OpenWeatherMap [OpenWeatherAlertDto] represents a 'Severe' or extreme alert.
     */
    fun isSevereAlert(alert: OpenWeatherAlertDto): Boolean {
        val combined = buildString {
            append(alert.event.orEmpty())
            append(' ')
            append(alert.description.orEmpty())
            append(' ')
            append(alert.tags.joinToString(" "))
        }.lowercase(Locale.US)

        if (combined.isBlank()) return false
        return severeKeywords.any { keyword -> combined.contains(keyword) }
    }

    /**
     * Returns true if an incoming FCM data message is a wake-up signal asking the background
     * worker to immediately query OpenWeatherMap for 'Severe' Balasore alerts.
     */
    fun shouldTriggerBackgroundOpenWeatherCheck(data: Map<String, String>): Boolean {
        val type = data["type"].orEmpty().trim().uppercase(Locale.US)
        val action = data["action"].orEmpty().trim().uppercase(Locale.US)
        val checkFlag = data["check_openweather"].orEmpty().trim().lowercase(Locale.US)
        return type == ACTION_OPENWEATHER_SEVERE_CHECK ||
            type == "OPENWEATHER_SEVERE_SYNC" ||
            action == ACTION_OPENWEATHER_SEVERE_CHECK ||
            action == "CHECK_OPENWEATHER_SEVERE" ||
            checkFlag == "true" ||
            checkFlag == "1"
    }

    /**
     * Returns true if an incoming FCM message directly delivers a 'Severe' weather alert for Balasore.
     */
    fun isSevereAlertPayload(
        data: Map<String, String>,
        title: String? = null,
        body: String? = null
    ): Boolean {
        val type = data["type"].orEmpty().lowercase(Locale.US)
        val severity = (data["severity"] ?: data["alert_level"]).orEmpty().lowercase(Locale.US)
        val text = "${title.orEmpty()} ${body.orEmpty()} ${data["event"].orEmpty()}".lowercase(Locale.US)
        return type.contains("severe_weather") ||
            severity == "severe" ||
            severity == "extreme" ||
            severity == "critical" ||
            (text.contains("severe") && (text.contains("weather") || text.contains("balasore") || text.contains("cyclone") || text.contains("storm")))
    }

    /**
     * Constructs a standardized FCM data payload representing a detected OpenWeatherMap
     * 'Severe' weather alert for Balasore.
     */
    fun buildFcmPayload(
        detection: SevereWeatherDetection,
        senderName: String? = null,
        fcmMessageId: String? = null,
        timestampMillis: Long = System.currentTimeMillis()
    ): Map<String, String> {
        return linkedMapOf(
            "type" to "SEVERE_WEATHER",
            "severity" to "SEVERE",
            "alert_level" to detection.alertLevel,
            "location" to "Balasore",
            "title" to detection.title,
            "body" to detection.message,
            "fingerprint" to detection.fingerprint,
            "source" to "OPENWEATHERMAP",
            "sender" to (senderName?.takeIf { it.isNotBlank() } ?: "OpenWeatherMap / IMD Balasore"),
            "fcm_message_id" to (fcmMessageId ?: "owm_${detection.fingerprint}_$timestampMillis"),
            "timestamp" to timestampMillis.toString()
        )
    }

    /**
     * Handles a 'Severe' weather detection produced by [BalasoreSevereWeatherAlertWorker]:
     * validates user preferences and cooldown, builds the FCM payload, persists to Room
     * `severe_weather_alerts`, and dispatches the local high-priority push notification.
     */
    suspend fun handleOpenWeatherSevereAlert(
        context: Context,
        detection: SevereWeatherDetection,
        senderName: String? = null,
        fcmMessageId: String? = null,
        validFromMillis: Long = System.currentTimeMillis(),
        validUntilMillis: Long = System.currentTimeMillis() + 6L * 60L * 60L * 1000L,
        forceNotify: Boolean = false
    ): FcmSevereWeatherDispatchResult {
        val payload = buildFcmPayload(
            detection = detection,
            senderName = senderName,
            fcmMessageId = fcmMessageId
        )

        if (!FcmManager.isWeatherAlertsEnabled(context)) {
            Log.d(TAG, "Skipping severe weather local push: weather alerts disabled in Settings.")
            return FcmSevereWeatherDispatchResult(
                dispatched = false,
                reason = "OPT_OUT",
                fcmPayload = payload
            )
        }

        val alertId = "owm_fcm_${detection.fingerprint}_${validFromMillis / 60_000L}"
        persistSevereAlertToRoom(
            context = context,
            alertId = alertId,
            title = detection.title,
            summary = detection.message,
            severity = detection.alertLevel,
            issuingAuthority = payload["sender"] ?: "OpenWeatherMap / IMD Balasore",
            validFromMillis = validFromMillis,
            validUntilMillis = validUntilMillis
        )

        if (!forceNotify && !shouldDispatchAlert(context, detection.fingerprint)) {
            Log.d(TAG, "Skipping duplicate severe weather notification within cooldown (${detection.fingerprint}).")
            return FcmSevereWeatherDispatchResult(
                dispatched = false,
                reason = "COOLDOWN_DEDUP",
                fcmPayload = payload,
                alertId = alertId
            )
        }

        BalasoreNotificationHelper.createNotificationChannels(context)
        BalasoreNotificationHelper.showWeatherAlertNotification(
            context = context,
            title = detection.title,
            message = detection.message,
            alertLevel = detection.alertLevel
        )
        recordDispatchedAlert(
            context = context,
            fingerprint = detection.fingerprint,
            title = detection.title,
            source = payload["source"] ?: "OPENWEATHERMAP"
        )
        Log.i(TAG, "Dispatched severe Balasore local push notification via FCM handler: ${detection.title}")

        return FcmSevereWeatherDispatchResult(
            dispatched = true,
            reason = "DISPATCHED",
            fcmPayload = payload,
            alertId = alertId
        )
    }

    /**
     * Handles an incoming Firebase Cloud Messaging message routed from [BalasoreFirebaseMessagingService].
     * Either wakes up [BalasoreSevereWeatherAlertWorker] to poll OpenWeatherMap immediately,
     * or directly persists and fires a local push notification for the severe Balasore alert.
     */
    suspend fun handleIncomingFcmMessage(
        context: Context,
        data: Map<String, String>,
        title: String?,
        body: String?,
        messageId: String?
    ): FcmSevereWeatherDispatchResult {
        if (shouldTriggerBackgroundOpenWeatherCheck(data)) {
            val force = data["force_notify"].equals("true", ignoreCase = true)
            BalasoreSevereWeatherAlertWorker.triggerImmediateWeatherAlertCheck(
                context = context,
                fcmMessageId = messageId,
                forceNotify = force
            )
            Log.i(TAG, "Enqueued immediate OpenWeatherMap severe weather worker from FCM messageId=$messageId")
            return FcmSevereWeatherDispatchResult(
                dispatched = true,
                reason = "WORKER_ENQUEUED",
                fcmPayload = data
            )
        }

        val resolvedTitle = data["title"]?.takeIf { it.isNotBlank() }
            ?: title?.takeIf { it.isNotBlank() }
            ?: "Severe Weather Alert — Balasore"
        val resolvedBody = data["body"]?.takeIf { it.isNotBlank() }
            ?: data["message"]?.takeIf { it.isNotBlank() }
            ?: body?.takeIf { it.isNotBlank() }
            ?: "Severe weather conditions reported for Balasore. Please stay indoors and monitor advisories."
        val resolvedLevel = (data["severity"] ?: data["alert_level"]).orEmpty()
            .ifBlank { "SEVERE" }
            .uppercase(Locale.US)
        val fingerprint = data["fingerprint"]?.takeIf { it.isNotBlank() }
            ?: "FCM_${resolvedTitle.hashCode()}"

        val detection = SevereWeatherDetection(
            alertLevel = resolvedLevel,
            title = resolvedTitle,
            message = resolvedBody,
            fingerprint = fingerprint
        )

        return handleOpenWeatherSevereAlert(
            context = context,
            detection = detection,
            senderName = data["sender"] ?: "Firebase Cloud Messaging • IMD Balasore",
            fcmMessageId = messageId,
            forceNotify = true
        )
    }

    private suspend fun persistSevereAlertToRoom(
        context: Context,
        alertId: String,
        title: String,
        summary: String,
        severity: String,
        issuingAuthority: String,
        validFromMillis: Long,
        validUntilMillis: Long
    ) = withContext(Dispatchers.IO) {
        runCatching {
            val db = AppDatabase.getInstance(context.applicationContext)
            db.severeWeatherAlertDao().insertAlert(
                SevereWeatherAlertEntity(
                    id = alertId,
                    title = title,
                    odiaTitle = "ବାଲେଶ୍ୱର ଗୁରୁତର ପାଣିପାଗ ସତର୍କତା",
                    summary = summary,
                    severity = severity,
                    affectedZones = "Balasore District, Chandipur Coast & Subarnarekha Basin",
                    issuingAuthority = issuingAuthority,
                    validFromMillis = validFromMillis,
                    validUntilMillis = validUntilMillis,
                    receivedAtMillis = System.currentTimeMillis()
                )
            )
        }.onFailure { err ->
            Log.w(TAG, "Failed to persist severe weather alert into Room: ${err.message}")
        }
    }

    fun shouldDispatchAlert(
        context: Context,
        fingerprint: String,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastFp = prefs.getString(KEY_LAST_FINGERPRINT, null)
        val lastTs = prefs.getLong(KEY_LAST_DISPATCH_MS, 0L)
        return lastFp != fingerprint || (nowMillis - lastTs) >= ALERT_COOLDOWN_MS
    }

    private fun recordDispatchedAlert(
        context: Context,
        fingerprint: String,
        title: String,
        source: String
    ) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_LAST_FINGERPRINT, fingerprint)
            .putLong(KEY_LAST_DISPATCH_MS, System.currentTimeMillis())
            .putString(KEY_LAST_EVENT_TITLE, title)
            .putString(KEY_LAST_SOURCE, source)
            .apply()
    }
}
