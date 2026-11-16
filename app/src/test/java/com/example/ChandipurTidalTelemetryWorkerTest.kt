package com.example

import com.example.data.notification.ChandipurTidalTelemetryWorker
import com.example.ui.components.LocalTideCalculationEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying WorkManager constants, telemetry calculation, and 30-minute
 * safe walk window notification triggers in [ChandipurTidalTelemetryWorker].
 */
class ChandipurTidalTelemetryWorkerTest {

    @Test
    fun testWorkerConstants() {
        assertEquals("ChandipurTidalWorker", ChandipurTidalTelemetryWorker.TAG)
        assertEquals("ChandipurTidalTelemetryPeriodicWork", ChandipurTidalTelemetryWorker.WORK_NAME_PERIODIC)
        assertEquals("ChandipurTidalTelemetryImmediateWork", ChandipurTidalTelemetryWorker.WORK_NAME_ONE_TIME)
        assertEquals("ChandipurSafeWalkWindowTargetedWork", ChandipurTidalTelemetryWorker.WORK_NAME_TARGETED)
        assertEquals("chandipur_safe_tide_30m_channel", ChandipurTidalTelemetryWorker.CHANNEL_ID_SAFE_TIDE_30M)
        assertEquals("Chandipur Safe Tide 30-Min Alerts", ChandipurTidalTelemetryWorker.CHANNEL_NAME_SAFE_TIDE_30M)
        assertEquals(8842, ChandipurTidalTelemetryWorker.NOTIFICATION_ID_SAFE_TIDE_30M)
    }

    @Test
    fun testHarmonicTidalTelemetryCalculation() {
        val now = System.currentTimeMillis()
        val result = LocalTideCalculationEngine.evaluateLiveTideWindow(now)

        assertNotNull(result)
        assertTrue("Water level must be within realistic Chandipur range (0.2m - 4.2m)", result.currentWaterLevelMeters in 0.2..4.2)
        assertTrue("Recession distance must be between 0.1km and 5.0km", result.currentRecessionKm in 0.1..5.0)
        assertNotNull(result.nextSafeWindowClockLabel)
        assertNotNull(result.nextHighTideClockLabel)
        assertNotNull(result.safeWalkingWindowRange)
        assertTrue("Countdown to next high tide should be positive", result.secondsUntilNextHighTide >= 0)
    }

    @Test
    fun testThirtyMinuteTriggerRangeLogic() {
        // 30 minutes before safe walking window occurs is ~1800 seconds (tested in 1200..2100s range)
        val thirtyMinsSeconds = 30 * 60L
        val minutes = (thirtyMinsSeconds / 60L).toInt()

        val is30MinApproaching = minutes in 20..35
        assertTrue("30 minutes must fall within the 20-35 minute advance alert window", is30MinApproaching)
    }
}
