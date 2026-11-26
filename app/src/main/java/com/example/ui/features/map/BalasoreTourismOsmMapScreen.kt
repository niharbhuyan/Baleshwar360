package com.example.ui.features.map

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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.FitScreen
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.HotspotEntity
import com.example.data.local.ReviewEntity
import com.example.data.maps.BalasoreOfflineTileCacheManager
import com.example.data.maps.BalasoreOsmDarkStyleHelper
import com.example.data.maps.BalasoreOsmOfflineTileProvider
import com.example.data.model.AppLanguage
import com.example.data.repository.TourismRepository
import com.example.ui.screens.HotspotDetailScreen
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.util.BoundingBox
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.ScaleBarOverlay
import org.osmdroid.views.overlay.compass.CompassOverlay
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider
import org.osmdroid.views.overlay.infowindow.InfoWindow
import java.util.Locale

private val BALASORE_CENTER = GeoPoint(21.4934, 86.9335)

/**
 * Interactive OSMDroid Tourism Map Screen backed directly by the Tourism Module Database
 * (Room [AppDatabase.hotspotDao] via [TourismRepository], enriched by Cloud Firestore
 * `/tourism_hotspots` when available).
 *
 * Marks hotspots like Chandipur Beach, Khirachora Gopinatha Temple (Remuna), Emami Jagannath,
 * Panchalingeswar, Kuldiha Sanctuary, and Talasari with custom category teardrop icons and
 * interactive database-driven info-callouts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreTourismOsmMapScreen(
    language: AppLanguage = AppLanguage.ENGLISH,
    initialSpotId: String? = null,
    onOpenCivicMap: (() -> Unit)? = null,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA

    // Configure OSMDroid tile cache and offline-resilient provider
    remember {
        BalasoreOfflineTileCacheManager.configureOsmDroidForUnstableNetwork(context)
    }
    val tileProvider = remember { BalasoreOsmOfflineTileProvider(context.applicationContext ?: context) }
    val isOfflineOnly by tileProvider.isOfflineOnlyMode.collectAsState()

    // Tourism Module Database repository (Room HotspotDao + CacheMetadataDao + ReviewDao + WeatherDao)
    val db = remember(context) { AppDatabase.getInstance(context.applicationContext) }
    val tourismRepository = remember(db) {
        TourismRepository(db.hotspotDao(), db.cacheMetadataDao())
    }
    val reviewDao = remember(db) { db.reviewDao() }
    val cachedWeather by remember(db) { db.weatherDao().getWeatherCache() }
        .collectAsStateWithLifecycle(initialValue = null)
    val dbHotspots by tourismRepository.allHotspots.collectAsStateWithLifecycle(initialValue = emptyList())

    // Ensure Room tourism_hotspots table is seeded and optionally sync public Firestore updates
    LaunchedEffect(tourismRepository) {
        withContext(Dispatchers.IO) {
            runCatching {
                tourismRepository.getUnifiedHotspotsFlow(forceRefresh = false).collect { _ -> }
            }
            syncCloudTourismHotspotsIntoRoom(context.applicationContext, tourismRepository)
        }
    }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var isDarkMapStyle by remember { mutableStateOf(false) }
    var selectedHotspotId by remember { mutableStateOf(initialSpotId) }
    var showBottomCard by remember { mutableStateOf(true) }
    var openCalloutSpotId by remember { mutableStateOf<String?>(initialSpotId) }
    var detailModalSpot by remember { mutableStateOf<HotspotEntity?>(null) }
    var reviewModalSpot by remember { mutableStateOf<HotspotEntity?>(null) }
    var mapViewInstance by remember { mutableStateOf<MapView?>(null) }

    val filteredHotspots = remember(dbHotspots, selectedCategory, searchQuery) {
        val query = searchQuery.trim().lowercase(Locale.US)
        dbHotspots.filter { spot ->
            val matchesCategory = when (selectedCategory) {
                "ALL" -> true
                "SAVED" -> spot.isFavorite
                "BEACH" -> TourismPinCategory.resolve(spot.category, spot.name) == TourismPinCategory.BEACH
                "TEMPLE" -> TourismPinCategory.resolve(spot.category, spot.name) == TourismPinCategory.TEMPLE
                "WILDLIFE" -> TourismPinCategory.resolve(spot.category, spot.name) == TourismPinCategory.WILDLIFE
                "HERITAGE" -> {
                    val cat = TourismPinCategory.resolve(spot.category, spot.name)
                    cat == TourismPinCategory.HERITAGE || cat == TourismPinCategory.PORT
                }
                else -> true
            }
            val matchesQuery = query.isEmpty() ||
                spot.name.lowercase(Locale.US).contains(query) ||
                spot.odiaName.lowercase(Locale.US).contains(query) ||
                spot.category.lowercase(Locale.US).contains(query) ||
                spot.shortDescription.lowercase(Locale.US).contains(query) ||
                spot.highlights.lowercase(Locale.US).contains(query)
            matchesCategory && matchesQuery
        }
    }

    val selectedHotspot = remember(dbHotspots, filteredHotspots, selectedHotspotId) {
        dbHotspots.find { it.id == selectedHotspotId }
            ?: filteredHotspots.firstOrNull { it.id.contains("chandipur", ignoreCase = true) }
            ?: filteredHotspots.firstOrNull()
    }

    val focusHotspotOnMap: (HotspotEntity, Boolean) -> Unit = { spot, zoomIn ->
        selectedHotspotId = spot.id
        showBottomCard = true
        openCalloutSpotId = spot.id
        mapViewInstance?.let { mv ->
            if (zoomIn) {
                mv.controller.setZoom(14.2)
            }
            mv.controller.animateTo(GeoPoint(spot.latitude, spot.longitude))
            val marker = mv.overlays.filterIsInstance<Marker>().find { it.id == spot.id }
            marker?.showInfoWindow()
            mv.invalidate()
        }
    }

    val fitAllHotspots: () -> Unit = {
        val list = filteredHotspots.ifEmpty { dbHotspots }
        mapViewInstance?.let { mv ->
            if (list.size >= 2) {
                val north = list.maxOf { it.latitude } + 0.05
                val south = list.minOf { it.latitude } - 0.05
                val east = list.maxOf { it.longitude } + 0.05
                val west = list.minOf { it.longitude } - 0.05
                mv.zoomToBoundingBox(BoundingBox(north, east, south, west), true, 96)
            } else {
                mv.controller.setZoom(11.6)
                mv.controller.animateTo(BALASORE_CENTER)
            }
        }
    }

    // Lifecycle management for OSMDroid MapView
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapViewInstance?.onResume()
                Lifecycle.Event.ON_PAUSE -> mapViewInstance?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapViewInstance?.onDetach()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            runCatching {
                mapViewInstance?.let { InfoWindow.closeAllInfoWindowsOn(it) }
                mapViewInstance?.onPause()
                mapViewInstance?.onDetach()
                tileProvider.detach()
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.osm_tourism_map_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${filteredHotspots.size}/${dbHotspots.size} • ${stringResource(R.string.osm_tourism_map_subtitle_db)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("tourism_osm_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    if (onOpenCivicMap != null) {
                        IconButton(
                            onClick = onOpenCivicMap,
                            modifier = Modifier
                                .minimumInteractiveComponentSize()
                                .testTag("tourism_osm_civic_switch_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ReportProblem,
                                contentDescription = "Open Civic Issues Map",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                    IconButton(
                        onClick = fitAllHotspots,
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("tourism_osm_fit_all_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitScreen,
                            contentDescription = stringResource(R.string.osm_tourism_fit_all)
                        )
                    }
                    IconButton(
                        onClick = {
                            isDarkMapStyle = !isDarkMapStyle
                            mapViewInstance?.let { mv ->
                                BalasoreOsmDarkStyleHelper.applyMapColorStyle(mv, isDarkMapStyle)
                                mv.invalidate()
                            }
                        },
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .testTag("tourism_osm_theme_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (isDarkMapStyle) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMapStyle) "Light Map Style" else "Dark Map Style"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive OSMDroid MapView
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("tourism_osmdroid_map_view"),
                factory = { ctx ->
                    MapView(ctx, tileProvider).apply {
                        mapViewInstance = this
                        setTileSource(tileProvider.tileSource)
                        setUseDataConnection(!isOfflineOnly)
                        setMultiTouchControls(true)
                        zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                        BalasoreOsmDarkStyleHelper.applyMapColorStyle(this, isDarkMapStyle)

                        controller.setZoom(11.3)
                        controller.setCenter(BALASORE_CENTER)

                        val compassOverlay = CompassOverlay(ctx, InternalCompassOrientationProvider(ctx), this)
                        compassOverlay.enableCompass()
                        overlays.add(compassOverlay)

                        val scaleBarOverlay = ScaleBarOverlay(this)
                        scaleBarOverlay.setCentred(true)
                        overlays.add(scaleBarOverlay)
                    }
                },
                update = { mapView ->
                    mapView.setUseDataConnection(!isOfflineOnly)
                    BalasoreOsmDarkStyleHelper.applyMapColorStyle(mapView, isDarkMapStyle)

                    // Preserve non-marker overlays (Compass, ScaleBar)
                    val nonMarkers = mapView.overlays.filter { it !is Marker }
                    InfoWindow.closeAllInfoWindowsOn(mapView)
                    mapView.overlays.clear()
                    mapView.overlays.addAll(nonMarkers)

                    var markerToOpen: Marker? = null
                    filteredHotspots.forEach { spot ->
                        val isSelected = spot.id == selectedHotspot?.id
                        val marker = buildTourismHotspotMarker(
                            mapView = mapView,
                            context = context,
                            hotspot = spot,
                            language = language,
                            isSelected = isSelected,
                            onMarkerTapped = { tapped ->
                                selectedHotspotId = tapped.id
                                openCalloutSpotId = tapped.id
                                showBottomCard = true
                            },
                            onViewDetails = { tapped ->
                                selectedHotspotId = tapped.id
                                showBottomCard = true
                                detailModalSpot = tapped
                            },
                            onNavigate = { tapped ->
                                TourismMapDirectionsHelper.launchDirections(
                                    context = context,
                                    spotName = tapped.name,
                                    destLat = tapped.latitude,
                                    destLng = tapped.longitude
                                )
                            }
                        )
                        mapView.overlays.add(marker)
                        if (spot.id == openCalloutSpotId) {
                            markerToOpen = marker
                        }
                    }

                    markerToOpen?.showInfoWindow()
                    mapView.invalidate()
                }
            )

            // Floating Top Search + Category Filters + Quick-Jump Hotspots Overlay
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    tonalElevation = 4.dp,
                    shadowElevation = 4.dp,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = stringResource(R.string.osm_tourism_search_placeholder),
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search hotspots",
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(
                                        onClick = { searchQuery = "" },
                                        modifier = Modifier.testTag("tourism_osm_clear_search_btn")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Clear search",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                focusedContainerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tourism_osm_search_input")
                        )

                        // Database Category Filter Chips
                        val categories = listOf(
                            "ALL" to (if (isOdia) "ସମସ୍ତ (${dbHotspots.size})" else "All (${dbHotspots.size})"),
                            "BEACH" to (if (isOdia) "ବେଳାଭୂମି" else "Beaches"),
                            "TEMPLE" to (if (isOdia) "ମନ୍ଦିର" else "Temples"),
                            "WILDLIFE" to (if (isOdia) "ବନ୍ୟପ୍ରାଣୀ" else "Wildlife"),
                            "HERITAGE" to (if (isOdia) "ଐତିହ୍ୟ" else "Heritage"),
                            "SAVED" to (if (isOdia) "ସାଇତା" else "Saved")
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(horizontal = 2.dp)
                        ) {
                            items(categories) { (key, label) ->
                                val active = selectedCategory == key
                                FilterChip(
                                    selected = active,
                                    onClick = { selectedCategory = key },
                                    label = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                    ),
                                    modifier = Modifier.testTag("tourism_osm_filter_${key.lowercase(Locale.US)}")
                                )
                            }
                        }

                        // Quick-Jump Hotspot Spotlight Pills (Chandipur Beach, Remuna Temple, etc.)
                        if (filteredHotspots.isNotEmpty()) {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                contentPadding = PaddingValues(horizontal = 2.dp)
                            ) {
                                items(filteredHotspots, key = { it.id }) { spot ->
                                    val pinStyle = TourismPinCategory.resolve(spot.category, spot.name)
                                    val isCurrent = spot.id == selectedHotspot?.id
                                    Surface(
                                        shape = RoundedCornerShape(999.dp),
                                        color = if (isCurrent) {
                                            Color(pinStyle.primaryColor)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                                        },
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .clickable { focusHotspotOnMap(spot, true) }
                                            .testTag("tourism_osm_quick_jump_${spot.id}")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(
                                                        if (isCurrent) Color.White else Color(pinStyle.primaryColor)
                                                    )
                                            )
                                            Text(
                                                text = if (isOdia && spot.odiaName.isNotBlank()) {
                                                    spot.odiaName.substringBefore(",")
                                                } else {
                                                    spot.name.substringBefore("(").trim()
                                                },
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurface,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        } else {
                            Text(
                                text = stringResource(R.string.osm_tourism_empty_filter),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Right-Edge Floating Map Controls (Zoom In, Zoom Out, Recenter, Offline/Online)
            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MapControlCircleButton(
                    icon = Icons.Default.ZoomIn,
                    contentDescription = "Zoom In",
                    testTag = "tourism_osm_zoom_in_btn",
                    onClick = { mapViewInstance?.controller?.zoomIn() }
                )
                MapControlCircleButton(
                    icon = Icons.Default.ZoomOut,
                    contentDescription = "Zoom Out",
                    testTag = "tourism_osm_zoom_out_btn",
                    onClick = { mapViewInstance?.controller?.zoomOut() }
                )
                MapControlCircleButton(
                    icon = Icons.Default.MyLocation,
                    contentDescription = stringResource(R.string.osm_tourism_recenter),
                    testTag = "tourism_osm_recenter_btn",
                    onClick = {
                        mapViewInstance?.controller?.apply {
                            setZoom(11.8)
                            animateTo(BALASORE_CENTER)
                        }
                    }
                )
                MapControlCircleButton(
                    icon = if (isOfflineOnly) Icons.Default.WifiOff else Icons.Default.Wifi,
                    contentDescription = if (isOfflineOnly) "Offline Map Tiles Active" else "Live Online Map Tiles Active",
                    containerColor = if (isOfflineOnly) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    tint = if (isOfflineOnly) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    testTag = "tourism_osm_offline_toggle_btn",
                    onClick = {
                        val nextOffline = !isOfflineOnly
                        tileProvider.setOfflineOnly(nextOffline)
                        mapViewInstance?.setUseDataConnection(!nextOffline)
                        mapViewInstance?.invalidate()
                        Toast.makeText(
                            context,
                            if (nextOffline) "Cached Offline OSM Tiles Active" else "Live OSM Tile Streaming Enabled",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }

            // Bottom Database-Backed Selected Hotspot Pin Card (Images, Short Description & View More Details)
            AnimatedVisibility(
                visible = showBottomCard && selectedHotspot != null,
                enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                selectedHotspot?.let { spot ->
                    TourismHotspotPinCard(
                        spot = spot,
                        isOdia = isOdia,
                        onToggleFavorite = {
                            coroutineScope.launch {
                                tourismRepository.toggleFavorite(spot.id, spot.isFavorite)
                            }
                        },
                        onViewMoreDetails = { detailModalSpot = spot },
                        onFocusCallout = { focusHotspotOnMap(spot, true) },
                        onOpenReviews = { reviewModalSpot = spot },
                        onDirections = {
                            TourismMapDirectionsHelper.launchDirections(
                                context = context,
                                spotName = spot.name,
                                destLat = spot.latitude,
                                destLng = spot.longitude
                            )
                        },
                        onDismiss = { showBottomCard = false }
                    )
                }
            }
        }
    }

    // Full-screen Hotspot Detail View launched from the Pin Card "View More Details" button or pin callout
    detailModalSpot?.let { activeSpot ->
        val liveSpot = dbHotspots.find { it.id == activeSpot.id } ?: activeSpot
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .testTag("tourism_map_hotspot_detail_overlay"),
            color = MaterialTheme.colorScheme.background
        ) {
            HotspotDetailScreen(
                hotspot = liveSpot,
                weather = cachedWeather,
                reviewsFlow = remember(liveSpot.id) {
                    reviewDao.getReviewsForTarget("HOTSPOT", liveSpot.id)
                },
                onSubmitReview = { rating, comment, guestName ->
                    coroutineScope.launch(Dispatchers.IO) {
                        reviewDao.insertReview(
                            ReviewEntity(
                                targetType = "HOTSPOT",
                                targetId = liveSpot.id,
                                targetTitle = liveSpot.name,
                                userId = "local_explorer",
                                userName = guestName?.takeIf { it.isNotBlank() } ?: "Balasore Explorer",
                                userLocality = "Balasore, Odisha",
                                rating = rating,
                                comment = comment
                            )
                        )
                    }
                },
                onNavigateBack = { detailModalSpot = null },
                onToggleFavorite = {
                    coroutineScope.launch {
                        tourismRepository.toggleFavorite(liveSpot.id, liveSpot.isFavorite)
                    }
                },
                onExploreWithMaps = {
                    TourismMapDirectionsHelper.launchDirections(
                        context = context,
                        spotName = liveSpot.name,
                        destLat = liveSpot.latitude,
                        destLng = liveSpot.longitude
                    )
                },
                selectedLanguage = language
            )
        }
    }

    reviewModalSpot?.let { spot ->
        TourismSpotReviewsAndDirectionsSheet(
            spotId = spot.id,
            spotName = if (isOdia && spot.odiaName.isNotBlank()) spot.odiaName else spot.name,
            spotCategory = spot.category,
            spotLocation = spot.highlights.ifBlank { "Balasore District, Odisha" },
            spotLatitude = spot.latitude,
            spotLongitude = spot.longitude,
            baselineRating = 4.8,
            language = language,
            onDismiss = { reviewModalSpot = null }
        )
    }
}

@Composable
private fun MapControlCircleButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    testTag: String,
    onClick: () -> Unit,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    tint: Color = MaterialTheme.colorScheme.onSurface
) {
    Surface(
        shape = CircleShape,
        color = containerColor,
        shadowElevation = 4.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

/**
 * Performs a non-blocking, public read sync from Cloud Firestore `/tourism_hotspots`
 * (aligned with the tourism schema) and merges updates into Room `tourism_hotspots` while
 * preserving local favorite flags. Silently falls back to Room cache when offline.
 */
