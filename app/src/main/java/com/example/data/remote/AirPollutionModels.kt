package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * OpenWeatherMap Air Pollution API response models.
 * API Endpoint: data/2.5/air_pollution?lat={lat}&lon={lon}&appid={API_key}
 */
@JsonClass(generateAdapter = true)
data class OpenWeatherAirPollutionResponse(
    @Json(name = "coord") val coord: OpenWeatherCoord? = null,
    @Json(name = "list") val list: List<AirPollutionItem> = emptyList()
)

@JsonClass(generateAdapter = true)
data class AirPollutionItem(
    @Json(name = "main") val main: AirQualityIndexMain? = null,
    @Json(name = "components") val components: AirPollutionComponents? = null,
    @Json(name = "dt") val dt: Long? = null
)

@JsonClass(generateAdapter = true)
data class AirQualityIndexMain(
    /**
     * Air Quality Index.
     * 1 = Good
     * 2 = Fair
     * 3 = Moderate
     * 4 = Poor
     * 5 = Very Poor
     */
    @Json(name = "aqi") val aqi: Int = 2
)

@JsonClass(generateAdapter = true)
data class AirPollutionComponents(
    @Json(name = "co") val co: Double? = null,       // Carbon monoxide (μg/m3)
    @Json(name = "no") val no: Double? = null,       // Nitrogen monoxide (μg/m3)
    @Json(name = "no2") val no2: Double? = null,     // Nitrogen dioxide (μg/m3)
    @Json(name = "o3") val o3: Double? = null,       // Ozone (μg/m3)
    @Json(name = "so2") val so2: Double? = null,     // Sulphur dioxide (μg/m3)
    @Json(name = "pm2_5") val pm2_5: Double? = null, // Fine particles (μg/m3)
    @Json(name = "pm10") val pm10: Double? = null,   // Coarse particulate matter (μg/m3)
    @Json(name = "nh3") val nh3: Double? = null      // Ammonia (μg/m3)
)

/**
 * Outdoor Activity Safe Health Window descriptor.
 */
data class OutdoorSafeHealthWindow(
    val timeSlotEn: String,
    val timeSlotOr: String,
    val recommendationEn: String,
    val recommendationOr: String,
    val safetyStatus: String, // "OPTIMAL_SAFE", "MODERATE_CAUTION", "SENSITIVE_GROUPS_RESTRICT"
    val suitableActivitiesEn: String,
    val suitableActivitiesOr: String,
    val iconEmoji: String
)
