package com.example

import com.example.data.model.D3TelemetryMode
import com.example.data.model.WeatherTimeframe
import com.example.data.model.WeatherTrendPoint
import com.example.data.model.WeatherTrendSummary
import com.example.ui.viewmodel.UniqueFeatureSheetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BalasoreD3ChartsTest {

    @Test
    fun testWeatherTimeframeValues() {
        val timeframes = WeatherTimeframe.entries
        assertEquals(3, timeframes.size)
        assertTrue(timeframes.contains(WeatherTimeframe.HOURLY_24H))
        assertTrue(timeframes.contains(WeatherTimeframe.HISTORICAL_7D))
        assertTrue(timeframes.contains(WeatherTimeframe.MONTHLY_30D))

        assertEquals("24H Diurnal", WeatherTimeframe.HOURLY_24H.label)
        assertEquals("7-Day Trend", WeatherTimeframe.HISTORICAL_7D.label)
        assertEquals("30-Day Climate", WeatherTimeframe.MONTHLY_30D.label)
    }

    @Test
    fun testD3TelemetryModes() {
        val modes = D3TelemetryMode.entries
        assertEquals(3, modes.size)
        assertTrue(modes.contains(D3TelemetryMode.TIDAL_TRENDS))
        assertTrue(modes.contains(D3TelemetryMode.WEATHER_TRENDS))
        assertTrue(modes.contains(D3TelemetryMode.CORRELATED_TRENDS))

        assertEquals("🌊", D3TelemetryMode.TIDAL_TRENDS.iconEmoji)
        assertEquals("⛅", D3TelemetryMode.WEATHER_TRENDS.iconEmoji)
        assertEquals("📊", D3TelemetryMode.CORRELATED_TRENDS.iconEmoji)
    }

    @Test
    fun testWeatherTrendPointCreationAndMetrics() {
        val now = System.currentTimeMillis()
        val point = WeatherTrendPoint(
            id = "wx_test_1",
            timestamp = now,
            timeLabel = "12:00 PM",
            tempCelsius = 33.5,
            feelsLikeCelsius = 39.2,
            humidityPercent = 76,
            rainfallMm = 14.5,
            windSpeedKmh = 22.0,
            weatherCondition = "Monsoon Shower",
            cycloneRiskIndex = "Advisory"
        )

        assertEquals("wx_test_1", point.id)
        assertEquals(now, point.timestamp)
        assertEquals("12:00 PM", point.timeLabel)
        assertEquals(33.5, point.tempCelsius, 0.01)
        assertEquals(39.2, point.feelsLikeCelsius, 0.01)
        assertEquals(76, point.humidityPercent)
        assertEquals(14.5, point.rainfallMm, 0.01)
        assertEquals(22.0, point.windSpeedKmh, 0.01)
        assertEquals("Monsoon Shower", point.weatherCondition)
        assertEquals("Advisory", point.cycloneRiskIndex)
    }

    @Test
    fun testWeatherTrendSummaryCalculation() {
        val summary = WeatherTrendSummary(
            currentTempCelsius = 31.0,
            maxTempCelsius = 35.5,
            minTempCelsius = 23.0,
            avgHumidityPercent = 78,
            totalRainfallMm = 45.2,
            maxWindSpeedKmh = 32.0,
            observationCount = 24,
            lastSyncTimestamp = 1726567200000L
        )

        assertEquals(31.0, summary.currentTempCelsius, 0.01)
        assertEquals(35.5, summary.maxTempCelsius, 0.01)
        assertEquals(23.0, summary.minTempCelsius, 0.01)
        assertEquals(78, summary.avgHumidityPercent)
        assertEquals(45.2, summary.totalRainfallMm, 0.01)
        assertEquals(32.0, summary.maxWindSpeedKmh, 0.01)
        assertEquals(24, summary.observationCount)
    }

    @Test
    fun testUniqueFeatureSheetTypeContainsD3Trends() {
        val sheetType = UniqueFeatureSheetType.HISTORICAL_D3_WEATHER_TIDAL_TRENDS
        assertNotNull(sheetType)
        assertEquals("HISTORICAL_D3_WEATHER_TIDAL_TRENDS", sheetType.name)
    }
}
