package com.example.ui.features.civic

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppLanguage
import com.example.util.BalasoreHapticHelper
import java.util.Locale

data class CivicEyeCategoryOption(
    val canonicalValue: String,
    val labelEn: String,
    val labelOr: String,
    val subtitleEn: String,
    val icon: ImageVector
)

val CIVIC_EYE_CATEGORIES: List<CivicEyeCategoryOption> = listOf(
    CivicEyeCategoryOption(
        canonicalValue = "Pothole",
        labelEn = "Pothole",
        labelOr = "ରାସ୍ତା ଖାଲ (Pothole)",
        subtitleEn = "Road craters, broken asphalt & pavement hazards",
        icon = Icons.Default.ReportProblem
    ),
    CivicEyeCategoryOption(
        canonicalValue = "Water Supply",
        labelEn = "Water Supply",
        labelOr = "ଜଳ ଯୋଗାଣ (Water Supply)",
        subtitleEn = "WATCO pipeline leak, low pressure or supply outage",
        icon = Icons.Default.WaterDrop
    ),
    CivicEyeCategoryOption(
        canonicalValue = "Drainage",
        labelEn = "Drainage",
        labelOr = "ଡ୍ରେନେଜ୍ ଓ ଜଳବନ୍ଦୀ (Drainage)",
        subtitleEn = "Blocked storm drains, sewage overflow or waterlogging",
        icon = Icons.Default.Waves
    ),
    CivicEyeCategoryOption(
        canonicalValue = "Garbage",
        labelEn = "Garbage",
        labelOr = "ଆବର୍ଜନା ଓ ସଫେଇ (Garbage)",
        subtitleEn = "Uncollected solid waste, open dumping or sanitation",
        icon = Icons.Default.DeleteOutline
    ),
    CivicEyeCategoryOption(
        canonicalValue = "Street Light",
        labelEn = "Street Light",
        labelOr = "ଷ୍ଟ୍ରିଟ୍ ଲାଇଟ୍ (Street Light)",
        subtitleEn = "Non-working LED poles, dark intersections or TPNODL faults",
        icon = Icons.Default.Lightbulb
    ),
    CivicEyeCategoryOption(
        canonicalValue = "Damaged Road",
        labelEn = "Damaged Road",
        labelOr = "କ୍ଷତିଗ୍ରସ୍ତ ରାସ୍ତା (Damaged Road)",
        subtitleEn = "Collapsed culvert, broken curb or uneven bridge approach",
        icon = Icons.Default.Build
    )
)

/**
 * Normalizes any legacy or free-text Civic Eye category into a clean canonical label
 * stored in Firestore `user_reports.category` (validated as 1..50 chars by firestore.rules).
 */
fun normalizeCivicEyeCategory(raw: String): String {
    val value = raw.trim()
    return when {
        value.contains("Pothole", ignoreCase = true) -> "Pothole"
        value.contains("Drain", ignoreCase = true) || value.contains("Waterlog", ignoreCase = true) -> "Drainage"
        value.contains("Garbage", ignoreCase = true) || value.contains("Waste", ignoreCase = true) || value.contains("Sanitation", ignoreCase = true) -> "Garbage"
        value.contains("Light", ignoreCase = true) || value.contains("Electric", ignoreCase = true) -> "Street Light"
        value.contains("Water", ignoreCase = true) || value.contains("Supply", ignoreCase = true) || value.contains("Leak", ignoreCase = true) -> "Water Supply"
        value.contains("Road", ignoreCase = true) || value.contains("Culvert", ignoreCase = true) -> "Damaged Road"
        value.isNotBlank() -> value.take(50)
        else -> "Pothole"
    }
}

/**
 * Material 3 ExposedDropdownMenuBox for selecting a Civic Eye incident category
 * (Pothole, Water Supply, Drainage, Garbage, Street Light, Damaged Road).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivicEyeCategoryDropdown(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    var expanded by remember { mutableStateOf(false) }

    val normalized = normalizeCivicEyeCategory(selectedCategory)
    val selectedOption = CIVIC_EYE_CATEGORIES.firstOrNull {
        it.canonicalValue.equals(normalized, ignoreCase = true)
    } ?: CIVIC_EYE_CATEGORIES.first()

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = if (isOdia) "୧. ସମସ୍ୟା ବିଭାଗ ବାଛନ୍ତୁ (Category)" else stringResource(R.string.civic_eye_category_section_title),
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(6.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = if (isOdia) selectedOption.labelOr else selectedOption.labelEn,
                onValueChange = {},
                readOnly = true,
                label = {
                    Text(
                        if (isOdia) "ସମସ୍ୟା ବିଭାଗ *" else stringResource(R.string.civic_eye_category_label)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = selectedOption.icon,
                        contentDescription = selectedOption.labelEn,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                supportingText = {
                    Text(
                        text = selectedOption.subtitleEn,
                        fontSize = 11.sp
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
                    .testTag("civic_eye_category_dropdown")
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                CIVIC_EYE_CATEGORIES.forEach { option ->
                    val optionTag = "civic_eye_category_option_${
                        option.canonicalValue.lowercase(Locale.US).replace(" ", "_")
                    }"
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = if (isOdia) option.labelOr else option.labelEn,
                                    fontWeight = if (option.canonicalValue == selectedOption.canonicalValue) {
                                        FontWeight.Bold
                                    } else {
                                        FontWeight.Medium
                                    },
                                    fontSize = 13.5.sp
                                )
                                Text(
                                    text = option.subtitleEn,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = option.icon,
                                contentDescription = option.labelEn,
                                tint = if (option.canonicalValue == selectedOption.canonicalValue) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        },
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            onCategorySelected(option.canonicalValue)
                            expanded = false
                        },
                        modifier = Modifier.testTag(optionTag)
                    )
                }
            }
        }
    }
}

/**
 * Civic Eye description text field with built-in Android Voice-to-Text (`RecognizerIntent`)
 * dictation button for hands-free reporting on the go.
 */
