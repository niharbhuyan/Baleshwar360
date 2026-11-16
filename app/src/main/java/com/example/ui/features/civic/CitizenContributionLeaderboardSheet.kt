package com.example.ui.features.civic

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.civic.CitizenLeaderboardRepository
import com.example.data.civic.CivicAchievementBadge
import com.example.data.civic.CivicContributor
import com.example.data.civic.CivicTier
import com.example.data.civic.LeaderboardTimePeriod
import com.example.data.local.CivicReportEntity
import com.example.data.model.AppLanguage
import com.example.ui.theme.BentoCardWhite
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate400
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.OceanBlue
import com.example.util.BalasoreHapticHelper
import java.util.Locale

/**
 * 'Citizen Contribution Leaderboard' Component & Sheet.
 * Recognizes and ranks citizens based on the number of 'Citizen Civic Eye' reports
 * submitted and successfully resolved, rewarding active community participation.
 */
@Composable
fun CitizenContributionLeaderboardSheet(
    language: AppLanguage,
    myReports: List<CivicReportEntity>,
    userName: String? = null,
    onOpenCivicEyeForm: () -> Unit = {},
    onClose: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val repository = remember { CitizenLeaderboardRepository(context) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var selectedTimePeriod by remember { mutableStateOf(LeaderboardTimePeriod.ALL_TIME) }
    var selectedWardFilter by remember { mutableStateOf("All Wards") }

    val isOdia = language == AppLanguage.ODIA

    val userStats = remember(myReports, userName) {
        repository.calculateUserCivicStats(myReports, userName)
    }

    val leaderboard = remember(myReports, selectedTimePeriod, selectedWardFilter, userName) {
        repository.getCombinedLeaderboard(
            userReports = myReports,
            timePeriod = selectedTimePeriod,
            wardFilter = selectedWardFilter,
            userName = userName
        )
    }

    val badges = remember(myReports) {
        repository.evaluateBadges(myReports)
    }

    val impactStats = remember {
        repository.getMunicipalImpactStats()
    }

    val wardsList = remember {
        listOf("All Wards", "Ward 14 - OT Road", "Ward 4 - Sahadevkhunta", "Ward 18 - Station", "Ward 1 - Azimabad", "Ward 7 - Motiganj", "Ward 12 - Cinema", "Remuna", "Soro")
    }

    Surface(
        modifier = modifier
            .fillMaxSize()
            .testTag("citizen_contribution_leaderboard_sheet"),
        color = BentoCardWhite
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 60.dp)
        ) {
            // 1. Header Banner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF0F172A), Color(0xFF1E293B))
                            )
                        )
                        .padding(horizontal = 18.dp, vertical = 18.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFF59E0B).copy(alpha = 0.2f))
                                        .border(1.5.dp, Color(0xFFF59E0B), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🏆", fontSize = 22.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (isOdia) "ନାଗରିକ ଯୋଗଦାନ ମାନ୍ୟତା" else "Citizen Contribution Leaderboard",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                fontSize = 16.sp
                                            )
                                        )
                                    }
                                    Text(
                                        text = if (isOdia) "ବାଲେଶ୍ୱର ସିଭିକ୍ ଆଇ ରିପୋର୍ଟ ଓ ସଫଳ ସମାଧାନ ସମ୍ମାନ"
                                        else "Recognizing Civic Eye reporters & resolved municipal issues",
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.5.sp
                                        )
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        BalasoreHapticHelper.vibrateButtonClick(context)
                                        val shareText = "🏆 I have ${userStats.civicPoints} Civic Points & ${userStats.reportsResolved} resolved reports on the Balasore Citizen Leaderboard! Download Baleshwar 360 to report local issues."
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "Balasore Citizen Leaderboard")
                                            putExtra(Intent.EXTRA_TEXT, shareText)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Civic Standing"))
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                                }

                                IconButton(
                                    onClick = {
                                        BalasoreHapticHelper.vibrateButtonClick(context)
                                        onClose()
                                    },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(22.dp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 2. Personal Standing Hero Card
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("personal_standing_card"),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                            border = BorderStroke(1.5.dp, Color(userStats.tier.badgeColorHex))
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = userStats.avatarEmoji, fontSize = 28.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = if (isOdia) userStats.odiaDisplayName else userStats.displayName,
                                                    color = Color.White,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 14.sp
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(userStats.tier.badgeColorHex).copy(alpha = 0.2f),
                                                    border = BorderStroke(0.8.dp, Color(userStats.tier.badgeColorHex))
                                                ) {
                                                    Text(
                                                        text = if (isOdia) userStats.tier.titleOd else userStats.tier.titleEn,
                                                        color = Color(userStats.tier.badgeColorHex),
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                                    )
                                                }
                                            }
                                            Text(
                                                text = "📍 ${userStats.wardName}",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 11.sp
                                            )
                                        }
                                    }

                                    // Rank Badge
                                    val userRank = leaderboard.find { it.isCurrentUser }?.rank ?: 1
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0F172A),
                                        border = BorderStroke(1.dp, Color(0xFF38BDF8))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("RANK", fontSize = 8.5.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "#$userRank",
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF38BDF8)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                // Metrics Grid: Submitted | Resolved | Upvotes | Total Points
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0F172A)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("SUBMITTED", fontSize = 8.5.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${userStats.reportsSubmitted}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0F172A),
                                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.5f))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("RESOLVED", fontSize = 8.5.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${userStats.reportsResolved}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFF34D399)
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.weight(1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0F172A)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("UPVOTES", fontSize = 8.5.sp, color = Color(0xFF94A3B8), fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${userStats.upvotesReceived}",
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color(0xFFFBBF24)
                                            )
                                        }
                                    }

                                    Surface(
                                        modifier = Modifier.weight(1.1f),
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color(0xFF0284C7).copy(alpha = 0.2f),
                                        border = BorderStroke(1.dp, Color(0xFF0284C7))
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text("CIVIC POINTS", fontSize = 8.sp, color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                                            Text(
                                                text = "${userStats.civicPoints}",
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                // Next Tier Progress Bar
                                val nextTier = when (userStats.tier) {
                                    CivicTier.BRONZE_SCOUT -> CivicTier.SILVER_PATROL
                                    CivicTier.SILVER_PATROL -> CivicTier.GOLD_GUARDIAN
                                    CivicTier.GOLD_GUARDIAN -> CivicTier.PLATINUM_CHAMPION
                                    CivicTier.PLATINUM_CHAMPION -> null
                                }

                                if (nextTier != null) {
                                    val ptsToNext = (nextTier.minPoints - userStats.civicPoints).coerceAtLeast(0)
                                    val progressFraction = (userStats.civicPoints.toFloat() / nextTier.minPoints.toFloat()).coerceIn(0f, 1f)

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "Next: ${nextTier.emoji} ${nextTier.titleEn}",
                                            fontSize = 10.sp,
                                            color = Color(0xFF94A3B8)
                                        )
                                        Text(
                                            text = "$ptsToNext pts to level up",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { progressFraction },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp)),
                                        color = Color(0xFF0284C7),
                                        trackColor = Color(0xFF334155),
                                    )
                                } else {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = "👑 Highest Tier Achieved: Apex Civic Champion!",
                                            fontSize = 10.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFFBBF24)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 3. Tab Navigation: Leaderboard Ranking | Badges | Community Impact
            item {
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = OceanBlue,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                            color = OceanBlue
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTabIndex == 0,
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            selectedTabIndex = 0
                        },
                        text = {
                            Text(
                                text = if (isOdia) "🏆 ମାନ୍ୟତା ସୂଚୀ" else "🏆 Leaderboard",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("leaderboard_tab_ranking")
                    )
                    Tab(
                        selected = selectedTabIndex == 1,
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            selectedTabIndex = 1
                        },
                        text = {
                            Text(
                                text = if (isOdia) "🎖️ ବ୍ୟାଜ୍ ଓ ସମ୍ମାନ" else "🎖️ Badges (${badges.count { it.isUnlocked }}/${badges.size})",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("leaderboard_tab_badges")
                    )
                    Tab(
                        selected = selectedTabIndex == 2,
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            selectedTabIndex = 2
                        },
                        text = {
                            Text(
                                text = if (isOdia) "📊 ପୌର ପ୍ରଭାବ" else "📊 Impact Stats",
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        modifier = Modifier.testTag("leaderboard_tab_impact")
                    )
                }
            }

            // 4. Tab Content Routing
            when (selectedTabIndex) {
                0 -> {
                    // Filter Bar: Time Period & Ward Filters
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Time Period Chips
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    LeaderboardTimePeriod.entries.forEach { period ->
                                        val isSelected = selectedTimePeriod == period
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = if (isSelected) OceanBlue else BentoSlate100,
                                            border = BorderStroke(1.dp, if (isSelected) OceanBlue else BentoSlate200),
                                            modifier = Modifier
                                                .clickable {
                                                    BalasoreHapticHelper.vibrateToggle(context)
                                                    selectedTimePeriod = period
                                                }
                                                .testTag("period_chip_${period.name}")
                                        ) {
                                            Text(
                                                text = if (isOdia) period.labelOd else period.labelEn,
                                                fontSize = 11.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else BentoSlate700,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                }

                                // CTA: Report Issue
                                Button(
                                    onClick = {
                                        BalasoreHapticHelper.vibrateButtonClick(context)
                                        onOpenCivicEyeForm()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 5.dp),
                                    modifier = Modifier.testTag("leaderboard_report_new_issue_btn")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(if (isOdia) "ଅଭିଯୋଗ (+୧୫ pt)" else "+ Report Issue", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Ward Filter Horizontal Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                wardsList.forEach { ward ->
                                    val isSelected = selectedWardFilter == ward
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = if (isSelected) Color(0xFF0F172A) else Color(0xFFF1F5F9),
                                        modifier = Modifier.clickable {
                                            BalasoreHapticHelper.vibrateToggle(context)
                                            selectedWardFilter = ward
                                        }
                                    ) {
                                        Text(
                                            text = ward,
                                            fontSize = 10.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else BentoSlate600,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Top 3 Podium Cards
                    if (leaderboard.size >= 3) {
                        item {
                            Top3PodiumSection(
                                topThree = leaderboard.take(3),
                                isOdia = isOdia
                            )
                        }
                    }

                    // Leaderboard List Items (Rank 4+)
                    val remainingList = if (leaderboard.size > 3) leaderboard.drop(3) else leaderboard
                    itemsIndexed(remainingList) { _, contributor ->
                        ContributorLeaderboardRow(
                            contributor = contributor,
                            isOdia = isOdia
                        )
                    }
                }

                1 -> {
                    // Badges Showcase
                    item {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = if (isOdia) "ବାଲେଶ୍ୱର ସିଭିକ୍ ଆଇ ସମ୍ମାନ ବ୍ୟାଜ୍" else "Civic Eye Achievement Badges",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoSlate900
                                )
                            )
                            Text(
                                text = if (isOdia) "ନିଜର ଅଭିଯୋଗ ସମାଧାନ କରି ବ୍ୟାଜ୍ ଅନଲକ୍ କରନ୍ତୁ" else "Earn badges by reporting infrastructure issues and verifying resolutions",
                                style = MaterialTheme.typography.bodySmall.copy(color = BentoSlate500)
                            )
                            Spacer(modifier = Modifier.height(14.dp))
                        }
                    }

                    itemsIndexed(badges) { _, badge ->
                        AchievementBadgeCard(
                            badge = badge,
                            isOdia = isOdia,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )
                    }
                }

                2 -> {
                    // Municipal Impact Stats
                    item {
                        MunicipalImpactDashboardContent(
                            stats = impactStats,
                            isOdia = isOdia,
                            onOpenCivicEyeForm = onOpenCivicEyeForm
                        )
                    }
                }
            }
        }
    }
}

