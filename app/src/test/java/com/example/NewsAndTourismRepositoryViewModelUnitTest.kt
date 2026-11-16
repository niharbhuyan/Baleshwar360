package com.example

import com.example.data.local.CacheMetadataDao
import com.example.data.local.CacheSyncMetadataEntity
import com.example.data.local.HotspotDao
import com.example.data.local.HotspotEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.local.NewsDao
import com.example.data.remote.BalasoreApiService
import com.example.data.remote.BalasoreNewsResponse
import com.example.data.remote.NewsArticleDto
import com.example.data.repository.INewsRepository
import com.example.data.repository.ITourismRemoteDataSource
import com.example.data.repository.ITourismRepository
import com.example.data.repository.NewsRepository
import com.example.data.repository.Resource
import com.example.data.repository.TourismRepository
import com.example.ui.viewmodel.BalasoreViewModel
import com.example.ui.viewmodel.NewsFeedTab
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.check
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

/**
 * Comprehensive Unit Tests for [NewsRepository], [TourismRepository], and [BalasoreViewModel]
 * using JUnit and Mockito to verify:
 * 1. Offline-first data synchronization & 24-hour cache expiration for News and Tourism Hotspots.
 * 2. Category filtering ('Politics', 'Sports', 'Tourism', 'Defense') and Room DAO query routing.
 * 3. News search across article titles and content.
 * 4. Bookmarking in News and Favorite toggling in Tourism Hotspots.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewsAndTourismRepositoryViewModelUnitTest {

    private val testDispatcher = StandardTestDispatcher()

    // News mocks
    private lateinit var mockNewsDao: NewsDao
    private lateinit var mockCacheMetadataDao: CacheMetadataDao
    private lateinit var mockApiService: BalasoreApiService
    private lateinit var newsRepository: NewsRepository

    // Tourism mocks
    private lateinit var mockHotspotDao: HotspotDao
    private lateinit var mockTourismRemoteDataSource: ITourismRemoteDataSource
    private lateinit var tourismRepository: TourismRepository

    private val sampleNewsEntities = listOf(
        NewsArticleEntity(
            id = 101L,
            title = "Balasore Zilla Parishad Approves Coastal Road Budget",
            summary = "District council passes resolution for rural ward connectivity.",
            content = "Elected representatives in Balasore Zilla Parishad approved the annual infrastructure budget.",
            category = "Politics",
            source = "Balasore Express",
            publishedAt = "1h ago",
            timestamp = System.currentTimeMillis(),
            isBreaking = true,
            isBookmarked = false
        ),
        NewsArticleEntity(
            id = 102L,
            title = "Balasore District Athletics Championship Opens at Police Line Stadium",
            summary = "Over 400 sprinters and football clubs compete in annual meet.",
            content = "Young athletes from Nilagiri, Jaleswar, and Soro gathered at the stadium for state trials.",
            category = "Sports",
            source = "Odisha Sports Desk",
            publishedAt = "2h ago",
            timestamp = System.currentTimeMillis() - 3600_000L,
            isBreaking = false,
            isBookmarked = true
        ),
        NewsArticleEntity(
            id = 103L,
            title = "Chandipur Eco-Promenade Welcomes Record Weekend Tourists",
            summary = "New viewing decks and beach gazebos attract visitors across Odisha.",
            content = "Tourism officials recorded high footfall along the vanishing sea promenade and Talasari coast.",
            category = "Tourism",
            source = "Odisha Tourism Bureau",
            publishedAt = "3h ago",
            timestamp = System.currentTimeMillis() - 7200_000L,
            isBreaking = false,
            isBookmarked = false
        ),
        NewsArticleEntity(
            id = 104L,
            title = "DRDO Conducts Successful Interceptor Flight Trial at ITR Chandipur",
            summary = "Telemetry radars track precision trajectory over Bay of Bengal.",
            content = "Defense scientists at Launch Complex-III validated next-generation indigenous missile guidance.",
            category = "Defense",
            source = "ITR Defense Desk",
            publishedAt = "4h ago",
            timestamp = System.currentTimeMillis() - 10800_000L,
            isBreaking = true,
            isBookmarked = false
        )
    )

    private val sampleHotspotEntities = listOf(
        HotspotEntity(
            id = "chandipur_beach",
            name = "Chandipur-on-Sea",
            odiaName = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
            category = "Beaches",
            shortDescription = "Famous vanishing sea beach receding up to 5 km during low tide.",
            fullDescription = "Unique intertidal beach on the Bay of Bengal home to horseshoe crabs.",
            highlights = "Seabed walk, Horseshoe crabs, Casuarina groves",
            distanceKmFromBls = 16,
            bestTimeToVisit = "October to March",
            timings = "6:00 AM - 8:00 PM",
            entryFee = "Free",
            specialty = "Vanishing Tide",
            localTip = "Check low-tide clock before walking out.",
            latitude = 21.4446,
            longitude = 87.0149,
            isFavorite = true,
            timestamp = System.currentTimeMillis()
        ),
        HotspotEntity(
            id = "panchalingeswar_temple",
            name = "Panchalingeswar Shrine",
            odiaName = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପୀଠ",
            category = "Temples",
            shortDescription = "Five perennial stream-bathed Shiva Lingams nestled in Nilagiri hills.",
            fullDescription = "Sacred hill shrine surrounded by Kuldiha forest range.",
            highlights = "Hill stream, Perennial waterfall, Trekking trail",
            distanceKmFromBls = 30,
            bestTimeToVisit = "Year-round",
            timings = "5:30 AM - 7:00 PM",
            entryFee = "Free",
            specialty = "Perennial Mountain Stream Lingams",
            localTip = "Visit early morning for peaceful darshan.",
            latitude = 21.4258,
            longitude = 86.7433,
            isFavorite = false,
            timestamp = System.currentTimeMillis()
        ),
        HotspotEntity(
            id = "kuldiha_sanctuary",
            name = "Kuldiha Wildlife Sanctuary",
            odiaName = "କୁଲଡିହା ଅଭୟାରଣ୍ୟ",
            category = "Wildlife",
            shortDescription = "Biodiverse forest sanctuary connected to Similipal biosphere.",
            fullDescription = "Home to Asian elephants, gaur, sambar, and giant squirrels.",
            highlights = "Jadachua Nature Camp, Gohirabhola watchtower, Eco-safari",
            distanceKmFromBls = 42,
            bestTimeToVisit = "November to May",
            timings = "6:00 AM - 4:00 PM",
            entryFee = "₹40 per person",
            specialty = "Elephant Corridor & Eco-Camps",
            localTip = "Book forest entry permit at Nilagiri range office.",
            latitude = 21.4106,
            longitude = 86.6520,
            isFavorite = false,
            timestamp = System.currentTimeMillis()
        )
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        mockNewsDao = mock()
        mockCacheMetadataDao = mock()
        mockApiService = mock()

        whenever(mockNewsDao.getAllNews()).thenReturn(flowOf(sampleNewsEntities))
        whenever(mockNewsDao.getBreakingNews()).thenReturn(flowOf(sampleNewsEntities.filter { it.isBreaking }))
        whenever(mockNewsDao.getBookmarkedNews()).thenReturn(flowOf(sampleNewsEntities.filter { it.isBookmarked }))

        newsRepository = NewsRepository(
            newsDao = mockNewsDao,
            cacheMetadataDao = mockCacheMetadataDao,
            apiService = mockApiService
        )

        mockHotspotDao = mock()
        mockTourismRemoteDataSource = mock()

        whenever(mockHotspotDao.getAllHotspots()).thenReturn(flowOf(sampleHotspotEntities))
        whenever(mockHotspotDao.getFavoriteHotspots()).thenReturn(flowOf(sampleHotspotEntities.filter { it.isFavorite }))

        tourismRepository = TourismRepository(
            hotspotDao = mockHotspotDao,
            cacheMetadataDao = mockCacheMetadataDao,
            remoteDataSource = mockTourismRemoteDataSource
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // 1. NEWS REPOSITORY UNIT TESTS (Offline Caching, Category Filters & Search)
    // =========================================================================

    @Test
    fun `NewsRepository - getNewsByCategory queries DAO for Politics Sports Tourism and Defense`() = runTest {
        val politicsArticles = sampleNewsEntities.filter { it.category == "Politics" }
        val sportsArticles = sampleNewsEntities.filter { it.category == "Sports" }
        val tourismArticles = sampleNewsEntities.filter { it.category == "Tourism" }
        val defenseArticles = sampleNewsEntities.filter { it.category == "Defense" }

        whenever(mockNewsDao.getNewsByCategory("Politics")).thenReturn(flowOf(politicsArticles))
        whenever(mockNewsDao.getNewsByCategory("Sports")).thenReturn(flowOf(sportsArticles))
        whenever(mockNewsDao.getNewsByCategory("Tourism")).thenReturn(flowOf(tourismArticles))
        whenever(mockNewsDao.getNewsByCategory("Defense")).thenReturn(flowOf(defenseArticles))

        val politicsResult = newsRepository.getNewsByCategory("Politics").first()
        assertEquals(1, politicsResult.size)
        assertEquals("Politics", politicsResult.first().category)

        val sportsResult = newsRepository.getNewsByCategory("Sports").first()
        assertEquals(1, sportsResult.size)
        assertEquals("Sports", sportsResult.first().category)

        val tourismResult = newsRepository.getNewsByCategory("Tourism").first()
        assertEquals(1, tourismResult.size)
        assertEquals("Tourism", tourismResult.first().category)

        val defenseResult = newsRepository.getNewsByCategory("Defense").first()
        assertEquals(1, defenseResult.size)
        assertEquals("Defense", defenseResult.first().category)

        verify(mockNewsDao).getNewsByCategory("Politics")
        verify(mockNewsDao).getNewsByCategory("Sports")
        verify(mockNewsDao).getNewsByCategory("Tourism")
        verify(mockNewsDao).getNewsByCategory("Defense")
    }

    @Test
    fun `NewsRepository - getNewsByCategoryAndSearch combines category and keyword query in Room`() = runTest {
        val expected = listOf(sampleNewsEntities[3]) // Defense article with "ITR"
        whenever(mockNewsDao.getNewsByCategoryAndSearch("Defense", "ITR")).thenReturn(flowOf(expected))

        val result = newsRepository.getNewsByCategoryAndSearch("Defense", "ITR").first()
        assertEquals(1, result.size)
        assertEquals("Defense", result.first().category)
        assertTrue(result.first().title.contains("ITR"))
        verify(mockNewsDao).getNewsByCategoryAndSearch("Defense", "ITR")
    }

    @Test
    fun `NewsRepository - searchNews searches across titles and content`() = runTest {
        val matchingContentArticle = listOf(sampleNewsEntities[1]) // contains "Nilagiri" in content
        whenever(mockNewsDao.searchNews("Nilagiri")).thenReturn(flowOf(matchingContentArticle))

        val result = newsRepository.searchNews("  Nilagiri  ").first()
        assertEquals(1, result.size)
        assertEquals(102L, result.first().id)
        verify(mockNewsDao).searchNews("Nilagiri")
    }

    @Test
    fun `NewsRepository - getUnifiedNewsFlow syncs network to Room and preserves bookmarks`() = runTest {
        whenever(mockNewsDao.getCount()).thenReturn(0, 2)
        whenever(mockNewsDao.getLatestTimestamp()).thenReturn(null)
        whenever(mockCacheMetadataDao.getMetadataSync("NEWS")).thenReturn(null)
        whenever(mockNewsDao.getBookmarkedNewsSync()).thenReturn(listOf(sampleNewsEntities[1])) // id 102L is bookmarked

        val remoteDtos = listOf(
            NewsArticleDto(
                id = 101L,
                title = "Balasore Zilla Parishad Approves Coastal Road Budget",
                summary = "Updated summary",
                content = "Updated content",
                category = "Politics",
                source = "Balasore Express",
                publishedAt = "Just now",
                timestamp = System.currentTimeMillis(),
                isBreaking = true
            ),
            NewsArticleDto(
                id = 102L,
                title = "Balasore District Athletics Championship Opens at Police Line Stadium",
                summary = "Updated sports summary",
                content = "Updated sports content",
                category = "Sports",
                source = "Odisha Sports Desk",
                publishedAt = "10m ago",
                timestamp = System.currentTimeMillis(),
                isBreaking = false
            )
        )
        whenever(mockApiService.getBalasoreNews()).thenReturn(
            BalasoreNewsResponse(status = "ok", totalResults = 2, articles = remoteDtos)
        )

        val emissions = newsRepository.getUnifiedNewsFlow(forceRefresh = true).toList()
        assertTrue(emissions.first() is Resource.Loading)
        val success = emissions.last() as Resource.Success
        assertFalse(success.isOfflineCached)
        assertEquals(2, success.data?.size)

        verify(mockNewsDao).insertArticles(check { inserted ->
            val article101 = inserted.find { it.id == 101L }
            val article102 = inserted.find { it.id == 102L }
            assertNotNull(article101)
            assertNotNull(article102)
            assertFalse(article101!!.isBookmarked)
            assertTrue("Existing bookmark must be preserved during network sync", article102!!.isBookmarked)
        })
    }

    @Test
    fun `NewsRepository - getUnifiedNewsFlow falls back to offline cache when network fails`() = runTest {
        whenever(mockNewsDao.getCount()).thenReturn(4)
        whenever(mockNewsDao.getLatestTimestamp()).thenReturn(0L)
        whenever(mockApiService.getBalasoreNews()).thenThrow(RuntimeException("Offline network error"))

        val emissions = newsRepository.getUnifiedNewsFlow(forceRefresh = true).toList()
        val lastEmission = emissions.last()
        assertTrue(lastEmission is Resource.Success)
        assertTrue((lastEmission as Resource.Success).isOfflineCached)
        verify(mockCacheMetadataDao).insertOrUpdate(check { metadata ->
            assertEquals("NEWS", metadata.cacheKey)
            assertEquals("OFFLINE", metadata.status)
        })
    }

    @Test
    fun `NewsRepository - toggleBookmark flips bookmark state in Room NewsDao`() = runTest {
        newsRepository.toggleBookmark(articleId = 101L, currentBookmarked = false)
        verify(mockNewsDao).updateBookmark(101L, true)

        newsRepository.toggleBookmark(articleId = 102L, currentBookmarked = true)
        verify(mockNewsDao).updateBookmark(102L, false)
    }

    // =========================================================================
    // 2. TOURISM REPOSITORY UNIT TESTS (Offline Caching & Hotspot Retrieval)
    // =========================================================================

    @Test
    fun `TourismRepository - isCacheOlderThan24Hours validates fresh vs expired cache`() = runTest {
        val freshTimestamp = System.currentTimeMillis() - (2 * 60 * 60 * 1000L) // 2 hours ago
        whenever(mockHotspotDao.getLatestTimestamp()).thenReturn(freshTimestamp)
        whenever(mockCacheMetadataDao.getMetadataSync("TOURISM")).thenReturn(null)

        assertFalse("Cache synced 2 hours ago should not be stale", tourismRepository.isCacheOlderThan24Hours())

        val expiredTimestamp = System.currentTimeMillis() - (26 * 60 * 60 * 1000L) // 26 hours ago
        whenever(mockHotspotDao.getLatestTimestamp()).thenReturn(expiredTimestamp)
        assertTrue("Cache synced 26 hours ago should be stale", tourismRepository.isCacheOlderThan24Hours())
    }

    @Test
    fun `TourismRepository - getUnifiedHotspotsFlow serves local Room cache without network when cache is fresh`() = runTest {
        val freshTimestamp = System.currentTimeMillis() - (1 * 60 * 60 * 1000L)
        whenever(mockHotspotDao.getCount()).thenReturn(sampleHotspotEntities.size)
        whenever(mockHotspotDao.getLatestTimestamp()).thenReturn(freshTimestamp)
        whenever(mockHotspotDao.getAllHotspotsSync()).thenReturn(sampleHotspotEntities)

        val emissions = tourismRepository.getUnifiedHotspotsFlow(forceRefresh = false).toList()
        assertTrue(emissions.first() is Resource.Loading)
        val result = emissions.last() as Resource.Success
        assertTrue(result.isOfflineCached)
        assertEquals(3, result.data?.size)
        assertEquals("Chandipur-on-Sea", result.data?.first()?.name)

        verify(mockTourismRemoteDataSource, never()).fetchRemoteHotspots()
    }

    @Test
    fun `TourismRepository - getUnifiedHotspotsFlow fetches remote hotspots when stale and preserves favorites`() = runTest {
        val staleTimestamp = System.currentTimeMillis() - (30 * 60 * 60 * 1000L)
        whenever(mockHotspotDao.getCount()).thenReturn(3)
        whenever(mockHotspotDao.getLatestTimestamp()).thenReturn(staleTimestamp)
        whenever(mockHotspotDao.getFavoriteHotspotsSync()).thenReturn(
            listOf(sampleHotspotEntities.first()) // chandipur_beach is favorite in local DB
        )

        val remoteFreshHotspots = sampleHotspotEntities.map { it.copy(isFavorite = false) }
        whenever(mockTourismRemoteDataSource.fetchRemoteHotspots()).thenReturn(remoteFreshHotspots)

        val emissions = tourismRepository.getUnifiedHotspotsFlow(forceRefresh = false).toList()
        val success = emissions.last() as Resource.Success
        assertFalse(success.isOfflineCached)
        assertEquals(3, success.data?.size)

        verify(mockHotspotDao).insertHotspots(check { inserted ->
            val chandipur = inserted.find { it.id == "chandipur_beach" }
            val panchalingeswar = inserted.find { it.id == "panchalingeswar_temple" }
            assertNotNull(chandipur)
            assertTrue("Favorite status for chandipur_beach must be preserved across sync", chandipur!!.isFavorite)
            assertFalse(panchalingeswar!!.isFavorite)
        })
        verify(mockCacheMetadataDao).insertOrUpdate(check { meta ->
            assertEquals("TOURISM", meta.cacheKey)
            assertEquals("FRESH", meta.status)
        })
    }

    @Test
    fun `TourismRepository - getUnifiedHotspotsFlow falls back to Room cache when remote source fails`() = runTest {
        whenever(mockHotspotDao.getCount()).thenReturn(3)
        whenever(mockHotspotDao.getLatestTimestamp()).thenReturn(0L)
        whenever(mockHotspotDao.getAllHotspotsSync()).thenReturn(sampleHotspotEntities)
        whenever(mockTourismRemoteDataSource.fetchRemoteHotspots()).thenThrow(RuntimeException("Timeout fetching tourism hotspots"))

        val emissions = tourismRepository.getUnifiedHotspotsFlow(forceRefresh = true).toList()
        val fallback = emissions.last() as Resource.Success
        assertTrue(fallback.isOfflineCached)
        assertEquals(3, fallback.data?.size)
        assertEquals("Chandipur-on-Sea", fallback.data?.first()?.name)

        verify(mockCacheMetadataDao).insertOrUpdate(check { meta ->
            assertEquals("TOURISM", meta.cacheKey)
            assertEquals("OFFLINE", meta.status)
        })
    }

    @Test
    fun `TourismRepository - retrieval by category search and toggleFavorite delegate to HotspotDao`() = runTest {
        val beachesOnly = sampleHotspotEntities.filter { it.category == "Beaches" }
        val kudiSearch = sampleHotspotEntities.filter { it.id == "kuldiha_sanctuary" }
        whenever(mockHotspotDao.getHotspotsByCategory("Beaches")).thenReturn(flowOf(beachesOnly))
        whenever(mockHotspotDao.searchHotspots("Kuldiha")).thenReturn(flowOf(kudiSearch))
        whenever(mockHotspotDao.getHotspotById("panchalingeswar_temple")).thenReturn(flowOf(sampleHotspotEntities[1]))

        val byCat = tourismRepository.getHotspotsByCategory("Beaches").first()
        assertEquals(1, byCat.size)
        assertEquals("chandipur_beach", byCat.first().id)

        val searched = tourismRepository.searchHotspots("  Kuldiha ").first()
        assertEquals(1, searched.size)
        assertEquals("kuldiha_sanctuary", searched.first().id)

        val byId = tourismRepository.getHotspotById("panchalingeswar_temple").first()
        assertNotNull(byId)
        assertEquals("Panchalingeswar Shrine", byId?.name)

        tourismRepository.toggleFavorite("panchalingeswar_temple", currentFavorite = false)
        verify(mockHotspotDao).updateFavorite("panchalingeswar_temple", true)
    }

    // =========================================================================
    // 3. VIEWMODEL UNIT TESTS (News & Tourism Offline Sync, Search & Filtering)
    // =========================================================================

    @Test
    fun `BalasoreViewModel - filters news feed by Politics Sports Tourism and Defense categories`() = runTest {
        val mockNewsRepo: INewsRepository = mock()
        val mockTourismRepo: ITourismRepository = mock()

        whenever(mockNewsRepo.allNews).thenReturn(flowOf(sampleNewsEntities))
        whenever(mockNewsRepo.getBookmarkedNews()).thenReturn(flowOf(sampleNewsEntities.filter { it.isBookmarked }))
        whenever(mockTourismRepo.allHotspots).thenReturn(flowOf(sampleHotspotEntities))
        whenever(mockTourismRepo.favoriteHotspots).thenReturn(flowOf(sampleHotspotEntities.filter { it.isFavorite }))

        val viewModel = BalasoreViewModel(
            injectedNewsRepo = mockNewsRepo,
            injectedTourismRepo = mockTourismRepo
        )
        advanceUntilIdle()

        assertEquals(4, viewModel.uiState.value.newsArticles.size)

        // Filter by Politics
        viewModel.setNewsCategory("Politics")
        assertEquals(NewsFeedTab.POLITICS.index, viewModel.uiState.value.selectedNewsTab)
        assertEquals(1, viewModel.uiState.value.filteredNewsArticles.size)
        assertEquals("Politics", viewModel.uiState.value.filteredNewsArticles.first().category)

        // Filter by Sports
        viewModel.setNewsCategory("Sports")
        assertEquals(NewsFeedTab.SPORTS.index, viewModel.uiState.value.selectedNewsTab)
        assertEquals(1, viewModel.uiState.value.filteredNewsArticles.size)
        assertEquals("Sports", viewModel.uiState.value.filteredNewsArticles.first().category)

        // Filter by Tourism
        viewModel.setNewsCategory("Tourism")
        assertEquals(NewsFeedTab.TOURISM_UPDATES.index, viewModel.uiState.value.selectedNewsTab)
        assertEquals(1, viewModel.uiState.value.filteredNewsArticles.size)
        assertEquals("Tourism", viewModel.uiState.value.filteredNewsArticles.first().category)

        // Filter by Defense
        viewModel.setNewsCategory("Defense")
        assertEquals(NewsFeedTab.DEFENSE_ALERTS.index, viewModel.uiState.value.selectedNewsTab)
        assertEquals(1, viewModel.uiState.value.filteredNewsArticles.size)
        assertEquals("Defense", viewModel.uiState.value.filteredNewsArticles.first().category)
    }

    @Test
    fun `BalasoreViewModel - searches news across article titles and content and toggles bookmarks`() = runTest {
        val mockNewsRepo: INewsRepository = mock()
        whenever(mockNewsRepo.allNews).thenReturn(flowOf(sampleNewsEntities))
        whenever(mockNewsRepo.getBookmarkedNews()).thenReturn(flowOf(sampleNewsEntities.filter { it.isBookmarked }))

        val viewModel = BalasoreViewModel(injectedNewsRepo = mockNewsRepo)
        advanceUntilIdle()

        // Search by keyword in content ("Launch Complex-III" only appears in article 104's content)
        viewModel.setNewsCategory("All")
        viewModel.setNewsSearchQuery("Launch Complex-III")
        assertEquals(1, viewModel.uiState.value.filteredNewsArticles.size)
        assertEquals("104", viewModel.uiState.value.filteredNewsArticles.first().id)

        // Toggle bookmark on article 101
        viewModel.toggleBookmark("101")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.bookmarkedIds.contains("101"))
        verify(mockNewsRepo).toggleBookmark(eq(101L), eq(false))
    }

    @Test
    fun `BalasoreViewModel - synchronizes TourismRepository hotspots filters by category and search and toggles favorite`() = runTest {
        val mockNewsRepo: INewsRepository = mock()
        val mockTourismRepo: ITourismRepository = mock()

        val hotspotsFlow = MutableStateFlow(sampleHotspotEntities)
        val favoritesFlow = MutableStateFlow(sampleHotspotEntities.filter { it.isFavorite })

        whenever(mockNewsRepo.allNews).thenReturn(flowOf(sampleNewsEntities))
        whenever(mockNewsRepo.getBookmarkedNews()).thenReturn(flowOf(emptyList()))
        whenever(mockTourismRepo.allHotspots).thenReturn(hotspotsFlow)
        whenever(mockTourismRepo.favoriteHotspots).thenReturn(favoritesFlow)

        val viewModel = BalasoreViewModel(
            injectedNewsRepo = mockNewsRepo,
            injectedTourismRepo = mockTourismRepo
        )
        advanceUntilIdle()

        // Verify offline-cached hotspots loaded into ViewModel UI state
        assertEquals(3, viewModel.uiState.value.hotspots.size)
        assertTrue(viewModel.uiState.value.favoriteHotspotIds.contains("chandipur_beach"))

        // Filter hotspots by category "Temples"
        viewModel.setHotspotCategory("Temples")
        val templeHotspots = viewModel.getFilteredHotspots()
        assertEquals(1, templeHotspots.size)
        assertEquals("panchalingeswar_temple", templeHotspots.first().id)

        // Filter hotspots by search query across description ("biosphere" in Kuldiha description)
        viewModel.setHotspotCategory("All")
        viewModel.setHotspotSearchQuery("biosphere")
        val searchedHotspots = viewModel.getFilteredHotspots()
        assertEquals(1, searchedHotspots.size)
        assertEquals("kuldiha_sanctuary", searchedHotspots.first().id)

        // Toggle favorite on panchalingeswar_temple
        viewModel.toggleFavoriteHotspot("panchalingeswar_temple")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value.favoriteHotspotIds.contains("panchalingeswar_temple"))
        verify(mockTourismRepo).toggleFavorite(eq("panchalingeswar_temple"), eq(false))
    }
}
