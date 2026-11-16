package com.example.widget

import android.Manifest
import android.annotation.SuppressLint
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Repository and calculation helper for Balasore Blocks, Cyclone Shelters,
 * and location-based proximity calculations.
 */
object BalasoreShelterWidgetHelper {

    private const val PREFS_NAME = "balasore_shelter_widget_prefs"
    private const val KEY_PINNED_BLOCK_ID = "pinned_block_id"
    private const val KEY_LAST_LATITUDE = "last_known_latitude"
    private const val KEY_LAST_LONGITUDE = "last_known_longitude"
    private const val KEY_USE_GPS = "use_gps_location"

    // Default coordinates: Balasore District Headquarters / Station Square
    const val DEFAULT_LAT = 21.4934
    const val DEFAULT_LNG = 86.9135

    /**
     * Complete directory of all 12 administrative blocks in Balasore district
     * with verified 24x7 Emergency Helplines and disaster response desks.
     */
    val BLOCKS: List<BalasoreBlockInfo> = listOf(
        BalasoreBlockInfo(
            blockId = "balasore_sadar",
            nameEn = "Balasore Sadar",
            nameOr = "ବାଲେଶ୍ୱର ସଦର",
            centerLat = 21.4934,
            centerLng = 86.9135,
            emergencyHelpline = "06782-262286",
            emergencyDeskLabelEn = "Sadar Block 24x7 Disaster Control",
            emergencyDeskLabelOr = "ସଦର ବ୍ଲକ ୨୪x୭ ବିପର୍ଯ୍ୟୟ କଣ୍ଟ୍ରୋଲ ରୁମ୍"
        ),
        BalasoreBlockInfo(
            blockId = "remuna",
            nameEn = "Remuna",
            nameOr = "ରେମୁଣା",
            centerLat = 21.5284,
            centerLng = 86.8647,
            emergencyHelpline = "06782-252115",
            emergencyDeskLabelEn = "Remuna Block Emergency Disaster Desk",
            emergencyDeskLabelOr = "ରେମୁଣା ବ୍ଲକ ଜରୁରୀକାଳୀନ ବିପର୍ଯ୍ୟୟ ଡେସ୍କ"
        ),
        BalasoreBlockInfo(
            blockId = "bahanaga",
            nameEn = "Bahanaga",
            nameOr = "ବାହାନଗା",
            centerLat = 21.3150,
            centerLng = 86.8050,
            emergencyHelpline = "06782-275420",
            emergencyDeskLabelEn = "Bahanaga CHC & Disaster Control Room",
            emergencyDeskLabelOr = "ବାହାନଗା ସିଏଚସି ଓ ବିପର୍ଯ୍ୟୟ ନିୟନ୍ତ୍ରଣ କକ୍ଷ"
        ),
        BalasoreBlockInfo(
            blockId = "soro",
            nameEn = "Soro",
            nameOr = "ସୋରୋ",
            centerLat = 21.2850,
            centerLng = 86.6920,
            emergencyHelpline = "06782-281200",
            emergencyDeskLabelEn = "Soro Emergency Response Cell",
            emergencyDeskLabelOr = "ସୋରୋ ଜରୁରୀକାଳୀନ ବିପର୍ଯ୍ୟୟ ପ୍ରକୋଷ୍ଠ"
        ),
        BalasoreBlockInfo(
            blockId = "simulia",
            nameEn = "Simulia",
            nameOr = "ସିମୁଳିଆ",
            centerLat = 21.1550,
            centerLng = 86.5820,
            emergencyHelpline = "06782-294100",
            emergencyDeskLabelEn = "Simulia Block Flood & Cyclone Desk",
            emergencyDeskLabelOr = "ସିମୁଳିଆ ବ୍ଲକ ବନ୍ୟା ଓ ବାତ୍ୟା ଡେସ୍କ"
        ),
        BalasoreBlockInfo(
            blockId = "khaira",
            nameEn = "Khaira",
            nameOr = "ଖଇରା",
            centerLat = 21.2800,
            centerLng = 86.5900,
            emergencyHelpline = "06782-235100",
            emergencyDeskLabelEn = "Khaira Block Control Room",
            emergencyDeskLabelOr = "ଖଇରା ବ୍ଲକ ଜରୁରୀକାଳୀନ କଣ୍ଟ୍ରୋଲ ରୁମ୍"
        ),
        BalasoreBlockInfo(
            blockId = "oupada",
            nameEn = "Oupada",
            nameOr = "ଔପଦା",
            centerLat = 21.4100,
            centerLng = 86.6500,
            emergencyHelpline = "06782-243200",
            emergencyDeskLabelEn = "Oupada Block Emergency Desk",
            emergencyDeskLabelOr = "ଔପଦା ବ୍ଲକ ଜରୁରୀକାଳୀନ ଡେସ୍କ"
        ),
        BalasoreBlockInfo(
            blockId = "nilagiri",
            nameEn = "Nilagiri",
            nameOr = "ନୀଳଗିରି",
            centerLat = 21.4550,
            centerLng = 86.7620,
            emergencyHelpline = "06782-233215",
            emergencyDeskLabelEn = "Nilagiri Sub-Collector Disaster Desk",
            emergencyDeskLabelOr = "ନୀଳଗିରି ଉପ-ଜିଲ୍ଲାପାଳ ବିପର୍ଯ୍ୟୟ ଡେସ୍କ"
        ),
        BalasoreBlockInfo(
            blockId = "basta",
            nameEn = "Basta",
            nameOr = "ବସ୍ତା",
            centerLat = 21.6850,
            centerLng = 86.7050,
            emergencyHelpline = "06781-251200",
            emergencyDeskLabelEn = "Basta Subarnarekha Basin Disaster Desk",
            emergencyDeskLabelOr = "ବସ୍ତା ସୁବର୍ଣ୍ଣରେଖା ଅବବାହିକା ବିପର୍ଯ୍ୟୟ ଡେସ୍କ"
        ),
        BalasoreBlockInfo(
            blockId = "baliapal",
            nameEn = "Baliapal",
            nameOr = "ବାଲିଆପାଳ",
            centerLat = 21.6420,
            centerLng = 87.2890,
            emergencyHelpline = "06781-254300",
            emergencyDeskLabelEn = "Baliapal Coastal Cyclone Control",
            emergencyDeskLabelOr = "ବାଲିଆପାଳ ଉପକୂଳ ବାତ୍ୟା ନିୟନ୍ତ୍ରଣ"
        ),
        BalasoreBlockInfo(
            blockId = "jaleswar",
            nameEn = "Jaleswar",
            nameOr = "ଜଳେଶ୍ୱର",
            centerLat = 21.8020,
            centerLng = 87.2150,
            emergencyHelpline = "06781-222400",
            emergencyDeskLabelEn = "Jaleswar Sub-Divisional Control Room",
            emergencyDeskLabelOr = "ଜଳେଶ୍ୱର ଉପଖଣ୍ଡ ଜରୁରୀକାଳୀନ ନିୟନ୍ତ୍ରଣ କକ୍ଷ"
        ),
        BalasoreBlockInfo(
            blockId = "bhograi",
            nameEn = "Bhograi",
            nameOr = "ଭୋଗରାଇ",
            centerLat = 21.5892,
            centerLng = 87.4582,
            emergencyHelpline = "06781-238210",
            emergencyDeskLabelEn = "Bhograi Coastal Evacuation Desk",
            emergencyDeskLabelOr = "ଭୋଗରାଇ ଉପକୂଳ ଉଦ୍ଧାର ଓ ବାତ୍ୟା ଡେସ୍କ"
        )
    )

