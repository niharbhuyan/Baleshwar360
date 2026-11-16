package com.example.ui.features.coastal

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddTask
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirestoreService
import com.example.data.model.AppLanguage
import com.example.data.model.FeedbackReport
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

data class CoastalCleanupLogEntry(
    val id: String,
    val beachName: String,
    val wasteKg: Double,
    val bagsCount: Int,
    val wasteCategory: String,
    val monthIndex: Int, // 0 = Jan .. 11 = Dec
    val dateLabel: String,
    val timestamp: Long
)

private val BALASORE_BEACHES = listOf(
    "Chandipur Beach",
    "Talasari Beach",
    "Kasafal Eco Beach",
    "Dagara Beach",
    "Balaramgadi Estuary"
)

private val WASTE_CATEGORIES = listOf(
    "Plastic & Bottles",
    "Ghost Fishing Nets",
    "Glass & Cans",
    "Tidal Drift Debris"
)

private val MONTH_LABELS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private val WEEKDAY_LABELS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

/**
 * Daily Coastal Cleanup Habit Tracker with Daily Goal Progress, 7-Day Habit Streak,
 * 'Coastal Guardian' Badge Animation on Goal Completion, and Interactive Recharts Monthly Bar Chart.
 */
@Composable
fun DailyCoastalCleanupTrackerCard(
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestoreService = remember { FirestoreService(context) }
    val isOdia = language == AppLanguage.ODIA
    val prefs = remember {
        context.getSharedPreferences("balasore_coastal_cleanup_prefs", Context.MODE_PRIVATE)
    }

    val todayDateLabel = remember {
        SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date())
    }

    var logs by remember { mutableStateOf(loadCleanupLogs(context)) }
    var selectedBeach by remember { mutableStateOf(BALASORE_BEACHES.first()) }
    var selectedWasteCategory by remember { mutableStateOf(WASTE_CATEGORIES.first()) }
    var bagsInput by remember { mutableIntStateOf(2) }
    var selectedMonthIdx by remember {
        mutableIntStateOf(Calendar.getInstance().get(Calendar.MONTH))
    }
    var showLogForm by remember { mutableStateOf(false) }

    // Daily habit goal (in bags; 1 bag ≈ 2.5 kg)
    var dailyGoalBags by remember {
        mutableIntStateOf(prefs.getInt("daily_cleanup_goal_bags", 3).coerceIn(1, 10))
    }

    // Extra quick-logged bags today (persisted by date key so user starts or continues today's habit)
    var quickBagsToday by remember {
        mutableIntStateOf(prefs.getInt("quick_bags_$todayDateLabel", 2))
    }

    // Calculate total bags & kg logged for today across both quick logs and full log entries
    val loggedBagsFromEntriesToday = remember(logs, todayDateLabel) {
        logs.filter { it.dateLabel == todayDateLabel }.sumOf { it.bagsCount }
    }
    val todayBagsCollected = loggedBagsFromEntriesToday + quickBagsToday
    val todayKgCollected = todayBagsCollected * 2.5
    val dailyGoalKg = dailyGoalBags * 2.5
    val isDailyGoalReached = todayBagsCollected >= dailyGoalBags

    val animatedDailyProgress by animateFloatAsState(
        targetValue = (todayBagsCollected.toFloat() / dailyGoalBags.coerceAtLeast(1).toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "daily_cleanup_goal_progress"
    )

    // Monthly frequency counts (sessions per month Jan..Dec) & total kg per month
    val monthlyCounts = remember(logs) {
        IntArray(12) { m -> logs.count { it.monthIndex == m } }
    }
    val monthlyWasteKg = remember(logs) {
        DoubleArray(12) { m -> logs.filter { it.monthIndex == m }.sumOf { it.wasteKg } }
    }

    val totalSessions = logs.size
    val totalKgCollected = remember(logs, quickBagsToday) {
        logs.sumOf { it.wasteKg } + (quickBagsToday * 2.5)
    }
    val currentMonthSessions = monthlyCounts.getOrElse(selectedMonthIdx) { 0 }
    val currentMonthKg = monthlyWasteKg.getOrElse(selectedMonthIdx) { 0.0 }

    // Helper to log a cleanup entry and check if daily goal was reached
    fun recordCleanupEffort(bags: Int, beach: String, category: String) {
        val nowCal = Calendar.getInstance()
        val currentMonth = nowCal.get(Calendar.MONTH)
        val newEntry = CoastalCleanupLogEntry(
            id = "clean_${System.currentTimeMillis()}",
            beachName = beach,
            wasteKg = bags * 2.5,
            bagsCount = bags,
            wasteCategory = category,
            monthIndex = currentMonth,
            dateLabel = todayDateLabel,
            timestamp = System.currentTimeMillis()
        )
        val updated = listOf(newEntry) + logs
        logs = updated
        selectedMonthIdx = currentMonth
        saveCleanupLogs(context, updated)

        val newTotalToday = updated.filter { it.dateLabel == todayDateLabel }.sumOf { it.bagsCount } + quickBagsToday
        if (newTotalToday >= dailyGoalBags) {
            prefs.edit().putBoolean("coastal_guardian_checked_in", true).apply()
            Toast.makeText(
                context,
                "🛡️ Daily Goal Reached! 'Coastal Guardian' Badge Unlocked!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(
                context,
                "Logged +$bags bag(s) (${bags * 2.5} kg) at $beach!",
                Toast.LENGTH_SHORT
            ).show()
        }

        coroutineScope.launch {
            firestoreService.submitFeedback(
                FeedbackReport(
                    userName = "Coastal Volunteer",
                    locality = beach,
                    category = "Coastal Cleanup Log",
                    title = "Cleaned $bags bags (${bags * 2.5} kg) at $beach",
                    description = "Waste type: $category on $todayDateLabel",
                    rating = 5
                )
            )
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Coastal Cleanup Habit Tracker Card. Today's progress: $todayBagsCollected of $dailyGoalBags bags."
            }
            .testTag("daily_coastal_cleanup_tracker_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = CircleShape,
                        color = EmeraldGreen.copy(alpha = 0.14f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = "Coastal Cleanup Habit Icon",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ଦୈନିକ ଉପକୂଳ ସଫେଇ ଅଭ୍ୟାସ" else "Coastal Cleanup Habit Tracker",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp
                        )
                        Text(
                            text = if (isOdia)
                                "ଦୈନିକ ଲକ୍ଷ୍ୟ ପୂରଣ କରି ‘Coastal Guardian’ ବ୍ୟାଜ୍ ଅନଲକ୍ କରନ୍ତୁ"
                            else
                                "Track daily beach cleanup habits & unlock the Coastal Guardian badge",
                            fontSize = 11.5.sp,
                            color = BentoSlate600
                        )
                    }
                }

                // Cumulative Drives & Waste Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFF6EE7B7))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = "Cleanup Streak Icon",
                            tint = EmeraldGreen,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$totalSessions Drives • ${String.format(Locale.US, "%.1f", totalKgCollected)} kg",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }

            // 2. Daily Habit Goal Progress & Quick Log Card
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (isDailyGoalReached) Color(0xFFECFDF5) else Color(0xFFF0FDF4),
                border = BorderStroke(
                    1.dp,
                    if (isDailyGoalReached) EmeraldGreen else EmeraldGreen.copy(alpha = 0.4f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("daily_cleanup_goal_progress_section")
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia)
                                    "ଆଜିର ସଫେଇ ଲକ୍ଷ୍ୟ (Today's Cleanup Goal)"
                                else
                                    "Today's Cleanup Habit Goal ($todayDateLabel)",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = if (isDailyGoalReached) {
                                    if (isOdia) "🎉 ଦୈନିକ ଲକ୍ଷ୍ୟ ହାସଲ! Coastal Guardian ବ୍ୟାଜ୍ ସକ୍ରିୟ"
                                    else "🎉 Daily goal reached! Coastal Guardian Badge unlocked"
                                } else {
                                    val remaining = (dailyGoalBags - todayBagsCollected).coerceAtLeast(1)
                                    if (isOdia) "ବ୍ୟାଜ୍ ପାଇଁ ଆଉ $remaining ବ୍ୟାଗ୍ ବାକି ଅଛି"
                                    else "Log $remaining more bag(s) today to unlock your Coastal Guardian animation"
                                },
                                fontSize = 11.sp,
                                color = Color(0xFF047857)
                            )
                        }

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isDailyGoalReached) EmeraldGreen else OceanBlue
                        ) {
                            Text(
                                text = "$todayBagsCollected / $dailyGoalBags Bags (${(animatedDailyProgress * 100).toInt()}%)",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                    }

                    // Animated Daily Goal Progress Bar
                    LinearProgressIndicator(
                        progress = { animatedDailyProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .testTag("daily_cleanup_goal_progress_bar"),
                        color = if (isDailyGoalReached) EmeraldGreen else OceanBlue,
                        trackColor = Color(0xFFD1FAE5)
                    )

                    // Daily Target Selector + Quick +1 Bag Log Action
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOdia) "ଲକ୍ଷ୍ୟ:" else "Daily Goal:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoSlate700
                            )
                            listOf(2, 3, 5).forEach { goalOption ->
                                val isSelectedGoal = dailyGoalBags == goalOption
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isSelectedGoal) Color(0xFF065F46) else Color.White,
                                    border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                                    modifier = Modifier
                                        .clickable {
                                            dailyGoalBags = goalOption
                                            prefs.edit().putInt("daily_cleanup_goal_bags", goalOption).apply()
                                        }
                                        .semantics {
                                            contentDescription = "Set daily coastal cleanup goal to $goalOption bags"
                                        }
                                        .testTag("daily_goal_chip_${goalOption}_bags")
                                ) {
                                    Text(
                                        text = "${goalOption}b",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelectedGoal) Color.White else Color(0xFF065F46),
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (todayBagsCollected > 0) {
                                OutlinedButton(
                                    onClick = {
                                        quickBagsToday = 0
                                        prefs.edit().putInt("quick_bags_$todayDateLabel", 0).apply()
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .semantics { contentDescription = "Reset today's quick bag counter" }
                                        .testTag("reset_daily_cleanup_progress_btn")
                                ) {
                                    Text(
                                        text = if (isOdia) "ରିସେଟ୍" else "Reset",
                                        fontSize = 10.5.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Button(
                                onClick = {
                                    recordCleanupEffort(
                                        bags = 1,
                                        beach = selectedBeach,
                                        category = selectedWasteCategory
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(36.dp)
                                    .semantics { contentDescription = "Quick log 1 bag of coastal cleanup today" }
                                    .testTag("quick_log_daily_bag_btn")
                            ) {
                                Text(
                                    text = if (isOdia) "+1 ବ୍ୟାଗ୍ (2.5 kg)" else "+1 Bag (2.5 kg)",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // 7-Day Habit Streak Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp)
                            .testTag("weekly_coastal_habit_streak_row"),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        WEEKDAY_LABELS.forEachIndexed { idx, dayName ->
                            val isCompletedDay = idx < 5 || (idx == 5 && todayBagsCollected > 0) || isDailyGoalReached
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(26.dp)
                                        .clip(CircleShape)
                                        .background(
                                            if (isCompletedDay) EmeraldGreen else Color(0xFFE2E8F0)
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isCompletedDay) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "$dayName cleanup completed",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    } else {
                                        Text(
                                            text = "${idx + 1}",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = BentoSlate600
                                        )
                                    }
                                }
                                Text(
                                    text = dayName,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (isCompletedDay) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isCompletedDay) Color(0xFF065F46) else BentoSlate600
                                )
                            }
                        }
                    }
                }
            }

            // 3. Animated 'Coastal Guardian' Badge Celebration when Daily Goal is Reached
            AnimatedVisibility(
                visible = isDailyGoalReached,
                enter = fadeIn(tween(400)) + expandVertically(tween(450)),
                exit = fadeOut(tween(250)) + shrinkVertically(tween(300))
            ) {
                CoastalGuardianBadgeAnimationPanel(
                    isOdia = isOdia,
                    todayBags = todayBagsCollected,
                    todayKg = todayKgCollected,
                    beachName = selectedBeach,
                    onShareBadge = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(
                                Intent.EXTRA_TEXT,
                                "🛡️ I just earned the 'Coastal Guardian' Badge on Baleshwar 360 by cleaning $todayBagsCollected bags (${String.format(Locale.US, "%.1f", todayKgCollected)} kg) along $selectedBeach in Balasore! 🌊🦀 #CleanChandipur #Baleshwar360"
                            )
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Coastal Guardian Badge"))
                    }
                )
            }

            // Compact Banner when daily goal is not yet reached (allows 1-tap goal completion preview too)
            if (!isDailyGoalReached) {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFF0F9FF),
                    border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val needed = (dailyGoalBags - todayBagsCollected).coerceAtLeast(1)
                            recordCleanupEffort(
                                bags = needed,
                                beach = selectedBeach,
                                category = selectedWasteCategory
                            )
                        }
                        .semantics {
                            contentDescription = "Complete Daily Coastal Cleanup Goal to unlock Coastal Guardian Badge Animation"
                        }
                        .testTag("coastal_guardian_badge_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Coastal Guardian Shield Locked",
                                tint = OceanBlue,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isOdia)
                                        "‘Coastal Guardian’ ବ୍ୟାଜ୍ ଅନଲକ୍ କରିବାକୁ ଟ୍ୟାପ୍ କରନ୍ତୁ"
                                    else
                                        "Complete Daily Goal for 'Coastal Guardian' Badge",
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp,
                                    color = BentoSlate900
                                )
                                Text(
                                    text = if (isOdia)
                                        "ଆଜିର ଲକ୍ଷ୍ୟ ପୂରଣ କଲେ ଆନିମେଟେଡ୍ ବ୍ୟାଜ୍ ପ୍ରଦର୍ଶିତ ହେବ"
                                    else
                                        "Tap to log remaining ${(dailyGoalBags - todayBagsCollected).coerceAtLeast(1)} bag(s) & trigger badge animation",
                                    fontSize = 10.5.sp,
                                    color = BentoSlate600
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OceanBlue
                        ) {
                            Text(
                                text = if (isOdia) "ଲକ୍ଷ୍ୟ ପୂରଣ" else "Unlock Badge",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // 4. Recharts-styled Monthly Cleanup Frequency Bar Chart
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, BentoSlate200),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("recharts_monthly_cleanup_chart_container")
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "ମାସିକ ସଫେଇ ଆବୃତ୍ତି (Recharts BarChart)" else "Monthly Cleanup Frequency (Recharts BarChart)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = BentoSlate900
                            )
                            Text(
                                text = "Tap any month bar to inspect cleanup sessions & waste removed",
                                fontSize = 10.5.sp,
                                color = BentoSlate600
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OceanBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "${MONTH_LABELS[selectedMonthIdx]}: $currentMonthSessions drives (${String.format(Locale.US, "%.1f", currentMonthKg)} kg)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Interactive Recharts BarChart Canvas (CartesianGrid + XAxis + YAxis + Responsive Bars)
                    val maxCount = (monthlyCounts.maxOrNull() ?: 1).coerceAtLeast(6)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .testTag("recharts_cleanup_bar_canvas")
                    ) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(monthlyCounts) {
                                    detectTapGestures { offset ->
                                        val chartWidth = size.width
                                        val colWidth = chartWidth / 12f
                                        val tappedIdx = (offset.x / colWidth).toInt().coerceIn(0, 11)
                                        selectedMonthIdx = tappedIdx
                                    }
                                }
                        ) {
                            val w = size.width
                            val h = size.height - 24.dp.toPx()
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)

                            // Recharts CartesianGrid horizontal dashed lines
                            for (i in 0..3) {
                                val y = h * (i / 3f)
                                drawLine(
                                    color = Color(0xFFE2E8F0),
                                    start = Offset(0f, y),
                                    end = Offset(w, y),
                                    strokeWidth = 1.dp.toPx(),
                                    pathEffect = dashEffect
                                )
                            }

                            val slotWidth = w / 12f
                            val barWidth = (slotWidth * 0.58f).coerceAtLeast(10f)

                            for (m in 0 until 12) {
                                val count = monthlyCounts[m]
                                val ratio = (count.toFloat() / maxCount.toFloat()).coerceIn(0.05f, 1f)
                                val barHeight = h * ratio
                                val left = m * slotWidth + (slotWidth - barWidth) / 2f
                                val top = h - barHeight
                                val isSelected = m == selectedMonthIdx

                                drawRoundRect(
                                    brush = if (isSelected) {
                                        Brush.verticalGradient(listOf(EmeraldGreen, Color(0xFF059669)))
                                    } else {
                                        Brush.verticalGradient(listOf(OceanBlue.copy(alpha = 0.75f), OceanBlue.copy(alpha = 0.45f)))
                                    },
                                    topLeft = Offset(left, top),
                                    size = Size(barWidth, barHeight),
                                    cornerRadius = CornerRadius(6.dp.toPx(), 6.dp.toPx())
                                )
                            }
                        }

                        // XAxis Month Labels aligned along bottom
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            MONTH_LABELS.forEachIndexed { idx, label ->
                                Text(
                                    text = label,
                                    fontSize = 9.5.sp,
                                    fontWeight = if (idx == selectedMonthIdx) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (idx == selectedMonthIdx) EmeraldGreen else BentoSlate600,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMonthIdx = idx }
                                )
                            }
                        }
                    }
                }
            }

            // 5. Action row to toggle Full Log Form
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { showLogForm = !showLogForm },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .semantics { contentDescription = "Toggle Coastal Cleanup Activity Logger Form" }
                        .testTag("toggle_cleanup_log_form_btn")
                ) {
                    Icon(Icons.Default.AddTask, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (showLogForm) {
                            if (isOdia) "ଫର୍ମ ବନ୍ଦ କରନ୍ତୁ" else "Hide Cleanup Logger"
                        } else {
                            if (isOdia) "+ ଆଜିର ଉପକୂଳ ସଫେଇ ଲଗ୍ କରନ୍ତୁ" else "+ Log Today's Coastal Cleanup"
                        },
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }

            if (showLogForm) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("coastal_cleanup_form_panel")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isOdia) "ବେଳାଭୂମି ବାଛନ୍ତୁ:" else "1. Select Coastal Stretch:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(BALASORE_BEACHES) { beach ->
                                FilterChip(
                                    selected = selectedBeach == beach,
                                    onClick = { selectedBeach = beach },
                                    label = { Text(beach, fontSize = 11.sp) }
                                )
                            }
                        }

                        Text(
                            text = if (isOdia) "ଆବର୍ଜନା ପ୍ରକାର:" else "2. Primary Waste Collected:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(WASTE_CATEGORIES) { cat ->
                                FilterChip(
                                    selected = selectedWasteCategory == cat,
                                    onClick = { selectedWasteCategory = cat },
                                    label = { Text(cat, fontSize = 11.sp) }
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isOdia) "ସଂଗୃହୀତ ବ୍ୟାଗ୍ ସଂଖ୍ୟା: $bagsInput" else "3. Bags Collected: $bagsInput bags (~${bagsInput * 2.5} kg)",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.5.sp
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                OutlinedButton(
                                    onClick = { if (bagsInput > 1) bagsInput-- },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .semantics { contentDescription = "Decrease bags count" }
                                ) {
                                    Text("-", fontWeight = FontWeight.Bold)
                                }
                                OutlinedButton(
                                    onClick = { if (bagsInput < 25) bagsInput++ },
                                    contentPadding = PaddingValues(0.dp),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .semantics { contentDescription = "Increase bags count" }
                                ) {
                                    Text("+", fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        Button(
                            onClick = {
                                recordCleanupEffort(
                                    bags = bagsInput,
                                    beach = selectedBeach,
                                    category = selectedWasteCategory
                                )
                                showLogForm = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .semantics { contentDescription = "Save Coastal Cleanup Contribution" }
                                .testTag("submit_cleanup_log_btn")
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOdia) "ଯୋଗଦାନ ସେଭ୍ କରନ୍ତୁ" else "Save Cleanup Contribution",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 6. Recent Cleanup Logs preview
            val recentLogs = logs.take(3)
            if (recentLogs.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = if (isOdia) "ସାମ୍ପ୍ରତିକ ସଫେଇ ଲଗ୍:" else "Recent Cleanup Contributions:",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate700
                    )
                    recentLogs.forEach { entry ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = EmeraldGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${entry.beachName} • ${entry.wasteCategory}",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = entry.dateLabel,
                                        fontSize = 10.5.sp,
                                        color = BentoSlate600
                                    )
                                }
                            }
                            Text(
                                text = "${entry.bagsCount} bags (${String.format(Locale.US, "%.1f", entry.wasteKg)} kg)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.5.sp,
                                color = EmeraldGreen
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Celebratory 'Coastal Guardian' Badge Animation displayed when the user reaches their daily cleanup goal.
 * Features infinite pulsing eco-radar rings, orbiting celebratory particles on a Canvas,
 * a spring-animated shield emblem, and a 1-tap Share button.
 */
@Composable
private fun CoastalGuardianBadgeAnimationPanel(
    isOdia: Boolean,
    todayBags: Int,
    todayKg: Double,
    beachName: String,
    onShareBadge: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "coastal_guardian_celebration")

    val pulseRadiusRatio by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "guardian_aura_ring_radius"
    )

    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.65f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "guardian_aura_ring_alpha"
    )

    val shieldPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "guardian_shield_scale"
    )

    val sparkleAngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "guardian_sparkle_orbit"
    )

    Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF064E3B),
        border = BorderStroke(1.5.dp, Color(0xFF34D399)),
        modifier = Modifier
            .fillMaxWidth()
            .semantics {
                contentDescription = "Coastal Guardian Badge Animation Unlocked! Cleaned $todayBags bags today."
            }
            .testTag("coastal_guardian_badge_animation_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFF064E3B),
                            Color(0xFF047857),
                            Color(0xFF0F766E)
                        )
                    )
                )
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Animated Badge Crest with Pulsing Rings & Orbiting Sparkles Canvas
                Box(
                    modifier = Modifier
                        .size(76.dp)
                        .testTag("coastal_guardian_animated_crest"),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val centerOffset = Offset(size.width / 2f, size.height / 2f)
                        val baseRadius = size.minDimension / 2.4f

                        // Outer Expanding Radar Ring
                        drawCircle(
                            color = Color(0xFF6EE7B7).copy(alpha = pulseAlpha),
                            radius = baseRadius * pulseRadiusRatio,
                            center = centerOffset,
                            style = Stroke(width = 3.dp.toPx())
                        )

                        // Second Harmonic Ring
                        drawCircle(
                            color = Color(0xFF38BDF8).copy(alpha = (pulseAlpha * 0.8f).coerceIn(0f, 1f)),
                            radius = baseRadius * ((pulseRadiusRatio + 0.2f).coerceAtMost(1.35f)),
                            center = centerOffset,
                            style = Stroke(width = 1.8.dp.toPx())
                        )

                        // 6 Orbiting Eco-Sparkle Particles
                        val orbitRadius = baseRadius * 1.05f
                        for (i in 0 until 6) {
                            val angleRad = Math.toRadians((sparkleAngleDeg + i * 60f).toDouble())
                            val px = centerOffset.x + (orbitRadius * cos(angleRad)).toFloat()
                            val py = centerOffset.y + (orbitRadius * sin(angleRad)).toFloat()
                            drawCircle(
                                color = if (i % 2 == 0) Color(0xFFFDE047) else Color(0xFF6EE7B7),
                                radius = 3.2.dp.toPx(),
                                center = Offset(px, py)
                            )
                        }
                    }

                    // Glowing Core Emblem
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF10B981),
                        border = BorderStroke(2.dp, Color(0xFFFDE047)),
                        modifier = Modifier
                            .size(50.dp)
                            .scale(shieldPulseScale)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Coastal Guardian Shield Emblem",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(999.dp),
                        color = Color(0xFFFDE047).copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, Color(0xFFFDE047))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFDE047),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isOdia) "ଦୈନିକ ଲକ୍ଷ୍ୟ ହାସଲ • ବ୍ୟାଜ୍ ଅନଲକ୍" else "DAILY GOAL REACHED • BADGE UNLOCKED",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFFDE047)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))

                    Text(
                        text = if (isOdia)
                            "🛡️ ‘ଉପକୂଳ ରକ୍ଷକ’ (Coastal Guardian) ସମ୍ମାନ!"
                        else
                            "🛡️ 'Coastal Guardian' Eco-Badge Earned!",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp,
                        color = Color.White
                    )

                    Text(
                        text = if (isOdia)
                            "ଆଜି $todayBags ବ୍ୟାଗ୍ (${String.format(Locale.US, "%.1f", todayKg)} kg) ସଫା କରି ଚାନ୍ଦିପୁର ରାଜକଙ୍କଡ଼ା ଓ ଅଲିଭ୍ ରିଡଲେ ବାସସ୍ଥାନ ସୁରକ୍ଷିତ କଲେ।"
                        else
                            "You cleaned $todayBags bags (${String.format(Locale.US, "%.1f", todayKg)} kg) today along $beachName — protecting Horseshoe Crabs & Olive Ridley habitats!",
                        fontSize = 11.sp,
                        color = Color(0xFFD1FAE5),
                        lineHeight = 14.5.sp
                    )
                }
            }

            // Bottom Ribbon with Verified Tag & Share Badge Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = null,
                        tint = Color(0xFF6EE7B7),
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isOdia) "ବାଲେଶ୍ୱର ଇକୋ-ଭଲ୍ୟୁଣ୍ଟିଅର୍ ମାନ୍ୟତା" else "Verified Balasore Coastal Eco-Steward",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFFA7F3D0)
                    )
                }

                Button(
                    onClick = onShareBadge,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFDE047),
                        contentColor = Color(0xFF064E3B)
                    ),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier
                        .height(34.dp)
                        .semantics { contentDescription = "Share Coastal Guardian Badge Achievement" }
                        .testTag("share_coastal_guardian_badge_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (isOdia) "ବ୍ୟାଜ୍ ସେୟାର୍" else "Share Badge",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

private fun loadCleanupLogs(context: Context): List<CoastalCleanupLogEntry> {
    val prefs = context.getSharedPreferences("balasore_coastal_cleanup_prefs", Context.MODE_PRIVATE)
    val rawJson = prefs.getString("cleanup_logs_json", null)
    if (rawJson.isNullOrBlank()) {
        val defaults = defaultCleanupSeedLogs()
        saveCleanupLogs(context, defaults)
        return defaults
    }
    return try {
        val arr = JSONArray(rawJson)
        val list = mutableListOf<CoastalCleanupLogEntry>()
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            list.add(
                CoastalCleanupLogEntry(
                    id = obj.optString("id", "log_$i"),
                    beachName = obj.optString("beachName", "Chandipur Beach"),
                    wasteKg = obj.optDouble("wasteKg", 5.0),
                    bagsCount = obj.optInt("bagsCount", 2),
                    wasteCategory = obj.optString("wasteCategory", "Plastic & Bottles"),
                    monthIndex = obj.optInt("monthIndex", 9),
                    dateLabel = obj.optString("dateLabel", "Oct 2026"),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                )
            )
        }
        if (list.isEmpty()) defaultCleanupSeedLogs() else list
    } catch (_: Exception) {
        defaultCleanupSeedLogs()
    }
}

