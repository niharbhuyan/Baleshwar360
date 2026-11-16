package com.example.util

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import com.example.data.model.AppLanguage
import com.example.data.model.HistoricalLandmarkAudioGuide
import com.example.data.model.HistoricalLandmarksCatalog
import com.example.data.model.Hotspot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Text-to-Speech audio guide engine for historical landmarks in the Balasore Tourism module.
 * Provides spoken narratives in English, Odia (ଓଡ଼ିଆ), and Hindi with real-time playback state,
 * speed adjustments, and pause/resume capabilities.
 */
class TourismAudioGuideManager private constructor(context: Context) : TextToSpeech.OnInitListener {

    private val appContext: Context = context.applicationContext
    private var tts: TextToSpeech? = null
    private var isInitialized = false

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _currentLandmarkId = MutableStateFlow<String?>(null)
    val currentLandmarkId: StateFlow<String?> = _currentLandmarkId.asStateFlow()

    private val _currentLandmarkTitle = MutableStateFlow<String?>(null)
    val currentLandmarkTitle: StateFlow<String?> = _currentLandmarkTitle.asStateFlow()

    private val _currentLandmark = MutableStateFlow<HistoricalLandmarkAudioGuide?>(null)
    val currentLandmark: StateFlow<HistoricalLandmarkAudioGuide?> = _currentLandmark.asStateFlow()

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>("Audio guide ready")
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    private val _elapsedSeconds = MutableStateFlow(0)
    val elapsedSeconds: StateFlow<Int> = _elapsedSeconds.asStateFlow()

    private val _totalEstimatedSeconds = MutableStateFlow(50)
    val totalEstimatedSeconds: StateFlow<Int> = _totalEstimatedSeconds.asStateFlow()

    private val _isOdiaVoiceSupported = MutableStateFlow(false)
    val isOdiaVoiceSupported: StateFlow<Boolean> = _isOdiaVoiceSupported.asStateFlow()

    // Internal state cache for resume
    private var lastNarrativeText: String = ""
    private var currentUtteranceId: String = ""
    private var tickerJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    init {
        try {
            tts = TextToSpeech(appContext, this)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to instantiate TextToSpeech engine: ${e.message}")
            _statusMessage.value = "TTS engine not available on device"
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            setupProgressListener()
            checkOdiaVoiceAvailability()
            configureLocaleForLanguage(_currentLanguage.value)
            tts?.setSpeechRate(_speechRate.value)
            tts?.setPitch(1.0f)
            _statusMessage.value = "Audio guide ready"
            Log.i(TAG, "Tourism Audio Guide TTS initialized successfully")
        } else {
            isInitialized = false
            _statusMessage.value = "TTS init failed ($status)"
            Log.w(TAG, "Tourism Audio Guide TTS initialization failed with status: $status")
        }
    }

    private fun checkOdiaVoiceAvailability() {
        val odiaLocale = Locale.Builder().setLanguage("or").setRegion("IN").build()
        val res = tts?.isLanguageAvailable(odiaLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
        _isOdiaVoiceSupported.value = (res >= TextToSpeech.LANG_AVAILABLE)
        Log.i(TAG, "Odia voice availability: ${_isOdiaVoiceSupported.value} (code=$res)")
    }

    private fun configureLocaleForLanguage(language: AppLanguage): Locale {
        val targetLocale = when (language) {
            AppLanguage.ODIA -> {
                val odia = Locale.Builder().setLanguage("or").setRegion("IN").build()
                val status = tts?.isLanguageAvailable(odia) ?: TextToSpeech.LANG_NOT_SUPPORTED
                if (status >= TextToSpeech.LANG_AVAILABLE) {
                    odia
                } else {
                    _statusMessage.value = "Odia voice fallback to Indian English"
                    Locale("en", "IN")
                }
            }
            AppLanguage.HINDI -> Locale("hi", "IN")
            AppLanguage.BENGALI -> Locale("bn", "IN")
            AppLanguage.TELUGU -> Locale("te", "IN")
            AppLanguage.ENGLISH, AppLanguage.SANTALI -> Locale("en", "IN")
            else -> Locale("en", "IN")
        }

        try {
            val setStatus = tts?.setLanguage(targetLocale) ?: TextToSpeech.LANG_NOT_SUPPORTED
            if (setStatus == TextToSpeech.LANG_MISSING_DATA || setStatus == TextToSpeech.LANG_NOT_SUPPORTED) {
                tts?.setLanguage(Locale.ENGLISH)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error applying locale: ${e.message}")
        }
        return targetLocale
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                scope.launch {
                    _isPlaying.value = true
                    _isPaused.value = false
                    _statusMessage.value = "Playing audio narration..."
                    startProgressTicker()
                }
            }

            override fun onDone(utteranceId: String?) {
                scope.launch {
                    _isPlaying.value = false
                    _isPaused.value = false
                    _statusMessage.value = "Narration finished"
                    stopProgressTicker()
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                scope.launch {
                    _isPlaying.value = false
                    _isPaused.value = false
                    _statusMessage.value = "Playback error occurred"
                    stopProgressTicker()
                }
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                scope.launch {
                    _isPlaying.value = false
                    _isPaused.value = false
                    _statusMessage.value = "Playback error code $errorCode"
                    stopProgressTicker()
                }
            }
        })
    }

