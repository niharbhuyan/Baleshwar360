package com.example.ui.features.transit

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.data.notification.BalasoreTrainNotificationManager
import com.example.data.repository.BalasoreTrainRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Dedicated Real-Time Train Status & Push Notification Companion for Balasore Railway Station (BLS).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreLiveTrainStatusSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    initialHighlightedTrain: String? = null,
    onClose: () -> Unit = {}
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA
    val repository = remember { BalasoreTrainRepository() }

    var trainList by remember { mutableStateOf(BalasoreTrainRepository.INITIAL_TRAIN_DATASET) }
    var isRefreshing by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedMovement by remember { mutableStateOf(TrainMovement.ALL) }
    var selectedPlatformFilter by remember { mutableStateOf("ALL") }
    var selectedTypeFilter by remember { mutableStateOf("ALL") }
    var onlyDelayedFilter by remember { mutableStateOf(false) }

    // Dialog state for setting up push notifications on a specific train
    var trainToConfigureAlerts by remember { mutableStateOf<TrainRunningStatus?>(null) }

    // Tracked train numbers state
    var trackedSet by remember { mutableStateOf(BalasoreTrainNotificationManager.getTrackedTrainNumbers(context)) }

    // Check permission state for Android 13+
    var hasNotifyPermission by remember {
        mutableStateOf(BalasoreTrainNotificationManager.hasNotificationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasNotifyPermission = isGranted
        if (isGranted) {
            Toast.makeText(
                context,
                if (isOdia) "ପୁସ୍ ନୋଟିଫିକେସନ୍ ଅନୁମତି ସଫଳ!" else "Push notification permission granted!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Auto-refresh countdown (30s ticker)
    var secondsToNextRefresh by remember { mutableIntStateOf(30) }
    var lastUpdatedTimeText by remember {
        mutableStateOf(SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date()))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            if (secondsToNextRefresh > 1) {
                secondsToNextRefresh--
            } else {
                secondsToNextRefresh = 30
                val updated = repository.refreshLiveStatuses()
                trainList = updated
                lastUpdatedTimeText = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
            }
        }
    }

    // Filtered trains
    val filteredTrains = remember(
        trainList,
        searchQuery,
        selectedMovement,
        selectedPlatformFilter,
        selectedTypeFilter,
        onlyDelayedFilter,
        trackedSet
    ) {
        trainList.filter { train ->
            // Search query filter
            val query = searchQuery.trim().lowercase()
            val matchesQuery = query.isEmpty() ||
                    train.trainNumber.lowercase().contains(query) ||
                    train.trainNameEn.lowercase().contains(query) ||
                    train.trainNameOr.lowercase().contains(query) ||
                    train.destinationNameEn.lowercase().contains(query) ||
                    train.originNameEn.lowercase().contains(query)

            // Movement tab
            val matchesMovement = when (selectedMovement) {
                TrainMovement.ALL -> true
                TrainMovement.ARRIVALS -> !train.isTerminatingAtBalasore || train.destinationCode == "BLS"
                TrainMovement.DEPARTURES -> train.scheduledDeparture != "--"
                TrainMovement.TRACKED_ONLY -> trackedSet.contains(train.trainNumber)
            }

            // Platform filter
            val matchesPlatform = when (selectedPlatformFilter) {
                "ALL" -> true
                else -> train.platform.equals(selectedPlatformFilter, ignoreCase = true)
            }

            // Type filter
            val matchesType = when (selectedTypeFilter) {
                "ALL" -> true
                "VANDE_BHARAT" -> train.type == TrainType.VANDE_BHARAT
                "SUPERFAST" -> train.type == TrainType.SUPERFAST
                "MEMU" -> train.type == TrainType.MEMU_PASSENGER
                else -> true
            }

            // Delay filter
            val matchesDelay = !onlyDelayedFilter || train.delayMinutes > 0

            matchesQuery && matchesMovement && matchesPlatform && matchesType && matchesDelay
        }
    }

    // Rotating animation for refresh icon
    val infiniteTransition = rememberInfiniteTransition(label = "refreshRotation")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🚆",
                                fontSize = 20.sp,
                                modifier = Modifier.padding(end = 6.dp)
                            )
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱର ଲାଇଭ୍ ଟ୍ରେନ୍ ଷ୍ଟାଟସ୍" else "Balasore Live Train Status",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F172A)
                                )
                            )
                        }
                        Text(
                            text = if (isOdia) "ଷ୍ଟେସନ କୋଡ୍: BLS • ଦକ୍ଷିଣ ପୂର୍ବ ରେଳପଥ" else "Station Code: BLS • South Eastern Railway",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("btn_close_train_sheet")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Train Status",
                            tint = Color(0xFF334155)
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isRefreshing = true
                                val updated = repository.refreshLiveStatuses()
                                trainList = updated
                                lastUpdatedTimeText = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
                                secondsToNextRefresh = 30
                                isRefreshing = false
                                Toast.makeText(
                                    context,
                                    if (isOdia) "ସର୍ବଶେଷ ଲାଇଭ୍ ତଥ୍ୟ ଆପଡେଟ୍ ହୋଇଛି" else "Live telemetry refreshed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier.testTag("btn_refresh_trains")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Live Telemetry",
                            tint = Color(0xFF0284C7),
                            modifier = if (isRefreshing) Modifier.rotate(rotationAngle) else Modifier
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC)
                )
            )
        },
        containerColor = Color(0xFFF1F5F9)
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .testTag("live_train_status_list"),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Permission Banner (if Android 13+ and notifications not granted)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifyPermission) {
                item {
                    NotificationPermissionBanner(
                        isOdia = isOdia,
                        onRequestPermission = {
                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    )
                }
            }

            // Balasore Station Live Telemetry Header Banner
            item {
                StationTelemetryHeaderCard(
                    isOdia = isOdia,
                    secondsToNextRefresh = secondsToNextRefresh,
                    lastUpdatedTime = lastUpdatedTimeText,
                    onManualRefresh = {
                        scope.launch {
                            isRefreshing = true
                            trainList = repository.refreshLiveStatuses()
                            lastUpdatedTimeText = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
                            secondsToNextRefresh = 30
                            isRefreshing = false
                        }
                    }
                )
            }

            // Quick Metrics Row (Total, On Time, Delayed, Tracked)
            item {
                val totalCount = trainList.size
                val onTimeCount = trainList.count { it.delayMinutes == 0 }
                val delayedCount = trainList.count { it.delayMinutes > 0 }
                val approachingCount = trainList.count { it.statusCategory == TrainStatusCategory.APPROACHING }
                val activeTrackedCount = trackedSet.size

                TrainMetricsSummaryRow(
                    total = totalCount,
                    onTime = onTimeCount,
                    delayed = delayedCount,
                    approaching = approachingCount,
                    tracked = activeTrackedCount,
                    isOdia = isOdia
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("train_search_input"),
                    placeholder = {
                        Text(
                            text = if (isOdia) "ଟ୍ରେନ୍ ନମ୍ବର (ଯଥା 20831), ନାମ ବା ସ୍ଥାନ ଖୋଜନ୍ତୁ..." else "Search train # (e.g. 20831), name, or destination...",
                            fontSize = 13.sp,
                            color = Color(0xFF94A3B8)
                        )
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "Search", tint = Color(0xFF64748B))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = Color(0xFF64748B))
                            }
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedBorderColor = Color(0xFF0284C7),
                        unfocusedBorderColor = Color(0xFFCBD5E1)
                    ),
                    singleLine = true
                )
            }

            // Movement Selection Tabs (All, Arrivals, Departures, Tracked)
            item {
                MovementFilterTabs(
                    selectedMovement = selectedMovement,
                    trackedCount = trackedSet.size,
                    isOdia = isOdia,
                    onSelectMovement = { selectedMovement = it }
                )
            }

            // Filter Chips (Platforms, Train Types, Delays)
            item {
                FilterChipsBar(
                    selectedPlatform = selectedPlatformFilter,
                    selectedType = selectedTypeFilter,
                    onlyDelayed = onlyDelayedFilter,
                    isOdia = isOdia,
                    onSelectPlatform = { selectedPlatformFilter = it },
                    onSelectType = { selectedTypeFilter = it },
                    onToggleDelayed = { onlyDelayedFilter = !onlyDelayedFilter }
                )
            }

            // Results Counter & Active Filter Reset
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOdia) "${filteredTrains.size} ଟି ଟ୍ରେନ୍ ଦୃଶ୍ୟମାନ" else "${filteredTrains.size} trains matching filters",
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF64748B)
                        )
                    )

                    if (searchQuery.isNotEmpty() || selectedPlatformFilter != "ALL" || selectedTypeFilter != "ALL" || onlyDelayedFilter || selectedMovement != TrainMovement.ALL) {
                        Text(
                            text = if (isOdia) "ସମସ୍ତ ଫିଲ୍ଟର ହଟାନ୍ତୁ" else "Reset filters",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0284C7),
                            modifier = Modifier
                                .clickable {
                                    searchQuery = ""
                                    selectedPlatformFilter = "ALL"
                                    selectedTypeFilter = "ALL"
                                    onlyDelayedFilter = false
                                    selectedMovement = TrainMovement.ALL
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }

            // Empty State
            if (filteredTrains.isEmpty()) {
                item {
                    EmptyTrainsView(
                        isOdia = isOdia,
                        onResetFilters = {
                            searchQuery = ""
                            selectedPlatformFilter = "ALL"
                            selectedTypeFilter = "ALL"
                            onlyDelayedFilter = false
                            selectedMovement = TrainMovement.ALL
                        }
                    )
                }
            } else {
                // Train Items List
                items(filteredTrains, key = { it.trainNumber }) { train ->
                    val isTracked = trackedSet.contains(train.trainNumber)
                    val isHighlighted = initialHighlightedTrain == train.trainNumber

                    TrainCard(
                        train = train,
                        isTracked = isTracked,
                        isHighlighted = isHighlighted,
                        isOdia = isOdia,
                        onConfigureTracking = {
                            // Check permission first on Android 13+
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifyPermission) {
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            } else {
                                trainToConfigureAlerts = train
                            }
                        },
                        onQuickToggleTracking = {
                            if (isTracked) {
                                BalasoreTrainNotificationManager.untrackTrain(context, train.trainNumber)
                                trackedSet = BalasoreTrainNotificationManager.getTrackedTrainNumbers(context)
                                Toast.makeText(
                                    context,
                                    if (isOdia) "${train.trainNumber} ପାଇଁ ଟ୍ରାକିଂ ବନ୍ଦ ହେଲା" else "Stopped tracking ${train.trainNumber}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !hasNotifyPermission) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    val defaultPref = TrainNotificationPreference(trainNumber = train.trainNumber)
                                    BalasoreTrainNotificationManager.setTrackedTrain(context, train.trainNumber, defaultPref)
                                    trackedSet = BalasoreTrainNotificationManager.getTrackedTrainNumbers(context)
                                    BalasoreTrainNotificationManager.sendImmediateTestPushNotification(context, train)
                                    Toast.makeText(
                                        context,
                                        if (isOdia) "ଟ୍ରେନ୍ ${train.trainNumber} ପାଇଁ ପୁସ୍ ଆଲର୍ଟ ସକ୍ରିୟ ହେଲା!" else "Alerts active for ${train.trainNumber}! Test notification sent.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        onShare = {
                            val shareText = "🚆 Balasore Junction (BLS) Train Running Status:\n" +
                                    "Train: ${train.trainNumber} ${train.trainNameEn}\n" +
                                    "Route: ${train.originNameEn} ➔ ${train.destinationNameEn}\n" +
                                    "Platform: ${train.platform}\n" +
                                    "Status: ${train.statusCategory.labelEn} (${if (train.delayMinutes > 0) "Delayed ${train.delayMinutes}m" else "On Time"})\n" +
                                    "Expected Arrival: ${train.estimatedArrival} (Sched: ${train.scheduledArrival})\n" +
                                    "Location: ${train.currentLocation}\n" +
                                    "— Shared via Baleshwar 360"

                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Train Status"))
                        }
                    )
                }
            }

            // Bottom Spacing
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Modal Dialog for configuring Push Notifications for the selected train
    if (trainToConfigureAlerts != null) {
        val train = trainToConfigureAlerts!!
        val currentPref = remember(train.trainNumber) {
            BalasoreTrainNotificationManager.getTrainPreference(context, train.trainNumber)
        }

        TrainNotificationConfigDialog(
            train = train,
            initialPref = currentPref,
            isOdia = isOdia,
            onDismiss = { trainToConfigureAlerts = null },
            onSaveAndTrack = { updatedPref ->
                BalasoreTrainNotificationManager.setTrackedTrain(context, train.trainNumber, updatedPref)
                trackedSet = BalasoreTrainNotificationManager.getTrackedTrainNumbers(context)
                trainToConfigureAlerts = null
                Toast.makeText(
                    context,
                    if (isOdia) "ଟ୍ରେନ୍ ${train.trainNumber} ପାଇଁ ନୋଟିଫିକେସନ୍ ସେଟିଂସ ସଂରକ୍ଷିତ ହେଲା!" else "Notification preferences saved for ${train.trainNumber}!",
                    Toast.LENGTH_SHORT
                ).show()
            },
            onTestPushNotification = {
                BalasoreTrainNotificationManager.sendImmediateTestPushNotification(context, train)
                Toast.makeText(
                    context,
                    if (isOdia) "ପରୀକ୍ଷାମୂଳକ ନୋଟିଫିକେସନ୍ ପଠାଗଲା! ନୋଟିଫିକେସନ୍ ବାର୍ ଯାଞ୍ଚ କରନ୍ତୁ।" else "Test push alert dispatched! Check your notification shade.",
                    Toast.LENGTH_LONG
                ).show()
            },
            onUntrack = {
                BalasoreTrainNotificationManager.untrackTrain(context, train.trainNumber)
                trackedSet = BalasoreTrainNotificationManager.getTrackedTrainNumbers(context)
                trainToConfigureAlerts = null
                Toast.makeText(
                    context,
                    if (isOdia) "ଟ୍ରାକିଂ ନିଷ୍କ୍ରିୟ ହେଲା" else "Alerts turned off for ${train.trainNumber}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )
    }
}

/**
 * Top Station Telemetry Header Banner with dynamic radar pulse, countdown ticker, and station features.
 */
@Composable
private fun StationTelemetryHeaderCard(
    isOdia: Boolean,
    secondsToNextRefresh: Int,
    lastUpdatedTime: String,
    onManualRefresh: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF22C55E))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOdia) "ଲାଇଭ୍ ଟେଲିମେଟ୍ରି ସକ୍ରିୟ" else "LIVE TELEMETRY FEED",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF22C55E),
                            letterSpacing = 1.1.sp
                        )
                    )
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.12f),
                    modifier = Modifier.clickable { onManualRefresh() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "ଆପଡେଟ୍: ${secondsToNextRefresh}s" else "Next sync: ${secondsToNextRefresh}s",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = if (isOdia) "ବାଲେଶ୍ୱର ରେଳ ଜଙ୍କସନ୍ (BLS)" else "Balasore Junction (BLS)",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            )

            Text(
                text = if (isOdia) "ପ୍ଲାଟଫର୍ମ ୧ ରୁ ୪ • ହାୱଡ଼ା-ଚେନ୍ନାଇ ମୁଖ୍ୟ ଲାଇନ୍ • ଦକ୍ଷିଣ ପୂର୍ବ ରେଳପଥ" else "Platforms 1 to 4 • Howrah-Chennai Mainline • South Eastern Railway",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp
                )
            )

            Spacer(modifier = Modifier.height(14.dp))

            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isOdia) "ଶେଷ ସମୟାଙ୍କନ: $lastUpdatedTime" else "Last feed sync: $lastUpdatedTime",
                    fontSize = 11.sp,
                    color = Color(0xFF94A3B8)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF0284C7).copy(alpha = 0.25f),
                    border = BorderStroke(1.dp, Color(0xFF0284C7).copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "Category NSG-3 / A1",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

/**
 * Summary metrics cards showing counts for total, on-time, delayed, approaching, and tracked trains.
 */
@Composable
private fun TrainMetricsSummaryRow(
    total: Int,
    onTime: Int,
    delayed: Int,
    approaching: Int,
    tracked: Int,
    isOdia: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricMiniCard(
            label = if (isOdia) "ସମୁଦାୟ" else "Total",
            count = "$total",
            bgColor = Color(0xFFE2E8F0),
            textColor = Color(0xFF334155),
            modifier = Modifier.weight(1f)
        )
        MetricMiniCard(
            label = if (isOdia) "ସମୟାନୁ." else "On Time",
            count = "$onTime",
            bgColor = Color(0xFFDCFCE7),
            textColor = Color(0xFF15803D),
            modifier = Modifier.weight(1f)
        )
        MetricMiniCard(
            label = if (isOdia) "ବିଳମ୍ବିତ" else "Delayed",
            count = "$delayed",
            bgColor = Color(0xFFFFEDD5),
            textColor = Color(0xFFC2410C),
            modifier = Modifier.weight(1f)
        )
        MetricMiniCard(
            label = if (isOdia) "ନିକଟବର୍ତ୍ତୀ" else "Near BLS",
            count = "$approaching",
            bgColor = Color(0xFFE0F2FE),
            textColor = Color(0xFF0369A1),
            modifier = Modifier.weight(1f)
        )
        MetricMiniCard(
            label = if (isOdia) "ଚିହ୍ନିତ" else "Tracked",
            count = "$tracked",
            bgColor = Color(0xFFF3E8FF),
            textColor = Color(0xFF7E22CE),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun MetricMiniCard(
    label: String,
    count: String,
    bgColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Black,
                    color = textColor
                )
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = textColor.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Filter tabs for Arrivals, Departures, All, and Tracked.
 */
@Composable
private fun MovementFilterTabs(
    selectedMovement: TrainMovement,
    trackedCount: Int,
    isOdia: Boolean,
    onSelectMovement: (TrainMovement) -> Unit
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFE2E8F0),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TrainMovement.values().forEach { movement ->
                val isSelected = selectedMovement == movement
                val tabText = when (movement) {
                    TrainMovement.ALL -> if (isOdia) "ସମସ୍ତ" else "All"
                    TrainMovement.ARRIVALS -> if (isOdia) "ଆଗମନ" else "Arrivals"
                    TrainMovement.DEPARTURES -> if (isOdia) "ପ୍ରସ୍ଥାନ" else "Departures"
                    TrainMovement.TRACKED_ONLY -> if (isOdia) "ଚିହ୍ନିତ ($trackedCount)" else "Tracked ($trackedCount)"
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) Color.White else Color.Transparent,
                    shadowElevation = if (isSelected) 2.dp else 0.dp,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectMovement(movement) }
                        .testTag("tab_movement_${movement.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier.padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tabText,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) Color(0xFF0284C7) else Color(0xFF64748B),
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

/**
 * Horizontal scrolling chips for platforms and train categories.
 */
@Composable
private fun FilterChipsBar(
    selectedPlatform: String,
    selectedType: String,
    onlyDelayed: Boolean,
    isOdia: Boolean,
    onSelectPlatform: (String) -> Unit,
    onSelectType: (String) -> Unit,
    onToggleDelayed: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Platform Chips
        listOf("ALL", "PF 1", "PF 2", "PF 3", "PF 4").forEach { pf ->
            val isSelected = selectedPlatform == pf
            FilterChip(
                selected = isSelected,
                onClick = { onSelectPlatform(pf) },
                label = {
                    Text(
                        text = if (pf == "ALL") (if (isOdia) "ସମସ୍ତ ପ୍ଲାଟଫର୍ମ" else "All Platforms") else pf,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF0284C7),
                    selectedLabelColor = Color.White
                )
            )
        }

        // Delay Only Chip
        FilterChip(
            selected = onlyDelayed,
            onClick = onToggleDelayed,
            label = {
                Text(
                    text = if (isOdia) "କେବଳ ବିଳମ୍ବିତ ⚠️" else "Delayed Only ⚠️",
                    fontSize = 11.sp,
                    fontWeight = if (onlyDelayed) FontWeight.Bold else FontWeight.Normal
                )
            },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFFEA580C),
                selectedLabelColor = Color.White
            )
        )

        // Vande Bharat Only Chip
        FilterChip(
            selected = selectedType == "VANDE_BHARAT",
            onClick = {
                onSelectType(if (selectedType == "VANDE_BHARAT") "ALL" else "VANDE_BHARAT")
            },
            label = {
                Text(
                    text = "Vande Bharat ⚡",
                    fontSize = 11.sp,
                    fontWeight = if (selectedType == "VANDE_BHARAT") FontWeight.Bold else FontWeight.Normal
                )
            },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF7C3AED),
                selectedLabelColor = Color.White
            )
        )

        // MEMU Only Chip
        FilterChip(
            selected = selectedType == "MEMU",
            onClick = {
                onSelectType(if (selectedType == "MEMU") "ALL" else "MEMU")
            },
            label = {
                Text(
                    text = "MEMU Local 🚉",
                    fontSize = 11.sp
                )
            },
            shape = RoundedCornerShape(20.dp),
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Color(0xFF0284C7),
                selectedLabelColor = Color.White
            )
        )
    }
}

