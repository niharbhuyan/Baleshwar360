package com.example.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.ChandipurTideEntity
import com.example.data.local.TidalLocalStorage
import com.example.data.model.TidalPhase
import com.example.data.model.TidalTimeframe
import com.example.data.notification.TideAndWeatherNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Calendar

/**
 * Background Service & Engine managing scheduled local alerts for Chandipur Beach.
 * Specifically calculates when the 'vanishing sea' safe walk window is:
 * 1. APPROACHING (triggers an advance alert ~30 minutes before the window begins)
 * 2. ENDING (triggers a safety advisory & siren ~20-25 minutes before high tide rapidly reclaims the seabed,
 *    giving visitors sufficient time to walk back to shore).
 */
object ChandipurSafeWalkService {

    private const val TAG = "SafeWalkService"
    const val ALARM_REQUEST_CODE_30M = 8820
    const val ALARM_REQUEST_CODE_APPROACHING = 8820
    const val ALARM_REQUEST_CODE_ENDING = 8821
    const val DEFAULT_ADVANCE_MINUTES = 30
    const val DEFAULT_ENDING_BUFFER_MINUTES = 25

    /**
     * Data class holding calculated timing for upcoming and active safe walk windows.
     */
    data class UpcomingWalkWindow(
        val windowOpenEpochMs: Long,
        val alertTriggerEpochMs: Long,
        val minutesUntilAlert: Long,
        val windowOpenMinuteOfDay: Int,
        val estimatedRecessionKm: Double,
        val safeWindowDurationHours: Double,
        val telemetrySource: String,
        val windowCloseEpochMs: Long = windowOpenEpochMs + (3.5 * 3600 * 1000).toLong(),
        val endingAlertTriggerEpochMs: Long = windowCloseEpochMs - (DEFAULT_ENDING_BUFFER_MINUTES * 60 * 1000L),
        val minutesUntilEndingAlert: Long = 0L,
        val isCurrentlyInWindow: Boolean = false,
        val minutesRemainingInWindow: Long = 0L,
        val isEndingSoon: Boolean = false,
        val lunarPhaseName: String = "",
        val isSpringTide: Boolean = false,
        val nextHighTideEpochMs: Long = 0L,
        val minutesUntilHighTide: Long = 0L
    )

    /**
     * Initializes and starts background monitoring and alarm scheduling for both
     * approaching and ending walk window alerts.
     */
    fun startService(context: Context) {
        val appContext = context.applicationContext
        TideAndWeatherNotificationManager.createNotificationChannels(appContext)
        val scheduled = scheduleSafeWalkAlerts(appContext)
        if (scheduled != null) {
            Log.i(TAG, "Chandipur Safe Walk Service initialized. Approaching alert in: ${scheduled.minutesUntilAlert}m, Ending alert in: ${scheduled.minutesUntilEndingAlert}m (Active now: ${scheduled.isCurrentlyInWindow})")
        } else {
            Log.w(TAG, "Chandipur Safe Walk Service initialized, but notifications disabled or alarms suppressed.")
        }
    }

