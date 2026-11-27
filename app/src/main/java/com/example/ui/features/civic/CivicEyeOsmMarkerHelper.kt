package com.example.ui.features.civic

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import com.example.data.local.CivicReportEntity
import com.example.data.municipal.BalasoreMunicipalWards
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import kotlin.math.abs

/**
 * Resolved map pin for a Civic Eye incident plotted on OSMDroid.
 */
internal data class CivicOsmPinItem(
    val report: CivicReportEntity,
    val latitude: Double,
    val longitude: Double,
    val hasExactGps: Boolean
) {
    val geoPoint: GeoPoint get() = GeoPoint(latitude, longitude)
}

internal object CivicEyeOsmMarkerHelper {
    const val BALASORE_CENTER_LAT = 21.4934
    const val BALASORE_CENTER_LNG = 86.9135

    fun isValidCoordinate(lat: Double?, lng: Double?): Boolean {
        if (lat == null || lng == null) return false
        if (lat.isNaN() || lng.isNaN()) return false
        if (abs(lat) < 0.0001 && abs(lng) < 0.0001) return false
        return lat in -90.0..90.0 && lng in -180.0..180.0
    }

    /**
     * Resolves an incident's GeoPoint. Uses its exact GPS coordinates whenever available;
     * falls back to the reported Balasore municipal ward centroid only if older reports
     * lack GPS coordinates.
     */
    fun resolvePinItem(
        report: CivicReportEntity,
        wardCoordsMap: Map<String, Triple<Double, Double, String>> = BalasoreMunicipalWards.getWardGeoCoordinatesMap()
    ): CivicOsmPinItem {
        if (isValidCoordinate(report.latitude, report.longitude)) {
            return CivicOsmPinItem(
                report = report,
                latitude = report.latitude!!,
                longitude = report.longitude!!,
                hasExactGps = true
            )
        }

        val wardCoords = wardCoordsMap[report.wardLocation] ?: wardCoordsMap.entries.find {
            report.wardLocation.contains(it.key, ignoreCase = true) ||
                it.key.contains(report.wardLocation, ignoreCase = true)
        }?.value

        val hash = report.id.hashCode()
        val jitterLat = ((hash % 40) / 10000.0)
        val jitterLng = (((hash / 40) % 40) / 10000.0)

        val baseLat = wardCoords?.first ?: BALASORE_CENTER_LAT
        val baseLng = wardCoords?.second ?: BALASORE_CENTER_LNG

        return CivicOsmPinItem(
            report = report,
            latitude = baseLat + jitterLat,
            longitude = baseLng + jitterLng,
            hasExactGps = false
        )
    }

    fun matchesMapCategoryFilter(report: CivicReportEntity, filter: String): Boolean {
        return when (filter) {
            "ALL" -> true
            "POTHOLE" -> report.category.contains("Pothole", ignoreCase = true) ||
                report.category.contains("Road", ignoreCase = true)
            "WATERLOGGING" -> report.category.contains("Waterlog", ignoreCase = true) ||
                report.category.contains("Drain", ignoreCase = true)
            "LIGHTING", "ELECTRICITY", "STREETLIGHT" ->
                report.category.contains("Streetlight", ignoreCase = true) ||
                    report.category.contains("Light", ignoreCase = true) ||
                    report.category.contains("Electric", ignoreCase = true)
            "WATER_SUPPLY", "WATER_LEAK" ->
                report.category.contains("Leak", ignoreCase = true) ||
                    (report.category.contains("Water", ignoreCase = true) &&
                        !report.category.contains("Waterlog", ignoreCase = true))
            "WASTE", "GARBAGE" ->
                report.category.contains("Garbage", ignoreCase = true) ||
                    report.category.contains("Waste", ignoreCase = true) ||
                    report.category.contains("Sanitation", ignoreCase = true)
            "ROAD" -> report.category.contains("Road", ignoreCase = true) ||
                report.category.contains("Culvert", ignoreCase = true)
            else -> true
        }
    }

    fun matchesMapStatusFilter(report: CivicReportEntity, filter: String): Boolean {
        return when (filter) {
            "ALL" -> true
            "PENDING" -> report.status.equals("PENDING", ignoreCase = true) ||
                report.status.equals("SUBMITTED", ignoreCase = true) ||
                report.status.equals("DRAFT", ignoreCase = true)
            "IN_PROGRESS" -> report.status.equals("IN_PROGRESS", ignoreCase = true)
            "RESOLVED" -> report.status.equals("RESOLVED", ignoreCase = true)
            else -> true
        }
    }

