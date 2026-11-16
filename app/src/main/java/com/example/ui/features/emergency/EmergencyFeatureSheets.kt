package com.example.ui.features.emergency

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
import androidx.compose.material3.OutlinedButton
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
import com.example.data.repository.BalasoreRepository
import com.example.ui.theme.BentoSlate700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreSnakebiteAntiVenomSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ସର୍ପାଘାତ ଆଣ୍ଟି-ଭେନମ୍ ଜରୁରୀକାଳୀନ କେନ୍ଦ୍ର" else "24x7 Snakebite Anti-Venom (ASV) & Wildlife Rescue",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Polyvalent Anti-Snake Venom (ASV) Stock Verified: FM Medical College Casualty (24x7), Nilagiri SDH, Jaleswar CHC, Baliapal CHC & Soro CHC.\nEmergency Ambulance: 108 • Snake Helpline Balasore: 06782-262270",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = BentoSlate700
                )
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}

@Composable
fun BalasoreShelterWidgetManagerSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onDismiss: () -> Unit = {},
    onOpenShelterNetwork: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = if (language == AppLanguage.ODIA) "ବାତ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ ହୋମ୍-ସ୍କ୍ରିନ୍ ୱିଜେଟ୍" else "Cyclone Shelter & Tide Home-Screen Widget Manager",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
        }
        Text(
            text = "Pin the Baleshwar 360 Weather, Chandipur Tide & Nearest OSDMA Shelter widget to your Android home screen for zero-latency offline access.",
            fontSize = 13.sp,
            color = BentoSlate700
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onOpenShelterNetwork, modifier = Modifier.weight(1f)) {
                Text("View Shelters")
            }
            Button(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Done")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearestEmergencyServicesMapSheet(
    onDismiss: () -> Unit = {},
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ନିକଟସ୍ଥ ଜରୁରୀକାଳୀନ ସେବା ମାନଚିତ୍ର" else "Nearest Hospitals, Police & Fire Stations",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
            BalasoreRepository.emergencyContacts.forEach { contact ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(if (language == AppLanguage.ODIA) contact.odiaName else contact.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${contact.category} • Helpline: ${contact.number}", fontSize = 12.sp, color = BentoSlate700)
                    }
                }
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}
