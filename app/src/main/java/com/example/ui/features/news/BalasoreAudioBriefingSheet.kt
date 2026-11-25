package com.example.ui.features.news

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.daily.DailyBalasorePulse
import com.example.data.model.AppLanguage
import com.example.data.model.NewsArticle
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.util.NewsTextToSpeechHelper

data class AudioBriefingSegment(
    val id: String,
    val badgeEn: String,
    val badgeOd: String,
    val titleEn: String,
    val titleOd: String,
    val scriptEn: String,
    val scriptOd: String,
    val accentColor: Color
)

fun buildBalasoreDailyBriefingSegments(
    articles: List<NewsArticle>,
    dailyPulse: DailyBalasorePulse
): List<AudioBriefingSegment> {
    val topHeadlines = articles.take(3)
    val headlineSummaryEn = if (topHeadlines.isNotEmpty()) {
        topHeadlines.mapIndexed { index, art ->
            "Headline ${index + 1}: ${art.title}. ${art.snippet}"
        }.joinToString(" ")
    } else {
        "Balasore district civic, transport, and coastal news feeds are updated and ready."
    }

    val headlineSummaryOd = if (topHeadlines.isNotEmpty()) {
        topHeadlines.mapIndexed { index, art ->
            val t = art.odiaTitle.ifBlank { art.title }
            val s = art.odiaSnippet.ifBlank { art.snippet }
            "ମୁଖ୍ୟ ଖବର ${index + 1}: $t। $s"
        }.joinToString(" ")
    } else {
        "ବାଲେଶ୍ୱର ଜିଲ୍ଲାର ତାଜା ଖବର ଏବଂ ସୂଚନା ପ୍ରସ୍ତୁତ ଅଛି।"
    }

    return listOf(
        AudioBriefingSegment(
            id = "briefing_headlines",
            badgeEn = "TOP HEADLINES",
            badgeOd = "ମୁଖ୍ୟ ଖବର",
            titleEn = "Balasore Today: District Headlines",
            titleOd = "ଆଜିର ବାଲେଶ୍ୱର: ଜିଲ୍ଲା ମୁଖ୍ୟ ଖବର",
            scriptEn = "Welcome to Listen to Balasore Today for ${dailyPulse.formattedDate}. $headlineSummaryEn",
            scriptOd = "ଆଜିର ବାଲେଶ୍ୱର ଅଡିଓ ବୁଲେଟିନ୍‌କୁ ସ୍ୱାଗତ। $headlineSummaryOd",
            accentColor = Color(0xFF0284C7)
        ),
        AudioBriefingSegment(
            id = "briefing_weather_tide",
            badgeEn = "WEATHER & TIDE",
            badgeOd = "ପାଣିପାଗ ଓ ଜୁଆର",
            titleEn = "Balasore Weather & Chandipur Tide Alert",
            titleOd = "ବାଲେଶ୍ୱର ପାଣିପାଗ ଓ ଚାନ୍ଦିପୁର ଜୁଆର ସୂଚନା",
            scriptEn = "Balasore coastal weather update: Safety index is ${dailyPulse.coastalSafetyIndex}. Chandipur Beach safe low-tide walk window is ${dailyPulse.chandipurLowTideWindow}, and next high tide arrives at ${dailyPulse.chandipurNextHighTide}. Please return to shore at least 30 minutes before high tide.",
            scriptOd = "ବାଲେଶ୍ୱର ଉପକୂଳ ପାଣିପାଗ ସୂଚନା: ଚାନ୍ଦିପୁର ବେଳାଭୂମିରେ ନିରାପଦ ଚାଲିବା ସମୟ ${dailyPulse.chandipurLowTideWindow} ଏବଂ ପରବର୍ତ୍ତୀ ଜୁଆର ${dailyPulse.chandipurNextHighTide} ରେ ଆସିବ। ଜୁଆର ଫେରିବାର ୩୦ ମିନିଟ୍ ପୂର୍ବରୁ କୂଳକୁ ଫେରିଆସନ୍ତୁ।",
            accentColor = Color(0xFF0D9488)
        ),
        AudioBriefingSegment(
            id = "briefing_drdo_coastal",
            badgeEn = "DRDO COASTAL ADVISORY",
            badgeOd = "DRDO ଉପକୂଳ ସୂଚନା",
            titleEn = "DRDO ITR Chandipur Coastal Test Advisory",
            titleOd = "DRDO ଚାନ୍ଦିପୁର କ୍ଷେପଣାସ୍ତ୍ର ପରୀକ୍ଷଣ ଉପକୂଳ ସତର୍କତା",
            scriptEn = "DRDO Integrated Test Range Chandipur coastal advisory: Fishing trawlers and boats operating near Launch Complex 3 and Balaramgadi estuary should verify daily maritime exclusion notices before venturing beyond 5 nautical miles. Emergency coastal helpline is 1077 or 112.",
            scriptOd = "DRDO ଚାନ୍ଦିପୁର ଉପକୂଳ ସତର୍କତା: ବଳରାମଗଡ଼ି ଓ ଚାନ୍ଦିପୁର ମତ୍ସ୍ୟଜୀବୀମାନେ ସମୁଦ୍ରକୁ ଯିବା ପୂର୍ବରୁ ଦୈନିକ ପ୍ରତିରକ୍ଷା ସୂଚନା ଯାଞ୍ଚ କରନ୍ତୁ। ଜରୁରୀକାଳୀନ ସହାୟତା ପାଇଁ ୧୦୭୭ କିମ୍ବା ୧୧୨ ରେ କଲ୍ କରନ୍ତୁ।",
            accentColor = Color(0xFFD97706)
        )
    )
}