/**
 * Detailed interactive Train Card showing train number, name, timings, platform, live location,
 * speed, coach composition, and push notification actions.
 */
@Composable
private fun TrainCard(
    train: TrainRunningStatus,
    isTracked: Boolean,
    isHighlighted: Boolean,
    isOdia: Boolean,
    onConfigureTracking: () -> Unit,
    onQuickToggleTracking: () -> Unit,
    onShare: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(isHighlighted) }

    val statusBgColor = when (train.statusCategory) {
        TrainStatusCategory.ON_TIME -> Color(0xFFDCFCE7)
        TrainStatusCategory.DELAYED -> Color(0xFFFFEDD5)
        TrainStatusCategory.APPROACHING -> Color(0xFFE0F2FE)
        TrainStatusCategory.AT_STATION -> Color(0xFFFEF08A)
        TrainStatusCategory.DEPARTED -> Color(0xFFF1F5F9)
    }

    val statusTextColor = when (train.statusCategory) {
        TrainStatusCategory.ON_TIME -> Color(0xFF15803D)
        TrainStatusCategory.DELAYED -> Color(0xFFC2410C)
        TrainStatusCategory.APPROACHING -> Color(0xFF0369A1)
        TrainStatusCategory.AT_STATION -> Color(0xFF854D0E)
        TrainStatusCategory.DEPARTED -> Color(0xFF64748B)
    }

    val cardBorder = when {
        isHighlighted -> BorderStroke(2.dp, Color(0xFF0284C7))
        isTracked -> BorderStroke(1.5.dp, Color(0xFF8B5CF6))
        train.type == TrainType.VANDE_BHARAT -> BorderStroke(1.dp, Color(0xFFC4B5FD))
        train.delayMinutes > 5 -> BorderStroke(1.dp, Color(0xFFFED7AA))
        else -> BorderStroke(1.dp, Color(0xFFE2E8F0))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("train_card_${train.trainNumber}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = if (isTracked || isHighlighted) 3.dp else 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Train Number, Type Pill, Status Badge & Notification Bell
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = "#${train.trainNumber}",
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (train.type) {
                            TrainType.VANDE_BHARAT -> Color(0xFFEDE9FE)
                            TrainType.SUPERFAST -> Color(0xFFE0F2FE)
                            TrainType.JAN_SHATABDI -> Color(0xFFDCFCE7)
                            else -> Color(0xFFF1F5F9)
                        }
                    ) {
                        Text(
                            text = if (isOdia) train.type.labelOr else train.type.labelEn,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (train.type) {
                                TrainType.VANDE_BHARAT -> Color(0xFF6D28D9)
                                TrainType.SUPERFAST -> Color(0xFF0369A1)
                                TrainType.JAN_SHATABDI -> Color(0xFF15803D)
                                else -> Color(0xFF475569)
                            },
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Status Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = statusBgColor
                    ) {
                        Text(
                            text = if (isOdia) train.statusCategory.labelOr else train.statusCategory.labelEn,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Bell Button with visual indicator if actively tracked
                    IconButton(
                        onClick = onConfigureTracking,
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("btn_alert_bell_${train.trainNumber}")
                    ) {
                        if (isTracked) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Tracking Active",
                                tint = Color(0xFF7C3AED)
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Outlined.Notifications,
                                contentDescription = "Track Train",
                                tint = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Train Title (En & Or)
            Text(
                text = if (isOdia) train.trainNameOr else train.trainNameEn,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A),
                    fontSize = 16.sp
                )
            )

            if (isOdia && train.trainNameEn.isNotBlank()) {
                Text(
                    text = train.trainNameEn,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Origin -> Destination Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${if (isOdia) train.originNameOr else train.originNameEn} (${train.originCode})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
                Text(
                    text = " ➔ ",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = "${if (isOdia) train.destinationNameOr else train.destinationNameEn} (${train.destinationCode})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timings & Platform Card Row
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Arrival Info
                    Column {
                        Text(
                            text = if (isOdia) "ଆଗମନ (Arrival)" else "Arrival at BLS",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = train.estimatedArrival,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (train.delayMinutes > 5) Color(0xFFC2410C) else Color(0xFF0F172A)
                                )
                            )
                            if (train.delayMinutes > 0) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = train.scheduledArrival,
                                    fontSize = 11.sp,
                                    textDecoration = TextDecoration.LineThrough,
                                    color = Color(0xFF94A3B8)
                                )
                            }
                        }
                    }

                    // Platform with platform changed banner
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (train.isPlatformChanged) Color(0xFFFEE2E2) else Color(0xFF0284C7).copy(alpha = 0.12f),
                            border = BorderStroke(
                                1.dp,
                                if (train.isPlatformChanged) Color(0xFFEF4444) else Color(0xFF0284C7).copy(alpha = 0.25f)
                            )
                        ) {
                            Text(
                                text = train.platform,
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Black,
                                    color = if (train.isPlatformChanged) Color(0xFFDC2626) else Color(0xFF0369A1)
                                ),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        if (train.isPlatformChanged) {
                            Text(
                                text = if (isOdia) "ପ୍ଲାଟଫର୍ମ ପରିବର୍ତ୍ତିତ!" else "PF Changed!",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFDC2626),
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }

                    // Departure Info
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = if (isOdia) "ପ୍ରସ୍ଥାନ (Departure)" else "Departure",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                        Text(
                            text = train.estimatedDeparture,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Current Location & Speed Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📍 ",
                        fontSize = 12.sp
                    )
                    Text(
                        text = if (isOdia) train.currentLocationOr else train.currentLocation,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF334155),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (train.speedKmh > 0) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFF1F5F9),
                        modifier = Modifier.padding(start = 6.dp)
                    ) {
                        Text(
                            text = "${train.speedKmh} km/h",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF475569),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            // Expandable details (Coach composition, Running days, Next station)
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                ) {
                    HorizontalDivider(color = Color(0xFFF1F5F9))
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isOdia) "କୋଚ୍ ବିନ୍ୟାସ: ${train.coachComposition}" else "Coach Rake: ${train.coachComposition}",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isOdia) "ଚଳାଚଳ ଦିନ: ${train.runningDays} • ରହଣି: ${train.haltsBalasoreMins} ମିନିଟ୍" else "Runs: ${train.runningDays} • Balasore Halt: ${train.haltsBalasoreMins} mins",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B)
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (isOdia) "ପରବର୍ତ୍ତୀ ଷ୍ଟପ୍: ${train.nextStationName}" else "Next Major Stop: ${train.nextStationName}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0369A1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Row: Expand Toggle, Quick Bell Tracker, Share Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Expand / Collapse Details
                Text(
                    text = if (isExpanded) {
                        if (isOdia) "କମ୍ ଦେଖନ୍ତୁ ▲" else "Less details ▲"
                    } else {
                        if (isOdia) "ଅଧିକ ବିବରଣୀ ▼" else "More details ▼"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7),
                    modifier = Modifier
                        .clickable { isExpanded = !isExpanded }
                        .padding(vertical = 4.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Share Button
                    OutlinedButton(
                        onClick = onShare,
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isOdia) "ସେୟାର" else "Share",
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Track & Alert Button
                    Button(
                        onClick = onConfigureTracking,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTracked) Color(0xFF7C3AED) else Color(0xFF0284C7)
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("btn_track_notify_${train.trainNumber}")
                    ) {
                        Icon(
                            imageVector = if (isTracked) Icons.Default.Notifications else Icons.Outlined.Notifications,
                            contentDescription = "Notifications",
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (isTracked) {
                                if (isOdia) "ଆଲର୍ଟ ସକ୍ରିୟ ✓" else "Tracking Active ✓"
                            } else {
                                if (isOdia) "ପୁସ୍ ଆଲର୍ଟ ସେଟ୍ 🔔" else "Set Push Alert 🔔"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive Dialog for configuring push notifications for a specific train.
 */
@Composable
private fun TrainNotificationConfigDialog(
    train: TrainRunningStatus,
    initialPref: TrainNotificationPreference,
    isOdia: Boolean,
    onDismiss: () -> Unit,
    onSaveAndTrack: (TrainNotificationPreference) -> Unit,
    onTestPushNotification: () -> Unit,
    onUntrack: () -> Unit
) {
    var notifyApproaching by remember { mutableStateOf(initialPref.notifyOnApproaching) }
    var notifyDelay by remember { mutableStateOf(initialPref.notifyOnDelay) }
    var notifyPlatformChange by remember { mutableStateOf(initialPref.notifyOnPlatformChange) }
    var notifyArrival by remember { mutableStateOf(initialPref.notifyOnArrival) }
    var advanceMinutes by remember { mutableIntStateOf(initialPref.advanceMinutes) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🔔", fontSize = 20.sp, modifier = Modifier.padding(end = 6.dp))
                    Text(
                        text = if (isOdia) "ପୁସ୍ ନୋଟିଫିକେସନ୍ ସେଟ୍ କରନ୍ତୁ" else "Configure Push Alerts",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
                Text(
                    text = "${train.trainNumber} • ${train.trainNameEn}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color(0xFF64748B)
                    )
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = if (isOdia) "ଆପଣ ବାଲେଶ୍ୱର ଷ୍ଟେସନରେ ପହଞ୍ଚିବା ବା ଚଳାଚଳ ସମୟରେ କେଉଁ ସତର୍କତା ଚାହାଁନ୍ତି ତାହା ବାଛନ୍ତୁ:" else "Select which real-time notifications you wish to receive for this train at Balasore (BLS):",
                    fontSize = 12.sp,
                    color = Color(0xFF475569)
                )

                // Toggle 1: Approaching Balasore
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { notifyApproaching = !notifyApproaching }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOdia) "⚡ ବାଲେଶ୍ୱର ନିକଟବର୍ତ୍ତୀ ଆଲର୍ଟ" else "⚡ Approaching Balasore Alert",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isOdia) "ଟ୍ରେନ୍ ୧୫ କି.ମି. ଦୂରରେ ଥିବା ବେଳେ ସୂଚନା" else "Alert when train is within 15 km of Balasore",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = notifyApproaching,
                        onCheckedChange = { notifyApproaching = it },
                        modifier = Modifier.testTag("switch_notify_approaching")
                    )
                }

                // Toggle 2: Delay Notification
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { notifyDelay = !notifyDelay }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOdia) "⚠️ ବିଳମ୍ବ ସତର୍କତା" else "⚠️ Delay Warning Alert",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isOdia) "୫ ମିନିଟରୁ ଅଧିକ ବିଳମ୍ବ ହେଲେ ତୁରନ୍ତ ସୂଚନା" else "Push alert if train runs > 5 mins late",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = notifyDelay,
                        onCheckedChange = { notifyDelay = it },
                        modifier = Modifier.testTag("switch_notify_delay")
                    )
                }

                // Toggle 3: Platform Change
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { notifyPlatformChange = !notifyPlatformChange }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOdia) "🚨 ପ୍ଲାଟଫର୍ମ ପରିବର୍ତ୍ତନ ଘୋଷଣା" else "🚨 Platform Change Alert",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isOdia) "ଷ୍ଟେସନରେ ପ୍ଲାଟଫର୍ମ ବଦଳିଲେ ଉଚ୍ଚ-ପ୍ରାଥମିକତା ଆଲର୍ଟ" else "Instant alert if assigned platform changes",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = notifyPlatformChange,
                        onCheckedChange = { notifyPlatformChange = it },
                        modifier = Modifier.testTag("switch_notify_platform")
                    )
                }

                // Toggle 4: Arrival at Balasore Platform
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { notifyArrival = !notifyArrival }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOdia) "🚉 ପ୍ଲାଟଫର୍ମ ଆଗମନ ଚାଇମ୍" else "🚉 Arrival at Platform Alert",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (isOdia) "ଟ୍ରେନ୍ ପ୍ଲାଟଫର୍ମରେ ପହଞ୍ଚିଲା ବେଳେ ସୂଚନା" else "Alert when train halts at Balasore PF",
                            fontSize = 11.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Switch(
                        checked = notifyArrival,
                        onCheckedChange = { notifyArrival = it },
                        modifier = Modifier.testTag("switch_notify_arrival")
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Immediate Test Push Notification Button
                OutlinedButton(
                    onClick = onTestPushNotification,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("btn_test_push_alert"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color(0xFF7C3AED)
                    ),
                    border = BorderStroke(1.dp, Color(0xFFC4B5FD))
                ) {
                    Text(
                        text = if (isOdia) "⚡ ଏବେ ପରୀକ୍ଷାମୂଳକ ପୁସ୍ ନୋଟିଫିକେସନ୍ ପଠାନ୍ତୁ" else "⚡ Test Push Notification Now",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialPref.copy(
                        isTrackingEnabled = true,
                        notifyOnApproaching = notifyApproaching,
                        notifyOnDelay = notifyDelay,
                        notifyOnPlatformChange = notifyPlatformChange,
                        notifyOnArrival = notifyArrival,
                        advanceMinutes = advanceMinutes
                    )
                    onSaveAndTrack(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                modifier = Modifier.testTag("btn_save_train_alert")
            ) {
                Text(if (isOdia) "ସଂରକ୍ଷଣ କରନ୍ତୁ" else "Save & Enable Alerts")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onUntrack,
                modifier = Modifier.testTag("btn_untrack_train")
            ) {
                Text(
                    text = if (isOdia) "ଟ୍ରାକିଂ ବନ୍ଦ" else "Disable Alerts",
                    color = Color(0xFFEF4444)
                )
            }
        }
    )
}

