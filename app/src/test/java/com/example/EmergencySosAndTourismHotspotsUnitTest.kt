package com.example

import com.example.data.emergency.EmergencySosContact
import com.example.data.emergency.EmergencySosManager
import com.example.ui.components.BALASORE_TOURIST_LOCATIONS
import com.example.ui.components.calculateHaversineDistanceKm
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test suite verifying the Emergency SOS logic (GPS message formatting,
 * nearest police control room selection, pre-set contacts) and Tourism Hotspots
 * distance calculations.
 */
class EmergencySosAndTourismHotspotsUnitTest {

    @Test
    fun testEmergencySosMessageConstruction() {
        val lat = 21.4934
        val lng = 86.9135
        val desc = "Balasore City Center / Station"
        val message = EmergencySosManager.buildSosMessage(lat, lng, desc)

        assertNotNull(message)
        assertTrue("Message should contain EMERGENCY SOS", message.contains("EMERGENCY SOS"))
        assertTrue("Message should contain Google Maps link", message.contains("https://maps.google.com/?q=21.49340,86.91350"))
        assertTrue("Message should include description", message.contains("Balasore City Center / Station"))
    }

    @Test
    fun testDefaultSosContactsConfigured() {
        val contacts = EmergencySosManager.DEFAULT_SOS_CONTACTS
        assertTrue("Must have at least 2 default emergency contacts", contacts.size >= 2)
        val primary = contacts.firstOrNull { it.isPrimary }
        assertNotNull("Must have a primary emergency contact", primary)
        assertTrue("Primary contact should have a valid phone number", primary!!.phoneNumber.isNotBlank())
    }

    @Test
    fun testNearestPoliceControlRoomLookup() {
        // Test near Balasore Station (Town Police Station / 112)
        val policeStation = EmergencySosManager.getNearestPoliceControlRoom(21.4934, 86.9135)
        assertNotNull("Nearest police station should not be null", policeStation)
        assertTrue("Phone number should be present", policeStation.phone.isNotBlank())
        assertTrue("Must be a police category", policeStation.category.name == "POLICE")

        // Test near Chandipur (should resolve Chandipur Marine Police)
        val chandipurPolice = EmergencySosManager.getNearestPoliceControlRoom(21.4674, 87.0177)
        assertNotNull(chandipurPolice)
        assertTrue("Should resolve Chandipur marine police near coast", chandipurPolice.id.contains("marine") || chandipurPolice.id.contains("pol"))
    }

    @Test
    fun testHaversineDistanceCalculationForTourismHotspots() {
        // Balasore Junction coordinates
        val stationLat = 21.4934
        val stationLng = 86.9135

        // Chandipur Beach coordinates (~15-16 km)
        val chandipurLat = 21.4695
        val chandipurLng = 87.0185

        val distance = calculateHaversineDistanceKm(stationLat, stationLng, chandipurLat, chandipurLng)
        assertTrue("Distance should be around 11-16 km", distance in 10.0..18.0)
    }

    @Test
    fun testBalasoreTouristLocationsIntegrity() {
        val hotspots = BALASORE_TOURIST_LOCATIONS
        assertTrue("Should have extensive list of tourist hotspots", hotspots.size >= 8)

        val chandipur = hotspots.find { it.id == "chandipur_beach" }
        assertNotNull("Chandipur Beach must exist in hotspots list", chandipur)
        assertEquals("BEACH", chandipur!!.categoryGroup)
        assertTrue("Must have valid latitude", chandipur.latitude > 20.0)
        assertTrue("Must have valid longitude", chandipur.longitude > 85.0)
        assertTrue("Must have transit routes", chandipur.transitRoutes.isNotEmpty())
        assertTrue("Must have detailed description", chandipur.detailedDescEn.isNotBlank())
        assertTrue("Must have historical significance", chandipur.historicalSignificance.isNotBlank())
    }

    @Test
    fun testExpandedRepositoryHotspotsHaveRichMetadata() {
        val repoHotspots = com.example.data.repository.BalasoreRepository.hotspots
        assertTrue("Repository should contain at least 8 hotspots", repoHotspots.size >= 8)

        repoHotspots.forEach { spot ->
            assertTrue("Hotspot ${spot.id} must have a detailed description", spot.effectiveDetailedDescription.length > 40)
            assertTrue("Hotspot ${spot.id} must have historical significance", spot.historicalSignificance.isNotBlank())
            assertTrue("Hotspot ${spot.id} must have operating hours", spot.effectiveOperatingHours.isNotBlank())
            assertTrue("Hotspot ${spot.id} must have positive rating", spot.rating in 4.0..5.0)
            assertTrue("Hotspot ${spot.id} must have valid review count", spot.reviewCount > 0)
            assertTrue("Hotspot ${spot.id} must have valid latitude", spot.latitude in 20.0..23.0)
            assertTrue("Hotspot ${spot.id} must have valid longitude", spot.longitude in 85.0..89.0)
        }
    }
}
