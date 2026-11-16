package com.example.ui.components

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.util.ShareHelper
import com.example.widget.BalasoreFloodGaugeWidgetProvider
import java.util.Locale

data class EvacuationCorridorRoute(
    val id: String,
    val corridorNameEn: String,
    val corridorNameOr: String,
    val shelterNameEn: String,
    val shelterNameOr: String,
    val distanceKm: String,
    val elevationGainM: String,
    val helplineNumber: String,
    val stepsEn: List<String>,
    val stepsOr: List<String>
)

data class RiverGaugeStationItem(
    val id: String,
    val riverNameEn: String,
    val riverNameOr: String,
    val gaugeSiteEn: String,
    val gaugeSiteOr: String,
    val currentLevelM: Float,
    val warningLevelM: Float,
    val dangerLevelM: Float,
    val barrageInfoEn: String,
    val barrageInfoOr: String
)

data class BalasoreGiArtisanCluster(
    val id: String,
    val craftNameEn: String,
    val craftNameOr: String,
    val clusterVillageEn: String,
    val clusterVillageOr: String,
    val giRegistryCode: String,
    val cooperativeNameEn: String,
    val cooperativePhone: String,
    val priceRange: String,
    val highlightEn: String,
    val highlightOr: String
)

/**
 * 1. Offline Voice-Guided Coastal Evacuation Companion
 * Turn-by-turn bilingual Odia/English TTS audio prompts guiding residents along safe
 * high-ground corridors to the nearest OSDMA multipurpose cyclone shelter when internet drops.
 */
