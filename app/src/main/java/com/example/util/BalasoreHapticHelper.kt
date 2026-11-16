package com.example.util

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * Utility for providing subtle, tactile haptic feedback across key user interactions
 * in the Balasore app:
 * - Tapping map markers and distance ruler points
 * - Successfully submitting civic issue reports
 * - Upvoting community reports
 * - Triggering high-priority emergency distress signals
 */
object BalasoreHapticHelper {

    private fun getVibrator(context: Context): Vibrator? {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Subtle, light tick for tapping map pins, markers, or ruler endpoints.
     */
    fun vibrateMarkerTap(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(25)
            }
        } catch (_: Exception) {}
    }

    /**
     * Crisp double-pulse indicating successful completion of an action
     * (e.g. submitting a Civic Eye report or upvoting an issue).
     */
    fun vibrateSuccess(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 35, 45, 60)
                val amplitudes = intArrayOf(0, 140, 0, 200)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 35, 45, 60), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * Crisp, tactile click for key user button interactions (chips, primary CTAs, icon actions).
     */
    fun vibrateButtonClick(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(28)
            }
        } catch (_: Exception) {}
    }

    /**
     * Distinctive firm haptic confirmation for acknowledging, dismissing, or verifying
     * critical emergency alerts, severe weather warnings, or disaster advisories.
     */
    fun vibrateEmergencyAlertAck(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 50, 45, 80)
                val amplitudes = intArrayOf(0, 180, 0, 220)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 50, 45, 80), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * Subtle, tactile tick for toggles, switch flips, and filter chip selections.
     */
    fun vibrateToggle(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(18)
            }
        } catch (_: Exception) {}
    }

    /**
     * Warning pulse alerting user to hazard or rising tide condition.
     */
    fun vibrateWarning(context: Context) {
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 100, 60, 120)
                val amplitudes = intArrayOf(0, 200, 0, 230)
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(longArrayOf(0, 100, 60, 120), -1)
            }
        } catch (_: Exception) {}
    }

    /**
     * Plays a high-volume emergency siren sound burst on STREAM_ALARM to alert nearby people.
     */
    fun playEmergencySirenBurst(context: Context) {
        Thread {
            try {
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? android.media.AudioManager
                val maxVol = audioManager?.getStreamMaxVolume(android.media.AudioManager.STREAM_ALARM) ?: 7
                audioManager?.setStreamVolume(android.media.AudioManager.STREAM_ALARM, maxVol, 0)
                val toneGen = android.media.ToneGenerator(android.media.AudioManager.STREAM_ALARM, 100)
                repeat(3) {
                    toneGen.startTone(android.media.ToneGenerator.TONE_CDMA_EMERGENCY_RINGBACK, 360)
                    Thread.sleep(400)
                    toneGen.startTone(android.media.ToneGenerator.TONE_SUP_ERROR, 360)
                    Thread.sleep(400)
                }
                toneGen.release()
            } catch (_: Exception) {
            }
        }.start()
    }

    /**
     * Distinct long-vibration SOS haptic sequence (... --- ...) that activates simultaneously
     * with the high-volume emergency siren sound for emergency SOS and disaster distress broadcasts.
     */
    fun vibrateEmergencySos(context: Context, playSiren: Boolean = true) {
        if (playSiren) {
            playEmergencySirenBurst(context)
        }
        val vibrator = getVibrator(context) ?: return
        if (!vibrator.hasVibrator()) return

        try {
            // SOS pattern: 3 short (150ms), 3 long (450ms), 3 short (150ms)
            val timings = longArrayOf(
                0,
                150, 100, 150, 100, 150, 220,
                450, 140, 450, 140, 450, 220,
                150, 100, 150, 100, 150
            )
            val amplitudes = intArrayOf(
                0,
                255, 0, 255, 0, 255, 0,
                255, 0, 255, 0, 255, 0,
                255, 0, 255, 0, 255
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(timings, -1)
            }
        } catch (_: Exception) {}
    }
}
