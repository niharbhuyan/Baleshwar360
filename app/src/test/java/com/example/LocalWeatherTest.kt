package com.example

import com.example.data.remote.*
import com.example.data.repository.OpenWeatherRepository
import com.example.ui.components.formatWeatherTemp
import com.example.ui.components.translateConditionToOdia
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for LocalWeather component logic, OpenWeatherMap integration,
 * temperature conversions, Odia localization, and fallback caching.
 */
class LocalWeatherTest {

    private lateinit var mockService: MockOpenWeatherMapApiService
    private lateinit var repository: OpenWeatherRepository

    @Before
    fun setUp() {
        mockService = MockOpenWeatherMapApiService()
        repository = OpenWeatherRepository(apiService = mockService)
    }

    @Test
    fun testTemperatureConversion_celsiusAndFahrenheit() {
        // 0°C should be 32°F
        assertEquals("0°C", formatWeatherTemp(0.0, isFahrenheit = false))
        assertEquals("32°F", formatWeatherTemp(0.0, isFahrenheit = true))

        // 25°C should be 77°F
        assertEquals("25°C", formatWeatherTemp(25.0, isFahrenheit = false))
        assertEquals("77°F", formatWeatherTemp(25.0, isFahrenheit = true))

        // 30°C should be 86°F
        assertEquals("30°C", formatWeatherTemp(30.0, isFahrenheit = false))
        assertEquals("86°F", formatWeatherTemp(30.0, isFahrenheit = true))

        // Negative temperature: -10°C should be 14°F
        assertEquals("-10°C", formatWeatherTemp(-10.0, isFahrenheit = false))
        assertEquals("14°F", formatWeatherTemp(-10.0, isFahrenheit = true))

        // Null safety fallback
        assertNotNull(formatWeatherTemp(null, isFahrenheit = false))
        assertNotNull(formatWeatherTemp(null, isFahrenheit = true))
    }

    @Test
    fun testConditionTranslationToOdia() {
        // Clear sky
        val clearOdia = translateConditionToOdia("clear sky")
        assertTrue("Expected clear sky translation in Odia", clearOdia.contains("ନିର୍ମଳ"))

        // Thunderstorm
        val thunderOdia = translateConditionToOdia("severe thunderstorm with lightning")
        assertTrue("Expected thunderstorm translation in Odia", thunderOdia.contains("ଝଡ଼ବର୍ଷା") || thunderOdia.contains("ବଜ୍ରପାତ"))

        // Heavy rain
        val rainOdia = translateConditionToOdia("heavy intensity rain")
        assertTrue("Expected rain translation in Odia", rainOdia.contains("ବର୍ଷା") || rainOdia.contains("ବୃଷ୍ଟିପାତ"))

        // Scattered clouds
        val cloudsOdia = translateConditionToOdia("scattered clouds")
        assertTrue("Expected clouds translation in Odia", cloudsOdia.contains("ମେଘ"))

        // Null check
        val defaultOdia = translateConditionToOdia(null)
        assertEquals("ନିର୍ମଳ ପାଣିପାଗ", defaultOdia)
    }

    @Test
    fun testCuratedBalasoreFallbackWeather_containsValidTelemetry() {
        val fallback = repository.getCuratedBalasoreWeatherFallback()

        assertNotNull("Fallback weather should not be null", fallback)
        assertEquals("Balasore", fallback.name)
        assertEquals(OpenWeatherRepository.BALASORE_LAT, fallback.coord?.lat ?: 0.0, 0.001)
        assertEquals(OpenWeatherRepository.BALASORE_LON, fallback.coord?.lon ?: 0.0, 0.001)

        val main = fallback.main
        assertNotNull("Main atmospheric telemetry should exist", main)
        assertTrue("Temperature should be realistic for Balasore coast", (main?.temp ?: 0.0) in 15.0..48.0)
        assertTrue("Humidity should be within 0-100%", (main?.humidity ?: 0) in 0..100)
        assertTrue("Pressure should be realistic", (main?.pressure ?: 0) > 950)

        assertTrue("Weather condition list must not be empty", fallback.weather.isNotEmpty())
        assertNotNull("Icon code should exist", fallback.weather.first().icon)
    }

    @Test
    fun testCuratedThreeDayForecast_hasThreeConsecutiveDays() {
        val forecast = repository.getCuratedBalasoreThreeDayForecastFallback()

        assertEquals("Forecast must have 3 days", 3, forecast.size)
        forecast.forEachIndexed { index, day ->
            assertEquals("Day index must match", index, day.dayIndex)
            assertNotNull("Day name in English must exist", day.dayName)
            assertNotNull("Day name in Odia must exist", day.odiaDayName)
            assertTrue("tempMax should be >= tempMin", day.tempMax >= day.tempMin)
            assertTrue("avgHumidity should be valid", day.avgHumidity in 0..100)
            assertTrue("popPercent should be in 0-100", day.popPercent in 0..100)
            assertTrue("Hourly slots should be available", day.hourlySlots.isNotEmpty())
        }
    }

    @Test
    fun testLiveApiDispatch_withValidMockResponse() = runTest {
        mockService.currentWeatherResponse = OpenWeatherCurrentResponse(
            name = "Balasore",
            coord = OpenWeatherCoord(lon = 86.9135, lat = 21.4934),
            main = OpenWeatherMain(temp = 31.0, humidity = 70, pressure = 1010),
            weather = listOf(OpenWeatherCondition(id = 800, main = "Clear", description = "clear sky", icon = "01d"))
        )

        val result = repository.fetchCurrentWeather("test_valid_api_key_12345")
        assertTrue("Result should be success", result.isSuccess)
        val data = result.getOrNull()
        assertNotNull(data)
        assertEquals("Balasore", data?.name)
        assertEquals("test_valid_api_key_12345", mockService.lastRequestedApiKey)
        assertEquals(21.4934, mockService.lastRequestedLat ?: 0.0, 0.001)
        assertEquals(86.9135, mockService.lastRequestedLon ?: 0.0, 0.001)
    }

    @Test
    fun testLiveApiFailure_gracefulFallbackWithoutCrashing() = runTest {
        mockService.currentWeatherException = java.io.IOException("Network unavailable")

        // fetchCurrentWeather with a key that fails network should fall back to cached data gracefully
        val result = repository.fetchCurrentWeather("test_key_network_down")
        assertTrue("Should still return success with fallback", result.isSuccess)
        val data = result.getOrNull()
        assertNotNull("Fallback data should be returned", data)
        assertEquals("Balasore", data?.name)
    }
}
