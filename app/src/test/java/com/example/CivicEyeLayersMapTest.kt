package com.example

import com.example.data.local.CivicReportEntity
import com.example.data.model.FirestoreUserReport
import com.example.ui.screens.CivicEyeLayersMapHelper
import com.example.ui.screens.CivicIncidentLayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CivicEyeLayersMapTest {

    private fun sampleReport(
        id: String,
        category: String,
        lat: Double = 21.4934,
        lng: Double = 86.9336,
        status: String = "PENDING"
    ) = CivicReportEntity(
        id = id,
        title = "Issue $id",
        category = category,
        wardLocation = "Ward 14 - OT Road",
        description = "Reported via Civic Eye",
        status = status,
        urgency = "HIGH",
        assignedDepartment = "Balasore Municipality",
        reportedTimestamp = 1_700_000_000_000L,
        lastUpdatedTimestamp = 1_700_000_000_000L,
        hasPhotoAttached = false,
        photoUri = null,
        latitude = lat,
        longitude = lng,
        geoAddress = "OT Road, Balasore",
        resolutionNotes = ""
    )

    @Test
    fun classifyLayer_mapsCategoriesAccurately() {
        assertEquals(CivicIncidentLayer.POTHOLES, CivicEyeLayersMapHelper.classifyLayer("Pothole"))
        assertEquals(CivicIncidentLayer.POTHOLES, CivicEyeLayersMapHelper.classifyLayer("Damaged Road"))
        assertEquals(CivicIncidentLayer.SANITATION, CivicEyeLayersMapHelper.classifyLayer("Garbage"))
        assertEquals(CivicIncidentLayer.SANITATION, CivicEyeLayersMapHelper.classifyLayer("Sanitation & Solid Waste"))
        assertEquals(CivicIncidentLayer.DRAINAGE, CivicEyeLayersMapHelper.classifyLayer("Drainage"))
        assertEquals(CivicIncidentLayer.DRAINAGE, CivicEyeLayersMapHelper.classifyLayer("Waterlogging"))
        assertEquals(CivicIncidentLayer.WATER_SUPPLY, CivicEyeLayersMapHelper.classifyLayer("Water Supply"))
        assertEquals(CivicIncidentLayer.STREET_LIGHTS, CivicEyeLayersMapHelper.classifyLayer("Street Light"))
    }

    @Test
    fun filterPinsByLayers_togglesPotholesAndSanitationIndependently() {
        val reports = listOf(
            sampleReport("r1", "Pothole", 21.4945, 86.9320),
            sampleReport("r2", "Garbage", 21.4978, 86.9245),
            sampleReport("r3", "Drainage", 21.5015, 86.9182),
            sampleReport("r4", "Street Light", 21.5082, 86.9261)
        )
        val pins = CivicEyeLayersMapHelper.resolvePins(reports, emptyMap())

        // Only Potholes + Sanitation enabled
        val onlyPotholesAndSanitation = CivicEyeLayersMapHelper.filterPinsByLayers(
            pins = pins,
            enabledLayers = setOf(CivicIncidentLayer.POTHOLES, CivicIncidentLayer.SANITATION)
        )
        assertEquals(listOf("r1", "r2"), onlyPotholesAndSanitation.map { it.report.id })

        // Toggle Potholes OFF -> only Sanitation remains
        val onlySanitation = CivicEyeLayersMapHelper.filterPinsByLayers(
            pins = pins,
            enabledLayers = setOf(CivicIncidentLayer.SANITATION)
        )
        assertEquals(listOf("r2"), onlySanitation.map { it.report.id })

        // Toggle all OFF -> empty
        val noneEnabled = CivicEyeLayersMapHelper.filterPinsByLayers(pins, emptySet())
        assertTrue(noneEnabled.isEmpty())

        // Verify per-layer counts
        val counts = CivicEyeLayersMapHelper.countPinsByLayer(pins)
        assertEquals(1, counts[CivicIncidentLayer.POTHOLES])
        assertEquals(1, counts[CivicIncidentLayer.SANITATION])
        assertEquals(1, counts[CivicIncidentLayer.DRAINAGE])
        assertEquals(0, counts[CivicIncidentLayer.WATER_SUPPLY])
        assertEquals(1, counts[CivicIncidentLayer.STREET_LIGHTS])
    }

    @Test
    fun mergeCivicEyeReports_deduplicatesFirestoreAndRoom() {
        val local = listOf(
            sampleReport("shared-1", "Pothole", status = "PENDING").copy(lastUpdatedTimestamp = 100L),
            sampleReport("local-only", "Garbage", status = "PENDING").copy(lastUpdatedTimestamp = 200L)
        )
        val remote = listOf(
            FirestoreUserReport(
                id = "shared-1",
                title = "Synced Pothole",
                category = "Pothole",
                status = "IN_PROGRESS",
                latitude = 21.4945,
                longitude = 86.9320,
                reportedTimestamp = 150L,
                lastUpdatedTimestamp = 300L
            )
        )

        val merged = CivicEyeLayersMapHelper.mergeCivicEyeReports(local, remote)
        assertEquals(2, merged.size)
        val shared = merged.first { it.id == "shared-1" }
        assertEquals("IN_PROGRESS", shared.status)
    }

    @Test
    fun classifySeverity_andFilterPinsBySeverity_workForLowMediumCritical() {
        assertEquals(
            com.example.ui.screens.CivicSeverityLevel.LOW,
            CivicEyeLayersMapHelper.classifySeverity("LOW")
        )
        assertEquals(
            com.example.ui.screens.CivicSeverityLevel.LOW,
            CivicEyeLayersMapHelper.classifySeverity("NORMAL")
        )
        assertEquals(
            com.example.ui.screens.CivicSeverityLevel.MEDIUM,
            CivicEyeLayersMapHelper.classifySeverity("MEDIUM")
        )
        assertEquals(
            com.example.ui.screens.CivicSeverityLevel.CRITICAL,
            CivicEyeLayersMapHelper.classifySeverity("CRITICAL")
        )
        assertEquals(
            com.example.ui.screens.CivicSeverityLevel.CRITICAL,
            CivicEyeLayersMapHelper.classifySeverity("EMERGENCY")
        )
        assertEquals(
            com.example.ui.screens.CivicSeverityLevel.CRITICAL,
            CivicEyeLayersMapHelper.classifySeverity("HIGH")
        )

        val reports = listOf(
            sampleReport("low-1", "Pothole").copy(urgency = "LOW"),
            sampleReport("med-1", "Garbage").copy(urgency = "MEDIUM"),
            sampleReport("crit-1", "Drainage").copy(urgency = "CRITICAL"),
            sampleReport("crit-2", "Pothole").copy(urgency = "EMERGENCY")
        )
        val pins = CivicEyeLayersMapHelper.resolvePins(reports, emptyMap())
        val allLayers = CivicIncidentLayer.entries.toSet()

        val onlyLow = CivicEyeLayersMapHelper.filterPinsByLayersAndSeverity(
            pins, allLayers, com.example.ui.screens.CivicSeverityLevel.LOW
        )
        assertEquals(listOf("low-1"), onlyLow.map { it.report.id })

        val onlyMedium = CivicEyeLayersMapHelper.filterPinsByLayersAndSeverity(
            pins, allLayers, com.example.ui.screens.CivicSeverityLevel.MEDIUM
        )
        assertEquals(listOf("med-1"), onlyMedium.map { it.report.id })

        val onlyCritical = CivicEyeLayersMapHelper.filterPinsByLayersAndSeverity(
            pins, allLayers, com.example.ui.screens.CivicSeverityLevel.CRITICAL
        )
        assertEquals(listOf("crit-1", "crit-2"), onlyCritical.map { it.report.id })

        val counts = CivicEyeLayersMapHelper.countPinsBySeverity(pins)
        assertEquals(1, counts[com.example.ui.screens.CivicSeverityLevel.LOW])
        assertEquals(1, counts[com.example.ui.screens.CivicSeverityLevel.MEDIUM])
        assertEquals(2, counts[com.example.ui.screens.CivicSeverityLevel.CRITICAL])
    }

    @Test
    fun distanceCalculation_andFormatting_returnAccurateLabels() {
        val distMeters = CivicEyeLayersMapHelper.distanceMetersBetween(
            lat1 = 21.4945,
            lon1 = 86.9320,
            lat2 = 21.4978,
            lon2 = 86.9245
        )
        assertTrue("Expected ~850m between Phandi Chhak & Cinema Chhak, got $distMeters", distMeters in 700.0..1000.0)
        assertEquals("420 m away", CivicEyeLayersMapHelper.formatDistanceFromUser(420.2))
        assertEquals("2.4 km away", CivicEyeLayersMapHelper.formatDistanceFromUser(2410.0))
    }
}
