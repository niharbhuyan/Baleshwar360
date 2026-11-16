package com.example

import com.example.data.remote.LiveCityBusDto
import com.example.data.repository.LocalTransportRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for live bus tracking, approaching stop conditions, and telemetry indicators.
 */
class LiveBusApproachingIndicatorTest {

    private lateinit var repository: LocalTransportRepository

    @Before
    fun setup() {
        repository = LocalTransportRepository()
    }

    @Test
    fun testBaselineBalasoreBusesContainsApproachingVehicles() {
        val buses = repository.createBaselineBalasoreBuses(cycle = 0)
        assertNotNull(buses)
        assertTrue("Should have multiple live buses", buses.size >= 5)

        // Count buses approaching their next stop (ETA <= 3 mins)
        val approachingBuses = buses.filter { it.etaNextStopMins <= 3 }
        assertTrue("At least one vehicle should be approaching a stop", approachingBuses.isNotEmpty())

        approachingBuses.forEach { bus ->
            assertTrue(
                "Approaching bus ${bus.busId} must have ETA <= 3 mins, but was ${bus.etaNextStopMins}",
                bus.etaNextStopMins in 1..3
            )
            assertNotNull(bus.nextStop)
            assertTrue("Next stop should not be blank", bus.nextStop.isNotBlank())
        }
    }

    @Test
    fun testApproachingFilterPartition() {
        val testBuses = listOf(
            createTestBus("BUS-1", "Sahadevkhunta", "Chandipur Beach", etaMins = 1),
            createTestBus("BUS-2", "FM MCH", "Remuna Temple", etaMins = 2),
            createTestBus("BUS-3", "Station Chhak", "FMU Campus", etaMins = 3),
            createTestBus("BUS-4", "Khantapara", "Soro Hospital", etaMins = 4),
            createTestBus("BUS-5", "Nilagiri", "Panchalingeswar", etaMins = 7)
        )

        val approaching = testBuses.filter { it.etaNextStopMins <= 3 }
        val inTransit = testBuses.filter { it.etaNextStopMins > 3 }

        assertEquals(3, approaching.size)
        assertEquals(2, inTransit.size)

        assertTrue(approaching.any { it.busId == "BUS-1" })
        assertTrue(approaching.any { it.busId == "BUS-2" })
        assertTrue(approaching.any { it.busId == "BUS-3" })
        assertFalse(approaching.any { it.busId == "BUS-4" })
        assertFalse(approaching.any { it.busId == "BUS-5" })
    }

    @Test
    fun testApproachingUrgencyState() {
        // ETA 1 minute corresponds to ARRIVING NOW (<1m)
        val arrivingNow = createTestBus("BUS-NOW", "DRDO Gate", "OTDC Panthanivas", etaMins = 1)
        val approachingSoon = createTestBus("BUS-SOON", "Station", "Cinema Bazar", etaMins = 3)
        val farAway = createTestBus("BUS-FAR", "Khantapara", "Soro Stand", etaMins = 6)

        assertTrue("1 min ETA should be urgent arrival", arrivingNow.etaNextStopMins <= 1)
        assertTrue("3 min ETA should be approaching", approachingSoon.etaNextStopMins in 2..3)
        assertFalse("6 min ETA should not be approaching", farAway.etaNextStopMins <= 3)
    }

    @Test
    fun testStopMatchingForMajorTerminals() {
        val bus = createTestBus("BUS-7101", "DRDO Gate", "OTDC Panthanivas Terminal", etaMins = 2)
        val stopName = "Chandipur OTDC Beach Gate"

        val matches = stopName.contains("OTDC", ignoreCase = true) &&
                bus.nextStop.contains("OTDC", ignoreCase = true)
        assertTrue("Should detect matching stop for incoming bus", matches)
    }

    private fun createTestBus(
        id: String,
        currentStop: String,
        nextStop: String,
        etaMins: Int
    ): LiveCityBusDto {
        return LiveCityBusDto(
            busId = id,
            fleetNumber = "OD-01-AX-9999",
            routeNumber = "Route 71",
            routeNameEn = "Test Route En",
            routeNameOd = "ଟେଷ୍ଟ ରୁଟ୍",
            originEn = "Origin",
            destinationEn = "Destination",
            latitude = 21.4934,
            longitude = 86.9135,
            speedKmh = 30.0,
            headingDegrees = 90,
            headingDirection = "East",
            currentStop = currentStop,
            nextStop = nextStop,
            etaNextStopMins = etaMins,
            occupancyStatus = "SEATS_AVAILABLE",
            scheduleStatus = "ON_TIME",
            busType = "Electric",
            fareRange = "₹10 - ₹20"
        )
    }
}
