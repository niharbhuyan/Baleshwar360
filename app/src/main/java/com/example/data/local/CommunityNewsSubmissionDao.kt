package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO for storing, querying, and moderating citizen-submitted local news stories.
 */
@Dao
interface CommunityNewsSubmissionDao {

    @Query("SELECT * FROM community_news_submissions ORDER BY CASE status WHEN 'PENDING' THEN 0 WHEN 'REVISION_REQUESTED' THEN 1 WHEN 'APPROVED' THEN 2 ELSE 3 END, submittedAt DESC")
    fun getAllSubmissionsFlow(): Flow<List<CommunityNewsSubmissionEntity>>

    @Query("SELECT * FROM community_news_submissions ORDER BY submittedAt DESC")
    suspend fun getAllSubmissionsSync(): List<CommunityNewsSubmissionEntity>

    @Query("SELECT * FROM community_news_submissions WHERE status = :status ORDER BY submittedAt DESC")
    fun getSubmissionsByStatusFlow(status: String): Flow<List<CommunityNewsSubmissionEntity>>

    @Query("SELECT * FROM community_news_submissions WHERE status = 'PENDING' ORDER BY submittedAt DESC")
    fun getPendingSubmissionsFlow(): Flow<List<CommunityNewsSubmissionEntity>>

    @Query("SELECT * FROM community_news_submissions WHERE status = 'PENDING' ORDER BY submittedAt DESC")
    suspend fun getPendingSubmissionsSync(): List<CommunityNewsSubmissionEntity>

    @Query("SELECT * FROM community_news_submissions WHERE status = 'APPROVED' ORDER BY moderatedAt DESC, submittedAt DESC")
    fun getApprovedSubmissionsFlow(): Flow<List<CommunityNewsSubmissionEntity>>

    @Query("SELECT * FROM community_news_submissions WHERE status = 'APPROVED' ORDER BY moderatedAt DESC, submittedAt DESC")
    suspend fun getApprovedSubmissionsSync(): List<CommunityNewsSubmissionEntity>

    @Query("SELECT * FROM community_news_submissions WHERE id = :id LIMIT 1")
    suspend fun getSubmissionByIdSync(id: String): CommunityNewsSubmissionEntity?

    @Query("SELECT COUNT(*) FROM community_news_submissions")
    suspend fun getCount(): Int

    @Query("SELECT COUNT(*) FROM community_news_submissions WHERE status = 'PENDING'")
    fun getPendingCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmission(submission: CommunityNewsSubmissionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubmissions(submissions: List<CommunityNewsSubmissionEntity>)

    @Update
    suspend fun updateSubmission(submission: CommunityNewsSubmissionEntity): Int

    @Query(
        "UPDATE community_news_submissions SET status = :status, moderatorNote = :moderatorNote, moderatedBy = :moderatedBy, moderatedAt = :moderatedAt, publishedArticleId = :publishedArticleId WHERE id = :id"
    )
    suspend fun updateModerationDecision(
        id: String,
        status: String,
        moderatorNote: String,
        moderatedBy: String,
        moderatedAt: Long,
        publishedArticleId: String?
    ): Int

    @Query("DELETE FROM community_news_submissions WHERE id = :id")
    suspend fun deleteSubmissionById(id: String): Int

    @Query("DELETE FROM community_news_submissions")
    suspend fun clearAll(): Int
}