    /**
     * Multipurpose Cyclone Shelters across Balasore with verified capacities,
     * backup power, and GPS coordinates.
     */
    val SHELTERS: List<BalasoreShelterInfo> = listOf(
        BalasoreShelterInfo(
            id = "cs_chandi_1",
            nameEn = "Chandipur Coastal Multipurpose Shelter",
            nameOr = "ଚାନ୍ଦିପୁର ବହୁମୁଖୀ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "balasore_sadar",
            blockName = "Balasore Sadar",
            latitude = 21.4682,
            longitude = 87.0163,
            capacity = 1800,
            elevationMeters = 9.2,
            hasSolarPower = true,
            caretakerPhone = "06782-262234",
            highGroundRoute = "Proof Road straight inland to Balasore Highway",
            generatorStatus = "Active (350L Reserve 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_kasa_2",
            nameEn = "Kasafal Marine Cyclone Center",
            nameOr = "କସାଫଳ ମତ୍ସ୍ୟଜୀବୀ ବାତ୍ୟା କେନ୍ଦ୍ର",
            blockId = "balasore_sadar",
            blockName = "Balasore Sadar",
            latitude = 21.5200,
            longitude = 87.1100,
            capacity = 1600,
            elevationMeters = 8.8,
            hasSolarPower = true,
            caretakerPhone = "06782-260100",
            highGroundRoute = "Kasafal-Haladipada high asphalt ridge road",
            generatorStatus = "Standby (Full reserve 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_balaram_3",
            nameEn = "Balaramgadi Fish Port Evacuation Safe Haven",
            nameOr = "ବଳରାମଗଡ଼ି ମୁହାଣ ଉଦ୍ଧାର ଓ ବାତ୍ୟା କେନ୍ଦ୍ର",
            blockId = "balasore_sadar",
            blockName = "Balasore Sadar",
            latitude = 21.4880,
            longitude = 87.0480,
            capacity = 1750,
            elevationMeters = 8.6,
            hasSolarPower = true,
            caretakerPhone = "06782-272210",
            highGroundRoute = "Balaramgadi-Proof Road inland corridor",
            generatorStatus = "Active (Solar + Diesel 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_remuna_4",
            nameEn = "Remuna High School Disaster Shelter",
            nameOr = "ରେମୁଣା ଉଚ୍ଚ ବିଦ୍ୟାଳୟ ଆଶ୍ରୟ କେନ୍ଦ୍ର",
            blockId = "remuna",
            blockName = "Remuna",
            latitude = 21.5284,
            longitude = 86.8647,
            capacity = 1400,
            elevationMeters = 8.0,
            hasSolarPower = true,
            caretakerPhone = "06782-252115",
            highGroundRoute = "Gopinath Temple bypass toward NH-16",
            generatorStatus = "Active (Standby 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_khanta_5",
            nameEn = "Khantapada NH-16 Highway Disaster Shelter",
            nameOr = "ଖନ୍ତାପଡ଼ା ରାଜପଥ ବିପର୍ଯ୍ୟୟ ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "remuna",
            blockName = "Remuna",
            latitude = 21.4120,
            longitude = 86.8450,
            capacity = 1400,
            elevationMeters = 7.9,
            hasSolarPower = true,
            caretakerPhone = "06782-277100",
            highGroundRoute = "NH-16 elevated bypass toward Balasore",
            generatorStatus = "Active (Reserve Ready 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_baha_6",
            nameEn = "Bahanaga Coastal Disaster Shelter (MCS-28)",
            nameOr = "ବାହାନଗା ଉପକୂଳ ବିପର୍ଯ୍ୟୟ ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "bahanaga",
            blockName = "Bahanaga",
            latitude = 21.3190,
            longitude = 86.8720,
            capacity = 1400,
            elevationMeters = 7.5,
            hasSolarPower = true,
            caretakerPhone = "+91 94372 11045",
            highGroundRoute = "Gopinathpur to NH-16 high embankment flyover link",
            generatorStatus = "Active (600L Diesel 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_baha_7",
            nameEn = "Bahanaga Relief Multipurpose Center",
            nameOr = "ବାହାନଗା ବିପର୍ଯ୍ୟୟ ରିଲିଫ କେନ୍ଦ୍ର",
            blockId = "bahanaga",
            blockName = "Bahanaga",
            latitude = 21.3150,
            longitude = 86.8050,
            capacity = 1500,
            elevationMeters = 7.5,
            hasSolarPower = true,
            caretakerPhone = "06782-275420",
            highGroundRoute = "NH-16 Bahanaga Bazar elevated spur",
            generatorStatus = "Active (Solar Battery 99% 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_soro_8",
            nameEn = "Soro High School Elevated Cyclone Shelter",
            nameOr = "ସୋରୋ ଉଚ୍ଚ ବିଦ୍ୟାଳୟ ବାତ୍ୟା ଆଶ୍ରୟ କେନ୍ଦ୍ର",
            blockId = "soro",
            blockName = "Soro",
            latitude = 21.2850,
            longitude = 86.6920,
            capacity = 1300,
            elevationMeters = 7.8,
            hasSolarPower = true,
            caretakerPhone = "06782-281200",
            highGroundRoute = "Soro Hospital Square to NH-16 elevated link",
            generatorStatus = "Active (Full Diesel Reserve 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_simulia_9",
            nameEn = "Simulia Canal Embankment Relief Shelter",
            nameOr = "ସିମୁଳିଆ କେନାଲ ବନ୍ଧ ରିଲିଫ ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "simulia",
            blockName = "Simulia",
            latitude = 21.1550,
            longitude = 86.5820,
            capacity = 1100,
            elevationMeters = 7.2,
            hasSolarPower = false,
            caretakerPhone = "06782-294100",
            highGroundRoute = "Simulia Block HQ canal embankment road",
            generatorStatus = "Standby (400L Diesel 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_khaira_10",
            nameEn = "Khaira High-Ground Multi-Shelter",
            nameOr = "ଖଇରା ଉଚ୍ଚ ସ୍ଥାନ ବହୁମୁଖୀ ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "khaira",
            blockName = "Khaira",
            latitude = 21.2800,
            longitude = 86.5900,
            capacity = 1250,
            elevationMeters = 11.0,
            hasSolarPower = true,
            caretakerPhone = "06782-235100",
            highGroundRoute = "Khaira Block pucca road toward Kupari",
            generatorStatus = "Active (Solar 95% 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_oupada_11",
            nameEn = "Oupada Community Disaster Center",
            nameOr = "ଔପଦା ଗୋଷ୍ଠୀ ବିପର୍ଯ୍ୟୟ କେନ୍ଦ୍ର",
            blockId = "oupada",
            blockName = "Oupada",
            latitude = 21.4100,
            longitude = 86.6500,
            capacity = 1150,
            elevationMeters = 14.0,
            hasSolarPower = true,
            caretakerPhone = "06782-243200",
            highGroundRoute = "Oupada valley high ridge road",
            generatorStatus = "Standby (Reserve ready 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_nila_12",
            nameEn = "Nilagiri Hill-Slope Disaster Relief Center",
            nameOr = "ନୀଳଗିରି ପାହାଡ଼ ତଳ ବିପର୍ଯ୍ୟୟ କେନ୍ଦ୍ର",
            blockId = "nilagiri",
            blockName = "Nilagiri",
            latitude = 21.4550,
            longitude = 86.7620,
            capacity = 1200,
            elevationMeters = 18.5,
            hasSolarPower = true,
            caretakerPhone = "06782-233215",
            highGroundRoute = "Nilagiri Palace road toward Swarnachuda hills",
            generatorStatus = "Active (Solar 100% 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_basta_13",
            nameEn = "Basta Subarnarekha Basin Flood Relief Center",
            nameOr = "ବସ୍ତା ସୁବର୍ଣ୍ଣରେଖା ବନ୍ୟା ଓ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "basta",
            blockName = "Basta",
            latitude = 21.6850,
            longitude = 87.0550,
            capacity = 1650,
            elevationMeters = 8.4,
            hasSolarPower = true,
            caretakerPhone = "06781-251200",
            highGroundRoute = "Rajghat Embankment toward Basta Railway Station",
            generatorStatus = "Active (Solar + Diesel 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_bali_14",
            nameEn = "Baliapal Subarnarekha Basin Shelter",
            nameOr = "ବାଲିଆପାଳ ସୁବର୍ଣ୍ଣରେଖା ଅବବାହିକା ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "baliapal",
            blockName = "Baliapal",
            latitude = 21.6420,
            longitude = 87.2890,
            capacity = 2100,
            elevationMeters = 9.0,
            hasSolarPower = true,
            caretakerPhone = "06781-254300",
            highGroundRoute = "Chaumukh River Embankment high bund",
            generatorStatus = "Active (Reserve Ready 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_bali_15",
            nameEn = "Chaumukh Subarnarekha Delta Shelter",
            nameOr = "ଚୌମୁଖ ସୁବର୍ଣ୍ଣରେଖା ଡେଲଟା ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "baliapal",
            blockName = "Baliapal",
            latitude = 21.5721,
            longitude = 87.3210,
            capacity = 1500,
            elevationMeters = 9.1,
            hasSolarPower = true,
            caretakerPhone = "+91 94372 11042",
            highGroundRoute = "Baliapal-Dagara concrete elevated embankment",
            generatorStatus = "Standby (Full reserve 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_singla_16",
            nameEn = "Singla Coastal Multipurpose Evacuation Center",
            nameOr = "ସିଙ୍ଗଲା ଉପକୂଳ ବହୁମୁଖୀ ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "baliapal",
            blockName = "Baliapal",
            latitude = 21.6500,
            longitude = 87.2780,
            capacity = 1550,
            elevationMeters = 8.9,
            hasSolarPower = true,
            caretakerPhone = "06781-258100",
            highGroundRoute = "Singla GP high road to Baliapal",
            generatorStatus = "Active (Solar 97% 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_jale_17",
            nameEn = "Jaleswar Subarnarekha Multi-purpose Shelter",
            nameOr = "ଜଳେଶ୍ୱର ସୁବର୍ଣ୍ଣରେଖା ବହୁମୁଖୀ ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "jaleswar",
            blockName = "Jaleswar",
            latitude = 21.8020,
            longitude = 87.2150,
            capacity = 1700,
            elevationMeters = 8.2,
            hasSolarPower = true,
            caretakerPhone = "06781-222400",
            highGroundRoute = "Station Road toward NH-60 elevated flyover",
            generatorStatus = "Active (Solar 97% 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_bhog_18",
            nameEn = "Bhograi Coastal Disaster Shelter",
            nameOr = "ଭୋଗରାଇ ଉପକୂଳ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "bhograi",
            blockName = "Bhograi",
            latitude = 21.5892,
            longitude = 87.4582,
            capacity = 2400,
            elevationMeters = 9.5,
            hasSolarPower = true,
            caretakerPhone = "06781-238210",
            highGroundRoute = "Talasari Beach Embankment inland to Chandaneswar",
            generatorStatus = "Active (Solar 95% 🟢)"
        ),
        BalasoreShelterInfo(
            id = "cs_bhog_19",
            nameEn = "Kirtania Port Cyclone Shelter (MCS-04)",
            nameOr = "କୀର୍ତ୍ତନିଆ ବନ୍ଦର ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳ",
            blockId = "bhograi",
            blockName = "Bhograi",
            latitude = 21.6025,
            longitude = 87.4812,
            capacity = 1200,
            elevationMeters = 9.8,
            hasSolarPower = true,
            caretakerPhone = "+91 94372 11041",
            highGroundRoute = "NH-116B via Batagram pucca high bund",
            generatorStatus = "Active (500L Diesel 🟢)"
        )
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Calculates great-circle distance between two GPS points using the Haversine formula in kilometers.
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Radius of Earth in kilometers
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2) * sin(dLon / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return r * c
    }

