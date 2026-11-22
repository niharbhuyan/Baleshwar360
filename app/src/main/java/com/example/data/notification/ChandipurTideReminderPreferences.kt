package com.example.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.data.service.ChandipurSafeWalkService
import com.example.ui.components.LocalTideCalculationEngine
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Sound options available for the Chandipur Vanishing Sea 30-minute safe walking window reminder.
 */
enum class TideAlertSoundOption(
    val id: String,
    val titleEn: String,
    val titleOr: String,
    val subtitleEn: String,
    val subtitleOr: String,
    val channelSuffix: String,
    val usesAlarmUsage: Boolean = false
) {
    DEFAULT_NOTIFICATION(
        id = "default_notification",
        titleEn = "Default Notification Tone",
        titleOr = "ଡିଫଲ୍ଟ ନୋଟିଫିକେସନ୍ ସ୍ୱର",
        subtitleEn = "Standard Android notification chime",
        subtitleOr = "ସାଧାରଣ ଆଣ୍ଡ୍ରଏଡ୍ ନୋଟିଫିକେସନ୍ ସ୍ୱର",
        channelSuffix = ""
    ),
    COASTAL_ALARM_SIREN(
        id = "coastal_alarm_siren",
        titleEn = "Coastal Alarm Siren",
        titleOr = "ଉପକୂଳ ସାଇରନ୍ ଆଲାର୍ମ",
        subtitleEn = "High-clarity alarm tone audible over sea breeze & waves",
        subtitleOr = "ସମୁଦ୍ର ପବନ ଓ ଲହରୀ ମଧ୍ୟରେ ସ୍ପଷ୍ଟ ଶୁଣାଯାଉଥିବା ଆଲାର୍ମ",
        channelSuffix = "coastal_alarm",
        usesAlarmUsage = true
    ),
    HARBOR_BELL_CHIME(
        id = "harbor_bell_chime",
        titleEn = "Harbor Ringtone Chime",
        titleOr = "ବନ୍ଦର ଘଣ୍ଟି ରିଙ୍ଗଟୋନ୍",
        subtitleEn = "Distinct melodic ringtone alert for safe walk openings",
        subtitleOr = "ସୁରକ୍ଷିତ ଚାଲିବା ସମୟ ପାଇଁ ସ୍ପଷ୍ଟ ରିଙ୍ଗଟୋନ୍ ସ୍ୱର",
        channelSuffix = "harbor_chime"
    ),
    CUSTOM_DEVICE_TONE(
        id = "custom_device_tone",
        titleEn = "Custom Device Alert Sound",
        titleOr = "କଷ୍ଟମ୍ ଡିଭାଇସ୍ ଆଲର୍ଟ ସ୍ୱର",
        subtitleEn = "Choose any tone installed on your Android device",
        subtitleOr = "ଆପଣଙ୍କ ଫୋନ୍ ରୁ ଯେକୌଣସି ସ୍ୱର ବାଛନ୍ତୁ",
        channelSuffix = "custom_tone"
    ),
    SILENT_VIBRATE_ONLY(
        id = "silent_vibrate_only",
        titleEn = "Silent (Vibrate Only)",
        titleOr = "ନୀରବ (କେବଳ କମ୍ପନ)",
        subtitleEn = "Visual alert & rhythmic vibration without audio",
        subtitleOr = "ବିନା ଶବ୍ଦରେ ଦୃଶ୍ୟ ଓ କମ୍ପନ ସତର୍କତା",
        channelSuffix = "silent_vibrate"
    );

    companion object {
        fun fromId(id: String?): TideAlertSoundOption {
            return entries.firstOrNull { it.id == id } ?: DEFAULT_NOTIFICATION
        }
    }
}

data class ChandipurTideReminderConfig(
    val is30MinReminderEnabled: Boolean = true,
    val selectedSound: TideAlertSoundOption = TideAlertSoundOption.DEFAULT_NOTIFICATION,
    val customSoundUri: String? = null,
    val customSoundTitle: String? = null,
    val vibrateEnabled: Boolean = true,
    val closingReturnReminderEnabled: Boolean = true
)

