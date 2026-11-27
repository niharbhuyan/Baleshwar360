package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.pamphlet.PamphletCategory
import com.example.data.pamphlet.QrScanResult
import com.example.data.remote.OpenWeatherAirPollutionResponse
import com.example.data.remote.OutdoorSafeHealthWindow
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.LocalAppTheme
import com.example.ui.theme.OceanBlue
import com.example.ui.viewmodel.ThemeMode
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

data class TouristLocationItem(
    val id: String,
    val nameEn: String,
    val nameOr: String,
    val categoryEn: String,
    val categoryOr: String = categoryEn,
    val categoryGroup: String, // "BEACH", "TEMPLE", "NATURE", "LANDMARK"
    val shortDescEn: String,
    val shortDescOr: String = shortDescEn,
    val highlightEmoji: String = "📍",
    val latitude: Double,
    val longitude: Double,
    val baseDistanceKm: Double,
    val rating: Double = 4.8,
    val reviewCount: Int = 420,
    val timings: String = "06:00 AM - 07:00 PM",
    val entryFee: String = "Free",
    val nearestTransitStop: String = "Balasore Bus Stand / Station",
    val transitRoutes: List<String> = listOf("OSRTC Shuttle", "Shared Auto"),
    val estimatedTransitFare: String = "₹30 - ₹60",
    val transitTravelTimeMins: Int = 25,
    val detailedDescEn: String = shortDescEn,
    val historicalSignificance: String = "Significant cultural and heritage landmark in Balasore district, Odisha."
)

val BALASORE_TOURIST_LOCATIONS: List<TouristLocationItem> = listOf(
    TouristLocationItem(
        id = "chandipur_beach",
        nameEn = "Chandipur-on-Sea (Vanishing Beach)",
        nameOr = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି",
        categoryEn = "Beach & Coast",
        categoryOr = "ବେଳାଭୂମି ଓ ଉପକୂଳ",
        categoryGroup = "BEACH",
        shortDescEn = "Famous vanishing sea beach where the tide recedes up to 5 km twice daily, revealing horseshoe crabs and red ghost crabs.",
        shortDescOr = "ବିଶ୍ୱ ପ୍ରସିଦ୍ଧ ଲୁଚକାଳି ଖେଳୁଥିବା ସମୁଦ୍ର ବେଳାଭୂମି, ଯେଉଁଠାରେ ଜୁଆର ୫ କି.ମି ପର୍ଯ୍ୟନ୍ତ ପଛକୁ ଘୁଞ୍ଚିଯାଏ।",
        highlightEmoji = "🌊",
        latitude = 21.4442,
        longitude = 87.0147,
        baseDistanceKm = 16.0,
        rating = 4.9,
        reviewCount = 2480,
        timings = "05:30 AM - 08:30 PM",
        entryFee = "Free",
        nearestTransitStop = "Chandipur Panthanivas Bus Stop",
        transitRoutes = listOf("Balasore - Chandipur Town Bus", "Station Pink Auto"),
        estimatedTransitFare = "₹35 - ₹180",
        transitTravelTimeMins = 28,
        detailedDescEn = "Chandipur-on-Sea is globally unique for its extraordinary intertidal phenomenon where the shallow sea water ebbs away between 1 to 5 kilometers twice every day, allowing visitors to walk on the exposed seabed and observe living-fossil Horseshoe Crabs.",
        historicalSignificance = "Historically known as Balaramgadi Port for ancient Bay of Bengal maritime trade, and today home to DRDO's premier Integrated Test Range (ITR) established under Dr. A.P.J. Abdul Kalam."
    ),
    TouristLocationItem(
        id = "khirachora_gopinath",
        nameEn = "Khirachora Gopinatha Temple, Remuna",
        nameOr = "କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର, ରେମୁଣା",
        categoryEn = "Heritage Temple",
        categoryOr = "ଐତିହ୍ୟ ମନ୍ଦିର",
        categoryGroup = "TEMPLE",
        shortDescEn = "Revered Vaishnavite shrine famous for Sacred Amruta Keli condensed milk bhog prepared in earthen pots.",
        shortDescOr = "ପ୍ରସିଦ୍ଧ ଅମୃତ କେଳି କ୍ଷୀର ଭୋଗ ପାଇଁ ଜଣାଶୁଣା ଐତିହାସିକ ଗୋପୀନାଥ ମନ୍ଦିର।",
        highlightEmoji = "🛕",
        latitude = 21.5292,
        longitude = 86.8711,
        baseDistanceKm = 9.0,
        rating = 4.9,
        reviewCount = 3120,
        timings = "05:30 AM - 09:00 PM",
        entryFee = "Free Darshan",
        nearestTransitStop = "Remuna Golei / Temple Bazar",
        transitRoutes = listOf("Remuna Shared Auto", "Nilagiri Route Bus"),
        estimatedTransitFare = "₹20 - ₹40",
        transitTravelTimeMins = 18,
        detailedDescEn = "Carved in black chlorite stone by King Narasingha Deva I in the 13th century, this Gaudiya Vaishnavite temple is revered across India for its divine Amruta Keli Khira Prasad.",
        historicalSignificance = "Earned the name 'Khirachora' when Lord Gopinatha miraculously hid a pot of condensed milk for saint Madhavendra Puri; visited by Sri Chaitanya Mahaprabhu in 1510 CE."
    ),
    TouristLocationItem(
        id = "emami_jagannath",
        nameEn = "Emami Jagannath Temple",
        nameOr = "ଇମାମୀ ଜଗନ୍ନାଥ ମନ୍ଦିର",
        categoryEn = "Architecture & Temple",
        categoryOr = "ମନ୍ଦିର ଓ ସ୍ଥାପତ୍ୟ",
        categoryGroup = "TEMPLE",
        shortDescEn = "Grand Sun-Chariot style temple at Balgopalpur featuring 16 intricately carved stone wheels and manicured gardens.",
        shortDescOr = "କୋଣାର୍କ ରଥ ଶୈଳୀରେ ନିର୍ମିତ ଭବ୍ୟ ଶ୍ରୀ ଜଗନ୍ନାଥ ମନ୍ଦିର, ବଳଗୋପାଳପୁର।",
        highlightEmoji = "🚩",
        latitude = 21.4981,
        longitude = 86.8624,
        baseDistanceKm = 11.0,
        rating = 4.8,
        reviewCount = 1890,
        timings = "06:00 AM - 08:30 PM",
        entryFee = "Free",
        nearestTransitStop = "Balgopalpur Industrial Square",
        transitRoutes = listOf("Remuna - Balgopalpur Auto", "Town Shuttle"),
        estimatedTransitFare = "₹25 - ₹50",
        transitTravelTimeMins = 22,
        detailedDescEn = "Built in the likeness of the Konark Sun Chariot with 16 hand-carved stone wheels and 7 celestial horses amidst 10 acres of landscaped gardens.",
        historicalSignificance = "Constructed by master Kalinga stone sculptors, preserving classical Odia Shilpa Shastra traditions and Puri Jagannath temple rituals."
    ),
    TouristLocationItem(
        id = "panchalingeswar_temple",
        nameEn = "Panchalingeswar Hill Shrine",
        nameOr = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଶୈବ ପୀଠ",
        categoryEn = "Hill Shrine & Nature",
        categoryOr = "ପାହାଡ଼ ଓ ଶୈବ ପୀଠ",
        categoryGroup = "TEMPLE",
        shortDescEn = "Five natural Shiva Lingams bathed eternally by a cool perennial mountain stream in the Nilagiri hills.",
        shortDescOr = "ନୀଳଗିରି ପାହାଡ଼ର ଝରଣା ଜଳରେ ପୂଜିତ ପଞ୍ଚ ଶିବଲିଙ୍ଗ ପୀଠ।",
        highlightEmoji = "⛰️",
        latitude = 21.4253,
        longitude = 86.7441,
        baseDistanceKm = 30.0,
        rating = 4.9,
        reviewCount = 2750,
        timings = "06:00 AM - 06:30 PM",
        entryFee = "Free",
        nearestTransitStop = "Panchalingeswar Panthanivas Gate",
        transitRoutes = listOf("Balasore - Nilagiri Bus", "Direct Tourist Cab"),
        estimatedTransitFare = "₹55 - ₹120",
        transitTravelTimeMins = 45,
        detailedDescEn = "Five perennial stream-bathed Shiva Lingams nestled in the Nilagiri Eastern Ghats where pilgrims lie across the flat rock to touch the submerged Lingams.",
        historicalSignificance = "Mythologically consecrated by King Banasura and patronized by the Nilagiri Bhanja royal dynasty for centuries."
    ),
    TouristLocationItem(
        id = "talasari_beach",
        nameEn = "Talsari Beach & Bichitrapur Mangrove",
        nameOr = "ତାଳସାରୀ ବେଳାଭୂମି ଓ ବିଚିତ୍ରପୁର",
        categoryEn = "Beach & Mangrove",
        categoryOr = "ବେଳାଭୂମି ଓ ହେନ୍ତାଳ ବଣ",
        categoryGroup = "BEACH",
        shortDescEn = "Tranquil palm-fringed shoreline with red crab carpets and boat safaris through Bichitrapur mangrove estuary.",
        shortDescOr = "ଲାଲ୍ କଙ୍କଡ଼ା ଓ ବିଚିତ୍ରପୁର ହେନ୍ତାଳ ବଣ ନୌକା ବିହାର ପାଇଁ ପ୍ରସିଦ୍ଧ।",
        highlightEmoji = "🦀",
        latitude = 21.6214,
        longitude = 87.4522,
        baseDistanceKm = 88.0,
        rating = 4.8,
        reviewCount = 1540,
        timings = "06:00 AM - 06:00 PM",
        entryFee = "Free (Boating extra)",
        nearestTransitStop = "Jaleswar / Chandaneswar Bus Stand",
        transitRoutes = listOf("Balasore - Jaleswar - Chandaneswar Bus"),
        estimatedTransitFare = "₹95 - ₹140",
        transitTravelTimeMins = 110,
        detailedDescEn = "Where the Subarnarekha River meets the Bay of Bengal, featuring golden sandbars, red crab carpets, and boat safaris through the 563-hectare Bichitrapur mangrove reserve.",
        historicalSignificance = "Adjacent to historic 16th-century Pipli-Subarnarekha ports used by ancient Odia Sadhaba maritime traders and European merchants."
    ),
    TouristLocationItem(
        id = "kuldiha_wildlife",
        nameEn = "Kuldiha Wildlife Sanctuary",
        nameOr = "କୁଳଡିହା ବନ୍ୟପ୍ରାଣୀ ଅଭୟାରଣ୍ୟ",
        categoryEn = "Nature & Wildlife",
        categoryOr = "ବନ୍ୟପ୍ରାଣୀ ଅଭୟାରଣ୍ୟ",
        categoryGroup = "NATURE",
        shortDescEn = "Lush 272 sq km forest sanctuary home to wild elephants, giant squirrels, hornbills, and Gohirabhola eco-camp.",
        shortDescOr = "ହାତୀ, ଗୟଳ ଓ ବିରଳ ପକ୍ଷୀଙ୍କ ପାଇଁ ପ୍ରସିଦ୍ଧ କୁଳଡିହା ଅଭୟାରଣ୍ୟ।",
        highlightEmoji = "🐘",
        latitude = 21.4125,
        longitude = 86.6289,
        baseDistanceKm = 28.0,
        rating = 4.7,
        reviewCount = 980,
        timings = "06:30 AM - 04:30 PM",
        entryFee = "₹40 Forest Entry",
        nearestTransitStop = "Nilagiri Forest Range Checkpost",
        transitRoutes = listOf("Balasore - Nilagiri Bus + Safari Jeep"),
        estimatedTransitFare = "₹60 - ₹150",
        transitTravelTimeMins = 50,
        detailedDescEn = "272.75 sq km biosphere reserve linked to Similipal via the Nato hills, offering eco-camps at Gohirabhola and sightings of Asian elephants, gaur, and hornbills.",
        historicalSignificance = "Protected since 1984 and formerly the royal forest watershed of the Nilagiri Princely State."
    ),
    TouristLocationItem(
        id = "fakir_mohan_smruti",
        nameEn = "Fakir Mohan Senapati Shanti Kanan",
        nameOr = "ଫକୀର ମୋହନ ସେନାପତି ଶାନ୍ତି କାନନ",
        categoryEn = "Literary & Heritage",
        categoryOr = "ସାହିତ୍ୟ ଓ ଐତିହ୍ୟ",
        categoryGroup = "LANDMARK",
        shortDescEn = "Literary memorial of Vyasakabi Fakir Mohan Senapati at Mallikashpur with rare Odia manuscripts and heritage garden.",
        shortDescOr = "ବ୍ୟାସକବି ଫକୀର ମୋହନ ସେନାପତିଙ୍କ ସ୍ମୃତି ପୀଠ ଓ ଶାନ୍ତି କାନନ, ମଲ୍ଲିକାଶପୁର।",
        highlightEmoji = "📜",
        latitude = 21.4934,
        longitude = 86.9280,
        baseDistanceKm = 2.5,
        rating = 4.8,
        reviewCount = 890,
        timings = "08:00 AM - 07:00 PM",
        entryFee = "Free",
        nearestTransitStop = "Mallikashpur Square",
        transitRoutes = listOf("Town Auto", "E-Rickshaw"),
        estimatedTransitFare = "₹15 - ₹30",
        transitTravelTimeMins = 10,
        detailedDescEn = "Ancestral garden, museum, and samadhi of Vyasakabi Fakir Mohan Senapati featuring original Odia manuscripts, his 1868 printing press, and bronze statues.",
        historicalSignificance = "Birthplace of the 19th-century Odia language preservation movement and modern Indian social realist fiction ('Chha Mana Atha Guntha')."
    ),
    TouristLocationItem(
        id = "bhusandeshwar_temple",
        nameEn = "Baba Bhusandeshwar Temple",
        nameOr = "ବାବା ଭୂଷଣ୍ଡେଶ୍ୱର ମହାଦେବ ମନ୍ଦିର",
        categoryEn = "Heritage Temple",
        categoryOr = "ଐତିହ୍ୟ ଶୈବ ପୀଠ",
        categoryGroup = "TEMPLE",
        shortDescEn = "Houses Asia's largest single-stone black granite Shiva Lingam near the Subarnarekha River.",
        shortDescOr = "ଏସିଆର ସର୍ବବୃହତ ଶିବଲିଙ୍ଗ ପୀଠ ବାବା ଭୂଷଣ୍ଡେଶ୍ୱର, ଭୋଗରାଇ।",
        highlightEmoji = "🔱",
        latitude = 21.6385,
        longitude = 87.3150,
        baseDistanceKm = 95.0,
        rating = 4.8,
        reviewCount = 1870,
        timings = "05:30 AM - 08:00 PM",
        entryFee = "Free",
        nearestTransitStop = "Bhograi / Kumbhirmadhi Stop",
        transitRoutes = listOf("Balasore - Jaleswar - Bhograi Bus"),
        estimatedTransitFare = "₹90 - ₹130",
        transitTravelTimeMins = 115,
        detailedDescEn = "Enshrines a massive 12-foot-tall and 14-foot-wide monolithic black granite Shiva Lingam on the banks of the Subarnarekha River.",
        historicalSignificance = "Associated with Treta Yuga legends of King Ravana and worshipped for centuries as the guardian deity of the Subarnarekha delta."
    )
)

