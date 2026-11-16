package com.example

import com.example.data.maps.BalasoreOsmOfflineTileProvider
import com.example.data.maps.BalasoreOsmTileSource
import com.example.data.maps.OsmPreCacheStats
import com.example.data.maps.OsmTileCoord
import com.example.ui.components.BALASORE_TOURIST_LOCATIONS
import com.example.ui.viewmodel.UniqueFeatureSheetType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BalasoreOsmOfflineTileProviderTest {

    @Test
    fun testBalasoreOsmTileSourceProperties() {
        val source = BalasoreOsmTileSource.SOURCE
        assertEquals("BalasoreOfflineMapnik", source.name())
        assertEquals(7, source.minimumZoomLevel)
        assertEquals(18, source.maximumZoomLevel)
        assertEquals(256, source.tileSizePixels)
    }

    @Test
    fun testLon2TileAndLat2TileCalculations() {
        // Balasore district center approximate coordinates: Lat 21.4934, Lon 86.9135
        val zoom = 14
        val tileX = BalasoreOsmOfflineTileProvider.lon2tile(86.9135, zoom)
        val tileY = BalasoreOsmOfflineTileProvider.lat2tile(21.4934, zoom)

        // At zoom 14, 2^14 = 16384 total tiles in each dimension
        assertTrue("TileX must be within valid range", tileX in 0..16383)
        assertTrue("TileY must be within valid range", tileY in 0..16383)

        // Specific bounding check for Balasore longitude (around ~86.9E is tile ~12140 at zoom 14)
        assertTrue("TileX around Balasore should be ~12140", tileX in 12100..12200)
    }

    @Test
    fun testComputeHotspotNeighborhoodTilesForPreCaching() {
        // Chandipur Sea Beach: 21.4446, 87.0149
        val zoomLevels = listOf(12, 13, 14, 15)
        val radius = 1 // 3x3 = 9 tiles per zoom level -> 36 tiles total
        val coords = BalasoreOsmOfflineTileProvider.computeHotspotNeighborhoodTiles(
            lat = 21.4446,
            lon = 87.0149,
            zoomLevels = zoomLevels,
            radiusTiles = radius
        )

        assertEquals("Should generate 9 tiles per zoom level across 4 zoom levels", 36, coords.size)
        assertTrue("All zoom levels 12..15 must be present", coords.map { it.zoom }.toSet() == setOf(12, 13, 14, 15))
        coords.forEach { coord ->
            assertTrue("Cache key must match zoom/x/y format", coord.cacheKey.startsWith("${coord.zoom}/"))
            assertTrue("MapTileIndex must be positive", coord.mapTileIndex > 0L)
        }
    }

    @Test
    fun testComputeBoundingBoxTilesForBalasoreTourismCorridor() {
        val boxTiles = BalasoreOsmOfflineTileProvider.computeBoundingBoxTiles(
            minLat = 21.40,
            maxLat = 21.55,
            minLon = 86.74,
            maxLon = 87.05,
            zoomLevels = listOf(11, 12)
        )
        assertTrue("Bounding box across Balasore corridor must yield multiple tiles", boxTiles.size >= 4)
        assertTrue("Should contain zoom 11 and 12 tiles", boxTiles.any { it.zoom == 11 } && boxTiles.any { it.zoom == 12 })
    }

    @Test
    fun testOsmPreCacheStatsHitRateCalculation() {
        val stats = OsmPreCacheStats(
            memoryCachedTiles = 144,
            diskCachedTiles = 320,
            totalCacheSizeMb = 18.6,
            cacheHitCount = 95,
            cacheMissCount = 5,
            preCachedHotspotIds = setOf("chandipur_beach", "panchalingeswar_temple")
        )
        assertEquals(95, stats.hitRatePercent)
        assertEquals(2, stats.preCachedHotspotIds.size)
    }

    @Test
    fun testBalasoreTouristLocationsAvailableForOfflineMapping() {
        assertTrue("Should have predefined Balasore tourist spots", BALASORE_TOURIST_LOCATIONS.size >= 8)

        val chandipur = BALASORE_TOURIST_LOCATIONS.find { it.id == "chandipur_beach" }
        assertNotNull("Chandipur beach must be present", chandipur)
        chandipur?.let {
            assertTrue("Chandipur latitude in Balasore range", it.latitude in 21.0..22.0)
            assertTrue("Chandipur longitude in Balasore range", it.longitude in 86.5..87.5)

            val x = BalasoreOsmOfflineTileProvider.lon2tile(it.longitude, 12)
            val y = BalasoreOsmOfflineTileProvider.lat2tile(it.latitude, 12)
            assertTrue("Tile coordinates must be non-negative", x > 0 && y > 0)
        }
    }

    @Test
    fun testOsmOfflineTourismMapSheetTypeEnum() {
        val sheetType = UniqueFeatureSheetType.OSM_OFFLINE_TOURISM_MAP
        assertNotNull(sheetType)
        assertEquals("OSM_OFFLINE_TOURISM_MAP", sheetType.name)
    }
}
