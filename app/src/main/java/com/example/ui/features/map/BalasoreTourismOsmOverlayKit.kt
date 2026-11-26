package com.example.ui.features.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.GradientDrawable
import android.util.LruCache
import android.widget.TextView
import com.example.R
import com.example.data.local.HotspotEntity
import com.example.data.model.AppLanguage
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

/**
 * Visual category styling for Balasore tourism hotspots stored in the Room/Firestore database.
 */
enum class TourismPinCategory(
    val primaryColor: Int,
    val accentColor: Int,
    val labelEn: String,
    val labelOr: String
) {
    BEACH(
        primaryColor = Color.rgb(2, 132, 199),     // Ocean Blue
        accentColor = Color.rgb(56, 189, 248),
        labelEn = "BEACH",
        labelOr = "ବେଳାଭୂମି"
    ),
    TEMPLE(
        primaryColor = Color.rgb(217, 119, 6),     // Sacred Saffron Amber
        accentColor = Color.rgb(251, 191, 36),
        labelEn = "TEMPLE",
        labelOr = "ମନ୍ଦିର"
    ),
    WILDLIFE(
        primaryColor = Color.rgb(5, 150, 105),     // Forest Emerald
        accentColor = Color.rgb(52, 211, 153),
        labelEn = "WILDLIFE",
        labelOr = "ବନ୍ୟପ୍ରାଣୀ"
    ),
    HERITAGE(
        primaryColor = Color.rgb(79, 70, 229),     // Royal Indigo
        accentColor = Color.rgb(129, 140, 248),
        labelEn = "HERITAGE",
        labelOr = "ଐତିହ୍ୟ"
    ),
    PORT(
        primaryColor = Color.rgb(15, 118, 110),    // Coastal Teal
        accentColor = Color.rgb(45, 212, 191),
        labelEn = "COASTAL",
        labelOr = "ଉପକୂଳ"
    );

    companion object {
        fun resolve(category: String, name: String = ""): TourismPinCategory {
            val combined = "$category $name".lowercase()
            return when {
                combined.contains("beach") || combined.contains("chandipur") || combined.contains("talasari") -> BEACH
                combined.contains("temple") || combined.contains("shrine") || combined.contains("remuna") ||
                    combined.contains("khirachora") || combined.contains("jagannath") ||
                    combined.contains("panchalingeswar") || combined.contains("bhusandeshwar") ||
                    combined.contains("chandaneswar") -> TEMPLE
                combined.contains("wildlife") || combined.contains("eco") || combined.contains("sanctuary") ||
                    combined.contains("kuldiha") || combined.contains("mangrove") -> WILDLIFE
                combined.contains("port") || combined.contains("harbor") || combined.contains("estuary") -> PORT
                else -> HERITAGE
            }
        }
    }
}

/**
 * Generates crisp, cached vector-drawn teardrop marker icons for OSMDroid Markers.
 * Each tourism category (Beaches like Chandipur, Temples like Remuna, Wildlife like Kuldiha,
 * Heritage monuments) gets a distinct silhouette, custom glyph, selection halo, and favorite badge.
 */
object BalasoreTourismMarkerIconFactory {

    private val iconCache = LruCache<String, Bitmap>(32)

    fun createDrawable(
        context: Context,
        category: String,
        spotName: String = "",
        isSelected: Boolean = false,
        isFavorite: Boolean = false
    ): BitmapDrawable {
        val style = TourismPinCategory.resolve(category, spotName)
        val density = context.resources.displayMetrics.density.coerceIn(1.5f, 4.0f)
        val cacheKey = "${style.name}_${isSelected}_${isFavorite}_${density.toInt()}"

        val bitmap = iconCache.get(cacheKey) ?: renderPinBitmap(
            style = style,
            density = density,
            isSelected = isSelected,
            isFavorite = isFavorite
        ).also { iconCache.put(cacheKey, it) }

        return BitmapDrawable(context.resources, bitmap)
    }

    private fun renderPinBitmap(
        style: TourismPinCategory,
        density: Float,
        isSelected: Boolean,
        isFavorite: Boolean
    ): Bitmap {
        val scale = if (isSelected) 1.16f else 1.0f
        val widthPx = (48f * density * scale).toInt().coerceAtLeast(64)
        val heightPx = (60f * density * scale).toInt().coerceAtLeast(80)
        val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val cx = widthPx / 2f
        val headRadius = widthPx * 0.34f
        val cy = headRadius + (6f * density)
        val tipY = heightPx - (4f * density)

        // Selection outer halo ring
        if (isSelected) {
            val haloPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.argb(65, Color.red(style.accentColor), Color.green(style.accentColor), Color.blue(style.accentColor))
                this.style = Paint.Style.FILL
            }
            canvas.drawCircle(cx, cy, headRadius * 1.28f, haloPaint)
        }

