package com.example.data.maps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.compose.ui.graphics.Color
import com.example.data.local.CivicReportEntity
import com.example.ui.screens.CivicSyncStatus
import com.example.ui.screens.calculateDistanceMeters
import com.example.ui.screens.getCivicSyncStatus
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint

/**
 * Data representation of a clustered group of civic issue reports on the OSM map.
 * When multiple issues are reported in proximity (e.g., adjacent potholes or drainage blocks),
 * this cluster aggregates them to eliminate pin overlap and provide an overview of density.
 */
data class CivicMarkerCluster(
    val id: String,
    val centerLat: Double,
    val centerLng: Double,
    val reports: List<CivicReportEntity>
) {
    val size: Int get() = reports.size
    val isSingle: Boolean get() = reports.size == 1
    val singleReport: CivicReportEntity? get() = reports.firstOrNull()
    val centerGeoPoint: GeoPoint get() = GeoPoint(centerLat, centerLng)

    // Category Counts
    val potholeCount: Int get() = reports.count {
        it.category.contains("Pothole", ignoreCase = true) || it.category.contains("Road", ignoreCase = true)
    }
    val drainageCount: Int get() = reports.count {
        it.category.contains("Drain", ignoreCase = true) || it.category.contains("Water", ignoreCase = true)
    }
    val wasteCount: Int get() = reports.count {
        it.category.contains("Waste", ignoreCase = true) || it.category.contains("Garbage", ignoreCase = true)
    }
    val streetlightCount: Int get() = reports.count {
        it.category.contains("Light", ignoreCase = true)
    }

    // Sync Status Counts relative to Firestore backend
    val draftCount: Int get() = reports.count { getCivicSyncStatus(it) == CivicSyncStatus.DRAFT }
    val pendingCount: Int get() = reports.count { getCivicSyncStatus(it) == CivicSyncStatus.PENDING }
    val uploadedCount: Int get() = reports.count { getCivicSyncStatus(it) == CivicSyncStatus.UPLOADED }

    // Criticality
    val hasCriticalUrgency: Boolean get() = reports.any { it.urgency.equals("CRITICAL", ignoreCase = true) }
    val hasHighUrgency: Boolean get() = reports.any { it.urgency.equals("HIGH", ignoreCase = true) }

    val dominantStatus: CivicSyncStatus
        get() = when {
            draftCount > 0 && uploadedCount == 0 && pendingCount == 0 -> CivicSyncStatus.DRAFT
            pendingCount > 0 && draftCount == 0 && uploadedCount == 0 -> CivicSyncStatus.PENDING
            uploadedCount > 0 && draftCount == 0 && pendingCount == 0 -> CivicSyncStatus.UPLOADED
            else -> if (draftCount > 0) CivicSyncStatus.DRAFT else CivicSyncStatus.PENDING
        }

    fun getBoundingBox(padding: Double = 0.0008): BoundingBox {
        val lats = reports.mapNotNull { it.latitude }.ifEmpty { listOf(centerLat) }
        val lngs = reports.mapNotNull { it.longitude }.ifEmpty { listOf(centerLng) }
        val minLat = lats.minOrNull() ?: centerLat
        val maxLat = lats.maxOrNull() ?: centerLat
        val minLng = lngs.minOrNull() ?: centerLng
        val maxLng = lngs.maxOrNull() ?: centerLng
        return BoundingBox(maxLat + padding, maxLng + padding, minLat - padding, minLng - padding)
    }
}

/**
 * Returns the cluster radius in meters corresponding to the OSMDroid map zoom level.
 * As the user zooms in, the clustering radius decreases until zoom 17+ where pins separate.
 */
fun getClusterRadiusMetersForZoom(zoomLevel: Double): Double {
    return when {
        zoomLevel <= 10.0 -> 1200.0
        zoomLevel <= 11.0 -> 800.0
        zoomLevel <= 12.0 -> 450.0
        zoomLevel <= 13.0 -> 240.0
        zoomLevel <= 14.0 -> 130.0
        zoomLevel <= 15.0 -> 70.0
        zoomLevel <= 16.0 -> 35.0
        else -> 0.0 // At zoom 17+, completely unclustered to inspect each distinct crater/drainage point
    }
}

/**
 * Centroid-based proximity marker clustering engine for OSMDroid.
 * Groups civic reports within proximity radius at the given zoom level to prevent visual clutter.
 */