    private fun startProgressTicker() {
        stopProgressTicker()
        tickerJob = scope.launch {
            while (isActive && _isPlaying.value && !_isPaused.value) {
                delay(1000L)
                val current = _elapsedSeconds.value
                val total = _totalEstimatedSeconds.value
                if (current < total) {
                    _elapsedSeconds.value = current + 1
                }
            }
        }
    }

    private fun stopProgressTicker() {
        tickerJob?.cancel()
        tickerJob = null
    }

    /**
     * Plays the audio guide for a given [HistoricalLandmarkAudioGuide].
     */
    fun playLandmark(
        landmark: HistoricalLandmarkAudioGuide,
        language: AppLanguage = _currentLanguage.value
    ) {
        _currentLandmarkId.value = landmark.id
        _currentLandmarkTitle.value = when (language) {
            AppLanguage.ODIA -> landmark.odiaName
            AppLanguage.HINDI -> landmark.hindiName
            else -> landmark.name
        }
        _currentLandmark.value = landmark
        _currentLanguage.value = language
        _totalEstimatedSeconds.value = landmark.durationSeconds
        _elapsedSeconds.value = 0

        val textToSpeak = when (language) {
            AppLanguage.ODIA -> landmark.audioScriptOr
            AppLanguage.HINDI -> landmark.audioScriptHi
            else -> landmark.audioScriptEn
        }
        lastNarrativeText = textToSpeak

        speakInternal(textToSpeak, "landmark_${landmark.id}_${language.code}")
    }

    /**
     * Helper to play an audio guide for a [Hotspot] from the tourism feed.
     * Searches for a matching historical landmark or constructs a concise informative guide.
     */
    fun playHotspotAudio(
        hotspot: Hotspot,
        language: AppLanguage = _currentLanguage.value
    ) {
        val matchingLandmark = HistoricalLandmarksCatalog.findByHotspotId(hotspot.id)
        if (matchingLandmark != null) {
            playLandmark(matchingLandmark, language)
        } else {
            // Build informative audio clip for custom/unmatched hotspots
            val title = if (language == AppLanguage.ODIA) hotspot.odiaName else hotspot.name
            val text = when (language) {
                AppLanguage.ODIA -> "${hotspot.odiaName}। ${hotspot.description}। ଏହା ବାଲେଶ୍ୱରର ଏକ ଲୋକପ୍ରିୟ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ। ଦୂରତା ପ୍ରାୟ ${hotspot.distanceKm} କିଲୋମିଟର।"
                AppLanguage.HINDI -> "${hotspot.name}। ${hotspot.description}। यह बालेश्वर का एक प्रसिद्ध पर्यटन स्थल है।"
                else -> "${hotspot.name}. ${hotspot.description}. Located ${hotspot.distanceKm} kilometers from Balasore city center. Open for visitors."
            }
            playCustomAudio(
                id = hotspot.id,
                title = title,
                text = text,
                language = language,
                estimatedSec = 30
            )
        }
    }

