package com.example.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.notification.TideAndWeatherNotificationManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver responsible for handling exact alarm notifications
 * scheduled when the Chandipur 'vanishing sea' safe walk window is
 * APPROACHING (e.g., 30 minutes in advance) or ENDING (e.g., 20-30 minutes before closing,
 * warning visitors that incoming high tide is returning rapidly to shore).
 *
 * It evaluates local tidal telemetry data, dispatches the high-priority local push notification alert,
 * and schedules the next tidal safe walk window alerts.
 */
class ChandipurTideAlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action ?: ACTION_TIDE_ALARM
        Log.i(TAG, "ChandipurTideAlarmReceiver received intent action: $action")

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                when (action) {
                    ACTION_TIDE_ALARM,
                    ACTION_TRIGGER_SAFE_WALK_30M,
                    ACTION_TRIGGER_SAFE_WALK_APPROACHING -> {
                        val minutesUntilOpen = intent.getIntExtra(EXTRA_MINUTES_UNTIL_OPEN, 30)
                        val recededDistance = intent.getDoubleExtra(EXTRA_RECEDED_KM, 3.5)
                        val windowDuration = intent.getDoubleExtra(EXTRA_WINDOW_DURATION_HOURS, 3.5)

                        Log.i(TAG, "Executing Safe Walk Approaching Alert push notification (receded: ${recededDistance}km, in ${minutesUntilOpen}m)")

                        // Dispatch local push notification for approaching safe walk window
                        TideAndWeatherNotificationManager.showWalkWindowAboutToOpen(
                            context = context,
                            minutesUntilOpen = minutesUntilOpen,
                            distanceRecededKm = recededDistance,
                            windowDurationHours = windowDuration
                        )

                        // Re-evaluate and schedule next tidal walk cycle alerts
                        ChandipurSafeWalkService.scheduleSafeWalkAlerts(context)
                    }

                    ACTION_TRIGGER_SAFE_WALK_ENDING -> {
                        val minutesRemaining = intent.getIntExtra(EXTRA_MINUTES_REMAINING, 25)
                        val isUrgent = intent.getBooleanExtra(EXTRA_IS_URGENT, false)

                        Log.i(TAG, "Executing Safe Walk ENDING Alert push notification (minutes remaining: $minutesRemaining, isUrgent: $isUrgent)")

                        // Dispatch local push notification warning that safe walk window is ending
                        TideAndWeatherNotificationManager.showWalkWindowAboutToClose(
                            context = context,
                            minutesRemaining = minutesRemaining,
                            isUrgent = isUrgent
                        )

                        // Re-evaluate and schedule next tidal walk cycle alerts
                        ChandipurSafeWalkService.scheduleSafeWalkAlerts(context)
                        ChandipurLunarTideService.schedulePreHighTideAlerts(context)
                    }

                    ACTION_TRIGGER_HIGH_TIDE_WARNING -> {
                        val minutesUntilHighTide = intent.getIntExtra(EXTRA_MINUTES_UNTIL_HIGH_TIDE, 30)
                        val tideHeight = intent.getDoubleExtra(EXTRA_TIDE_HEIGHT, 4.2)
                        val lunarInfo = ChandipurLunarTideService.calculateLunarTideInfo()

                        Log.i(TAG, "Executing Pre-High Tide Warning push notification (in ${minutesUntilHighTide}m, lunar phase: ${lunarInfo.phaseNameEn})")

                        TideAndWeatherNotificationManager.showLunarHighTideApproachingAlert(
                            context = context,
                            minutesUntilHighTide = minutesUntilHighTide,
                            lunarInfo = lunarInfo,
                            estimatedHeight = tideHeight
                        )

                        // Re-evaluate and schedule next lunar high tide alerts
                        ChandipurLunarTideService.schedulePreHighTideAlerts(context)
                    }

                    Intent.ACTION_BOOT_COMPLETED,
                    Intent.ACTION_MY_PACKAGE_REPLACED,
                    Intent.ACTION_TIME_CHANGED,
                    Intent.ACTION_TIMEZONE_CHANGED -> {
                        Log.i(TAG, "System event $action: Rescheduling Chandipur safe walk background alarms")
                        ChandipurSafeWalkService.startService(context)
                        ChandipurLunarTideService.schedulePreHighTideAlerts(context)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing Chandipur safe walk alarm", e)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val TAG = "ChandipurTideAlarm"
        const val ACTION_TIDE_ALARM = "com.example.action.CHANDIPUR_SAFE_WALK_ALARM"
        const val ACTION_TRIGGER_SAFE_WALK_30M = "com.example.action.TRIGGER_SAFE_WALK_30M"
        const val ACTION_TRIGGER_SAFE_WALK_APPROACHING = "com.example.action.TRIGGER_SAFE_WALK_APPROACHING"
        const val ACTION_TRIGGER_SAFE_WALK_ENDING = "com.example.action.TRIGGER_SAFE_WALK_ENDING"
        const val ACTION_TRIGGER_HIGH_TIDE_WARNING = "com.example.action.TRIGGER_HIGH_TIDE_WARNING"

        const val EXTRA_MINUTES_UNTIL_OPEN = "extra_minutes_until_open"
        const val EXTRA_MINUTES_REMAINING = "extra_minutes_remaining"
        const val EXTRA_IS_URGENT = "extra_is_urgent"
        const val EXTRA_RECEDED_KM = "extra_receded_km"
        const val EXTRA_WINDOW_DURATION_HOURS = "extra_window_duration_hours"
        const val EXTRA_MINUTES_UNTIL_HIGH_TIDE = "extra_minutes_until_high_tide"
        const val EXTRA_TIDE_HEIGHT = "extra_tide_height"
        const val EXTRA_LUNAR_PHASE = "extra_lunar_phase"
        const val EXTRA_IS_SPRING_TIDE = "extra_is_spring_tide"
        const val EXTRA_WATER_VELOCITY = "extra_water_velocity"
    }
}
