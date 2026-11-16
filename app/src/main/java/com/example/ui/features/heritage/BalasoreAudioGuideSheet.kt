package com.example.ui.features.heritage

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.data.model.AppLanguage
import com.example.data.model.HistoricalLandmarkAudioGuide
import com.example.data.model.HistoricalLandmarksCatalog
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate300
import com.example.ui.theme.BentoSlate400
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.util.TourismAudioGuideManager

/**
 * Interactive Audio Guide Bottom Sheet for Balasore Tourism module.
 * Provides spoken historical narrations for landmarks utilizing Android's Text-To-Speech engine.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BalasoreAudioGuideSheet(
    onClose: () -> Unit,
    initialLandmarkId: String? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val audioManager = remember { TourismAudioGuideManager.getInstance(context) }

    val isPlaying by audioManager.isPlaying.collectAsState()
    val isPaused by audioManager.isPaused.collectAsState()
    val currentLandmarkId by audioManager.currentLandmarkId.collectAsState()
    val currentLanguage by audioManager.currentLanguage.collectAsState()
    val currentSpeed by audioManager.speechRate.collectAsState()
    val elapsedSeconds by audioManager.elapsedSeconds.collectAsState()
    val totalSeconds by audioManager.totalEstimatedSeconds.collectAsState()
    val statusMessage by audioManager.statusMessage.collectAsState()

    val landmarks = remember { HistoricalLandmarksCatalog.landmarks }
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Ancient Temple", "Coastal Marvel", "Freedom & Defense", "Literary Heritage", "Eco-Heritage")

    // If initialLandmarkId was specified and not playing, preselect/play
    remember(initialLandmarkId) {
        if (!initialLandmarkId.isNullOrBlank() && currentLandmarkId == null) {
            val matching = HistoricalLandmarksCatalog.findById(initialLandmarkId)
            if (matching != null) {
                audioManager.playLandmark(matching, currentLanguage)
            }
        }
    }

    val activeLandmark = remember(currentLandmarkId) {
        currentLandmarkId?.let { id -> HistoricalLandmarksCatalog.findById(id) }
            ?: landmarks.first()
    }

    val filteredLandmarks = remember(selectedCategory) {
        if (selectedCategory == "All") landmarks
        else landmarks.filter { it.category.contains(selectedCategory, ignoreCase = true) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .testTag("balasore_audio_guide_sheet")
    ) {
        // Sheet Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(OceanBlue, OceanBlueDark))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Headphones,
                        contentDescription = "Audio Guide",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Column {
                    Text(
                        text = "Historical Audio Guide",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900
                        )
                    )
                    Text(
                        text = "ବାଲେଶ୍ୱର ଐତିହ୍ୟ ଅଡିଓ ଗାଇଡ୍ • TTS Engine",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = BentoSlate500
                        )
                    )
                }
            }

            IconButton(
                onClick = {
                    audioManager.stop()
                    onClose()
                },
                modifier = Modifier
                    .size(36.dp)
                    .testTag("close_audio_guide_sheet_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Audio Guide",
                    tint = BentoSlate600
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Language Selection Chips
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, BentoSlate200)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Narration Voice Language / ଭାଷା ଚୟନ",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = BentoSlate700
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val languages = listOf(
                                Triple(AppLanguage.ENGLISH, "English (Indian)", "en"),
                                Triple(AppLanguage.ODIA, "ଓଡ଼ିଆ (Odia)", "or"),
                                Triple(AppLanguage.HINDI, "हिंदी (Hindi)", "hi")
                            )

                            languages.forEach { (lang, label, code) ->
                                val isSelected = currentLanguage == lang
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { audioManager.setLanguage(lang) },
                                    label = {
                                        Text(
                                            text = label,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OceanBlue,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag("audio_guide_lang_chip_$code")
                                )
                            }
                        }
                    }
                }
            }

            // Active Audio Player Card
            item {
                ActiveAudioPlayerCard(
                    landmark = activeLandmark,
                    isPlaying = isPlaying,
                    isPaused = isPaused,
                    elapsedSeconds = elapsedSeconds,
                    totalSeconds = totalSeconds,
                    currentSpeed = currentSpeed,
                    currentLanguage = currentLanguage,
                    statusMessage = statusMessage,
                    onTogglePlay = { audioManager.togglePlayPause(activeLandmark, currentLanguage) },
                    onStop = { audioManager.stop() },
                    onReplay = { audioManager.playLandmark(activeLandmark, currentLanguage) },
                    onChangeSpeed = { nextSpeed -> audioManager.setSpeechRate(nextSpeed) },
                    onShareScript = {
                        val script = when (currentLanguage) {
                            AppLanguage.ODIA -> activeLandmark.audioScriptOr
                            AppLanguage.HINDI -> activeLandmark.audioScriptHi
                            else -> activeLandmark.audioScriptEn
                        }
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Audio Guide: ${activeLandmark.name}")
                            putExtra(Intent.EXTRA_TEXT, "${activeLandmark.name} - Historical Audio Guide:\n\n$script\n\nShared via Baleshwar 360 App")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Audio Guide Narration"))
                    }
                )
            }

            // Category Filter Row
            item {
                Column {
                    Text(
                        text = "Historical Categories / ବର୍ଗ",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate800
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(categories) { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OceanBlue.copy(alpha = 0.15f),
                                    selectedLabelColor = OceanBlue
                                ),
                                modifier = Modifier.testTag("audio_cat_chip_$cat")
                            )
                        }
                    }
                }
            }

            // Landmark Audio List
            items(filteredLandmarks, key = { it.id }) { landmark ->
                LandmarkAudioItemRow(
                    landmark = landmark,
                    isCurrentlyPlaying = currentLandmarkId == landmark.id && isPlaying,
                    isCurrentSelected = currentLandmarkId == landmark.id,
                    language = currentLanguage,
                    onPlay = { audioManager.playLandmark(landmark, currentLanguage) },
                    onPause = { audioManager.pause() }
                )
            }
        }
    }
}

/**
 * Top active player card with waveform animation, progress bar, play/pause controls, and transcript.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActiveAudioPlayerCard(
    landmark: HistoricalLandmarkAudioGuide,
    isPlaying: Boolean,
    isPaused: Boolean,
    elapsedSeconds: Int,
    totalSeconds: Int,
    currentSpeed: Float,
    currentLanguage: AppLanguage,
    statusMessage: String?,
    onTogglePlay: () -> Unit,
    onStop: () -> Unit,
    onReplay: () -> Unit,
    onChangeSpeed: (Float) -> Unit,
    onShareScript: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "audioWave")
    val waveScale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "waveScale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_audio_player_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Era & Category Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF38BDF8).copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = landmark.era,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFF38BDF8),
                            fontWeight = FontWeight.SemiBold
                        ),
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                // Audio Status Badge
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (isPlaying) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Wave",
                            tint = EmeraldGreen,
                            modifier = Modifier
                                .size((18 * waveScale).dp)
                        )
                        Text(
                            text = "SPEAKING",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        )
                    } else if (isPaused) {
                        Text(
                            text = "PAUSED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = AmberGold
                            )
                        )
                    } else {
                        Text(
                            text = "READY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BentoSlate400
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Landmark Title
            Text(
                text = if (currentLanguage == AppLanguage.ODIA) landmark.odiaName else landmark.name,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            )

            if (currentLanguage != AppLanguage.ODIA) {
                Text(
                    text = landmark.odiaName,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BentoSlate400
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Narration Excerpt / Transcript Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E293B),
                border = BorderStroke(1.dp, BentoSlate700)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spoken Narrative Transcript",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = Color(0xFF94A3B8),
                                fontWeight = FontWeight.SemiBold
                            )
                        )
                        IconButton(
                            onClick = onShareScript,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share Transcript",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = when (currentLanguage) {
                            AppLanguage.ODIA -> landmark.audioScriptOr
                            AppLanguage.HINDI -> landmark.audioScriptHi
                            else -> landmark.audioScriptEn
                        },
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = Color(0xFFE2E8F0),
                            lineHeight = 18.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Progress Slider Bar
            val progressFraction = if (totalSeconds > 0) {
                (elapsedSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
            } else 0f

            LinearProgressIndicator(
                progress = { progressFraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = Color(0xFF38BDF8),
                trackColor = BentoSlate800
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Time & Status Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${formatTime(elapsedSeconds)} / ${formatTime(totalSeconds)}",
                    style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate400)
                )
                Text(
                    text = statusMessage ?: "Text-to-Speech active",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.Medium
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Playback Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Speed Toggle Button
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF1E293B))
                        .clickable {
                            val next = when (currentSpeed) {
                                0.8f -> 1.0f
                                1.0f -> 1.25f
                                else -> 0.8f
                            }
                            onChangeSpeed(next)
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("audio_speed_btn"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = "Speed",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${currentSpeed}x",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                }

                // Center Play/Pause / Replay Buttons
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Replay Button
                    IconButton(
                        onClick = onReplay,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay,
                            contentDescription = "Replay",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Main Big Play/Pause Action
                    Button(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .height(48.dp)
                            .testTag("active_audio_play_pause_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isPlaying) "PAUSE" else "LISTEN NOW",
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Stop Button
                    IconButton(
                        onClick = onStop,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1E293B))
                            .testTag("active_audio_stop_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Stop,
                            contentDescription = "Stop",
                            tint = Color(0xFFF43F5E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single landmark row item in the audio guide catalog.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LandmarkAudioItemRow(
    landmark: HistoricalLandmarkAudioGuide,
    isCurrentlyPlaying: Boolean,
    isCurrentSelected: Boolean,
    language: AppLanguage,
    onPlay: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("landmark_audio_item_${landmark.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentSelected) Color(0xFFF0F9FF) else Color.White
        ),
        border = BorderStroke(
            1.dp,
            if (isCurrentSelected) OceanBlue else BentoSlate200
        )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.ODIA) landmark.odiaName else landmark.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900
                        )
                    )
                    Text(
                        text = "${landmark.category} • ${landmark.era}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = BentoSlate500
                        )
                    )
                }

                // Play / Pause Circle Action Button
                IconButton(
                    onClick = {
                        if (isCurrentlyPlaying) onPause() else onPlay()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(if (isCurrentlyPlaying) OceanBlue else Color(0xFFE0F2FE))
                        .testTag("play_landmark_btn_${landmark.id}")
                ) {
                    Icon(
                        imageVector = if (isCurrentlyPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isCurrentlyPlaying) "Pause" else "Play Audio Clip",
                        tint = if (isCurrentlyPlaying) Color.White else OceanBlue,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Key Highlights Flow Row
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Duration Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = BentoSlate100
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = BentoSlate600,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${landmark.durationSeconds}s Audio",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BentoSlate700,
                                fontWeight = FontWeight.Medium
                            )
                        )
                    }
                }

                landmark.keyHighlights.take(3).forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, BentoSlate200)
                    ) {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = BentoSlate600
                            ),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatTime(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return String.format("%02d:%02d", m, s)
}
