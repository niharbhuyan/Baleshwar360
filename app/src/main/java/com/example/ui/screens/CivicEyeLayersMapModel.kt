package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Typeface
import android.graphics.drawable.BitmapDrawable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.WaterDamage
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.data.local.CivicReportEntity
import com.example.data.model.FirestoreUserReport
import com.example.data.municipal.BalasoreMunicipalWards
import com.example.ui.features.civic.normalizeCivicEyeCategory
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import kotlin.math.abs

/**
 * Toggleable Civic Eye map incident layers for Balasore.
 * Each layer can be independently switched ON or OFF on the OSMDroid map.
 */
enum class CivicIncidentLayer(
    val labelEn: String,
    val labelOd: String,
    val subtitleEn: String,
    val glyph: String,
    val accentColor: Color,
    val androidPinColor: Int,
    val icon: ImageVector
) {
    POTHOLES(
        labelEn = "Potholes",
        labelOd = "ରାସ୍ତା ଖାଲ",
        subtitleEn = "Road craters, damaged asphalt & culverts",
        glyph = "P",
        accentColor = Color(0xFFEA580C),
        androidPinColor = AndroidColor.rgb(234, 88, 12),
        icon = Icons.Default.ReportProblem
    ),
    SANITATION(
        labelEn = "Sanitation",
        labelOd = "ସ୍ୱଚ୍ଛତା",
        subtitleEn = "Garbage heaps, solid waste & street sweeping",
        glyph = "S",
        accentColor = Color(0xFF16A34A),
        androidPinColor = AndroidColor.rgb(22, 163, 74),
        icon = Icons.Default.DeleteSweep
    ),
    DRAINAGE(
        labelEn = "Drainage",
        labelOd = "ଡ୍ରେନେଜ୍",
        subtitleEn = "Blocked drains, monsoon waterlogging & overflow",
        glyph = "D",
        accentColor = Color(0xFF0284C7),
        androidPinColor = AndroidColor.rgb(2, 132, 199),
        icon = Icons.Default.WaterDamage
    ),
    WATER_SUPPLY(
        labelEn = "Water Supply",
        labelOd = "ପାନୀୟ ଜଳ",
        subtitleEn = "Pipe leaks, low pressure & WATCO supply outages",
        glyph = "W",
        accentColor = Color(0xFF0D9488),
        androidPinColor = AndroidColor.rgb(13, 148, 136),
        icon = Icons.Default.Opacity
    ),
    STREET_LIGHTS(
        labelEn = "Lighting",
        labelOd = "ଷ୍ଟ୍ରିଟ୍ ଲାଇଟ୍",
        subtitleEn = "Dark poles, faulty streetlights & electrical faults",
        glyph = "L",
        accentColor = Color(0xFF7C3AED),
        androidPinColor = AndroidColor.rgb(124, 58, 237),
        icon = Icons.Default.Lightbulb
    )
}

/**
 * Incident severity tiers for filtering Civic Eye map pins (Low, Medium, Critical).
 */
enum class CivicSeverityLevel(
    val labelEn: String,
    val labelOd: String,
    val accentColor: Color,
    val containerColor: Color
) {
    LOW(
        labelEn = "Low",
        labelOd = "ସାଧାରଣ",
        accentColor = Color(0xFF15803D),
        containerColor = Color(0xFFDCFCE7)
    ),
    MEDIUM(
        labelEn = "Medium",
        labelOd = "ମଧ୍ୟମ",
        accentColor = Color(0xFFB45309),
        containerColor = Color(0xFFFEF3C7)
    ),
    CRITICAL(
        labelEn = "Critical",
        labelOd = "ଜରୁରୀ",
        accentColor = Color(0xFFDC2626),
        containerColor = Color(0xFFFEE2E2)
    )
}

/**
 * Resolved map pin for a Civic Eye report plotted on the Balasore OSMDroid layers screen.
 */
data class CivicLayerMapPin(
    val report: CivicReportEntity,
    val layer: CivicIncidentLayer,
    val latitude: Double,
    val longitude: Double,
    val hasExactGps: Boolean
) {
    val geoPoint: GeoPoint get() = GeoPoint(latitude, longitude)
}