/**
 * Centralized preferences, Android NotificationChannel sound manager, and audio preview helper
 * for the Chandipur Vanishing Sea 30-minute safe walking window reminder.
 */
object ChandipurTideReminderPreferences {

    private const val TAG = "ChandipurTideSoundPrefs"
    private const val PREFS_NAME = "chandipur_tide_reminder_settings_prefs"

    private const val KEY_SOUND_OPTION_ID = "key_tide_sound_option_id"
    private const val KEY_CUSTOM_SOUND_URI = "key_tide_custom_sound_uri"
    private const val KEY_CUSTOM_SOUND_TITLE = "key_tide_custom_sound_title"
    private const val KEY_CUSTOM_CHANNEL_REVISION = "key_tide_custom_channel_revision"
    private const val KEY_VIBRATE_ENABLED = "key_tide_vibrate_enabled"
    private const val KEY_CLOSING_RETURN_ENABLED = "key_tide_closing_return_enabled"

    private val _configFlow = MutableStateFlow(ChandipurTideReminderConfig())
    val configFlow: StateFlow<ChandipurTideReminderConfig> = _configFlow.asStateFlow()

    private var activePreviewRingtone: Ringtone? = null
    private var activeToneGenerator: ToneGenerator? = null

    private fun getPrefs(context: Context) =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun loadConfig(context: Context): ChandipurTideReminderConfig {
        val prefs = getPrefs(context)
        val reminderEnabled = LocalTideCalculationEngine.isSafeTideReminderEnabled(context)
        val soundOption = TideAlertSoundOption.fromId(
            prefs.getString(KEY_SOUND_OPTION_ID, TideAlertSoundOption.DEFAULT_NOTIFICATION.id)
        )
        val customUri = prefs.getString(KEY_CUSTOM_SOUND_URI, null)
        val customTitle = prefs.getString(KEY_CUSTOM_SOUND_TITLE, null)
        val vibrateEnabled = prefs.getBoolean(KEY_VIBRATE_ENABLED, true)
        val closingEnabled = prefs.getBoolean(KEY_CLOSING_RETURN_ENABLED, true)

        val config = ChandipurTideReminderConfig(
            is30MinReminderEnabled = reminderEnabled,
            selectedSound = soundOption,
            customSoundUri = customUri,
            customSoundTitle = customTitle,
            vibrateEnabled = vibrateEnabled,
            closingReturnReminderEnabled = closingEnabled
        )
        _configFlow.value = config
        return config
    }

    fun set30MinReminderEnabled(context: Context, enabled: Boolean): ChandipurTideReminderConfig {
        val appContext = context.applicationContext
        LocalTideCalculationEngine.setSafeTideReminderEnabled(appContext, enabled)
        TideAndWeatherNotificationManager.setWalkWindowAlertsEnabled(appContext, enabled)
        TideAndWeatherNotificationManager.setWalkWindowAdvanceMinutes(appContext, 30)

        if (enabled) {
            ensureConfiguredNotificationChannel(appContext)
            ChandipurTidalTelemetryWorker.schedulePeriodic(appContext, intervalMinutes = 15)
            ChandipurSafeWalkService.scheduleSafeWalkAlerts(appContext)
        } else {
            ChandipurTidalTelemetryWorker.cancel(appContext)
            ChandipurSafeWalkService.cancelAlert(appContext)
        }
        return loadConfig(appContext)
    }

    fun setSelectedSoundOption(context: Context, option: TideAlertSoundOption): ChandipurTideReminderConfig {
        val appContext = context.applicationContext
        getPrefs(appContext).edit()
            .putString(KEY_SOUND_OPTION_ID, option.id)
            .apply()
        ensureConfiguredNotificationChannel(appContext)
        return loadConfig(appContext)
    }

