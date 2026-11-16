package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.WeatherTimeframe
import com.example.data.model.WeatherTrendPoint
import com.example.data.model.WeatherTrendSummary
import org.json.JSONArray
import org.json.JSONObject

/**
 * Local Storage manager for Balasore historical weather and meteorological trends.
 * Persists and retrieves time-series data (temperature, humidity, rainfall, wind) locally
 * using Android private preferences and JSON serialization to guarantee a 100% offline-first experience.
 */
class WeatherHistoricalLocalStorage(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    companion object {
        private const val PREFS_NAME = "balasore_weather_historical_storage"
        private const val KEY_HOURLY_DATA = "key_weather_hourly_24h"
        private const val KEY_HISTORICAL_7D = "key_weather_historical_7d"
        private const val KEY_MONTHLY_30D = "key_weather_monthly_30d"
        private const val KEY_LAST_SYNC = "key_weather_last_sync_timestamp"
        private const val KEY_INITIALIZED = "key_weather_storage_initialized"

        @Volatile
        private var INSTANCE: WeatherHistoricalLocalStorage? = null

        fun getInstance(context: Context): WeatherHistoricalLocalStorage {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WeatherHistoricalLocalStorage(context).also { INSTANCE = it }
            }
        }
    }

    init {
        if (!prefs.getBoolean(KEY_INITIALIZED, false)) {
            seedDefaultData()
        }
    }

    fun getWeatherData(timeframe: WeatherTimeframe): List<WeatherTrendPoint> {
        val key = when (timeframe) {
            WeatherTimeframe.HOURLY_24H -> KEY_HOURLY_DATA
            WeatherTimeframe.HISTORICAL_7D -> KEY_HISTORICAL_7D
            WeatherTimeframe.MONTHLY_30D -> KEY_MONTHLY_30D
        }
        val rawJson = prefs.getString(key, null) ?: return getDefaultPointsForTimeframe(timeframe)
        val list = parseJsonToList(rawJson)
        return if (list.isEmpty()) getDefaultPointsForTimeframe(timeframe) else list
    }

    fun saveWeatherData(timeframe: WeatherTimeframe, points: List<WeatherTrendPoint>) {
        val key = when (timeframe) {
            WeatherTimeframe.HOURLY_24H -> KEY_HOURLY_DATA
            WeatherTimeframe.HISTORICAL_7D -> KEY_HISTORICAL_7D
            WeatherTimeframe.MONTHLY_30D -> KEY_MONTHLY_30D
        }
        val jsonString = serializeListToJson(points)
        prefs.edit()
            .putString(key, jsonString)
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()
    }

    fun addObservation(point: WeatherTrendPoint, timeframe: WeatherTimeframe = WeatherTimeframe.HOURLY_24H) {
        val current = getWeatherData(timeframe).toMutableList()
        current.add(point)
        saveWeatherData(timeframe, current)
    }

    fun getSummary(): WeatherTrendSummary {
        val hourly = getWeatherData(WeatherTimeframe.HOURLY_24H)
        val historical = getWeatherData(WeatherTimeframe.HISTORICAL_7D)
        val allPoints = (hourly + historical).ifEmpty { hourly }

        val currentPoint = hourly.lastOrNull() ?: hourly.firstOrNull()
        val currentTemp = currentPoint?.tempCelsius ?: 29.0
        val maxTemp = allPoints.maxOfOrNull { it.tempCelsius } ?: 34.0
        val minTemp = allPoints.minOfOrNull { it.tempCelsius } ?: 22.0
        val avgHum = if (allPoints.isNotEmpty()) (allPoints.sumOf { it.humidityPercent } / allPoints.size) else 75
        val totalRain = allPoints.sumOf { it.rainfallMm }
        val maxWind = allPoints.maxOfOrNull { it.windSpeedKmh } ?: 25.0
        val lastSync = prefs.getLong(KEY_LAST_SYNC, System.currentTimeMillis())

        return WeatherTrendSummary(
            currentTempCelsius = currentTemp,
            maxTempCelsius = maxTemp,
            minTempCelsius = minTemp,
            avgHumidityPercent = avgHum,
            totalRainfallMm = totalRain,
            maxWindSpeedKmh = maxWind,
            observationCount = allPoints.size,
            lastSyncTimestamp = lastSync
        )
    }

    fun resetToDefaults() {
        seedDefaultData()
    }

    private fun seedDefaultData() {
        val hourly = getDefault24HourData()
        val historical7d = getDefault7DayData()
        val monthly30d = getDefault30DayData()

        prefs.edit()
            .putString(KEY_HOURLY_DATA, serializeListToJson(hourly))
            .putString(KEY_HISTORICAL_7D, serializeListToJson(historical7d))
            .putString(KEY_MONTHLY_30D, serializeListToJson(monthly30d))
            .putBoolean(KEY_INITIALIZED, true)
            .putLong(KEY_LAST_SYNC, System.currentTimeMillis())
            .apply()
    }

    private fun getDefaultPointsForTimeframe(timeframe: WeatherTimeframe): List<WeatherTrendPoint> {
        return when (timeframe) {
            WeatherTimeframe.HOURLY_24H -> getDefault24HourData()
            WeatherTimeframe.HISTORICAL_7D -> getDefault7DayData()
            WeatherTimeframe.MONTHLY_30D -> getDefault30DayData()
        }
    }

    private fun getDefault24HourData(): List<WeatherTrendPoint> {
        val now = System.currentTimeMillis()
        val hourMs = 3600000L
        return listOf(
            WeatherTrendPoint("wx_24h_1", now - 21 * hourMs, "03:00 AM", 24.2, 26.0, 88, 0.0, 11.0, "Clear Night"),
            WeatherTrendPoint("wx_24h_2", now - 18 * hourMs, "06:00 AM", 23.8, 25.2, 92, 0.2, 9.5, "Misty Dawn"),
            WeatherTrendPoint("wx_24h_3", now - 15 * hourMs, "09:00 AM", 27.5, 30.1, 82, 0.0, 14.0, "Sunny"),
            WeatherTrendPoint("wx_24h_4", now - 12 * hourMs, "12:00 PM", 32.8, 38.4, 72, 0.0, 18.2, "Hot & Humid"),
            WeatherTrendPoint("wx_24h_5", now - 9 * hourMs, "03:00 PM", 33.6, 40.2, 68, 2.5, 22.0, "Coastal Breezes"),
            WeatherTrendPoint("wx_24h_6", now - 6 * hourMs, "06:00 PM", 29.4, 34.0, 79, 8.4, 26.5, "Monsoon Shower", "Advisory"),
            WeatherTrendPoint("wx_24h_7", now - 3 * hourMs, "09:00 PM", 26.8, 30.0, 85, 3.2, 16.0, "Overcast"),
            WeatherTrendPoint("wx_24h_8", now, "12:00 AM", 25.5, 28.2, 89, 0.0, 12.0, "Partly Cloudy")
        )
    }

    private fun getDefault7DayData(): List<WeatherTrendPoint> {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        return listOf(
            WeatherTrendPoint("wx_7d_1", now - 6 * dayMs, "Day -6", 31.5, 36.2, 74, 0.0, 14.5, "Clear Sunny"),
            WeatherTrendPoint("wx_7d_2", now - 5 * dayMs, "Day -5", 33.2, 39.0, 71, 0.0, 16.0, "Hot Weather"),
            WeatherTrendPoint("wx_7d_3", now - 4 * dayMs, "Day -4", 34.8, 41.5, 69, 1.2, 20.0, "Pre-Monsoon Clouds"),
            WeatherTrendPoint("wx_7d_4", now - 3 * dayMs, "Day -3", 28.6, 33.0, 88, 24.5, 34.0, "Heavy Rainfall", "Warning"),
            WeatherTrendPoint("wx_7d_5", now - 2 * dayMs, "Day -2", 27.2, 31.0, 91, 18.2, 28.0, "Squall Overcast", "Advisory"),
            WeatherTrendPoint("wx_7d_6", now - 1 * dayMs, "Yesterday", 29.8, 34.2, 80, 4.0, 19.5, "Clearing Up"),
            WeatherTrendPoint("wx_7d_7", now, "Today", 32.0, 37.5, 76, 0.5, 15.0, "Scattered Sun")
        )
    }

    private fun getDefault30DayData(): List<WeatherTrendPoint> {
        val now = System.currentTimeMillis()
        val dayMs = 86400000L
        return listOf(
            WeatherTrendPoint("wx_30d_1", now - 28 * dayMs, "W1-Early", 33.5, 39.2, 68, 5.0, 15.0, "Pre-cyclonic Heat"),
            WeatherTrendPoint("wx_30d_2", now - 24 * dayMs, "W1-Late", 34.2, 40.8, 65, 0.0, 17.5, "Dry Spurt"),
            WeatherTrendPoint("wx_30d_3", now - 20 * dayMs, "W2-Mid", 31.0, 36.5, 78, 12.0, 22.0, "Depression Forming"),
            WeatherTrendPoint("wx_30d_4", now - 16 * dayMs, "W3-Early", 26.5, 30.5, 94, 52.0, 46.0, "Deep Bay Depression", "Warning"),
            WeatherTrendPoint("wx_30d_5", now - 12 * dayMs, "W3-Late", 28.0, 32.8, 88, 28.0, 30.0, "Monsoon Surge"),
            WeatherTrendPoint("wx_30d_6", now - 8 * dayMs, "W4-Early", 30.2, 35.0, 80, 8.5, 18.0, "Moderate Rain"),
            WeatherTrendPoint("wx_30d_7", now - 4 * dayMs, "W4-Late", 32.5, 38.0, 74, 2.0, 16.0, "Sunny Intervals"),
            WeatherTrendPoint("wx_30d_8", now, "Current", 31.8, 36.8, 77, 1.0, 14.0, "Stable Coastal")
        )
    }

    private fun serializeListToJson(points: List<WeatherTrendPoint>): String {
        val array = JSONArray()
        for (p in points) {
            val obj = JSONObject().apply {
                put("id", p.id)
                put("timestamp", p.timestamp)
                put("timeLabel", p.timeLabel)
                put("tempCelsius", p.tempCelsius)
                put("feelsLikeCelsius", p.feelsLikeCelsius)
                put("humidityPercent", p.humidityPercent)
                put("rainfallMm", p.rainfallMm)
                put("windSpeedKmh", p.windSpeedKmh)
                put("weatherCondition", p.weatherCondition)
                put("cycloneRiskIndex", p.cycloneRiskIndex)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun parseJsonToList(jsonString: String): List<WeatherTrendPoint> {
        val list = mutableListOf<WeatherTrendPoint>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val point = WeatherTrendPoint(
                    id = obj.optString("id", "wx_$i"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    timeLabel = obj.optString("timeLabel", ""),
                    tempCelsius = obj.optDouble("tempCelsius", 28.0),
                    feelsLikeCelsius = obj.optDouble("feelsLikeCelsius", 32.0),
                    humidityPercent = obj.optInt("humidityPercent", 75),
                    rainfallMm = obj.optDouble("rainfallMm", 0.0),
                    windSpeedKmh = obj.optDouble("windSpeedKmh", 15.0),
                    weatherCondition = obj.optString("weatherCondition", "Clear"),
                    cycloneRiskIndex = obj.optString("cycloneRiskIndex", "Normal")
                )
                list.add(point)
            }
        } catch (_: Exception) {}
        return list
    }
}
