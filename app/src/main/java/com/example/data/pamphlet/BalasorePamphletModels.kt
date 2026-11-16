package com.example.data.pamphlet

import com.example.data.model.Hotspot

/**
 * Data models for Official Balasore Tourism, Event Posters, and Emergency Information Pamphlets
 * verified and issued by Balasore District Administration, Odisha Tourism,
 * and District Disaster Management Authority (DDMA).
 */
enum class PamphletCategory(val displayNameEn: String, val displayNameOr: String) {
    TOURISM("Tourism & Heritage", "ପର୍ଯ୍ୟଟନ ଓ ଐତିହ୍ୟ"),
    EVENT_POSTER("Official Event Posters", "ସରକାରୀ ଉତ୍ସବ ପୋଷ୍ଟର"),
    EMERGENCY("Emergency & Disaster", "ଜରୁରୀକାଳୀନ ଓ ବିପର୍ଯ୍ୟୟ"),
    HEALTHCARE("Healthcare & Hospital", "ସ୍ୱାସ୍ଥ୍ୟସେବା ଓ ଡାକ୍ତରଖାନା"),
    CIVIC("Civic & Governance", "ପୌର ପ୍ରଶାସନ")
}

data class BalasorePamphlet(
    val id: String,
    val titleEn: String,
    val titleOr: String,
    val category: PamphletCategory,
    val issuingAuthorityEn: String,
    val issuingAuthorityOr: String,
    val summaryEn: String,
    val summaryOr: String,
    val fullContentEn: String,
    val fullContentOr: String,
    val keyDirectivesEn: List<String>,
    val keyDirectivesOr: List<String>,
    val emergencyContactNumbers: List<String> = emptyList(),
    val officialHotlineLabel: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val locationName: String? = null,
    val qrPayload: String,
    val documentSize: String = "1.2 MB",
    val officialReferenceNumber: String,
    val publicationDate: String = "2026 Edition",
    val verifiedBadge: Boolean = true,
    val linkedHotspotId: String? = null,
    val linkedEventId: String? = null
)

data class QrScanResult(
    val rawValue: String,
    val matchedPamphlet: BalasorePamphlet? = null,
    val isOfficialBalasorePamphlet: Boolean = false,
    val detectedType: QrPayloadType = QrPayloadType.TEXT,
    val matchedHotspot: Hotspot? = null,
    val matchedEventDate: String? = null,
    val matchedEventStageTime: String? = null,
    val matchedAppTargetType: String = "PAMPHLET"
)

enum class QrPayloadType {
    OFFICIAL_PAMPHLET,
    EMERGENCY_HOTLINE,
    GEO_COORDINATES,
    OFFICIAL_URL,
    TEXT
}
