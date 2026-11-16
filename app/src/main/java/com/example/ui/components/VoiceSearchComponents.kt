package com.example.ui.components

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * Supported voice recognition languages for Baleshwar 360 Voice-to-Text Search.
 */
enum class VoiceSearchLanguage(
    val localeCode: String,
    val labelEn: String,
    val labelOdia: String,
    val shortBadge: String,
    val recognizerPrompt: String
) {
    ENGLISH(
        localeCode = "en-IN",
        labelEn = "English (India)",
        labelOdia = "ଇଂରାଜୀ (English)",
        shortBadge = "EN",
        recognizerPrompt = "Speak in English to search Baleshwar 360..."
    ),
    ODIA(
        localeCode = "or-IN",
        labelEn = "Odia (ଓଡ଼ିଆ)",
        labelOdia = "ଓଡ଼ିଆ (Odia)",
        shortBadge = "ଓଡ଼ିଆ",
        recognizerPrompt = "ବାଲେଶ୍ୱର ୩୬୦ ଖୋଜିବା ପାଇଁ ଓଡ଼ିଆରେ କୁହନ୍ତୁ (Speak in Odia)..."
    )
}

enum class VoiceSearchSection {
    NEWS,
    TOURISM
}

data class VoicePhraseSuggestion(
    val phrase: String,
    val subtitle: String,
    val language: VoiceSearchLanguage
)

val NEWS_VOICE_SUGGESTIONS = listOf(
    VoicePhraseSuggestion("Chandipur missile test", "Defense & DRDO updates", VoiceSearchLanguage.ENGLISH),
    VoicePhraseSuggestion("Subarnarekha river alert", "Weather & flood news", VoiceSearchLanguage.ENGLISH),
    VoicePhraseSuggestion("Fakir Mohan University", "Education & campus news", VoiceSearchLanguage.ENGLISH),
    VoicePhraseSuggestion("Balasore railway station", "Civic & transit news", VoiceSearchLanguage.ENGLISH),
    VoicePhraseSuggestion("ଚାନ୍ଦିପୁର", "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ଓ ପ୍ରତିରକ୍ଷା ଖବର", VoiceSearchLanguage.ODIA),
    VoicePhraseSuggestion("ବାଲେଶ୍ୱର", "ବାଲେଶ୍ୱର ଜିଲ୍ଲା ମୁଖ୍ୟ ଖବର", VoiceSearchLanguage.ODIA),
    VoicePhraseSuggestion("ସୁବର୍ଣ୍ଣରେଖା", "ବନ୍ୟା ଓ ପାଣିପାଗ ସୂଚନା", VoiceSearchLanguage.ODIA),
    VoicePhraseSuggestion("ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର", "ପର୍ଯ୍ୟଟନ ଓ ସଂସ୍କୃତି ଖବର", VoiceSearchLanguage.ODIA)
)

val TOURISM_VOICE_SUGGESTIONS = listOf(
    VoicePhraseSuggestion("Chandipur Beach", "Vanishing sea & coastal retreat", VoiceSearchLanguage.ENGLISH),
    VoicePhraseSuggestion("Panchalingeswar", "Hilltop shrine & perennial stream", VoiceSearchLanguage.ENGLISH),
    VoicePhraseSuggestion("Khirachora Gopinath", "Historic Remuna temple", VoiceSearchLanguage.ENGLISH),
    VoicePhraseSuggestion("Kuldiha Wildlife", "Elephant reserve & eco-tourism", VoiceSearchLanguage.ENGLISH),
    VoicePhraseSuggestion("Talasari Beach", "Red crabs & Subarnarekha delta", VoiceSearchLanguage.ENGLISH),
    VoicePhraseSuggestion("ଚାନ୍ଦିପୁର ବେଳାଭୂମି", "Chandipur Beach (Odia)", VoiceSearchLanguage.ODIA),
    VoicePhraseSuggestion("ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର", "Panchalingeswar Temple (Odia)", VoiceSearchLanguage.ODIA),
    VoicePhraseSuggestion("କ୍ଷୀରଚୋରା ଗୋପୀନାଥ", "Khirachora Gopinath (Odia)", VoiceSearchLanguage.ODIA),
    VoicePhraseSuggestion("କୁଲଡିହା ଅଭୟାରଣ୍ୟ", "Kuldiha Sanctuary (Odia)", VoiceSearchLanguage.ODIA),
    VoicePhraseSuggestion("ତାଳସାରୀ ବେଳାଭୂମି", "Talasari Beach (Odia)", VoiceSearchLanguage.ODIA)
)