        // Ground contact shadow ellipse
        val shadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.argb(72, 15, 23, 42)
            this.style = Paint.Style.FILL
        }
        canvas.drawOval(
            RectF(cx - headRadius * 0.42f, tipY - 3f * density, cx + headRadius * 0.42f, tipY + 2.5f * density),
            shadowPaint
        )

        // Teardrop pin silhouette path
        val pinPath = Path().apply {
            moveTo(cx, tipY)
            cubicTo(
                cx - headRadius * 0.28f, tipY - headRadius * 0.65f,
                cx - headRadius * 1.05f, cy + headRadius * 0.55f,
                cx - headRadius, cy
            )
            arcTo(
                RectF(cx - headRadius, cy - headRadius, cx + headRadius, cy + headRadius),
                180f,
                180f,
                false
            )
            cubicTo(
                cx + headRadius * 1.05f, cy + headRadius * 0.55f,
                cx + headRadius * 0.28f, tipY - headRadius * 0.65f,
                cx, tipY
            )
            close()
        }

        val pinFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.primaryColor
            this.style = Paint.Style.FILL
        }
        val pinBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (isSelected) Color.rgb(254, 240, 138) else Color.WHITE
            this.style = Paint.Style.STROKE
            strokeWidth = (if (isSelected) 2.6f else 2.0f) * density
            strokeJoin = Paint.Join.ROUND
        }
        canvas.drawPath(pinPath, pinFillPaint)
        canvas.drawPath(pinPath, pinBorderPaint)

        // Inner crisp white badge circle
        val innerRadius = headRadius * 0.68f
        val innerBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            this.style = Paint.Style.FILL
        }
        canvas.drawCircle(cx, cy, innerRadius, innerBgPaint)

        // Custom vector glyph inside the badge per tourism category
        drawCategoryGlyph(canvas, style, cx, cy, innerRadius * 0.72f, density)

        // Favorite star badge in top-right when saved in Room database
        if (isFavorite) {
            val badgeR = headRadius * 0.34f
            val bx = cx + headRadius * 0.72f
            val by = cy - headRadius * 0.68f
            val badgeBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(225, 29, 72)
                this.style = Paint.Style.FILL
            }
            val badgeStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.WHITE
                this.style = Paint.Style.STROKE
                strokeWidth = 1.4f * density
            }
            canvas.drawCircle(bx, by, badgeR, badgeBg)
            canvas.drawCircle(bx, by, badgeR, badgeStroke)

            val dotPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.rgb(254, 240, 138)
                this.style = Paint.Style.FILL
            }
            canvas.drawCircle(bx, by, badgeR * 0.38f, dotPaint)
        }

        return bitmap
    }

    private fun drawCategoryGlyph(
        canvas: Canvas,
        style: TourismPinCategory,
        cx: Float,
        cy: Float,
        r: Float,
        density: Float
    ) {
        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.primaryColor
            this.style = Paint.Style.FILL
        }
        val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = style.primaryColor
            this.style = Paint.Style.STROKE
            strokeWidth = 1.9f * density
            strokeCap = Paint.Cap.ROUND
            strokeJoin = Paint.Join.ROUND
        }

        when (style) {
            TourismPinCategory.BEACH -> {
                // Rising sun over Chandipur horizon + two crisp sea waves
                val sunPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.rgb(245, 158, 11)
                    this.style = Paint.Style.FILL
                }
                canvas.drawCircle(cx, cy - r * 0.32f, r * 0.34f, sunPaint)

                val wave1 = Path().apply {
                    moveTo(cx - r * 0.85f, cy + r * 0.16f)
                    quadTo(cx - r * 0.42f, cy - r * 0.10f, cx, cy + r * 0.16f)
                    quadTo(cx + r * 0.42f, cy + r * 0.42f, cx + r * 0.85f, cy + r * 0.16f)
                }
                val wave2 = Path().apply {
                    moveTo(cx - r * 0.72f, cy + r * 0.56f)
                    quadTo(cx - r * 0.34f, cy + r * 0.32f, cx, cy + r * 0.56f)
                    quadTo(cx + r * 0.34f, cy + r * 0.78f, cx + r * 0.72f, cy + r * 0.56f)
                }
                canvas.drawPath(wave1, strokePaint)
                canvas.drawPath(wave2, strokePaint)
            }

            TourismPinCategory.TEMPLE -> {
                // Kalinga Temple Shikhara (spire) with Kalasa finial and sanctum doorway
                val spire = Path().apply {
                    moveTo(cx, cy - r * 0.88f) // Kalasa peak
                    lineTo(cx + r * 0.38f, cy - r * 0.20f)
                    lineTo(cx + r * 0.62f, cy + r * 0.65f)
                    lineTo(cx - r * 0.62f, cy + r * 0.65f)
                    lineTo(cx - r * 0.38f, cy - r * 0.20f)
                    close()
                }
                canvas.drawPath(spire, fillPaint)
                // Kalasa finial dot
                canvas.drawCircle(cx, cy - r * 0.92f, r * 0.14f, fillPaint)
                // Base pedestal
                canvas.drawLine(cx - r * 0.78f, cy + r * 0.72f, cx + r * 0.78f, cy + r * 0.72f, strokePaint)
                // White sanctum arch cutout
                val doorPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.WHITE
                    this.style = Paint.Style.FILL
                }
                canvas.drawRoundRect(
                    RectF(cx - r * 0.18f, cy + r * 0.15f, cx + r * 0.18f, cy + r * 0.65f),
                    r * 0.16f,
                    r * 0.16f,
                    doorPaint
                )
            }

            TourismPinCategory.WILDLIFE -> {
                // Layered Sal-forest canopy tree for Kuldiha & Bichitrapur Mangroves
                val crownTop = Path().apply {
                    moveTo(cx, cy - r * 0.88f)
                    lineTo(cx + r * 0.56f, cy - r * 0.06f)
                    lineTo(cx - r * 0.56f, cy - r * 0.06f)
                    close()
                }
                val crownBottom = Path().apply {
                    moveTo(cx, cy - r * 0.36f)
                    lineTo(cx + r * 0.72f, cy + r * 0.42f)
                    lineTo(cx - r * 0.72f, cy + r * 0.42f)
                    close()
                }
                canvas.drawPath(crownTop, fillPaint)
                canvas.drawPath(crownBottom, fillPaint)
                // Tree trunk
                canvas.drawLine(cx, cy + r * 0.38f, cx, cy + r * 0.82f, strokePaint)
            }

            TourismPinCategory.PORT -> {
                // Maritime anchor glyph for Balaramgadi & Subarnarekha estuaries
                canvas.drawCircle(cx, cy - r * 0.58f, r * 0.20f, strokePaint)
                canvas.drawLine(cx, cy - r * 0.36f, cx, cy + r * 0.66f, strokePaint)
                canvas.drawLine(cx - r * 0.44f, cy - r * 0.12f, cx + r * 0.44f, cy - r * 0.12f, strokePaint)
                val flukes = Path().apply {
                    moveTo(cx - r * 0.68f, cy + r * 0.26f)
                    quadTo(cx, cy + r * 0.92f, cx + r * 0.68f, cy + r * 0.26f)
                }
                canvas.drawPath(flukes, strokePaint)
            }

            TourismPinCategory.HERITAGE -> {
                // Classical museum / memorial colonnade facade
                val roof = Path().apply {
                    moveTo(cx, cy - r * 0.76f)
                    lineTo(cx + r * 0.78f, cy - r * 0.26f)
                    lineTo(cx - r * 0.78f, cy - r * 0.26f)
                    close()
                }
                canvas.drawPath(roof, fillPaint)
                // Three pillars
                canvas.drawLine(cx - r * 0.48f, cy - r * 0.16f, cx - r * 0.48f, cy + r * 0.56f, strokePaint)
                canvas.drawLine(cx, cy - r * 0.16f, cx, cy + r * 0.56f, strokePaint)
                canvas.drawLine(cx + r * 0.48f, cy - r * 0.16f, cx + r * 0.48f, cy + r * 0.56f, strokePaint)
                // Base plinth
                canvas.drawLine(cx - r * 0.78f, cy + r * 0.66f, cx + r * 0.78f, cy + r * 0.66f, strokePaint)
            }
        }
    }
}