fun clusterCivicReports(
    reports: List<CivicReportEntity>,
    zoomLevel: Double,
    userLat: Double,
    userLng: Double,
    clusteringEnabled: Boolean = true
): List<CivicMarkerCluster> {
    if (reports.isEmpty()) return emptyList()

    if (!clusteringEnabled) {
        return reports.mapIndexed { idx, report ->
            CivicMarkerCluster(
                id = "single_${report.id}",
                centerLat = report.latitude ?: userLat,
                centerLng = report.longitude ?: userLng,
                reports = listOf(report)
            )
        }
    }

    val radiusMeters = getClusterRadiusMetersForZoom(zoomLevel)
    if (radiusMeters <= 0.0) {
        return reports.mapIndexed { idx, report ->
            CivicMarkerCluster(
                id = "single_${report.id}",
                centerLat = report.latitude ?: userLat,
                centerLng = report.longitude ?: userLng,
                reports = listOf(report)
            )
        }
    }

    val mutableClusters = mutableListOf<MutableCivicCluster>()

    for (report in reports) {
        val rLat = report.latitude ?: userLat
        val rLng = report.longitude ?: userLng

        var closestCluster: MutableCivicCluster? = null
        var closestDistance = Double.MAX_VALUE

        for (cluster in mutableClusters) {
            val dist = calculateDistanceMeters(cluster.centerLat, cluster.centerLng, rLat, rLng)
            if (dist <= radiusMeters && dist < closestDistance) {
                closestDistance = dist
                closestCluster = cluster
            }
        }

        if (closestCluster != null) {
            closestCluster.addReport(report, rLat, rLng)
        } else {
            mutableClusters.add(MutableCivicCluster(report, rLat, rLng))
        }
    }

    return mutableClusters.mapIndexed { index, mc ->
        mc.toImmutable("cluster_${index}_${mc.reports.size}")
    }
}

private class MutableCivicCluster(initialReport: CivicReportEntity, val initialLat: Double, val initialLng: Double) {
    var centerLat: Double = initialLat
    var centerLng: Double = initialLng
    val reports = mutableListOf(initialReport)

    fun addReport(report: CivicReportEntity, rLat: Double, rLng: Double) {
        reports.add(report)
        var sumLat = 0.0
        var sumLng = 0.0
        for (r in reports) {
            sumLat += (r.latitude ?: initialLat)
            sumLng += (r.longitude ?: initialLng)
        }
        centerLat = sumLat / reports.size
        centerLng = sumLng / reports.size
    }

    fun toImmutable(id: String): CivicMarkerCluster {
        return CivicMarkerCluster(
            id = id,
            centerLat = centerLat,
            centerLng = centerLng,
            reports = reports.toList()
        )
    }
}

/**
 * Creates a custom clustered marker bitmap drawable displaying the item count,
 * category composition, and a visual synchronization breakdown pill.
 */
fun createClusterMarkerDrawable(
    context: Context,
    cluster: CivicMarkerCluster
): BitmapDrawable {
    val size = 126
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val count = cluster.size

    // Color theme based on composition
    val (primaryColor, outerHaloColor, statusText) = when {
        cluster.draftCount > 0 && cluster.uploadedCount > 0 -> {
            Triple(
                android.graphics.Color.rgb(109, 40, 217), // Purple / Indigo mixed
                android.graphics.Color.argb(55, 109, 40, 217),
                "${cluster.draftCount}D • ${cluster.uploadedCount}U"
            )
        }
        cluster.draftCount > 0 -> {
            Triple(
                android.graphics.Color.rgb(217, 119, 6), // Amber Draft
                android.graphics.Color.argb(55, 245, 158, 11),
                "${cluster.draftCount} DRAFT"
            )
        }
        cluster.uploadedCount > 0 -> {
            Triple(
                android.graphics.Color.rgb(21, 128, 61), // Emerald Uploaded
                android.graphics.Color.argb(55, 34, 197, 94),
                "${cluster.uploadedCount} SYNCED"
            )
        }
        else -> {
            Triple(
                android.graphics.Color.rgb(29, 78, 216), // Blue Pending
                android.graphics.Color.argb(55, 37, 99, 235),
                "${cluster.pendingCount} PENDING"
            )
        }
    }

    val centerX = size / 2f
    val centerY = size / 2f + 4f

    // 1. Drop Shadow
    val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(60, 0, 0, 0)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY + 6f, 44f, shadowPaint)

    // 2. Outer Translucent Pulse Ring
    val haloPaint = Paint().apply {
        isAntiAlias = true
        color = outerHaloColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, 46f, haloPaint)

    // 3. Middle Accent Ring
    val middlePaint = Paint().apply {
        isAntiAlias = true
        color = primaryColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, 38f, middlePaint)

    // 4. Inner White / Light Core
    val corePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(centerX, centerY, 27f, corePaint)

    // 5. Cluster Count Text
    val countPaint = Paint().apply {
        isAntiAlias = true
        color = primaryColor
        textSize = if (count > 99) 24f else 28f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(count.toString(), centerX, centerY + 10f, countPaint)

    // 6. Top Status / Density Pill
    val pillBgPaint = Paint().apply {
        isAntiAlias = true
        color = primaryColor
        style = Paint.Style.FILL
    }
    val pillRect = RectF(centerX - 46f, 4f, centerX + 46f, 26f)
    canvas.drawRoundRect(pillRect, 8f, 8f, pillBgPaint)

    val pillTextPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        textSize = 12f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(statusText, centerX, 19f, pillTextPaint)

    // 7. Small Category Emoji Indicator on Bottom
    val emoji = when {
        cluster.potholeCount > 0 && cluster.drainageCount > 0 -> "🕳️🌊"
        cluster.potholeCount > 0 -> "🕳️"
        cluster.drainageCount > 0 -> "🌊"
        cluster.wasteCount > 0 -> "🗑️"
        else -> "⚠️"
    }
    val emojiPaint = Paint().apply {
        isAntiAlias = true
        textSize = 14f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(emoji, centerX, size - 4f, emojiPaint)

    return BitmapDrawable(context.resources, bitmap)
}