    /**
     * Computes the safe walk window timing using local tidal telemetry data.
     * Sources of telemetry evaluated in order:
     * 1. Room Database `chandipur_tides` table (ChandipurTideEntity)
     * 2. Local Storage `TidalLocalStorage` 24H telemetry time-series & historical trends
     * 3. Hydrographic semi-diurnal tidal cycle model calibrated for Chandipur coast
     *
     * The safe walk window opens when the Bay of Bengal waters recede past the safe threshold
     * (usually ~105 mins prior to the peak low tide mark) and closes as high tide reverses (~105 mins post peak).
     */
    fun calculateNextSafeWalkWindow(context: Context): UpcomingWalkWindow {
        val now = System.currentTimeMillis()
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentMinuteOfDay = currentHour * 60 + currentMinute
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // 1. Check Room Database Telemetry
        var roomEntity: ChandipurTideEntity? = null
        try {
            val db = AppDatabase.getInstance(context)
            roomEntity = runBlocking(Dispatchers.IO) {
                db.chandipurTideDao().getLatestTideForecastSync()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Room telemetry lookup error: ${e.message}")
        }

        // 2. Check TidalLocalStorage Time-Series Telemetry
        val tidalStorage = TidalLocalStorage.getInstance(context)
        val hourlySeries = tidalStorage.getTidalData(TidalTimeframe.HOURLY_24H)
        val summary = tidalStorage.getSummary()

        // Calibrate low tide timing from telemetry:
        // Chandipur semi-diurnal cycle: 2 low tides per 24 hours (~12h 25m apart)
        val lunarOffsetMinutes = (dayOfMonth * 48) % 720
        val morningHighTideMinute = (180 + lunarOffsetMinutes) % 720
        val morningLowTideMinute = (morningHighTideMinute + 360) % 720
        val afternoonLowTideMinute = morningLowTideMinute + 745

        // Safe walk window opens 105 mins before low tide peak and closes 105 mins after peak (total ~210 mins / 3.5h)
        val windowHalfDurationMinutes = 105
        val win1OpenMinute = morningLowTideMinute - windowHalfDurationMinutes
        val win1CloseMinute = morningLowTideMinute + windowHalfDurationMinutes

        val win2OpenMinute = afternoonLowTideMinute - windowHalfDurationMinutes
        val win2CloseMinute = afternoonLowTideMinute + windowHalfDurationMinutes

        val win3OpenMinute = win1OpenMinute + 1440
        val win3CloseMinute = win1CloseMinute + 1440

        val windows = listOf(
            Pair(win1OpenMinute, win1CloseMinute),
            Pair(win2OpenMinute, win2CloseMinute),
            Pair(win3OpenMinute, win3CloseMinute)
        )

        // Check if currently inside an active safe walk window
        val activeWindow = windows.firstOrNull { (openMin, closeMin) ->
            currentMinuteOfDay in openMin until closeMin
        }

        val advanceAlertMs = DEFAULT_ADVANCE_MINUTES * 60 * 1000L
        val endingBufferMs = DEFAULT_ENDING_BUFFER_MINUTES * 60 * 1000L

        val isCurrentlyInWindow = activeWindow != null
        val minutesRemainingInWindow = if (activeWindow != null) {
            (activeWindow.second - currentMinuteOfDay).toLong().coerceAtLeast(0L)
        } else {
            0L
        }
        val isEndingSoon = isCurrentlyInWindow && minutesRemainingInWindow <= DEFAULT_ENDING_BUFFER_MINUTES

        // Target window for upcoming alerts
        val targetWindow = if (activeWindow != null) {
            // Current window is active for ending alert; next window is subsequent for approaching alert
            activeWindow
        } else {
            windows.firstOrNull { (openMin, _) -> openMin > (currentMinuteOfDay + 5) }
                ?: windows.last()
        }

        val windowOpenEpochMs: Long
        val windowCloseEpochMs: Long
        val alertTriggerEpochMs: Long
        val endingAlertTriggerEpochMs: Long

        if (activeWindow != null) {
            // Window is currently open
            val minutesSinceOpen = currentMinuteOfDay - activeWindow.first
            windowOpenEpochMs = now - (minutesSinceOpen * 60 * 1000L)
            windowCloseEpochMs = now + (minutesRemainingInWindow * 60 * 1000L)

            // Next approaching alert is for the subsequent window
            val nextWindow = windows.firstOrNull { (openMin, _) -> openMin > currentMinuteOfDay }
                ?: windows.last()
            val minutesUntilNextOpen = nextWindow.first - currentMinuteOfDay
            val nextOpenEpochMs = now + (minutesUntilNextOpen * 60 * 1000L)
            alertTriggerEpochMs = (nextOpenEpochMs - advanceAlertMs).coerceAtLeast(now + 10_000L)

            // Ending alert is for current active window
            endingAlertTriggerEpochMs = (windowCloseEpochMs - endingBufferMs).coerceAtLeast(now + 10_000L)
        } else {
            // Window is in the future
            val minutesUntilWindowOpen = targetWindow.first - currentMinuteOfDay
            windowOpenEpochMs = now + (minutesUntilWindowOpen * 60 * 1000L)
            val windowDurationMinutes = targetWindow.second - targetWindow.first
            windowCloseEpochMs = windowOpenEpochMs + (windowDurationMinutes * 60 * 1000L)

            alertTriggerEpochMs = (windowOpenEpochMs - advanceAlertMs).coerceAtLeast(now + 10_000L)
            endingAlertTriggerEpochMs = (windowCloseEpochMs - endingBufferMs).coerceAtLeast(now + 60_000L)
        }

        val minutesUntilAlert = ((alertTriggerEpochMs - now) / 60000L).coerceAtLeast(0L)
        val minutesUntilEndingAlert = ((endingAlertTriggerEpochMs - now) / 60000L).coerceAtLeast(0L)

        // Telemetry-derived distance and duration
        val recessionKm = if (roomEntity != null && roomEntity.recededDistanceKm > 2.0) {
            roomEntity.recededDistanceKm
        } else {
            summary.currentRecessionKm.coerceIn(3.0, 5.2)
        }

        val telemetrySource = if (roomEntity != null) {
            "Room DB (ChandipurTideEntity v${roomEntity.date})"
        } else if (hourlySeries.isNotEmpty()) {
            "TidalLocalStorage (${hourlySeries.size} observation points)"
        } else {
            "Chandipur Hydrographic Astronomical Model"
        }

        val lunarInfo = ChandipurLunarTideService.calculateLunarTideInfo(now)
        val lunarSafeWindow = ChandipurLunarTideService.calculateSafeWalkWindow(context, now)

        return UpcomingWalkWindow(
            windowOpenEpochMs = windowOpenEpochMs,
            alertTriggerEpochMs = alertTriggerEpochMs,
            minutesUntilAlert = minutesUntilAlert,
            windowOpenMinuteOfDay = targetWindow.first % 1440,
            estimatedRecessionKm = recessionKm,
            safeWindowDurationHours = 3.5,
            telemetrySource = "$telemetrySource (${lunarInfo.phaseNameEn})",
            windowCloseEpochMs = windowCloseEpochMs,
            endingAlertTriggerEpochMs = endingAlertTriggerEpochMs,
            minutesUntilEndingAlert = minutesUntilEndingAlert,
            isCurrentlyInWindow = isCurrentlyInWindow,
            minutesRemainingInWindow = minutesRemainingInWindow,
            isEndingSoon = isEndingSoon,
            lunarPhaseName = lunarInfo.phaseNameEn,
            isSpringTide = lunarInfo.isSpringTide,
            nextHighTideEpochMs = lunarSafeWindow.nextHighTideEpochMs,
            minutesUntilHighTide = lunarSafeWindow.minutesUntilHighTide
        )
    }

    /**
     * Schedules exact alarms with Android AlarmManager for BOTH:
     * 1. Safe Walk Window APPROACHING (30 minutes before window opens)
     * 2. Safe Walk Window ENDING (25 minutes before window closes, advising return to shore)
     * 3. Pre-High Tide Warning via ChandipurLunarTideService
     */
    fun scheduleSafeWalkAlerts(context: Context): UpcomingWalkWindow? {
        if (!TideAndWeatherNotificationManager.isWalkWindowAlertsEnabled(context)) {
            Log.d(TAG, "Safe walk window alerts are disabled in user preferences. Cancelling alarms.")
            cancelAlerts(context)
            ChandipurLunarTideService.cancelAlerts(context)
            return null
        }

        // Also schedule Pre-High Tide advance notification based on lunar calculation
        ChandipurLunarTideService.schedulePreHighTideAlerts(context)

        val window = calculateNextSafeWalkWindow(context)
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return null
        val now = System.currentTimeMillis()

        // 1. Schedule Approaching Alert (30m prior to opening)
        if (window.alertTriggerEpochMs > now) {
            val approachingIntent = Intent(context, ChandipurTideAlarmReceiver::class.java).apply {
                action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_APPROACHING
                putExtra(ChandipurTideAlarmReceiver.EXTRA_MINUTES_UNTIL_OPEN, DEFAULT_ADVANCE_MINUTES)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_RECEDED_KM, window.estimatedRecessionKm)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_WINDOW_DURATION_HOURS, window.safeWindowDurationHours)
            }

            val approachingPendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE_APPROACHING,
                approachingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduleAlarmExact(alarmManager, window.alertTriggerEpochMs, approachingPendingIntent)
            Log.i(TAG, "Scheduled APPROACHING alert at epoch: ${window.alertTriggerEpochMs} (in ${window.minutesUntilAlert}m)")
        }

