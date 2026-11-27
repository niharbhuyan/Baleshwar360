package com.example

import com.example.data.config.BalasoreApiKeys
import com.example.data.remote.NewsApiOrgArticleDto
import com.example.data.remote.NewsApiOrgResponse
import com.example.data.remote.NewsApiOrgService
import com.example.data.remote.NewsApiOrgSourceDto
import com.example.data.repository.NewsApiOrgClient
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NewsApiOrgKeyIntegrationTest {

    private class FakeNewsApiOrgService(
        private val everythingResponse: NewsApiOrgResponse = NewsApiOrgResponse(status = "ok"),
        private val topHeadlinesResponse: NewsApiOrgResponse = NewsApiOrgResponse(status = "ok")
    ) : NewsApiOrgService {
        val capturedApiKeys = mutableListOf<String>()
        val capturedQueries = mutableListOf<String>()

        override suspend fun searchEverything(
            query: String,
            language: String,
            sortBy: String,
            pageSize: Int,
            apiKey: String
        ): NewsApiOrgResponse {
            capturedApiKeys.add(apiKey)
            capturedQueries.add(query)
            return everythingResponse
        }

        override suspend fun getTopHeadlines(
            country: String?,
            category: String?,
            query: String?,
            pageSize: Int,
            apiKey: String
        ): NewsApiOrgResponse {
            capturedApiKeys.add(apiKey)
            return topHeadlinesResponse
        }
    }

    @Test
    fun placeholderKeys_areSanitizedAndSkipNetworkCallsGracefully() = runTest {
        assertEquals("", BalasoreApiKeys.sanitize("YOUR_NEWS_API_KEY"))
        assertEquals("", BalasoreApiKeys.sanitize("NEWS_API_KEY_DEFAULT_VALUE"))
        assertEquals("", BalasoreApiKeys.sanitize("MY_PLACEHOLDER_KEY"))
        assertEquals("live_news_key_12345", BalasoreApiKeys.sanitize("  \"live_news_key_12345\"  "))

        val fakeService = FakeNewsApiOrgService()
        val client = NewsApiOrgClient(
            service = fakeService,
            apiKeyProvider = { "YOUR_NEWS_API_KEY" }
        )

        assertFalse(client.isConfigured())
        val daily = client.fetchBalasoreDailyNews().getOrThrow()
        assertTrue(daily.isEmpty())
        assertTrue(fakeService.capturedApiKeys.isEmpty())
    }

    @Test
    fun configuredNewsApiKey_fetchesAndMapsBalasoreDailyAndRoomEntities() = runTest {
        val sampleArticles = listOf(
            NewsApiOrgArticleDto(
                source = NewsApiOrgSourceDto(id = "the-hindu", name = "The Hindu"),
                author = "Balasore Bureau",
                title = "IMD issues cyclone watch for Chandipur and Balasore coast",
                description = "Heavy rain expected along Odisha coast.",
                url = "https://www.thehindu.com/news/balasore-cyclone-watch",
                urlToImage = "https://www.thehindu.com/img/balasore.jpg",
                publishedAt = "2026-11-27T05:30:00Z",
                content = "Full bulletin from Balasore district administration [+1200 chars]"
            ),
            NewsApiOrgArticleDto(
                source = NewsApiOrgSourceDto(name = "Removed"),
                title = "[Removed]",
                url = "https://removed.example.com"
            ),
            NewsApiOrgArticleDto(
                source = NewsApiOrgSourceDto(name = "Unsafe"),
                title = "Javascript link should be rejected safely",
                url = "javascript:alert(1)"
            )
        )

        val fakeService = FakeNewsApiOrgService(
            everythingResponse = NewsApiOrgResponse(
                status = "ok",
                totalResults = sampleArticles.size,
                articles = sampleArticles
            ),
            topHeadlinesResponse = NewsApiOrgResponse(
                status = "ok",
                totalResults = 1,
                articles = listOf(
                    NewsApiOrgArticleDto(
                        source = NewsApiOrgSourceDto(name = "PTI"),
                        title = "ISRO announces new coastal telemetry upgrade across India",
                        description = "Eastern seaboard tracking stations upgraded.",
                        url = "https://pti.example.org/isro-telemetry",
                        publishedAt = "2026-11-27T04:00:00Z"
                    )
                )
            )
        )

        val client = NewsApiOrgClient(
            service = fakeService,
            apiKeyProvider = { "test_valid_newsapi_key_999" }
        )

        assertTrue(client.isConfigured())

        val dailyItems = client.fetchBalasoreDailyNews().getOrThrow()
        assertEquals(1, dailyItems.size)
        assertEquals("Weather", dailyItems.first().category)
        assertEquals("The Hindu", dailyItems.first().source)
        assertTrue(fakeService.capturedApiKeys.contains("test_valid_newsapi_key_999"))

        val stableId = NewsApiOrgClient.computeStableArticleId(sampleArticles.first().title!!)
        val entities = client.fetchNewsArticleEntities(bookmarkedIds = setOf(stableId)).getOrThrow()
        assertEquals(1, entities.size)
        assertTrue(entities.first().isBookmarked)
        assertTrue(entities.first().isBreaking)
        assertEquals("https://www.thehindu.com/img/balasore.jpg", entities.first().imageUrl)

        val (multiSection, imageMap) = client.fetchMultiSectionNews().getOrThrow()
        assertTrue(multiSection.any { it.category == "Weather" })
        assertTrue(multiSection.any { it.category == "National News" })
        assertTrue(imageMap.values.contains("https://www.thehindu.com/img/balasore.jpg"))
    }
}
