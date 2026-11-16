package com.example.ui.features.ar

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.DisposableEffect
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.AppLanguage
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class BalasoreArHistoricalLandmark(
    val id: String,
    val nameEn: String,
    val nameOr: String,
    val eraEn: String,
    val eraOr: String,
    val dynasticOriginEn: String,
    val latitude: Double,
    val longitude: Double,
    val proximityRadiusMeters: Int,
    val badgeEmoji: String,
    val historicalOverlayEn: String,
    val historicalOverlayOr: String,
    val architecturalHighlights: List<String>,
    val gradientColors: List<Color>,
    val targetCategory: String = "HISTORICAL_LANDMARK", // "PUBLIC_AMENITY" or "HISTORICAL_LANDMARK"
    val amenityType: String = "HERITAGE" // "PUBLIC_TOILET", "POLICE_STATION", "CYCLONE_SHELTER", "WATER_STATION", "HERITAGE"
)

private val BALASORE_AR_LANDMARKS = listOf(
    BalasoreArHistoricalLandmark(
        id = "ar_amenity_station_sulabh_toilet",
        nameEn = "Sulabh Smart Public Toilet • Station Square",
        nameOr = "ସୁଲଭ ସ୍ମାର୍ଟ ସାର୍ବଜନୀନ ଶୌଚାଳୟ • ଷ୍ଟେସନ ଛକ",
        eraEn = "Public Amenity • Verified Clean (24x7)",
        eraOr = "ସାର୍ବଜନୀନ ସୁବିଧା • ୨୪ ଘଣ୍ଟିଆ ଖୋଲା",
        dynasticOriginEn = "Balasore Municipality Ward 14 • Accessibility Ramp & RO Water",
        latitude = 21.5002,
        longitude = 86.9205,
        proximityRadiusMeters = 450,
        badgeEmoji = "🚻",
        historicalOverlayEn = "Registered municipal public sanitation complex outside Balasore Railway Station & Sahadevkhunta Bus Stand. Equipped with sensor taps, differently-abled access ramp, baby-care room, and solar lighting.",
        historicalOverlayOr = "ବାଲେଶ୍ୱର ରେଳ ଷ୍ଟେସନ ଓ ସହଦେବଖୁଣ୍ଟା ବସ୍ ଷ୍ଟାଣ୍ଡ ନିକଟରେ ଅବସ୍ଥିତ ଆଧୁନିକ ସାର୍ବଜନୀନ ଶୌଚାଳୟ ଓ ପାନୀୟ ଜଳ କେନ୍ଦ୍ର।",
        architecturalHighlights = listOf(
            "Verified Operational Status • Cleaned every 2 hours",
            "Wheelchair-accessible ramp & separate ladies/gents cubicles",
            "Adjacent Sujal 24x7 Drink-From-Tap RO Kiosk"
        ),
        gradientColors = listOf(Color(0xFF064E3B), Color(0xFF047857), Color(0xFF10B981)),
        targetCategory = "PUBLIC_AMENITY",
        amenityType = "PUBLIC_TOILET"
    ),
    BalasoreArHistoricalLandmark(
        id = "ar_amenity_town_police_station",
        nameEn = "Balasore Town Model Police Station & Aid Post",
        nameOr = "ବାଲେଶ୍ୱର ଟାଉନ୍ ଆଦର୍ଶ ଥାନା ଓ ସହାୟତା କେନ୍ଦ୍ର",
        eraEn = "Public Safety Amenity • 24x7 Emergency (Dial 112)",
        eraOr = "ଜରୁରୀକାଳୀନ ସୁରକ୍ଷା କେନ୍ଦ୍ର • ୨୪x୭ ସକ୍ରିୟ (112)",
        dynasticOriginEn = "Odisha Police • Phandi Chhak / Kacheri Road Corridor",
        latitude = 21.4938,
        longitude = 86.9318,
        proximityRadiusMeters = 600,
        badgeEmoji = "🚓",
        historicalOverlayEn = "Primary district urban police headquarters and pedestrian citizen assistance booth. Offers immediate tourist safety support, Lost & Found reporting desk, and women & senior citizen PCR patrol dispatch.",
        historicalOverlayOr = "ଫାଣ୍ଡି ଛକ ଓ କଚେରୀ ରୋଡ୍ ନିକଟରେ ଅବସ୍ଥିତ ବାଲେଶ୍ୱର ଟାଉନ୍ ଥାନା। ପର୍ଯ୍ୟଟକ ସୁରକ୍ଷା ଓ ୨୪ ଘଣ୍ଟିଆ ପୋଲିସ୍ ସହାୟତା ଉପଲବ୍ଧ।",
        architecturalHighlights = listOf(
            "24x7 Reception Desk & Women Help Desk",
            "Direct ERSS 112 Patrol Vehicle Dispatch Hub",
            "CCTV Command Node for OT Road & Cinema Chhak"
        ),
        gradientColors = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF3B82F6)),
        targetCategory = "PUBLIC_AMENITY",
        amenityType = "POLICE_STATION"
    ),
    BalasoreArHistoricalLandmark(
        id = "ar_amenity_chandipur_cyclone_shelter",
        nameEn = "Chandipur OSDMA Multipurpose Cyclone Shelter",
        nameOr = "ଚାନ୍ଦିପୁର ବହୁମୁଖୀ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ (OSDMA)",
        eraEn = "Disaster Resilience Amenity • Emergency Shelter",
        eraOr = "ବିପର୍ଯ୍ୟୟ ଆଶ୍ରୟସ୍ଥଳୀ • ଓସଡମା (OSDMA)",
        dynasticOriginEn = "Odisha State Disaster Management Authority • Coastal Sector",
        latitude = 21.4452,
        longitude = 87.0155,
        proximityRadiusMeters = 750,
        badgeEmoji = "🌀",
        historicalOverlayEn = "Elevated reinforced concrete cyclone and tidal-surge shelter capable of housing 1,200 coastal residents and tourists. Stocked with solar backup generators, potable water tanks, first-aid kits, and early-warning siren towers.",
        historicalOverlayOr = "ଚାନ୍ଦିପୁର ଉପକୂଳରେ ୧୨୦୦ ଲୋକଙ୍କ ସୁରକ୍ଷା ପାଇଁ ନିର୍ମିତ ବହୁମୁଖୀ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ। ଏଠାରେ ପାନୀୟ ଜଳ, ସୌର ବିଦ୍ୟୁତ୍ ଓ ପ୍ରାଥମିକ ଚିକିତ୍ସା ଉପଲବ୍ଧ।",
        architecturalHighlights = listOf(
            "Stilt-elevated above 5-meter storm surge level",
            "Emergency satellite radio & automated coastal siren",
            "24x7 Drinking Water Reserve & Medical First-Aid Bay"
        ),
        gradientColors = listOf(Color(0xFF7C2D12), Color(0xFFC2410C), Color(0xFFF97316)),
        targetCategory = "PUBLIC_AMENITY",
        amenityType = "CYCLONE_SHELTER"
    ),
    BalasoreArHistoricalLandmark(
        id = "ar_amenity_cinema_chhak_water_atm",
        nameEn = "WATCO Sujal Drinking Water ATM & Eco Toilet • Cinema Chhak",
        nameOr = "ସୁଜଳ ବିଶୁଦ୍ଧ ପାନୀୟ ଜଳ ଏଟିଏମ୍ ଓ ଶୌଚାଳୟ • ସିନେମା ଛକ",
        eraEn = "Public Amenity • UV-Filtered Chilled Water",
        eraOr = "ସାର୍ବଜନୀନ ପାନୀୟ ଜଳ ଓ ସ୍ୱଚ୍ଛତା କେନ୍ଦ୍ର",
        dynasticOriginEn = "WATCO Balasore Division • Ward 12 Market Hub",
        latitude = 21.4910,
        longitude = 86.9285,
        proximityRadiusMeters = 450,
        badgeEmoji = "🚰",
        historicalOverlayEn = "24x7 automated Drink-From-Tap UV/RO water dispensing station and modular eco-restroom serving shoppers and pedestrians at Cinema Chhak and Motiganj Bazar.",
        historicalOverlayOr = "ସିନେମା ଛକ ଓ ମୋତିଗଞ୍ଜ ବଜାର ଯାତ୍ରୀଙ୍କ ପାଇଁ ୨୪ ଘଣ୍ଟିଆ ବିଶୁଦ୍ଧ ଥଣ୍ଡା ପାନୀୟ ଜଳ ଓ ଇକୋ-ଶୌଚାଳୟ।",
        architecturalHighlights = listOf(
            "Real-time TDS & pH sensor quality display (TDS < 65 ppm)",
            "Free potable drinking water refill point for pedestrians",
            "Co-located with modular bio-digester public toilet"
        ),
        gradientColors = listOf(Color(0xFF0C4A6E), Color(0xFF0369A1), Color(0xFF0EA5E9)),
        targetCategory = "PUBLIC_AMENITY",
        amenityType = "WATER_STATION"
    ),
    BalasoreArHistoricalLandmark(
        id = "ar_khirachora_remuna",
        nameEn = "Khirachora Gopinatha Temple, Remuna",
        nameOr = "ক্ষୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର, ରେମୁଣା",
        eraEn = "13th Century CE (Eastern Ganga Dynasty)",
        eraOr = "୧୩ଶ ଶତାବ୍ଦୀ (ପୂର୍ବ ଗଙ୍ଗ ବଂଶ)",
        dynasticOriginEn = "Commissioned by King Langula Narasingha Deva I (Builder of Konark)",
        latitude = 21.5298,
        longitude = 86.8732,
        proximityRadiusMeters = 500,
        badgeEmoji = "🛕",
        historicalOverlayEn = "Carved from rare black chlorite stone, the deity Gopinatha is flanked by Govinda and Madana Mohana. In the 14th century, the deity famously concealed a pot of condensed milk (Amruta Keli Khira) for saint Madhavendra Puri, earning the name 'Khirachora'.",
        historicalOverlayOr = "କଳା ମୁଗୁନି ପଥରରେ ନିର୍ମିତ ଏହି ଐତିହାସିକ ମନ୍ଦିରରେ ଭକ୍ତ ମାଧବେନ୍ଦ୍ର ପୁରୀଙ୍କ ପାଇଁ ପ୍ରଭୁ ଗୋପୀନାଥ କ୍ଷୀର ଚୋରି କରିଥିବାରୁ 'କ୍ଷୀରଚୋରା' ନାମରେ ପ୍ରସିଦ୍ଧ।",
        architecturalHighlights = listOf(
            "Rekha Deula & Pidha Jagamohana Kalinga style",
            "Visited by Sri Chaitanya Mahaprabhu in 1510 CE",
            "Sacred Rasabali & Amruta Keli Khira Bhog"
        ),
        gradientColors = listOf(Color(0xFF451A03), Color(0xFFB45309), Color(0xFFD97706))
    ),
    BalasoreArHistoricalLandmark(
        id = "ar_chandipur_intertidal",
        nameEn = "Chandipur-on-Sea & Maritime Coast",
        nameOr = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ଓ ସାମୁଦ୍ରିକ ଐତିହ୍ୟ",
        eraEn = "Quaternary Geomorphology & 1980s Defense Era",
        eraOr = "ଭୌଗୋଳିକ ବିସ୍ମୟ ଓ ପ୍ରତିରକ୍ଷା ଐତିହ୍ୟ",
        dynasticOriginEn = "Historic Utkal Coastal Salt Port & DRDO Proof Establishment (PXE est. 1894)",
        latitude = 21.4446,
        longitude = 87.0149,
        proximityRadiusMeters = 800,
        badgeEmoji = "🌊",
        historicalOverlayEn = "One of the only beaches on Earth where the Bay of Bengal recedes up to 5 kilometers twice daily along an ultra-gentle 1:1200 continental shelf gradient. Adjacent Proof & Experimental Establishment (PXE) dates back to 1894.",
        historicalOverlayOr = "ଦିନକୁ ଦୁଇଥର ସମୁଦ୍ର ଜଳ ପ୍ରାୟ ୫ କିଲୋମିଟର ପଛକୁ ଘୁଞ୍ଚିଯାଏ। ୧୮୯୪ ମସିହାରୁ ଏଠାରେ ଐତିହାସିକ ପ୍ରତିରକ୍ଷା ପରୀକ୍ଷଣ କେନ୍ଦ୍ର ଅବସ୍ଥିତ।",
        architecturalHighlights = listOf(
            "5 km rhythmic tidal recession twice every 24 hours",
            "250-million-year-old living fossil Horseshoe Crabs (Tachypleus gigas)",
            "Historic 1894 British Ordnance Proof Range & DRDO ITR"
        ),
        gradientColors = listOf(Color(0xFF0C4A6E), Color(0xFF0284C7), Color(0xFF06B6D4))
    ),
    BalasoreArHistoricalLandmark(
        id = "ar_raibania_fort",
        nameEn = "Raibania Medieval Fort Complex",
        nameOr = "ରାଇବଣିଆ ମଧ୍ୟযୁଗୀୟ ଦୁର୍ଗ ସମୂହ",
        eraEn = "13th Century CE (Medieval Kalinga Frontier)",
        eraOr = "୧୩ଶ ଶତାବ୍ଦୀ (ମଧ୍ୟଯୁଗୀୟ କଳିଙ୍ଗ ଦୁର୍ଗ)",
        dynasticOriginEn = "Eastern Ganga Frontier Citadel along the Subarnarekha Valley",
        latitude = 21.9245,
        longitude = 87.1755,
        proximityRadiusMeters = 650,
        badgeEmoji = "🏰",
        historicalOverlayEn = "Recognized in the Ain-i-Akbari as a cluster of massive laterite-and-mud forts guarding Kalinga's northern border. Houses 161 ancient water reservoirs (Gadia) and shrines of Jayachandi.",
        historicalOverlayOr = "ଆଇନ୍-ଇ-ଆକବରୀରେ ବର୍ଣ୍ଣିତ ପୂର୍ବ ଭାରତର ସର୍ବବୃହତ ମାଙ୍କଡ଼ା ପଥର ଦୁର୍ଗ, ଯାହା କଳିଙ୍ଗର ଉତ୍ତର ସୀମାକୁ ସୁରକ୍ଷା ଦେଉଥିଲା।",
        architecturalHighlights = listOf(
            "4 interlocking laterite stone fortresses (2 larger, 2 smaller)",
            "Over 160 ancient moats and step-reservoirs",
            "Jain & Shakta sculptural antiquities from 11th–13th Century"
        ),
        gradientColors = listOf(Color(0xFF3F2E21), Color(0xFF78350F), Color(0xFFA16207))
    ),
    BalasoreArHistoricalLandmark(
        id = "ar_dutch_british_loges",
        nameEn = "17th-Century Dutch & British Maritime Loges",
        nameOr = "୧୭ଶ ଶତାବ୍ଦୀ ଓଲନ୍ଦାଜ ଓ ବ୍ରିଟିଶ ବାଣିଜ୍ୟ କେନ୍ଦ୍ର",
        eraEn = "1633–1676 CE (Maritime Port of Pipili-Baleshwar)",
        eraOr = "୧୬୩୩–୧୬୭୬ ଖ୍ରୀଷ୍ଟାବ୍ଦ (ସାମୁଦ୍ରିକ ବନ୍ଦର ଯୁଗ)",
        dynasticOriginEn = "Dutch VOC, English, French & Danish Trading Factories at Barabati & Olandaz Sahi",
        latitude = 21.4942,
        longitude = 86.9368,
        proximityRadiusMeters = 450,
        badgeEmoji = "⚓",
        historicalOverlayEn = "Balasore was Odisha's premier deep-water textile & saltpetre port in the 1600s. Steeped pyramidal Dutch tombs from 1683 CE still stand at Olandaz Sahi near Barabati.",
        historicalOverlayOr = "୧୭ଶ ଶତାବ୍ଦୀରେ ବାଲେଶ୍ୱର ଓଡ଼ିଶାର ପ୍ରମୁଖ ସାମୁଦ୍ରିକ ବନ୍ଦର ଥିଲା। ବାରବାଟୀ ନିକଟସ୍ଥ ଓଲନ୍ଦାଜ ସାହିରେ ୧୬୮୩ ମସିହାର ପିରାମିଡ୍ ସ୍ତମ୍ଭ ଆଜି ବି ରହିଛି।",
        architecturalHighlights = listOf(
            "1683 CE Dutch VOC obelisk tombs at Olandaz Sahi",
            "Ancient saltpetre & tussar silk riverine dockyards on Budhabalanga",
            "First English trading factory on the Odisha coast (1633 CE)"
        ),
        gradientColors = listOf(Color(0xFF1E1B4B), Color(0xFF3730A3), Color(0xFF4F46E5))
    ),
    BalasoreArHistoricalLandmark(
        id = "ar_panchalingeswar",
        nameEn = "Panchalingeswar Hill Shrine, Nilagiri",
        nameOr = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପାହାଡ଼ ଶୈବପୀଠ, ନୀଳଗିରି",
        eraEn = "Ancient Shaivite & Nilagiri Royal Era",
        eraOr = "ପ୍ରାଚୀନ ଶୈବପୀଠ ଓ ନୀଳଗିରି ରାଜବଂଶ",
        dynasticOriginEn = "Consecrated in the Devagiri Hill Range of Kuldiha Biosphere",
        latitude = 21.4258,
        longitude = 86.7438,
        proximityRadiusMeters = 550,
        badgeEmoji = "⛰️",
        historicalOverlayEn = "Five self-manifested Shiva Lingas lie submerged under a perennial crystalline mountain stream flowing through the Eastern Ghats granite gorge. Pilgrims touch the Lingas through the cool flowing water.",
        historicalOverlayOr = "ଦେବଗିରି ପାହାଡ଼ର ଚିରସ୍ରୋତା ଝରଣା ତଳେ ପୂଜା ପାଉଥିବା ପଞ୍ଚ ଶିବଲିଙ୍ଗ, ଯାହାକୁ ଭକ୍ତମାନେ ପ୍ରବାହିତ ଜଳ ମଧ୍ୟରେ ସ୍ପର୍ଶ କରି ଦର୍ଶନ କରନ୍ତି।",
        architecturalHighlights = listOf(
            "300+ stone steps carved into Devagiri granite hill",
            "Perennial spring fed by Kuldiha evergreen forest aquifer",
            "Royal patronage of the Bhanja rulers of Nilagiri State"
        ),
        gradientColors = listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF10B981))
    )
)

