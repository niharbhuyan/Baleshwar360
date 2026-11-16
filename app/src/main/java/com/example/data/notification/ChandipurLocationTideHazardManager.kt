package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.R
import com.example.data.service.ChandipurLunarTideService
import com.example.data.service.ChandipurSafeWalkService
import kotlin.math.*

/**
 * Refined Location-Based Notification System for Chandipur Beach High Tide Hazard.
 *
 * Uses location services / GPS coordinates to determine if a user is within
 * dangerous proximity (exposed seabed mudflats, beach shoreline, or coastal promenade)
 * during incoming or dangerous high tide periods, leveraging existing tide schedule data.
 */
object ChandipurLocationTideHazardManager {

    private const val TAG = "ChandipurLocationHazard"
    private const val PREFS_NAME = "chandipur_location_hazard_prefs"

    // Chandipur Sea Beach Main Shoreline Coordinates (Balasore, Odisha)
    const val CHANDIPUR_BEACH_LAT = 21.4705
    const val CHANDIPUR_BEACH_LNG = 87.0175

    // Notification Channel & IDs
    const val CHANNEL_ID_PROXIMITY_HAZARD = "chandipur_proximity_hazard_channel"
    const val CHANNEL_NAME_PROXIMITY_HAZARD = "Chandipur Coastal Proximity High Tide Alerts"
    const val NOTIFICATION_ID_PROXIMITY_HAZARD = 8130

    // Emergency Contacts for Chandipur Coast
    const val CHANDIPUR_MARINE_POLICE_PHONE = "06782-272210"
    const val BALASORE_DISASTER_HELPLINE = "1077"

    // Preference keys
    private const val KEY_PROXIMITY_ALERTS_ENABLED = "key_proximity_alerts_enabled"
    private const val KEY_PROXIMITY_RADIUS_KM = "key_proximity_radius_km"
    private const val KEY_LAST_HAZARD_ALERT_EPOCH = "key_last_hazard_alert_epoch"
    private const val KEY_LAST_RECORDED_LAT = "key_last_recorded_lat"
    private const val KEY_LAST_RECORDED_LNG = "key_last_recorded_lng"

    // Default parameters
    const val DEFAULT_HAZARD_RADIUS_KM = 5.0
    private const val ALERT_COOLDOWN_MS = 20 * 60 * 1000L // 20 minutes cooldown between notifications

    enum class HazardLevel(val titleEn: String, val titleOd: String, val badgeColorHex: String) {
        SAFE("Safe - Sea Receded", "ସୁରକ୍ଷିତ - ସମୁଦ୍ର ଭଟ୍ଟା", "#10B981"),
        CAUTION("Caution - Incoming Waters", "ସତର୍କତା - ଜୁଆର ଆରମ୍ଭ", "#F59E0B"),
        DANGER("CRITICAL DANGER: HIGH TIDE SURGE", "ଅତ୍ୟନ୍ତ ବିପଦ: ଉଚ୍ଚ ଜୁଆର ମାଡ଼ି ଆସୁଛି", "#EF4444")
    }

    enum class UserZone(val labelEn: String, val labelOd: String) {
        ON_EXPOSED_SEABED("Exposed Intertidal Seabed (< 2 km)", "ସମୁଦ୍ର ଶଯ୍ୟା / ବାଲୁକା ତଟ"),
        BEACH_SHORELINE("Chandipur Shoreline (< 5 km)", "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ସୀମା"),
        COASTAL_BLOCK("Nearby Coastal Belt (< 10 km)", "ଉପକୂଳବର୍ତ୍ତୀ ଅଞ୍ଚଳ"),
        OUTSIDE_HAZARD_ZONE("Outside Coastal Hazard Zone", "ବିପଦ ସୀମା ବାହାରେ")
    }

    data class ProximityHazardStatus(
        val distanceKm: Double,
        val userZone: UserZone,
        val isHighTideDangerous: Boolean,
        val hazardLevel: HazardLevel,
        val surgeSpeedMs: Double,
        val minutesUntilPeakHighTide: Long,
        val safeWalkMinutesRemaining: Long,
        val advisoryTitle: String,
        val advisoryMessage: String,
        val odiaAdvisoryMessage: String,
        val isNotificationSent: Boolean,
        val evaluatedTimestamp: Long = System.currentTimeMillis()
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isProximityAlertsEnabled(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_PROXIMITY_ALERTS_ENABLED, true)
    }