    fun computeBoundingBox(pins: List<CivicOsmPinItem>): BoundingBox? {
        if (pins.isEmpty()) return null
        val minLat = pins.minOf { it.latitude }
        val maxLat = pins.maxOf { it.latitude }
        val minLng = pins.minOf { it.longitude }
        val maxLng = pins.maxOf { it.longitude }

        val latPad = ((maxLat - minLat) * 0.22).coerceAtLeast(0.008)
        val lngPad = ((maxLng - minLng) * 0.22).coerceAtLeast(0.008)

        return BoundingBox(
            (maxLat + latPad).coerceAtMost(85.0),
            (maxLng + lngPad).coerceAtMost(180.0),
            (minLat - latPad).coerceAtLeast(-85.0),
            (minLng - lngPad).coerceAtLeast(-180.0)
        )
    }

    /**
     * Draws a crisp, high-contrast teardrop OSMDroid marker bitmap color-coded by status & urgency,
     * with a short category glyph inside and an outer pulse halo when selected.
     */
    fun createIncidentMarkerDrawable(
        context: Context,
        report: CivicReportEntity,
        isSelected: Boolean
    ): BitmapDrawable {
        val density = context.resources.displayMetrics.density.coerceAtLeast(1f)
        val widthPx = ((if (isSelected) 54f else 44f) * density).toInt()
        val heightPx = ((if (isSelected) 64f else 52f) * density).toInt()

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val pinColor = when {
            report.status.equals("RESOLVED", ignoreCase = true) -> AndroidColor.rgb(22, 163, 74)
            report.urgency.equals("EMERGENCY", ignoreCase = true) ||
                report.urgency.equals("CRITICAL", ignoreCase = true) -> AndroidColor.rgb(220, 38, 38)
            report.urgency.equals("HIGH", ignoreCase = true) -> AndroidColor.rgb(234, 88, 12)
            report.status.equals("IN_PROGRESS", ignoreCase = true) -> AndroidColor.rgb(2, 132, 199)
            else -> AndroidColor.rgb(217, 119, 6)
        }

        val cx = widthPx / 2f
        val headRadius = (if (isSelected) 18f else 14.5f) * density
        val headCy = (if (isSelected) 22f else 18f) * density
        val tipY = heightPx - 3f * density

        // Selection outer halo
        if (isSelected) {
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.argb(
                    70,
                    AndroidColor.red(pinColor),
                    AndroidColor.green(pinColor),
                    AndroidColor.blue(pinColor)
                )
                style = Paint.Style.FILL
            }
            canvas.drawCircle(cx, headCy, headRadius + 6f * density, haloPaint)
        }

        // Ground shadow ellipse
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(65, 15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawOval(
            cx - 9f * density,
            tipY - 3.5f * density,
            cx + 9f * density,
            tipY + 2f * density,
            shadowPaint
        )

        // Teardrop silhouette
        val teardropPath = AndroidPath().apply {
            moveTo(cx, tipY)
            cubicTo(
                cx - headRadius * 0.95f,
                headCy + headRadius * 0.85f,
                cx - headRadius,
                headCy + headRadius * 0.15f,
                cx - headRadius,
                headCy
            )
            arcTo(
                cx - headRadius,
                headCy - headRadius,
                cx + headRadius,
                headCy + headRadius,
                180f,
                180f,
                false
            )
            cubicTo(
                cx + headRadius,
                headCy + headRadius * 0.15f,
                cx + headRadius * 0.95f,
                headCy + headRadius * 0.85f,
                cx,
                tipY
            )
            close()
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pinColor
            style = Paint.Style.FILL
        }
        canvas.drawPath(teardropPath, fillPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.STROKE
            strokeWidth = (if (isSelected) 2.8f else 2.2f) * density
        }
        canvas.drawPath(teardropPath, borderPaint)

        // Inner white circle
        val innerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, headCy, headRadius * 0.62f, innerPaint)

        // Short category letter/symbol inside pin head
        val glyph = when {
            report.category.contains("Pothole", ignoreCase = true) -> "P"
            report.category.contains("Drain", ignoreCase = true) ||
                report.category.contains("Waterlog", ignoreCase = true) -> "D"
            report.category.contains("Light", ignoreCase = true) ||
                report.category.contains("Electric", ignoreCase = true) -> "L"
            report.category.contains("Water", ignoreCase = true) ||
                report.category.contains("Leak", ignoreCase = true) -> "W"
            report.category.contains("Waste", ignoreCase = true) ||
                report.category.contains("Garbage", ignoreCase = true) -> "G"
            report.category.contains("Road", ignoreCase = true) -> "R"
            else -> "!"
        }
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = pinColor
            textSize = (if (isSelected) 13.5f else 11f) * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val textOffsetY = (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(glyph, cx, headCy - textOffsetY, textPaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
