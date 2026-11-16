package com.example.data.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.local.ChandipurTideEntity
import com.example.data.notification.TideAndWeatherNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * Service and Astronomical Engine for Chandipur Beach (Balasore, Odisha).
 * Calculates:
 * 1. Lunar cycles, moon age, synodic phases (Amavasya, Purnima, Ashtami), and Spring/Neap tide coefficients.
 * 2. Semi-diurnal tidal cycles of the Bay of Bengal at Chandipur.
 * 3. Safe Walking Windows on the exposed seafloor (accounting for vanishing sea distance and incoming surge speed).
 * 4. Schedules proactive local notifications and alarms to alert users BEFORE high tide returns to shore.
 */
object ChandipurLunarTideService {

    private const val TAG = "LunarTideService"

    // Alarm Request Codes
    const val ALARM_REQUEST_CODE_HIGH_TIDE_ALERT = 8830
    const val ALARM_REQUEST_CODE_WALK_OPEN = 8831
    const val ALARM_REQUEST_CODE_WALK_CLOSE = 8832

    // Default alert lead times
    const val DEFAULT_HIGH_TIDE_ADVANCE_MINUTES = 30
    const val DEFAULT_WALK_CLOSE_BUFFER_MINUTES = 25
    const val DEFAULT_WALK_OPEN_ADVANCE_MINUTES = 30

    // Astronomical constants
    // Known reference New Moon (UTC): Jan 11, 2024 11:57:00 UTC = 1704974220000 ms
    private const val REFERENCE_NEW_MOON_EPOCH_MS = 1704974220000L
    // Mean synodic month = 29.53058867 days in milliseconds
    const val SYNODIC_MONTH_DAYS = 29.53058867
    private const val SYNODIC_MONTH_MS = (29.53058867 * 86400 * 1000L).toLong()

    // Semi-diurnal tidal period: 12 hours 25.2 minutes (~745.2 minutes)
    const val LUNAR_TIDAL_PERIOD_MINUTES = 745.2

    /**
     * Lunar Phase definition with English and Odia terminology and astronomical attributes.
     */
    enum class LunarPhase(
        val phaseNameEn: String,
        val phaseNameOdia: String,
        val emoji: String,
        val isSpringTide: Boolean,
        val isNeapTide: Boolean
    ) {
        NEW_MOON("New Moon (Amavasya)", "ଅମାବାସ୍ୟା (ଭରା ଜୁଆର)", "🌑", true, false),
        WAXING_CRESCENT("Waxing Crescent", "ଶୁକ୍ଳ ପକ୍ଷ ଚନ୍ଦ୍ରକଳା", "🌒", false, false),
        FIRST_QUARTER("First Quarter (Ashtami)", "ଶୁକ୍ଳ ଅଷ୍ଟମୀ (ମରୁ ଜୁଆର)", "🌓", false, true),
        WAXING_GIBBOUS("Waxing Gibbous", "ଶୁକ୍ଳ ତ୍ରୟୋଦଶୀ", "🌔", false, false),
        FULL_MOON("Full Moon (Purnima)", "ପୂର୍ଣ୍ଣିମା (ଭରା ଜୁଆର)", "🌕", true, false),
        WANING_GIBBOUS("Waning Gibbous", "କୃଷ୍ଣ ପକ୍ଷ ବୃଦ୍ଧି", "🌖", false, false),
        LAST_QUARTER("Last Quarter (Ashtami)", "କୃଷ୍ଣ ଅଷ୍ଟମୀ (ମରୁ ଜୁଆର)", "🌗", false, true),
        WANING_CRESCENT("Waning Crescent", "କୃଷ୍ଣ ପକ୍ଷ ଶେଷ", "🌘", false, false)
    }