private suspend fun syncCloudTourismHotspotsIntoRoom(
    appContext: android.content.Context,
    repository: TourismRepository
) {
    runCatching {
        if (FirebaseApp.getApps(appContext).isEmpty()) {
            return
        }
        val dbId = appContext.getString(R.string.firestore_database_id)
        val snapshot = FirebaseFirestore.getInstance(dbId)
            .collection("tourism_hotspots")
            .limit(30)
            .get()
            .await()

        if (snapshot.isEmpty) return

        val cloudEntities = snapshot.documents.mapNotNull { doc ->
            val lat = doc.getDouble("latitude") ?: return@mapNotNull null
            val lon = doc.getDouble("longitude") ?: return@mapNotNull null
            val nameEn = doc.getString("nameEn")?.takeIf { it.isNotBlank() } ?: return@mapNotNull null
            val spotId = doc.getString("spotId")?.takeIf { it.isNotBlank() } ?: doc.id
            val existing = repository.getHotspotByIdSync(spotId)

            HotspotEntity(
                id = spotId,
                name = nameEn,
                odiaName = doc.getString("nameOr").orEmpty(),
                hindiName = nameEn,
                category = doc.getString("category").orEmpty().ifBlank { "Heritage" },
                shortDescription = doc.getString("shortDescription").orEmpty(),
                fullDescription = doc.getString("description").orEmpty(),
                highlights = doc.getString("address").orEmpty(),
                distanceKmFromBls = (doc.getDouble("distanceFromBlsKm") ?: 0.0).toInt(),
                bestTimeToVisit = doc.getString("bestSeason").orEmpty().ifBlank { "October to March" },
                timings = doc.getString("visitingHours").orEmpty().ifBlank { "6:00 AM - 6:00 PM" },
                entryFee = doc.getLong("entryFeeInr")?.let { fee -> if (fee <= 0L) "Free Entry" else "₹$fee" } ?: "Free Entry",
                specialty = doc.getString("address").orEmpty(),
                localTip = "Synced from Balasore Tourism Module Database.",
                latitude = lat,
                longitude = lon,
                isFavorite = existing?.isFavorite ?: false,
                timestamp = System.currentTimeMillis()
            )
        }

        if (cloudEntities.isNotEmpty()) {
            repository.insertHotspots(cloudEntities)
        }
    }
}
