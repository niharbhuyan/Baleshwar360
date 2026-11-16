package com.example

import android.Manifest
import com.example.data.emergency.EmergencySosManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test verifying the 1-tap Emergency Helpline 112 calling integration.
 */
class BalasoreEmergency112CallTest {

    @Test
    fun testEmergencyHelpline112Constant() {
        assertEquals("112", EmergencySosManager.POLICE_CONTROL_ROOM_112)
    }

    @Test
    fun testNearestPoliceControlRoomFallbackTo112() {
        val nearest = EmergencySosManager.getNearestPoliceControlRoom(21.4934, 86.9135)
        assertNotNull(nearest)
        assertTrue(
            "Emergency number should either be 112 or local control room",
            nearest.phone.contains("112") || nearest.phone.contains("06782")
        )
    }

    @Test
    fun testCallPhonePermissionString() {
        assertEquals("android.permission.CALL_PHONE", Manifest.permission.CALL_PHONE)
    }

    @Test
    fun testVoiceActivatedEmergencyCommandRecognition() {
        assertTrue(EmergencySosManager.isEmergencyVoiceCommand("Call Balasore Emergency"))
        assertTrue(EmergencySosManager.isEmergencyVoiceCommand("call emergency now please"))
        assertTrue(EmergencySosManager.isEmergencyVoiceCommand("dial 112"))
        assertTrue(EmergencySosManager.isEmergencyVoiceCommand("Help, this is an emergency"))
        assertTrue(EmergencySosManager.isEmergencyVoiceCommand("ବାଲେଶ୍ୱର ପୋଲିସ କୁ ଫୋନ୍ କରନ୍ତୁ"))
        org.junit.Assert.assertFalse(EmergencySosManager.isEmergencyVoiceCommand("Show tourist spots"))
    }

    @Test
    fun testSosMessageBuildingWithGpsCoordinates() {
        val message = EmergencySosManager.buildSosMessage(21.4934, 86.9135, "Balasore City Center")
        assertTrue(message.contains("EMERGENCY SOS"))
        assertTrue(message.contains("https://maps.google.com/?q=21.49340,86.91350"))
        assertTrue(message.contains("112") || message.contains("Police"))
    }
}
