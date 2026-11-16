package com.example.ui.features.notifications

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.fcm.FcmManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PushNotificationCenterSheet(
    language: com.example.data.model.AppLanguage = com.example.data.model.AppLanguage.ENGLISH,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val fcmToken by FcmManager.fcmToken.collectAsStateWithLifecycle()
    val weatherEnabled by FcmManager.weatherAlertsEnabled.collectAsStateWithLifecycle()
    val breakingNewsEnabled by FcmManager.breakingNewsEnabled.collectAsStateWithLifecycle()
    val defenseEnabled by FcmManager.defenseAdvisoriesEnabled.collectAsStateWithLifecycle()
    val transportEnabled by FcmManager.transportAlertsEnabled.collectAsStateWithLifecycle()
    val chandipurHighTideEnabled by FcmManager.chandipurHighTideAlertsEnabled.collectAsStateWithLifecycle()
    val floodEnabled by FcmManager.floodAlertsEnabled.collectAsStateWithLifecycle()
    val tourismEnabled by FcmManager.tourismHotspotsEnabled.collectAsStateWithLifecycle()
    val tidalSummary = remember(context) {
        com.example.data.local.TidalLocalStorage.getInstance(context).getTrendSummary()
    }

    val isOdia = language == com.example.data.model.AppLanguage.ODIA

    ModalBottomSheet(
        onDismissRequest = onClose,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("push_notification_center_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = "Push Notifications",
                            tint = Color(0xFF0284C7)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ପୁସ୍ ନୋଟିଫିକେସନ୍ ସେଣ୍ଟର୍" else "Push Notification Center",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isOdia) "ରିଅଲ-ଟାଇମ୍ ସ୍ଥାନୀୟ ସତର୍କତା ଓ ପାଣିପାଗ ସୂଚନା" else "Real-Time News & Weather Alert Channels",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onClose,
                    modifier = Modifier.testTag("close_notification_sheet_btn")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Connection Status Card
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF16A34A))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = if (isOdia) "ସକ୍ରିୟ ପୁସ୍ ନେଟୱାର୍କ (FCM କନେକ୍ଟେଡ୍)" else "Push Alerts Active (FCM Connected)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = if (isOdia) "ଆପଣଙ୍କ ଡିଭାଇସ୍ କୁ ସିଧାସଳଖ ତାଜା ଖବର ଓ ଚେତାବନୀ ଆସିବ" else "Instant heads-up alerts dispatched directly to your device",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                // Alert Channel Toggles Header
                item {
                    Text(
                        text = if (isOdia) "ସତର୍କତା ଚ୍ୟାନେଲ୍ ସେଟିଂସ୍" else "Alert Channel Preferences",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // 1. Breaking Local News Toggle
                item {
                    NotificationChannelCard(
                        title = if (isOdia) "ସ୍ଥାନୀୟ ତାଜା ଖବର ସତର୍କତା" else "Breaking Local News Alerts",
                        description = if (isOdia) "ଜିଲ୍ଲାପାଳଙ୍କ ଜରୁରୀ ନିର୍ଦ୍ଦେଶ ଓ ସ୍ଥାନୀୟ ପ୍ରମୁଖ ଘଟଣାବଳୀ" else "District Magistrate orders, civic advisories & urgent community wire",
                        icon = Icons.Default.Newspaper,
                        iconColor = Color(0xFF2563EB),
                        isChecked = breakingNewsEnabled,
                        onCheckedChange = { FcmManager.setBreakingNewsEnabled(context, it) },
                        testTag = "toggle_breaking_news"
                    )
                }

                // 2. Urgent Weather Warnings Toggle
                item {
                    NotificationChannelCard(
                        title = if (isOdia) "ଜରୁରୀ ପାଣିପାଗ ଓ ବାତ୍ୟା ସତର୍କତା" else "Urgent Weather & Tide Warnings",
                        description = if (isOdia) "ବଙ୍ଗୋପସାଗର ଝଡ଼, ବାତ୍ୟା, ଉଚ୍ଚ ଜୁଆର ଓ କାଳବୈଶାଖୀ ସୂଚନା" else "Bay of Bengal cyclones, squalls, Chandipur tidal surges & Nor'westers",
                        icon = Icons.Default.CloudSync,
                        iconColor = Color(0xFFDC2626),
                        isChecked = weatherEnabled,
                        onCheckedChange = { FcmManager.setWeatherAlertsEnabled(context, it) },
                        testTag = "toggle_weather_alerts"
                    )
                }

                // 3. Transport Delays Toggle
                item {
                    NotificationChannelCard(
                        title = if (isOdia) "ଯାତାୟାତ ଓ ଟ୍ରେନ୍ ବିଳମ୍ବ ସୂଚନା" else "Transit & Train Delay Alerts",
                        description = if (isOdia) "ବାଲେଶ୍ୱର ଜଙ୍କସନ ଟ୍ରେନ୍ ଏବଂ ମୋ ବସ୍ ବିଳମ୍ବ ଅଦ୍ୟତନ" else "Balasore Junction rail delays, platform shifts & OSRTC bus notices",
                        icon = Icons.Default.DirectionsTransit,
                        iconColor = Color(0xFFD97706),
                        isChecked = transportEnabled,
                        onCheckedChange = { FcmManager.setTransportAlertsEnabled(context, it) },
                        testTag = "toggle_transport_alerts"
                    )
                }

                // 4. Defense Advisories Toggle
                item {
                    NotificationChannelCard(
                        title = if (isOdia) "ଉପକୂଳ ପ୍ରତିରକ୍ଷା ଓ DRDO ସୂଚନା" else "Coastal Defense & DRDO Advisories",
                        description = if (isOdia) "ଚାନ୍ଦିପୁର ITR କ୍ଷେପଣାସ୍ତ୍ର ପରୀକ୍ଷା ୱିଣ୍ଡୋ ଓ ବିମାନ/ସମୁଦ୍ର ନିଷେଧାଞ୍ଚଳ" else "ITR Chandipur missile tests, NOTAM clearance & coastal maritime zones",
                        icon = Icons.Default.Shield,
                        iconColor = Color(0xFF059669),
                        isChecked = defenseEnabled,
                        onCheckedChange = { FcmManager.setDefenseAdvisoriesEnabled(context, it) },
                        testTag = "toggle_defense_alerts"
                    )
                }

                // 5. Chandipur Beach 'High Tide' Coastal Safety Alerts Toggle (Local Tidal Data)
                item {
                    NotificationChannelCard(
                        title = if (isOdia) "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ଉଚ୍ଚ ଜୁଆର ସୁରକ୍ଷା ଆଲର୍ଟ" else "Chandipur Beach 'High Tide' Alerts",
                        description = if (isOdia) {
                            "ସ୍ଥାନୀୟ ଜୁଆର ତଥ୍ୟ ଆଧାରିତ ସୁରକ୍ଷା ସତର୍କତା (ବର୍ତ୍ତମାନ ଜଳସ୍ତର: ${String.format("%.1f", tidalSummary.currentHeightMeters)}m • ବେଳାଭୂମି ଦୂରତା: ${String.format("%.1f", tidalSummary.currentRecessionKm)} km)"
                        } else {
                            "Local tidal data safety warnings for Chandipur-on-Sea (Water level: ${String.format("%.1f", tidalSummary.currentHeightMeters)}m • Seabed exposure: ${String.format("%.1f", tidalSummary.currentRecessionKm)} km)"
                        },
                        icon = Icons.Default.Waves,
                        iconColor = Color(0xFF0284C7),
                        isChecked = chandipurHighTideEnabled,
                        onCheckedChange = { FcmManager.setChandipurHighTideAlertsEnabled(context, it) },
                        testTag = "toggle_chandipur_high_tide_alerts"
                    )
                }

                // 6. Emergency Flood & River Inflow Alerts Toggle (Subarnarekha & Budhabalanga)
                item {
                    NotificationChannelCard(
                        title = if (isOdia) "ଜରୁରୀ ବନ୍ୟା ଓ ନଦୀ ଜଳସ୍ତର ସତର୍କତା" else "Emergency Flood & River Alerts",
                        description = if (isOdia) {
                            "ସୁବର୍ଣ୍ଣରେଖା (ରାଜଘାଟ) ଓ ବୁଢ଼ାବଳଙ୍ଗ ନଦୀର ଜଳସ୍ତର ଏବଂ ଗାଲୁଡ଼ିହ ବ୍ୟାରେଜ୍ ଡିସଚାର୍ଜ ଆଲର୍ଟ"
                        } else {
                            "Subarnarekha (Rajghat) & Budhabalanga river danger levels, Galudih barrage outflow & flash flood warnings"
                        },
                        icon = Icons.Default.WaterDamage,
                        iconColor = Color(0xFF0284C7),
                        isChecked = floodEnabled,
                        onCheckedChange = { FcmManager.setFloodAlertsEnabled(context, it) },
                        testTag = "toggle_flood_alerts"
                    )
                }

                // 7. Tourism Hotspots & Cultural Updates Toggle
                item {
                    NotificationChannelCard(
                        title = if (isOdia) "ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ଓ ଐତିହ୍ୟ ଅଦ୍ୟତନ" else "Tourism Hotspots & Heritage Updates",
                        description = if (isOdia) {
                            "ଚାନ୍ଦିପୁର ସୁରକ୍ଷିତ ସମୁଦ୍ର ଯାତ୍ରା ସମୟ, କୁଲଡିହା ବନ୍ୟପ୍ରାଣୀ ସଫାରି ପରମିଟ୍ ଓ ପଞ୍ଚଲିଙ୍ଗେଶ୍ୱର ଦର୍ଶନ ସୂଚନା"
                        } else {
                            "Chandipur tidal walk hours, Kuldiha wildlife sanctuary safaris & Panchalingeswar hill stream updates"
                        },
                        icon = Icons.Default.Landscape,
                        iconColor = Color(0xFF0D9488),
                        isChecked = tourismEnabled,
                        onCheckedChange = { FcmManager.setTourismHotspotsEnabled(context, it) },
                        testTag = "toggle_tourism_alerts"
                    )
                }

                // Sync with Local News Preferences Card
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sync_push_news_prefs_card")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoMode,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = if (isOdia) "ସ୍ଥାନୀୟ ଖବର ପସନ୍ଦ ଆଧାରରେ ସ୍ୱୟଂକ୍ରିୟ ସିଙ୍କ୍" else "Sync Channels With News Preferences",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                            Text(
                                text = if (isOdia)
                                    "ଆପଣଙ୍କ ପସନ୍ଦିତ ଖବର ବିଭାଗ (ପ୍ରତିରକ୍ଷା, ଜରୁରୀ ପାଣିପାଗ, ରାଜନୀତି) ଆଧାରରେ FCM ପୁସ୍ ନୋଟିଫିକେସନ୍ ସ୍ୱୟଂକ୍ରିୟ ଭାବେ ସବସ୍କ୍ରାଇବ୍ କରନ୍ତୁ।"
                                else
                                    "Auto-subscribe to critical 'Defense Coastal Advisories' and 'Emergency Flood Alerts' based on your selected local news categories.",
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = {
                                    val preferredCats = setOf("Defense", "Emergency", "Weather", "Local", "Politics")
                                    FcmManager.syncPushSubscriptionsWithNewsPreferences(context, preferredCats)
                                    Toast.makeText(
                                        context,
                                        if (isOdia) "ପ୍ରତିରକ୍ଷା ଓ ବନ୍ୟା ସତର୍କତା ସଫଳତାର ସହ ସବସ୍କ୍ରାଇବ୍ ହେଲା!"
                                        else "Subscribed to Defense Coastal Advisories & Emergency Flood Alerts based on news preferences!",
                                        Toast.LENGTH_LONG
                                    ).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("apply_sync_news_prefs_btn"),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    text = if (isOdia) "ପସନ୍ଦ ଅନୁଯାୟୀ ସବସ୍କ୍ରାଇବ୍ କରନ୍ତୁ" else "Auto-Subscribe Based on Preferences",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                )
                            }
                        }
                    }
                }

                // Push Simulation Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isOdia) "ରିଅଲ-ଟାଇମ୍ ପୁସ୍ ପରୀକ୍ଷା (ତୁରନ୍ତ ଟେଷ୍ଟ୍)" else "Real-Time Push Test Simulator",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isOdia) "ଡିଭାଇସ୍ ରେ ପ୍ରତ୍ୟକ୍ଷ ଭାବେ ନୋଟିଫିକେସନ୍ ପରୀକ୍ଷା କରନ୍ତୁ" else "Simulate instant heads-up push notifications to your device bar",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Test Breaking News Push Button
                item {
                    Button(
                        onClick = {
                            FcmManager.simulateBreakingNewsPush(context)
                            Toast.makeText(context, if (isOdia) "ତାଜା ଖବର ପୁସ୍ ସତର୍କତା ପ୍ରେରଣ ହେଲା!" else "Breaking News push dispatched!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("test_push_breaking_news_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia) "ତାଜା ଖବର ପୁସ୍ ଟେଷ୍ଟ୍ କରନ୍ତୁ" else "Test Real-Time Breaking News Push",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Test Defense Advisory Push Button
                item {
                    Button(
                        onClick = {
                            FcmManager.simulateDefenseAdvisoryPush(context)
                            Toast.makeText(context, if (isOdia) "ପ୍ରତିରକ୍ଷା ଉପକୂଳ ସତର୍କତା ପୁସ୍ ପ୍ରେରଣ ହେଲା!" else "Defense Coastal Advisory push dispatched!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("test_push_defense_advisory_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia) "ପ୍ରତିରକ୍ଷା ଉପକୂଳ ସତର୍କତା (DRDO) ଟେଷ୍ଟ୍" else "Test Defense Coastal Advisory Push",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Test Emergency Flood Alert Push Button
                item {
                    Button(
                        onClick = {
                            FcmManager.simulateCoastalFloodPush(context)
                            Toast.makeText(context, if (isOdia) "ଜରୁରୀ ବନ୍ୟା ଆଲର୍ଟ ପୁସ୍ ପ୍ରେରଣ ହେଲା!" else "Emergency Flood Alert push dispatched!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("test_push_flood_alert_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.WaterDamage, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia) "ଜରୁରୀ ବନ୍ୟା ସତର୍କତା (ସୁବର୍ଣ୍ଣରେଖା) ଟେଷ୍ଟ୍" else "Test Emergency Flood Alert Push",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Test Weather Alert Push Button
                item {
                    Button(
                        onClick = {
                            FcmManager.simulateWeatherAlertPush(context)
                            Toast.makeText(context, if (isOdia) "ଜରୁରୀ ପାଣିପାଗ ସତର୍କତା ପ୍ରେରଣ ହେଲା!" else "Weather Alert push dispatched!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("test_push_weather_alert_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia) "ପାଣିପାଗ ଚେତାବନୀ ପୁସ୍ ଟେଷ୍ଟ୍ କରନ୍ତୁ" else "Test Urgent Weather Alert Push",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Test Chandipur Beach High Tide Push Button (Based on Local Tidal Data)
                item {
                    Button(
                        onClick = {
                            FcmManager.dispatchChandipurHighTidePushFromLocalData(
                                context = context,
                                bypassToggleCheck = true
                            )
                            Toast.makeText(
                                context,
                                if (isOdia) "ଚାନ୍ଦିପୁର ଉଚ୍ଚ ଜୁଆର ସତର୍କତା ପ୍ରେରଣ ହେଲା!" else "Chandipur Beach High Tide alert dispatched from local tidal data!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("test_push_chandipur_high_tide_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Waves, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia) "ଚାନ୍ଦିପୁର ଉଚ୍ଚ ଜୁଆର ପୁସ୍ ଟେଷ୍ଟ୍ କରନ୍ତୁ" else "Test Chandipur Beach High Tide Push",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Test Tourism Hotspot Push Button
                item {
                    Button(
                        onClick = {
                            FcmManager.triggerSampleTourismHotspot(context)
                            Toast.makeText(
                                context,
                                if (isOdia) "ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ପୁସ୍ ସତର୍କତା ପ୍ରେରଣ ହେଲା!" else "Tourism Hotspot update push dispatched!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .testTag("test_push_tourism_hotspot_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D9488)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Landscape, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia) "ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ପୁସ୍ ଟେଷ୍ଟ୍ କରନ୍ତୁ" else "Test Tourism Hotspots & Heritage Push",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Test Coastal Flood Push Button
                item {
                    OutlinedButton(
                        onClick = {
                            FcmManager.simulateCoastalFloodPush(context)
                            Toast.makeText(context, if (isOdia) "ଉପକୂଳ ବନ୍ୟା ସତର୍କତା ପ୍ରେରଣ ହେଲା!" else "Coastal flood warning dispatched!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("test_push_flood_alert_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Waves, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = if (isOdia) "ଉପକୂଳ ବନ୍ୟା ସତର୍କତା ପରୀକ୍ଷା" else "Test Coastal Flood & River Alert")
                    }
                }

                // Custom Push Notification Dispatcher (Urgent Weather Alert or Major Breaking News)
                item {
                    CustomPushNotificationComposerCard(isOdia = isOdia)
                }

                // Recent Dispatched Push Notifications Log
                item {
                    RecentDispatchedPushLogSection(isOdia = isOdia)
                }
            }
        }
    }
}

@Composable
private fun CustomPushNotificationComposerCard(isOdia: Boolean) {
    val context = LocalContext.current
    var selectedType by remember { mutableStateOf("WEATHER") } // "WEATHER" or "NEWS"
    var customTitle by remember { mutableStateOf("") }
    var customMessage by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("custom_push_composer_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = if (isOdia) "କଷ୍ଟମ୍ ପୁସ୍ ନୋଟିଫିକେସନ୍ ପଠାନ୍ତୁ" else "Send Custom Push Alert to Device",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.primary
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = selectedType == "WEATHER",
                    onClick = { selectedType = "WEATHER" },
                    label = { Text(if (isOdia) "⛈️ ପାଣିପାଗ" else "⛈️ Weather", fontSize = 11.sp) },
                    modifier = Modifier.testTag("custom_push_type_weather_chip")
                )
                FilterChip(
                    selected = selectedType == "HIGH_TIDE",
                    onClick = { selectedType = "HIGH_TIDE" },
                    label = { Text(if (isOdia) "🌊 ଉଚ୍ଚ ଜୁଆର" else "🌊 High Tide", fontSize = 11.sp) },
                    modifier = Modifier.testTag("custom_push_type_high_tide_chip")
                )
                FilterChip(
                    selected = selectedType == "NEWS",
                    onClick = { selectedType = "NEWS" },
                    label = { Text(if (isOdia) "📰 ତାଜା ଖବର" else "📰 Breaking News", fontSize = 11.sp) },
                    modifier = Modifier.testTag("custom_push_type_news_chip")
                )
            }

            OutlinedTextField(
                value = customTitle,
                onValueChange = { customTitle = it },
                label = {
                    Text(
                        when (selectedType) {
                            "WEATHER" -> if (isOdia) "ପାଣିପାଗ ସତର୍କତା ଶୀର୍ଷକ" else "Weather Alert Headline"
                            "HIGH_TIDE" -> if (isOdia) "ଚାନ୍ଦିପୁର ଉଚ୍ଚ ଜୁଆର ଶୀର୍ଷକ" else "Chandipur High Tide Headline"
                            else -> if (isOdia) "ତାଜା ଖବର ଶୀର୍ଷକ" else "Breaking News Headline"
                        }
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_push_title_input"),
                shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
                value = customMessage,
                onValueChange = { customMessage = it },
                label = {
                    Text(if (isOdia) "ସତର୍କତା ବାର୍ତ୍ତା / ବିବରଣୀ" else "Notification Message Body")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("custom_push_message_input"),
                shape = RoundedCornerShape(12.dp)
            )

            Button(
                onClick = {
                    when (selectedType) {
                        "HIGH_TIDE" -> {
                            FcmManager.dispatchChandipurHighTidePushFromLocalData(
                                context = context,
                                customTitle = customTitle.takeIf { it.isNotBlank() },
                                customMessage = customMessage.takeIf { it.isNotBlank() },
                                bypassToggleCheck = true
                            )
                        }
                        "WEATHER" -> {
                            val finalTitle = customTitle.ifBlank { "Urgent Balasore Cyclone & High Tide Warning" }
                            val finalBody = customMessage.ifBlank { "Strong coastal winds (50 km/h) and high tide surge expected at Chandipur & Talasari." }
                            FcmManager.dispatchCustomWeatherAlertPush(
                                context = context,
                                title = finalTitle,
                                message = finalBody,
                                alertLevel = "URGENT"
                            )
                        }
                        else -> {
                            val finalTitle = customTitle.ifBlank { "Breaking: Balasore Collectorate Issues District Update" }
                            val finalBody = customMessage.ifBlank { "Important civic & transit update issued for Balasore Town, Remuna, and coastal blocks." }
                            FcmManager.dispatchCustomBreakingNewsPush(
                                context = context,
                                title = finalTitle,
                                summary = finalBody
                            )
                        }
                    }
                    customTitle = ""
                    customMessage = ""
                    Toast.makeText(
                        context,
                        if (isOdia) "ଡିଭାଇସ୍ କୁ ପୁସ୍ ନୋଟିଫିକେସନ୍ ପଠାଗଲା!" else "Push notification sent directly to device!",
                        Toast.LENGTH_SHORT
                    ).show()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = when (selectedType) {
                        "WEATHER" -> Color(0xFFDC2626)
                        "HIGH_TIDE" -> Color(0xFF0284C7)
                        else -> Color(0xFF2563EB)
                    }
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_custom_push_notification_btn")
            ) {
                Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOdia) "ଡିଭାଇସ୍ କୁ ପୁସ୍ ପଠାନ୍ତୁ" else "Dispatch Push Notification Now",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun RecentDispatchedPushLogSection(isOdia: Boolean) {
    val dispatchedList by FcmManager.recentDispatchedAlerts.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("recent_dispatched_push_log_section"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = if (isOdia) "ସମ୍ପ୍ରତି ପଠାଯାଇଥିବା ପୁସ୍ ସତର୍କତା (${dispatchedList.size})" else "Recent Device Push Notifications (${dispatchedList.size})",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.primary
        )

        dispatchedList.forEach { alert ->
            val isHighTide = alert.channel.contains("HIGH_TIDE") || alert.channel.contains("TIDE")
            val isWeather = alert.channel.contains("WEATHER") || alert.channel.contains("FLOOD")
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        isHighTide -> Color(0xFFE0F2FE)
                        isWeather -> Color(0xFFFEF2F2)
                        else -> Color(0xFFEFF6FF)
                    }
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                isHighTide -> "🌊 CHANDIPUR HIGH TIDE"
                                isWeather -> "⛈️ URGENT WEATHER"
                                else -> "📰 BREAKING NEWS"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when {
                                isHighTide -> Color(0xFF0284C7)
                                isWeather -> Color(0xFFDC2626)
                                else -> Color(0xFF2563EB)
                            }
                        )
                        Text(
                            text = alert.timestampFormatted,
                            fontSize = 10.sp,
                            color = Color(0xFF64748B)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = alert.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = alert.message,
                        fontSize = 11.5.sp,
                        color = Color(0xFF334155)
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationChannelCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconColor: Color,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(22.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
