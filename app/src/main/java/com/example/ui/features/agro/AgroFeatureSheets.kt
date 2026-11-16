package com.example.ui.features.agro

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
import com.example.ui.theme.OceanBlue

@Composable
private fun AgroFeatureSheetBody(
    title: String,
    subtitle: String,
    highlights: List<String>,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        Text(text = subtitle, fontSize = 13.sp, color = OceanBlue, fontWeight = FontWeight.SemiBold)
        highlights.forEach { line ->
            Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                Text(text = line, modifier = Modifier.padding(12.dp), fontSize = 13.sp, color = BentoSlate700)
            }
        }
        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("Done")
        }
    }
}

@Composable
fun PanBarajaAgroSheet(
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AgroFeatureSheetBody(
        title = "Bhograi & Baliapal Chandanpur Paan Baraja Hub",
        subtitle = "GI-Grade Banarasi & Deshi Betel Vine Cultivators Network",
        highlights = listOf(
            "Bhograi & Langaleswar Paan Mandi Auction: ₹1,450 - ₹2,100 per Gadi (4,000 leaves)",
            "Coastal Bamboo Shade (Baraja) Micro-Climate Advisory: Optimal 76% relative humidity",
            "Export Dispatch: Daily rail & road consignments from Jaleswar to Howrah, Varanasi & Mumbai"
        ),
        onClose = onClose,
        modifier = modifier
    )
}

@Composable
fun HeirloomRiceAgroSheet(
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AgroFeatureSheetBody(
        title = "Subarnarekha Basin Heirloom Paddy & Aromatic Rice",
        subtitle = "Kalajeera, Tulasiphoola & Flood-Resilient Swarna Sub-1 Seeds",
        highlights = listOf(
            "Current Growth Stage: ${DailyUpdateEngine.getDailyPulse().heirloomPaddyGrowthStage}",
            "Basta & Baliapal Organic Seed Bank: Traditional salt-tolerant coastal varieties preserved",
            "Kharif & Rabi PACS Procurement Support: Soil health card & moisture testing active"
        ),
        onClose = onDismiss,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanBarajaAquaShrimpSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onClose) {
        AgroFeatureSheetBody(
            title = if (language == AppLanguage.ODIA) "ପାନ ବରଜ ଓ ଚିଙ୍ଗୁଡ଼ି ଚାଷ ସୂଚନା" else "Paan Baraja & Coastal Vannamei Aquaculture",
            subtitle = "Integrated Coastal Agro-Fishery Advisory for Bhograi, Basta & Bahanaga",
            highlights = listOf(
                "Brackish Water Pond Salinity: 15-22 PPT optimal across Chandipur & Kasafal creeks",
                "Dissolved Oxygen Night Aerator Advisory: Run paddlewheel aerators 10:00 PM - 05:00 AM",
                "Betel Vine Fungal Guard: Bordeaux mixture organic spray recommended before coastal showers"
            ),
            onClose = onClose,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalaramgadiMandiPriceIndexSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        AgroFeatureSheetBody(
            title = if (language == AppLanguage.ODIA) "ବଳରାମଗଡ଼ି ଓ କୃଷି ମଣ୍ଡି ଦର ସୂଚୀ" else "Balaramgadi & District Mandi Price Index",
            subtitle = "Live Morning Wholesale Auction Rates • ${dailyPulse.formattedDate}",
            highlights = listOf(
                "Subarnarekha Estuary Hilsa (1kg+): ₹${dailyPulse.hilsaEstuaryRateKg}/kg • Silver Pomfret: ₹${dailyPulse.pomfretExportRateKg}/kg",
                "Tiger Prawn (Bagada): ₹${dailyPulse.tigerPrawnRateKg}/kg • Bhograi Betel Leaf: ₹${dailyPulse.betelLeafChandanpurRate}/1000 leaves",
                "Jaleswar Vegetable & Cashew Mandi: Grade W-240 Cashew Kernel ₹760/kg"
            ),
            onClose = onDismiss,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubarnarekhaCropAdvisorySheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        AgroFeatureSheetBody(
            title = if (language == AppLanguage.ODIA) "ସୁବର୍ଣ୍ଣରେଖା କୃଷି ଓ ଜଳସେଚନ ପରାମର୍ଶ" else "Subarnarekha & Budhabalanga Crop Advisory",
            subtitle = "OUAT KVK Baliapal Agro-Met Bulletin • Basin Status: ${dailyPulse.subarnarekhaBasinRisk}",
            highlights = listOf(
                "Paddy Stage: ${dailyPulse.heirloomPaddyGrowthStage}",
                "Canal Irrigation Schedule: Coast Canal & Subarnarekha Left Bank distributaries open",
                "Groundnut & Cashew Intercropping: Recommended across sandy loam belts of Jaleswar & Baliapal"
            ),
            onClose = onDismiss,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BhograiCashewGroundnutMandiSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    onDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        AgroFeatureSheetBody(
            title = if (language == AppLanguage.ODIA) "ଭୋଗରାଇ କାଜୁ ଓ ଚିନାବାଦାମ ମଣ୍ଡି" else "Bhograi & Baliapal Cashew & Groundnut Mandi",
            subtitle = "Coastal Sand-Dune Cash Crop Processing & Wholesale Hub",
            highlights = listOf(
                "Raw Cashew Nut (RCN) Mandi Rate: ₹13,800 - ₹15,200 / Quintal at Jaleswar & Baliapal",
                "Coastal Riverbed Groundnut (TG-37A / Devi): ₹6,650 / Quintal (Oil content 49%)",
                "Over 85 local cashew roasting & grading units active across North Balasore"
            ),
            onClose = onDismiss,
            modifier = modifier
        )
    }
}

@Composable
fun KrushakPacsMandiGrainSheet(
    dailyPulse: DailyBalasorePulse = DailyUpdateEngine.getDailyPulse(),
    language: AppLanguage = AppLanguage.ENGLISH,
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    AgroFeatureSheetBody(
        title = if (language == AppLanguage.ODIA) "କୃଷକ ପ୍ୟାକ୍ସ (PACS) ଧାନ ମଣ୍ଡି ସୂଚନା" else "Krushak PACS Paddy & Grain Mandi Portal",
        subtitle = "MSP + Input Assistance Procurement Across 12 Balasore Blocks",
        highlights = listOf(
            "Effective Paddy Procurement Rate in Odisha: ₹3,100 / Quintal (Direct DBT within 48 hrs)",
            "Moisture Limit: Up to 17% FAQ standard accepted at Soro, Basta, Remuna & Simulia PACS",
            "Farmer Helpline & Token Grievance Cell: 1967 / District Civil Supplies Office Balasore"
        ),
        onClose = onClose,
        modifier = modifier
    )
}
