package com.example.ui.features.civic

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.util.BalasoreHapticHelper

/**
 * Filter Bottom Sheet for the Civic Eye component.
 * Allows citizens to filter reports by category (Pothole, Waterlogging, Waste, Electricity, etc.),
 * status, urgency, and ward with instant counts and haptic feedback.
 */
@Composable
fun CivicFilterBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    selectedCategory: String,
    onSelectCategory: (String) -> Unit,
    selectedStatus: String,
    onSelectStatus: (String) -> Unit,
    selectedUrgency: String,
    onSelectUrgency: (String) -> Unit,
    matchingCount: Int,
    onResetFilters: () -> Unit,
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .testTag("civic_filter_sheet"),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 24.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .width(44.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = Color(0xFF0284C7),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Column {
                            Text(
                                text = if (isOdia) "ସମସ୍ୟା ଫିଲ୍ଟର୍ କରନ୍ତୁ" else "Filter Civic Reports",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 17.sp
                                )
                            )
                            Text(
                                text = if (isOdia) "$matchingCount ରିପୋର୍ଟ ଉପଲବ୍ଧ" else "$matchingCount matching reports found",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(20.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 1. CATEGORY FILTER (PRIMARY FOCUS)
                Text(
                    text = if (isOdia) "ସମସ୍ୟା ବର୍ଗ (Category)" else "Category",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                val categoryOptions = listOf(
                    Triple("ALL", if (isOdia) "ସବୁ ବର୍ଗ" else "All Categories", "🌐"),
                    Triple("POTHOLE", if (isOdia) "ଖାଲ-ଖମା (Potholes)" else "Potholes", "🕳️"),
                    Triple("LIGHTING", if (isOdia) "ଷ୍ଟ୍ରିଟ୍ ଲାଇଟ୍ (Lighting)" else "Lighting", "💡"),
                    Triple("WATER_SUPPLY", if (isOdia) "ଜଳ ଯୋଗାଣ (Water Supply)" else "Water Supply", "🚰"),
                    Triple("WATERLOGGING", if (isOdia) "ଜଳବନ୍ଦୀ (Waterlogging)" else "Waterlogging", "💧"),
                    Triple("WASTE", if (isOdia) "ଆବର୍ଜନା (Waste)" else "Waste", "🗑️"),
                    Triple("ELECTRICITY", if (isOdia) "ବିଦ୍ୟୁତ୍ (Electricity)" else "Electricity", "⚡"),
                    Triple("ROAD", if (isOdia) "ରାସ୍ତା ନିର୍ମାଣ (Damaged Road)" else "Damaged Road", "🚧")
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    categoryOptions.chunked(2).forEach { rowOptions ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowOptions.forEach { (catKey, label, emoji) ->
                                val isSelected = selectedCategory == catKey
                                val activeColor = when (catKey) {
                                    "POTHOLE" -> Color(0xFFEA580C)
                                    "LIGHTING", "ELECTRICITY" -> Color(0xFFD97706)
                                    "WATER_SUPPLY", "WATER_LEAK" -> Color(0xFF0D9488)
                                    "WATERLOGGING" -> Color(0xFF0284C7)
                                    "WASTE" -> Color(0xFF9333EA)
                                    "ROAD" -> Color(0xFF64748B)
                                    else -> Color(0xFF0284C7)
                                }

                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) activeColor.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) activeColor else MaterialTheme.colorScheme.outlineVariant
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable {
                                            BalasoreHapticHelper.vibrateButtonClick(context)
                                            onSelectCategory(catKey)
                                        }
                                        .testTag("civic_filter_cat_${catKey.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(text = emoji, fontSize = 16.sp)
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) activeColor else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            if (rowOptions.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 2. STATUS FILTER
                Text(
                    text = if (isOdia) "ପ୍ରଗତି ସ୍ଥିତି (Status)" else "Status",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusList = listOf(
                        "ALL" to (if (isOdia) "ସବୁ ସ୍ଥିତି" else "All Status"),
                        "PENDING" to (if (isOdia) "⏳ ଦାଖଲ" else "⏳ Submitted"),
                        "IN_PROGRESS" to (if (isOdia) "🔧 କାର୍ଯ୍ୟ ଚାଲିଛି" else "🔧 In Progress"),
                        "RESOLVED" to (if (isOdia) "✅ ସମାଧାନ" else "✅ Resolved")
                    )

                    statusList.forEach { (statusKey, label) ->
                        val isSelected = selectedStatus == statusKey
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onSelectStatus(statusKey)
                            },
                            label = { Text(label, fontSize = 11.5.sp) },
                            modifier = Modifier.testTag("civic_filter_status_${statusKey.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // 3. SEVERITY FILTER (Low, Medium, Critical)
                Text(
                    text = if (isOdia) "ତୀବ୍ରତା ସ୍ତର (Severity Level)" else "Severity Level",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.5.sp
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val urgencyList = listOf(
                        "ALL" to (if (isOdia) "ସବୁ" else "All"),
                        "LOW" to (if (isOdia) "🟢 ସାଧାରଣ" else "🟢 Low"),
                        "MEDIUM" to (if (isOdia) "🟡 ମଧ୍ୟମ" else "🟡 Medium"),
                        "CRITICAL" to (if (isOdia) "🔴 ଜରୁରୀ" else "🔴 Critical")
                    )

                    urgencyList.forEach { (urgencyKey, label) ->
                        val isSelected = selectedUrgency.equals(urgencyKey, ignoreCase = true) ||
                            (urgencyKey == "CRITICAL" && (selectedUrgency.equals("EMERGENCY", ignoreCase = true) ||
                                selectedUrgency.equals("HIGH", ignoreCase = true)))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSelected) Color(0xFF0284C7).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    onSelectUrgency(urgencyKey)
                                }
                                .testTag("civic_filter_urgency_${urgencyKey.lowercase()}")
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color(0xFF0284C7) else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ACTION BUTTONS (APPLY & RESET)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            onResetFilters()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("civic_filter_reset_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isOdia) "ପୂର୍ବବତ୍" else "Reset All", fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            BalasoreHapticHelper.vibrateSuccess(context)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        modifier = Modifier
                            .weight(1.5f)
                            .height(48.dp)
                            .testTag("civic_filter_apply_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Done, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOdia) "ଦେଖନ୍ତୁ ($matchingCount)" else "Apply Filters ($matchingCount)",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