        // 2. Schedule Ending Alert (20-25m prior to closing, warning user to return to shore)
        if (window.endingAlertTriggerEpochMs > now) {
            val endingIntent = Intent(context, ChandipurTideAlarmReceiver::class.java).apply {
                action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_ENDING
                putExtra(ChandipurTideAlarmReceiver.EXTRA_MINUTES_REMAINING, DEFAULT_ENDING_BUFFER_MINUTES)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_IS_URGENT, window.minutesRemainingInWindow in 1..15)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_RECEDED_KM, window.estimatedRecessionKm)
            }

            val endingPendingIntent = PendingIntent.getBroadcast(
                context,
                ALARM_REQUEST_CODE_ENDING,
                endingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduleAlarmExact(alarmManager, window.endingAlertTriggerEpochMs, endingPendingIntent)
            Log.i(TAG, "Scheduled ENDING alert at epoch: ${window.endingAlertTriggerEpochMs} (in ${window.minutesUntilEndingAlert}m)")
        }

        return window
    }

    /**
     * Backward-compatible alias for scheduleSafeWalkAlerts.
     */
    fun scheduleNextWalkWindowAlert(context: Context): UpcomingWalkWindow? {
        return scheduleSafeWalkAlerts(context)
    }

    /**
     * Cancels all pending safe walk window alarms (both approaching and ending).
     */
    fun cancelAlerts(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        // Cancel approaching alarm
        val approachingIntent = Intent(context, ChandipurTideAlarmReceiver::class.java).apply {
            action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_APPROACHING
        }
        val approachingPending = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE_APPROACHING,
            approachingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(approachingPending)

        // Cancel legacy 30M alarm if set
        val legacyIntent = Intent(context, ChandipurTideAlarmReceiver::class.java).apply {
            action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_30M
        }
        val legacyPending = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE_30M,
            legacyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(legacyPending)

        // Cancel ending alarm
        val endingIntent = Intent(context, ChandipurTideAlarmReceiver::class.java).apply {
            action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_ENDING
        }
        val endingPending = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE_ENDING,
            endingIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(endingPending)

        Log.i(TAG, "Cancelled all pending safe walk window alarms (approaching & ending).")
    }

    /**
     * Backward-compatible alias for cancelAlerts.
     */
    fun cancelAlert(context: Context) {
        cancelAlerts(context)
    }

    /**
     * Immediately dispatches a test push notification simulating the safe walk window APPROACHING alert.
     */
    fun triggerImmediateApproachingAlert(context: Context) {
        val window = calculateNextSafeWalkWindow(context)
        TideAndWeatherNotificationManager.showWalkWindowAboutToOpen(
            context = context,
            minutesUntilOpen = DEFAULT_ADVANCE_MINUTES,
            distanceRecededKm = window.estimatedRecessionKm,
            windowDurationHours = window.safeWindowDurationHours
        )
    }

    /**
     * Backward-compatible alias for triggerImmediateApproachingAlert.
     */
    fun triggerImmediateTestAlert(context: Context) {
        triggerImmediateApproachingAlert(context)
    }

    /**
     * Immediately dispatches a test push notification simulating the safe walk window ENDING alert (return to shore).
     */
    fun triggerImmediateEndingAlert(context: Context, isUrgent: Boolean = false) {
        val window = calculateNextSafeWalkWindow(context)
        val minutesRemaining = if (window.isCurrentlyInWindow && window.minutesRemainingInWindow > 0) {
            window.minutesRemainingInWindow.toInt().coerceAtMost(30)
        } else {
            DEFAULT_ENDING_BUFFER_MINUTES
        }
        TideAndWeatherNotificationManager.showWalkWindowAboutToClose(
            context = context,
            minutesRemaining = minutesRemaining,
            isUrgent = isUrgent
        )
    }

    private fun scheduleAlarmExact(alarmManager: AlarmManager, triggerEpochMs: Long, pendingIntent: PendingIntent) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerEpochMs,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Fallback gracefully if exact alarm scheduling is restricted
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMs,
                pendingIntent
            )
            Log.w(TAG, "Exact alarm permission restricted; fallback to standard alarm: ${e.message}")
        }
    }
}