/**
 * Pure domain helpers for classifying Civic Eye incidents into toggleable map layers,
 * merging local Room + Firestore reports, resolving Balasore coordinates, and rendering
 * color-coded OSMDroid pin drawables.
 */
object CivicEyeLayersMapHelper {
    const val BALASORE_CENTER_LAT = 21.4934
    const val BALASORE_CENTER_LNG = 86.9336

    fun isValidCoordinate(lat: Double?, lng: Double?): Boolean {
        if (lat == null || lng == null) return false
        if (lat.isNaN() || lng.isNaN()) return false
        if (abs(lat) < 0.0001 && abs(lng) < 0.0001) return false
        return lat in -90.0..90.0 && lng in -180.0..180.0
    }

    /**
     * Classifies any Civic Eye report category string into one of the toggleable [CivicIncidentLayer]s.
     */
    fun classifyLayer(category: String): CivicIncidentLayer {
        val c = category.trim().lowercase()
        return when {
            c.contains("garbage") || c.contains("waste") || c.contains("sanitation") || c.contains("clean") ->
                CivicIncidentLayer.SANITATION
            c.contains("drain") || c.contains("waterlog") || c.contains("flood") || c.contains("sewer") ->
                CivicIncidentLayer.DRAINAGE
            c.contains("water supply") || c.contains("pipe") || c.contains("leak") ||
                (c.contains("water") && !c.contains("waterlog")) ->
                CivicIncidentLayer.WATER_SUPPLY
            c.contains("light") || c.contains("electric") || c.contains("lamp") || c.contains("pole") ->
                CivicIncidentLayer.STREET_LIGHTS
            else -> CivicIncidentLayer.POTHOLES
        }
    }

    /**
     * Combines Firestore cloud reports and local Room reports into a single deduplicated list,
     * preferring newer timestamps / Firestore metadata when IDs collide.
     */
    fun mergeCivicEyeReports(
        localReports: List<CivicReportEntity>,
        firestoreReports: List<FirestoreUserReport>
    ): List<CivicReportEntity> {
        val byId = LinkedHashMap<String, CivicReportEntity>()

        firestoreReports.forEach { f ->
            if (f.id.isBlank()) return@forEach
            byId[f.id] = CivicReportEntity(
                id = f.id,
                title = f.title.ifBlank { normalizeCivicEyeCategory(f.category) },
                category = normalizeCivicEyeCategory(f.category),
                wardLocation = f.wardLocation.ifBlank { "Balasore Municipality" },
                description = f.description,
                status = f.status.ifBlank { "PENDING" },
                urgency = f.urgency.ifBlank { "MEDIUM" },
                assignedDepartment = "Balasore Municipality",
                reportedTimestamp = f.reportedTimestamp,
                lastUpdatedTimestamp = f.lastUpdatedTimestamp,
                hasPhotoAttached = !f.photoUrl.isNullOrBlank() || !f.photoBase64.isNullOrBlank(),
                photoUri = f.photoUrl ?: f.photoBase64,
                latitude = f.latitude,
                longitude = f.longitude,
                geoAddress = f.geoAddress ?: f.wardLocation,
                resolutionNotes = f.resolutionNotes.orEmpty()
            )
        }

        localReports.forEach { local ->
            val existing = byId[local.id]
            if (existing == null || local.lastUpdatedTimestamp > existing.lastUpdatedTimestamp) {
                byId[local.id] = local
            }
        }

        return byId.values.sortedByDescending { it.reportedTimestamp }
    }