    /**
     * Plays custom informative audio narration for any landmark or historical topic.
     */
    fun playCustomAudio(
        id: String,
        title: String,
        text: String,
        language: AppLanguage = _currentLanguage.value,
        estimatedSec: Int = 40
    ) {
        _currentLandmarkId.value = id
        _currentLandmarkTitle.value = title
        _currentLanguage.value = language
        _totalEstimatedSeconds.value = estimatedSec
        _elapsedSeconds.value = 0
        lastNarrativeText = text

        speakInternal(text, "custom_${id}_${language.code}")
    }

    /**
     * Toggles playback for a selected landmark.
     */
    fun togglePlayPause(
        landmark: HistoricalLandmarkAudioGuide,
        language: AppLanguage = _currentLanguage.value
    ) {
        if (_currentLandmarkId.value == landmark.id) {
            if (_isPlaying.value) {
                pause()
            } else if (_isPaused.value) {
                resume()
            } else {
                playLandmark(landmark, language)
            }
        } else {
            playLandmark(landmark, language)
        }
    }

    private fun speakInternal(text: String, utteranceId: String) {
        if (!isInitialized || tts == null) {
            Log.w(TAG, "TTS engine not yet initialized. Attempting re-init...")
            _statusMessage.value = "Initializing audio engine..."
            try {
                tts = TextToSpeech(appContext, this)
            } catch (e: Exception) {
                Log.e(TAG, "Cannot re-init TTS: ${e.message}")
            }
            return
        }

        try {
            configureLocaleForLanguage(_currentLanguage.value)
            tts?.setSpeechRate(_speechRate.value)

            val params = Bundle().apply {
                putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, utteranceId)
            }
            currentUtteranceId = utteranceId

            val result = tts?.speak(text, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
            if (result == TextToSpeech.SUCCESS) {
                _isPlaying.value = true
                _isPaused.value = false
                _statusMessage.value = "Playing narration: ${_currentLandmarkTitle.value}"
            } else {
                _isPlaying.value = false
                _statusMessage.value = "TTS speak request error ($result)"
                Log.e(TAG, "TextToSpeech speak returned error code: $result")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during TTS speak: ${e.message}", e)
            _statusMessage.value = "Playback failure: ${e.message}"
        }
    }

    fun pause() {
        if (_isPlaying.value) {
            try {
                tts?.stop()
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping TTS: ${e.message}")
            }
            _isPlaying.value = false
            _isPaused.value = true
            _statusMessage.value = "Audio guide paused"
            stopProgressTicker()
        }
    }

    fun resume() {
        if (_isPaused.value && lastNarrativeText.isNotBlank()) {
            _isPaused.value = false
            speakInternal(lastNarrativeText, "resume_${_currentLandmarkId.value ?: "current"}")
        }
    }

    fun stop() {
        try {
            tts?.stop()
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping TTS: ${e.message}")
        }
        _isPlaying.value = false
        _isPaused.value = false
        _currentLandmarkId.value = null
        _currentLandmarkTitle.value = null
        _elapsedSeconds.value = 0
        _statusMessage.value = "Audio guide stopped"
        stopProgressTicker()
    }

    fun setSpeechRate(rate: Float) {
        _speechRate.value = rate.coerceIn(0.5f, 2.0f)
        tts?.setSpeechRate(_speechRate.value)
    }

    fun setLanguage(language: AppLanguage) {
        if (_currentLanguage.value != language) {
            _currentLanguage.value = language
            configureLocaleForLanguage(language)
            val landmark = _currentLandmark.value
            if (landmark != null && (_isPlaying.value || _isPaused.value)) {
                playLandmark(landmark, language)
            }
        }
    }

    fun shutdown() {
        stopProgressTicker()
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (e: Exception) {
            Log.e(TAG, "Error shutting down TTS: ${e.message}")
        }
        tts = null
        isInitialized = false
    }

    companion object {
        private const val TAG = "TourismAudioGuideMgr"

        @Volatile
        private var instance: TourismAudioGuideManager? = null

        fun getInstance(context: Context): TourismAudioGuideManager {
            return instance ?: synchronized(this) {
                instance ?: TourismAudioGuideManager(context).also { instance = it }
            }
        }
    }
}
