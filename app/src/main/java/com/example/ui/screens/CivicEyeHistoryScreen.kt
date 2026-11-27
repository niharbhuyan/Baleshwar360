package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Update
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.local.CivicReportEntity
import com.example.data.model.AppLanguage
import com.example.data.model.FirestoreUserReport
import com.example.ui.features.civic.CivicEyeMediaAndLocationHelper
import com.example.ui.theme.LocalAppTheme
import com.example.util.BalasoreHapticHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Scrollable chronological history screen listing all Civic Eye incidents across Balasore,
 * complete with exact & relative timestamps, status indicators, search, status filtering,
 * photo thumbnails, and expandable municipal details.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CivicEyeHistoryScreen(
    localReports: List<CivicReportEntity>,
    firestoreReports: List<FirestoreUserReport>,
    language: AppLanguage = AppLanguage.ENGLISH,
    onRefreshReports: () -> Unit = {},
    onUpvoteReport: (String) -> Unit = {},
    onOpenLayersMap: () -> Unit = {},
    onReportNewIssue: () -> Unit = {},
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = LocalAppTheme.current.isDark
    val isOdia = language == AppLanguage.ODIA

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var selectedStatusName by rememberSaveable { mutableStateOf<String?>(null) }
    var sortOrderName by rememberSaveable { mutableStateOf(CivicHistorySortOrder.NEWEST_FIRST.name) }
    var expandedReportId by rememberSaveable { mutableStateOf<String?>(null) }

    val selectedStatus = remember(selectedStatusName) {
        selectedStatusName?.let { name ->
            CivicReportStatus.entries.firstOrNull { it.name == name }
        }
    }
    val sortOrder = remember(sortOrderName) {
        CivicHistorySortOrder.entries.firstOrNull { it.name == sortOrderName }
            ?: CivicHistorySortOrder.NEWEST_FIRST
    }

    val allMergedReports = remember(localReports, firestoreReports, sortOrder) {
        CivicEyeHistoryHelper.mergeAndSortReports(localReports, firestoreReports, sortOrder)
    }
    val statusCounts = remember(allMergedReports) {
        CivicEyeHistoryHelper.countByStatus(allMergedReports)
    }
    val filteredReports = remember(allMergedReports, selectedStatus, searchQuery) {
        CivicEyeHistoryHelper.filterReports(allMergedReports, selectedStatus, searchQuery)
    }

    val todayLabel = stringResource(R.string.civic_history_today)
    val yesterdayLabel = stringResource(R.string.civic_history_yesterday)
    val nowMillis = remember(allMergedReports.size, sortOrder) { System.currentTimeMillis() }

    val sections = remember(filteredReports, nowMillis, todayLabel, yesterdayLabel) {
        CivicEyeHistoryHelper.buildChronologicalSections(
            reports = filteredReports,
            nowMillis = nowMillis,
            todayLabel = todayLabel,
            yesterdayLabel = yesterdayLabel
        )
    }

    BackHandler {
        if (expandedReportId != null) {
            expandedReportId = null
        } else {
            onClose()
        }
    }

    val sortLabel = if (sortOrder == CivicHistorySortOrder.NEWEST_FIRST) {
        stringResource(R.string.civic_history_sort_newest)
    } else {
        stringResource(R.string.civic_history_sort_oldest)
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("civic_history_screen"),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isOdia) "ସିଭିକ୍ ଆଇ ରିପୋର୍ଟ ଇତିହାସ" else stringResource(R.string.civic_history_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(
                                R.string.civic_history_subtitle,
                                filteredReports.size,
                                sortLabel
                            ),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("civic_history_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.civic_history_back_cd)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            sortOrderName = if (sortOrder == CivicHistorySortOrder.NEWEST_FIRST) {
                                CivicHistorySortOrder.OLDEST_FIRST.name
                            } else {
                                CivicHistorySortOrder.NEWEST_FIRST.name
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("civic_history_sort_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.civic_history_sort_cd),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            onOpenLayersMap()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("civic_history_open_map_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = stringResource(R.string.civic_history_open_map_cd)
                        )
                    }
                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            onRefreshReports()
                            Toast.makeText(
                                context,
                                context.getString(R.string.civic_history_refreshing),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("civic_history_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.civic_history_refresh_cd)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    BalasoreHapticHelper.vibrateButtonClick(context)
                    onReportNewIssue()
                },
                icon = {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null
                    )
                },
                text = {
                    Text(
                        text = if (isOdia) "ଅଭିଯୋଗ ଦାଖଲ" else stringResource(R.string.civic_history_report_cta),
                        fontWeight = FontWeight.SemiBold
                    )
                },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("civic_history_report_fab")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 720.dp)
                    .testTag("civic_history_list"),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = 12.dp,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item(key = "history_summary_header") {
                    CivicHistorySummaryBanner(
                        totalCount = allMergedReports.size,
                        statusCounts = statusCounts,
                        isOdia = isOdia,
                        isDark = isDark
                    )
                }

                item(key = "history_search_and_filters") {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            singleLine = true,
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.civic_history_search_placeholder),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier
                                            .minimumInteractiveComponentSize()
                                            .testTag("civic_history_clear_search_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = stringResource(R.string.civic_history_clear_search_cd)
                                        )
                                    }
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("civic_history_search_input")
                        )

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            item(key = "filter_all") {
                                FilterChip(
                                    selected = selectedStatus == null,
                                    onClick = {
                                        BalasoreHapticHelper.vibrateButtonClick(context)
                                        selectedStatusName = null
                                    },
                                    label = {
                                        Text(
                                            text = "${stringResource(R.string.civic_history_status_all)} (${allMergedReports.size})",
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("civic_history_status_chip_all")
                                )
                            }

                            items(CivicReportStatus.entries, key = { it.name }) { status ->
                                val count = statusCounts[status] ?: 0
                                val isSelected = selectedStatus == status
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        BalasoreHapticHelper.vibrateButtonClick(context)
                                        selectedStatusName = if (isSelected) null else status.name
                                    },
                                    label = {
                                        Text(
                                            text = "${if (isOdia) status.labelOd else status.labelEn} ($count)",
                                            fontWeight = FontWeight.Medium
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = status.icon,
                                            contentDescription = null,
                                            tint = if (isSelected) status.accentColor else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = if (isDark) status.containerColorDark else status.containerColorLight,
                                        selectedLabelColor = status.accentColor
                                    ),
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("civic_history_status_chip_${status.name.lowercase()}")
                                )
                            }
                        }
                    }
                }

                if (sections.isEmpty()) {
                    item(key = "history_empty_state") {
                        CivicHistoryEmptyState(
                            hasActiveFilters = selectedStatus != null || searchQuery.isNotBlank(),
                            onClearFilters = {
                                selectedStatusName = null
                                searchQuery = ""
                            },
                            onReportNewIssue = onReportNewIssue
                        )
                    }
                } else {
                    sections.forEach { section ->
                        item(key = "section_header_${section.dayKey}") {
                            ChronologicalDateHeader(
                                label = section.headerLabel,
                                count = section.entries.size
                            )
                        }
                        itemsIndexed(
                            items = section.entries,
                            key = { _, entry -> entry.report.id }
                        ) { index, entry ->
                            CivicHistoryTimelineRow(
                                entry = entry,
                                isFirstInSection = index == 0,
                                isLastInSection = index == section.entries.lastIndex,
                                isExpanded = expandedReportId == entry.report.id,
                                isOdia = isOdia,
                                isDark = isDark,
                                onToggleExpand = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    expandedReportId = if (expandedReportId == entry.report.id) null else entry.report.id
                                },
                                onUpvote = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    onUpvoteReport(entry.report.id)
                                    Toast.makeText(
                                        context,
                                        context.getString(R.string.civic_layers_map_upvoted_toast),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                onViewOnMap = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    onOpenLayersMap()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CivicHistorySummaryBanner(
    totalCount: Int,
    statusCounts: Map<CivicReportStatus, Int>,
    isOdia: Boolean,
    isDark: Boolean
) {
    val pendingCount = statusCounts[CivicReportStatus.PENDING] ?: 0
    val inProgressCount = statusCounts[CivicReportStatus.IN_PROGRESS] ?: 0
    val resolvedCount = statusCounts[CivicReportStatus.RESOLVED] ?: 0
    val resolvedFraction = if (totalCount > 0) resolvedCount.toFloat() / totalCount.toFloat() else 0f
    val resolvedPercent = (resolvedFraction * 100f).toInt()

    val gradientBrush = Brush.linearGradient(
        colors = if (isDark) {
            listOf(
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f),
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.9f)
            )
        } else {
            listOf(
                MaterialTheme.colorScheme.primaryContainer,
                MaterialTheme.colorScheme.surface
            )
        }
    )

    Card(
        shape = RoundedCornerShape(22.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("civic_history_summary_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(gradientBrush)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) "ବାଲେଶ୍ୱର ନାଗରିକ ରିପୋର୍ଟ ଟାଇମଲାଇନ୍" else stringResource(R.string.civic_history_summary_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.civic_history_summary_resolved_rate, resolvedPercent),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "$totalCount",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = { resolvedFraction.coerceIn(0f, 1f) },
                color = CivicReportStatus.RESOLVED.accentColor,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusMetricPill(
                    label = if (isOdia) CivicReportStatus.PENDING.labelOd else CivicReportStatus.PENDING.labelEn,
                    count = pendingCount,
                    accent = CivicReportStatus.PENDING.accentColor,
                    container = if (isDark) CivicReportStatus.PENDING.containerColorDark else CivicReportStatus.PENDING.containerColorLight,
                    modifier = Modifier.weight(1f)
                )
                StatusMetricPill(
                    label = if (isOdia) CivicReportStatus.IN_PROGRESS.labelOd else CivicReportStatus.IN_PROGRESS.labelEn,
                    count = inProgressCount,
                    accent = CivicReportStatus.IN_PROGRESS.accentColor,
                    container = if (isDark) CivicReportStatus.IN_PROGRESS.containerColorDark else CivicReportStatus.IN_PROGRESS.containerColorLight,
                    modifier = Modifier.weight(1f)
                )
                StatusMetricPill(
                    label = if (isOdia) CivicReportStatus.RESOLVED.labelOd else CivicReportStatus.RESOLVED.labelEn,
                    count = resolvedCount,
                    accent = CivicReportStatus.RESOLVED.accentColor,
                    container = if (isDark) CivicReportStatus.RESOLVED.containerColorDark else CivicReportStatus.RESOLVED.containerColorLight,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatusMetricPill(
    label: String,
    count: Int,
    accent: Color,
    container: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = container.copy(alpha = 0.85f),
        border = BorderStroke(1.dp, accent.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = accent
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ChronologicalDateHeader(
    label: String,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(999.dp),
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.outlineVariant
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun CivicHistoryTimelineRow(
    entry: CivicHistoryEntry,
    isFirstInSection: Boolean,
    isLastInSection: Boolean,
    isExpanded: Boolean,
    isOdia: Boolean,
    isDark: Boolean,
    onToggleExpand: () -> Unit,
    onUpvote: () -> Unit,
    onViewOnMap: () -> Unit
) {
    val railColor = MaterialTheme.colorScheme.outlineVariant
    val nodeColor = entry.status.accentColor
    val statusContainer = if (isDark) entry.status.containerColorDark else entry.status.containerColorLight

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Chronological vertical timeline spine & status node
        Box(
            modifier = Modifier
                .width(20.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerX = size.width / 2f
                val nodeY = 24.dp.toPx()
                if (!isFirstInSection) {
                    drawLine(
                        color = railColor,
                        start = Offset(centerX, 0f),
                        end = Offset(centerX, nodeY - 7.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                if (!isLastInSection) {
                    drawLine(
                        color = railColor,
                        start = Offset(centerX, nodeY + 7.dp.toPx()),
                        end = Offset(centerX, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
                drawCircle(
                    color = nodeColor.copy(alpha = 0.22f),
                    radius = 9.dp.toPx(),
                    center = Offset(centerX, nodeY)
                )
                drawCircle(
                    color = nodeColor,
                    radius = 5.dp.toPx(),
                    center = Offset(centerX, nodeY)
                )
            }
        }

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(
                width = if (isExpanded) 1.5.dp else 1.dp,
                color = if (isExpanded) nodeColor.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outlineVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 3.dp else 1.dp),
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .clickable(onClick = onToggleExpand)
                .testTag("civic_history_card_${entry.report.id}")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Status + Severity + Category Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = entry.layer.accentColor.copy(alpha = 0.14f),
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = entry.layer.icon,
                                    contentDescription = null,
                                    tint = entry.layer.accentColor,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isOdia) entry.layer.labelOd else entry.layer.labelEn,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = entry.layer.accentColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Status Indicator Badge
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = statusContainer,
                            border = BorderStroke(1.dp, nodeColor.copy(alpha = 0.45f)),
                            modifier = Modifier.testTag("civic_history_status_badge_${entry.report.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = entry.status.icon,
                                    contentDescription = null,
                                    tint = nodeColor,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = if (isOdia) entry.status.labelOd else entry.status.labelEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = nodeColor
                                )
                            }
                        }

                        // Severity Pill
                        Surface(
                            shape = RoundedCornerShape(999.dp),
                            color = entry.severity.containerColor.copy(alpha = if (isDark) 0.22f else 1f)
                        ) {
                            Text(
                                text = if (isOdia) entry.severity.labelOd else entry.severity.labelEn,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = entry.severity.accentColor,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                // Title + optional Photo Thumbnail
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = entry.report.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = if (isExpanded) 4 else 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (entry.report.description.isNotBlank()) {
                            Text(
                                text = entry.report.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = if (isExpanded) 8 else 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    if (!entry.report.photoUri.isNullOrBlank()) {
                        CivicHistoryPhotoThumbnail(
                            photoSource = entry.report.photoUri,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Ward Location & Exact + Relative Timestamp Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = entry.report.wardLocation,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Timestamp Strip
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = entry.exactTimestampLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.testTag("civic_history_timestamp_${entry.report.id}")
                            )
                        }
                        Text(
                            text = entry.relativeTimestampLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Expandable Details Drawer
                AnimatedVisibility(
                    visible = isExpanded,
                    enter = fadeIn(spring(stiffness = Spring.StiffnessMediumLow)) + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp)
                    ) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                        entry.updatedTimestampLabel?.let { updated ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Update,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.tertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = stringResource(R.string.civic_history_updated_at, updated),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (entry.report.assignedDepartment.isNotBlank()) {
                            Text(
                                text = "${stringResource(R.string.civic_history_department_label)} ${entry.report.assignedDepartment}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (entry.report.resolutionNotes.isNotBlank()) {
                            Text(
                                text = "${stringResource(R.string.civic_history_notes_label)} ${entry.report.resolutionNotes}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onUpvote,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("civic_history_upvote_btn_${entry.report.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ThumbUp,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "${stringResource(R.string.civic_osm_map_upvote)} (${entry.report.upvotesCount})",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            Button(
                                onClick = onViewOnMap,
                                modifier = Modifier
                                    .weight(1f)
                                    .heightIn(min = 48.dp)
                                    .testTag("civic_history_map_btn_${entry.report.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Map,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = stringResource(R.string.civic_history_view_on_map),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CivicHistoryPhotoThumbnail(
    photoSource: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isRemoteUrl = photoSource.startsWith("http://", ignoreCase = true) ||
        photoSource.startsWith("https://", ignoreCase = true)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        if (isRemoteUrl) {
            AsyncImage(
                model = photoSource,
                contentDescription = stringResource(R.string.civic_history_photo_cd),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            val decodedBitmap by produceState<Bitmap?>(initialValue = null, key1 = photoSource) {
                value = withContext(Dispatchers.IO) {
                    when {
                        photoSource.startsWith("data:image", ignoreCase = true) ->
                            CivicEyeMediaAndLocationHelper.decodeBase64Photo(photoSource)
                        photoSource.startsWith("content://", ignoreCase = true) ||
                            photoSource.startsWith("file://", ignoreCase = true) ->
                            CivicEyeMediaAndLocationHelper.decodeSampledBitmap(
                                context = context,
                                uri = Uri.parse(photoSource),
                                maxDimPx = 256
                            )
                        else -> {
                            val localFile = File(photoSource)
                            if (localFile.exists()) {
                                CivicEyeMediaAndLocationHelper.decodeSampledBitmap(
                                    context = context,
                                    file = localFile,
                                    maxDimPx = 256
                                )
                            } else {
                                CivicEyeMediaAndLocationHelper.decodeBase64Photo(photoSource)
                            }
                        }
                    }
                }
            }

            if (decodedBitmap != null) {
                Image(
                    bitmap = decodedBitmap!!.asImageBitmap(),
                    contentDescription = stringResource(R.string.civic_history_photo_cd),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = stringResource(R.string.civic_history_photo_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CivicHistoryEmptyState(
    hasActiveFilters: Boolean,
    onClearFilters: () -> Unit,
    onReportNewIssue: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp)
            .testTag("civic_history_empty_state")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(64.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (hasActiveFilters) Icons.Default.Search else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            Text(
                text = if (hasActiveFilters) {
                    stringResource(R.string.civic_history_empty_filtered_title)
                } else {
                    stringResource(R.string.civic_history_empty_title)
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = if (hasActiveFilters) {
                    stringResource(R.string.civic_history_empty_filtered_desc)
                } else {
                    stringResource(R.string.civic_history_empty_desc)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (hasActiveFilters) {
                TextButton(
                    onClick = onClearFilters,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("civic_history_reset_filters_btn")
                ) {
                    Text(
                        text = stringResource(R.string.civic_history_clear_filters),
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onReportNewIssue,
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .testTag("civic_history_empty_report_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddAPhoto,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.civic_history_report_cta))
                }
            }
        }
    }
}
