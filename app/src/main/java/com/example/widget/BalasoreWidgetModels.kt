package com.example.widget

/**
 * Administrative block metadata in Balasore district with official 24x7 emergency contacts.
 */
data class BalasoreBlockInfo(
    val blockId: String,
    val nameEn: String,
    val nameOr: String,
    val centerLat: Double,
    val centerLng: Double,
    val emergencyHelpline: String,
    val emergencyDeskLabelEn: String,
    val emergencyDeskLabelOr: String,
    val bdoPhone: String = emergencyHelpline,
    val tehsilControlPhone: String = "1077"
)

/**
 * Designated Multipurpose Cyclone Shelter (MPCS) in Balasore district.
 */
data class BalasoreShelterInfo(
    val id: String,
    val nameEn: String,
    val nameOr: String,
    val blockId: String,
    val blockName: String,
    val latitude: Double,
    val longitude: Double,
    val capacity: Int,
    val elevationMeters: Double,
    val hasSolarPower: Boolean,
    val caretakerPhone: String,
    val highGroundRoute: String,
    val generatorStatus: String = "Active (Reserve Ready 🟢)"
)

/**
 * Resolved state for rendering the location-based Balasore Shelter widget.
 */
data class BalasoreWidgetState(
    val block: BalasoreBlockInfo,
    val nearestShelter: BalasoreShelterInfo,
    val distanceKm: Double,
    val isGpsActive: Boolean,
    val lastUpdatedMillis: Long = System.currentTimeMillis()
)
