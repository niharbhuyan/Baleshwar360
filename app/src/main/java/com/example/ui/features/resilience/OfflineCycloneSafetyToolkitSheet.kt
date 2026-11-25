package com.example.ui.features.resilience

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Water
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.core.content.ContextCompat
import com.example.data.daily.DailyBalasorePulse
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate400
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.OceanBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class OfflineShelterInfo(
    val id: String,
    val name: String,
    val nameOd: String,
    val block: String,
    val capacityPersons: Int,
    val elevationMeters: Float,
    val landmark: String,
    val contactNumber: String
)

data class OfflineSurvivalItem(
    val id: String,
    val title: String,
    val titleOd: String,
    var isChecked: Boolean = false
)

private data class RiverThresholdGauge(
    val riverNameEn: String,
    val riverNameOd: String,
    val station: String,
    val currentLevelM: Float,
    val warningLevelM: Float,
    val dangerLevelM: Float
)

private const val OFFLINE_SURVIVAL_PREFS = "balasore_offline_survival_pack_v1"

private fun resolveLastKnownGpsPair(context: Context): Pair<Double, Double> {
    val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    val hasCoarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    if (hasFine || hasCoarse) {
        try {
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            for (provider in providers) {
                @Suppress("MissingPermission")
                val loc = lm?.getLastKnownLocation(provider)
                if (loc != null) {
                    return loc.latitude to loc.longitude
                }
            }
        } catch (_: Exception) {}
    }
    return 21.4934 to 86.9336 // Balasore District HQ fallback coordinates
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineCycloneSafetyToolkitSheet(
    language: AppLanguage,
    dailyPulse: DailyBalasorePulse,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    var isStrobeActive by remember { mutableStateOf(false) }
    var selectedBlock by remember { mutableStateOf("All") }

    val prefs = remember { context.getSharedPreferences(OFFLINE_SURVIVAL_PREFS, Context.MODE_PRIVATE) }
    var isPackDownloaded by remember { mutableStateOf(prefs.getBoolean("pack_saved", false)) }
    var packSavedTimestamp by remember { mutableStateOf(prefs.getString("pack_saved_at", "") ?: "") }

    val infiniteTransition = rememberInfiniteTransition(label = "strobe")
    val strobeColor by infiniteTransition.animateColor(
        initialValue = Color(0xFFEF4444),
        targetValue = Color(0xFFF59E0B),
        animationSpec = infiniteRepeatable(
            animation = tween<Color>(250, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "strobe_color"
    )

    val shelters = remember {
        listOf(
            OfflineShelterInfo("sh_remuna", "Remuna Block Multi-Purpose Cyclone Shelter", "ରେମୁଣା ବ୍ଲକ ବହୁମୁଖୀ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ", "Remuna", 1050, 12.4f, "Near Remuna Tehsil & Golei Road", "06782-255107"),
            OfflineShelterInfo("sh_soro", "Soro Coastal & Flood Relief Shelter", "ସୋରୋ ଉପକୂଳ ଓ ବନ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ", "Soro", 1150, 10.8f, "Near Soro College Road & Block Office", "06788-220144"),
            OfflineShelterInfo("sh_jaleswar", "Jaleswar Subarnarekha Flood Shelter", "ଜଳେଶ୍ୱର ସୁବର୍ଣ୍ଣରେଖା ବନ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ", "Jaleswar", 1300, 9.6f, "Near Rajghat High School, Jaleswar", "06781-222340"),
            OfflineShelterInfo("sh_basta", "Basta Block Emergency Cyclone Center", "ବସ୍ତା ବ୍ଲକ ଜରୁରୀ ବାତ୍ୟା ଆଶ୍ରୟକେନ୍ଦ୍ର", "Basta", 980, 11.2f, "Near Basta CHC & Bus Stand", "06781-230210"),
            OfflineShelterInfo("sh_nilagiri", "Nilagiri Foothills Community Shelter", "ନୀଳଗିରି ସମୁଦାୟ ବିପର୍ଯ୍ୟୟ ଆଶ୍ରୟସ୍ଥଳୀ", "Nilagiri", 860, 24.5f, "Near Nilagiri Sub-Collector Office", "06782-233201"),
            OfflineShelterInfo("sh_chandipur", "Chandipur Multi-Purpose Cyclone Shelter", "ଚାନ୍ଦିପୁର ବହୁମୁଖୀ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ", "Balasore Sadar", 1200, 8.5f, "Near Chandipur High School & Beach Road", "06782-262244"),
            OfflineShelterInfo("sh_talasari", "Talasari Estuary Cyclone Center", "ତାଳସାରୀ ମୁହାଣ ବାତ୍ୟା ଆଶ୍ରୟକେନ୍ଦ୍ର", "Bhograi", 1100, 9.0f, "Near Talasari Forest Checkpost", "06781-233400")
        )
    }

    val riverGauges = remember {
        listOf(
            RiverThresholdGauge(
                riverNameEn = "Subarnarekha River",
                riverNameOd = "ସୁବର୍ଣ୍ଣରେଖା ନଦୀ",
                station = "Rajghat (Jaleswar / Basta)",
                currentLevelM = 8.45f,
                warningLevelM = 9.45f,
                dangerLevelM = 10.36f
            ),
            RiverThresholdGauge(
                riverNameEn = "Budhabalanga River",
                riverNameOd = "ବୁଢ଼ାବଳଙ୍ଗ ନଦୀ",
                station = "NH-5 Govindpur (Balasore / Remuna)",
                currentLevelM = 5.80f,
                warningLevelM = 7.21f,
                dangerLevelM = 8.13f
            )
        )
    }

    val blocks = listOf("All", "Remuna", "Soro", "Jaleswar", "Basta", "Nilagiri")
    val filteredShelters = remember(selectedBlock) {
        if (selectedBlock == "All") shelters else shelters.filter { it.block.equals(selectedBlock, ignoreCase = true) }
    }

    var survivalChecklist by remember {
        mutableStateOf(
            listOf(
                OfflineSurvivalItem("c_1", "Battery Torch & Extra Cells", "ବ୍ୟାଟେରୀ ଲାଇଟ୍ ଓ ଅତିରିକ୍ତ ସେଲ୍", true),
                OfflineSurvivalItem("c_2", "Dry Food: Chuda, Mudhi & Biscuits (3 Days)", "ଶୁଖିଲା ଖାଦ୍ୟ: ଚୁଡ଼ା, ମୁଢ଼ି ଓ ବିସ୍କୁଟ", true),
                OfflineSurvivalItem("c_3", "Boiled Water Bottles & Chlorine Halazone Tablets", "ଫୁଟା ପାଣି ଓ ହାଲାଜୋନ୍ ବଟିକା", true),
                OfflineSurvivalItem("c_4", "Important Documents Wrapped in Waterproof Polybag", "ଜରୁରୀ କାଗଜପତ୍ର ପଲିଥିନ୍ ସୁରକ୍ଷିତ", false),
                OfflineSurvivalItem("c_5", "Charged Power Bank & Emergency FM Radio", "ଚାର୍ଜ୍ ଥିବା ପାୱାର ବ୍ୟାଙ୍କ ଓ ରେଡିଓ", false),
                OfflineSurvivalItem("c_6", "Basic First-Aid: Bandages, Antiseptic, ORS", "ପ୍ରାଥମିକ ଚିକିତ୍ସା ସରଞ୍ଜାମ ଓ ଓଆରଏସ୍", false)
            )
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("offline_cyclone_safety_toolkit_sheet"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFEF3C7),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text("🛡️", fontSize = 24.sp)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ଅଫଲାଇନ୍ ବାତ୍ୟା ଓ ବନ୍ୟା ସୁରକ୍ଷା ପ୍ୟାକ୍" else "Offline Cyclone & Flood Survival Pack",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900
                        )
                        Text(
                            text = if (isOdia) "ରେମୁଣା, ସୋରୋ, ଜଳେଶ୍ୱର, ବସ୍ତା, ନୀଳଗିରି • SMS SOS" else "Remuna, Soro, Jaleswar, Basta & Nilagiri • Offline SMS SOS",
                            style = MaterialTheme.typography.bodySmall,
                            color = BentoSlate500
                        )
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("btn_close_cyclone_toolkit_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = BentoSlate600)
                }
            }
        }

        // One-Tap Offline Download Survival Pack Card
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isPackDownloaded) Color(0xFFECFDF5) else Color(0xFFF0F9FF)
                ),
                border = BorderStroke(1.2.dp, if (isPackDownloaded) Color(0xFF10B981) else OceanBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_survival_pack_download_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPackDownloaded) {
                                if (isOdia) "✅ ଅଫଲାଇନ୍ ପ୍ୟାକ୍ ସେଭ୍ ହୋଇଛି" else "✅ Offline Survival Pack Saved"
                            } else {
                                if (isOdia) "📦 ୧-ଟ୍ୟାପ୍ ଅଫଲାଇନ୍ ସୁରକ୍ଷା ପ୍ୟାକ୍" else "📦 1-Tap Offline Survival Pack"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isPackDownloaded) Color(0xFF065F46) else Color(0xFF0C4A6E)
                        )
                        Text(
                            text = if (isPackDownloaded && packSavedTimestamp.isNotBlank()) {
                                "Cached ${shelters.size} block shelters + 2 river gauges ($packSavedTimestamp)"
                            } else {
                                "Save Remuna, Soro, Jaleswar, Basta & Nilagiri shelters + river danger thresholds offline."
                            },
                            fontSize = 11.sp,
                            color = BentoSlate600
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val now = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date())
                            val serialized = shelters.joinToString("\n") { "${it.block}: ${it.name} (${it.contactNumber})" }
                            prefs.edit()
                                .putBoolean("pack_saved", true)
                                .putString("pack_saved_at", now)
                                .putString("shelters_cache", serialized)
                                .apply()
                            isPackDownloaded = true
                            packSavedTimestamp = now
                            Toast.makeText(
                                context,
                                "✅ Saved all 5 block shelters & river danger thresholds for offline use!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isPackDownloaded) Color(0xFF059669) else OceanBlue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                        modifier = Modifier.testTag("btn_download_offline_survival_pack")
                    ) {
                        Icon(
                            imageVector = if (isPackDownloaded) Icons.Default.CheckCircle else Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (isPackDownloaded) "Updated" else "Download",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Play-Compliant Offline SMS Fallback SOS (Zero SEND_SMS permission - opens default SMS app pre-filled with GPS)
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
                border = BorderStroke(1.2.dp, Color(0xFFEF4444)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_sms_fallback_sos_card")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "📡 ନେଟୱର୍କ ନଥିଲେ SMS GPS SOS" else "📡 No-Data SMS Fallback SOS",
                                fontSize = 13.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                            Text(
                                text = if (isOdia) "ମୋବାଇଲ୍ ଡାଟା ବନ୍ଦ ଥିଲେ GPS ଠିକଣା ସହ SMS ପଠାନ୍ତୁ (1077)" else "Works when mobile data is down — drafts an emergency SMS with your GPS coordinates to 1077.",
                                fontSize = 11.sp,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val (lat, lon) = resolveLastKnownGpsPair(context)
                            val latStr = String.format(Locale.US, "%.5f", lat)
                            val lonStr = String.format(Locale.US, "%.5f", lon)
                            val smsBody = "SOS EMERGENCY (Balasore 360): Need immediate cyclone/flood rescue assistance. " +
                                "My GPS: $latStr, $lonStr (https://maps.google.com/?q=$latStr,$lonStr)"
                            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("smsto:1077")
                                putExtra("sms_body", smsBody)
                            }
                            runCatching {
                                context.startActivity(smsIntent)
                            }.onFailure {
                                Toast.makeText(context, "Unable to open SMS app on this device.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("btn_offline_sms_gps_sos")
                    ) {
                        Icon(Icons.Default.Sms, contentDescription = null, modifier = Modifier.size(17.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia) "GPS ଠିକଣା ସହ ଜରୁରୀ SMS ପଠାନ୍ତୁ (1077)" else "Send GPS Coordinates via Offline SMS (1077)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // SOS High Visibility Strobe Beacon + Dial 1077 / 112
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isStrobeActive) strobeColor else Color(0xFFFEF2F2)
                ),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isStrobeActive) "🚨 SOS SEARCH & RESCUE STROBE ON" else "Emergency SOS Strobe Beacon",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isStrobeActive) Color.White else Color(0xFF991B1B)
                            )
                            Text(
                                text = "High-visibility flashing beacon for aerial & ground search teams.",
                                fontSize = 11.sp,
                                color = if (isStrobeActive) Color.White.copy(alpha = 0.9f) else BentoSlate600
                            )
                        }
                        Button(
                            onClick = { isStrobeActive = !isStrobeActive },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isStrobeActive) Color.Black else Color(0xFFDC2626)
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(if (isStrobeActive) "STOP" else "ACTIVATE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:1077")))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dial 1077", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                        OutlinedButton(
                            onClick = {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:112")))
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, OceanBlue),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Shield, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Dial 112", fontSize = 11.5.sp, color = OceanBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Subarnarekha & Budhabalanga River Danger-Level Thresholds
        item {
            Text(
                text = if (isOdia) "🌊 ସୁବର୍ଣ୍ଣରେଖା ଓ ବୁଢ଼ାବଳଙ୍ଗ ବିପଦ ସଙ୍କେତ ସ୍ତର" else "🌊 Subarnarekha & Budhabalanga Danger Thresholds",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BentoSlate800
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                riverGauges.forEach { gauge ->
                    val ratio = (gauge.currentLevelM / gauge.dangerLevelM).coerceIn(0f, 1f)
                    val isWarning = gauge.currentLevelM >= gauge.warningLevelM
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                        border = BorderStroke(1.dp, if (isWarning) Color(0xFFF59E0B) else BentoSlate200),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Water, contentDescription = null, tint = OceanBlue, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (isOdia) gauge.riverNameOd else gauge.riverNameEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = BentoSlate900
                                    )
                                }
                                Text(
                                    text = "${gauge.currentLevelM}m / Danger ${gauge.dangerLevelM}m",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isWarning) Color(0xFFD97706) else Color(0xFF0369A1)
                                )
                            }
                            Text(
                                text = "Station: ${gauge.station} • Warning: ${gauge.warningLevelM}m | Danger: ${gauge.dangerLevelM}m",
                                fontSize = 10.5.sp,
                                color = BentoSlate600
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { ratio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isWarning) Color(0xFFEF4444) else OceanBlue,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }

        // Block-Wise Cyclone Shelters Directory
        item {
            Text(
                text = if (isOdia) "ବ୍ଲକ ଅନୁଯାୟୀ ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ (ଅଫଲାଇନ୍)" else "Block-Wise Cyclone Shelters (Offline Directory)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BentoSlate800
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(blocks) { block ->
                    FilterChip(
                        selected = selectedBlock == block,
                        onClick = { selectedBlock = block },
                        label = { Text(block, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanBlue,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
        }

        items(filteredShelters, key = { it.id }) { sh ->
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                border = BorderStroke(1.dp, BentoSlate200),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) sh.nameOd else sh.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = BentoSlate900,
                            modifier = Modifier.weight(1f)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Cap: ${sh.capacityPersons}",
                                fontSize = 11.sp,
                                color = Color(0xFF15803D),
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = {
                                    val shelterTitle = if (isOdia) sh.nameOd else sh.name
                                    val shareText = "🛡️ BALASORE CYCLONE SHELTER\n📍 $shelterTitle (${sh.block})\nLandmark: ${sh.landmark}\nCapacity: ${sh.capacityPersons} | Elev: ${sh.elevationMeters}m\nHelpline: ${sh.contactNumber} / 1077"
                                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                    }
                                    context.startActivity(Intent.createChooser(sendIntent, "Share Cyclone Shelter"))
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share Shelter Info", tint = OceanBlue, modifier = Modifier.size(15.dp))
                            }
                        }
                    }
                    Text(
                        text = "📍 ${sh.landmark} (${sh.block}) • Elev: ${sh.elevationMeters}m • Tel: ${sh.contactNumber}",
                        fontSize = 11.sp,
                        color = BentoSlate600
                    )
                }
            }
        }

        // Survival Checklist Section
        item {
            Text(
                text = if (isOdia) "ଜରୁରୀ ସାମଗ୍ରୀ ତାଲିକା (୭୨-ଘଣ୍ଟା କିଟ୍)" else "Offline 72-Hour Survival Checklist",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = BentoSlate800
            )
            Spacer(modifier = Modifier.height(6.dp))
            survivalChecklist.forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            survivalChecklist = survivalChecklist.map {
                                if (it.id == item.id) it.copy(isChecked = !it.isChecked) else it
                            }
                        }
                        .padding(vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = item.isChecked,
                        onCheckedChange = { checked ->
                            survivalChecklist = survivalChecklist.map {
                                if (it.id == item.id) it.copy(isChecked = checked) else it
                            }
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOdia) item.titleOd else item.title,
                        fontSize = 12.sp,
                        color = if (item.isChecked) BentoSlate400 else BentoSlate800,
                        fontWeight = if (item.isChecked) FontWeight.Normal else FontWeight.Medium
                    )
                }
            }
        }
    }
}
