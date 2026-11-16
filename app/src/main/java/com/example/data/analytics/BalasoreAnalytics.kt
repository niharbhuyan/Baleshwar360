package com.example.data.analytics

import android.content.Context
import android.os.Bundle
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics

/**
 * Enterprise Firebase Analytics tracking coordinator for Balasore 360.
 *
 * Tracks:
 * 1. Screen views across primary hubs (Home, Weather, News, Transport, Tourism, Essentials, Settings)
 * 2. User feature engagements (D3.js telemetry scrubbing, Tidal walk advisories, Emergency SOS, Offline Maps)
 * 3. Cache management operations (storage breakdowns and cache purges)
 * 4. Battery Aware background synchronization toggles
 *
 * Designed to provide actionable insights for prioritizing future app updates while
 * failing gracefully with local diagnostics when offline or when Firebase credentials are not yet linked.
 */
object BalasoreAnalytics {

    private const val TAG = "BalasoreAnalytics"

    @Volatile
    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun initialize(context: Context) {
        if (firebaseAnalytics == null) {
            synchronized(this) {
                if (firebaseAnalytics == null) {
                    try {
                        if (FirebaseApp.getApps(context).isNotEmpty()) {
                            firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
                            Log.i(TAG, "Firebase Analytics initialized successfully.")
                        } else {
                            Log.w(TAG, "FirebaseApp is not configured yet; Analytics will run in diagnostic fallback mode.")
                        }
                    } catch (e: Exception) {
                        Log.w(TAG, "Unable to initialize Firebase Analytics instance: ${e.message}")
                    }
                }
            }
        }
    }

    private fun getAnalytics(context: Context): FirebaseAnalytics? {
        if (firebaseAnalytics == null) {
            initialize(context)
        }
        return firebaseAnalytics
    }

    /**
     * Track a user navigating to a screen to prioritize popular features.
     */
    fun trackScreenView(context: Context, screenName: String, screenClass: String? = null) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
                putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenClass ?: screenName)
                putLong("timestamp_ms", System.currentTimeMillis())
            }
            getAnalytics(context)?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
            Log.d(TAG, "Analytics ScreenView: $screenName")
        } catch (e: Exception) {
            Log.w(TAG, "Failed logging screen view: ${e.message}")
        }
    }

    /**
     * Track feature engagement (e.g. tapping high tide radar, asking AI chatbot, dialing emergency line).
     */
    fun trackFeatureEngagement(
        context: Context,
        featureId: String,
        action: String,
        parameters: Map<String, Any>? = null
    ) {
        try {
            val bundle = Bundle().apply {
                putString(FirebaseAnalytics.Param.ITEM_ID, featureId)
                putString(FirebaseAnalytics.Param.ITEM_NAME, featureId)
                putString(FirebaseAnalytics.Param.CONTENT_TYPE, "feature_interaction")
                putString("action", action)
                parameters?.forEach { (key, value) ->
                    when (value) {
                        is String -> putString(key, value)
                        is Int -> putInt(key, value)
                        is Long -> putLong(key, value)
                        is Double -> putDouble(key, value)
                        is Float -> putFloat(key, value)
                        is Boolean -> putBoolean(key, value)
                        else -> putString(key, value.toString())
                    }
                }
                putLong("timestamp_ms", System.currentTimeMillis())
            }
            getAnalytics(context)?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
            Log.d(TAG, "Analytics FeatureEngagement: $featureId - $action")
        } catch (e: Exception) {
            Log.w(TAG, "Failed logging feature engagement: ${e.message}")
        }
    }

    /**
     * Track when user purges local cache to free storage.
     */
    fun trackCachePurged(
        context: Context,
        mapsBytes: Long,
        imagesBytes: Long,
        newsBytes: Long,
        totalBytesFreed: Long
    ) {
        val params = mapOf(
            "maps_bytes_freed" to mapsBytes,
            "images_bytes_freed" to imagesBytes,
            "news_bytes_freed" to newsBytes,
            "total_bytes_freed" to totalBytesFreed,
            "freed_mb" to String.format(java.util.Locale.US, "%.2f", totalBytesFreed / (1024.0 * 1024.0))
        )
        trackFeatureEngagement(context, "clear_local_cache", "purge_confirmed", params)
    }

    /**
     * Track when user toggles or changes Intelligent Battery Aware Mode.
     */
    fun trackBatteryAwareModeChanged(
        context: Context,
        enabled: Boolean,
        batteryPercentage: Int,
        isScreenOff: Boolean,
        effectiveIntervalMinutes: Int
    ) {
        val params = mapOf(
            "battery_aware_enabled" to enabled,
            "battery_level" to batteryPercentage,
            "is_screen_off" to isScreenOff,
            "effective_interval_min" to effectiveIntervalMinutes
        )
        trackFeatureEngagement(context, "battery_aware_mode", if (enabled) "enable" else "disable", params)
    }

    /**
     * Track user applying a battery longevity optimization tip or sync interval change.
     */
    fun trackBatteryOptimizationApplied(
        context: Context,
        tipId: String,
        batteryGainedEstimate: String
    ) {
        val params = mapOf(
            "tip_id" to tipId,
            "gain_estimate" to batteryGainedEstimate
        )
        trackFeatureEngagement(context, "battery_health_monitor", "apply_tip", params)
    }

    /**
     * Track user tapping on D3.js Tidal Trend chart data point for exact tide height & timestamp.
     */
    fun trackTidalChartTap(
        context: Context,
        timeLabel: String,
        waterLevelMeters: Double,
        recessionKm: Double,
        isSafeWalk: Boolean
    ) {
        val params = mapOf(
            "time_label" to timeLabel,
            "water_level_m" to waterLevelMeters,
            "recession_km" to recessionKm,
            "is_safe_walk" to isSafeWalk
        )
        trackFeatureEngagement(context, "d3_tidal_trend_chart", "point_tapped", params)
    }

    /**
     * Track offline historical weather trend chart view / scrubbing.
     */
    fun trackHistoricalWeatherChartInteraction(
        context: Context,
        timeframe: String,
        selectedMetric: String,
        isOfflineMode: Boolean
    ) {
        val params = mapOf(
            "timeframe" to timeframe,
            "metric" to selectedMetric,
            "is_offline_mode" to isOfflineMode
        )
        trackFeatureEngagement(context, "d3_historical_weather_chart", "chart_view", params)
    }
}
