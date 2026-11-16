package com.example

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import com.example.data.fcm.BalasoreNotificationHelper
import com.example.data.fcm.FcmManager
import com.example.data.maps.BalasoreOfflineMapManager
import com.example.data.maps.BalasoreOfflineTileProvider
import com.example.data.maps.BalasoreOsmOfflineTileProvider
import com.google.android.gms.maps.model.TileProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import java.io.File

/**
 * Unit tests verifying:
 * 1. Offline Vector Raster Map Provider tile synthesis and zoom bounding.
 * 2. OSMDroid offline mode toggle behavior and coordinate tile math.
 * 3. Offline map pack storage calculations and simulated outage mode.
 * 4. Firebase Cloud Messaging channels and topics for real-time weather and coastal defense alerts.
 */
class OfflineVectorRasterMapDashboardTest {

    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var tempDir: File

    @Before
    fun setUp() {
        mockContext = mock()
        mockPrefs = mock()
        mockEditor = mock()
        tempDir = File(System.getProperty("java.io.tmpdir"), "offline_map_test_${System.currentTimeMillis()}").apply { mkdirs() }

        whenever(mockContext.applicationContext).thenReturn(mockContext)
        whenever(mockContext.filesDir).thenReturn(tempDir)
        whenever(mockContext.getSharedPreferences(any(), eq(Context.MODE_PRIVATE))).thenReturn(mockPrefs)
        whenever(mockPrefs.edit()).thenReturn(mockEditor)
        whenever(mockEditor.putBoolean(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putInt(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putLong(any(), any())).thenReturn(mockEditor)
        whenever(mockEditor.putString(any(), any())).thenReturn(mockEditor)
    }

    @Test
    fun testBalasoreOfflineTileProvider_ZoomBoundsAndTileCoordinates() {
        val provider = BalasoreOfflineTileProvider(mockContext, isEmergencyHighContrast = false)

        // Out-of-bounds zoom (< 10) must return NO_TILE
        val zoom9Tile = provider.getTile(x = 100, y = 100, zoom = 9)
        assertEquals(TileProvider.NO_TILE, zoom9Tile)

        // Out-of-bounds zoom (> 17) must return NO_TILE
        val zoom18Tile = provider.getTile(x = 100, y = 100, zoom = 18)
        assertEquals(TileProvider.NO_TILE, zoom18Tile)

        // Verify Balasore LatLng to Tile conversion
        val lat = 21.4934
        val lng = 86.9135
        val zoom = 14
        val tileX = BalasoreOfflineTileProvider.latLngToTileX(lng, zoom)
        val tileY = BalasoreOfflineTileProvider.latLngToTileY(lat, zoom)

        assertTrue("Tile X should be positive", tileX > 0)
        assertTrue("Tile Y should be positive", tileY > 0)
        val maxTileIndex = 1 shl zoom
        assertTrue("Tile X within zoom bounds", tileX < maxTileIndex)
        assertTrue("Tile Y within zoom bounds", tileY < maxTileIndex)
    }

    @Test
    fun testBalasoreOsmTileMath_CalculatesCoordinatesAccurately() {
        // Test Chandipur Coordinates (Lat: 21.4700, Lon: 87.0160)
        val chandipurLat = 21.4700
        val chandipurLon = 87.0160
        val zoom = 14

        val tileX = BalasoreOsmOfflineTileProvider.lon2tile(chandipurLon, zoom)
        val tileY = BalasoreOsmOfflineTileProvider.lat2tile(chandipurLat, zoom)

        assertTrue("TileX must be within standard spherical Mercator range for zoom 14", tileX in 12000..13000)
        assertTrue("TileY must be within standard spherical Mercator range for zoom 14", tileY in 7000..8000)
    }

    @Test
    fun testBalasoreOfflineMapManager_KeyAreasAndOutageToggle() {
        val manager = BalasoreOfflineMapManager.getInstance(mockContext)

        val areas = manager.keyAreas
        assertTrue("Must define at least 6 key Balasore areas for pre-caching", areas.size >= 6)

        val areaIds = areas.map { it.id }
        assertTrue("Must include Balasore central town", areaIds.contains("balasore_town"))
        assertTrue("Must include Chandipur coast & defense range", areaIds.contains("chandipur_coast"))
        assertTrue("Must include Balaramgadi fishing port", areaIds.contains("balaramgadi_harbor"))

        // Simulated Outage Toggle
        val initialOutage = manager.isSimulatedOutage.value
        manager.toggleSimulatedOutage()
        assertEquals(!initialOutage, manager.isSimulatedOutage.value)

        // Reset
        manager.toggleSimulatedOutage()
        assertEquals(initialOutage, manager.isSimulatedOutage.value)
    }

    @Test
    fun testFcmRealTimePushNotificationTopics_ConfiguredCorrectly() {
        // Verify topic constants for Weather Warnings, Coastal Defense Advisories, and Breaking News
        assertEquals("balasore_weather_alerts", FcmManager.TOPIC_WEATHER_ALERTS)
        assertEquals("balasore_defense_advisories", FcmManager.TOPIC_DEFENSE_ADVISORIES)
        assertEquals("balasore_breaking_news", FcmManager.TOPIC_BREAKING_NEWS)

        // Verify notification channel IDs
        assertEquals("balasore_weather_alerts", BalasoreNotificationHelper.CHANNEL_ID_WEATHER)
        assertEquals("balasore_defense_advisories", BalasoreNotificationHelper.CHANNEL_ID_DEFENSE_TEST)
        assertEquals("balasore_breaking_news", BalasoreNotificationHelper.CHANNEL_ID_NEWS)
        assertEquals("balasore_coastal_flood_alerts", BalasoreNotificationHelper.CHANNEL_ID_COASTAL_FLOOD)
    }
}
