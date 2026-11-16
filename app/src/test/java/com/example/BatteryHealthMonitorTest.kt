package com.example

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.BatteryManager
import android.os.PowerManager
import com.example.data.sync.EnergyImpactLevel
import com.example.data.sync.SyncManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

/**
 * Unit tests for Battery Health Monitor:
 * 1. Tracks background services energy consumption (drain % and mAh).
 * 2. Provides actionable tips to optimize sync intervals for battery longevity.
 * 3. Telemetry calculations for battery health, temperature, voltage, and longevity score.
 */
class BatteryHealthMonitorTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockBatteryManager: BatteryManager
    private lateinit var mockPowerManager: PowerManager

    private val prefsStore = mutableMapOf<String, Any>()

    @Before
    fun setUp() {
        mockContext = mock()
        mockPrefs = mock()
        mockEditor = mock()
        mockBatteryManager = mock()
        mockPowerManager = mock()

        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockContext.getSharedPreferences(eq("app_health_sync_preferences"), eq(Context.MODE_PRIVATE)))
            .thenReturn(mockPrefs)
        whenever(mockPrefs.edit()).thenReturn(mockEditor)

        whenever(mockContext.getSystemService(Context.BATTERY_SERVICE)).thenReturn(mockBatteryManager)
        whenever(mockContext.getSystemService(Context.POWER_SERVICE)).thenReturn(mockPowerManager)

        // Mock shared preferences get/put
        whenever(mockEditor.putBoolean(any(), any())).thenAnswer {
            val key = it.getArgument<String>(0)
            val value = it.getArgument<Boolean>(1)
            prefsStore[key] = value
            mockEditor
        }
        whenever(mockEditor.putInt(any(), any())).thenAnswer {
            val key = it.getArgument<String>(0)
            val value = it.getArgument<Int>(1)
            prefsStore[key] = value
            mockEditor
        }
        whenever(mockEditor.putLong(any(), any())).thenAnswer {
            val key = it.getArgument<String>(0)
            val value = it.getArgument<Long>(1)
            prefsStore[key] = value
            mockEditor
        }
        whenever(mockEditor.putFloat(any(), any())).thenAnswer {
            val key = it.getArgument<String>(0)
            val value = it.getArgument<Float>(1)
            prefsStore[key] = value
            mockEditor
        }

        whenever(mockPrefs.getBoolean(any(), any())).thenAnswer {
            val key = it.getArgument<String>(0)
            val default = it.getArgument<Boolean>(1)
            (prefsStore[key] as? Boolean) ?: default
        }
        whenever(mockPrefs.getInt(any(), any())).thenAnswer {
            val key = it.getArgument<String>(0)
            val default = it.getArgument<Int>(1)
            (prefsStore[key] as? Int) ?: default
        }
        whenever(mockPrefs.getLong(any(), any())).thenAnswer {
            val key = it.getArgument<String>(0)
            val default = it.getArgument<Long>(1)
            (prefsStore[key] as? Long) ?: default
        }
        whenever(mockPrefs.getFloat(any(), any())).thenAnswer {
            val key = it.getArgument<String>(0)
            val default = it.getArgument<Float>(1)
            (prefsStore[key] as? Float) ?: default
        }

        whenever(mockPowerManager.isInteractive).thenReturn(true)
        whenever(mockBatteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)).thenReturn(85)
    }

    @Test
    fun testBackgroundServicesEnergyProfiles_ContainsAllServicesAndValidDrain() {
        val profiles = SyncManager.getBackgroundServiceProfiles(mockContext)

        // Should profile the core background services in the app
        assertTrue("Must track at least 5 background services", profiles.size >= 5)

        val serviceIds = profiles.map { it.id }
        assertTrue("Must include WorkManager essential data sync", serviceIds.contains("svc_datasync"))
        assertTrue("Must include Chandipur tidal alarm", serviceIds.contains("svc_tide_alarm"))
        assertTrue("Must include Balasore live train poller", serviceIds.contains("svc_train_poller"))
        assertTrue("Must include FCM severe weather dispatcher", serviceIds.contains("svc_cyclone_fcm"))
        assertTrue("Must include offline map cache", serviceIds.contains("svc_offline_maps"))
        assertTrue("Must include home screen widgets", serviceIds.contains("svc_home_widgets"))

        // Verify drain metrics
        profiles.forEach { profile ->
            assertTrue("Drain percentage must be positive for ${profile.name}", profile.dailyDrainPercent >= 0f)
            assertTrue("Energy in mAh must be positive for ${profile.name}", profile.energyMilliAmpHours >= 0f)
            assertTrue("Runs per day must be >= 0 for ${profile.name}", profile.runsPerDay >= 0)
            assertNotNull("Must specify power impact level for ${profile.name}", profile.powerImpactLevel)
        }
    }

    @Test
    fun testSyncIntervalLongevityAdjustment_ReducesWakeups() {
        // Base interval 15m
        SyncManager.setUserBaseSyncIntervalMinutes(mockContext, 15)
        assertEquals(15, SyncManager.getUserBaseSyncIntervalMinutes(mockContext))

        val profiles15m = SyncManager.getBackgroundServiceProfiles(mockContext)
        val dataSync15m = profiles15m.first { it.id == "svc_datasync" }

        // Optimize interval to 60m
        SyncManager.setUserBaseSyncIntervalMinutes(mockContext, 60)
        assertEquals(60, SyncManager.getUserBaseSyncIntervalMinutes(mockContext))

        val profiles60m = SyncManager.getBackgroundServiceProfiles(mockContext)
        val dataSync60m = profiles60m.first { it.id == "svc_datasync" }

        // 60m interval must execute fewer runs and consume less daily energy
        assertTrue(
            "60m interval must produce fewer daily runs than 15m interval",
            dataSync60m.runsPerDay < dataSync15m.runsPerDay
        )
        assertTrue(
            "60m interval must consume less daily drain % than 15m interval",
            dataSync60m.dailyDrainPercent < dataSync15m.dailyDrainPercent
        )
    }

    @Test
    fun testActionableLongevityTips_ProvidesOptimizationAndCanApplyAll() {
        val tips = SyncManager.getActionableLongevityTips(mockContext)
        assertTrue("Must provide at least 4 actionable tips", tips.size >= 4)

        val tipIds = tips.map { it.id }
        assertTrue("Must include sync interval tip", tipIds.contains("tip_sync_interval"))
        assertTrue("Must include battery aware scaling tip", tipIds.contains("tip_battery_aware"))
        assertTrue("Must include Wi-Fi only restriction tip", tipIds.contains("tip_wifi_only"))
        assertTrue("Must include night quiet hours tip", tipIds.contains("tip_night_quiet"))

        // Apply all optimizations
        SyncManager.applyAllLongevityOptimizations(mockContext)

        val updatedTips = SyncManager.getActionableLongevityTips(mockContext)
        val intervalTip = updatedTips.first { it.id == "tip_sync_interval" }
        val wifiTip = updatedTips.first { it.id == "tip_wifi_only" }
        val nightQuietTip = updatedTips.first { it.id == "tip_night_quiet" }
        assertTrue("Sync interval tip must be marked applied after applyAll", intervalTip.isApplied)
        assertEquals(60, SyncManager.getUserBaseSyncIntervalMinutes(mockContext))
        assertTrue(SyncManager.isBatteryAwareModeEnabled(mockContext))
        assertTrue(wifiTip.isApplied)
        assertTrue(nightQuietTip.isApplied)
    }

    @Test
    fun testBatteryHealthTelemetry_CalculatesLongevityScore() {
        val telemetry = SyncManager.getBatteryHealthTelemetry(mockContext)

        assertEquals(85, telemetry.percentage)
        assertTrue("Longevity score must be in range 40..100", telemetry.longevityScore in 40..100)
        assertTrue("Estimated remaining hours must be positive", telemetry.estimatedRemainingHours > 0f)
        assertNotNull(telemetry.longevityRating)
        assertNotNull(telemetry.projectedLifespanYears)
    }
}
