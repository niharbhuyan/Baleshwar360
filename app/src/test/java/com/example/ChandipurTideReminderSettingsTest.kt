package com.example

import com.example.data.notification.ChandipurTidalTelemetryWorker
import com.example.data.notification.ChandipurTideReminderConfig
import com.example.data.notification.ChandipurTideReminderPreferences
import com.example.data.notification.TideAlertSoundOption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for Chandipur 30-minute tide reminder sound customization & channel ID resolution.
 */
class ChandipurTideReminderSettingsTest {

    @Test
    fun soundOptionLookup_resolvesAllOptionsAndFallsBackToDefault() {
        TideAlertSoundOption.entries.forEach { option ->
            assertEquals(option, TideAlertSoundOption.fromId(option.id))
            assertTrue(option.titleEn.isNotBlank())
            assertTrue(option.titleOr.isNotBlank())
            assertTrue(option.subtitleEn.isNotBlank())
            assertTrue(option.subtitleOr.isNotBlank())
        }
        assertEquals(TideAlertSoundOption.DEFAULT_NOTIFICATION, TideAlertSoundOption.fromId("unknown_id"))
        assertEquals(TideAlertSoundOption.DEFAULT_NOTIFICATION, TideAlertSoundOption.fromId(null))
    }

    @Test
    fun channelIdResolution_preservesDefaultAndDifferentiatesCustomSounds() {
        assertEquals(
            ChandipurTidalTelemetryWorker.CHANNEL_ID_SAFE_TIDE_30M,
            ChandipurTideReminderPreferences.resolveActiveChannelId(TideAlertSoundOption.DEFAULT_NOTIFICATION)
        )

        val alarmChannel = ChandipurTideReminderPreferences.resolveActiveChannelId(TideAlertSoundOption.COASTAL_ALARM_SIREN)
        val chimeChannel = ChandipurTideReminderPreferences.resolveActiveChannelId(TideAlertSoundOption.HARBOR_BELL_CHIME)
        val silentChannel = ChandipurTideReminderPreferences.resolveActiveChannelId(TideAlertSoundOption.SILENT_VIBRATE_ONLY)
        val customV1 = ChandipurTideReminderPreferences.resolveActiveChannelId(TideAlertSoundOption.CUSTOM_DEVICE_TONE, 1)
        val customV2 = ChandipurTideReminderPreferences.resolveActiveChannelId(TideAlertSoundOption.CUSTOM_DEVICE_TONE, 2)

        assertNotEquals(ChandipurTidalTelemetryWorker.CHANNEL_ID_SAFE_TIDE_30M, alarmChannel)
        assertNotEquals(alarmChannel, chimeChannel)
        assertNotEquals(chimeChannel, silentChannel)
        assertNotEquals(customV1, customV2)
        assertTrue(TideAlertSoundOption.COASTAL_ALARM_SIREN.usesAlarmUsage)
        assertFalse(TideAlertSoundOption.DEFAULT_NOTIFICATION.usesAlarmUsage)
    }

    @Test
    fun displayLabel_usesCustomRingtoneTitleWhenCustomToneSelected() {
        val customConfig = ChandipurTideReminderConfig(
            is30MinReminderEnabled = true,
            selectedSound = TideAlertSoundOption.CUSTOM_DEVICE_TONE,
            customSoundUri = "content://media/internal/audio/media/42",
            customSoundTitle = "Ocean Breeze Chime"
        )
        assertEquals(
            "Ocean Breeze Chime",
            ChandipurTideReminderPreferences.getSoundDisplayLabel(customConfig, isOdia = false)
        )

        val defaultConfig = ChandipurTideReminderConfig(
            is30MinReminderEnabled = true,
            selectedSound = TideAlertSoundOption.COASTAL_ALARM_SIREN
        )
        assertEquals(
            TideAlertSoundOption.COASTAL_ALARM_SIREN.titleEn,
            ChandipurTideReminderPreferences.getSoundDisplayLabel(defaultConfig, isOdia = false)
        )
        assertEquals(
            TideAlertSoundOption.COASTAL_ALARM_SIREN.titleOr,
            ChandipurTideReminderPreferences.getSoundDisplayLabel(defaultConfig, isOdia = true)
        )
    }
}
