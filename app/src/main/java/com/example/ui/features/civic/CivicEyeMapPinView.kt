package com.example.ui.features.civic

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.local.CivicReportEntity
import com.example.data.model.AppLanguage
import com.example.data.municipal.BalasoreMunicipalWards
import com.example.util.BalasoreHapticHelper
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Interactive Balasore Map Pin View for the 'Civic Eye' component.
 * Allows citizens to visually locate reported infrastructure issues (potholes, waterlogging,
 * streetlights, waste, damaged roads) across Balasore Municipality.
 */
@Composable
fun CivicEyeMapPinView(
    reports: List<CivicReportEntity>,
    language: AppLanguage = AppLanguage.ENGLISH,
    onUpvoteReport: (String) -> Unit = {},
    onAdvanceStatus: (String) -> Unit = {},
    onSelectReportInFeed: ((CivicReportEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val textMeasurer = rememberTextMeasurer()

    // Bounding Box for Balasore Municipality & Surroundings
    val minLat = 21.4650
    val maxLat = 21.5280
    val minLon = 86.8900
    val maxLon = 86.9600

    // Coordinate Mapping Cache from Municipal Wards
    val wardCoordsMap = remember { BalasoreMunicipalWards.getWardGeoCoordinatesMap() }

    // Seed realistic sample reports if empty so users immediately see pins
    val displayReports = remember(reports) {
        if (reports.isNotEmpty()) {
            reports
        } else {
            listOf(
                CivicReportEntity(
                    id = "BLS-SEED-01",
                    title = "Deep Crater Pothole on OT Road",
                    category = "Road Pothole",
                    wardLocation = "Ward 14 - OT Road / Phandi Chhak",
                    description = "Major crater near Phandi Chhak traffic light causing two-wheeler skidding.",
                    status = "PENDING",
                    urgency = "EMERGENCY",
                    assignedDepartment = "Balasore Roads & Buildings (R&B)",
                    reportedTimestamp = System.currentTimeMillis() - 7200000L,
                    lastUpdatedTimestamp = System.currentTimeMillis(),
                    hasPhotoAttached = true,
                    photoUri = null,
                    latitude = 21.4934,
                    longitude = 86.9135,
                    geoAddress = "Phandi Chhak, OT Road, Balasore",
                    resolutionNotes = "Reported to Ward Supervisor."
                ),
                CivicReportEntity(
                    id = "BLS-SEED-02",
                    title = "Severe Waterlogging near Sahadevkhunta Bus Bay",
                    category = "Drain Clog / Waterlogging",
                    wardLocation = "Ward 4 - Sahadevkhunta",
                    description = "Drain overflow during rainfall submerging bus platform access.",
                    status = "IN_PROGRESS",
                    urgency = "HIGH",
                    assignedDepartment = "Balasore Municipality Drainage Wing",
                    reportedTimestamp = System.currentTimeMillis() - 14400000L,
                    lastUpdatedTimestamp = System.currentTimeMillis(),
                    hasPhotoAttached = true,
                    photoUri = null,
                    latitude = 21.4981,
                    longitude = 86.9240,
                    geoAddress = "Sahadevkhunta Central Bus Stand, Ward 4",
                    resolutionNotes = "Municipality suction pump deployed."
                ),
                CivicReportEntity(
                    id = "BLS-SEED-03",
                    title = "Faulty LED Streetlights across Cinema Chhak",
                    category = "Faulty Streetlight",
                    wardLocation = "Ward 12 - Cinema Chhak",
                    description = "Continuous dark stretch of 6 streetlights from flyover ramp.",
                    status = "PENDING",
                    urgency = "MEDIUM",
                    assignedDepartment = "TPNODL Streetlight Cell",
                    reportedTimestamp = System.currentTimeMillis() - 28800000L,
                    lastUpdatedTimestamp = System.currentTimeMillis(),
                    hasPhotoAttached = false,
                    photoUri = null,
                    latitude = 21.4980,
                    longitude = 86.9280,
                    geoAddress = "Cinema Chhak Flyover Approach, Balasore",
                    resolutionNotes = "Dispatched for contractor repair."
                ),
                CivicReportEntity(
                    id = "BLS-SEED-04",
                    title = "Overflowing Municipal Dumpster at Motiganj",
                    category = "Garbage Heap / Waste",
                    wardLocation = "Ward 7 - Motiganj Market",
                    description = "Commercial waste spilling onto street near fish market entrance.",
                    status = "RESOLVED",
                    urgency = "HIGH",
                    assignedDepartment = "Balasore Sanitation Wing",
                    reportedTimestamp = System.currentTimeMillis() - 86400000L,
                    lastUpdatedTimestamp = System.currentTimeMillis(),
                    hasPhotoAttached = true,
                    photoUri = null,
                    latitude = 21.4910,
                    longitude = 86.9220,
                    geoAddress = "Motiganj Market Bazaar Road, Balasore",
                    resolutionNotes = "Garbage truck cleared waste on 09 Nov."
                ),
                CivicReportEntity(
                    id = "BLS-SEED-05",
                    title = "High-Pressure Drinking Water Pipe Leak",
                    category = "Drinking Water Leak",
                    wardLocation = "Ward 1 - Azimabad",
                    description = "WATCO main line rupture creating water pool near FM University gate.",
                    status = "IN_PROGRESS",
                    urgency = "HIGH",
                    assignedDepartment = "WATCO Balasore Water Supply",
                    reportedTimestamp = System.currentTimeMillis() - 3600000L,
                    lastUpdatedTimestamp = System.currentTimeMillis(),
                    hasPhotoAttached = false,
                    photoUri = null,
                    latitude = 21.5034,
                    longitude = 86.9180,
                    geoAddress = "FM University Old Gate, Azimabad, Balasore",
                    resolutionNotes = "WATCO pipeline valve shut for repair."
                )
            )
        }
    }

    // Interactive Map State: Zoom and Offset
    var zoomLevel by remember { mutableFloatStateOf(1.0f) }
    var panOffsetX by remember { mutableFloatStateOf(0f) }
    var panOffsetY by remember { mutableFloatStateOf(0f) }

    // Filter States
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedUrgencyFilter by remember { mutableStateOf("ALL") }
    var showFilterBottomSheet by remember { mutableStateOf(false) }

    // Selected Pin for Floating Bottom Card Preview
    var selectedReportId by remember { mutableStateOf<String?>(null) }
    val selectedReport = remember(selectedReportId, displayReports) {
        displayReports.find { it.id == selectedReportId }
    }

    // Filtered Reports
    val filteredReports = remember(displayReports, selectedCategoryFilter, selectedStatusFilter, selectedUrgencyFilter) {
        displayReports.filter { report ->
            val matchesCategory = when (selectedCategoryFilter) {
                "POTHOLE" -> report.category.contains("Pothole", ignoreCase = true)
                "WATERLOGGING" -> report.category.contains("Waterlog", ignoreCase = true) || report.category.contains("Drain", ignoreCase = true)
                "ELECTRICITY", "STREETLIGHT" -> report.category.contains("Streetlight", ignoreCase = true) || report.category.contains("Electric", ignoreCase = true)
                "WASTE", "GARBAGE" -> report.category.contains("Garbage", ignoreCase = true) || report.category.contains("Waste", ignoreCase = true)
                "WATER_LEAK" -> report.category.contains("Leak", ignoreCase = true) || report.category.contains("Water", ignoreCase = true)
                "ROAD" -> report.category.contains("Road", ignoreCase = true) || report.category.contains("Culvert", ignoreCase = true)
                else -> true
            }

            val matchesStatus = when (selectedStatusFilter) {
                "PENDING" -> report.status.equals("PENDING", ignoreCase = true) || report.status.equals("SUBMITTED", ignoreCase = true)
                "IN_PROGRESS" -> report.status.equals("IN_PROGRESS", ignoreCase = true)
                "RESOLVED" -> report.status.equals("RESOLVED", ignoreCase = true)
                else -> true
            }

            val matchesUrgency = when (selectedUrgencyFilter) {
                "ALL" -> true
                else -> report.urgency.equals(selectedUrgencyFilter, ignoreCase = true)
            }

            matchesCategory && matchesStatus && matchesUrgency
        }
    }

    // Resolved Coordinates with deterministic jitter if missing
    val mappedPins = remember(filteredReports, wardCoordsMap) {
        filteredReports.map { report ->
            val lat: Double
            val lon: Double

            if (report.latitude != null && report.latitude != 0.0 && report.longitude != null && report.longitude != 0.0) {
                lat = report.latitude
                lon = report.longitude
            } else {
                // Lookup ward
                val coords = wardCoordsMap[report.wardLocation] ?: wardCoordsMap.entries.find {
                    report.wardLocation.contains(it.key, ignoreCase = true) || it.key.contains(report.wardLocation, ignoreCase = true)
                }?.value

                if (coords != null) {
                    val hash = report.id.hashCode()
                    val jitterLat = ((hash % 100) / 10000.0)
                    val jitterLon = (((hash / 100) % 100) / 10000.0)
                    lat = coords.first + jitterLat
                    lon = coords.second + jitterLon
                } else {
                    // Default Balasore Center + jitter
                    val hash = report.id.hashCode()
                    lat = 21.4934 + ((hash % 80) / 10000.0)
                    lon = 86.9135 + (((hash / 80) % 80) / 10000.0)
                }
            }
            PinMapItem(report = report, latitude = lat, longitude = lon)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
            .testTag("civic_map_container")
    ) {
        // 1. INTERACTIVE VECTOR MAP CANVAS
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomLevel = (zoomLevel * zoom).coerceIn(0.75f, 4.0f)
                        panOffsetX = (panOffsetX + pan.x).coerceIn(-1200f * zoomLevel, 1200f * zoomLevel)
                        panOffsetY = (panOffsetY + pan.y).coerceIn(-1200f * zoomLevel, 1200f * zoomLevel)
                    }
                }
        ) {
            val width = constraints.maxWidth.toFloat()
            val height = constraints.maxHeight.toFloat()

            // Handle Tap Detection on Pins
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(mappedPins, zoomLevel, panOffsetX, panOffsetY) {
                        detectTapGestures { tapOffset ->
                            // Calculate touch distance to all pins in screen coordinates
                            var closestPin: PinMapItem? = null
                            var minDistance = Float.MAX_VALUE

                            mappedPins.forEach { pin ->
                                val screenPos = projectGeoToScreen(
                                    lat = pin.latitude,
                                    lon = pin.longitude,
                                    minLat = minLat,
                                    maxLat = maxLat,
                                    minLon = minLon,
                                    maxLon = maxLon,
                                    viewWidth = width,
                                    viewHeight = height,
                                    zoom = zoomLevel,
                                    panX = panOffsetX,
                                    panY = panOffsetY
                                )
                                val dx = tapOffset.x - screenPos.x
                                val dy = tapOffset.y - (screenPos.y - 18f * zoomLevel) // Pin head center
                                val dist = sqrt(dx * dx + dy * dy)
                                if (dist < 44.dp.toPx() && dist < minDistance) {
                                    minDistance = dist
                                    closestPin = pin
                                }
                            }

                            if (closestPin != null) {
                                BalasoreHapticHelper.vibrateMarkerTap(context)
                                selectedReportId = closestPin!!.report.id
                            } else {
                                // Tap on blank area deselects card
                                if (selectedReportId != null) {
                                    selectedReportId = null
                                }
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawBalasoreVectorMap(
                        viewWidth = width,
                        viewHeight = height,
                        minLat = minLat,
                        maxLat = maxLat,
                        minLon = minLon,
                        maxLon = maxLon,
                        zoom = zoomLevel,
                        panX = panOffsetX,
                        panY = panOffsetY,
                        textMeasurer = textMeasurer,
                        isOdia = isOdia
                    )

                    // Draw all reported issue pins
                    mappedPins.forEach { pin ->
                        val isSelected = pin.report.id == selectedReportId
                        val screenPos = projectGeoToScreen(
                            lat = pin.latitude,
                            lon = pin.longitude,
                            minLat = minLat,
                            maxLat = maxLat,
                            minLon = minLon,
                            maxLon = maxLon,
                            viewWidth = width,
                            viewHeight = height,
                            zoom = zoomLevel,
                            panX = panOffsetX,
                            panY = panOffsetY
                        )

                        drawCivicPinMarker(
                            drawScope = this,
                            pos = screenPos,
                            report = pin.report,
                            isSelected = isSelected,
                            zoom = zoomLevel
                        )
                    }
                }
            }
        }

        // 2. TOP FILTER & CONTROLS OVERLAY
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xEE0B1120),
                            Color(0xCC0F172A),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            // Header stats row
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
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0284C7)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isOdia) "ବାଲେଶ୍ୱର ନାଗରିକ ମାନଚିତ୍ର" else "Balasore Civic Map Pins",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                fontSize = 14.5.sp
                            )
                        )
                        Text(
                            text = if (isOdia) "${mappedPins.size} ସମସ୍ୟା ପିନ୍ କରାଯାଇଛି" else "${mappedPins.size} infrastructure issues plotted",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF94A3B8),
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val hasActiveFilters = selectedCategoryFilter != "ALL" || selectedStatusFilter != "ALL" || selectedUrgencyFilter != "ALL"

                    // Quick Filter Bottom Sheet button
                    Surface(
                        color = if (hasActiveFilters) Color(0xFF0284C7) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, if (hasActiveFilters) Color(0xFF38BDF8) else Color(0xFF334155)),
                        modifier = Modifier
                            .clickable {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                showFilterBottomSheet = !showFilterBottomSheet
                            }
                            .testTag("civic_map_filter_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.FilterList, contentDescription = "Filters", tint = Color.White, modifier = Modifier.size(14.dp))
                            Text(
                                text = if (hasActiveFilters) {
                                    if (isOdia) "ଫିଲ୍ଟର୍ (ସକ୍ରିୟ)" else "Filters (Active)"
                                } else {
                                    if (isOdia) "ଫିଲ୍ଟର୍" else "Filters"
                                },
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Quick Recenter button
                    Surface(
                        color = Color(0xFF1E293B),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFF334155)),
                        modifier = Modifier
                            .clickable {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                zoomLevel = 1.0f
                                panOffsetX = 0f
                                panOffsetY = 0f
                            }
                            .testTag("civic_map_recenter")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.MyLocation, contentDescription = "Center", tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                            Text("Center", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // CATEGORY FILTER CHIPS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val categories = listOf(
                    "ALL" to (if (isOdia) "ସବୁ ବର୍ଗ" else "All"),
                    "POTHOLE" to (if (isOdia) "🕳️ ଖାଲ-ଖମା" else "🕳️ Pothole"),
                    "WATERLOGGING" to (if (isOdia) "💧 ଜଳବନ୍ଦୀ" else "💧 Waterlogging"),
                    "WASTE" to (if (isOdia) "🗑️ ଆବର୍ଜନା" else "🗑️ Waste"),
                    "ELECTRICITY" to (if (isOdia) "⚡ ବିଦ୍ୟୁତ୍" else "⚡ Electricity"),
                    "WATER_LEAK" to (if (isOdia) "🚰 ଜଳ ଲିକ୍" else "🚰 Water Leak"),
                    "ROAD" to (if (isOdia) "🚧 ରାସ୍ତା" else "🚧 Damaged Road")
                )

                categories.forEach { (catKey, label) ->
                    val isSelected = selectedCategoryFilter == catKey
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) Color(0xFF0284C7) else Color(0xFF1E293B),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)),
                        modifier = Modifier
                            .clickable {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedCategoryFilter = catKey
                            }
                            .testTag("civic_map_cat_${catKey.lowercase()}")
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            fontSize = 11.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // STATUS FILTER ROW
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val statuses = listOf(
                    "ALL" to (if (isOdia) "ସମସ୍ତ ସ୍ଥିତି" else "All Status"),
                    "PENDING" to (if (isOdia) "⏳ ଦାଖଲ" else "⏳ Pending Review"),
                    "IN_PROGRESS" to (if (isOdia) "🔧 କାର୍ଯ୍ୟ ଚାଲିଛି" else "🔧 In Progress"),
                    "RESOLVED" to (if (isOdia) "✅ ସମାଧାନ" else "✅ Resolved")
                )

                statuses.forEach { (statusKey, label) ->
                    val isSelected = selectedStatusFilter == statusKey
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) Color(0xFF334155) else Color(0xFF0F172A).copy(alpha = 0.8f),
                        border = BorderStroke(1.dp, if (isSelected) Color(0xFF94A3B8) else Color(0xFF1E293B)),
                        modifier = Modifier.clickable {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            selectedStatusFilter = statusKey
                        }
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else Color(0xFF94A3B8),
                            fontSize = 10.5.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // 3. ZOOM CONTROLS (FLOATING ON RIGHT)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FloatingActionButton(
                onClick = {
                    BalasoreHapticHelper.vibrateButtonClick(context)
                    zoomLevel = (zoomLevel * 1.3f).coerceAtMost(4.0f)
                },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("civic_map_zoom_in"),
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", modifier = Modifier.size(18.dp))
            }

            FloatingActionButton(
                onClick = {
                    BalasoreHapticHelper.vibrateButtonClick(context)
                    zoomLevel = (zoomLevel / 1.3f).coerceAtLeast(0.75f)
                },
                modifier = Modifier
                    .size(40.dp)
                    .testTag("civic_map_zoom_out"),
                containerColor = Color(0xFF1E293B),
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", modifier = Modifier.size(18.dp))
            }
        }

        // 4. FLOATING BOTTOM PREVIEW CARD (WHEN PIN IS SELECTED)
        AnimatedVisibility(
            visible = selectedReport != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (selectedReport != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(16.dp, RoundedCornerShape(16.dp))
                        .testTag("civic_map_preview_card"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        // Title row + Close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Category Pill
                                val (badgeColor, textColor) = getCategoryBadgeColors(selectedReport.category)
                                Surface(
                                    color = badgeColor,
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = selectedReport.category.uppercase(),
                                        color = textColor,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }

                                // Urgency Pill
                                val urgencyColor = when (selectedReport.urgency.uppercase()) {
                                    "EMERGENCY", "CRITICAL" -> Color(0xFFEF4444)
                                    "HIGH" -> Color(0xFFF97316)
                                    "MEDIUM" -> Color(0xFFF59E0B)
                                    else -> Color(0xFF10B981)
                                }
                                Surface(
                                    color = urgencyColor.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(1.dp, urgencyColor.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = "${selectedReport.urgency} URGENCY",
                                        color = urgencyColor,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            IconButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    selectedReportId = null
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Dismiss", modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = selectedReport.title,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                            Text(
                                text = selectedReport.wardLocation,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.5.sp
                                )
                            )
                        }

                        if (selectedReport.description.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedReport.description,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 11.5.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                ),
                                maxLines = 2
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Actions Row: Upvote, Directions, Share
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Upvote Button
                            Button(
                                onClick = {
                                    BalasoreHapticHelper.vibrateSuccess(context)
                                    onUpvoteReport(selectedReport.id)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF0284C7)
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("civic_map_upvote_btn"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.ThumbUp, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upvote Issue", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            // Directions via Maps Intent
                            OutlinedButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    val lat = selectedReport.latitude ?: 21.4934
                                    val lon = selectedReport.longitude ?: 86.9135
                                    val uri = Uri.parse("geo:$lat,$lon?q=$lat,$lon(${Uri.encode(selectedReport.title)})")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, uri)
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (_: Exception) {
                                        val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lon")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("civic_map_directions_btn"),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Directions", fontSize = 12.sp)
                            }

                            // Share Button
                            IconButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Civic Eye Alert: ${selectedReport.title}")
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "🚨 Civic Eye Report (Baleshwar 360)\n" +
                                                    "Issue: ${selectedReport.title}\n" +
                                                    "Category: ${selectedReport.category}\n" +
                                                    "Location: ${selectedReport.wardLocation}\n" +
                                                    "Urgency: ${selectedReport.urgency}\n" +
                                                    "Status: ${selectedReport.status}\n\n" +
                                                    "Track and upvote civic infrastructure issues on Baleshwar 360 app."
                                        )
                                    }
                                    val chooser = Intent.createChooser(sendIntent, "Share Civic Eye Issue").apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(chooser)
                                },
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }
        }

        // 5. CATEGORY & CRITERIA FILTER BOTTOM SHEET OVERLAY
        CivicFilterBottomSheet(
            visible = showFilterBottomSheet,
            onDismiss = { showFilterBottomSheet = false },
            selectedCategory = selectedCategoryFilter,
            onSelectCategory = { selectedCategoryFilter = it },
            selectedStatus = selectedStatusFilter,
            onSelectStatus = { selectedStatusFilter = it },
            selectedUrgency = selectedUrgencyFilter,
            onSelectUrgency = { selectedUrgencyFilter = it },
            matchingCount = filteredReports.size,
            onResetFilters = {
                selectedCategoryFilter = "ALL"
                selectedStatusFilter = "ALL"
                selectedUrgencyFilter = "ALL"
            },
            language = language,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

private data class PinMapItem(
    val report: CivicReportEntity,
    val latitude: Double,
    val longitude: Double
)

/**
 * Projects geographical WGS84 coordinates (lat, lon) to Canvas screen pixel space (x, y)
 * with support for pan offsets and zoom magnification.
 */
private fun projectGeoToScreen(
    lat: Double,
    lon: Double,
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    viewWidth: Float,
    viewHeight: Float,
    zoom: Float,
    panX: Float,
    panY: Float
): Offset {
    val normX = ((lon - minLon) / (maxLon - minLon)).toFloat().coerceIn(0f, 1f)
    val normY = ((maxLat - lat) / (maxLat - minLat)).toFloat().coerceIn(0f, 1f)

    val centerX = viewWidth / 2f
    val centerY = viewHeight / 2f

    val rawX = normX * viewWidth
    val rawY = normY * viewHeight

    val zoomedX = centerX + (rawX - centerX) * zoom + panX
    val zoomedY = centerY + (rawY - centerY) * zoom + panY

    return Offset(zoomedX, zoomedY)
}

/**
 * Renders the cartographic base layer of Balasore:
 * - River Budhabalanga curved water path
 * - NH-16 Golden Quadrilateral Highway
 * - OT Road and urban spines
 * - Proof Road / Chandipur corridor
 * - South Eastern Railway track & Balasore Junction
 * - Landmark markers (FM Medical College, Sahadevkhunta, Cinema Chhak)
 */
private fun DrawScope.drawBalasoreVectorMap(
    viewWidth: Float,
    viewHeight: Float,
    minLat: Double,
    maxLat: Double,
    minLon: Double,
    maxLon: Double,
    zoom: Float,
    panX: Float,
    panY: Float,
    textMeasurer: TextMeasurer,
    isOdia: Boolean
) {
    // 1. Waterbody: River Budhabalanga flowing through Balasore
    val riverPoints = listOf(
        Pair(21.5200, 86.8950),
        Pair(21.5080, 86.9100),
        Pair(21.4980, 86.9180),
        Pair(21.4880, 86.9320),
        Pair(21.4780, 86.9480),
        Pair(21.4700, 86.9580)
    )

    val riverPath = Path()
    riverPoints.forEachIndexed { index, (lat, lon) ->
        val screenPos = projectGeoToScreen(lat, lon, minLat, maxLat, minLon, maxLon, viewWidth, viewHeight, zoom, panX, panY)
        if (index == 0) riverPath.moveTo(screenPos.x, screenPos.y) else riverPath.lineTo(screenPos.x, screenPos.y)
    }

    // River background glow and stream
    drawPath(
        path = riverPath,
        color = Color(0x330284C7),
        style = Stroke(width = 18f * zoom, cap = StrokeCap.Round)
    )
    drawPath(
        path = riverPath,
        color = Color(0xFF0369A1),
        style = Stroke(width = 8f * zoom, cap = StrokeCap.Round)
    )

    // 2. NH-16 Highway Corridor (Major Trunk Road)
    val nh16Points = listOf(
        Pair(21.4700, 86.8980),
        Pair(21.4850, 86.9120),
        Pair(21.5000, 86.9260),
        Pair(21.5250, 86.9450)
    )
    val nh16Path = Path()
    nh16Points.forEachIndexed { index, (lat, lon) ->
        val pos = projectGeoToScreen(lat, lon, minLat, maxLat, minLon, maxLon, viewWidth, viewHeight, zoom, panX, panY)
        if (index == 0) nh16Path.moveTo(pos.x, pos.y) else nh16Path.lineTo(pos.x, pos.y)
    }
    drawPath(
        path = nh16Path,
        color = Color(0xFF475569),
        style = Stroke(width = 7f * zoom, cap = StrokeCap.Round)
    )
    drawPath(
        path = nh16Path,
        color = Color(0xFFF59E0B),
        style = Stroke(
            width = 2.5f * zoom,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(15f, 15f), 0f)
        )
    )

    // 3. Old Trunk (OT) Road Corridor (Main Civic Spine)
    val otRoadPoints = listOf(
        Pair(21.5150, 86.9080),
        Pair(21.5030, 86.9140),
        Pair(21.4934, 86.9135), // Phandi Chhak
        Pair(21.4910, 86.9220), // Motiganj
        Pair(21.4980, 86.9280), // Cinema Chhak
        Pair(21.4950, 86.9310)  // Station Bazaar
    )
    val otPath = Path()
    otRoadPoints.forEachIndexed { index, (lat, lon) ->
        val pos = projectGeoToScreen(lat, lon, minLat, maxLat, minLon, maxLon, viewWidth, viewHeight, zoom, panX, panY)
        if (index == 0) otPath.moveTo(pos.x, pos.y) else otPath.lineTo(pos.x, pos.y)
    }
    drawPath(
        path = otPath,
        color = Color(0xFF64748B),
        style = Stroke(width = 5.5f * zoom, cap = StrokeCap.Round)
    )

    // 4. Proof Road to Chandipur
    val proofPoints = listOf(
        Pair(21.4950, 86.9310),
        Pair(21.4850, 86.9420),
        Pair(21.4700, 86.9550)
    )
    val proofPath = Path()
    proofPoints.forEachIndexed { index, (lat, lon) ->
        val pos = projectGeoToScreen(lat, lon, minLat, maxLat, minLon, maxLon, viewWidth, viewHeight, zoom, panX, panY)
        if (index == 0) proofPath.moveTo(pos.x, pos.y) else proofPath.lineTo(pos.x, pos.y)
    }
    drawPath(
        path = proofPath,
        color = Color(0xFF334155),
        style = Stroke(width = 4f * zoom, cap = StrokeCap.Round)
    )

    // 5. South Eastern Railway line
    val railPoints = listOf(
        Pair(21.4680, 86.9250),
        Pair(21.4950, 86.9310), // Balasore Junction
        Pair(21.5280, 86.9380)
    )
    val railPath = Path()
    railPoints.forEachIndexed { index, (lat, lon) ->
        val pos = projectGeoToScreen(lat, lon, minLat, maxLat, minLon, maxLon, viewWidth, viewHeight, zoom, panX, panY)
        if (index == 0) railPath.moveTo(pos.x, pos.y) else railPath.lineTo(pos.x, pos.y)
    }
    drawPath(
        path = railPath,
        color = Color(0xFF94A3B8),
        style = Stroke(
            width = 3.5f * zoom,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    )

    // 6. Draw Balasore Landmarks Labels
    val landmarks = listOf(
        Triple(21.4950, 86.9310, "🚉 Balasore Junction"),
        Triple(21.4981, 86.9240, "🚌 Sahadevkhunta Bus Stand"),
        Triple(21.4980, 86.9280, "🎬 Cinema Chhak"),
        Triple(21.4934, 86.9135, "🏛️ Phandi Chhak (OT Road)"),
        Triple(21.5034, 86.9180, "🎓 FM University / Azimabad"),
        Triple(21.4700, 86.9550, "🌊 Proof Road (Chandipur Link)")
    )

    landmarks.forEach { (lat, lon, name) ->
        val pos = projectGeoToScreen(lat, lon, minLat, maxLat, minLon, maxLon, viewWidth, viewHeight, zoom, panX, panY)
        drawCircle(
            color = Color(0xFF38BDF8),
            radius = 3f * zoom,
            center = pos
        )
        if (zoom >= 1.1f) {
            drawText(
                textMeasurer = textMeasurer,
                text = name,
                topLeft = Offset(pos.x + 6f, pos.y - 12f),
                style = TextStyle(
                    color = Color(0xFF94A3B8),
                    fontSize = (9f * zoom).coerceIn(8f, 13f).sp,
                    fontWeight = FontWeight.Medium
                )
            )
        }
    }
}

/**
 * Draws an interactive civic map pin marker for a report with urgency glow, category icon,
 * and selection indicator.
 */
private fun drawCivicPinMarker(
    drawScope: DrawScope,
    pos: Offset,
    report: CivicReportEntity,
    isSelected: Boolean,
    zoom: Float
) {
    val pinRadius = if (isSelected) 16f * zoom else 12f * zoom
    val pinHeadCenter = Offset(pos.x, pos.y - 18f * zoom)

    val (pinColor, glowColor) = when (report.urgency.uppercase()) {
        "EMERGENCY", "CRITICAL" -> Pair(Color(0xFFEF4444), Color(0x66EF4444))
        "HIGH" -> Pair(Color(0xFFF97316), Color(0x66F97316))
        "MEDIUM" -> Pair(Color(0xFFF59E0B), Color(0x66F59E0B))
        else -> Pair(Color(0xFF10B981), Color(0x6610B981))
    }

    // Outer glow ripple
    drawScope.drawCircle(
        color = glowColor,
        radius = if (isSelected) pinRadius * 2.2f else pinRadius * 1.5f,
        center = pinHeadCenter
    )

    if (isSelected) {
        drawScope.drawCircle(
            color = Color.White,
            radius = pinRadius * 1.35f,
            center = pinHeadCenter,
            style = Stroke(width = 3f * zoom)
        )
    }

    // Pin stem connecting head to location point
    val stemPath = Path().apply {
        moveTo(pinHeadCenter.x - pinRadius * 0.7f, pinHeadCenter.y + pinRadius * 0.4f)
        lineTo(pos.x, pos.y)
        lineTo(pinHeadCenter.x + pinRadius * 0.7f, pinHeadCenter.y + pinRadius * 0.4f)
        close()
    }
    drawScope.drawPath(path = stemPath, color = pinColor)

    // Pin Head Circle
    drawScope.drawCircle(
        color = pinColor,
        radius = pinRadius,
        center = pinHeadCenter
    )

    // Inner White Ring
    drawScope.drawCircle(
        color = Color.White,
        radius = pinRadius * 0.75f,
        center = pinHeadCenter
    )

    // Category Inner Dot
    val categoryDotColor = when {
        report.category.contains("Pothole", ignoreCase = true) -> Color(0xFFC2410C)
        report.category.contains("Waterlog", ignoreCase = true) -> Color(0xFF0284C7)
        report.category.contains("Streetlight", ignoreCase = true) -> Color(0xFFD97706)
        report.category.contains("Garbage", ignoreCase = true) -> Color(0xFF7C3AED)
        report.category.contains("Leak", ignoreCase = true) -> Color(0xFF0D9488)
        else -> Color(0xFF475569)
    }

    drawScope.drawCircle(
        color = categoryDotColor,
        radius = pinRadius * 0.45f,
        center = pinHeadCenter
    )
}

private fun getCategoryBadgeColors(category: String): Pair<Color, Color> {
    return when {
        category.contains("Pothole", ignoreCase = true) -> Pair(Color(0xFFFFEDD5), Color(0xFFC2410C))
        category.contains("Waterlog", ignoreCase = true) -> Pair(Color(0xFFE0F2FE), Color(0xFF0369A1))
        category.contains("Streetlight", ignoreCase = true) -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        category.contains("Garbage", ignoreCase = true) -> Pair(Color(0xFFF3E8FF), Color(0xFF7E22CE))
        category.contains("Leak", ignoreCase = true) -> Pair(Color(0xFFCCFBF1), Color(0xFF0F766E))
        else -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
    }
}
