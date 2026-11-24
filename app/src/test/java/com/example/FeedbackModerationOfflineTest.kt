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
}