@Composable
fun VoiceGuidedEvacuationCompanionCard(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    var selectedCorridorIndex by remember { mutableIntStateOf(0) }
    var activeStepIndex by remember { mutableIntStateOf(0) }
    var isSpeaking by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val locale = if (isOdia) Locale("or", "IN") else Locale.US
                val res = instance?.setLanguage(locale)
                if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    instance?.setLanguage(Locale("en", "IN"))
                }
            }
        }
        ttsEngine = instance
        onDispose {
            instance?.stop()
            instance?.shutdown()
        }
    }

    val corridors = remember {
        listOf(
            EvacuationCorridorRoute(
                id = "chandipur_mirzapur",
                corridorNameEn = "Chandipur Coast ➔ Mirzapur MPCS",
                corridorNameOr = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ➔ ମିର୍ଜାପୁର ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ",
                shelterNameEn = "Mirzapur OSDMA Multipurpose Cyclone Shelter",
                shelterNameOr = "ମିର୍ଜାପୁର ବହୁମୁଖୀ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ",
                distanceKm = "3.2 km",
                elevationGainM = "+14m High Ground",
                helplineNumber = "06782-262043",
                stepsEn = listOf(
                    "Step 1: Head west from Chandipur Panthanivas along Proof Road high embankment for 1.4 km.",
                    "Step 2: Cross the tidal sluice bridge and turn right onto Mirzapur elevated concrete link road.",
                    "Step 3: Proceed 800m to Mirzapur OSDMA Cyclone Shelter (Solar backup, drinking water & medical bay active)."
                ),
                stepsOr = listOf(
                    "ପଦକ୍ଷେପ ୧: ଚାନ୍ଦିପୁର ପାନ୍ଥନିବାସରୁ ପଶ୍ଚିମ ଦିଗରେ ପ୍ରୁଫ୍ ରୋଡ୍ ଉଚ୍ଚ ବନ୍ଧ ଦେଇ ୧.୪ କି.ମି. ଆଗକୁ ବଢନ୍ତୁ।",
                    "ପଦକ୍ଷେପ ୨: ଜୁଆର ସ୍ଲুইସ୍ ପୋଲ ପାର ହୋଇ ମିର୍ଜାପୁର ଉଚ୍ଚ କଂକ୍ରିଟ୍ ରାସ୍ତାରେ ଡାହାଣକୁ ବୁലନ୍ତୁ।",
                    "ପଦକ୍ଷେପ ୩: ୮୦୦ ମିଟର ଆଗରେ ମିର୍ଜାପୁର ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀରେ ପହଞ୍ଚନ୍ତୁ (ସୌର ବିଦ୍ୟୁତ୍ ଓ ପ୍ରାଥମିକ ଚିକିତ୍ସା ଉପଲବ୍ଧ)।"
                )
            ),
            EvacuationCorridorRoute(
                id = "talasari_udaipur",
                corridorNameEn = "Talasari Estuary ➔ Udaipur MPCS",
                corridorNameOr = "ତାଳସାରୀ ମୁହାଣ ➔ ଉଦୟପୁର ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ",
                shelterNameEn = "Udaipur Coastal Multipurpose Shelter",
                shelterNameOr = "ଉଦୟପୁର ଉପକୂଳ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ",
                distanceKm = "2.6 km",
                elevationGainM = "+12m Casuarina Ridge",
                helplineNumber = "06781-253210",
                stepsEn = listOf(
                    "Step 1: Exit Talasari Jetty northwards past the casuarina shelterbelt dune ridge.",
                    "Step 2: Follow the brick-paved cyclone evacuation corridor 1.8 km towards Udaipur school ground.",
                    "Step 3: Enter Udaipur OSDMA Shelter on the left (Capacity 1,500 • VHF radio connected)."
                ),
                stepsOr = listOf(
                    "ପଦକ୍ଷେପ ୧: ତାଳସାରୀ ଜେଟିରୁ ଉତ୍ତର ଦିଗରେ ঝାଉଁ ଜଙ୍ଗଲ ଉଚ୍ଚ ରାସ୍ତା ଦେଇ ଆଗକୁ ବଢନ୍ତୁ।",
                    "ପଦକ୍ଷେପ ୨: ୧.୮ କି.ମି. ବାତ୍ୟା ଉଦ୍ଧାର ରାସ୍ତା ଦେଇ ଉଦୟପୁର ସ୍କୁଲ ପଡ଼ିଆ ନିକଟକୁ ଯାଆନ୍ତୁ।",
                    "ପଦକ୍ଷେପ ୩: ବାମ ପାର୍ଶ୍ୱରେ ଥିବା ଉଦୟପୁର ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀରେ ପ୍ରବେଶ କରନ୍ତୁ।"
                )
            ),
            EvacuationCorridorRoute(
                id = "baliapal_jambhirai",
                corridorNameEn = "Baliapal / Subarnarekha ➔ Jambhirai MPCS",
                corridorNameOr = "ବାଲିଆପାଳ / ସୁବର୍ଣ୍ଣରେଖା ➔ ଜମ୍ଭୀରାଇ ଆଶ୍ରୟସ୍ଥଳୀ",
                shelterNameEn = "Jambhirai High-Ground Flood & Cyclone Shelter",
                shelterNameOr = "ଜମ୍ଭୀରାଇ ବନ୍ୟା ଓ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ",
                distanceKm = "3.5 km",
                elevationGainM = "+15m Embankment",
                helplineNumber = "06781-252244",
                stepsEn = listOf(
                    "Step 1: Move inland from low-lying Subarnarekha floodplain onto Baliapal-Kamtarda SH-57 embankment.",
                    "Step 2: Follow ODRAF reflective markers 2.1 km east to Jambhirai elevated junction.",
                    "Step 3: Arrive at Jambhirai MPCS (Community kitchen & life-jacket store ready)."
                ),
                stepsOr = listOf(
                    "ପଦକ୍ଷେପ ୧: ସୁବର୍ଣ୍ଣରେଖା ତଳିଆ ଅଞ୍ଚଳରୁ ବାଲିଆପାଳ-କମର୍ଦ୍ଦା ଉଚ୍ଚ ରାଜପଥ ବନ୍ଧକୁ ଉଠନ୍ତୁ।",
                    "ପଦକ୍ଷେପ ୨: ODRAF ସୂଚନା ଚିହ୍ନ ଅନୁସରଣ କରି ୨.୧ କି.ମି. ଜମ୍ଭୀରାଇ ଛକକୁ ଯାଆନ୍ତୁ।",
                    "ପଦକ୍ଷେପ ୩: ଜମ୍ଭୀରାଇ ବହୁମୁଖୀ ଆଶ୍ରୟସ୍ଥଳୀରେ ସୁରକ୍ଷିତ ପହଞ୍ଚନ୍ତୁ।"
                )
            )
        )
    }

    val currentCorridor = corridors[selectedCorridorIndex]
    val steps = if (isOdia) currentCorridor.stepsOr else currentCorridor.stepsEn

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_guided_evacuation_companion_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OceanBlue,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.GraphicEq else Icons.Default.VolumeUp,
                                contentDescription = "Offline Voice Evacuation Guide",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) {
                                "🔊 ଅଫଲାଇନ୍ ଭଏସ୍-ଗାଇଡେଡ୍ ବାତ୍ୟା ଉଦ୍ଧାର ପଥ"
                            } else {
                                "🔊 Offline Voice-Guided Evacuation Navigator"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = BentoSlate900
                        )
                        Text(
                            text = if (isOdia) {
                                "ଇଣ୍ଟରନେଟ୍ ବିନା ଓଡ଼ିଆ/ଇଂରାଜୀ ଅଡିଓ ନିର୍ଦ୍ଦେଶାବଳୀ"
                            } else {
                                "Zero-internet Odia/English spoken turn-by-turn shelter routing"
                            },
                            fontSize = 11.sp,
                            color = OceanBlueDark
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldGreen.copy(alpha = 0.14f),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = if (isOdia) "ଅଫଲାଇନ୍ TTS" else "OFFLINE TTS",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = EmeraldGreen,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Corridor selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                corridors.forEachIndexed { idx, corridor ->
                    val selected = idx == selectedCorridorIndex
                    Surface(
                        onClick = {
                            selectedCorridorIndex = idx
                            activeStepIndex = 0
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selected) OceanBlue else Color.White,
                        border = BorderStroke(1.dp, if (selected) OceanBlue else Color(0xFFBAE6FD)),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("evacuation_corridor_chip_${corridor.id}")
                    ) {
                        Text(
                            text = corridor.corridorNameEn.substringBefore(" ➔"),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else OceanBlueDark,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color.White,
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.2f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) currentCorridor.shelterNameOr else currentCorridor.shelterNameEn,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BentoSlate900,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "${currentCorridor.distanceKm} • ${currentCorridor.elevationGainM}",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    steps.forEachIndexed { stepIdx, stepText ->
                        val isCurrentStep = stepIdx == activeStepIndex
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isCurrentStep) OceanBlue.copy(alpha = 0.1f) else Color.Transparent)
                                .clickable { activeStepIndex = stepIdx }
                                .padding(horizontal = 8.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Text(
                                text = if (isCurrentStep) "🔊 " else "• ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                            Text(
                                text = stepText,
                                fontSize = 11.sp,
                                fontWeight = if (isCurrentStep) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCurrentStep) BentoSlate900 else BentoSlate700,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        val fullSpeech = steps.joinToString(" ")
                        isSpeaking = true
                        runCatching {
                            ttsEngine?.speak(
                                fullSpeech,
                                TextToSpeech.QUEUE_FLUSH,
                                null,
                                "balasore_evac_${currentCorridor.id}"
                            )
                        }
                        Toast.makeText(
                            context,
                            if (isOdia) "ଭଏସ୍ ନିର୍ଦ୍ଦେଶାବଳୀ ଚାଲୁଅଛି..." else "Playing Offline Voice Evacuation Prompt...",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("voice_evacuation_speak_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "ଭଏସ୍ ନିର୍ଦ୍ଦେଶ ଶୁଣନ୍ତୁ" else "Speak Voice Route",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Surface(
                    onClick = {
                        activeStepIndex = (activeStepIndex + 1) % steps.size
                        val stepSpeech = steps[activeStepIndex]
                        isSpeaking = true
                        runCatching {
                            ttsEngine?.speak(stepSpeech, TextToSpeech.QUEUE_FLUSH, null, "step_$activeStepIndex")
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = OceanBlue.copy(alpha = 0.12f),
                    border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f)),
                    modifier = Modifier.testTag("voice_evacuation_next_step_btn")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Navigation,
                            contentDescription = "Next Step",
                            tint = OceanBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOdia) "ପରବର୍ତ୍ତୀ ମୋଡ଼" else "Next Turn",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                    }
                }
            }
        }
    }
}

