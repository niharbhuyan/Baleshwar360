package com.example.data.firebase.dao

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.FirestoreUserReportEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * Data Access Object for the Cloud Firestore 'user_reports' collection.
 * Enables citizens to submit, track, and receive official updates on civic issues in Balasore.
 */
class FirestoreUserReportsDao(private val context: Context) {

    private val db: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed initializing Firestore with named DB: ${e.message}")
            null
        }
    }

    private val reportsCollection
        get() = db?.collection(COLLECTION_NAME)

    /**
     * Real-time Flow of all public citizen reports in Balasore.
     */
    fun observeAllReports(limitCount: Long = 50): Flow<List<FirestoreUserReportEntity>> {
        val col = reportsCollection ?: throw IllegalStateException("Firestore database uninitialized")
        return col.orderBy("reportedTimestamp", Query.Direction.DESCENDING)
            .limit(limitCount)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    FirestoreUserReportEntity.fromFirestoreMap(doc.data ?: emptyMap()).copy(id = doc.id)
                }
            }
            .catch { e ->
                Log.e(TAG, "Error observing all user reports: ${e.message}")
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Real-time Flow of reports submitted specifically by the current user.
     */
    fun observeUserReports(userId: String): Flow<List<FirestoreUserReportEntity>> {
        val col = reportsCollection ?: throw IllegalStateException("Firestore database uninitialized")
        return col.whereEqualTo("userId", userId)
            .orderBy("reportedTimestamp", Query.Direction.DESCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    FirestoreUserReportEntity.fromFirestoreMap(doc.data ?: emptyMap()).copy(id = doc.id)
                }
            }
            .catch { e ->
                Log.e(TAG, "Error observing user reports for $userId: ${e.message}")
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Submits a new citizen civic issue report to Firestore.
     */
    suspend fun submitReport(report: FirestoreUserReportEntity): Result<String> = withContext(Dispatchers.IO) {
        try {
            val col = reportsCollection ?: return@withContext Result.failure(Exception("Firestore not initialized"))
            val docId = report.id.ifBlank { col.document().id }
            val now = System.currentTimeMillis()
            val finalReport = report.copy(
                id = docId,
                reportedTimestamp = if (report.reportedTimestamp > 0) report.reportedTimestamp else now,
                lastUpdatedTimestamp = now
            )
            col.document(docId).set(finalReport.toFirestoreMap(), SetOptions.merge()).await()
            Log.d(TAG, "Citizen civic report submitted successfully: $docId")
            Result.success(docId)
        } catch (e: Exception) {
            Log.e(TAG, "Error submitting civic report: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Updates status (e.g. IN_PROGRESS, RESOLVED) and official resolution notes of a report.
     */
    suspend fun updateReportStatus(
        reportId: String,
        newStatus: String,
        notes: String? = null
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val col = reportsCollection ?: return@withContext Result.failure(Exception("Firestore not initialized"))
            val updates = mutableMapOf<String, Any>(
                "status" to newStatus,
                "lastUpdatedTimestamp" to System.currentTimeMillis()
            )
            if (notes != null) {
                updates["resolutionNotes"] = notes
            }
            col.document(reportId).update(updates).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating report status: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Deletes a user report.
     */
    suspend fun deleteReport(reportId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val col = reportsCollection ?: return@withContext Result.failure(Exception("Firestore not initialized"))
            col.document(reportId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting report $reportId: ${e.message}")
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "FirestoreUserReportsDao"
        const val COLLECTION_NAME = "user_reports"
    }
}