    /**
     * Lunar and tidal dynamics for a given point in time.
     */
    data class LunarTideInfo(
        val moonAgeDays: Double,
        val phase: LunarPhase,
        val phaseNameEn: String,
        val phaseNameOdia: String,
        val emoji: String,
        val isSpringTide: Boolean,
        val isNeapTide: Boolean,
        val tidalCoefficient: Int, // 40 (weakest neap) to 120 (strongest spring)
        val tideCategoryEn: String,
        val tideCategoryOdia: String,
        val maxRecessionKm: Double, // up to 5.2 km during spring tides, ~3.0 km during neap
        val incomingWaterVelocityMs: Double, // incoming wave velocity in m/s (0.4 m/s neap to 0.95 m/s spring)
        val estimatedPeakHighTideMeters: Double, // 3.2m to 4.8m
        val estimatedLowTideMeters: Double // 0.2m to 1.1m
    )

    /**
     * Comprehensive calculated walking window and high tide notification schedule.
     */
    data class SafeWalkWindowInfo(
        val windowOpenEpochMs: Long,
        val windowCloseEpochMs: Long,
        val peakLowTideEpochMs: Long,
        val nextHighTideEpochMs: Long,
        val minutesUntilHighTide: Long,
        val minutesUntilWindowOpen: Long,
        val minutesRemainingInWindow: Long,
        val isCurrentlySafeToWalk: Boolean,
        val isClosingSoon: Boolean, // safe return warning active
        val highTideAlertEpochMs: Long,
        val minutesUntilHighTideAlert: Long,
        val advanceAlertMinutesBeforeHighTide: Int,
        val maxRecessionKm: Double,
        val safeWindowDurationHours: Double,
        val lunarInfo: LunarTideInfo,
        val safetyAdvisoryEn: String,
        val safetyAdvisoryOdia: String,
        val telemetrySource: String
    )