/**
 * Top 3 Podium displaying 1st, 2nd, and 3rd place citizen contributors.
 */
@Composable
private fun Top3PodiumSection(
    topThree: List<CivicContributor>,
    isOdia: Boolean
) {
    val rank1 = topThree.getOrNull(0) ?: return
    val rank2 = topThree.getOrNull(1)
    val rank3 = topThree.getOrNull(2)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        border = BorderStroke(1.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isOdia) "🌟 ମ୍ୟୁନିସିପାଲ୍ ଟପ୍ କଣ୍ଟ୍ରିବ୍ୟୁଟର୍ସ" else "🌟 Top Municipal Contributors",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.5.sp,
                color = BentoSlate800
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Bottom
            ) {
                // 2nd Place (Silver)
                if (rank2 != null) {
                    PodiumColumnItem(
                        contributor = rank2,
                        podiumRank = 2,
                        badgeColor = Color(0xFF94A3B8),
                        crownEmoji = "🥈",
                        barHeight = 75.dp,
                        isOdia = isOdia,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 1st Place (Gold Center)
                PodiumColumnItem(
                    contributor = rank1,
                    podiumRank = 1,
                    badgeColor = Color(0xFFF59E0B),
                    crownEmoji = "👑",
                    barHeight = 98.dp,
                    isOdia = isOdia,
                    modifier = Modifier.weight(1.15f)
                )

                // 3rd Place (Bronze)
                if (rank3 != null) {
                    PodiumColumnItem(
                        contributor = rank3,
                        podiumRank = 3,
                        badgeColor = Color(0xFFCD7F32),
                        crownEmoji = "🥉",
                        barHeight = 60.dp,
                        isOdia = isOdia,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun PodiumColumnItem(
    contributor: CivicContributor,
    podiumRank: Int,
    badgeColor: Color,
    crownEmoji: String,
    barHeight: androidx.compose.ui.unit.Dp,
    isOdia: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = crownEmoji, fontSize = if (podiumRank == 1) 22.sp else 18.sp)
        Box(
            modifier = Modifier
                .size(if (podiumRank == 1) 48.dp else 40.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, badgeColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = contributor.avatarEmoji, fontSize = if (podiumRank == 1) 24.sp else 20.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = if (isOdia) contributor.odiaDisplayName.split(" ").first() else contributor.displayName.split(" ").first(),
            fontSize = 11.5.sp,
            fontWeight = FontWeight.Bold,
            color = BentoSlate900,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${contributor.civicPoints} pts",
            fontSize = 11.sp,
            fontWeight = FontWeight.ExtraBold,
            color = OceanBlue
        )
        Text(
            text = "✓ ${contributor.reportsResolved} resolved",
            fontSize = 9.sp,
            color = Color(0xFF16A34A),
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Podium Block
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp),
            color = badgeColor.copy(alpha = if (podiumRank == 1) 0.25f else 0.15f),
            border = BorderStroke(1.dp, badgeColor)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "#$podiumRank",
                    fontSize = if (podiumRank == 1) 18.sp else 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = badgeColor
                )
            }
        }
    }
}

/**
 * Individual Contributor row in the leaderboard.
 */
@Composable
private fun ContributorLeaderboardRow(
    contributor: CivicContributor,
    isOdia: Boolean
) {
    val isCurrentUser = contributor.isCurrentUser
    val rowBg = if (isCurrentUser) Color(0xFFEFF6FF) else Color.White
    val rowBorder = if (isCurrentUser) Color(0xFF0284C7) else BentoSlate200

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = rowBg,
        border = BorderStroke(if (isCurrentUser) 1.5.dp else 1.dp, rowBorder),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .testTag("leaderboard_row_${contributor.rank}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank Number Pill
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (contributor.rank) {
                        1 -> Color(0xFFFEF3C7)
                        2 -> Color(0xFFF1F5F9)
                        3 -> Color(0xFFFEF2F2)
                        else -> BentoSlate100
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "#${contributor.rank}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = when (contributor.rank) {
                                1 -> Color(0xFFD97706)
                                2 -> Color(0xFF475569)
                                3 -> Color(0xFFB45309)
                                else -> BentoSlate700
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(text = contributor.avatarEmoji, fontSize = 22.sp)

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (isOdia) contributor.odiaDisplayName else contributor.displayName,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isCurrentUser) FontWeight.ExtraBold else FontWeight.Bold,
                                color = if (isCurrentUser) Color(0xFF0369A1) else BentoSlate900
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFF0284C7)
                            ) {
                                Text(
                                    text = "YOU",
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contributor.wardName,
                            fontSize = 10.5.sp,
                            color = BentoSlate500,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "✓ ${contributor.reportsResolved} resolved",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
            }

            // Points Column
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${contributor.civicPoints} pts",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = OceanBlue
                    )
                )
                Text(
                    text = "${contributor.reportsSubmitted} reports",
                    fontSize = 10.sp,
                    color = BentoSlate400
                )
            }
        }
    }
}

