package com.example

import com.example.data.model.AppLanguage
import com.example.data.model.FeedbackType
import com.example.data.repository.OpenWeatherRepository
import com.example.ui.components.BalasoreFeedbackPurpose
import com.example.ui.components.ChandipurTidalAnalyticsApiService
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BalasoreSuiteFeaturesUnitTest {

    @Test
    fun openWeatherThreeDayForecast_returnsThreeDaysForBalasoreRegion() = runBlocking {
        val repository = OpenWeatherRepository(context = null)
        val forecastResult = repository.fetchThreeDayForecast(apiKey = "")
        assertTrue(forecastResult.isSuccess)
        val days = forecastResult.getOrThrow()
        assertEquals(3, days.size)
        assertEquals("Today", days[0].dayName)
        assertEquals("Tomorrow", days[1].dayName)
        assertTrue(days.all { it.tempMax >= it.tempMin })
        assertTrue(days.all { it.hourlySlots.isNotEmpty() })
    }

    @Test
    fun chandipurTidalAnalyticsApiService_fetchesSevenDayRecessionPatterns() = runBlocking {
        val patterns = ChandipurTidalAnalyticsApiService.fetchNextSevenDaysTidalPatterns(
            forceRefresh = true,
            simulatedDelayMs = 0L
        )
        assertEquals(7, patterns.size)
        assertEquals("Today", patterns[0].dayLabelEn)
        assertEquals("ଆଜି", patterns[0].dayLabelOr)
        assertTrue(patterns.all { it.maxRecessionKm in 3.5..5.5 })
        assertTrue(patterns.all { it.safeWalkWindowMins >= 120 })
    }

    @Test
    fun userFeedbackPurposes_supportNewsFeedbackHotspotSuggestionAndIssueReporting() {
        val purposes = BalasoreFeedbackPurpose.values().toList()
        assertEquals(3, purposes.size)

        val newsPurpose = BalasoreFeedbackPurpose.NEWS_ARTICLE
        assertEquals("NEWS_FEEDBACK", newsPurpose.targetTypeKey)
        assertEquals(FeedbackType.CONTENT_CORRECTION, newsPurpose.defaultFeedbackType)

        val hotspotPurpose = BalasoreFeedbackPurpose.SUGGEST_HOTSPOT
        assertEquals("HOTSPOT_SUGGESTION", hotspotPurpose.targetTypeKey)
        assertEquals(FeedbackType.SUGGESTION, hotspotPurpose.defaultFeedbackType)

        val issuePurpose = BalasoreFeedbackPurpose.REPORT_ISSUE
        assertEquals("ISSUE_REPORT", issuePurpose.targetTypeKey)
        assertEquals(FeedbackType.BUG_REPORT, issuePurpose.defaultFeedbackType)
        assertTrue(FeedbackType.BUG_REPORT.getDisplayName(AppLanguage.ODIA).isNotBlank())
    }

    @Test
    fun localBusTrackerSimulatedApi_fetchesLiveArrivalsAndDeparturesAcrossBalasoreRegions() = runBlocking {
        val initialTelemetry = com.example.ui.components.BalasoreLocalBusApiService.fetchLiveBusTelemetry(
            tickCycle = 0,
            simulateNetworkDelayMs = 0L
        )
        assertEquals(8, initialTelemetry.totalActiveFleet)
        assertTrue(initialTelemetry.onTimePercentage in 70..100)
        assertTrue(initialTelemetry.routes.all { it.departureTime.isNotBlank() && it.scheduledArrivalTime.isNotBlank() })
        assertTrue(initialTelemetry.routes.all { it.stopsSequence.isNotEmpty() && it.connectingRegions.isNotEmpty() })

        val nextTickTelemetry = com.example.ui.components.BalasoreLocalBusApiService.fetchLiveBusTelemetry(
            tickCycle = 2,
            simulateNetworkDelayMs = 0L
        )
        assertEquals(8, nextTickTelemetry.routes.size)
        assertTrue(nextTickTelemetry.routes.any { it.destination.contains("Chandipur") })
        assertTrue(nextTickTelemetry.routes.any { it.destination.contains("Remuna") })
        assertTrue(nextTickTelemetry.routes.any { it.destination.contains("Nilagiri") })

        val sampleRoute = nextTickTelemetry.routes.first()
        val stopEta = com.example.ui.components.BalasoreLocalBusApiService.computeArrivalMinutesForStop(
            bus = sampleRoute,
            stopName = "Sahadevkhunta"
        )
        assertTrue("Computed stop ETA should be positive", stopEta >= 1)
    }

    @Test
    fun communityQrScanner_resolvesTourismAndEventPostersToAppDetails() {
        val eventScan = com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(
            "balasore360://poster/baleswar_mahotsav_2026"
        )
        assertTrue(eventScan.isOfficialBalasorePamphlet)
        assertEquals("EVENT_POSTER", eventScan.matchedAppTargetType)
        assertEquals("baleswar_mahotsav_2026", eventScan.matchedPamphlet?.id)
        assertTrue(!eventScan.matchedEventDate.isNullOrBlank())
        assertEquals("fakir_mohan_smruti", eventScan.matchedHotspot?.id)

        val tourismScan = com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(
            "balasore360://pamphlet/chandipur_beach_safety"
        )
        assertTrue(tourismScan.isOfficialBalasorePamphlet)
        assertEquals("HOTSPOT", tourismScan.matchedAppTargetType)
        assertEquals("chandipur_beach", tourismScan.matchedHotspot?.id)

        val directHotspotScan = com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(
            "balasore360://hotspot/emami_jagannath"
        )
        assertTrue(directHotspotScan.isOfficialBalasorePamphlet)
        assertEquals("emami_jagannath", directHotspotScan.matchedHotspot?.id)
    }

    @Test
    fun interactiveMapDataSource_providesClickableNewsLocationsWeatherAlertZonesAndTouristHotspots() {
        val newsLocations = com.example.data.maps.BalasoreInteractiveMapDataSource.BALASORE_GEO_NEWS_LOCATIONS
        val weatherZones = com.example.data.maps.BalasoreInteractiveMapDataSource.BALASORE_WEATHER_ALERT_ZONES
        val touristHotspots = com.example.data.maps.BalasoreInteractiveMapDataSource.getTouristHotspots()

        assertTrue("Expected at least 6 geotagged news locations", newsLocations.size >= 6)
        assertTrue("Expected at least 4 weather alert zones", weatherZones.size >= 4)
        assertTrue("Expected at least 8 tourist hotspots", touristHotspots.size >= 8)

        // Verify news locations have full bilingual titles, summaries, and coordinates in Balasore district
        assertTrue(newsLocations.all {
            it.headlineEn.isNotBlank() &&
                it.headlineOr.isNotBlank() &&
                it.summaryEn.isNotBlank() &&
                it.latitude in 21.0..22.0 &&
                it.longitude in 86.4..87.5
        })

        // Verify weather alert zones have valid radius, IMD advisory, and nearest shelter details
        assertTrue(weatherZones.all {
            it.radiusMeters >= 3000.0 &&
                it.imdAdvisoryEn.isNotBlank() &&
                it.imdAdvisoryOr.isNotBlank() &&
                it.nearestShelterName.isNotBlank() &&
                it.affectedAreas.isNotEmpty()
        })

        // Verify geospatial point-in-zone and nearest lookup helpers
        val chandipurZone = weatherZones.first { it.id == "wx_zone_chandipur_surge" }
        assertTrue(
            com.example.data.maps.BalasoreInteractiveMapDataSource.isPointInsideAlertZone(
                chandipurZone.centerLat,
                chandipurZone.centerLng,
                chandipurZone
            )
        )

        val nearestNews = com.example.data.maps.BalasoreInteractiveMapDataSource.findNearestNewsLocation(
            21.4695,
            87.0185
        )
        assertEquals("news_loc_chandipur_seawall", nearestNews?.id)

        val nearestAlert = com.example.data.maps.BalasoreInteractiveMapDataSource.findNearestWeatherAlertZone(
            21.4695,
            87.0185
        )
        assertEquals("wx_zone_chandipur_surge", nearestAlert?.id)
    }

    @Test
    fun publicAmenitiesMapLayer_providesColorCodedDrinkingWaterAndPublicToiletMaintenanceStatuses() {
        val amenities = com.example.ui.screens.BALASORE_PUBLIC_AMENITIES_LAYER
        assertTrue("Expected at least 6 public amenities", amenities.size >= 6)
        assertTrue(amenities.any { it.amenityType == "DRINKING_WATER" })
        assertTrue(amenities.any { it.amenityType == "PUBLIC_TOILET" })
        assertTrue(amenities.any { it.maintenanceStatus == "VERIFIED_OPERATIONAL" })
        assertTrue(amenities.any { it.maintenanceStatus == "SCHEDULED_MAINTENANCE" })
        assertTrue(amenities.any { it.maintenanceStatus == "NEEDS_REPAIR" })
    }

    @Test
    fun userAuthProfilePreferencesAndFavoriteNews_roundTripsCustomNewsTopicsAndWeatherThresholds() {
        val customPrefs = com.example.data.model.UserWeatherPreferences(
            userId = "usr_balasore_01",
            notifyCyclone = true,
            notifyHighTide = true,
            notifyBreakingNews = true,
            highTideAlertHoursBefore = 3,
            minTempThreshold = 14,
            maxTempThreshold = 40,
            preferredNewsCategories = listOf("Weather", "Defense", "Tourism")
        )
        val firestoreMap = customPrefs.toFirestoreMap()
        val restored = com.example.data.model.UserWeatherPreferences.fromFirestoreMap(firestoreMap)

        assertEquals("usr_balasore_01", restored.userId)
        assertEquals(3, restored.highTideAlertHoursBefore)
        assertEquals(40, restored.maxTempThreshold)
        assertEquals(listOf("Weather", "Defense", "Tourism"), restored.preferredNewsCategories)

        val favNews = com.example.data.model.FavoriteNewsCloudItem(
            newsId = "news_101",
            userId = "usr_balasore_01",
            title = "Chandipur Marine Drive Upgrade Approved",
            source = "Balasore Express",
            category = "Development"
        )
        val restoredNews = com.example.data.model.FavoriteNewsCloudItem.fromFirestoreMap(favNews.toFirestoreMap())
        assertEquals("news_101", restoredNews.newsId)
        assertEquals("Development", restoredNews.category)
    }

    @Test
    fun tourismUgcModerationSystem_supportsPhotoUploadReviewsTipsAndModerationWorkflow() {
        val submitted = com.example.ui.components.TourismUgcModerationRepository.submitUgc(
            spotId = "1",
            spotName = "Chandipur-on-Sea",
            userId = "usr_tester_99",
            authorName = "Rakesh Nayak",
            rating = 5,
            reviewText = "Beautiful low-tide walk across the shimmering sandbars.",
            travelTip = "Arrive 2 hours before peak low tide for best horseshoe crab sightings.",
            tipCategory = "Safety & Tide Tip",
            photoUris = listOf("content://balasore360/tourism_photos/chandipur_low_tide_sunset.jpg"),
            photoCaption = "Dawn low tide at Chandipur",
            visitSeason = "Winter"
        )

        assertEquals(com.example.ui.features.admin.ModerationStatus.PENDING, submitted.moderationStatus)
        assertEquals(1, submitted.photoUris.size)
        assertTrue(com.example.ui.components.TourismUgcModerationRepository.getPendingAndFlaggedQueue().any { it.id == submitted.id })

        // Approve the submission via moderation system
        val approved = com.example.ui.components.TourismUgcModerationRepository.moderateSubmission(
            itemId = submitted.id,
            newStatus = com.example.ui.features.admin.ModerationStatus.APPROVED,
            moderatorNote = "Verified authentic visitor photo & coastal tip"
        )
        assertEquals(com.example.ui.features.admin.ModerationStatus.APPROVED, approved?.moderationStatus)
        assertTrue(com.example.ui.components.TourismUgcModerationRepository.getApprovedForSpot("1").any { it.id == submitted.id })
    }

    @Test
    fun contentBasedRecommendationEngine_suggestsPersonalizedNewsWeatherAlertsAndTourismHotspots() {
        val coastalProfile = com.example.data.recommendation.UserBehaviorProfile(
            viewedArticleIds = setOf("1", "2"),
            savedArticleIds = setOf("1"),
            viewedLocationIds = setOf("1"),
            savedLocationIds = setOf("1"), // Chandipur-on-Sea
            visitedLocationIds = setOf("1"),
            categoryAffinities = mapOf("Beach" to 8, "Weather" to 6, "Tourism" to 5),
            localityAffinities = mapOf("Chandipur" to 9, "Balasore Town" to 3),
            totalInteractions = 15
        )

        val recommendedSpots = com.example.data.recommendation.BalasoreRecommendationEngine.getRecommendedHotspots(
            profileOverride = coastalProfile,
            limit = 4
        )
        assertEquals(4, recommendedSpots.size)
        assertTrue(recommendedSpots.all { it.matchScorePercent in 78..99 })

        val recommendedNews = com.example.data.recommendation.BalasoreRecommendationEngine.getRecommendedArticles(
            profileOverride = coastalProfile,
            limit = 4
        )
        assertEquals(4, recommendedNews.size)
        assertTrue(recommendedNews.all { it.matchScorePercent in 80..99 })

        val recommendedWeatherAlerts = com.example.data.recommendation.BalasoreRecommendationEngine.getRecommendedWeatherAlerts(
            profileOverride = coastalProfile,
            limit = 4
        )
        assertEquals(4, recommendedWeatherAlerts.size)
        // Chandipur coastal tide alert should rank at the top for a coastal user profile
        assertEquals("rec_wx_chandipur_tide", recommendedWeatherAlerts.first().id)
        assertTrue(recommendedWeatherAlerts.first().matchScorePercent >= 85)
    }

    @Test
    fun offlineAccessEntities_preserveNewsWeatherAndTourismSpotDetailsForOfflineViewing() {
        val newsEntity = com.example.data.local.NewsArticleEntity(
            id = 101L,
            title = "Subarnarekha Flood Embankment Strengthened Near Jaleswar",
            summary = "District administration completes monsoon embankment reinforcement.",
            content = "Full offline article text available without internet connection.",
            category = "Civic",
            source = "Baleshwar Desk",
            publishedAt = "1h ago",
            isBreaking = true,
            isBookmarked = true
        )
        assertTrue(newsEntity.isBookmarked)
        assertTrue(newsEntity.content.contains("offline"))

        val weatherEntity = com.example.data.local.WeatherCacheEntity(
            id = 1,
            temperature = 31.0,
            apparentTemperature = 33.5,
            weatherCode = 2,
            weatherDescription = "Partly Cloudy • Coastal Breeze",
            windSpeed = 18.5,
            windGusts = 26.5,
            humidity = 74,
            alertLevel = "ADVISORY",
            alertTitle = "Balasore Coastal Tide & Monsoon Watch",
            alertMessage = "Chandipur seabed walk safe during low tide window.",
            tideState = "LOW_TIDE",
            tideDescription = "Chandipur Sea Recession Active (~3.8 km exposed seabed)",
            sunrise = "05:32 AM",
            sunset = "06:14 PM",
            uvIndex = 6.5,
            maxTemp = 33.5,
            minTemp = 25.0
        )
        assertEquals("LOW_TIDE", weatherEntity.tideState)
        assertEquals(31.0, weatherEntity.temperature, 0.01)

        val hotspotEntity = com.example.data.local.HotspotEntity(
            id = "1",
            name = "Chandipur-on-Sea",
            odiaName = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
            category = "Beach",
            shortDescription = "Famous vanishing sea beach where water recedes up to 5 km.",
            fullDescription = "Detailed offline guide with tide timings, DRDO proximity info, and casuarina trails.",
            highlights = "Beach • Chandipur • ★ 4.8",
            distanceKmFromBls = 16,
            bestTimeToVisit = "Oct - Mar",
            timings = "05:30 AM - 08:30 PM",
            entryFee = "Free",
            specialty = "Vanishing Tide",
            localTip = "Check low-tide table before walking out on the seabed.",
            isFavorite = true
        )
        assertEquals("Chandipur-on-Sea", hotspotEntity.name)
        assertTrue(hotspotEntity.isFavorite)
    }

    @Test
    fun globalThemeToggle_switchesBetweenLightDarkAndHighContrastModes() {
        val viewModel = com.example.ui.viewmodel.BalasoreViewModel()
        assertEquals(com.example.ui.viewmodel.ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)

        // Direct 1-tap Dark Mode toggle from system light
        viewModel.toggleDarkMode(isSystemCurrentlyDark = false)
        assertEquals(com.example.ui.viewmodel.ThemeMode.DARK, viewModel.uiState.value.themeMode)

        // Toggling again switches back to LIGHT
        viewModel.toggleDarkMode(isSystemCurrentlyDark = false)
        assertEquals(com.example.ui.viewmodel.ThemeMode.LIGHT, viewModel.uiState.value.themeMode)

        // Cycle through LIGHT -> DARK -> HIGH_CONTRAST_DARK -> SYSTEM
        viewModel.cycleThemeMode()
        assertEquals(com.example.ui.viewmodel.ThemeMode.DARK, viewModel.uiState.value.themeMode)
        viewModel.cycleThemeMode()
        assertEquals(com.example.ui.viewmodel.ThemeMode.HIGH_CONTRAST_DARK, viewModel.uiState.value.themeMode)
        viewModel.cycleThemeMode()
        assertEquals(com.example.ui.viewmodel.ThemeMode.SYSTEM, viewModel.uiState.value.themeMode)
    }
}




