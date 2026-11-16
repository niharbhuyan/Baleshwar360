package com.example

import com.example.data.local.CivicReportDao
import com.example.data.local.CivicReportEntity
import com.example.data.repository.CivicReportRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying the Civic Eye reporting feature:
 * 1. Snapped camera photo evidence tagging with geolocation coordinates.
 * 2. Saving local drafts in Room database (status = "DRAFT").
 * 3. Editing existing drafts and updating persisted records.
 * 4. Submitting saved drafts to municipal registry (transitioning to "PENDING").
 */
class CivicEyeDraftAndCameraUnitTest {

    private lateinit var fakeDao: FakeCivicReportDao
    private lateinit var repository: CivicReportRepository

    class FakeCivicReportDao : CivicReportDao {
        val storage = mutableMapOf<String, CivicReportEntity>()

        override suspend fun insertOrUpdateReport(report: CivicReportEntity) {
            storage[report.id] = report
        }

        override suspend fun insertReports(reports: List<CivicReportEntity>) {
            reports.forEach { storage[it.id] = it }
        }

        override fun getAllReportsFlow(): Flow<List<CivicReportEntity>> {
            return flowOf(storage.values.toList())
        }

        override suspend fun getAllReportsSync(): List<CivicReportEntity> {
            return storage.values.toList()
        }

        override fun getReportsByStatusFlow(status: String): Flow<List<CivicReportEntity>> {
            return flowOf(storage.values.filter { it.status == status })
        }

        override fun getReportsByCategoryFlow(categoryKeyword: String): Flow<List<CivicReportEntity>> {
            return flowOf(storage.values.filter { it.category.contains(categoryKeyword, ignoreCase = true) })
        }

        override fun getSyncWhenOnlineQueueFlow(): Flow<List<CivicReportEntity>> {
            return flowOf(storage.values.filter { it.isQueuedForSync })
        }

        override suspend fun getSyncWhenOnlineQueueSync(): List<CivicReportEntity> {
            return storage.values.filter { it.isQueuedForSync }
        }

        override suspend fun getReportById(id: String): CivicReportEntity? {
            return storage[id]
        }

        override suspend fun updateReportStatus(
            id: String,
            newStatus: String,
            updatedAt: Long,
            notes: String
        ) {
            val existing = storage[id]
            if (existing != null) {
                storage[id] = existing.copy(
                    status = newStatus,
                    lastUpdatedTimestamp = updatedAt,
                    resolutionNotes = notes
                )
            }
        }

        override suspend fun markReportSynced(id: String, updatedAt: Long, notes: String) {
            val existing = storage[id]
            if (existing != null) {
                storage[id] = existing.copy(
                    status = if (existing.status == "DRAFT") "PENDING" else existing.status,
                    syncStatus = "SYNCED",
                    lastUpdatedTimestamp = updatedAt,
                    resolutionNotes = notes
                )
            }
        }

        override suspend fun deleteReportById(id: String) {
            storage.remove(id)
        }

        override suspend fun clearAllReports() {
            storage.clear()
        }

        override suspend fun getReportsCount(): Int {
            return storage.size
        }

        override suspend fun incrementUpvotes(id: String) {
            val existing = storage[id]
            if (existing != null) {
                storage[id] = existing.copy(upvotesCount = existing.upvotesCount + 1)
            }
        }
    }

    @Before
    fun setUp() {
        fakeDao = FakeCivicReportDao()
        repository = CivicReportRepository(fakeDao)
    }

    @Test
    fun testSaveDraftWithCameraPhotoAndGeolocation() = runBlocking {
        val draftEntity = CivicReportEntity(
            id = "BLS-DRAFT-1001",
            title = "Hazardous road pothole at Phandi Chhak",
            category = "Road Pothole",
            wardLocation = "Ward 14 - OT Road / Phandi Chhak",
            description = "Camera snapped photo of 15cm pothole deep crater.",
            status = "DRAFT",
            urgency = "HIGH",
            hasPhotoAttached = true,
            photoUri = "file:///data/user/0/com.aistudio.cleanbalasore.kxmpzq/files/civic_eye_1729850000.jpg",
            latitude = 21.4925,
            longitude = 86.9312,
            geoAddress = "OT Road, Phandi Chhak, Ward 14, Balasore",
            reportedTimestamp = System.currentTimeMillis(),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            assignedDepartment = "Balasore Municipality PWD",
            resolutionNotes = "Local draft recorded for later submission."
        )

        val saved = repository.saveDraft(draftEntity)

        assertEquals("DRAFT", saved.status)
        assertEquals("BLS-DRAFT-1001", saved.id)
        assertTrue("Draft must have photo attached flag set", saved.hasPhotoAttached)
        assertNotNull("Draft photo URI must not be null", saved.photoUri)
        assertTrue("Photo URI should reference local file", saved.photoUri!!.startsWith("file://"))
        assertEquals(21.4925, saved.latitude!!, 0.0001)
        assertEquals(86.9312, saved.longitude!!, 0.0001)

        val fromDb = fakeDao.getReportById("BLS-DRAFT-1001")
        assertNotNull(fromDb)
        assertEquals("DRAFT", fromDb!!.status)
        assertEquals("OT Road, Phandi Chhak, Ward 14, Balasore", fromDb.geoAddress)
    }

