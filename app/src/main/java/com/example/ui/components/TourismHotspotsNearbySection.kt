package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.emergency.EmergencySosManager
import com.example.data.model.AppLanguage
import com.example.data.model.Hotspot
import com.example.ui.theme.*
import java.util.Locale

/**
 * Enhanced Tourism Hotspots Explorer List that uses Google Maps API / coordinates
 * to show points of interest near the user's current GPS location, complete with
 * rich descriptions, distances, transit routes, and turn-by-turn map directions.
 */
@Composable
fun TourismHotspotsNearbySection(
    hotspots: List<Hotspot> = emptyList(),
    language: AppLanguage = AppLanguage.ENGLISH,
    onOpenFullMap: () -> Unit = {},
    onAddToItinerary: (Hotspot) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Live or Selected Location State
    var userLat by remember { mutableDoubleStateOf(21.4934) }
    var userLng by remember { mutableDoubleStateOf(86.9135) }
    var locationName by remember { mutableStateOf("Balasore Junction (City Center)") }
    var isLiveGpsActive by remember { mutableStateOf(false) }

    // Filter and Sort State
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }
    var selectedHotspotDetail by remember { mutableStateOf<TouristLocationItem?>(null) }

    // Location Permission Request
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { perms ->
        val granted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            EmergencySosManager.getCurrentCoordinates(context) { lat, lng, desc ->
                userLat = lat
                userLng = lng
                locationName = "Your GPS Location"
                isLiveGpsActive = true
                Toast.makeText(context, "Location updated: %.4f, %.4f".format(Locale.US, lat, lng), Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Categories available
    val categories = listOf("All", "Beach & Coast", "Heritage & Spiritual", "Nature & Wildlife", "Landmark")

    // Filter and compute distances relative to user's coordinates
    val computedHotspots = remember(userLat, userLng, selectedCategory, searchQuery) {
        BALASORE_TOURIST_LOCATIONS.map { spot ->
            val distance = calculateHaversineDistanceKm(userLat, userLng, spot.latitude, spot.longitude)
            spot.copy(baseDistanceKm = distance)
        }.filter { spot ->
            val matchesCategory = when (selectedCategory) {
                "All" -> true
                "Beach & Coast" -> spot.categoryGroup == "BEACH"
                "Heritage & Spiritual" -> spot.categoryGroup == "TEMPLE"
                "Nature & Wildlife" -> spot.categoryGroup == "NATURE"
                "Landmark" -> spot.categoryGroup == "LANDMARK"
                else -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    spot.nameEn.contains(searchQuery, ignoreCase = true) ||
                    spot.nameOr.contains(searchQuery, ignoreCase = true) ||
                    spot.shortDescEn.contains(searchQuery, ignoreCase = true)

            matchesCategory && matchesSearch
        }.sortedBy { it.baseDistanceKm }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("tourism_hotspots_nearby_section"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, BentoSlate200)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: Title & GPS Location Sync Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = OceanBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Explore,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.ODIA) "ନିକଟତମ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ" else "Nearby Tourism Hotspots",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = BentoSlate900
                            )
                        )
                        Text(
                            text = "Google Maps API • ${computedHotspots.size} points of interest",
                            fontSize = 11.sp,
                            color = BentoSlate500
                        )
                    }
                }

                // Interactive GPS Acquire Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isLiveGpsActive) Color(0xFFDCFCE7) else Color(0xFFEFF6FF),
                    border = BorderStroke(1.dp, if (isLiveGpsActive) Color(0xFF86EFAC) else Color(0xFFBFDBFE)),
                    modifier = Modifier
                        .clickable {
                            val hasFine = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasFine) {
                                EmergencySosManager.getCurrentCoordinates(context) { lat, lng, _ ->
                                    userLat = lat
                                    userLng = lng
                                    locationName = "Your GPS Location"
                                    isLiveGpsActive = true
                                    Toast.makeText(context, "Distance re-calculated from current GPS", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        }
                        .testTag("hotspot_gps_sync_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isLiveGpsActive) Icons.Default.MyLocation else Icons.Default.LocationSearching,
                            contentDescription = "Sync GPS",
                            tint = if (isLiveGpsActive) Color(0xFF15803D) else OceanBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isLiveGpsActive) "Live GPS" else "Use GPS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLiveGpsActive) Color(0xFF15803D) else OceanBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Origin Indicator Bar
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = BentoSlate50,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📍", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reference: $locationName (%.4f° N, %.4f° E)".format(Locale.US, userLat, userLng),
                            fontSize = 11.sp,
                            color = BentoSlate700,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    TextButton(
                        onClick = onOpenFullMap,
                        contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp)
                    ) {
                        Text(
                            text = "Full Map ➔",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar & Filter Chips
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text("Search nearby beaches, temples, waterfalls...", fontSize = 12.sp, color = BentoSlate400)
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hotspot_search_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OceanBlue,
                    unfocusedBorderColor = BentoSlate200,
                    focusedContainerColor = BentoSlate50,
                    unfocusedContainerColor = BentoSlate50
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Category Chips Row
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories) { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(text = cat, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanBlue,
                            selectedLabelColor = Color.White,
                            containerColor = BentoSlate100,
                            labelColor = BentoSlate800
                        ),
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // List of Points of Interest sorted by distance
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                computedHotspots.take(5).forEach { spot ->
                    HotspotNearbyListItem(
                        spot = spot,
                        language = language,
                        onViewDetails = { selectedHotspotDetail = spot },
                        onNavigate = {
                            val uri = Uri.parse("google.navigation:q=${spot.latitude},${spot.longitude}&mode=d")
                            val mapIntent = Intent(Intent.ACTION_VIEW, uri).apply {
                                setPackage("com.google.android.apps.maps")
                            }
                            try {
                                context.startActivity(mapIntent)
                            } catch (_: Exception) {
                                val browserUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${spot.latitude},${spot.longitude}")
                                context.startActivity(Intent(Intent.ACTION_VIEW, browserUri))
                            }
                        }
                    )
                }
            }

            if (computedHotspots.size > 5) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onOpenFullMap,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("view_all_hotspots_on_map_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "View All ${computedHotspots.size} Hotspots on Google Maps",
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // Hotspot Detail Bottom Dialog
    if (selectedHotspotDetail != null) {
        val spot = selectedHotspotDetail!!
        AlertDialog(
            onDismissRequest = { selectedHotspotDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(spot.highlightEmoji, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.ODIA) spot.nameOr else spot.nameEn,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = BentoSlate900
                        )
                        Text(
                            text = "${spot.categoryEn} • %.1f km from you".format(Locale.US, spot.baseDistanceKm),
                            fontSize = 11.sp,
                            color = OceanBlue
                        )
                    }
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = if (language == AppLanguage.ODIA) spot.shortDescOr else spot.shortDescEn,
                        fontSize = 12.5.sp,
                        color = BentoSlate700,
                        lineHeight = 18.sp
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BentoSlate100,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("🕒 Timings: ${spot.timings}", fontSize = 11.sp, color = BentoSlate800)
                            Text("🎟️ Entry: ${spot.entryFee}", fontSize = 11.sp, color = BentoSlate800)
                            Text("🚌 Transit: ${spot.transitRoutes.joinToString(", ")}", fontSize = 11.sp, color = BentoSlate800)
                            Text("💰 Approx Fare: ${spot.estimatedTransitFare}", fontSize = 11.sp, color = BentoSlate800)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val uri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${spot.latitude},${spot.longitude}")
                        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                        selectedHotspotDetail = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                ) {
                    Icon(imageVector = Icons.Default.Directions, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Navigate in Google Maps")
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedHotspotDetail = null }) {
                    Text("Close")
                }
            }
        )
    }
}