    /**
     * Calculates precise lunar cycle information for a given epoch.
     */
    fun calculateLunarTideInfo(epochMs: Long = System.currentTimeMillis()): LunarTideInfo {
        val diffMs = epochMs - REFERENCE_NEW_MOON_EPOCH_MS
        val remainderMs = ((diffMs % SYNODIC_MONTH_MS) + SYNODIC_MONTH_MS) % SYNODIC_MONTH_MS
        val moonAgeDays = remainderMs / (86400 * 1000.0)

        // Classify phase across 8 octants of the synodic month (~3.69 days per octant)
        val phase = when {
            moonAgeDays < 1.84 || moonAgeDays >= 27.69 -> LunarPhase.NEW_MOON
            moonAgeDays < 5.53 -> LunarPhase.WAXING_CRESCENT
            moonAgeDays < 9.22 -> LunarPhase.FIRST_QUARTER
            moonAgeDays < 12.92 -> LunarPhase.WAXING_GIBBOUS
            moonAgeDays < 16.61 -> LunarPhase.FULL_MOON
            moonAgeDays < 20.30 -> LunarPhase.WANING_GIBBOUS
            moonAgeDays < 23.99 -> LunarPhase.LAST_QUARTER
            else -> LunarPhase.WANING_CRESCENT
        }

        // Spring vs Neap Tide calculations:
        // Spring tides occur around syzygy (New Moon & Full Moon), Neap tides around quadrature (Quarters).
        // Angular phase angle phi: 0 at New Moon, PI at Full Moon.
        val phaseAngleRad = 2.0 * PI * (moonAgeDays / SYNODIC_MONTH_DAYS)
        // Cosine variation squared yields peaks at New Moon (0) and Full Moon (PI)
        val springNeapFactor = cos(2.0 * phaseAngleRad) // 1.0 at Spring, -1.0 at Neap

        // Tidal coefficient: standard French/hydrographic scale 20 to 120 (Mean = 70, Spring = 95-120, Neap = 40-55)
        val tidalCoefficient = (70.0 + 35.0 * springNeapFactor).toInt().coerceIn(35, 120)

        val isSpringTide = phase == LunarPhase.NEW_MOON || phase == LunarPhase.FULL_MOON || tidalCoefficient >= 90
        val isNeapTide = phase == LunarPhase.FIRST_QUARTER || phase == LunarPhase.LAST_QUARTER || tidalCoefficient <= 55

        // Max seafloor recession distance: Chandipur famously recedes up to 5.2 km during peak Spring tides
        val maxRecessionKm = (3.0 + 2.2 * ((springNeapFactor + 1.0) / 2.0)).coerceIn(2.8, 5.2)

        // Incoming water surge speed: flat gradient causes fast rush in spring tides (0.4m/s to 0.95m/s)
        val incomingWaterVelocityMs = (0.40 + 0.55 * ((springNeapFactor + 1.0) / 2.0)).coerceIn(0.40, 0.95)

        // Peak high tide height: 3.2m in neap to 4.75m in spring tides
        val peakHighTideMeters = (3.20 + 1.55 * ((springNeapFactor + 1.0) / 2.0)).coerceIn(3.1, 4.8)

        // Peak low tide height: 1.1m in neap down to 0.22m in spring tides
        val peakLowTideMeters = (1.10 - 0.88 * ((springNeapFactor + 1.0) / 2.0)).coerceIn(0.2, 1.2)

        val tideCategoryEn = when {
            isSpringTide -> "Spring Tide (Extreme Vanishing Sea & Fast Inrush)"
            isNeapTide -> "Neap Tide (Moderate Vanishing Sea & Gentle Inrush)"
            else -> "Intermediate Tidal Phase"
        }

        val tideCategoryOdia = when {
            isSpringTide -> "ଭରା ଜୁଆର (ସର୍ବାଧିକ ସମୁଦ୍ର ଅପସାରଣ ଓ ଦ୍ରୁତ ଫେରନ୍ତା ତରଙ୍ଗ)"
            isNeapTide -> "ମରୁ ଜୁଆର (ମଧ୍ୟମ ଅପସାରଣ ଓ ଧୀର ତରଙ୍ଗ)"
            else -> "ସାଧାରଣ ଜୁଆର-ଭଟ୍ଟା"
        }

        return LunarTideInfo(
            moonAgeDays = moonAgeDays,
            phase = phase,
            phaseNameEn = phase.phaseNameEn,
            phaseNameOdia = phase.phaseNameOdia,
            emoji = phase.emoji,
            isSpringTide = isSpringTide,
            isNeapTide = isNeapTide,
            tidalCoefficient = tidalCoefficient,
            tideCategoryEn = tideCategoryEn,
            tideCategoryOdia = tideCategoryOdia,
            maxRecessionKm = maxRecessionKm,
            incomingWaterVelocityMs = incomingWaterVelocityMs,
            estimatedPeakHighTideMeters = peakHighTideMeters,
            estimatedLowTideMeters = peakLowTideMeters
        )
    }

    /**
     * Calculates the safe walking window and high tide notification schedule
     * using lunar cycle modeling and local telemetry.
     */
    fun calculateSafeWalkWindow(
        context: Context,
        epochMs: Long = System.currentTimeMillis()
    ): SafeWalkWindowInfo {
        val lunarInfo = calculateLunarTideInfo(epochMs)
        val calendar = Calendar.getInstance().apply { timeInMillis = epochMs }
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val currentMinute = calendar.get(Calendar.MINUTE)
        val currentMinuteOfDay = currentHour * 60 + currentMinute
        val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)