    /**
     * Converts a list of reports into resolved [CivicLayerMapPin] items using exact GPS coordinates
     * when present or deterministic Balasore municipal ward centroids as fallback.
     */
    fun resolvePins(
        reports: List<CivicReportEntity>,
        wardCoordsMap: Map<String, Triple<Double, Double, String>> = BalasoreMunicipalWards.getWardGeoCoordinatesMap()
    ): List<CivicLayerMapPin> {
        return reports.map { report ->
            val layer = classifyLayer(report.category)
            if (isValidCoordinate(report.latitude, report.longitude)) {
                CivicLayerMapPin(
                    report = report,
                    layer = layer,
                    latitude = report.latitude!!,
                    longitude = report.longitude!!,
                    hasExactGps = true
                )
            } else {
                val wardCoords = wardCoordsMap[report.wardLocation] ?: wardCoordsMap.entries.find {
                    report.wardLocation.contains(it.key, ignoreCase = true) ||
                        it.key.contains(report.wardLocation, ignoreCase = true)
                }?.value
                val hash = report.id.hashCode()
                val jitterLat = ((hash % 36) / 10000.0)
                val jitterLng = (((hash / 36) % 36) / 10000.0)
                CivicLayerMapPin(
                    report = report,
                    layer = layer,
                    latitude = (wardCoords?.first ?: BALASORE_CENTER_LAT) + jitterLat,
                    longitude = (wardCoords?.second ?: BALASORE_CENTER_LNG) + jitterLng,
                    hasExactGps = false
                )
            }
        }
    }

    fun filterPinsByLayers(
        pins: List<CivicLayerMapPin>,
        enabledLayers: Set<CivicIncidentLayer>
    ): List<CivicLayerMapPin> {
        if (enabledLayers.isEmpty()) return emptyList()
        return pins.filter { it.layer in enabledLayers }
    }

    /**
     * Classifies any Civic Eye report urgency/severity string into Low, Medium, or Critical.
     */
    fun classifySeverity(urgency: String): CivicSeverityLevel {
        val u = urgency.trim().uppercase()
        return when {
            u.contains("CRITICAL") || u.contains("EMERGENCY") || u.contains("HIGH") || u.contains("SEVERE") ->
                CivicSeverityLevel.CRITICAL
            u.contains("MEDIUM") || u.contains("MODERATE") ->
                CivicSeverityLevel.MEDIUM
            else -> CivicSeverityLevel.LOW
        }
    }

    /**
     * Matches a report urgency string against a filter key ("ALL", "LOW", "MEDIUM", "CRITICAL", "EMERGENCY", "HIGH").
     */
    fun matchesSeverityFilter(urgency: String, filterKey: String): Boolean {
        val key = filterKey.trim().uppercase()
        if (key == "ALL" || key.isBlank()) return true
        val level = classifySeverity(urgency)
        return when (key) {
            "LOW", "NORMAL" -> level == CivicSeverityLevel.LOW
            "MEDIUM", "MODERATE" -> level == CivicSeverityLevel.MEDIUM
            "CRITICAL", "EMERGENCY", "HIGH" -> level == CivicSeverityLevel.CRITICAL
            else -> true
        }
    }

    fun filterPinsByLayersAndSeverity(
        pins: List<CivicLayerMapPin>,
        enabledLayers: Set<CivicIncidentLayer>,
        selectedSeverity: CivicSeverityLevel?
    ): List<CivicLayerMapPin> {
        if (enabledLayers.isEmpty()) return emptyList()
        return pins.filter { pin ->
            pin.layer in enabledLayers &&
                (selectedSeverity == null || classifySeverity(pin.report.urgency) == selectedSeverity)
        }
    }

    fun countPinsBySeverity(pins: List<CivicLayerMapPin>): Map<CivicSeverityLevel, Int> {
        val counts = CivicSeverityLevel.entries.associateWith { 0 }.toMutableMap()
        pins.forEach { pin ->
            val sev = classifySeverity(pin.report.urgency)
            counts[sev] = (counts[sev] ?: 0) + 1
        }
        return counts
    }

    fun distanceMetersBetween(
        lat1: Double,
        lon1: Double,
        lat2: Double,
        lon2: Double
    ): Double {
        return com.example.data.notification.CivicProximityAlertManager.calculateDistanceMeters(
            lat1 = lat1,
            lon1 = lon1,
            lat2 = lat2,
            lon2 = lon2
        )
    }

    fun formatDistanceFromUser(meters: Double): String {
        val safe = meters.coerceAtLeast(0.0)
        return if (safe < 1000.0) {
            "${kotlin.math.round(safe).toInt()} m away"
        } else {
            String.format(java.util.Locale.US, "%.1f km away", safe / 1000.0)
        }
    }

