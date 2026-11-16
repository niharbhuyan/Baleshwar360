package com.example.data.fcm

import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.NewsArticleEntity
import com.example.data.local.WeatherCacheEntity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Firebase Cloud Messaging Service for handling real-time push notifications
 * regarding urgent weather alerts (cyclones, tidal swells, Chandipur warnings)
 * and breaking civic/news updates in the Balasore area.
 */
class BalasoreFirebaseMessagingService : FirebaseMessagingService() {

    @Suppress("OVERRIDE_DEPRECATION", "DEPRECATION")
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New Firebase Cloud Messaging registration token: $token")
        FcmManager.saveToken(applicationContext, token)

        // Resubscribe to default topics
        FcmManager.subscribeToTopic(FcmManager.TOPIC_WEATHER_ALERTS)
        FcmManager.subscribeToTopic(FcmManager.TOPIC_BREAKING_NEWS)
        FcmManager.subscribeToTopic(FcmManager.TOPIC_TRANSPORT_ALERTS)
        if (FcmManager.isChandipurHighTideAlertsEnabled(applicationContext)) {
            FcmManager.subscribeToTopic(FcmManager.TOPIC_CHANDIPUR_HIGH_TIDE)
        }
        if (FcmManager.isTourismHotspotsEnabled(applicationContext)) {
            FcmManager.subscribeToTopic(FcmManager.TOPIC_TOURISM_HOTSPOTS)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Received FCM message from: ${remoteMessage.from}")

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        val type = data["type"] ?: "GENERAL"
        val title = data["title"] ?: notification?.title ?: "Baleshwar 360 Alert"
        val body = data["body"] ?: data["message"] ?: notification?.body ?: "New urgent update received for Baleshwar 360."
        val alertLevel = data["alert_level"] ?: "URGENT"
        val articleId = data["article_id"]

        when {
            type.contains("TRANSPORT", ignoreCase = true) ||
                    type.contains("TRAIN", ignoreCase = true) ||
                    type.contains("BUS", ignoreCase = true) ||
                    type.contains("DELAY", ignoreCase = true) ||
                    title.contains("Train", ignoreCase = true) ||
                    title.contains("Bus Delay", ignoreCase = true) -> {
                val transportNumber = data["transport_number"] ?: data["identifier"] ?: "12821"
                val serviceName = data["service_name"] ?: title
                val delayMinutes = data["delay_minutes"]?.toIntOrNull() ?: 20
                val platformOrBay = data["platform_or_bay"] ?: data["platform"] ?: "PF 1"
                val transportType = data["transport_type"] ?: if (serviceName.contains("Bus", true)) "BUS" else "TRAIN"
                val reason = data["reason"] ?: body

                // Update Room database cache asynchronously
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val repo = com.example.data.repository.TransportAlertRepository(applicationContext)
                        repo.recordDelayAlert(
                            transportType = transportType,
                            identifier = transportNumber,
                            serviceName = serviceName,
                            delayMinutes = delayMinutes,
                            scheduledTime = data["scheduled_time"] ?: "--:--",
                            estimatedTime = data["estimated_time"] ?: "--:--",
                            platformOrBay = platformOrBay,
                            reason = reason,
                            fcmMessageId = remoteMessage.messageId
                        )
                    } catch (e: Exception) {
                        Log.e(TAG, "Failed to cache transport alert to Room", e)
                    }
                }

                // Dispatch system notification
                BalasoreNotificationHelper.showTransportDelayNotification(
                    context = applicationContext,
                    title = title,
                    message = body,
                    transportNumber = transportNumber,
                    delayMinutes = delayMinutes,
                    platformOrBay = platformOrBay,
                    transportType = transportType
                )
            }

            type.contains("FLOOD", ignoreCase = true) ||
                    title.contains("Flood", ignoreCase = true) ||
                    title.contains("River", ignoreCase = true) ||
                    title.contains("Subarnarekha", ignoreCase = true) -> {
                val gauge = data["gauge"] ?: "Rajghat: 9.45m (Danger Level)"
                if (FcmManager.isWeatherAlertsEnabled(applicationContext)) {
                    BalasoreNotificationHelper.showCoastalFloodNotification(
                        context = applicationContext,
                        title = title,
                        message = body,
                        riverLevelGauge = gauge,
                        alertLevel = alertLevel
                    )
                }
            }