@Composable
fun CivicEyeVoiceDescriptionField(
    value: String,
    onValueChange: (String) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    var isListening by remember { mutableStateOf(false) }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spoken = matches?.firstOrNull()?.trim().orEmpty()
            if (spoken.isNotBlank()) {
                val combined = if (value.isBlank()) spoken else "${value.trimEnd()} $spoken"
                onValueChange(combined)
                Toast.makeText(
                    context,
                    if (isOdia) "ଭଏସ୍ ବିବରଣୀ ଯୋଡ଼ାଗଲା" else "Voice description added",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val launchVoiceInput: () -> Unit = {
        BalasoreHapticHelper.vibrateButtonClick(context)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                if (isOdia) "or-IN" else "en-IN"
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                if (isOdia) {
                    "ସମସ୍ୟାର ବିବରଣୀ କୁହନ୍ତୁ (ଯଥା: ରାସ୍ତାରେ ବଡ଼ ଖାଲ ଅଛି)"
                } else {
                    "Describe the Balasore civic issue…"
                }
            )
        }
        try {
            isListening = true
            speechLauncher.launch(intent)
        } catch (_: ActivityNotFoundException) {
            isListening = false
            Toast.makeText(
                context,
                context.getString(R.string.civic_eye_voice_unavailable),
                Toast.LENGTH_LONG
            ).show()
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = {
                Text(
                    if (isOdia) "ସମ୍ପୂର୍ଣ୍ଣ ବିବରଣୀ (ଭଏସ୍ ଇନପୁଟ୍ ଉପଲବ୍ଧ) *"
                    else stringResource(R.string.civic_eye_desc_label)
                )
            },
            placeholder = {
                Text(stringResource(R.string.civic_eye_desc_placeholder))
            },
            trailingIcon = {
                Surface(
                    shape = CircleShape,
                    color = if (isListening) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    },
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    IconButton(
                        onClick = launchVoiceInput,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("civic_eye_voice_input_btn")
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = stringResource(R.string.civic_eye_voice_input_cd),
                            tint = if (isListening) {
                                MaterialTheme.colorScheme.onErrorContainer
                            } else {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    }
                }
            },
            supportingText = {
                Text(
                    text = if (isListening) {
                        stringResource(R.string.civic_eye_voice_listening)
                    } else {
                        stringResource(R.string.civic_eye_voice_helper)
                    },
                    fontSize = 11.sp
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 108.dp)
                .testTag("civic_eye_input_desc"),
            shape = RoundedCornerShape(12.dp),
            maxLines = 5
        )
    }
}

/**
 * Real-time 3-step visual status indicator ('Pending' → 'In Progress' → 'Resolved')
 * displayed on each Civic Eye report card and synchronized live with Firestore updates.
 */
@Composable
fun CivicReportStatusProgressIndicator(
    status: String,
    isSyncedWithFirestore: Boolean = true,
    modifier: Modifier = Modifier
) {
    val normalizedStage = when (status.trim().uppercase()) {
        "RESOLVED" -> 2
        "IN_PROGRESS", "IN PROGRESS" -> 1
        else -> 0
    }

    val activeColor = when (normalizedStage) {
        2 -> Color(0xFF16A34A)
        1 -> Color(0xFF0284C7)
        else -> Color(0xFFD97706)
    }
    val inactiveTrackColor = MaterialTheme.colorScheme.outlineVariant

    val steps = listOf("Pending", "In Progress", "Resolved")

    Surface(
        shape = RoundedCornerShape(10.dp),
        color = activeColor.copy(alpha = 0.06f),
        border = BorderStroke(1.dp, activeColor.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .testTag("civic_report_status_indicator")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status: ${steps[normalizedStage]}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = activeColor
                )
                if (isSyncedWithFirestore) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Synced with Firestore",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Live Firestore",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF0369A1)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                steps.forEachIndexed { index, _ ->
                    val reached = index <= normalizedStage
                    Box(
                        modifier = Modifier
                            .size(if (index == normalizedStage) 14.dp else 11.dp)
                            .clip(CircleShape)
                            .background(if (reached) activeColor else inactiveTrackColor),
                        contentAlignment = Alignment.Center
                    ) {
                        if (reached && index < normalizedStage) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .background(
                                    if (index < normalizedStage) activeColor else inactiveTrackColor
                                )
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                steps.forEachIndexed { index, label ->
                    val reached = index <= normalizedStage
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        fontWeight = if (index == normalizedStage) FontWeight.Bold else FontWeight.Medium,
                        color = if (reached) activeColor else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