    fun countPinsByLayer(pins: List<CivicLayerMapPin>): Map<CivicIncidentLayer, Int> {
        val counts = CivicIncidentLayer.entries.associateWith { 0 }.toMutableMap()
        pins.forEach { pin ->
            counts[pin.layer] = (counts[pin.layer] ?: 0) + 1
        }
        return counts
    }

    fun computeBoundingBox(pins: List<CivicLayerMapPin>): BoundingBox? {
        if (pins.isEmpty()) return null
        val minLat = pins.minOf { it.latitude }
        val maxLat = pins.maxOf { it.latitude }
        val minLng = pins.minOf { it.longitude }
        val maxLng = pins.maxOf { it.longitude }

        val latPad = ((maxLat - minLat) * 0.25).coerceAtLeast(0.01)
        val lngPad = ((maxLng - minLng) * 0.25).coerceAtLeast(0.01)

        return BoundingBox(
            (maxLat + latPad).coerceAtMost(85.0),
            (maxLng + lngPad).coerceAtMost(180.0),
            (minLat - latPad).coerceAtLeast(-85.0),
            (minLng - lngPad).coerceAtLeast(-180.0)
        )
    }

    /**
     * Draws a crisp teardrop OSMDroid pin color-coded by [CivicIncidentLayer], with a white inner
     * badge carrying the layer glyph, subtle drop shadow, and outer focus halo when selected.
     */
    fun createLayerMarkerDrawable(
        context: Context,
        pin: CivicLayerMapPin,
        isSelected: Boolean
    ): BitmapDrawable {
        val density = context.resources.displayMetrics.density.coerceAtLeast(1f)
        val widthPx = ((if (isSelected) 54f else 42f) * density).toInt()
        val heightPx = ((if (isSelected) 64f else 50f) * density).toInt()

        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val isResolved = pin.report.status.equals("RESOLVED", ignoreCase = true)
        val baseColor = pin.layer.androidPinColor

        val cx = widthPx / 2f
        val headRadius = (if (isSelected) 18f else 14f) * density
        val headCy = (if (isSelected) 22f else 17.5f) * density
        val tipY = heightPx - 3f * density

        if (isSelected) {
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = AndroidColor.argb(
                    72,
                    AndroidColor.red(baseColor),
                    AndroidColor.green(baseColor),
                    AndroidColor.blue(baseColor)
                )
                style = Paint.Style.FILL
            }
            canvas.drawCircle(cx, headCy, headRadius + 6f * density, haloPaint)
        }

        // Ground shadow
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(65, 15, 23, 42)
            style = Paint.Style.FILL
        }
        canvas.drawOval(
            cx - 8.5f * density,
            tipY - 3f * density,
            cx + 8.5f * density,
            tipY + 2f * density,
            shadowPaint
        )

        // Teardrop marker path
        val path = AndroidPath().apply {
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
            color = baseColor
            alpha = if (isResolved) 190 else 255
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, fillPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isResolved) AndroidColor.rgb(22, 163, 74) else AndroidColor.WHITE
            style = Paint.Style.STROKE
            strokeWidth = (if (isSelected) 2.8f else 2.2f) * density
        }
        canvas.drawPath(path, borderPaint)

        val innerCirclePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, headCy, headRadius * 0.62f, innerCirclePaint)

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = baseColor
            textSize = (if (isSelected) 13.5f else 11f) * density
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val glyph = if (isResolved) "✓" else pin.layer.glyph
        val textOffsetY = (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(glyph, cx, headCy - textOffsetY, textPaint)

        return BitmapDrawable(context.resources, bitmap)
    }

    /**
     * Draws a clean blue GPS location dot with outer accuracy ring for the user's current position.
     */
    fun createUserLocationMarkerDrawable(context: Context): BitmapDrawable {
        val density = context.resources.displayMetrics.density.coerceAtLeast(1f)
        val sizePx = (34f * density).toInt()
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = sizePx / 2f
        val cy = sizePx / 2f

        val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.argb(60, 37, 99, 235)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, 15f * density, haloPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, 8.5f * density, borderPaint)

        val corePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = AndroidColor.rgb(37, 99, 235)
            style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, 6f * density, corePaint)

        return BitmapDrawable(context.resources, bitmap)
    }
}
