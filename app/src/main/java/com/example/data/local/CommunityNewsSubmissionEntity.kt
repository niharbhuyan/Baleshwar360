package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.data.model.NewsArticle

/**
 * Moderation lifecycle status for user-submitted local news stories in BALASORE (Baleshwar 360).
 * Submitted stories start as [PENDING] and are NEVER published to the live news feed until
 * reviewed and marked [APPROVED] by a moderator.
 */
enum class NewsModerationStatus(
    val code: String,
    val labelEn: String,
    val labelOr: String
) {
    PENDING("PENDING", "Pending Review", "ସମୀକ୍ଷା ବାକି"),
    APPROVED("APPROVED", "Approved & Live", "ଅନୁମୋଦିତ ଓ ଲାଇଭ୍"),
    REVISION_REQUESTED("REVISION_REQUESTED", "Revision Needed", "ସଂଶୋଧନ ଆବଶ୍ୟକ"),
    REJECTED("REJECTED", "Rejected", "ପ୍ରତ୍ୟାଖ୍ୟାତ");

    companion object {
        fun fromCode(code: String?): NewsModerationStatus {
            return entries.firstOrNull { it.code.equals(code, ignoreCase = true) } ?: PENDING
        }
    }
}

/**
 * Room Database entity storing citizen-submitted local news stories and their editorial moderation state.
 */
@Entity(
    tableName = "community_news_submissions",
    indices = [
        Index("status"),
        Index("category"),
        Index("submittedAt")
    ]
)
data class CommunityNewsSubmissionEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val odiaTitle: String = "",
    val snippet: String,
    val content: String,
    val category: String = "Local",
    val locationWard: String = "Balasore Town",
    val authorName: String = "Balasore Citizen Reporter",
    val authorContact: String = "",
    val sourceVerificationType: String = "Eyewitness Report",
    val photoUri: String? = null,
    val status: String = NewsModerationStatus.PENDING.code, // "PENDING", "APPROVED", "REVISION_REQUESTED", "REJECTED"
    val moderatorNote: String = "Submitted by citizen reporter — awaiting editorial review before going live.",
    val moderatedBy: String? = null,
    val submittedAt: Long = System.currentTimeMillis(),
    val moderatedAt: Long? = null,
    val publishedArticleId: String? = null,
    val isBreakingCandidate: Boolean = false
) {
    val moderationStatus: NewsModerationStatus
        get() = NewsModerationStatus.fromCode(status)

    val isLive: Boolean
        get() = moderationStatus == NewsModerationStatus.APPROVED

    val isPendingReview: Boolean
        get() = moderationStatus == NewsModerationStatus.PENDING

    /**
     * Converts an approved citizen news submission into a live [NewsArticle] for the public feed.
     */
    fun toLiveNewsArticle(): NewsArticle {
        val resolvedId = publishedArticleId?.takeIf { it.isNotBlank() }
            ?: (9000L + kotlin.math.abs(id.hashCode() % 90000)).toString()
        val verifiedSource = "Citizen Desk • ${authorName.ifBlank { "Balasore Resident" }} ($locationWard)"
        val effectiveOdiaTitle = odiaTitle.ifBlank { title }
        val fullBody = buildString {
            append(content.ifBlank { snippet })
            if (locationWard.isNotBlank()) {
                append("\n\n📍 Location: $locationWard")
            }
            append("\n✓ Verified by ${moderatedBy ?: "Baleshwar 360 Editorial Moderation Desk"}")
            if (sourceVerificationType.isNotBlank()) {
                append(" ($sourceVerificationType)")
            }
        }
        return NewsArticle(
            id = resolvedId,
            title = title,
            odiaTitle = effectiveOdiaTitle,
            snippet = snippet,
            odiaSnippet = snippet,
            category = category,
            timeAgo = "Verified • Just now",
            source = verifiedSource,
            isBookmarked = false,
            content = fullBody,
            odiaContent = fullBody
        )
    }
}
