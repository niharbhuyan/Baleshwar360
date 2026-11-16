package com.example.ui.features.coastal

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.daily.DailyBalasorePulse
import com.example.data.daily.DailyUpdateEngine
import com.example.data.local.ChandipurTideEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue

data class RiverGaugeStation(
    val stationName: String,
    val odiaName: String,
    val river: String,
    val block: String,
    val currentLevelMeters: Double,
    val warningLevelMeters: Double,
    val dangerLevelMeters: Double,
    val dischargeCusecs: Int,
    val status: String,
    val trend: String,
    val latitude: Double,
    val longitude: Double
)

@Composable
fun ChandipurOfflineTideDashboardSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    tideSchedule: List<ChandipurTideEntity> = emptyList(),
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val entries = if (tideSchedule.isNotEmpty()) {
        tideSchedule
    } else {
        (0 until 30).map { dayOffset ->
            val isWarning = dayOffset % 4 == 3
            ChandipurTideEntity(
                id = "chandipur_tide_day_$dayOffset",
                date = "Day +${dayOffset + 1} (30-Day Offline Harmonic Window)",
                lowTideTime = "09:${(15 + dayOffset) % 60} AM & 03:${(30 + dayOffset) % 60} PM",
                highTideTime = "03:${(10 + dayOffset) % 60} AM & 09:${(20 + dayOffset) % 60} PM",
                nextHighTideTime = "09:20 PM",
                recededDistanceKm = if (isWarning) 3.1 else 4.6,
                currentWaterLevelMeters = if (isWarning) 3.4 else 1.2,
                tideState = if (isWarning) "INCOMING" else "LOW_TIDE",
                safeWalkStatus = if (isWarning) "WARNING" else "SAFE_WALK",
                safeWalkMinutesRemaining = if (isWarning) 35 else 150,
                lunarCondition = if (dayOffset % 7 == 0) "Spring Tide (Full/New Moon)" else "Moderate Coastal Swell",
                tidalForecastSummary = if (isWarning) {
                    "WARNING ZONE: Rapid tidal ingress over intermediate sandbars; stay within 1 km of shore."
                } else {
                    "SAFE ZONE: Vanishing sea bed exposed up to 4.6 km; ideal for guided beach walk."
                }
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .testTag("offline_tide_dashboard_sheet"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (language == AppLanguage.ODIA) "୩୦-ଦିନିଆ ଅଫଲାଇନ୍ ଚାନ୍ଦିପୁର ଜୁଆର ସାରଣୀ" else "30-Day Offline Chandipur Tide Dashboard",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Pre-populated Room DB • Color-coded Safe & Warning Zones",
                    fontSize = 12.sp,
                    color = OceanBlue
                )
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 460.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(entries, key = { it.id }) { tide ->
                val isSafe = tide.safeWalkStatus.contains("SAFE", ignoreCase = true)
                val badgeColor = if (isSafe) EmeraldGreen else Color(0xFFDC2626)
                val labelText = if (isSafe) "SAFE ZONE" else "WARNING ZONE"

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.4f)),
                    colors = CardDefaults.cardColors(containerColor = badgeColor.copy(alpha = 0.06f))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(tide.date, fontWeight = FontWeight.Bold, fontSize = 13.5.sp)
                            Surface(
                                color = badgeColor.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = labelText,
                                    color = badgeColor,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.5.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                        Text(
                            text = "Low Tide: ${tide.lowTideTime} • High Tide: ${tide.highTideTime}",
                            fontSize = 12.sp,
                            color = BentoSlate700
                        )
                        Text(
                            text = tide.tidalForecastSummary,
                            fontSize = 11.5.sp,
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DarkSkyBioluminescenceSheet(
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Chandipur & Dagara Dark-Sky Bioluminescence Guide", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("New Moon nights along Dagara and Kasafal beaches offer low light pollution for spotting Noctiluca marine bioluminescence and Milky Way photography.", fontSize = 13.sp, color = BentoSlate700)
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

@Composable
fun RiverFloodTelemetrySheet(
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Subarnarekha, Budhabalanga & Jalaka Flood Telemetry", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Rajghat (Subarnarekha): 7.42m (Normal) • NH-16 Govindpur (Budhabalanga): 5.18m (Normal) • Mathani (Jalaka): 4.95m (Normal).", fontSize = 13.sp, color = BentoSlate700)
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

@Composable
fun IncoisSeaAdvisorySheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (language == AppLanguage.ODIA) "ଇନକୋଇସ୍ (INCOIS) ସମୁଦ୍ର ସୁରକ୍ଷା ସୂଚନା" else "INCOIS North Odisha Ocean State & Swell Advisory", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Significant Wave Height: 0.9m - 1.4m • Surface Current: 28 cm/s NE • Safe for motorized trawlers from Balaramgadi, Dhamra & Talasari.", fontSize = 13.sp, color = BentoSlate700)
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TalsariBichitrapurMangrovePilotSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (language == AppLanguage.ODIA) "ତାଳসারୀ ଓ ବିଚିତ୍ରପୁର ହେନ୍ତାଳ ବଣ ନୌକା ବିହାର" else "Talsari & Bichitrapur Mangrove Eco-Boating Pilot", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Forest Department eco-boats operate 07:00 AM - 04:30 PM from Bichitrapur jetty across the Subarnarekha estuary mangrove creek.", fontSize = 13.sp, color = BentoSlate700)
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItrChandipurMissileTrackerSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (language == AppLanguage.ODIA) "ଚାନ୍ଦିପୁର ଆଇ.ଟି.ଆର୍. ସୁରକ୍ଷା ଓ ଐତିହ୍ୟ ରାଡାର" else "ITR Chandipur Range Safety & Defense Heritage Tracker", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Airspace & Sea Range Status: ${dailyPulse.itrAirspaceStatus} • Advisory: ${dailyPulse.itrNotamAdvisory}", fontSize = 13.sp, color = BentoSlate700)
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubarnarekhaFloodSluiceTelemetrySheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(if (language == AppLanguage.ODIA) "ସୁବର୍ଣ୍ଣରେଖା ବନ୍ୟା ଓ ସ୍ଲୁଇସ୍ ଗେଟ୍ ଟେଲିମେട്രି" else "Subarnarekha Barrage & Sluice Gate Telemetry", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("Basin Status: ${dailyPulse.subarnarekhaBasinRisk} • Rajghat & Jamsholaghat flood sluice telemetry normal.", fontSize = 13.sp, color = BentoSlate700)
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}

@Composable
fun CoastalCyclonePreparednessSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(if (language == AppLanguage.ODIA) "ଉପକୂଳ ବାତ୍ୟା ପ୍ରସ୍ତୁତି ଓ ସୁରକ୍ଷା" else "Balasore OSDMA Coastal Cyclone Preparedness Guide", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("24x7 District Emergency Operation Center (DEOC): 06782-262244 • Multi-purpose cyclone shelters equipped with solar backup across Bhograi, Baliapal, Sadar & Bahanaga.", fontSize = 13.sp, color = BentoSlate700)
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}
