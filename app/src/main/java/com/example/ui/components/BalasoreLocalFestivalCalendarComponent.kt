package com.example.ui.components

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.OdiaFestivalEntity
import com.example.data.model.AppLanguage
import com.example.data.repository.OdiaFestivalRepository
import com.example.ui.theme.*
import com.example.util.BalasoreHapticHelper
import kotlinx.coroutines.launch

/**
 * BalasoreLocalFestivalCalendarComponent:
 * Jetpack Compose component for the Balasore Local Festival Calendar backed by Room Database
 * (`OdiaFestivalEntity` / `OdiaFestivalDao` via `OdiaFestivalRepository`).
 * Stores and displays dates, Odia tithis, venues, and rich cultural descriptions for major
 * Balasore festivals including Chandipur Beach Festival and Pana Sankranti.
 */
@Composable
fun BalasoreLocalFestivalCalendarComponent(
    language: AppLanguage = AppLanguage.ENGLISH,
    onOpenFullFestivalsScreen: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA
    val repository = remember { OdiaFestivalRepository.getInstance(context) }

    // Reactive Room Database Flows via Repository + collectAsStateWithLifecycle
    val allFestivals by repository.allFestivalsFlow.collectAsStateWithLifecycle(initialValue = emptyList())
    val lastSyncTime by repository.lastSyncTime.collectAsStateWithLifecycle()

    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var expandedFestivalId by remember { mutableStateOf<String?>("chandipur_beach_festival") }
    var showAddFestivalDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        repository.ensureInitialized()
    }

    val displayedFestivals = remember(allFestivals, selectedFilter, searchQuery) {
        allFestivals.filter { fest ->
            val matchesFilter = when (selectedFilter) {
                "ALL" -> true
                "FEATURED" -> fest.festivalId in listOf(
                    "chandipur_beach_festival",
                    "pana_sankranti_odia_nababarsa",
                    "chandaneswar_chadak"
                )
                "STARRED" -> fest.isStarred
                "BEACH" -> fest.category.contains("Beach", ignoreCase = true) ||
                    fest.category.contains("Maritime", ignoreCase = true)
                "TEMPLE" -> fest.category.contains("Temple", ignoreCase = true)
                "FOLK" -> fest.category.contains("Folk", ignoreCase = true) ||
                    fest.category.contains("Harvest", ignoreCase = true)
                else -> true
            }
            val q = searchQuery.trim()
            val matchesSearch = q.isEmpty() ||
                fest.titleEn.contains(q, ignoreCase = true) ||
                fest.titleOr.contains(q, ignoreCase = true) ||
                fest.eventDate.contains(q, ignoreCase = true) ||
                fest.venueEn.contains(q, ignoreCase = true) ||
                fest.significanceEn.contains(q, ignoreCase = true)
            matchesFilter && matchesSearch
        }
    }

    val chandipurFestival = remember(allFestivals) {
        allFestivals.find { it.festivalId == "chandipur_beach_festival" }
    }
    val panaSankrantiFestival = remember(allFestivals) {
        allFestivals.find { it.festivalId == "pana_sankranti_odia_nababarsa" }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balasore_local_festival_calendar_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, Color(0xFFD97706).copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // ================= 1. HEADER & ROOM DB STATUS =================
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFD97706), Color(0xFFB45309))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = "Balasore Festival Calendar",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia)
                                "ବାଲେଶ୍ୱର ପର୍ବପର୍ବାଣୀ କ୍ୟାଲେଣ୍ଡର (Room DB)"
                            else
                                "Balasore Local Festival Calendar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOdia)
                                "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ମହୋତ୍ସବ, ପଣା ସଂକ୍ରାନ୍ତି ଓ ଆଞ୍ଚଳିକ ପର୍ବ (${allFestivals.size} ସେଭ୍)"
                            else
                                "Room SQLite Offline Store • ${allFestivals.size} Festivals Cached (Synced $lastSyncTime)",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                FilledTonalButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        showAddFestivalDialog = true
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFFFEF3C7),
                        contentColor = Color(0xFFB45309)
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(36.dp)
                        .testTag("festival_calendar_add_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "ପର୍ବ ଯୋଡ଼ନ୍ତୁ" else "Add Event",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // ================= 2. SPOTLIGHT CARDS: CHANDIPUR BEACH FESTIVAL & PANA SANKRANTI =================
            if (chandipurFestival != null || panaSankrantiFestival != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("festival_calendar_spotlight_row"),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    chandipurFestival?.let { fest ->
                        SpotlightFestivalMiniCard(
                            badgeEmoji = "🏖️",
                            title = if (isOdia) "ଚାନ୍ଦିପୁର ମହୋତ୍ସବ" else "Chandipur Beach Fest",
                            dateText = fest.eventDate,
                            daysRemaining = fest.daysRemaining,
                            accentColor = OceanBlue,
                            isOdia = isOdia,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedFilter = "ALL"
                                expandedFestivalId = fest.festivalId
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("spotlight_chandipur_beach_festival")
                        )
                    }
                    panaSankrantiFestival?.let { fest ->
                        SpotlightFestivalMiniCard(
                            badgeEmoji = "🏺",
                            title = if (isOdia) "ପଣା ସଂକ୍ରାନ୍ତି" else "Pana Sankranti",
                            dateText = fest.eventDate,
                            daysRemaining = fest.daysRemaining,
                            accentColor = Color(0xFFD97706),
                            isOdia = isOdia,
                            onClick = {
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedFilter = "ALL"
                                expandedFestivalId = fest.festivalId
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("spotlight_pana_sankranti")
                        )
                    }
                }
            }

            // ================= 3. SEARCH & CATEGORY FILTER CHIPS =================
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isOdia)
                            "ଚାନ୍ଦିପୁର ମହୋତ୍ସବ, ପଣା ସଂକ୍ରାନ୍ତି କିମ୍ବା ତାରିଖ ଖୋଜନ୍ତୁ..."
                        else
                            "Search Chandipur Beach Festival, Pana Sankranti, date, or venue...",
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search Festivals",
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = if (searchQuery.isNotEmpty()) {
                    {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear Search", modifier = Modifier.size(16.dp))
                        }
                    }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("festival_calendar_search_input")
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val categories = listOf(
                    "ALL" to (if (isOdia) "ସମସ୍ତ (${allFestivals.size})" else "All (${allFestivals.size})"),
                    "FEATURED" to (if (isOdia) "ପ୍ରମୁଖ ପର୍ବ" else "Chandipur & Pana Sankranti"),
                    "STARRED" to (if (isOdia) "★ ପସନ୍ଦ" else "★ Starred"),
                    "BEACH" to (if (isOdia) "ବେଳାଭୂମି ଓ ବୋଇତ" else "Beach & Maritime"),
                    "TEMPLE" to (if (isOdia) "ମନ୍ଦିର ଯାତ୍ରା" else "Temple Festivals"),
                    "FOLK" to (if (isOdia) "ଲୋକ ମେଳା" else "Folk & Harvest")
                )
                categories.forEach { (key, label) ->
                    val selected = selectedFilter == key
                    FilterChip(
                        selected = selected,
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            selectedFilter = key
                        },
                        label = {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFFEF3C7),
                            selectedLabelColor = Color(0xFF92400E)
                        ),
                        modifier = Modifier.testTag("festival_calendar_filter_$key")
                    )
                }
            }

            // ================= 4. ROOM DATABASE FESTIVAL CALENDAR LIST =================
            if (displayedFestivals.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = BentoSlate50,
                    border = BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isOdia)
                            "କୌଣସି ପର୍ବପର୍ବାଣୀ ମିଳିଲା ନାହିଁ। ଅନ୍ୟ ଏକ ଫିଲ୍ଟର ଚୟନ କରନ୍ତୁ।"
                        else
                            "No matching festivals found in local Room calendar.",
                        fontSize = 12.5.sp,
                        color = BentoSlate600,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                displayedFestivals.take(6).forEach { festival ->
                    val isExpanded = expandedFestivalId == festival.festivalId
                    FestivalCalendarRoomItemCard(
                        festival = festival,
                        isOdia = isOdia,
                        isExpanded = isExpanded,
                        onToggleExpand = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            expandedFestivalId = if (isExpanded) null else festival.festivalId
                        },
                        onToggleStar = {
                            BalasoreHapticHelper.vibrateToggle(context)
                            coroutineScope.launch {
                                repository.toggleStar(festival.id, festival.isStarred)
                            }
                        },
                        onShareFestival = {
                            val shareText = buildString {
                                append("🎉 ${festival.titleEn} (${festival.titleOr})\n")
                                append("📅 Date: ${festival.eventDate} (${festival.monthPeriodEn})\n")
                                append("🕉️ Odia Tithi: ${festival.odiaTithi}\n")
                                append("📍 Venue: ${festival.venueEn}\n")
                                append("✨ Description: ${festival.significanceEn}\n")
                                append("🍯 Prasad / Specialty: ${festival.prasadSpecialty}")
                            }
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_SUBJECT, festival.titleEn)
                                putExtra(Intent.EXTRA_TEXT, shareText)
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Balasore Festival"))
                        }
                    )
                }
            }

            if (onOpenFullFestivalsScreen != null) {
                OutlinedButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        onOpenFullFestivalsScreen()
                    },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFD97706)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("festival_calendar_open_full_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isOdia)
                            "ସମ୍ପୂର୍ଣ୍ଣ ବାଲେଶ୍ୱର ପର୍ବପର୍ବାଣୀ ପଞ୍ଜିକା ଦେଖନ୍ତୁ"
                        else
                            "Open Full Balasore Cultural & Festival Panjika",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }

    // ================= 5. ADD CUSTOM FESTIVAL TO ROOM DB DIALOG =================
    if (showAddFestivalDialog) {
        var newTitle by remember { mutableStateOf("") }
        var newDate by remember { mutableStateOf("14 April 2026") }
        var newVenue by remember { mutableStateOf("Chandipur Beach / Balasore Town") }
        var newCategory by remember { mutableStateOf("Maritime & Beach") }
        var newDescription by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddFestivalDialog = false },
            title = {
                Text(
                    text = if (isOdia) "ନୂତନ ବାଲେଶ୍ୱର ପର୍ବ ଯୋଡ଼ନ୍ତୁ (Room DB)" else "Add Festival to Room Calendar",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text(if (isOdia) "ପର୍ବର ନାମ" else "Festival Name") },
                        placeholder = { Text("e.g., Subarnarekha Sand Art Utsav") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_festival_title_input")
                    )
                    OutlinedTextField(
                        value = newDate,
                        onValueChange = { newDate = it },
                        label = { Text(if (isOdia) "ତାରିଖ" else "Festival Date") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_festival_date_input")
                    )
                    OutlinedTextField(
                        value = newVenue,
                        onValueChange = { newVenue = it },
                        label = { Text(if (isOdia) "ସ୍ଥାନ" else "Venue in Balasore") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_festival_venue_input")
                    )
                    OutlinedTextField(
                        value = newDescription,
                        onValueChange = { newDescription = it },
                        label = { Text(if (isOdia) "ବିବରଣୀ ଓ ମହତ୍ତ୍ୱ" else "Description & Cultural Significance") },
                        minLines = 2,
                        maxLines = 4,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("add_festival_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val saved = repository.addOrganizerEvent(
                                title = newTitle.ifBlank { "Chandipur Coastal Cultural Utsav" },
                                organizerName = "Balasore Cultural Committee",
                                eventDate = newDate,
                                eventTime = "06:00 AM – 09:00 PM IST",
                                venue = newVenue,
                                category = newCategory,
                                description = newDescription.ifBlank {
                                    "Community cultural celebration in Balasore featuring traditional Odia music, handlooms, and coastal delicacies."
                                }
                            )
                            expandedFestivalId = saved.festivalId
                            showAddFestivalDialog = false
                            Toast.makeText(
                                context,
                                if (isOdia) "ପର୍ବ Room ଡାଟାବେସରେ ସେଭ୍ ହେଲା!" else "Festival saved to Room Database!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                    modifier = Modifier.testTag("add_festival_save_btn")
                ) {
                    Text(if (isOdia) "ସେଭ୍ କରନ୍ତୁ" else "Save to Room DB")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddFestivalDialog = false }) {
                    Text(if (isOdia) "ବାତିଲ୍" else "Cancel")
                }
            }
        )
    }
}

