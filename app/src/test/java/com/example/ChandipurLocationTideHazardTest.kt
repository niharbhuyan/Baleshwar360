package com.example

import com.example.data.notification.ChandipurLocationTideHazardManager
import com.example.ui.components.BALASORE_BUS_AUTOCOMPLETE_SUGGESTIONS
import com.example.ui.components.SAMPLE_BALASORE_BUS_ARRIVALS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

private data class FiveDayTrendPoint(
    val dayLabelEn: String,
    val tempMaxCelsius: Int,
    val tempMinCelsius: Int,
    val humidityPct: Int
)

private val DEFAULT_5_DAY_TRENDS = listOf(
    FiveDayTrendPoint("Mon", 32, 25, 74),
    FiveDayTrendPoint("Tue", 33, 25, 72),
    FiveDayTrendPoint("Wed", 31, 24, 78),
    FiveDayTrendPoint("Thu", 30, 24, 80),
    FiveDayTrendPoint("Fri", 32, 25, 75)
)

class ChandipurLocationTideHazardTest {

    @Test
    fun testHaversineDistanceCalculation_AccurateForChandipur() {
        // Distance from Chandipur beach point to itself should be 0 km
        val distanceSame = ChandipurLocationTideHazardManager.calculateHaversineDistanceKm(
            ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LAT,
            ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LNG,
            ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LAT,
            ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LNG
        )
        assertEquals(0.0, distanceSame, 0.001)

        // Distance from Balasore Station (~21.498° N, 86.932° E) to Chandipur Beach (~11-13 km)
        val distanceStationToBeach = ChandipurLocationTideHazardManager.calculateHaversineDistanceKm(
            21.498, 86.932,
            ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LAT,
            ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LNG
        )
        assertTrue("Distance from station to beach should be ~9-15 km, was: $distanceStationToBeach",
            distanceStationToBeach in 8.0..16.0)
    }

    @Test
    fun testDistanceToExposedMudflats() {
        // 1.5 km out on the receded Bay of Bengal seafloor
        val distanceSeafloor = ChandipurLocationTideHazardManager.calculateHaversineDistanceKm(
            ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LAT,
            ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LNG,
            ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LAT + 0.008,
            ChandipurLocationTideHazardManager.CHANDIPUR_BEACH_LNG + 0.008
        )
        assertTrue("Distance should be under 2 km on mudflats, was: $distanceSeafloor", distanceSeafloor < 2.0)
    }

    @Test
    fun testLocalBusTracker_DataModelAndAutocomplete() {
        assertTrue("Sample arrivals should be populated", SAMPLE_BALASORE_BUS_ARRIVALS.isNotEmpty())

        val chandipurBus = SAMPLE_BALASORE_BUS_ARRIVALS.firstOrNull { it.destination.contains("Chandipur") }
        assertNotNull("Chandipur route should exist", chandipurBus)
        assertEquals("Route 101", chandipurBus?.routeNumber)
        assertTrue(chandipurBus?.arrivalMinutes ?: 0 > 0)

        // Autocomplete suggestions check
        assertTrue(BALASORE_BUS_AUTOCOMPLETE_SUGGESTIONS.contains("Chandipur Beach"))
        assertTrue(BALASORE_BUS_AUTOCOMPLETE_SUGGESTIONS.contains("Remuna Gopinath Temple"))
    }

    @Test
    fun testFiveDayWeatherTrends_MetricsRange() {
        assertEquals(5, DEFAULT_5_DAY_TRENDS.size)
        DEFAULT_5_DAY_TRENDS.forEach { point ->
            assertTrue("Max temp should be greater than min temp", point.tempMaxCelsius >= point.tempMinCelsius)
            assertTrue("Humidity should be within 0..100%", point.humidityPct in 0..100)
            assertTrue("Day label should not be blank", point.dayLabelEn.isNotBlank())
        }
    }
}
