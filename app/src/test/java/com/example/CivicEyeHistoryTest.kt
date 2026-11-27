package com.example

import com.example.data.local.CivicReportEntity
import com.example.data.model.FirestoreUserReport
import com.example.ui.screens.CivicEyeHistoryHelper
import com.example.ui.screens.CivicHistorySortOrder
import com.example.ui.screens.CivicReportStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Locale
import java.util.concurrent.TimeUnit

class CivicEyeHistoryTest {

    @Test
    fun classifyStatus_mapsAllWorkflowVariants() {
        assertEquals(CivicReportStatus.PENDING, CivicEyeHistoryHelper.classifyStatus("PENDING"))
        assertEquals(CivicReportStatus.IN_PROGRESS, CivicEyeHistoryHelper.classifyStatus("IN_PROGRESS"))
        assertEquals(CivicReportStatus.IN_PROGRESS, CivicEyeHistoryHelper.classifyStatus("Assigned to Engineer"))
        assertEquals(CivicReportStatus.RESOLVED, CivicEyeHistoryHelper.classifyStatus("RESOLVED"))
        assertEquals(CivicReportStatus.RESOLVED, CivicEyeHistoryHelper.classifyStatus("Completed"))
        assertEquals(CivicReportStatus.DRAFT, CivicEyeHistoryHelper.classifyStatus("DRAFT"))
        assertEquals(CivicReportStatus.PENDING, CivicEyeHistoryHelper.classifyStatus(null))
    }

    @Test
    fun mergeAndSortReports_sortsNewestAndOldestChronologicallyWithoutDuplicates() {
        val now = 1_760_000_000_000L
        val local = listOf(
            CivicReportEntity(
                id = "rep-1",
                title = "Old Pothole",
                category = "Road Potholes",
                wardLocation = "Ward 4",
                description = "Near market",
                status = "PENDING",
                reportedTimestamp = now - TimeUnit.DAYS.toMillis(3),
                lastUpdatedTimestamp = now - TimeUnit.DAYS.toMillis(3)
            ),
            CivicReportEntity(
                id = "rep-2",
                title = "Latest Drainage",
                category = "Drainage",
                wardLocation = "Ward 12",
                description = "Blocked culvert",
                status = "IN_PROGRESS",
                reportedTimestamp = now - TimeUnit.HOURS.toMillis(2),
                lastUpdatedTimestamp = now - TimeUnit.HOURS.toMillis(1)
            )
        )
        val firestore = listOf(
            FirestoreUserReport(
                id = "rep-2",
                title = "Latest Drainage",
                category = "Drainage",
                wardLocation = "Ward 12",
                description = "Older snapshot",
                status = "PENDING",
                reportedTimestamp = now - TimeUnit.HOURS.toMillis(2),
                lastUpdatedTimestamp = now - TimeUnit.HOURS.toMillis(2)
            ),
            FirestoreUserReport(
                id = "rep-3",
                title = "Middle Sanitation",
                category = "Garbage & Sanitation",
                wardLocation = "Ward 7",
                description = "Overflowing bin",
                status = "RESOLVED",
                reportedTimestamp = now - TimeUnit.DAYS.toMillis(1),
                lastUpdatedTimestamp = now - TimeUnit.HOURS.toMillis(12)
            )
        )

        val newestFirst = CivicEyeHistoryHelper.mergeAndSortReports(
            localReports = local,
            firestoreReports = firestore,
            sortOrder = CivicHistorySortOrder.NEWEST_FIRST
        )
        assertEquals(listOf("rep-2", "rep-3", "rep-1"), newestFirst.map { it.id })
        assertEquals("IN_PROGRESS", newestFirst.first().status)

        val oldestFirst = CivicEyeHistoryHelper.mergeAndSortReports(
            localReports = local,
            firestoreReports = firestore,
            sortOrder = CivicHistorySortOrder.OLDEST_FIRST
        )
        assertEquals(listOf("rep-1", "rep-3", "rep-2"), oldestFirst.map { it.id })
    }

    @Test
    fun filterReports_filtersByStatusAndSearchQuery() {
        val now = 1_760_000_000_000L
        val reports = listOf(
            CivicReportEntity(
                id = "c1",
                title = "Broken Streetlight at Station Square",
                category = "Street Lights",
                wardLocation = "Ward 10",
                description = "Pole dark for 3 nights",
                status = "PENDING",
                reportedTimestamp = now
            ),
            CivicReportEntity(
                id = "c2",
                title = "Garbage Pile Cleaned",
                category = "Sanitation",
                wardLocation = "Ward 14",
                description = "Cleared by municipal tractor",
                status = "RESOLVED",
                reportedTimestamp = now - 60_000L
            )
        )

        val resolvedOnly = CivicEyeHistoryHelper.filterReports(
            reports = reports,
            statusFilter = CivicReportStatus.RESOLVED,
            searchQuery = ""
        )
        assertEquals(listOf("c2"), resolvedOnly.map { it.id })

        val wardSearch = CivicEyeHistoryHelper.filterReports(
            reports = reports,
            statusFilter = null,
            searchQuery = "station square"
        )
        assertEquals(listOf("c1"), wardSearch.map { it.id })
    }

    @Test
    fun buildChronologicalSections_groupsTodayYesterdayAndOlderWithTimestamps() {
        val now = 1_764_200_000_000L
        val reports = listOf(
            CivicReportEntity(
                id = "t1",
                title = "Today Issue",
                category = "Potholes",
                wardLocation = "Ward 1",
                description = "Fresh crater",
                status = "PENDING",
                reportedTimestamp = now - TimeUnit.MINUTES.toMillis(15),
                lastUpdatedTimestamp = now - TimeUnit.MINUTES.toMillis(15)
            ),
            CivicReportEntity(
                id = "y1",
                title = "Yesterday Issue",
                category = "Water Supply",
                wardLocation = "Ward 5",
                description = "Low pressure",
                status = "RESOLVED",
                reportedTimestamp = now - TimeUnit.DAYS.toMillis(1),
                lastUpdatedTimestamp = now - TimeUnit.HOURS.toMillis(18)
            )
        )

        val sections = CivicEyeHistoryHelper.buildChronologicalSections(
            reports = reports,
            nowMillis = now,
            todayLabel = "Today",
            yesterdayLabel = "Yesterday",
            locale = Locale.US
        )

        assertEquals(2, sections.size)
        assertEquals("Today", sections[0].headerLabel)
        assertEquals("15m ago", sections[0].entries.first().relativeTimestampLabel)
        assertNull(sections[0].entries.first().updatedTimestampLabel)
        assertTrue(sections[0].entries.first().exactTimestampLabel.contains("•"))

        assertEquals("Yesterday", sections[1].headerLabel)
        assertEquals(CivicReportStatus.RESOLVED, sections[1].entries.first().status)
        assertNotNull(sections[1].entries.first().updatedTimestampLabel)
    }
}