@Composable
private fun SpotlightFestivalMiniCard(
    badgeEmoji: String,
    title: String,
    dateText: String,
    daysRemaining: Int,
    accentColor: Color,
    isOdia: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = badgeEmoji, fontSize = 18.sp)
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = accentColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = if (isOdia) "$daysRemaining ଦିନ ବାକି" else "In $daysRemaining d",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                    )
                }
            }
            Text(
                text = title,
                fontSize = 12.5.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = dateText,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun FestivalCalendarRoomItemCard(
    festival: OdiaFestivalEntity,
    isOdia: Boolean,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onToggleStar: () -> Unit,
    onShareFestival: () -> Unit
) {
    val isFeatured = festival.festivalId in listOf(
        "chandipur_beach_festival",
        "pana_sankranti_odia_nababarsa"
    )
    val accentColor = when {
        festival.festivalId == "chandipur_beach_festival" -> OceanBlue
        festival.festivalId == "pana_sankranti_odia_nababarsa" -> Color(0xFFD97706)
        festival.category.contains("Temple", ignoreCase = true) -> Color(0xFFDC2626)
        else -> Color(0xFF059669)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isExpanded) accentColor.copy(alpha = 0.06f) else BentoSlate50,
        border = BorderStroke(
            width = if (isExpanded || isFeatured) 1.4.dp else 1.dp,
            color = if (isExpanded || isFeatured) accentColor.copy(alpha = 0.55f) else BentoBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onToggleExpand() }
            .testTag("festival_calendar_item_${festival.festivalId}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date Badge Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = accentColor.copy(alpha = 0.14f),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(4.dp)
                        ) {
                            val parts = festival.eventDate.split(" ")
                            val dayNum = parts.firstOrNull() ?: "14"
                            val monthShort = parts.getOrNull(1)?.take(3)?.uppercase() ?: "APR"
                            Text(
                                text = dayNum,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Black,
                                color = accentColor
                            )
                            Text(
                                text = monthShort,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = accentColor
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isOdia) festival.titleOr else festival.titleEn,
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${festival.eventDate} • ${if (isOdia) festival.venueOr else festival.venueEn}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onToggleStar,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("festival_star_btn_${festival.festivalId}")
                    ) {
                        Icon(
                            imageVector = if (festival.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Bookmark Festival in Room",
                            tint = if (festival.isStarred) Color(0xFFF59E0B) else BentoSlate400,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = BentoSlate500,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Description preview
            Text(
                text = if (isOdia) festival.significanceOr else festival.significanceEn,
                fontSize = 12.sp,
                color = BentoSlate700,
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 17.sp
            )

            // Expandable Cultural Details stored in Room
            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    HorizontalDivider(color = BentoBorder)

                    FestivalDetailRow(
                        icon = Icons.Default.AutoAwesome,
                        label = if (isOdia) "ଓଡ଼ିଆ ତିଥି:" else "Odia Tithi:",
                        value = festival.odiaTithi,
                        tint = accentColor
                    )
                    FestivalDetailRow(
                        icon = Icons.Default.Celebration,
                        label = if (isOdia) "ରୀତିନୀତି ଓ ଆକର୍ଷଣ:" else "Rituals & Highlights:",
                        value = if (isOdia) festival.ritualsOr else festival.ritualsEn,
                        tint = accentColor
                    )
                    FestivalDetailRow(
                        icon = Icons.Default.Restaurant,
                        label = if (isOdia) "ପ୍ରସାଦ ଓ ବିଶେଷତ୍ୱ:" else "Prasad & Delicacy:",
                        value = festival.prasadSpecialty,
                        tint = accentColor
                    )
                    FestivalDetailRow(
                        icon = Icons.Default.DirectionsBus,
                        label = if (isOdia) "ଯାତାୟାତ ସୂଚନା:" else "Local Transit:",
                        value = if (isOdia) festival.specialTransitOr else festival.specialTransitEn,
                        tint = accentColor
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        OutlinedButton(
                            onClick = onShareFestival,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("festival_share_btn_${festival.festivalId}")
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) "ପର୍ବ ସେୟାର୍ କରନ୍ତୁ" else "Share Festival",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FestivalDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    tint: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier
                .size(16.dp)
                .padding(top = 2.dp)
        )
        Column {
            Text(
                text = label,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = BentoSlate700
            )
            Text(
                text = value,
                fontSize = 11.5.sp,
                color = BentoSlate800,
                lineHeight = 16.sp
            )
        }
    }
}
