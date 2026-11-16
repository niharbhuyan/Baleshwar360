package com.example.data.model

/**
 * Historical timeframes for Balasore weather trend analysis.
 */
enum class WeatherTimeframe(val label: String, val description: String) {
    HOURLY_24H("24H Diurnal", "Continuous 24-hour hourly temperature, humidity & heat index"),
    HISTORICAL_7D("7-Day Trend", "7-day daily maximum/minimum temperatures & monsoon rainfall"),
    MONTHLY_30D("30-Day Climate", "Month-long coastal climatic patterns & pre-cyclone trends")
}

/**
 * Granular observation data point representing historical weather metrics.
 */
data class WeatherTrendPoint(
    val id: String,
    val timestamp: Long,
    val timeLabel: String,
    val tempCelsius: Double,
    val feelsLikeCelsius: Double,
    val humidityPercent: Int,
    val rainfallMm: Double,
    val windSpeedKmh: Double,
    val weatherCondition: String,
    val cycloneRiskIndex: String = "Normal"
)

/**
 * Summary metrics computed from local weather storage.
 */
data class WeatherTrendSummary(
    val currentTempCelsius: Double,
    val maxTempCelsius: Double,
    val minTempCelsius: Double,
    val avgHumidityPercent: Int,
    val totalRainfallMm: Double,
    val maxWindSpeedKmh: Double,
    val observationCount: Int,
    val lastSyncTimestamp: Long
)

/**
 * Modes of telemetry visualization supported by the D3 charts component.
 */
enum class D3TelemetryMode(val label: String, val iconEmoji: String) {
    TIDAL_TRENDS("Tidal & Recession", "🌊"),
    WEATHER_TRENDS("Weather & Rain", "⛅"),
    CORRELATED_TRENDS("Coastal Correlation", "📊")
}
