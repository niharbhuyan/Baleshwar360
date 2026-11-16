package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Directions
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.DirectionsTransit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Train
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.data.model.AppLanguage
import com.example.data.model.BalasoreTransitDataSource
import com.example.data.model.BusRouteOverlay
import com.example.data.model.BusStopPoint
import com.example.data.model.RailwayStationNode
import com.example.data.model.TrainStatusMarker
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.gms.maps.model.TileOverlayOptions
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.BuildConfig
import com.example.data.maps.BalasoreGeoNewsLocation
import com.example.data.maps.BalasoreInteractiveMapDataSource
import com.example.data.maps.BalasoreOfflineTileCacheManager
import com.example.data.maps.BalasoreOfflineTileProvider
import com.example.data.maps.BalasoreWeatherAlertZone
import com.example.data.remote.LiveCityBusDto
import com.example.data.repository.LocalTransportRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Data structure representing a transit reference origin for distance calculations.
 */
data class TransitOrigin(
    val id: String,
    val nameEn: String,
    val nameOr: String,
    val latitude: Double,
    val longitude: Double,
    val iconEmoji: String
)

val BALASORE_TRANSIT_ORIGINS = listOf(
    TransitOrigin("station", "Balasore Rly Station", "ବାଲେଶ୍ୱର ରେଳ ଷ୍ଟେସନ", 21.4934, 86.9135, "🚉"),
    TransitOrigin("sahadevkhunta", "Sahadevkhunta Bus Stand", "ସହଦେବଖୁଣ୍ଟା ବସ୍ ଷ୍ଟାଣ୍ଡ", 21.4880, 86.9320, "🚌"),
    TransitOrigin("remuna_golei", "Remuna Golei NH-16", "ରେମୁଣା ଗୋଲେଇ ଛକ", 21.5180, 86.8920, "🚩"),
    TransitOrigin("chandipur", "Chandipur Sea Beach", "ଚାନ୍ଦିପୁର ବେଳାଭୂମି", 21.4695, 87.0185, "🏖️")
)

/**
 * Interactive 'Explore' section using Google Maps API & Offline Vector Map that:
 *  - Plots local tourism hotspots, sacred temples, and historic landmarks with clickable markers.
 *  - Displays geotagged local news locations with clickable press pins & full breaking details.
 *  - Displays geospatial severe weather alert areas (circular hazard zones & clickable markers).
 *  - Overlays local bus routes (Mo Bus 101, 102, 103, OSRTC) with interactive polylines and stops.
 *  - Overlays railway corridor and live train status markers along the Howrah-Bhubaneswar mainline.
 */
