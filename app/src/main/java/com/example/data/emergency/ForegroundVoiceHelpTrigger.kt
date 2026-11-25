package com.example.data.emergency

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

/**
 * Runtime state for the foreground hands-free "Help me" -> 112 emergency voice trigger.
 */
data class ForegroundVoiceTriggerState(
    val isArmed: Boolean = false,
    val isListening: Boolean = false,
    val isSpeechAvailable: Boolean = true,
    val lastHeardSnippet: String = "",
    val statusMessage: String = "Tap mic to arm foreground 'Help me' -> 112 voice guard"
)

/**
 * Lifecycle-aware foreground-only voice trigger powered by Android's native [SpeechRecognizer].
 *
 * Continuously listens ONLY while:
 * 1. The user has armed the voice trigger toggle,
 * 2. `RECORD_AUDIO` permission is granted, and
 * 3. The app is in the foreground (`onAppResumed()` active; immediately stops on `onAppPaused()`).
 *
 * When the user says "Help me" (English or Odia equivalent), [onHelpMeDetected] is invoked
 * with the matched transcript so the UI can trigger the 112 emergency call flow.
 */
class ForegroundVoiceHelpTrigger(
    private val context: Context,
    private val onHelpMeDetected: (matchedTranscript: String) -> Unit
) {
    private val appContext = context.applicationContext
    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null

    private var isForeground: Boolean = false
    private var isArmed: Boolean = isSavedArmedPreference(appContext)
    private var isPausedForAlertDialog: Boolean = false
    private var currentLocaleTag: String = "en-IN"
    private var lastTriggerTimestampMs: Long = 0L

    private val _state = MutableStateFlow(
        ForegroundVoiceTriggerState(
            isArmed = isArmed,
            isListening = false,
            isSpeechAvailable = SpeechRecognizer.isRecognitionAvailable(appContext),
            statusMessage = if (isArmed) "Foreground 'Help me' guard armed" else "Foreground 'Help me' guard off"
        )
    )
    val state: StateFlow<ForegroundVoiceTriggerState> = _state.asStateFlow()

    private val restartRunnable = Runnable {
        if (shouldActivelyListen()) {
            startListeningInternal()
        }
    }

    fun setLocale(localeTag: String) {
        currentLocaleTag = localeTag
    }

    fun setArmed(armed: Boolean) {
        isArmed = armed
        saveArmedPreference(appContext, armed)
        if (armed) {
            if (isForeground) {
                startListeningIfReady()
            } else {
                _state.value = _state.value.copy(
                    isArmed = true,
                    isListening = false,
                    statusMessage = "Armed — activates in foreground"
                )
            }
        } else {
            stopAndReleaseRecognizer(keepArmedFlag = false)
        }
    }

    fun pauseForAlertConfirmation(paused: Boolean) {
        isPausedForAlertDialog = paused
        if (paused) {
            stopListeningOnly()
        } else if (shouldActivelyListen()) {
            scheduleRestart(delayMs = 900L)
        }
    }

    fun onAppResumed() {
        isForeground = true
        if (shouldActivelyListen()) {
            startListeningIfReady()
        }
    }

    fun onAppPaused() {
        isForeground = false
        mainHandler.removeCallbacks(restartRunnable)
        stopAndReleaseRecognizer(keepArmedFlag = isArmed)
    }

    fun destroy() {
        isForeground = false
        mainHandler.removeCallbacks(restartRunnable)
        stopAndReleaseRecognizer(keepArmedFlag = isArmed)
    }

    fun hasRecordAudioPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            appContext,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun shouldActivelyListen(): Boolean {
        return isArmed && isForeground && !isPausedForAlertDialog && hasRecordAudioPermission()
    }

    fun startListeningIfReady() {
        mainHandler.post {
            if (!isArmed) return@post
            if (!hasRecordAudioPermission()) {
                _state.value = _state.value.copy(
                    isArmed = false,
                    isListening = false,
                    statusMessage = "Microphone permission required for 'Help me' voice guard"
                )
                isArmed = false
                saveArmedPreference(appContext, false)
                return@post
            }
            if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
                _state.value = _state.value.copy(
                    isArmed = isArmed,
                    isListening = false,
                    isSpeechAvailable = false,
                    statusMessage = "On-device SpeechRecognizer unavailable on this device"
                )
                return@post
            }
            if (!isForeground || isPausedForAlertDialog) return@post
            startListeningInternal()
        }
    }

    private fun startListeningInternal() {
        mainHandler.removeCallbacks(restartRunnable)
        try {
            if (speechRecognizer == null) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext).apply {
                    setRecognitionListener(recognitionListener)
                }
            }
            val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, currentLocaleTag)
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 4)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 2200L)
                putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 1800L)
            }
            speechRecognizer?.startListening(recognizerIntent)
            _state.value = _state.value.copy(
                isArmed = true,
                isListening = true,
                isSpeechAvailable = true,
                statusMessage = "Listening in foreground for 'Help me'..."
            )
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start foreground SpeechRecognizer: ${e.message}")
            _state.value = _state.value.copy(
                isListening = false,
                statusMessage = "Voice guard standby (retrying...)"
            )
            scheduleRestart(delayMs = 2000L)
        }
    }

    private fun stopListeningOnly() {
        mainHandler.removeCallbacks(restartRunnable)
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {}
            _state.value = _state.value.copy(isListening = false)
        }
    }

    private fun stopAndReleaseRecognizer(keepArmedFlag: Boolean) {
        mainHandler.removeCallbacks(restartRunnable)
        mainHandler.post {
            try {
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
            speechRecognizer = null
            _state.value = _state.value.copy(
                isArmed = keepArmedFlag,
                isListening = false,
                statusMessage = if (keepArmedFlag) {
                    "Foreground 'Help me' guard paused (background)"
                } else {
                    "Foreground 'Help me' guard off"
                }
            )
        }
    }

    private fun scheduleRestart(delayMs: Long) {
        mainHandler.removeCallbacks(restartRunnable)
        if (shouldActivelyListen()) {
            mainHandler.postDelayed(restartRunnable, delayMs)
        }
    }

    private fun evaluateMatches(matches: List<String>?) {
        if (matches.isNullOrEmpty()) return
        val topSnippet = matches.firstOrNull()?.trim().orEmpty()
        if (topSnippet.isNotBlank()) {
            _state.value = _state.value.copy(lastHeardSnippet = topSnippet)
        }

        val matchedPhrase = matches.firstOrNull { isHelpMeTriggerPhrase(it) }
        if (matchedPhrase != null) {
            val now = System.currentTimeMillis()
            if (now - lastTriggerTimestampMs >= TRIGGER_COOLDOWN_MS) {
                lastTriggerTimestampMs = now
                isPausedForAlertDialog = true
                stopListeningOnly()
                Log.i(TAG, "Foreground emergency voice phrase detected: \"$matchedPhrase\"")
                onHelpMeDetected(matchedPhrase.trim())
            }
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = _state.value.copy(
                isListening = true,
                statusMessage = "Listening in foreground for 'Help me'..."
            )
        }

        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}

        override fun onEndOfSpeech() {
            _state.value = _state.value.copy(isListening = false)
        }

        override fun onError(error: Int) {
            _state.value = _state.value.copy(isListening = false)
            when (error) {
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                    isArmed = false
                    saveArmedPreference(appContext, false)
                    _state.value = _state.value.copy(
                        isArmed = false,
                        statusMessage = "Microphone permission needed for 'Help me' guard"
                    )
                }
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> {
                    scheduleRestart(delayMs = 1500L)
                }
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    scheduleRestart(delayMs = 600L)
                }
                else -> {
                    scheduleRestart(delayMs = 1400L)
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            evaluateMatches(matches)
            if (shouldActivelyListen()) {
                scheduleRestart(delayMs = 650L)
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partials = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            evaluateMatches(partials)
        }

        override fun onEvent(eventType: Int, params: Bundle?) {}
    }

    companion object {
        private const val TAG = "ForegroundVoiceHelp"
        private const val PREFS_NAME = "balasore_voice_help_trigger_prefs"
        private const val KEY_VOICE_GUARD_ARMED = "key_foreground_voice_help_armed"
        private const val TRIGGER_COOLDOWN_MS = 8_000L

        private val HELP_ME_REGEX = Regex("\\bhelp\\s+me\\b", RegexOption.IGNORE_CASE)
        private val ODIA_OR_EMERGENCY_TRIGGERS = listOf(
            "ମୋତେ ସାହାଯ୍ୟ କର",
            "ସାହାଯ୍ୟ କରନ୍ତୁ",
            "ସାହାଯ୍ୟ କର",
            "call 112",
            "dial 112",
            "emergency 112"
        )

        /**
         * Returns true when [transcript] contains the foreground distress phrase "Help me"
         * (with word boundaries so normal words like "helpful" or "helpline" do not falsely trigger)
         * or its Odia / 112 equivalents.
         */
        fun isHelpMeTriggerPhrase(transcript: String): Boolean {
            val normalized = transcript
                .lowercase(Locale.ROOT)
                .replace(Regex("[.,!?\"'-]+"), " ")
                .replace(Regex("\\s+"), " ")
                .trim()
            if (normalized.isBlank()) return false
            if (HELP_ME_REGEX.containsMatchIn(normalized)) return true
            return ODIA_OR_EMERGENCY_TRIGGERS.any { normalized.contains(it) }
        }

        fun isSavedArmedPreference(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_VOICE_GUARD_ARMED, false)
        }

        fun saveArmedPreference(context: Context, armed: Boolean) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_VOICE_GUARD_ARMED, armed)
                .apply()
        }
    }
}
