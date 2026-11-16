package com.example.ui.features.nature

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
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

data class PhotoSpotItem(
    val id: String,
    val nameEn: String,
    val nameOd: String,
    val category: String, // "COASTAL", "HERITAGE", "WILD"
    val bestTime: String,
    val gearTip: String,
    val droneStatus: String,
    val description: String,
    val lat: Double,
    val lng: Double,
    val iconEmoji: String
)

/**
 * Balasore Hidden Gems & Photography Spots Checklist Sheet.
 * Allows users to track spots photographed, unlock explorer achievement badges,
 * and view professional lighting/gear advice and drone flight regulations.
 */
@Composable
fun BalasorePhotoSpotsChecklistSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val spots = remember {
        listOf(
            PhotoSpotItem(
                id = "SPOT_1",
                nameEn = "Subarnarekha Estuary Sunrise",
                nameOd = "ସୁବର୍ଣ୍ଣରେଖା ମୁହାଣ ସୂର୍ଯ୍ୟୋଦୟ",
                category = "COASTAL",
                bestTime = "05:30 AM - 06:15 AM",
                gearTip = "Telephoto or 50mm Prime with CPL filter for rich gold river reflections.",
                droneStatus = "Permitted with local caution (Avoid flying over coast guard post).",
                description = "Traditional wooden boats anchored on silty sandbanks as golden dawn breaks over the Bay.",
                lat = 21.5750,
                lng = 87.4050,
                iconEmoji = "🌅"
            ),
            PhotoSpotItem(
                id = "SPOT_2",
                nameEn = "Talasari Red Ghost Crab Carpet",
                nameOd = "ତାଳସାରୀ ଲାଲ୍ କଙ୍କଡ଼ା ଗାଲିଚା",
                category = "COASTAL",
                bestTime = "06:30 AM or 04:30 PM (Low Tide)",
                gearTip = "Macro / Low-angle wide lens. Lie flat on ground level with sand.",
                droneStatus = "Permitted (Great top-down contrast of crimson against white sand).",
                description = "Millions of bright red crabs emerge across glistening intertidal sands creating a living red carpet.",
                lat = 21.5950,
                lng = 87.4600,
                iconEmoji = "🦀"
            ),
            PhotoSpotItem(
                id = "SPOT_3",
                nameEn = "Chandipur Vanishing Sea Mirage",
                nameOd = "ଚାନ୍ଦିପୁର ମୃଗତୃଷ୍ଣା ଓ ଜଳ ସୀମା",
                category = "COASTAL",
                bestTime = "12:00 PM - 02:00 PM (Midday Low Tide)",
                gearTip = "ND Filter + Polarizer to capture the heat ripples over exposed wet seabed.",
                droneStatus = "STRICT NO-FLY ZONE (Adjacent to DRDO ITR & PXE Military Testing Range).",
                description = "Water receding up to 5 km over the horizon with solitary fishing nets and mangrove stakes.",
                lat = 21.4680,
                lng = 87.0160,
                iconEmoji = "🌊"
            ),
            PhotoSpotItem(
                id = "SPOT_4",
                nameEn = "Panchalingeswar Perennial Stream",
                nameOd = "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ପାହାଡ଼ି ଝରଣା",
                category = "WILD",
                bestTime = "07:00 AM - 09:30 AM",
                gearTip = "Tripod + ND1000 filter for 2-3 sec silky water motion blur.",
                droneStatus = "Permitted below tree canopy, take care of thick Sal foliage.",
                description = "Perennial hill stream gushing over moss-draped boulders concealing five rock-cut submerged Shiva lingams.",
                lat = 21.4320,
                lng = 86.7210,
                iconEmoji = "⛰️"
            ),
            PhotoSpotItem(
                id = "SPOT_5",
                nameEn = "13th-Century Raibania Fort Ruins",
                nameOd = "ତ୍ରୟୋଦଶ ଶତାବ୍ଦୀ ରାଇବଣିଆ ଦୁର୍ଗ",
                category = "HERITAGE",
                bestTime = "03:30 PM - 05:00 PM (Warm afternoon side-light)",
                gearTip = "Ultra-wide lens (16-35mm) to capture colossal laterite moat walls and stone arches.",
                droneStatus = "Permitted (Spectacular aerial view of the ancient moat and ramparts).",
                description = "The largest medieval fort complex in Eastern India built by Eastern Ganga monarch Langula Narasimha Deva.",
                lat = 21.9160,
                lng = 87.1950,
                iconEmoji = "🏰"
            ),
            PhotoSpotItem(
                id = "SPOT_6",
                nameEn = "Remuna Temple Pond Dusk Reflections",
                nameOd = "ରେମୁଣା ମନ୍ଦିର ପୋଖରୀ ସନ୍ଧ୍ୟା ଆଳତୀ",
                category = "HERITAGE",
                bestTime = "05:45 PM - 06:45 PM (Blue Hour)",
                gearTip = "Fast f/1.8 lens for low-light diya oil lamps and temple spire water reflections.",
                droneStatus = "Prohibited over active temple sanctum; allowed outside perimeter.",
                description = "Deep peaceful reflection of the Khirachora Gopinath sanctum towers during evening Sandhya Aarti.",
                lat = 21.5284,
                lng = 86.8722,
                iconEmoji = "🛕"
            ),
            PhotoSpotItem(
                id = "SPOT_7",
                nameEn = "Bichitrapur Mangrove Creek Reflections",
                nameOd = "ବିଚିତ୍ରପୁର ହେନ୍ତାଳବଣ କେନାଲ",
                category = "WILD",
                bestTime = "08:00 AM - 10:30 AM",
                gearTip = "Zoom lens with image stabilization for shooting from moving country boats.",
                droneStatus = "Permitted under Forest Department boat guide supervision.",
                description = "Dense mangrove forest canals with arched roots and resting egrets, kingfishers and mudskippers.",
                lat = 21.5800,
                lng = 87.4200,
                iconEmoji = "🛶"
            ),
            PhotoSpotItem(
                id = "SPOT_8",
                nameEn = "Kasafal Virgin Casuarina Beach Dunes",
                nameOd = "କଷାଫଳ ନିର୍ଜନ ଝାଉଁବଣ ବେଳାଭୂମି",
                category = "COASTAL",
                bestTime = "04:00 PM - 05:45 PM (Sunset)",
                gearTip = "Silhouette portrait setup with setting sun filtering through casuarina needles.",
                droneStatus = "Permitted without restrictions (Vast open coastline).",
                description = "Secluded pristine sandy expanse with dramatic sea-washed driftwood and pristine tidal channels.",
                lat = 21.5280,
                lng = 87.1230,
                iconEmoji = "🌲"
            )
        )
    }

    // Checked status list
    val checkedSpots = remember {
        mutableStateListOf(false, false, false, false, false, false, false, false)
    }

    val completedCount = checkedSpots.count { it }
    val progress = completedCount.toFloat() / spots.size.toFloat()

    // Count by categories to unlock badges
    val coastalDone = spots.indices.count { checkedSpots[it] && spots[it].category == "COASTAL" }
    val heritageDone = spots.indices.count { checkedSpots[it] && spots[it].category == "HERITAGE" }
    val wildDone = spots.indices.count { checkedSpots[it] && spots[it].category == "WILD" }

    val hasCoastalBadge = coastalDone >= 3
    val hasHeritageBadge = heritageDone >= 2
    val hasWildBadge = wildDone >= 2
    val hasMasterBadge = completedCount == spots.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("balasore_photo_spots_checklist_sheet")
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
                        .background(Color(0xFF8B5CF6)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📸", fontSize = 22.sp)
                }
                Column {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ଫଟୋଗ୍ରାଫି ସ୍ପଟ୍ ଓ ବ୍ୟାଜ୍" else "Balasore Photo Spots & Badges",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (language == AppLanguage.ODIA) "୮ଟି ମନୋରମ ସ୍ଥଳୀ, କ୍ୟାମେରା ଟିପ୍ସ ଓ ଡ୍ରୋନ୍ ନିୟମ" else "8 Scenic Spots, Pro Tips & Drone Rules",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.testTag("close_photo_spots_btn")
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Progress Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF5F3FF)),
            border = BorderStroke(1.dp, Color(0xFFDDD6FE)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Explorer Progress: ${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF5B21B6)
                    )
                    Text(
                        text = "$completedCount of ${spots.size} Spots Captured",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFF7C3AED)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = Color(0xFF8B5CF6),
                    trackColor = Color(0xFFEDE9FE),
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Achievement Badges Row
        Text(
            text = "Achievement Badges:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Triple("🌊 Coastal Pioneer", hasCoastalBadge, "3 Coastal"),
                Triple("🏛️ Heritage Custodian", hasHeritageBadge, "2 Heritage"),
                Triple("🌿 Wild Scout", hasWildBadge, "2 Eco"),
                Triple("🏆 Master 360", hasMasterBadge, "All 8")
            ).forEach { (title, unlocked, req) ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (unlocked) Color(0xFFEDE9FE) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, if (unlocked) Color(0xFF8B5CF6) else Color.Transparent),
                    modifier = Modifier.weight(1f)
                ) {
                    Column(
                        modifier = Modifier.padding(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (unlocked) "⭐" else "🔒",
                            fontSize = 14.sp
                        )
                        Text(
                            text = title.substringAfter(" "),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (unlocked) Color(0xFF5B21B6) else Color.Gray,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Spots Checklist
        spots.forEachIndexed { index, spot ->
            val isChecked = checkedSpots[index]

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isChecked) Color(0xFFF5F3FF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                border = BorderStroke(1.dp, if (isChecked) Color(0xFF8B5CF6) else Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp)
                    .clickable {
                        checkedSpots[index] = !isChecked
                        if (!isChecked) {
                            Toast.makeText(context, "Marked '${spot.nameEn}' as Captured! 📸", Toast.LENGTH_SHORT).show()
                        }
                    }
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(spot.iconEmoji, fontSize = 24.sp)
                            Column {
                                Text(
                                    text = spot.nameEn,
                                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = spot.nameOd,
                                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                    color = Color(0xFF7C3AED)
                                )
                            }
                        }
                        Icon(
                            imageVector = if (isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = if (isChecked) Color(0xFF7C3AED) else MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = spot.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFFD97706))
                        Text("Best Time: ${spot.bestTime}", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF92400E))
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color(0xFF7C3AED))
                        Text("Pro Tip: ${spot.gearTip}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = if (spot.droneStatus.contains("STRICT NO-FLY")) Color(0xFFDC2626) else Color(0xFF16A34A)
                        )
                        Text(
                            "Drone Rules: ${spot.droneStatus}",
                            fontSize = 11.sp,
                            fontWeight = if (spot.droneStatus.contains("STRICT NO-FLY")) FontWeight.Bold else FontWeight.Normal,
                            color = if (spot.droneStatus.contains("STRICT NO-FLY")) Color(0xFFDC2626) else MaterialTheme.colorScheme.outline
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedButton(
                        onClick = {
                            val geoUri = Uri.parse("geo:${spot.lat},${spot.lng}?q=${spot.lat},${spot.lng}(${Uri.encode(spot.nameEn)})")
                            context.startActivity(Intent(Intent.ACTION_VIEW, geoUri))
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Get Vantage Point Directions", fontSize = 11.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onClose,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED)),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("Done / Close Photo Checklist", fontWeight = FontWeight.Bold)
        }
    }
}