/**
 * Banner shown when Notification permission has not been granted on Android 13+.
 */
@Composable
private fun NotificationPermissionBanner(
    isOdia: Boolean,
    onRequestPermission: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFEF3C7),
        border = BorderStroke(1.dp, Color(0xFFFDE68A))
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isOdia) "🔔 ନୋଟିଫିକେସନ୍ ଅନୁମତି ଆବଶ୍ୟକ" else "🔔 Push Notifications Disabled",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF92400E)
                )
                Text(
                    text = if (isOdia) "ଟ୍ରେନ୍ ଆସିବା ଓ ବିଳମ୍ବ ସମ୍ବନ୍ଧୀୟ ଆଲର୍ଟ ପାଇବା ପାଇଁ ଅନୁମତି ଦିଅନ୍ତୁ।" else "Allow notifications to receive real-time approaching, delay, and platform alerts.",
                    fontSize = 11.sp,
                    color = Color(0xFFB45309)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onRequestPermission,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.testTag("btn_grant_notify_perm")
            ) {
                Text(if (isOdia) "ଅନୁମତି ଦିଅନ୍ତୁ" else "Enable", fontSize = 11.sp)
            }
        }
    }
}

/**
 * Empty state view when no trains match active filters.
 */
@Composable
private fun EmptyTrainsView(
    isOdia: Boolean,
    onResetFilters: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 24.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "🔍", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = if (isOdia) "କୌଣସି ଟ୍ରେନ୍ ମିଳିଲା ନାହିଁ" else "No matching trains found",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = if (isOdia) "ଦୟାକରି ଖୋଜିବା ଶବ୍ଦ ବା ଫିଲ୍ଟର ପରିବର୍ତ୍ତନ କରନ୍ତୁ।" else "Try adjusting your search query, platform filter, or movement tab.",
                style = MaterialTheme.typography.bodySmall.copy(
                    color = Color(0xFF64748B)
                )
            )
            Spacer(modifier = Modifier.height(14.dp))
            Button(
                onClick = onResetFilters,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text(if (isOdia) "ସମସ୍ତ ଫିଲ୍ଟର ହଟାନ୍ତୁ" else "Reset All Filters")
            }
        }
    }
}
