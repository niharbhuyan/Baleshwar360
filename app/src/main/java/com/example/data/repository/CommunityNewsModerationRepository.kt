package com.example.data.repository

import android.content.Context
import com.example.data.local.AppDatabase
import com.example.data.local.CommunityNewsSubmissionDao
import com.example.data.local.CommunityNewsSubmissionEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.local.NewsDao
import com.example.data.local.NewsModerationStatus
import com.example.data.model.NewsArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Automated editorial pre-check summary shown to moderators when reviewing a submitted story.
 */
data class EditorialPreCheckResult(
    val hasClearHeadline: Boolean,
    val hasDetailedContent: Boolean,
    val hasLocalBalasoreContext: Boolean,
    val hasVerifiedSourceType: Boolean,
    val trustScorePercent: Int,
    val summaryNote: String
)

/**
 * Repository managing citizen-submitted local news stories and the moderation workflow
 * (Pending Review -> Approved & Live / Revision Requested / Rejected).
 */
class CommunityNewsModerationRepository(
    private val submissionDao: CommunityNewsSubmissionDao? = null,
    private val newsDao: NewsDao? = null
) {
    val allSubmissionsFlow: Flow<List<CommunityNewsSubmissionEntity>> =
        submissionDao?.getAllSubmissionsFlow() ?: Companion._submissionsStateFlow

    val pendingSubmissionsFlow: Flow<List<CommunityNewsSubmissionEntity>> =
        submissionDao?.getPendingSubmissionsFlow()
            ?: Companion._submissionsStateFlow.map { list ->
                list.filter { it.moderationStatus == NewsModerationStatus.PENDING }
            }

    val approvedSubmissionsFlow: Flow<List<CommunityNewsSubmissionEntity>> =
        submissionDao?.getApprovedSubmissionsFlow()
            ?: Companion._submissionsStateFlow.map { list ->
                list.filter { it.moderationStatus == NewsModerationStatus.APPROVED }
            }

    suspend fun ensureSeeded(): List<CommunityNewsSubmissionEntity> {
        val dao = submissionDao
        if (dao != null) {
            val existingCount = dao.getCount()
            if (existingCount == 0) {
                val defaults = defaultSeededSubmissions()
                dao.insertSubmissions(defaults)
                Companion._submissionsStateFlow.value = defaults
                return defaults
            }
            val current = dao.getAllSubmissionsSync()
            Companion._submissionsStateFlow.value = current
            return current
        }
        return Companion._submissionsStateFlow.value
    }

    /**
     * Submits a new citizen news story.
     * Enforces the moderation gate: all user-submitted stories start in [NewsModerationStatus.PENDING]
     * and do NOT go live until explicitly approved by a moderator.
     */
    suspend fun submitNewsStory(
        title: String,
        odiaTitle: String = "",
        snippet: String,
        content: String,
        category: String = "Local",
        locationWard: String = "Balasore Town",
        authorName: String = "Balasore Citizen Reporter",
        authorContact: String = "",
        sourceVerificationType: String = "Eyewitness Report",
        photoUri: String? = null,
        isBreakingCandidate: Boolean = false
    ): CommunityNewsSubmissionEntity {
        val now = System.currentTimeMillis()
        val cleanTitle = title.trim()
        val cleanSnippet = snippet.trim().ifBlank { content.trim().take(140) }
        val cleanContent = content.trim().ifBlank { cleanSnippet }
        val submission = CommunityNewsSubmissionEntity(
            id = "sub_news_${now}_${(100..999).random()}",
            title = cleanTitle,
            odiaTitle = odiaTitle.trim().ifBlank { cleanTitle },
            snippet = cleanSnippet,
            content = cleanContent,
            category = category.trim().ifBlank { "Local" },
            locationWard = locationWard.trim().ifBlank { "Balasore Town" },
            authorName = authorName.trim().ifBlank { "Balasore Citizen Reporter" },
            authorContact = authorContact.trim(),
            sourceVerificationType = sourceVerificationType.trim().ifBlank { "Eyewitness Report" },
            photoUri = photoUri?.takeIf { it.isNotBlank() },
            status = NewsModerationStatus.PENDING.code,
            moderatorNote = "Submitted by citizen reporter — queued for moderator verification before going live.",
            moderatedBy = null,
            submittedAt = now,
            moderatedAt = null,
            publishedArticleId = null,
            isBreakingCandidate = isBreakingCandidate
        )

        submissionDao?.insertSubmission(submission)
        val updated = listOf(submission) + Companion._submissionsStateFlow.value.filterNot { it.id == submission.id }
        Companion._submissionsStateFlow.value = sortSubmissions(updated)
        return submission
    }

    /**
     * Moderator action: Approves a pending/revised submission and publishes it to the live news feed.
     * Also allows optional inline editorial refinements (headline, snippet, content, category).
     */
    suspend fun approveAndPublishSubmission(
        submissionId: String,
        moderatorNote: String = "",
        moderatedBy: String = "Baleshwar 360 Editorial Desk",
        editedTitle: String? = null,
        editedOdiaTitle: String? = null,
        editedSnippet: String? = null,
        editedContent: String? = null,
        editedCategory: String? = null
    ): Pair<CommunityNewsSubmissionEntity, NewsArticle>? {
        val existing = submissionDao?.getSubmissionByIdSync(submissionId)
            ?: Companion._submissionsStateFlow.value.firstOrNull { it.id == submissionId }
            ?: return null

        val now = System.currentTimeMillis()
        val publishedId = existing.publishedArticleId?.takeIf { it.isNotBlank() }
            ?: (9000L + kotlin.math.abs(existing.id.hashCode() % 90000)).toString()
        val resolvedNote = moderatorNote.trim().ifBlank {
            "Verified & Approved by $moderatedBy — Live on Baleshwar 360 News Feed."
        }

        val approvedSubmission = existing.copy(
            title = editedTitle?.trim()?.takeIf { it.isNotBlank() } ?: existing.title,
            odiaTitle = editedOdiaTitle?.trim()?.takeIf { it.isNotBlank() } ?: existing.odiaTitle,
            snippet = editedSnippet?.trim()?.takeIf { it.isNotBlank() } ?: existing.snippet,
            content = editedContent?.trim()?.takeIf { it.isNotBlank() } ?: existing.content,
            category = editedCategory?.trim()?.takeIf { it.isNotBlank() } ?: existing.category,
            status = NewsModerationStatus.APPROVED.code,
            moderatorNote = resolvedNote,
            moderatedBy = moderatedBy,
            moderatedAt = now,
            publishedArticleId = publishedId
        )

        submissionDao?.insertSubmission(approvedSubmission)

        val liveArticle = approvedSubmission.toLiveNewsArticle()
        val numericId = publishedId.toLongOrNull() ?: (9000L + kotlin.math.abs(existing.id.hashCode() % 90000))
        newsDao?.insertArticle(
            NewsArticleEntity(
                id = numericId,
                title = liveArticle.title,
                summary = liveArticle.snippet,
                content = liveArticle.content,
                category = liveArticle.category,
                source = liveArticle.source,
                publishedAt = liveArticle.timeAgo,
                timestamp = now,
                isBreaking = approvedSubmission.isBreakingCandidate,
                imageUrl = approvedSubmission.photoUri,
                isBookmarked = false
            )
        )

        val updatedList = Companion._submissionsStateFlow.value
            .map { if (it.id == submissionId) approvedSubmission else it }
            .let { list ->
                if (list.any { it.id == submissionId }) list else listOf(approvedSubmission) + list
            }
        Companion._submissionsStateFlow.value = sortSubmissions(updatedList)
        return approvedSubmission to liveArticle
    }

    /**
     * Moderator action: Rejects a submission so it does not go live (or unpublishes it if previously approved).
     */
    suspend fun rejectSubmission(
        submissionId: String,
        moderatorNote: String = "",
        moderatedBy: String = "Baleshwar 360 Fact-Check Desk"
    ): CommunityNewsSubmissionEntity? {
        val existing = submissionDao?.getSubmissionByIdSync(submissionId)
            ?: Companion._submissionsStateFlow.value.firstOrNull { it.id == submissionId }
            ?: return null

        val now = System.currentTimeMillis()
        val resolvedNote = moderatorNote.trim().ifBlank {
            "Rejected by Moderator: Unverified claim or insufficient local source attribution."
        }
        val previousPublishedId = existing.publishedArticleId

        val rejected = existing.copy(
            status = NewsModerationStatus.REJECTED.code,
            moderatorNote = resolvedNote,
            moderatedBy = moderatedBy,
            moderatedAt = now,
            publishedArticleId = null
        )

        submissionDao?.insertSubmission(rejected)
        previousPublishedId?.toLongOrNull()?.let { numericId ->
            newsDao?.deleteArticleById(numericId)
        }

        val updatedList = Companion._submissionsStateFlow.value.map {
            if (it.id == submissionId) rejected else it
        }
        Companion._submissionsStateFlow.value = sortSubmissions(updatedList)
        return rejected
    }

    /**
     * Moderator action: Requests a revision from the submitter before the story can be approved.
     */
    suspend fun requestRevision(
        submissionId: String,
        moderatorNote: String,
        moderatedBy: String = "Baleshwar 360 Editorial Desk"
    ): CommunityNewsSubmissionEntity? {
        val existing = submissionDao?.getSubmissionByIdSync(submissionId)
            ?: Companion._submissionsStateFlow.value.firstOrNull { it.id == submissionId }
            ?: return null

        val now = System.currentTimeMillis()
        val resolvedNote = moderatorNote.trim().ifBlank {
            "Revision requested: Please clarify exact ward location and source details."
        }
        val revised = existing.copy(
            status = NewsModerationStatus.REVISION_REQUESTED.code,
            moderatorNote = resolvedNote,
            moderatedBy = moderatedBy,
            moderatedAt = now
        )
        submissionDao?.insertSubmission(revised)

        val updatedList = Companion._submissionsStateFlow.value.map {
            if (it.id == submissionId) revised else it
        }
        Companion._submissionsStateFlow.value = sortSubmissions(updatedList)
        return revised
    }

    /**
     * Allows a citizen reporter to update their story and resubmit it to the [NewsModerationStatus.PENDING] queue.
     */
    suspend fun updateAndResubmitStory(
        submissionId: String,
        title: String,
        odiaTitle: String = "",
        snippet: String,
        content: String,
        category: String,
        locationWard: String
    ): CommunityNewsSubmissionEntity? {
        val existing = submissionDao?.getSubmissionByIdSync(submissionId)
            ?: Companion._submissionsStateFlow.value.firstOrNull { it.id == submissionId }
            ?: return null

        val now = System.currentTimeMillis()
        val resubmitted = existing.copy(
            title = title.trim().ifBlank { existing.title },
            odiaTitle = odiaTitle.trim().ifBlank { title.trim().ifBlank { existing.odiaTitle } },
            snippet = snippet.trim().ifBlank { existing.snippet },
            content = content.trim().ifBlank { existing.content },
            category = category.trim().ifBlank { existing.category },
            locationWard = locationWard.trim().ifBlank { existing.locationWard },
            status = NewsModerationStatus.PENDING.code,
            moderatorNote = "Updated & resubmitted by author — awaiting moderator review.",
            submittedAt = now
        )
        submissionDao?.insertSubmission(resubmitted)

        val updatedList = Companion._submissionsStateFlow.value.map {
            if (it.id == submissionId) resubmitted else it
        }
        Companion._submissionsStateFlow.value = sortSubmissions(updatedList)
        return resubmitted
    }

    /**
     * Batch-approves all currently pending news submissions and returns the published [NewsArticle] list.
     */
    suspend fun approveAllPending(
        moderatedBy: String = "Baleshwar 360 Editorial Desk"
    ): List<NewsArticle> {
        val pendingItems = submissionDao?.getPendingSubmissionsSync()
            ?: Companion._submissionsStateFlow.value.filter { it.moderationStatus == NewsModerationStatus.PENDING }

        val publishedArticles = mutableListOf<NewsArticle>()
        for (item in pendingItems) {
            val result = approveAndPublishSubmission(
                submissionId = item.id,
                moderatorNote = "Batch verified & approved by $moderatedBy",
                moderatedBy = moderatedBy
            )
            if (result != null) {
                publishedArticles.add(result.second)
            }
        }
        return publishedArticles
    }

    suspend fun deleteSubmission(submissionId: String) {
        val existing = submissionDao?.getSubmissionByIdSync(submissionId)
            ?: Companion._submissionsStateFlow.value.firstOrNull { it.id == submissionId }
        existing?.publishedArticleId?.toLongOrNull()?.let { numericId ->
            newsDao?.deleteArticleById(numericId)
        }
        submissionDao?.deleteSubmissionById(submissionId)
        Companion._submissionsStateFlow.value =
            Companion._submissionsStateFlow.value.filterNot { it.id == submissionId }
    }

    companion object {
        private val _submissionsStateFlow =
            MutableStateFlow<List<CommunityNewsSubmissionEntity>>(defaultSeededSubmissions())
        val submissionsStateFlow: StateFlow<List<CommunityNewsSubmissionEntity>> =
            _submissionsStateFlow.asStateFlow()

        fun getInstance(context: Context): CommunityNewsModerationRepository {
            val db = AppDatabase.getInstance(context)
            return CommunityNewsModerationRepository(
                submissionDao = db.communityNewsSubmissionDao(),
                newsDao = db.newsDao()
            )
        }

        suspend fun syncWithRoom(context: Context): List<CommunityNewsSubmissionEntity> =
            withContext(Dispatchers.IO) {
                try {
                    val repo = getInstance(context)
                    repo.ensureSeeded()
                } catch (_: Exception) {
                    _submissionsStateFlow.value
                }
            }

        /**
         * Evaluates a submission against editorial quality & fact-check heuristics to assist moderators.
         */
        fun evaluateEditorialChecklist(item: CommunityNewsSubmissionEntity): EditorialPreCheckResult {
            val hasClearHeadline = item.title.trim().length >= 12
            val hasDetailedContent = item.content.trim().length >= 30 || item.snippet.trim().length >= 25
            val combinedText = "${item.title} ${item.snippet} ${item.content} ${item.locationWard}".lowercase()
            val balasoreKeywords = listOf(
                "balasore", "baleshwar", "chandipur", "remuna", "soro", "jaleswar", "nilagiri",
                "basta", "bhograi", "simulia", "khaira", "oupada", "baliapal", "fakir mohan",
                "fm university", "subarnarekha", "budhabalanga", "panchalingeswar", "talasari",
                "ward", "nh-16", "nh-60", "station", "odisha", "itrs", "drdo", "hospital"
            )
            val hasLocalContext = balasoreKeywords.any { combinedText.contains(it) }
            val hasVerifiedSource = item.sourceVerificationType.isNotBlank() &&
                !item.authorName.equals("Anonymous Submitter", ignoreCase = true)

            var score = 25
            if (hasClearHeadline) score += 25
            if (hasDetailedContent) score += 20
            if (hasLocalContext) score += 20
            if (hasVerifiedSource) score += 10
            val clampedScore = score.coerceIn(10, 100)

            val note = when {
                clampedScore >= 85 -> "High-confidence local report • Ready for editorial approval"
                clampedScore >= 65 -> "Standard citizen submission • Verify ward & source details"
                else -> "Low verification score • Cross-check for accuracy before approval"
            }

            return EditorialPreCheckResult(
                hasClearHeadline = hasClearHeadline,
                hasDetailedContent = hasDetailedContent,
                hasLocalBalasoreContext = hasLocalContext,
                hasVerifiedSourceType = hasVerifiedSource,
                trustScorePercent = clampedScore,
                summaryNote = note
            )
        }

        private fun sortSubmissions(list: List<CommunityNewsSubmissionEntity>): List<CommunityNewsSubmissionEntity> {
            return list.sortedWith(
                compareBy<CommunityNewsSubmissionEntity> {
                    when (it.moderationStatus) {
                        NewsModerationStatus.PENDING -> 0
                        NewsModerationStatus.REVISION_REQUESTED -> 1
                        NewsModerationStatus.APPROVED -> 2
                        NewsModerationStatus.REJECTED -> 3
                    }
                }.thenByDescending { it.submittedAt }
            )
        }

        fun defaultSeededSubmissions(): List<CommunityNewsSubmissionEntity> {
            val now = System.currentTimeMillis()
            return listOf(
                CommunityNewsSubmissionEntity(
                    id = "sub_seed_pending_1",
                    title = "45 New Solar LED Streetlights Installed Along Chandipur Beach Approach Road",
                    odiaTitle = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ରାସ୍ତାରେ ୪୫ଟି ନୂତନ ସୌର ଏଲଇଡି ଷ୍ଟ୍ରିଟ୍ ଲାଇଟ୍ ସ୍ଥାପନ",
                    snippet = "Evening commuters and tourists travelling between Nua Bazar and Chandipur coast now have continuous solar-powered illumination across dark stretches.",
                    content = "Local residents and shopkeepers along the Chandipur approach corridor reported the completion of 45 high-efficiency solar LED street lamps with automatic dusk-to-dawn sensors. The upgrade improves night-time visibility for fishermen, evening walkers, and tourist vehicles returning from the vanishing sea beach.",
                    category = "Development",
                    locationWard = "Chandipur Approach Road, Balasore",
                    authorName = "Rakesh Mohanty",
                    authorContact = "rakesh.bls@citizen.in",
                    sourceVerificationType = "Eyewitness Report & Photo Evidence",
                    status = NewsModerationStatus.PENDING.code,
                    moderatorNote = "Awaiting moderator verification of Ward Engineering completion notice.",
                    submittedAt = now - 45 * 60_000L,
                    isBreakingCandidate = false
                ),
                CommunityNewsSubmissionEntity(
                    id = "sub_seed_pending_2",
                    title = "Fakir Mohan University Hosts District Youth Science & Coastal Ecology Symposium",
                    odiaTitle = "ଫକୀର ମୋହନ ବିଶ୍ୱବିଦ୍ୟାଳୟରେ ଜିଲ୍ଲା ଯୁବ ବିଜ୍ଞାନ ଓ ଉପକୂଳ ପରିବେଶ ଆଲୋଚନାଚକ୍ର",
                    snippet = "Over 300 students from Balasore, Bhadrak, and Mayurbhanj presented conservation models on horseshoe crabs and Subarnarekha mangrove restoration.",
                    content = "The Department of Environmental Science at Fakir Mohan University organized a two-day symposium focusing on Balasore's intertidal biodiversity, casuarina shelterbelts, and community-led plastic-free coastline drives at Talasari and Bichitrapur.",
                    category = "Education",
                    locationWard = "Nuapadhi Campus, Balasore",
                    authorName = "Srabani Panda",
                    authorContact = "srabani.fmu@edu.in",
                    sourceVerificationType = "Campus Press Release",
                    status = NewsModerationStatus.PENDING.code,
                    moderatorNote = "Submitted by campus correspondent — pending moderator review.",
                    submittedAt = now - 110 * 60_000L,
                    isBreakingCandidate = false
                ),
                CommunityNewsSubmissionEntity(
                    id = "sub_seed_approved_1",
                    title = "Budhabalanga Riverbank Morning Clean-Up Completed by 200 Balasore Volunteers",
                    odiaTitle = "୨୦୦ ସ୍ୱେଚ୍ଛାସେବୀଙ୍କ ଦ୍ୱାରା ବୁଢ଼ାବଳଙ୍ଗ ନଦୀ ତଟ ସଫେଇ ଅଭିଯାନ ସମ୍ପନ୍ନ",
                    snippet = "Citizen volunteers cleared 1.4 tonnes of plastic waste along Balighat ghat and planted 150 native saplings with Municipal support.",
                    content = "Over 200 youth volunteers, senior citizens, and Balasore Municipality sanitation workers joined hands at 6:00 AM at Balighat to clean the Budhabalanga riverfront. Separate collection bins were installed near the ferry point.",
                    category = "Local",
                    locationWard = "Balighat Riverfront, Ward 18, Balasore",
                    authorName = "Balasore Eco Youth Forum",
                    authorContact = "ecoyouth.bls@org.in",
                    sourceVerificationType = "Verified Community Drive",
                    status = NewsModerationStatus.APPROVED.code,
                    moderatorNote = "Verified with Ward 18 Sanitation Officer & published to live feed.",
                    moderatedBy = "Baleshwar 360 Editorial Desk",
                    submittedAt = now - 5 * 3600_000L,
                    moderatedAt = now - 4 * 3600_000L,
                    publishedArticleId = "9101"
                ),
                CommunityNewsSubmissionEntity(
                    id = "sub_seed_rejected_1",
                    title = "Unverified Rumour of 48-Hour NH-16 Highway Closure Near Soro",
                    odiaTitle = "ସୋର ନିକଟରେ ୪୮ ଘଣ୍ଟିଆ ଜାତୀୟ ରାଜପଥ ବନ୍ଦ ଗୁଜବ",
                    snippet = "Forwarded message claiming total closure of NH-16 near Soro flyover for two days.",
                    content = "A social media message claimed all lanes of NH-16 at Soro would remain shut for 48 hours.",
                    category = "Emergency",
                    locationWard = "Soro NH-16 Corridor",
                    authorName = "Anonymous Submitter",
                    authorContact = "",
                    sourceVerificationType = "Unverified Forward",
                    status = NewsModerationStatus.REJECTED.code,
                    moderatorNote = "Rejected by Fact-Check Desk: NHAI Balasore confirmed traffic is moving normally on both carriageways.",
                    moderatedBy = "Baleshwar 360 Fact-Check Desk",
                    submittedAt = now - 9 * 3600_000L,
                    moderatedAt = now - 8 * 3600_000L,
                    publishedArticleId = null
                )
            )
        }
    }
}