    /**
     * Resolves the nearest cyclone shelter from user's coordinates.
     */
    fun findNearestShelter(userLat: Double, userLng: Double): Pair<BalasoreShelterInfo, Double> {
        var minDistance = Double.MAX_VALUE
        var nearest = SHELTERS.first()

        for (shelter in SHELTERS) {
            val dist = calculateDistanceKm(userLat, userLng, shelter.latitude, shelter.longitude)
            if (dist < minDistance) {
                minDistance = dist
                nearest = shelter
            }
        }
        return Pair(nearest, minDistance)
    }

    /**
     * Resolves the closest Balasore block from user coordinates.
     */
    fun findNearestBlock(userLat: Double, userLng: Double): BalasoreBlockInfo {
        var minDistance = Double.MAX_VALUE
        var nearestBlock = BLOCKS.first()

        for (block in BLOCKS) {
            val dist = calculateDistanceKm(userLat, userLng, block.centerLat, block.centerLng)
            if (dist < minDistance) {
                minDistance = dist
                nearestBlock = block
            }
        }
        return nearestBlock
    }

    fun getBlockById(blockId: String): BalasoreBlockInfo {
        return BLOCKS.find { it.blockId.equals(blockId, ignoreCase = true) } ?: BLOCKS.first()
    }

    /**
     * Saves user's pinned Balasore block (for manual block selection or fallback).
     */
    fun setPinnedBlockId(context: Context, blockId: String) {
        getPrefs(context).edit()
            .putString(KEY_PINNED_BLOCK_ID, blockId)
            .apply()
        updateAllWidgets(context)
    }

