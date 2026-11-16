package com.example.ui.features.transit

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
import com.example.data.repository.BalasoreRepository
import com.example.ui.theme.BentoSlate700

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreSmartTransitTrackerSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ସ୍ମାର୍ଟ ଟ୍ରାନ୍ସିଟ୍ ଟ୍ରାକର" else "Balasore Smart Rail, Bus & Pink Auto Tracker",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            BalasoreRepository.transitSchedules.forEach { schedule ->
                Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(schedule.routeName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("${schedule.origin} ➔ ${schedule.destination} • ${schedule.time} (${schedule.status})", fontSize = 12.sp, color = BentoSlate700)
                    }
                }
            }
            Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth()) {
                Text("Close")
            }
        }
    }
}