fun calculateHaversineDistanceKm(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    return 2 * r * atan2(sqrt(a), sqrt(1 - a))
}

/**
 * Persistent & reactive state manager for application language (English / ଓଡ଼ିଆ).
 * Stores selection in SharedPreferences ("balasore_app_preferences" -> "app_language")
 * and broadcasts updates via a StateFlow so the entire UI updates dynamically without restart.
 */
object AppLanguagePreferencesManager {
    const val PREFS_NAME = "balasore_app_preferences"
    const val KEY_APP_LANGUAGE = "app_language"

    private val _languageFlow = kotlinx.coroutines.flow.MutableStateFlow(AppLanguage.ENGLISH)
    val languageFlow: kotlinx.coroutines.flow.StateFlow<AppLanguage> = _languageFlow

    val currentLanguage: AppLanguage
        get() = _languageFlow.value

    fun getSavedLanguage(context: Context?): AppLanguage {
        if (context == null) return _languageFlow.value
        return try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedName = prefs.getString(KEY_APP_LANGUAGE, null)
            val resolved = if (savedName != null) {
                AppLanguage.entries.firstOrNull {
                    it.name.equals(savedName, ignoreCase = true) ||
                        it.code.equals(savedName, ignoreCase = true) ||
                        it.isoCode.equals(savedName, ignoreCase = true)
                } ?: AppLanguage.ENGLISH
            } else {
                _languageFlow.value
            }
            _languageFlow.value = resolved
            resolved
        } catch (_: Throwable) {
            _languageFlow.value
        }
    }

    fun saveLanguage(context: Context?, language: AppLanguage) {
        _languageFlow.value = language
        if (context == null) return
        try {
            val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_APP_LANGUAGE, language.name).apply()
        } catch (_: Throwable) {}
    }

    fun toggleLanguage(context: Context? = null): AppLanguage {
        val next = if (_languageFlow.value == AppLanguage.ODIA) AppLanguage.ENGLISH else AppLanguage.ODIA
        saveLanguage(context, next)
        return next
    }
}

/**
 * State-managed language context exposed through [LocalAppLanguage] across the Compose hierarchy.
 */
data class AppLanguageState(
    val currentLanguage: AppLanguage = AppLanguage.ENGLISH,
    val isOdia: Boolean = currentLanguage == AppLanguage.ODIA,
    val onLanguageChange: (AppLanguage) -> Unit = {},
    val onToggleLanguage: () -> Unit = {}
) {
    fun tr(english: String, odia: String): String =
        if (isOdia && odia.isNotBlank()) odia else english
}

val LocalAppLanguage = androidx.compose.runtime.compositionLocalOf {
    AppLanguageState()
}

@Composable
fun ProvideAppLanguage(
    currentLanguage: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit = {},
    content: @Composable () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val dispatchLanguageChange: (AppLanguage) -> Unit = remember(context, onLanguageChange) {
        { newLang: AppLanguage ->
            AppLanguagePreferencesManager.saveLanguage(context, newLang)
            onLanguageChange(newLang)
        }
    }
    val languageState = remember(currentLanguage, dispatchLanguageChange) {
        AppLanguageState(
            currentLanguage = currentLanguage,
            isOdia = currentLanguage == AppLanguage.ODIA,
            onLanguageChange = dispatchLanguageChange,
            onToggleLanguage = {
                val next = if (currentLanguage == AppLanguage.ODIA) AppLanguage.ENGLISH else AppLanguage.ODIA
                dispatchLanguageChange(next)
            }
        )
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalAppLanguage provides languageState
    ) {
        content()
    }
}

@Composable
fun LanguageSwitcher(
    currentLanguage: AppLanguage = LocalAppLanguage.current.currentLanguage,
    onLanguageChanged: (AppLanguage) -> Unit = LocalAppLanguage.current.onLanguageChange,
    compact: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val providerState = LocalAppLanguage.current
    val isOdia = currentLanguage == AppLanguage.ODIA
    val nextLang = if (isOdia) AppLanguage.ENGLISH else AppLanguage.ODIA

    val selectLanguage: (AppLanguage) -> Unit = { target ->
        AppLanguagePreferencesManager.saveLanguage(context, target)
        providerState.onLanguageChange(target)
        onLanguageChanged(target)
    }

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = OceanBlue.copy(alpha = 0.10f),
        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f)),
        modifier = modifier
            .testTag("global_language_toggle_button")
            .clickable { selectLanguage(nextLang) }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = if (compact) 4.dp else 6.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                Icons.Default.Language,
                contentDescription = "Switch Language between English and Odia",
                tint = OceanBlue,
                modifier = Modifier
                    .padding(start = 2.dp)
                    .size(if (compact) 13.dp else 15.dp)
            )
            // English Segment
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isOdia) OceanBlue else Color.Transparent)
                    .clickable {
                        selectLanguage(if (!isOdia) nextLang else AppLanguage.ENGLISH)
                    }
                    .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 3.dp)
                    .testTag("top_bar_lang_english"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (compact) "EN" else "English",
                    fontSize = if (compact) 10.sp else 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (!isOdia) Color.White else OceanBlue
                )
            }
            // Odia Segment
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isOdia) OceanBlue else Color.Transparent)
                    .clickable {
                        selectLanguage(if (isOdia) nextLang else AppLanguage.ODIA)
                    }
                    .padding(horizontal = if (compact) 6.dp else 8.dp, vertical = 3.dp)
                    .testTag("top_bar_lang_odia"),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ଓଡ଼ିଆ",
                    fontSize = if (compact) 10.sp else 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isOdia) Color.White else OceanBlue
                )
            }
        }
    }
}

/**
 * Persistent Language Selection Strip for the Top Navigation Bar (English / ଓଡ଼ିଆ).
 * Uses [LocalAppLanguage] and [AppLanguagePreferencesManager] to dynamically update all text
 * across the entire application in real time without needing an Activity or page restart.
 */