private fun distanceMetersBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371000.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2) * sin(dLat / 2) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
        sin(dLon / 2) * sin(dLon / 2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

private fun bearingDegrees(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
    val dLon = Math.toRadians(lon2 - lon1)
    val y = sin(dLon) * cos(Math.toRadians(lat2))
    val x = cos(Math.toRadians(lat1)) * sin(Math.toRadians(lat2)) -
        sin(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * cos(dLon)
    val brng = Math.toDegrees(atan2(y, x))
    return ((brng + 360) % 360).toInt()
}

@Composable
fun ArLandmarkCameraSheet(
    language: AppLanguage,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClose)

    val context = LocalContext.current
    var isOdia by remember(language) { mutableStateOf(language == AppLanguage.ODIA) }

    var hasCameraPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    var hasLocationPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        )
    }

    var selectedWayfinderFilter by remember { mutableStateOf("ALL") } // "ALL", "PUBLIC_AMENITY", "PUBLIC_TOILET", "POLICE_STATION", "CYCLONE_SHELTER", "WATER_STATION", "HISTORICAL_LANDMARK"
    val filteredLandmarks = remember(selectedWayfinderFilter) {
        BALASORE_AR_LANDMARKS.filter { item ->
            when (selectedWayfinderFilter) {
                "ALL" -> true
                "PUBLIC_AMENITY" -> item.targetCategory == "PUBLIC_AMENITY"
                "HISTORICAL_LANDMARK" -> item.targetCategory == "HISTORICAL_LANDMARK"
                else -> item.amenityType == selectedWayfinderFilter
            }
        }.ifEmpty { BALASORE_AR_LANDMARKS }
    }

    var selectedLandmark by remember { mutableStateOf(BALASORE_AR_LANDMARKS.first()) }
    var userLat by remember { mutableDoubleStateOf(BALASORE_AR_LANDMARKS.first().latitude - 0.0012) }
    var userLon by remember { mutableDoubleStateOf(BALASORE_AR_LANDMARKS.first().longitude - 0.0010) }
    var simulatedDistanceMeters by remember { mutableFloatStateOf(140f) }
    var useProximitySimulation by remember { mutableStateOf(true) }
    var capturedCameraBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var userHeadingDeg by remember { mutableFloatStateOf(45f) }
    var isCompassSensorActive by remember { mutableStateOf(false) }

    // Register hardware compass sensor (Rotation Vector / Orientation) for live walking AR wayfinding
    DisposableEffect(context) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val rotationSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ORIENTATION)
        val listener = object : SensorEventListener {
            private val rotationMatrix = FloatArray(9)
            private val orientationAngles = FloatArray(3)
            override fun onSensorChanged(event: SensorEvent?) {
                if (event == null) return
                if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    SensorManager.getOrientation(rotationMatrix, orientationAngles)
                    val azimuth = ((Math.toDegrees(orientationAngles[0].toDouble()) + 360.0) % 360.0).toFloat()
                    userHeadingDeg = azimuth
                    isCompassSensorActive = true
                } else if (event.sensor.type == Sensor.TYPE_ORIENTATION && event.values.isNotEmpty()) {
                    userHeadingDeg = ((event.values[0] + 360f) % 360f)
                    isCompassSensorActive = true
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }
        if (sensorManager != null && rotationSensor != null) {
            sensorManager.registerListener(listener, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        }
        onDispose {
            sensorManager?.unregisterListener(listener)
        }
    }

    val cameraPreviewLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            capturedCameraBitmap = bmp
            Toast.makeText(
                context,
                "AR Historical Overlay locked onto live camera frame!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        hasCameraPerm = result[Manifest.permission.CAMERA] == true ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        hasLocationPerm = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (hasLocationPerm) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val loc: Location? = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) {
                    userLat = loc.latitude
                    userLon = loc.longitude
                }
            } catch (_: SecurityException) {
            }
        }
        if (hasCameraPerm) {
            cameraPreviewLauncher.launch(null)
        }
    }

    LaunchedEffect(hasLocationPerm) {
        if (hasLocationPerm) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val loc: Location? = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    ?: lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                if (loc != null) {
                    userLat = loc.latitude
                    userLon = loc.longitude
                }
            } catch (_: SecurityException) {
            }
        }
    }

    val computedGpsDistance = remember(userLat, userLon, selectedLandmark) {
        distanceMetersBetween(userLat, userLon, selectedLandmark.latitude, selectedLandmark.longitude)
    }
    val effectiveDistanceMeters = if (useProximitySimulation) {
        simulatedDistanceMeters.toInt()
    } else {
        computedGpsDistance.toInt()
    }
    val isInProximity = effectiveDistanceMeters <= selectedLandmark.proximityRadiusMeters
    val bearingDeg = remember(userLat, userLon, selectedLandmark) {
        bearingDegrees(userLat, userLon, selectedLandmark.latitude, selectedLandmark.longitude)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "ar_reticle_pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 0.88f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    val overlayCardScale by animateFloatAsState(
        targetValue = if (isInProximity) 1.0f else 0.96f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ar_card_spring"
    )

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("ar_landmark_camera_sheet"),
        color = Color(0xFF090D16)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewInAr,
                                contentDescription = "AR Landmark Historical Overlay",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isOdia) "AR ଐତିହାସିକ କ୍ୟାମେରା ଓଭରଲେ" else "AR Landmark Historical Overlay",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = if (isOdia) {
                                    "ନିକଟବର୍ତ୍ତୀ ଐତିହ୍ୟ ସ୍ଥଳୀରେ କ୍ୟାମେରା ଦ୍ୱାରା ଇତିହାସ ଦେଖନ୍ତୁ"
                                } else {
                                    "Proximity GPS + Camera Viewfinder Historical HUD"
                                },
                                fontSize = 11.5.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        FilterChip(
                            selected = isOdia,
                            onClick = { isOdia = !isOdia },
                            label = { Text(if (isOdia) "ଓଡ଼ିଆ" else "EN / ଓଡ଼ିଆ", fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                labelColor = Color.White
                            ),
                            modifier = Modifier.testTag("ar_landmark_lang_toggle")
                        )
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.14f))
                                .testTag("ar_landmark_close_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close AR View", tint = Color.White)
                        }
                    }
                }
            }

            // Permission & Live Camera Capture Status Bar
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF1E293B),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "GPS: ${if (hasLocationPerm) "ACTIVE" else "READY"} • Camera: ${if (hasCameraPerm) "GRANTED" else "TAP TO ENABLE"}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF38BDF8)
                            )
                            Text(
                                text = "Bearing ${bearingDeg}° NE • Distance: ${effectiveDistanceMeters}m (${if (isInProximity) "IN PROXIMITY" else "OUT OF RANGE"})",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }

                        Button(
                            onClick = {
                                if (hasCameraPerm && hasLocationPerm) {
                                    cameraPreviewLauncher.launch(null)
                                } else {
                                    permissionsLauncher.launch(
                                        arrayOf(
                                            Manifest.permission.CAMERA,
                                            Manifest.permission.ACCESS_FINE_LOCATION,
                                            Manifest.permission.ACCESS_COARSE_LOCATION
                                        )
                                    )
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                            modifier = Modifier.testTag("ar_capture_live_camera_btn")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (capturedCameraBitmap != null) "Retake AR Frame" else "Live Camera AR",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // AR Mode & Amenity Type Filter Chips + Snap to Nearest Public Amenity Button
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "ALL" to (if (isOdia) "🧭 ସମସ୍ତ (All AR)" else "🧭 All Wayfinder & Heritage"),
                            "PUBLIC_AMENITY" to (if (isOdia) "🚰 ସାର୍ବଜନୀନ ସୁବିଧା" else "🚰 Public Amenities"),
                            "PUBLIC_TOILET" to (if (isOdia) "🚻 ଶୌଚାଳୟ" else "🚻 Public Toilets"),
                            "POLICE_STATION" to (if (isOdia) "🚓 ଥାନା (Police)" else "🚓 Police Stations"),
                            "CYCLONE_SHELTER" to (if (isOdia) "🌀 ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ" else "🌀 Cyclone Shelters"),
                            "WATER_STATION" to (if (isOdia) "🚰 ପାନୀୟ ଜଳ" else "🚰 Water Stations"),
                            "HISTORICAL_LANDMARK" to (if (isOdia) "🏛️ ଐତିହ୍ୟ ସ୍ଥଳୀ" else "🏛️ Heritage Landmarks")
                        ).forEach { (filterKey, label) ->
                            val isSelected = selectedWayfinderFilter == filterKey
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedWayfinderFilter = filterKey
                                    val matching = BALASORE_AR_LANDMARKS.filter { item ->
                                        when (filterKey) {
                                            "ALL" -> true
                                            "PUBLIC_AMENITY" -> item.targetCategory == "PUBLIC_AMENITY"
                                            "HISTORICAL_LANDMARK" -> item.targetCategory == "HISTORICAL_LANDMARK"
                                            else -> item.amenityType == filterKey
                                        }
                                    }
                                    if (matching.isNotEmpty() && matching.none { it.id == selectedLandmark.id }) {
                                        selectedLandmark = matching.first()
                                    }
                                },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF0284C7),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.testTag("ar_wayfinder_filter_$filterKey")
                            )
                        }
                    }

                    // Snap to Nearest Registered Public Amenity Button
                    OutlinedButton(
                        onClick = {
                            val nearestAmenity = BALASORE_AR_LANDMARKS
                                .filter { it.targetCategory == "PUBLIC_AMENITY" }
                                .minByOrNull { distanceMetersBetween(userLat, userLon, it.latitude, it.longitude) }
                                ?: BALASORE_AR_LANDMARKS.first()
                            selectedWayfinderFilter = "PUBLIC_AMENITY"
                            selectedLandmark = nearestAmenity
                            simulatedDistanceMeters = 120f
                            userHeadingDeg = bearingDegrees(userLat, userLon, nearestAmenity.latitude, nearestAmenity.longitude).toFloat()
                            Toast.makeText(
                                context,
                                "Locked AR Wayfinder Arrow onto nearest public amenity: ${nearestAmenity.nameEn}",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, Color(0xFF34D399)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ar_locate_nearest_public_amenity_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = Color(0xFF34D399),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOdia) {
                                "🚶 ନିକଟତମ ସାର୍ବଜନୀନ ସୁବିଧା ଖୋଜନ୍ତୁ (AR Wayfinder)"
                            } else {
                                "🚶 Lock AR Wayfinder onto Nearest Public Amenity (Toilet / Police / Shelter)"
                            },
                            color = Color(0xFF34D399),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Target Selector Pills
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        filteredLandmarks.forEach { lm ->
                            val selected = selectedLandmark.id == lm.id
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    selectedLandmark = lm
                                    userLat = lm.latitude - 0.0010
                                    userLon = lm.longitude - 0.0009
                                },
                                label = {
                                    Text(
                                        text = "${lm.badgeEmoji} ${if (isOdia) lm.nameOr.take(20) else lm.nameEn.substringBefore("•").trim()}",
                                        fontSize = 11.5.sp,
                                        fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFD97706),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFF1E293B),
                                    labelColor = Color(0xFFE2E8F0)
                                ),
                                modifier = Modifier.testTag("ar_select_landmark_${lm.id}")
                            )
                        }
                    }
                }
            }

            // Interactive AR Wayfinder Compass HUD Pointing Towards Selected Amenity or Landmark
            item {
                val deltaDeg = remember(bearingDeg, userHeadingDeg) {
                    var diff = (bearingDeg - userHeadingDeg.toInt()) % 360
                    if (diff > 180) diff -= 360
                    if (diff < -180) diff += 360
                    diff
                }
                val isAlignedWithBookmark = kotlin.math.abs(deltaDeg) <= 15

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF111827)),
                    border = BorderStroke(
                        1.5.dp,
                        if (isAlignedWithBookmark) Color(0xFF34D399) else Color(0xFF38BDF8)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ar_interactive_bookmark_compass")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                // Interactive Compass Dial Canvas with Rotating Needle
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF0F172A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Canvas(modifier = Modifier.fillMaxSize()) {
                                        val r = size.minDimension / 2f - 6.dp.toPx()
                                        val centerPt = Offset(size.width / 2f, size.height / 2f)
                                        drawCircle(
                                            color = if (isAlignedWithBookmark) Color(0xFF34D399) else Color(0xFF38BDF8),
                                            radius = r,
                                            center = centerPt,
                                            style = Stroke(width = 2.dp.toPx())
                                        )
                                        val angleRad = Math.toRadians((deltaDeg - 90).toDouble())
                                        val tipX = centerPt.x + (r * 0.78f * kotlin.math.cos(angleRad)).toFloat()
                                        val tipY = centerPt.y + (r * 0.78f * kotlin.math.sin(angleRad)).toFloat()
                                        drawLine(
                                            color = if (isAlignedWithBookmark) Color(0xFF34D399) else Color(0xFFFBBF24),
                                            start = centerPt,
                                            end = Offset(tipX, tipY),
                                            strokeWidth = 3.5.dp.toPx()
                                        )
                                        drawCircle(
                                            color = Color.White,
                                            radius = 4.dp.toPx(),
                                            center = centerPt
                                        )
                                    }
                                    Text(
                                        text = "N",
                                        color = Color(0xFFF87171),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 2.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (isOdia) {
                                            "🧭 AR ବୁକମାର୍କ କମ୍ପାସ୍: ${selectedLandmark.nameOr}"
                                        } else {
                                            "🧭 Interactive AR Compass → ${selectedLandmark.nameEn}"
                                        },
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp
                                    )
                                    Text(
                                        text = "Target Bearing: ${bearingDeg}° • Your Heading: ${userHeadingDeg.toInt()}° • Dist: ${effectiveDistanceMeters}m",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 11.sp
                                    )
                                    Text(
                                        text = when {
                                            isAlignedWithBookmark -> if (isOdia) "✓ ଲକ୍ଷ୍ୟ ଦିଗରେ ଅଛନ୍ତି (ON TARGET)" else "✓ LOCKED ON BOOKMARKED LANDMARK (${effectiveDistanceMeters}m ahead)"
                                            deltaDeg > 0 -> if (isOdia) "↻ ଡାହାଣକୁ ${deltaDeg}° ঘୂରନ୍ତୁ" else "↻ Rotate ${deltaDeg}° Right towards ${selectedLandmark.nameEn}"
                                            else -> if (isOdia) "↺ ବାମକୁ ${-deltaDeg}° ଘୂରନ୍ତୁ" else "↺ Rotate ${-deltaDeg}° Left towards ${selectedLandmark.nameEn}"
                                        },
                                        color = if (isAlignedWithBookmark) Color(0xFF34D399) else Color(0xFFFBBF24),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }

                        // Interactive Compass Heading Rotation Slider + Snap-to-Landmark Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Slider(
                                value = userHeadingDeg,
                                onValueChange = { userHeadingDeg = it },
                                valueRange = 0f..359f,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ar_compass_heading_slider")
                            )
                            OutlinedButton(
                                onClick = { userHeadingDeg = bearingDeg.toFloat() },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                border = BorderStroke(1.dp, Color(0xFF34D399)),
                                modifier = Modifier.testTag("ar_compass_align_target_btn")
                            ) {
                                Text(
                                    text = if (isOdia) "ସିଧା କରନ୍ତୁ" else "Align (${bearingDeg}°)",
                                    color = Color(0xFF34D399),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Camera Viewfinder with Augmented Reality Historical Overlay
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(390.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .background(Brush.verticalGradient(selectedLandmark.gradientColors))
                        .testTag("ar_camera_viewfinder_box")
                ) {
                    // Optional Live Captured Camera Frame Underneath AR HUD
                    if (capturedCameraBitmap != null) {
                        Image(
                            bitmap = capturedCameraBitmap!!.asImageBitmap(),
                            contentDescription = "Captured AR Camera View",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.45f))
                        )
                    }

                    // AR Target Reticle, Compass Grid & Radar Canvas
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val cx = size.width / 2f
                        val cy = size.height * 0.28f
                        val reticleR = 46.dp.toPx() * pulseRadius

                        // Horizon dashed line
                        drawLine(
                            color = Color.White.copy(alpha = 0.3f),
                            start = Offset(24.dp.toPx(), cy),
                            end = Offset(size.width - 24.dp.toPx(), cy),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                        )

                        // Outer AR target ring
                        drawCircle(
                            color = if (isInProximity) Color(0xFF34D399) else Color(0xFFFBBF24),
                            radius = reticleR,
                            center = Offset(cx, cy),
                            style = Stroke(width = 2.5.dp.toPx())
                        )

                        // Inner target lock dot
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = Offset(cx, cy)
                        )

                        // Walking AR Wayfinder Directional Navigation Arrow Overlay
                        var turnDiff = (bearingDeg - userHeadingDeg.toInt()) % 360
                        if (turnDiff > 180) turnDiff -= 360
                        if (turnDiff < -180) turnDiff += 360
                        val arrowAngleRad = Math.toRadians((turnDiff - 90).toDouble())
                        val arrowCenter = Offset(cx, size.height * 0.48f)
                        val arrowLen = 36.dp.toPx()
                        val arrowTip = Offset(
                            arrowCenter.x + (arrowLen * cos(arrowAngleRad)).toFloat(),
                            arrowCenter.y + (arrowLen * sin(arrowAngleRad)).toFloat()
                        )
                        val arrowTail = Offset(
                            arrowCenter.x - (arrowLen * 0.6f * cos(arrowAngleRad)).toFloat(),
                            arrowCenter.y - (arrowLen * 0.6f * sin(arrowAngleRad)).toFloat()
                        )
                        val arrowColor = if (kotlin.math.abs(turnDiff) <= 15) Color(0xFF34D399) else Color(0xFF38BDF8)

                        // Halo ring around walking wayfinder arrow
                        drawCircle(
                            color = Color.Black.copy(alpha = 0.45f),
                            radius = 44.dp.toPx(),
                            center = arrowCenter
                        )
                        drawCircle(
                            color = arrowColor,
                            radius = 44.dp.toPx(),
                            center = arrowCenter,
                            style = Stroke(width = 2.5.dp.toPx())
                        )
                        // Shaft
                        drawLine(
                            color = arrowColor,
                            start = arrowTail,
                            end = arrowTip,
                            strokeWidth = 6.dp.toPx()
                        )
                        // Left & Right arrowheads
                        val wingAngle1 = arrowAngleRad + Math.toRadians(150.0)
                        val wingAngle2 = arrowAngleRad - Math.toRadians(150.0)
                        val wingLen = 16.dp.toPx()
                        drawLine(
                            color = arrowColor,
                            start = arrowTip,
                            end = Offset(
                                arrowTip.x + (wingLen * cos(wingAngle1)).toFloat(),
                                arrowTip.y + (wingLen * sin(wingAngle1)).toFloat()
                            ),
                            strokeWidth = 5.dp.toPx()
                        )
                        drawLine(
                            color = arrowColor,
                            start = arrowTip,
                            end = Offset(
                                arrowTip.x + (wingLen * cos(wingAngle2)).toFloat(),
                                arrowTip.y + (wingLen * sin(wingAngle2)).toFloat()
                            ),
                            strokeWidth = 5.dp.toPx()
                        )
                    }

                    // Top Viewfinder Telemetry Strip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .align(Alignment.TopCenter),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.65f),
                            border = BorderStroke(
                                1.dp,
                                if (isInProximity) Color(0xFF34D399) else Color(0xFFF59E0B)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = null,
                                    tint = if (isInProximity) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isInProximity) {
                                        "AR PROXIMITY LOCK • ${effectiveDistanceMeters}m"
                                    } else {
                                        "APPROACHING LANDMARK • ${effectiveDistanceMeters}m"
                                    },
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color.Black.copy(alpha = 0.65f)
                        ) {
                            Text(
                                text = "🧭 ${bearingDeg}° • ${String.format(Locale.US, "%.4f°N, %.4f°E", selectedLandmark.latitude, selectedLandmark.longitude)}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFBAE6FD),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Floating AR Landmark Pin Label at Center-Top
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.Black.copy(alpha = 0.72f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 68.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedLandmark.badgeEmoji, fontSize = 18.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isOdia) selectedLandmark.nameOr else selectedLandmark.nameEn,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.5.sp,
                                    color = Color.White
                                )
                                Text(
                                    text = if (isOdia) selectedLandmark.eraOr else selectedLandmark.eraEn,
                                    fontSize = 10.5.sp,
                                    color = Color(0xFFFDE68A),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Bottom Floating AR Historical Overlay Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(12.dp)
                            .scale(overlayCardScale)
                            .testTag("ar_historical_overlay_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFF0F172A).copy(alpha = 0.90f)
                        ),
                        border = BorderStroke(1.5.dp, Color(0xFF38BDF8).copy(alpha = 0.75f))
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.HistoryEdu,
                                        contentDescription = null,
                                        tint = Color(0xFFFBBF24),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = selectedLandmark.dynasticOriginEn,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFFDE68A),
                                        maxLines = 1
                                    )
                                }
                            }

                            Text(
                                text = if (isOdia) selectedLandmark.historicalOverlayOr else selectedLandmark.historicalOverlayEn,
                                fontSize = 12.sp,
                                color = Color.White,
                                lineHeight = 17.sp
                            )

                            selectedLandmark.architecturalHighlights.forEach { point ->
                                Text(
                                    text = "✦ $point",
                                    fontSize = 11.sp,
                                    color = Color(0xFFBAE6FD),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Proximity Radar Distance Simulator Slider & Share AR Postcard Actions
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    border = BorderStroke(1.dp, Color(0xFF334155)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOdia) {
                                    "🎯 ନିକଟତର ଦୂରତା ସିମୁଲେଟର୍ (${effectiveDistanceMeters} ମିଟର)"
                                } else {
                                    "🎯 Proximity Trigger Distance (${effectiveDistanceMeters}m / ${selectedLandmark.proximityRadiusMeters}m radius)"
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isInProximity) "ACTIVE OVERLAY" else "OUTSIDE RADIUS",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isInProximity) Color(0xFF34D399) else Color(0xFFFBBF24)
                            )
                        }

                        Slider(
                            value = simulatedDistanceMeters,
                            onValueChange = {
                                useProximitySimulation = true
                                simulatedDistanceMeters = it
                            },
                            valueRange = 25f..1000f,
                            modifier = Modifier.testTag("ar_proximity_distance_slider")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val navUri = Uri.parse("google.navigation:q=${selectedLandmark.latitude},${selectedLandmark.longitude}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (_: Exception) {
                                        context.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("geo:${selectedLandmark.latitude},${selectedLandmark.longitude}?q=${Uri.encode(selectedLandmark.nameEn)}")
                                            )
                                        )
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ar_landmark_navigate_btn")
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOdia) "ଦିଗନିର୍ଦ୍ଦେଶ" else "Navigate", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = {
                                    val shareBody = buildString {
                                        append("🏛️ Baleshwar 360 AR Historical Landmark: ${selectedLandmark.nameEn} (${selectedLandmark.nameOr})\n")
                                        append("Era: ${selectedLandmark.eraEn}\n")
                                        append("${selectedLandmark.historicalOverlayEn}\n")
                                        append("📍 Coordinates: ${selectedLandmark.latitude}, ${selectedLandmark.longitude}")
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "AR Heritage: ${selectedLandmark.nameEn}")
                                        putExtra(Intent.EXTRA_TEXT, shareBody)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share AR Landmark History"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF38BDF8)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("ar_landmark_share_btn")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOdia) "ସେୟାର୍ କରନ୍ତୁ" else "Share AR Card",
                                    fontSize = 12.sp,
                                    color = Color(0xFF38BDF8),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