/**
 * Compact hands-free "Listen to Balasore Today" banner card embedded directly in NewsScreen.
 */
@Composable
fun ListenToBalasoreTodayBanner(
    articles: List<NewsArticle>,
    dailyPulse: DailyBalasorePulse,
    language: AppLanguage,
    onOpenFullBriefing: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsHelper = remember { NewsTextToSpeechHelper.getInstance(context) }
    val isSpeaking by ttsHelper.isSpeaking.collectAsStateWithLifecycle()
    val currentArticleId by ttsHelper.currentArticleId.collectAsStateWithLifecycle()
    val isOdia = language == AppLanguage.ODIA
    val segments = remember(articles, dailyPulse) {
        buildBalasoreDailyBriefingSegments(articles, dailyPulse)
    }
    val isBriefingPlaying = isSpeaking && currentArticleId?.startsWith("briefing_") == true

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        border = BorderStroke(1.2.dp, Color(0xFF38BDF8)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpenFullBriefing() }
            .testTag("listen_to_balasore_today_banner")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF0F172A), Color(0xFF0C4A6E))
                    )
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0284C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isBriefingPlaying) Icons.Default.GraphicEq else Icons.Default.Headphones,
                        contentDescription = "Listen to Balasore Today",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (isOdia) "🎧 ଆଜିର ବାଲେଶ୍ୱର ଶୁଣନ୍ତୁ (ଅଡିଓ ବୁଲେଟିନ୍)" else "🎧 Listen to Balasore Today",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 13.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = if (isOdia) "ମୁଖ୍ୟ ଖବର • ପାଣିପାଗ • DRDO ଉପକୂଳ ସୂଚନା (ଓଡ଼ିଆ / EN)" else "Headlines • Weather Alerts • DRDO Advisories (Odia / EN)",
                        color = Color(0xFFBAE6FD),
                        fontSize = 11.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = {
                    if (isBriefingPlaying) {
                        ttsHelper.stop()
                    } else {
                        val combinedEn = segments.joinToString(" ") { it.scriptEn }
                        val combinedOd = segments.joinToString(" ") { it.scriptOd }
                        ttsHelper.speakArticle(
                            articleId = "briefing_full_daily",
                            title = if (isOdia) "ଆଜିର ବାଲେଶ୍ୱର ଅଡିଓ ବୁଲେଟିନ୍" else "Listen to Balasore Today",
                            content = if (isOdia) combinedOd else combinedEn,
                            language = language
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isBriefingPlaying) Color(0xFFEF4444) else Color(0xFF38BDF8),
                    contentColor = Color(0xFF0F172A)
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("listen_balasore_quick_play_btn")
            ) {
                Icon(
                    imageVector = if (isBriefingPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = if (isBriefingPlaying) "Stop Briefing" else "Play Briefing",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isBriefingPlaying) (if (isOdia) "ବନ୍ଦ" else "Stop") else (if (isOdia) "ଶୁଣନ୍ତୁ" else "Play"),
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 12.sp
                )
            }
        }
    }
}

/**
 * Full hands-free Odia & English Audio Briefing player sheet.
 */
