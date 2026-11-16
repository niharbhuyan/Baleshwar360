package com.example.data.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.MainActivity
import com.example.R
import com.example.data.model.TrainNotificationPreference
import com.example.data.model.TrainRunningStatus
import com.example.data.service.TrainAlertWorker
import java.util.concurrent.TimeUnit

/**
 * Manages push notifications and background tracking for specific trains at Balasore Railway Station (BLS).
 */
object BalasoreTrainNotificationManager {

    private const val TAG = "TrainNotificationMgr"
    private const val PREFS_NAME = "balasore_train_tracking_prefs"

    const val CHANNEL_ID_TRAIN = "balasore_train_alerts_channel"
    const val CHANNEL_NAME_TRAIN = "Balasore Railway Live Train Alerts"

    // Base Notification ID range for trains
    private const val BASE_NOTIFICATION_ID = 9300

    private const val KEY_TRACKED_SET = "key_tracked_train_numbers"
    private const val PREFIX_TRAIN_PREF = "train_pref_"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Initializes the High-Priority Railway Notification Channel.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_COMMUNICATION_INSTANT)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID_TRAIN,
                CHANNEL_NAME_TRAIN,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Real-time alerts for approaching trains, platform assignments, and delay updates at Balasore Station."
                enableLights(true)
                lightColor = Color.rgb(2, 132, 199)
                enableVibration(true)
                vibrationPattern = longArrayOf(0, 250, 150, 250, 150, 400)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    /**
     * Check if a specific train number is currently being tracked.
     */
    fun isTrainTracked(context: Context, trainNumber: String): Boolean {
        val set = getPrefs(context).getStringSet(KEY_TRACKED_SET, emptySet()) ?: emptySet()
        return set.contains(trainNumber)
    }

    /**
     * Return all train numbers currently tracked by the user.
     */
    fun getTrackedTrainNumbers(context: Context): Set<String> {
        return getPrefs(context).getStringSet(KEY_TRACKED_SET, emptySet()) ?: emptySet()
    }

    /**
     * Save tracking preferences for a specific train and ensure background worker is enqueued.
     */
    fun setTrackedTrain(
        context: Context,
        trainNumber: String,
        preference: TrainNotificationPreference
    ) {
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_TRACKED_SET, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        currentSet.add(trainNumber)

        val serialized = "${preference.notifyOnApproaching}|${preference.notifyOnDelay}|${preference.notifyOnPlatformChange}|${preference.notifyOnArrival}|${preference.advanceMinutes}|${preference.lastNotifiedStatus}|${preference.lastNotifiedTimestamp}"

        prefs.edit()
            .putStringSet(KEY_TRACKED_SET, currentSet)
            .putString("$PREFIX_TRAIN_PREF$trainNumber", serialized)
            .apply()

        scheduleBackgroundWorker(context)
        Log.d(TAG, "Tracked train $trainNumber with preferences: $serialized")
    }

    /**
     * Remove tracking for a train.
     */
    fun untrackTrain(context: Context, trainNumber: String) {
        val prefs = getPrefs(context)
        val currentSet = prefs.getStringSet(KEY_TRACKED_SET, mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        currentSet.remove(trainNumber)

        prefs.edit()
            .putStringSet(KEY_TRACKED_SET, currentSet)
            .remove("$PREFIX_TRAIN_PREF$trainNumber")
            .apply()

        Log.d(TAG, "Untracked train $trainNumber")
    }

    /**
     * Retrieve preference configuration for a train.
     */
    fun getTrainPreference(context: Context, trainNumber: String): TrainNotificationPreference {
        val raw = getPrefs(context).getString("$PREFIX_TRAIN_PREF$trainNumber", null)
        if (raw != null) {
            val parts = raw.split("|")
            if (parts.size >= 5) {
                return TrainNotificationPreference(
                    trainNumber = trainNumber,
                    isTrackingEnabled = true,
                    notifyOnApproaching = parts[0].toBooleanStrictOrNull() ?: true,
                    notifyOnDelay = parts[1].toBooleanStrictOrNull() ?: true,
                    notifyOnPlatformChange = parts[2].toBooleanStrictOrNull() ?: true,
                    notifyOnArrival = parts[3].toBooleanStrictOrNull() ?: true,
                    advanceMinutes = parts[4].toIntOrNull() ?: 15,
                    lastNotifiedStatus = if (parts.size > 5) parts[5] else "",
                    lastNotifiedTimestamp = if (parts.size > 6) parts[6].toLongOrNull() ?: 0L else 0L
                )
            }
        }
        return TrainNotificationPreference(trainNumber = trainNumber)
    }

    /**
     * Sends an immediate test push notification for a train to verify that device notifications are operating properly.
     */
    fun sendImmediateTestPushNotification(context: Context, train: TrainRunningStatus) {
        createNotificationChannel(context)
        if (!hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission missing when attempting test push")
            return
        }

        val title = "🚆 Train Alert: ${train.trainNumber} ${train.trainNameEn}"
        val delayNote = if (train.delayMinutes > 0) "⚠️ Running late by ${train.delayMinutes}m" else "🟢 Running on time"
        val message = "Tracking Active at Balasore Junction (BLS)!\n" +
                "• Status: ${train.statusCategory.labelEn}\n" +
                "• Platform: ${train.platform}\n" +
                "• $delayNote\n" +
                "• Current: ${train.currentLocation}\n" +
                "• Scheduled Arrival: ${train.scheduledArrival}"

        dispatchNotification(
            context = context,
            train = train,
            title = title,
            message = message,
            summary = "Balasore (BLS) • Platform ${train.platform}"
        )
    }

    /**
     * Dispatches real-time train alerts (Approaching, Platform Changed, Delay Alert).
     */
    fun sendTrainAlertNotification(
        context: Context,
        train: TrainRunningStatus,
        alertType: String,
        customDetail: String = ""
    ) {
        createNotificationChannel(context)
        if (!hasNotificationPermission(context)) return

        val title = when (alertType) {
            "APPROACHING" -> "⚡ ${train.trainNumber} Approaching Balasore (${train.distanceToBalasoreKm} km away)"
            "PLATFORM_CHANGED" -> "🚨 Platform Change: ${train.trainNumber} shifted to ${train.platform}!"
            "DELAYED" -> "⚠️ Delay Alert: ${train.trainNumber} running ${train.delayMinutes}m late"
            "AT_STATION" -> "🚉 Arrived at Balasore ${train.platform}: ${train.trainNumber}"
            else -> "🚆 Balasore Train Update: ${train.trainNumber}"
        }

        val message = if (customDetail.isNotBlank()) {
            customDetail
        } else {
            "${train.trainNameEn}\n" +
                    "• Platform: ${train.platform}\n" +
                    "• Expected Arrival: ${train.estimatedArrival} (Sched: ${train.scheduledArrival})\n" +
                    "• Current Speed: ${train.speedKmh} km/h • Next: ${train.nextStationName}"
        }

        dispatchNotification(
            context = context,
            train = train,
            title = title,
            message = message,
            summary = "BLS Live Alert • ${train.platform}"
        )
    }

    private fun dispatchNotification(
        context: Context,
        train: TrainRunningStatus,
        title: String,
        message: String,
        summary: String
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("target_tab", "ESSENTIALS")
            putExtra("open_feature_sheet", "BALASORE_LIVE_TRAIN_STATUS")
            putExtra("highlight_train_number", train.trainNumber)
        }

        val notificationId = BASE_NOTIFICATION_ID + (train.trainNumber.hashCode() % 500).let { if (it < 0) -it else it }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_TRAIN)
            .setSmallIcon(R.drawable.ic_notification_train)
            .setContentTitle(title)
            .setContentText("${train.trainNameEn} • ${train.platform}")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
                    .setBigContentTitle(title)
                    .setSummaryText(summary)
            )
            .setColor(if (train.delayMinutes > 5) Color.rgb(234, 88, 12) else Color.rgb(2, 132, 199))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_TRANSPORT)
            .setSound(soundUri)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_notification_train, "View Live Status", pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.d(TAG, "Dispatched notification for train ${train.trainNumber}: $title")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to dispatch train notification", e)
        }
    }

    /**
     * Enqueues periodic background checking for tracked trains via WorkManager.
     */
    fun scheduleBackgroundWorker(context: Context) {
        try {
            val constraintsBuilder = Constraints.Builder()
            if (com.example.data.sync.SyncManager.isSmartSyncEnabled(context)) {
                constraintsBuilder.setRequiresBatteryNotLow(true)
            }
            val constraints = constraintsBuilder.build()
            val request = PeriodicWorkRequestBuilder<TrainAlertWorker>(
                15, TimeUnit.MINUTES,
                5, TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "balasore_train_tracking_worker",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
            Log.d(TAG, "TrainAlertWorker scheduled")
        } catch (e: Exception) {
            Log.e(TAG, "Could not enqueue TrainAlertWorker", e)
        }
    }
}
