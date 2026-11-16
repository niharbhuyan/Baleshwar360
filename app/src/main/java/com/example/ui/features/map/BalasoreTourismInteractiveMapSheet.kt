package com.example.ui.features.map

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.LocalPolice
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.maps.BalasoreGeoNewsLocation
import com.example.data.maps.BalasoreInteractiveMapDataSource
import com.example.data.maps.BalasoreWeatherAlertZone
import com.example.data.model.AppLanguage
import com.example.data.model.CycloneShelter
import com.example.data.model.Hotspot
import com.example.data.repository.BalasoreRepository
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polygon
import kotlin.math.*

enum class BalasoreMapLayer(val label: String) {
    ALL("All Layers"),
    NEWS_LOCATIONS("News Locations"),
    WEATHER_ALERTS("Weather Alert Areas"),
    HOTSPOTS("Tourism Hotspots"),
    CYCLONE_SHELTERS("Cyclone Shelters"),
    POLICE_STATIONS("Police Stations"),
    HOSPITALS("Hospitals")
}

data class BalasoreCivicPoi(
    val id: String,
    val name: String,
    val layer: BalasoreMapLayer,
    val latitude: Double,
    val longitude: Double,
    val subtitle: String,
    val contactPhone: String
)

private fun haversineKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2.0)
    return 2 * r * asin(sqrt(a))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreTourismInteractiveMapSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    hotspots: List<Hotspot> = BalasoreRepository.hotspots,
    cycloneShelters: List<CycloneShelter> = BalasoreRepository.cycloneShelters,
    isOdia: Boolean = (language == AppLanguage.ODIA),
    initialSelectedHotspot: Hotspot? = null,
    onSelectHotspotForDetail: (Hotspot) -> Unit = {},
    onDismiss: () -> Unit = onClose
) {
    var activeLayer by remember { mutableStateOf(BalasoreMapLayer.ALL) }
    var userLat by remember { mutableStateOf(21.4934) }
    var userLon by remember { mutableStateOf(86.9325) }
    var selectedHotspotModal by remember(hotspots, initialSelectedHotspot) {
        mutableStateOf<Hotspot?>(initialSelectedHotspot ?: hotspots.firstOrNull())
    }
    var selectedNewsModal by remember { mutableStateOf<BalasoreGeoNewsLocation?>(null) }
    var selectedWeatherZoneModal by remember { mutableStateOf<BalasoreWeatherAlertZone?>(null) }
    var selectedPoiSummary by remember {
        mutableStateOf<String?>(
            initialSelectedHotspot?.let { "${it.name} • ${it.location}" }
        )
    }

    val geoNewsList = remember { BalasoreInteractiveMapDataSource.BALASORE_GEO_NEWS_LOCATIONS }
    val weatherZonesList = remember { BalasoreInteractiveMapDataSource.BALASORE_WEATHER_ALERT_ZONES }

    val civicPois = remember(cycloneShelters, hotspots, geoNewsList, weatherZonesList, isOdia) {
        val list = mutableListOf(
            BalasoreCivicPoi("cs_1", "Chandipur Multipurpose Cyclone Shelter", BalasoreMapLayer.CYCLONE_SHELTERS, 21.4450, 87.0155, "Capacity: 1200 • OSDMA Coastal Shelter", "06782-270012"),
            BalasoreCivicPoi("cs_2", "Balaramgadi Coastal Cyclone Shelter", BalasoreMapLayer.CYCLONE_SHELTERS, 21.4712, 87.0398, "Capacity: 950 • Harbor Evacuation Point", "06782-270108"),
            BalasoreCivicPoi("cs_3", "Talsari-Udayapur Cyclone Shelter", BalasoreMapLayer.CYCLONE_SHELTERS, 21.6085, 87.4520, "Capacity: 1100 • Bhograi Coastal Block", "06781-253210"),
            BalasoreCivicPoi("ps_1", "Chandipur Marine Police Station", BalasoreMapLayer.POLICE_STATIONS, 21.4510, 87.0220, "24x7 Coastal Patrol & Tourist Safety", "112 / 06782-270033"),
            BalasoreCivicPoi("ps_2", "Sahadevkhunta Model Police Station", BalasoreMapLayer.POLICE_STATIONS, 21.4988, 86.9310, "Balasore Town HQ & Railway Corridor", "06782-262040"),
            BalasoreCivicPoi("ps_3", "Town Police Station Balasore", BalasoreMapLayer.POLICE_STATIONS, 21.4895, 86.9380, "City Emergency Response Unit", "06782-262223"),
            BalasoreCivicPoi("hp_1", "Fakir Mohan Medical College & Hospital (FMMCH)", BalasoreMapLayer.HOSPITALS, 21.4922, 86.9248, "24x7 Trauma, Anti-Venom & ICU Center", "108 / 06782-262007"),
            BalasoreCivicPoi("hp_2", "CHC Chandipur Coastal Health Center", BalasoreMapLayer.HOSPITALS, 21.4480, 87.0090, "Primary Emergency & Marine First Aid", "06782-270045"),
            BalasoreCivicPoi("hp_3", "Jyoti Hospital Kuruda", BalasoreMapLayer.HOSPITALS, 21.4640, 86.8980, "NH-16 Highway Multi-Specialty Care", "06782-255890")
        )
        cycloneShelters.forEachIndexed { idx, shelter ->
            val latOffset = (idx + 1) * 0.011
            val lonOffset = (idx + 1) * 0.009
            list.add(
                BalasoreCivicPoi(
                    id = "shelter_dyn_$idx",
                    name = shelter.name,
                    layer = BalasoreMapLayer.CYCLONE_SHELTERS,
                    latitude = 21.4550 + latOffset,
                    longitude = 87.0050 + lonOffset,
                    subtitle = "${shelter.block} • Capacity: ${shelter.capacityPeople}",
                    contactPhone = shelter.phone
                )
            )
        }
        hotspots.forEachIndexed { idx, h ->
            val lat = if (h.latitude > 1.0) h.latitude else 21.4446 + (idx * 0.018)
            val lon = if (h.longitude > 1.0) h.longitude else 87.0149 - (idx * 0.022)
            list.add(
                BalasoreCivicPoi(
                    id = "hotspot_${h.id}",
                    name = if (isOdia && h.odiaName.isNotBlank()) h.odiaName else h.name,
                    layer = BalasoreMapLayer.HOTSPOTS,
                    latitude = lat,
                    longitude = lon,
                    subtitle = "${h.category} • ${h.location}",
                    contactPhone = h.timing
                )
            )
        }
        geoNewsList.forEach { news ->
            list.add(
                BalasoreCivicPoi(
                    id = "news_${news.id}",
                    name = "📰 ${if (isOdia) news.headlineOr else news.headlineEn}",
                    layer = BalasoreMapLayer.NEWS_LOCATIONS,
                    latitude = news.latitude,
                    longitude = news.longitude,
                    subtitle = "${news.locationNameEn} • ${news.category} (${news.publishedTimeAgo})",
                    contactPhone = news.sourceReporter
                )
            )
        }
        weatherZonesList.forEach { wx ->
            list.add(
                BalasoreCivicPoi(
                    id = "wx_${wx.id}",
                    name = "${wx.emoji} [${wx.severity}] ${if (isOdia) wx.alertTitleOr else wx.alertTitleEn}",
                    layer = BalasoreMapLayer.WEATHER_ALERTS,
                    latitude = wx.centerLat,
                    longitude = wx.centerLng,
                    subtitle = "${wx.zoneNameEn} • Wind ${wx.windSpeedKmh} km/h • Rain ${wx.expectedRainfallMm} mm",
                    contactPhone = wx.nearestShelterPhone
                )
            )
        }
        list
    }

    val sheetContext = LocalContext.current
    var isCachedOfflineMode by remember {
        mutableStateOf(
            com.example.data.maps.BalasoreOfflineTileCacheManager.isCachedOfflineMapMode(
                context = sheetContext,
                defaultOffline = false
            )
        )
    }
    // Radius-based proximity filtering (null = All, 5.0 = Within 5km, 10.0 = Within 10km, 25.0 = Within 25km)
    var selectedRadiusKm by remember { mutableStateOf<Double?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) "ବାଲେଶ୍ୱର ଇଣ୍ଟରାକ୍ଟିଭ୍ ମାନଚିତ୍ର (ଖବର, ପାଣିପାଗ ଓ ପର୍ଯ୍ୟଟନ)" else "Balasore Interactive Map (News, Weather & Hotspots)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isOdia) "ଖବର ସ୍ଥାନ, ପାଣିପାଗ ସତର୍କତା ଅଞ୍ଚଳ ଓ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ମାର୍କର" else "Clickable News Locations (${geoNewsList.size}), Weather Alert Zones (${weatherZonesList.size}) & Hotspots (${hotspots.size})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close Map")
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // MapView Settings Toggle Switch: 'Live Online Map' vs 'Cached Offline Map'
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isCachedOfflineMode) "Cached Offline Map Mode" else "Live Online Map Mode",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isCachedOfflineMode) {
                                "Using cached offline tiles • Saves data & works without connectivity"
                            } else {
                                "Streaming live online tiles • Switch to Cached Offline Map to save data"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isCachedOfflineMode,
                        onCheckedChange = { checked ->
                            isCachedOfflineMode = checked
                            com.example.data.maps.BalasoreOfflineTileCacheManager.setCachedOfflineMapMode(sheetContext, checked)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                FilterChip(
                    selected = activeLayer == BalasoreMapLayer.ALL,
                    onClick = { activeLayer = BalasoreMapLayer.ALL },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = activeLayer == BalasoreMapLayer.NEWS_LOCATIONS,
                    onClick = {
                        activeLayer = BalasoreMapLayer.NEWS_LOCATIONS
                        selectedNewsModal = geoNewsList.firstOrNull()
                        selectedWeatherZoneModal = null
                        selectedHotspotModal = null
                    },
                    label = { Text("📰 News (${geoNewsList.size})") },
                    modifier = Modifier.testTag("map_filter_news_locations_chip")
                )
                FilterChip(
                    selected = activeLayer == BalasoreMapLayer.WEATHER_ALERTS,
                    onClick = {
                        activeLayer = BalasoreMapLayer.WEATHER_ALERTS
                        selectedWeatherZoneModal = weatherZonesList.firstOrNull()
                        selectedNewsModal = null
                        selectedHotspotModal = null
                    },
                    label = { Text("⛈️ Weather Alerts (${weatherZonesList.size})") },
                    modifier = Modifier.testTag("map_filter_weather_alerts_chip")
                )
                FilterChip(
                    selected = activeLayer == BalasoreMapLayer.HOTSPOTS,
                    onClick = {
                        activeLayer = BalasoreMapLayer.HOTSPOTS
                        selectedHotspotModal = hotspots.firstOrNull()
                        selectedNewsModal = null
                        selectedWeatherZoneModal = null
                    },
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Hotspots (${hotspots.size})") },
                    modifier = Modifier.testTag("map_filter_hotspots_chip")
                )
                FilterChip(
                    selected = activeLayer == BalasoreMapLayer.CYCLONE_SHELTERS,
                    onClick = { activeLayer = BalasoreMapLayer.CYCLONE_SHELTERS },
                    leadingIcon = { Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Shelters") }
                )
                FilterChip(
                    selected = activeLayer == BalasoreMapLayer.POLICE_STATIONS,
                    onClick = { activeLayer = BalasoreMapLayer.POLICE_STATIONS },
                    leadingIcon = { Icon(Icons.Default.LocalPolice, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Police") }
                )
                FilterChip(
                    selected = activeLayer == BalasoreMapLayer.HOSPITALS,
                    onClick = { activeLayer = BalasoreMapLayer.HOSPITALS },
                    leadingIcon = { Icon(Icons.Default.LocalHospital, contentDescription = null, modifier = Modifier.size(16.dp)) },
                    label = { Text("Hospitals") }
                )
            }

            // Radius-Based Proximity Filter Bar (Within 5km, 10km, 25km, All)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOdia) "📍 ଦୂରତା ଫିଲ୍ଟର:" else "📍 Radius Filter:",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                val radiusOptions = listOf(
                    null to (if (isOdia) "ସମସ୍ତ (All)" else "All Distances"),
                    5.0 to "Within 5 km",
                    10.0 to "Within 10 km",
                    25.0 to "Within 25 km"
                )
                radiusOptions.forEach { (radiusVal, label) ->
                    val tagSuffix = radiusVal?.toInt()?.let { "${it}km" } ?: "all"
                    FilterChip(
                        selected = selectedRadiusKm == radiusVal,
                        onClick = { selectedRadiusKm = radiusVal },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.sp,
                                fontWeight = if (selectedRadiusKm == radiusVal) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("map_radius_filter_$tagSuffix")
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                val sheetTileProvider = remember {
                    com.example.data.maps.BalasoreOsmOfflineTileProvider(sheetContext.applicationContext ?: sheetContext)
                }
                var sheetMapView by remember { mutableStateOf<MapView?>(null) }

                DisposableEffect(Unit) {
                    onDispose {
                        try {
                            sheetMapView?.onPause()
                            sheetMapView?.onDetach()
                            sheetTileProvider.detach()
                        } catch (_: Exception) {}
                    }
                }

                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { ctx ->
                        Configuration.getInstance().userAgentValue = ctx.packageName
                        MapView(ctx, sheetTileProvider).apply {
                            sheetMapView = this
                            setTileSource(TileSourceFactory.MAPNIK)
                            setUseDataConnection(!isCachedOfflineMode)
                            setMultiTouchControls(true)
                            com.example.data.maps.BalasoreOsmDarkStyleHelper.applyMapColorStyle(this, true)
                            controller.setZoom(11.4)
                            controller.setCenter(GeoPoint(userLat, userLon))
                        }
                    },
                    update = { mapView ->
                        mapView.setUseDataConnection(!isCachedOfflineMode)
                        com.example.data.maps.BalasoreOsmDarkStyleHelper.applyMapColorStyle(mapView, true)
                        mapView.overlays.clear()

                        // Draw Weather Alert Area circular polygons when All or Weather Alerts layer is active
                        if (activeLayer == BalasoreMapLayer.ALL || activeLayer == BalasoreMapLayer.WEATHER_ALERTS) {
                            weatherZonesList.forEach { wxZone ->
                                val distKm = haversineKm(userLat, userLon, wxZone.centerLat, wxZone.centerLng)
                                if (selectedRadiusKm == null || distKm <= selectedRadiusKm!!) {
                                    val circlePoints = Polygon.pointsAsCircle(
                                        GeoPoint(wxZone.centerLat, wxZone.centerLng),
                                        wxZone.radiusMeters
                                    )
                                    val strokeColor = when (wxZone.severity) {
                                        "CRITICAL" -> android.graphics.Color.rgb(220, 38, 38)
                                        "SEVERE" -> android.graphics.Color.rgb(234, 88, 12)
                                        else -> android.graphics.Color.rgb(217, 119, 6)
                                    }
                                    val fillColor = when (wxZone.severity) {
                                        "CRITICAL" -> android.graphics.Color.argb(58, 220, 38, 38)
                                        "SEVERE" -> android.graphics.Color.argb(52, 234, 88, 12)
                                        else -> android.graphics.Color.argb(45, 217, 119, 6)
                                    }
                                    val polygon = Polygon(mapView).apply {
                                        points = circlePoints
                                        fillPaint.color = fillColor
                                        outlinePaint.color = strokeColor
                                        outlinePaint.strokeWidth = 4f
                                        title = "${wxZone.emoji} [${wxZone.severity}] ${wxZone.alertTitleEn}"
                                        setOnClickListener { _, _, _ ->
                                            selectedWeatherZoneModal = wxZone
                                            selectedNewsModal = null
                                            selectedHotspotModal = null
                                            selectedPoiSummary = "${wxZone.emoji} ${wxZone.alertTitleEn} • Wind ${wxZone.windSpeedKmh} km/h • Rain ${wxZone.expectedRainfallMm} mm"
                                            true
                                        }
                                    }
                                    mapView.overlays.add(polygon)
                                }
                            }
                        }

                        val userMarker = Marker(mapView).apply {
                            position = GeoPoint(userLat, userLon)
                            title = "Your Current Location (Balasore)"
                            snippet = "GPS: ${String.format("%.4f", userLat)}, ${String.format("%.4f", userLon)}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                        }
                        mapView.overlays.add(userMarker)

                        val filtered = civicPois.filter { poi ->
                            val matchesLayer = activeLayer == BalasoreMapLayer.ALL || poi.layer == activeLayer
                            val distKm = haversineKm(userLat, userLon, poi.latitude, poi.longitude)
                            val matchesRadius = selectedRadiusKm == null || distKm <= selectedRadiusKm!!
                            matchesLayer && matchesRadius
                        }

                        filtered.forEach { poi ->
                            val distKm = haversineKm(userLat, userLon, poi.latitude, poi.longitude)
                            val marker = Marker(mapView).apply {
                                position = GeoPoint(poi.latitude, poi.longitude)
                                title = "${poi.name} (${String.format("%.1f km", distKm)})"
                                snippet = "${poi.subtitle} • ${poi.contactPhone}"
                                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                setOnMarkerClickListener { m, _ ->
                                    m.showInfoWindow()
                                    selectedPoiSummary = "${poi.name} • ${String.format("%.1f km away", distKm)} • ${poi.subtitle} (${poi.contactPhone})"
                                    val matchedSpot = hotspots.find { "hotspot_${it.id}" == poi.id }
                                    val matchedNews = geoNewsList.find { "news_${it.id}" == poi.id }
                                    val matchedWx = weatherZonesList.find { "wx_${it.id}" == poi.id }
                                    when {
                                        matchedNews != null -> {
                                            selectedNewsModal = matchedNews
                                            selectedWeatherZoneModal = null
                                            selectedHotspotModal = null
                                        }
                                        matchedWx != null -> {
                                            selectedWeatherZoneModal = matchedWx
                                            selectedNewsModal = null
                                            selectedHotspotModal = null
                                        }
                                        matchedSpot != null -> {
                                            selectedHotspotModal = matchedSpot
                                            selectedNewsModal = null
                                            selectedWeatherZoneModal = null
                                            onSelectHotspotForDetail(matchedSpot)
                                        }
                                    }
                                    true
                                }
                            }
                            mapView.overlays.add(marker)
                        }
                        mapView.invalidate()
                    }
                )

                SmallFloatingActionButton(
                    onClick = {
                        userLat = 21.4446
                        userLon = 87.0149
                        selectedPoiSummary = "Centered on Chandipur Coastline (21.4446° N, 87.0149° E)"
                    },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = "Recenter on User Location")
                }
            }

            // Tapped Marker Detail Modal: News Location OR Weather Alert Area OR Tourist Hotspot
            val activeNews = selectedNewsModal
            val activeWx = selectedWeatherZoneModal
            val spot = selectedHotspotModal
            if (activeNews != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFEEF2FF)),
                    border = BorderStroke(1.dp, Color(0xFF4F46E5).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("interactive_map_news_modal")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activeNews.emoji} ${if (isOdia) activeNews.headlineOr else activeNews.headlineEn}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF1E1B4B)
                                )
                                Text(
                                    text = "📍 ${activeNews.locationNameEn} (${activeNews.wardOrBlock}) • ${activeNews.publishedTimeAgo}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4338CA)
                                )
                            }
                            IconButton(onClick = { selectedNewsModal = null }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close News Modal")
                            }
                        }
                        Text(
                            text = if (isOdia) activeNews.summaryOr else activeNews.summaryEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF312E81)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Source: ${activeNews.sourceReporter}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4338CA)
                            )
                            Button(
                                onClick = {
                                    sheetContext.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("geo:${activeNews.latitude},${activeNews.longitude}?q=${Uri.encode(activeNews.locationNameEn)}")
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOdia) "ସ୍ଥାନକୁ ଯାଆନ୍ତୁ" else "Navigate", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (activeWx != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = BorderStroke(1.dp, Color(0xFFDC2626).copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("interactive_map_weather_alert_modal")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${activeWx.emoji} [${activeWx.severity}] ${if (isOdia) activeWx.alertTitleOr else activeWx.alertTitleEn}",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF7F1D1D)
                                )
                                Text(
                                    text = "${activeWx.zoneNameEn} • Wind ${activeWx.windSpeedKmh} km/h • Rain ${activeWx.expectedRainfallMm} mm",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB91C1C)
                                )
                            }
                            IconButton(onClick = { selectedWeatherZoneModal = null }, modifier = Modifier.size(28.dp)) {
                                Icon(Icons.Default.Close, contentDescription = "Close Weather Alert Modal")
                            }
                        }
                        Text(
                            text = if (isOdia) activeWx.imdAdvisoryOr else activeWx.imdAdvisoryEn,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF991B1B)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🛡️ Shelter: ${activeWx.nearestShelterName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF047857),
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    sheetContext.startActivity(
                                        Intent(
                                            Intent.ACTION_VIEW,
                                            Uri.parse("geo:${activeWx.nearestShelterLat},${activeWx.nearestShelterLng}?q=${Uri.encode(activeWx.nearestShelterName)}")
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOdia) "ଆଶ୍ରୟସ୍ଥଳୀ" else "Shelter Nav", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else if (spot != null) {
                val spotIdx = hotspots.indexOfFirst { it.id == spot.id }.coerceAtLeast(0)
                val spotLat = if (spot.latitude > 1.0) spot.latitude else 21.4446 + (spotIdx * 0.018)
                val spotLon = if (spot.longitude > 1.0) spot.longitude else 87.0149 - (spotIdx * 0.022)

                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("interactive_map_hotspot_modal")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Visual Preview Banner Header
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(64.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFF0C4A6E), Color(0xFF0284C7), Color(0xFF38BDF8))
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isOdia && spot.odiaName.isNotBlank()) spot.odiaName else spot.name,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 14.sp,
                                        color = Color.White,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = "${spot.category} • ⭐ ${spot.rating} (Verified Rating) • ${spot.location}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFBAE6FD),
                                        maxLines = 1
                                    )
                                }
                                IconButton(
                                    onClick = { selectedHotspotModal = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close Hotspot Modal", tint = Color.White)
                                }
                            }
                        }

                        // Swipeable High-Resolution Offline Image Gallery Carousel
                        com.example.ui.components.HotspotOfflineHighResGalleryCarousel(
                            spotName = spot.name,
                            spotCategory = spot.category,
                            isOdia = isOdia
                        )

                        Text(
                            text = spot.description,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 2
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🕒 ${spot.timing}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Button(
                                onClick = {
                                    val navUri = Uri.parse("google.navigation:q=$spotLat,$spotLon")
                                    val mapIntent = Intent(Intent.ACTION_VIEW, navUri).apply {
                                        setPackage("com.google.android.apps.maps")
                                    }
                                    try {
                                        sheetContext.startActivity(mapIntent)
                                    } catch (_: Exception) {
                                        sheetContext.startActivity(
                                            Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse("geo:$spotLat,$spotLon?q=${Uri.encode(spot.name)}")
                                            )
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.testTag("interactive_map_get_directions_btn")
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(if (isOdia) "ଦିଗନିର୍ଦ୍ଦେଶ" else "Get Directions", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                selectedPoiSummary?.let { summary ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = summary,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}


