package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.maps.BalasoreGeoNewsLocation
import com.example.data.maps.BalasoreInteractiveMapDataSource
import com.example.data.maps.BalasoreWeatherAlertZone
import com.example.data.model.AppLanguage
import com.example.data.model.BalasoreTransitDataSource
import com.example.data.model.BusRouteOverlay
import com.example.data.model.RailwayStationNode
import com.example.data.model.TrainStatusMarker
import com.example.data.remote.LiveCityBusDto
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import kotlin.math.hypot

/**
 * Coordinate-to-screen projector function for Balasore District geographical canvas.
 */
fun geoToCanvasOffset(
    lat: Double,
    lng: Double,
    minLat: Double,
    maxLat: Double,
    minLng: Double,
    maxLng: Double,
    canvasWidth: Float,
    canvasHeight: Float,
    zoom: Float,
    pan: Offset
): Offset {
    val normX = ((lng - minLng) / (maxLng - minLng)).toFloat()
    val normY = 1f - ((lat - minLat) / (maxLat - minLat)).toFloat()

    val screenX = (normX * canvasWidth * zoom) + pan.x
    val screenY = (normY * canvasHeight * zoom) + pan.y
    return Offset(screenX, screenY)
}

/**
 * High-performance, offline-first 2D Vector District Map of Balasore.
 * Renders local topography, Bay of Bengal coastline, Budhabalanga & Subarnarekha rivers,
 * NH16 highway, railway corridor, live train status markers, bus routes with stops,
 * live GPS city buses, geotagged news locations, geospatial weather alert areas,
 * and tourism hotspots with tap detection.
 */
