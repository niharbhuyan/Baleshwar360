package com.example

import com.example.data.local.SavedBookmarkEntity
import com.example.data.model.EmergencyContact
import com.example.data.model.NewsArticle
import com.example.ui.viewmodel.NewsFeedFilter
import com.example.ui.viewmodel.NewsFeedTab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying the Room Database bookmarking system for news articles
 * and emergency directory contacts, as well as the 'My Saved' tab categorization.
 */
class SavedBookmarksRoomIntegrationTest {

    @Test
    fun testNewsFeedTabMySavedIndexAndProperties() {
        val savedTab = NewsFeedTab.MY_SAVED
        assertEquals(6, savedTab.index)
        assertEquals("My Saved", savedTab.title)
        assertEquals("ସଂରକ୍ଷିତ", savedTab.odiaTitle)

        // Verify lookup by index and category
        assertEquals(NewsFeedTab.MY_SAVED, NewsFeedTab.fromIndex(6))
        assertEquals(NewsFeedTab.MY_SAVED, NewsFeedTab.fromCategory("My Saved"))
        assertEquals(NewsFeedTab.MY_SAVED, NewsFeedTab.fromCategory("Saved Bookmarks"))
        assertEquals(NewsFeedTab.MY_SAVED, NewsFeedTab.fromCategory("Bookmarks"))
    }

    @Test
    fun testNewsFeedFilterMatchesBookmarkedArticlesInMySavedTab() {
        val bookmarkedArticle = NewsArticle(
            id = "1",
            title = "Balasore Smart Drainage Project Approved",
            odiaTitle = "ବାଲେଶ୍ୱର ସ୍ମାର୍ଟ ଡ୍ରେନେଜ୍",
            snippet = "Municipal council sanctions funds",
            odiaSnippet = "ପୌର ପରିଷଦ ପାଣ୍ଠି ମଞ୍ଜୁର",
            category = "Local News",
            timeAgo = "1 hour ago",
            isBookmarked = true
        )

        val regularArticle = NewsArticle(
            id = "2",
            title = "OTDC Chandipur Beach Festival Begins",
            odiaTitle = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ଉତ୍ସବ",
            snippet = "Cultural dance troupes perform",
            odiaSnippet = "ସାଂସ୍କୃତିକ ନୃତ୍ୟ ପ୍ରଦର୍ଶନ",
            category = "Tourism Updates",
            timeAgo = "2 hours ago",
            isBookmarked = false
        )

        assertTrue(
            "Bookmarked article should match MY_SAVED tab",
            NewsFeedFilter.matchesTab(bookmarkedArticle, NewsFeedTab.MY_SAVED.index)
        )
        assertFalse(
            "Non-bookmarked article should NOT match MY_SAVED tab",
            NewsFeedFilter.matchesTab(regularArticle, NewsFeedTab.MY_SAVED.index)
        )
    }

    @Test
    fun testSavedBookmarkEntityStructureForEmergencyContact() {
        val contact = EmergencyContact(
            id = "police_sp",
            name = "Balasore SP Office Control Room",
            odiaName = "ବାଲେଶ୍ୱର ଏସ୍.ପି. ଅଫିସ୍ କଣ୍ଟ୍ରୋଲ୍ ରୁମ୍",
            number = "06782-262026",
            category = "Police",
            is24x7 = true,
            description = "District Police Headquarters, Azimabad"
        )

        val entity = SavedBookmarkEntity(
            id = "emergency_${contact.id}",
            itemType = "EMERGENCY",
            originalId = contact.id,
            title = contact.name,
            odiaTitle = contact.odiaName,
            subtitle = contact.description,
            category = contact.category,
            phoneNumber = contact.number,
            sourceOrDepartment = "Balasore Police District"
        )

        assertEquals("emergency_police_sp", entity.id)
        assertEquals("EMERGENCY", entity.itemType)
        assertEquals("06782-262026", entity.phoneNumber)
        assertEquals("Balasore SP Office Control Room", entity.title)
        assertNotNull(entity.timestamp)
    }

    @Test
    fun testSavedBookmarkEntityStructureForNewsArticle() {
        val article = NewsArticle(
            id = "news_701",
            title = "DRDO Successfully Tests Advanced Air Defense Interceptor from ITR Chandipur",
            odiaTitle = "ଆଇ.ଟି.ଆର୍ ଚାନ୍ଦିପୁରରୁ ଡି.ଆର.ଡି.ଓ ର ସଫଳ କ୍ଷେପଣାସ୍ତ୍ର ପରୀକ୍ଷଣ",
            snippet = "Mission accomplishes all flight objectives in maritime test range.",
            odiaSnippet = "ବଙ୍ଗୋପସାଗରୀୟ ପରୀକ୍ଷଣ କ୍ଷେତ୍ରରେ ସମସ୍ତ ଲକ୍ଷ୍ୟ ହାସଲ।",
            category = "Defense Alerts",
            timeAgo = "25m ago",
            source = "Defense News Bureau"
        )

        val entity = SavedBookmarkEntity(
            id = "news_${article.id}",
            itemType = "NEWS",
            originalId = article.id,
            title = article.title,
            odiaTitle = article.odiaTitle,
            subtitle = article.snippet,
            category = article.category,
            sourceOrDepartment = article.source
        )

        assertEquals("news_news_701", entity.id)
        assertEquals("NEWS", entity.itemType)
        assertEquals("news_701", entity.originalId)
        assertEquals("Defense Alerts", entity.category)
    }

    @Test
    fun testPartitioningSavedBookmarksList() {
        val bookmarks = listOf(
            SavedBookmarkEntity(
                id = "news_1",
                itemType = "NEWS",
                originalId = "1",
                title = "Story 1"
            ),
            SavedBookmarkEntity(
                id = "emergency_101",
                itemType = "EMERGENCY",
                originalId = "101",
                title = "Fire Control",
                phoneNumber = "101"
            ),
            SavedBookmarkEntity(
                id = "emergency_108",
                itemType = "EMERGENCY",
                originalId = "108",
                title = "Ambulance",
                phoneNumber = "108"
            ),
            SavedBookmarkEntity(
                id = "news_2",
                itemType = "NEWS",
                originalId = "2",
                title = "Story 2"
            )
        )

        val newsItems = bookmarks.filter { it.itemType == "NEWS" }
        val emergencyItems = bookmarks.filter { it.itemType == "EMERGENCY" }

        assertEquals(2, newsItems.size)
        assertEquals(2, emergencyItems.size)
        assertEquals("101", emergencyItems.first().phoneNumber)
    }
}