private val BILINGUAL_KEYWORD_EQUIVALENTS: List<Pair<String, String>> = listOf(
    "ଚାନ୍ଦିପୁର" to "chandipur",
    "ବେଳାଭୂମି" to "beach",
    "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର" to "panchalingeswar",
    "କ୍ଷୀରଚୋରା" to "khirachora",
    "ଗୋପୀନାଥ" to "gopinath",
    "ରେମୁଣା" to "remuna",
    "କୁଲଡିହା" to "kuldiha",
    "ଅଭୟାରଣ୍ୟ" to "sanctuary",
    "ତାଳସାରୀ" to "talasari",
    "ସୁବର୍ଣ୍ଣରେଖା" to "subarnarekha",
    "ବୁଢାବଳଙ୍ଗ" to "budhabalanga",
    "ବାଲେଶ୍ୱର" to "balasore",
    "ବାଲେଶ୍ୱର" to "baleshwar",
    "ନୀଳଗିରି" to "nilgiri",
    "ରାଇବଣିଆ" to "raibania",
    "ଭୂଷଣ୍ଡେଶ୍ୱର" to "bhusandeswar",
    "ଇମାମୀ" to "emami",
    "ଜଗନ୍ନାଥ" to "jagannath",
    "ମନ୍ଦିର" to "temple",
    "ପ୍ରତିରକ୍ଷା" to "defense",
    "କ୍ଷେପଣାସ୍ତ୍ର" to "missile",
    "ବାତ୍ୟା" to "cyclone",
    "ବନ୍ୟା" to "flood",
    "ପାଣିପାଗ" to "weather",
    "ଜୁଆର" to "tide",
    "ଖବର" to "news",
    "ପର୍ଯ୍ୟଟନ" to "tourism"
)

/**
 * Matches a search query spoken or typed in either English or Odia against target text fields.
 */
fun matchesBilingualSearchQuery(query: String, vararg fields: String): Boolean {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return true

    val combinedText = fields.joinToString(" ").lowercase()
    val normalizedQuery = trimmed.lowercase()

    if (combinedText.contains(normalizedQuery)) return true

    // Expand bilingual equivalents (Odia <-> English) and check individual tokens
    val tokens = normalizedQuery
        .split(" ", ",", "•", "-", "—")
        .map { it.trim() }
        .filter { it.length >= 2 }

    val expandedTerms = mutableSetOf<String>()
    expandedTerms.addAll(tokens)

    BILINGUAL_KEYWORD_EQUIVALENTS.forEach { (odiaTerm, enTerm) ->
        if (normalizedQuery.contains(odiaTerm)) {
            expandedTerms.add(enTerm)
        }
        if (normalizedQuery.contains(enTerm)) {
            expandedTerms.add(odiaTerm)
        }
    }

    return expandedTerms.any { term -> term.isNotEmpty() && combinedText.contains(term) }
}

/**
 * Reusable Voice-to-Text Search Action Row with:
 * - Language switcher chip (English `en-IN` / Odia `or-IN`)
 * - Microphone button that launches Android Speech Recognizer (`RecognizerIntent.ACTION_RECOGNIZE_SPEECH`)
 *   and gracefully opens an interactive Bilingual Voice Assistant dialog if hardware mic / speech activity
 *   is unavailable or when the user wants guided bilingual voice phrases.
 */
