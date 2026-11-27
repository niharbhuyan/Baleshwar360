package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import com.example.ui.features.map.TourismMapDirectionsHelper
import com.example.ui.features.map.TourismSpotReviewsAndDirectionsSheet
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.maps.BalasoreOsmDarkStyleHelper
import com.example.data.maps.BalasoreOsmOfflineTileProvider
import com.example.data.model.AppLanguage
import com.example.ui.components.BALASORE_TOURIST_LOCATIONS
import com.example.ui.components.TouristLocationItem
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import kotlinx.coroutines.launch
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import com.example.data.local.CivicReportEntity
import com.example.data.maps.BalasoreOfflineRoutingEngine
import com.example.data.maps.BalasoreVectorMapLayerOverlay
import com.example.data.maps.OfflineHotspotRoute
import com.example.ui.components.OfflineHotspotNavigationHud
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import com.example.data.remote.LiveCityBusDto
import com.example.data.repository.LocalTransportRepository
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Interactive OSMDroid MapView for Balasore Tourism Hotspots and Citizen Civic Eye Infrastructure.
 *
 * Utilizes [BalasoreOsmOfflineTileProvider] to deliver complete offline map rendering
 * without needing an active internet connection or external Google Maps API key.
 * Supports interactive layer overlay for citizen civic reports (potholes, drainage issues, etc.).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreOsmTourismMapView(
    language: AppLanguage = AppLanguage.ENGLISH,
    initialSpotId: String? = null,
    civicReports: List<CivicReportEntity> = emptyList(),
    onReportStatusChange: ((String) -> Unit)? = null,
    onAddReportClick: (() -> Unit)? = null,
    initialShowCivicEye: Boolean = false,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()

    // Initialize OSMDroid Configuration & Unstable Network Cache Optimization
    remember {
        com.example.data.maps.BalasoreOfflineTileCacheManager.configureOsmDroidForUnstableNetwork(context)
    }

    // Custom Offline Tile Provider with two-tier LRU Memory + Disk Pre-Cache (uses applicationContext to avoid Activity receiver leaks)
    val tileProvider = remember { BalasoreOsmOfflineTileProvider(context.applicationContext ?: context) }
    val isOfflineOnly by tileProvider.isOfflineOnlyMode.collectAsState()
    val preCacheStats by tileProvider.preCacheStats.collectAsState()

    var selectedCategory by remember { mutableStateOf("ALL") } // "ALL", "BEACH", "TEMPLE", "NATURE"
    var selectedSpot by remember {
        mutableStateOf<TouristLocationItem?>(
            if (initialSpotId != null) {
                BALASORE_TOURIST_LOCATIONS.find { it.id == initialSpotId } ?: BALASORE_TOURIST_LOCATIONS.first()
            } else {
                BALASORE_TOURIST_LOCATIONS.first()
            }
        )
    }

    var isCachingAll by remember { mutableStateOf(false) }
    var cacheProgress by remember { mutableFloatStateOf(0f) }
    var showDetailSheet by remember { mutableStateOf(true) }
    var reviewModalSpot by remember { mutableStateOf<TouristLocationItem?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    // Automatically pre-cache all Balasore tourism hotspots on launch for smooth unstable-network panning
    LaunchedEffect(Unit) {
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            tileProvider.prefetchAllTourismSpots { prog ->
                cacheProgress = prog
            }
        }
        mapViewInstance?.invalidate()
    }

    // Pre-warm high-zoom (13..16) neighborhood tiles whenever user selects a tourism hotspot
    LaunchedEffect(selectedSpot?.id) {
        selectedSpot?.let { spot ->
            tileProvider.preCacheHotspotNeighborhoodAsync(
                spotId = spot.id,
                lat = spot.latitude,
                lon = spot.longitude,
                zoomLevels = listOf(13, 14, 15, 16),
                radiusTiles = 1
            ) {
                mapViewInstance?.invalidate()
            }
        }
    }

    val filteredSpots = remember(selectedCategory) {
        when (selectedCategory) {
            "ALL" -> BALASORE_TOURIST_LOCATIONS
            "HERITAGE" -> BALASORE_TOURIST_LOCATIONS.filter {
                it.categoryGroup == "HERITAGE" ||
                    it.categoryEn.contains("Heritage", ignoreCase = true) ||
                    it.categoryEn.contains("Fort", ignoreCase = true) ||
                    it.historicalSignificance.contains("century", ignoreCase = true)
            }
            else -> BALASORE_TOURIST_LOCATIONS.filter { it.categoryGroup == selectedCategory }
        }
    }

    var showTourismLayer by remember { mutableStateOf(!initialShowCivicEye) }
    var showCivicEyeLayer by remember { mutableStateOf(initialShowCivicEye || civicReports.isNotEmpty()) }
    var showLiveBusLayer by remember { mutableStateOf(true) }
    var isDarkMapStyle by remember { mutableStateOf(true) }
    var selectedCivicReport by remember { mutableStateOf<CivicReportEntity?>(null) }
    var selectedLiveBus by remember { mutableStateOf<LiveCityBusDto?>(null) }

    val transportRepository = remember { LocalTransportRepository() }
    var liveBuses by remember {
        mutableStateOf(transportRepository.createBaselineBalasoreBuses(0))
    }
    var busGpsCycle by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        val initial = transportRepository.fetchLiveCityBuses(0).getOrNull()
        if (!initial.isNullOrEmpty()) {
            liveBuses = initial
        }
        while (true) {
            delay(10000L)
            busGpsCycle += 1
            val updated = transportRepository.fetchLiveCityBuses(busGpsCycle).getOrDefault(
                transportRepository.createBaselineBalasoreBuses(busGpsCycle)
            )
            liveBuses = updated
            selectedLiveBus?.let { cur ->
                selectedLiveBus = updated.find { it.busId == cur.busId } ?: cur
            }
        }
    }

    var civicCategoryFilter by remember { mutableStateOf("ALL") } // "ALL", "POTHOLE", "DRAINAGE", "SANITATION", "STREETLIGHT"
    var civicStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "ACTIVE", "RESOLVED"

    val filteredCivicReports = remember(civicReports, civicCategoryFilter, civicStatusFilter) {
        civicReports.filter { report ->
            val matchesCategory = when (civicCategoryFilter) {
                "POTHOLE" -> report.category.contains("Pothole", ignoreCase = true) || report.category.contains("Road", ignoreCase = true)
                "DRAINAGE" -> report.category.contains("Drain", ignoreCase = true) || report.category.contains("Water", ignoreCase = true)
                "SANITATION" -> report.category.contains("Garbage", ignoreCase = true) || report.category.contains("Waste", ignoreCase = true)
                "STREETLIGHT" -> report.category.contains("Light", ignoreCase = true) || report.category.contains("Lamp", ignoreCase = true)
                else -> true
            }
            val matchesStatus = when (civicStatusFilter) {
                "ACTIVE" -> report.status != "RESOLVED"
                "RESOLVED" -> report.status == "RESOLVED"
                else -> true
            }
            matchesCategory && matchesStatus
        }
    }

    var activeOfflineRoute by remember { mutableStateOf<OfflineHotspotRoute?>(null) }
    var activeStepIndex by remember { mutableIntStateOf(0) }
    var showVectorCorridorsLayer by remember { mutableStateOf(true) }
    val vectorLayerOverlay = remember {
        BalasoreVectorMapLayerOverlay(context) { tappedSpotId ->
            val spot = BALASORE_TOURIST_LOCATIONS.find { it.id == tappedSpotId }
            if (spot != null) {
                selectedSpot = spot
                showDetailSheet = true
            }
        }
    }

    val startOfflineNavigation: (TouristLocationItem) -> Unit = { spot ->
        val route = BalasoreOfflineRoutingEngine.calculateOfflineHotspotRoute(
            destinationId = spot.id,
            destinationNameEn = spot.nameEn,
            destinationNameOr = spot.nameOr,
            destLat = spot.latitude,
            destLng = spot.longitude
        )
        activeOfflineRoute = route
        activeStepIndex = 0
        vectorLayerOverlay.activeRoute = route
        vectorLayerOverlay.activeStepIndex = 0
        showDetailSheet = false

        mapViewInstance?.apply {
            controller.setZoom(14.0)
            controller.animateTo(route.corridorPoints.first())
            invalidate()
        }
        Toast.makeText(
            context,
            if (language == AppLanguage.ODIA) "ଅଫଲାଇନ୍ ନେଭିଗେସନ୍ ସକ୍ରିୟ: ${spot.nameOr}" else "Offline Vector Route: ${spot.nameEn}",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Lifecycle Observer for MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewInstance?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewInstance?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapViewInstance?.onDetach()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                mapViewInstance?.onPause()
                mapViewInstance?.onDetach()
                tileProvider.detach()
            } catch (_: Exception) {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ମ୍ୟାପ୍ (OSMDroid)" else "Balasore Map (OSMDroid)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(if (isOfflineOnly) EmeraldGreen else OceanBlue)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (showCivicEyeLayer && civicReports.isNotEmpty()) {
                                    "${filteredCivicReports.size} Civic Reports • Real-Time Ward Telemetry"
                                } else if (isOfflineOnly) {
                                    "Cached Offline Map • Zero Data Needed"
                                } else {
                                    "Live Online Map • OSM Tile Streaming"
                                },
                                fontSize = 10.5.sp,
                                color = BentoSlate700
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("osm_map_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = BentoSlate900
                        )
                    }
                },
                actions = {
                    // Quick Report Civic Issue Button
                    if (onAddReportClick != null) {
                        IconButton(
                            onClick = onAddReportClick,
                            modifier = Modifier.testTag("osm_add_civic_report_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Report Civic Issue",
                                tint = Color(0xFFDC2626)
                            )
                        }
                    }

                    // Recenter to Balasore City
                    IconButton(
                        onClick = {
                            mapViewInstance?.controller?.apply {
                                setZoom(13.0)
                                animateTo(GeoPoint(21.4934, 86.9135))
                            }
                        },
                        modifier = Modifier.testTag("osm_map_recenter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = "Recenter",
                            tint = OceanBlue
                        )
                    }

                    // Pre-cache All Tourism Spots Button
                    IconButton(
                        onClick = {
                            if (!isCachingAll) {
                                isCachingAll = true
                                Toast.makeText(context, "Caching offline tiles for all Balasore spots...", Toast.LENGTH_SHORT).show()
                                tileProvider.prefetchAllTourismSpots { progress ->
                                    cacheProgress = progress
                                    if (progress >= 1f) {
                                        isCachingAll = false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.testTag("osm_map_cache_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = "Pre-cache Tiles",
                            tint = if (isCachingAll) EmeraldGreen else BentoSlate800
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF8FAFC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // OSMDroid MapView Host
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("osm_map_view"),
                factory = { ctx ->
                    MapView(ctx, tileProvider).apply {
                        mapViewInstance = this
                        setTileSource(tileProvider.tileSource)
                        setUseDataConnection(!isOfflineOnly)
                        setMultiTouchControls(true)
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                        BalasoreOsmDarkStyleHelper.applyMapColorStyle(this, isDarkMapStyle)

                        // Center on Balasore District (21.4934° N, 86.9135° E)
                        controller.setZoom(12.5)
                        controller.setCenter(GeoPoint(21.4934, 86.9135))

                        // Overlays: Vector Map Layer (Roads, Highways & Glowing Navigation Routes)
                        vectorLayerOverlay.activeRoute = activeOfflineRoute
                        vectorLayerOverlay.activeStepIndex = activeStepIndex
                        vectorLayerOverlay.isVectorLayerEnabled = showVectorCorridorsLayer
                        if (!overlays.contains(vectorLayerOverlay)) {
                            overlays.add(0, vectorLayerOverlay)
                        }

                        // Overlays: Compass & ScaleBar
                        val compassOverlay = CompassOverlay(ctx, InternalCompassOrientationProvider(ctx), this)
                        compassOverlay.enableCompass()
                        overlays.add(compassOverlay)

                        val scaleBarOverlay = ScaleBarOverlay(this)
                        scaleBarOverlay.setCentred(true)
                        overlays.add(scaleBarOverlay)

                        updateOsmMarkers(
                            mapView = this,
                            spots = filteredSpots,
                            civicReports = filteredCivicReports,
                            liveBuses = liveBuses,
                            showTourism = showTourismLayer,
                            showCivicEye = showCivicEyeLayer,
                            showLiveBuses = showLiveBusLayer,
                            context = ctx,
                            language = language,
                            onSpotClick = { spot ->
                                selectedSpot = spot
                                selectedCivicReport = null
                                selectedLiveBus = null
                                showDetailSheet = true
                                controller.animateTo(GeoPoint(spot.latitude, spot.longitude))
                            },
                            onCivicReportClick = { report ->
                                selectedCivicReport = report
                                selectedSpot = null
                                selectedLiveBus = null
                                showDetailSheet = true
                                val lat = report.latitude ?: 21.4934
                                val lng = report.longitude ?: 86.9135
                                controller.animateTo(GeoPoint(lat, lng))
                            },
                            onLiveBusClick = { bus ->
                                selectedLiveBus = bus
                                selectedSpot = null
                                selectedCivicReport = null
                                showDetailSheet = true
                                controller.animateTo(GeoPoint(bus.latitude, bus.longitude))
                            }
                        )
                    }
                },
                update = { map ->
                    map.setUseDataConnection(!isOfflineOnly)
                    BalasoreOsmDarkStyleHelper.applyMapColorStyle(map, isDarkMapStyle)
                    vectorLayerOverlay.activeRoute = activeOfflineRoute
                    vectorLayerOverlay.activeStepIndex = activeStepIndex
                    vectorLayerOverlay.isVectorLayerEnabled = showVectorCorridorsLayer
                    if (!map.overlays.contains(vectorLayerOverlay)) {
                        map.overlays.add(0, vectorLayerOverlay)
                    }

                    updateOsmMarkers(
                        mapView = map,
                        spots = filteredSpots,
                        civicReports = filteredCivicReports,
                        liveBuses = liveBuses,
                        showTourism = showTourismLayer,
                        showCivicEye = showCivicEyeLayer,
                        showLiveBuses = showLiveBusLayer,
                        context = context,
                        language = language,
                        onSpotClick = { spot ->
                            selectedSpot = spot
                            selectedCivicReport = null
                            selectedLiveBus = null
                            showDetailSheet = true
                            map.controller.animateTo(GeoPoint(spot.latitude, spot.longitude))
                        },
                        onCivicReportClick = { report ->
                            selectedCivicReport = report
                            selectedSpot = null
                            selectedLiveBus = null
                            showDetailSheet = true
                            val lat = report.latitude ?: 21.4934
                            val lng = report.longitude ?: 86.9135
                            map.controller.animateTo(GeoPoint(lat, lng))
                        },
                        onLiveBusClick = { bus ->
                            selectedLiveBus = bus
                            selectedSpot = null
                            selectedCivicReport = null
                            showDetailSheet = true
                            map.controller.animateTo(GeoPoint(bus.latitude, bus.longitude))
                        }
                    )
                    map.invalidate()
                }
            )

            // Top Category & Layer Filter Chips
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.White.copy(alpha = 0.95f))
                    .padding(vertical = 6.dp)
            ) {
                if (isCachingAll || preCacheStats.isPreCaching) {
                    LinearProgressIndicator(
                        progress = { if (isCachingAll) cacheProgress else preCacheStats.preCacheProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .testTag("osm_precache_progress_bar"),
                        color = EmeraldGreen,
                        trackColor = Color(0xFFE2E8F0)
                    )
                }

                // OSMDroid Tile Pre-Cache Telemetry & Unstable Network Guard Pill
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 2.dp)
                        .testTag("osm_precache_status_banner"),
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, Color(0xFFCBD5E1))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = "OSMDroid Tile Pre-Cache",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "ପ୍ରି-କ୍ୟାଚ୍: ${preCacheStats.memoryCachedTiles} RAM • ${preCacheStats.diskCachedTiles} ଡିସ୍କ ଟାଇଲ୍ (${String.format(Locale.US, "%.1f", preCacheStats.totalCacheSizeMb)} MB)"
                                } else {
                                    "Pre-Cached: ${preCacheStats.memoryCachedTiles} RAM • ${preCacheStats.diskCachedTiles} Disk Tiles (${String.format(Locale.US, "%.1f", preCacheStats.totalCacheSizeMb)} MB • ${preCacheStats.hitRatePercent}% Hit)"
                                },
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoSlate800,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldGreen.copy(alpha = 0.14f),
                            modifier = Modifier
                                .clickable {
                                    if (!isCachingAll) {
                                        isCachingAll = true
                                        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                                            tileProvider.prefetchAllTourismSpots { progress ->
                                                cacheProgress = progress
                                                if (progress >= 1f) {
                                                    isCachingAll = false
                                                }
                                            }
                                        }
                                        Toast.makeText(
                                            context,
                                            "OSMDroid tiles pre-cached for all ${BALASORE_TOURIST_LOCATIONS.size} Balasore hotspots",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                .testTag("osm_precache_trigger_btn")
                        ) {
                            Text(
                                text = if (isCachingAll) "Caching..." else "⚡ Pre-Cache (${preCacheStats.preCachedHotspotIds.size}/${BALASORE_TOURIST_LOCATIONS.size})",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF065F46),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Row 0: MapView Settings Toggle Switch ('Live Online Map' vs 'Cached Offline Map')
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .testTag("osm_map_view_settings_toggle_card"),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isOfflineOnly) Color(0xFFECFDF5) else Color(0xFFF0F9FF),
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isOfflineOnly) Color(0xFF34D399) else Color(0xFF38BDF8)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isOfflineOnly) Icons.Default.WifiOff else Icons.Default.Wifi,
                                contentDescription = if (isOfflineOnly) "Cached Offline Map Active" else "Live Online Map Active",
                                tint = if (isOfflineOnly) Color(0xFF047857) else OceanBlue,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "MapView Settings",
                                        tint = BentoSlate700,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOfflineOnly) {
                                            if (language == AppLanguage.ODIA) "କ୍ୟାଚ୍ ଅଫଲାଇନ୍ ମ୍ୟାପ୍ (Cached Offline Map)" else "Cached Offline Map Mode"
                                        } else {
                                            if (language == AppLanguage.ODIA) "ଲାଇଭ୍ ଅନଲାଇନ୍ ମ୍ୟାପ୍ (Live Online Map)" else "Live Online Map Mode"
                                        },
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BentoSlate900
                                    )
                                }
                                Text(
                                    text = if (isOfflineOnly) {
                                        "Zero data usage • Offline osmdroid tiles active"
                                    } else {
                                        "Live online tile streaming • Toggle to save data"
                                    },
                                    fontSize = 10.sp,
                                    color = if (isOfflineOnly) Color(0xFF065F46) else Color(0xFF0369A1),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Switch(
                            checked = isOfflineOnly,
                            onCheckedChange = { newMode ->
                                tileProvider.setOfflineOnly(newMode)
                                mapViewInstance?.setUseDataConnection(!newMode)
                                mapViewInstance?.invalidate()
                                val msg = if (newMode) {
                                    "Switched to Cached Offline Map Mode (Zero Data Usage)"
                                } else {
                                    "Switched to Live Online Map Mode"
                                }
                                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = EmeraldGreen,
                                uncheckedThumbColor = Color.White,
                                uncheckedTrackColor = OceanBlue
                            ),
                            modifier = Modifier
                                .semantics {
                                    contentDescription = "Toggle between Live Online Map and Cached Offline Map modes"
                                }
                                .testTag("osm_map_view_settings_mode_switch")
                        )
                    }
                }

                // Row 1: Layer Switcher (Tourism Spots vs Live GPS Buses vs Citizen Civic Eye Overlay)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = showTourismLayer,
                        onClick = { showTourismLayer = !showTourismLayer },
                        label = {
                            Text(
                                text = if (language == AppLanguage.ODIA) "🏛️ ପର୍ଯ୍ୟଟନ (${filteredSpots.size})" else "🏛️ Tourism Spots (${filteredSpots.size})",
                                fontSize = 11.5.sp,
                                fontWeight = if (showTourismLayer) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanBlue,
                            selectedLabelColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("osm_toggle_tourism_layer")
                    )

                    FilterChip(
                        selected = showLiveBusLayer,
                        onClick = { showLiveBusLayer = !showLiveBusLayer },
                        label = {
                            Text(
                                text = if (language == AppLanguage.ODIA) "🚌 ଲାଇଭ୍ ବସ୍ (${liveBuses.size})" else "🚌 Live Buses (${liveBuses.size})",
                                fontSize = 11.5.sp,
                                fontWeight = if (showLiveBusLayer) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = EmeraldGreen,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFECFDF5),
                            labelColor = Color(0xFF065F46)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("osm_toggle_live_bus_layer")
                    )

                    FilterChip(
                        selected = showCivicEyeLayer,
                        onClick = { showCivicEyeLayer = !showCivicEyeLayer },
                        label = {
                            Text(
                                text = if (language == AppLanguage.ODIA) "📸 ସିଭିକ୍ ଆଇ (${filteredCivicReports.size})" else "📸 Civic Eye (${filteredCivicReports.size})",
                                fontSize = 11.5.sp,
                                fontWeight = if (showCivicEyeLayer) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDC2626),
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFFFEF2F2),
                            labelColor = Color(0xFF991B1B)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("osm_toggle_civic_layer")
                    )

                    FilterChip(
                        selected = isDarkMapStyle,
                        onClick = {
                            isDarkMapStyle = !isDarkMapStyle
                            mapViewInstance?.let { mv ->
                                BalasoreOsmDarkStyleHelper.applyMapColorStyle(mv, isDarkMapStyle)
                                mv.invalidate()
                            }
                        },
                        label = {
                            Text(
                                text = if (language == AppLanguage.ODIA) "🌙 ଡାର୍କ ଥିମ୍" else "🌙 Dark Style",
                                fontSize = 11.5.sp,
                                fontWeight = if (isDarkMapStyle) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = BentoSlate900,
                            selectedLabelColor = Color(0xFF38BDF8),
                            containerColor = Color(0xFFF1F5F9),
                            labelColor = BentoSlate800
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("osm_toggle_dark_map_style")
                    )
                }

                // Row 2: Secondary Category / Filter Chips
                if (showCivicEyeLayer) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val civicFilters = listOf(
                            "ALL" to (if (language == AppLanguage.ODIA) "ସବୁ ଅଭିଯୋଗ" else "All (${civicReports.size})"),
                            "POTHOLE" to (if (language == AppLanguage.ODIA) "🕳️ ରାସ୍ତା ଖାଲ" else "🕳️ Potholes"),
                            "DRAINAGE" to (if (language == AppLanguage.ODIA) "🌊 ଡ୍ରେନେଜ୍" else "🌊 Drainage"),
                            "SANITATION" to (if (language == AppLanguage.ODIA) "🗑️ ଆବର୍ଜନା" else "🗑️ Waste"),
                            "STREETLIGHT" to (if (language == AppLanguage.ODIA) "💡 ଲାଇଟ୍" else "💡 Lights")
                        )
                        items(civicFilters) { (key, label) ->
                            val isSel = civicCategoryFilter == key
                            FilterChip(
                                selected = isSel,
                                onClick = { civicCategoryFilter = key },
                                label = { Text(text = label, fontSize = 11.sp, fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF1E293B),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = BentoSlate800
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }

                        // Status toggle filter
                        item {
                            val isActiveFilter = civicStatusFilter == "ACTIVE"
                            FilterChip(
                                selected = isActiveFilter,
                                onClick = { civicStatusFilter = if (isActiveFilter) "ALL" else "ACTIVE" },
                                label = { Text(text = if (language == AppLanguage.ODIA) "ଚାଲୁ ଅଛି" else "Active Only", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF2563EB),
                                    selectedLabelColor = Color.White
                                ),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
                if (showTourismLayer) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val categories = listOf(
                            "ALL" to (if (language == AppLanguage.ODIA) "ସମସ୍ତ ସ୍ଥାନ (${BALASORE_TOURIST_LOCATIONS.size})" else "All Attractions (${BALASORE_TOURIST_LOCATIONS.size})"),
                            "BEACH" to (if (language == AppLanguage.ODIA) "🌊 ବେଳାଭୂମି" else "🌊 Beaches"),
                            "TEMPLE" to (if (language == AppLanguage.ODIA) "🛕 ମନ୍ଦିର" else "🛕 Temples"),
                            "NATURE" to (if (language == AppLanguage.ODIA) "🌿 ବନ୍ୟପ୍ରାଣୀ ଓ ପ୍ରକୃତି" else "🌿 Nature & Wildlife"),
                            "HERITAGE" to (if (language == AppLanguage.ODIA) "🏛️ ଐତିହ୍ୟ ଓ ଦୁର୍ଗ" else "🏛️ Heritage & Forts")
                        )

                        items(categories) { (key, label) ->
                            val isSelected = selectedCategory == key
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = key },
                                label = { Text(text = label, fontSize = 11.5.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OceanBlue,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color(0xFFF1F5F9),
                                    labelColor = BentoSlate800
                                ),
                                border = BorderStroke(1.dp, if (isSelected) OceanBlue else Color(0xFFE2E8F0)),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            // Floating Controls: Zoom In, Zoom Out, Offline Only Toggle
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Zoom In
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { mapViewInstance?.controller?.zoomIn() }
                        .testTag("osm_zoom_in_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = BentoSlate800)
                    }
                }

                // Zoom Out
                Surface(
                    shape = CircleShape,
                    color = Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(42.dp)
                        .clickable { mapViewInstance?.controller?.zoomOut() }
                        .testTag("osm_zoom_out_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = BentoSlate800)
                    }
                }

                // Offline Mode Force Toggle
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isOfflineOnly) Color(0xFF0F172A) else Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .clickable {
                            val newMode = !isOfflineOnly
                            tileProvider.setOfflineOnly(newMode)
                            val msg = if (newMode) "Strict Offline Tile Provider Activated" else "Online Tile Fetching Enabled"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        }
                        .testTag("osm_offline_mode_toggle")
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (isOfflineOnly) Icons.Default.WifiOff else Icons.Default.Wifi,
                            contentDescription = "Toggle Offline Mode",
                            tint = if (isOfflineOnly) EmeraldGreen else BentoSlate700,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = if (isOfflineOnly) "OFFLINE" else "HYBRID",
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isOfflineOnly) Color.White else BentoSlate800
                        )
                    }
                }
            }

            // Bottom Selected Spot Detail Card
            AnimatedVisibility(
                visible = showDetailSheet && selectedSpot != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                selectedSpot?.let { spot ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .testTag("osm_selected_spot_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = OceanBlue.copy(alpha = 0.12f),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = spot.highlightEmoji, fontSize = 22.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.ODIA) spot.nameOr else spot.nameEn,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoSlate900,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${spot.categoryEn} • ⭐ ${spot.rating} (${spot.reviewCount} reviews)",
                                            fontSize = 11.sp,
                                            color = OceanBlue,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = { showDetailSheet = false },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = BentoSlate700
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            com.example.ui.components.HotspotOfflineHighResGalleryCarousel(
                                spotName = spot.nameEn,
                                spotCategory = spot.categoryEn,
                                isOdia = language == AppLanguage.ODIA,
                                compactHeight = 124.dp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = if (language == AppLanguage.ODIA) spot.shortDescOr else spot.shortDescEn,
                                fontSize = 12.sp,
                                color = BentoSlate700,
                                lineHeight = 17.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Transit & Timings Quick Chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFF1F5F9),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "🕒", fontSize = 12.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = spot.timings,
                                            fontSize = 10.5.sp,
                                            color = BentoSlate800,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }

                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFECFDF5),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = "🚌", fontSize = 12.sp)
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            text = spot.nearestTransitStop,
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF065F46),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Action Buttons: Directions + Reviews & Rate + Offline Route
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        TourismMapDirectionsHelper.launchDirections(
                                            context = context,
                                            spotName = spot.nameEn,
                                            destLat = spot.latitude,
                                            destLng = spot.longitude
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("osm_external_directions_button"),
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ଦିଗ ନିର୍ଦ୍ଦେଶ" else "Directions",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { reviewModalSpot = spot },
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .testTag("osm_spot_reviews_button"),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, AmberGold)
                                ) {
                                    Icon(imageVector = Icons.Default.RateReview, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ରିଭ୍ୟୁ ଓ ରେଟିଂ" else "Reviews & Rate",
                                        fontSize = 12.sp,
                                        color = Color(0xFFB45309),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        mapViewInstance?.controller?.apply {
                                            setZoom(15.5)
                                            animateTo(GeoPoint(spot.latitude, spot.longitude))
                                        }
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("osm_focus_spot_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.45f))
                                ) {
                                    Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ସ୍ଥାନ ଦେଖନ୍ତୁ" else "Focus Pin",
                                        fontSize = 11.5.sp,
                                        color = OceanBlue,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                OutlinedButton(
                                    onClick = { startOfflineNavigation(spot) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("osm_offline_route_button"),
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.5f))
                                ) {
                                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(15.dp))
                                    Spacer(Modifier.width(4.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ଅଫଲାଇନ୍ ରୁଟ୍" else "Offline Route",
                                        fontSize = 11.5.sp,
                                        color = EmeraldGreen,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Bottom Selected Civic Report Detail Card (Citizen Civic Eye)
            AnimatedVisibility(
                visible = showDetailSheet && selectedCivicReport != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                selectedCivicReport?.let { report ->
                    CivicReportDetailCard(
                        report = report,
                        language = language,
                        onAdvanceStatus = onReportStatusChange,
                        onNavigate = { lat: Double, lng: Double, name: String ->
                            val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(name)})")
                            val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            try {
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://maps.google.com/?q=$lat,$lng"))
                                context.startActivity(browserIntent)
                            }
                        },
                        onDismiss = {
                            selectedCivicReport = null
                            showDetailSheet = false
                        }
                    )
                }
            }
            // Bottom Selected Live Bus Telemetry Detail Card
            AnimatedVisibility(
                visible = showDetailSheet && selectedLiveBus != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            ) {
                selectedLiveBus?.let { bus ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                            .testTag("osm_selected_live_bus_card"),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        border = BorderStroke(1.5.dp, EmeraldGreen)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = EmeraldGreen.copy(alpha = 0.14f),
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(text = "🚌", fontSize = 22.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "${bus.routeNumber} • ${bus.fleetNumber} (${bus.busType})",
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 14.sp,
                                            color = BentoSlate900
                                        )
                                        Text(
                                            text = if (language == AppLanguage.ODIA) bus.routeNameOd else bus.routeNameEn,
                                            fontSize = 11.5.sp,
                                            color = EmeraldGreen,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        selectedLiveBus = null
                                        showDetailSheet = false
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close Live Bus Card", tint = BentoSlate700)
                                }
                            }

                            Text(
                                text = "📍 Current Stop: ${bus.currentStop} ➔ Next: ${bus.nextStop} (ETA ${bus.etaNextStopMins} mins)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = BentoSlate800
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = String.format(Locale.US, "🛰️ GPS: %.4f°N, %.4f°E", bus.latitude, bus.longitude),
                                    fontSize = 11.sp,
                                    color = BentoSlate700
                                )
                                Text(
                                    text = "⚡ ${String.format(Locale.US, "%.0f", bus.speedKmh)} km/h • ${bus.occupancyStatus.replace("_", " ")}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlue
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    reviewModalSpot?.let { modalSpot ->
        TourismSpotReviewsAndDirectionsSheet(
            spotId = modalSpot.id,
            spotName = if (language == AppLanguage.ODIA) modalSpot.nameOr else modalSpot.nameEn,
            spotCategory = if (language == AppLanguage.ODIA) modalSpot.categoryOr else modalSpot.categoryEn,
            spotLocation = modalSpot.nearestTransitStop,
            spotLatitude = modalSpot.latitude,
            spotLongitude = modalSpot.longitude,
            baselineRating = modalSpot.rating,
            language = language,
            onStartOfflineRoute = { startOfflineNavigation(modalSpot) },
            onDismiss = { reviewModalSpot = null }
        )
    }
}

/**
 * Creates and binds OSMDroid markers with custom icons and popups for Balasore tourist locations,
 * live GPS transit vehicles, and Citizen Civic Eye infrastructure reports.
 */
private fun updateOsmMarkers(
    mapView: MapView,
    spots: List<TouristLocationItem>,
    civicReports: List<CivicReportEntity>,
    liveBuses: List<LiveCityBusDto>,
    showTourism: Boolean,
    showCivicEye: Boolean,
    showLiveBuses: Boolean,
    context: Context,
    language: AppLanguage,
    onSpotClick: (TouristLocationItem) -> Unit,
    onCivicReportClick: (CivicReportEntity) -> Unit,
    onLiveBusClick: (LiveCityBusDto) -> Unit
) {
    // Retain non-marker overlays (like compass and scale bar)
    val nonMarkerOverlays = mapView.overlays.filter { it !is Marker }
    mapView.overlays.clear()
    mapView.overlays.addAll(nonMarkerOverlays)

    if (showTourism) {
        for (spot in spots) {
            val marker = Marker(mapView).apply {
                position = GeoPoint(spot.latitude, spot.longitude)
                title = if (language == AppLanguage.ODIA) spot.nameOr else spot.nameEn
                snippet = "${spot.categoryEn} • ⭐ ${spot.rating}"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                // Custom pin icon with spot emoji
                icon = createOsmMarkerDrawable(context, spot.highlightEmoji, spot.categoryGroup)

                setOnMarkerClickListener { _, _ ->
                    onSpotClick(spot)
                    showInfoWindow()
                    true
                }
            }
            mapView.overlays.add(marker)
        }
    }

    if (showLiveBuses) {
        for (bus in liveBuses) {
            val busMarker = Marker(mapView).apply {
                position = GeoPoint(bus.latitude, bus.longitude)
                title = "🚌 ${bus.routeNumber} (${bus.fleetNumber})"
                snippet = "Next: ${bus.nextStop} in ${bus.etaNextStopMins}m • ${String.format(Locale.US, "%.0f", bus.speedKmh)} km/h"
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                icon = createOsmMarkerDrawable(context, "🚌", "NATURE")
                setOnMarkerClickListener { _, _ ->
                    onLiveBusClick(bus)
                    showInfoWindow()
                    true
                }
            }
            mapView.overlays.add(busMarker)
        }
    }

    if (showCivicEye) {
        updateOsmCivicMarkers(
            mapView = mapView,
            reports = civicReports,
            context = context,
            language = language,
            onReportClick = onCivicReportClick
        )
    }

    mapView.invalidate()
}

/**
 * Generates an Android BitmapDrawable marker for OSMDroid featuring the spot's emoji badge.
 */
private fun createOsmMarkerDrawable(context: Context, emoji: String, categoryGroup: String): BitmapDrawable {
    val size = 96
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val pinColor = when (categoryGroup) {
        "BEACH" -> android.graphics.Color.rgb(2, 132, 199) // Ocean Blue
        "TEMPLE" -> android.graphics.Color.rgb(217, 119, 6) // Amber/Saffron
        "NATURE" -> android.graphics.Color.rgb(16, 185, 129) // Emerald Green
        else -> android.graphics.Color.rgb(99, 102, 241) // Indigo
    }

    // Draw Pin Teardrop / Circle Base
    val bgPaint = Paint().apply {
        isAntiAlias = true
        color = pinColor
        style = Paint.Style.FILL
    }
    val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(80, 0, 0, 0)
        style = Paint.Style.FILL
    }
    // Shadow
    canvas.drawCircle(48f, 52f, 36f, shadowPaint)
    // Pin head
    canvas.drawCircle(48f, 44f, 36f, bgPaint)

    // Inner white disc
    val whitePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(48f, 44f, 26f, whitePaint)

    // Emoji icon
    val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(emoji, 48f, 54f, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Updates OSMDroid map overlays with citizen civic report markers.
 */
private fun updateOsmCivicMarkers(
    mapView: MapView,
    reports: List<CivicReportEntity>,
    context: Context,
    language: AppLanguage,
    onReportClick: (CivicReportEntity) -> Unit
) {
    for (report in reports) {
        val lat = report.latitude ?: 21.4934
        val lng = report.longitude ?: 86.9135
        val marker = Marker(mapView).apply {
            position = GeoPoint(lat, lng)
            title = report.title
            snippet = "${report.category} • ${report.status} (${report.wardLocation})"
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            icon = createOsmCivicMarkerDrawable(context, report)
            setOnMarkerClickListener { _, _ ->
                onReportClick(report)
                showInfoWindow()
                true
            }
        }
        mapView.overlays.add(marker)
    }
}

/**
 * Creates custom pin drawable for Civic Report markers on OSMDroid.
 */
private fun createOsmCivicMarkerDrawable(context: Context, report: CivicReportEntity): BitmapDrawable {
    val size = 96
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    val (emoji, pinColor) = when {
        report.category.contains("Pothole", ignoreCase = true) || report.category.contains("Road", ignoreCase = true) ->
            "🕳️" to android.graphics.Color.rgb(234, 88, 12)
        report.category.contains("Water", ignoreCase = true) || report.category.contains("Drain", ignoreCase = true) ->
            "🌊" to android.graphics.Color.rgb(2, 132, 199)
        report.category.contains("Light", ignoreCase = true) ->
            "💡" to android.graphics.Color.rgb(234, 179, 8)
        report.category.contains("Waste", ignoreCase = true) || report.category.contains("Sanitation", ignoreCase = true) ->
            "🗑️" to android.graphics.Color.rgb(16, 185, 129)
        report.urgency == "CRITICAL" ->
            "🚨" to android.graphics.Color.rgb(220, 38, 38)
        else ->
            "⚠️" to android.graphics.Color.rgb(124, 58, 237)
    }

    val shadowPaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.argb(80, 0, 0, 0)
        style = Paint.Style.FILL
    }
    canvas.drawCircle(48f, 52f, 36f, shadowPaint)

    val bgPaint = Paint().apply {
        isAntiAlias = true
        color = if (report.status == "RESOLVED") android.graphics.Color.rgb(16, 185, 129) else pinColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(48f, 44f, 36f, bgPaint)

    val whitePaint = Paint().apply {
        isAntiAlias = true
        color = android.graphics.Color.WHITE
        style = Paint.Style.FILL
    }
    canvas.drawCircle(48f, 44f, 26f, whitePaint)

    val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }
    canvas.drawText(if (report.status == "RESOLVED") "✅" else emoji, 48f, 54f, textPaint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Bottom detail card displayed when a citizen civic eye report is selected on the OSM map.
 */
@Composable
fun CivicReportDetailCard(
    report: CivicReportEntity,
    language: AppLanguage,
    onAdvanceStatus: ((String) -> Unit)?,
    onNavigate: (Double, Double, String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val statusColor = when (report.status) {
        "RESOLVED" -> EmeraldGreen
        "IN_PROGRESS" -> OceanBlue
        else -> AmberGold
    }

    val statusText = when (report.status) {
        "RESOLVED" -> if (language == AppLanguage.ODIA) "ସମାଧାନ ହୋଇଛି" else "Resolved"
        "IN_PROGRESS" -> if (language == AppLanguage.ODIA) "କାର୍ଯ୍ୟ ଚାଲିଛି" else "In Progress"
        else -> if (language == AppLanguage.ODIA) "ବକେୟା" else "Pending"
    }

    val categoryEmoji = when {
        report.category.contains("Pothole", ignoreCase = true) || report.category.contains("Road", ignoreCase = true) -> "🕳️"
        report.category.contains("Water", ignoreCase = true) || report.category.contains("Drain", ignoreCase = true) -> "🌊"
        report.category.contains("Light", ignoreCase = true) -> "💡"
        report.category.contains("Waste", ignoreCase = true) || report.category.contains("Sanitation", ignoreCase = true) -> "🗑️"
        else -> "⚠️"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp)
            .testTag("osm_civic_report_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusColor.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(text = categoryEmoji, fontSize = 22.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = report.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = statusColor.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = statusText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📍 ${report.wardLocation}",
                                fontSize = 11.sp,
                                color = BentoSlate700,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = BentoSlate700
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Description
            Text(
                text = report.description,
                fontSize = 12.5.sp,
                color = BentoSlate800,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            if (report.resolutionNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = BentoSlate100,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "📋 ${report.resolutionNotes}",
                        fontSize = 11.sp,
                        color = BentoSlate700,
                        modifier = Modifier.padding(8.dp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (onAdvanceStatus != null && report.status != "RESOLVED") {
                    OutlinedButton(
                        onClick = { onAdvanceStatus(report.id) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "ଅଗ୍ରଗତି କରନ୍ତୁ" else "Advance Status",
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                Button(
                    onClick = {
                        val lat = report.latitude ?: 21.4934
                        val lng = report.longitude ?: 86.9135
                        onNavigate(lat, lng, report.title)
                    },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "Navigate",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଦିଗ ନିର୍ଦ୍ଦେଶ" else "Directions",
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

