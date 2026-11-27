package com.example.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.example.data.model.AppLanguage
import com.example.data.model.FirestoreUserReport
import com.example.data.municipal.BalasoreMunicipalWards
import com.example.util.BalasoreHapticHelper
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import java.util.Locale

/**
 * Dedicated OSMDroid map screen of Balasore showing reported 'Civic Eye' incidents
 * with independent ON/OFF layer toggles (Potholes, Sanitation, Drainage, Water Supply, Street Lights).
 */
@SuppressLint("ClickableViewAccessibility")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreCivicLayersMapScreen(
    localReports: List<CivicReportEntity>,
    firestoreReports: List<FirestoreUserReport>,
    language: AppLanguage = AppLanguage.ENGLISH,
    onRefreshReports: () -> Unit = {},
    onUpvoteReport: (String) -> Unit = {},
    onReportNewIssue: () -> Unit = {},
    onOpenHistory: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isOdia = language == AppLanguage.ODIA

    // Configure OSMDroid tile cache & user-agent once
    remember {
        BalasoreOfflineTileCacheManager.configureOsmDroidForUnstableNetwork(context)
        Configuration.getInstance().userAgentValue = context.packageName
    }

    val tileProvider = remember {
        BalasoreOsmOfflineTileProvider(context.applicationContext ?: context)
    }
    val isOfflineOnly by tileProvider.isOfflineOnlyMode.collectAsState()
    val wardCoordsMap = remember { BalasoreMunicipalWards.getWardGeoCoordinatesMap() }

    // All incident layers enabled by default; each can be toggled ON/OFF independently
    var enabledLayers by remember {
        mutableStateOf(CivicIncidentLayer.entries.toSet())
    }
    // Optional severity filter (null = All, or LOW / MEDIUM / CRITICAL)
    var selectedSeverity by remember { mutableStateOf<CivicSeverityLevel?>(null) }
    var showLayersSheet by remember { mutableStateOf(false) }
    var selectedPinId by remember { mutableStateOf<String?>(null) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var hasAutoFitted by remember { mutableStateOf(false) }

    // Real device GPS location for computing distance from user's current location
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

    val mergedReports = remember(localReports, firestoreReports) {
        CivicEyeLayersMapHelper.mergeCivicEyeReports(localReports, firestoreReports)
    }
    val allPins = remember(mergedReports, wardCoordsMap) {
        CivicEyeLayersMapHelper.resolvePins(mergedReports, wardCoordsMap)
    }
    val countsByLayer = remember(allPins) {
        CivicEyeLayersMapHelper.countPinsByLayer(allPins)
    }
    val layerFilteredPins = remember(allPins, enabledLayers) {
        CivicEyeLayersMapHelper.filterPinsByLayers(allPins, enabledLayers)
    }
    val countsBySeverity = remember(layerFilteredPins) {
        CivicEyeLayersMapHelper.countPinsBySeverity(layerFilteredPins)
    }
    val visiblePins = remember(allPins, enabledLayers, selectedSeverity) {
        CivicEyeLayersMapHelper.filterPinsByLayersAndSeverity(allPins, enabledLayers, selectedSeverity)
    }
    val selectedPin = remember(selectedPinId, visiblePins) {
        visiblePins.find { it.report.id == selectedPinId }
    }
    val nearestIncidentDistanceMeters = remember(visiblePins, userLocation) {
        val loc = userLocation ?: return@remember null
        visiblePins.minOfOrNull { pin ->
            CivicEyeLayersMapHelper.distanceMetersBetween(
                loc.first,
                loc.second,
                pin.latitude,
                pin.longitude
            )
        }
    }

    // Clear selection if its layer was toggled off
    LaunchedEffect(visiblePins, selectedPinId) {
        if (selectedPinId != null && visiblePins.none { it.report.id == selectedPinId }) {
            selectedPinId = null
        }
    }

    // Auto-fit once when initial pins load
    LaunchedEffect(allPins, mapViewRef) {
        val map = mapViewRef ?: return@LaunchedEffect
        if (!hasAutoFitted && allPins.isNotEmpty()) {
            hasAutoFitted = true
            CivicEyeLayersMapHelper.computeBoundingBox(allPins)?.let { box ->
                map.post {
                    runCatching { map.zoomToBoundingBox(box, true, 96) }
                }
            }
        }
    }

    BackHandler {
        when {
            showLayersSheet -> showLayersSheet = false
            selectedPinId != null -> selectedPinId = null
            else -> onClose()
        }
    }

    DisposableEffect(lifecycleOwner, mapViewRef) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewRef?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDetach()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            runCatching {
                mapViewRef?.onPause()
                mapViewRef?.onDetach()
                tileProvider.detach()
            }
        }
    }

    val toggleLayer: (CivicIncidentLayer) -> Unit = { layer ->
        BalasoreHapticHelper.vibrateButtonClick(context)
        enabledLayers = if (layer in enabledLayers) {
            enabledLayers - layer
        } else {
            enabledLayers + layer
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .testTag("civic_layers_map_screen"),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (isOdia) {
                                "ବାଲେଶ୍ୱର ସିଭିକ୍ ଆଇ ଲେୟାର ମ୍ୟାପ୍"
                            } else {
                                stringResource(R.string.civic_layers_map_title)
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = stringResource(
                                R.string.civic_layers_map_subtitle,
                                visiblePins.size,
                                enabledLayers.size,
                                CivicIncidentLayer.entries.size
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
                            .testTag("civic_layers_map_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.civic_layers_map_back_cd)
                        )
                    }
                },
                actions = {
                    if (onOpenHistory != null) {
                        IconButton(
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                onOpenHistory()
                            },
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("civic_layers_map_history_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = stringResource(R.string.civic_layers_map_history_cd)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            showLayersSheet = true
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("civic_layers_map_layers_btn")
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                ) {
                                    Text("${enabledLayers.size}")
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Layers,
                                contentDescription = stringResource(R.string.civic_layers_map_toggle_layers_cd)
                            )
                        }
                    }
                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            onRefreshReports()
                            Toast.makeText(
                                context,
                                context.getString(R.string.civic_layers_map_refreshing),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("civic_layers_map_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.civic_layers_map_refresh_cd)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive OSMDroid MapView of Balasore
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("civic_layers_osm_map"),
                factory = { ctx ->
                    MapView(ctx, tileProvider).apply {
                        mapViewRef = this
                        setTileSource(tileProvider.tileSource)
                        setUseDataConnection(!isOfflineOnly)
                        setMultiTouchControls(true)
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                        minZoomLevel = 10.0
                        maxZoomLevel = 19.0
                        controller.setZoom(13.2)
                        controller.setCenter(
                            GeoPoint(
                                CivicEyeLayersMapHelper.BALASORE_CENTER_LAT,
                                CivicEyeLayersMapHelper.BALASORE_CENTER_LNG
                            )
                        )

                        val compass = CompassOverlay(ctx, InternalCompassOrientationProvider(ctx), this)
                        compass.enableCompass()
                        overlays.add(compass)

                        val scaleBar = ScaleBarOverlay(this)
                        scaleBar.setCentred(true)
                        overlays.add(scaleBar)

                        overlays.add(
                            MapEventsOverlay(object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                    selectedPinId = null
                                    return false
                                }

                                override fun longPressHelper(p: GeoPoint?): Boolean = false
                            })
                        )
                    }
                },
                update = { map ->
                    map.setUseDataConnection(!isOfflineOnly)
                    // Replace only Marker overlays while preserving Compass, ScaleBar & MapEventsOverlay
                    map.overlays.removeAll { it is Marker }

                    userLocation?.let { (uLat, uLng) ->
                        val userMarker = Marker(map).apply {
                            position = GeoPoint(uLat, uLng)
                            title = "Your Current Location"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                            icon = CivicEyeLayersMapHelper.createUserLocationMarkerDrawable(context)
                            infoWindow = null
                        }
                        map.overlays.add(userMarker)
                    }

                    visiblePins.forEach { pin ->
                        val isSelected = pin.report.id == selectedPinId
                        val severity = CivicEyeLayersMapHelper.classifySeverity(pin.report.urgency)
                        val distSuffix = userLocation?.let { (uLat, uLng) ->
                            val d = CivicEyeLayersMapHelper.distanceMetersBetween(
                                uLat, uLng, pin.latitude, pin.longitude
                            )
                            " • ${CivicEyeLayersMapHelper.formatDistanceFromUser(d)}"
                        }.orEmpty()
                        val marker = Marker(map).apply {
                            position = pin.geoPoint
                            title = pin.report.title
                            snippet = "${severity.labelEn} • ${pin.layer.labelEn}$distSuffix"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            icon = CivicEyeLayersMapHelper.createLayerMarkerDrawable(
                                context = context,
                                pin = pin,
                                isSelected = isSelected
                            )
                            setOnMarkerClickListener { _, _ ->
                                BalasoreHapticHelper.vibrateMarkerTap(context)
                                selectedPinId = pin.report.id
                                map.controller.animateTo(pin.geoPoint)
                                true
                            }
                        }
                        map.overlays.add(marker)
                    }
                    map.invalidate()
                }
            )

            // Floating Quick Layer Toggle Chips + Severity Filter Bar (Low, Medium, Critical)
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("civic_layers_quick_bar")
                ) {
                    items(CivicIncidentLayer.entries, key = { it.name }) { layer ->
                        val isOn = layer in enabledLayers
                        val count = countsByLayer[layer] ?: 0
                        FilterChip(
                            selected = isOn,
                            onClick = { toggleLayer(layer) },
                            label = {
                                Text(
                                    text = "${if (isOdia) layer.labelOd else layer.labelEn} ($count)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (isOn) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isOn) MaterialTheme.colorScheme.onPrimary
                                            else layer.accentColor
                                        )
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = layer.accentColor,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isOn,
                                borderColor = layer.accentColor.copy(alpha = 0.55f),
                                selectedBorderColor = layer.accentColor
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("civic_layer_chip_${layer.name.lowercase(Locale.US)}")
                        )
                    }
                }

                // Severity Filter Strip (All, Low, Medium, Critical)
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.testTag("civic_layers_severity_bar")
                ) {
                    item(key = "ALL_SEVERITY") {
                        val isSelected = selectedSeverity == null
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedSeverity = null
                            },
                            label = {
                                Text(
                                    text = if (isOdia) "ସବୁ ସ୍ତର (${layerFilteredPins.size})"
                                    else "${stringResource(R.string.civic_severity_all)} (${layerFilteredPins.size})",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("civic_layers_severity_all")
                        )
                    }
                    items(CivicSeverityLevel.entries, key = { it.name }) { sev ->
                        val isSelected = selectedSeverity == sev
                        val count = countsBySeverity[sev] ?: 0
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedSeverity = if (isSelected) null else sev
                            },
                            label = {
                                Text(
                                    text = "${if (isOdia) sev.labelOd else sev.labelEn} ($count)",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White else sev.accentColor)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                selectedContainerColor = sev.accentColor,
                                selectedLabelColor = Color.White
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = sev.accentColor.copy(alpha = 0.5f),
                                selectedBorderColor = sev.accentColor
                            ),
                            modifier = Modifier
                                .heightIn(min = 48.dp)
                                .testTag("civic_layers_severity_${sev.name.lowercase(Locale.US)}")
                        )
                    }
                }

                // Live Nearest-Incident Distance Pill when user GPS is available
                if (nearestIncidentDistanceMeters != null) {
                    val nearestLabel = CivicEyeLayersMapHelper.formatDistanceFromUser(nearestIncidentDistanceMeters)
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.95f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .padding(horizontal = 12.dp)
                            .testTag("civic_layers_nearest_distance_pill")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = nearestLabel,
                                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = stringResource(R.string.civic_distance_nearest_template, nearestLabel),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                }
            }

            // Right-edge floating map controls (Zoom In / Zoom Out / Fit All / Recenter Balasore)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = { mapViewRef?.controller?.zoomIn() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("civic_layers_zoom_in_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.civic_osm_map_zoom_in_cd)
                    )
                }
                SmallFloatingActionButton(
                    onClick = { mapViewRef?.controller?.zoomOut() },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("civic_layers_zoom_out_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Remove,
                        contentDescription = stringResource(R.string.civic_osm_map_zoom_out_cd)
                    )
                }
                SmallFloatingActionButton(
                    onClick = {
                        val map = mapViewRef ?: return@SmallFloatingActionButton
                        val box = CivicEyeLayersMapHelper.computeBoundingBox(visiblePins)
                        if (box != null) {
                            runCatching { map.zoomToBoundingBox(box, true, 96) }
                        } else {
                            map.controller.setZoom(13.2)
                            map.controller.animateTo(
                                GeoPoint(
                                    CivicEyeLayersMapHelper.BALASORE_CENTER_LAT,
                                    CivicEyeLayersMapHelper.BALASORE_CENTER_LNG
                                )
                            )
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("civic_layers_fit_all_btn")
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
                        .minimumInteractiveComponentSize()
                        .testTag("civic_layers_recenter_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = stringResource(R.string.civic_distance_locate_me_cd)
                    )
                }
            }

            // Friendly Empty-State Overlay when all layers are turned off or zero incidents exist
            if (visiblePins.isEmpty() && selectedPin == null) {
                Card(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 84.dp)
                        .navigationBarsPadding()
                        .testTag("civic_layers_empty_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (enabledLayers.isEmpty()) {
                                stringResource(R.string.civic_layers_map_all_off_title)
                            } else if (allPins.isEmpty()) {
                                stringResource(R.string.civic_osm_map_empty_title)
                            } else {
                                stringResource(R.string.civic_layers_map_no_layer_match)
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (enabledLayers.isEmpty()) {
                                stringResource(R.string.civic_layers_map_all_off_desc)
                            } else {
                                stringResource(R.string.civic_osm_map_empty_desc)
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (enabledLayers.size < CivicIncidentLayer.entries.size || selectedSeverity != null) {
                                Button(
                                    onClick = {
                                        enabledLayers = CivicIncidentLayer.entries.toSet()
                                        selectedSeverity = null
                                    },
                                    modifier = Modifier
                                        .heightIn(min = 48.dp)
                                        .testTag("civic_layers_restore_all_btn")
                                ) {
                                    Text(stringResource(R.string.civic_layers_map_show_all))
                                }
                            }
                            OutlinedButton(
                                onClick = onReportNewIssue,
                                modifier = Modifier
                                    .heightIn(min = 48.dp)
                                    .testTag("civic_layers_empty_report_btn")
                            ) {
                                Text(stringResource(R.string.civic_osm_map_report_cta))
                            }
                        }
                    }
                }
            }

            // Floating Selected Incident Callout Card
            AnimatedVisibility(
                visible = selectedPin != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(12.dp)
            ) {
                selectedPin?.let { pin ->
                    val distanceFromUser = userLocation?.let { (uLat, uLng) ->
                        CivicEyeLayersMapHelper.distanceMetersBetween(
                            uLat,
                            uLng,
                            pin.latitude,
                            pin.longitude
                        )
                    }
                    CivicLayerPinCalloutCard(
                        pin = pin,
                        distanceMeters = distanceFromUser,
                        isOdia = isOdia,
                        onRequestLocation = requestOrCenterUserLocation,
                        onDismiss = { selectedPinId = null },
                        onUpvote = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            onUpvoteReport(pin.report.id)
                            Toast.makeText(
                                context,
                                context.getString(R.string.civic_layers_map_upvoted_toast),
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        onDirections = {
                            val uri = Uri.parse(
                                "geo:${pin.latitude},${pin.longitude}?q=${pin.latitude},${pin.longitude}(${Uri.encode(pin.report.title)})"
                            )
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            runCatching { context.startActivity(intent) }.onFailure {
                                Toast.makeText(
                                    context,
                                    "No maps application available",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    )
                }
            }

            // Report New Issue FAB (shown when no pin callout is open)
            if (selectedPin == null) {
                ExtendedFloatingActionButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        onReportNewIssue()
                    },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.AddLocationAlt,
                            contentDescription = stringResource(R.string.civic_osm_map_report_new_cd)
                        )
                    },
                    text = {
                        Text(
                            text = if (isOdia) "ସମସ୍ୟା ଜଣାନ୍ତୁ" else stringResource(R.string.civic_osm_map_report_cta),
                            fontWeight = FontWeight.Bold
                        )
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .navigationBarsPadding()
                        .padding(16.dp)
                        .testTag("civic_layers_map_report_fab")
                )
            }
        }
    }

    // Dedicated Incident Layers Toggle ModalBottomSheet (with Material 3 Switches)
    if (showLayersSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLayersSheet = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .testTag("civic_layers_control_sheet"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOdia) "ସିଭିକ୍ ଆଇ ଲେୟାର ନିୟନ୍ତ୍ରଣ" else stringResource(R.string.civic_layers_sheet_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.civic_layers_sheet_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        TextButton(
                            onClick = { enabledLayers = CivicIncidentLayer.entries.toSet() },
                            modifier = Modifier.testTag("civic_layers_enable_all_btn")
                        ) {
                            Text(stringResource(R.string.civic_layers_map_show_all))
                        }
                        TextButton(
                            onClick = { enabledLayers = emptySet() },
                            modifier = Modifier.testTag("civic_layers_hide_all_btn")
                        ) {
                            Text(stringResource(R.string.civic_layers_map_hide_all))
                        }
                    }
                }

                HorizontalDivider()

                CivicIncidentLayer.entries.forEach { layer ->
                    val isChecked = layer in enabledLayers
                    val count = countsByLayer[layer] ?: 0
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isChecked) {
                            layer.accentColor.copy(alpha = 0.10f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        border = BorderStroke(
                            1.dp,
                            if (isChecked) layer.accentColor.copy(alpha = 0.55f)
                            else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(layer.accentColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = layer.icon,
                                    contentDescription = layer.labelEn,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
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
                                        fontWeight = FontWeight.Bold
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
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = layer.subtitleEn,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Switch(
                                checked = isChecked,
                                onCheckedChange = { toggleLayer(layer) },
                                modifier = Modifier.testTag("civic_layer_switch_${layer.name.lowercase(Locale.US)}")
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CivicLayerPinCalloutCard(
    pin: CivicLayerMapPin,
    distanceMeters: Double?,
    isOdia: Boolean,
    onRequestLocation: () -> Unit,
    onDismiss: () -> Unit,
    onUpvote: () -> Unit,
    onDirections: () -> Unit
) {
    val report = pin.report
    val severity = CivicEyeLayersMapHelper.classifySeverity(report.urgency)
    val statusNormalized = report.status.trim().uppercase(Locale.US)
    val statusLabel = when (statusNormalized) {
        "RESOLVED" -> "Resolved"
        "IN_PROGRESS" -> "In Progress"
        else -> "Pending"
    }
    val statusBg = when (statusNormalized) {
        "RESOLVED" -> Color(0xFFDCFCE7)
        "IN_PROGRESS" -> Color(0xFFE0F2FE)
        else -> Color(0xFFFEF3C7)
    }
    val statusFg = when (statusNormalized) {
        "RESOLVED" -> Color(0xFF15803D)
        "IN_PROGRESS" -> Color(0xFF0369A1)
        else -> Color(0xFFB45309)
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, pin.layer.accentColor.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("civic_layers_pin_callout")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = pin.layer.accentColor.copy(alpha = 0.14f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = pin.layer.icon,
                                contentDescription = pin.layer.labelEn,
                                tint = pin.layer.accentColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isOdia) pin.layer.labelOd else pin.layer.labelEn,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = pin.layer.accentColor
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = severity.containerColor
                    ) {
                        Text(
                            text = if (isOdia) severity.labelOd else severity.labelEn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = severity.accentColor,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusLabel,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusFg,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .testTag("civic_layers_callout_close_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.civic_osm_map_dismiss_callout_cd)
                    )
                }
            }

            Text(
                text = report.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = if (pin.hasExactGps) Icons.Default.GpsFixed else Icons.Default.LocationOn,
                    contentDescription = report.wardLocation,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "${report.wardLocation} • ${
                        if (pin.hasExactGps) stringResource(R.string.civic_osm_map_exact_gps)
                        else stringResource(R.string.civic_osm_map_ward_approx)
                    }",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Distance from user's current location badge (or 1-tap GPS prompt)
            if (distanceMeters != null) {
                val distLabel = CivicEyeLayersMapHelper.formatDistanceFromUser(distanceMeters)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.testTag("civic_layers_callout_distance_badge")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = distLabel,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.civic_distance_from_you, distLabel),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            } else {
                TextButton(
                    onClick = onRequestLocation,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp),
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .testTag("civic_layers_callout_enable_gps_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = stringResource(R.string.civic_distance_enable_gps),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.civic_distance_enable_gps),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (report.description.isNotBlank()) {
                Text(
                    text = report.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onDirections,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("civic_layers_callout_directions_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = stringResource(R.string.civic_osm_map_directions),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.civic_osm_map_directions))
                }
                OutlinedButton(
                    onClick = onUpvote,
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 48.dp)
                        .testTag("civic_layers_callout_upvote_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = stringResource(R.string.civic_osm_map_upvote),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.civic_osm_map_upvote))
                }
            }
        }
    }
}
