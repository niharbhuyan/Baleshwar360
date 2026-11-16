package com.example

import com.example.data.service.ChandipurSafeWalkService
import com.example.data.service.ChandipurTideAlarmReceiver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Chandipur Safe Walk Local Notification & Alarm Service,
 * testing both Approaching (30m prior) and Ending (advisory/siren to return to shore)
 * window alert calculations and action constants.
 */
class ChandipurSafeWalkServiceTest {

    @Test
    fun testAlarmReceiverActionConstants() {
        assertEquals("com.example.action.CHANDIPUR_SAFE_WALK_ALARM", ChandipurTideAlarmReceiver.ACTION_TIDE_ALARM)
        assertEquals("com.example.action.TRIGGER_SAFE_WALK_30M", ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_30M)
        assertEquals("com.example.action.TRIGGER_SAFE_WALK_APPROACHING", ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_APPROACHING)
        assertEquals("com.example.action.TRIGGER_SAFE_WALK_ENDING", ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_ENDING)
        assertEquals("extra_minutes_until_open", ChandipurTideAlarmReceiver.EXTRA_MINUTES_UNTIL_OPEN)
        assertEquals("extra_minutes_remaining", ChandipurTideAlarmReceiver.EXTRA_MINUTES_REMAINING)
        assertEquals("extra_is_urgent", ChandipurTideAlarmReceiver.EXTRA_IS_URGENT)
        assertEquals("extra_receded_km", ChandipurTideAlarmReceiver.EXTRA_RECEDED_KM)
        assertEquals("extra_window_duration_hours", ChandipurTideAlarmReceiver.EXTRA_WINDOW_DURATION_HOURS)
    }

    @Test
    fun testSafeWalkAlarmRequestCodes() {
        assertEquals(8820, ChandipurSafeWalkService.ALARM_REQUEST_CODE_30M)
        assertEquals(8820, ChandipurSafeWalkService.ALARM_REQUEST_CODE_APPROACHING)
        assertEquals(8821, ChandipurSafeWalkService.ALARM_REQUEST_CODE_ENDING)
        assertEquals(30, ChandipurSafeWalkService.DEFAULT_ADVANCE_MINUTES)
        assertEquals(25, ChandipurSafeWalkService.DEFAULT_ENDING_BUFFER_MINUTES)
    }

    @Test
    fun testUpcomingWalkWindowDataStructure() {
        val now = System.currentTimeMillis()
        val windowOpen = now + 45 * 60 * 1000L
        val alertTrigger = windowOpen - 30 * 60 * 1000L
        val windowClose = windowOpen + (3.5 * 3600 * 1000).toLong()
        val endingTrigger = windowClose - (25 * 60 * 1000L)

        val window = ChandipurSafeWalkService.UpcomingWalkWindow(
            windowOpenEpochMs = windowOpen,
            alertTriggerEpochMs = alertTrigger,
            minutesUntilAlert = 15L,
            windowOpenMinuteOfDay = 720,
            estimatedRecessionKm = 4.8,
            safeWindowDurationHours = 3.5,
            telemetrySource = "Tidal Telemetry Test",
            windowCloseEpochMs = windowClose,
            endingAlertTriggerEpochMs = endingTrigger,
            minutesUntilEndingAlert = 200L,
            isCurrentlyInWindow = false,
            minutesRemainingInWindow = 0L,
            isEndingSoon = false
        )

        assertEquals(15L, window.minutesUntilAlert)
        assertEquals(30 * 60 * 1000L, window.windowOpenEpochMs - window.alertTriggerEpochMs)
        assertEquals(4.8, window.estimatedRecessionKm, 0.001)
        assertEquals(3.5, window.safeWindowDurationHours, 0.001)
        assertTrue(window.telemetrySource.contains("Telemetry"))
        assertEquals(25 * 60 * 1000L, window.windowCloseEpochMs - window.endingAlertTriggerEpochMs)
        assertEquals(200L, window.minutesUntilEndingAlert)
        assertTrue(!window.isCurrentlyInWindow)
    }

    @Test
    fun testActiveWalkWindowEndingStatus() {
        val now = System.currentTimeMillis()
        val windowOpen = now - 60 * 60 * 1000L // opened 1 hour ago
        val windowClose = now + 20 * 60 * 1000L // closes in 20 minutes
        val endingTrigger = windowClose - 25 * 60 * 1000L

        val activeWindow = ChandipurSafeWalkService.UpcomingWalkWindow(
            windowOpenEpochMs = windowOpen,
            alertTriggerEpochMs = windowOpen - 30 * 60 * 1000L,
            minutesUntilAlert = 0L,
            windowOpenMinuteOfDay = 600,
            estimatedRecessionKm = 4.2,
            safeWindowDurationHours = 3.5,
            telemetrySource = "Hydrographic Realtime",
            windowCloseEpochMs = windowClose,
            endingAlertTriggerEpochMs = endingTrigger,
            minutesUntilEndingAlert = 0L,
            isCurrentlyInWindow = true,
            minutesRemainingInWindow = 20L,
            isEndingSoon = true
        )

        assertTrue(activeWindow.isCurrentlyInWindow)
        assertTrue(activeWindow.isEndingSoon)
        assertEquals(20L, activeWindow.minutesRemainingInWindow)
    }
}
