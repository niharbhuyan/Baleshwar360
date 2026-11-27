package com.example

import com.example.data.local.CivicReportEntity
import com.example.data.notification.CivicProximityAlertManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CivicProximityAlertTest {

    @Test
    fun calculateDistanceMeters_returnsAccurateBalasoreDistances() {
        // Phandi Chhak (21.4945, 86.9320) to Chandipur Beach (21.4442, 87.0147) is ~10.2 km
        val townToChandipur = CivicProximityAlertManager.calculateDistanceMeters(
            21.4945, 86.9320,
            21.4442, 87.0147
        )
        assertTrue("Expected ~10km between Balasore Town and Chandipur, got $townToChandipur", townToChandipur in 9_500.0..11_500.0)

        // Zero distance for identical coordinates
        val zeroDist = CivicProximityAlertManager.calculateDistanceMeters(21.4934, 86.9336, 21.4934, 86.9336)
        assertTrue(zeroDist < 0.01)
    }

    @Test
    fun findNearbyOpenIncidents_filtersByRadiusExcludesResolvedAndSortsNearestFirst() {
        val userLat = 21.4934
        val userLon = 86.9336 // OT Road / Phandi Chhak

        val reports = listOf(
            CivicReportEntity(
                id = "R-NEAR-GPS",
                title = "Deep pothole near OT Road",
                category = "Pothole",
                wardLocation = "Ward 14 - OT Road",
                description = "Crater on road",
                status = "PENDING",
                urgency = "HIGH",
                assignedDepartment = "PWD",
                reportedTimestamp = 1000L,
                lastUpdatedTimestamp = 1000L,
                latitude = 21.4942,
                longitude = 86.9340
            ),
            CivicReportEntity(
                id = "R-RESOLVED-CLOSE",
                title = "Fixed streetlight",
                category = "Street Light",
                wardLocation = "Ward 14 - OT Road",
                description = "Already resolved",
                status = "RESOLVED",
                urgency = "LOW",
                assignedDepartment = "TPNODL",
                reportedTimestamp = 1000L,
                lastUpdatedTimestamp = 1000L,
                latitude = 21.4935,
                longitude = 86.9337
            ),
            CivicReportEntity(
                id = "R-WARD-CENTROID",
                title = "Drain blockage near Cinema Chhak",
                category = "Drainage",
                wardLocation = "Ward 12 - Cinema Chhak",
                description = "Overflowing drain",
                status = "IN_PROGRESS",
                urgency = "MEDIUM",
                assignedDepartment = "Municipality",
                reportedTimestamp = 1000L,
                lastUpdatedTimestamp = 1000L,
                latitude = null,
                longitude = null
            ),
            CivicReportEntity(
                id = "R-FAR-CHANDIPUR",
                title = "Broken beach lamp",
                category = "Street Light",
                wardLocation = "Chandipur Coastal Beach Road",
                description = "Far away",
                status = "PENDING",
                urgency = "LOW",
                assignedDepartment = "TPNODL",
                reportedTimestamp = 1000L,
                lastUpdatedTimestamp = 1000L,
                latitude = 21.4442,
                longitude = 87.0147
            )
        )

        val within1Km = CivicProximityAlertManager.findNearbyOpenIncidents(userLat, userLon, reports, 1000)
        assertEquals(1, within1Km.size)
        assertEquals("R-NEAR-GPS", within1Km.first().reportId)

        val within2Km = CivicProximityAlertManager.findNearbyOpenIncidents(userLat, userLon, reports, 2000)
        assertEquals(2, within2Km.size)
        assertEquals("R-NEAR-GPS", within2Km[0].reportId)
        assertEquals("R-WARD-CENTROID", within2Km[1].reportId)
    }
}