    fun setCustomDeviceSound(
        context: Context,
        soundUri: Uri?,
        resolvedTitle: String? = null
    ): ChandipurTideReminderConfig {
        val appContext = context.applicationContext
        val prefs = getPrefs(appContext)
        if (soundUri == null) {
            return setSelectedSoundOption(appContext, TideAlertSoundOption.SILENT_VIBRATE_ONLY)
        }
        val title = resolvedTitle?.takeIf { it.isNotBlank() } ?: runCatching {
            RingtoneManager.getRingtone(appContext, soundUri)?.getTitle(appContext)
        }.getOrNull().orEmpty().ifBlank { "Custom Device Tone" }

        val nextRevision = prefs.getInt(KEY_CUSTOM_CHANNEL_REVISION, 0) + 1
        prefs.edit()
            .putString(KEY_SOUND_OPTION_ID, TideAlertSoundOption.CUSTOM_DEVICE_TONE.id)
            .putString(KEY_CUSTOM_SOUND_URI, soundUri.toString())
            .putString(KEY_CUSTOM_SOUND_TITLE, title)
            .putInt(KEY_CUSTOM_CHANNEL_REVISION, nextRevision)
            .apply()

        ensureConfiguredNotificationChannel(appContext)
        return loadConfig(appContext)
    }

    fun setVibrateEnabled(context: Context, enabled: Boolean): ChandipurTideReminderConfig {
        val appContext = context.applicationContext
        getPrefs(appContext).edit()
            .putBoolean(KEY_VIBRATE_ENABLED, enabled)
            .apply()
        ensureConfiguredNotificationChannel(appContext)
        return loadConfig(appContext)
    }

    fun setClosingReturnReminderEnabled(context: Context, enabled: Boolean): ChandipurTideReminderConfig {
        val appContext = context.applicationContext
        getPrefs(appContext).edit()
            .putBoolean(KEY_CLOSING_RETURN_ENABLED, enabled)
            .apply()
        return loadConfig(appContext)
    }

