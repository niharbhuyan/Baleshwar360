package com.example.ui.features.civic

import android.Manifest
import android.annotation.SuppressLint
import android.view.MotionEvent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.R
import com.example.data.local.CivicReportEntity
import com.example.data.maps.BalasoreOfflineTileCacheManager
import com.example.data.maps.BalasoreOsmOfflineTileProvider
import com.example.data.maps.clusterCivicReports
import com.example.data.maps.createClusterMarkerDrawable
import com.example.data.model.AppLanguage
import com.example.data.municipal.BalasoreMunicipalWards
import com.example.util.BalasoreHapticHelper
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay

/**
 * Interactive OSMDroid MapView for the 'Civic Eye' feature.
 * Plots real coordinates of Civic Eye incidents from Firestore and Room DB so citizens
 * can visualize local potholes, drainage clogs, streetlight outages, and waste issues.
 */
@SuppressLint("ClickableViewAccessibility")
@Composable
fun CivicEyeMapPinView(
    reports: List<CivicReportEntity>,
    language: AppLanguage = AppLanguage.ENGLISH,
    focusedReportId: String? = null,
    onFocusedReportHandled: () -> Unit = {},
    onUpvoteReport: (String) -> Unit = {},
    onAdvanceStatus: (String) -> Unit = {},
    onReportNewIssue: (() -> Unit)? = null,
    onRefreshReports: (() -> Unit)? = null,
    onSelectReportInFeed: ((CivicReportEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isOdia = language == AppLanguage.ODIA

    // Configure OSMDroid cache & user-agent once
    remember {
        BalasoreOfflineTileCacheManager.configureOsmDroidForUnstableNetwork(context)
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val tileProvider = remember {
        BalasoreOsmOfflineTileProvider(context.applicationContext ?: context)
    }
    val wardCoordsMap = remember { BalasoreMunicipalWards.getWardGeoCoordinatesMap() }

    // Filter state
    var selectedCategoryFilter by remember { mutableStateOf("ALL") }
    var selectedStatusFilter by remember { mutableStateOf("ALL") }
    var selectedUrgencyFilter by remember { mutableStateOf("ALL") }
    var showFilterBottomSheet by remember { mutableStateOf(false) }

    // Map state
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var currentZoom by remember { mutableDoubleStateOf(13.2) }
    var selectedReportId by remember { mutableStateOf<String?>(focusedReportId) }
    var hasAutoFittedInitialPins by remember { mutableStateOf(false) }

    // Real device GPS location for computing live distance from user's current location
    var userLocation by remember {
        mutableStateOf<Pair<Double, Double>?>(
            com.example.data.notification.CivicProximityAlertManager
                .getBestLastKnownLocation(context)
                ?.let { it.latitude to it.longitude }
        )
    }

    val refreshUserLocation: (Boolean) -> Unit = { centerMap ->
        val loc = com.example.data.notification.CivicProximityAlertManager.getBestLastKnownLocation(context)
        if (loc != null) {
            userLocation = loc.latitude to loc.longitude
            if (centerMap) {
                mapViewRef?.controller?.apply {
                    setZoom(14.5)
                    animateTo(GeoPoint(loc.latitude, loc.longitude))
                }
            }
        } else if (centerMap) {
            Toast.makeText(
                context,
                context.getString(R.string.civic_distance_gps_unavailable),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        val granted = results[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            results[Manifest.permission.ACCESS_COARSE_LOCATION] == true ||
            com.example.data.notification.CivicProximityAlertManager.hasLocationPermission(context)
        if (granted) {
            refreshUserLocation(true)
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.civic_eye_gps_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    val requestOrCenterUserLocation: () -> Unit = {
        if (com.example.data.notification.CivicProximityAlertManager.hasLocationPermission(context)) {
            refreshUserLocation(true)
        } else {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val filteredReports = remember(reports, selectedCategoryFilter, selectedStatusFilter, selectedUrgencyFilter) {
        reports.filter { report ->
            CivicEyeOsmMarkerHelper.matchesMapCategoryFilter(report, selectedCategoryFilter) &&
                CivicEyeOsmMarkerHelper.matchesMapStatusFilter(report, selectedStatusFilter) &&
                com.example.ui.screens.CivicEyeLayersMapHelper.matchesSeverityFilter(
                    report.urgency,
                    selectedUrgencyFilter
                )
        }
    }

    val mappedPins = remember(filteredReports, wardCoordsMap) {
        filteredReports.map { report ->
            CivicEyeOsmMarkerHelper.resolvePinItem(report, wardCoordsMap)
        }
    }

    val exactGpsCount = remember(mappedPins) { mappedPins.count { it.hasExactGps } }
    val selectedPin = remember(selectedReportId, mappedPins) {
        mappedPins.find { it.report.id == selectedReportId }
    }

    // Properly bind OSMDroid MapView to Compose lifecycle and detach on dispose
    DisposableEffect(lifecycleOwner, mapViewRef) {
        val map = mapViewRef
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> map?.onResume()
                Lifecycle.Event.ON_PAUSE -> map?.onPause()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            map?.onPause()
            map?.onDetach()
        }
    }

    // Focus a specific report when navigated from the Submit banner or Live Feed card
    LaunchedEffect(focusedReportId, mappedPins, mapViewRef) {
        val targetId = focusedReportId ?: return@LaunchedEffect
        val targetPin = mappedPins.find { it.report.id == targetId }
        if (targetPin != null && mapViewRef != null) {
            selectedReportId = targetId
            mapViewRef?.controller?.setZoom(16.5)
            mapViewRef?.controller?.animateTo(targetPin.geoPoint)
            hasAutoFittedInitialPins = true
            onFocusedReportHandled()
        }
    }

    // Auto-fit bounds once when incidents arrive and no specific report is pre-focused
    LaunchedEffect(mappedPins, mapViewRef) {
        val map = mapViewRef ?: return@LaunchedEffect
        if (!hasAutoFittedInitialPins && focusedReportId == null && mappedPins.isNotEmpty()) {
            hasAutoFittedInitialPins = true
            if (mappedPins.size == 1) {
                map.controller.setZoom(15.5)
                map.controller.animateTo(mappedPins.first().geoPoint)
            } else {
                CivicEyeOsmMarkerHelper.computeBoundingBox(mappedPins)?.let { box ->
                    map.post {
                        runCatching { map.zoomToBoundingBox(box, true, 96) }
                    }
                }
            }
        }
    }

    val hasActiveFilters = selectedCategoryFilter != "ALL" ||
        selectedStatusFilter != "ALL" ||
        selectedUrgencyFilter != "ALL"

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .testTag("civic_map_container")
    ) {
        // 1. REAL INTERACTIVE OSMDROID MAPVIEW
        AndroidView(
            modifier = Modifier
                .fillMaxSize()
                .testTag("civic_eye_osm_map_view"),
            factory = { ctx ->
                MapView(ctx, tileProvider).apply {
                    mapViewRef = this
                    setTileSource(tileProvider.tileSource)
                    setUseDataConnection(true)
                    setMultiTouchControls(true)
                    isTilesScaledToDpi = true
                    minZoomLevel = 10.0
                    maxZoomLevel = 19.5
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

                    controller.setZoom(13.2)
                    controller.setCenter(
                        GeoPoint(
                            CivicEyeOsmMarkerHelper.BALASORE_CENTER_LAT,
                            CivicEyeOsmMarkerHelper.BALASORE_CENTER_LNG
                        )
                    )

                    // Prevent parent ModalBottomSheet from hijacking pan/pinch gestures on the map
                    setOnTouchListener { view, event ->
                        when (event.actionMasked) {
                            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE ->
                                view.parent?.requestDisallowInterceptTouchEvent(true)
                            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL ->
                                view.parent?.requestDisallowInterceptTouchEvent(false)
                        }
                        false
                    }

                    // Scale bar overlay
                    val scaleBar = ScaleBarOverlay(this).apply {
                        setCentred(true)
                    }
                    overlays.add(scaleBar)

                    // Tap empty map space to dismiss callout
                    val eventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            if (selectedReportId != null) {
                                selectedReportId = null
                                return true
                            }
                            return false
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    })
                    overlays.add(0, eventsOverlay)

                    // Track zoom level so clusters smoothly expand/collapse
                    addMapListener(object : MapListener {
                        override fun onScroll(event: ScrollEvent?): Boolean = false
                        override fun onZoom(event: ZoomEvent?): Boolean {
                            val newZoom = event?.zoomLevel ?: zoomLevelDouble
                            if (kotlin.math.abs(newZoom - currentZoom) >= 0.6) {
                                currentZoom = newZoom
                            }
                            return false
                        }
                    })

                    onResume()
                }
            },
            update = { map ->
                syncCivicOsmMarkers(
                    mapView = map,
                    pins = mappedPins,
                    userLocation = userLocation,
                    selectedReportId = selectedReportId,
                    zoomLevel = currentZoom,
                    onSelectPin = { pin ->
                        BalasoreHapticHelper.vibrateMarkerTap(context)
                        selectedReportId = pin.report.id
                        map.controller.animateTo(pin.geoPoint)
                    },
                    onSelectCluster = { clusterBox ->
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        selectedReportId = null
                        runCatching { map.zoomToBoundingBox(clusterBox, true, 110) }
                    }
                )
            }
        )

        // 2. TOP HEADER & FILTER CONTROLS OVERLAY
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 10.dp, vertical = 10.dp),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
            tonalElevation = 4.dp,
            shadowElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
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
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱର ନାଗରିକ ମାନଚିତ୍ର" else stringResource(R.string.civic_osm_map_title),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            )
                            Text(
                                text = if (isOdia) {
                                    "${mappedPins.size} ସମସ୍ୟା • $exactGpsCount GPS • OSMDroid"
                                } else {
                                    stringResource(
                                        R.string.civic_osm_map_subtitle_template,
                                        mappedPins.size,
                                        exactGpsCount
                                    )
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onRefreshReports != null) {
                            IconButton(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    onRefreshReports()
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("civic_map_refresh_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = stringResource(R.string.civic_osm_map_refresh_cd),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        IconButton(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                showFilterBottomSheet = !showFilterBottomSheet
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("civic_map_filter_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = stringResource(R.string.civic_osm_map_filters_cd),
                                tint = if (hasActiveFilters) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Category filter chips
                val categories = listOf(
                    "ALL" to (if (isOdia) "ସବୁ" else "All"),
                    "POTHOLE" to (if (isOdia) "ଖାଲ-ଖମା" else "Potholes"),
                    "WATERLOGGING" to (if (isOdia) "ଜଳବନ୍ଦୀ" else "Drainage"),
                    "LIGHTING" to (if (isOdia) "ଷ୍ଟ୍ରିଟ୍ ଲାଇଟ୍" else "Lighting"),
                    "WATER_SUPPLY" to (if (isOdia) "ଜଳ ଯୋଗାଣ" else "Water"),
                    "WASTE" to (if (isOdia) "ଆବର୍ଜନା" else "Waste")
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { (catKey, label) ->
                        FilterChip(
                            selected = selectedCategoryFilter == catKey,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedCategoryFilter = catKey
                            },
                            label = { Text(label, fontSize = 11.5.sp) },
                            modifier = Modifier.testTag("civic_map_cat_${catKey.lowercase()}")
                        )
                    }
                }

                // Status filter chips
                val statuses = listOf(
                    "ALL" to (if (isOdia) "ସମସ୍ତ ସ୍ଥିତି" else "All Status"),
                    "PENDING" to (if (isOdia) "ଦାଖଲ" else "Submitted"),
                    "IN_PROGRESS" to (if (isOdia) "କାର୍ଯ୍ୟ ଚାଲିଛି" else "In Progress"),
                    "RESOLVED" to (if (isOdia) "ସମାଧାନ" else "Resolved")
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statuses.forEach { (statusKey, label) ->
                        FilterChip(
                            selected = selectedStatusFilter == statusKey,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedStatusFilter = statusKey
                            },
                            label = { Text(label, fontSize = 11.sp) },
                            modifier = Modifier.testTag("civic_map_status_${statusKey.lowercase()}")
                        )
                    }
                }

                // Severity filter chips (Low, Medium, Critical)
                val severities = listOf(
                    "ALL" to (if (isOdia) "ସବୁ ସ୍ତର" else stringResource(R.string.civic_severity_all)),
                    "LOW" to (if (isOdia) "ସାଧାରଣ" else stringResource(R.string.civic_severity_low)),
                    "MEDIUM" to (if (isOdia) "ମଧ୍ୟମ" else stringResource(R.string.civic_severity_medium)),
                    "CRITICAL" to (if (isOdia) "ଜରୁରୀ" else stringResource(R.string.civic_severity_critical))
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    severities.forEach { (sevKey, label) ->
                        val isSelected = selectedUrgencyFilter.equals(sevKey, ignoreCase = true) ||
                            (sevKey == "CRITICAL" && (selectedUrgencyFilter.equals("EMERGENCY", ignoreCase = true) ||
                                selectedUrgencyFilter.equals("HIGH", ignoreCase = true)))
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedUrgencyFilter = sevKey
                            },
                            label = { Text(label, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                            modifier = Modifier.testTag("civic_map_severity_${sevKey.lowercase()}")
                        )
                    }
                }
            }
        }

        // 3. FLOATING MAP CONTROLS ON RIGHT (48.dp touch targets)
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SmallFloatingActionButton(
                onClick = {
                    BalasoreHapticHelper.vibrateButtonClick(context)
                    mapViewRef?.controller?.zoomIn()
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("civic_map_zoom_in")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.civic_osm_map_zoom_in_cd)
                )
            }

            SmallFloatingActionButton(
                onClick = {
                    BalasoreHapticHelper.vibrateButtonClick(context)
                    mapViewRef?.controller?.zoomOut()
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("civic_map_zoom_out")
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = stringResource(R.string.civic_osm_map_zoom_out_cd)
                )
            }

            SmallFloatingActionButton(
                onClick = {
                    BalasoreHapticHelper.vibrateButtonClick(context)
                    val map = mapViewRef ?: return@SmallFloatingActionButton
                    val box = CivicEyeOsmMarkerHelper.computeBoundingBox(mappedPins)
                    if (box != null && mappedPins.size > 1) {
                        runCatching { map.zoomToBoundingBox(box, true, 96) }
                    } else if (mappedPins.size == 1) {
                        map.controller.setZoom(16.0)
                        map.controller.animateTo(mappedPins.first().geoPoint)
                    } else {
                        map.controller.setZoom(13.2)
                        map.controller.animateTo(
                            GeoPoint(
                                CivicEyeOsmMarkerHelper.BALASORE_CENTER_LAT,
                                CivicEyeOsmMarkerHelper.BALASORE_CENTER_LNG
                            )
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("civic_map_fit_all")
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = stringResource(R.string.civic_osm_map_fit_all_cd)
                )
            }

            SmallFloatingActionButton(
                onClick = {
                    BalasoreHapticHelper.vibrateButtonClick(context)
                    requestOrCenterUserLocation()
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("civic_map_recenter")
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.civic_distance_locate_me_cd)
                )
            }

            if (onReportNewIssue != null) {
                FloatingActionButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        onReportNewIssue()
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("civic_map_report_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddLocationAlt,
                        contentDescription = stringResource(R.string.civic_osm_map_report_new_cd)
                    )
                }
            }
        }

        // 4. EMPTY STATE OVERLAY WHEN NO INCIDENTS ARE PLOTTED
        if (mappedPins.isEmpty()) {
            Card(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .testTag("civic_map_empty_state"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(28.dp)
                    )
                    Text(
                        text = stringResource(
                            if (hasActiveFilters) R.string.civic_osm_map_empty_filtered
                            else R.string.civic_osm_map_empty_title
                        ),
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = stringResource(R.string.civic_osm_map_empty_desc),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        if (hasActiveFilters) {
                            OutlinedButton(
                                onClick = {
                                    selectedCategoryFilter = "ALL"
                                    selectedStatusFilter = "ALL"
                                    selectedUrgencyFilter = "ALL"
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("civic_map_clear_filters_btn")
                            ) {
                                Text(stringResource(R.string.civic_osm_map_clear_filters))
                            }
                        }
                        if (onReportNewIssue != null) {
                            Button(
                                onClick = {
                                    BalasoreHapticHelper.vibrateButtonClick(context)
                                    onReportNewIssue()
                                },
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("civic_map_empty_report_btn")
                            ) {
                                Icon(Icons.Default.AddLocationAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(stringResource(R.string.civic_osm_map_report_cta))
                            }
                        }
                    }
                }
            }
        }

        // 5. COMPACT COLOR LEGEND WHEN NO CALLOUT IS OPEN
        AnimatedVisibility(
            visible = selectedPin == null && mappedPins.isNotEmpty(),
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 14.dp, start = 12.dp, end = 12.dp)
        ) {
            CivicEyeOsmLegendPill(modifier = Modifier.testTag("civic_map_legend_pill"))
        }

        // 6. INTERACTIVE INCIDENT CALLOUT CARD WHEN A MARKER IS SELECTED
        AnimatedVisibility(
            visible = selectedPin != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            if (selectedPin != null) {
                val distMeters = userLocation?.let { (uLat, uLng) ->
                    com.example.ui.screens.CivicEyeLayersMapHelper.distanceMetersBetween(
                        uLat,
                        uLng,
                        selectedPin.latitude,
                        selectedPin.longitude
                    )
                }
                CivicEyeOsmIncidentCalloutCard(
                    pin = selectedPin,
                    distanceMeters = distMeters,
                    onRequestLocation = requestOrCenterUserLocation,
                    onDismiss = { selectedReportId = null },
                    onUpvote = { onUpvoteReport(selectedPin.report.id) },
                    onAdvanceStatus = { onAdvanceStatus(selectedPin.report.id) }
                )
            }
        }

        // 7. FILTER BOTTOM SHEET OVERLAY
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

/**
 * Rebuilds OSMDroid Marker overlays for the current filtered Civic Eye incidents,
 * clustering dense pins at lower zoom levels while keeping selected/zoomed-in pins individual.
 */
private fun syncCivicOsmMarkers(
    mapView: MapView,
    pins: List<CivicOsmPinItem>,
    userLocation: Pair<Double, Double>?,
    selectedReportId: String?,
    zoomLevel: Double,
    onSelectPin: (CivicOsmPinItem) -> Unit,
    onSelectCluster: (org.osmdroid.util.BoundingBox) -> Unit
) {
    val ctx = mapView.context
    mapView.overlays.removeAll { it is Marker }

    userLocation?.let { (uLat, uLng) ->
        val userMarker = Marker(mapView).apply {
            position = GeoPoint(uLat, uLng)
            title = "Your Current Location"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
            icon = com.example.ui.screens.CivicEyeLayersMapHelper.createUserLocationMarkerDrawable(ctx)
            infoWindow = null
        }
        mapView.overlays.add(userMarker)
    }

    if (pins.isEmpty()) {
        mapView.invalidate()
        return
    }

    val pinByReportId = pins.associateBy { it.report.id }
    val normalizedReports = pins.map { pin ->
        pin.report.copy(latitude = pin.latitude, longitude = pin.longitude)
    }

    val useClustering = pins.size > 6 && zoomLevel < 15.2
    val clusters = clusterCivicReports(
        reports = normalizedReports,
        zoomLevel = zoomLevel,
        userLat = CivicEyeOsmMarkerHelper.BALASORE_CENTER_LAT,
        userLng = CivicEyeOsmMarkerHelper.BALASORE_CENTER_LNG,
        clusteringEnabled = useClustering
    )

    clusters.forEach { cluster ->
        val containsSelected = selectedReportId != null &&
            cluster.reports.any { it.id == selectedReportId }

        if (cluster.isSingle || containsSelected) {
            cluster.reports.forEach { report ->
                val pin = pinByReportId[report.id] ?: return@forEach
                val isSelected = pin.report.id == selectedReportId
                val marker = Marker(mapView).apply {
                    position = pin.geoPoint
                    title = pin.report.title
                    snippet = pin.report.geoAddress ?: pin.report.wardLocation
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    icon = CivicEyeOsmMarkerHelper.createIncidentMarkerDrawable(
                        context = ctx,
                        report = pin.report,
                        isSelected = isSelected
                    )
                    infoWindow = null // Handled by our M3 Compose callout card
                    setOnMarkerClickListener { _, _ ->
                        onSelectPin(pin)
                        true
                    }
                }
                mapView.overlays.add(marker)
            }
        } else {
            val clusterMarker = Marker(mapView).apply {
                position = cluster.centerGeoPoint
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                icon = createClusterMarkerDrawable(ctx, cluster)
                infoWindow = null
                setOnMarkerClickListener { _, _ ->
                    onSelectCluster(cluster.getBoundingBox(padding = 0.002))
                    true
                }
            }
            mapView.overlays.add(clusterMarker)
        }
    }

    mapView.invalidate()
}