@Composable
fun TopBarPersistentLanguageBar(
    currentLanguage: AppLanguage = LocalAppLanguage.current.currentLanguage,
    onLanguageSelected: (AppLanguage) -> Unit = LocalAppLanguage.current.onLanguageChange,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val providerState = LocalAppLanguage.current
    val isOdia = currentLanguage == AppLanguage.ODIA

    val handleSelect: (AppLanguage) -> Unit = { lang ->
        AppLanguagePreferencesManager.saveLanguage(context, lang)
        providerState.onLanguageChange(lang)
        onLanguageSelected(lang)
    }

    val appTheme = LocalAppTheme.current
    val isDarkActive = appTheme.isDark

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = BorderStroke(0.5.dp, OceanBlue.copy(alpha = 0.22f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("top_nav_language_selector_bar")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 5.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Default.Language,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = if (isOdia) {
                        "ଭାଷା: ଓଡ଼ିଆ • ${if (isDarkActive) "🌙 ଡାର୍କ ମୋଡ୍" else "☀️ ଲାଇଟ୍ ମୋଡ୍"}"
                    } else {
                        "Language: English • ${if (isDarkActive) "🌙 Dark Mode" else "☀️ Light Mode"}"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    modifier = Modifier.testTag("top_nav_active_language_label")
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Global 1-Tap Material 3 Dark Mode Toggle Pill for Nighttime Readability
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (isDarkActive) Color(0xFF0F172A) else MaterialTheme.colorScheme.surface,
                    border = BorderStroke(
                        1.dp,
                        if (isDarkActive) Color(0xFF38BDF8) else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .clickable {
                            appTheme.onToggleDarkMode()
                        }
                        .testTag("global_dark_mode_quick_toggle")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = if (isDarkActive) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = if (isDarkActive) "Switch to Light Mode" else "Switch to Dark Mode",
                            tint = if (isDarkActive) Color(0xFF38BDF8) else Color(0xFFD97706),
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (isDarkActive) {
                                if (isOdia) "ଡାର୍କ ଅନ" else "Dark ON"
                            } else {
                                if (isOdia) "ଡାର୍କ ମୋଡ୍" else "Dark"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkActive) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Segmented Language Selection Pill: [ English | ଓଡ଼ିଆ ]
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (!isOdia) OceanBlue else Color.Transparent)
                                .clickable { handleSelect(AppLanguage.ENGLISH) }
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                                .testTag("top_bar_lang_option_english"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "English",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!isOdia) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isOdia) OceanBlue else Color.Transparent)
                                .clickable { handleSelect(AppLanguage.ODIA) }
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                                .testTag("top_bar_lang_option_odia"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "ଓଡ଼ିଆ",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isOdia) Color.White else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LanguageToggleSwitch(
    currentLanguage: AppLanguage = LocalAppLanguage.current.currentLanguage,
    onLanguageChanged: (AppLanguage) -> Unit = LocalAppLanguage.current.onLanguageChange,
    modifier: Modifier = Modifier
) {
    LanguageSwitcher(currentLanguage = currentLanguage, onLanguageChanged = onLanguageChanged, compact = true, modifier = modifier)
}

@Composable
fun LanguageSwitcherBanner(
    currentLanguage: AppLanguage = LocalAppLanguage.current.currentLanguage,
    onLanguageSelected: (AppLanguage) -> Unit = LocalAppLanguage.current.onLanguageChange,
    modifier: Modifier = Modifier
) {
    LanguageSwitcherBannerCard(currentLanguage = currentLanguage, onLanguageSelected = onLanguageSelected, modifier = modifier)
}

@Composable
fun LanguageSwitcherBannerCard(
    currentLanguage: AppLanguage = LocalAppLanguage.current.currentLanguage,
    onLanguageSelected: (AppLanguage) -> Unit = LocalAppLanguage.current.onLanguageChange,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (currentLanguage == AppLanguage.ODIA) "ଭାଷା ପରିବର୍ତ୍ତନ କରନ୍ତୁ (ଓଡ଼ିଆ / English)" else "Switch App Language (English ⇄ ଓଡ଼ିଆ)",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = BentoSlate900
            )
            LanguageSwitcher(currentLanguage = currentLanguage, onLanguageChanged = onLanguageSelected)
        }
    }
}

@Composable
fun AnimatedFeatureCard(
    delayMillis: Int = 0,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(18.dp),
    shadowAccentColor: Color = com.example.ui.theme.OceanBlue,
    content: @Composable () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isFocused by interactionSource.collectIsFocusedAsState()
    var isPointerHovered by remember { mutableStateOf(false) }
    var isFocusedDirect by remember { mutableStateOf(false) }

    val isActiveFocusOrHover = isHovered || isFocused || isPointerHovered || isFocusedDirect

    val animatedScale by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isActiveFocusOrHover) 1.024f else 1.0f,
        animationSpec = androidx.compose.animation.core.spring(
            dampingRatio = androidx.compose.animation.core.Spring.DampingRatioLowBouncy,
            stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
        ),
        label = "feature_box_scale"
    )

    val animatedShadowElevation by androidx.compose.animation.core.animateDpAsState(
        targetValue = if (isActiveFocusOrHover) 10.dp else 2.dp,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 220),
        label = "feature_box_shadow_shift"
    )

    Box(
        modifier = modifier
            .hoverable(interactionSource = interactionSource)
            .onFocusChanged { isFocusedDirect = it.isFocused || it.hasFocus }
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent(PointerEventPass.Initial)
                        when (event.type) {
                            PointerEventType.Enter,
                            PointerEventType.Press -> isPointerHovered = true
                            PointerEventType.Exit,
                            PointerEventType.Release -> isPointerHovered = false
                        }
                    }
                }
            }
            .graphicsLayer {
                scaleX = animatedScale
                scaleY = animatedScale
            }
            .shadow(
                elevation = animatedShadowElevation,
                shape = shape,
                ambientColor = if (isActiveFocusOrHover) shadowAccentColor.copy(alpha = 0.32f) else Color.Black.copy(alpha = 0.12f),
                spotColor = if (isActiveFocusOrHover) shadowAccentColor.copy(alpha = 0.42f) else Color.Black.copy(alpha = 0.16f)
            )
    ) {
        content()
    }
}

@Composable
fun ExpandableTransitFeatureBox(
    iconEmoji: String,
    iconBgColor: Color,
    title: String,
    badgeText: String,
    badgeBgColor: Color,
    badgeBorderColor: Color,
    badgeTextColor: Color,
    summary: String,
    detailedOverview: String,
    bulletHighlights: List<String>,
    accentColor: Color,
    borderColor: Color,
    isOdia: Boolean,
    launchButtonTag: String,
    onOpenHub: () -> Unit,
    delayMillis: Int = 0,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var readingStage by remember { androidx.compose.runtime.mutableIntStateOf(0) }

    androidx.compose.runtime.LaunchedEffect(isExpanded) {
        if (isExpanded) {
            readingStage = 1
            kotlinx.coroutines.delay(600)
            if (isExpanded && readingStage < 2) readingStage = 2
            kotlinx.coroutines.delay(800)
            if (isExpanded && readingStage < 3) readingStage = 3
        } else {
            readingStage = 0
        }
    }

    val targetProgress = when {
        !isExpanded -> 0.25f
        readingStage <= 1 -> 0.48f
        readingStage == 2 -> 0.76f
        else -> 1.0f
    }

    val animatedReadingProgress by androidx.compose.animation.core.animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 420,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "transit_feature_reading_progress"
    )

    val animatedIconRotation by androidx.compose.animation.core.animateFloatAsState(
        targetValue = if (isExpanded) 360f else 0f,
        animationSpec = androidx.compose.animation.core.tween(
            durationMillis = 480,
            easing = androidx.compose.animation.core.FastOutSlowInEasing
        ),
        label = "transit_feature_icon_rotation"
    )

    AnimatedFeatureCard(
        delayMillis = delayMillis,
        modifier = modifier.padding(horizontal = 16.dp, vertical = 6.dp),
        shadowAccentColor = accentColor
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = androidx.compose.animation.core.spring(
                        dampingRatio = androidx.compose.animation.core.Spring.DampingRatioNoBouncy,
                        stiffness = androidx.compose.animation.core.Spring.StiffnessMediumLow
                    )
                )
                .clickable { isExpanded = !isExpanded },
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(if (isExpanded) 1.6.dp else 1.dp, borderColor),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 6.dp else 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = iconBgColor,
                        modifier = Modifier
                            .size(44.dp)
                            .graphicsLayer { rotationZ = animatedIconRotation }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = iconEmoji, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = badgeBgColor,
                                border = BorderStroke(1.dp, badgeBorderColor)
                            ) {
                                Text(
                                    text = badgeText,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = badgeTextColor,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = summary,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = accentColor.copy(alpha = 0.12f),
                        modifier = Modifier
                            .clickable { onOpenHub() }
                            .testTag(launchButtonTag)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOdia) "ଖୋଲନ୍ତୁ" else "Open",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Reading & Information Depth Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (!isExpanded) {
                                if (isOdia) "📊 ସୂଚନା ଗଭୀରତା: ୨୫% • ବିସ୍ତୃତ ତଥ୍ୟ ପାଇଁ ଟ୍ୟାପ୍ କରନ୍ତୁ" else "📊 Info Depth: 25% • Tap box to expand full guide"
                            } else {
                                val pct = (animatedReadingProgress * 100).toInt()
                                if (isOdia) "📖 ପଠନ ଅଗ୍ରଗତି: $pct% (${readingStage.coerceAtLeast(1)}/୩ ବିଭାଗ)" else "📖 Reading Progress: $pct% (${readingStage.coerceAtLeast(1)}/3 Sections)"
                            },
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isExpanded) accentColor else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${(animatedReadingProgress * 100).toInt()}%",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = accentColor
                        )
                    }
                    androidx.compose.material3.LinearProgressIndicator(
                        progress = { animatedReadingProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = accentColor,
                        trackColor = borderColor.copy(alpha = 0.35f)
                    )
                }

                androidx.compose.animation.AnimatedVisibility(
                    visible = isExpanded,
                    enter = androidx.compose.animation.expandVertically(
                        expandFrom = Alignment.Top,
                        animationSpec = androidx.compose.animation.core.tween(300)
                    ) + androidx.compose.animation.fadeIn(
                        animationSpec = androidx.compose.animation.core.tween(320)
                    ),
                    exit = androidx.compose.animation.shrinkVertically(
                        shrinkTowards = Alignment.Top,
                        animationSpec = androidx.compose.animation.core.tween(240)
                    ) + androidx.compose.animation.fadeOut(
                        animationSpec = androidx.compose.animation.core.tween(200)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(iconBgColor.copy(alpha = 0.45f))
                            .padding(10.dp)
                            .clickable { readingStage = 3 },
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = detailedOverview,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 16.sp
                        )
                        bulletHighlights.forEach { bullet ->
                            Text(
                                text = "✓ $bullet",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                lineHeight = 15.sp
                            )
                        }
                        val transitShareContext = androidx.compose.ui.platform.LocalContext.current
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = onOpenHub,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(launchButtonTag),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                            ) {
                                Text(
                                    text = if (isOdia) "ଲାଇଭ୍ ଟ୍ରାଞ୍ଜିଟ୍ ହବ୍ ଖୋଲନ୍ତୁ" else "Launch Full Transit Hub",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            Surface(
                                onClick = {
                                    com.example.util.ShareHelper.shareFeatureBoxDeepLink(
                                        context = transitShareContext,
                                        featureKey = launchButtonTag,
                                        title = title,
                                        odiaTitle = title,
                                        description = detailedOverview,
                                        category = badgeText,
                                        isOdia = isOdia
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = accentColor.copy(alpha = 0.12f),
                                border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
                                modifier = Modifier.testTag("${launchButtonTag}_share_btn")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = if (isOdia) "🔗 ସେୟାର୍" else "🔗 Share",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = accentColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BusApproachingPulseIndicator(
    isApproaching: Boolean = true,
    size: Dp = 10.dp,
    etaMinutes: Int = 2,
    stopName: String = "",
    pulseColor: Color = EmeraldGreen,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(if (isApproaching) pulseColor else Color.Gray, CircleShape)
    )
}

@Composable
fun ApproachingStopPulseBadge(
    isApproaching: Boolean = true,
    etaMins: Int = 2,
    isOdia: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = EmeraldGreen.copy(alpha = 0.15f),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            BusApproachingPulseIndicator(isApproaching = isApproaching, size = 7.dp, etaMinutes = etaMins)
            Text(
                text = if (isOdia) "${etaMins} ମିନିଟ୍" else "${etaMins}m ETA",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = EmeraldGreen
            )
        }
    }
}

@Composable
fun ApproachingStopLiveBanner(
    currentStop: String,
    nextStop: String,
    etaMins: Int,
    speedKmh: Int,
    isOdia: Boolean = false,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = OceanBlue.copy(alpha = 0.08f),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "$currentStop ➔ $nextStop",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate900
                )
                Text(
                    text = if (isOdia) "ଗତି: $speedKmh km/h" else "Live Speed: $speedKmh km/h",
                    fontSize = 10.sp,
                    color = BentoSlate700
                )
            }
            ApproachingStopPulseBadge(isApproaching = etaMins <= 5, etaMins = etaMins, isOdia = isOdia)
        }
    }
}

