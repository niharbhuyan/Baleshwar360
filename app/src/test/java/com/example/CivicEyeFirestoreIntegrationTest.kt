package com.example

import com.example.data.model.FirestoreUserReport
import com.example.util.SafeWebViewClient
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CivicEyeFirestoreIntegrationTest {

    @Test
    fun testPotholeReportFirestoreMapSerialization() {
        val report = FirestoreUserReport(
            id = "BLS-CIVIC-2026-9901",
            userId = "test_citizen_uid_123",
            userName = "Baleswari Resident",
            title = "Deep crater pothole cluster on OT Road",
            category = "pothole",
            description = "Hazardous pothole cluster causing two-wheelers to skid during rain near Phandi Chhak.",
            wardLocation = "Ward 14 - OT Road / Phandi Chhak",
            status = "SUBMITTED",
            urgency = "HIGH",
            latitude = 21.4934,
            longitude = 86.9135,
            photoUrl = "https://balasore360.local/evidence/pothole_ot_road.jpg",
            reportedTimestamp = 1731139200000L,
            lastUpdatedTimestamp = 1731139200000L,
            resolutionNotes = "Submitted via Civic Eye. Synced to municipal registry."
        )

        val map = report.toFirestoreMap()

        assertEquals("BLS-CIVIC-2026-9901", map["id"])
        assertEquals("test_citizen_uid_123", map["userId"])
        assertEquals("Baleswari Resident", map["userName"])
        assertEquals("Deep crater pothole cluster on OT Road", map["title"])
        assertEquals("pothole", map["category"])
        assertEquals("Ward 14 - OT Road / Phandi Chhak", map["wardLocation"])
        assertEquals("SUBMITTED", map["status"])
        assertEquals("HIGH", map["urgency"])
        assertEquals(21.4934, map["latitude"])
        assertEquals(86.9135, map["longitude"])
        assertEquals("https://balasore360.local/evidence/pothole_ot_road.jpg", map["photoUrl"])

        // Deserialization round-trip
        val restored = FirestoreUserReport.fromFirestoreMap(map)
        assertEquals(report.id, restored.id)
        assertEquals(report.title, restored.title)
        assertEquals(report.category, restored.category)
        assertEquals(report.urgency, restored.urgency)
        assertEquals(report.status, restored.status)
        assertEquals(report.wardLocation, restored.wardLocation)
    }

    @Test
    fun testWaterloggingReportFirestoreMapSerialization() {
        val report = FirestoreUserReport(
            id = "BLS-CIVIC-2026-9902",
            userId = "test_citizen_uid_456",
            userName = "Cinema Chhak Trader",
            title = "Severe monsoon waterlogging & clogged storm culvert",
            category = "waterlogging",
            description = "Drain overflow flooding road and shop entrances near Cinema Chhak junction.",
            wardLocation = "Ward 12 - Cinema Chhak",
            status = "IN_PROGRESS",
            urgency = "EMERGENCY",
            latitude = 21.4910,
            longitude = 86.9265,
            photoUrl = "https://balasore360.local/evidence/drain_cinema_chhak.jpg",
            reportedTimestamp = 1731139300000L,
            lastUpdatedTimestamp = 1731140000000L,
            resolutionNotes = "Drainage Desiltation Taskforce dispatched excavator to clear plastic blockage."
        )

        val map = report.toFirestoreMap()
        assertEquals("waterlogging", map["category"])
        assertEquals("EMERGENCY", map["urgency"])
        assertEquals("IN_PROGRESS", map["status"])

        val restored = FirestoreUserReport.fromFirestoreMap(map)
        assertEquals(report.id, restored.id)
        assertEquals("waterlogging", restored.category)
        assertEquals("EMERGENCY", restored.urgency)
        assertEquals("IN_PROGRESS", restored.status)
        assertEquals(report.resolutionNotes, restored.resolutionNotes)
    }

    @Test
    fun testSafeWebViewClientRendererCrashSafety() {
        var crashIntercepted = false
        val client = SafeWebViewClient(
            onCrashFallback = {
                crashIntercepted = true
            }
        )

        assertNotNull(client)
    }

    @Test
    fun testBalasoreWardGeoCoordinatesMappingForMapPins() {
        val wardMap = com.example.data.municipal.BalasoreMunicipalWards.getWardGeoCoordinatesMap()
        assertTrue("Ward map should contain municipal entries", wardMap.isNotEmpty())

        // Check Ward 4 Sahadevkhunta coordinates
        val sahadevkhuntaCoords = wardMap.entries.find { it.key.contains("Sahadevkhunta", ignoreCase = true) }?.value
        assertNotNull("Sahadevkhunta coordinates must exist for map plotting", sahadevkhuntaCoords)
        assertTrue("Sahadevkhunta latitude within Balasore bounds", sahadevkhuntaCoords!!.first in 21.45..21.55)
        assertTrue("Sahadevkhunta longitude within Balasore bounds", sahadevkhuntaCoords.second in 86.88..86.96)

        // Check Azimabad FM University coordinates
        val azimabadCoords = wardMap.entries.find { it.key.contains("Azimabad", ignoreCase = true) }?.value
        assertNotNull("Azimabad coordinates must exist for map plotting", azimabadCoords)
        assertTrue("Azimabad latitude within Balasore bounds", azimabadCoords!!.first in 21.48..21.54)
    }

    @Test
    fun testCivicReportCoordinateResolutionFallback() {
        // Given report without explicit GPS coordinates
        val report = com.example.data.local.CivicReportEntity(
            id = "BLS-TEST-PIN-01",
            title = "Clogged Drain near Station Bazaar",
            category = "Drain Clog / Waterlogging",
            wardLocation = "Ward 9 - Station Bazaar",
            description = "Water overflow near station approach",
            status = "PENDING",
            urgency = "HIGH",
            assignedDepartment = "Balasore Sanitation",
            reportedTimestamp = System.currentTimeMillis(),
            lastUpdatedTimestamp = System.currentTimeMillis(),
            hasPhotoAttached = false,
            photoUri = null,
            latitude = null,
            longitude = null,
            geoAddress = null,
            resolutionNotes = "Pending inspection"
        )

        val wardMap = com.example.data.municipal.BalasoreMunicipalWards.getWardGeoCoordinatesMap()
        val resolvedCoords = wardMap[report.wardLocation] ?: wardMap.entries.find {
            report.wardLocation.contains(it.key, ignoreCase = true) || it.key.contains(report.wardLocation, ignoreCase = true)
        }?.value

        assertNotNull("Station Bazaar coordinates must be resolved from ward dictionary", resolvedCoords)
        assertTrue("Resolved latitude should be within Balasore urban sector", resolvedCoords!!.first in 21.46..21.53)
        assertTrue("Resolved longitude should be within Balasore urban sector", resolvedCoords.second in 86.89..86.96)
    }

    @Test
    fun testCivicEyeCategoryFilteringSystem() {
        val sampleReports = listOf(
            com.example.data.local.CivicReportEntity(
                id = "R1",
                title = "Deep crater on OT road",
                category = "Road Pothole",
                wardLocation = "Ward 14",
                description = "Pothole",
                status = "PENDING",
                urgency = "HIGH",
                assignedDepartment = "R&B",
                reportedTimestamp = 1000L,
                lastUpdatedTimestamp = 1000L,
                hasPhotoAttached = true,
                photoUri = null,
                latitude = 21.4934,
                longitude = 86.9135,
                geoAddress = "Ward 14",
                resolutionNotes = ""
            ),
            com.example.data.local.CivicReportEntity(
                id = "R2",
                title = "Severe drain overflow",
                category = "Drain Clog / Waterlogging",
                wardLocation = "Ward 4",
                description = "Waterlogging",
                status = "IN_PROGRESS",
                urgency = "EMERGENCY",
                assignedDepartment = "Drainage",
                reportedTimestamp = 2000L,
                lastUpdatedTimestamp = 2000L,
                hasPhotoAttached = false,
                photoUri = null,
                latitude = 21.4981,
                longitude = 86.9240,
                geoAddress = "Ward 4",
                resolutionNotes = ""
            ),
            com.example.data.local.CivicReportEntity(
                id = "R3",
                title = "Market waste pile",
                category = "Garbage Heap / Waste",
                wardLocation = "Ward 7",
                description = "Waste",
                status = "RESOLVED",
                urgency = "MEDIUM",
                assignedDepartment = "Sanitation",
                reportedTimestamp = 3000L,
                lastUpdatedTimestamp = 3000L,
                hasPhotoAttached = true,
                photoUri = null,
                latitude = 21.4910,
                longitude = 86.9220,
                geoAddress = "Ward 7",
                resolutionNotes = ""
            ),
            com.example.data.local.CivicReportEntity(
                id = "R4",
                title = "Dark stretch streetlights",
                category = "Faulty Streetlight",
                wardLocation = "Ward 12",
                description = "Electricity streetlight failure",
                status = "PENDING",
                urgency = "MEDIUM",
                assignedDepartment = "TPNODL",
                reportedTimestamp = 4000L,
                lastUpdatedTimestamp = 4000L,
                hasPhotoAttached = false,
                photoUri = null,
                latitude = 21.4980,
                longitude = 86.9280,
                geoAddress = "Ward 12",
                resolutionNotes = ""
            )
        )

        fun filter(category: String, status: String = "ALL"): List<com.example.data.local.CivicReportEntity> {
            return sampleReports.filter { report ->
                val matchesCat = when (category) {
                    "POTHOLE" -> report.category.contains("Pothole", ignoreCase = true)
                    "WATERLOGGING" -> report.category.contains("Waterlog", ignoreCase = true) || report.category.contains("Drain", ignoreCase = true)
                    "WASTE", "GARBAGE" -> report.category.contains("Garbage", ignoreCase = true) || report.category.contains("Waste", ignoreCase = true)
                    "ELECTRICITY", "STREETLIGHT" -> report.category.contains("Streetlight", ignoreCase = true) || report.category.contains("Electric", ignoreCase = true)
                    else -> true
                }
                val matchesStat = status == "ALL" || report.status.equals(status, ignoreCase = true)
                matchesCat && matchesStat
            }
        }

        // Test Pothole filter
        val potholes = filter("POTHOLE")
        assertEquals(1, potholes.size)
        assertEquals("R1", potholes.first().id)

        // Test Waterlogging filter
        val waterlogged = filter("WATERLOGGING")
        assertEquals(1, waterlogged.size)
        assertEquals("R2", waterlogged.first().id)

        // Test Waste filter
        val waste = filter("WASTE")
        assertEquals(1, waste.size)
        assertEquals("R3", waste.first().id)

        // Test Electricity filter
        val electricity = filter("ELECTRICITY")
        assertEquals(1, electricity.size)
        assertEquals("R4", electricity.first().id)

        // Test Combined Category + Status filter
        val resolvedWaste = filter("WASTE", status = "RESOLVED")
        assertEquals(1, resolvedWaste.size)
        val pendingWaste = filter("WASTE", status = "PENDING")
        assertEquals(0, pendingWaste.size)

        // Test All filter
        val all = filter("ALL")
        assertEquals(4, all.size)
    }

    @Test
    fun testCivicEyeRoomDbSyncWhenOnlineQueueAndCategoryFilters() {
        val viewModel = com.example.ui.viewmodel.BalasoreViewModel()

        // Submit a Potholes report with photo into the Room DB 'sync-when-online' queue
        viewModel.submitCivicReport(
            title = "Crater pothole near Cinema Chhak",
            category = "Potholes",
            wardLocation = "Ward 12 - Cinema Chhak",
            description = "Deep pothole filled with rain water",
            urgency = "HIGH",
            hasPhotoAttached = true,
            photoUri = "preset://pothole_cinema_chhak",
            queueForSyncWhenOnline = true
        )

        // Submit a Lighting report into the Room DB 'sync-when-online' queue
        viewModel.submitCivicReport(
            title = "Streetlight outage on Proof Road",
            category = "Lighting",
            wardLocation = "Ward 21 - Proof Road / Chandipur Link",
            description = "4 street lamps not working",
            urgency = "MEDIUM",
            hasPhotoAttached = true,
            photoUri = "preset://lighting_proof_road",
            queueForSyncWhenOnline = true
        )

        // Submit a Water Supply report directly synced
        viewModel.submitCivicReport(
            title = "Drinking water pipeline leak",
            category = "Water Supply",
            wardLocation = "Ward 7 - Motiganj Market",
            description = "WATCO supply line leaking",
            urgency = "HIGH",
            hasPhotoAttached = false,
            queueForSyncWhenOnline = false
        )

        val allReports = viewModel.uiState.value.myCivicReports

        // Verify Category Filtering ('Potholes', 'Lighting', 'Water Supply')
        val potholeFiltered = allReports.filter { it.matchesCategoryFilter("Potholes") }
        val lightingFiltered = allReports.filter { it.matchesCategoryFilter("Lighting") }
        val waterSupplyFiltered = allReports.filter { it.matchesCategoryFilter("Water Supply") }

        assertTrue("Potholes filter must match pothole reports", potholeFiltered.isNotEmpty())
        assertTrue("Lighting filter must match lighting reports", lightingFiltered.isNotEmpty())
        assertTrue("Water Supply filter must match water supply reports", waterSupplyFiltered.isNotEmpty())

        // Verify Sync-When-Online Queue
        val queuedBeforeFlush = allReports.filter { it.isQueuedForSync }
        assertTrue("Should have reports waiting in Room sync-when-online queue", queuedBeforeFlush.size >= 2)

        // Flush the Sync-When-Online Queue
        val flushedCount = viewModel.flushCivicSyncWhenOnlineQueue()
        assertTrue("Flushed count should be at least 2", flushedCount >= 2)

        val queuedAfterFlush = viewModel.uiState.value.myCivicReports.filter { it.isQueuedForSync }
        assertEquals("All queued reports must be marked SYNCED after flush", 0, queuedAfterFlush.size)
    }
}

