package com.example.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.CivicReportEntity
import com.example.data.model.FirestoreUserReport
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Normalized workflow status for Civic Eye incident history indicators.
 */
enum class CivicReportStatus(
    val labelEn: String,
    val labelOd: String,
    val accentColor: Color,
    val containerColorLight: Color,
    val containerColorDark: Color,
    val icon: ImageVector
) {
    PENDING(
        labelEn = "Pending",
        labelOd = "ବିଚାରାଧୀନ",
        accentColor = Color(0xFFD97706),
        containerColorLight = Color(0xFFFEF3C7),
        containerColorDark = Color(0xFF451A03),
        icon = Icons.Default.Schedule
    ),
    IN_PROGRESS(
        labelEn = "In Progress",
        labelOd = "କାର୍ଯ୍ୟ ଚାଲିଛି",
        accentColor = Color(0xFF0284C7),
        containerColorLight = Color(0xFFE0F2FE),
        containerColorDark = Color(0xFF0C4A6E),
        icon = Icons.Default.Engineering
    ),
    RESOLVED(
        labelEn = "Resolved",
        labelOd = "ସମାଧାନ ହୋଇଛି",
        accentColor = Color(0xFF15803D),
        containerColorLight = Color(0xFFDCFCE7),
        containerColorDark = Color(0xFF052E16),
        icon = Icons.Default.CheckCircle
    ),
    DRAFT(
        labelEn = "Draft",
        labelOd = "ଡ୍ରାଫ୍ଟ",
        accentColor = Color(0xFF64748B),
        containerColorLight = Color(0xFFE2E8F0),
        containerColorDark = Color(0xFF1E293B),
        icon = Icons.Default.EditNote
    )
}

enum class CivicHistorySortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

/**
 * Enriched item displayed inside the chronological Civic Eye history timeline.
 */
data class CivicHistoryEntry(
    val report: CivicReportEntity,
    val status: CivicReportStatus,
    val layer: CivicIncidentLayer,
    val severity: CivicSeverityLevel,
    val exactTimestampLabel: String,
    val relativeTimestampLabel: String,
    val updatedTimestampLabel: String?
)

/**
 * A calendar-day section grouping Civic Eye incidents chronologically.
 */
data class CivicHistorySection(
    val dayKey: String,
    val headerLabel: String,
    val entries: List<CivicHistoryEntry>
)

/**
 * Pure domain utilities for merging, sorting, filtering, and chronologically grouping
 * Civic Eye incident reports with readable timestamps and status classifications.
 */
object CivicEyeHistoryHelper {

    fun classifyStatus(rawStatus: String?): CivicReportStatus {
        val norm = rawStatus.orEmpty().trim().uppercase(Locale.US)
        return when {
            norm.contains("RESOLV") || norm.contains("COMPLET") || norm.contains("CLOSED") || norm.contains("FIXED") ->
                CivicReportStatus.RESOLVED
            norm.contains("PROGRESS") || norm.contains("ASSIGN") || norm.contains("INSPECT") || norm.contains("WORK") ->
                CivicReportStatus.IN_PROGRESS
            norm.contains("DRAFT") || norm.contains("QUEUED") ->
                CivicReportStatus.DRAFT
            else -> CivicReportStatus.PENDING
        }
    }

    /**
     * Merges local Room reports and Firestore cloud reports, deduplicating by ID, and sorts
     * chronologically by `reportedTimestamp`.
     */
    fun mergeAndSortReports(
        localReports: List<CivicReportEntity>,
        firestoreReports: List<FirestoreUserReport>,
        sortOrder: CivicHistorySortOrder = CivicHistorySortOrder.NEWEST_FIRST
    ): List<CivicReportEntity> {
        val merged = CivicEyeLayersMapHelper.mergeCivicEyeReports(localReports, firestoreReports)
        return when (sortOrder) {
            CivicHistorySortOrder.NEWEST_FIRST ->
                merged.sortedWith(
                    compareByDescending<CivicReportEntity> { it.reportedTimestamp }
                        .thenBy { it.id }
                )
            CivicHistorySortOrder.OLDEST_FIRST ->
                merged.sortedWith(
                    compareBy<CivicReportEntity> { it.reportedTimestamp }
                        .thenBy { it.id }
                )
        }
    }