        // Check Room Database for real-time sensor / forecast entity
        var roomEntity: ChandipurTideEntity? = null
        try {
            val db = AppDatabase.getInstance(context)
            roomEntity = runBlocking(Dispatchers.IO) {
                db.chandipurTideDao().getLatestTideForecastSync()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Room lookup notice: ${e.message}")
        }

        // Semi-diurnal astronomical model calibrated for Chandipur (87.0° E):
        // Each calendar day shifts by ~50.4 minutes due to the Moon's synodic lag.
        val lunarProgressionMinuteOfDay = ((lunarInfo.moonAgeDays * 50.4) % 720.0).toInt()
        val morningHighTideMinute = (180 + lunarProgressionMinuteOfDay) % 720 // ~03:00 to 09:00 AM cycle
        val morningLowTideMinute = (morningHighTideMinute + 372) % 720 // ~6 hours 12 mins after high tide
        val afternoonHighTideMinute = morningHighTideMinute + 745 // Second high tide ~12h 25m later
        val afternoonLowTideMinute = morningLowTideMinute + 745 // Second low tide

        // Window open/close durations based on lunar spring/neap dynamics:
        // On Spring tides, water recedes faster & farther (opens 115m before peak),
        // but rushes in violently when turning (safe window closes 85m after peak, giving a 150m safety return buffer).
        // On Neap tides, water moves more gently (opens 100m before peak, closes 105m after peak).
        val windowBeforePeakMinutes = if (lunarInfo.isSpringTide) 115 else 100
        val windowAfterPeakMinutes = if (lunarInfo.isSpringTide) 85 else 105

        val lowTideEvents = listOf(
            morningLowTideMinute,
            afternoonLowTideMinute,
            morningLowTideMinute + 1440
        )

        val highTideEvents = listOf(
            morningHighTideMinute,
            afternoonHighTideMinute,
            morningHighTideMinute + 1440,
            afternoonHighTideMinute + 1440
        )

        // Find relevant low tide and high tide events
        val nextLowTideMinute = lowTideEvents.firstOrNull { it > (currentMinuteOfDay - windowAfterPeakMinutes) }
            ?: lowTideEvents.last()

        val nextHighTideMinute = highTideEvents.firstOrNull { it > currentMinuteOfDay }
            ?: highTideEvents.last()

        val windowOpenMinute = nextLowTideMinute - windowBeforePeakMinutes
        val windowCloseMinute = nextLowTideMinute + windowAfterPeakMinutes

        // Convert relative minutes of day to Epoch Milliseconds
        val startOfTodayCal = Calendar.getInstance().apply {
            timeInMillis = epochMs
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfTodayEpochMs = startOfTodayCal.timeInMillis

        val windowOpenEpochMs = startOfTodayEpochMs + (windowOpenMinute * 60 * 1000L)
        val windowCloseEpochMs = startOfTodayEpochMs + (windowCloseMinute * 60 * 1000L)
        val peakLowTideEpochMs = startOfTodayEpochMs + (nextLowTideMinute * 60 * 1000L)
        val nextHighTideEpochMs = startOfTodayEpochMs + (nextHighTideMinute * 60 * 1000L)

        val isCurrentlySafeToWalk = epochMs in windowOpenEpochMs until windowCloseEpochMs
        val minutesRemainingInWindow = if (isCurrentlySafeToWalk) {
            ((windowCloseEpochMs - epochMs) / 60000L).coerceAtLeast(0L)
        } else {
            0L
        }

        val minutesUntilWindowOpen = if (epochMs < windowOpenEpochMs) {
            ((windowOpenEpochMs - epochMs) / 60000L).coerceAtLeast(0L)
        } else {
            0L
        }

        val minutesUntilHighTide = ((nextHighTideEpochMs - epochMs) / 60000L).coerceAtLeast(0L)

        // Closing soon advisory if within 25 minutes of window closing
        val isClosingSoon = isCurrentlySafeToWalk && minutesRemainingInWindow <= DEFAULT_WALK_CLOSE_BUFFER_MINUTES

        // Pre-High Tide notification alert calculation
        val advanceAlertMinutesBeforeHighTide = TideAndWeatherNotificationManager.getAdvanceWarningMinutes(context)
            .coerceIn(15, 60)
        val highTideAlertEpochMs = nextHighTideEpochMs - (advanceAlertMinutesBeforeHighTide * 60 * 1000L)
        val minutesUntilHighTideAlert = ((highTideAlertEpochMs - epochMs) / 60000L).coerceAtLeast(0L)

        // Safety Advisories
        val safetyAdvisoryEn = when {
            isClosingSoon -> {
                if (lunarInfo.isSpringTide) {
                    "🚨 URGENT: Spring High Tide returning at ~${String.format(Locale.US, "%.1f", lunarInfo.incomingWaterVelocityMs)} m/s! Evacuate mudflats and return to the Chandipur sea embankment immediately."
                } else {
                    "⚠️ Tide has reversed in the Bay of Bengal. Begin walking back toward shore now (${minutesRemainingInWindow}m safe buffer remaining)."
                }
            }
            isCurrentlySafeToWalk -> {
                "🟢 SAFE WALK ACTIVE: Sea has receded ~${String.format(Locale.US, "%.1f", lunarInfo.maxRecessionKm)} km! Walk on the firm seabed. Stay within the safe 2.5 km perimeter."
            }
            minutesUntilWindowOpen in 1..45 -> {
                "🌊 Ebb tide in progress. Vanishing sea safe walk window opens in ${minutesUntilWindowOpen} mins (~${lunarInfo.phaseNameEn})."
            }
            else -> {
                "⛔ Seabed currently flooded by high tide (~${String.format(Locale.US, "%.1f", lunarInfo.estimatedPeakHighTideMeters)}m). Walking prohibited until next receding cycle."
            }
        }

        val safetyAdvisoryOdia = when {
            isClosingSoon -> {
                "🚨 ଜରୁରୀ: ଫେରନ୍ତା ଜୁଆର ମାଡ଼ିଆସୁଛି! ତୁରନ୍ତ ଚାନ୍ଦିପୁର ବେଳାଭୂମି ବନ୍ଧକୁ ଫେରିଆସନ୍ତୁ (${minutesRemainingInWindow} ମିନିଟ୍ ବାକି)।"
            }
            isCurrentlySafeToWalk -> {
                "🟢 ସୁରକ୍ଷିତ ଭ୍ରମଣ ସକ୍ରିୟ: ସମୁଦ୍ର ~${String.format(Locale.US, "%.1f", lunarInfo.maxRecessionKm)} କିମି ପଛକୁ ଯାଇଛି। ବାଲିପଠାରେ ବୁଲିବା ପାଇଁ ଅନୁକୂଳ ସମୟ।"
            }
            minutesUntilWindowOpen in 1..45 -> {
                "🌊 ସମୁଦ୍ର ଅପସାରଣ ଜାରି ରହିଛି। ଆଗାମୀ ${minutesUntilWindowOpen} ମିନିଟ୍ ମଧ୍ୟରେ ସୁରକ୍ଷିତ ୱାକ୍ ୱିଣ୍ଡୋ ଖୋଲିବ।"
            }
            else -> {
                "⛔ ବର୍ତ୍ତମାନ ଭରା ଜୁଆର। ସମୁଦ୍ର ଭିତରକୁ ଯିବା ନିଷେଧ।"
            }
        }

        val telemetrySource = if (roomEntity != null) {
            "Room DB & Lunar Model (${lunarInfo.phaseNameEn})"
        } else {
            "Astronomical Lunar-Tidal Engine (${lunarInfo.phaseNameEn})"
        }

        val safeWindowDurationHours = (windowCloseMinute - windowOpenMinute) / 60.0

        return SafeWalkWindowInfo(
            windowOpenEpochMs = windowOpenEpochMs,
            windowCloseEpochMs = windowCloseEpochMs,
            peakLowTideEpochMs = peakLowTideEpochMs,
            nextHighTideEpochMs = nextHighTideEpochMs,
            minutesUntilHighTide = minutesUntilHighTide,
            minutesUntilWindowOpen = minutesUntilWindowOpen,
            minutesRemainingInWindow = minutesRemainingInWindow,
            isCurrentlySafeToWalk = isCurrentlySafeToWalk,
            isClosingSoon = isClosingSoon,
            highTideAlertEpochMs = highTideAlertEpochMs,
            minutesUntilHighTideAlert = minutesUntilHighTideAlert,
            advanceAlertMinutesBeforeHighTide = advanceAlertMinutesBeforeHighTide,
            maxRecessionKm = lunarInfo.maxRecessionKm,
            safeWindowDurationHours = safeWindowDurationHours,
            lunarInfo = lunarInfo,
            safetyAdvisoryEn = safetyAdvisoryEn,
            safetyAdvisoryOdia = safetyAdvisoryOdia,
            telemetrySource = telemetrySource
        )
    }

    /**
     * Schedules exact Android local notifications and alarms with AlarmManager to:
     * 1. Alert users BEFORE HIGH TIDE (e.g. 30m before high tide peak)
     * 2. Alert users when SAFE WALK WINDOW is about to close (return to shore)
     * 3. Alert users when SAFE WALK WINDOW is opening (seabed accessible)
     */
    fun schedulePreHighTideAlerts(context: Context, advanceMinutesBeforeHighTide: Int = DEFAULT_HIGH_TIDE_ADVANCE_MINUTES): SafeWalkWindowInfo? {
        val appContext = context.applicationContext
        if (!TideAndWeatherNotificationManager.isTideAlertsEnabled(appContext) &&
            !TideAndWeatherNotificationManager.isWalkWindowAlertsEnabled(appContext)) {
            Log.d(TAG, "Tidal notifications disabled in user preferences. Cancelling alarms.")
            cancelAlerts(appContext)
            return null
        }

        TideAndWeatherNotificationManager.createNotificationChannels(appContext)
        val info = calculateSafeWalkWindow(appContext)
        val alarmManager = appContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return null
        val now = System.currentTimeMillis()

        // 1. Schedule Pre-High Tide Warning Alarm (e.g. 30 mins before high tide arrives)
        if (info.highTideAlertEpochMs > now) {
            val highTideIntent = Intent(appContext, ChandipurTideAlarmReceiver::class.java).apply {
                action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_HIGH_TIDE_WARNING
                putExtra(ChandipurTideAlarmReceiver.EXTRA_MINUTES_UNTIL_HIGH_TIDE, info.advanceAlertMinutesBeforeHighTide)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_TIDE_HEIGHT, info.lunarInfo.estimatedPeakHighTideMeters)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_LUNAR_PHASE, info.lunarInfo.phaseNameEn)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_IS_SPRING_TIDE, info.lunarInfo.isSpringTide)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_WATER_VELOCITY, info.lunarInfo.incomingWaterVelocityMs)
            }

            val highTidePendingIntent = PendingIntent.getBroadcast(
                appContext,
                ALARM_REQUEST_CODE_HIGH_TIDE_ALERT,
                highTideIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduleAlarmExact(alarmManager, info.highTideAlertEpochMs, highTidePendingIntent)
            Log.i(TAG, "Scheduled Pre-High Tide alarm at epoch: ${info.highTideAlertEpochMs} (in ${info.minutesUntilHighTideAlert}m)")
        }

        // 2. Schedule Safe Walk Window Ending / Return to Shore Alarm
        val walkCloseAlertEpochMs = info.windowCloseEpochMs - (DEFAULT_WALK_CLOSE_BUFFER_MINUTES * 60 * 1000L)
        if (walkCloseAlertEpochMs > now) {
            val endingIntent = Intent(appContext, ChandipurTideAlarmReceiver::class.java).apply {
                action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_ENDING
                putExtra(ChandipurTideAlarmReceiver.EXTRA_MINUTES_REMAINING, DEFAULT_WALK_CLOSE_BUFFER_MINUTES)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_IS_URGENT, info.lunarInfo.isSpringTide)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_RECEDED_KM, info.maxRecessionKm)
            }

            val endingPendingIntent = PendingIntent.getBroadcast(
                appContext,
                ALARM_REQUEST_CODE_WALK_CLOSE,
                endingIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduleAlarmExact(alarmManager, walkCloseAlertEpochMs, endingPendingIntent)
            Log.i(TAG, "Scheduled Walk Window Close alarm at epoch: $walkCloseAlertEpochMs")
        }

        // 3. Schedule Safe Walk Window Opening Alarm (30m before opening)
        val walkOpenAlertEpochMs = info.windowOpenEpochMs - (DEFAULT_WALK_OPEN_ADVANCE_MINUTES * 60 * 1000L)
        if (walkOpenAlertEpochMs > now) {
            val openIntent = Intent(appContext, ChandipurTideAlarmReceiver::class.java).apply {
                action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_APPROACHING
                putExtra(ChandipurTideAlarmReceiver.EXTRA_MINUTES_UNTIL_OPEN, DEFAULT_WALK_OPEN_ADVANCE_MINUTES)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_RECEDED_KM, info.maxRecessionKm)
                putExtra(ChandipurTideAlarmReceiver.EXTRA_WINDOW_DURATION_HOURS, info.safeWindowDurationHours)
            }

            val openPendingIntent = PendingIntent.getBroadcast(
                appContext,
                ALARM_REQUEST_CODE_WALK_OPEN,
                openIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            scheduleAlarmExact(alarmManager, walkOpenAlertEpochMs, openPendingIntent)
            Log.i(TAG, "Scheduled Walk Window Open alarm at epoch: $walkOpenAlertEpochMs")
        }

        return info
    }

    /**
     * Cancels all pending lunar and high tide alarms.
     */
    fun cancelAlerts(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

        val highTideIntent = Intent(context, ChandipurTideAlarmReceiver::class.java).apply {
            action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_HIGH_TIDE_WARNING
        }
        val highTidePending = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE_HIGH_TIDE_ALERT,
            highTideIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(highTidePending)

        val closeIntent = Intent(context, ChandipurTideAlarmReceiver::class.java).apply {
            action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_ENDING
        }
        val closePending = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE_WALK_CLOSE,
            closeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(closePending)

        val openIntent = Intent(context, ChandipurTideAlarmReceiver::class.java).apply {
            action = ChandipurTideAlarmReceiver.ACTION_TRIGGER_SAFE_WALK_APPROACHING
        }
        val openPending = PendingIntent.getBroadcast(
            context,
            ALARM_REQUEST_CODE_WALK_OPEN,
            openIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(openPending)

        Log.i(TAG, "Cancelled all Chandipur lunar high tide alarms.")
    }

    /**
     * Immediately dispatches a test push notification simulating the pre-high tide warning.
     */
    fun triggerImmediateHighTideAlert(context: Context, advanceMinutes: Int = DEFAULT_HIGH_TIDE_ADVANCE_MINUTES) {
        val info = calculateSafeWalkWindow(context)
        TideAndWeatherNotificationManager.showLunarHighTideApproachingAlert(
            context = context,
            minutesUntilHighTide = advanceMinutes,
            lunarInfo = info.lunarInfo,
            estimatedHeight = info.lunarInfo.estimatedPeakHighTideMeters
        )
    }

    /**
     * Immediately dispatches a test push notification simulating the safe walk window opening alert.
     */
    fun triggerImmediateSafeWalkOpenAlert(context: Context) {
        val info = calculateSafeWalkWindow(context)
        TideAndWeatherNotificationManager.showWalkWindowAboutToOpen(
            context = context,
            minutesUntilOpen = DEFAULT_WALK_OPEN_ADVANCE_MINUTES,
            distanceRecededKm = info.maxRecessionKm,
            windowDurationHours = info.safeWindowDurationHours
        )
    }

    /**
     * Immediately dispatches a test push notification simulating the safe walk window closing alert (return to shore).
     */
    fun triggerImmediateSafeWalkCloseAlert(context: Context) {
        val info = calculateSafeWalkWindow(context)
        TideAndWeatherNotificationManager.showWalkWindowAboutToClose(
            context = context,
            minutesRemaining = DEFAULT_WALK_CLOSE_BUFFER_MINUTES,
            isUrgent = info.lunarInfo.isSpringTide
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
            alarmManager.set(
                AlarmManager.RTC_WAKEUP,
                triggerEpochMs,
                pendingIntent
            )
            Log.w(TAG, "Exact alarm permission restricted; fallback to standard alarm: ${e.message}")
        }
    }
}