@Composable
fun BalasoreAudioBriefingSheet(
    articles: List<NewsArticle>,
    dailyPulse: DailyBalasorePulse,
    initialLanguage: AppLanguage,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val ttsHelper = remember { NewsTextToSpeechHelper.getInstance(context) }
    val isSpeaking by ttsHelper.isSpeaking.collectAsStateWithLifecycle()
    val isPaused by ttsHelper.isPaused.collectAsStateWithLifecycle()
    val speechSpeed by ttsHelper.speechSpeed.collectAsStateWithLifecycle()
    val statusMessage by ttsHelper.statusMessage.collectAsStateWithLifecycle()

    var voiceLanguage by remember { mutableStateOf(initialLanguage) }
    var activeIndex by remember { mutableIntStateOf(0) }
    val isOdia = voiceLanguage == AppLanguage.ODIA

    val segments = remember(articles, dailyPulse) {
        buildBalasoreDailyBriefingSegments(articles, dailyPulse)
    }
    val activeSegment = segments[activeIndex.coerceIn(0, segments.lastIndex)]

    fun speakSegment(index: Int, lang: AppLanguage = voiceLanguage) {
        val seg = segments[index.coerceIn(0, segments.lastIndex)]
        ttsHelper.speakArticle(
            articleId = seg.id,
            title = if (lang == AppLanguage.ODIA) seg.titleOd else seg.titleEn,
            content = if (lang == AppLanguage.ODIA) seg.scriptOd else seg.scriptEn,
            language = lang
        )
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("balasore_audio_briefing_sheet")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOdia) "🎧 ଆଜିର ବାଲେଶ୍ୱର ଅଡିଓ ବୁଲେଟିନ୍" else "🎧 Listen to Balasore Today",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = statusMessage ?: (if (isOdia) "ଓଡ଼ିଆ ଓ ଇଂରାଜୀ ଭଏସ୍ ନ୍ୟୁଜ୍ ରିଡର୍" else "Hands-free Odia & English Audio Briefing"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = {
                    ttsHelper.stop()
                    onClose()
                },
                modifier = Modifier.testTag("audio_briefing_close_btn")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close Audio Briefing")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Language & Speed Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = voiceLanguage == AppLanguage.ODIA,
                    onClick = {
                        voiceLanguage = AppLanguage.ODIA
                        if (isSpeaking) speakSegment(activeIndex, AppLanguage.ODIA)
                    },
                    label = { Text("ଓଡ଼ିଆ Voice", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OceanBlue,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("audio_briefing_lang_odia")
                )
                FilterChip(
                    selected = voiceLanguage == AppLanguage.ENGLISH,
                    onClick = {
                        voiceLanguage = AppLanguage.ENGLISH
                        if (isSpeaking) speakSegment(activeIndex, AppLanguage.ENGLISH)
                    },
                    label = { Text("English", fontSize = 11.5.sp, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OceanBlue,
                        selectedLabelColor = Color.White
                    ),
                    modifier = Modifier.testTag("audio_briefing_lang_en")
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(0.85f to "0.85x", 1.0f to "1x", 1.25f to "1.25x").forEach { (rate, label) ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (speechSpeed == rate) OceanBlueDark else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { ttsHelper.setSpeechSpeed(rate) }
                    ) {
                        Text(
                            text = label,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (speechSpeed == rate) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Now Playing Deck
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
            border = BorderStroke(1.2.dp, activeSegment.accentColor),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = activeSegment.accentColor
                ) {
                    Text(
                        text = if (isOdia) activeSegment.badgeOd else activeSegment.badgeEn,
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (isOdia) activeSegment.titleOd else activeSegment.titleEn,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isOdia) activeSegment.scriptOd else activeSegment.scriptEn,
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            val prev = if (activeIndex > 0) activeIndex - 1 else segments.lastIndex
                            activeIndex = prev
                            speakSegment(prev)
                        },
                        modifier = Modifier.testTag("audio_briefing_prev_btn")
                    ) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous Segment", tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Surface(
                        shape = CircleShape,
                        color = activeSegment.accentColor,
                        modifier = Modifier
                            .size(54.dp)
                            .clickable {
                                when {
                                    isSpeaking -> ttsHelper.pause()
                                    isPaused -> ttsHelper.resume()
                                    else -> speakSegment(activeIndex)
                                }
                            }
                            .testTag("audio_briefing_play_pause_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isSpeaking) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    IconButton(
                        onClick = {
                            val next = (activeIndex + 1) % segments.size
                            activeIndex = next
                            speakSegment(next)
                        },
                        modifier = Modifier.testTag("audio_briefing_next_btn")
                    ) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next Segment", tint = Color.White, modifier = Modifier.size(28.dp))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { ttsHelper.stop() },
                        modifier = Modifier.testTag("audio_briefing_stop_btn")
                    ) {
                        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = Color(0xFFF87171), modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isOdia) "ବୁଲେଟିନ୍ ବିଭାଗଗୁଡ଼ିକ" else "Briefing Playlist",
            fontWeight = FontWeight.Bold,
            fontSize = 13.5.sp,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            itemsIndexed(segments) { index, segment ->
                val isSelected = index == activeIndex
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) segment.accentColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    ),
                    border = BorderStroke(1.dp, if (isSelected) segment.accentColor else MaterialTheme.colorScheme.outlineVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            activeIndex = index
                            speakSegment(index)
                        }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) segment.badgeOd else segment.badgeEn,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = segment.accentColor
                            )
                            Text(
                                text = if (isOdia) segment.titleOd else segment.titleEn,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Icon(
                            imageVector = if (isSelected && isSpeaking) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                            contentDescription = "Play segment",
                            tint = segment.accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
        }
    }
}