    fun filterReports(
        reports: List<CivicReportEntity>,
        statusFilter: CivicReportStatus?,
        searchQuery: String
    ): List<CivicReportEntity> {
        val trimmed = searchQuery.trim()
        return reports.filter { report ->
            val matchesStatus = statusFilter == null || classifyStatus(report.status) == statusFilter
            val matchesQuery = trimmed.isEmpty() ||
                report.title.contains(trimmed, ignoreCase = true) ||
                report.category.contains(trimmed, ignoreCase = true) ||
                report.wardLocation.contains(trimmed, ignoreCase = true) ||
                report.description.contains(trimmed, ignoreCase = true) ||
                report.id.contains(trimmed, ignoreCase = true) ||
                (report.geoAddress?.contains(trimmed, ignoreCase = true) == true)
            matchesStatus && matchesQuery
        }
    }

    fun countByStatus(reports: List<CivicReportEntity>): Map<CivicReportStatus, Int> {
        val counts = CivicReportStatus.entries.associateWith { 0 }.toMutableMap()
        reports.forEach { report ->
            val status = classifyStatus(report.status)
            counts[status] = (counts[status] ?: 0) + 1
        }
        return counts
    }

    fun formatExactTimestamp(timestampMillis: Long, locale: Locale = Locale.getDefault()): String {
        if (timestampMillis <= 0L) return "Unknown time"
        val formatter = SimpleDateFormat("dd MMM yyyy • hh:mm a", locale)
        return formatter.format(Date(timestampMillis))
    }

    fun formatShortTime(timestampMillis: Long, locale: Locale = Locale.getDefault()): String {
        if (timestampMillis <= 0L) return ""
        val formatter = SimpleDateFormat("hh:mm a", locale)
        return formatter.format(Date(timestampMillis))
    }

    fun formatRelativeTimestamp(timestampMillis: Long, nowMillis: Long): String {
        if (timestampMillis <= 0L) return "Just now"
        val diffMs = (nowMillis - timestampMillis).coerceAtLeast(0L)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
        val days = TimeUnit.MILLISECONDS.toDays(diffMs)
        return when {
            minutes < 1L -> "Just now"
            minutes < 60L -> "${minutes}m ago"
            hours < 24L -> "${hours}h ago"
            days == 1L -> "1d ago"
            days < 30L -> "${days}d ago"
            else -> {
                val months = (days / 30L).coerceAtLeast(1L)
                "${months}mo ago"
            }
        }
    }

    /**
     * Groups already-sorted Civic Eye reports into calendar day sections ("Today", "Yesterday",
     * or formatted date) while preserving the input chronological order.
     */
    fun buildChronologicalSections(
        reports: List<CivicReportEntity>,
        nowMillis: Long = System.currentTimeMillis(),
        todayLabel: String = "Today",
        yesterdayLabel: String = "Yesterday",
        locale: Locale = Locale.getDefault()
    ): List<CivicHistorySection> {
        if (reports.isEmpty()) return emptyList()

        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val headerDateFormat = SimpleDateFormat("EEE, dd MMM yyyy", locale)

        val todayKey = dayKeyFormat.format(Date(nowMillis))
        val yesterdayCal = Calendar.getInstance().apply {
            timeInMillis = nowMillis
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val yesterdayKey = dayKeyFormat.format(yesterdayCal.time)

        val grouped = LinkedHashMap<String, MutableList<CivicHistoryEntry>>()
        val headerLabels = HashMap<String, String>()

        reports.forEach { report ->
            val ts = report.reportedTimestamp.takeIf { it > 0L } ?: nowMillis
            val date = Date(ts)
            val key = dayKeyFormat.format(date)
            if (!headerLabels.containsKey(key)) {
                headerLabels[key] = when (key) {
                    todayKey -> todayLabel
                    yesterdayKey -> yesterdayLabel
                    else -> headerDateFormat.format(date)
                }
            }

            val updatedLabel = if (report.lastUpdatedTimestamp > ts + 60_000L) {
                formatExactTimestamp(report.lastUpdatedTimestamp, locale)
            } else {
                null
            }

            val entry = CivicHistoryEntry(
                report = report,
                status = classifyStatus(report.status),
                layer = CivicEyeLayersMapHelper.classifyLayer(report.category),
                severity = CivicEyeLayersMapHelper.classifySeverity(report.urgency),
                exactTimestampLabel = formatExactTimestamp(ts, locale),
                relativeTimestampLabel = formatRelativeTimestamp(ts, nowMillis),
                updatedTimestampLabel = updatedLabel
            )
            grouped.getOrPut(key) { mutableListOf() }.add(entry)
        }

        return grouped.map { (dayKey, items) ->
            CivicHistorySection(
                dayKey = dayKey,
                headerLabel = headerLabels[dayKey] ?: dayKey,
                entries = items
            )
        }
    }
}