    fun setProximityAlertsEnabled(context: Context, enabled: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_PROXIMITY_ALERTS_ENABLED, enabled).apply()
    }

    fun getProximityRadiusKm(context: Context): Double {
        val radiusFloat = getPrefs(context).getFloat(KEY_PROXIMITY_RADIUS_KM, DEFAULT_HAZARD_RADIUS_KM.toFloat())
        return radiusFloat.toDouble()
    }

    fun setProximityRadiusKm(context: Context, radiusKm: Double) {
        getPrefs(context).edit().putFloat(KEY_PROXIMITY_RADIUS_KM, radiusKm.toFloat()).apply()
    }

    fun getLastHazardAlertTime(context: Context): Long {
        return getPrefs(context).getLong(KEY_LAST_HAZARD_ALERT_EPOCH, 0L)
    }

    fun setLastHazardAlertTime(context: Context, timeMs: Long) {
        getPrefs(context).edit().putLong(KEY_LAST_HAZARD_ALERT_EPOCH, timeMs).apply()
    }

    fun saveLastKnownCoordinates(context: Context, lat: Double, lng: Double) {
        getPrefs(context).edit()
            .putFloat(KEY_LAST_RECORDED_LAT, lat.toFloat())
            .putFloat(KEY_LAST_RECORDED_LNG, lng.toFloat())
            .apply()
    }

    fun getLastSavedCoordinates(context: Context): Pair<Double, Double>? {
        val prefs = getPrefs(context)
        if (!prefs.contains(KEY_LAST_RECORDED_LAT) || !prefs.contains(KEY_LAST_RECORDED_LNG)) {
            return null
        }
        val lat = prefs.getFloat(KEY_LAST_RECORDED_LAT, 0f).toDouble()
        val lng = prefs.getFloat(KEY_LAST_RECORDED_LNG, 0f).toDouble()
        return Pair(lat, lng)
    }

    /**
     * Calculates distance between two GPS coordinates using Haversine formula (km).
     * JVM local unit test safe (no native Android Location binding required).
     */
    fun calculateHaversineDistanceKm(
        lat1: Double, lon1: Double,
        lat2: Double, lon2: Double
    ): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }

    /**
     * Creates notification channels including the high-priority location hazard channel.
     */
    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val channel = NotificationChannel(
                CHANNEL_ID_PROXIMITY_HAZARD,
                CHANNEL_NAME_PROXIMITY_HAZARD,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent high tide proximity warnings when near Chandipur beach shoreline or intertidal mudflats"
                enableLights(true)
                lightColor = Color.RED
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 400, 200, 400, 200, 600)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Attempts to read the best available device location using LocationManager.
     */
    fun getCurrentDeviceLocation(context: Context): Pair<Double, Double>? {
        val hasFine = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (!hasFine && !hasCoarse) {
            return getLastSavedCoordinates(context)
        }

        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val gpsLoc = try {
                if (hasFine) locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER) else null
            } catch (_: Exception) { null }

            val networkLoc = try {
                locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            } catch (_: Exception) { null }

            val bestLoc = gpsLoc ?: networkLoc
            if (bestLoc != null) {
                saveLastKnownCoordinates(context, bestLoc.latitude, bestLoc.longitude)
                Pair(bestLoc.latitude, bestLoc.longitude)
            } else {
                getLastSavedCoordinates(context)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to read device location: ${e.message}")
            getLastSavedCoordinates(context)
        }
    }

    /**
     * Core evaluation engine:
     * Cross-references user location against the existing Chandipur tidal schedule.
     * Triggers a location-aware push alert if user is near Chandipur beach during dangerous high tide.
     */
    fun evaluateLocationTideHazard(
        context: Context,
        userLat: Double? = null,
        userLng: Double? = null,
        forceNotification: Boolean = false
    ): ProximityHazardStatus {
        createNotificationChannels(context)

        // 1. Determine user coordinates
        val coords = if (userLat != null && userLng != null) {
            Pair(userLat, userLng)
        } else {
            getCurrentDeviceLocation(context) ?: Pair(CHANDIPUR_BEACH_LAT + 0.02, CHANDIPUR_BEACH_LNG + 0.01) // Default near coast
        }

        val distanceKm = calculateHaversineDistanceKm(coords.first, coords.second, CHANDIPUR_BEACH_LAT, CHANDIPUR_BEACH_LNG)
        val configuredRadius = getProximityRadiusKm(context)

        val userZone = when {
            distanceKm <= 2.0 -> UserZone.ON_EXPOSED_SEABED
            distanceKm <= 5.0 -> UserZone.BEACH_SHORELINE
            distanceKm <= 10.0 -> UserZone.COASTAL_BLOCK
            else -> UserZone.OUTSIDE_HAZARD_ZONE
        }

        // 2. Leverage existing tide schedule models
        val walkWindow = ChandipurSafeWalkService.calculateNextSafeWalkWindow(context)
        val lunarInfo = ChandipurLunarTideService.calculateLunarTideInfo(System.currentTimeMillis())

        val now = System.currentTimeMillis()
        val minutesUntilHighTide = walkWindow.minutesUntilHighTide
        val isCurrentlyInSafeWalk = walkWindow.isCurrentlyInWindow
        val minutesRemainingInWalk = walkWindow.minutesRemainingInWindow

        // High tide is dangerous when:
        // A. Walk window has ended or is closing in <= 25 mins (water returning)
        // B. High tide peak is within 45 minutes or currently cresting
        // C. Surge speed is elevated (spring tide surge > 0.6 m/s)
        val isWalkEndingOrEnded = !isCurrentlyInSafeWalk || minutesRemainingInWalk <= 25
        val isHighTideApproaching = minutesUntilHighTide in 0..45
        val isHighTideDangerous = isWalkEndingOrEnded || isHighTideApproaching

        val isWithinHazardProximity = distanceKm <= configuredRadius

        val hazardLevel = when {
            isWithinHazardProximity && isHighTideDangerous && userZone == UserZone.ON_EXPOSED_SEABED -> HazardLevel.DANGER
            isWithinHazardProximity && isHighTideDangerous -> HazardLevel.DANGER
            isWithinHazardProximity && minutesUntilHighTide in 46..90 -> HazardLevel.CAUTION
            else -> HazardLevel.SAFE
        }

        val surgeSpeedMs = lunarInfo.incomingWaterVelocityMs

        val formattedDistance = String.format(java.util.Locale.US, "%.1f", distanceKm)
        val advisoryTitle = when (hazardLevel) {
            HazardLevel.DANGER -> "🚨 CHANDIPUR BEACH DANGER: HIGH TIDE SURGE IN PROGRESS"
            HazardLevel.CAUTION -> "⚠️ Chandipur High Tide Warning: Approaching Shoreline"
            HazardLevel.SAFE -> "✅ Chandipur Safe Walking Window Active"
        }

        val advisoryMessage = when (hazardLevel) {
            HazardLevel.DANGER -> "You are detected $formattedDistance km from Chandipur Beach. The Bay of Bengal tide is rapidly returning ($surgeSpeedMs m/s). Vanishing sea seabed is submerging. Evacuate mudflats to concrete seawall immediately!"
            HazardLevel.CAUTION -> "You are $formattedDistance km from Chandipur. High tide arrives in ${minutesUntilHighTide}m. Prepare to exit the intertidal sands before water covers the seafloor."
            HazardLevel.SAFE -> "You are $formattedDistance km from Chandipur. Sea receded ~${walkWindow.estimatedRecessionKm} km. Safe to walk on seabed for next ${walkWindow.minutesRemainingInWindow}m."
        }

        val odiaMessage = when (hazardLevel) {
            HazardLevel.DANGER -> "ଚେତାବନୀ: ଆପଣ ଚାନ୍ଦିପୁର ବେଳାଭୂମିରୁ $formattedDistance କି.ମି. ଦୂରରେ ଅଛନ୍ତି! ସମୁଦ୍ର ଜୁଆର ତୀବ୍ର ଗତିରେ ($surgeSpeedMs m/s) ମାଡ଼ି ଆସୁଛି। ତୁରନ୍ତ ସମୁଦ୍ର ଶଯ୍ୟା ଛାଡ଼ି ପକ୍କା ବନ୍ଧକୁ ଫେରି ଆସନ୍ତୁ।"
            HazardLevel.CAUTION -> "ସତର୍କତା: ଆଗାମୀ ${minutesUntilHighTide} ମିନିଟରେ ଚାନ୍ଦିପୁରରେ ଭରା ଜୁଆର ଆସିବ। ସମୁଦ୍ର ଭିତରୁ କୂଳକୁ ଫେରିବା ପାଇଁ ପ୍ରସ୍ତୁତ ହୁଅନ୍ତୁ।"
            HazardLevel.SAFE -> "ଚାନ୍ଦିପୁର ଭଟ୍ଟା ସମୟ ଚାଲୁଅଛି। ଆଗାମୀ ${walkWindow.minutesRemainingInWindow} ମିନିଟ୍ ପର୍ଯ୍ୟନ୍ତ ନିରାପଦରେ ଚାଲିପାରିବେ।"
        }

        var notificationSent = false
        if (hazardLevel == HazardLevel.DANGER && isProximityAlertsEnabled(context)) {
            val lastAlertTime = getLastHazardAlertTime(context)
            if (forceNotification || (now - lastAlertTime) > ALERT_COOLDOWN_MS) {
                sendHighTideProximityNotification(
                    context = context,
                    title = advisoryTitle,
                    body = advisoryMessage,
                    distanceKm = distanceKm,
                    surgeSpeed = surgeSpeedMs
                )
                setLastHazardAlertTime(context, now)
                notificationSent = true
            }
        }

        return ProximityHazardStatus(
            distanceKm = distanceKm,
            userZone = userZone,
            isHighTideDangerous = isHighTideDangerous,
            hazardLevel = hazardLevel,
            surgeSpeedMs = surgeSpeedMs,
            minutesUntilPeakHighTide = minutesUntilHighTide,
            safeWalkMinutesRemaining = minutesRemainingInWalk,
            advisoryTitle = advisoryTitle,
            advisoryMessage = advisoryMessage,
            odiaAdvisoryMessage = odiaMessage,
            isNotificationSent = notificationSent
        )
    }

    /**
     * Dispatches the high-priority location hazard notification to Android System Tray.
     */
    private fun sendHighTideProximityNotification(
        context: Context,
        title: String,
        body: String,
        distanceKm: Double,
        surgeSpeed: Double
    ) {
        val notificationManager = NotificationManagerCompat.from(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "POST_NOTIFICATIONS permission not granted. Skipping proximity notification.")
                return
            }
        }

        // Tap opens app directly to the weather/tide screen
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("navigate_to", "WEATHER")
            putExtra("hazard_focus", "CHANDIPUR_PROXIMITY_HAZARD")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            9130,
            tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        // Action: Call Chandipur Marine Police
        val callIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$CHANDIPUR_MARINE_POLICE_PHONE"))
        val callPendingIntent = PendingIntent.getActivity(
            context,
            9131,
            callIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_PROXIMITY_HAZARD)
            .setSmallIcon(R.drawable.ic_notification_tide)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "$body\n\n⚠️ Safety Advisory:\n• Do not linger on sandbars; water fills tidal gullies first cutting off escape routes.\n• Head directly West toward Chandipur Panthanivas & Seawall.\n• Emergency Marine Police: $CHANDIPUR_MARINE_POLICE_PHONE"
            ))
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setColor(Color.RED)
            .setSound(soundUri)
            .setVibrate(longArrayOf(0, 400, 200, 400, 200, 600))
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_widget_call, "Call Marine Police", callPendingIntent)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID_PROXIMITY_HAZARD, notification)
            Log.i(TAG, "Dispatched Chandipur High Tide Proximity Danger Alert! Distance: ${distanceKm}km, Surge: ${surgeSpeed}m/s")
        } catch (e: Exception) {
            Log.e(TAG, "Error displaying proximity hazard notification: ${e.message}")
        }
    }
}