/**
 * Achievement Badge Card.
 */
@Composable
private fun AchievementBadgeCard(
    badge: CivicAchievementBadge,
    isOdia: Boolean,
    modifier: Modifier = Modifier
) {
    val isUnlocked = badge.isUnlocked
    val cardBg = if (isUnlocked) Color.White else Color(0xFFF8FAFC)
    val borderColor = if (isUnlocked) Color(0xFFF59E0B).copy(alpha = 0.5f) else BentoSlate200

    Surface(
        shape = RoundedCornerShape(14.dp),
        color = cardBg,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) Color(0xFFFEF3C7) else BentoSlate100)
                    .border(
                        1.dp,
                        if (isUnlocked) Color(0xFFF59E0B) else BentoSlate200,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badge.iconEmoji,
                    fontSize = 22.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isOdia) badge.odiaTitle else badge.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (isUnlocked) BentoSlate900 else BentoSlate500
                        )
                    )

                    if (isUnlocked) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("UNLOCKED", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF166534))
                            }
                        }
                    } else {
                        Text(
                            text = badge.progressLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSlate400
                        )
                    }
                }

                Text(
                    text = if (isOdia) badge.odiaDescription else badge.description,
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BentoSlate600,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                )

                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(6.dp))
                    LinearProgressIndicator(
                        progress = { badge.progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = Color(0xFF0284C7),
                        trackColor = BentoSlate200,
                    )
                }
            }
        }
    }
}