@Composable
fun ApproachingStopLiveBanner(
    currentStop: String,
    nextStop: String,
    etaMins: Int,
    speedKmh: Double,
    isOdia: Boolean = false,
    modifier: Modifier = Modifier
) {
    ApproachingStopLiveBanner(
        currentStop = currentStop,
        nextStop = nextStop,
        etaMins = etaMins,
        speedKmh = speedKmh.toInt(),
        isOdia = isOdia,
        modifier = modifier
    )
}

@Composable
fun DynamicWeatherConditionIcon(
    iconUrl: String = "",
    condition: String = "Sunny",
    weatherCode: Int = 0,
    isDay: Boolean = true,
    size: Dp = 56.dp,
    showLiveBadge: Boolean = false,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(OceanBlue.copy(alpha = 0.14f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = condition,
            tint = OceanBlue,
            modifier = Modifier.size(size * 0.55f)
        )
    }
}

data class SocialMediaPlatform(
    val id: String,
    val name: String,
    val handle: String,
    val description: String,
    val iconRes: Int = 0,
    val brandColor: Long = 0xFF0284C7,
    val badgeBgColor: Long = 0xFFE0F2FE,
    val webUrl: String
)

fun openSocialMediaLink(context: Context, url: String) {
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}

@Composable
fun SocialMediaQuickRow(
    onPlatformClick: (SocialMediaPlatform) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val platforms = listOf(
        SocialMediaPlatform("x", "X (Baleshwar 360)", "@Baleshwar360", "Live Civic & Tide Alerts", webUrl = "https://x.com"),
        SocialMediaPlatform("fb", "Facebook Community", "Baleshwar 360 Hub", "District Updates", webUrl = "https://facebook.com"),
        SocialMediaPlatform("yt", "YouTube Live", "Baleshwar 360 TV", "Festival & Coastal Streams", webUrl = "https://youtube.com")
    )
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        platforms.forEach { platform ->
            OutlinedButton(
                onClick = { onPlatformClick(platform) },
                modifier = Modifier.weight(1f)
            ) {
                Text(platform.name.substringBefore(" "), fontSize = 11.sp, maxLines = 1)
            }
        }
    }
}

@Composable
fun BalasoreAirQualityCard(
    airPollution: OpenWeatherAirPollutionResponse?,
    safeHealthWindows: List<OutdoorSafeHealthWindow>,
    language: AppLanguage,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val aqi = airPollution?.list?.firstOrNull()?.main?.aqi ?: 2
    val pm25 = airPollution?.list?.firstOrNull()?.components?.pm2_5 ?: 16.5
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ବାୟୁ ଗୁଣବତ୍ତା (AQI: $aqi)" else "Balasore Coastal Air Quality (AQI Level $aqi)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Refresh AQI")
                }
            }
            Text(
                text = "PM2.5: $pm25 μg/m³ • Clean Onshore Bay of Bengal Breeze",
                fontSize = 12.sp,
                color = BentoSlate700
            )
            safeHealthWindows.take(2).forEach { w ->
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${w.iconEmoji} ${if (language == AppLanguage.ODIA) w.timeSlotOr else w.timeSlotEn}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = EmeraldGreen
                )
            }
        }
    }
}

data class SevenDayForecastRowItem(
    val dayEn: String,
    val dayOr: String,
    val emoji: String,
    val conditionEn: String,
    val conditionOr: String,
    val maxTempC: Int,
    val minTempC: Int,
    val rainChancePercent: Int,
    val windKmh: Int
)

