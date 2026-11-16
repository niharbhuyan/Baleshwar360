package com.example

import com.example.data.local.CommunityNewsSubmissionDao
import com.example.data.local.CommunityNewsSubmissionEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.local.NewsDao
import com.example.data.local.NewsModerationStatus
import com.example.data.repository.CommunityNewsModerationRepository
import com.example.ui.viewmodel.BalasoreViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CitizenNewsSubmissionAndModerationTest {

    private lateinit var fakeSubmissionDao: FakeCommunityNewsSubmissionDao
    private lateinit var fakeNewsDao: FakeNewsDao
    private lateinit var repository: CommunityNewsModerationRepository

    class FakeCommunityNewsSubmissionDao : CommunityNewsSubmissionDao {
        val storage = LinkedHashMap<String, CommunityNewsSubmissionEntity>()

        override fun getAllSubmissionsFlow(): Flow<List<CommunityNewsSubmissionEntity>> =
            flowOf(storage.values.toList())

        override suspend fun getAllSubmissionsSync(): List<CommunityNewsSubmissionEntity> =
            storage.values.toList()

        override fun getSubmissionsByStatusFlow(status: String): Flow<List<CommunityNewsSubmissionEntity>> =
            flowOf(storage.values.filter { it.status == status })

        override fun getPendingSubmissionsFlow(): Flow<List<CommunityNewsSubmissionEntity>> =
            flowOf(storage.values.filter { it.status == NewsModerationStatus.PENDING.code })

        override suspend fun getPendingSubmissionsSync(): List<CommunityNewsSubmissionEntity> =
            storage.values.filter { it.status == NewsModerationStatus.PENDING.code }

        override fun getApprovedSubmissionsFlow(): Flow<List<CommunityNewsSubmissionEntity>> =
            flowOf(storage.values.filter { it.status == NewsModerationStatus.APPROVED.code })

        override suspend fun getApprovedSubmissionsSync(): List<CommunityNewsSubmissionEntity> =
            storage.values.filter { it.status == NewsModerationStatus.APPROVED.code }

        override suspend fun getSubmissionByIdSync(id: String): CommunityNewsSubmissionEntity? =
            storage[id]

        override suspend fun getCount(): Int = storage.size

        override fun getPendingCountFlow(): Flow<Int> =
            flowOf(storage.values.count { it.status == NewsModerationStatus.PENDING.code })

        override suspend fun insertSubmission(submission: CommunityNewsSubmissionEntity) {
            storage[submission.id] = submission
        }

        override suspend fun insertSubmissions(submissions: List<CommunityNewsSubmissionEntity>) {
            submissions.forEach { storage[it.id] = it }
        }

        override suspend fun updateSubmission(submission: CommunityNewsSubmissionEntity): Int {
            storage[submission.id] = submission
            return 1
        }

        override suspend fun updateModerationDecision(
            id: String,
            status: String,
            moderatorNote: String,
            moderatedBy: String,
            moderatedAt: Long,
            publishedArticleId: String?
        ): Int {
            val current = storage[id] ?: return 0
            storage[id] = current.copy(
                status = status,
                moderatorNote = moderatorNote,
                moderatedBy = moderatedBy,
                moderatedAt = moderatedAt,
                publishedArticleId = publishedArticleId
            )
            return 1
        }

        override suspend fun deleteSubmissionById(id: String): Int {
            return if (storage.remove(id) != null) 1 else 0
        }

        override suspend fun clearAll(): Int {
            val size = storage.size
            storage.clear()
            return size
        }
    }

    class FakeNewsDao : NewsDao {
        val articles = LinkedHashMap<Long, NewsArticleEntity>()

        override suspend fun insertArticle(article: NewsArticleEntity): Long {
            val id = if (article.id == 0L) (articles.size + 1L) else article.id
            articles[id] = article.copy(id = id)
            return id
        }

        override suspend fun insertArticles(articles: List<NewsArticleEntity>) {
            articles.forEach { insertArticle(it) }
        }

        override fun getAllNews(): Flow<List<NewsArticleEntity>> = flowOf(articles.values.toList())
        override fun getNewsByCategory(category: String): Flow<List<NewsArticleEntity>> = flowOf(emptyList())
        override fun getNewsByCategoryAndSearch(category: String, query: String): Flow<List<NewsArticleEntity>> = flowOf(emptyList())
        override fun getBookmarkedNews(): Flow<List<NewsArticleEntity>> = flowOf(emptyList())
        override suspend fun getBookmarkedNewsSync(): List<NewsArticleEntity> = emptyList()
        override fun getBreakingNews(): Flow<List<NewsArticleEntity>> = flowOf(emptyList())
        override fun getArticleById(id: Long): Flow<NewsArticleEntity?> = flowOf(articles[id])
        override suspend fun getArticleByIdSync(id: Long): NewsArticleEntity? = articles[id]
        override fun searchNews(query: String): Flow<List<NewsArticleEntity>> = flowOf(emptyList())
        override suspend fun getAllArticlesSync(): List<NewsArticleEntity> = articles.values.toList()
        override suspend fun getCount(): Int = articles.size
        override fun getNewsCountFlow(): Flow<Int> = flowOf(articles.size)
        override suspend fun getLatestTimestamp(): Long? = articles.values.maxOfOrNull { it.timestamp }
        override suspend fun getOldestTimestamp(): Long? = articles.values.minOfOrNull { it.timestamp }
        override suspend fun updateArticle(article: NewsArticleEntity): Int {
            articles[article.id] = article
            return 1
        }
        override suspend fun updateArticles(articles: List<NewsArticleEntity>): Int = articles.size
        override suspend fun updateBookmark(id: Long, isBookmarked: Boolean) {}
        override suspend fun deleteArticle(article: NewsArticleEntity): Int {
            articles.remove(article.id)
            return 1
        }
        override suspend fun deleteArticleById(id: Long): Int {
            return if (articles.remove(id) != null) 1 else 0
        }
        override suspend fun clearNonBookmarked() {}
        override suspend fun clearAll() {
            articles.clear()
        }
    }

    @Before
    fun setUp() {
        fakeSubmissionDao = FakeCommunityNewsSubmissionDao()
        fakeNewsDao = FakeNewsDao()
        repository = CommunityNewsModerationRepository(
            submissionDao = fakeSubmissionDao,
            newsDao = fakeNewsDao
        )
    }

    @Test
    fun testSubmitNewsStory_staysPendingUntilModeratorApproves_thenGoesLive() = runBlocking {
        repository.ensureSeeded()

        val headline = "New Footbridge Inaugurated Over Budhabalanga Near Balighat"
        val snippet = "Pedestrians and school students in Ward 18 now have safe river crossing access."
        val submitted = repository.submitNewsStory(
            title = headline,
            odiaTitle = "ବାଲିଘାଟ ନିକଟରେ ବୁଢ଼ାବଳଙ୍ଗ ନଦୀ ଉପରେ ନୂତନ ପାଦଚଲା ପୋଲ ଉଦଘାଟିତ",
            snippet = snippet,
            content = "The Balasore Municipality completed the pedestrian bridge at Balighat.",
            category = "Development",
            locationWard = "Balighat, Ward 18, Balasore",
            authorName = "Subrat Das",
            authorContact = "subrat@balasore.in",
            sourceVerificationType = "Eyewitness Report"
        )

        // 1. Verify newly submitted story is PENDING and NOT yet in live NewsDao
        assertEquals(NewsModerationStatus.PENDING, submitted.moderationStatus)
        assertFalse(submitted.isLive)
        val liveArticlesBeforeApproval = fakeNewsDao.getAllArticlesSync()
        assertFalse(liveArticlesBeforeApproval.any { it.title == headline })

        // 2. Verify automated editorial pre-check recognizes Balasore context and headline quality
        val preCheck = CommunityNewsModerationRepository.evaluateEditorialChecklist(submitted)
        assertTrue(preCheck.hasClearHeadline)
        assertTrue(preCheck.hasLocalBalasoreContext)
        assertTrue(preCheck.trustScorePercent >= 80)

        // 3. Moderator approves the story -> transitions to APPROVED and publishes to live NewsDao
        val approvalResult = repository.approveAndPublishSubmission(
            submissionId = submitted.id,
            moderatorNote = "Verified with Ward 18 Engineer & approved for publication",
            moderatedBy = "Baleshwar 360 Chief Moderator"
        )
        assertNotNull(approvalResult)
        val (approvedEntity, publishedArticle) = approvalResult!!
        assertEquals(NewsModerationStatus.APPROVED, approvedEntity.moderationStatus)
        assertTrue(approvedEntity.isLive)
        assertEquals(headline, publishedArticle.title)

        val liveArticlesAfterApproval = fakeNewsDao.getAllArticlesSync()
        assertTrue(liveArticlesAfterApproval.any { it.title == headline })
    }

    @Test
    fun testRevisionAndRejectionWorkflow_removesRejectedFromLiveFeed() = runBlocking {
        val story = repository.submitNewsStory(
            title = "Temporary Traffic Diversion at Fakir Mohan Golai for Drain Repair",
            snippet = "Two-wheelers diverted via Station Road until 6 PM today.",
            content = "Municipal drainage repair work is underway near Fakir Mohan Golai.",
            category = "Local",
            locationWard = "Fakir Mohan Golai, Balasore",
            authorName = "Ananya Panda"
        )

        // Request revision
        val revisionReq = repository.requestRevision(
            submissionId = story.id,
            moderatorNote = "Please confirm exact completion time with Traffic Police."
        )
        assertNotNull(revisionReq)
        assertEquals(NewsModerationStatus.REVISION_REQUESTED, revisionReq!!.moderationStatus)

        // Author updates & resubmits -> returns to PENDING
        val resubmitted = repository.updateAndResubmitStory(
            submissionId = story.id,
            title = "Traffic Diversion at Fakir Mohan Golai Cleared Ahead of Schedule",
            odiaTitle = "",
            snippet = "Both lanes reopened at 4 PM after culvert slab installation.",
            content = "Confirmed by Balasore Town Traffic Police.",
            category = "Local",
            locationWard = "Fakir Mohan Golai, Ward 10, Balasore"
        )
        assertNotNull(resubmitted)
        assertEquals(NewsModerationStatus.PENDING, resubmitted!!.moderationStatus)

        // Approve first, then reject -> verify rejection removes it from live NewsDao
        val approved = repository.approveAndPublishSubmission(submissionId = story.id)
        assertNotNull(approved)
        assertTrue(fakeNewsDao.getAllArticlesSync().any { it.title.contains("Fakir Mohan Golai Cleared") })

        val rejected = repository.rejectSubmission(
            submissionId = story.id,
            moderatorNote = "Superseded by official traffic bulletin"
        )
        assertNotNull(rejected)
        assertEquals(NewsModerationStatus.REJECTED, rejected!!.moderationStatus)
        assertFalse(fakeNewsDao.getAllArticlesSync().any { it.title.contains("Fakir Mohan Golai Cleared") })
    }

    @Test
    fun testViewModelCitizenNewsModerationFlow() = runBlocking {
        val viewModel = BalasoreViewModel()
        val testTitle = "Talasari Coastal Eco-Cycling Trail Opened for Morning Visitors"

        // 1. Submit story via ViewModel -> added to communityNewsSubmissions as PENDING, NOT in live newsArticles
        viewModel.submitCommunityNewsStory(
            title = testTitle,
            snippet = "3 km casuarina beach cycling path inaugurated at Talasari.",
            content = "Eco-tourists can rent bicycles at Talasari beach reception.",
            category = "Tourism",
            locationWard = "Talasari Beach, Bhograi Block",
            authorName = "Debasis Rout"
        )
        Thread.sleep(150)

        val submitted = viewModel.uiState.value.communityNewsSubmissions.firstOrNull { it.title == testTitle }
        assertNotNull(submitted)
        assertEquals(NewsModerationStatus.PENDING, submitted!!.moderationStatus)
        assertFalse(viewModel.uiState.value.newsArticles.any { it.title == testTitle })

        // 2. Moderator approves story via ViewModel -> now live in viewModel.uiState.value.newsArticles
        viewModel.approveCommunityNewsSubmission(
            submissionId = submitted.id,
            moderatorNote = "Verified with Bhograi Tourism Officer"
        )
        Thread.sleep(150)

        val approved = viewModel.uiState.value.communityNewsSubmissions.firstOrNull { it.id == submitted.id }
        assertEquals(NewsModerationStatus.APPROVED, approved?.moderationStatus)
        assertTrue(viewModel.uiState.value.newsArticles.any { it.title == testTitle })
    }

    @Test
    fun testPullToRefreshNewsFromServer_signalsLoadingStateAndCompletes() = runBlocking {
        val viewModel = BalasoreViewModel()
        val initialCycle = viewModel.uiState.value.newsAutoUpdateCycleCount

        viewModel.refreshNewsFromServer()
        Thread.sleep(100)
        // Immediately after pull-to-refresh is triggered, isRefreshing is true and status message signals server fetch
        assertTrue(viewModel.uiState.value.isRefreshing)
        assertTrue(viewModel.uiState.value.lastSyncStatusMessage.contains("Fetching new local stories", ignoreCase = true))

        // Wait for server refresh cycle to complete
        Thread.sleep(750)
        assertFalse(viewModel.uiState.value.isRefreshing)
        assertTrue(viewModel.uiState.value.newsAutoUpdateCycleCount > initialCycle)
        assertTrue(viewModel.uiState.value.lastSyncStatusMessage.contains("Fetched latest local Balasore stories", ignoreCase = true))
    }
}