@Composable
fun BalasoreDistrictVectorMap(
    activeExploreMode: String,
    selectedHotspotCategory: String,
    selectedHotspot: TouristLocationItem?,
    onSelectHotspot: (TouristLocationItem) -> Unit,
    selectedBusRoute: BusRouteOverlay?,
    onSelectBusRoute: (BusRouteOverlay) -> Unit,
    selectedTrain: TrainStatusMarker?,
    onSelectTrain: (TrainStatusMarker) -> Unit,
    selectedStation: RailwayStationNode?,
    onSelectStation: (RailwayStationNode) -> Unit,
    language: AppLanguage,
    modifier: Modifier = Modifier,
    liveBuses: List<LiveCityBusDto> = emptyList(),
    showLiveGpsBusLayer: Boolean = true,
    selectedLiveBus: LiveCityBusDto? = null,
    onSelectLiveBus: (LiveCityBusDto) -> Unit = {},
    newsLocations: List<BalasoreGeoNewsLocation> = BalasoreInteractiveMapDataSource.BALASORE_GEO_NEWS_LOCATIONS,
    selectedNewsLocation: BalasoreGeoNewsLocation? = null,
    onSelectNewsLocation: (BalasoreGeoNewsLocation) -> Unit = {},
    weatherAlertZones: List<BalasoreWeatherAlertZone> = BalasoreInteractiveMapDataSource.BALASORE_WEATHER_ALERT_ZONES,
    selectedWeatherAlertZone: BalasoreWeatherAlertZone? = null,
    onSelectWeatherAlertZone: (BalasoreWeatherAlertZone) -> Unit = {}
) {
    // District Geographic Bounding Box
    val minLat = 21.28
    val maxLat = 21.72
    val minLng = 86.62
    val maxLng = 87.26

    // Pan and Zoom State
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    val transformableState = rememberTransformableState { zoomChange, offsetChange, _ ->
        zoomScale = (zoomScale * zoomChange).coerceIn(0.75f, 4.0f)
        panOffset += offsetChange
    }

    // Filter hotspots
    val filteredHotspots = remember(selectedHotspotCategory) {
        if (selectedHotspotCategory == "ALL") {
            BALASORE_TOURIST_LOCATIONS
        } else {
            BALASORE_TOURIST_LOCATIONS.filter { it.categoryGroup == selectedHotspotCategory }
        }
    }

    // Animation transition for pulsing train & live bus GPS beacons
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_train")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 22f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_radius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulse_alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(18.dp))
            .background(Color(0xFFF1F5F9))
            .transformable(state = transformableState)
            .testTag("balasore_vector_map_container")
    ) {
        // Core Vector Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .testTag("balasore_vector_map_canvas")
                .pointerInput(
                    activeExploreMode,
                    filteredHotspots,
                    selectedBusRoute,
                    liveBuses,
                    showLiveGpsBusLayer,
                    newsLocations,
                    weatherAlertZones,
                    zoomScale,
                    panOffset
                ) {
                    detectTapGestures { tapOffset ->
                        val cWidth = size.width.toFloat()
                        val cHeight = size.height.toFloat()
                        if (cWidth <= 0 || cHeight <= 0) return@detectTapGestures

                        var handled = false

                        // 0A. Hit test News Location markers when News or All Overlays active
                        if (!handled && (activeExploreMode == "NEWS_LOCATIONS" || activeExploreMode == "ALL_OVERLAYS")) {
                            for (newsLoc in newsLocations) {
                                val pt = geoToCanvasOffset(
                                    newsLoc.latitude, newsLoc.longitude,
                                    minLat, maxLat, minLng, maxLng,
                                    cWidth, cHeight, zoomScale, panOffset
                                )
                                if (hypot(tapOffset.x - pt.x, tapOffset.y - pt.y) < 34f) {
                                    onSelectNewsLocation(newsLoc)
                                    handled = true
                                    break
                                }
                            }
                        }

                        // 0B. Hit test Weather Alert Area center pins & radar zones when Weather Alerts or All Overlays active
                        if (!handled && (activeExploreMode == "WEATHER_ALERTS" || activeExploreMode == "ALL_OVERLAYS")) {
                            for (zone in weatherAlertZones) {
                                val pt = geoToCanvasOffset(
                                    zone.centerLat, zone.centerLng,
                                    minLat, maxLat, minLng, maxLng,
                                    cWidth, cHeight, zoomScale, panOffset
                                )
                                val hitRadius = if (activeExploreMode == "WEATHER_ALERTS") 48f * zoomScale else 34f
                                if (hypot(tapOffset.x - pt.x, tapOffset.y - pt.y) < hitRadius) {
                                    onSelectWeatherAlertZone(zone)
                                    handled = true
                                    break
                                }
                            }
                        }

                        // 0C. Hit test live GPS Bus vehicle markers
                        if (!handled && showLiveGpsBusLayer && (activeExploreMode == "BUS_ROUTES" || activeExploreMode == "ALL_OVERLAYS")) {
                            for (bus in liveBuses) {
                                val pt = geoToCanvasOffset(
                                    bus.latitude, bus.longitude,
                                    minLat, maxLat, minLng, maxLng,
                                    cWidth, cHeight, zoomScale, panOffset
                                )
                                if (hypot(tapOffset.x - pt.x, tapOffset.y - pt.y) < 34f) {
                                    onSelectLiveBus(bus)
                                    handled = true
                                    break
                                }
                            }
                        }

                        // 1. Hit test live train markers
                        if (!handled && (activeExploreMode == "TRAIN_STATUS" || activeExploreMode == "ALL_OVERLAYS")) {
                            for (train in BalasoreTransitDataSource.LIVE_TRAIN_STATUSES) {
                                val pt = geoToCanvasOffset(
                                    train.currentLat, train.currentLng,
                                    minLat, maxLat, minLng, maxLng,
                                    cWidth, cHeight, zoomScale, panOffset
                                )
                                if (hypot(tapOffset.x - pt.x, tapOffset.y - pt.y) < 32f) {
                                    onSelectTrain(train)
                                    handled = true
                                    break
                                }
                            }
                        }

                        // 2. Hit test railway stations
                        if (!handled && (activeExploreMode == "TRAIN_STATUS" || activeExploreMode == "ALL_OVERLAYS")) {
                            for (stn in BalasoreTransitDataSource.BALASORE_RAILWAY_STATIONS) {
                                val pt = geoToCanvasOffset(
                                    stn.lat, stn.lng,
                                    minLat, maxLat, minLng, maxLng,
                                    cWidth, cHeight, zoomScale, panOffset
                                )
                                if (hypot(tapOffset.x - pt.x, tapOffset.y - pt.y) < 28f) {
                                    onSelectStation(stn)
                                    handled = true
                                    break
                                }
                            }
                        }

                        // 3. Hit test hotspot markers
                        if (!handled && (activeExploreMode == "HOTSPOTS" || activeExploreMode == "ALL_OVERLAYS")) {
                            for (spot in filteredHotspots) {
                                val pt = geoToCanvasOffset(
                                    spot.latitude, spot.longitude,
                                    minLat, maxLat, minLng, maxLng,
                                    cWidth, cHeight, zoomScale, panOffset
                                )
                                if (hypot(tapOffset.x - pt.x, tapOffset.y - pt.y) < 32f) {
                                    onSelectHotspot(spot)
                                    handled = true
                                    break
                                }
                            }
                        }

                        // 4. Hit test bus route lines or stops
                        if (!handled && (activeExploreMode == "BUS_ROUTES" || activeExploreMode == "ALL_OVERLAYS")) {
                            // Check stops first
                            for (route in BalasoreTransitDataSource.BALASORE_BUS_ROUTES) {
                                for (stop in route.stops) {
                                    val pt = geoToCanvasOffset(
                                        stop.lat, stop.lng,
                                        minLat, maxLat, minLng, maxLng,
                                        cWidth, cHeight, zoomScale, panOffset
                                    )
                                    if (hypot(tapOffset.x - pt.x, tapOffset.y - pt.y) < 26f) {
                                        onSelectBusRoute(route)
                                        handled = true
                                        break
                                    }
                                }
                                if (handled) break
                            }

                            // Check route polylines
                            if (!handled) {
                                for (route in BalasoreTransitDataSource.BALASORE_BUS_ROUTES) {
                                    val pts = route.pathPoints.map {
                                        geoToCanvasOffset(
                                            it.latitude, it.longitude,
                                            minLat, maxLat, minLng, maxLng,
                                            cWidth, cHeight, zoomScale, panOffset
                                        )
                                    }
                                    for (i in 0 until pts.size - 1) {
                                        val dist = distanceToSegment(tapOffset, pts[i], pts[i + 1])
                                        if (dist < 28f) {
                                            onSelectBusRoute(route)
                                            handled = true
                                            break
                                        }
                                    }
                                    if (handled) break
                                }
                            }
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height

            // 1. Draw Land Base
            drawRect(color = Color(0xFFF8FAFC))

            // 2. Draw Bay of Bengal Coastline & Sea (East/Southeast)
            val coastPt1 = geoToCanvasOffset(21.72, 87.12, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset)
            val coastPt2 = geoToCanvasOffset(21.58, 87.08, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset)
            val coastPt3 = geoToCanvasOffset(21.47, 87.02, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset) // Chandipur
            val coastPt4 = geoToCanvasOffset(21.35, 86.92, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset)
            val coastPt5 = geoToCanvasOffset(21.28, 86.88, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset)

            val seaPath = Path().apply {
                moveTo(coastPt1.x, coastPt1.y)
                cubicTo(coastPt2.x, coastPt2.y, coastPt3.x, coastPt3.y, coastPt4.x, coastPt4.y)
                lineTo(coastPt5.x, coastPt5.y)
                lineTo(width * 2f, coastPt5.y)
                lineTo(width * 2f, 0f)
                close()
            }
            drawPath(path = seaPath, color = Color(0xFFE0F2FE)) // Soft Bay of Bengal Blue

            // 3. Chandipur Receding Mudflat (Vanishing Sea Zone)
            val mudflatCenter = geoToCanvasOffset(21.47, 87.05, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset)
            drawOval(
                color = Color(0xFFBAE6FD).copy(alpha = 0.55f),
                topLeft = Offset(mudflatCenter.x - 35f * zoomScale, mudflatCenter.y - 18f * zoomScale),
                size = androidx.compose.ui.geometry.Size(70f * zoomScale, 36f * zoomScale)
            )

            // 4. Budhabalanga River Stream
            val riverPts = listOf(
                geoToCanvasOffset(21.60, 86.75, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset),
                geoToCanvasOffset(21.52, 86.85, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset),
                geoToCanvasOffset(21.49, 86.93, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset), // Balasore Town
                geoToCanvasOffset(21.47, 87.02, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset)  // Balaramgadi
            )
            val riverPath = Path().apply {
                moveTo(riverPts[0].x, riverPts[0].y)
                for (i in 1 until riverPts.size) {
                    lineTo(riverPts[i].x, riverPts[i].y)
                }
            }
            drawPath(
                path = riverPath,
                color = Color(0xFF7DD3FC),
                style = Stroke(width = 4.5f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // 5. National Highway 16 (Chennai-Kolkata)
            val nh16Pts = listOf(
                geoToCanvasOffset(21.30, 86.72, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset),
                geoToCanvasOffset(21.40, 86.82, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset),
                geoToCanvasOffset(21.49, 86.92, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset), // Balasore Bypass
                geoToCanvasOffset(21.60, 87.05, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset),
                geoToCanvasOffset(21.72, 87.18, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset)
            )
            val nhPath = Path().apply {
                moveTo(nh16Pts[0].x, nh16Pts[0].y)
                for (i in 1 until nh16Pts.size) {
                    lineTo(nh16Pts[i].x, nh16Pts[i].y)
                }
            }
            drawPath(
                path = nhPath,
                color = Color(0xFFCBD5E1),
                style = Stroke(width = 6f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                path = nhPath,
                color = Color(0xFFF1F5F9),
                style = Stroke(
                    width = 2.5f * zoomScale,
                    cap = StrokeCap.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f), 0f)
                )
            )

            // 6. Railway Track (East Coast Mainline Corridor)
            val railPts = BalasoreTransitDataSource.BALASORE_RAIL_CORRIDOR_POINTS.map {
                geoToCanvasOffset(it.latitude, it.longitude, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset)
            }
            if (railPts.isNotEmpty()) {
                val railPath = Path().apply {
                    moveTo(railPts[0].x, railPts[0].y)
                    for (i in 1 until railPts.size) {
                        lineTo(railPts[i].x, railPts[i].y)
                    }
                }
                // Base rail line
                drawPath(
                    path = railPath,
                    color = Color(0xFF64748B),
                    style = Stroke(width = 4f * zoomScale, cap = StrokeCap.Square)
                )
                // Ties / Dashes
                drawPath(
                    path = railPath,
                    color = Color.White,
                    style = Stroke(
                        width = 2f * zoomScale,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                    )
                )
            }

            // 7. Bus Routes Polylines (if Bus or All mode)
            if (activeExploreMode == "BUS_ROUTES" || activeExploreMode == "ALL_OVERLAYS") {
                for (route in BalasoreTransitDataSource.BALASORE_BUS_ROUTES) {
                    val isSelected = selectedBusRoute?.id == route.id
                    val rPts = route.pathPoints.map {
                        geoToCanvasOffset(it.latitude, it.longitude, minLat, maxLat, minLng, maxLng, width, height, zoomScale, panOffset)
                    }
                    if (rPts.size >= 2) {
                        val rPath = Path().apply {
                            moveTo(rPts[0].x, rPts[0].y)
                            for (i in 1 until rPts.size) {
                                lineTo(rPts[i].x, rPts[i].y)
                            }
                        }

                        // Route line
                        val strokeW = if (isSelected) 8.5f * zoomScale else 4.5f * zoomScale
                        val rColor = if (isSelected) Color(0xFF0284C7) else Color(route.colorArgb)

                        if (isSelected) {
                            // Outer glow
                            drawPath(
                                path = rPath,
                                color = rColor.copy(alpha = 0.35f),
                                style = Stroke(width = strokeW + 7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                            )
                        }

                        drawPath(
                            path = rPath,
                            color = rColor,
                            style = Stroke(width = strokeW, cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )

                        // Draw bus stops along selected route
                        if (isSelected || activeExploreMode == "BUS_ROUTES") {
                            for (stop in route.stops) {
                                val sPt = geoToCanvasOffset(
                                    stop.lat, stop.lng,
                                    minLat, maxLat, minLng, maxLng,
                                    width, height, zoomScale, panOffset
                                )
                                drawCircle(
                                    color = Color.White,
                                    radius = 4.5f * zoomScale,
                                    center = sPt
                                )
                                drawCircle(
                                    color = rColor,
                                    radius = 3.0f * zoomScale,
                                    center = sPt
                                )
                            }
                        }
                    }
                }
            }

            // 8. Railway Stations
            if (activeExploreMode == "TRAIN_STATUS" || activeExploreMode == "ALL_OVERLAYS") {
                for (stn in BalasoreTransitDataSource.BALASORE_RAILWAY_STATIONS) {
                    val stnPt = geoToCanvasOffset(
                        stn.lat, stn.lng,
                        minLat, maxLat, minLng, maxLng,
                        width, height, zoomScale, panOffset
                    )
                    val isSel = selectedStation?.id == stn.id
                    drawCircle(
                        color = Color(0xFFDC2626),
                        radius = if (isSel) 7.5f * zoomScale else 5.5f * zoomScale,
                        center = stnPt
                    )
                    drawCircle(
                        color = Color.White,
                        radius = if (isSel) 4.5f * zoomScale else 3.0f * zoomScale,
                        center = stnPt
                    )
                }
            }

            // 9. Live Train Markers
            if (activeExploreMode == "TRAIN_STATUS" || activeExploreMode == "ALL_OVERLAYS") {
                for (train in BalasoreTransitDataSource.LIVE_TRAIN_STATUSES) {
                    val tPt = geoToCanvasOffset(
                        train.currentLat, train.currentLng,
                        minLat, maxLat, minLng, maxLng,
                        width, height, zoomScale, panOffset
                    )
                    val isSel = selectedTrain?.id == train.id

                    // Pulsing radar beacon
                    drawCircle(
                        color = (if (train.statusType == "DELAYED") Color(0xFFEAB308) else Color(0xFFEF4444)).copy(alpha = pulseAlpha),
                        radius = pulseRadius * zoomScale,
                        center = tPt
                    )

                    // Train Center Pin
                    val pinColor = if (train.statusType == "DELAYED") Color(0xFFCA8A04) else Color(0xFFDC2626)
                    drawCircle(
                        color = Color.White,
                        radius = if (isSel) 10.5f * zoomScale else 8f * zoomScale,
                        center = tPt
                    )
                    drawCircle(
                        color = pinColor,
                        radius = if (isSel) 8.5f * zoomScale else 6.5f * zoomScale,
                        center = tPt
                    )

                    // Train number badge text via native canvas
                    val paint = android.graphics.Paint().apply {
                        color = android.graphics.Color.DKGRAY
                        textSize = (9f * zoomScale).coerceIn(8f, 16f)
                        isFakeBoldText = true
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        train.trainNumber,
                        tPt.x + (10f * zoomScale),
                        tPt.y - (4f * zoomScale),
                        paint
                    )
                }
            }

            // 10. Tourism Hotspots, Temples, Beaches, Landmarks
            if (activeExploreMode == "HOTSPOTS" || activeExploreMode == "ALL_OVERLAYS") {
                for (spot in filteredHotspots) {
                    val spotPt = geoToCanvasOffset(
                        spot.latitude, spot.longitude,
                        minLat, maxLat, minLng, maxLng,
                        width, height, zoomScale, panOffset
                    )
                    val isSel = selectedHotspot?.id == spot.id

                    val pinColor = when (spot.categoryGroup) {
                        "TEMPLE" -> Color(0xFFEA580C)  // Saffron Orange
                        "BEACH" -> Color(0xFF0284C7)   // Ocean Blue
                        "LANDMARK" -> Color(0xFF7C3AED) // Violet
                        "NATURE" -> Color(0xFF059669)  // Emerald
                        else -> Color(0xFFCA8A04)
                    }

                    if (isSel) {
                        drawCircle(
                            color = AmberGold.copy(alpha = 0.4f),
                            radius = 16f * zoomScale,
                            center = spotPt
                        )
                    }

                    // Hotspot Marker Pin
                    drawCircle(
                        color = Color.White,
                        radius = if (isSel) 9.5f * zoomScale else 7f * zoomScale,
                        center = spotPt
                    )
                    drawCircle(
                        color = pinColor,
                        radius = if (isSel) 7.5f * zoomScale else 5.5f * zoomScale,
                        center = spotPt
                    )

                    // Text Label
                    if (isSel || zoomScale > 1.35f) {
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.BLACK
                            textSize = (10f * zoomScale).coerceIn(9f, 15f)
                            isFakeBoldText = isSel
                        }
                        val spotName = if (language == AppLanguage.ODIA) spot.nameOr else spot.nameEn
                        val shortName = if (spotName.length > 18) spotName.take(16) + "…" else spotName
                        drawContext.canvas.nativeCanvas.drawText(
                            shortName,
                            spotPt.x + (9f * zoomScale),
                            spotPt.y + (3f * zoomScale),
                            textPaint
                        )
                    }
                }
            }

            // 11. Live GPS Bus Vehicles (CRUT Mo Bus & OSRTC Real-Time Feed)
            if (showLiveGpsBusLayer && (activeExploreMode == "BUS_ROUTES" || activeExploreMode == "ALL_OVERLAYS")) {
                for (bus in liveBuses) {
                    val bPt = geoToCanvasOffset(
                        bus.latitude, bus.longitude,
                        minLat, maxLat, minLng, maxLng,
                        width, height, zoomScale, panOffset
                    )
                    val isSelBus = selectedLiveBus?.busId == bus.busId

                    // Pulsing GPS radar ring around each live moving vehicle
                    val radarColor = if (bus.etaNextStopMins <= 3) EmeraldGreen else OceanBlue
                    drawCircle(
                        color = radarColor.copy(alpha = pulseAlpha),
                        radius = (pulseRadius * 1.15f) * zoomScale,
                        center = bPt
                    )

                    if (isSelBus) {
                        drawCircle(
                            color = AmberGold.copy(alpha = 0.55f),
                            radius = 16f * zoomScale,
                            center = bPt
                        )
                    }

                    // Vehicle marker outer ring & core
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelBus) 11f * zoomScale else 8.5f * zoomScale,
                        center = bPt
                    )
                    drawCircle(
                        color = if (isSelBus) Color(0xFF047857) else radarColor,
                        radius = if (isSelBus) 9f * zoomScale else 6.8f * zoomScale,
                        center = bPt
                    )

                    // Route & ETA label
                    val busLabelPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.rgb(6, 78, 59)
                        textSize = (9.5f * zoomScale).coerceIn(8.5f, 15f)
                        isFakeBoldText = true
                    }
                    drawContext.canvas.nativeCanvas.drawText(
                        "🚌 ${bus.routeNumber} (${bus.etaNextStopMins}m)",
                        bPt.x + (10f * zoomScale),
                        bPt.y + (4f * zoomScale),
                        busLabelPaint
                    )
                }
            }

            // 12. Geospatial Weather Alert Areas (Radar Zones + Perimeter + Center Pin)
            if (activeExploreMode == "WEATHER_ALERTS" || activeExploreMode == "ALL_OVERLAYS") {
                for (zone in weatherAlertZones) {
                    val zPt = geoToCanvasOffset(
                        zone.centerLat, zone.centerLng,
                        minLat, maxLat, minLng, maxLng,
                        width, height, zoomScale, panOffset
                    )
                    val isSelZone = selectedWeatherAlertZone?.id == zone.id

                    val zoneColor = when (zone.severity) {
                        "CRITICAL" -> Color(0xFFDC2626) // Crimson Red
                        "SEVERE" -> Color(0xFFEA580C)   // Deep Orange
                        else -> Color(0xFFD97706)       // Amber Warning
                    }

                    // Convert radiusMeters (~4000-6000m) to canvas pixel radius
                    val basePixelRadius = ((zone.radiusMeters / 135.0).toFloat()).coerceIn(26f, 52f) * zoomScale

                    // Translucent alert zone fill
                    drawCircle(
                        color = zoneColor.copy(alpha = if (isSelZone) 0.28f else 0.16f),
                        radius = basePixelRadius,
                        center = zPt
                    )

                    // Dashed alert zone perimeter ring
                    drawCircle(
                        color = zoneColor.copy(alpha = if (isSelZone) 0.95f else 0.65f),
                        radius = basePixelRadius,
                        center = zPt,
                        style = Stroke(
                            width = (if (isSelZone) 2.6f else 1.6f) * zoomScale,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 6f), 0f)
                        )
                    )

                    // Center Weather Alert Pin
                    drawCircle(
                        color = Color.White,
                        radius = if (isSelZone) 10.5f * zoomScale else 8f * zoomScale,
                        center = zPt
                    )
                    drawCircle(
                        color = zoneColor,
                        radius = if (isSelZone) 8.5f * zoomScale else 6.2f * zoomScale,
                        center = zPt
                    )

                    if (isSelZone || activeExploreMode == "WEATHER_ALERTS" || zoomScale > 1.3f) {
                        val wxPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.rgb(153, 27, 27)
                            textSize = (9.5f * zoomScale).coerceIn(8.5f, 15f)
                            isFakeBoldText = true
                        }
                        val zoneShort = (if (language == AppLanguage.ODIA) zone.zoneNameOr else zone.zoneNameEn).take(18)
                        drawContext.canvas.nativeCanvas.drawText(
                            "${zone.emoji} $zoneShort",
                            zPt.x + (10f * zoomScale),
                            zPt.y - (5f * zoomScale),
                            wxPaint
                        )
                    }
                }
            }

            // 13. Geotagged Local News Locations (Press Pins + Breaking Pulse + Label)
            if (activeExploreMode == "NEWS_LOCATIONS" || activeExploreMode == "ALL_OVERLAYS") {
                for (newsLoc in newsLocations) {
                    val nPt = geoToCanvasOffset(
                        newsLoc.latitude, newsLoc.longitude,
                        minLat, maxLat, minLng, maxLng,
                        width, height, zoomScale, panOffset
                    )
                    val isSelNews = selectedNewsLocation?.id == newsLoc.id
                    val newsPinColor = if (newsLoc.isBreaking) Color(0xFFBE123C) else Color(0xFF4F46E5)

                    if (newsLoc.isBreaking || isSelNews) {
                        drawCircle(
                            color = newsPinColor.copy(alpha = pulseAlpha),
                            radius = (pulseRadius * 1.1f) * zoomScale,
                            center = nPt
                        )
                    }

                    if (isSelNews) {
                        drawCircle(
                            color = AmberGold.copy(alpha = 0.55f),
                            radius = 15f * zoomScale,
                            center = nPt
                        )
                    }

                    drawCircle(
                        color = Color.White,
                        radius = if (isSelNews) 10.5f * zoomScale else 8f * zoomScale,
                        center = nPt
                    )
                    drawCircle(
                        color = newsPinColor,
                        radius = if (isSelNews) 8.5f * zoomScale else 6.2f * zoomScale,
                        center = nPt
                    )

                    if (isSelNews || activeExploreMode == "NEWS_LOCATIONS" || zoomScale > 1.25f) {
                        val newsPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.rgb(49, 46, 129)
                            textSize = (9.5f * zoomScale).coerceIn(8.5f, 15f)
                            isFakeBoldText = true
                        }
                        val locLabel = (if (language == AppLanguage.ODIA) newsLoc.locationNameOr else newsLoc.locationNameEn).take(18)
                        drawContext.canvas.nativeCanvas.drawText(
                            "📰 $locLabel",
                            nPt.x + (10f * zoomScale),
                            nPt.y + (4f * zoomScale),
                            newsPaint
                        )
                    }
                }
            }
        }

        // Top Status Badge: District Vector Map Engine Status
        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(10.dp),
            shape = RoundedCornerShape(10.dp),
            color = BentoSlate900.copy(alpha = 0.88f),
            shadowElevation = 3.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(EmeraldGreen)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == AppLanguage.ODIA) "ଅଫଲାଇନ୍ ଭେକ୍ଟର ମ୍ୟାପ୍ (ବାଲେଶ୍ୱର)" else "Offline Vector Map (Balasore)",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                )
            }
        }

        // Floating Map Controls (Zoom In, Zoom Out, Recenter)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Recenter
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(34.dp)
                    .clickable {
                        zoomScale = 1.0f
                        panOffset = Offset.Zero
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Recenter",
                        tint = OceanBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Zoom In (+)
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(34.dp)
                    .clickable {
                        zoomScale = (zoomScale * 1.35f).coerceAtMost(4.0f)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = BentoSlate800,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Zoom Out (-)
            Surface(
                shape = CircleShape,
                color = Color.White.copy(alpha = 0.95f),
                shadowElevation = 3.dp,
                modifier = Modifier
                    .size(34.dp)
                    .clickable {
                        zoomScale = (zoomScale / 1.35f).coerceAtLeast(0.75f)
                    }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = BentoSlate800,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Calculates perpendicular distance from a point to a 2D line segment.
 */
private fun distanceToSegment(p: Offset, v: Offset, w: Offset): Float {
    val l2 = (v.x - w.x) * (v.x - w.x) + (v.y - w.y) * (v.y - w.y)
    if (l2 == 0f) return hypot(p.x - v.x, p.y - v.y)
    val t = (((p.x - v.x) * (w.x - v.x) + (p.y - v.y) * (w.y - v.y)) / l2).coerceIn(0f, 1f)
    val projection = Offset(v.x + t * (w.x - v.x), v.y + t * (w.y - v.y))
    return hypot(p.x - projection.x, p.y - projection.y)
}