/**
 * Custom OSMDroid InfoWindow callout populated directly from the Tourism Module Database
 * ([HotspotEntity]). Renders bilingual name, category pill, distance from Balasore,
 * description, timings/entry fee, and interactive Details + Directions actions.
 */
class TourismHotspotInfoWindow(
    mapView: MapView,
    private val hotspot: HotspotEntity,
    private val language: AppLanguage,
    private val onViewDetails: (HotspotEntity) -> Unit,
    private val onNavigate: (HotspotEntity) -> Unit
) : InfoWindow(R.layout.osm_tourism_callout, mapView) {

    override fun onOpen(item: Any?) {
        // Ensure only one callout is open on the map at a time
        closeAllInfoWindowsOn(mMapView)

        val style = TourismPinCategory.resolve(hotspot.category, hotspot.name)
        val isOdia = language == AppLanguage.ODIA

        val tvCategory = mView.findViewById<TextView>(R.id.tv_callout_category)
        val tvDistance = mView.findViewById<TextView>(R.id.tv_callout_distance)
        val btnClose = mView.findViewById<TextView>(R.id.btn_callout_close)
        val tvTitle = mView.findViewById<TextView>(R.id.tv_callout_title)
        val tvSubtitle = mView.findViewById<TextView>(R.id.tv_callout_subtitle)
        val tvDesc = mView.findViewById<TextView>(R.id.tv_callout_description)
        val tvMeta = mView.findViewById<TextView>(R.id.tv_callout_meta)
        val btnDetails = mView.findViewById<TextView>(R.id.btn_callout_details)
        val btnDirections = mView.findViewById<TextView>(R.id.btn_callout_directions)

        tvCategory.text = if (isOdia) style.labelOr else style.labelEn
        (tvCategory.background?.mutate() as? GradientDrawable)?.setColor(style.primaryColor)

        val distLabel = if (hotspot.distanceKmFromBls > 0) {
            if (isOdia) "ବାଲେଶ୍ୱରରୁ ${hotspot.distanceKmFromBls} କି.ମି." else "${hotspot.distanceKmFromBls} km from Balasore"
        } else {
            if (isOdia) "ବାଲେଶ୍ୱର ସହର" else "Balasore Town"
        }
        tvDistance.text = distLabel

        tvTitle.text = if (isOdia && hotspot.odiaName.isNotBlank()) hotspot.odiaName else hotspot.name
        tvSubtitle.text = if (isOdia) hotspot.name else hotspot.odiaName.ifBlank { hotspot.highlights }
        tvDesc.text = hotspot.shortDescription.ifBlank { hotspot.fullDescription }

        val timingText = hotspot.timings.ifBlank { "6:00 AM - 6:00 PM" }
        val feeText = hotspot.entryFee.ifBlank { "Free Entry" }
        tvMeta.text = "$timingText • $feeText"

        btnDetails.text = if (isOdia) "ବିବରଣୀ" else "Details"
        btnDirections.text = if (isOdia) "ଦିଗ ନିର୍ଦ୍ଦେଶ" else "Directions"
        (btnDirections.background?.mutate() as? GradientDrawable)?.setColor(style.primaryColor)

        btnClose.setOnClickListener {
            close()
        }
        btnDetails.setOnClickListener {
            onViewDetails(hotspot)
            close()
        }
        btnDirections.setOnClickListener {
            onNavigate(hotspot)
        }
    }

    override fun onClose() {
        // No transient resources held beyond inflated View
    }
}