            type.contains("HIGH_TIDE", ignoreCase = true) ||
                    type.contains("CHANDIPUR_TIDE", ignoreCase = true) ||
                    title.contains("High Tide", ignoreCase = true) ||
                    (title.contains("Chandipur", ignoreCase = true) && title.contains("Tide", ignoreCase = true)) -> {
                if (FcmManager.isChandipurHighTideAlertsEnabled(applicationContext)) {
                    FcmManager.dispatchChandipurHighTidePushFromLocalData(
                        context = applicationContext,
                        customTitle = title,
                        customMessage = body
                    )
                } else {
                    Log.d(TAG, "Chandipur High Tide notification skipped (opt-out active in Settings)")
                }
            }

            type.contains("WEATHER", ignoreCase = true) ||
                    title.contains("Weather", ignoreCase = true) ||
                    title.contains("Cyclone", ignoreCase = true) ||
                    title.contains("Tide", ignoreCase = true) -> {
                // Update local Room database with latest weather alert
                updateRoomWeatherCache(title, body, alertLevel)

                // Dispatch high-priority system notification if user opted-in
                if (FcmManager.isWeatherAlertsEnabled(applicationContext)) {
                    BalasoreNotificationHelper.showWeatherAlertNotification(
                        context = applicationContext,
                        title = title,
                        message = body,
                        alertLevel = alertLevel
                    )
                } else {
                    Log.d(TAG, "Weather alert notification skipped (opt-out active)")
                }
            }

            type.contains("DEFENSE", ignoreCase = true) ||
                    type.contains("MISSILE", ignoreCase = true) ||
                    title.contains("ITR", ignoreCase = true) ||
                    title.contains("Chandipur", ignoreCase = true) ||
                    title.contains("Launch", ignoreCase = true) ||
                    title.contains("NOTAM", ignoreCase = true) -> {
                val notamWindow = data["notam_window"] ?: "08:00 AM - 12:30 PM"
                val perimeter = data["exclusion_perimeter"] ?: "15 km seaward"
                if (FcmManager.isDefenseAdvisoriesEnabled(applicationContext)) {
                    BalasoreNotificationHelper.showDefenseAdvisoryNotification(
                        context = applicationContext,
                        title = title,
                        message = body,
                        notamWindow = notamWindow,
                        exclusionPerimeterKm = perimeter
                    )
                } else {
                    Log.d(TAG, "Defense test notification skipped (opt-out active)")
                }
            }

            type.contains("NEWS", ignoreCase = true) ||
                    type.contains("BREAKING", ignoreCase = true) ||
                    title.contains("Breaking", ignoreCase = true) -> {
                // Insert into Room database
                insertBreakingNewsToRoom(title, body, data)

                // Dispatch system notification if user opted-in
                if (FcmManager.isBreakingNewsEnabled(applicationContext)) {
                    BalasoreNotificationHelper.showBreakingNewsNotification(
                        context = applicationContext,
                        title = title,
                        message = body,
                        articleId = articleId
                    )
                } else {
                    Log.d(TAG, "Breaking news notification skipped (opt-out active)")
                }
            }