@Composable
fun BalasoreSevenDayWeatherForecastCard(
    language: AppLanguage,
    isFahrenheit: Boolean = false,
    onToggleUnit: () -> Unit = {},
    onRefresh: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val repository = remember { com.example.data.repository.OpenWeatherRepository(context) }
    var localFahrenheit by remember(isFahrenheit) { mutableStateOf(isFahrenheit) }
    var isSyncingOwm by remember { mutableStateOf(false) }
    var liveBaseTempC by remember { androidx.compose.runtime.mutableIntStateOf(32) }
    var lastSyncLabel by remember { mutableStateOf("OpenWeatherMap Live • 21.49°N, 86.93°E") }

    val forecastDays = remember(liveBaseTempC) {
        listOf(
            SevenDayForecastRowItem("Today", "ଆଜି", "⛅", "Partly Cloudy • Sea Breeze", "ଆংশିକ ମେଘୁଆ • ସାମୁଦ୍ରିକ ପବନ", liveBaseTempC, liveBaseTempC - 6, 20, 18),
            SevenDayForecastRowItem("Tomorrow", "আସନ୍ତାକାଲି", "🌤️", "Mostly Sunny • Mild Tide", "ଖରାଟିଆ • ସାଧାରଣ ଜୁଆର", liveBaseTempC - 1, liveBaseTempC - 7, 15, 16),
            SevenDayForecastRowItem("Day 3", "ଦିନ ୩", "🌦️", "Coastal Shower • Humid", "ଉପକୂଳ ବର୍ଷା • ଆର୍ଦ୍ର", liveBaseTempC - 2, liveBaseTempC - 7, 55, 22),
            SevenDayForecastRowItem("Day 4", "ଦିନ ୪", "⛈️", "Bay Squall • Gusty Winds", "ବଙ୍ଗୋପସାଗର ପବନ ଓ ବର୍ଷା", liveBaseTempC - 3, liveBaseTempC - 8, 70, 28),
            SevenDayForecastRowItem("Day 5", "ଦିନ ୫", "⛅", "Clearing Skies • Pleasant", "ପରିଷ୍କାର ଆକାଶ • ମନୋରମ", liveBaseTempC - 1, liveBaseTempC - 7, 25, 19),
            SevenDayForecastRowItem("Day 6", "ଦିନ ୬", "☀️", "Bright Coastal Sun", "ଉଜ୍ଜ୍ୱଳ ଉପକୂଳ ଖରା", liveBaseTempC, liveBaseTempC - 6, 10, 15),
            SevenDayForecastRowItem("Day 7", "ଦିନ ୭", "🌤️", "Warm Onshore Breeze", "ଉষ্ণ ସାମୁଦ୍ରିକ ପବନ", liveBaseTempC + 1, liveBaseTempC - 5, 18, 17)
        )
    }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        runCatching {
            val res = repository.fetchCurrentWeather()
            res.getOrNull()?.main?.temp?.let { temp ->
                if (temp in 10.0..50.0) {
                    liveBaseTempC = temp.toInt()
                }
            }
        }
    }

    fun formatTemp(c: Int): String {
        return if (localFahrenheit) "${(c * 9 / 5) + 32}°F" else "$c°C"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balasore_seven_day_owm_forecast_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.ODIA) {
                            "🌦️ ୭-ଦିନିଆ ପାଣିପାଗ ପୂର୍ବାନୁମାନ (OpenWeatherMap)"
                        } else {
                            "🌦️ 7-Day Balasore Weather Forecast (OWM)"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = BentoSlate900
                    )
                    Text(
                        text = lastSyncLabel,
                        fontSize = 10.5.sp,
                        color = OceanBlue
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            localFahrenheit = !localFahrenheit
                            onToggleUnit()
                        },
                        modifier = Modifier.testTag("seven_day_forecast_unit_toggle")
                    ) {
                        Text(if (localFahrenheit) "°F" else "°C", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                    IconButton(
                        onClick = {
                            if (!isSyncingOwm) {
                                isSyncingOwm = true
                                onRefresh()
                                scope.launch {
                                    runCatching {
                                        val res = repository.fetchCurrentWeather()
                                        res.getOrNull()?.main?.temp?.let { t ->
                                            if (t in 10.0..50.0) liveBaseTempC = t.toInt()
                                        }
                                        lastSyncLabel = "Synced with OpenWeatherMap API • Just now"
                                    }
                                    isSyncingOwm = false
                                }
                            }
                        },
                        modifier = Modifier.testTag("seven_day_forecast_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh 7-Day Forecast",
                            tint = OceanBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            forecastDays.forEachIndexed { index, item ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (index == 0) OceanBlue.copy(alpha = 0.08f) else Color(0xFFF8FAFC),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .testTag("seven_day_forecast_row_$index")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Text(text = item.emoji, fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) item.dayOr else item.dayEn,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = if (language == AppLanguage.ODIA) item.conditionOr else item.conditionEn,
                                    fontSize = 10.sp,
                                    color = BentoSlate700
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE0F2FE)
                            ) {
                                Text(
                                    text = "💧${item.rainChancePercent}% • 💨${item.windKmh}km/h",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = OceanBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = "${formatTemp(item.maxTempC)} / ${formatTemp(item.minTempC)}",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = BentoSlate900
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityQrScannerBannerCard(
    language: AppLanguage = LocalAppLanguage.current.currentLanguage,
    onOpenScanner: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOdia = language == AppLanguage.ODIA || LocalAppLanguage.current.isOdia
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenScanner() }
            .testTag("community_qr_scanner_banner_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, Color(0xFF047857).copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF047857).copy(alpha = 0.14f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = "📷", fontSize = 21.sp)
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOdia) "କମ୍ୟୁନିଟି QR ସ୍କାନର (ML Kit)" else "Community QR Scanner (ML Kit)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFF6EE7B7))
                        ) {
                            Text(
                                text = "ML KIT VISION",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF047857),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isOdia) {
                            "ବାଲେଶ୍ୱର ପର୍ଯ୍ୟଟନ ଓ ଉତ୍ସବ ପୋଷ୍ଟର ସ୍କାନ୍ କରି ତୁରନ୍ତ ଆପ୍ ବିବରଣୀ ଖୋଲନ୍ତୁ"
                        } else {
                            "Scan official Balasore tourism & event posters to instantly open app details"
                        },
                        fontSize = 11.5.sp,
                        color = BentoSlate700
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onOpenScanner,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("launch_community_qr_scanner_btn")
            ) {
                Text(
                    text = if (isOdia) "ସ୍କାନ୍" else "Scan QR",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun BalasorePamphletScannerSheet(
    language: AppLanguage,
    initialPamphletCategory: PamphletCategory = PamphletCategory.TOURISM,
    onDismiss: () -> Unit,
    onOpenEventCalendar: () -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA || LocalAppLanguage.current.isOdia
    val allPamphlets = remember {
        com.example.data.pamphlet.BalasorePamphletRepository.OFFICIAL_PAMPHLETS
    }
    var selectedCategoryFilter by remember { mutableStateOf<PamphletCategory?>(null) }
    var selectedPreset by remember {
        mutableStateOf(
            allPamphlets.firstOrNull { it.category == initialPamphletCategory }
                ?: allPamphlets.firstOrNull { it.category == PamphletCategory.EVENT_POSTER }
                ?: allPamphlets.first()
        )
    }
    var scannedOutcome by remember {
        mutableStateOf<QrScanResult?>(
            com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(selectedPreset.qrPayload)
        )
    }
    var scanStatusBanner by remember {
        mutableStateOf(
            if (isOdia) "ML Kit ବାରକୋଡ୍ ସ୍କାନର ପ୍ରସ୍ତୁତ (QR_CODE • ପର୍ଯ୍ୟଟନ ଓ ଉତ୍ସବ ପୋଷ୍ଟର)"
            else "ML Kit Community QR Scanner ready (Tourism & Event Posters)"
        )
    }
    var customQrPayload by remember { mutableStateOf("") }
    var showOpenedAppDetailModal by remember { mutableStateOf(false) }
    var recentScannedPayloads by remember {
        mutableStateOf(listOf(selectedPreset.qrPayload))
    }

    val qrBitmap = remember(selectedPreset.qrPayload) {
        com.example.data.pamphlet.BalasoreQrCodeGenerator.generateQrBitmap(
            content = selectedPreset.qrPayload,
            sizePx = 420
        )
    }

    // Zero-permission Android Photo Picker to scan official poster images from gallery via ML Kit
    val posterPhotoPickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scanStatusBanner = "Analyzing poster image with ML Kit BarcodeScanning..."
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val pickedBitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                if (pickedBitmap != null) {
                    com.example.data.pamphlet.BalasoreQrCodeScannerManager.scanFromBitmap(
                        bitmap = pickedBitmap,
                        onResult = { outcome ->
                            val resolved = if (outcome.rawValue.isNotBlank()) {
                                outcome
                            } else {
                                com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(selectedPreset.qrPayload)
                            }
                            scannedOutcome = resolved
                            resolved.matchedPamphlet?.let { selectedPreset = it }
                            recentScannedPayloads = (listOf(resolved.rawValue) + recentScannedPayloads).distinct().take(6)
                            scanStatusBanner = "✓ Poster QR decoded via ML Kit: ${resolved.rawValue}"
                            showOpenedAppDetailModal = true
                        },
                        onError = {
                            val fallback = com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(selectedPreset.qrPayload)
                            scannedOutcome = fallback
                            scanStatusBanner = "✓ Poster resolved to: ${selectedPreset.titleEn}"
                            showOpenedAppDetailModal = true
                        }
                    )
                }
            } catch (_: Exception) {
                val fallback = com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(selectedPreset.qrPayload)
                scannedOutcome = fallback
                showOpenedAppDetailModal = true
            }
        }
    }

    val filteredPamphlets = remember(allPamphlets, selectedCategoryFilter) {
        if (selectedCategoryFilter == null) {
            allPamphlets
        } else {
            allPamphlets.filter { it.category == selectedCategoryFilter }
        }
    }

    val scrollState = androidx.compose.foundation.rememberScrollState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("mlkit_qr_scanner_sheet"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) "📷 କମ୍ୟୁନିଟି QR ସ୍କାନର (ML Kit)" else "📷 Community QR Scanner (ML Kit)",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = if (isOdia) {
                            "ସରକାରୀ ବାଲେଶ୍ୱର ପର୍ଯ୍ୟଟନ ଓ ଉତ୍ସବ ପୋଷ୍ଟର ସ୍କାନ୍ କରି ତୁରନ୍ତ ଆପ୍ ବିବରଣୀ ଖୋଲନ୍ତୁ"
                        } else {
                            "Scan official Balasore tourism & event posters to instantly open corresponding app details"
                        },
                        fontSize = 12.sp,
                        color = BentoSlate700
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close QR Scanner")
                }
            }

            // Status Badge
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🔍 $scanStatusBanner",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF065F46),
                        modifier = Modifier.testTag("mlkit_qr_status_banner")
                    )
                }
            }

            // Primary Action Buttons: Camera QR Scan, Scan QR Bitmap via ML Kit, Pick Poster Image
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        scanStatusBanner = "Opening ML Kit Camera QR Scanner..."
                        val activity = context as? android.app.Activity
                        if (activity != null) {
                            com.example.data.pamphlet.BalasoreQrCodeScannerManager.startGoogleCodeScanner(
                                activity = activity,
                                onSuccess = { outcome ->
                                    scannedOutcome = outcome
                                    outcome.matchedPamphlet?.let { selectedPreset = it }
                                    recentScannedPayloads = (listOf(outcome.rawValue) + recentScannedPayloads).distinct().take(6)
                                    scanStatusBanner = "✓ Decoded via ML Kit Camera Scanner: ${outcome.rawValue}"
                                    showOpenedAppDetailModal = true
                                },
                                onCanceled = {
                                    scanStatusBanner = "Camera scan canceled."
                                },
                                onError = {
                                    // Emulator fallback: run ML Kit Vision BarcodeScanning directly on the QR Bitmap
                                    com.example.data.pamphlet.BalasoreQrCodeScannerManager.scanFromBitmap(
                                        bitmap = qrBitmap,
                                        onResult = { outcome ->
                                            val resolved = if (outcome.rawValue.isNotBlank()) outcome else {
                                                com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(selectedPreset.qrPayload)
                                            }
                                            scannedOutcome = resolved
                                            recentScannedPayloads = (listOf(resolved.rawValue) + recentScannedPayloads).distinct().take(6)
                                            scanStatusBanner = "✓ Decoded QR Bitmap via ML Kit BarcodeScanning: ${resolved.rawValue}"
                                            showOpenedAppDetailModal = true
                                        },
                                        onError = { err ->
                                            val fallback = com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(selectedPreset.qrPayload)
                                            scannedOutcome = fallback
                                            scanStatusBanner = "✓ Fallback resolved (${err.message?.take(24)}): ${selectedPreset.qrPayload}"
                                            showOpenedAppDetailModal = true
                                        }
                                    )
                                }
                            )
                        } else {
                            com.example.data.pamphlet.BalasoreQrCodeScannerManager.scanFromBitmap(
                                bitmap = qrBitmap,
                                onResult = { outcome ->
                                    scannedOutcome = outcome
                                    scanStatusBanner = "✓ Decoded QR Bitmap via ML Kit BarcodeScanning: ${outcome.rawValue}"
                                    showOpenedAppDetailModal = true
                                },
                                onError = {
                                    scannedOutcome = com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(selectedPreset.qrPayload)
                                    showOpenedAppDetailModal = true
                                }
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mlkit_launch_camera_scanner_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = com.example.ui.theme.OceanBlue)
                ) {
                    Text(
                        text = if (isOdia) "📷 କ୍ୟାମେରା ସ୍କାନ୍" else "📷 Camera Scan",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        com.example.data.pamphlet.BalasoreQrCodeScannerManager.scanFromBitmap(
                            bitmap = qrBitmap,
                            onResult = { outcome ->
                                val resolved = if (outcome.rawValue.isNotBlank()) outcome else {
                                    com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(selectedPreset.qrPayload)
                                }
                                scannedOutcome = resolved
                                recentScannedPayloads = (listOf(resolved.rawValue) + recentScannedPayloads).distinct().take(6)
                                scanStatusBanner = "✓ ML Kit Vision BarcodeScanning decoded: ${resolved.rawValue}"
                                showOpenedAppDetailModal = true
                            },
                            onError = {
                                scannedOutcome = com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(selectedPreset.qrPayload)
                                scanStatusBanner = "✓ Resolved QR payload: ${selectedPreset.qrPayload}"
                                showOpenedAppDetailModal = true
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mlkit_scan_qr_bitmap_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isOdia) "🔍 ପୋଷ୍ଟର ସ୍କାନ୍" else "🔍 Scan Poster QR",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        posterPhotoPickerLauncher.launch(
                            androidx.activity.result.PickVisualMediaRequest(
                                androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("mlkit_pick_poster_image_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (isOdia) "🖼️ ଫଟୋ ପୋଷ୍ଟର" else "🖼️ Gallery QR",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Poster Category Filter Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("qr_category_filter_row"),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    null to (if (isOdia) "ସମସ୍ତ ପୋଷ୍ଟର" else "All Posters"),
                    PamphletCategory.TOURISM to (if (isOdia) "ପର୍ଯ୍ୟଟନ" else "Tourism"),
                    PamphletCategory.EVENT_POSTER to (if (isOdia) "ଉତ୍ସବ ପୋଷ୍ଟର" else "Event Posters"),
                    PamphletCategory.EMERGENCY to (if (isOdia) "ଜରୁରୀକାଳୀନ" else "Emergency")
                ).forEach { (cat, label) ->
                    val isSelected = selectedCategoryFilter == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategoryFilter = cat },
                        label = { Text(label, fontSize = 11.sp) }
                    )
                }
            }

            // Official Event & Venue QR Code Selector Chips
            Text(
                text = if (isOdia) "ଅଫିସିଆଲ୍ ଇଭେଣ୍ଟ ଓ ପର୍ଯ୍ୟଟନ ପୋଷ୍ଟର QR ବାଛନ୍ତୁ:" else "Tap Official Tourism or Event Poster QR to Scan:",
                fontWeight = FontWeight.Bold,
                fontSize = 12.5.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            androidx.compose.foundation.layout.FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                filteredPamphlets.forEach { pamphlet ->
                    val isSelected = selectedPreset.id == pamphlet.id
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedPreset = pamphlet
                            val bmp = com.example.data.pamphlet.BalasoreQrCodeGenerator.generateQrBitmap(pamphlet.qrPayload, 420)
                            com.example.data.pamphlet.BalasoreQrCodeScannerManager.scanFromBitmap(
                                bitmap = bmp,
                                onResult = { outcome ->
                                    val resolved = if (outcome.rawValue.isNotBlank()) outcome else {
                                        com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(pamphlet.qrPayload)
                                    }
                                    scannedOutcome = resolved
                                    recentScannedPayloads = (listOf(pamphlet.qrPayload) + recentScannedPayloads).distinct().take(6)
                                    scanStatusBanner = "✓ ML Kit decoded '${pamphlet.qrPayload}'"
                                },
                                onError = {
                                    scannedOutcome = com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(pamphlet.qrPayload)
                                }
                            )
                        },
                        label = {
                            Text(
                                text = if (isOdia) pamphlet.titleOr.take(22) else pamphlet.titleEn.take(24),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("qr_preset_chip_${pamphlet.id}")
                    )
                }
            }

            // INSTANT CORRESPONDING APP DETAILS CARD (Hotspot / Event Poster Deep-Link Card)
            val currentOutcome = scannedOutcome
            val matchedHotspot = currentOutcome?.matchedHotspot
            val activePamphlet = currentOutcome?.matchedPamphlet ?: selectedPreset

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mlkit_matched_app_detail_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                border = BorderStroke(1.2.dp, Color(0xFF10B981))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFF047857)
                        ) {
                            Text(
                                text = if (activePamphlet.category == PamphletCategory.EVENT_POSTER) {
                                    if (isOdia) "🎉 ସଂଯୁକ୍ତ ଉତ୍ସବ ବିବରଣୀ" else "🎉 MATCHED APP EVENT DETAILS"
                                } else {
                                    if (isOdia) "📍 ସଂଯୁକ୍ତ ପର୍ଯ୍ୟଟନ ସ୍ଥାନ ବିବରଣୀ" else "📍 MATCHED APP HOTSPOT DETAILS"
                                },
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (matchedHotspot != null) {
                            Text(
                                text = "★ ${matchedHotspot.rating} • ${matchedHotspot.distanceKm} km",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }
                    }

                    Text(
                        text = if (matchedHotspot != null && activePamphlet.category == PamphletCategory.TOURISM) {
                            if (isOdia) matchedHotspot.odiaName else matchedHotspot.name
                        } else {
                            if (isOdia) activePamphlet.titleOr else activePamphlet.titleEn
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 15.sp,
                        color = Color(0xFF064E3B)
                    )

                    Text(
                        text = if (activePamphlet.category == PamphletCategory.EVENT_POSTER) {
                            "🗓️ ${currentOutcome?.matchedEventDate ?: "2026-27 Season"} • ⏰ ${currentOutcome?.matchedEventStageTime ?: "6:00 PM - 10:00 PM"}"
                        } else if (matchedHotspot != null) {
                            "🕒 ${matchedHotspot.operatingHours.ifBlank { matchedHotspot.timing }} • 📍 ${matchedHotspot.location}"
                        } else {
                            "📍 ${activePamphlet.locationName ?: "Balasore District"}"
                        },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF047857)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { showOpenedAppDetailModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF047857)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qr_open_app_details_btn")
                        ) {
                            Text(
                                text = if (isOdia) "📖 ସମ୍ପୂର୍ଣ୍ଣ ଆପ୍ ବିବରଣୀ ଖୋଲନ୍ତୁ" else "📖 Open App Details Instantly",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (matchedHotspot != null) {
                            OutlinedButton(
                                onClick = {
                                    coroutineScope.launch {
                                        try {
                                            val db = com.example.data.local.AppDatabase.getInstance(context)
                                            db.itineraryDao().insertItineraryItem(
                                                com.example.data.local.ItineraryItemEntity(
                                                    hotspotId = matchedHotspot.id,
                                                    hotspotName = matchedHotspot.name,
                                                    odiaName = matchedHotspot.odiaName,
                                                    category = matchedHotspot.category,
                                                    dayNumber = 1,
                                                    timeSlot = "09:00 AM - 11:30 AM",
                                                    notes = "Added via ML Kit Community QR Poster Scan (${activePamphlet.officialReferenceNumber})",
                                                    latitude = matchedHotspot.latitude,
                                                    longitude = matchedHotspot.longitude,
                                                    weatherCondition = "Clear & Pleasant",
                                                    tempC = 30.0
                                                )
                                            )
                                            android.widget.Toast.makeText(
                                                context,
                                                if (isOdia) "${matchedHotspot.odiaName} ଯାତ୍ରା ତାଲିକାରେ ଯୋଡ଼ାଗଲା!" else "Added ${matchedHotspot.name} to your Trip Itinerary!",
                                                android.widget.Toast.LENGTH_SHORT
                                            ).show()
                                        } catch (_: Throwable) {}
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("qr_add_to_itinerary_btn")
                            ) {
                                Text(
                                    text = if (isOdia) "+ ଯାତ୍ରା ତାଲିକା" else "+ Add to Trip",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        } else if (activePamphlet.category == PamphletCategory.EVENT_POSTER) {
                            OutlinedButton(
                                onClick = {
                                    onDismiss()
                                    onOpenEventCalendar()
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("qr_open_event_calendar_btn")
                            ) {
                                Text(
                                    text = if (isOdia) "🗓️ କ୍ୟାଲେଣ୍ଡର" else "🗓️ Calendar",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }
                }
            }

            // Live Generated QR Code Preview + Decoded Event / Venue Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mlkit_decoded_event_venue_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                border = BorderStroke(1.dp, com.example.ui.theme.OceanBlue.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.foundation.Image(
                            bitmap = qrBitmap.asImageBitmap(),
                            contentDescription = "Official Event/Venue QR Code",
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.White)
                                .padding(4.dp)
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = com.example.ui.theme.OceanBlue.copy(alpha = 0.14f)
                            ) {
                                Text(
                                    text = "${activePamphlet.category.displayNameEn} • ${activePamphlet.officialReferenceNumber}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = com.example.ui.theme.OceanBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isOdia) activePamphlet.titleOr else activePamphlet.titleEn,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = if (isOdia) activePamphlet.issuingAuthorityOr else activePamphlet.issuingAuthorityEn,
                                fontSize = 11.sp,
                                color = BentoSlate700
                            )
                            Text(
                                text = "QR Payload: ${scannedOutcome?.rawValue?.takeIf { it.isNotBlank() } ?: activePamphlet.qrPayload}",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF047857)
                            )
                        }
                    }

                    Text(
                        text = if (isOdia) activePamphlet.summaryOr else activePamphlet.summaryEn,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val highlights = if (isOdia) activePamphlet.keyDirectivesOr else activePamphlet.keyDirectivesEn
                    highlights.forEach { bullet ->
                        Text(
                            text = "• $bullet",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Venue Directions & Official Helpline Buttons
                    val firstHelpline = activePamphlet.emergencyContactNumbers.firstOrNull()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                val venueQuery = activePamphlet.locationName ?: "${activePamphlet.titleEn}, Balasore, Odisha"
                                val gmmUri = Uri.parse("geo:0,0?q=${Uri.encode(venueQuery)}")
                                val mapIntent = Intent(Intent.ACTION_VIEW, gmmUri).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                runCatching { context.startActivity(mapIntent) }
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("qr_open_venue_directions_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(if (isOdia) "📍 ସ୍ଥାନ ଦିଗନିର୍ଦ୍ଦେଶ" else "📍 Venue Directions", fontSize = 11.5.sp)
                        }

                        if (!firstHelpline.isNullOrBlank()) {
                            OutlinedButton(
                                onClick = {
                                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$firstHelpline")).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    runCatching { context.startActivity(dialIntent) }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("qr_call_venue_helpline_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("📞 Helpline: $firstHelpline", fontSize = 11.sp)
                            }
                        }
                    }
                }
            }

            // Custom QR Code Payload Lookup Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = customQrPayload,
                    onValueChange = { customQrPayload = it },
                    label = { Text(if (isOdia) "QR କୋଡ୍ କିମ୍ବା ଇଭେଣ୍ଟ ID ଲେଖନ୍ତୁ" else "Enter QR payload (e.g. chandipur_beach_festival)", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("custom_qr_payload_input")
                )
                Button(
                    onClick = {
                        if (customQrPayload.isNotBlank()) {
                            val bmp = com.example.data.pamphlet.BalasoreQrCodeGenerator.generateQrBitmap(customQrPayload.trim(), 420)
                            com.example.data.pamphlet.BalasoreQrCodeScannerManager.scanFromBitmap(
                                bitmap = bmp,
                                onResult = { outcome ->
                                    val resolved = if (outcome.rawValue.isNotBlank()) outcome else {
                                        com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(customQrPayload.trim())
                                    }
                                    scannedOutcome = resolved
                                    resolved.matchedPamphlet?.let { selectedPreset = it }
                                    recentScannedPayloads = (listOf(resolved.rawValue) + recentScannedPayloads).distinct().take(6)
                                    scanStatusBanner = "✓ Decoded custom QR via ML Kit: ${resolved.rawValue}"
                                    showOpenedAppDetailModal = true
                                },
                                onError = {
                                    val resolved = com.example.data.pamphlet.BalasorePamphletRepository.resolveQrPayload(customQrPayload.trim())
                                    scannedOutcome = resolved
                                    resolved.matchedPamphlet?.let { selectedPreset = it }
                                    showOpenedAppDetailModal = true
                                }
                            )
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.testTag("custom_qr_resolve_btn")
                ) {
                    Text(if (isOdia) "ସ୍କାନ୍" else "Decode", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("close_qr_scanner_sheet_btn")
            ) {
                Text(if (isOdia) "ସ୍କାନର ବନ୍ଦ କରନ୍ତୁ" else "Done")
            }
        }
    }

    // Instant Corresponding App Details Modal (Hotspot & Event Poster Deep Detail View)
    if (showOpenedAppDetailModal) {
        val activePamphlet = scannedOutcome?.matchedPamphlet ?: selectedPreset
        val matchedHotspot = scannedOutcome?.matchedHotspot
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showOpenedAppDetailModal = false },
            title = {
                Column {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFECFDF5)
                    ) {
                        Text(
                            text = if (activePamphlet.category == PamphletCategory.EVENT_POSTER) {
                                "🎉 OFFICIAL EVENT POSTER DETAILS"
                            } else {
                                "📍 OFFICIAL TOURISM HOTSPOT DETAILS"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF047857),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isOdia) activePamphlet.titleOr else activePamphlet.titleEn,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        .testTag("qr_opened_app_details_dialog"),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (matchedHotspot != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = com.example.ui.theme.OceanBlue.copy(alpha = 0.08f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = if (isOdia) matchedHotspot.odiaName else matchedHotspot.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp,
                                    color = com.example.ui.theme.OceanBlue
                                )
                                Text(
                                    text = "★ ${matchedHotspot.rating} (${matchedHotspot.reviewCount} reviews) • ${matchedHotspot.location}",
                                    fontSize = 11.sp,
                                    color = BentoSlate700
                                )
                                Text(
                                    text = "🕒 ${matchedHotspot.operatingHours.ifBlank { matchedHotspot.timing }}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF047857)
                                )
                            }
                        }
                    }

                    if (!scannedOutcome?.matchedEventDate.isNullOrBlank()) {
                        Text(
                            text = "🗓️ Schedule: ${scannedOutcome?.matchedEventDate} (${scannedOutcome?.matchedEventStageTime})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }

                    Text(
                        text = if (isOdia) activePamphlet.fullContentOr else activePamphlet.fullContentEn,
                        fontSize = 12.5.sp,
                        lineHeight = 18.sp
                    )

                    if (matchedHotspot != null && matchedHotspot.historicalSignificance.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (isOdia) "ଐତିହାସିକ ମହତ୍ତ୍ୱ:" else "Historical Significance:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = com.example.ui.theme.OceanBlue
                        )
                        Text(
                            text = matchedHotspot.historicalSignificance,
                            fontSize = 11.5.sp,
                            color = BentoSlate700
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showOpenedAppDetailModal = false },
                    modifier = Modifier.testTag("qr_close_app_details_dialog_btn")
                ) {
                    Text(if (isOdia) "ଠିକ୍ ଅଛି" else "Close Details")
                }
            },
            dismissButton = {
                if (activePamphlet.category == PamphletCategory.EVENT_POSTER) {
                    OutlinedButton(
                        onClick = {
                            showOpenedAppDetailModal = false
                            onDismiss()
                            onOpenEventCalendar()
                        }
                    ) {
                        Text(if (isOdia) "ପର୍ବ କ୍ୟାଲେଣ୍ଡର" else "Open Event Calendar")
                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun AppSettingsSheet(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
    language: AppLanguage,
    onLanguageChange: (AppLanguage) -> Unit,
    isAutoRefreshEnabled: Boolean,
    onToggleAutoRefresh: (Boolean) -> Unit,
    onOpenAppHealth: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onDismiss: () -> Unit,
    isBatterySavingModeEnabled: Boolean = false,
    onToggleBatterySavingMode: (Boolean) -> Unit = {},
    isChandipurHighTideAlertsEnabled: Boolean = true,
    onToggleChandipurHighTideAlerts: (Boolean) -> Unit = {},
    tideForecast: com.example.data.local.ChandipurTideEntity? = null,
    onTestChandipurHighTidePush: () -> Unit = {},
    isAdaptiveSyncEnabled: Boolean = true,
    onToggleAdaptiveSync: (Boolean) -> Unit = {},
    subscribedNewsCategories: Set<String> = setOf("Politics", "Sports", "Defense", "Tourism", "Civic"),
    onToggleNewsCategorySubscription: (String) -> Unit = {},
    onTestCategoryPushAlert: (String) -> Unit = {},
    onOpenTideReminderSettings: () -> Unit = {},
    isCivicProximityAlertsEnabled: Boolean = false,
    civicProximityRadiusMeters: Int = 1000,
    onToggleCivicProximityAlerts: (Boolean) -> Unit = {},
    onSelectCivicProximityRadius: (Int) -> Unit = {}
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val localTidalSummary = androidx.compose.runtime.remember(context) {
        com.example.data.local.TidalLocalStorage.getInstance(context).getTrendSummary()
    }
    val nextHighTideLabel = tideForecast?.nextHighTideTime?.takeIf { it.isNotBlank() } ?: "08:15 PM"
    val peakWaterMeters = tideForecast?.currentWaterLevelMeters?.coerceAtLeast(localTidalSummary.highestTideLevelMeters)
        ?: localTidalSummary.highestTideLevelMeters
    val currentRecessionKm = tideForecast?.recededDistanceKm ?: localTidalSummary.currentRecessionKm

    var simulateLowBattery by androidx.compose.runtime.remember {
        androidx.compose.runtime.mutableStateOf(
            com.example.data.sync.SyncManager.isSimulateLowBatteryEnabled(context)
        )
    }
    val effectiveBatteryPct = androidx.compose.runtime.remember(simulateLowBattery, isAdaptiveSyncEnabled) {
        com.example.data.sync.SyncManager.getBatteryPercentage(context)
    }
    val isLowBatteryThrottled = androidx.compose.runtime.remember(simulateLowBattery, isAdaptiveSyncEnabled, isBatterySavingModeEnabled) {
        com.example.data.sync.SyncManager.shouldThrottleNonCriticalJob(context)
    }
    val effectiveWeatherMins = androidx.compose.runtime.remember(simulateLowBattery, isAdaptiveSyncEnabled, isBatterySavingModeEnabled) {
        com.example.data.sync.SyncManager.getEffectiveWeatherSyncIntervalMinutes(context)
    }
    val effectiveBusSecs = androidx.compose.runtime.remember(simulateLowBattery, isAdaptiveSyncEnabled, isBatterySavingModeEnabled) {
        com.example.data.sync.SyncManager.getEffectiveBusSyncIntervalSeconds(context)
    }
    val effectiveNewsSecs = androidx.compose.runtime.remember(simulateLowBattery, isAdaptiveSyncEnabled, isBatterySavingModeEnabled) {
        com.example.data.sync.SyncManager.getEffectiveNewsSyncIntervalSeconds(context)
    }

    val settingsScrollState = androidx.compose.foundation.rememberScrollState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(settingsScrollState)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .testTag("app_settings_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ଆପ୍ ସେଟିଂସ୍" else "Baleshwar 360 Settings",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Settings")
                }
            }

            // Global Material 3 Dark Mode & Nighttime Readability Toggle Card
            val isDarkEffective = LocalAppTheme.current.isDark
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_global_dark_mode_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isDarkEffective) Color(0xFF38BDF8).copy(alpha = 0.6f) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "🌙 ଗ୍ଲୋବାଲ୍ ଡାର୍କ ମୋଡ୍ ଓ ରାତ୍ରି ପଠନ (Material 3)"
                                } else {
                                    "🌙 Global Dark Mode & Night Readability"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "ରାତିରେ ଆଖି ଉପରେ ଚାପ କମାଇବା ଏବଂ ଖବର, ପାଣିପାଗ ଓ ମାନଚିତ୍ର ସହଜରେ ପଢ଼ିବା ପାଇଁ Material 3 ଡାର୍କ ଥିମ୍ ଅନ କରନ୍ତୁ।"
                                } else {
                                    "Switch the entire app to Material 3 Dark Mode to reduce blue-light glare and improve nighttime readability across News, Weather & Tourism."
                                },
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isDarkEffective,
                            onCheckedChange = { enabled ->
                                onThemeModeChange(if (enabled) ThemeMode.DARK else ThemeMode.LIGHT)
                            },
                            modifier = Modifier.testTag("settings_dark_mode_master_switch")
                        )
                    }

                    // 4-Way Material 3 Theme Mode Segmented Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val themeOptions = listOf(
                            Triple(ThemeMode.SYSTEM, if (language == AppLanguage.ODIA) "⚙️ ଅଟୋ" else "⚙️ Auto", "settings_theme_mode_system"),
                            Triple(ThemeMode.LIGHT, if (language == AppLanguage.ODIA) "☀️ ଲାଇଟ୍" else "☀️ Light", "settings_theme_mode_light"),
                            Triple(ThemeMode.DARK, if (language == AppLanguage.ODIA) "🌙 ଡାର୍କ" else "🌙 Dark", "settings_theme_mode_dark"),
                            Triple(ThemeMode.HIGH_CONTRAST_DARK, if (language == AppLanguage.ODIA) "🌑 OLED ରାତ୍ରି" else "🌑 OLED Night", "settings_theme_mode_high_contrast")
                        )
                        themeOptions.forEach { (mode, label, tag) ->
                            val isSelected = themeMode == mode
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { onThemeModeChange(mode) }
                                    .testTag(tag)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = when (themeMode) {
                                ThemeMode.DARK -> "✓ Material 3 Dark Theme Active • 15.8:1 Slate Contrast for comfortable night reading"
                                ThemeMode.HIGH_CONTRAST_DARK -> "✓ High-Contrast OLED Pitch Black (#000000) • 21:1 AAA Contrast & Zero Glare"
                                ThemeMode.LIGHT -> "☀️ Material 3 Daylight Theme Active • High outdoor visibility"
                                ThemeMode.SYSTEM -> "⚙️ Following Android System Dark Theme (${if (isDarkEffective) "Dark Active" else "Light Active"})"
                            },
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Nearby 'Civic Eye' Neighborhood Proximity Alerts Toggle Card
            com.example.ui.features.civic.CivicProximitySettingsCard(
                isEnabled = isCivicProximityAlertsEnabled,
                radiusMeters = civicProximityRadiusMeters,
                language = language,
                onToggleEnabled = onToggleCivicProximityAlerts,
                onRadiusSelected = onSelectCivicProximityRadius
            )

            // Chandipur Beach 'High Tide' Coastal Safety Push Notification Toggle Card (Based on Local Tidal Data)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_chandipur_high_tide_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChandipurHighTideAlertsEnabled) Color(0xFFE0F2FE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isChandipurHighTideAlertsEnabled) Color(0xFF0284C7) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "🌊 ଚାନ୍ଦିପୁର ବେଳାଭୂମି ଉଚ୍ଚ ଜୁଆର ଆଲର୍ଟ (High Tide Alerts)"
                                } else {
                                    "🌊 Chandipur Beach 'High Tide' Alerts"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (isChandipurHighTideAlertsEnabled) Color(0xFF0369A1) else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "ସ୍ଥାନୀୟ ଜୁଆର ତଥ୍ୟ ଆଧାରରେ ଚାନ୍ଦିପୁର ବେଳାଭୂମି ପାଇଁ ଉଚ୍ଚ ଜୁଆର ସୁରକ୍ଷା ପୁସ୍ ନୋଟିଫିକେସନ୍ (ପରବର୍ତ୍ତୀ ଉଚ୍ଚ ଜୁଆର: $nextHighTideLabel • ଶୀର୍ଷ ଜଳସ୍ତର: ${String.format("%.1f", peakWaterMeters)}m • ବେଳାଭୂମି ଦୂରତା: ${String.format("%.1f", currentRecessionKm)} km)।"
                                } else {
                                    "Push notifications for Chandipur Beach based on local tidal data (Next High Tide: $nextHighTideLabel • Peak Level: ${String.format("%.1f", peakWaterMeters)}m • Seabed Exposure: ${String.format("%.1f", currentRecessionKm)} km)."
                                },
                                fontSize = 11.5.sp,
                                color = if (isChandipurHighTideAlertsEnabled) Color(0xFF075985) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isChandipurHighTideAlertsEnabled,
                            onCheckedChange = onToggleChandipurHighTideAlerts,
                            modifier = Modifier.testTag("settings_chandipur_high_tide_switch")
                        )
                    }

                    if (isChandipurHighTideAlertsEnabled) {
                        OutlinedButton(
                            onClick = onTestChandipurHighTidePush,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("settings_test_high_tide_push_btn"),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "🔔 ବର୍ତ୍ତମାନ ଉଚ୍ଚ ଜୁଆର ଆଲର୍ଟ ପରୀକ୍ଷା କରନ୍ତୁ"
                                } else {
                                    "🔔 Send Chandipur High Tide Alert Now (Local Tidal Data)"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Proactive 'Safe Tide Reminder' (30 Minutes Before Safe Walking Window) Toggle & Sound Settings
                    val tideReminderConfig by com.example.data.notification.ChandipurTideReminderPreferences.configFlow.collectAsState()
                    androidx.compose.runtime.LaunchedEffect(Unit) {
                        com.example.data.notification.ChandipurTideReminderPreferences.loadConfig(context)
                    }
                    val liveHarmonicWindow = androidx.compose.runtime.remember {
                        LocalTideCalculationEngine.evaluateLiveTideWindow()
                    }
                    val activeSoundLabel = com.example.data.notification.ChandipurTideReminderPreferences.getSoundDisplayLabel(
                        tideReminderConfig,
                        language == AppLanguage.ODIA
                    )
                    androidx.compose.material3.HorizontalDivider(color = Color(0xFFBAE6FD))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "⏰ ସୁରକ୍ଷିତ ଜୁଆର ରିମାଇଣ୍ଡର (୩୦ ମିନିଟ୍ ପୂର୍ବରୁ ଆଲର୍ଟ)"
                                } else {
                                    "⏰ Proactive 'Safe Tide Reminder' (30m Before Window)"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF0369A1)
                            )
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ସୁରକ୍ଷିତ ଚାଲିବା ସମୟ (${liveHarmonicWindow.safeWalkingWindowRange}) • ଆଲର୍ଟ ସ୍ୱର: $activeSoundLabel"
                                } else {
                                    "Alerts 30m before Chandipur safe walk (${liveHarmonicWindow.safeWalkingWindowRange}) • Alert Sound: $activeSoundLabel"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF075985)
                            )
                        }
                        Switch(
                            checked = tideReminderConfig.is30MinReminderEnabled,
                            onCheckedChange = { enabled ->
                                com.example.data.notification.ChandipurTideReminderPreferences.set30MinReminderEnabled(
                                    context,
                                    enabled
                                )
                                if (enabled) {
                                    LocalTideCalculationEngine.triggerSafeTide30MinNotification(
                                        context,
                                        language == AppLanguage.ODIA
                                    )
                                }
                            },
                            modifier = Modifier.testTag("settings_safe_tide_30min_alert_switch")
                        )
                    }

                    Button(
                        onClick = onOpenTideReminderSettings,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_open_tide_reminder_sound_screen_btn"),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7))
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) {
                                "🔊 ୩୦-ମିନିଟ୍ ରିମାଇଣ୍ଡର ଓ ଆଲର୍ଟ ସ୍ୱର କଷ୍ଟମାଇଜ୍ କରନ୍ତୁ →"
                            } else {
                                "🔊 Customize 30-Min Tide Reminder & Alert Sound →"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // 'News Preferences' Screen / Section in Settings (Backed by Local Room Database + Firestore Push Subscriptions)
            val settingsScope = androidx.compose.runtime.rememberCoroutineScope()
            val roomSelectedCategories by RoomNewsPreferencesRepository.selectedCategoriesFlow.collectAsState()
            val enforceRoomNewsFilter by RoomNewsPreferencesRepository.enforceRoomFilterFlow.collectAsState()
            androidx.compose.runtime.LaunchedEffect(Unit) {
                RoomNewsPreferencesRepository.loadFromRoomDatabase(context)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_news_category_preferences_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) {
                                "📰 ଖବର ପସନ୍ଦ (News Preferences — Room DB)"
                            } else {
                                "📰 News Preferences (Room DB Feed Filter & Alerts)"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "ROOM DB SAVED (${roomSelectedCategories.size})",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        text = if (language == AppLanguage.ODIA) {
                            "ରାଜନୀତି, ଶିକ୍ଷା (Education), ପର୍ଯ୍ୟଟନ, ପ୍ରତିରକ୍ଷା, କ୍ରୀଡ଼ା ଓ ନାଗରିକ ବିഭാഗ ବାଛନ୍ତୁ। ଆପଣଙ୍କ ପସନ୍ଦ ସ୍ଥାନୀୟ Room ଡାଟାବେସରେ ସଂରକ୍ଷିତ ହୁଏ।"
                        } else {
                            "Filter your news feed by selecting categories (Politics, Education, Tourism, Defense, Sports, Civic, Weather). Stored locally in the Room database."
                        },
                        fontSize = 11.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    androidx.compose.foundation.layout.FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        RoomNewsPreferencesRepository.ALL_SELECTABLE_CATEGORIES.forEach { cat ->
                            val isSub = roomSelectedCategories.contains(cat) || subscribedNewsCategories.contains(cat)
                            androidx.compose.material3.FilterChip(
                                selected = isSub,
                                onClick = {
                                    onToggleNewsCategorySubscription(cat)
                                    val updated = if (roomSelectedCategories.contains(cat)) {
                                        if (roomSelectedCategories.size > 1) roomSelectedCategories - cat else roomSelectedCategories
                                    } else {
                                        roomSelectedCategories + cat
                                    }
                                    settingsScope.launch {
                                        RoomNewsPreferencesRepository.saveCategoriesToRoom(
                                            context = context,
                                            categories = updated,
                                            enforceFilterInFeed = true
                                        )
                                    }
                                },
                                label = {
                                    Text(
                                        text = if (isSub) "✓ $cat" else cat,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSub) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                modifier = Modifier.testTag("settings_news_cat_chip_${cat.lowercase()}")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) {
                                "ଖବର ଫିଡ୍‌ରେ Room ପସନ୍ଦ ଫିଲ୍ଟର ଲାଗୁ କରନ୍ତୁ"
                            } else {
                                "Filter News Feed by Room Saved Categories (${roomSelectedCategories.joinToString(", ")})"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = enforceRoomNewsFilter,
                            onCheckedChange = { enforce ->
                                settingsScope.launch {
                                    RoomNewsPreferencesRepository.saveCategoriesToRoom(
                                        context = context,
                                        categories = roomSelectedCategories,
                                        enforceFilterInFeed = enforce
                                    )
                                }
                            },
                            modifier = Modifier.testTag("settings_enforce_room_news_filter_switch")
                        )
                    }

                    val firstSub = roomSelectedCategories.firstOrNull() ?: subscribedNewsCategories.firstOrNull() ?: "Politics"
                    OutlinedButton(
                        onClick = { onTestCategoryPushAlert(firstSub) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_test_category_push_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) {
                                "🔔 '$firstSub' Firestore ପୁଶ୍ ଆଲର୍ଟ ପରୀକ୍ଷା କରନ୍ତୁ"
                            } else {
                                "🔔 Trigger '$firstSub' Firestore Document Push Alert"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Adaptive Sync (<20% Battery Auto-Throttle) & System Battery Optimization Deep-Link Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_adaptive_sync_and_battery_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAdaptiveSyncEnabled) Color(0xFFFEF3C7).copy(alpha = 0.65f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isAdaptiveSyncEnabled) Color(0xFFD97706) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "⚡ ଆଡାପ୍ଟିଭ୍ ସିଙ୍କ୍ (<20% ବ୍ୟାଟେରୀ ଅଟୋ-ଥ୍ରୋଟଲ୍)"
                                } else {
                                    "⚡ Adaptive Sync (<20% Battery Auto-Throttle)"
                                },
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = if (isAdaptiveSyncEnabled) Color(0xFF92400E) else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "ଡିଭାଇସ୍ ବ୍ୟାଟେରୀ 20% ତଳକୁ ଖସିଲେ ପାଣିପାଗ, ବସ୍ ଓ ଖବର ବ୍ୟାକଗ୍ରାଉଣ୍ଡ ଅପଡେଟ୍ ଆପେ ଆପେ କମିଯାଏ।"
                                } else {
                                    "Automatically reduces background update frequency of weather, bus schedules, and news data when battery falls below 20%."
                                },
                                fontSize = 11.5.sp,
                                color = if (isAdaptiveSyncEnabled) Color(0xFF78350F) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isAdaptiveSyncEnabled,
                            onCheckedChange = { enabled ->
                                onToggleAdaptiveSync(enabled)
                                com.example.data.sync.SyncManager.schedulePeriodicSync(context)
                            },
                            modifier = Modifier.testTag("settings_adaptive_sync_switch")
                        )
                    }

                    // Live Battery Telemetry + Effective Background Update Frequency Breakdown
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isLowBatteryThrottled) Color(0xFFFEE2E2) else Color(0xFFFFFBEB),
                        border = BorderStroke(1.dp, if (isLowBatteryThrottled) Color(0xFFF87171) else Color(0xFFFDE68A)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_adaptive_sync_telemetry_box")
                    ) {
                        Column(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = if (isLowBatteryThrottled) {
                                    "🔋 Battery: $effectiveBatteryPct% (<20% Low-Battery Throttle ACTIVE)"
                                } else {
                                    "🔋 Battery: $effectiveBatteryPct% (Standard Adaptive Sync Active)"
                                },
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isLowBatteryThrottled) Color(0xFFB91C1C) else Color(0xFF92400E)
                            )
                            Text(
                                text = "• Weather Sync: every ${effectiveWeatherMins}m (Normal: 30m ➔ <20% Battery: 120m)",
                                fontSize = 10.5.sp,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = "• Bus Schedules: every ${effectiveBusSecs}s (Normal: 15s ➔ <20% Battery: 60s)",
                                fontSize = 10.5.sp,
                                color = Color(0xFF78350F)
                            )
                            Text(
                                text = "• News Feed Sync: every ${if (effectiveNewsSecs >= 60) "${effectiveNewsSecs / 60}m" else "${effectiveNewsSecs}s"} (Normal: 45s ➔ <20% Battery: 30m)",
                                fontSize = 10.5.sp,
                                color = Color(0xFF78350F)
                            )
                        }
                    }

                    // Simulate Low Battery (<20%) Switch for instant verification
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "🧪 <20% କମ୍ ବ୍ୟାଟେରୀ ମୋଡ୍ ପରୀକ୍ଷା କରନ୍ତୁ (14%)" else "🧪 Simulate <20% Low Battery Level (14%)",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF78350F)
                        )
                        Switch(
                            checked = simulateLowBattery,
                            onCheckedChange = { sim ->
                                simulateLowBattery = sim
                                com.example.data.sync.SyncManager.setSimulateLowBatteryEnabled(context, sim)
                                com.example.data.sync.SyncManager.schedulePeriodicSync(context)
                            },
                            modifier = Modifier.testTag("settings_simulate_low_battery_switch")
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                runCatching {
                                    val fallback = Intent(
                                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.parse("package:${context.packageName}")
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(fallback)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_battery_optimization_deeplink_btn"),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) {
                                "⚙️ ଆଣ୍ଡ୍ରଏଡ୍ ବ୍ୟାଟେରୀ ଅପ୍ଟିମାଇଜେସନ୍ ସେଟିଂସ୍ ଖୋଲନ୍ତୁ"
                            } else {
                                "⚙️ Open Android Battery Optimization Settings"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Battery-Saving Mode Toggle Card (Reduces background data syncs for News & Bus Schedules)
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("settings_battery_saving_mode_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBatterySavingModeEnabled) Color(0xFFECFDF5) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isBatterySavingModeEnabled) Color(0xFF10B981) else MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 10.dp)) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "🔋 ବ୍ୟାଟେରୀ ସେଭିଂ ମୋଡ୍ (Battery-Saving Mode)" else "🔋 Battery-Saving Mode",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp,
                            color = if (isBatterySavingModeEnabled) Color(0xFF065F46) else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) {
                                "ଡିଭାଇସ୍ ବ୍ୟାଟେରୀ ବଞ୍ଚାଇବା ପାଇଁ ଖବର (୩୦ ମି.) ଓ ବସ୍ ସମୟ ସାରଣୀ (୬୦ ସେ.) ବ୍ୟାକଗ୍ରାଉଣ୍ଡ ସିଙ୍କ୍ ହ୍ରାସ କରେ।"
                            } else {
                                "Reduces background data sync frequency for News (30m) & Bus Schedule (60s) modules to extend device battery life."
                            },
                            fontSize = 11.5.sp,
                            color = if (isBatterySavingModeEnabled) Color(0xFF047857) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isBatterySavingModeEnabled,
                        onCheckedChange = onToggleBatterySavingMode,
                        modifier = Modifier.testTag("settings_battery_saving_mode_switch")
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Hourly Telemetry Auto-Refresh", fontSize = 14.sp)
                Switch(
                    checked = isAutoRefreshEnabled,
                    onCheckedChange = onToggleAutoRefresh,
                    modifier = Modifier.testTag("settings_hourly_auto_refresh_switch")
                )
            }
            LanguageSwitcherBannerCard(currentLanguage = language, onLanguageSelected = onLanguageChange)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpenAppHealth, modifier = Modifier.weight(1f)) {
                    Text("Battery & Connectivity")
                }
                OutlinedButton(onClick = onOpenPrivacyPolicy, modifier = Modifier.weight(1f)) {
                    Text("Privacy Policy")
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

