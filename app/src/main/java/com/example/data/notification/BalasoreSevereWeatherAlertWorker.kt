package com.example.data.notification

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.fcm.FcmManager
import com.example.data.fcm.FcmSevereWeatherHandler
import com.example.data.local.AppDatabase
import com.example.data.remote.OpenWeatherAlertDto
import com.example.data.remote.OpenWeatherCurrentResponse
import com.example.data.remote.OpenWeatherForecastItem
import com.example.data.repository.OpenWeatherRepository
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

/**
 * Detected severe weather condition for Balasore derived from OpenWeatherMap API payloads.
 */
data class SevereWeatherDetection(
    val alertLevel: String,
    val title: String,
    val message: String,
    val fingerprint: String
)

/**
 * Background WorkManager task that periodically queries the OpenWeatherMap API
 * (`/data/2.5/weather` & `/data/2.5/forecast`) for Balasore (21.4934° N, 86.9135° E),
 * evaluates severe weather hazards (cyclone/thunderstorm, heavy rain, squall/high winds,
 * extreme heatwave), updates Room weather cache, and dispatches a high-priority push notification.
 */
class BalasoreSevereWeatherAlertWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        if (!FcmManager.isWeatherAlertsEnabled(applicationContext)) {
            Log.d(TAG, "Weather alerts disabled by user preference; skipping OpenWeatherMap check.")
            return Result.success()
        }

        val fcmMessageId = inputData.getString(KEY_FCM_MESSAGE_ID)
        val forceNotify = inputData.getBoolean(KEY_FORCE_NOTIFY, false)

        // Ensure Firebase Cloud Messaging topic subscription for Balasore severe weather stays active
        FcmSevereWeatherHandler.ensureFcmTopicSubscription(applicationContext)

        return try {
            val repo = OpenWeatherRepository(applicationContext)
            val officialAlerts = repo.fetchOfficialWeatherAlerts().getOrNull().orEmpty()
            val currentWeather = repo.fetchCurrentWeather().getOrNull()
            val forecastItems = repo.fetchFiveDayForecast().getOrNull()
                ?.firstOrNull()
                ?.hourlySlots
                .orEmpty()

            if (currentWeather == null && officialAlerts.isEmpty()) {
                Log.w(TAG, "OpenWeatherMap weather & alerts unavailable; will retry later.")
                return Result.retry()
            }

            val severeWarning = evaluateSevereWeatherWarning(
                current = currentWeather,
                upcomingForecast = forecastItems,
                officialAlerts = officialAlerts
            )
            if (severeWarning != null) {
                if (currentWeather != null) {
                    updateLocalWeatherCache(currentWeather, severeWarning)
                }
                val matchingOfficialAlert = officialAlerts.firstOrNull {
                    FcmSevereWeatherHandler.isSevereAlert(it)
                }
                val validFromMs = matchingOfficialAlert?.start?.times(1000L)
                    ?.takeIf { it > 0L } ?: System.currentTimeMillis()
                val validUntilMs = matchingOfficialAlert?.end?.times(1000L)
                    ?.takeIf { it > validFromMs } ?: (validFromMs + 6L * 60L * 60L * 1000L)

                val dispatchResult = FcmSevereWeatherHandler.handleOpenWeatherSevereAlert(
                    context = applicationContext,
                    detection = severeWarning,
                    senderName = matchingOfficialAlert?.senderName,
                    fcmMessageId = fcmMessageId,
                    validFromMillis = validFromMs,
                    validUntilMillis = validUntilMs,
                    forceNotify = forceNotify
                )
                Log.i(
                    TAG,
                    "FCM Severe Weather Handler finished (${dispatchResult.reason}) for: ${severeWarning.title}"
                )
            } else {
                Log.d(TAG, "Balasore OpenWeatherMap check completed: no severe weather alerts active.")
            }

            Result.success()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Error running OpenWeatherMap severe weather check", e)
            if (runAttemptCount < 3) Result.retry() else Result.failure()
        }
    }

    private suspend fun updateLocalWeatherCache(
        current: OpenWeatherCurrentResponse,
        warning: SevereWeatherDetection
    ) {
        try {
            val db = AppDatabase.getInstance(applicationContext)
            val existing = db.weatherDao().getWeatherCacheSync()
            if (existing != null) {
                val windKmh = (current.wind?.speed ?: 0.0) * 3.6
                val gustsKmh = (current.wind?.gust ?: current.wind?.speed ?: 0.0) * 3.6
                val updated = existing.copy(
                    temperature = current.main?.temp ?: existing.temperature,
                    apparentTemperature = current.main?.feelsLike ?: existing.apparentTemperature,
                    humidity = current.main?.humidity ?: existing.humidity,
                    windSpeed = if (windKmh > 0.0) windKmh else existing.windSpeed,
                    windGusts = if (gustsKmh > 0.0) gustsKmh else existing.windGusts,
                    weatherDescription = current.weather.firstOrNull()?.description
                        ?.replaceFirstChar { it.uppercase(Locale.US) }
                        ?: existing.weatherDescription,
                    alertLevel = warning.alertLevel,
                    alertTitle = warning.title,
                    alertMessage = warning.message,
                    lastUpdated = System.currentTimeMillis()
                )
                db.weatherDao().insertOrUpdateWeather(updated)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update Room weather cache with severe alert: ${e.message}")
        }
    }

    companion object {
        private const val TAG = "BalasoreWeatherWorker"
        const val PERIODIC_WORK_NAME = "balasore_owm_severe_weather_periodic"
        const val IMMEDIATE_WORK_NAME = "balasore_owm_severe_weather_immediate"
        const val KEY_FCM_MESSAGE_ID = "fcm_message_id"
        const val KEY_FORCE_NOTIFY = "force_notify"

        /**
         * Pure evaluation engine that inspects OpenWeatherMap official alerts, current weather,
         * and upcoming 3-hour forecast slots for Balasore, returning a [SevereWeatherDetection]
         * when a 'Severe' or hazardous condition is present.
         */
        fun evaluateSevereWeatherWarning(
            current: OpenWeatherCurrentResponse?,
            upcomingForecast: List<OpenWeatherForecastItem> = emptyList(),
            officialAlerts: List<OpenWeatherAlertDto> = emptyList()
        ): SevereWeatherDetection? {
            // 0. Official OpenWeatherMap 'Severe' / extreme weather alerts (One Call alerts array)
            val severeOfficialAlert = officialAlerts.firstOrNull { alert ->
                FcmSevereWeatherHandler.isSevereAlert(alert)
            }
            if (severeOfficialAlert != null) {
                val eventName = severeOfficialAlert.event?.trim()?.takeIf { it.isNotBlank() }
                    ?: "Severe Weather Alert"
                val sender = severeOfficialAlert.senderName?.trim()?.takeIf { it.isNotBlank() }
                    ?: "OpenWeatherMap / IMD"
                val detail = severeOfficialAlert.description?.trim()?.takeIf { it.isNotBlank() }
                    ?: "OpenWeatherMap issued a $eventName for Balasore. Move indoors and follow local safety advisories."
                val eventKey = eventName.uppercase(Locale.US).replace(Regex("[^A-Z0-9]+"), "_").take(32)
                return SevereWeatherDetection(
                    alertLevel = "SEVERE",
                    title = "$eventName — Balasore",
                    message = "$detail ($sender)",
                    fingerprint = "OWM_ALERT_${eventKey}_${severeOfficialAlert.start ?: 0L}"
                )
            }

            if (current == null) return null

            val currentCondition = current.weather.firstOrNull()
            val weatherId = currentCondition?.id ?: 800
            val mainText = currentCondition?.main.orEmpty()
            val desc = currentCondition?.description
                ?.replaceFirstChar { it.uppercase(Locale.US) }
                ?: mainText.takeIf { it.isNotBlank() }
                ?: "Severe Weather"
            val tempC = current.main?.temp ?: 30.0
            val feelsLikeC = current.main?.feelsLike ?: tempC
            val windKmh = (current.wind?.speed ?: 0.0) * 3.6
            val gustKmh = (current.wind?.gust ?: 0.0) * 3.6
            val peakWindKmh = maxOf(windKmh, gustKmh)

            // 0B. Explicit 'Severe' keyword returned in OpenWeatherMap condition text
            if (mainText.contains("severe", ignoreCase = true) ||
                desc.contains("severe", ignoreCase = true)
            ) {
                return SevereWeatherDetection(
                    alertLevel = "SEVERE",
                    title = "Severe Weather Alert — Balasore",
                    message = "OpenWeatherMap reports $desc over Balasore (${tempC.roundToInt()}°C, winds ${peakWindKmh.roundToInt()} km/h). Take immediate precautions.",
                    fingerprint = "SEVERE_COND_$weatherId"
                )
            }

            // 1. Cyclone / Tornado / Severe Squall (OWM 771, 781) or Extreme Gale Winds >= 60 km/h
            if (weatherId == 781 || weatherId == 771 || peakWindKmh >= 60.0) {
                return SevereWeatherDetection(
                    alertLevel = "SEVERE",
                    title = "Severe Cyclone / Coastal Squall Warning — Balasore",
                    message = "$desc detected over Balasore (${peakWindKmh.roundToInt()} km/h winds). Move indoors, avoid Chandipur & Talasari shoreline, and follow OSDMA advisories.",
                    fingerprint = "CYCLONE_SQUALL_$weatherId"
                )
            }

            // 2. Active Thunderstorm (OWM 200..232)
            if (weatherId in 200..232) {
                return SevereWeatherDetection(
                    alertLevel = "URGENT",
                    title = "Thunderstorm & Lightning Warning — Balasore",
                    message = "$desc reported in Balasore with ${peakWindKmh.roundToInt()} km/h gusts. Seek shelter away from open fields, trees, and coastal beaches.",
                    fingerprint = "THUNDERSTORM_$weatherId"
                )
            }

            // 3. Heavy / Extreme Rainfall (OWM 502..504, 522)
            if (weatherId in 502..504 || weatherId == 522) {
                return SevereWeatherDetection(
                    alertLevel = "URGENT",
                    title = "Heavy Rainfall & Waterlogging Alert — Balasore",
                    message = "$desc active in Balasore (${tempC.roundToInt()}°C). Expect localized waterlogging near Cinema Chhak, OT Road, and Subarnarekha low-lying pockets.",
                    fingerprint = "HEAVY_RAIN_$weatherId"
                )
            }

            // 4. High Coastal Winds >= 45 km/h
            if (peakWindKmh >= 45.0) {
                return SevereWeatherDetection(
                    alertLevel = "WARNING",
                    title = "High Coastal Wind Warning — Balasore",
                    message = "Strong winds reaching ${peakWindKmh.roundToInt()} km/h near Balasore & Chandipur coast. Fishermen and commuters advised to exercise caution.",
                    fingerprint = "HIGH_WIND_${(peakWindKmh / 10).toInt()}"
                )
            }

            // 5. Severe Heatwave (>= 40°C ambient or >= 44°C heat index)
            if (tempC >= 40.0 || feelsLikeC >= 44.0) {
                return SevereWeatherDetection(
                    alertLevel = "URGENT",
                    title = "Extreme Heatwave Warning — Balasore",
                    message = "Temperature reached ${String.format(Locale.US, "%.1f", tempC)}°C (feels like ${String.format(Locale.US, "%.1f", feelsLikeC)}°C) in Balasore. Stay hydrated and avoid midday outdoor exposure.",
                    fingerprint = "HEATWAVE_${tempC.roundToInt()}"
                )
            }

            // 6. Inspect upcoming 3-hour forecast windows (next 12 hours) for approaching severe weather
            val severeUpcoming = upcomingForecast.take(4).firstOrNull { slot ->
                val slotId = slot.weather.firstOrNull()?.id ?: 800
                val slotWindKmh = maxOf(
                    (slot.wind?.speed ?: 0.0) * 3.6,
                    (slot.wind?.gust ?: 0.0) * 3.6
                )
                slotId in 200..232 || slotId in 502..504 || slotId == 771 || slotId == 781 || slotWindKmh >= 50.0
            }
            if (severeUpcoming != null) {
                val slotCond = severeUpcoming.weather.firstOrNull()?.description
                    ?.replaceFirstChar { it.uppercase(Locale.US) }
                    ?: "Severe Weather"
                val timeSlot = severeUpcoming.dtTxt?.substringAfter(" ")?.take(5) ?: "upcoming hours"
                return SevereWeatherDetection(
                    alertLevel = "WARNING",
                    title = "Incoming Severe Weather Advisory — Balasore",
                    message = "OpenWeatherMap forecasts $slotCond over Balasore around $timeSlot. Plan travel and Chandipur beach visits accordingly.",
                    fingerprint = "FORECAST_${severeUpcoming.weather.firstOrNull()?.id ?: 0}"
                )
            }

            return null
        }

        /**
         * Schedules periodic WorkManager checks against OpenWeatherMap for Balasore severe weather warnings.
         */
        fun schedulePeriodicWeatherAlertCheck(
            context: Context,
            repeatIntervalMinutes: Long = 60L
        ) {
            val safeInterval = repeatIntervalMinutes.coerceAtLeast(15L)
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val request = PeriodicWorkRequestBuilder<BalasoreSevereWeatherAlertWorker>(
                safeInterval,
                TimeUnit.MINUTES
            )
                .setConstraints(constraints)
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniquePeriodicWork(
                    PERIODIC_WORK_NAME,
                    ExistingPeriodicWorkPolicy.KEEP,
                    request
                )
        }

        /**
         * Triggers an immediate one-time OpenWeatherMap severe weather check for Balasore,
         * optionally carrying Firebase Cloud Messaging metadata when triggered from FCM.
         */
        fun triggerImmediateWeatherAlertCheck(
            context: Context,
            fcmMessageId: String? = null,
            forceNotify: Boolean = false
        ) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val input = workDataOf(
                KEY_FCM_MESSAGE_ID to fcmMessageId,
                KEY_FORCE_NOTIFY to forceNotify
            )

            val request = OneTimeWorkRequestBuilder<BalasoreSevereWeatherAlertWorker>()
                .setConstraints(constraints)
                .setInputData(input)
                .build()

            WorkManager.getInstance(context.applicationContext)
                .enqueueUniqueWork(
                    IMMEDIATE_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    request
                )
        }
    }
}
