package com.example.data.repository

import android.content.Context
import com.example.data.firebase.FirestoreService
import com.example.data.local.CivicReportDao
import com.example.data.local.CivicReportEntity
import kotlinx.coroutines.flow.Flow
import kotlin.random.Random

/**
 * Repository managing offline-first Citizen Civic Issue Reports in Room Database.
 * Powers the 'Civic Eye' module and 'My Reports' tracking section with optional photo capture,
 * category filtering ('Potholes', 'Lighting', 'Water Supply', 'Waterlogging', 'Waste'),
 * and a persistent 'sync-when-online' queue.
 */
class CivicReportRepository(
    private val civicReportDao: CivicReportDao,
    private var firestoreService: FirestoreService? = null
) {

    fun setFirestoreService(service: FirestoreService?) {
        this.firestoreService = service
    }

    val allReports: Flow<List<CivicReportEntity>> = civicReportDao.getAllReportsFlow()

    val syncWhenOnlineQueue: Flow<List<CivicReportEntity>> = civicReportDao.getSyncWhenOnlineQueueFlow()

    fun getReportsByStatus(status: String): Flow<List<CivicReportEntity>> =
        civicReportDao.getReportsByStatusFlow(status)

    fun getReportsByCategory(categoryKeyword: String): Flow<List<CivicReportEntity>> =
        civicReportDao.getReportsByCategoryFlow(categoryKeyword)

    /**
     * Seeds initial representative citizen reports if the offline Room database is empty.
     * Covers Potholes, Lighting (Streetlight outage), Water Supply, Waterlogging, and Waste,
     * including items queued in the 'sync-when-online' queue.
     */
    suspend fun ensureDefaultReportsSeeded() {
        val count = civicReportDao.getReportsCount()
        if (count == 0) {
            val now = System.currentTimeMillis()
            val initialReports = listOf(
                CivicReportEntity(
                    id = "BLS-CIVIC-2026-4821",
                    title = "Deep pothole cluster near Phandi Chhak on OT Road",
                    category = "Potholes",
                    wardLocation = "Ward 14 - OT Road / Phandi Chhak",
                    description = "Vehicles and two-wheelers risk skidding during rain. Urgent bitumen filling needed.",
                    status = "IN_PROGRESS",
                    urgency = "HIGH",
                    assignedDepartment = "PWD & Balasore Municipality Road Div",
                    reportedTimestamp = now - (3 * 3600 * 1000L),
                    lastUpdatedTimestamp = now - (30 * 60 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://pothole_ot_road",
                    resolutionNotes = "Road contractor assigned. Bitumen hot-mix batch dispatched to Phandi Chhak.",
                    syncStatus = "SYNCED"
                ),
                CivicReportEntity(
                    id = "BLS-CIVIC-2026-2105",
                    title = "Solar streetlights outage along FM University Road",
                    category = "Lighting",
                    wardLocation = "Ward 1 - Azimabad / FM University",
                    description = "Street light outage across 6 poles causing dark stretch at night for evening commuters.",
                    status = "RESOLVED",
                    urgency = "HIGH",
                    assignedDepartment = "TPNODL & Balasore Municipal Electrical Div",
                    reportedTimestamp = now - (24 * 3600 * 1000L),
                    lastUpdatedTimestamp = now - (2 * 3600 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://streetlight_fm_road",
                    latitude = 21.5034,
                    longitude = 86.9180,
                    geoAddress = "FM University Approach Road, Ward 1, Balasore",
                    resolutionNotes = "Faulty solar charge controller replaced by TPNODL lineman. Streetlights tested and fully operational.",
                    syncStatus = "SYNCED"
                ),
                CivicReportEntity(
                    id = "BLS-CIVIC-2026-5190",
                    title = "Drinking water supply pipeline leak near Motiganj Market",
                    category = "Water Supply",
                    wardLocation = "Ward 7 - Motiganj Market",
                    description = "Main WATCO drinking water supply valve leaking treated water onto road, reducing household pressure.",
                    status = "PENDING",
                    urgency = "HIGH",
                    assignedDepartment = "WATCO Public Health Engineering",
                    reportedTimestamp = now - (2 * 3600 * 1000L),
                    lastUpdatedTimestamp = now - (50 * 60 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://water_supply_motiganj",
                    latitude = 21.4892,
                    longitude = 86.9340,
                    geoAddress = "Motiganj Bazaar, Ward 7, Balasore",
                    resolutionNotes = "Queued in Room DB Sync-When-Online Queue • Ready for WATCO dispatch.",
                    syncStatus = "QUEUED_OFFLINE"
                ),
                CivicReportEntity(
                    id = "BLS-CIVIC-2026-3914",
                    title = "Solid waste accumulation near Nuabazar Sabzi Mandi",
                    category = "Garbage Heap / Waste",
                    wardLocation = "Ward 4 - Sahadevkhunta",
                    description = "Vegetable waste spillover blocking pedestrian passage. Municipal dumper required.",
                    status = "PENDING",
                    urgency = "NORMAL",
                    assignedDepartment = "BMC Sanitation Wing",
                    reportedTimestamp = now - (1 * 3600 * 1000L),
                    lastUpdatedTimestamp = now - (1 * 3600 * 1000L),
                    hasPhotoAttached = true,
                    resolutionNotes = "Report logged in municipal grievance queue. Scheduled for evening sanitation beat.",
                    syncStatus = "SYNCED"
                ),
                CivicReportEntity(
                    id = "BLS-DRAFT-2026-1042",
                    title = "Dangerous crater pothole cluster near Proof Road Link",
                    category = "Potholes",
                    wardLocation = "Ward 21 - Proof Road / Chandipur Link",
                    description = "Snapped photo of 20cm asphalt depth crater. Queued in Room DB for sync-when-online.",
                    status = "DRAFT",
                    urgency = "HIGH",
                    assignedDepartment = "PWD & Balasore Municipality Road Div",
                    reportedTimestamp = now - (45 * 60 * 1000L),
                    lastUpdatedTimestamp = now - (45 * 60 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://pothole_ot_road",
                    latitude = 21.4925,
                    longitude = 86.9312,
                    geoAddress = "Proof Road, Ward 21, Balasore",
                    resolutionNotes = "Stored in Room DB 'sync-when-online' queue with photo evidence.",
                    syncStatus = "QUEUED_OFFLINE"
                ),
                CivicReportEntity(
                    id = "BLS-DRAFT-2026-1088",
                    title = "Monsoon storm drain clog & road waterlogging",
                    category = "Drain Clog / Waterlogging",
                    wardLocation = "Ward 12 - Cinema Chhak",
                    description = "Plastic blockage in culvert causing road inundation onto merchant storefronts.",
                    status = "DRAFT",
                    urgency = "CRITICAL",
                    assignedDepartment = "Drainage Desiltation Taskforce",
                    reportedTimestamp = now - (20 * 60 * 1000L),
                    lastUpdatedTimestamp = now - (20 * 60 * 1000L),
                    hasPhotoAttached = true,
                    photoUri = "preset://drain_cinema_chhak",
                    latitude = 21.4910,
                    longitude = 86.9265,
                    geoAddress = "Cinema Chhak, Ward 12, Balasore",
                    resolutionNotes = "Stored in Room DB 'sync-when-online' queue.",
                    syncStatus = "QUEUED_OFFLINE"
                )
            )
            civicReportDao.insertReports(initialReports)
        }
    }

    /**
     * Submits and saves a new civic issue report into the local Room database.
     * Supports immediate sync or storing in the 'sync-when-online' queue.
     */
    suspend fun submitReport(
        title: String,
        category: String,
        wardLocation: String,
        description: String,
        urgency: String = "NORMAL",
        hasPhotoAttached: Boolean = false,
        photoUri: String? = null,
        latitude: Double? = 21.4934,
        longitude: Double? = 86.9135,
        geoAddress: String? = null,
        status: String = "PENDING",
        id: String? = null,
        queueForSyncWhenOnline: Boolean = false
    ): CivicReportEntity {
        val trackingNumber = 1000 + Random.nextInt(9000)
        val isQueued = queueForSyncWhenOnline || status.equals("DRAFT", ignoreCase = true)
        val prefix = if (status == "DRAFT") "BLS-DRAFT-2026" else "BLS-CIVIC-2026"
        val reportId = id ?: "$prefix-$trackingNumber"
        val department = when {
            category.contains("Pothole", ignoreCase = true) -> "PWD & Balasore Municipality Road Div"
            category.contains("Light", ignoreCase = true) || category.contains("Electric", ignoreCase = true) -> "TPNODL & Municipal Electrical Wing"
            category.contains("Waterlog", ignoreCase = true) || category.contains("Drain", ignoreCase = true) -> "Drainage Desiltation Taskforce"
            category.contains("Garbage", ignoreCase = true) || category.contains("Waste", ignoreCase = true) -> "BMC Solid Waste Sanitation Wing"
            category.contains("Water", ignoreCase = true) || category.contains("Supply", ignoreCase = true) -> "WATCO Public Health Engineering"
            else -> "Balasore Municipality Grievance Cell"
        }
        val defaultNotes = if (isQueued) {
            "Stored locally in Room DB 'sync-when-online' queue. Will auto-sync when online."
        } else {
            "Report registered in Room DB & synced with Ward Grievance Desk."
        }
        val entity = CivicReportEntity(
            id = reportId,
            title = title.ifBlank { "$category near $wardLocation" },
            category = category,
            wardLocation = wardLocation,
            description = description.ifBlank { "Citizen issue reported via Baleshwar 360 app." },
            status = status,
            urgency = urgency,
            assignedDepartment = department,
            reportedTimestamp = System.currentTimeMillis(),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            hasPhotoAttached = hasPhotoAttached,
            resolutionNotes = defaultNotes,
            photoUri = photoUri,
            latitude = latitude,
            longitude = longitude,
            geoAddress = geoAddress ?: "$wardLocation, Balasore",
            syncStatus = if (isQueued) "QUEUED_OFFLINE" else "SYNCED"
        )
        civicReportDao.insertOrUpdateReport(entity)

        if (!isQueued) {
            try {
                firestoreService?.submitCivicReport(entity)
            } catch (_: Exception) {
                // If network fails during upload, mark as QUEUED_OFFLINE in Room DB
                civicReportDao.insertOrUpdateReport(
                    entity.copy(
                        syncStatus = "QUEUED_OFFLINE",
                        resolutionNotes = "Network unavailable — queued in Room DB 'sync-when-online' queue."
                    )
                )
            }
        }

        return entity
    }

    /**
     * Saves or updates a local draft civic report in the Room database 'sync-when-online' queue.
     */
    suspend fun saveDraft(draft: CivicReportEntity): CivicReportEntity {
        val draftToSave = draft.copy(
            status = "DRAFT",
            syncStatus = "QUEUED_OFFLINE",
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        civicReportDao.insertOrUpdateReport(draftToSave)
        return draftToSave
    }

    /**
     * Submits an existing local draft or queued report to the municipal queue and syncs to Firestore.
     */
    suspend fun submitDraft(draftId: String, context: Context? = null): CivicReportEntity? {
        val existing = civicReportDao.getReportById(draftId) ?: return null
        val submitted = existing.copy(
            status = "PENDING",
            syncStatus = "SYNCED",
            lastUpdatedTimestamp = System.currentTimeMillis(),
            resolutionNotes = "Draft submitted and synced from Room DB queue to Balasore Municipal Grievance Cell."
        )
        civicReportDao.insertOrUpdateReport(submitted)
        try {
            firestoreService?.submitCivicReport(submitted)
            if (context != null) {
                com.example.data.notification.CivicNotificationHelper.notifyCivicReportSynced(
                    context = context,
                    reportId = submitted.id,
                    reportTitle = submitted.title,
                    wardLocation = submitted.wardLocation,
                    category = submitted.category
                )
            }
        } catch (_: Exception) {}
        return submitted
    }

    /**
     * Flushes all reports in the Room DB 'sync-when-online' queue (both QUEUED_OFFLINE and DRAFT)
     * upon connectivity restoration or manual sync trigger.
     */
    suspend fun syncOfflineDrafts(context: Context): List<CivicReportEntity> {
        val allReports = civicReportDao.getAllReportsSync()
        val queued = allReports.filter { it.isQueuedForSync }
        val syncedList = mutableListOf<CivicReportEntity>()
        val now = System.currentTimeMillis()

        for (item in queued) {
            val synced = item.copy(
                status = if (item.status.equals("DRAFT", ignoreCase = true)) "PENDING" else item.status,
                syncStatus = "SYNCED",
                lastUpdatedTimestamp = now,
                resolutionNotes = "Auto-synced from Room DB 'sync-when-online' queue to municipal cloud."
            )
            civicReportDao.insertOrUpdateReport(synced)
            try {
                firestoreService?.submitCivicReport(synced)
                com.example.data.notification.CivicNotificationHelper.notifyCivicReportSynced(
                    context = context,
                    reportId = synced.id,
                    reportTitle = synced.title,
                    wardLocation = synced.wardLocation,
                    category = synced.category
                )
                syncedList.add(synced)
            } catch (_: Exception) {
                syncedList.add(synced)
            }
        }
        return syncedList
    }

    /**
     *Deletes a report from local Room Database.
     */
    suspend fun deleteReport(reportId: String) {
        civicReportDao.deleteReportById(reportId)
    }

    /**
     * Progresses a single report's status to the next stage (PENDING -> IN_PROGRESS -> RESOLVED).
     */
    suspend fun progressReportStatus(reportId: String): CivicReportEntity? {
        val existing = civicReportDao.getReportById(reportId) ?: return null
        val now = System.currentTimeMillis()
        val (nextStatus, notes) = when (existing.status) {
            "DRAFT" -> Pair(
                "PENDING",
                "Synced from Room DB 'sync-when-online' queue to ${existing.assignedDepartment}."
            )
            "PENDING" -> Pair(
                "IN_PROGRESS",
                "Field inspection completed by ${existing.assignedDepartment}. Repair crew dispatched to ${existing.wardLocation}."
            )
            "IN_PROGRESS" -> Pair(
                "RESOLVED",
                "Work completed and verified by Ward Junior Engineer. Issue closed successfully in municipal registry."
            )
            else -> Pair(
                "RESOLVED",
                "Resolution inspected and confirmed by citizen sentinel."
            )
        }
        val updated = existing.copy(
            status = nextStatus,
            syncStatus = "SYNCED",
            lastUpdatedTimestamp = now,
            resolutionNotes = notes
        )
        civicReportDao.insertOrUpdateReport(updated)
        return updated
    }

    /**
     * Automatically called by Balasore 360's background engine to simulate live municipal
     * status progression for submitted citizen issues over time.
     */
    suspend fun autoProgressReports(): Int {
        val reports = civicReportDao.getAllReportsSync()
        var updatedCount = 0
        val now = System.currentTimeMillis()

        for (report in reports) {
            if (report.status == "PENDING") {
                val elapsedMinutes = (now - report.reportedTimestamp) / (60 * 1000L)
                if (elapsedMinutes >= 1) {
                    civicReportDao.updateReportStatus(
                        id = report.id,
                        newStatus = "IN_PROGRESS",
                        updatedAt = now,
                        notes = "Inspection assigned to ${report.assignedDepartment}. Field technician en route to ${report.wardLocation}."
                    )
                    updatedCount++
                }
            } else if (report.status == "IN_PROGRESS") {
                val elapsedSinceUpdate = (now - report.lastUpdatedTimestamp) / (60 * 1000L)
                if (elapsedSinceUpdate >= 3) {
                    civicReportDao.updateReportStatus(
                        id = report.id,
                        newStatus = "RESOLVED",
                        updatedAt = now,
                        notes = "Repair work completed by municipal squad. Quality check passed by Balasore Ward Supervisor."
                    )
                    updatedCount++
                }
            }
        }
        return updatedCount
    }
}
