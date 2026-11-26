package com.example.data.config

import android.content.Context
import android.content.pm.PackageManager
import com.example.BuildConfig
import com.google.firebase.FirebaseApp

/**
 * Snapshot of an integrated external API's automatic key resolution state.
 */
data class BalasoreApiKeyStatus(
    val serviceName: String,
    val secretKeyName: String,
    val isLiveKeyConfigured: Boolean,
    val activeProviderDescription: String
)

/**
 * Centralized automatic API key & configuration resolver for Baleshwar 360.
 *
 * Reads credentials injected automatically at build time from the AI Studio Secrets panel
 * (`.env` / environment variables -> [BuildConfig]), AndroidManifest meta-data, and Firebase
 * project options (`google-services.json` + `firebase_applet_config.xml`). Sanitizes placeholder
 * strings so unconfigured services seamlessly fall back to live keyless public APIs
 * (Open-Meteo, Google News RSS, OSMDroid) and auto-configured local/cloud storage.
 */
object BalasoreApiKeys {

    private const val LEGACY_PREFS = "balasore_maps_api_key_prefs"
    private const val PREF_WEATHER_KEY = "auto_weather_map_api_key"
    private const val PREF_MAPS_KEY = "auto_maps_api_key"

    fun sanitize(candidate: String?): String {
        val clean = candidate?.trim()?.trim('"', '\'').orEmpty()
        if (clean.isBlank()) return ""
        val isPlaceholder =
            clean.startsWith("YOUR_", ignoreCase = true) ||
                clean.startsWith("MY_", ignoreCase = true) ||
                clean.contains("PLACEHOLDER", ignoreCase = true) ||
                clean.contains("REPLACE_ME", ignoreCase = true) ||
                clean.equals("AIzaSyBalasore360MapsKey", ignoreCase = true) ||
                clean.equals("AIzaSyBiQAArK-Hc0lYU35S41zBvPQnZT1S0d0k", ignoreCase = true)
        return if (isPlaceholder) "" else clean
    }

    fun geminiApiKey(): String =
        sanitize(runCatching { BuildConfig.GEMINI_API_KEY }.getOrDefault(""))

    fun isGeminiConfigured(): Boolean = geminiApiKey().isNotBlank()

    fun openWeatherApiKey(context: Context? = null): String {
        val fromBuildConfig = sanitize(
            runCatching { BuildConfig.OPENWEATHERMAP_API_KEY }.getOrDefault("")
        )
        if (fromBuildConfig.isNotBlank() && !fromBuildConfig.startsWith("AIza")) {
            return fromBuildConfig
        }
        val fromPrefs = sanitize(
            context?.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
                ?.getString(PREF_WEATHER_KEY, null)
        )
        return if (fromPrefs.isNotBlank() && !fromPrefs.startsWith("AIza")) fromPrefs else ""
    }

    fun isOpenWeatherConfigured(context: Context? = null): Boolean =
        openWeatherApiKey(context).isNotBlank()

    fun newsApiKey(): String =
        sanitize(runCatching { BuildConfig.NEWS_API_KEY }.getOrDefault(""))

    fun isNewsApiConfigured(): Boolean = newsApiKey().isNotBlank()

    fun balasoreDbPath(): String =
        sanitize(runCatching { BuildConfig.BALASORE_DB_PATH }.getOrDefault("balasore_database"))
            .ifBlank { "balasore_database" }

    fun firebaseProjectId(): String =
        sanitize(runCatching { BuildConfig.FIREBASE_PROJECT_ID }.getOrDefault(""))

    fun isFirebaseAutoConfigured(context: Context? = null): Boolean {
        val fromBuildConfig = runCatching { BuildConfig.FIREBASE_AUTO_CONFIGURED }.getOrDefault(false)
        if (fromBuildConfig) return true
        return runCatching {
            context != null && FirebaseApp.getApps(context).isNotEmpty()
        }.getOrDefault(false)
    }

    fun mapsApiKey(context: Context? = null): String {
        val fromBuildConfig = sanitize(
            runCatching { BuildConfig.MAPS_API_KEY }.getOrDefault("")
        )
        if (fromBuildConfig.isNotBlank()) return fromBuildConfig

        val fromManifest = sanitize(
            runCatching {
                val appInfo = context?.packageManager?.getApplicationInfo(
                    context.packageName,
                    PackageManager.GET_META_DATA
                )
                appInfo?.metaData?.getString("com.google.android.geo.API_KEY")
            }.getOrDefault("")
        )
        if (fromManifest.isNotBlank()) return fromManifest

        val fromFirebase = sanitize(
            runCatching { FirebaseApp.getInstance().options.apiKey }.getOrDefault("")
        )
        if (fromFirebase.isNotBlank()) return fromFirebase

        return sanitize(
            context?.getSharedPreferences(LEGACY_PREFS, Context.MODE_PRIVATE)
                ?.getString(PREF_MAPS_KEY, null)
        )
    }

    fun isMapsConfigured(context: Context? = null): Boolean = mapsApiKey(context).isNotBlank()

    fun statusSummary(context: Context? = null): List<BalasoreApiKeyStatus> = listOf(
        BalasoreApiKeyStatus(
            serviceName = "Weather & Alerts",
            secretKeyName = "OPENWEATHERMAP_API_KEY",
            isLiveKeyConfigured = isOpenWeatherConfigured(context),
            activeProviderDescription = if (isOpenWeatherConfigured(context)) {
                "OpenWeatherMap Live API (Auto-Injected)"
            } else {
                "Open-Meteo Live Keyless API (Auto-Fallback)"
            }
        ),
        BalasoreApiKeyStatus(
            serviceName = "Balasore News",
            secretKeyName = "NEWS_API_KEY",
            isLiveKeyConfigured = isNewsApiConfigured(),
            activeProviderDescription = if (isNewsApiConfigured()) {
                "NewsAPI + Google News RSS (Auto-Injected)"
            } else {
                "Google News Live Keyless RSS (Auto-Active)"
            }
        ),
        BalasoreApiKeyStatus(
            serviceName = "Local Database",
            secretKeyName = "BALASORE_DB_PATH",
            isLiveKeyConfigured = true,
            activeProviderDescription = "Auto-Configured Room SQLite (${balasoreDbPath()})"
        ),
        BalasoreApiKeyStatus(
            serviceName = "Firebase Cloud & Civic Eye",
            secretKeyName = "FIREBASE_SERVICE_ACCOUNT_PATH",
            isLiveKeyConfigured = isFirebaseAutoConfigured(context),
            activeProviderDescription = if (isFirebaseAutoConfigured(context)) {
                "Auto-Configured via google-services.json (${firebaseProjectId().ifBlank { "Firebase" }})"
            } else {
                "Firebase Android SDK Auto-Init"
            }
        ),
        BalasoreApiKeyStatus(
            serviceName = "Maps & Hotspots",
            secretKeyName = "MAPS_API_KEY",
            isLiveKeyConfigured = isMapsConfigured(context),
            activeProviderDescription = if (isMapsConfigured(context)) {
                "Google Maps SDK + OSMDroid (Auto-Injected)"
            } else {
                "OSMDroid Interactive Map (Keyless)"
            }
        ),
        BalasoreApiKeyStatus(
            serviceName = "Gemini AI Guide",
            secretKeyName = "GEMINI_API_KEY",
            isLiveKeyConfigured = isGeminiConfigured(),
            activeProviderDescription = if (isGeminiConfigured()) {
                "Gemini Live API (Auto-Injected)"
            } else {
                "Add GEMINI_API_KEY in AI Studio Secrets"
            }
        )
    )
}
