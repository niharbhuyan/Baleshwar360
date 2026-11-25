package com.example.ui.features.heritage

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Badge
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.daily.DailyBalasorePulse
import com.example.data.model.AppLanguage
import com.example.util.NewsTextToSpeechHelper

private const val TEMPLE_CROWD_PREFS = "balasore_temple_crowd_estimator_v1"

private enum class CrowdLevel(
    val labelEn: String,
    val labelOd: String,
    val waitMins: String,
    val bg: Color,
    val fg: Color
) {
    LIGHT("Light Crowd", "କମ୍ ଭିଡ଼", "5–10 min wait", Color(0xFFDCFCE7), Color(0xFF15803D)),
    MODERATE("Moderate", "ମଧ୍ୟମ ଭିଡ଼", "15–25 min wait", Color(0xFFFEF3C7), Color(0xFFB45309)),
    HEAVY("Heavy Rush", "ଅଧିକ ଭିଡ଼", "35–50 min wait", Color(0xFFFEE2E2), Color(0xFFB91C1C))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreTemplePilgrimageCompanionSheet(
    language: AppLanguage,
    dailyPulse: DailyBalasorePulse,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val ttsHelper = remember { NewsTextToSpeechHelper.getInstance(context) }
    val isSpeaking by ttsHelper.isSpeaking.collectAsStateWithLifecycle()
    val currentUtteranceId by ttsHelper.currentArticleId.collectAsStateWithLifecycle()

    var selectedTempleIndex by remember { mutableIntStateOf(0) }
    var audioGuideLang by remember { mutableStateOf(language) }

    val crowdPrefs = remember { context.getSharedPreferences(TEMPLE_CROWD_PREFS, Context.MODE_PRIVATE) }
    val crowdState = remember {
        mutableStateMapOf(
            "remuna" to CrowdLevel.entries.getOrElse(crowdPrefs.getInt("crowd_remuna", 1)) { CrowdLevel.MODERATE },
            "panchalingeswar" to CrowdLevel.entries.getOrElse(crowdPrefs.getInt("crowd_panchalingeswar", 0)) { CrowdLevel.LIGHT },
            "chandaneswar" to CrowdLevel.entries.getOrElse(crowdPrefs.getInt("crowd_chandaneswar", 1)) { CrowdLevel.MODERATE },
            "bhusandheswar" to CrowdLevel.entries.getOrElse(crowdPrefs.getInt("crowd_bhusandheswar", 0)) { CrowdLevel.LIGHT }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            if (ttsHelper.currentArticleId.value?.startsWith("temple_guide_") == true) {
                ttsHelper.stop()
            }
        }
    }

    val temples = listOf(
        TempleDetail(
            id = "remuna",
            nameEn = "Remuna Khirachora Gopinath Temple",
            nameOd = "ରେମୁଣା ଖିରଚୋରା ଗୋପୀନାଥ ଜିଉ",
            location = "Remuna Block, 9 km from Balasore",
            deity = "Lord Krishna (Stealer of Condensed Milk / Khira)",
            specialty = "Holy Amruta Keli Khira Bhog in earthen pots",
            timings = "05:30 AM - 12:45 PM & 04:30 PM - 08:30 PM",
            currentPhase = dailyPulse.remunaActiveAartiPhase,
            potsRemaining = dailyPulse.remunaKhiraBhogPotsAvailable,
            history = "Saint Madhavendra Puri received the miraculously hidden bowl of condensed milk directly from Lord Gopinath in the 15th century.",
            audioGuideOd = "ରେମୁଣା ଖିରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିରକୁ ସ୍ୱାଗତ। ଏଠାରେ ଭଗବାନ ଶ୍ରୀକୃଷ୍ଣ ଅମୃତକେଳି ଖିରଭୋଗ ପାଇଁ ବିଶ୍ୱପ୍ରସିଦ୍ଧ। ସକାଳ ୫ଟା ୩୦ ରୁ ମଙ୍ଗଳ ଆଳତି ଓ ମଧ୍ୟାହ୍ନ ୧୨ଟା ୩୦ ରେ ବିଶେଷ ଖିରଭୋଗ ଲାଗି ହୁଏ।",
            rituals = listOf(
                "05:30 AM • Mangala Alati (First Darshan)",
                "08:00 AM • Sakala Dhupa & Tulasi Arpan",
                "12:30 PM • Madhyanna Dhupa & Special Amruta Keli Khira Bhog",
                "06:45 PM • Sandhya Deepa Alati & Gita Govinda recitation",
                "08:30 PM • Pahada (Night rest of the Lord)"
            )
        ),
        TempleDetail(
            id = "panchalingeswar",
            nameEn = "Panchalingeswar Temple",
            nameOd = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ମହାଦେବ",
            location = "Nilagiri Hills, 30 km from Balasore",
            deity = "Five Swayambhu Shiva Lingams submerged in mountain spring",
            specialty = "Touch the holy lingams inside perennial hill stream",
            timings = "06:00 AM - 06:30 PM (Daylight hours)",
            currentPhase = if (dailyPulse.isPanchalingeswarClimbSafe) "Climb Safe 🟢 (Spring Flow: ${dailyPulse.panchalingeswarStreamFlowMeters}m)" else "Caution on Steps ⚠️",
            potsRemaining = null,
            history = "Revered in Valmiki Ramayana; Goddess Sita worshiped these five self-manifested Shiva lingams in the Nilagiri hills during her exile.",
            audioGuideOd = "ନୀଳଗିରି ପର୍ବତର ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ମହାଦେବ ପୀଠକୁ ସ୍ୱାଗତ। ଏଠାରେ ପାହାଡ଼ୀ ଝରଣା ତଳେ ପାଞ୍ଚୋଟି ସ୍ୱୟମ୍ଭୂ ଶିବଲିଙ୍ଗ ପୂଜା ପାଆନ୍ତି। ସକାଳ ୬ଟାରୁ ସନ୍ଧ୍ୟା ୬ଟା ୩୦ ପର୍ଯ୍ୟନ୍ତ ଦର୍ଶନ ଖୋଲା ରହେ।",
            rituals = listOf(
                "06:00 AM • Morning Spring Jalabhisheka",
                "11:30 AM • Hill Stream Maha Naivedya",
                "05:30 PM • Sunset Alati & Mountain Twilight Gate Closing"
            )
        ),
        TempleDetail(
            id = "chandaneswar",
            nameEn = "Chandaneswar Shiva Temple",
            nameOd = "ଚନ୍ଦନେଶ୍ୱର ମହାଦେବ",
            location = "Bhograi Block, Coastal Border",
            deity = "Lord Shiva (Chandaneswar Bada Deula)",
            specialty = "Ancient miracle temple famous for historic Chadak Mela",
            timings = "05:00 AM - 01:00 PM & 04:00 PM - 09:00 PM",
            currentPhase = "Holy Darshan & Bilwa Patra Arpan Active",
            potsRemaining = null,
            history = "Renowned regional pilgrimage center where millions assemble during the Chaitra Pana Sankranti for holy penance and fire-walking rites.",
            audioGuideOd = "ଭୋଗରାଇ ବ୍ଲକର ପ୍ରସିଦ୍ଧ ଚନ୍ଦନେଶ୍ୱର ମହାଦେବ ମନ୍ଦିରକୁ ସ୍ୱାଗତ। ଚୈତ୍ର ମାସର ଐତିହାସିକ ଚଡ଼କ ମେଳା ପାଇଁ ଏହି ପୀଠ ପ୍ରସିଦ୍ଧ। ଦର୍ଶନ ସମୟ ସକାଳ ୫ଟାରୁ ରାତି ୯ଟା।",
            rituals = listOf(
                "05:00 AM • Pahali Dhupa & Gangajal Snana",
                "12:00 PM • Madhyanna Bhoga (Sweet Rice & Dal)",
                "07:00 PM • Sandhya Sankha Dhwani & Alati"
            )
        ),
        TempleDetail(
            id = "bhusandheswar",
            nameEn = "Baba Bhusandheswar Temple",
            nameOd = "ବାବା ଭୂଷଣ୍ଡେଶ୍ୱର ମନ୍ଦିର",
            location = "Bhusandheswar, near Subarnarekha bank",
            deity = "Colossal Monolithic Shiva Lingam (Asia's Largest)",
            specialty = "Black granite monolithic lingam (12ft height, 14ft circumference)",
            timings = "06:00 AM - 07:30 PM",
            currentPhase = "Dugdha & Belpatra Abhisheka Open",
            potsRemaining = null,
            history = "Legends date back to Treta Yuga when Ravana carried this mighty Shiva lingam to Lanka and placed it down near the Subarnarekha river bank.",
            audioGuideOd = "ସୁବର୍ଣ୍ଣରେଖା କୂଳର ବାବା ଭୂଷଣ୍ଡେଶ୍ୱର ମନ୍ଦିରକୁ ସ୍ୱାଗତ। ଏଠାରେ ଏସିଆର ବୃହତ୍ତମ କଳା ମୁଗୁନି ପଥର ଶିବଲିଙ୍ଗ ବିରାଜମାନ କରିଛନ୍ତି।",
            rituals = listOf(
                "06:30 AM • Pratah Maha Abhisheka with Pure Milk",
                "01:00 PM • Anna Bhoga & Chhatra Arpan",
                "06:30 PM • Deepa Dana & Sandhya Alati"
            )
        )
    )

    val selectedTemple = temples[selectedTempleIndex]
    val activeCrowd = crowdState[selectedTemple.id] ?: CrowdLevel.MODERATE
    val isThisTempleAudioPlaying = isSpeaking && currentUtteranceId == "temple_guide_${selectedTemple.id}"

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .testTag("temple_pilgrimage_companion_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ମନ୍ଦିର ଦର୍ଶନ, ଭୋଗ ଓ ଭିଡ଼ ସୂଚନା 🛕" else "Temple Darshan, Bhog & Crowd Guide 🛕",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (language == AppLanguage.ODIA) "ରେମୁଣା, ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଓ ଚନ୍ଦନେଶ୍ୱର • ଲାଇଭ୍ ଭିଡ଼ ଓ ଅଡିଓ ଗାଇଡ୍" else "Live Darshan Timings, Crowd Estimator & Odia/EN Audio Guide",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }
            }

            // Spiritual Bell Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF78350F)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("TEMPLE CHIME & SHANKHA", color = Color(0xFFFDE68A), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            Text("Play Sacred Bell Sound", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text("Audible darshan chime for prayer meditation", color = Color(0xFFFCD34D), fontSize = 11.sp)
                        }
                        IconButton(
                            onClick = {
                                try {
                                    val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 90)
                                    toneGen.startTone(ToneGenerator.TONE_PROP_BEEP2, 400)
                                } catch (_: Exception) {}
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B))
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = "Ring Temple Bell", tint = Color(0xFF78350F))
                        }
                    }
                }
            }

            // Remuna Khira Bhog Live Counter
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                    border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🥣", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("Amruta Keli Khira Bhog Counter", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF78350F))
                                    Text("Fresh earthen pots at Remuna Gopinatha", fontSize = 11.sp, color = Color(0xFF92400E))
                                }
                            }
                            Badge(containerColor = Color(0xFFB45309)) {
                                Text("${dailyPulse.remunaKhiraBhogPotsAvailable} POTS READY", color = Color.White, fontSize = 10.sp, modifier = Modifier.padding(2.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Current Sanctum Phase: ${dailyPulse.remunaActiveAartiPhase}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFFB45309)
                        )
                    }
                }
            }

            // Temple Selector Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    temples.forEachIndexed { index, _ ->
                        val isSelected = index == selectedTempleIndex
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTempleIndex = index },
                            label = {
                                Text(
                                    text = when (index) {
                                        0 -> "Remuna"
                                        1 -> "Panchalingeswar"
                                        2 -> "Chandaneswar"
                                        else -> "Bhusandheswar"
                                    },
                                    fontSize = 10.5.sp
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Community Crowd Estimator Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("temple_crowd_estimator_card"),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = activeCrowd.bg.copy(alpha = 0.65f)),
                    border = BorderStroke(1.2.dp, activeCrowd.fg.copy(alpha = 0.45f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Groups, contentDescription = null, tint = activeCrowd.fg, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ଲାଇଭ୍ ଭିଡ଼ ଅନୁମାନ (Community)" else "Community Crowd Estimator",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = activeCrowd.fg
                                    )
                                    Text(
                                        text = "${if (language == AppLanguage.ODIA) activeCrowd.labelOd else activeCrowd.labelEn} • ${activeCrowd.waitMins}",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = activeCrowd.fg
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ବର୍ତ୍ତମାନ ଭିଡ଼ ଅପଡେଟ୍ କରନ୍ତୁ:" else "At the temple? Update current crowd:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = activeCrowd.fg
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            CrowdLevel.entries.forEachIndexed { idx, level ->
                                val isSel = activeCrowd == level
                                FilterChip(
                                    selected = isSel,
                                    onClick = {
                                        crowdState[selectedTemple.id] = level
                                        crowdPrefs.edit().putInt("crowd_${selectedTemple.id}", idx).apply()
                                        Toast.makeText(
                                            context,
                                            "Updated ${selectedTemple.nameEn} crowd to ${level.labelEn}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    label = {
                                        Text(
                                            text = if (language == AppLanguage.ODIA) level.labelOd else level.labelEn.substringBefore(" "),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = level.fg,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("crowd_report_chip_${level.name.lowercase()}")
                                )
                            }
                        }
                    }
                }
            }

            // Selected Temple Card + Bilingual Odia & English Audio Guide
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = if (language == AppLanguage.ODIA) selectedTemple.nameOd else selectedTemple.nameEn,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(selectedTemple.location, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Deity: ${selectedTemple.deity}", fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        Text("Specialty: ${selectedTemple.specialty}", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Audio Guide in Odia & English
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFF0F172A),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("temple_audio_guide_card")
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.Headphones, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (audioGuideLang == AppLanguage.ODIA) "ମନ୍ଦିର ଅଡିଓ ଗାଇଡ୍ (ଓଡ଼ିଆ)" else "Quick Temple Audio Guide",
                                            color = Color.White,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        FilterChip(
                                            selected = audioGuideLang == AppLanguage.ODIA,
                                            onClick = { audioGuideLang = AppLanguage.ODIA },
                                            label = { Text("ଓଡ଼ିଆ", fontSize = 10.sp) }
                                        )
                                        FilterChip(
                                            selected = audioGuideLang == AppLanguage.ENGLISH,
                                            onClick = { audioGuideLang = AppLanguage.ENGLISH },
                                            label = { Text("EN", fontSize = 10.sp) }
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Button(
                                    onClick = {
                                        if (isThisTempleAudioPlaying) {
                                            ttsHelper.stop()
                                        } else {
                                            val enGuide = "${selectedTemple.nameEn}, located at ${selectedTemple.location}. ${selectedTemple.history} Darshan timings are ${selectedTemple.timings}. Specialty: ${selectedTemple.specialty}."
                                            ttsHelper.speakArticle(
                                                articleId = "temple_guide_${selectedTemple.id}",
                                                title = if (audioGuideLang == AppLanguage.ODIA) selectedTemple.nameOd else selectedTemple.nameEn,
                                                content = if (audioGuideLang == AppLanguage.ODIA) selectedTemple.audioGuideOd else enGuide,
                                                language = audioGuideLang
                                            )
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isThisTempleAudioPlaying) Color(0xFFEF4444) else Color(0xFF0284C7)
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(vertical = 8.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("temple_audio_guide_play_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isThisTempleAudioPlaying) Icons.Default.Stop else Icons.Default.Headphones,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isThisTempleAudioPlaying) "Stop Audio Guide" else if (audioGuideLang == AppLanguage.ODIA) "ଓଡ଼ିଆ ଅଡିଓ ଗାଇଡ୍ ଶୁଣନ୍ତୁ" else "Play English Audio Guide",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Gate Hours: ${selectedTemple.timings}", fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text("🔔 ${selectedTemple.currentPhase}", fontSize = 10.5.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Daily Aarti & Ritual Schedule", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        selectedTemple.rituals.forEach { ritual ->
                            Text("• $ritual", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(vertical = 2.dp))
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = selectedTemple.history,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private data class TempleDetail(
    val id: String,
    val nameEn: String,
    val nameOd: String,
    val location: String,
    val deity: String,
    val specialty: String,
    val timings: String,
    val currentPhase: String,
    val potsRemaining: Int?,
    val history: String,
    val audioGuideOd: String,
    val rituals: List<String>
)
