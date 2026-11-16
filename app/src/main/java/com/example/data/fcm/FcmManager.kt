package com.example.data.fcm

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.NewsArticleEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FcmManager {

    private const val TAG = "FcmManager"
    private const val PREFS_NAME = "balasore_fcm_prefs"
    private const val KEY_FCM_TOKEN = "fcm_token"
    private const val KEY_WEATHER_ALERTS = "topic_weather_alerts"
    private const val KEY_DEFENSE_ADVISORIES = "topic_defense_advisories"
    private const val KEY_BREAKING_NEWS = "topic_breaking_news"
    private const val KEY_TRANSPORT_ALERTS = "topic_transport_alerts"
    private const val KEY_CHANDIPUR_HIGH_TIDE_ALERTS = "topic_chandipur_high_tide_alerts"
    private const val KEY_FLOOD_ALERTS = "topic_flood_alerts"
    private const val KEY_TOURISM_HOTSPOTS = "topic_tourism_hotspots"

    const val TOPIC_WEATHER_ALERTS = "balasore_weather_alerts"
    const val TOPIC_DEFENSE_ADVISORIES = "balasore_defense_advisories"
    const val TOPIC_BREAKING_NEWS = "balasore_breaking_news"
    const val TOPIC_TRANSPORT_ALERTS = "balasore_transport_alerts"
    const val TOPIC_CHANDIPUR_HIGH_TIDE = "chandipur_high_tide_alerts"
    const val TOPIC_EMERGENCY_FLOOD_ALERTS = "balasore_flood_alerts"
    const val TOPIC_TOURISM_HOTSPOTS = "balasore_tourism_hotspots"

    private val _fcmToken = MutableStateFlow<String?>(null)
    val fcmToken: StateFlow<String?> = _fcmToken.asStateFlow()

    private val _weatherAlertsEnabled = MutableStateFlow(true)
    val weatherAlertsEnabled: StateFlow<Boolean> = _weatherAlertsEnabled.asStateFlow()

    private val _defenseAdvisoriesEnabled = MutableStateFlow(true)
    val defenseAdvisoriesEnabled: StateFlow<Boolean> = _defenseAdvisoriesEnabled.asStateFlow()

    private val _breakingNewsEnabled = MutableStateFlow(true)
    val breakingNewsEnabled: StateFlow<Boolean> = _breakingNewsEnabled.asStateFlow()

    private val _transportAlertsEnabled = MutableStateFlow(true)
    val transportAlertsEnabled: StateFlow<Boolean> = _transportAlertsEnabled.asStateFlow()

    private val _chandipurHighTideAlertsEnabled = MutableStateFlow(true)
    val chandipurHighTideAlertsEnabled: StateFlow<Boolean> = _chandipurHighTideAlertsEnabled.asStateFlow()

    private val _floodAlertsEnabled = MutableStateFlow(true)
    val floodAlertsEnabled: StateFlow<Boolean> = _floodAlertsEnabled.asStateFlow()

    private val _tourismHotspotsEnabled = MutableStateFlow(true)
    val tourismHotspotsEnabled: StateFlow<Boolean> = _tourismHotspotsEnabled.asStateFlow()

    @Volatile
    private var isFirebaseReady: Boolean = false

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Verifies if Firebase has been genuinely initialized via google-services.json
     * with valid project credentials (not a placeholder or dummy configuration).
     */
    fun isFirebaseConfigured(context: Context): Boolean {
        return try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                false
            } else {
                val app = FirebaseApp.getInstance()
                val apiKey = app.options.apiKey
                apiKey.isNotBlank() &&
                        !apiKey.contains("Fallback", ignoreCase = true) &&
                        !apiKey.contains("dummy", ignoreCase = true) &&
                        !apiKey.contains("placeholder", ignoreCase = true)
            }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Initializes local notification channels and safely checks for Firebase Messaging.
     * If google-services.json is absent, runs in offline-first mode without triggering
     * Firebase Installations Service (FIS) network errors.
     */
    fun initialize(context: Context) {
        BalasoreNotificationHelper.createNotificationChannels(context)

        val prefs = getPrefs(context)
        _fcmToken.value = prefs.getString(KEY_FCM_TOKEN, null)
        _weatherAlertsEnabled.value = prefs.getBoolean(KEY_WEATHER_ALERTS, true)
        _defenseAdvisoriesEnabled.value = prefs.getBoolean(KEY_DEFENSE_ADVISORIES, true)
        _breakingNewsEnabled.value = prefs.getBoolean(KEY_BREAKING_NEWS, true)
        _transportAlertsEnabled.value = prefs.getBoolean(KEY_TRANSPORT_ALERTS, true)
        val highTidePref = prefs.getBoolean(
            KEY_CHANDIPUR_HIGH_TIDE_ALERTS,
            com.example.data.notification.TideAndWeatherNotificationManager.isTideAlertsEnabled(context)
        )
        _chandipurHighTideAlertsEnabled.value = highTidePref
        _floodAlertsEnabled.value = prefs.getBoolean(KEY_FLOOD_ALERTS, true)
        _tourismHotspotsEnabled.value = prefs.getBoolean(KEY_TOURISM_HOTSPOTS, true)

        isFirebaseReady = isFirebaseConfigured(context)

        if (!isFirebaseReady) {
            Log.i(TAG, "Running in offline-first mode: google-services.json not configured. Local Room database and notification engine active.")
            return
        }

        try {
            FirebaseMessaging.getInstance().isAutoInitEnabled = true
            @Suppress("DEPRECATION")
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    Log.d(TAG, "FCM registration token obtained: $token")
                    saveToken(context, token)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "FCM registration token retrieval deferred: ${e.message}")
                }

            // Ensure topics are subscribed if enabled
            if (_weatherAlertsEnabled.value) {
                subscribeToTopic(TOPIC_WEATHER_ALERTS)
            }
            if (_defenseAdvisoriesEnabled.value) {
                subscribeToTopic(TOPIC_DEFENSE_ADVISORIES)
            }
            if (_breakingNewsEnabled.value) {
                subscribeToTopic(TOPIC_BREAKING_NEWS)
            }
            if (_transportAlertsEnabled.value) {
                subscribeToTopic(TOPIC_TRANSPORT_ALERTS)
            }
            if (_chandipurHighTideAlertsEnabled.value) {
                subscribeToTopic(TOPIC_CHANDIPUR_HIGH_TIDE)
            }
            if (_tourismHotspotsEnabled.value) {
                subscribeToTopic(TOPIC_TOURISM_HOTSPOTS)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Non-blocking FCM initialization notice: ${e.message}")
        }
    }

    /**
     * Manually requests a fresh Firebase Cloud Messaging registration token if configured.
     */
    fun refreshToken(context: Context) {
        if (!isFirebaseConfigured(context)) {
            Log.i(TAG, "Token refresh skipped: Firebase not configured via google-services.json.")
            return
        }
        try {
            @Suppress("DEPRECATION")
            FirebaseMessaging.getInstance().token
                .addOnSuccessListener { token ->
                    Log.d(TAG, "FCM registration token refreshed: $token")
                    saveToken(context, token)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Failed to refresh FCM registration token", e)
                }
        } catch (e: Exception) {
            Log.w(TAG, "Error during FCM token refresh: ${e.message}")
        }
    }

    fun saveToken(context: Context, token: String) {
        _fcmToken.value = token
        getPrefs(context).edit().putString(KEY_FCM_TOKEN, token).apply()
    }

    fun isWeatherAlertsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_WEATHER_ALERTS, true)
    }

    fun isDefenseAdvisoriesEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_DEFENSE_ADVISORIES, true)
    }

    fun isBreakingNewsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_BREAKING_NEWS, true)
    }

    fun setWeatherAlertsEnabled(context: Context, enabled: Boolean) {
        _weatherAlertsEnabled.value = enabled
        getPrefs(context).edit().putBoolean(KEY_WEATHER_ALERTS, enabled).apply()
        if (enabled) {
            subscribeToTopic(TOPIC_WEATHER_ALERTS)
        } else {
            unsubscribeFromTopic(TOPIC_WEATHER_ALERTS)
        }
    }

    fun setDefenseAdvisoriesEnabled(context: Context, enabled: Boolean) {
        _defenseAdvisoriesEnabled.value = enabled
        getPrefs(context).edit().putBoolean(KEY_DEFENSE_ADVISORIES, enabled).apply()
        if (enabled) {
            subscribeToTopic(TOPIC_DEFENSE_ADVISORIES)
        } else {
            unsubscribeFromTopic(TOPIC_DEFENSE_ADVISORIES)
        }
    }

    fun setBreakingNewsEnabled(context: Context, enabled: Boolean) {
        _breakingNewsEnabled.value = enabled
        getPrefs(context).edit().putBoolean(KEY_BREAKING_NEWS, enabled).apply()
        if (enabled) {
            subscribeToTopic(TOPIC_BREAKING_NEWS)
        } else {
            unsubscribeFromTopic(TOPIC_BREAKING_NEWS)
        }
    }

    fun isTransportAlertsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TRANSPORT_ALERTS, true)
    }

    fun setTransportAlertsEnabled(context: Context, enabled: Boolean) {
        _transportAlertsEnabled.value = enabled
        getPrefs(context).edit().putBoolean(KEY_TRANSPORT_ALERTS, enabled).apply()
        if (enabled) {
            subscribeToTopic(TOPIC_TRANSPORT_ALERTS)
        } else {
            unsubscribeFromTopic(TOPIC_TRANSPORT_ALERTS)
        }
    }

    fun isChandipurHighTideAlertsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(
            KEY_CHANDIPUR_HIGH_TIDE_ALERTS,
            com.example.data.notification.TideAndWeatherNotificationManager.isTideAlertsEnabled(context)
        )
    }

    /**
     * Toggles Chandipur Beach 'High Tide' coastal safety push notifications on/off.
     * Synchronizes both FCM topic subscription and local TideAndWeatherNotificationManager preferences.
     */
    fun setChandipurHighTideAlertsEnabled(context: Context, enabled: Boolean) {
        _chandipurHighTideAlertsEnabled.value = enabled
        getPrefs(context).edit().putBoolean(KEY_CHANDIPUR_HIGH_TIDE_ALERTS, enabled).apply()
        com.example.data.notification.TideAndWeatherNotificationManager.setTideAlertsEnabled(context, enabled)
        if (enabled) {
            subscribeToTopic(TOPIC_CHANDIPUR_HIGH_TIDE)
        } else {
            unsubscribeFromTopic(TOPIC_CHANDIPUR_HIGH_TIDE)
        }
    }

    fun isFloodAlertsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_FLOOD_ALERTS, true)
    }

    /**
     * Toggles Emergency Flood & River Discharge Push Notifications (Subarnarekha & Budhabalanga).
     */
    fun setFloodAlertsEnabled(context: Context, enabled: Boolean) {
        _floodAlertsEnabled.value = enabled
        getPrefs(context).edit().putBoolean(KEY_FLOOD_ALERTS, enabled).apply()
        if (enabled) {
            subscribeToTopic(TOPIC_EMERGENCY_FLOOD_ALERTS)
        } else {
            unsubscribeFromTopic(TOPIC_EMERGENCY_FLOOD_ALERTS)
        }
    }

    fun isTourismHotspotsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_TOURISM_HOTSPOTS, true)
    }

    /**
     * Toggles Tourism Hotspot & Cultural Heritage Push Notifications (Chandipur walks, Kuldiha safari, Panchalingeswar).
     */
    fun setTourismHotspotsEnabled(context: Context, enabled: Boolean) {
        _tourismHotspotsEnabled.value = enabled
        getPrefs(context).edit().putBoolean(KEY_TOURISM_HOTSPOTS, enabled).apply()
        if (enabled) {
            subscribeToTopic(TOPIC_TOURISM_HOTSPOTS)
        } else {
            unsubscribeFromTopic(TOPIC_TOURISM_HOTSPOTS)
        }
    }

    /**
     * Triggers a live sample push notification for breaking district news.
     */
    fun triggerSampleBreakingNews(context: Context) {
        BalasoreNotificationHelper.triggerSampleBreakingNews(context)
    }

    /**
     * Triggers a live sample push notification for critical coastal weather alerts.
     */
    fun triggerSampleWeatherAlert(context: Context) {
        BalasoreNotificationHelper.triggerSampleWeatherAlert(context)
    }

    /**
     * Triggers a live sample push notification for tourism hotspot updates.
     */
    fun triggerSampleTourismHotspot(context: Context) {
        BalasoreNotificationHelper.triggerSampleTourismHotspot(context)
    }

    /**
     * Synchronizes FCM push notification subscriptions based on the user's selected local news preferences.
     * When users select news categories (e.g. Defense, Emergency/Weather, Local/Politics), this automatically
     * configures topic subscriptions for critical 'Defense Coastal Advisories' and 'Emergency Flood Alerts'.
     */
    fun syncPushSubscriptionsWithNewsPreferences(context: Context, preferredCategories: Set<String>) {
        if (preferredCategories.isEmpty() || preferredCategories.contains("All")) {
            setDefenseAdvisoriesEnabled(context, true)
            setFloodAlertsEnabled(context, true)
            setWeatherAlertsEnabled(context, true)
            setBreakingNewsEnabled(context, true)
            return
        }

        val hasDefense = preferredCategories.any { it.contains("Defense", ignoreCase = true) }
        val hasFloodOrEmergency = preferredCategories.any {
            it.contains("Emergency", ignoreCase = true) || it.contains("Weather", ignoreCase = true)
        }
        val hasLocalNews = preferredCategories.any {
            it.contains("Local", ignoreCase = true) || it.contains("Politics", ignoreCase = true)
        }

        setDefenseAdvisoriesEnabled(context, hasDefense)
        setFloodAlertsEnabled(context, hasFloodOrEmergency)
        setWeatherAlertsEnabled(context, hasFloodOrEmergency)
        setBreakingNewsEnabled(context, hasLocalNews)
    }

    fun subscribeToTopic(topic: String) {
        if (!isFirebaseReady) {
            Log.d(TAG, "Topic subscription '$topic' stored in preferences (running in offline-first mode)")
            return
        }
        try {
            FirebaseMessaging.getInstance().subscribeToTopic(topic)
                .addOnSuccessListener {
                    Log.d(TAG, "Subscribed successfully to FCM topic: $topic")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "FCM topic subscription deferred: $topic: ${e.message}")
                }
        } catch (e: Exception) {
            Log.d(TAG, "Cannot subscribe to topic $topic: ${e.message}")
        }
    }

    fun unsubscribeFromTopic(topic: String) {
        if (!isFirebaseReady) {
            Log.d(TAG, "Topic unsubscription '$topic' updated in preferences (running in offline-first mode)")
            return
        }
        try {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic)
                .addOnSuccessListener {
                    Log.d(TAG, "Unsubscribed from FCM topic: $topic")
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "FCM topic unsubscription deferred: $topic: ${e.message}")
                }
        } catch (e: Exception) {
            Log.d(TAG, "Cannot unsubscribe from topic $topic: ${e.message}")
        }
    }

    /**
     * Simulates an urgent real-time Weather Alert push notification.
     * Updates the local Room database and triggers an Android system notification.
     */
    fun simulateWeatherAlertPush(context: Context) {
        val title = "IMD Coastal Storm & Tide Advisory"
        val message = "High tidal swell waves up to 4.5m and squally coastal winds (45–55 km/h) forecasted for Chandipur Beach and Balaramgadi coast over the next 12 hours. Fishermen and beach tourists advised to avoid deep-water recession."

        // Update local Room database in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val current = db.weatherDao().getWeatherCacheSync()
                if (current != null) {
                    val updated = current.copy(
                        alertLevel = "WARNING",
                        alertTitle = title,
                        alertMessage = message,
                        lastUpdated = System.currentTimeMillis()
                    )
                    db.weatherDao().insertOrUpdateWeather(updated)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update Room weather cache on push simulation", e)
            }
        }

        // Post system notification
        BalasoreNotificationHelper.showWeatherAlertNotification(
            context = context,
            title = title,
            message = message,
            alertLevel = "URGENT"
        )
    }

    /**
     * Simulates a real-time Breaking News push notification.
     * Inserts the urgent article directly into Room and fires a system notification.
     */
    fun simulateBreakingNewsPush(context: Context) {
        val timeString = SimpleDateFormat("h:mm a, d MMM", Locale.getDefault()).format(Date())
        val title = "District Admin Issues Chandipur Shoreline Advisory"
        val summary = "District Administration Balasore has issued an emergency safety alert urging visitors to adhere to the low tide return timings at Chandipur beach."
        val content = "Following an advisory from the Coastal Safety Division and IMD, the District Magistrate and Collector of Balasore have notified all coastal checkpoints at Chandipur, Kasafal, and Talasari. Visitors walking onto the receded seabed during low tide are instructed to return to the high-water berm line before 4:30 PM. NDRF and ODRAF personnel have deployed mobile patrol quad bikes along the shoreline."

        // Insert into Room Database so it is immediately cached and works offline
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val newArticle = NewsArticleEntity(
                    title = title,
                    summary = summary,
                    content = content,
                    category = "Civic",
                    source = "Collectorate & District Magistrate, Balasore",
                    publishedAt = "Just now ($timeString)",
                    timestamp = System.currentTimeMillis(),
                    isBreaking = true,
                    isBookmarked = false
                )
                db.newsDao().insertArticles(listOf(newArticle))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert breaking news article into Room on push simulation", e)
            }
        }

        // Post system notification
        BalasoreNotificationHelper.showBreakingNewsNotification(
            context = context,
            title = title,
            message = summary
        )
    }

    /**
     * Simulates a real-time Coastal Flood & Sluice Gate push notification.
     * Triggers a heads-up alert with river gauge levels and evacuation advisories.
     */
    fun simulateCoastalFloodPush(context: Context) {
        val title = "Subarnarekha River Flood & Sluice Gate Warning"
        val message = "Rajghat river gauge has crossed warning level reaching 9.62m (Danger: 9.45m). Coastal sluice gates 3, 5, and 7 opened in Bhograi block. Riparian villages on flood watch."
        BalasoreNotificationHelper.showCoastalFloodNotification(
            context = context,
            title = title,
            message = message,
            riverLevelGauge = "Rajghat: 9.62m (Above Danger Level)",
            alertLevel = "CRITICAL"
        )
    }

    /**
     * Simulates an urgent real-time Coastal Defense & Missile Test Advisory push notification.
     * Triggers a heads-up alert with NOTAM airspace window and maritime exclusion radius for Chandipur / Kalam Island.
     */
    fun simulateDefenseAdvisoryPush(context: Context) {
        val title = "ITR Chandipur Coastal Defense & NOTAM Advisory"
        val message = "DRDO test range window active from Launch Complex-3 (LC-3). Airspace exclusion active under NOTAM A0412/26. All mechanized and artisanal fishing vessels from Balaramgadi and Kasafal must maintain minimum 18 km seaward clearance."
        BalasoreNotificationHelper.showDefenseAdvisoryNotification(
            context = context,
            title = title,
            message = message,
            notamWindow = "09:30 AM - 01:30 PM",
            exclusionPerimeterKm = "18 km"
        )
    }

    /**
     * Simulates an urgent real-time Train or Bus Delay push notification.
     * Caches the delay in Room database and dispatches a high-priority heads-up notification.
     */
    fun simulateTransportDelayPush(
        context: Context,
        transportNumber: String = "12821",
        serviceName: String = "12821 Dhauli Express",
        delayMinutes: Int = 30,
        platformOrBay: String = "PF 1",
        reason: String = "Track renewal & interlocking hold near Kharagpur Junction"
    ) {
        val title = "$serviceName Delay Alert (+${delayMinutes}m)"
        val message = "$serviceName is delayed by $delayMinutes mins. Revised arrival at Balasore $platformOrBay. Reason: $reason."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val repo = com.example.data.repository.TransportAlertRepository(context)
                repo.recordDelayAlert(
                    transportType = if (serviceName.contains("Bus", true)) "BUS" else "TRAIN",
                    identifier = transportNumber,
                    serviceName = serviceName,
                    delayMinutes = delayMinutes,
                    scheduledTime = "12:40 PM",
                    estimatedTime = "01:10 PM",
                    platformOrBay = platformOrBay,
                    reason = reason,
                    reasonOdia = "ଖଡଗପୁର ଜଙ୍କସନ ନିକଟରେ ଟ୍ରାକ୍ ନବୀକରଣ ଯୋଗୁଁ ବିଳମ୍ବ",
                    fcmMessageId = "sim_fcm_${System.currentTimeMillis()}"
                )
            } catch (e: Exception) {
                Log.e(TAG, "Failed to record simulated transport delay", e)
            }
        }

        BalasoreNotificationHelper.showTransportDelayNotification(
            context = context,
            title = title,
            message = message,
            transportNumber = transportNumber,
            delayMinutes = delayMinutes,
            platformOrBay = platformOrBay,
            transportType = if (serviceName.contains("Bus", true)) "BUS" else "TRAIN"
        )
    }

    const val KEY_SELECTED_WARD = "selected_user_ward"

    /**
     * Subscribes the user to localized FCM push notifications for their selected Balasore Municipal Ward (1 to 31).
     */
    fun subscribeToWard(context: Context, wardNumber: Int) {
        val prefs = getPrefs(context)
        val oldWard = prefs.getInt(KEY_SELECTED_WARD, -1)
        if (oldWard > 0 && oldWard != wardNumber) {
            unsubscribeFromTopic("balasore_ward_$oldWard")
        }
        prefs.edit().putInt(KEY_SELECTED_WARD, wardNumber).apply()
        subscribeToTopic("balasore_ward_$wardNumber")
        Log.i(TAG, "Subscribed to Balasore Municipal Ward #$wardNumber FCM push alerts")
    }

    /**
     * Returns the citizen's selected Balasore Municipal Ward (default 14 - OT Road).
     */
    fun getSelectedWard(context: Context): Int {
        return getPrefs(context).getInt(KEY_SELECTED_WARD, 14)
    }

    /**
     * Simulates an urgent localized Municipal Ward Push Notification (e.g. water supply cut, road closure).
     */
    fun simulateWardAlertPush(
        context: Context,
        wardNumber: Int = 14,
        wardName: String = "OT Road / Phandi Chhak",
        title: String = "Urgent WATCO Water Supply Interruption",
        message: String = "Main distribution conduit valve repair on OT Road. Water supply suspended between 2:00 PM and 6:30 PM. Standby municipal tankers deployed at Phandi Chhak.",
        alertType: String = "WATER_SUPPLY",
        department: String = "Balasore Municipality & WATCO"
    ) {
        com.example.data.notification.CivicNotificationHelper.notifyWardMunicipalAlert(
            context = context,
            wardNumber = wardNumber,
            wardName = wardName,
            title = title,
            message = message,
            alertType = alertType,
            department = department
        )
        recordDispatchedAlert(
            channel = "WARD_ALERT",
            title = title,
            message = message,
            severity = "URGENT"
        )
    }

    data class DispatchedPushAlert(
        val id: String,
        val channel: String, // "BREAKING_NEWS", "WEATHER_ALERT", "DEFENSE_ALERT", "FLOOD_ALERT"
        val title: String,
        val message: String,
        val severity: String,
        val timestampFormatted: String
    )

    private val _recentDispatchedAlerts = MutableStateFlow(
        listOf(
            DispatchedPushAlert(
                id = "init_push_1",
                channel = "WEATHER_ALERT",
                title = "IMD Coastal Storm & High Tide Advisory",
                message = "Squally coastal winds (45–55 km/h) and 4.2m tidal swell forecasted along Chandipur & Balaramgadi coast.",
                severity = "URGENT",
                timestampFormatted = "Today, 09:30 AM"
            ),
            DispatchedPushAlert(
                id = "init_push_2",
                channel = "BREAKING_NEWS",
                title = "Major Breaking: Balasore Ring Road & Remuna Flyover Corridor Cleared",
                message = "District Administration opens new smart transit bays at Sahadevkhunta and Remuna Golei.",
                severity = "BREAKING",
                timestampFormatted = "Today, 10:15 AM"
            )
        )
    )
    val recentDispatchedAlerts: StateFlow<List<DispatchedPushAlert>> = _recentDispatchedAlerts.asStateFlow()

    private fun recordDispatchedAlert(
        channel: String,
        title: String,
        message: String,
        severity: String
    ) {
        val timeString = SimpleDateFormat("hh:mm:ss a, d MMM", Locale.US).format(Date())
        val entry = DispatchedPushAlert(
            id = "push_${System.currentTimeMillis()}",
            channel = channel,
            title = title,
            message = message,
            severity = severity,
            timestampFormatted = timeString
        )
        _recentDispatchedAlerts.value = (listOf(entry) + _recentDispatchedAlerts.value).take(15)
    }

    /**
     * Dispatches a custom Major Breaking News push notification directly to the user's device,
     * persists the article in Room Database, and logs it in recentDispatchedAlerts.
     */
    fun dispatchCustomBreakingNewsPush(
        context: Context,
        title: String,
        summary: String,
        category: String = "Breaking News",
        source: String = "Balasore District Administration & News Wire"
    ) {
        val timeString = SimpleDateFormat("h:mm a, d MMM", Locale.US).format(Date())
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val newArticle = NewsArticleEntity(
                    title = title,
                    summary = summary,
                    content = summary,
                    category = category,
                    source = source,
                    publishedAt = "Just now ($timeString)",
                    timestamp = System.currentTimeMillis(),
                    isBreaking = true,
                    isBookmarked = false
                )
                db.newsDao().insertArticles(listOf(newArticle))
            } catch (e: Exception) {
                Log.e(TAG, "Failed to insert custom breaking news article into Room", e)
            }
        }

        BalasoreNotificationHelper.showBreakingNewsNotification(
            context = context,
            title = title,
            message = summary
        )
        recordDispatchedAlert(
            channel = "BREAKING_NEWS",
            title = title,
            message = summary,
            severity = "BREAKING"
        )
    }

    /**
     * Dispatches a custom Urgent Weather Alert push notification directly to the user's device,
     * updates the Room weather cache, and logs it in recentDispatchedAlerts.
     */
    fun dispatchCustomWeatherAlertPush(
        context: Context,
        title: String,
        message: String,
        alertLevel: String = "URGENT"
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)
                val current = db.weatherDao().getWeatherCacheSync()
                if (current != null) {
                    val updated = current.copy(
                        alertLevel = alertLevel,
                        alertTitle = title,
                        alertMessage = message,
                        lastUpdated = System.currentTimeMillis()
                    )
                    db.weatherDao().insertOrUpdateWeather(updated)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update Room weather cache on custom push", e)
            }
        }

        BalasoreNotificationHelper.showWeatherAlertNotification(
            context = context,
            title = title,
            message = message,
            alertLevel = alertLevel
        )
        recordDispatchedAlert(
            channel = "WEATHER_ALERT",
            title = title,
            message = message,
            severity = alertLevel
        )
    }

    /**
     * Evaluates local Chandipur Beach tidal data from Room (`ChandipurTideDao`) and `TidalLocalStorage`
     * and dispatches a 'High Tide' coastal safety push notification directly to the user's device.
     */
    fun dispatchChandipurHighTidePushFromLocalData(
        context: Context,
        customTitle: String? = null,
        customMessage: String? = null,
        bypassToggleCheck: Boolean = false
    ) {
        if (!bypassToggleCheck && !isChandipurHighTideAlertsEnabled(context)) {
            Log.d(TAG, "Chandipur High Tide push skipped because user toggled off High Tide alerts in Settings.")
            return
        }

        val tidalStorage = com.example.data.local.TidalLocalStorage.getInstance(context)
        val trendSummary = tidalStorage.getTrendSummary()
        val points24h = tidalStorage.getTidalPointsForTimeframe(com.example.data.model.TidalTimeframe.HOURLY_24H)
        val peakPoint = points24h.maxByOrNull { it.tideHeightMeters }

        val computedHeightM = (peakPoint?.tideHeightMeters ?: trendSummary.highestTideLevelMeters).coerceAtLeast(3.4)
        val currentRecessionKm = trendSummary.currentRecessionKm.coerceIn(0.2, 4.8)
        val lunarCondition = peakPoint?.lunarCondition ?: "Spring Tide (Bay of Bengal Surge)"

        CoroutineScope(Dispatchers.IO).launch {
            var nextHighTideTime = "08:15 PM"
            var safeWalkRemaining = 25

            try {
                val db = AppDatabase.getInstance(context)
                val cachedTide = db.chandipurTideDao().getLatestTideForecastSync()
                if (cachedTide != null) {
                    nextHighTideTime = cachedTide.nextHighTideTime.ifBlank { cachedTide.highTideTime }
                    safeWalkRemaining = cachedTide.safeWalkMinutesRemaining.coerceIn(10, 45)
                    db.chandipurTideDao().insertOrUpdateTide(
                        cachedTide.copy(
                            currentWaterLevelMeters = computedHeightM,
                            tideState = "HIGH_TIDE",
                            safeWalkStatus = "UNSAFE_RETURN_SHORE",
                            safeWalkMinutesRemaining = safeWalkRemaining,
                            tidalForecastSummary = "High Tide Surge Alert (${String.format("%.2f", computedHeightM)}m) at Chandipur Beach. Sea advancing rapidly across the intertidal flat.",
                            lastFetchedTimestamp = System.currentTimeMillis()
                        )
                    )
                }

                val currentWeather = db.weatherDao().getWeatherCacheSync()
                if (currentWeather != null) {
                    db.weatherDao().insertOrUpdateWeather(
                        currentWeather.copy(
                            tideState = "HIGH_TIDE",
                            tideDescription = "High Tide Alert (${String.format("%.2f", computedHeightM)}m) • Next peak $nextHighTideTime",
                            lastUpdated = System.currentTimeMillis()
                        )
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error reading/updating local Chandipur tidal cache for push alert", e)
            }

            val finalTitle = customTitle?.takeIf { it.isNotBlank() }
                ?: "Chandipur Beach High Tide Alert (${String.format("%.1f", computedHeightM)}m)"
            val finalMessage = customMessage?.takeIf { it.isNotBlank() }
                ?: "Bay of Bengal tide is advancing rapidly at Chandipur-on-Sea (Peak: ${String.format("%.2f", computedHeightM)}m at $nextHighTideTime, current exposure ${String.format("%.1f", currentRecessionKm)} km). Visitors on the seabed must return to the beach embankment within $safeWalkRemaining mins."

            BalasoreNotificationHelper.showChandipurHighTideNotification(
                context = context,
                title = finalTitle,
                message = finalMessage,
                tideHeightMeters = computedHeightM,
                recededDistanceKm = currentRecessionKm,
                nextHighTideTime = nextHighTideTime,
                safeWalkMinutesRemaining = safeWalkRemaining,
                lunarCondition = lunarCondition
            )

            recordDispatchedAlert(
                channel = "CHANDIPUR_HIGH_TIDE",
                title = finalTitle,
                message = finalMessage,
                severity = "HIGH_TIDE"
            )
        }
    }

    // --- ADAPTIVE SYNC (<20% BATTERY AUTO-THROTTLE) & PERSONALIZED NEWS CATEGORY SUBSCRIPTIONS ---

    private const val KEY_ADAPTIVE_SYNC_ENABLED = "adaptive_sync_low_battery_enabled"
    private const val KEY_SUBSCRIBED_NEWS_CATEGORIES = "subscribed_news_categories_set"

    val AVAILABLE_NEWS_CATEGORIES = listOf("Politics", "Sports", "Defense", "Tourism", "Civic")

    fun isAdaptiveSyncEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_ADAPTIVE_SYNC_ENABLED, true)
    }

    fun setAdaptiveSyncEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_ADAPTIVE_SYNC_ENABLED, enabled).apply()
    }

    fun getSubscribedNewsCategories(context: Context): Set<String> {
        val defaultSet = AVAILABLE_NEWS_CATEGORIES.toSet()
        return getPrefs(context).getStringSet(KEY_SUBSCRIBED_NEWS_CATEGORIES, defaultSet)?.toSet() ?: defaultSet
    }

    fun toggleNewsCategorySubscription(context: Context, category: String): Set<String> {
        val current = getSubscribedNewsCategories(context).toMutableSet()
        val topicName = "balasore_news_${category.lowercase(Locale.US)}"
        if (current.contains(category)) {
            current.remove(category)
            unsubscribeFromTopic(topicName)
        } else {
            current.add(category)
            subscribeToTopic(topicName)
        }
        getPrefs(context).edit().putStringSet(KEY_SUBSCRIBED_NEWS_CATEGORIES, current).apply()
        return current.toSet()
    }

    fun dispatchCategoryBreakingAlert(context: Context, category: String) {
        val (title, summary) = when (category.lowercase(Locale.US)) {
            "politics" -> "Balasore Municipal Council Approves Ward 1–31 Smart Road Budget" to
                "Firestore live document update [Politics]: ₹48 Cr sanctioned for storm-water drains and solar LED corridors across Balasore Municipality."
            "sports" -> "Balasore District Athletics Meet Opens at Police Line Stadium" to
                "Firestore live document update [Sports]: Over 420 sprinters and football clubs from Remuna, Nilagiri, and Jaleswar competing today."
            "defense" -> "DRDO ITR Chandipur Coastal Range Advisory Update" to
                "Firestore live document update [Defense]: Scheduled telemetry tracking window active along Launch Complex-III; coastal marine corridor monitored."
            "tourism" -> "Chandipur & Panchalingeswar Eco-Corridor Records Peak Footfall" to
                "Firestore live document update [Tourism]: New solar tourist shuttle and bilingual heritage kiosks inaugurated for weekend visitors."
            else -> "Balasore Civic Eye: Rapid Drain Desiltation Completed at Cinema Chhak" to
                "Firestore live document update [Civic]: Municipal sanitation squad cleared Ward 12 storm drains ahead of coastal showers."
        }
        dispatchCustomBreakingNewsPush(
            context = context,
            title = "[$category] $title",
            summary = summary,
            category = category,
            source = "Baleshwar 360 Firestore Live Feed"
        )
    }
}

