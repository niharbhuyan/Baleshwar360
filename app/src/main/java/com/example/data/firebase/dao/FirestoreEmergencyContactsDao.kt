package com.example.data.firebase.dao

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.model.FirestoreEmergencyContactEntity
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
 * Data Access Object for the Cloud Firestore 'emergency_contacts' collection.
 * Provides real-time observation, category filtering, and helpline verification.
 */
class FirestoreEmergencyContactsDao(private val context: Context) {

    private val db: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w(TAG, "Failed initializing Firestore with named DB: ${e.message}")
            null
        }
    }

    private val emergencyCollection
        get() = db?.collection(COLLECTION_NAME)

    /**
     * Real-time Flow observing all official emergency services sorted by priority.
     */
    fun observeAllContacts(): Flow<List<FirestoreEmergencyContactEntity>> {
        val col = emergencyCollection ?: throw IllegalStateException("Firestore database uninitialized")
        return col.orderBy("priority", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    FirestoreEmergencyContactEntity.fromFirestoreMap(doc.data ?: emptyMap()).copy(id = doc.id)
                }
            }
            .catch { e ->
                Log.e(TAG, "Error observing emergency contacts: ${e.message}")
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * Real-time Flow observing emergency contacts by specific department category.
     */
    fun observeContactsByCategory(category: String): Flow<List<FirestoreEmergencyContactEntity>> {
        val col = emergencyCollection ?: throw IllegalStateException("Firestore database uninitialized")
        return col.whereEqualTo("category", category)
            .orderBy("priority", Query.Direction.ASCENDING)
            .snapshots()
            .map { snapshot ->
                snapshot.documents.map { doc ->
                    FirestoreEmergencyContactEntity.fromFirestoreMap(doc.data ?: emptyMap()).copy(id = doc.id)
                }
            }
            .catch { e ->
                Log.e(TAG, "Error observing contacts by category $category: ${e.message}")
                emit(emptyList())
            }
            .flowOn(Dispatchers.IO)
    }

    /**
     * One-shot fetch of all emergency contacts.
     */
    suspend fun fetchAllContacts(): Result<List<FirestoreEmergencyContactEntity>> = withContext(Dispatchers.IO) {
        try {
            val col = emergencyCollection ?: return@withContext Result.failure(Exception("Firestore not initialized"))
            val snapshot = col.orderBy("priority", Query.Direction.ASCENDING).get().await()
            val list = snapshot.documents.map { doc ->
                FirestoreEmergencyContactEntity.fromFirestoreMap(doc.data ?: emptyMap()).copy(id = doc.id)
            }
            Result.success(list)
        } catch (e: Exception) {
            Log.e(TAG, "fetchAllContacts error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Inserts or updates an emergency service contact in Firestore.
     */
    suspend fun saveContact(contact: FirestoreEmergencyContactEntity): Result<String> = withContext(Dispatchers.IO) {
        try {
            val col = emergencyCollection ?: return@withContext Result.failure(Exception("Firestore not initialized"))
            val docId = contact.id.ifBlank { col.document().id }
            val itemToSave = contact.copy(id = docId, updatedAt = System.currentTimeMillis())
            col.document(docId).set(itemToSave.toFirestoreMap(), SetOptions.merge()).await()
            Log.d(TAG, "Emergency contact saved: $docId")
            Result.success(docId)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving emergency contact: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Seeds initial verified Balasore emergency services (FM MCH, DHH, Town Police, Marine Police, Shelters)
     * into Firestore if the collection is currently empty.
     */
    suspend fun seedInitialContactsIfEmpty(contacts: List<FirestoreEmergencyContactEntity>): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val col = emergencyCollection ?: return@withContext Result.failure(Exception("Firestore not initialized"))
            val existing = col.limit(1).get().await()
            if (!existing.isEmpty) {
                return@withContext Result.success(0)
            }

            var count = 0
            val batch = db?.batch() ?: return@withContext Result.failure(Exception("Firestore batch unavailable"))
            for (contact in contacts) {
                val docId = contact.id.ifBlank { col.document().id }
                val docRef = col.document(docId)
                batch.set(docRef, contact.copy(id = docId).toFirestoreMap(), SetOptions.merge())
                count++
            }
            batch.commit().await()
            Log.i(TAG, "Successfully seeded $count emergency contacts into Firestore 'emergency_contacts'")
            Result.success(count)
        } catch (e: Exception) {
            Log.w(TAG, "Failed seeding emergency contacts: ${e.message}")
            Result.failure(e)
        }
    }

    companion object {
        private const val TAG = "FirestoreEmergencyContactsDao"
        const val COLLECTION_NAME = "emergency_contacts"
    }
}
