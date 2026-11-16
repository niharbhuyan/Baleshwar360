package com.example.data.service

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.data.model.TrainStatusCategory
import com.example.data.notification.BalasoreTrainNotificationManager
import com.example.data.repository.BalasoreTrainRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Background WorkManager worker that inspects user-tracked trains passing or halting at Balasore (BLS),
 * evaluating real-time delay, approaching distance, and platform updates, then dispatching push notifications.
 */
class TrainAlertWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // Smart Sync check: disable non-critical train tracking if battery is below 20%
        if (com.example.data.sync.SyncManager.shouldThrottleNonCriticalJob(context, isCritical = false)) {
            val batteryPct = com.example.data.sync.SyncManager.getBatteryPercentage(context)
            Log.i(TAG, "TrainAlertWorker: Smart Sync mode active (battery $batteryPct% < 20%). Non-critical train tracking paused to prioritize foreground operations.")
            return@withContext Result.success()
        }

        try {
            val trackedTrainNumbers = BalasoreTrainNotificationManager.getTrackedTrainNumbers(context)
            if (trackedTrainNumbers.isEmpty()) {
                Log.d(TAG, "No trains tracked currently, worker exiting successfully")
                return@withContext Result.success()
            }

            val repository = BalasoreTrainRepository()
            val liveStatuses = repository.refreshLiveStatuses()

            val now = System.currentTimeMillis()
            for (number in trackedTrainNumbers) {
                val train = liveStatuses.find { it.trainNumber == number } ?: continue
                val preference = BalasoreTrainNotificationManager.getTrainPreference(context, number)

                // Minimum 15-minute throttle between duplicate alerts for the same train
                val throttleThresholdMs = 15 * 60 * 1000L
                val timeSinceLastAlert = now - preference.lastNotifiedTimestamp

                if (timeSinceLastAlert < throttleThresholdMs && preference.lastNotifiedStatus == train.statusCategory.name) {
                    continue
                }

                var alertType: String? = null
                var alertDetail = ""

                if (preference.notifyOnPlatformChange && train.isPlatformChanged) {
                    alertType = "PLATFORM_CHANGED"
                    alertDetail = "${train.trainNameEn} (Train #${train.trainNumber}) platform has been moved to ${train.platform} at Balasore (BLS)!"
                } else if (preference.notifyOnApproaching && train.statusCategory == TrainStatusCategory.APPROACHING) {
                    alertType = "APPROACHING"
                    alertDetail = "${train.trainNameEn} is now ${String.format("%.1f", train.distanceToBalasoreKm)} km from Balasore Junction. Scheduled at ${train.platform}."
                } else if (preference.notifyOnArrival && train.statusCategory == TrainStatusCategory.AT_STATION) {
                    alertType = "AT_STATION"
                    alertDetail = "${train.trainNameEn} has arrived at Balasore ${train.platform}. Scheduled halt: ${train.haltsBalasoreMins} minutes."
                } else if (preference.notifyOnDelay && train.delayMinutes >= 10 && train.statusCategory == TrainStatusCategory.DELAYED) {
                    alertType = "DELAYED"
                    alertDetail = "${train.trainNameEn} is currently running late by ${train.delayMinutes} minutes. Expected arrival: ${train.estimatedArrival}."
                }

                if (alertType != null) {
                    BalasoreTrainNotificationManager.sendTrainAlertNotification(
                        context = context,
                        train = train,
                        alertType = alertType,
                        customDetail = alertDetail
                    )
                    // Update state to prevent redundant duplicate alerts
                    BalasoreTrainNotificationManager.setTrackedTrain(
                        context = context,
                        trainNumber = number,
                        preference = preference.copy(
                            lastNotifiedStatus = train.statusCategory.name,
                            lastNotifiedTimestamp = now
                        )
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error in TrainAlertWorker", e)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "TrainAlertWorker"
    }
}