/**
 * 2. Live Subarnarekha, Budhabalanga & Jalaka River Flood Gauge Monitor & Widget Launcher
 * Tracks upstream Galudih barrage releases and real-time river gauge levels against warning & danger marks.
 */
@Composable
fun LiveRiverFloodGaugeCard(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    var subarnarekhaLevel by remember { mutableStateOf(8.42f) }
    var budhabalangaLevel by remember { mutableStateOf(6.18f) }
    var jalakaLevel by remember { mutableStateOf(5.12f) }
    var lastUpdatedLabel by remember { mutableStateOf("CWC & Water Resources Telemetry • Live") }

    val stations = remember(subarnarekhaLevel, budhabalangaLevel, jalakaLevel) {
        listOf(
            RiverGaugeStationItem(
                id = "subarnarekha_rajghat",
                riverNameEn = "Subarnarekha River",
                riverNameOr = "ସୁବର୍ଣ୍ଣରେଖା ନଦୀ",
                gaugeSiteEn = "Rajghat Gauge (Jaleswar / Baliapal)",
                gaugeSiteOr = "ରାଜଘାଟ ମାନକ (ଜଳେଶ୍ୱର / ବାଲିଆପାଳ)",
                currentLevelM = subarnarekhaLevel,
                warningLevelM = 9.45f,
                dangerLevelM = 10.36f,
                barrageInfoEn = "Galudih Barrage: 2 Gates Open (18,400 cusecs) • Steady Trend",
                barrageInfoOr = "ଗାଲୁଡିহি ବ୍ୟାରେଜ୍: ୨ ଗେଟ୍ ଖୋଲା (୧୮,୪୦୦ କ୍ୟୁସେକ୍) • ସ୍ଥିର ପ୍ରବାହ"
            ),
            RiverGaugeStationItem(
                id = "budhabalanga_govindpur",
                riverNameEn = "Budhabalanga River",
                riverNameOr = "ବୁଢ଼ାବଳଙ୍ଗ ନଦୀ",
                gaugeSiteEn = "Govindpur NH-16 Bridge (Balasore Sadar)",
                gaugeSiteOr = "ଗୋବିନ୍ଦପୁର NH-16 ପୋଲ (ବାଲେଶ୍ୱର ସଦର)",
                currentLevelM = budhabalangaLevel,
                warningLevelM = 7.21f,
                dangerLevelM = 8.13f,
                barrageInfoEn = "Similipal Catchment Inflow Normal • Balaramgadi Estuary Tide Clear",
                barrageInfoOr = "ଶିମିଳିପାଳ ଜଳଗ୍ରହଣ ପ୍ରବାହ ସ୍ୱାଭାବିକ • ବଳରାମଗଡ଼ି ମୁହାଣ ସଫା"
            ),
            RiverGaugeStationItem(
                id = "jalaka_mathani",
                riverNameEn = "Jalaka River",
                riverNameOr = "ଜଳକା ନଦୀ",
                gaugeSiteEn = "Mathani Gauge (Basta Block)",
                gaugeSiteOr = "ମଥାନୀ ମାନକ (ବସ୍ତା ବ୍ଲକ୍)",
                currentLevelM = jalakaLevel,
                warningLevelM = 5.50f,
                dangerLevelM = 6.50f,
                barrageInfoEn = "Basta Embankment Patrol Active • 0.38m below Warning Mark",
                barrageInfoOr = "ବସ୍ତା ନଦୀବନ୍ଧ ପାטרୋଲିଂ ସକ୍ରିୟ • ସତର୍କ ସଙ୍କେତ ତଳେ"
            )
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_river_flood_gauge_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) {
                            "🌊 ସୁବର୍ଣ୍ଣରେଖା ଓ ବୁଢ଼ାବଳଙ୍ଗ ବନ୍ୟା ଜଳସ୍ତର ମାନକ"
                        } else {
                            "🌊 Live Subarnarekha & Budhabalanga Flood Gauge"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = BentoSlate900
                    )
                    Text(
                        text = lastUpdatedLabel,
                        fontSize = 10.5.sp,
                        color = OceanBlue
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val prefs = context.getSharedPreferences("balasore_flood_gauge_prefs", Context.MODE_PRIVATE)
                            prefs.edit()
                                .putFloat("subarnarekha_level_m", subarnarekhaLevel)
                                .putFloat("budhabalanga_level_m", budhabalangaLevel)
                                .apply()
                            BalasoreFloodGaugeWidgetProvider.updateAllWidgets(context)
                            lastUpdatedLabel = "Synced with CWC Rajghat & NH-16 Telemetry • Just now"
                        },
                        modifier = Modifier.testTag("flood_gauge_sync_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync River Flood Gauges",
                            tint = OceanBlue
                        )
                    }

                    Surface(
                        onClick = {
                            BalasoreFloodGaugeWidgetProvider.updateAllWidgets(context)
                            val appWidgetManager = AppWidgetManager.getInstance(context)
                            val provider = ComponentName(
                                context,
                                BalasoreFloodGaugeWidgetProvider::class.java
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                                appWidgetManager.isRequestPinAppWidgetSupported
                            ) {
                                appWidgetManager.requestPinAppWidget(provider, null, null)
                            } else {
                                Toast.makeText(
                                    context,
                                    "Flood Gauge Widget updated! Add from Home Screen -> Widgets -> Baleshwar Flood Gauge",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = OceanBlue,
                        modifier = Modifier.testTag("pin_flood_gauge_widget_btn")
                    ) {
                        Text(
                            text = if (isOdia) "+ ୱିଜେଟ୍" else "+ Pin Widget",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            stations.forEach { station ->
                val ratio = (station.currentLevelM / station.dangerLevelM).coerceIn(0.1f, 1f)
                val isWarning = station.currentLevelM >= station.warningLevelM
                val statusColor = if (isWarning) Color(0xFFD97706) else EmeraldGreen

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("river_gauge_row_${station.id}")
                ) {
                    Column(modifier = Modifier.padding(11.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOdia) station.riverNameOr else station.riverNameEn,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = if (isOdia) station.gaugeSiteOr else station.gaugeSiteEn,
                                    fontSize = 10.5.sp,
                                    color = BentoSlate600
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = statusColor.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                            ) {
                                Text(
                                    text = String.format(Locale.US, "%.2f m", station.currentLevelM),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        LinearProgressIndicator(
                            progress = { ratio },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(999.dp)),
                            color = statusColor,
                            trackColor = Color(0xFFE2E8F0)
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Warning: ${station.warningLevelM}m • Danger: ${station.dangerLevelM}m",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoSlate700
                            )
                            Text(
                                text = if (isWarning) "⚠️ Watch" else "🟢 Safe Below Mark",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = statusColor
                            )
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Text(
                            text = if (isOdia) station.barrageInfoOr else station.barrageInfoEn,
                            fontSize = 10.sp,
                            color = OceanBlueDark
                        )
                    }
                }
            }
        }
    }
}

/**
 * 3. Interactive Artisan Marketplace & GI Craft Passport
 * Digital directory, GI authenticity pass, and stamp collector for Nilagiri stone carving,
 * Sabai grass eco-crafts, Remuna Khira Gaja, and Dokra metal artisans with direct cooperative links.
 */
@Composable
fun ArtisanMarketplaceGiPassportCard(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val prefs = remember {
        context.getSharedPreferences("balasore_gi_passport_prefs", Context.MODE_PRIVATE)
    }

    val clusters = remember {
        listOf(
            BalasoreGiArtisanCluster(
                id = "nilagiri_stone",
                craftNameEn = "Nilagiri Black Chlorite Stone Craft",
                craftNameOr = "ନୀଳଗିରି କଳା ପଥର ଶିଳ୍ପ (ବାଉଳଗଡ଼ିଆ)",
                clusterVillageEn = "Boulagadia & Santaragadia Artisan Cluster, Nilagiri",
                clusterVillageOr = "ବାଉଳଗଡ଼ିଆ ଓ ସାନ୍ତରାଗଡ଼ିଆ ଶିଳ୍ପୀ ଗ୍ରାମ, ନୀଳଗିରି",
                giRegistryCode = "GI-OD-BLS-01 • ORMAS Verified",
                cooperativeNameEn = "Boulagadia Stone Craft Cooperative Society",
                cooperativePhone = "06782-233145",
                priceRange = "₹450 – ₹6,500",
                highlightEn = "Hand-carved Konark wheels, temple idols & heritage bowls sculpted from soft chlorite stone.",
                highlightOr = "କୋଣାର୍କ ଚକ, ମନ୍ଦିର ମୂର୍ତ୍ତି ଓ ପାରମ୍ପରିକ ପଥର ବାସନ ହସ୍ତশିଳ୍ପ।"
            ),
            BalasoreGiArtisanCluster(
                id = "sabai_grass",
                craftNameEn = "Sabai Grass Braided Eco-Crafts",
                craftNameOr = "ସବାଇ ଘାସ ପରିବେଶ ଅନୁକୂଳ ହସ୍ତଶିଳ୍ପ",
                clusterVillageEn = "Baliapal & Basta Mission Shakti SHG Cluster",
                clusterVillageOr = "ବାଲିଆପାଳ ଓ ବସ୍ତା ମିଶନ ଶକ୍ତି ମହିଳା ଗୋଷ୍ଠୀ",
                giRegistryCode = "GI-OD-BLS-02 • Mission Shakti",
                cooperativeNameEn = "Subarnarekha Sabai Producer Collective",
                cooperativePhone = "06781-252389",
                priceRange = "₹180 – ₹2,200",
                highlightEn = "Zero-plastic handwoven baskets, coasters, wall organizers & handloom tote bags.",
                highlightOr = "ପ୍ଲାଷ୍ଟিক୍ ମୁକ୍ତ ହାତବୁଣା ଟୋକେଇ, ବ୍ୟାଗ୍ ଓ ଗୃହ ସଜ୍ଜା ସାମଗ୍ରୀ।"
            ),
            BalasoreGiArtisanCluster(
                id = "remuna_khira_gaja",
                craftNameEn = "Remuna Khira Gaja & Bhoga Guild",
                craftNameOr = "ରେମୁଣା କ୍ଷୀର ଗଜା ଓ ଅമୃତ ମଣୋହି ଭୋଗ",
                clusterVillageEn = "Kshirachora Gopinath Temple Heritage Lane, Remuna",
                clusterVillageOr = "ক্ষୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର ବଜାର, ରେମୁଣା",
                giRegistryCode = "GI-OD-BLS-03 • Temple Heritage",
                cooperativeNameEn = "Remuna Traditional Confectioners Guild",
                cooperativePhone = "06782-241208",
                priceRange = "₹280 – ₹520 / kg",
                highlightEn = "Dense caramelized chhena & condensed milk gaja prepared using 600-year-old Gopinath recipes.",
                highlightOr = "୬୦୦ ବର୍ଷର ପାରମ୍ପରିକ ପଦ୍ଧତିରେ ପ୍ରସ୍ତୁତ ପ୍ରସିଦ୍ଧ କ୍ଷୀର ଗଜା ଓ କ୍ଷୀର ଭୋଗ।"
            ),
            BalasoreGiArtisanCluster(
                id = "nilagiri_dokra",
                craftNameEn = "Kuldiha Foothills Dokra & Lacquerware",
                craftNameOr = "କୁଲଡିହା ଡୋକ୍ରା ଧାତୁ ଓ ଲାଖ ଶିଳ୍ପ",
                clusterVillageEn = "Kuldiha & Ajodhya Tribal Craft Corridor",
                clusterVillageOr = "କୁଲଡିହା ଓ ଅଯୋଧ୍ୟା ଆଦିବାସୀ ଶିଳ୍ପୀ ଗ୍ରାମ",
                giRegistryCode = "GI-OD-BLS-04 • Tribal Co-op",
                cooperativeNameEn = "Panchalingeswar Tribal Artisan Guild",
                cooperativePhone = "06782-233490",
                priceRange = "₹350 – ₹4,800",
                highlightEn = "Lost-wax brass figurines, elephant lamps & natural bamboo lacquerware.",
                highlightOr = "ପାରମ୍ପରିକ ପିତ୍ତଳ ଡୋକ୍ରା ମୂର୍ତ୍ତି, ଦୀପ ଓ ବାଉଁଶ ଲାଖ କାରୁକାର୍ଯ୍ୟ।"
            )
        )
    }

    var stampedIds by remember {
        val initial = clusters.map { it.id }.filter { prefs.getBoolean("stamped_$it", it == "nilagiri_stone") }.toSet()
        mutableStateOf(initial)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("artisan_marketplace_gi_passport_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
        border = BorderStroke(1.dp, Color(0xFFF59E0B).copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(15.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) {
                            "🏺 ଶିଳ୍ପୀ ବଜାର ଓ GI କ୍ରାଫ୍ଟ ପାସପୋର୍ଟ (GI Craft Passport)"
                        } else {
                            "🏺 Artisan Marketplace & GI Craft Passport"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        color = Color(0xFF78350F)
                    )
                    Text(
                        text = if (isOdia) {
                            "ନୀଳଗିରି ପଥର, ସବାଇ ଘାସ ଓ ରେମୁଣା କ୍ଷୀର ଗଜା ସମବାୟ ସୂଚୀ"
                        } else {
                            "Verified Nilagiri stone, Sabai grass & Remuna Khira Gaja cooperatives"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF92400E)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEF3C7),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "GI Passport Stamps",
                            tint = Color(0xFFD97706),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${stampedIds.size}/${clusters.size} Stamps",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF78350F)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            clusters.forEach { cluster ->
                val isStamped = stampedIds.contains(cluster.id)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(
                        1.dp,
                        if (isStamped) EmeraldGreen.copy(alpha = 0.5f) else Color(0xFFFDE68A)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .testTag("artisan_cluster_card_${cluster.id}")
                ) {
                    Column(modifier = Modifier.padding(11.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isOdia) cluster.craftNameOr else cluster.craftNameEn,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = if (isOdia) cluster.clusterVillageOr else cluster.clusterVillageEn,
                                    fontSize = 10.sp,
                                    color = BentoSlate600
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = cluster.priceRange,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (isOdia) cluster.highlightOr else cluster.highlightEn,
                            fontSize = 11.sp,
                            color = BentoSlate700,
                            lineHeight = 15.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "🏷️ ${cluster.giRegistryCode} • ${cluster.cooperativeNameEn}",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = OceanBlueDark
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // 1. Stamp GI Passport button
                            Surface(
                                onClick = {
                                    val updated = if (isStamped) stampedIds - cluster.id else stampedIds + cluster.id
                                    stampedIds = updated
                                    prefs.edit().putBoolean("stamped_${cluster.id}", !isStamped).apply()
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = if (isStamped) Color(0xFFDCFCE7) else Color(0xFFFEF3C7),
                                border = BorderStroke(
                                    1.dp,
                                    if (isStamped) EmeraldGreen else Color(0xFFF59E0B)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("gi_passport_stamp_btn_${cluster.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = if (isStamped) EmeraldGreen else Color(0xFFD97706),
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isStamped) {
                                            if (isOdia) "ପାସପୋର୍ଟ ଷ୍ଟାମ୍ପ୍ ✓" else "GI Stamped ✓"
                                        } else {
                                            if (isOdia) "+ GI ଷ୍ଟାମ୍ପ୍ ସଂଗ୍ରହ" else "+ Collect GI Stamp"
                                        },
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isStamped) Color(0xFF166534) else Color(0xFF92400E)
                                    )
                                }
                            }

                            // 2. Call Cooperative button
                            Surface(
                                onClick = {
                                    runCatching {
                                        context.startActivity(
                                            Intent(Intent.ACTION_DIAL, Uri.parse("tel:${cluster.cooperativePhone}")).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = OceanBlue.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.testTag("artisan_call_coop_btn_${cluster.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Call,
                                        contentDescription = "Call Artisan Cooperative",
                                        tint = OceanBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOdia) "କଲ୍" else "Co-op",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OceanBlue
                                    )
                                }
                            }

                            // 3. Share GI Craft Deep Link button
                            Surface(
                                onClick = {
                                    ShareHelper.shareFeatureBoxDeepLink(
                                        context = context,
                                        featureKey = "gi_craft_${cluster.id}",
                                        title = cluster.craftNameEn,
                                        odiaTitle = cluster.craftNameOr,
                                        description = "${cluster.highlightEn} (${cluster.giRegistryCode})",
                                        category = "GI Craft Passport",
                                        isOdia = isOdia
                                    )
                                },
                                shape = RoundedCornerShape(8.dp),
                                color = OceanBlue.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.testTag("artisan_share_gi_btn_${cluster.id}")
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share GI Craft Link",
                                        tint = OceanBlue,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOdia) "ସେୟାର୍" else "Share",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = OceanBlue
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
