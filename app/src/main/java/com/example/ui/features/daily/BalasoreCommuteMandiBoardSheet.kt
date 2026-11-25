package com.example.ui.features.daily

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Train
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.daily.DailyBalasorePulse
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private data class CommuteTrainEntry(
    val number: String,
    val nameEn: String,
    val nameOd: String,
    val corridor: String, // "BBS" or "HWH"
    val scheduledTime: String,
    val platform: String,
    val statusText: String,
    val isDelayed: Boolean
)

private data class LocalBusDeparture(
    val routeEn: String,
    val routeOd: String,
    val stand: String,
    val nextDeparture: String,
    val frequency: String,
    val fareInr: String
)

private data class MandiPriceItem(
    val nameEn: String,
    val nameOd: String,
    val market: String, // "HARBOR" or "MOTIGANJ"
    val unit: String,
    val priceRange: String,
    val trend: String // "UP", "DOWN", "STEADY"
)

@Composable
fun BalasoreCommuteMandiBoardSheet(
    language: AppLanguage,
    dailyPulse: DailyBalasorePulse,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Trains, 1 = Local Buses, 2 = Fish & Mandi
    var trainCorridorFilter by remember { mutableStateOf("ALL") } // ALL, BBS, HWH
    var mandiFilter by remember { mutableStateOf("ALL") } // ALL, HARBOR, MOTIGANJ
    var lastRefreshedLabel by remember {
        mutableStateOf(SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date()))
    }

    val trains = remember {
        listOf(
            CommuteTrainEntry("12073", "Howrah–Bhubaneswar Jan Shatabdi", "ହାଓଡ଼ା-ଭୁବନେଶ୍ୱର ଜନ ଶତାବ୍ଦୀ", "BBS", "09:12 AM", "PF 1", "On Time", false),
            CommuteTrainEntry("22895", "Howrah–Puri Vande Bharat Exp", "ହାଓଡ଼ା-ପୁରୀ ବନ୍ଦେ ଭାରତ ଏକ୍ସପ୍ରେସ୍", "BBS", "09:03 AM", "PF 1", "On Time", false),
            CommuteTrainEntry("12821", "Dhauli Express (Towards BBS)", "ଧଉଳି ଏକ୍ସପ୍ରେସ୍ (ଭୁବନେଶ୍ୱର)", "BBS", "12:18 PM", "PF 2", "Delayed 10m", true),
            CommuteTrainEntry("12842", "Coromandel Express (Towards HWH)", "କରମଣ୍ଡଳ ଏକ୍ସପ୍ରେସ୍ (ହାଓଡ଼ା)", "HWH", "01:45 PM", "PF 3", "On Time", false),
            CommuteTrainEntry("12822", "Dhauli Express (Towards HWH)", "ଧଉଳି ଏକ୍ସପ୍ରେସ୍ (ହାଓଡ଼ା)", "HWH", "03:05 PM", "PF 3", "On Time", false),
            CommuteTrainEntry("12704", "Falaknuma Express (Towards HWH)", "ଫଲକନୁମା ଏକ୍ସପ୍ରେସ୍ (ହାଓଡ଼ା)", "HWH", "04:52 PM", "PF 3", "Delayed 15m", true),
            CommuteTrainEntry("18045", "East Coast Express (Towards BBS)", "ଇଷ୍ଟ କୋଷ୍ଟ ଏକ୍ସପ୍ରେସ୍ (ଭୁବନେଶ୍ୱର)", "BBS", "03:40 PM", "PF 2", "On Time", false)
        )
    }

    val localBuses = remember {
        listOf(
            LocalBusDeparture("Balasore → Chandipur Beach (Mo Bus / Shuttle)", "ବାଲେଶ୍ୱର → ଚାନ୍ଦିପୁର ବେଳାଭୂମି", "Station Square / Sahadevkhunta", "In 12 mins", "Every 20m", "₹25"),
            LocalBusDeparture("Balasore → Remuna Khirachora Express", "ବାଲେଶ୍ୱର → ରେମୁଣା ଖିରଚୋରା", "Sahadevkhunta Bus Stand", "In 8 mins", "Every 15m", "₹20"),
            LocalBusDeparture("Balasore → Nilagiri & Panchalingeswar", "ବାଲେଶ୍ୱର → ନୀଳଗିରି ଓ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର", "Sahadevkhunta Bay 4", "In 18 mins", "Every 30m", "₹45"),
            LocalBusDeparture("Balasore → Soro – Bhadrak Corridor", "ବାଲେଶ୍ୱର → ସୋରୋ - ଭଦ୍ରକ", "Sahadevkhunta NH-16 Bay", "In 10 mins", "Every 15m", "₹50"),
            LocalBusDeparture("Balasore → Jaleswar & Chandaneswar", "ବାଲେଶ୍ୱର → ଜଳେଶ୍ୱର ଓ ଚନ୍ଦନେଶ୍ୱର", "Sahadevkhunta Bay 2", "In 25 mins", "Every 35m", "₹85"),
            LocalBusDeparture("Balasore → Baripada Express", "ବାଲେଶ୍ୱର → ବାରିପଦା ଏକ୍ସପ୍ରେସ୍", "Sahadevkhunta Bay 1", "In 15 mins", "Every 20m", "₹70")
        )
    }

    val mandiRates = remember {
        listOf(
            MandiPriceItem("Fresh Bay Hilsa (Ilish 800g+)", "ତାଜା ବଙ୍ଗୋପସାଗର ଇଲିଶି", "HARBOR", "per kg", "₹1,150 – ₹1,450", "UP"),
            MandiPriceItem("Silver Pomfret (Chandua)", "ଧଳା ଚାନ୍ଦୁଆ ମାଛ", "HARBOR", "per kg", "₹680 – ₹820", "STEADY"),
            MandiPriceItem("Jumbo Tiger Prawn (Bagda Chingudi)", "ବାଗଦା ଚିଙ୍ଗୁଡ଼ି (ବଡ଼)", "HARBOR", "per kg", "₹620 – ₹760", "DOWN"),
            MandiPriceItem("Coastal Bhetki (Fresh Catch)", "ତାଜା ଭେଟକି ମାଛ", "HARBOR", "per kg", "₹460 – ₹540", "STEADY"),
            MandiPriceItem("Estuary Mud Crab (Kankada)", "ମୁହାଣ କଙ୍କଡ଼ା", "HARBOR", "per kg", "₹520 – ₹650", "UP"),
            MandiPriceItem("Baleswari Mitha Paan (100 Leaves)", "ବାଲେଶ୍ୱରୀ ମିଠା ପାନ (୧୦୦ ପତ୍ର)", "MOTIGANJ", "per bundle", "₹90 – ₹130", "STEADY"),
            MandiPriceItem("Local Swarna Rice (Wholesale)", "ସ୍ଥାନୀୟ ସ୍ୱର୍ଣ୍ଣ ଚାଉଳ", "MOTIGANJ", "per kg", "₹38 – ₹44", "STEADY"),
            MandiPriceItem("Fresh Potato & Nasik Onion", "ଆଳୁ ଓ ପିଆଜ (ମୋତିଗଞ୍ଜ)", "MOTIGANJ", "per kg", "₹26 / ₹38", "DOWN"),
            MandiPriceItem("Desi Brinjal, Pointed Gourd & Okra", "ବାଇଗଣ, ପୋଟଳ ଓ ଭେଣ୍ଡି", "MOTIGANJ", "per kg", "₹35 – ₹55", "DOWN"),
            MandiPriceItem("Cold-Pressed Mustard Oil", "ଶୁଦ୍ଧ ସୋରିଷ ତେଲ", "MOTIGANJ", "per litre", "₹158 – ₹172", "UP")
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .testTag("balasore_commute_mandi_board_sheet")
    ) {
        // Gradient Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        listOf(Color(0xFF0F172A), OceanBlueDark, OceanBlue)
                    )
                )
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOdia) "ସ୍ମାର୍ଟ ଯାତାୟାତ ଓ ମାଛ/ମଣ୍ଡି ଦର ବୋର୍ଡ" else "Smart Commute & Fish/Mandi Board",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isOdia) "BLS ଟ୍ରେନ୍/ବସ୍ + ବଳରାମଗଡ଼ି ଓ ମୋତିଗଞ୍ଜ ସକାଳ ଦର • $lastRefreshedLabel" else "BLS–BBS/HWH Trains, Buses & Morning Market Ticker • $lastRefreshedLabel",
                            color = Color(0xFFBAE6FD),
                            fontSize = 11.5.sp
                        )
                    }
                    Row {
                        IconButton(
                            onClick = {
                                lastRefreshedLabel = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                            },
                            modifier = Modifier.testTag("commute_mandi_refresh_btn")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh Ticker", tint = Color.White)
                        }
                        IconButton(
                            onClick = {
                                val shareBody = buildString {
                                    append("🚆🐟 BALASORE DAILY COMMUTE & MANDI BOARD (${dailyPulse.formattedDate})\n")
                                    append("• BLS–BBS: Jan Shatabdi 09:12 AM (PF1 On Time), Vande Bharat 09:03 AM\n")
                                    append("• Balaramgadi Harbor: Hilsa ₹1150-1450/kg, Pomfret ₹680-820/kg, Bagda Prawn ₹620-760/kg\n")
                                    append("• Motiganj Bazaar: Baleswari Paan ₹90-130/100, Rice ₹38-44/kg\n")
                                    append("Shared via Baleshwar 360")
                                }
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, shareBody)
                                }
                                context.startActivity(Intent.createChooser(intent, "Share Daily Board"))
                            },
                            modifier = Modifier.testTag("commute_mandi_share_btn")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Board", tint = Color.White)
                        }
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("commute_mandi_close_btn")
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }
            }
        }

        // 3-Tab Switcher
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text(if (isOdia) "ଟ୍ରେନ୍ (BLS)" else "Trains (BLS)", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Train, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("commute_tab_trains")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text(if (isOdia) "ଲୋକାଲ୍ ବସ୍" else "Local Buses", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.DirectionsBus, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("commute_tab_buses")
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text(if (isOdia) "ମାଛ ଓ ମଣ୍ଡି" else "Fish & Mandi", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                icon = { Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(18.dp)) },
                modifier = Modifier.testTag("commute_tab_mandi")
            )
        }

        when (selectedTab) {
            0 -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to (if (isOdia) "ସବୁ ଟ୍ରେନ୍" else "All Corridors"),
                        "BBS" to "→ Bhubaneswar",
                        "HWH" to "→ Howrah"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = trainCorridorFilter == key,
                            onClick = { trainCorridorFilter = key },
                            label = { Text(label, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = OceanBlue,
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                val filteredTrains = remember(trainCorridorFilter) {
                    if (trainCorridorFilter == "ALL") trains else trains.filter { it.corridor == trainCorridorFilter }
                }
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredTrains, key = { it.number + it.corridor }) { train ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            border = BorderStroke(1.dp, if (train.isDelayed) Color(0xFFFCA5A5) else BentoSlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = OceanBlue.copy(alpha = 0.14f)
                                        ) {
                                            Text(
                                                text = train.number,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = OceanBlueDark,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = train.platform,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoSlate500
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = if (isOdia) train.nameOd else train.nameEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Scheduled BLS: ${train.scheduledTime}",
                                        fontSize = 11.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (train.isDelayed) Color(0xFFFEF2F2) else Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = train.statusText,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (train.isDelayed) Color(0xFFDC2626) else Color(0xFF15803D),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            1 -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(localBuses) { bus ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            border = BorderStroke(1.dp, BentoSlate200),
                            modifier = Modifier.fillMaxWidth()
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
                                        text = if (isOdia) bus.routeOd else bus.routeEn,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Text(
                                        text = "📍 ${bus.stand} • ${bus.frequency}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color(0xFFE0F2FE)
                                    ) {
                                        Text(
                                            text = bus.nextDeparture,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = OceanBlueDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Fare ${bus.fareInr}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = EmeraldGreen
                                    )
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }

            else -> {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "ALL" to (if (isOdia) "ସବୁ ବଜାର" else "All Markets"),
                        "HARBOR" to (if (isOdia) "🐟 ବଳରାମଗଡ଼ି" else "🐟 Balaramgadi Harbor"),
                        "MOTIGANJ" to (if (isOdia) "🥬 ମୋତିଗଞ୍ଜ" else "🥬 Motiganj Bazaar")
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = mandiFilter == key,
                            onClick = { mandiFilter = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0D9488),
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }
                val filteredMandi = remember(mandiFilter) {
                    if (mandiFilter == "ALL") mandiRates else mandiRates.filter { it.market == mandiFilter }
                }
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredMandi, key = { it.nameEn }) { item ->
                        val (trendSymbol, trendColor, trendBg) = when (item.trend) {
                            "UP" -> Triple("▲ Rising", Color(0xFFDC2626), Color(0xFFFEF2F2))
                            "DOWN" -> Triple("▼ Lower", Color(0xFF15803D), Color(0xFFDCFCE7))
                            else -> Triple("▬ Steady", Color(0xFF0369A1), Color(0xFFE0F2FE))
                        }
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)),
                            border = BorderStroke(1.dp, BentoSlate200),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (item.market == "HARBOR") Color(0xFFE0F2FE) else Color(0xFFDCFCE7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(if (item.market == "HARBOR") "🐟" else "🥬", fontSize = 18.sp)
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (isOdia) item.nameOd else item.nameEn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = (if (item.market == "HARBOR") "Balaramgadi Harbor" else "Motiganj Bazaar") + " • ${item.unit}",
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = item.priceRange,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 13.5.sp,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.height(3.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = trendBg
                                    ) {
                                        Text(
                                            text = trendSymbol,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = trendColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(24.dp)) }
                }
            }
        }
    }
}
