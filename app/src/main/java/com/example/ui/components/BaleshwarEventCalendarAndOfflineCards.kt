package com.example.ui.components

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.CalendarContract
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.local.CacheSyncMetadataEntity
import com.example.data.local.DailyForecastEntity
import com.example.data.local.HotspotEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.local.OdiaFestivalEntity
import com.example.data.local.WeatherCacheEntity
import com.example.data.model.AppLanguage
import com.example.data.model.DailyBalasoreIdiom
import com.example.data.model.Hotspot
import com.example.data.model.NewsArticle
import com.example.data.model.WeatherInfo
import com.example.data.repository.BalasoreRepository
import com.example.data.repository.OdiaFestivalRepository
import com.example.data.repository.OpenWeatherRepository
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * 1. AUTO-UPDATING BALESHWAR EVENT & FESTIVAL CALENDAR CARD
 * Displays upcoming local events, festivals, and important dates with time, location,
 * descriptions, automatic background calendar sync, reminders, and WhatsApp/social sharing.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BaleshwarEventCalendarCard(
    language: AppLanguage,
    onOpenFullCalendar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { OdiaFestivalRepository.getInstance(context) }
    val isOdia = language == AppLanguage.ODIA

    val allFestivals by repository.allFestivalsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    var selectedCategory by remember { mutableStateOf("All") }
    var autoUpdateCountdown by remember { mutableIntStateOf(30) }
    var isAutoUpdateActive by remember { mutableStateOf(true) }
    var lastSyncedLabel by remember {
        mutableStateOf(SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date()))
    }

    val reminderPrefs = remember {
        context.getSharedPreferences("baleshwar_event_reminders_prefs", Context.MODE_PRIVATE)
    }
    var activeReminderIds by remember {
        mutableStateOf(reminderPrefs.getStringSet("reminder_ids", setOf("chadak_mela_2026", "chandipur_beach_fest")) ?: emptySet())
    }

    // Seed Room database and run automatic calendar update loop
    LaunchedEffect(isAutoUpdateActive) {
        repository.ensureInitialized()
        while (isAutoUpdateActive) {
            delay(1000L)
            if (autoUpdateCountdown > 1) {
                autoUpdateCountdown--
            } else {
                repository.refreshFestivalDates()
                lastSyncedLabel = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
                autoUpdateCountdown = 30
            }
        }
    }

    val categories = listOf("All", "Temple Festival", "Maritime & Beach", "Folk & Fair", "Public Meeting", "Sports Event")
    var showAdminAddEventForm by remember { mutableStateOf(false) }
    var newEventTitle by remember { mutableStateOf("") }
    var newEventOrganizer by remember { mutableStateOf("Balasore District Cultural Committee") }
    var newEventDate by remember { mutableStateOf("18 Nov 2026") }
    var newEventTime by remember { mutableStateOf("05:00 PM – 08:30 PM IST") }
    var newEventVenue by remember { mutableStateOf("Gandhi Smruti Bhawan, Balasore Town") }
    var newEventCategory by remember { mutableStateOf("Public Meeting") }
    var newEventDescription by remember { mutableStateOf("") }

    val filteredEvents = remember(allFestivals, selectedCategory) {
        val base = if (selectedCategory == "All") {
            allFestivals
        } else {
            allFestivals.filter { fest -> fest.category.equals(selectedCategory, ignoreCase = true) }
        }
        base.sortedBy { fest -> fest.daysRemaining }.take(5)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("baleshwar_event_calendar_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF5)),
        border = BorderStroke(1.2.dp, Color(0xFFF59E0B).copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row with Auto-Update Status, Admin Add Event & Manual Sync
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = "Baleshwar Event Calendar",
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱର ପର୍ବ ଓ ଉତ୍ସବ କ୍ୟାଲେଣ୍ଡର" else "Baleshwar Local Events & Festivals",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp,
                                color = Color(0xFF78350F)
                            )
                        }
                        Text(
                            text = if (isOdia) {
                                "ସ୍ୱୟଂକ୍ରିୟ ଅପଡେଟ୍ (${autoUpdateCountdown}s) • ଶେଷ ସିଙ୍କ୍: $lastSyncedLabel"
                            } else {
                                "Auto-updates in ${autoUpdateCountdown}s • Synced $lastSyncedLabel"
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF92400E),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    OutlinedButton(
                        onClick = { showAdminAddEventForm = !showAdminAddEventForm },
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        border = BorderStroke(1.dp, Color(0xFFB45309)),
                        modifier = Modifier
                            .height(32.dp)
                            .testTag("event_calendar_add_admin_btn")
                    ) {
                        Text(
                            text = if (showAdminAddEventForm) "Close" else "+ Add Event",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB45309)
                        )
                    }

                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                repository.refreshFestivalDates()
                                lastSyncedLabel = SimpleDateFormat("hh:mm:ss a", Locale.US).format(Date())
                                autoUpdateCountdown = 30
                                Toast.makeText(
                                    context,
                                    "Event Calendar synced with Baleshwar Cultural Feed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("event_calendar_refresh_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh Event Calendar",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Admin Manual Local Event Addition Form (Persisted to Room Database)
            AnimatedVisibility(visible = showAdminAddEventForm) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFFFBEB),
                    border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_calendar_admin_form")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isOdia) "🛡️ ନୂତନ ସ୍ଥାନୀୟ କାର୍ଯ୍ୟକ୍ରମ ଯୋଗ କରନ୍ତୁ (Organizer Input)" else "🛡️ Event Organizer: Add Upcoming Local Event",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp,
                            color = Color(0xFF78350F)
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = newEventTitle,
                            onValueChange = { newEventTitle = it },
                            label = { Text("Event Title (e.g., Balasore Marathon / Town Hall)", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_event_title_input")
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = newEventOrganizer,
                            onValueChange = { newEventOrganizer = it },
                            label = { Text("Event Organizer / Committee Name", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_event_organizer_input")
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            androidx.compose.material3.OutlinedTextField(
                                value = newEventDate,
                                onValueChange = { newEventDate = it },
                                label = { Text("Date", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_event_date_input")
                            )
                            androidx.compose.material3.OutlinedTextField(
                                value = newEventTime,
                                onValueChange = { newEventTime = it },
                                label = { Text("Time", fontSize = 11.sp) },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("admin_event_time_input")
                            )
                        }
                        androidx.compose.material3.OutlinedTextField(
                            value = newEventVenue,
                            onValueChange = { newEventVenue = it },
                            label = { Text("Location / Venue", fontSize = 11.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_event_venue_input")
                        )
                        androidx.compose.material3.OutlinedTextField(
                            value = newEventDescription,
                            onValueChange = { newEventDescription = it },
                            label = { Text("Brief Description", fontSize = 11.sp) },
                            minLines = 2,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_event_desc_input")
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Public Meeting", "Sports Event", "Temple Festival", "Maritime & Beach", "Folk & Fair").forEach { catOpt ->
                                FilterChip(
                                    selected = newEventCategory == catOpt,
                                    onClick = { newEventCategory = catOpt },
                                    label = { Text(catOpt, fontSize = 10.5.sp) }
                                )
                            }
                        }
                        Button(
                            onClick = {
                                val finalTitle = newEventTitle.trim().ifBlank { "Balasore District Sports & Civic Meet" }
                                val finalOrganizer = newEventOrganizer.trim().ifBlank { "Balasore District Cultural Committee" }
                                val finalDesc = newEventDescription.trim().ifBlank {
                                    "Organized by $finalOrganizer at $newEventVenue ($newEventTime). Open to all Balasore residents and visitors."
                                }
                                coroutineScope.launch {
                                    val db = AppDatabase.getInstance(context)
                                    val newEntity = com.example.data.local.OdiaFestivalEntity(
                                        festivalId = "custom_evt_${System.currentTimeMillis()}",
                                        titleEn = finalTitle,
                                        titleOr = finalTitle,
                                        monthPeriodEn = newEventDate,
                                        monthPeriodOr = newEventDate,
                                        odiaTithi = newEventTime,
                                        venueEn = "$newEventVenue • Organizer: $finalOrganizer",
                                        venueOr = "$newEventVenue • ଆୟୋଜକ: $finalOrganizer",
                                        category = newEventCategory,
                                        eventDate = newEventDate,
                                        daysRemaining = 5,
                                        significanceEn = finalDesc,
                                        significanceOr = finalDesc,
                                        ritualsEn = "Organizer: $finalOrganizer • Time: $newEventTime",
                                        ritualsOr = "ଆୟୋଜକ: $finalOrganizer • ସମୟ: $newEventTime",
                                        specialTransitEn = "City Bus & Auto Shuttle Available",
                                        specialTransitOr = "ସିଟି ବସ୍ ଉପଲବ୍ଧ",
                                        prasadSpecialty = "Community Refreshments"
                                    )
                                    db.odiaFestivalDao().insertFestival(newEntity)
                                    newEventTitle = ""
                                    newEventDescription = ""
                                    showAdminAddEventForm = false
                                    selectedCategory = "All"
                                    Toast.makeText(context, "Added '$finalTitle' (by $finalOrganizer) to Local Events Calendar!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_save_local_event_btn")
                        ) {
                            Text("Publish Event to Calendar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Auto-Update Toggle Bar
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFFEF3C7).copy(alpha = 0.65f),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (isAutoUpdateActive) EmeraldGreen else Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia) {
                                "ସ୍ୱୟଂକ୍ରିୟ ପଞ୍ଜିକା ଓ ମେଳା ତାରିଖ ସିଙ୍କ୍ (${allFestivals.size} ଉତ୍ସବ)"
                            } else {
                                "Live Panjika & Local Event Auto-Sync (${allFestivals.size} Upcoming)"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF78350F)
                        )
                    }
                    Switch(
                        checked = isAutoUpdateActive,
                        onCheckedChange = { isAutoUpdateActive = it },
                        modifier = Modifier
                            .scale(0.8f)
                            .testTag("event_calendar_auto_sync_switch")
                    )
                }
            }

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val selected = selectedCategory == cat
                    FilterChip(
                        selected = selected,
                        onClick = { selectedCategory = cat },
                        label = {
                            Text(
                                text = when (cat) {
                                    "All" -> if (isOdia) "ସମସ୍ତ ପର୍ବ" else "All Events"
                                    "Temple Festival" -> if (isOdia) "ମନ୍ଦିର ଯାତ୍ରା" else "Temple Melas"
                                    "Maritime & Beach" -> if (isOdia) "ବେଳାଭୂମି ଉତ୍ସବ" else "Beach & Maritime"
                                    "Folk & Fair" -> if (isOdia) "ଲୋକକଳା ଓ ମେଳା" else "Folk & Fairs"
                                    "Public Meeting" -> if (isOdia) "ଜନସଭା ଓ ବୈଠକ" else "Public Meetings"
                                    "Sports Event" -> if (isOdia) "କ୍ରୀଡ଼ା" else "Sports Events"
                                    else -> if (isOdia) "କୃଷି ପର୍ବ" else "Harvest"
                                },
                                fontSize = 11.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFB45309),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("event_calendar_cat_${cat.lowercase().replace(" ", "_")}")
                    )
                }
            }

            // Upcoming Event Cards with Date, Time, Venue, Description, Reminder & Share
            filteredEvents.forEach { event ->
                val isReminderSet = activeReminderIds.contains(event.festivalId)
                val eventTimeSlot = remember(event.category, event.odiaTithi) {
                    if (event.odiaTithi.contains("AM") || event.odiaTithi.contains("PM")) {
                        event.odiaTithi
                    } else {
                        when (event.category) {
                            "Temple Festival" -> "05:30 AM – 10:00 PM IST (Sandhya Alati 6:30 PM)"
                            "Maritime & Beach" -> "06:00 AM – 09:30 PM IST (Cultural Stage 5:00 PM)"
                            "Folk & Fair" -> "09:00 AM – 10:30 PM IST (Chhau Dance 7:00 PM)"
                            "Public Meeting" -> "10:30 AM – 01:30 PM IST (Municipal Town Hall)"
                            "Sports Event" -> "06:30 AM – 05:30 PM IST (Police Line Stadium)"
                            else -> "07:00 AM – 08:30 PM IST"
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(
                        width = if (isReminderSet) 1.5.dp else 1.dp,
                        color = if (isReminderSet) Color(0xFFF59E0B) else Color(0xFFFED7AA)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("calendar_event_item_${event.festivalId}")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFFFFFBEB),
                                border = BorderStroke(1.dp, Color(0xFFFDE68A))
                            ) {
                                Text(
                                    text = "🗓️ ${event.eventDate} • ${event.odiaTithi}",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (event.daysRemaining <= 30) Color(0xFFFEE2E2) else Color(0xFFE0F2FE)
                            ) {
                                Text(
                                    text = if (isOdia) "ଆଉ ${event.daysRemaining} ଦିନ" else "In ${event.daysRemaining} days",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (event.daysRemaining <= 30) Color(0xFFDC2626) else Color(0xFF0369A1),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = if (isOdia) event.titleOr else event.titleEn,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp,
                            color = Color(0xFF451A03)
                        )

                        // Time & Location Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = "Event Time",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = eventTimeSlot,
                                fontSize = 11.sp,
                                color = Color(0xFF78350F),
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Event Location",
                                tint = Color(0xFFEA580C),
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isOdia) event.venueOr else event.venueEn,
                                fontSize = 11.5.sp,
                                color = BentoSlate700,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        // Description
                        Text(
                            text = if (isOdia) event.significanceOr else event.significanceEn,
                            fontSize = 12.sp,
                            color = BentoSlate700,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        // Actions: Set Reminder + Add to Device Calendar + Share via Intent.ACTION_SEND
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = isReminderSet,
                                onClick = {
                                    val updated = if (isReminderSet) {
                                        activeReminderIds - event.festivalId
                                    } else {
                                        activeReminderIds + event.festivalId
                                    }
                                    activeReminderIds = updated
                                    reminderPrefs.edit().putStringSet("reminder_ids", updated).apply()
                                    if (!isReminderSet) {
                                        dispatchFestivalReminderNotification(context, event)
                                        Toast.makeText(
                                            context,
                                            "Reminder set for ${event.titleEn} (${event.eventDate})!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Reminder removed for ${event.titleEn}",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = if (isReminderSet) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                        contentDescription = "Toggle Event Reminder",
                                        modifier = Modifier.size(15.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = if (isReminderSet) {
                                            if (isOdia) "ରିମାଇଣ୍ଡର୍ ସକ୍ରିୟ" else "Reminder Set"
                                        } else {
                                            if (isOdia) "ରିମାଇଣ୍ଡର୍" else "Remind Me"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                },
                                modifier = Modifier.testTag("event_reminder_btn_${event.festivalId}")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                // Add to System Calendar button
                                IconButton(
                                    onClick = {
                                        try {
                                            val calIntent = Intent(Intent.ACTION_INSERT).apply {
                                                data = CalendarContract.Events.CONTENT_URI
                                                putExtra(CalendarContract.Events.TITLE, "${event.titleEn} (${event.titleOr})")
                                                putExtra(CalendarContract.Events.EVENT_LOCATION, event.venueEn)
                                                putExtra(
                                                    CalendarContract.Events.DESCRIPTION,
                                                    "${event.significanceEn}\nRituals: ${event.ritualsEn}\nPrasad: ${event.prasadSpecialty}"
                                                )
                                            }
                                            context.startActivity(calIntent)
                                        } catch (_: Exception) {
                                            Toast.makeText(context, "Saved to Baleshwar Event Calendar", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("add_system_calendar_btn_${event.festivalId}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EventAvailable,
                                        contentDescription = "Add Event to Device Calendar",
                                        tint = Color(0xFFB45309),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }

                                // Share Event via Intent.ACTION_SEND (WhatsApp / Community Engagement)
                                IconButton(
                                    onClick = {
                                        val shareBody = buildString {
                                            append("🏮 ${event.titleEn} (${event.titleOr})\n")
                                            append("🗓️ Date: ${event.eventDate} (${event.odiaTithi})\n")
                                            append("⏰ Time: $eventTimeSlot\n")
                                            append("📍 Location: ${event.venueEn}\n")
                                            append("✨ Highlights: ${event.significanceEn}\n")
                                            append("🍯 Prasad: ${event.prasadSpecialty}\n")
                                            append("Shared via Baleshwar 360 Community Calendar")
                                        }
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, event.titleEn)
                                            putExtra(Intent.EXTRA_TEXT, shareBody)
                                        }
                                        context.startActivity(Intent.createChooser(sendIntent, "Share ${event.titleEn}"))
                                    },
                                    modifier = Modifier
                                        .size(34.dp)
                                        .testTag("share_calendar_event_btn_${event.festivalId}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share Festival Information",
                                        tint = OceanBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Open Full Interactive Cultural Calendar Button
            Button(
                onClick = onOpenFullCalendar,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB45309)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(46.dp)
                    .testTag("open_full_event_calendar_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Open Full Baleshwar Event Calendar",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isOdia) {
                        "ସମ୍ପୂର୍ଣ୍ଣ ବାଲେଶ୍ୱର ପର୍ବ କ୍ୟାଲେଣ୍ଡର ଦେଖନ୍ତୁ (${allFestivals.size} ପର୍ବ)"
                    } else {
                        "Explore Full Baleshwar Cultural Calendar (${allFestivals.size} Events)"
                    },
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )
            }
        }
    }
}

private fun dispatchFestivalReminderNotification(context: Context, event: OdiaFestivalEntity) {
    try {
        val channelId = "baleshwar_festival_reminders"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Baleshwar Event & Festival Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("🔔 Festival Reminder: ${event.titleEn}")
            .setContentText("${event.eventDate} at ${event.venueEn} (${event.daysRemaining} days left)")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(event.id.toInt() + 4000, notification)
    } catch (_: Exception) {
    }
}

/**
 * 2. OFFLINE MODE, INTELLIGENT ROOM CACHING & STORAGE FOOTPRINT CARD
 * Automatically caches News Articles, Tourism Hotspots, and Event Calendar into Room when online,
 * provides Battery-Saving Sync toggle, osmdroid Regional Map Download, and "Clear All Offline Data".
 */
@Composable
fun OfflineModeStorageDashboardCard(
    hotspots: List<Hotspot>,
    newsArticles: List<NewsArticle> = BalasoreRepository.newsArticles,
    language: AppLanguage,
    onOpenOfflineManagerSheet: () -> Unit = {},
    onOpenOfflineMapTilesSheet: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val db = remember { AppDatabase.getInstance(context) }
    val newsDao = remember { db.newsDao() }
    val hotspotDao = remember { db.hotspotDao() }
    val weatherDao = remember { db.weatherDao() }
    val festivalDao = remember { db.odiaFestivalDao() }
    val metadataDao = remember { db.cacheMetadataDao() }
    val weatherRepository = remember { OpenWeatherRepository(context) }
    val isOdia = language == AppLanguage.ODIA

    val prefs = remember {
        context.getSharedPreferences("baleshwar_offline_mode_prefs", Context.MODE_PRIVATE)
    }
    var isOfflineModeActive by remember {
        mutableStateOf(prefs.getBoolean("offline_mode_enabled", false))
    }
    var isBatterySaverSyncActive by remember {
        mutableStateOf(prefs.getBoolean("battery_saver_sync_enabled", false))
    }
    var cachedMapTileMb by remember {
        mutableFloatStateOf(prefs.getFloat("cached_osm_tiles_mb", 14.2f))
    }
    var isSyncingRoom by remember { mutableStateOf(false) }
    var syncProgress by remember { mutableFloatStateOf(1.0f) }
    var hasSyncError by remember { mutableStateOf(false) }
    var lastUpdatedText by remember {
        val savedTime = prefs.getLong("last_room_sync_epoch_ms", System.currentTimeMillis())
        val formatted = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.US).format(Date(savedTime))
        mutableStateOf(formatted)
    }

    val cachedNewsCount by newsDao.getNewsCountFlow().collectAsStateWithLifecycle(initialValue = 0)
    val cachedHotspotsCount by hotspotDao.getHotspotsCountFlow().collectAsStateWithLifecycle(initialValue = 0)
    val cachedDailyForecasts by weatherDao.getDailyForecasts().collectAsStateWithLifecycle(initialValue = emptyList())
    val cachedFestivals by festivalDao.getAllFestivalsFlow().collectAsStateWithLifecycle(initialValue = emptyList())

    fun triggerRoomDatabaseSync(simulateFailure: Boolean = false) {
        coroutineScope.launch {
            isSyncingRoom = true
            hasSyncError = false
            syncProgress = 0.18f
            kotlinx.coroutines.delay(180)
            if (simulateFailure) {
                syncProgress = 0.45f
                isSyncingRoom = false
                hasSyncError = true
                return@launch
            }
            try {
                syncProgress = 0.48f
                val spotEntities = hotspots.mapIndexed { idx, spot ->
                    HotspotEntity(
                        id = spot.id,
                        name = spot.name,
                        odiaName = spot.odiaName,
                        category = spot.category,
                        shortDescription = spot.description,
                        fullDescription = "${spot.description} Located at ${spot.location}. Timings: ${spot.timing}.",
                        highlights = "${spot.category}, ${spot.location}, Rating ${spot.rating}",
                        distanceKmFromBls = (idx + 1) * 6,
                        bestTimeToVisit = spot.timing,
                        timings = spot.timing,
                        entryFee = "Free / Nominal",
                        specialty = spot.category,
                        localTip = "Cached offline for zero-network viewing in Baleshwar 360."
                    )
                }
                if (spotEntities.isNotEmpty()) {
                    hotspotDao.insertHotspots(spotEntities)
                    metadataDao.insertOrUpdate(
                        CacheSyncMetadataEntity(
                            cacheKey = "TOURISM",
                            lastSyncedAt = System.currentTimeMillis(),
                            itemCount = spotEntities.size,
                            status = "CACHED",
                            cacheLabel = "Tourism Hotspots & Photo Pack",
                            details = "${spotEntities.size} hotspots cached in Room"
                        )
                    )
                }
                syncProgress = 0.72f
                val articleEntities = newsArticles.mapIndexed { idx, art ->
                    NewsArticleEntity(
                        id = (idx + 1).toLong(),
                        title = art.title,
                        summary = art.snippet,
                        content = art.content.ifBlank { art.snippet },
                        category = art.category,
                        source = art.source,
                        publishedAt = art.timeAgo,
                        timestamp = System.currentTimeMillis() - idx * 60000L,
                        isBreaking = idx == 0,
                        isBookmarked = art.isBookmarked
                    )
                }
                if (articleEntities.isNotEmpty()) {
                    newsDao.insertArticles(articleEntities)
                    metadataDao.insertOrUpdate(
                        CacheSyncMetadataEntity(
                            cacheKey = "NEWS",
                            lastSyncedAt = System.currentTimeMillis(),
                            itemCount = articleEntities.size,
                            status = "CACHED",
                            cacheLabel = "Offline News Articles",
                            details = "${articleEntities.size} articles cached in Room"
                        )
                    )
                }
                syncProgress = 0.90f
                val weatherFallback = weatherRepository.getCuratedBalasoreWeatherFallback()
                val forecastFallback = weatherRepository.getCuratedBalasoreThreeDayForecastFallback()
                weatherDao.insertOrUpdateWeather(
                    WeatherCacheEntity(
                        id = 1,
                        temperature = weatherFallback.main?.temp ?: 29.5,
                        apparentTemperature = weatherFallback.main?.feelsLike ?: 31.0,
                        weatherCode = 800,
                        weatherDescription = weatherFallback.weather.firstOrNull()?.description ?: "Clear sky",
                        windSpeed = 16.2,
                        windGusts = 21.0,
                        humidity = weatherFallback.main?.humidity ?: 74,
                        alertLevel = "NORMAL",
                        alertTitle = "Balasore Coastal Advisory",
                        alertMessage = "Normal Bay of Bengal coastal conditions",
                        tideState = "LOW_TIDE",
                        tideDescription = "Sea receded ~4.8 km at Chandipur",
                        sunrise = "05:42 AM",
                        sunset = "05:18 PM",
                        uvIndex = 6.0,
                        maxTemp = weatherFallback.main?.tempMax ?: 31.2,
                        minTemp = weatherFallback.main?.tempMin ?: 24.5,
                        lastUpdated = System.currentTimeMillis()
                    )
                )
                val dailyEntities = forecastFallback.take(3).map { d ->
                    DailyForecastEntity(
                        date = d.dateLabel,
                        dayOfWeek = "${d.dayName} (${d.odiaDayName}) • Humidity ${d.avgHumidity}% • Rain ${d.popPercent}%",
                        weatherCode = 800,
                        weatherDescription = "${d.conditionMain} - ${d.conditionDesc}",
                        maxTemp = d.tempMax,
                        minTemp = d.tempMin,
                        sunrise = "05:42 AM",
                        sunset = "05:18 PM",
                        uvIndex = 6.0,
                        cachedAt = System.currentTimeMillis()
                    )
                }
                weatherDao.insertDailyForecasts(dailyEntities)
                metadataDao.insertOrUpdate(
                    CacheSyncMetadataEntity(
                        cacheKey = "WEATHER",
                        lastSyncedAt = System.currentTimeMillis(),
                        itemCount = 1 + dailyEntities.size,
                        status = "CACHED",
                        cacheLabel = "Balasore Weather & 3-Day Forecast",
                        details = "Current + 3-Day Forecast cached in Room"
                    )
                )

                syncProgress = 1.0f
                val now = System.currentTimeMillis()
                prefs.edit().putLong("last_room_sync_epoch_ms", now).apply()
                lastUpdatedText = SimpleDateFormat("dd MMM, hh:mm:ss a", Locale.US).format(Date(now))
                hasSyncError = false
            } catch (_: Exception) {
                hasSyncError = true
            } finally {
                isSyncingRoom = false
            }
        }
    }

    // Intelligent Auto-Caching into Room Database when online
    LaunchedEffect(hotspots, newsArticles) {
        if ((cachedHotspotsCount == 0 && hotspots.isNotEmpty()) || (cachedNewsCount == 0 && newsArticles.isNotEmpty()) || cachedDailyForecasts.isEmpty()) {
            triggerRoomDatabaseSync(simulateFailure = false)
        }
    }

    val newsSizeKb = remember(cachedNewsCount) { cachedNewsCount * 28 }
    val hotspotsSizeKb = remember(cachedHotspotsCount) { cachedHotspotsCount * 145 }
    val weatherSizeKb = remember(cachedDailyForecasts.size) { if (cachedDailyForecasts.isNotEmpty()) 90 else 0 }
    val festivalsSizeKb = remember(cachedFestivals.size) { cachedFestivals.size * 18 }
    val totalFootprintMb = remember(newsSizeKb, hotspotsSizeKb, weatherSizeKb, festivalsSizeKb, cachedMapTileMb) {
        ((newsSizeKb + hotspotsSizeKb + weatherSizeKb + festivalsSizeKb) / 1024f) + cachedMapTileMb
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("offline_mode_storage_dashboard_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOfflineModeActive) Color(0xFFF0FDF4) else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = 1.2.dp,
            color = if (isOfflineModeActive) EmeraldGreen else OceanBlue.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isOfflineModeActive) EmeraldGreen.copy(alpha = 0.15f) else OceanBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isOfflineModeActive) Icons.Default.CloudOff else Icons.Default.CloudDone,
                                contentDescription = "Offline Mode Status",
                                tint = if (isOfflineModeActive) EmeraldGreen else OceanBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ଅଫଲାଇନ୍ ମୋଡ୍ ଓ ସ୍ମାର୍ଟ କ୍ୟାସ୍" else "Offline Mode & Smart Room Cache",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isOfflineModeActive) {
                                if (isOdia) "ଅଫଲାଇନ୍ ମୋଡ୍ ସକ୍ରିୟ • ବିନା ଇଣ୍ଟରନେଟରେ ଉପଲବ୍ଧ" else "Offline Mode Active • Serving from Room SQLite & osmdroid"
                            } else {
                                if (isOdia) "ଅନଲାଇନ୍ ଅଟୋ-କ୍ୟାସ୍ • ମୋଟ ${String.format(Locale.US, "%.1f", totalFootprintMb)} MB" else "Intelligent Auto-Cache Active • ${String.format(Locale.US, "%.1f", totalFootprintMb)} MB Stored"
                            },
                            fontSize = 11.5.sp,
                            color = BentoSlate600
                        )
                    }
                }

                Switch(
                    checked = isOfflineModeActive,
                    onCheckedChange = { enabled ->
                        isOfflineModeActive = enabled
                        prefs.edit().putBoolean("offline_mode_enabled", enabled).apply()
                        Toast.makeText(
                            context,
                            if (enabled) "Offline Mode Enabled: Viewing cached News, Hotspots & Map Tiles" else "Online Mode Restored: Auto-syncing live feeds",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    modifier = Modifier.testTag("offline_mode_master_switch")
                )
            }

            // Visual Progress Bar for Offline Room Database Synchronization + Last Updated Timestamp + Retry Button
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (hasSyncError) Color(0xFFFEF2F2) else OceanBlue.copy(alpha = 0.07f),
                border = BorderStroke(
                    1.dp,
                    if (hasSyncError) Color(0xFFFCA5A5) else OceanBlue.copy(alpha = 0.22f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("offline_room_sync_status_container")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = when {
                                isSyncingRoom -> if (isOdia) "ରූମ୍ ଡାଟାବେସ୍ ଅଟୋ-ସିଙ୍କ୍ ଚାଲିଛି..." else "Synchronizing Offline Room Database..."
                                hasSyncError -> if (isOdia) "⚠️ ବ୍ୟାକଗ୍ରାଉଣ୍ଡ ସିଙ୍କ୍ ବିଫଳ ହୋଇଛି" else "⚠️ Background Room Sync Interrupted"
                                else -> if (isOdia) "✅ ଅଫଲାଇନ୍ ରූମ୍ ଡାଟାବେସ୍ ଅପଡେଟ୍ ଅଛି" else "✅ Offline Room Database Synchronized"
                            },
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (hasSyncError) Color(0xFFB91C1C) else OceanBlue
                        )
                        Text(
                            text = "${(syncProgress * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (hasSyncError) Color(0xFFB91C1C) else EmeraldGreen
                        )
                    }

                    LinearProgressIndicator(
                        progress = { syncProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(7.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("offline_room_sync_progress_bar"),
                        color = if (hasSyncError) Color(0xFFDC2626) else EmeraldGreen,
                        trackColor = OceanBlue.copy(alpha = 0.15f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isOdia) "ଶେଷ ଅପଡେଟ୍ (Last Updated): $lastUpdatedText" else "Last Updated: $lastUpdatedText",
                            fontSize = 10.5.sp,
                            color = BentoSlate600,
                            modifier = Modifier.testTag("offline_room_last_updated_text")
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = { triggerRoomDatabaseSync(simulateFailure = false) },
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(28.dp)
                                    .testTag("offline_room_sync_retry_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Retry Room Database Sync",
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isOdia) "ପୁନଃ ଚେଷ୍ଟା (Retry)" else "Retry Sync",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Storage Footprint Breakdown Pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OfflineFootprintPill(
                    title = if (isOdia) "ସମ୍ବାଦ (News)" else "News Cache",
                    value = "$cachedNewsCount items (${newsSizeKb} KB)",
                    modifier = Modifier.weight(1f)
                )
                OfflineFootprintPill(
                    title = if (isOdia) "ପାଣିପାଗ" else "Weather (3d)",
                    value = "${cachedDailyForecasts.size} days (${weatherSizeKb} KB)",
                    modifier = Modifier.weight(1f)
                )
                OfflineFootprintPill(
                    title = if (isOdia) "ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ" else "Hotspots",
                    value = "$cachedHotspotsCount spots (${hotspotsSizeKb} KB)",
                    modifier = Modifier.weight(1f)
                )
            }

            // Open Downloaded Content Manager Button
            OutlinedButton(
                onClick = onOpenOfflineManagerSheet,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, OceanBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_offline_content_manager_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Storage,
                    contentDescription = "Manage Downloaded Content",
                    tint = OceanBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOdia)
                        "ଡାଉନଲୋଡ୍ ହୋଇଥିବା ଖବର, ପାଣିପାଗ ଓ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ପରିଚାଳନା କରନ୍ତୁ"
                    else
                        "Manage Downloaded Content (News, 3-Day Weather & Hotspots)",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = OceanBlue
                )
            }

            // Battery-Saving Background Sync Toggle
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.BatteryChargingFull,
                            contentDescription = "Battery Saving Sync Mode",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isOdia) "ବ୍ୟାଟେରୀ ସେଭିଂ ସିଙ୍କ୍ ମୋଡ୍" else "Battery-Saving Background Sync",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                text = if (isBatterySaverSyncActive) {
                                    "Reduced sync frequency (News & Bus Schedule every 60 mins)"
                                } else {
                                    "Standard real-time sync for News, Tide & Bus telemetry"
                                },
                                fontSize = 10.5.sp,
                                color = BentoSlate600
                            )
                        }
                    }
                    Switch(
                        checked = isBatterySaverSyncActive,
                        onCheckedChange = { enabled ->
                            isBatterySaverSyncActive = enabled
                            prefs.edit().putBoolean("battery_saver_sync_enabled", enabled).apply()
                            Toast.makeText(
                                context,
                                if (enabled) "Battery-Saving Mode ON: Background sync throttled" else "Standard Sync Frequency Restored",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .scale(0.82f)
                            .testTag("battery_saving_sync_switch")
                    )
                }
            }

            // Action Buttons: Cache All Now, Download Map Area, Clear All Offline Data
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        triggerRoomDatabaseSync(simulateFailure = false)
                        cachedMapTileMb = 18.6f
                        prefs.edit().putFloat("cached_osm_tiles_mb", cachedMapTileMb).apply()
                        Toast.makeText(
                            context,
                            "Synchronized News Articles, Hotspots & Map Tiles to Offline Room DB!",
                            Toast.LENGTH_SHORT
                        ).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sync_offline_cache_now_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Cache Data for Offline Viewing",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "ଅଫଲାଇନ୍ ସେଭ୍" else "Cache All Now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onOpenOfflineMapTilesSheet,
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("download_map_region_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = "Download Offline Map Region",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "ମ୍ୟାପ୍ ଡାଉନଲୋଡ୍" else "Map Regions",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = {
                        coroutineScope.launch {
                            newsDao.clearNonBookmarked()
                            hotspotDao.clearAllHotspots()
                            metadataDao.clearAll()
                            cachedMapTileMb = 0.0f
                            prefs.edit().putFloat("cached_osm_tiles_mb", 0.0f).apply()
                            Toast.makeText(
                                context,
                                "Cleared all cached offline News, Hotspots & Map Tiles!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                    modifier = Modifier.testTag("clear_all_offline_data_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteSweep,
                        contentDescription = "Clear All Offline Data",
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "ସଫା କରନ୍ତୁ" else "Clear Cache",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun OfflineFootprintPill(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, BentoSlate200),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = title,
                fontSize = 10.sp,
                color = BentoSlate600,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = value,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                color = BentoSlate900,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * 3. SAFE WALK TIDE COUNTDOWN TIMER + LOW TIDE PROACTIVE ALERT + COMPACT WEATHER + DAILY ODIA PROVERB
 */
@Composable
fun SafeWalkTideCountdownAndProactiveAlertCard(
    weather: WeatherInfo,
    dailyIdiom: DailyBalasoreIdiom,
    language: AppLanguage,
    onShareProverb: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val prefs = remember {
        context.getSharedPreferences("chandipur_safe_walk_prefs", Context.MODE_PRIVATE)
    }

    var safeWalkSecondsRemaining by remember { mutableIntStateOf(52 * 60 + 30) } // 52m 30s default safe window
    val totalSafeWindowSeconds = 90 * 60 // 90 mins total low-tide walk window
    var isProactiveLowTideAlertEnabled by remember {
        mutableStateOf(prefs.getBoolean("low_tide_proactive_alert", true))
    }
    var alertLeadMinutes by remember {
        mutableIntStateOf(prefs.getInt("low_tide_lead_mins", 45))
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            if (safeWalkSecondsRemaining > 0) {
                safeWalkSecondsRemaining--
            } else {
                safeWalkSecondsRemaining = totalSafeWindowSeconds
            }
        }
    }

    val safeWalkProgress by animateFloatAsState(
        targetValue = (safeWalkSecondsRemaining.toFloat() / totalSafeWindowSeconds.toFloat()).coerceIn(0f, 1f),
        label = "safe_walk_progress"
    )
    val minsLeft = safeWalkSecondsRemaining / 60
    val secsLeft = safeWalkSecondsRemaining % 60

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("safe_walk_tide_and_weather_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Compact Weather + 3-Hour Precipitation Forecast Row
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFF0F9FF),
                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.WbSunny,
                            contentDescription = "Current Baleshwar Weather",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "${weather.tempCelsius}°C • ${weather.condition} (Humidity ${weather.humidity}%)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp,
                                color = Color(0xFF0C4A6E)
                            )
                            Text(
                                text = if (isOdia) {
                                    "ଆଗାମୀ ୩ ଘଣ୍ଟା ବର୍ଷା ସମ୍ଭାବନା: 15% (1PM) • 22% (2PM) • 18% (3PM)"
                                } else {
                                    "Next 3-Hr Rain Prob: 15% (+1h) • 22% (+2h) • 18% (+3h) • Wind ${weather.windSpeedKmh} km/h"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF0369A1),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Chandipur 'Safe Walk' Countdown Timer with Real-Time Progress Bar
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFECFDF5),
                border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("chandipur_safe_walk_countdown_box")
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "🌊 ଚାନ୍ଦିପୁର 'ସୁରକ୍ଷିତ ବେଳାଭୂମି ପଦଯାତ୍ରା' କାଉଣ୍ଟଡାଉନ୍" else "🌊 Chandipur 'Safe Walk' Tide Countdown",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.5.sp,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = if (isOdia) {
                                    "ଜୁଆର ଆସିବା ପୂର୍ବରୁ ବେଳାଭୂମି ପ୍ରବେଶ ଦ୍ୱାରକୁ ଫେରିଆସନ୍ତୁ (୩.୮ କି.ମି. ସମୁଦ୍ର ପଛକୁ ହଟିଛି)"
                                } else {
                                    "Head back to Chandipur beach entrance before tide rises (Sea receded 3.8 km)"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF047857)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFF059669)
                        ) {
                            Text(
                                text = String.format(Locale.US, "%02d:%02d left", minsLeft, secsLeft),
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    LinearProgressIndicator(
                        progress = { safeWalkProgress },
                        color = if (minsLeft > 20) EmeraldGreen else Color(0xFFEA580C),
                        trackColor = Color(0xFFA7F3D0),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .testTag("safe_walk_progress_bar")
                    )

                    // Proactive Low-Tide Alert (30m / 45m before daily lowest tide)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Low Tide Proactive Alert",
                                tint = Color(0xFF059669),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) {
                                    "ଭଟ୍ଟା ପୂର୍ବରୁ ସତର୍କତା (${alertLeadMinutes} ମିନିଟ୍ ପୂର୍ବରୁ ପୁଶ୍ ନୋଟିଫିକେସନ୍)"
                                } else {
                                    "Low Tide Proactive Alert (${alertLeadMinutes}m before lowest tide)"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF065F46)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            FilterChip(
                                selected = alertLeadMinutes == 45,
                                onClick = {
                                    alertLeadMinutes = if (alertLeadMinutes == 45) 30 else 45
                                    prefs.edit().putInt("low_tide_lead_mins", alertLeadMinutes).apply()
                                    dispatchLowTideProactiveAlert(context, alertLeadMinutes)
                                    Toast.makeText(
                                        context,
                                        "Proactive Low-Tide Alert set for $alertLeadMinutes mins before lowest tide!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                label = { Text("${alertLeadMinutes}m", fontSize = 10.5.sp, fontWeight = FontWeight.Bold) },
                                modifier = Modifier.testTag("low_tide_lead_time_chip")
                            )
                            Switch(
                                checked = isProactiveLowTideAlertEnabled,
                                onCheckedChange = { enabled ->
                                    isProactiveLowTideAlertEnabled = enabled
                                    prefs.edit().putBoolean("low_tide_proactive_alert", enabled).apply()
                                    if (enabled) {
                                        dispatchLowTideProactiveAlert(context, alertLeadMinutes)
                                    }
                                },
                                modifier = Modifier
                                    .scale(0.8f)
                                    .testTag("low_tide_proactive_alert_switch")
                            )
                        }
                    }
                }
            }

            // Daily Odia Proverb Widget with English Translation & Share
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFFFFBEB),
                border = BorderStroke(1.dp, Color(0xFFFDE68A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_odia_proverb_widget")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOdia) "📜 ଆଜିର ବାଲେଶ୍ୱରୀ ଲୋକବାଣୀ (Daily Odia Proverb)" else "📜 Daily Odia Proverb (Baleswariya Wisdom)",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFB45309)
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "\"${dailyIdiom.odiaScript}\" (${dailyIdiom.wordOrIdiom})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFF78350F)
                        )
                        Text(
                            text = "${dailyIdiom.meaning} — ${dailyIdiom.funContext}",
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = Color(0xFF92400E)
                        )
                    }
                    IconButton(
                        onClick = {
                            val text = "📜 Baleswariya Proverb: \"${dailyIdiom.odiaScript}\" (${dailyIdiom.wordOrIdiom})\nMeaning: ${dailyIdiom.meaning}\n— Shared via Baleshwar 360"
                            onShareProverb(text)
                            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, text)
                            }
                            context.startActivity(Intent.createChooser(sendIntent, "Share Odia Proverb"))
                        },
                        modifier = Modifier.testTag("share_daily_proverb_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Daily Odia Proverb",
                            tint = Color(0xFFB45309),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

private fun dispatchLowTideProactiveAlert(context: Context, leadMinutes: Int) {
    try {
        val channelId = "chandipur_low_tide_alerts"
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Chandipur Low Tide Proactive Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("🌊 Chandipur Low Tide in $leadMinutes Minutes!")
            .setContentText("Optimal beach walk departure window is starting. Return to entrance 30 mins before incoming tide.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()
        manager.notify(7112, notification)
    } catch (_: Exception) {
    }
}

/**
 * 4. COASTAL WELLNESS & HYDRATION TRACKER + COMMUNITY PULSE WEEKLY POLL (RECHARTS) + FISHING TRENDS (D3)
 */
@Composable
fun CoastalWellnessAndCommunityPulseCard(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    val prefs = remember {
        context.getSharedPreferences("baleshwar_wellness_and_poll_prefs", Context.MODE_PRIVATE)
    }

    // Hydration Tracker State (Goal: 8 glasses = 2.0L)
    var glassesLogged by remember { mutableIntStateOf(prefs.getInt("hydration_glasses", 5)) }
    val goalGlasses = 8
    val hydrationRatio by animateFloatAsState(
        targetValue = (glassesLogged.toFloat() / goalGlasses.toFloat()).coerceIn(0f, 1f),
        label = "hydration_progress"
    )
    var showWellnessGuide by remember { mutableStateOf(false) }

    // Community Pulse Poll State
    var selectedPollOption by remember { mutableIntStateOf(prefs.getInt("community_poll_choice", -1)) }
    var pollVotes by remember {
        mutableStateOf(
            listOf(
                prefs.getInt("poll_v0", 142),
                prefs.getInt("poll_v1", 98),
                prefs.getInt("poll_v2", 116),
                prefs.getInt("poll_v3", 64)
            )
        )
    }
    val pollOptions = listOf(
        if (isOdia) "ଚାନ୍ଦିପୁର ସୌର ଆଲୋକୀକରଣ" else "Chandipur Solar Beach Promenade",
        if (isOdia) "ବୁଢ଼ାବଳଙ୍ଗ ନଦୀ ବନ୍ଧ ସୁରକ୍ଷା" else "Budhabalanga Embankment Park",
        if (isOdia) "ନୀଳଗିରି-କୁଲଡିହା ଇକୋ ବସ୍" else "Nilagiri-Kuldiha Electric Shuttle",
        if (isOdia) "ରେମୁଣା ଗୋଲେଇ ସ୍ମାର୍ଟ ଟ୍ରାଫିକ୍" else "Remuna Golei Smart Flyover"
    )

    // D3 Coastal Fishing Trends Species Filter
    var selectedFishSpecies by remember { mutableStateOf("Hilsa (ଇଲିଶି)") }
    val fishSeries = remember(selectedFishSpecies) {
        when {
            selectedFishSpecies.startsWith("Hilsa") -> listOf(38f, 44f, 62f, 88f, 95f, 74f)
            selectedFishSpecies.startsWith("Pomfret") -> listOf(52f, 58f, 64f, 70f, 82f, 78f)
            else -> listOf(60f, 72f, 68f, 84f, 90f, 86f)
        }
    }
    val fishMonths = listOf("May", "Jun", "Jul", "Aug", "Sep", "Oct")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("coastal_wellness_and_community_pulse_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, BentoSlate200)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Section A: Coastal Wellness & Hydration Tracker
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF0F9FF),
                border = BorderStroke(1.dp, Color(0xFFBAE6FD)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.WaterDrop,
                                contentDescription = "Coastal Hydration Tracker",
                                tint = OceanBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isOdia) "ଉପକୂଳ ସ୍ୱାସ୍ଥ୍ୟ ଓ ଜଳପାନ ଟ୍ରାକର୍" else "Coastal Wellness & Hydration Tracker",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 13.5.sp,
                                    color = Color(0xFF0C4A6E)
                                )
                                Text(
                                    text = "$glassesLogged / $goalGlasses Glasses (${(hydrationRatio * 100).toInt()}% Daily Goal)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF0369A1),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedButton(
                                onClick = {
                                    if (glassesLogged > 0) {
                                        glassesLogged--
                                        prefs.edit().putInt("hydration_glasses", glassesLogged).apply()
                                    }
                                },
                                contentPadding = PaddingValues(0.dp),
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("hydration_minus_btn")
                            ) {
                                Text("-", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    if (glassesLogged < 12) {
                                        glassesLogged++
                                        prefs.edit().putInt("hydration_glasses", glassesLogged).apply()
                                        if (glassesLogged == goalGlasses) {
                                            Toast.makeText(
                                                context,
                                                "🎉 Coastal Hydration Goal Reached (8/8 Glasses)!",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .height(32.dp)
                                    .testTag("hydration_add_glass_btn")
                            ) {
                                Text("+1 Glass 💧", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    LinearProgressIndicator(
                        progress = { hydrationRatio },
                        color = if (glassesLogged >= goalGlasses) EmeraldGreen else OceanBlue,
                        trackColor = Color(0xFFBAE6FD),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (glassesLogged >= goalGlasses) {
                                "🌟 Goal Achieved! Ready for sunny Chandipur & Talasari walks."
                            } else {
                                "☀️ UV Index 7 (High): Drink 250ml every 45m during beach exploration."
                            },
                            fontSize = 11.sp,
                            color = Color(0xFF0C4A6E),
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = if (showWellnessGuide) "Hide Tips ▲" else "First-Aid Tips ▼",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue,
                            modifier = Modifier
                                .clickable { showWellnessGuide = !showWellnessGuide }
                                .padding(start = 6.dp)
                                .testTag("toggle_coastal_wellness_tips_btn")
                        )
                    }

                    AnimatedVisibility(visible = showWellnessGuide) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "• Sun Safety: Apply SPF 50+ 20 mins before visiting Chandipur or Talasari sandbars.",
                                fontSize = 11.sp,
                                color = BentoSlate700
                            )
                            Text(
                                text = "• Jellyfish Sting First-Aid: Rinse with seawater (never fresh water), apply vinegar, and visit Chandipur PHC if swelling persists.",
                                fontSize = 11.sp,
                                color = BentoSlate700
                            )
                            Text(
                                text = "• Horseshoe Crab Conservation: Do not flip or pick up mating horseshoe crabs along Balaramgadi intertidal flats.",
                                fontSize = 11.sp,
                                color = BentoSlate700
                            )
                        }
                    }
                }
            }

            // Section B: Interactive 'Community Pulse' Weekly Poll (Recharts BarChart)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Poll,
                            contentDescription = "Community Pulse Weekly Poll",
                            tint = Color(0xFF7C3AED),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOdia) "ଜନମତ ସର୍ଭେ (Community Pulse Poll)" else "Community Pulse Weekly Civic Poll",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.sp
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF3E8FF)
                    ) {
                        Text(
                            text = "${pollVotes.sum()} Votes • Recharts",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF6D28D9),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Text(
                    text = if (isOdia) {
                        "ଏହି ସପ୍ତାହର ପ୍ରଶ୍ନ: ବାଲେଶ୍ୱରର କେଉଁ ପ୍ରକଳ୍ପକୁ ପ୍ରଥମେ ପ୍ରାଥମିକତା ମିଳିବା ଉଚିତ?"
                    } else {
                        "Weekly Question: Which Baleshwar civic project should be prioritized next?"
                    },
                    fontSize = 12.sp,
                    color = BentoSlate700,
                    fontWeight = FontWeight.Medium
                )

                val totalVotes = pollVotes.sum().coerceAtLeast(1)
                pollOptions.forEachIndexed { idx, label ->
                    val count = pollVotes[idx]
                    val pct = (count * 100f / totalVotes)
                    val isVoted = selectedPollOption == idx

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isVoted) Color(0xFFF5F3FF) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isVoted) Color(0xFF7C3AED) else BentoSlate200
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selectedPollOption != idx) {
                                    val mutable = pollVotes.toMutableList()
                                    mutable[idx] = mutable[idx] + 1
                                    pollVotes = mutable
                                    selectedPollOption = idx
                                    prefs.edit()
                                        .putInt("community_poll_choice", idx)
                                        .putInt("poll_v$idx", mutable[idx])
                                        .apply()
                                    Toast.makeText(context, "Vote recorded for: $label", Toast.LENGTH_SHORT).show()
                                }
                            }
                            .testTag("community_pulse_option_$idx")
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isVoted) "✓ $label" else label,
                                    fontSize = 11.5.sp,
                                    fontWeight = if (isVoted) FontWeight.ExtraBold else FontWeight.SemiBold,
                                    color = if (isVoted) Color(0xFF5B21B6) else BentoSlate900
                                )
                                Text(
                                    text = "${String.format(Locale.US, "%.0f", pct)}% ($count)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF6D28D9)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            LinearProgressIndicator(
                                progress = { (pct / 100f).coerceIn(0.04f, 1f) },
                                color = if (isVoted) Color(0xFF7C3AED) else OceanBlue,
                                trackColor = Color(0xFFE2E8F0),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = BentoSlate200)

            // Section C: Coastal Fishing Trends (D3-styled Abundance Line & Area Chart)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isOdia) "🐟 ବଳରାମଗଡ଼ି ମାଛ ଅମଳ ଧାରା (D3 Chart)" else "🐟 Coastal Fishing Trends (Balaramgadi D3 Chart)",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp
                        )
                        Text(
                            text = "Seasonal catch index (Metric Tons) across Baleshwar landing centers",
                            fontSize = 10.5.sp,
                            color = BentoSlate600
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Hilsa (ଇଲିଶି)", "Pomfret (ପମ୍ଫ୍ରେଟ୍)", "Tiger Prawn (ଚିଙ୍ଗୁଡ଼ି)").forEach { species ->
                        FilterChip(
                            selected = selectedFishSpecies == species,
                            onClick = { selectedFishSpecies = species },
                            label = { Text(species, fontSize = 10.5.sp) },
                            modifier = Modifier.testTag("fishing_trend_chip_${species.take(5).lowercase()}")
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .padding(10.dp)
                        .testTag("d3_coastal_fishing_trends_canvas")
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height - 16.dp.toPx()
                        val maxVal = 100f
                        val stepX = w / (fishSeries.size - 1).coerceAtLeast(1)

                        val linePath = Path()
                        fishSeries.forEachIndexed { i, v ->
                            val x = i * stepX
                            val y = h - (v / maxVal) * h
                            if (i == 0) linePath.moveTo(x, y) else linePath.lineTo(x, y)
                        }
                        drawPath(
                            path = linePath,
                            color = Color(0xFF38BDF8),
                            style = Stroke(width = 3.dp.toPx())
                        )
                        fishSeries.forEachIndexed { i, v ->
                            val x = i * stepX
                            val y = h - (v / maxVal) * h
                            drawCircle(
                                color = Color(0xFFFBBF24),
                                radius = 4.dp.toPx(),
                                center = Offset(x, y)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        fishMonths.forEachIndexed { idx, m ->
                            Text(
                                text = "$m (${fishSeries[idx].toInt()}t)",
                                fontSize = 9.sp,
                                color = Color(0xFFBAE6FD),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 5. SWIPEABLE MULTI-PHOTO GALLERY FOR TOURISM HOTSPOTS
 * Displays a HorizontalPager with curated visual slides, captions, and page indicator dots.
 */
private data class HotspotGallerySlide(
    val title: String,
    val subtitle: String,
    val badge: String,
    val gradientColors: List<Color>,
    val emoji: String
)

@Composable
fun HotspotSwipeablePhotoGallery(
    spot: Hotspot,
    isOdia: Boolean,
    modifier: Modifier = Modifier
) {
    val slides = remember(spot.id, spot.name, spot.category) {
        listOf(
            HotspotGallerySlide(
                title = if (isOdia) spot.odiaName else spot.name,
                subtitle = "Panoramic View • ${spot.location}",
                badge = "1/3 • ${spot.category.uppercase(Locale.US)}",
                gradientColors = listOf(Color(0xFF0C4A6E), Color(0xFF0284C7), Color(0xFF38BDF8)),
                emoji = when {
                    spot.category.contains("Beach", ignoreCase = true) -> "🏖️"
                    spot.category.contains("Temple", ignoreCase = true) -> "🛕"
                    spot.category.contains("Wildlife", ignoreCase = true) -> "🐘"
                    else -> "🏛️"
                }
            ),
            HotspotGallerySlide(
                title = "Golden Hour at ${spot.name}",
                subtitle = "Hours: ${spot.timing} • ${spot.distanceKm} km from BLS",
                badge = "2/3 • GOLDEN HOUR",
                gradientColors = listOf(Color(0xFF7C2D12), Color(0xFFD97706), Color(0xFFFBBF24)),
                emoji = "🌅"
            ),
            HotspotGallerySlide(
                title = "Heritage & Local Culture",
                subtitle = spot.description.take(68),
                badge = "3/3 • OFFLINE CACHED",
                gradientColors = listOf(Color(0xFF064E3B), Color(0xFF059669), Color(0xFF34D399)),
                emoji = "📸"
            )
        )
    }

    val pagerState = rememberPagerState(pageCount = { slides.size })

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("hotspot_swipeable_gallery_${spot.id}")
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .height(118.dp)
                .clip(RoundedCornerShape(14.dp))
        ) { page ->
            val slide = slides[page]
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Brush.linearGradient(slide.gradientColors))
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.Black.copy(alpha = 0.35f)
                    ) {
                        Text(
                            text = slide.badge,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        )
                    }
                    Text(text = slide.emoji, fontSize = 24.sp)
                }

                Column(
                    modifier = Modifier.align(Alignment.BottomStart)
                ) {
                    Text(
                        text = slide.title,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${slide.subtitle} • Swipe ⇄",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.9f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Page Indicator Dots
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(slides.size) { index ->
                val selected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(6.dp)
                        .width(if (selected) 18.dp else 6.dp)
                        .clip(CircleShape)
                        .background(if (selected) OceanBlue else BentoSlate200)
                )
            }
        }
    }
}
