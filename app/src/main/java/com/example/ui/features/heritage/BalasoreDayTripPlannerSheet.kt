package com.example.ui.features.heritage

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TwoWheeler
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

data class ItineraryStep(
    val time: String,
    val placeName: String,
    val placeOdia: String,
    val durationMin: Int,
    val activity: String,
    val foodTip: String? = null,
    val lat: Double,
    val lng: Double
)

/**
 * Balasore Heritage Trails & Custom Day-Trip Itinerary Generator Sheet.
 * Offers 4 curated specialized trails and an interactive generator matching user's
 * duration, transport mode, and starting point with authentic culinary stops.
 */
@Composable
fun BalasoreDayTripPlannerSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var selectedDuration by remember { mutableStateOf("FULL_DAY") } // "HALF_DAY", "FULL_DAY", "WEEKEND"
    var selectedTransport by remember { mutableStateOf("CAR") } // "BIKE", "CAR", "AUTO"
    var selectedCircuit by remember { mutableStateOf("DEFENCE") } // "DEFENCE", "FREEDOM", "PILGRIM", "ECO"
    var selectedStartPoint by remember { mutableStateOf("Balasore Station") }

    val itineraries = remember(selectedCircuit, selectedDuration) {
        when (selectedCircuit) {
            "DEFENCE" -> listOf(
                ItineraryStep("08:30 AM", "PXE & DRDO Heritage Display", "ପି.ଏକ୍ସ.ଇ ଓ ଡି.ଆର.ଡି.ଓ ସ୍ମାରକୀ", 60, "Explore vintage artillery guns and the historic defense testing legacy.", "Morning tea at Chandipur Chowk", 21.4650, 87.0100),
                ItineraryStep("10:00 AM", "Chandipur Vanishing Beach", "ଚାନ୍ଦିପୁର ବେଳାଭୂମି", 120, "Witness the world-famous receding sea, walk the seabed during low tide.", "Fresh coconut water near beach", 21.4680, 87.0150),
                ItineraryStep("01:00 PM", "Balaramgadi Fishing Harbor", "ବଳରାମଗଡ଼ି ମତ୍ସ୍ୟ ବନ୍ଦର", 90, "Watch deep-sea motorized trawlers returning with fresh Ilish and prawns.", "Famous Balaramgadi Fish Thali", 21.4720, 87.0500),
                ItineraryStep("03:30 PM", "Dr. APJ Abdul Kalam Memorial Trail", "ଡ଼ଃ ଏପିଜେ ଅବଦୁଲ କଲାମ ସ୍ମୃତି ପଥ", 75, "View coastal missile launch tracking towers overlooking the Bay.", "Evening snacks & biscuits", 21.4550, 87.0200)
            )
            "FREEDOM" -> listOf(
                ItineraryStep("08:30 AM", "Inchudi Salt Satyagraha Peetha", "ଇଞ୍ଚୁଡ଼ି ଲବଣ ସତ୍ୟାଗ୍ରହ ପୀଠ", 90, "India's 2nd Dandi! Historic site where Gopabandhu Choudhury & Rama Devi made contraband salt in 1930.", "Local chuda-chhena breakfast", 21.3650, 86.9500),
                ItineraryStep("11:00 AM", "Srijang Civil Disobedience Firing Site", "ଶ୍ରୀଜଙ୍ଗ ଗୁଳିକାଣ୍ଡ ସ୍ଥଳୀ", 75, "Honor martyrs of the British police firing during the Chowkidari tax boycott.", "Coconut water stall", 21.3400, 86.9200),
                ItineraryStep("01:30 PM", "Chasakhand (Bagha Jatin Battleground)", "ଚାଷାଖଣ୍ଡ ବାଘା ଯତୀନ ସଂଗ୍ରାମ ସ୍ଥଳ", 90, "Memorial of the legendary 1915 trench gunfight between Bagha Jatin and British police along Budhabalanga river.", "Traditional Baleswari Mudhi Mansa", 21.4850, 86.9200),
                ItineraryStep("04:30 PM", "Fakir Mohan Senapati Smruti Mandir, Shanti Kanan", "ଫକୀର ମୋହନ ସ୍ମୃତି ମନ୍ଦିର", 60, "Pay homage to the father of modern Odia literature at his historic residence.", "Nua Bazar Chhena Gaja", 21.4920, 86.9320)
            )
            "PILGRIM" -> listOf(
                ItineraryStep("07:30 AM", "Khirachora Gopinath Temple, Remuna", "କ୍ଷୀରଚୋରା ଗୋପୀନାଥ, ରେମୁଣା", 90, "Ancient 12th-century Vaishnavite shrine famous for Sri Chaitanya Mahaprabhu & Madhavendra Puri.", "Sacred Amruta Keli (Khira) Bhoga", 21.5284, 86.8722),
                ItineraryStep("10:00 AM", "Emami Jagannath Temple", "ଇମାମି ଜଗନ୍ନାଥ ମନ୍ଦିର", 90, "Majestic architectural marvel built in traditional Kalinga style with expansive courtyards.", "Mahaprasad Lunch at Anandabazar", 21.5120, 86.8950),
                ItineraryStep("02:00 PM", "Panchalingeswar Temple", "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ମନ୍ଦିର", 150, "Five rock-cut Shiva Lingams submerged under a perennial natural mountain hill stream.", "Mountain stream herbal tea", 21.4320, 86.7210),
                ItineraryStep("05:30 PM", "Baba Bhusandeswar & Chandaneswar", "ବାବା ଭୂଷଣ୍ଡେଶ୍ୱର ଓ ଚନ୍ଦନେଶ୍ୱର", 90, "Worship at the colossal Asia's largest black granite Shiva Lingam.", "Sweet curd & local peda", 21.6120, 87.4500)
            )
            else -> listOf(
                ItineraryStep("06:30 AM", "Kuldiha Wildlife Sanctuary", "କୁଲଡିହା ବନ୍ୟପ୍ରାଣୀ ଅଭୟାରଣ୍ୟ", 240, "Morning safari through dense Sal forests, view wild Asiatic Elephants, Bison & Giant Squirrels at Rissia Dam.", "Packed forest breakfast", 21.4100, 86.6800),
                ItineraryStep("12:30 PM", "Bichitrapur Tidal Mangrove Sanctuary", "ବିଚିତ୍ରପୁର ମାନ୍ଦାରିଆ ମ୍ୟାଙ୍ଗ୍ରୋଭ", 150, "Guided eco-boating along the mangrove creeks where Subarnarekha River joins Bay of Bengal.", "Local Bhograi crab & fish curry", 21.5800, 87.4200),
                ItineraryStep("04:30 PM", "Talasari-Udaypur Red Crab Beach", "ତାଳସାରୀ ଲାଲ୍ କଙ୍କଡ଼ା ବେଳାଭୂମି", 120, "Walk along quiet casuarina groves and witness millions of bright red ghost crabs at low tide.", "Fresh fried fish stalls", 21.5950, 87.4600)
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("balasore_day_trip_planner_sheet")
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
                        .background(Color(0xFFD97706)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🏛️", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ଐତିହ୍ୟ ପଥ ଓ ଦିବସ ପରିଭ୍ରମଣ" else "Balasore Heritage Trails & Day Trip",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == AppLanguage.ODIA) "କଷ୍ଟମ୍ ଟ୍ରିପ୍ ପ୍ଲାନର୍ ଓ ସ୍ମାର୍ଟ ରୁଟ୍ ମାର୍ଗଦର୍ଶକ" else "Curated Circuits & Step-by-Step Itinerary",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_day_trip_planner_btn")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Circuit Selector Chips
        Text(
            text = "Select Curated Theme / Circuit:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                FilterChip(
                    selected = selectedCircuit == "DEFENCE",
                    onClick = { selectedCircuit = "DEFENCE" },
                    label = { Text("🚀 Defence & Science Trail") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706),
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCircuit == "FREEDOM",
                    onClick = { selectedCircuit = "FREEDOM" },
                    label = { Text("🇮🇳 Freedom & Salt Satyagraha") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706),
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCircuit == "PILGRIM",
                    onClick = { selectedCircuit = "PILGRIM" },
                    label = { Text("🛕 Sanctum & Sacred Temples") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706),
                        selectedLabelColor = Color.White
                    )
                )
            }
            item {
                FilterChip(
                    selected = selectedCircuit == "ECO",
                    onClick = { selectedCircuit = "ECO" },
                    label = { Text("🌿 Wild Eco & Mangroves") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFD97706),
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Trip Customizer Controls (Duration & Vehicle)
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Duration:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Pair("HALF_DAY", "4 Hours"),
                            Pair("FULL_DAY", "Full Day (8h)"),
                            Pair("WEEKEND", "2 Days")
                        ).forEach { (id, label) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedDuration == id) Color(0xFFD97706) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable { selectedDuration = id }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedDuration == id) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transport:", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(
                            Pair("BIKE", "🏍️ Bike"),
                            Pair("CAR", "🚗 Car / Cab"),
                            Pair("AUTO", "🛺 Toto / Auto")
                        ).forEach { (id, label) ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selectedTransport == id) Color(0xFFD97706) else MaterialTheme.colorScheme.surface,
                                modifier = Modifier.clickable { selectedTransport = id }
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (selectedTransport == id) Color.White else MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Trip Summary Banner
        val totalDistanceKm = when (selectedCircuit) {
            "DEFENCE" -> 38
            "FREEDOM" -> 64
            "PILGRIM" -> 88
            else -> 145
        }
        val estimatedBudget = when (selectedTransport) {
            "BIKE" -> "₹250 - ₹400 (Petrol)"
            "CAR" -> "₹1,200 - ₹1,800 (Fuel/Cab)"
            else -> "₹600 - ₹900 (Toto fare)"
        }

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
            border = BorderStroke(1.dp, Color(0xFFFDE68A)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Round Trip", fontSize = 11.sp, color = Color(0xFF92400E))
                    Text("$totalDistanceKm km", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color(0xFFB45309))
                }
                Column {
                    Text("Est. Travel Expense", fontSize = 11.sp, color = Color(0xFF92400E))
                    Text(estimatedBudget, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                }
                Column {
                    Text("Must-Try Delicacy", fontSize = 11.sp, color = Color(0xFF92400E))
                    Text(
                        when (selectedCircuit) {
                            "DEFENCE" -> "Balaramgadi Fish"
                            "FREEDOM" -> "Mudhi Mansa"
                            "PILGRIM" -> "Remuna Khira"
                            else -> "Bhograi Crab"
                        },
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Step by Step Route Timeline
        Text(
            text = "Step-by-Step Itinerary Route:",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        val displayedSteps = if (selectedDuration == "HALF_DAY") itineraries.take(2) else itineraries

        displayedSteps.forEachIndexed { index, step ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Timeline left dot & line
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 12.dp, top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFD97706)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${index + 1}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                    if (index < displayedSteps.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(60.dp)
                                .background(Color(0xFFFDE68A))
                        )
                    }
                }

                // Step Details Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = step.time,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFD97706)
                            )
                            Text(
                                text = "${step.durationMin} mins stop",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = if (language == AppLanguage.ODIA) step.placeOdia else step.placeName,
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = step.activity,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        if (step.foodTip != null) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Restaurant,
                                        contentDescription = null,
                                        modifier = Modifier.size(12.dp),
                                        tint = Color(0xFFB45309)
                                    )
                                    Text(
                                        text = step.foodTip,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF92400E)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedButton(
                            onClick = {
                                val geoUri = Uri.parse("geo:${step.lat},${step.lng}?q=${step.lat},${step.lng}(${Uri.encode(step.placeName)})")
                                val mapIntent = Intent(Intent.ACTION_VIEW, geoUri)
                                context.startActivity(mapIntent)
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Open in Maps", fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Full Circuit Google Maps Launch
        Button(
            onClick = {
                val origin = Uri.encode(selectedStartPoint)
                val destination = Uri.encode(displayedSteps.last().placeName)
                val waypoints = displayedSteps.drop(1).dropLast(1).joinToString("|") { Uri.encode(it.placeName) }
                val mapsUrl = if (waypoints.isNotEmpty()) {
                    "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination&waypoints=$waypoints&travelmode=driving"
                } else {
                    "https://www.google.com/maps/dir/?api=1&origin=$origin&destination=$destination&travelmode=driving"
                }
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(mapsUrl))
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.Route, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Launch Entire Day-Trip Route in Google Maps", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedButton(
            onClick = onClose,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().height(44.dp)
        ) {
            Text("Close Planner")
        }
    }
}