/**
 * Helper to create a configured OSMDroid [Marker] with custom category icon and database info-callout.
 */
fun buildTourismHotspotMarker(
    mapView: MapView,
    context: Context,
    hotspot: HotspotEntity,
    language: AppLanguage,
    isSelected: Boolean,
    onMarkerTapped: (HotspotEntity) -> Unit,
    onViewDetails: (HotspotEntity) -> Unit,
    onNavigate: (HotspotEntity) -> Unit
): Marker {
    return Marker(mapView).apply {
        id = hotspot.id
        position = org.osmdroid.util.GeoPoint(hotspot.latitude, hotspot.longitude)
        title = if (language == AppLanguage.ODIA && hotspot.odiaName.isNotBlank()) hotspot.odiaName else hotspot.name
        snippet = hotspot.shortDescription
        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        setInfoWindowAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_TOP)
        icon = BalasoreTourismMarkerIconFactory.createDrawable(
            context = context,
            category = hotspot.category,
            spotName = hotspot.name,
            isSelected = isSelected,
            isFavorite = hotspot.isFavorite
        )
        infoWindow = TourismHotspotInfoWindow(
            mapView = mapView,
            hotspot = hotspot,
            language = language,
            onViewDetails = onViewDetails,
            onNavigate = onNavigate
        )
        setOnMarkerClickListener { marker, map ->
            onMarkerTapped(hotspot)
            marker.showInfoWindow()
            map.controller.animateTo(marker.position)
            true
        }
    }
}
