package com.example.ui.features.health

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RuralMobileTelemedicineNetworkSheet(
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    language: AppLanguage = AppLanguage.ENGLISH,
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ଗ୍ରାମୀଣ ଟେଲିମେଡିସିନ୍ ଓ ମୋବାଇଲ୍ ସ୍ୱାସ୍ଥ୍ୟ ସେବା" else "Rural Mobile Health Unit & e-Sanjeevani Telemedicine",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = "Close") }
            }
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "OPD Status: ${dailyPulse.todaySpecialistOPDStatus}\nOn-Duty Emergency Roster: ${dailyPulse.todayOnDutyEmergencyDoctors}\nMobile Health Vans active across Bhograi, Baliapal, Oupada & Khaira gram panchayats.",
                    modifier = Modifier.padding(12.dp),
                    fontSize = 13.sp,
                    color = BentoSlate700
                )
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) { Text("Close") }
        }
    }
}
