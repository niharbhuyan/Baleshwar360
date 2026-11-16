package com.example.ui.features.heritage

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
fun BuddhistJainCircuitSheet(
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Ayodhya, Kupari & Soro Ancient Buddhist-Jain Circuit", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Explore 8th-11th century Marichi, Tara, Avalokitesvara, and Jain Tirthankara sculptures excavated at Ayodhya (Nilagiri) and Kupari (Khaira).", fontSize = 13.sp, color = BentoSlate700)
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

@Composable
fun ChhenaGajaHotBatchSheet(
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Basta, Soro & Remuna Hot Chhena Gaja & Amruta Keli Tracker", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("Fresh wood-fired batches of traditional Baleswari Chhena Gaja and Remuna Khirachora Amruta Keli condensed milk bhog available at 12:30 PM and 06:30 PM.", fontSize = 13.sp, color = BentoSlate700)
        Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BaleswariCuisineExplorerSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱରୀ ଖାଦ୍ୟ ଓ ମିଠା ଐତିହ୍ୟ" else "Authentic Baleswari Cuisine & Sweet Explorer",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Signature Specialties:\n• Remuna Amruta Keli Kheer (${dailyPulse.amritaKeliBatchStatus})\n• Balaramgadi Sorisa Ilishi (Mustard Hilsa)\n• Basta Chhena Gaja & Gaintha Pitha",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = BentoSlate700
                )
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}
