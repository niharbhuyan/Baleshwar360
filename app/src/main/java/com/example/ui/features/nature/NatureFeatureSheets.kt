package com.example.ui.features.nature

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.daily.DailyBalasorePulse
import com.example.data.daily.DailyUpdateEngine
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoSlate700

@Composable
fun KuldihaElephantCorridorSheet(
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Kuldiha - Hadagarh - Similipal Elephant Corridor", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Live Forest Range Advisory: Green (Clear). Observe 20 km/h speed limit near Nilagiri-Panchalingeswar ghat road between 05:30 PM and 06:00 AM.", fontSize = 13.sp, color = BentoSlate700)
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubarnarekhaSluiceFloodRadarSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onClose) {
        Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ସୁବର୍ଣ୍ଣରେଖା ଓ ବୁଢ଼ାବଳଙ୍ଗ ବନ୍ୟା ରାଡାର" else "Subarnarekha & Budhabalanga Sluice Flood Radar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Rajghat Gauge: 7.42m (Danger: 10.36m) • Govindpur Bridge: 5.18m (Danger: 8.13m) • Mathani Jalaka: 4.95m (Danger: 6.50m).",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = BentoSlate700
                )
            }
            Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HorseshoeCrabIntertidalProtectionSheet(
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    language: AppLanguage = AppLanguage.ENGLISH,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ଚାନ୍ଦିପୁର ଅଶ୍ୱଖୁରା କଙ୍କଡ଼ା ସଂରକ୍ଷଣ" else "Chandipur Living Fossil Horseshoe Crab Conservation",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = " protected 400-million-year-old Tachypleus gigas breeding zone along Chandipur, Balaramgadi & Khandia estuary sandbars. Report stranded crabs to FMU Marine Biology Cell.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = BentoSlate700
                )
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuldihaEcoCorridorFireRadarSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (language == AppLanguage.ODIA) "କୁଳଡିହା ବନାଗ୍ନି ଓ ଇକୋ-କରିଡର ରାଡାର" else "Kuldiha Sanctuary Forest Fire & Eco-Corridor Radar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
            Text("Zero active thermal hotspots across Kuldiha, Devgiri & Swarnachuda Reserve Forest beats. Vana Surakshya Samiti squads on patrol.", fontSize = 13.sp, color = BentoSlate700)
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}

@Composable
fun KuldihaElephantCorridorSentinelSheet(
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (language == AppLanguage.ODIA) "କୁଳଡିହା ଗଜ-ସେଣ୍ଟିନେଲ୍ ସତର୍କତା" else "Kuldiha Elephant Corridor Gaja-Sentinel Tracker",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text("Solar acoustic fence & forest watcher telemetry across Nilagiri, Oupada & Tinkosia corridors. Emergency Wildlife Control Room: 06782-262270.", fontSize = 13.sp, color = BentoSlate700)
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

@Composable
fun PanchalingeswarWaterfallTrekSheet(
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = if (language == AppLanguage.ODIA) "ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଝରଣା ଓ ପାହାଡ଼ ଟ୍ରେକିଂ" else "Panchalingeswar Perennial Stream & Devgiri Hill Trek",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
        )
        Text("Stream Flow Status: ${dailyPulse.panchalingeswarFlowStatus} • Rock Step Slip Risk: ${dailyPulse.panchalingeswarSlipRisk}. Guided eco-treks start from Panthanivas gate at 06:30 AM.", fontSize = 13.sp, color = BentoSlate700)
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}
