package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.MainActivity
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.ChandipurTideEntity
import com.example.data.service.ChandipurSafeWalkService
import com.example.ui.components.LocalTideCalculationEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * WorkManager CoroutineWorker that continuously monitors Chandipur Beach tidal telemetry data
 * (including offline harmonic tidal constituents and Room DB cached forecasts)
 * and triggers a rich local push notification 30 minutes before safe walking windows occur.
 *
 * This allows citizens, tourists, and researchers to prepare and walk safely on the exposed seabed
 * (where the Bay of Bengal waters recede up to 5 km) while ensuring timely return before high tide surge.
 */
class ChandipurTidalTelemetryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val context = applicationContext
        Log.d(TAG, "ChandipurTidalTelemetryWorker: Initiating tidal telemetry evaluation...")

        // 1. Verify user notification preference and master sync switches
        val isTideAlertEnabled = LocalTideCalculationEngine.isSafeTideReminderEnabled(context)
        val isWalkWindowAlertEnabled = TideAndWeatherNotificationManager.isWalkWindowAlertsEnabled(context)

        val forceTrigger = inputData.getBoolean(KEY_FORCE_TRIGGER, false)

        if (!forceTrigger && (!isTideAlertEnabled && !isWalkWindowAlertEnabled)) {
            Log.i(TAG, "ChandipurTidalTelemetryWorker: Tidal alerts disabled by user in settings. Skipping execution.")
            return@withContext Result.success(
                workDataOf(
                    KEY_STATUS to "SKIPPED",
                    KEY_MESSAGE to "Tidal alerts disabled by user"
                )
            )
        }

        try {
            // 2. Evaluate live harmonic tidal telemetry data
            val nowMillis = System.currentTimeMillis()
            val liveTideResult = LocalTideCalculationEngine.evaluateLiveTideWindow(nowMillis)
            val currentWaterLevel = liveTideResult.currentWaterLevelMeters
            val currentRecessionKm = liveTideResult.currentRecessionKm
            val isWindowOpenNow = liveTideResult.isSafeWalkingWindowOpen
            val secondsUntilNextSafeWindow = liveTideResult.secondsUntilNextSafeWindow
            val minutesUntilNextSafeWindow = (secondsUntilNextSafeWindow / 60L).toInt()

            Log.d(
                TAG,
                "Tidal Telemetry: WaterLevel=%.2fm, Recession=%.2fkm, IsOpenNow=%b, NextSafeWindowCountdown=%ds (%dm), NextWindowClock=%s, Range=%s".format(
                    currentWaterLevel,
                    currentRecessionKm,
                    isWindowOpenNow,
                    secondsUntilNextSafeWindow,
                    minutesUntilNextSafeWindow,
                    liveTideResult.nextSafeWindowClockLabel,
                    liveTideResult.safeWalkingWindowRange
                )
            )

            // 3. Sync & cross-verify with Room database
            var dbTideEntity: ChandipurTideEntity? = null
            try {
                val db = AppDatabase.getInstance(context)
                dbTideEntity = db.chandipurTideDao().getLatestTideForecastSync()

                // Update Room database with fresh evaluated telemetry
                val statusString = when {
                    isWindowOpenNow -> "SAFE_WALK"
                    minutesUntilNextSafeWindow in 1..40 -> "CAUTION"
                    else -> "UNSAFE_RETURN_SHORE"
                }

                val tideStateString = when {
                    currentWaterLevel <= 1.75 -> "LOW_TIDE"
                    currentWaterLevel < 2.5 -> "RECEDING"
                    currentWaterLevel < 3.2 -> "INCOMING"
                    else -> "HIGH_TIDE"
                }

                val safeWalkMinutes = if (isWindowOpenNow) {
                    ((liveTideResult.secondsUntilNextHighTide - 1800L).coerceAtLeast(0L) / 60L).toInt()
                } else {
                    0
                }

                val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(nowMillis))

                val updatedEntity = ChandipurTideEntity(
                    id = "chandipur_live_telemetry",
                    date = todayDateStr,
                    lowTideTime = liveTideResult.nextSafeWindowClockLabel,
                    highTideTime = liveTideResult.nextHighTideClockLabel,
                    nextHighTideTime = liveTideResult.nextHighTideClockLabel,
                    recededDistanceKm = currentRecessionKm,
                    currentWaterLevelMeters = currentWaterLevel,
                    tideState = tideStateString,
                    safeWalkStatus = statusString,
                    safeWalkMinutesRemaining = safeWalkMinutes,
                    lunarCondition = "Normal Cycle",
                    tidalForecastSummary = "Chandipur Vanishing Sea: ${liveTideResult.safeWalkingWindowRange}. Water level %.2fm, recession %.1f km.".format(
                        currentWaterLevel,
                        currentRecessionKm
                    ),
                    isOfflineCached = true,
                    lastFetchedTimestamp = nowMillis
                )
                db.chandipurTideDao().insertOrUpdateTide(updatedEntity)
                Log.d(TAG, "Synchronized Room DB ChandipurTideEntity with latest telemetry.")
            } catch (dbEx: Exception) {
                Log.w(TAG, "Room DB telemetry sync notice: ${dbEx.message}")
            }

            // 4. Update home screen widgets
            try {
                com.example.widget.ChandipurSafeWalkWidgetProvider.updateAllWidgets(context)
            } catch (wEx: Exception) {
                Log.w(TAG, "Widget update fallback: ${wEx.message}")
            }

            // 5. Evaluate the 30-minute advance trigger condition:
            // Trigger if:
            // a) Force test trigger requested from UI, OR
            // b) The safe walking window is not open yet, and will open in approximately 30 minutes
            //    (window countdown between 20 and 35 minutes)
            val is30MinApproaching = !isWindowOpenNow && minutesUntilNextSafeWindow in 20..35

            var notificationDispatched = false
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val windowKey = "WINDOW_${liveTideResult.nextSafeWindowClockLabel}"
            val lastNotifiedKey = prefs.getString(KEY_LAST_NOTIFIED_WINDOW_KEY, "")
            val lastNotifyTime = prefs.getLong(KEY_LAST_NOTIFY_TIMESTAMP, 0L)
            val cooldownMs = 60 * 60 * 1000L // 1 hour cooldown between alerts for the same window

            val shouldNotify = forceTrigger || (is30MinApproaching && (lastNotifiedKey != windowKey || (nowMillis - lastNotifyTime) > cooldownMs))

            if (shouldNotify) {
                Log.i(
                    TAG,
                    "Chandipur 30-Min Safe Walk Window triggered! Countdown: ${minutesUntilNextSafeWindow}m, Window: ${liveTideResult.nextSafeWindowClockLabel}"
                )

                dispatchSafeWalk30MinNotification(
                    context = context,
                    minutesUntilOpen = if (minutesUntilNextSafeWindow in 1..45) minutesUntilNextSafeWindow else 30,
                    recessionKm = currentRecessionKm,
                    nextSafeWindowClock = liveTideResult.nextSafeWindowClockLabel,
                    safeWindowRange = liveTideResult.safeWalkingWindowRange,
                    nextHighTideClock = liveTideResult.nextHighTideClockLabel
                )

                notificationDispatched = true

                // Record notification history to prevent duplicate alerts
                prefs.edit()
                    .putString(KEY_LAST_NOTIFIED_WINDOW_KEY, windowKey)
                    .putLong(KEY_LAST_NOTIFY_TIMESTAMP, nowMillis)
                    .putInt(KEY_LAST_NOTIFIED_MINUTES, minutesUntilNextSafeWindow)
                    .apply()
            } else {
                Log.d(
                    TAG,
                    "No 30-min alert required at this time (is30MinApproaching=$is30MinApproaching, minutesUntilOpen=$minutesUntilNextSafeWindow, alreadyNotified=${lastNotifiedKey == windowKey})"
                )
            }

            // 6. Schedule a targeted one-time worker for the exact window if known
            if (!isWindowOpenNow && minutesUntilNextSafeWindow > 35) {
                val windowOpenEpochMs = nowMillis + (secondsUntilNextSafeWindow * 1000L)
                scheduleTargeted30MinWindowWork(context, windowOpenEpochMs)
            }

            val outputData = workDataOf(
                KEY_NOTIFICATION_DISPATCHED to notificationDispatched,
                KEY_MINUTES_UNTIL_WINDOW to minutesUntilNextSafeWindow,
                KEY_WATER_LEVEL to currentWaterLevel,
                KEY_RECESSION_KM to currentRecessionKm,
                KEY_WINDOW_RANGE to liveTideResult.safeWalkingWindowRange,
                KEY_NEXT_HIGH_TIDE to liveTideResult.nextHighTideClockLabel,
                KEY_STATUS to "COMPLETED"
            )

            Result.success(outputData)
        } catch (e: Exception) {
            Log.e(TAG, "ChandipurTidalTelemetryWorker encountered an error", e)
            if (runAttemptCount < 2) {
                Result.retry()
            } else {
                Result.failure(
                    workDataOf(
                        KEY_STATUS to "ERROR",
                        KEY_MESSAGE to (e.message ?: "Unknown worker error")
                    )
                )
            }
        }
    }

    /**
     * Builds and dispatches the high-priority local push notification 30 minutes before
     * safe walking windows occur at Chandipur Beach.
     */
    private fun dispatchSafeWalk30MinNotification(
        context: Context,
        minutesUntilOpen: Int,
        recessionKm: Double,
        nextSafeWindowClock: String,
        safeWindowRange: String,
        nextHighTideClock: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        // 1. Ensure Notification Channel for Chandipur Safe Tide 30-Min Alerts uses customized alert sound
        val activeChannelId = ChandipurTideReminderPreferences.ensureConfiguredNotificationChannel(context)
        val reminderConfig = ChandipurTideReminderPreferences.loadConfig(context)
        val soundUri = ChandipurTideReminderPreferences.resolveAlertSoundUri(
            context = context,
            option = reminderConfig.selectedSound,
            customUriString = reminderConfig.customSoundUri
        )

        // 2. Check Android 13+ POST_NOTIFICATIONS runtime permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                Log.w(TAG, "Notification permission POST_NOTIFICATIONS not granted. Cannot display status bar alert.")
                return
            }
        }

        // 3. Navigation Intent when notification is tapped (opens Chandipur Tide sheet in App)
        val mainIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "WEATHER")
            putExtra("open_feature_sheet", "CHANDIPUR_TIDE")
            putExtra("safe_tide_walk_alert", true)
            putExtra("walk_window_alert", "OPENING_SOON")
        }
        val mainPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_SAFE_TIDE_30M,
            mainIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action Intent for Coast & Safety Radar
        val mapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "ESSENTIALS")
            putExtra("open_feature_sheet", "BALASORE_MAP_EXPLORER")
        }
        val mapPendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID_SAFE_TIDE_30M + 1,
            mapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Localized rich copy (English & Odia)
        val title = "🌊 Chandipur Safe Walking Window in ${minutesUntilOpen}m!"
        val bigText = buildString {
            append("Bay of Bengal waters have receded ")
            append(String.format(Locale.US, "%.1f", recessionKm))
            append(" km! Safe seabed walk opens at ")
            append(nextSafeWindowClock)
            append(" (Safe Window: ")
            append(safeWindowRange)
            append(").\n\n")
            append("Ideal time to explore the exposed mudflats, view rare living fossil horseshoe crabs & red ghost crab dunes. ")
            append("Remember to return toward the shore 30 mins before High Tide (")
            append(nextHighTideClock)
            append(").")
        }

        val builder = NotificationCompat.Builder(context, activeChannelId)
            .setSmallIcon(R.drawable.ic_notification_tide)
            .setContentTitle(title)
            .setContentText("Sea receded %.1f km! Safe seabed walking window opens at %s (%s).".format(
                recessionKm,
                nextSafeWindowClock,
                safeWindowRange
            ))
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(bigText)
                    .setBigContentTitle(title)
                    .setSummaryText("🦀 Chandipur Vanishing Sea • 30-Min Safe Walk Notice")
            )
            .setColor(Color.rgb(2, 132, 199)) // Ocean Blue
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(
                if (reminderConfig.selectedSound.usesAlarmUsage) {
                    NotificationCompat.CATEGORY_ALARM
                } else {
                    NotificationCompat.CATEGORY_EVENT
                }
            )
            .setAutoCancel(true)
            .setContentIntent(mainPendingIntent)
            .addAction(R.drawable.ic_notification_tide, "🌊 Open Tide Clock", mainPendingIntent)
            .addAction(R.drawable.ic_notification_weather, "🗺️ Coast & Map", mapPendingIntent)

        if (soundUri != null) {
            builder.setSound(soundUri)
        } else {
            builder.setSilent(true)
        }
        if (reminderConfig.vibrateEnabled) {
            builder.setVibrate(longArrayOf(0, 350, 150, 350, 150, 450))
        }

        val notification = builder.build()

        try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_SAFE_TIDE_30M, notification)
            Log.i(TAG, "Successfully dispatched Chandipur Safe Walk 30-Min notification to status bar!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post notification", e)
        }
    }

    companion object {
        const val TAG = "ChandipurTidalWorker"
        const val WORK_NAME_PERIODIC = "ChandipurTidalTelemetryPeriodicWork"
        const val WORK_NAME_ONE_TIME = "ChandipurTidalTelemetryImmediateWork"
        const val WORK_NAME_TARGETED = "ChandipurSafeWalkWindowTargetedWork"

        const val CHANNEL_ID_SAFE_TIDE_30M = "chandipur_safe_tide_30m_channel"
        const val CHANNEL_NAME_SAFE_TIDE_30M = "Chandipur Safe Tide 30-Min Alerts"
        const val NOTIFICATION_ID_SAFE_TIDE_30M = 8842

        private const val PREFS_NAME = "chandipur_telemetry_worker_prefs"
        private const val KEY_LAST_NOTIFIED_WINDOW_KEY = "key_last_notified_window_key"
        private const val KEY_LAST_NOTIFY_TIMESTAMP = "key_last_notify_timestamp"
        private const val KEY_LAST_NOTIFIED_MINUTES = "key_last_notified_minutes"

        const val KEY_FORCE_TRIGGER = "key_force_trigger"
        const val KEY_NOTIFICATION_DISPATCHED = "notification_dispatched"
        const val KEY_MINUTES_UNTIL_WINDOW = "minutes_until_window"
        const val KEY_WATER_LEVEL = "water_level"
        const val KEY_RECESSION_KM = "recession_km"
        const val KEY_WINDOW_RANGE = "window_range"
        const val KEY_NEXT_HIGH_TIDE = "next_high_tide"
        const val KEY_STATUS = "status"
        const val KEY_MESSAGE = "message"

        /**
         * Schedules periodic background monitoring of Chandipur tidal telemetry using WorkManager.
         * Runs every [intervalMinutes] (minimum 15 mins enforced by Android OS).
         */
        fun schedulePeriodic(context: Context, intervalMinutes: Long = 15) {
            val constraints = Constraints.Builder()
                .setRequiresBatteryNotLow(false)
                .build()

            val periodicRequest = PeriodicWorkRequestBuilder<ChandipurTidalTelemetryWorker>(
                intervalMinutes.coerceAtLeast(15),
                TimeUnit.MINUTES,
                5,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 5, TimeUnit.MINUTES)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
                WORK_NAME_PERIODIC,
                ExistingPeriodicWorkPolicy.KEEP,
                periodicRequest
            )
            Log.i(TAG, "WorkManager: Enqueued periodic Chandipur tidal telemetry monitoring (${intervalMinutes}m interval)")
        }

        /**
         * Triggers an immediate one-time WorkManager evaluation of tidal telemetry data.
         * If [forceNotification] is true, dispatches the 30-minute safe walk notification immediately.
         */
        fun enqueueImmediateCheck(context: Context, forceNotification: Boolean = false) {
            val inputData = workDataOf(KEY_FORCE_TRIGGER to forceNotification)

            val oneTimeRequest = OneTimeWorkRequestBuilder<ChandipurTidalTelemetryWorker>()
                .setInputData(inputData)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                WORK_NAME_ONE_TIME,
                ExistingWorkPolicy.REPLACE,
                oneTimeRequest
            )
            Log.i(TAG, "WorkManager: Enqueued immediate Chandipur tidal telemetry check (forceNotification=$forceNotification)")
        }

        /**
         * Schedules a targeted one-time WorkManager worker specifically timed to awaken
         * exactly 30 minutes before the upcoming safe walking window [windowOpenEpochMs].
         */
        fun scheduleTargeted30MinWindowWork(context: Context, windowOpenEpochMs: Long) {
            val now = System.currentTimeMillis()
            val thirtyMinutesBeforeEpoch = windowOpenEpochMs - (30 * 60 * 1000L)
            val delayMs = (thirtyMinutesBeforeEpoch - now).coerceAtLeast(0L)

            val inputData = workDataOf(KEY_FORCE_TRIGGER to true)

            val targetedRequest = OneTimeWorkRequestBuilder<ChandipurTidalTelemetryWorker>()
                .setInitialDelay(delayMs, TimeUnit.MILLISECONDS)
                .setInputData(inputData)
                .addTag(TAG)
                .build()

            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                WORK_NAME_TARGETED,
                ExistingWorkPolicy.REPLACE,
                targetedRequest
            )
            val delayMins = delayMs / (60 * 1000L)
            Log.i(TAG, "WorkManager: Scheduled targeted 30-min advance alert worker in ${delayMins}m (at ${Date(thirtyMinutesBeforeEpoch)})")
        }

        /**
         * Cancels all scheduled Chandipur tidal telemetry workers.
         */
        fun cancel(context: Context) {
            val workManager = WorkManager.getInstance(context.applicationContext)
            workManager.cancelUniqueWork(WORK_NAME_PERIODIC)
            workManager.cancelUniqueWork(WORK_NAME_ONE_TIME)
            workManager.cancelUniqueWork(WORK_NAME_TARGETED)
            Log.i(TAG, "WorkManager: Cancelled Chandipur tidal telemetry workers")
        }

        /**
         * Observes the WorkInfo status of the periodic tidal telemetry worker.
         */
        fun observeWorkInfo(context: Context): Flow<WorkInfo?> {
            return WorkManager.getInstance(context.applicationContext)
                .getWorkInfosForUniqueWorkFlow(WORK_NAME_PERIODIC)
                .map { list -> list.firstOrNull() }
        }
    }
}