    /**
     * Resolves the Android [Uri] for the user's selected alert sound option (or null when silent).
     */
    fun resolveAlertSoundUri(
        context: Context,
        option: TideAlertSoundOption = loadConfig(context).selectedSound,
        customUriString: String? = loadConfig(context).customSoundUri
    ): Uri? {
        return when (option) {
            TideAlertSoundOption.DEFAULT_NOTIFICATION -> {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            TideAlertSoundOption.COASTAL_ALARM_SIREN -> {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            TideAlertSoundOption.HARBOR_BELL_CHIME -> {
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            TideAlertSoundOption.CUSTOM_DEVICE_TONE -> {
                customUriString?.takeIf { it.isNotBlank() }?.let { runCatching { Uri.parse(it) }.getOrNull() }
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            }
            TideAlertSoundOption.SILENT_VIBRATE_ONLY -> null
        }
    }

    /**
     * Computes a deterministic channel ID for the active sound selection so Android O+ honors
     * sound customizations immediately without being locked to the initial channel's immutable sound.
     */
    fun resolveActiveChannelId(
        option: TideAlertSoundOption,
        customChannelRevision: Int = 0
    ): String {
        return when (option) {
            TideAlertSoundOption.DEFAULT_NOTIFICATION -> ChandipurTidalTelemetryWorker.CHANNEL_ID_SAFE_TIDE_30M
            TideAlertSoundOption.CUSTOM_DEVICE_TONE -> {
                val rev = customChannelRevision.coerceAtLeast(1)
                "${ChandipurTidalTelemetryWorker.CHANNEL_ID_SAFE_TIDE_30M}_${option.channelSuffix}_v$rev"
            }
            else -> "${ChandipurTidalTelemetryWorker.CHANNEL_ID_SAFE_TIDE_30M}_${option.channelSuffix}"
        }
    }

    fun getActiveChannelId(context: Context): String {
        val prefs = getPrefs(context)
        val option = TideAlertSoundOption.fromId(
            prefs.getString(KEY_SOUND_OPTION_ID, TideAlertSoundOption.DEFAULT_NOTIFICATION.id)
        )
        val revision = prefs.getInt(KEY_CUSTOM_CHANNEL_REVISION, 1)
        return resolveActiveChannelId(option, revision)
    }

    /**
     * Ensures the Android O+ NotificationChannel for the user's selected Chandipur 30-min reminder
     * sound exists with matching [AudioAttributes], sound [Uri], and vibration pattern.
     */
    fun ensureConfiguredNotificationChannel(context: Context): String {
        val appContext = context.applicationContext
        val config = loadConfig(appContext)
        val channelId = getActiveChannelId(appContext)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = appContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return channelId

            val soundUri = resolveAlertSoundUri(appContext, config.selectedSound, config.customSoundUri)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(
                    if (config.selectedSound.usesAlarmUsage) {
                        AudioAttributes.USAGE_ALARM
                    } else {
                        AudioAttributes.USAGE_NOTIFICATION_EVENT
                    }
                )
                .build()

            val channelName = when (config.selectedSound) {
                TideAlertSoundOption.DEFAULT_NOTIFICATION -> ChandipurTidalTelemetryWorker.CHANNEL_NAME_SAFE_TIDE_30M
                TideAlertSoundOption.CUSTOM_DEVICE_TONE -> {
                    val label = config.customSoundTitle ?: "Custom Sound"
                    "Chandipur Safe Tide 30-Min ($label)"
                }
                else -> "Chandipur Safe Tide 30-Min (${config.selectedSound.titleEn})"
            }

            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Proactive 30-minute notifications before safe walking windows at Chandipur Vanishing Sea"
                enableLights(true)
                lightColor = Color.rgb(2, 132, 199)
                setShowBadge(true)
                enableVibration(config.vibrateEnabled)
                if (config.vibrateEnabled) {
                    vibrationPattern = longArrayOf(0, 350, 150, 350, 150, 450)
                }
                setSound(soundUri, if (soundUri != null) audioAttributes else null)
            }
            manager.createNotificationChannel(channel)
        }

        return channelId
    }

    fun getSoundDisplayLabel(
        config: ChandipurTideReminderConfig,
        isOdia: Boolean = false
    ): String {
        return if (config.selectedSound == TideAlertSoundOption.CUSTOM_DEVICE_TONE && !config.customSoundTitle.isNullOrBlank()) {
            config.customSoundTitle
        } else if (isOdia) {
            config.selectedSound.titleOr
        } else {
            config.selectedSound.titleEn
        }
    }

    /**
     * Plays a brief audible preview of the selected tide alert sound so users can evaluate clarity.
     * Returns true if playback started successfully.
     */
    fun previewSound(
        context: Context,
        option: TideAlertSoundOption,
        customUriString: String? = null
    ): Boolean {
        stopPreview()
        if (option == TideAlertSoundOption.SILENT_VIBRATE_ONLY) {
            return false
        }
        val uri = resolveAlertSoundUri(context, option, customUriString)
        if (uri != null) {
            try {
                val ringtone = RingtoneManager.getRingtone(context.applicationContext, uri)
                if (ringtone != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ringtone.audioAttributes = AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setUsage(
                                if (option.usesAlarmUsage) AudioAttributes.USAGE_ALARM
                                else AudioAttributes.USAGE_NOTIFICATION_EVENT
                            )
                            .build()
                    }
                    activePreviewRingtone = ringtone
                    ringtone.play()
                    return true
                }
            } catch (e: Exception) {
                Log.w(TAG, "Ringtone preview fallback triggered: ${e.message}")
            }
        }

        // Graceful fallback tone when running on emulators without default ringtone media
        return try {
            val toneType = when (option) {
                TideAlertSoundOption.COASTAL_ALARM_SIREN -> ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK
                TideAlertSoundOption.HARBOR_BELL_CHIME -> ToneGenerator.TONE_PROP_BEEP2
                else -> ToneGenerator.TONE_PROP_ACK
            }
            val generator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 85)
            activeToneGenerator = generator
            generator.startTone(toneType, 650)
            true
        } catch (e: Exception) {
            Log.w(TAG, "ToneGenerator preview unavailable: ${e.message}")
            false
        }
    }

    fun stopPreview() {
        try {
            activePreviewRingtone?.let {
                if (it.isPlaying) it.stop()
            }
        } catch (_: Exception) {
        } finally {
            activePreviewRingtone = null
        }

        try {
            activeToneGenerator?.stopTone()
            activeToneGenerator?.release()
        } catch (_: Exception) {
        } finally {
            activeToneGenerator = null
        }
    }
}