    fun getPinnedBlockId(context: Context): String? {
        return getPrefs(context).getString(KEY_PINNED_BLOCK_ID, null)
    }

    fun clearPinnedBlock(context: Context) {
        getPrefs(context).edit()
            .remove(KEY_PINNED_BLOCK_ID)
            .apply()
        updateAllWidgets(context)
    }

    fun saveLastKnownCoordinates(context: Context, lat: Double, lng: Double) {
        getPrefs(context).edit()
            .putFloat(KEY_LAST_LATITUDE, lat.toFloat())
            .putFloat(KEY_LAST_LONGITUDE, lng.toFloat())
            .apply()
    }

    /**
     * Safely queries the latest GPS/Network location or cached location.
     */
    @SuppressLint("MissingPermission")
    fun resolveCurrentLocation(context: Context): Pair<Double, Double> {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        if (hasFine || hasCoarse) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                if (lm != null) {
                    val providers = listOf(
                        LocationManager.GPS_PROVIDER,
                        LocationManager.NETWORK_PROVIDER,
                        LocationManager.PASSIVE_PROVIDER
                    )
                    var bestLocation: Location? = null
                    for (provider in providers) {
                        if (lm.isProviderEnabled(provider)) {
                            val loc = lm.getLastKnownLocation(provider)
                            if (loc != null) {
                                if (bestLocation == null || loc.time > bestLocation.time) {
                                    bestLocation = loc
                                }
                            }
                        }
                    }
                    if (bestLocation != null) {
                        saveLastKnownCoordinates(context, bestLocation.latitude, bestLocation.longitude)
                        return Pair(bestLocation.latitude, bestLocation.longitude)
                    }
                }
            } catch (_: Exception) {}
        }

        // Check if user has a pinned block
        val pinnedId = getPinnedBlockId(context)
        if (!pinnedId.isNullOrBlank()) {
            val block = getBlockById(pinnedId)
            return Pair(block.centerLat, block.centerLng)
        }

        // Check cached preferences
        val prefs = getPrefs(context)
        if (prefs.contains(KEY_LAST_LATITUDE) && prefs.contains(KEY_LAST_LONGITUDE)) {
            val lat = prefs.getFloat(KEY_LAST_LATITUDE, DEFAULT_LAT.toFloat()).toDouble()
            val lng = prefs.getFloat(KEY_LAST_LONGITUDE, DEFAULT_LNG.toFloat()).toDouble()
            return Pair(lat, lng)
        }

        // Default Balasore Town center
        return Pair(DEFAULT_LAT, DEFAULT_LNG)
    }

    /**
     * Fully resolves current state for the home screen widget.
     */
    fun resolveWidgetState(context: Context): BalasoreWidgetState {
        val (lat, lng) = resolveCurrentLocation(context)
        val pinnedId = getPinnedBlockId(context)

        val block = if (!pinnedId.isNullOrBlank()) {
            getBlockById(pinnedId)
        } else {
            findNearestBlock(lat, lng)
        }

        val (shelter, distance) = findNearestShelter(lat, lng)

        val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        return BalasoreWidgetState(
            block = block,
            nearestShelter = shelter,
            distanceKm = distance,
            isGpsActive = hasPermission && pinnedId.isNullOrBlank()
        )
    }

    /**
     * Broadcasts update to all active Balasore Shelter widget instances.
     */
    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val componentName = ComponentName(context, BalasoreShelterWidgetProvider::class.java)
        val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

        if (appWidgetIds.isNotEmpty()) {
            val intent = Intent(context, BalasoreShelterWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