            type.contains("TOURISM", ignoreCase = true) ||
                    type.contains("HOTSPOT", ignoreCase = true) ||
                    type.contains("HERITAGE", ignoreCase = true) ||
                    type.contains("SAFARI", ignoreCase = true) ||
                    title.contains("Hotspot", ignoreCase = true) ||
                    title.contains("Kuldiha", ignoreCase = true) ||
                    title.contains("Panchalingeswar", ignoreCase = true) ||
                    title.contains("Talasari", ignoreCase = true) ||
                    title.contains("Remuna", ignoreCase = true) -> {
                val spotId = data["spot_id"] ?: data["hotspot_id"] ?: "chandipur_beach"
                val spotName = data["spot_name"] ?: title
                val hotspotStatus = data["status"] ?: data["hotspot_status"] ?: "OPEN"
                val timingWindow = data["timing_window"] ?: data["timing"] ?: "Open Daily"

                if (FcmManager.isTourismHotspotsEnabled(applicationContext)) {
                    BalasoreNotificationHelper.showTourismHotspotNotification(
                        context = applicationContext,
                        title = title,
                        message = body,
                        spotId = spotId,
                        spotName = spotName,
                        timingWindow = timingWindow,
                        hotspotStatus = hotspotStatus
                    )
                } else {
                    Log.d(TAG, "Tourism hotspot notification skipped (opt-out active in profile preferences)")
                }
            }

            type.contains("WARD", ignoreCase = true) ||
                    data.containsKey("ward_number") ||
                    title.contains("Ward", ignoreCase = true) -> {
                val wardNumber = data["ward_number"]?.toIntOrNull() ?: 14
                val wardName = data["ward_name"] ?: "Ward $wardNumber"
                val alertType = data["alert_type"] ?: "WATER_SUPPLY"
                val department = data["department"] ?: "Balasore Municipality"

                com.example.data.notification.CivicNotificationHelper.notifyWardMunicipalAlert(
                    context = applicationContext,
                    wardNumber = wardNumber,
                    wardName = wardName,
                    title = title,
                    message = body,
                    alertType = alertType,
                    department = department
                )
            }

            else -> {
                // Default to breaking news notification
                insertBreakingNewsToRoom(title, body, data)
                if (FcmManager.isBreakingNewsEnabled(applicationContext)) {
                    BalasoreNotificationHelper.showBreakingNewsNotification(
                        context = applicationContext,
                        title = title,
                        message = body,
                        articleId = articleId
                    )
                }
            }
        }
    }

    private fun updateRoomWeatherCache(title: String, message: String, level: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val current = db.weatherDao().getWeatherCacheSync()
                if (current != null) {
                    val updated = current.copy(
                        alertLevel = if (level.isNotBlank()) level else "WARNING",
                        alertTitle = title,
                        alertMessage = message,
                        lastUpdated = System.currentTimeMillis()
                    )
                    db.weatherDao().insertOrUpdateWeather(updated)
                } else {
                    val initialWeather = WeatherCacheEntity(
                        temperature = 29.5,
                        apparentTemperature = 33.0,
                        weatherCode = 80,
                        weatherDescription = "Coastal Weather Alert Active",
                        windSpeed = 28.0,
                        windGusts = 50.0,
                        humidity = 84,
                        alertLevel = if (level.isNotBlank()) level else "WARNING",
                        alertTitle = title,
                        alertMessage = message,
                        tideState = "INCOMING",
                        tideDescription = "High tidal surge expected along Chandipur coastline.",
                        sunrise = "05:30 AM",
                        sunset = "06:10 PM",
                        uvIndex = 6.0,
                        maxTemp = 32.0,
                        minTemp = 26.0,
                        lastUpdated = System.currentTimeMillis()
                    )
                    db.weatherDao().insertOrUpdateWeather(initialWeather)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating Room weather cache from FCM", e)
            }
        }
    }

    private fun insertBreakingNewsToRoom(title: String, body: String, data: Map<String, String>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(applicationContext)
                val timeString = SimpleDateFormat("h:mm a, d MMM", Locale.getDefault()).format(Date())
                val newArticle = NewsArticleEntity(
                    title = title,
                    summary = body,
                    content = data["content"] ?: body,
                    category = data["category"] ?: "Civic",
                    source = data["source"] ?: "Baleshwar 360 Wire",
                    publishedAt = "Just now ($timeString)",
                    timestamp = System.currentTimeMillis(),
                    isBreaking = true,
                    isBookmarked = false
                )
                db.newsDao().insertArticles(listOf(newArticle))
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting breaking news into Room from FCM", e)
            }
        }
    }

    companion object {
        private const val TAG = "BalasoreFcmService"
    }
}