@Composable
fun BalasoreExploreMapSection(
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier,
    onOpenFullScreenMap: () -> Unit = {},
    onAddToItinerary: ((TouristLocationItem) -> Unit)? = null
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Explore Layer Modes:
    // "ALL_OVERLAYS": Hotspots + News Locations + Weather Alert Areas + Bus Routes + Trains
    // "NEWS_LOCATIONS": Geotagged Local News Stories across Balasore
    // "WEATHER_ALERTS": Geospatial Severe Weather Alert Areas & Radar Zones
    // "HOTSPOTS": Focus on Temples, Beaches, Landmarks
    // "BUS_ROUTES": Focus on Bus Routes and Bus Stops
    // "TRAIN_STATUS": Focus on Railway line and Live Trains
    var activeExploreMode by remember { mutableStateOf("ALL_OVERLAYS") }

    // Sub-category filter for Hotspots mode: "ALL", "TEMPLE", "BEACH", "LANDMARK", "NATURE"
    var selectedHotspotCategory by remember { mutableStateOf("ALL") }

    // Origin selector for live distance calculations
    var selectedOrigin by remember { mutableStateOf(BALASORE_TRANSIT_ORIGINS.first()) }

    // Selected Route / Train / Hotspot / News Location / Weather Alert Area / Live GPS Bus for detailed sheet
    var selectedBusRoute by remember { mutableStateOf<BusRouteOverlay?>(null) }
    var selectedTrain by remember { mutableStateOf<TrainStatusMarker?>(null) }
    var selectedStation by remember { mutableStateOf<RailwayStationNode?>(null) }
    var selectedNewsLocation by remember { mutableStateOf<BalasoreGeoNewsLocation?>(null) }
    var selectedWeatherAlertZone by remember { mutableStateOf<BalasoreWeatherAlertZone?>(null) }
    var selectedHotspot by remember {
        mutableStateOf<TouristLocationItem?>(BALASORE_TOURIST_LOCATIONS.firstOrNull())
    }

    // Real-Time Bus GPS Telemetry Feed (CRUT Mo Bus & OSRTC Local Transit Providers)
    val coroutineScope = rememberCoroutineScope()
    val transportRepository = remember { LocalTransportRepository() }
    var gpsCycle by remember { mutableIntStateOf(0) }
    var liveBuses by remember {
        mutableStateOf(transportRepository.createBaselineBalasoreBuses(0))
    }
    var selectedLiveBus by remember {
        mutableStateOf<LiveCityBusDto?>(liveBuses.firstOrNull())
    }
    var selectedTransitProviderFilter by remember { mutableStateOf("ALL") } // ALL, CRUT_MO_BUS, OSRTC, APPROACHING
    var gpsCountdownSecs by remember { mutableIntStateOf(10) }
    var lastGpsSyncLabel by remember {
        mutableStateOf(SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date()))
    }
    var showLiveGpsBusLayer by remember { mutableStateOf(true) }

    // Continuous real-time GPS feed ticker (respects Battery-Saver toggle if enabled)
    LaunchedEffect(Unit) {
        val initialResult = transportRepository.fetchLiveCityBuses(0).getOrNull()
        if (!initialResult.isNullOrEmpty()) {
            liveBuses = initialResult
            if (selectedLiveBus == null) {
                selectedLiveBus = initialResult.firstOrNull()
            }
        }
        while (true) {
            delay(1000L)
            if (gpsCountdownSecs > 1) {
                gpsCountdownSecs -= 1
            } else {
                val isBatterySaver = context.getSharedPreferences("baleshwar_battery_prefs", Context.MODE_PRIVATE)
                    .getBoolean("battery_saving_mode_enabled", false)
                gpsCountdownSecs = if (isBatterySaver) 25 else 10
                gpsCycle += 1
                val updated = transportRepository.fetchLiveCityBuses(gpsCycle).getOrDefault(
                    transportRepository.createBaselineBalasoreBuses(gpsCycle)
                )
                liveBuses = updated
                selectedLiveBus?.let { currentSel ->
                    selectedLiveBus = updated.find { it.busId == currentSel.busId } ?: currentSel
                }
                lastGpsSyncLabel = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
            }
        }
    }

    val filteredLiveBuses = remember(liveBuses, selectedTransitProviderFilter) {
        liveBuses.filter { bus ->
            when (selectedTransitProviderFilter) {
                "CRUT_MO_BUS" -> bus.busType.contains("Mo Bus", ignoreCase = true) || bus.busId.contains("CRUT", ignoreCase = true)
                "OSRTC" -> bus.routeNumber in listOf("Route 74", "Route 75") || bus.busType.contains("Regional", ignoreCase = true) || bus.busType.contains("Feeder", ignoreCase = true)
                "APPROACHING" -> bus.etaNextStopMins <= 4
                else -> true
            }
        }
    }

    // Modal dialog for complete bus schedule timetable
    var showFullBusScheduleDialog by remember { mutableStateOf<BusRouteOverlay?>(null) }

    // Google Maps references
    var googleMapInstance by remember { mutableStateOf<GoogleMap?>(null) }
    var currentMapType by remember { mutableIntStateOf(GoogleMap.MAP_TYPE_NORMAL) }

    val hotspotMarkersMap = remember { mutableMapOf<String, Marker>() }
    val newsLocationMarkersMap = remember { mutableMapOf<String, Marker>() }
    val weatherAlertMarkersMap = remember { mutableMapOf<String, Marker>() }
    val weatherAlertCirclesMap = remember { mutableMapOf<String, Circle>() }
    val busStopMarkersMap = remember { mutableMapOf<String, Marker>() }
    val liveBusMarkersMap = remember { mutableMapOf<String, Marker>() }
    val trainMarkersMap = remember { mutableMapOf<String, Marker>() }
    val stationMarkersMap = remember { mutableMapOf<String, Marker>() }
    val busPolylinesMap = remember { mutableMapOf<String, Polyline>() }
    var railPolylineInstance by remember { mutableStateOf<Polyline?>(null) }

    val newsLocationsList = remember { BalasoreInteractiveMapDataSource.BALASORE_GEO_NEWS_LOCATIONS }
    val weatherAlertZonesList = remember { BalasoreInteractiveMapDataSource.BALASORE_WEATHER_ALERT_ZONES }

    // Filtered list of hotspots
    val filteredHotspots = remember(selectedHotspotCategory) {
        if (selectedHotspotCategory == "ALL") {
            BALASORE_TOURIST_LOCATIONS
        } else {
            BALASORE_TOURIST_LOCATIONS.filter { it.categoryGroup == selectedHotspotCategory }
        }
    }

    // Map Engine configuration: Default to 100% offline district vector map
    var useVectorEngine by remember { mutableStateOf(true) }
    val isMapsKeyConfigured = remember {
        BuildConfig.MAPS_API_KEY.isNotBlank()
    }

    // Lazy MapView instance with lifecycle: Only initialized if user explicitly switches to Google Maps satellite
    val mapView = remember(useVectorEngine) {
        if (!useVectorEngine && isMapsKeyConfigured) {
            MapView(context).apply {
                onCreate(Bundle())
            }
        } else {
            null
        }
    }

    DisposableEffect(lifecycleOwner, mapView) {
        val observer = LifecycleEventObserver { _, event ->
            mapView?.let { mv ->
                when (event) {
                    Lifecycle.Event.ON_START -> mv.onStart()
                    Lifecycle.Event.ON_RESUME -> mv.onResume()
                    Lifecycle.Event.ON_PAUSE -> mv.onPause()
                    Lifecycle.Event.ON_STOP -> mv.onStop()
                    Lifecycle.Event.ON_DESTROY -> mv.onDestroy()
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try {
                mapView?.onDestroy()
            } catch (_: Exception) {}
        }
    }

    // Core function to refresh all map overlays based on active mode & selections
    fun refreshMapOverlays(map: GoogleMap) {
        map.clear()
        hotspotMarkersMap.clear()
        newsLocationMarkersMap.clear()
        weatherAlertMarkersMap.clear()
        weatherAlertCirclesMap.clear()
        busStopMarkersMap.clear()
        liveBusMarkersMap.clear()
        trainMarkersMap.clear()
        stationMarkersMap.clear()
        busPolylinesMap.clear()
        railPolylineInstance = null

        val showHotspots = activeExploreMode == "ALL_OVERLAYS" || activeExploreMode == "HOTSPOTS"
        val showNewsLocations = activeExploreMode == "ALL_OVERLAYS" || activeExploreMode == "NEWS_LOCATIONS"
        val showWeatherAlerts = activeExploreMode == "ALL_OVERLAYS" || activeExploreMode == "WEATHER_ALERTS"
        val showBusRoutes = activeExploreMode == "ALL_OVERLAYS" || activeExploreMode == "BUS_ROUTES"
        val showTrains = activeExploreMode == "ALL_OVERLAYS" || activeExploreMode == "TRAIN_STATUS"

        // 0. Base raster tile overlay for seamless rendering without server authorization dependence
        try {
            map.addTileOverlay(
                TileOverlayOptions()
                    .tileProvider(BalasoreOfflineTileProvider(context))
                    .zIndex(1f)
            )
        } catch (_: Exception) {}

        // 1. Draw Railway Corridor & Stations (if Train or All mode)
        if (showTrains) {
            val railOptions = PolylineOptions()
                .addAll(BalasoreTransitDataSource.BALASORE_RAIL_CORRIDOR_POINTS)
                .color(0xFF475569.toInt()) // Slate charcoal for rail line
                .width(9f)
                .zIndex(4f)
                .clickable(true)

            railPolylineInstance = map.addPolyline(railOptions)

            // Railway Stations
            BalasoreTransitDataSource.BALASORE_RAILWAY_STATIONS.forEach { stn ->
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(stn.lat, stn.lng))
                        .title("${stn.nameEn} (${stn.stationCode})")
                        .snippet("${stn.platformsCount} Platforms • ${stn.upcomingTrainsCount} Trains")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                )
                if (marker != null) {
                    stationMarkersMap[stn.id] = marker
                }
            }

            // Live Train Status Markers
            BalasoreTransitDataSource.LIVE_TRAIN_STATUSES.forEach { train ->
                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(train.currentLat, train.currentLng))
                        .title("🚆 ${train.trainNumber} ${train.trainNameEn}")
                        .snippet("${train.statusText} • Speed: ${train.speedKmh} km/h")
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                if (train.statusType == "DELAYED") BitmapDescriptorFactory.HUE_YELLOW
                                else BitmapDescriptorFactory.HUE_ROSE
                            )
                        )
                )
                if (marker != null) {
                    trainMarkersMap[train.id] = marker
                }
            }
        }

        // 2. Draw Local Bus Routes, Stops & Real-Time GPS Bus Vehicles (if Bus or All mode)
        if (showBusRoutes) {
            BalasoreTransitDataSource.BALASORE_BUS_ROUTES.forEach { route ->
                val isSelected = selectedBusRoute?.id == route.id
                val polylineOptions = PolylineOptions()
                    .addAll(route.pathPoints)
                    .color(if (isSelected) 0xFF0284C7.toInt() else route.colorArgb)
                    .width(if (isSelected) 14f else 8f)
                    .zIndex(if (isSelected) 12f else 6f)
                    .clickable(true)

                val polyline = map.addPolyline(polylineOptions)
                busPolylinesMap[route.id] = polyline

                // If this route is selected or in Bus mode, add key stop markers
                if (isSelected || activeExploreMode == "BUS_ROUTES") {
                    route.stops.filter { it.isTransferHub || isSelected }.forEach { stop ->
                        val stopMarker = map.addMarker(
                            MarkerOptions()
                                .position(LatLng(stop.lat, stop.lng))
                                .title("🚏 ${stop.nameEn} (${route.routeNumber})")
                                .snippet("+${stop.scheduledDepartureOffsetMins}m from origin • ${stop.landmarkInfo}")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        )
                        if (stopMarker != null) {
                            busStopMarkersMap[stop.id] = stopMarker
                        }
                    }
                }
            }

            // Plot Live GPS Bus Vehicles from local transit providers on GoogleMap MapView
            if (showLiveGpsBusLayer) {
                filteredLiveBuses.forEach { bus ->
                    val isSelBus = selectedLiveBus?.busId == bus.busId
                    val busMarker = map.addMarker(
                        MarkerOptions()
                            .position(LatLng(bus.latitude, bus.longitude))
                            .title("🚌 ${bus.routeNumber} (${bus.fleetNumber}) • ${bus.busType}")
                            .snippet("Next: ${bus.nextStop} in ${bus.etaNextStopMins}m • ${String.format(Locale.US, "%.0f", bus.speedKmh)} km/h")
                            .icon(
                                BitmapDescriptorFactory.defaultMarker(
                                    if (isSelBus) BitmapDescriptorFactory.HUE_GREEN
                                    else BitmapDescriptorFactory.HUE_AZURE
                                )
                            )
                    )
                    if (busMarker != null) {
                        liveBusMarkersMap[bus.busId] = busMarker
                    }
                }
            }
        }

        // 3. Draw Tourism Hotspots, Temples & Landmarks (if Hotspot or All mode)
        if (showHotspots) {
            filteredHotspots.forEach { spot ->
                val markerHue = when (spot.categoryGroup) {
                    "TEMPLE" -> BitmapDescriptorFactory.HUE_ORANGE
                    "BEACH" -> BitmapDescriptorFactory.HUE_AZURE
                    "LANDMARK" -> BitmapDescriptorFactory.HUE_VIOLET
                    "NATURE" -> BitmapDescriptorFactory.HUE_GREEN
                    else -> BitmapDescriptorFactory.HUE_YELLOW
                }

                val marker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(spot.latitude, spot.longitude))
                        .title(if (language == AppLanguage.ODIA) spot.nameOr else spot.nameEn)
                        .snippet("${spot.categoryEn} • ${spot.nearestTransitStop}")
                        .icon(BitmapDescriptorFactory.defaultMarker(markerHue))
                )
                if (marker != null) {
                    hotspotMarkersMap[spot.id] = marker
                }
            }
        }

        // 4. Draw Geospatial Weather Alert Areas (CircleOptions + Center Marker)
        if (showWeatherAlerts) {
            weatherAlertZonesList.forEach { zone ->
                val strokeArgb = when (zone.severity) {
                    "CRITICAL" -> 0xFFDC2626.toInt()
                    "SEVERE" -> 0xFFEA580C.toInt()
                    else -> 0xFFD97706.toInt()
                }
                val fillArgb = when (zone.severity) {
                    "CRITICAL" -> 0x38DC2626
                    "SEVERE" -> 0x33EA580C
                    else -> 0x2DD97706
                }

                val circle = map.addCircle(
                    CircleOptions()
                        .center(LatLng(zone.centerLat, zone.centerLng))
                        .radius(zone.radiusMeters)
                        .strokeColor(strokeArgb)
                        .strokeWidth(4f)
                        .fillColor(fillArgb)
                        .zIndex(7f)
                        .clickable(true)
                )
                weatherAlertCirclesMap[zone.id] = circle

                val wxMarker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(zone.centerLat, zone.centerLng))
                        .title("${zone.emoji} [${zone.severity}] ${if (language == AppLanguage.ODIA) zone.alertTitleOr else zone.alertTitleEn}")
                        .snippet("${zone.zoneNameEn} • Wind ${zone.windSpeedKmh} km/h • Rain ${zone.expectedRainfallMm} mm")
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                if (zone.severity == "CRITICAL") BitmapDescriptorFactory.HUE_RED
                                else BitmapDescriptorFactory.HUE_ORANGE
                            )
                        )
                )
                if (wxMarker != null) {
                    weatherAlertMarkersMap[zone.id] = wxMarker
                }
            }
        }

        // 5. Draw Geotagged Local News Locations
        if (showNewsLocations) {
            newsLocationsList.forEach { newsLoc ->
                val newsMarker = map.addMarker(
                    MarkerOptions()
                        .position(LatLng(newsLoc.latitude, newsLoc.longitude))
                        .title("📰 ${if (language == AppLanguage.ODIA) newsLoc.headlineOr else newsLoc.headlineEn}")
                        .snippet("${newsLoc.locationNameEn} • ${newsLoc.category} • ${newsLoc.publishedTimeAgo}")
                        .icon(
                            BitmapDescriptorFactory.defaultMarker(
                                if (newsLoc.isBreaking) BitmapDescriptorFactory.HUE_MAGENTA
                                else BitmapDescriptorFactory.HUE_VIOLET
                            )
                        )
                )
                if (newsMarker != null) {
                    newsLocationMarkersMap[newsLoc.id] = newsMarker
                }
            }
        }
    }

    // Reactively refresh map whenever active mode, selections, filters, or live GPS buses change
    LaunchedEffect(
        activeExploreMode,
        selectedHotspotCategory,
        selectedBusRoute,
        selectedTrain,
        selectedNewsLocation,
        selectedWeatherAlertZone,
        selectedLiveBus,
        filteredLiveBuses,
        showLiveGpsBusLayer,
        googleMapInstance
    ) {
        googleMapInstance?.let { map ->
            refreshMapOverlays(map)
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("balasore_explore_map_section"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoSlate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp, bottom = 14.dp)
        ) {
            // 1. Header with Title & Fullscreen Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = OceanBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = "Explore",
                                tint = OceanBlue,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ଇଣ୍ଟରାକ୍ଟିଭ୍ ମ୍ୟାପ୍" else "Balasore Interactive Map",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoSlate900
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE0F2FE)
                            ) {
                                Text(
                                    text = "News • Weather • Hotspots",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0369A1),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (language == AppLanguage.ODIA)
                                "ଖବର ସ୍ଥାନ, ପାଣିପାଗ ସତର୍କତା, ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ଓ ବସ୍/ଟ୍ରେନ୍ ରୁଟ୍"
                            else
                                "Tap news pins, weather alert zones & tourist hotspots for details",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BentoSlate600,
                                fontSize = 11.5.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Full screen button
                IconButton(
                    onClick = onOpenFullScreenMap,
                    modifier = Modifier
                        .size(36.dp)
                        .background(BentoSlate100, CircleShape)
                        .testTag("explore_section_fullscreen_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Fullscreen,
                        contentDescription = "Full Screen Map",
                        tint = BentoSlate700,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Primary Explore Mode Segmented Bar (All Overlays, News Locations, Weather Alert Areas, Hotspots, Bus Routes, Live Trains)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val modes = listOf(
                    Triple("ALL_OVERLAYS", if (language == AppLanguage.ODIA) "ସବୁ ସ୍ତର (All)" else "All Overlays", "🗺️"),
                    Triple("NEWS_LOCATIONS", if (language == AppLanguage.ODIA) "ଖବର ସ୍ଥାନ (${newsLocationsList.size})" else "News Locations (${newsLocationsList.size})", "📰"),
                    Triple("WEATHER_ALERTS", if (language == AppLanguage.ODIA) "ପାଣିପାଗ ସତର୍କତା (${weatherAlertZonesList.size})" else "Weather Alerts (${weatherAlertZonesList.size})", "⛈️"),
                    Triple("HOTSPOTS", if (language == AppLanguage.ODIA) "ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ (${BALASORE_TOURIST_LOCATIONS.size})" else "Hotspots (${BALASORE_TOURIST_LOCATIONS.size})", "🛕"),
                    Triple("BUS_ROUTES", if (language == AppLanguage.ODIA) "ବସ୍ ରୁଟ୍ (${BalasoreTransitDataSource.BALASORE_BUS_ROUTES.size})" else "Bus Routes (${BalasoreTransitDataSource.BALASORE_BUS_ROUTES.size})", "🚌"),
                    Triple("TRAIN_STATUS", if (language == AppLanguage.ODIA) "ଲାଇଭ୍ ଟ୍ରେନ୍ (${BalasoreTransitDataSource.LIVE_TRAIN_STATUSES.size})" else "Live Trains (${BalasoreTransitDataSource.LIVE_TRAIN_STATUSES.size})", "🚆")
                )

                modes.forEach { (modeKey, label, emoji) ->
                    val isSelected = activeExploreMode == modeKey
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            activeExploreMode = modeKey
                            when (modeKey) {
                                "NEWS_LOCATIONS" -> {
                                    if (selectedNewsLocation == null) {
                                        selectedNewsLocation = newsLocationsList.firstOrNull()
                                    }
                                    selectedWeatherAlertZone = null
                                    selectedBusRoute = null
                                    selectedTrain = null
                                    selectedHotspot = null
                                }
                                "WEATHER_ALERTS" -> {
                                    if (selectedWeatherAlertZone == null) {
                                        selectedWeatherAlertZone = weatherAlertZonesList.firstOrNull()
                                    }
                                    selectedNewsLocation = null
                                    selectedBusRoute = null
                                    selectedTrain = null
                                    selectedHotspot = null
                                }
                                "HOTSPOTS" -> {
                                    if (selectedHotspot == null) {
                                        selectedHotspot = BALASORE_TOURIST_LOCATIONS.firstOrNull()
                                    }
                                    selectedNewsLocation = null
                                    selectedWeatherAlertZone = null
                                    selectedBusRoute = null
                                    selectedTrain = null
                                }
                                "BUS_ROUTES" -> {
                                    if (selectedBusRoute == null) {
                                        selectedBusRoute = BalasoreTransitDataSource.BALASORE_BUS_ROUTES.first()
                                    }
                                    selectedNewsLocation = null
                                    selectedWeatherAlertZone = null
                                    selectedTrain = null
                                    selectedHotspot = null
                                }
                                "TRAIN_STATUS" -> {
                                    if (selectedTrain == null) {
                                        selectedTrain = BalasoreTransitDataSource.LIVE_TRAIN_STATUSES.first()
                                    }
                                    selectedNewsLocation = null
                                    selectedWeatherAlertZone = null
                                    selectedBusRoute = null
                                    selectedHotspot = null
                                }
                            }
                        },
                        modifier = Modifier.testTag("explore_mode_chip_${modeKey.lowercase()}"),
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = emoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (modeKey) {
                                "NEWS_LOCATIONS" -> Color(0xFF4F46E5)
                                "WEATHER_ALERTS" -> Color(0xFFDC2626)
                                "BUS_ROUTES" -> Color(0xFF0284C7)
                                "TRAIN_STATUS" -> Color(0xFFB91C1C)
                                "HOTSPOTS" -> Color(0xFFD97706)
                                else -> OceanBlue
                            },
                            selectedLabelColor = Color.White,
                            containerColor = BentoSlate100,
                            labelColor = BentoSlate800
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = isSelected,
                            borderColor = if (isSelected) OceanBlue else BentoSlate200
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 3. Quick Route / News / Weather Alert / Category Selector Pills
            if (activeExploreMode == "NEWS_LOCATIONS") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଖବର ସ୍ଥାନ ବାଛନ୍ତୁ:" else "Tap News Pin:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate600
                    )
                    newsLocationsList.forEach { newsLoc ->
                        val isSel = selectedNewsLocation?.id == newsLoc.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) Color(0xFF4F46E5) else BentoSlate100,
                            modifier = Modifier
                                .clickable {
                                    selectedNewsLocation = newsLoc
                                    selectedWeatherAlertZone = null
                                    selectedBusRoute = null
                                    selectedTrain = null
                                    selectedHotspot = null
                                    googleMapInstance?.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(newsLoc.latitude, newsLoc.longitude),
                                            13.0f
                                        )
                                    )
                                }
                                .padding(vertical = 2.dp)
                                .testTag("news_location_pill_${newsLoc.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${newsLoc.emoji} ${if (language == AppLanguage.ODIA) newsLoc.locationNameOr else newsLoc.locationNameEn}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else BentoSlate800
                                )
                            }
                        }
                    }
                }
            } else if (activeExploreMode == "WEATHER_ALERTS") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ସତର୍କତା ଅଞ୍ଚଳ:" else "Tap Alert Zone:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate600
                    )
                    weatherAlertZonesList.forEach { zone ->
                        val isSel = selectedWeatherAlertZone?.id == zone.id
                        val pillColor = when (zone.severity) {
                            "CRITICAL" -> Color(0xFFDC2626)
                            "SEVERE" -> Color(0xFFEA580C)
                            else -> Color(0xFFD97706)
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) pillColor else BentoSlate100,
                            modifier = Modifier
                                .clickable {
                                    selectedWeatherAlertZone = zone
                                    selectedNewsLocation = null
                                    selectedBusRoute = null
                                    selectedTrain = null
                                    selectedHotspot = null
                                    googleMapInstance?.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(zone.centerLat, zone.centerLng),
                                            12.0f
                                        )
                                    )
                                }
                                .padding(vertical = 2.dp)
                                .testTag("weather_alert_zone_pill_${zone.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${zone.emoji} ${if (language == AppLanguage.ODIA) zone.zoneNameOr else zone.zoneNameEn} (${zone.severity})",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else BentoSlate800
                                )
                            }
                        }
                    }
                }
            } else if (activeExploreMode == "BUS_ROUTES" || activeExploreMode == "ALL_OVERLAYS") {
                // Bus Route Selector row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ବସ୍ ରୁଟ୍ ବାଛନ୍ତୁ:" else "Tap Route:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate600
                    )

                    BalasoreTransitDataSource.BALASORE_BUS_ROUTES.forEach { route ->
                        val isSelected = selectedBusRoute?.id == route.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(route.colorArgb) else BentoSlate100,
                            modifier = Modifier
                                .clickable {
                                    selectedBusRoute = route
                                    selectedNewsLocation = null
                                    selectedWeatherAlertZone = null
                                    selectedTrain = null
                                    selectedHotspot = null

                                    // Zoom camera to fit route bounds
                                    val builder = LatLngBounds.Builder()
                                    route.pathPoints.forEach { builder.include(it) }
                                    googleMapInstance?.animateCamera(
                                        CameraUpdateFactory.newLatLngBounds(builder.build(), 70)
                                    )
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🚌 ${route.routeNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else BentoSlate800
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "(${route.fareStandard})",
                                    fontSize = 9.5.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else Color(0xFF0369A1),
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            } else if (activeExploreMode == "TRAIN_STATUS") {
                // Train Selector row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଟ୍ରେନ୍ ବାଛନ୍ତୁ:" else "Live Trains:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate600
                    )

                    BalasoreTransitDataSource.LIVE_TRAIN_STATUSES.forEach { train ->
                        val isSelected = selectedTrain?.id == train.id
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) Color(0xFFDC2626) else BentoSlate100,
                            modifier = Modifier
                                .clickable {
                                    selectedTrain = train
                                    selectedNewsLocation = null
                                    selectedWeatherAlertZone = null
                                    selectedBusRoute = null
                                    selectedHotspot = null

                                    googleMapInstance?.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(
                                            LatLng(train.currentLat, train.currentLng),
                                            13.0f
                                        )
                                    )
                                }
                                .padding(vertical = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "🚆 ${train.trainNumber}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else BentoSlate800
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = train.platform,
                                    fontSize = 9.5.sp,
                                    color = if (isSelected) Color.White.copy(alpha = 0.9f) else BentoSlate600
                                )
                            }
                        }
                    }
                }
            } else if (activeExploreMode == "HOTSPOTS") {
                // Hotspot Category Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val subCats = listOf(
                        Triple("ALL", "All Spots", "🗺️"),
                        Triple("TEMPLE", "Temples", "🛕"),
                        Triple("BEACH", "Beaches", "🏖️"),
                        Triple("LANDMARK", "Landmarks", "🏛️"),
                        Triple("NATURE", "Eco/Safari", "🌿")
                    )

                    subCats.forEach { (catKey, label, emoji) ->
                        val isSelected = selectedHotspotCategory == catKey
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedHotspotCategory = catKey },
                            label = { Text("$emoji $label", fontSize = 11.sp) },
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. MapView Settings: Quick Toggle Switch ('Live Online Map' vs 'Cached Offline Map')
            var mapStylePreset by remember { mutableStateOf("Standard") }
            var preferOfflineTiles by remember {
                mutableStateOf(
                    BalasoreOfflineTileCacheManager.isCachedOfflineMapMode(context, defaultOffline = true)
                )
            }
            var cachedRegionLabel by remember { mutableStateOf("Chandipur, Town & Remuna (14.2 MB Cached)") }
            var showCulturalCalendarLayer by remember { mutableStateOf(true) }
            var activeExperienceLayer by remember { mutableStateOf("All Layers") }

            val applyMapConnectivityMode: (Boolean) -> Unit = { isCachedOffline ->
                preferOfflineTiles = isCachedOffline
                BalasoreOfflineTileCacheManager.setCachedOfflineMapMode(context, isCachedOffline)
                useVectorEngine = isCachedOffline || !isMapsKeyConfigured
                val toastMessage = if (isCachedOffline) {
                    "Cached Offline Map Enabled • Saving mobile data & active without connectivity"
                } else {
                    "Live Online Map Enabled • Real-time map tile streaming active"
                }
                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show()
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("map_view_settings_mode_toggle_card"),
                shape = RoundedCornerShape(16.dp),
                color = if (preferOfflineTiles) Color(0xFFECFDF5) else Color(0xFFF0F9FF),
                border = BorderStroke(
                    width = 1.dp,
                    color = if (preferOfflineTiles) Color(0xFF34D399) else Color(0xFF38BDF8)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                shape = CircleShape,
                                color = if (preferOfflineTiles) EmeraldGreen.copy(alpha = 0.16f) else OceanBlue.copy(alpha = 0.16f),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (preferOfflineTiles) Icons.Default.WifiOff else Icons.Default.Wifi,
                                        contentDescription = if (preferOfflineTiles) "Cached Offline Map Active" else "Live Online Map Active",
                                        tint = if (preferOfflineTiles) Color(0xFF047857) else OceanBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "MapView Settings",
                                        tint = BentoSlate700,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA)
                                            "ମ୍ୟାପ୍ ସେଟିଂସ୍: ${if (preferOfflineTiles) "କ୍ୟାଚ୍ ଅଫଲାଇନ୍ ମ୍ୟାପ୍" else "ଲାଇଭ୍ ଅନଲାଇନ୍ ମ୍ୟାପ୍"}"
                                        else
                                            "MapView Settings: ${if (preferOfflineTiles) "Cached Offline Map" else "Live Online Map"}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BentoSlate900
                                    )
                                }
                                Text(
                                    text = if (preferOfflineTiles) {
                                        if (language == AppLanguage.ODIA)
                                            "ଡାଟା ସଞ୍ଚୟ ଓ ବିନା ଇଣ୍ଟରନେଟ୍ ରେ ନେଭିଗେସନ୍ ($cachedRegionLabel)"
                                        else
                                            "Saves mobile data & ensures access without connectivity ($cachedRegionLabel)"
                                    } else {
                                        if (language == AppLanguage.ODIA)
                                            "ଲାଇଭ୍ ଅନଲାଇନ୍ ଟାଇଲ୍ ଷ୍ଟ୍ରିମିଂ ସକ୍ରିୟ ଅଛି"
                                        else
                                            "Streaming live online map tiles • Switch to Cached Offline to save data"
                                    },
                                    fontSize = 10.5.sp,
                                    color = if (preferOfflineTiles) Color(0xFF065F46) else Color(0xFF0369A1),
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = preferOfflineTiles,
                            onCheckedChange = { isChecked ->
                                applyMapConnectivityMode(isChecked)
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
                                .testTag("map_view_settings_offline_mode_switch")
                        )
                    }

                    // Mode Selector Pills ('Live Online Map' vs 'Cached Offline Map')
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!preferOfflineTiles) OceanBlue else Color.White,
                            border = BorderStroke(1.dp, if (!preferOfflineTiles) OceanBlue else BentoSlate200),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { applyMapConnectivityMode(false) }
                                .testTag("select_live_online_map_mode_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Wifi,
                                    contentDescription = "Live Online Map Mode",
                                    tint = if (!preferOfflineTiles) Color.White else OceanBlue,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଲାଇଭ୍ ଅନଲାଇନ୍ ମ୍ୟାପ୍" else "Live Online Map",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (!preferOfflineTiles) Color.White else BentoSlate800
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (preferOfflineTiles) EmeraldGreen else Color.White,
                            border = BorderStroke(1.dp, if (preferOfflineTiles) EmeraldGreen else BentoSlate200),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { applyMapConnectivityMode(true) }
                                .testTag("select_cached_offline_map_mode_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.WifiOff,
                                    contentDescription = "Cached Offline Map Mode",
                                    tint = if (preferOfflineTiles) Color.White else Color(0xFF047857),
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "କ୍ୟାଚ୍ ଅଫଲାଇନ୍ ମ୍ୟାପ୍" else "Cached Offline Map",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (preferOfflineTiles) Color.White else BentoSlate800
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4A. Map Engine Switcher (Offline Vector District Map vs Google Maps SDK)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoSlate100,
                    border = BorderStroke(1.dp, BentoSlate200)
                ) {
                    Row(
                        modifier = Modifier.padding(3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (useVectorEngine) OceanBlue else Color.Transparent,
                            modifier = Modifier
                                .clickable { useVectorEngine = true }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "🗺️ ଭେକ୍ଟର (ଅଫଲାଇନ୍)" else "🗺️ Vector Map (Offline)",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (useVectorEngine) Color.White else BentoSlate800
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (!useVectorEngine) OceanBlue else Color.Transparent,
                            modifier = Modifier
                                .clickable { useVectorEngine = false }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🌐 Google Maps",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (!useVectorEngine) Color.White else BentoSlate800
                            )
                        }
                    }
                }

                // Offline Cached Tiles vs Live Online Tiles Toggle Pill
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (preferOfflineTiles) Color(0xFFECFDF5) else Color(0xFFE0F2FE),
                    border = BorderStroke(1.dp, if (preferOfflineTiles) Color(0xFF34D399) else Color(0xFF38BDF8)),
                    modifier = Modifier
                        .clickable {
                            applyMapConnectivityMode(!preferOfflineTiles)
                        }
                        .testTag("toggle_offline_vs_online_tiles_pill")
                ) {
                    Text(
                        text = if (preferOfflineTiles) "⚡ Cached Offline Map" else "🌐 Live Online Map",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (preferOfflineTiles) Color(0xFF065F46) else Color(0xFF0369A1),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 4B. Map Style Switcher + Download Map Area + Cultural Calendar & Live GPS Bus Overlays
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf("Standard", "Satellite-Hybrid", "High-Contrast Night").forEach { styleName ->
                    FilterChip(
                        selected = mapStylePreset == styleName,
                        onClick = {
                            mapStylePreset = styleName
                            currentMapType = when (styleName) {
                                "Satellite-Hybrid" -> GoogleMap.MAP_TYPE_HYBRID
                                else -> GoogleMap.MAP_TYPE_NORMAL
                            }
                            googleMapInstance?.mapType = currentMapType
                        },
                        label = { Text("🎨 $styleName", fontSize = 10.5.sp) },
                        modifier = Modifier.testTag("map_style_chip_${styleName.lowercase().replace("-", "_")}")
                    )
                }

                FilterChip(
                    selected = showCulturalCalendarLayer,
                    onClick = { showCulturalCalendarLayer = !showCulturalCalendarLayer },
                    label = {
                        Text(
                            text = if (language == AppLanguage.ODIA) "🏮 ପର୍ବ ମାର୍କର୍" else "🏮 Cultural Calendar Pins",
                            fontSize = 10.5.sp
                        )
                    },
                    modifier = Modifier.testTag("map_cultural_calendar_layer_chip")
                )

                FilterChip(
                    selected = showLiveGpsBusLayer,
                    onClick = { showLiveGpsBusLayer = !showLiveGpsBusLayer },
                    label = {
                        Text(
                            text = if (language == AppLanguage.ODIA) "🚌 ଲାଇଭ୍ GPS ବସ୍" else "🚌 Live GPS Bus Radar",
                            fontSize = 10.5.sp
                        )
                    },
                    modifier = Modifier.testTag("map_live_gps_bus_layer_chip")
                )

                listOf("Historical Sites", "Beach Hotspots", "Local Markets").forEach { expLayer ->
                    FilterChip(
                        selected = activeExperienceLayer == expLayer,
                        onClick = {
                            activeExperienceLayer = if (activeExperienceLayer == expLayer) "All Layers" else expLayer
                            selectedHotspotCategory = when (activeExperienceLayer) {
                                "Historical Sites" -> "LANDMARK"
                                "Beach Hotspots" -> "BEACH"
                                else -> "ALL"
                            }
                        },
                        label = { Text("✨ $expLayer", fontSize = 10.5.sp) },
                        modifier = Modifier.testTag("map_exp_layer_${expLayer.take(5).lowercase()}")
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = OceanBlue,
                    modifier = Modifier
                        .clickable {
                            cachedRegionLabel = "Full Baleshwar District + Chandipur + Nilagiri (22.8 MB Cached)"
                            context.getSharedPreferences("baleshwar_offline_mode_prefs", android.content.Context.MODE_PRIVATE)
                                .edit()
                                .putFloat("cached_osm_tiles_mb", 22.8f)
                                .apply()
                            android.widget.Toast.makeText(
                                context,
                                "osmdroid Region Downloaded: $cachedRegionLabel",
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                        .testTag("download_map_area_osm_btn")
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA) "⬇ ମ୍ୟାପ୍ ଏରିଆ ଡାଉନଲୋଡ୍" else "⬇ Download Map Area",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5. Interactive Map View Container (Supports both Vector Canvas & Google Maps SDK)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, BentoSlate200, RoundedCornerShape(20.dp))
                    .testTag("explore_section_map_view_container")
            ) {
                if (useVectorEngine) {
                    // Offline 2D Vector Canvas Map (Zero network/API key requirement, 100% resilient)
                    BalasoreDistrictVectorMap(
                        activeExploreMode = activeExploreMode,
                        selectedHotspotCategory = selectedHotspotCategory,
                        selectedHotspot = selectedHotspot,
                        onSelectHotspot = { spot ->
                            selectedHotspot = spot
                            selectedNewsLocation = null
                            selectedWeatherAlertZone = null
                            selectedBusRoute = null
                            selectedTrain = null
                        },
                        selectedBusRoute = selectedBusRoute,
                        onSelectBusRoute = { route ->
                            selectedBusRoute = route
                            selectedNewsLocation = null
                            selectedWeatherAlertZone = null
                            selectedTrain = null
                            selectedHotspot = null
                        },
                        selectedTrain = selectedTrain,
                        onSelectTrain = { train ->
                            selectedTrain = train
                            selectedNewsLocation = null
                            selectedWeatherAlertZone = null
                            selectedBusRoute = null
                            selectedHotspot = null
                        },
                        selectedStation = selectedStation,
                        onSelectStation = { stn ->
                            selectedStation = stn
                            selectedTrain = BalasoreTransitDataSource.LIVE_TRAIN_STATUSES.firstOrNull { it.platform.isNotEmpty() }
                            selectedNewsLocation = null
                            selectedWeatherAlertZone = null
                            selectedBusRoute = null
                            selectedHotspot = null
                        },
                        language = language,
                        modifier = Modifier.fillMaxSize(),
                        liveBuses = filteredLiveBuses,
                        showLiveGpsBusLayer = showLiveGpsBusLayer,
                        selectedLiveBus = selectedLiveBus,
                        onSelectLiveBus = { bus ->
                            selectedLiveBus = bus
                            Toast.makeText(
                                context,
                                "🚌 ${bus.routeNumber} (${bus.fleetNumber}) • Next: ${bus.nextStop} in ${bus.etaNextStopMins}m",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        newsLocations = newsLocationsList,
                        selectedNewsLocation = selectedNewsLocation,
                        onSelectNewsLocation = { newsLoc ->
                            selectedNewsLocation = newsLoc
                            selectedWeatherAlertZone = null
                            selectedBusRoute = null
                            selectedTrain = null
                            selectedHotspot = null
                        },
                        weatherAlertZones = weatherAlertZonesList,
                        selectedWeatherAlertZone = selectedWeatherAlertZone,
                        onSelectWeatherAlertZone = { zone ->
                            selectedWeatherAlertZone = zone
                            selectedNewsLocation = null
                            selectedBusRoute = null
                            selectedTrain = null
                            selectedHotspot = null
                        }
                    )
                } else if (mapView != null) {
                    // Embedded Google Maps SDK MapView (Only active when key is configured)
                    AndroidView(
                        factory = { _ ->
                            mapView.apply {
                                getMapAsync { map ->
                                    googleMapInstance = map
                                    map.uiSettings.isZoomControlsEnabled = false
                                    map.uiSettings.isCompassEnabled = true
                                    map.uiSettings.isMapToolbarEnabled = true
                                    map.mapType = currentMapType

                                    // Initial Center on Balasore
                                    val initialCenter = LatLng(21.4934, 86.9135)
                                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(initialCenter, 10.5f))

                                    refreshMapOverlays(map)

                                    // Circle click listener: Weather Alert Areas
                                    map.setOnCircleClickListener { circle ->
                                        val matchedZone = weatherAlertZonesList.find { z ->
                                            weatherAlertCirclesMap[z.id]?.id == circle.id
                                        }
                                        if (matchedZone != null) {
                                            selectedWeatherAlertZone = matchedZone
                                            selectedNewsLocation = null
                                            selectedBusRoute = null
                                            selectedTrain = null
                                            selectedHotspot = null
                                            map.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(matchedZone.centerLat, matchedZone.centerLng),
                                                    12f
                                                )
                                            )
                                            weatherAlertMarkersMap[matchedZone.id]?.showInfoWindow()
                                        }
                                    }

                                    // Polyline click listener: Allows users to tap on routes for schedule details!
                                    map.setOnPolylineClickListener { polyline ->
                                        val matchedRoute = BalasoreTransitDataSource.BALASORE_BUS_ROUTES.find { route ->
                                            busPolylinesMap[route.id]?.id == polyline.id
                                        }
                                        if (matchedRoute != null) {
                                            selectedBusRoute = matchedRoute
                                            selectedNewsLocation = null
                                            selectedWeatherAlertZone = null
                                            selectedTrain = null
                                            selectedHotspot = null

                                            // Zoom to route bounds
                                            val builder = LatLngBounds.Builder()
                                            matchedRoute.pathPoints.forEach { builder.include(it) }
                                            map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 70))
                                        } else if (railPolylineInstance?.id == polyline.id) {
                                            // Railway line clicked: Focus on Balasore Junction
                                            activeExploreMode = "TRAIN_STATUS"
                                            selectedTrain = BalasoreTransitDataSource.LIVE_TRAIN_STATUSES.first()
                                            selectedNewsLocation = null
                                            selectedWeatherAlertZone = null
                                            selectedBusRoute = null
                                        }
                                    }

                                    // Marker click listener
                                    map.setOnMarkerClickListener { marker ->
                                        // 0A. Check if it's a Geotagged News Location marker
                                        val matchedNewsLoc = newsLocationsList.find {
                                            newsLocationMarkersMap[it.id]?.id == marker.id
                                        }
                                        if (matchedNewsLoc != null) {
                                            selectedNewsLocation = matchedNewsLoc
                                            selectedWeatherAlertZone = null
                                            selectedBusRoute = null
                                            selectedTrain = null
                                            selectedHotspot = null
                                            map.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(matchedNewsLoc.latitude, matchedNewsLoc.longitude),
                                                    13.2f
                                                )
                                            )
                                            marker.showInfoWindow()
                                            return@setOnMarkerClickListener true
                                        }

                                        // 0B. Check if it's a Weather Alert Area center marker
                                        val matchedWxZone = weatherAlertZonesList.find {
                                            weatherAlertMarkersMap[it.id]?.id == marker.id
                                        }
                                        if (matchedWxZone != null) {
                                            selectedWeatherAlertZone = matchedWxZone
                                            selectedNewsLocation = null
                                            selectedBusRoute = null
                                            selectedTrain = null
                                            selectedHotspot = null
                                            map.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(matchedWxZone.centerLat, matchedWxZone.centerLng),
                                                    12.0f
                                                )
                                            )
                                            marker.showInfoWindow()
                                            return@setOnMarkerClickListener true
                                        }

                                        // 0C. Check if it's a Live GPS Bus Vehicle marker
                                        val matchedLiveBus = liveBuses.find {
                                            liveBusMarkersMap[it.busId]?.id == marker.id
                                        }
                                        if (matchedLiveBus != null) {
                                            selectedLiveBus = matchedLiveBus
                                            map.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(matchedLiveBus.latitude, matchedLiveBus.longitude),
                                                    13.5f
                                                )
                                            )
                                            marker.showInfoWindow()
                                            return@setOnMarkerClickListener true
                                        }

                                        // 1. Check if it's a Train Status marker
                                        val matchedTrain = BalasoreTransitDataSource.LIVE_TRAIN_STATUSES.find {
                                            trainMarkersMap[it.id]?.id == marker.id
                                        }
                                        if (matchedTrain != null) {
                                            selectedTrain = matchedTrain
                                            selectedNewsLocation = null
                                            selectedWeatherAlertZone = null
                                            selectedBusRoute = null
                                            selectedHotspot = null
                                            map.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(matchedTrain.currentLat, matchedTrain.currentLng),
                                                    13f
                                                )
                                            )
                                            marker.showInfoWindow()
                                            return@setOnMarkerClickListener true
                                        }

                                        // 2. Check if it's a Railway Station
                                        val matchedStation = BalasoreTransitDataSource.BALASORE_RAILWAY_STATIONS.find {
                                            stationMarkersMap[it.id]?.id == marker.id
                                        }
                                        if (matchedStation != null) {
                                            selectedStation = matchedStation
                                            selectedTrain = BalasoreTransitDataSource.LIVE_TRAIN_STATUSES.firstOrNull { it.platform.isNotEmpty() }
                                            selectedNewsLocation = null
                                            selectedWeatherAlertZone = null
                                            selectedBusRoute = null
                                            selectedHotspot = null
                                            marker.showInfoWindow()
                                            return@setOnMarkerClickListener true
                                        }

                                        // 3. Check if it's a Bus Stop
                                        val matchedStop = BalasoreTransitDataSource.BALASORE_BUS_ROUTES.flatMap { it.stops }.find {
                                            busStopMarkersMap[it.id]?.id == marker.id
                                        }
                                        if (matchedStop != null) {
                                            val parentRoute = BalasoreTransitDataSource.BALASORE_BUS_ROUTES.find { route ->
                                                route.stops.any { it.id == matchedStop.id }
                                            }
                                            if (parentRoute != null) {
                                                selectedBusRoute = parentRoute
                                                selectedNewsLocation = null
                                                selectedWeatherAlertZone = null
                                                selectedTrain = null
                                                selectedHotspot = null
                                            }
                                            marker.showInfoWindow()
                                            return@setOnMarkerClickListener true
                                        }

                                        // 4. Check if it's a Tourism Hotspot
                                        val matchedSpot = BALASORE_TOURIST_LOCATIONS.find {
                                            hotspotMarkersMap[it.id]?.id == marker.id
                                        }
                                        if (matchedSpot != null) {
                                            selectedHotspot = matchedSpot
                                            selectedNewsLocation = null
                                            selectedWeatherAlertZone = null
                                            selectedBusRoute = null
                                            selectedTrain = null
                                            map.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(matchedSpot.latitude, matchedSpot.longitude),
                                                    13f
                                                )
                                            )
                                            marker.showInfoWindow()
                                            return@setOnMarkerClickListener true
                                        }

                                        false
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Controls (Recenter, Layer Switcher, Zoom +/-) for Google Maps
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp),
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
                                    googleMapInstance?.animateCamera(
                                        CameraUpdateFactory.newLatLngZoom(LatLng(21.4934, 86.9135), 11f)
                                    )
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.MyLocation, contentDescription = "Recenter", tint = OceanBlue, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Map Type Toggle
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.95f),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable {
                                    currentMapType = if (currentMapType == GoogleMap.MAP_TYPE_NORMAL) GoogleMap.MAP_TYPE_HYBRID else GoogleMap.MAP_TYPE_NORMAL
                                    googleMapInstance?.mapType = currentMapType
                                }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Layers, contentDescription = "Layers", tint = BentoSlate800, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Zoom In
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.95f),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { googleMapInstance?.animateCamera(CameraUpdateFactory.zoomIn()) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = BentoSlate800, modifier = Modifier.size(18.dp))
                            }
                        }

                        // Zoom Out
                        Surface(
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.95f),
                            shadowElevation = 3.dp,
                            modifier = Modifier
                                .size(34.dp)
                                .clickable { googleMapInstance?.animateCamera(CameraUpdateFactory.zoomOut()) }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = BentoSlate800, modifier = Modifier.size(18.dp))
                            }
                        }
                    }

                    // Key Setup Guidance Banner on Google Maps if key not configured
                    if (!isMapsKeyConfigured) {
                        Surface(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .fillMaxWidth()
                                .padding(8.dp),
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFFEF3C7).copy(alpha = 0.96f),
                            border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                            shadowElevation = 3.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = Color(0xFFB45309),
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Google Maps API Key required for cloud tiles. Switch to Vector Map anytime.",
                                    fontSize = 10.sp,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(
                                    onClick = { useVectorEngine = true },
                                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Use Vector",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309)
                                    )
                                }
                            }
                        }
                    }
                }

                // Bottom Left Badge: Hint for tapping routes
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xDD0F172A),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when (activeExploreMode) {
                                "NEWS_LOCATIONS" -> "📰 Tap any news pin for story details"
                                "WEATHER_ALERTS" -> "⛈️ Tap any weather alert zone for IMD advisory"
                                "BUS_ROUTES" -> "🚌 Tap any bus line for schedule"
                                "TRAIN_STATUS" -> "🚆 Tap train markers for live status"
                                "HOTSPOTS" -> "🛕 Tap markers for distance & transit"
                                else -> "🗺️ Tap news, weather zones & hotspots"
                            },
                            fontSize = 10.5.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5A. Real-Time Balasore Bus Tracking Dashboard (Integrated with MapView Live Vehicle Locations)
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFFF0FDF4),
                border = BorderStroke(1.2.dp, EmeraldGreen.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("realtime_bus_tracking_dashboard_card")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Header: Title + Live GPS Ping Status + Refresh Button
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
                                shape = CircleShape,
                                color = EmeraldGreen,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DirectionsBus,
                                        contentDescription = "Real-time Bus Tracking Icon",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (language == AppLanguage.ODIA)
                                            "ଲାଇଭ୍ ବସ୍ GPS ଟ୍ରାକିଂ ଡ୍ୟାସବୋର୍ଡ"
                                        else
                                            "Real-Time Bus GPS Dashboard",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color(0xFF064E3B)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFDCFCE7),
                                        border = BorderStroke(1.dp, EmeraldGreen)
                                    ) {
                                        Text(
                                            text = "● ${filteredLiveBuses.size} LIVE • ${gpsCountdownSecs}s",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color(0xFF065F46),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = if (language == AppLanguage.ODIA)
                                        "CRUT Mo Bus ଓ OSRTC ଲାଇଭ୍ GPS ଲୋକେସନ • ସିଙ୍କ୍: $lastGpsSyncLabel"
                                    else
                                        "CRUT Mo Bus & OSRTC GPS feeds plotted on MapView • Sync: $lastGpsSyncLabel",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF047857)
                                )
                            }
                        }

                        // Manual GPS Feed Refresh Button
                        IconButton(
                            onClick = {
                                coroutineScope.launch {
                                    gpsCycle += 1
                                    val updated = transportRepository.fetchLiveCityBuses(gpsCycle).getOrDefault(
                                        transportRepository.createBaselineBalasoreBuses(gpsCycle)
                                    )
                                    liveBuses = updated
                                    selectedLiveBus = updated.find { it.busId == selectedLiveBus?.busId } ?: updated.firstOrNull()
                                    gpsCountdownSecs = 10
                                    lastGpsSyncLabel = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
                                    Toast.makeText(
                                        context,
                                        "Live GPS transit telemetry synced (${updated.size} vehicles active)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            modifier = Modifier
                                .size(34.dp)
                                .background(Color.White, CircleShape)
                                .testTag("refresh_live_bus_gps_feed_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Live Bus GPS Feed",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Transit Provider Filter Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val providerFilters = listOf(
                            "ALL" to (if (language == AppLanguage.ODIA) "ସବୁ ବସ୍ (${liveBuses.size})" else "All Providers (${liveBuses.size})"),
                            "CRUT_MO_BUS" to "🚌 CRUT Mo Bus",
                            "OSRTC" to "🛣️ OSRTC Regional",
                            "APPROACHING" to "⚡ Approaching (≤4m)"
                        )
                        providerFilters.forEach { (filterKey, label) ->
                            val isSelFilter = selectedTransitProviderFilter == filterKey
                            FilterChip(
                                selected = isSelFilter,
                                onClick = { selectedTransitProviderFilter = filterKey },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 10.5.sp,
                                        fontWeight = if (isSelFilter) FontWeight.Bold else FontWeight.Medium
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF065F46),
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.White,
                                    labelColor = BentoSlate800
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("bus_provider_filter_$filterKey")
                            )
                        }
                    }

                    // Horizontal Scrollable Live GPS Bus Cards (Tap to focus vehicle on MapView)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("live_bus_vehicles_lazy_row")
                    ) {
                        items(filteredLiveBuses, key = { it.busId }) { bus ->
                            val isSelectedVehicle = selectedLiveBus?.busId == bus.busId
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (isSelectedVehicle) Color(0xFF064E3B) else Color.White,
                                border = BorderStroke(
                                    1.2.dp,
                                    if (isSelectedVehicle) Color(0xFFFDE047) else Color(0xFFA7F3D0)
                                ),
                                modifier = Modifier
                                    .width(235.dp)
                                    .clickable {
                                        selectedLiveBus = bus
                                        showLiveGpsBusLayer = true
                                        activeExploreMode = "ALL_OVERLAYS"
                                        googleMapInstance?.animateCamera(
                                            CameraUpdateFactory.newLatLngZoom(
                                                LatLng(bus.latitude, bus.longitude),
                                                13.5f
                                            )
                                        )
                                    }
                                    .testTag("live_bus_vehicle_card_${bus.busId}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(11.dp),
                                    verticalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (isSelectedVehicle) Color(0xFF10B981) else OceanBlue.copy(alpha = 0.12f)
                                        ) {
                                            Text(
                                                text = "🚌 ${bus.routeNumber} • ${bus.fleetNumber}",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (isSelectedVehicle) Color.White else OceanBlue,
                                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (bus.etaNextStopMins <= 3) Color(0xFFDCFCE7) else Color(0xFFE0F2FE)
                                        ) {
                                            Text(
                                                text = "ETA ${bus.etaNextStopMins}m",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (bus.etaNextStopMins <= 3) Color(0xFF166534) else Color(0xFF0369A1),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Text(
                                        text = if (language == AppLanguage.ODIA) bus.routeNameOd else bus.routeNameEn,
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelectedVehicle) Color.White else BentoSlate900,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Text(
                                        text = "📍 At: ${bus.currentStop} ➔ ${bus.nextStop}",
                                        fontSize = 10.sp,
                                        color = if (isSelectedVehicle) Color(0xFFD1FAE5) else BentoSlate700,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = String.format(
                                                Locale.US,
                                                "🛰️ %.4f°N, %.4f°E",
                                                bus.latitude,
                                                bus.longitude
                                            ),
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = if (isSelectedVehicle) Color(0xFFFDE047) else Color(0xFF047857)
                                        )
                                        Text(
                                            text = "${String.format(Locale.US, "%.0f", bus.speedKmh)} km/h • ${bus.fareRange}",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelectedVehicle) Color.White else BentoSlate800
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Selected Live Vehicle GPS Telemetry Dossier Strip
                    selectedLiveBus?.let { activeBus ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("selected_live_bus_dossier_card")
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 9.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "🛰️ Tracking ${activeBus.fleetNumber} (${activeBus.busType})",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFF064E3B)
                                    )
                                    Text(
                                        text = "${activeBus.headingDirection} • Occupancy: ${activeBus.occupancyStatus.replace("_", " ")} • Next: ${activeBus.nextStop} (${activeBus.etaNextStopMins} min)",
                                        fontSize = 10.sp,
                                        color = BentoSlate700
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = OceanBlue,
                                    modifier = Modifier
                                        .clickable {
                                            // Match corresponding route overlay if available
                                            val matchedOverlay = BalasoreTransitDataSource.BALASORE_BUS_ROUTES.find { r ->
                                                activeBus.destinationEn.contains("Chandipur", ignoreCase = true) && r.id == "mo_bus_101" ||
                                                    activeBus.destinationEn.contains("Remuna", ignoreCase = true) && r.id == "mo_bus_102" ||
                                                    activeBus.destinationEn.contains("Panchalingeswar", ignoreCase = true) && r.id == "mo_bus_103"
                                            } ?: BalasoreTransitDataSource.BALASORE_BUS_ROUTES.firstOrNull()
                                            selectedBusRoute = matchedOverlay
                                            googleMapInstance?.animateCamera(
                                                CameraUpdateFactory.newLatLngZoom(
                                                    LatLng(activeBus.latitude, activeBus.longitude),
                                                    13.5f
                                                )
                                            )
                                        }
                                        .testTag("focus_selected_live_bus_on_map_btn")
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ମ୍ୟାପ୍ ଫୋକସ୍" else "Focus Map",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. Dynamic Interactive Dossier Card: Shows News Location OR Weather Alert Area OR Bus Schedule OR Train Status OR Hotspot Info
            if (selectedNewsLocation != null) {
                val newsLoc = selectedNewsLocation!!
                val distanceFromOriginKm = remember(newsLoc, selectedOrigin) {
                    calculateHaversineDistanceKm(
                        selectedOrigin.latitude,
                        selectedOrigin.longitude,
                        newsLoc.latitude,
                        newsLoc.longitude
                    )
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("explore_news_location_detail_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                    border = BorderStroke(1.5.dp, Color(0xFFC7D2FE)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (newsLoc.isBreaking) Color(0xFFBE123C) else Color(0xFF4F46E5),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = newsLoc.emoji, fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (newsLoc.isBreaking) Color(0xFFFFE4E6) else Color(0xFFE0E7FF)
                                        ) {
                                            Text(
                                                text = if (newsLoc.isBreaking) "🔴 BREAKING NEWS LOCATION" else "📰 ${newsLoc.category.uppercase()}",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (newsLoc.isBreaking) Color(0xFF9F1239) else Color(0xFF3730A3),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = newsLoc.publishedTimeAgo,
                                            fontSize = 10.sp,
                                            color = BentoSlate600,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) newsLoc.headlineOr else newsLoc.headlineEn,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BentoSlate900,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedNewsLocation = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close News Details", tint = BentoSlate600, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (language == AppLanguage.ODIA) newsLoc.summaryOr else newsLoc.summaryEn,
                            fontSize = 11.5.sp,
                            color = BentoSlate800,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BentoSlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "📍 ${if (language == AppLanguage.ODIA) newsLoc.locationNameOr else newsLoc.locationNameEn} (${newsLoc.wardOrBlock})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = BentoSlate900
                                    )
                                    Text(
                                        text = "Source: ${newsLoc.sourceReporter} • ${String.format(Locale.US, "%.1f km from %s", distanceFromOriginKm, selectedOrigin.nameEn)}",
                                        fontSize = 10.sp,
                                        color = BentoSlate600
                                    )
                                }
                                Text(
                                    text = "📞 ${newsLoc.relatedEmergencyPhone}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4F46E5)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val gmmUri = Uri.parse("geo:${newsLoc.latitude},${newsLoc.longitude}?q=${Uri.encode(newsLoc.locationNameEn)}")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, gmmUri))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("explore_news_location_navigate_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(text = "Navigate to Spot", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    val shareText = "📰 ${newsLoc.headlineEn}\n📍 ${newsLoc.locationNameEn} (${newsLoc.wardOrBlock})\n${newsLoc.summaryEn}\n🗺️ https://maps.google.com/?q=${newsLoc.latitude},${newsLoc.longitude}"
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Balasore News Location"))
                                },
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("explore_news_location_share_btn"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, Color(0xFF4F46E5).copy(alpha = 0.4f))
                            ) {
                                Text(text = "📤 Share", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF4F46E5))
                            }
                        }
                    }
                }
            } else if (selectedWeatherAlertZone != null) {
                val zone = selectedWeatherAlertZone!!
                val zoneAccent = when (zone.severity) {
                    "CRITICAL" -> Color(0xFFDC2626)
                    "SEVERE" -> Color(0xFFEA580C)
                    else -> Color(0xFFD97706)
                }
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("explore_weather_alert_detail_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.5.dp, zoneAccent.copy(alpha = 0.45f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = zoneAccent,
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = zone.emoji, fontSize = 18.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = zoneAccent.copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                text = "⚠️ ${zone.severity} • ${zone.hazardType}",
                                                fontSize = 9.5.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = zoneAccent,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) zone.alertTitleOr else zone.alertTitleEn,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BentoSlate900,
                                            fontSize = 13.5.sp
                                        )
                                    )
                                }
                            }

                            IconButton(
                                onClick = { selectedWeatherAlertZone = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close Weather Alert Details", tint = BentoSlate600, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Weather Alert Telemetry Strip
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BentoSlate200)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "💨 Peak Wind", fontSize = 9.5.sp, color = BentoSlate600)
                                    Text(text = "${zone.windSpeedKmh} km/h", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = zoneAccent)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BentoSlate200)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "🌧️ Expected Rain", fontSize = 9.5.sp, color = BentoSlate600)
                                    Text(text = "${zone.expectedRainfallMm} mm", fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = OceanBlue)
                                }
                            }
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BentoSlate200)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "📡 Alert Radius", fontSize = 9.5.sp, color = BentoSlate600)
                                    Text(text = String.format(Locale.US, "%.1f km", zone.radiusMeters / 1000.0), fontSize = 12.sp, fontWeight = FontWeight.ExtraBold, color = BentoSlate900)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (language == AppLanguage.ODIA) zone.imdAdvisoryOr else zone.imdAdvisoryEn,
                            fontSize = 11.5.sp,
                            color = BentoSlate800,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BentoSlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(
                                    text = "📍 Affected Areas: ${zone.affectedAreas.joinToString(", ")}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = "🛡️ Nearest Shelter: ${zone.nearestShelterName} (${zone.nearestShelterPhone})",
                                    fontSize = 10.5.sp,
                                    color = Color(0xFF047857),
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "🕒 ${zone.validUntil}",
                                    fontSize = 9.5.sp,
                                    color = BentoSlate600
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val gmmUri = Uri.parse("google.navigation:q=${zone.nearestShelterLat},${zone.nearestShelterLng}")
                                    val navIntent = Intent(Intent.ACTION_VIEW, gmmUri).apply { setPackage("com.google.android.apps.maps") }
                                    try {
                                        context.startActivity(navIntent)
                                    } catch (_: Exception) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${zone.nearestShelterLat},${zone.nearestShelterLng}?q=${Uri.encode(zone.nearestShelterName)}")))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("explore_weather_alert_shelter_nav_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = zoneAccent),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(text = "Navigate to Shelter", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    val shareText = "${zone.emoji} [${zone.severity}] ${zone.alertTitleEn}\nZone: ${zone.zoneNameEn} (Wind: ${zone.windSpeedKmh} km/h, Rain: ${zone.expectedRainfallMm} mm)\n${zone.imdAdvisoryEn}\nNearest Shelter: ${zone.nearestShelterName} (${zone.nearestShelterPhone})"
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Weather Alert Area"))
                                },
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("explore_weather_alert_share_btn"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, zoneAccent.copy(alpha = 0.4f))
                            ) {
                                Text(text = "📤 Share Alert", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = zoneAccent)
                            }
                        }
                    }
                }
            } else if (selectedBusRoute != null) {
                // === BUS ROUTE SCHEDULE & TIMETABLE CARD ===
                val route = selectedBusRoute!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("bus_route_schedule_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF0F9FF)),
                    border = BorderStroke(1.5.dp, Color(0xFFBAE6FD)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Title + Deselect Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(route.colorArgb),
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.DirectionsBus, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) route.routeNameOr else route.routeNameEn,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BentoSlate900,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFDCFCE7)
                                        ) {
                                            Text(
                                                text = "🟢 Active • On Time",
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF166534),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = route.operator,
                                            fontSize = 10.5.sp,
                                            color = BentoSlate600
                                        )
                                    }
                                }
                            }

                            // Dismiss route selection button
                            IconButton(
                                onClick = { selectedBusRoute = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = BentoSlate600, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Schedule & Distance Bento Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Distance & Time
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BentoSlate200)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "📏 Distance & Time", fontSize = 9.5.sp, color = BentoSlate600)
                                    Text(text = "${route.distanceKm} km • ~${route.durationMins}m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoSlate900)
                                }
                            }

                            // Fare
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BentoSlate200)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "💳 Fare (Std / AC)", fontSize = 9.5.sp, color = BentoSlate600)
                                    Text(text = "${route.fareStandard} / ${route.fareAc}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                            }

                            // Frequency
                            Surface(
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, BentoSlate200)
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text(text = "🕒 Frequency", fontSize = 9.5.sp, color = BentoSlate600)
                                    Text(text = "Every 20m", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Upcoming Scheduled Departures Strip
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BentoSlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = if (language == AppLanguage.ODIA) "ଦୈନିକ ବସ୍ ଚଳାଚଳ ସମୟସୂଚୀ:" else "Daily Bus Departures:",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoSlate800
                                        )
                                    }
                                    Text(
                                        text = "${route.operatingHours}",
                                        fontSize = 10.sp,
                                        color = BentoSlate600
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Horizontal departure chips
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    route.departuresSummary.take(8).forEachIndexed { idx, timeStr ->
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (idx == 0) Color(0xFF0284C7) else BentoSlate100
                                        ) {
                                            Text(
                                                text = if (idx == 0) "Next: $timeStr" else timeStr,
                                                fontSize = 10.5.sp,
                                                fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Medium,
                                                color = if (idx == 0) Color.White else BentoSlate800,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Major Stops Timeline Preview
                        Text(
                            text = if (language == AppLanguage.ODIA) "ରୁଟ୍ ଷ୍ଟପ୍ ସମୟ ତାଲିକା:" else "Route Stops & Timings (${route.stops.size} stops):",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate700
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            route.stops.forEachIndexed { index, stop ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White,
                                    border = BorderStroke(1.dp, BentoSlate200),
                                    modifier = Modifier.width(135.dp)
                                ) {
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "Stop ${index + 1}",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF0284C7)
                                            )
                                            Text(
                                                text = "+${stop.scheduledDepartureOffsetMins}m",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = EmeraldGreen
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (language == AppLanguage.ODIA) stop.nameOr else stop.nameEn,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BentoSlate900,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = stop.landmarkInfo,
                                            fontSize = 9.sp,
                                            color = BentoSlate600,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Action Buttons: View Complete Timetable + Directions
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showFullBusScheduleDialog = route },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("view_full_bus_schedule_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(route.colorArgb)),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ସମ୍ପୂର୍ଣ୍ଣ ସମୟସାରଣୀ" else "Full Timetable",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val text = buildString {
                                        append("${route.routeNameEn}\n")
                                        append("Distance: ${route.distanceKm} km • Fare: ${route.fareStandard} (AC: ${route.fareAc})\n")
                                        append("Frequency: ${route.frequency}\n")
                                        append("Stops:\n")
                                        route.stops.forEach { s ->
                                            append(" - ${s.nameEn} (+${s.scheduledDepartureOffsetMins} mins)\n")
                                        }
                                    }
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Bus Schedule", text))
                                    Toast.makeText(context, "Bus schedule copied to clipboard!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("copy_bus_schedule_btn"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BentoSlate200),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = BentoSlate700, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Copy", fontSize = 11.5.sp, color = BentoSlate800)
                            }
                        }
                    }
                }
            } else if (selectedTrain != null) {
                // === LIVE TRAIN STATUS & DEPARTURE CARD ===
                val train = selectedTrain!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("train_status_schedule_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.5.dp, Color(0xFFFECACA)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        // Title + Deselect
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFFDC2626),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(imageVector = Icons.Default.Train, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${train.trainNumber} ${if (language == AppLanguage.ODIA) train.trainNameOr else train.trainNameEn}",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = BentoSlate900,
                                            fontSize = 14.sp
                                        )
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (train.statusType == "DELAYED") Color(0xFFFEF3C7) else Color(0xFFDCFCE7)
                                        ) {
                                            Text(
                                                text = train.statusText,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (train.statusType == "DELAYED") Color(0xFFB45309) else Color(0xFF166534),
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = train.platform,
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFDC2626)
                                        )
                                    }
                                }
                            }

                            IconButton(
                                onClick = { selectedTrain = null },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = BentoSlate600, modifier = Modifier.size(18.dp))
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Train Specs Grid
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BentoSlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(10.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Origin ➔ Destination", fontSize = 9.5.sp, color = BentoSlate600)
                                        Text(text = "${train.origin} ➔ ${train.destination}", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = BentoSlate900)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Balasore Timing", fontSize = 9.5.sp, color = BentoSlate600)
                                        Text(text = train.scheduledTimeAtBalasore, fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFFDC2626))
                                    }
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column {
                                        Text(text = "Next Stop", fontSize = 9.5.sp, color = BentoSlate600)
                                        Text(text = train.nextStopName, fontSize = 11.sp, color = BentoSlate800)
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(text = "Live Speed", fontSize = 9.5.sp, color = BentoSlate600)
                                        Text(text = "${train.speedKmh} km/h", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0369A1))
                                    }
                                }

                                // Coach layout info
                                Text(
                                    text = "Coach Layout: ${train.coachPositionInfo}",
                                    fontSize = 10.sp,
                                    color = BentoSlate600,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Action Buttons: Copy train status
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("geo:21.4934,86.9135?q=Balasore+Railway+Station")
                                    context.startActivity(Intent(Intent.ACTION_VIEW, gmmIntentUri))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("navigate_balasore_station_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Place, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = "Station Platform Map", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val text = "${train.trainNumber} ${train.trainNameEn}\nStatus: ${train.statusText}\nPlatform: ${train.platform}\nBalasore Time: ${train.scheduledTimeAtBalasore}\nNext Stop: ${train.nextStopName}"
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Train Status", text))
                                    Toast.makeText(context, "Train status copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.height(38.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BentoSlate200)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, tint = BentoSlate700, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "Copy", fontSize = 11.sp, color = BentoSlate800)
                            }
                        }
                    }
                }
            } else if (selectedHotspot != null) {
                // === TOURISM HOTSPOT DISTANCE & TRANSIT CARD ===
                val spot = selectedHotspot!!
                val liveDistanceKm = remember(spot, selectedOrigin) {
                    calculateHaversineDistanceKm(
                        selectedOrigin.latitude,
                        selectedOrigin.longitude,
                        spot.latitude,
                        spot.longitude
                    )
                }
                val estimatedMinutes = remember(liveDistanceKm, spot.transitTravelTimeMins) {
                    val roadSpeedKmH = if (liveDistanceKm > 40) 45.0 else 30.0
                    ((liveDistanceKm / roadSpeedKmH) * 60).toInt().coerceAtLeast(8)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .testTag("explore_marker_transit_info_card"),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color.White,
                                    border = BorderStroke(1.dp, BentoSlate200),
                                    modifier = Modifier.size(40.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(text = spot.highlightEmoji, fontSize = 20.sp)
                                    }
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) spot.nameOr else spot.nameEn,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = BentoSlate900,
                                            fontSize = 14.5.sp
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (language == AppLanguage.ODIA) spot.categoryOr else spot.categoryEn,
                                            fontSize = 10.5.sp,
                                            color = OceanBlue,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(text = "⭐ ${spot.rating}", fontSize = 10.5.sp, color = BentoSlate700)
                                    }
                                }
                            }

                            // Distance Badge
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = OceanBlue
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f km", liveDistanceKm),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "~$estimatedMinutes mins",
                                        fontSize = 9.5.sp,
                                        color = Color.White.copy(alpha = 0.9f)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) spot.shortDescOr else spot.shortDescEn,
                            fontSize = 11.5.sp,
                            color = BentoSlate700,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Transit Dossier
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            border = BorderStroke(1.dp, BentoSlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.DirectionsBus, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(15.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(text = spot.transitRoutes.joinToString(" • "), fontSize = 11.sp, color = BentoSlate800)
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(text = "🚏 ${spot.nearestTransitStop}", fontSize = 10.sp, color = BentoSlate600)
                                    Text(text = spot.estimatedTransitFare, fontSize = 10.5.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    val gmmIntentUri = Uri.parse("google.navigation:q=${spot.latitude},${spot.longitude}")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply { setPackage("com.google.android.apps.maps") }
                                    try {
                                        context.startActivity(mapIntent)
                                    } catch (_: Exception) {
                                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:${spot.latitude},${spot.longitude}?q=${Uri.encode(spot.nameEn)}")))
                                    }
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("explore_marker_navigate_btn"),
                                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(15.dp), tint = Color.White)
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(text = "Navigate", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            OutlinedButton(
                                onClick = {
                                    val shareText = buildString {
                                        append("📍 ${spot.nameEn} (${spot.categoryEn}) — ⭐ ${spot.rating}\n")
                                        append("${spot.shortDescEn}\n")
                                        append("🚏 Nearest Stop: ${spot.nearestTransitStop} (${spot.estimatedTransitFare})\n")
                                        append("🗺️ Google Maps: https://maps.google.com/?q=${spot.latitude},${spot.longitude}")
                                    }
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_SUBJECT, "Explore ${spot.nameEn} in Baleshwar 360")
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share ${spot.nameEn}"))
                                },
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("explore_marker_share_btn"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.4f))
                            ) {
                                Text(text = "📤 Share", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = OceanBlue)
                            }

                            OutlinedButton(
                                onClick = {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val text = "${spot.nameEn}\nDistance: ${String.format(Locale.US, "%.1f km", liveDistanceKm)}\nTransit: ${spot.transitRoutes.joinToString(", ")}\nNearest Stop: ${spot.nearestTransitStop}\nFare: ${spot.estimatedTransitFare}"
                                    clipboard.setPrimaryClip(ClipData.newPlainText("Transit Info", text))
                                    Toast.makeText(context, "Transit guide copied!", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("explore_marker_copy_btn"),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, BentoSlate200)
                            ) {
                                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp), tint = BentoSlate700)
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Dialog: Full Bus Schedule Timetable with All Daily Departures & Stop Matrix
    if (showFullBusScheduleDialog != null) {
        val route = showFullBusScheduleDialog!!
        AlertDialog(
            onDismissRequest = { showFullBusScheduleDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🚌", fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "${route.routeNumber}: ${route.originName} ⇄ ${route.destinationName}",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 14.5.sp)
                        )
                        Text(
                            text = "${route.operator} • ${route.distanceKm} km • ${route.durationMins} mins",
                            fontSize = 11.sp,
                            color = BentoSlate600
                        )
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Fare & Frequency
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Fare: ${route.fareStandard} (AC: ${route.fareAc})", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = EmeraldGreen)
                            Text(text = route.frequency, fontSize = 11.sp, color = Color(0xFF0369A1))
                        }
                    }

                    // Daily Departures Matrix
                    Text(text = "🕒 Complete Daily Departures from ${route.originName}:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoSlate900)
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(route.departuresSummary) { depTime ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFE0F2FE)
                            ) {
                                Text(
                                    text = depTime,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF0369A1),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // All Stops Chain
                    Text(text = "🚏 All Route Stops & Timing Offsets:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = BentoSlate900)
                    route.stops.forEachIndexed { idx, stop ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (idx == 0 || idx == route.stops.lastIndex) Color(route.colorArgb) else BentoSlate200,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        text = "${idx + 1}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (idx == 0 || idx == route.stops.lastIndex) Color.White else BentoSlate800
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = stop.nameEn,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = stop.landmarkInfo,
                                    fontSize = 10.sp,
                                    color = BentoSlate600
                                )
                            }
                            Text(
                                text = "+${stop.scheduledDepartureOffsetMins}m",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldGreen
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFullBusScheduleDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                ) {
                    Text("Close")
                }
            }
        )
    }
}
