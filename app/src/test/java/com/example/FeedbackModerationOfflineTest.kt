package com.example

import com.example.data.local.NewsArticleEntity
import com.example.data.model.FeedbackReport
import com.example.data.model.FeedbackType
import com.example.ui.components.OfflineAccessStatusHelper
import com.example.ui.features.admin.ContentModerationRepository
import com.example.ui.features.admin.ModerationStatus
import com.example.ui.features.map.TourismMapDirectionsHelper
import com.example.ui.features.map.TourismMapReviewDisplayItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedbackModerationOfflineTest {

    @Test
    fun offlineStorageFootprint_andSmartArticleRetention_preserveBookmarksAndCapQuota() {
        val footprintKb = OfflineAccessStatusHelper.calculateOfflineStorageFootprintKb(
            newsCount = 5,
            hotspotsCount = 4,
            isWeatherCached = true
        )
        // 5*28 + 4*42 + 36 = 140 + 168 + 36 = 344 KB
        assertEquals(344, footprintKb)

        val articles = (1..6).map { idx ->
            NewsArticleEntity(
                id = idx.toLong(),
                title = "Balasore News $idx",
                summary = "Snippet $idx",
                content = "Full content $idx",
                category = "Civic",
                source = "Baleshwar 360 Desk",
                publishedAt = "Today",
                timestamp = idx * 1000L,
                isBreaking = false,
                isBookmarked = idx == 1 || idx == 2 // oldest 2 are bookmarked
            )
        }

        val retained = OfflineAccessStatusHelper.selectArticlesForRetention(
            articles = articles,
            maxNonBookmarkedArticles = 2
        )

        // Should keep both bookmarked articles (1L, 2L) plus the 2 newest non-bookmarked (6L, 5L)
        assertEquals(4, retained.size)
        val retainedIds = retained.map { it.id }.toSet()
        assertTrue(retainedIds.contains(1L))
        assertTrue(retainedIds.contains(2L))
        assertTrue(retainedIds.contains(5L))
        assertTrue(retainedIds.contains(6L))
        assertFalse(retainedIds.contains(3L))
        assertFalse(retainedIds.contains(4L))
    }

    @Test
    fun feedbackReport_roundTripSerialization_andStarRatingSummary_areAccurate() {
        val report = FeedbackReport(
            id = "fb_test_101",
            type = FeedbackType.CONTENT_CORRECTION,
            category = "App Features & Feedback",
            title = "Great coverage of Subarnarekha flood embankment",
            description = "[News Article: Subarnarekha Flood Embankment Upgraded] Very helpful update for Jaleswar and Baliapal commuters.",
            userName = "Subrat Mohanty",
            contactInfo = "subrat@example.com",
            locality = "Sahadevkhunta",
            rating = 5
        )

        val map = report.toFirestoreMap()
        val restored = FeedbackReport.fromFirestoreMap(map)

        assertEquals("fb_test_101", restored.id)
        assertEquals(5, restored.rating)
        assertEquals("App Features & Feedback", restored.category)
        assertEquals("Subrat Mohanty", restored.userName)
        assertEquals("Sahadevkhunta", restored.locality)

        val summary = TourismMapDirectionsHelper.summarizeReviews(
            reviews = listOf(
                TourismMapReviewDisplayItem(
                    id = "c1",
                    authorName = "Ananya",
                    authorLocality = "Chandipur",
                    rating = 5,
                    comment = "Clean beach and clear low-tide timetable.",
                    timestamp = 2000L
                ),
                TourismMapReviewDisplayItem(
                    id = "c2",
                    authorName = "Debasis",
                    authorLocality = "Remuna",
                    rating = 4,
                    comment = "Good parking near DRDO square.",
                    timestamp = 3000L
                )
            ),
            baselineRating = 4.6
        )

        assertEquals(2, summary.totalCount)
        assertEquals(4.5, summary.averageRating, 0.01)
    }

    @Test
    fun contentModeration_evaluateAutoModerationRule_flagsSpamAndSafetyRumors_approvesCleanReviews() {
        val (cleanStatus, cleanReasons) = ContentModerationRepository.evaluateAutoModerationRule(
            comment = "Visited Panchalingeswar temple at 6 AM. Clean steps and peaceful hill stream.",
            rating = 5
        )
        assertEquals(ModerationStatus.APPROVED, cleanStatus)
        assertTrue(cleanReasons.isEmpty())

        val (spamStatus, spamReasons) = ContentModerationRepository.evaluateAutoModerationRule(
            comment = "Buy cheap unregulated beach tour now at https://spam-link.example.com with promo code!",
            rating = 5
        )
        assertEquals(ModerationStatus.FLAGGED, spamStatus)
        assertTrue(spamReasons.contains("Spam or Promotional Link"))

        val (rumorStatus, rumorReasons) = ContentModerationRepository.evaluateAutoModerationRule(
            comment = "Fake rumour: All bridges in Rajghat are closed today!",
            rating = 1
        )
        assertEquals(ModerationStatus.FLAGGED, rumorStatus)
        assertTrue(rumorReasons.contains("Misleading Local / Safety Information"))
    }

    @Test
    fun socialSharingBuilders_generateValidFacebookTwitterWhatsAppUrls_andTourismHotspotPayload() {
        val (subject, body, url) = com.example.util.ShareHelper.buildTourismHotspotShareText(
            spotId = "chandipur_beach",
            name = "Chandipur Sea Beach",
            odiaName = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
            category = "Beach",
            location = "16 km from Balasore",
            rating = 4.8,
            description = "Unique vanishing sea receding up to 5 km during low tide.",
            operatingHours = "Open 24 Hours"
        )
        assertTrue(subject.contains("Chandipur Sea Beach"))
        assertTrue(body.contains("4.8"))
        assertTrue(body.contains("https://baleshwar360.app/tourism/chandipur_beach"))
        assertEquals("https://baleshwar360.app/tourism/chandipur_beach", url)

        val twitterUrl = com.example.util.ShareHelper.buildTwitterShareUrl("Chandipur high tide alert!", url)
        assertTrue(twitterUrl.startsWith("https://twitter.com/intent/tweet?text="))
        assertTrue(twitterUrl.contains("url=https%3A%2F%2Fbaleshwar360.app%2Ftourism%2Fchandipur_beach"))

        val facebookUrl = com.example.util.ShareHelper.buildFacebookShareUrl("Explore Chandipur Beach", url)
        assertTrue(facebookUrl.startsWith("https://www.facebook.com/sharer/sharer.php?u="))
        assertTrue(facebookUrl.contains("quote=Explore%20Chandipur%20Beach"))

        val whatsappUrl = com.example.util.ShareHelper.buildWhatsAppShareUrl("Breaking Balasore news!")
        assertTrue(whatsappUrl.startsWith("https://api.whatsapp.com/send?text="))
        assertTrue(whatsappUrl.contains("Breaking%20Balasore%20news"))
    }

    @Test
    fun userWeatherPreferences_roundTripSerialization_preservesSeverityFrequencyWindowAndTopics() {
        val prefs = com.example.data.model.UserWeatherPreferences(
            notifyCyclone = true,
            notifyHighTide = true,
            notifyFloodAlerts = true,
            maxTempThreshold = 40,
            highTideAlertHoursBefore = 2,
            preferredNewsCategories = listOf("Events", "Traffic", "Civic", "Weather"),
            minWeatherAlertSeverity = "SEVERE",
            notificationFrequency = "DAILY_SUMMARY",
            preferredNotificationWindow = "QUIET_NIGHT_10PM_7AM"
        )
        val firestoreMap = prefs.toFirestoreMap()
        val restored = com.example.data.model.UserWeatherPreferences.fromFirestoreMap(firestoreMap)

        assertEquals("SEVERE", restored.minWeatherAlertSeverity)
        assertEquals("DAILY_SUMMARY", restored.notificationFrequency)
        assertEquals("QUIET_NIGHT_10PM_7AM", restored.preferredNotificationWindow)
        assertTrue(restored.preferredNewsCategories.contains("Events"))
        assertTrue(restored.preferredNewsCategories.contains("Traffic"))
        assertEquals(40, restored.maxTempThreshold)
        assertEquals(2, restored.highTideAlertHoursBefore)

        // Verify FcmManager severity threshold filtering
        assertTrue(com.example.data.fcm.FcmManager.shouldDeliverWeatherAlert("CRITICAL", "SEVERE"))
        assertTrue(com.example.data.fcm.FcmManager.shouldDeliverWeatherAlert("SEVERE", "SEVERE"))
        assertFalse(com.example.data.fcm.FcmManager.shouldDeliverWeatherAlert("ADVISORY", "SEVERE"))

        // Verify FcmManager preferred time window filtering
        assertTrue(com.example.data.fcm.FcmManager.isWithinPreferredDeliveryWindow("MORNING_8AM", 8))
        assertFalse(com.example.data.fcm.FcmManager.isWithinPreferredDeliveryWindow("MORNING_8AM", 20))
        assertFalse(com.example.data.fcm.FcmManager.isWithinPreferredDeliveryWindow("QUIET_NIGHT_10PM_7AM", 23))
        assertTrue(com.example.data.fcm.FcmManager.isWithinPreferredDeliveryWindow("QUIET_NIGHT_10PM_7AM", 14))
    }

    @Test
    fun balasoreOpenWeatherForecast_andDistrictCoords_provideCompleteFiveDayAndCoastalTelemetry() {
        val repo = com.example.data.repository.OpenWeatherRepository()
        assertEquals(21.4934, com.example.data.repository.OpenWeatherRepository.BALASORE_LAT, 0.001)
        assertEquals(86.9135, com.example.data.repository.OpenWeatherRepository.BALASORE_LON, 0.001)

        val currentFallback = repo.getCuratedBalasoreWeatherFallback()
        assertEquals("Balasore", currentFallback.name)
        assertTrue((currentFallback.main?.temp ?: 0.0) in 15.0..48.0)
        assertTrue((currentFallback.main?.humidity ?: 0) in 20..100)
        assertFalse(currentFallback.weather.isEmpty())

        val fiveDay = repo.getCuratedBalasoreFiveDayForecastFallback()
        assertEquals(5, fiveDay.size)
        assertTrue(fiveDay.all { it.tempMax >= it.tempMin })
        assertTrue(fiveDay.first().dayName.isNotBlank())
        assertTrue(fiveDay.first().odiaDayName.isNotBlank())
    }

    @Test
    fun foregroundVoiceHelpTrigger_matchesHelpMeAndOdiaPhrases_ignoresNormalSpeech() {
        // Positive matches for 'Help me' and Odia / 112 emergency commands
        assertTrue(com.example.data.emergency.ForegroundVoiceHelpTrigger.isHelpMeTriggerPhrase("Help me"))
        assertTrue(com.example.data.emergency.ForegroundVoiceHelpTrigger.isHelpMeTriggerPhrase("please help me!"))
        assertTrue(com.example.data.emergency.ForegroundVoiceHelpTrigger.isHelpMeTriggerPhrase("Someone HELP ME right now"))
        assertTrue(com.example.data.emergency.ForegroundVoiceHelpTrigger.isHelpMeTriggerPhrase("Call 112 immediately"))
        assertTrue(com.example.data.emergency.ForegroundVoiceHelpTrigger.isHelpMeTriggerPhrase("ମୋତେ ସାହାଯ୍ୟ କର"))

        // Negative cases to prevent accidental emergency dials during ordinary speech
        assertFalse(com.example.data.emergency.ForegroundVoiceHelpTrigger.isHelpMeTriggerPhrase("This weather widget is very helpful"))
        assertFalse(com.example.data.emergency.ForegroundVoiceHelpTrigger.isHelpMeTriggerPhrase("Show Balasore tourism helpline"))
        assertFalse(com.example.data.emergency.ForegroundVoiceHelpTrigger.isHelpMeTriggerPhrase("Hello Balasore news"))
        assertFalse(com.example.data.emergency.ForegroundVoiceHelpTrigger.isHelpMeTriggerPhrase(""))
    }
}