/**
 * Individual point-of-interest card showing name, distance, description, and navigation trigger.
 */
@Composable
fun HotspotNearbyListItem(
    spot: TouristLocationItem,
    language: AppLanguage,
    onViewDetails: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onViewDetails() }
            .testTag("hotspot_item_${spot.id}"),
        shape = RoundedCornerShape(14.dp),
        color = BentoSlate50,
        border = BorderStroke(1.dp, BentoSlate200)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BentoSlate200),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(text = spot.highlightEmoji, fontSize = 22.sp)
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (language == AppLanguage.ODIA) spot.nameOr else spot.nameEn,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate900
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = if (language == AppLanguage.ODIA) spot.shortDescOr else spot.shortDescEn,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 11.5.sp,
                            color = BentoSlate600,
                            lineHeight = 15.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = OceanBlue.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "📍 %.1f km away".format(Locale.US, spot.baseDistanceKm),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }

                        Text(
                            text = "⭐ ${spot.rating} (${spot.reviewCount})",
                            fontSize = 10.5.sp,
                            color = AmberGold,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Navigation Button
            Surface(
                shape = CircleShape,
                color = OceanBlue,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .clickable { onNavigate() }
                    .testTag("navigate_hotspot_${spot.id}")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Directions,
                        contentDescription = "Navigate to ${spot.nameEn}",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