    @Test
    fun testUpdateExistingDraft() = runBlocking {
        val initialDraft = CivicReportEntity(
            id = "BLS-DRAFT-1002",
            title = "Drainage overflow near Cinema Chhak",
            category = "Drain Clog / Waterlogging",
            wardLocation = "Ward 12 - Cinema Chhak",
            description = "Initial draft without photo.",
            status = "DRAFT",
            urgency = "NORMAL",
            hasPhotoAttached = false,
            photoUri = null,
            latitude = 21.4910,
            longitude = 86.9265,
            geoAddress = "Cinema Chhak, Ward 12, Balasore",
            reportedTimestamp = System.currentTimeMillis(),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            assignedDepartment = "Drainage Taskforce",
            resolutionNotes = "Draft pending review."
        )

        repository.saveDraft(initialDraft)

        // User snaps camera photo and updates draft
        val updatedDraft = initialDraft.copy(
            hasPhotoAttached = true,
            photoUri = "file:///data/user/0/com.aistudio.cleanbalasore.kxmpzq/files/civic_eye_snapped.jpg",
            description = "Snapped camera evidence showing overflowing drain after heavy rain."
        )
        repository.saveDraft(updatedDraft)

        val fromDb = fakeDao.getReportById("BLS-DRAFT-1002")
        assertNotNull(fromDb)
        assertEquals("DRAFT", fromDb!!.status)
        assertTrue(fromDb.hasPhotoAttached)
        assertEquals("file:///data/user/0/com.aistudio.cleanbalasore.kxmpzq/files/civic_eye_snapped.jpg", fromDb.photoUri)
        assertEquals("Snapped camera evidence showing overflowing drain after heavy rain.", fromDb.description)
    }

    @Test
    fun testSubmitDraftTransitionsToPending() = runBlocking {
        val draftEntity = CivicReportEntity(
            id = "BLS-DRAFT-1003",
            title = "Waterlogging at Station Square",
            category = "Drain Clog / Waterlogging",
            wardLocation = "Ward 18 - Station Square",
            description = "Flooding near auto stand.",
            status = "DRAFT",
            urgency = "CRITICAL",
            hasPhotoAttached = true,
            photoUri = "file:///data/user/0/com.aistudio.cleanbalasore.kxmpzq/files/civic_eye_station.jpg",
            latitude = 21.4990,
            longitude = 86.9350,
            geoAddress = "Balasore Railway Station Road, Ward 18, Balasore",
            reportedTimestamp = System.currentTimeMillis(),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            assignedDepartment = "Drainage Taskforce",
            resolutionNotes = "Local draft."
        )

        repository.saveDraft(draftEntity)
        assertEquals("DRAFT", fakeDao.getReportById("BLS-DRAFT-1003")?.status)

        // Submit the draft
        val submitted = repository.submitDraft("BLS-DRAFT-1003")
        assertNotNull(submitted)
        assertEquals("PENDING", submitted!!.status)
        assertTrue(submitted.resolutionNotes.contains("Draft submitted"))

        val inDb = fakeDao.getReportById("BLS-DRAFT-1003")
        assertNotNull(inDb)
        assertEquals("PENDING", inDb!!.status)
        // Geolocation and photo evidence are preserved
        assertEquals(21.4990, inDb.latitude!!, 0.0001)
        assertEquals("file:///data/user/0/com.aistudio.cleanbalasore.kxmpzq/files/civic_eye_station.jpg", inDb.photoUri)
    }

    @Test
    fun testSubmitNonExistentDraftReturnsNull() = runBlocking {
        val result = repository.submitDraft("NON-EXISTENT-ID")
        assertNull(result)
    }

    @Test
    fun testDeleteDraft() = runBlocking {
        val draftEntity = CivicReportEntity(
            id = "BLS-DRAFT-1004",
            title = "Garbage pile draft",
            category = "Garbage Heap / Waste",
            wardLocation = "Ward 7 - Motiganj",
            description = "Draft to be deleted",
            status = "DRAFT",
            urgency = "NORMAL",
            hasPhotoAttached = false,
            photoUri = null,
            latitude = 21.4942,
            longitude = 86.9205,
            geoAddress = "Motiganj Market, Ward 7, Balasore",
            reportedTimestamp = System.currentTimeMillis(),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            assignedDepartment = "BMC Sanitation Wing",
            resolutionNotes = "Draft."
        )

        repository.saveDraft(draftEntity)
        assertNotNull(fakeDao.getReportById("BLS-DRAFT-1004"))

        repository.deleteReport("BLS-DRAFT-1004")
        assertNull(fakeDao.getReportById("BLS-DRAFT-1004"))
    }
}