/**
 * Community Municipal Resolution Impact Stats Dashboard Content.
 */
@Composable
private fun MunicipalImpactDashboardContent(
    stats: com.example.data.civic.MunicipalCivicImpactStats,
    isOdia: Boolean,
    onOpenCivicEyeForm: () -> Unit
) {
    val context = LocalContext.current

    Column(modifier = Modifier.padding(16.dp)) {
        // Hero Resolution Counter
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (isOdia) "ପୌରପାଳିକା ସଫଳ ସମାଧାନ ହାର" else "Balasore Municipal Resolution Rate",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${stats.resolutionRatePercentage}%",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF34D399)
                )
                Text(
                    text = "${stats.totalReportsResolved} issues resolved out of ${stats.totalReportsLogged} citizen reports",
                    fontSize = 11.5.sp,
                    color = Color(0xFFE2E8F0)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    Text(
                        text = "⚡ Avg. Turnaround: ${stats.averageTurnaroundDays} days",
                        fontSize = 10.5.sp,
                        color = Color(0xFF38BDF8),
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isOdia) "ବିଭାଗ ଅନୁଯାୟୀ ସମାଧାନ ସଫଳତା" else "Resolved Issues by Infrastructure Category",
            style = MaterialTheme.typography.titleSmall.copy(
                fontWeight = FontWeight.Bold,
                color = BentoSlate900
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Infrastructure Metrics Grid
        val categoryBreakdown = listOf(
            Triple("🕳️ Road Potholes Repaired", "${stats.potholesRepaired} craters filled", Color(0xFFEA580C)),
            Triple("💡 Streetlights Restored", "${stats.streetlightsRestored} lights operational", Color(0xFFD97706)),
            Triple("🌊 Storm Drains Cleared", "${stats.drainsDesilted} de-silted channels", Color(0xFF0284C7)),
            Triple("🗑️ Garbage Spots Sanitized", "${stats.garbageDumpsCleared} cleared heaps", Color(0xFF16A34A)),
            Triple("🚰 Water Pipelines Repaired", "${stats.waterPipelinesFixed} leakages sealed", Color(0xFF2563EB))
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            categoryBreakdown.forEach { (title, subtitle, color) ->
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
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate900
                            )
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = color.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = subtitle,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = color,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // CTA: Submit Report to improve Balasore
        Button(
            onClick = {
                BalasoreHapticHelper.vibrateButtonClick(context)
                onOpenCivicEyeForm()
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = if (isOdia) "ନୂତନ ଅଭିଯୋଗ ରିପୋର୍ଟ କରନ୍ତୁ (+୧୫ pt)" else "Capture Issue with Civic Eye (+15 pts)",
                fontSize = 12.5.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
