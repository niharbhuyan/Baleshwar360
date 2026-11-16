package com.example.ui.features.coastal

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage

data class SeafoodItem(
    val nameEn: String,
    val nameOd: String,
    val category: String, // "PREMIUM", "ESTUARY", "CRUSTACEAN"
    val pricePerKg: String,
    val peakSeason: String,
    val freshnessTip: String,
    val culinaryStyle: String,
    val iconEmoji: String
)

data class SeafoodStall(
    val name: String,
    val location: String,
    val specialty: String,
    val timings: String,
    val lat: Double,
    val lng: Double
)

/**
 * Balaramgadi Port Daily Catch & Fresh Seafood Index Sheet.
 * Real-time estimated market prices, trawler arrival telemetry, freshness inspection checklist,
 * and famous local seafood fry shacks along Chandipur & Balaramgadi.
 */
@Composable
fun BalaramgadiSeafoodIndexSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("ALL") }

    val fishList = remember {
        listOf(
            SeafoodItem(
                nameEn = "Silver Hilsa (Ilish)",
                nameOd = "ଇଲିଶି ମାଛ",
                category = "PREMIUM",
                pricePerKg = "₹900 - ₹1,450 / kg",
                peakSeason = "July to October (Monsoon Estuary Upstream)",
                freshnessTip = "Budhabalanga estuary catch has silvery oily sheen & delicate crimson gills.",
                culinaryStyle = "Baleswari Sorisa Ilish (Mustard gravy) or fried in its own natural oil.",
                iconEmoji = "🐟"
            ),
            SeafoodItem(
                nameEn = "Tiger Prawns (Bagda Chingudi)",
                nameOd = "ବାଘା ଚିଙ୍ଗୁଡ଼ି",
                category = "CRUSTACEAN",
                pricePerKg = "₹700 - ₹1,100 / kg",
                peakSeason = "Year-Round (Best Oct - Feb)",
                freshnessTip = "Shell must be translucent and firmly attached to the body without black spots.",
                culinaryStyle = "Crispy Baleswari Tawa Rawa Fry or creamy Malai Curry.",
                iconEmoji = "🦐"
            ),
            SeafoodItem(
                nameEn = "Silver Pomfret (Chandi Machha)",
                nameOd = "ଚାନ୍ଦି ମାଛ",
                category = "PREMIUM",
                pricePerKg = "₹750 - ₹950 / kg",
                peakSeason = "September to February",
                freshnessTip = "Clear black eyes with iridescent silver belly and stiff tail fin.",
                culinaryStyle = "Whole Pomfret masala roast with red chili and lemon.",
                iconEmoji = "🐠"
            ),
            SeafoodItem(
                nameEn = "Sea Bass (Bhetki)",
                nameOd = "ଭେଟକି ମାଛ",
                category = "ESTUARY",
                pricePerKg = "₹550 - ₹750 / kg",
                peakSeason = "Winter (November to February)",
                freshnessTip = "White firm meat that springs back instantly upon finger touch.",
                culinaryStyle = "Boneless Bhetki Fry or traditional Odia Besar curry.",
                iconEmoji = "🐟"
            ),
            SeafoodItem(
                nameEn = "Mud Crabs (Kankada)",
                nameOd = "କାଦୁଅ କଙ୍କଡ଼ା",
                category = "CRUSTACEAN",
                pricePerKg = "₹450 - ₹650 / kg",
                peakSeason = "Post-Monsoon (October to March)",
                freshnessTip = "Always buy live, heavy crabs with intact pincers tied with jute string.",
                culinaryStyle = "Spicy Kankada Jhola (Crab Curry) cooked with local red potatoes.",
                iconEmoji = "🦀"
            ),
            SeafoodItem(
                nameEn = "Indian Salmon (Sahala / Rawas)",
                nameOd = "ସାହାଳ ମାଛ",
                category = "ESTUARY",
                pricePerKg = "₹420 - ₹600 / kg",
                peakSeason = "August to December",
                freshnessTip = "Firm silvery pinkish flesh, ocean-fresh saline scent.",
                culinaryStyle = "Steamed or shallow pan fried with green chilies.",
                iconEmoji = "🐟"
            ),
            SeafoodItem(
                nameEn = "Bombay Duck / Lizardfish (Bam)",
                nameOd = "ବାମ ମାଛ",
                category = "ESTUARY",
                pricePerKg = "₹200 - ₹320 / kg",
                peakSeason = "November to April",
                freshnessTip = "Gelatinous texture, best dried or eaten ultra-fresh off the boat.",
                culinaryStyle = "Crispy rawa fried crunch starter.",
                iconEmoji = "🐟"
            )
        )
    }

    val stalls = remember {
        listOf(
            SeafoodStall(
                name = "Balaramgadi Jetty Fishermen Cooperative Stalls",
                location = "Budhabalanga River Estuary Jetty, Chandipur",
                specialty = "Direct off-trawler whole fish auction & fresh cut",
                timings = "05:30 AM - 09:00 AM & 03:30 PM - 06:00 PM",
                lat = 21.4720,
                lng = 87.0500
            ),
            SeafoodStall(
                name = "Chandipur Beachfront Casuarina Fish Fry Shacks",
                location = "Near OTDC Panthanivas beach road",
                specialty = "Hot Pomfret & Tiger Prawn tawa fry with onion salad",
                timings = "04:00 PM - 10:00 PM",
                lat = 21.4680,
                lng = 87.0160
            ),
            SeafoodStall(
                name = "Station Bazar Fresh Fish Mandi",
                location = "Near Balasore Old Railway Station Market",
                specialty = "Morning fresh river catches & Remuna freshwater fish",
                timings = "06:00 AM - 11:30 AM",
                lat = 21.4910,
                lng = 86.9350
            )
        )
    }

    val filteredList = remember(searchQuery, selectedFilter) {
        fishList.filter { item ->
            val matchesSearch = item.nameEn.contains(searchQuery, ignoreCase = true) ||
                    item.nameOd.contains(searchQuery, ignoreCase = true)
            val matchesCategory = when (selectedFilter) {
                "PREMIUM" -> item.category == "PREMIUM"
                "ESTUARY" -> item.category == "ESTUARY"
                "CRUSTACEAN" -> item.category == "CRUSTACEAN"
                else -> true
            }
            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("balaramgadi_seafood_index_sheet")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0D9488)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🐟", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ବଳରାମଗଡ଼ି ମତ୍ସ୍ୟ ସୂଚକାଙ୍କ ଓ ତାଜା ମାଛ ଦର" else "Balaramgadi Fresh Seafood Index",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == AppLanguage.ODIA) "ଦୈନିକ ଟ୍ରଲର ଅବତରଣ ସମୟ ଓ ବଜାର ଦର" else "Daily Trawler Landings & Market Rates",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_seafood_index_btn")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Trawler Arrival Timing Banner
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDFA)),
            border = BorderStroke(1.dp, Color(0xFF99F6E4)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = Color(0xFF0F766E),
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "Trawler Fleet Landings at Balaramgadi Jetty",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF0F766E)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFCCFBF1)),
                        modifier = Modifier.weight(1f).padding(end = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("🌅 Morning Haul", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488))
                            Text("05:30 AM - 08:30 AM", fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text("Best for Hilsa & Giant Prawns", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, Color(0xFFCCFBF1)),
                        modifier = Modifier.weight(1f).padding(start = 4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("🌇 Twilight Haul", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0D9488))
                            Text("03:30 PM - 05:45 PM", fontSize = 12.sp, fontWeight = FontWeight.Black)
                            Text("Best for Live Mud Crabs & Pomfret", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search & Filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search fish in English or Odia (ଇଲିଶି, କଙ୍କଡ଼ା)...", fontSize = 12.sp) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF0D9488)) },
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All Catches (${fishList.size})") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0D9488),
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "PREMIUM",
                    onClick = { selectedFilter = "PREMIUM" },
                    label = { Text("🌟 Premium Marine") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0D9488),
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "CRUSTACEAN",
                    onClick = { selectedFilter = "CRUSTACEAN" },
                    label = { Text("🦐 Prawns & Crabs") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0D9488),
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedFilter == "ESTUARY",
                    onClick = { selectedFilter = "ESTUARY" },
                    label = { Text("🌊 Estuary Catches") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF0D9488),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Fish List
        filteredList.forEach { fish ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, Color(0xFF99F6E4).copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(fish.iconEmoji, fontSize = 24.sp)
                            Column {
                                Text(
                                    text = fish.nameEn,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = fish.nameOd,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFF0D9488)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color(0xFFCCFBF1)
                        ) {
                            Text(
                                text = fish.pricePerKg,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF0F766E)
                                ),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "📅 Peak Season: ${fish.peakSeason}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.outline
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "🔎 Freshness Tip: ${fish.freshnessTip}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surface
                    ) {
                        Text(
                            text = "🍲 Recipe: ${fish.culinaryStyle}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Freshness Inspection 4-Point Checklist
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF0D9488))
                    Text(
                        text = "How to Inspect Freshness at the Jetty",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                val tips = listOf(
                    "Gills Check: Lift gill cover — should be bright ruby/crimson red, never brownish or grey.",
                    "Eyes Test: Eyes should be plump, clear and convex, never sunken or milky cloudy.",
                    "Elasticity: Press gently on flesh with thumb; it must bounce back instantly without leaving a depression.",
                    "Ocean Aroma: Fresh catches smell like mild sea breeze & salt spray; never sour or ammoniac."
                )
                tips.forEach { tip ->
                    Row(
                        modifier = Modifier.padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text("✔", fontSize = 12.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.Bold)
                        Text(tip, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Famous Seafood Fry Counters
        Text(
            text = "Popular Coastal Seafood Fry Spots:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        stalls.forEach { stall ->
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(stall.name, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
                        Text("📍 ${stall.location}", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                        Text("🔥 ${stall.specialty}", fontSize = 11.sp, color = Color(0xFF0D9488), fontWeight = FontWeight.Medium)
                        Text("⏰ ${stall.timings}", fontSize = 10.sp, color = Color.Gray)
                    }
                    IconButton(
                        onClick = {
                            val geoUri = Uri.parse("geo:${stall.lat},${stall.lng}?q=${stall.lat},${stall.lng}(${Uri.encode(stall.name)})")
                            context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
                        }
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = "Navigate", tint = Color(0xFF0D9488))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClose,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Done / Close Seafood Index", fontWeight = FontWeight.Bold)
        }
    }
}
