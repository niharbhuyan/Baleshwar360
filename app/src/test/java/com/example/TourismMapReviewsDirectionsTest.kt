package com.example

import com.example.ui.components.OfflineAccessStatusHelper
import com.example.ui.features.map.DirectionsTravelMode
import com.example.ui.features.map.TourismMapDirectionsHelper
import com.example.ui.features.map.TourismMapReviewDisplayItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TourismMapReviewsDirectionsTest {

    @Test
    fun calculateDistanceKm_balasoreStationToChandipur_isReasonable() {
        val chandipurLat = 21.4660
        val chandipurLng = 87.0150

        val distanceKm = TourismMapDirectionsHelper.calculateDistanceKm(
            originLat = TourismMapDirectionsHelper.BALASORE_STATION_LAT,
            originLng = TourismMapDirectionsHelper.BALASORE_STATION_LNG,
            destLat = chandipurLat,
            destLng = chandipurLng
        )

        assertTrue("Expected ~10-14 km great-circle distance, got $distanceKm", distanceKm in 9.5..14.5)
    }

    @Test
    fun calculateDistanceKm_sameCoordinates_isZero() {
        val distanceKm = TourismMapDirectionsHelper.calculateDistanceKm(
            originLat = 21.4934,
            originLng = 86.9135,
            destLat = 21.4934,
            destLng = 86.9135
        )

        assertEquals(0.0, distanceKm, 0.0001)
    }

    @Test
    fun estimateMinutes_respectsTravelModeOrdering() {
        val distanceKm = 12.0
        val drivingMins = TourismMapDirectionsHelper.estimateMinutes(distanceKm, DirectionsTravelMode.DRIVING)
        val transitMins = TourismMapDirectionsHelper.estimateMinutes(distanceKm, DirectionsTravelMode.TRANSIT)
        val walkingMins = TourismMapDirectionsHelper.estimateMinutes(distanceKm, DirectionsTravelMode.WALKING)

        assertTrue("Driving ($drivingMins) should be faster than transit ($transitMins)", drivingMins < transitMins)
        assertTrue("Transit ($transitMins) should be faster than walking ($walkingMins)", transitMins < walkingMins)
        assertEquals(1, TourismMapDirectionsHelper.estimateMinutes(0.01, DirectionsTravelMode.DRIVING))
    }

    @Test
    fun buildDirectionsUris_containCoordinatesAndMode() {
        val lat = 21.466
        val lng = 87.015

        assertEquals(
            "google.navigation:q=$lat,$lng&mode=d",
            TourismMapDirectionsHelper.buildGoogleNavigationUri(lat, lng, DirectionsTravelMode.DRIVING)
        )
        assertEquals(
            "google.navigation:q=$lat,$lng&mode=r",
            TourismMapDirectionsHelper.buildGoogleNavigationUri(lat, lng, DirectionsTravelMode.TRANSIT)
        )
        assertEquals(
            "google.navigation:q=$lat,$lng&mode=w",
            TourismMapDirectionsHelper.buildGoogleNavigationUri(lat, lng, DirectionsTravelMode.WALKING)
        )

        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=driving",
            TourismMapDirectionsHelper.buildWebDirectionsUrl(lat, lng, DirectionsTravelMode.DRIVING)
        )
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=transit",
            TourismMapDirectionsHelper.buildWebDirectionsUrl(lat, lng, DirectionsTravelMode.TRANSIT)
        )
        assertEquals(
            "https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=walking",
            TourismMapDirectionsHelper.buildWebDirectionsUrl(lat, lng, DirectionsTravelMode.WALKING)
        )
        assertEquals(
            "geo:$lat,$lng?q=$lat,$lng(Chandipur%20Beach)",
            TourismMapDirectionsHelper.buildGeoDirectionsUri("Chandipur Beach", lat, lng)
        )
    }

    @Test
    fun summarizeReviews_emptyListUsesClampedBaseline() {
        val stats = TourismMapDirectionsHelper.summarizeReviews(emptyList(), baselineRating = 4.7)
        assertEquals(4.7, stats.averageRating, 0.001)
        assertEquals(0, stats.totalCount)
        assertEquals(0, stats.starCounts[5])
        assertEquals(0, stats.starCounts[1])
    }

    @Test
    fun summarizeReviews_computesAverageAndDistributionAccurately() {
        val reviews = listOf(
            TourismMapReviewDisplayItem(
                id = "1",
                authorName = "Aarav",
                authorLocality = "Balasore",
                rating = 5,
                comment = "Unforgettable vanishing sea view!",
                timestamp = 1000L
            ),
            TourismMapReviewDisplayItem(
                id = "2",
                authorName = "Meera",
                authorLocality = "Remuna",
                rating = 4,
                comment = "Peaceful sunrise walk along the coast.",
                timestamp = 2000L
            ),
            TourismMapReviewDisplayItem(
                id = "3",
                authorName = "Rohan",
                authorLocality = "Soro",
                rating = 5,
                comment = "Great seafood and clean shoreline.",
                timestamp = 3000L
            )
        )

        val stats = TourismMapDirectionsHelper.summarizeReviews(reviews, baselineRating = 4.2)

        assertEquals(4.7, stats.averageRating, 0.001)
        assertEquals(3, stats.totalCount)
        assertEquals(2, stats.starCounts[5])
        assertEquals(1, stats.starCounts[4])
        assertEquals(0, stats.starCounts[3])
        assertEquals(0, stats.starCounts[2])
        assertEquals(0, stats.starCounts[1])
    }

    @Test
    fun offlineStatusHelper_formatsRelativeAgeAndSectionIndicatorsCorrectly() {
        val now = 1_700_000_000_000L
        assertEquals("Just now", OfflineAccessStatusHelper.formatRelativeSyncAge(now - 20_000L, now))
        assertEquals("5m ago", OfflineAccessStatusHelper.formatRelativeSyncAge(now - 5 * 60_000L, now))
        assertEquals("2h ago", OfflineAccessStatusHelper.formatRelativeSyncAge(now - 2 * 3_600_000L, now))
        assertEquals("3d ago", OfflineAccessStatusHelper.formatRelativeSyncAge(now - 3 * 86_400_000L, now))
        assertEquals("Not synced yet", OfflineAccessStatusHelper.formatRelativeSyncAge(0L, now))

        val weatherOffline = OfflineAccessStatusHelper.buildSectionIndicator(
            mode = "WEATHER",
            isOnline = false,
            downloadedNewsCount = 6,
            downloadedHotspotsCount = 8,
            isWeatherDownloaded = true,
            weatherSummary = "30°C • Sunny • 3-Day Forecast",
            lastSyncMs = now - 10 * 60_000L,
            nowMs = now
        )
        assertTrue(weatherOffline.isDownloaded)
        assertFalse(weatherOffline.canRefreshNow)
        assertEquals("✓ Forecast Downloaded", weatherOffline.badgeLabelEn)
        assertTrue(weatherOffline.detailSummaryEn.contains("10m ago"))

        val newsOnline = OfflineAccessStatusHelper.buildSectionIndicator(
            mode = "NEWS",
            isOnline = true,
            downloadedNewsCount = 12,
            downloadedHotspotsCount = 8,
            isWeatherDownloaded = true,
            weatherSummary = "",
            lastSyncMs = now - 30_000L,
            nowMs = now
        )
        assertTrue(newsOnline.isDownloaded)
        assertTrue(newsOnline.canRefreshNow)
        assertEquals("✓ 12 Articles Offline", newsOnline.badgeLabelEn)

        val tourismEmpty = OfflineAccessStatusHelper.buildSectionIndicator(
            mode = "TOURISM",
            isOnline = true,
            downloadedNewsCount = 0,
            downloadedHotspotsCount = 0,
            isWeatherDownloaded = false,
            weatherSummary = "",
            lastSyncMs = now,
            nowMs = now
        )
        assertFalse(tourismEmpty.isDownloaded)
        assertTrue(tourismEmpty.canRefreshNow)
        assertEquals("Not Downloaded", tourismEmpty.badgeLabelEn)
    }
}