@Composable
fun VoiceSearchControls(
    section: VoiceSearchSection,
    isOdiaUi: Boolean,
    selectedVoiceLang: VoiceSearchLanguage,
    onVoiceLangChanged: (VoiceSearchLanguage) -> Unit,
    onVoiceResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    tagPrefix: String = if (section == VoiceSearchSection.NEWS) "news" else "tourism"
) {
    var showVoiceAssistantDialog by rememberSaveable { mutableStateOf(false) }
    var systemRecognizerUnavailable by rememberSaveable { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenMatches = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val bestMatch = spokenMatches?.firstOrNull()?.trim().orEmpty()
            if (bestMatch.isNotEmpty()) {
                onVoiceResult(bestMatch)
            }
        }
    }

    val triggerSystemOrAssistantVoice: (VoiceSearchLanguage) -> Unit = { lang ->
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, lang.localeCode)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, lang.localeCode)
            putExtra(RecognizerIntent.EXTRA_ONLY_RETURN_LANGUAGE_PREFERENCE, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, lang.recognizerPrompt)
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
        }
        try {
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            systemRecognizerUnavailable = true
            showVoiceAssistantDialog = true
        } catch (_: Exception) {
            systemRecognizerUnavailable = true
            showVoiceAssistantDialog = true
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Language pill toggle (EN <-> ଓଡ଼ିଆ)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.85f),
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .clickable {
                    val next = if (selectedVoiceLang == VoiceSearchLanguage.ENGLISH) {
                        VoiceSearchLanguage.ODIA
                    } else {
                        VoiceSearchLanguage.ENGLISH
                    }
                    onVoiceLangChanged(next)
                }
                .testTag("${tagPrefix}_voice_lang_toggle")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Translate,
                    contentDescription = "Switch Voice Search Language (English or Odia)",
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = selectedVoiceLang.shortBadge,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        // Primary Microphone IconButton -> opens bilingual voice search modal with direct mic trigger + instant voice phrases
        IconButton(
            onClick = {
                showVoiceAssistantDialog = true
            },
            modifier = Modifier
                .size(48.dp)
                .testTag("${tagPrefix}_voice_search_btn")
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = if (isOdiaUi) {
                    "ଭଏସ୍ ସର୍ଚ୍ଚ (ଇଂରାଜୀ କିମ୍ବା ଓଡ଼ିଆରେ କୁହନ୍ତୁ)"
                } else {
                    "Voice Search in English or Odia"
                },
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (showVoiceAssistantDialog) {
        BilingualVoiceSearchDialog(
            section = section,
            isOdiaUi = isOdiaUi,
            selectedLanguage = selectedVoiceLang,
            systemRecognizerUnavailable = systemRecognizerUnavailable,
            onLanguageSelected = onVoiceLangChanged,
            onLaunchSystemRecognizer = { lang ->
                triggerSystemOrAssistantVoice(lang)
            },
            onPhraseSpoken = { recognizedText ->
                onVoiceResult(recognizedText)
                showVoiceAssistantDialog = false
            },
            onDismiss = { showVoiceAssistantDialog = false },
            tagPrefix = tagPrefix
        )
    }
}

