package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for local citizen civic issue reports stored in Room Database.
 * Supports offline storage, category filtering, a 'sync-when-online' queue, and status progression.
 */
@Dao
interface CivicReportDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReport(report: CivicReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReports(reports: List<CivicReportEntity>)

    @Query("SELECT * FROM civic_reports ORDER BY reportedTimestamp DESC")
    fun getAllReportsFlow(): Flow<List<CivicReportEntity>>

    @Query("SELECT * FROM civic_reports ORDER BY reportedTimestamp DESC")
    suspend fun getAllReportsSync(): List<CivicReportEntity>

    @Query("SELECT * FROM civic_reports WHERE status = :status ORDER BY reportedTimestamp DESC")
    fun getReportsByStatusFlow(status: String): Flow<List<CivicReportEntity>>

    @Query("SELECT * FROM civic_reports WHERE category LIKE '%' || :categoryKeyword || '%' ORDER BY reportedTimestamp DESC")
    fun getReportsByCategoryFlow(categoryKeyword: String): Flow<List<CivicReportEntity>>

    @Query("SELECT * FROM civic_reports WHERE syncStatus = 'QUEUED_OFFLINE' OR status = 'DRAFT' ORDER BY reportedTimestamp DESC")
    fun getSyncWhenOnlineQueueFlow(): Flow<List<CivicReportEntity>>

    @Query("SELECT * FROM civic_reports WHERE syncStatus = 'QUEUED_OFFLINE' OR status = 'DRAFT' ORDER BY reportedTimestamp DESC")
    suspend fun getSyncWhenOnlineQueueSync(): List<CivicReportEntity>

    @Query("SELECT * FROM civic_reports WHERE id = :id LIMIT 1")
    suspend fun getReportById(id: String): CivicReportEntity?

    @Query("UPDATE civic_reports SET status = :newStatus, lastUpdatedTimestamp = :updatedAt, resolutionNotes = :notes WHERE id = :id")
    suspend fun updateReportStatus(id: String, newStatus: String, updatedAt: Long, notes: String)

    @Query("UPDATE civic_reports SET syncStatus = 'SYNCED', status = CASE WHEN status = 'DRAFT' THEN 'PENDING' ELSE status END, lastUpdatedTimestamp = :updatedAt, resolutionNotes = :notes WHERE id = :id")
    suspend fun markReportSynced(id: String, updatedAt: Long, notes: String)

    @Query("DELETE FROM civic_reports WHERE id = :id")
    suspend fun deleteReportById(id: String)

    @Query("DELETE FROM civic_reports")
    suspend fun clearAllReports()

    @Query("UPDATE civic_reports SET upvotesCount = upvotesCount + 1 WHERE id = :id")
    suspend fun incrementUpvotes(id: String)

    @Query("SELECT COUNT(*) FROM civic_reports")
    suspend fun getReportsCount(): Int
}