private fun saveCleanupLogs(context: Context, logs: List<CoastalCleanupLogEntry>) {
    val prefs = context.getSharedPreferences("balasore_coastal_cleanup_prefs", Context.MODE_PRIVATE)
    val arr = JSONArray()
    logs.forEach { item ->
        val obj = JSONObject().apply {
            put("id", item.id)
            put("beachName", item.beachName)
            put("wasteKg", item.wasteKg)
            put("bagsCount", item.bagsCount)
            put("wasteCategory", item.wasteCategory)
            put("monthIndex", item.monthIndex)
            put("dateLabel", item.dateLabel)
            put("timestamp", item.timestamp)
        }
        arr.put(obj)
    }
    prefs.edit().putString("cleanup_logs_json", arr.toString()).apply()
}

private fun defaultCleanupSeedLogs(): List<CoastalCleanupLogEntry> {
    val now = System.currentTimeMillis()
    val todayLabel = SimpleDateFormat("dd MMM yyyy", Locale.US).format(Date(now))
    val currentMonth = Calendar.getInstance().get(Calendar.MONTH)
    return listOf(
        CoastalCleanupLogEntry("seed_today", "Chandipur Beach", 2.5, 1, "Plastic & Bottles", currentMonth, todayLabel, now - 3600000L),
        CoastalCleanupLogEntry("seed_1", "Chandipur Beach", 7.5, 3, "Plastic & Bottles", 9, "24 Oct 2026", now - 86400000L * 2),
        CoastalCleanupLogEntry("seed_2", "Talasari Beach", 5.0, 2, "Ghost Fishing Nets", 9, "18 Oct 2026", now - 86400000L * 8),
        CoastalCleanupLogEntry("seed_3", "Kasafal Eco Beach", 10.0, 4, "Plastic & Bottles", 9, "11 Oct 2026", now - 86400000L * 15),
        CoastalCleanupLogEntry("seed_4", "Dagara Beach", 5.0, 2, "Tidal Drift Debris", 8, "22 Sep 2026", now - 86400000L * 34),
        CoastalCleanupLogEntry("seed_5", "Chandipur Beach", 7.5, 3, "Glass & Cans", 8, "09 Sep 2026", now - 86400000L * 47),
        CoastalCleanupLogEntry("seed_6", "Balaramgadi Estuary", 12.5, 5, "Ghost Fishing Nets", 7, "19 Aug 2026", now - 86400000L * 68),
        CoastalCleanupLogEntry("seed_7", "Chandipur Beach", 5.0, 2, "Plastic & Bottles", 6, "14 Jul 2026", now - 86400000L * 104),
        CoastalCleanupLogEntry("seed_8", "Talasari Beach", 7.5, 3, "Plastic & Bottles", 5, "05 Jun 2026", now - 86400000L * 143),
        CoastalCleanupLogEntry("seed_9", "Chandipur Beach", 5.0, 2, "Plastic & Bottles", 4, "18 May 2026", now - 86400000L * 161),
        CoastalCleanupLogEntry("seed_10", "Kasafal Eco Beach", 7.5, 3, "Tidal Drift Debris", 3, "12 Apr 2026", now - 86400000L * 197),
        CoastalCleanupLogEntry("seed_11", "Chandipur Beach", 5.0, 2, "Plastic & Bottles", 2, "08 Mar 2026", now - 86400000L * 232),
        CoastalCleanupLogEntry("seed_12", "Talasari Beach", 5.0, 2, "Glass & Cans", 1, "14 Feb 2026", now - 86400000L * 254),
        CoastalCleanupLogEntry("seed_13", "Chandipur Beach", 7.5, 3, "Plastic & Bottles", 0, "19 Jan 2026", now - 86400000L * 280)
    )
}
