package com.example

import com.example.data.pamphlet.BalasorePamphletRepository
import com.example.data.pamphlet.PamphletCategory
import com.example.data.pamphlet.QrPayloadType
import org.junit.Assert.*
import org.junit.Test

class BalasorePamphletQrScannerTest {

    @Test
    fun testOfficialPamphletRepositoryCompleteness() {
        val pamphlets = BalasorePamphletRepository.OFFICIAL_PAMPHLETS
        assertTrue("Official pamphlets catalog should not be empty", pamphlets.isNotEmpty())

        val tourismPamphlets = pamphlets.filter { it.category == PamphletCategory.TOURISM }
        val emergencyPamphlets = pamphlets.filter { it.category == PamphletCategory.EMERGENCY || it.category == PamphletCategory.HEALTHCARE }

        assertTrue("Should include Tourism pamphlets", tourismPamphlets.isNotEmpty())
        assertTrue("Should include Emergency pamphlets", emergencyPamphlets.isNotEmpty())

        pamphlets.forEach { p ->
            assertFalse("TitleEn should not be blank: ${p.id}", p.titleEn.isBlank())
            assertFalse("TitleOr should not be blank: ${p.id}", p.titleOr.isBlank())
            assertFalse("SummaryEn should not be blank: ${p.id}", p.summaryEn.isBlank())
            assertFalse("SummaryOr should not be blank: ${p.id}", p.summaryOr.isBlank())
            assertFalse("QrPayload should not be blank: ${p.id}", p.qrPayload.isBlank())
            assertTrue("Key directives should not be empty: ${p.id}", p.keyDirectivesEn.isNotEmpty())
            assertTrue("Odia directives should not be empty: ${p.id}", p.keyDirectivesOr.isNotEmpty())
        }
    }

    @Test
    fun testQrPayloadResolutionMatchesChandipurBeach() {
        val result = BalasorePamphletRepository.resolveQrPayload("balasore360://pamphlet/chandipur_beach_safety")
        assertTrue("Should be official pamphlet", result.isOfficialBalasorePamphlet)
        assertEquals(QrPayloadType.OFFICIAL_PAMPHLET, result.detectedType)
        assertNotNull(result.matchedPamphlet)
        assertEquals("chandipur_beach_safety", result.matchedPamphlet?.id)
        assertEquals(PamphletCategory.TOURISM, result.matchedPamphlet?.category)
    }

    @Test
    fun testQrPayloadResolutionMatchesDisasterSop() {
        val result = BalasorePamphletRepository.resolveQrPayload("balasore360://pamphlet/balasore_disaster_odraf_sop")
        assertTrue(result.isOfficialBalasorePamphlet)
        assertEquals(QrPayloadType.OFFICIAL_PAMPHLET, result.detectedType)
        assertEquals("balasore_disaster_odraf_sop", result.matchedPamphlet?.id)
        assertEquals(PamphletCategory.EMERGENCY, result.matchedPamphlet?.category)
        assertTrue(result.matchedPamphlet!!.emergencyContactNumbers.contains("1077"))
    }

    @Test
    fun testQrPayloadResolutionMatchesFmMedicalEmergency() {
        val result = BalasorePamphletRepository.resolveQrPayload("balasore360://pamphlet/fm_medical_college_emergency")
        assertTrue(result.isOfficialBalasorePamphlet)
        assertEquals("fm_medical_college_emergency", result.matchedPamphlet?.id)
        assertEquals(PamphletCategory.HEALTHCARE, result.matchedPamphlet?.category)
        assertTrue(result.matchedPamphlet!!.emergencyContactNumbers.contains("108"))
    }

    @Test
    fun testEmergencyPhonePayloadDetection() {
        val result = BalasorePamphletRepository.resolveQrPayload("tel:112")
        assertEquals(QrPayloadType.EMERGENCY_HOTLINE, result.detectedType)
        assertFalse(result.isOfficialBalasorePamphlet)

        val rawPhone = BalasorePamphletRepository.resolveQrPayload("06782-262674")
        assertEquals(QrPayloadType.EMERGENCY_HOTLINE, rawPhone.detectedType)
    }

    @Test
    fun testGeoLocationPayloadDetection() {
        val result = BalasorePamphletRepository.resolveQrPayload("geo:21.4682,87.0163")
        assertEquals(QrPayloadType.GEO_COORDINATES, result.detectedType)
    }

    @Test
    fun testUrlPayloadDetection() {
        val result = BalasorePamphletRepository.resolveQrPayload("https://balasore.odisha.gov.in/tourism")
        assertEquals(QrPayloadType.OFFICIAL_URL, result.detectedType)
    }

    @Test
    fun testOfficialEventPostersResolution() {
        val posters = listOf(
            "balasore360://poster/baleswar_mahotsav_2026" to "baleswar_mahotsav_2026",
            "balasore360://poster/chandipur_beach_festival" to "chandipur_beach_festival",
            "balasore360://poster/chandaneswar_chadak_festival" to "chandaneswar_chadak_festival",
            "balasore360://poster/nilagiri_chhau_mahotsav_2026" to "nilagiri_chhau_mahotsav_2026",
            "balasore360://poster/balasore_international_sand_art_expo" to "balasore_international_sand_art_expo",
            "balasore360://poster/raibania_fortress_heritage_gala" to "raibania_fortress_heritage_gala",
            "balasore360://poster/talsari_marine_eco_utsav" to "talsari_marine_eco_utsav"
        )

        posters.forEach { (payload, expectedId) ->
            val result = BalasorePamphletRepository.resolveQrPayload(payload)
            assertTrue("Payload $payload must be recognized as official Balasore pamphlet/poster", result.isOfficialBalasorePamphlet)
            assertEquals(QrPayloadType.OFFICIAL_PAMPHLET, result.detectedType)
            assertNotNull("Matched pamphlet should not be null for $payload", result.matchedPamphlet)
            assertEquals(expectedId, result.matchedPamphlet?.id)
            assertEquals(PamphletCategory.EVENT_POSTER, result.matchedPamphlet?.category)
            assertFalse(result.matchedPamphlet!!.titleEn.isBlank())
            assertFalse(result.matchedPamphlet!!.titleOr.isBlank())
        }
    }

    @Test
    fun testQrCodeBitMatrixGeneration() {
        val samplePayload = "balasore360://poster/baleswar_mahotsav_2026"
        val bitMatrix = com.example.data.pamphlet.BalasoreQrCodeGenerator.generateBitMatrix(samplePayload, 200)
        assertNotNull("Generated QR bitMatrix should not be null", bitMatrix)
        assertEquals(200, bitMatrix.width)
        assertEquals(200, bitMatrix.height)
    }

    @Test
    fun testUniqueFeatureSheetTypeRegistered() {
        val sheetType = com.example.ui.viewmodel.UniqueFeatureSheetType.OFFICIAL_TOURISM_EVENT_POSTER_SCANNER
        assertEquals("OFFICIAL_TOURISM_EVENT_POSTER_SCANNER", sheetType.name)
    }
}