/**
 * Interactive Bilingual Voice-to-Text Dialog supporting both English (en-IN) and Odia (or-IN).
 * Allows launching the native Android SpeechRecognizer OR selecting/speaking instant English & Odia voice phrases.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BilingualVoiceSearchDialog(
    section: VoiceSearchSection,
    isOdiaUi: Boolean,
    selectedLanguage: VoiceSearchLanguage,
    systemRecognizerUnavailable: Boolean,
    onLanguageSelected: (VoiceSearchLanguage) -> Unit,
    onLaunchSystemRecognizer: (VoiceSearchLanguage) -> Unit,
    onPhraseSpoken: (String) -> Unit,
    onDismiss: () -> Unit,
    tagPrefix: String
) {
    var liveTranscript by rememberSaveable { mutableStateOf("") }
    var isListeningPulse by rememberSaveable { mutableStateOf(true) }

    val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse_scale"
    )

    val allSuggestions = remember(section) {
        if (section == VoiceSearchSection.NEWS) NEWS_VOICE_SUGGESTIONS else TOURISM_VOICE_SUGGESTIONS
    }
    val filteredSuggestions = remember(allSuggestions, selectedLanguage) {
        allSuggestions.filter { it.language == selectedLanguage }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .size(42.dp)
                        .scale(if (isListeningPulse) pulseScale else 1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice to text active",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Column {
                    Text(
                        text = if (isOdiaUi) {
                            "ଭଏସ୍-ଟୁ-ଟେକ୍ସଟ୍ ସର୍ଚ୍ଚ (Voice Search)"
                        } else {
                            "Voice-to-Text Search"
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (section == VoiceSearchSection.NEWS) {
                            "Baleshwar 360 News • Speak in English or ଓଡ଼ିଆ"
                        } else {
                            "Baleshwar 360 Tourism • Speak in English or ଓଡ଼ିଆ"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Language Selector Row: English (en-IN) vs Odia (or-IN)
                Text(
                    text = if (isOdiaUi) "ଭାଷା ବାଛନ୍ତୁ (Select Speech Language):" else "Select Speech Recognition Language:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VoiceSearchLanguage.entries.forEach { lang ->
                        val isSelected = selectedLanguage == lang
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                onLanguageSelected(lang)
                                isListeningPulse = true
                            },
                            label = {
                                Text(
                                    text = if (lang == VoiceSearchLanguage.ENGLISH) {
                                        "English (en-IN)"
                                    } else {
                                        "ଓଡ଼ିଆ • Odia (or-IN)"
                                    },
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.Language,
                                    contentDescription = lang.labelEn,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("${tagPrefix}_voice_lang_${lang.name.lowercase()}")
                        )
                    }
                }

                // Launch Native Android Microphone Card
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GraphicEq,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (selectedLanguage == VoiceSearchLanguage.ODIA) {
                                        "ଓଡ଼ିଆ ସ୍ୱର ଚିହ୍ନଟ (or-IN)"
                                    } else {
                                        "English Speech Recognizer (en-IN)"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (systemRecognizerUnavailable) {
                                    "Device speech service unavailable on emulator — use instant voice dictation below."
                                } else {
                                    selectedLanguage.recognizerPrompt
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = { onLaunchSystemRecognizer(selectedLanguage) },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("${tagPrefix}_launch_system_mic_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Start device microphone recognition",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }

                // Voice Transcript Box (editable or populated by spoken phrase)
                OutlinedTextField(
                    value = liveTranscript,
                    onValueChange = { liveTranscript = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("${tagPrefix}_voice_transcript_input"),
                    label = {
                        Text(
                            if (selectedLanguage == VoiceSearchLanguage.ODIA) {
                                "କଥିତ ଓଡ଼ିଆ ବାక୍ୟ (Spoken Odia Text)"
                            } else {
                                "Spoken Search Transcript (English / Odia)"
                            }
                        )
                    },
                    placeholder = {
                        Text(
                            if (selectedLanguage == VoiceSearchLanguage.ODIA) {
                                "ଉଦାହରଣ: ଚାନ୍ଦିପୁର ବେଳାଭୂମି..."
                            } else {
                                "e.g., Chandipur Beach, Panchalingeswar..."
                            }
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = {
                        if (liveTranscript.isNotEmpty()) {
                            IconButton(
                                onClick = { liveTranscript = "" },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear spoken transcript"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp)
                )

                // Quick Voice Presets in the Selected Language (English or Odia)
                Text(
                    text = if (selectedLanguage == VoiceSearchLanguage.ODIA) {
                        "ତୁରନ୍ତ ଓଡ଼ିଆ ଭଏସ୍ ସର୍ଚ୍ଚ (Tap to Speak in Odia):"
                    } else {
                        "Tap a Voice Phrase to Speak (${selectedLanguage.labelEn}):"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    filteredSuggestions.forEachIndexed { idx, suggestion ->
                        AssistChip(
                            onClick = {
                                liveTranscript = suggestion.phrase
                                onPhraseSpoken(suggestion.phrase)
                            },
                            label = {
                                Column(modifier = Modifier.padding(vertical = 2.dp)) {
                                    Text(
                                        text = "🎙️ \"${suggestion.phrase}\"",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = suggestion.subtitle,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            },
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            ),
                            modifier = Modifier.testTag("${tagPrefix}_voice_preset_${selectedLanguage.shortBadge.lowercase()}_$idx")
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val queryToApply = liveTranscript.trim().ifEmpty {
                        filteredSuggestions.firstOrNull()?.phrase.orEmpty()
                    }
                    if (queryToApply.isNotEmpty()) {
                        onPhraseSpoken(queryToApply)
                    } else {
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("${tagPrefix}_apply_voice_search_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isOdiaUi) "ସର୍ଚ୍ଚ କରନ୍ତୁ" else "Search Now")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("${tagPrefix}_dismiss_voice_search_btn")
            ) {
                Text(if (isOdiaUi) "ବନ୍ଦ କରନ୍ତୁ" else "Cancel")
            }
        }
    )
}
