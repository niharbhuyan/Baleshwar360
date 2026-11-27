package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import java.util.Locale

/**
 * Slide-out Material 3 filter drawer sheet for toggling Civic Eye incident map layers
 * (Potholes, Sanitation, Lighting, Drainage, Water Supply) and optional severity filters.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CivicMapLayerFilterDrawerContent(
    enabledLayers: Set<CivicIncidentLayer>,
    countsByLayer: Map<CivicIncidentLayer, Int>,
    visibleCount: Int,
    totalCount: Int,
    isOdia: Boolean,
    onToggleLayer: (CivicIncidentLayer) -> Unit,
    onShowAllLayers: () -> Unit,
    onHideAllLayers: () -> Unit,
    onCloseDrawer: () -> Unit,
    selectedSeverity: CivicSeverityLevel? = null,
    countsBySeverity: Map<CivicSeverityLevel, Int>? = null,
    onSelectSeverity: ((CivicSeverityLevel?) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val orderedLayers = listOf(
        CivicIncidentLayer.POTHOLES,
        CivicIncidentLayer.SANITATION,
        CivicIncidentLayer.STREET_LIGHTS,
        CivicIncidentLayer.DRAINAGE,
        CivicIncidentLayer.WATER_SUPPLY
    )

    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .widthIn(max = 336.dp)
            .fillMaxHeight()
            .testTag("civic_map_filter_drawer")
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Drawer Header with subtle atmospheric gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
                    .padding(start = 18.dp, end = 8.dp, top = 14.dp, bottom = 14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = if (isOdia) "ସିଭିକ୍ ଆଇ ଫିଲ୍ଟର ଡ୍ରୟର" else stringResource(R.string.civic_drawer_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (isOdia) {
                                    "ମ୍ୟାପ୍‌ରେ ଅଭିଯୋଗ ଲେୟାର ଅନ୍/ଅଫ୍ କରନ୍ତୁ"
                                } else {
                                    stringResource(R.string.civic_drawer_subtitle)
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    IconButton(
                        onClick = onCloseDrawer,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("civic_filter_drawer_close_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.civic_drawer_close_cd)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Scrollable Drawer Body
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = stringResource(R.string.civic_drawer_layers_section),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        TextButton(
                            onClick = onShowAllLayers,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("civic_layers_enable_all_btn")
                        ) {
                            Text(
                                text = stringResource(R.string.civic_layers_map_show_all),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        TextButton(
                            onClick = onHideAllLayers,
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("civic_layers_hide_all_btn")
                        ) {
                            Text(
                                text = stringResource(R.string.civic_layers_map_hide_all),
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                orderedLayers.forEach { layer ->
                    val isChecked = layer in enabledLayers
                    val count = countsByLayer[layer] ?: 0
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isChecked) {
                            layer.accentColor.copy(alpha = 0.11f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                        },
                        border = BorderStroke(
                            width = if (isChecked) 1.25.dp else 1.dp,
                            color = if (isChecked) {
                                layer.accentColor.copy(alpha = 0.55f)
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onToggleLayer(layer) }
                            .testTag("civic_drawer_layer_row_${layer.name.lowercase(Locale.US)}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isChecked) layer.accentColor
                                        else layer.accentColor.copy(alpha = 0.35f)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = layer.icon,
                                    contentDescription = layer.labelEn,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = if (isOdia) layer.labelOd else layer.labelEn,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = layer.accentColor.copy(alpha = 0.16f)
                                    ) {
                                        Text(
                                            text = "$count",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = layer.accentColor,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = layer.subtitleEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Switch(
                                checked = isChecked,
                                onCheckedChange = { onToggleLayer(layer) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = layer.accentColor
                                ),
                                modifier = Modifier.testTag("civic_layer_switch_${layer.name.lowercase(Locale.US)}")
                            )
                        }
                    }
                }

                // Optional Severity Filter Section inside the drawer
                if (onSelectSeverity != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = stringResource(R.string.civic_drawer_severity_section),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        FilterChip(
                            selected = selectedSeverity == null,
                            onClick = { onSelectSeverity(null) },
                            label = {
                                Text(
                                    text = stringResource(R.string.civic_severity_all),
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("civic_drawer_severity_all")
                        )

                        CivicSeverityLevel.entries.forEach { severity ->
                            val isSelected = selectedSeverity == severity
                            val count = countsBySeverity?.get(severity)
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    onSelectSeverity(if (isSelected) null else severity)
                                },
                                label = {
                                    val baseLabel = if (isOdia) severity.labelOd else severity.labelEn
                                    Text(
                                        text = if (count != null) "$baseLabel ($count)" else baseLabel,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = severity.containerColor,
                                    selectedLabelColor = severity.accentColor
                                ),
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("civic_drawer_severity_${severity.name.lowercase(Locale.US)}")
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // Sticky Footer with Live Count + Apply Button
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = stringResource(
                        R.string.civic_drawer_active_summary,
                        visibleCount,
                        totalCount
                    ),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
                Button(
                    onClick = onCloseDrawer,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 48.dp)
                        .testTag("civic_filter_drawer_apply_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Layers,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.civic_drawer_apply_btn),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
