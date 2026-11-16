package com.example.ui.features.transit

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.fcm.FcmManager
import com.example.data.local.BusScheduleEntity
import com.example.data.local.TrainScheduleEntity
import com.example.data.local.TransportAlertEntity
import com.example.data.model.AppLanguage
import com.example.data.repository.TransportAlertRepository
import com.example.ui.theme.*
import kotlinx.coroutines.launch

enum class TransportFilterTab {
    ALL,
    TRAINS,
    BUSES,
    DELAYED_ONLY
}

/**
 * Transport Alerts feature backed by Room Database local cache for
 * Balasore Railway Station (BLS) trains and Sahadevkhunta Central Bus Stand schedules,
 * featuring real-time delay push notification support via Firebase Cloud Messaging.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransportAlertsSheet(
    language: AppLanguage,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
    highlightTransportNumber: String? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA

    val repo = remember { TransportAlertRepository(context) }

    // Seed database on first launch
    LaunchedEffect(Unit) {
        repo.seedInitialTransportDataIfEmpty()
    }

    val trainSchedules by repo.trainSchedules.collectAsState(initial = emptyList())
    val busSchedules by repo.busSchedules.collectAsState(initial = emptyList())
    val transportAlerts by repo.transportAlerts.collectAsState(initial = emptyList())
    val activeDelayAlerts by repo.activeDelayAlerts.collectAsState(initial = emptyList())

    var selectedTab by remember { mutableStateOf(TransportFilterTab.ALL) }
    var searchQuery by remember { mutableStateOf(highlightTransportNumber ?: "") }
    var isSimulatingPush by remember { mutableStateOf(false) }

    val totalTrains = trainSchedules.size
    val totalBuses = busSchedules.size
    val totalDelays = trainSchedules.count { it.delayMinutes > 0 } + busSchedules.count { it.status == "DELAYED" }
    val onTimePercentage = if (totalTrains + totalBuses > 0) {
        (((totalTrains + totalBuses - totalDelays).toDouble() / (totalTrains + totalBuses)) * 100).toInt()
    } else 100

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = Color(0xFFF97316).copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DirectionsTransit,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (isOdia) "ଯାତାୟାତ ସତର୍କତା ଓ ସମୟସାରଣୀ" else "Transport Alerts & Schedules",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "Room DB",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black,
                                        color = Color(0xFF166534),
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isOdia)
                                    "ବାଲେଶ୍ୱର ଜଙ୍କସନ (BLS) • ସହଦେବଖୁଣ୍ଟା ବସ୍ ଟର୍ମିନାଲ୍"
                                else
                                    "Balasore Junction (BLS) • Sahadevkhunta Central Bus Stand",
                                fontSize = 11.sp,
                                color = BentoSlate500
                            )
                        }
                    }

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_transport_alerts_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = BentoSlate700
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action Bar: FCM Live Push Status & Simulate Delay Test Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // FCM Connected Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, BentoSlate200)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (isOdia) "FCM ସତର୍କତା ସକ୍ରିୟ" else "FCM Push Alerts: Active",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoSlate700
                            )
                        }
                    }

                    // Simulate Push Notification Button
                    Button(
                        onClick = {
                            isSimulatingPush = true
                            coroutineScope.launch {
                                FcmManager.simulateTransportDelayPush(
                                    context = context,
                                    transportNumber = "12821",
                                    serviceName = "12821 Dhauli Express",
                                    delayMinutes = 35,
                                    platformOrBay = "PF 1",
                                    reason = "Signal interlocking regulation in Kharagpur-Balasore trunk line"
                                )
                                Toast.makeText(
                                    context,
                                    if (isOdia) "FCM ଟ୍ରେନ୍ ବିଳମ୍ବ ସତର୍କତା ପ୍ରେରିତ!" else "FCM Train Delay Push Notification Dispatched!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isSimulatingPush = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        modifier = Modifier.testTag("simulate_fcm_delay_btn"),
                        enabled = !isSimulatingPush
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isSimulatingPush) "Sending..." else if (isOdia) "FCM ସତର୍କତା ପରୀକ୍ଷା" else "Test FCM Push",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        },
        containerColor = Color(0xFFF8FAFC),
        modifier = modifier.testTag("transport_alerts_sheet")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // METRICS ROW: Total Services, Active Delays, On-Time Score
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Total Services Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, BentoSlate200),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isOdia) "ମୋଟ ସେବା" else "Total Cached",
                                fontSize = 10.5.sp,
                                color = BentoSlate500
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${totalTrains + totalBuses}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = BentoSlate900
                            )
                            Text(
                                text = "$totalTrains Trains • $totalBuses Buses",
                                fontSize = 9.sp,
                                color = OceanBlue
                            )
                        }
                    }

                    // Active Delays Card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (totalDelays > 0) Color(0xFFFEF2F2) else Color(0xFFF0FDF4),
                        border = BorderStroke(1.dp, if (totalDelays > 0) Color(0xFFFECACA) else Color(0xFFBBF7D0)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isOdia) "ବିଳମ୍ବିତ" else "Active Delays",
                                fontSize = 10.5.sp,
                                color = if (totalDelays > 0) Color(0xFF991B1B) else Color(0xFF166534)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$totalDelays",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (totalDelays > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )
                            Text(
                                text = if (totalDelays > 0) "Alerts Active" else "All On Time",
                                fontSize = 9.sp,
                                color = if (totalDelays > 0) Color(0xFFDC2626) else Color(0xFF16A34A)
                            )
                        }
                    }

                    // On-Time Score
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, BentoSlate200),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = if (isOdia) "ସମୟାନୁବର୍ତ୍ତିତା" else "On-Time Rate",
                                fontSize = 10.5.sp,
                                color = BentoSlate500
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "$onTimePercentage%",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = if (onTimePercentage >= 80) Color(0xFF16A34A) else Color(0xFFEA580C)
                            )
                            Text(
                                text = "Balasore Hub",
                                fontSize = 9.sp,
                                color = BentoSlate500
                            )
                        }
                    }
                }
            }

            // ACTIVE DELAY ALERTS BANNER SECTION
            if (activeDelayAlerts.isNotEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFFEF2F2))
                            .border(BorderStroke(1.2.dp, Color(0xFFFECACA)), RoundedCornerShape(16.dp))
                            .padding(12.dp)
                            .testTag("active_delay_alerts_section")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isOdia) "ସକ୍ରିୟ ବିଳମ୍ବ ସତର୍କତା (${activeDelayAlerts.size})" else "Active Delay Alerts (${activeDelayAlerts.size})",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF991B1B)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFDC2626)
                            ) {
                                Text(
                                    text = "LIVE FCM",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        activeDelayAlerts.take(3).forEach { alert ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White,
                                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = if (alert.transportType == "TRAIN") "🚆" else "🚌",
                                                fontSize = 13.sp
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = alert.serviceName,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = BentoSlate900
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = Color(0xFFFEE2E2)
                                            ) {
                                                Text(
                                                    text = "+${alert.delayMinutes}m",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Black,
                                                    color = Color(0xFFDC2626),
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isOdia && alert.reasonOdia.isNotBlank()) alert.reasonOdia else alert.reason,
                                            fontSize = 10.5.sp,
                                            color = Color(0xFF7F1D1D),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Sched: ${alert.scheduledTime} → Exp: ${alert.estimatedTime} • ${alert.platformOrBay}",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = BentoSlate600
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // SEARCH & FILTER CONTROLS
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("transport_search_input"),
                        placeholder = {
                            Text(
                                text = if (isOdia) "ଟ୍ରେନ୍/ବସ୍ ନମ୍ବର କିମ୍ବା ଗନ୍ତବ୍ୟସ୍ଥଳ ଖୋଜନ୍ତୁ..." else "Search train/bus number or destination (e.g. Howrah, Puri)...",
                                fontSize = 12.sp
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = BentoSlate400,
                                modifier = Modifier.size(18.dp)
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = BentoSlate400,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedBorderColor = OceanBlue,
                            unfocusedBorderColor = BentoSlate200
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Tab Filter Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        TransportFilterTab.values().forEach { tab ->
                            val isSelected = selectedTab == tab
                            val label = when (tab) {
                                TransportFilterTab.ALL -> if (isOdia) "ସମସ୍ତ" else "All"
                                TransportFilterTab.TRAINS -> if (isOdia) "🚆 ଟ୍ରେନ୍" else "🚆 Trains"
                                TransportFilterTab.BUSES -> if (isOdia) "🚌 ବସ୍" else "🚌 Buses"
                                TransportFilterTab.DELAYED_ONLY -> if (isOdia) "⚠️ ବିଳମ୍ବିତ" else "⚠️ Delayed"
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTab = tab },
                                label = {
                                    Text(
                                        text = label,
                                        fontSize = 11.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (tab == TransportFilterTab.DELAYED_ONLY) Color(0xFFDC2626) else OceanBlue,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) Color.Transparent else BentoSlate200,
                                    selectedBorderColor = Color.Transparent,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                modifier = Modifier.testTag("transport_filter_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // FILTERED TRAIN LIST
            if (selectedTab != TransportFilterTab.BUSES) {
                val filteredTrains = trainSchedules.filter { train ->
                    val matchesSearch = searchQuery.isBlank() ||
                            train.trainNumber.contains(searchQuery, true) ||
                            train.trainNameEn.contains(searchQuery, true) ||
                            train.destination.contains(searchQuery, true) ||
                            train.origin.contains(searchQuery, true)
                    val matchesDelay = selectedTab != TransportFilterTab.DELAYED_ONLY || train.delayMinutes > 0
                    matchesSearch && matchesDelay
                }

                if (filteredTrains.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isOdia) "ବାଲେଶ୍ୱର ରେଳ ଷ୍ଟେସନ୍ (BLS) ସମୟସାରଣୀ" else "Balasore Railway Station (BLS) Trains",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate700,
                            modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
                        )
                    }

                    items(filteredTrains, key = { "train_${it.trainNumber}" }) { train ->
                        TrainScheduleCard(
                            train = train,
                            isOdia = isOdia,
                            onToggleTrack = {
                                coroutineScope.launch {
                                    repo.toggleTrackedTrain(train.trainNumber, !train.isTracked)
                                }
                            }
                        )
                    }
                }
            }

            // FILTERED BUS LIST
            if (selectedTab != TransportFilterTab.TRAINS) {
                val filteredBuses = busSchedules.filter { bus ->
                    val matchesSearch = searchQuery.isBlank() ||
                            bus.busNumber.contains(searchQuery, true) ||
                            bus.serviceName.contains(searchQuery, true) ||
                            bus.destination.contains(searchQuery, true) ||
                            bus.destinationOdia.contains(searchQuery, true)
                    val matchesDelay = selectedTab != TransportFilterTab.DELAYED_ONLY || bus.status == "DELAYED"
                    matchesSearch && matchesDelay
                }

                if (filteredBuses.isNotEmpty()) {
                    item {
                        Text(
                            text = if (isOdia) "ସହଦେବଖୁଣ୍ଟା ସେଣ୍ଟ୍ରାଲ୍ ବସ୍ ଷ୍ଟାଣ୍ଡ" else "Sahadevkhunta Central Bus Stand Schedules",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate700,
                            modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                        )
                    }

                    items(filteredBuses, key = { "bus_${it.id}_${it.busNumber}" }) { bus ->
                        BusScheduleCard(
                            bus = bus,
                            isOdia = isOdia
                        )
                    }
                }
            }

            // ROOM CACHE FOOTER
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, BentoSlate200),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = Color(0xFF6366F1),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia)
                                "ରୁମ୍ ଡାଟାବେସ୍ (Room SQLite): ଟ୍ରେନ୍ ଓ ବସ୍ ସମୟସାରଣୀ ଅଫଲାଇନ୍ ସଞ୍ଚିତ। ନୂତନ ବିଳମ୍ବ ସୂଚନା FCM ପୁଶ୍ ମାଧ୍ୟମରେ ସ୍ୱୟଂଚାଳିତ ଭାବେ ଅପଡେଟ୍ ହୁଏ।"
                            else
                                "Room Database Cache: Train & Bus schedules cached locally for full offline access. Real-time delays update via Firebase Cloud Messaging push.",
                            fontSize = 11.sp,
                            color = BentoSlate600,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TrainScheduleCard(
    train: TrainScheduleEntity,
    isOdia: Boolean,
    onToggleTrack: () -> Unit
) {
    val isDelayed = train.delayMinutes > 0
    val cardBorderColor = if (isDelayed) Color(0xFFFECACA) else BentoSlate200
    val cardBgColor = if (isDelayed) Color(0xFFFFF7ED) else Color.White

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(1.dp, cardBorderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("train_schedule_card_${train.trainNumber}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header Row: Number, Name, Track toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF0284C7).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = train.trainNumber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF0284C7),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOdia && train.trainNameOr.isNotBlank()) train.trainNameOr else train.trainNameEn,
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Track Toggle Icon
                IconButton(
                    onClick = onToggleTrack,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (train.isTracked) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                        contentDescription = "Track Train Alerts",
                        tint = if (train.isTracked) Color(0xFFEA580C) else BentoSlate400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Route: Origin -> Destination
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = train.origin,
                    fontSize = 11.5.sp,
                    color = BentoSlate700
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = BentoSlate400,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = train.destination,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BentoSlate900
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Timings & Platform Badge Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text(
                            text = if (isOdia) "ଆଗମନ / ପ୍ରସ୍ଥାନ" else "Arrival / Departure",
                            fontSize = 9.5.sp,
                            color = BentoSlate500
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = train.scheduledArrival,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDelayed) BentoSlate400 else BentoSlate900
                            )
                            if (isDelayed) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "→ ${train.estimatedArrival}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Platform
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        border = BorderStroke(1.dp, BentoSlate200)
                    ) {
                        Text(
                            text = train.platform,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate700,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Delay or On-time status
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDelayed) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                    ) {
                        Text(
                            text = if (isDelayed) "+${train.delayMinutes}m Late" else "On Time",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDelayed) Color(0xFFDC2626) else Color(0xFF166534),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (train.currentLocation.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7))
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isOdia && train.currentLocationOr.isNotBlank()) train.currentLocationOr else train.currentLocation,
                        fontSize = 10.5.sp,
                        color = Color(0xFF0284C7),
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
fun BusScheduleCard(
    bus: BusScheduleEntity,
    isOdia: Boolean
) {
    val isDelayed = bus.status == "DELAYED"

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoSlate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("bus_schedule_card_${bus.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFF059669).copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "BUS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF059669),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = bus.serviceName,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate900,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (isDelayed) Color(0xFFFEE2E2) else Color(0xFFDCFCE7)
                ) {
                    Text(
                        text = if (isDelayed) "DELAYED" else "ON TIME",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDelayed) Color(0xFFDC2626) else Color(0xFF166534),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Destination & Route
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isOdia && bus.destinationOdia.isNotBlank()) bus.destinationOdia else bus.destination,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BentoSlate900
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "• ${bus.busType}",
                    fontSize = 11.sp,
                    color = BentoSlate500
                )
            }

            Text(
                text = "Via: ${bus.viaRoute}",
                fontSize = 10.5.sp,
                color = BentoSlate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Departure time, Bay, Fare
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = BentoSlate500,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = bus.departureTime,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate900
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFFF1F5F9)
                    ) {
                        Text(
                            text = bus.bayPlatform,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium,
                            color = BentoSlate700,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = "₹${bus.fareInr}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF059669)
                )
            }
        }
    }
}
