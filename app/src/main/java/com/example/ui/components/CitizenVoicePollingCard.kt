package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.data.firebase.FirebaseAuthManager
import com.example.data.model.AppLanguage
import com.example.data.polling.CitizenPoll
import com.example.data.polling.CitizenVoicePollingRepository
import com.example.data.polling.PollOption
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate400
import com.example.ui.theme.BentoSlate50
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.OceanBlue
import com.example.util.BalasoreHapticHelper
import kotlinx.coroutines.launch
import java.util.Locale

/**
 * Interactive 'Citizen Voice' Polling Component for the City Essentials dashboard.
 * Allows citizens to vote on municipal initiatives (park improvements, event scheduling, coastal eco-measures),
 * stores votes instantly in the local Room database, and seamlessly synchronizes with Firestore when online.
 */
@Composable
fun CitizenVoicePollingCard(
    language: AppLanguage,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { CitizenVoicePollingRepository(context) }
    val authManager = remember { FirebaseAuthManager(context) }

    val pollsWithVotes by repository.observePollsWithUserVotes().collectAsState(initial = emptyList())
    val activeSessionUser by authManager.activeSessionUser.collectAsState()
    val activeUid = activeSessionUser?.id

    var selectedCategoryFilter by remember { mutableStateOf("All") }
    val selectedOptionsMap = remember { mutableStateMapOf<String, String>() }
    var isSyncing by remember { mutableStateOf(false) }

    val isOdia = language == AppLanguage.ODIA
    val categories = listOf("All", "Park Improvements", "Event Scheduling", "Coastal Preservation", "Urban Infrastructure")

    val filteredPolls = remember(pollsWithVotes, selectedCategoryFilter) {
        if (selectedCategoryFilter == "All") {
            pollsWithVotes
        } else {
            pollsWithVotes.filter { it.first.category.equals(selectedCategoryFilter, ignoreCase = true) }
        }
    }

    val totalCommunityVotes = remember(pollsWithVotes) {
        pollsWithVotes.sumOf { it.first.totalVotes }
    }

    val unsyncedCount = remember(pollsWithVotes) {
        pollsWithVotes.count { it.second != null && it.second?.syncedToCloud == false }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("citizen_voice_polling_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, Color(0xFF0284C7).copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 1. Header Bar: Title + Total Votes Badge + Manual Firestore Sync
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
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0284C7).copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.HowToVote,
                            contentDescription = "Citizen Voice Polling",
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOdia) "ନାଗରିକ ମତଦାନ (Citizen Voice)" else "Citizen Voice Polling",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoSlate900
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFE0F2FE)
                            ) {
                                Text(
                                    text = "ODISHA CIVIC",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF0284C7),
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isOdia) {
                                "ବାଲେଶ୍ୱର ପୌରପାଳିକା ଉନ୍ନୟନ ପ୍ରକଳ୍ପ ଉପରେ ଆପଣଙ୍କ ମତାମତ ଦିଅନ୍ତୁ"
                            } else {
                                "Vote on Balasore civic initiatives • Offline Room & Firestore Cloud Sync"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BentoSlate500,
                                fontSize = 11.5.sp
                            )
                        )
                    }
                }

                // Cloud Sync Action Button
                IconButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        if (!isSyncing) {
                            isSyncing = true
                            coroutineScope.launch {
                                val result = repository.syncPendingVotesToFirestore()
                                isSyncing = false
                                result.fold(
                                    onSuccess = { count ->
                                        BalasoreHapticHelper.vibrateSuccess(context)
                                        Toast.makeText(
                                            context,
                                            if (count > 0) "✓ Synced $count votes to Balasore Civic Cloud!"
                                            else "✓ All votes are up to date with Firestore!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    onFailure = { err ->
                                        Toast.makeText(
                                            context,
                                            "Cloud sync note: ${err.message ?: "Offline"}. Votes remain safe locally in Room DB.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (unsyncedCount > 0) Color(0xFFFEF3C7) else Color(0xFFF1F5F9))
                        .testTag("sync_citizen_votes_btn")
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color(0xFF0284C7)
                        )
                    } else {
                        Icon(
                            imageVector = if (unsyncedCount > 0) Icons.Default.CloudSync else Icons.Default.CloudDone,
                            contentDescription = "Sync votes to Firestore",
                            tint = if (unsyncedCount > 0) Color(0xFFD97706) else Color(0xFF059669),
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 2. Category Filter Chips (Park, Event, Coastal, Infrastructure)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategoryFilter == cat
                    val chipLabel = when (cat) {
                        "All" -> if (isOdia) "ସମସ୍ତ (All)" else "All Initiatives"
                        "Park Improvements" -> if (isOdia) "ପାର୍କ ଉନ୍ନୟନ" else "Park Improvements"
                        "Event Scheduling" -> if (isOdia) "ମହୋତ୍ସବ ସମୟ" else "Event Scheduling"
                        "Coastal Preservation" -> if (isOdia) "ଉପକୂଳ ସୁରକ୍ଷା" else "Coastal Preservation"
                        "Urban Infrastructure" -> if (isOdia) "ସହରୀ ଭିତ୍ତିଭୂମି" else "Urban Infrastructure"
                        else -> cat
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) Color(0xFF0284C7) else BentoSlate100,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF0284C7) else BentoSlate200
                        ),
                        modifier = Modifier
                            .clickable {
                                BalasoreHapticHelper.vibrateToggle(context)
                                selectedCategoryFilter = cat
                            }
                            .testTag("filter_chip_$cat")
                    ) {
                        Text(
                            text = chipLabel,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                            color = if (isSelected) Color.White else BentoSlate700,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Initiative Polls List
            if (filteredPolls.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BentoSlate50,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🗳️", fontSize = 28.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isOdia) "କୌଣସି ମତଦାନ ମିଳିଲା ନାହିଁ" else "No initiatives found in this category",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate700
                        )
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    filteredPolls.forEach { (poll, userVote) ->
                        CitizenPollItemCard(
                            poll = poll,
                            userVote = userVote,
                            selectedOptionId = selectedOptionsMap[poll.id] ?: userVote?.optionId,
                            onOptionSelected = { optId ->
                                BalasoreHapticHelper.vibrateButtonClick(context)
                                selectedOptionsMap[poll.id] = optId
                            },
                            onCastVote = { optId, optLabel ->
                                coroutineScope.launch {
                                    val res = repository.castVote(
                                        pollId = poll.id,
                                        optionId = optId,
                                        optionLabel = optLabel,
                                        initiativeCategory = poll.category,
                                        userId = activeUid
                                    )
                                    res.fold(
                                        onSuccess = { savedVote ->
                                            BalasoreHapticHelper.vibrateSuccess(context)
                                            val syncMsg = if (savedVote.syncedToCloud) "synced to Firestore" else "saved in local Room DB"
                                            Toast.makeText(
                                                context,
                                                if (isOdia) "ଆପଣଙ୍କ ମତ ସଫଳତାର ସହ ଦିଆଗଲା! ($syncMsg)"
                                                else "✓ Vote recorded! ($syncMsg)",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        onFailure = {
                                            Toast.makeText(context, "Could not record vote: ${it.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    )
                                }
                            },
                            isOdia = isOdia
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 4. Civic Participation Stats & Local-to-Cloud Integrity Note
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFF8FAFC),
                border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isOdia) "ମୋଟ ସମୁଦାୟ ମତ: $totalCommunityVotes • ଅଫଲାଇନ୍ ଡାଟାବେସ୍ ପ୍ରସ୍ତୁତ"
                            else "Total votes cast: $totalCommunityVotes • 100% Offline Capable",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate700
                        )
                    }

                    if (unsyncedCount > 0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEF3C7)
                        ) {
                            Text(
                                text = "$unsyncedCount unsynced",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB45309),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7)
                        ) {
                            Text(
                                text = "Cloud Synced",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Individual Civic Initiative Poll Item Card.
 * Renders radio voting if unvoted, and animated percentage progress bars once voted.
 */
@Composable
private fun CitizenPollItemCard(
    poll: CitizenPoll,
    userVote: com.example.data.local.CitizenVoteEntity?,
    selectedOptionId: String?,
    onOptionSelected: (String) -> Unit,
    onCastVote: (String, String) -> Unit,
    isOdia: Boolean
) {
    val context = LocalContext.current
    val hasVoted = userVote != null

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.2.dp, if (hasVoted) Color(0xFF10B981).copy(alpha = 0.5f) else BentoSlate200),
        shadowElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("citizen_poll_card_${poll.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {

            // Top Row: Category Pill + Closing Date + Sync Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = when (poll.category) {
                        "Park Improvements" -> Color(0xFFDCFCE7)
                        "Event Scheduling" -> Color(0xFFFEF3C7)
                        "Coastal Preservation" -> Color(0xFFE0F2FE)
                        else -> Color(0xFFF3E8FF)
                    }
                ) {
                    Text(
                        text = poll.category.uppercase(Locale.US),
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = when (poll.category) {
                            "Park Improvements" -> Color(0xFF166534)
                            "Event Scheduling" -> Color(0xFFB45309)
                            "Coastal Preservation" -> Color(0xFF0369A1)
                            else -> Color(0xFF7E22CE)
                        },
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.5.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (hasVoted) {
                        if (userVote?.syncedToCloud == true) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFDCFCE7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.CloudDone, contentDescription = null, tint = Color(0xFF166534), modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Firestore Synced", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF166534))
                                }
                            }
                        } else {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = Color(0xFFFEF3C7)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(11.dp))
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text("Local Room DB", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Text(
                        text = "📅 ${poll.closingDate}",
                        fontSize = 10.sp,
                        color = BentoSlate400,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Poll Title
            Text(
                text = if (isOdia && poll.odiaTitle.isNotBlank()) poll.odiaTitle else poll.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate900,
                    fontSize = 14.sp
                )
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Description
            Text(
                text = if (isOdia && poll.odiaDescription.isNotBlank()) poll.odiaDescription else poll.description,
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BentoSlate600,
                    fontSize = 11.5.sp,
                    lineHeight = 16.sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = BentoSlate100, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // Options List
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                poll.options.forEach { option ->
                    val isOptionSelected = selectedOptionId == option.id
                    val isUserVotedOption = userVote?.optionId == option.id

                    val animatedPercentage by animateFloatAsState(
                        targetValue = if (hasVoted) option.percentage else 0f,
                        label = "option_pct_${option.id}"
                    )

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when {
                            isUserVotedOption -> Color(0xFFEFF6FF)
                            isOptionSelected && !hasVoted -> Color(0xFFF0FDF4)
                            else -> BentoSlate50
                        },
                        border = BorderStroke(
                            1.dp,
                            when {
                                isUserVotedOption -> Color(0xFF0284C7)
                                isOptionSelected && !hasVoted -> Color(0xFF10B981)
                                else -> BentoSlate200
                            }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !hasVoted) {
                                onOptionSelected(option.id)
                            }
                            .testTag("vote_option_${option.id}")
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (!hasVoted) {
                                        RadioButton(
                                            selected = isOptionSelected,
                                            onClick = { onOptionSelected(option.id) },
                                            colors = RadioButtonDefaults.colors(
                                                selectedColor = Color(0xFF0284C7),
                                                unselectedColor = BentoSlate400
                                            ),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    } else if (isUserVotedOption) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Your Vote",
                                            tint = Color(0xFF0284C7),
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                    }

                                    Text(
                                        text = if (isOdia && option.odiaLabel.isNotBlank()) option.odiaLabel else option.label,
                                        fontSize = 12.sp,
                                        fontWeight = if (isUserVotedOption || isOptionSelected) FontWeight.Bold else FontWeight.SemiBold,
                                        color = if (isUserVotedOption) Color(0xFF0369A1) else BentoSlate800
                                    )
                                }

                                if (hasVoted) {
                                    Text(
                                        text = String.format(Locale.US, "%.1f%% (%d)", animatedPercentage, option.currentVotes),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (isUserVotedOption) Color(0xFF0284C7) else BentoSlate600
                                    )
                                }
                            }

                            // Animated Progress Bar when user has cast vote
                            if (hasVoted) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(BentoSlate200)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedPercentage / 100f)
                                            .height(6.dp)
                                            .clip(RoundedCornerShape(3.dp))
                                            .background(
                                                if (isUserVotedOption) Color(0xFF0284C7)
                                                else Color(0xFF94A3B8)
                                            )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Submit Vote or Confirmation Pill
            if (!hasVoted) {
                Button(
                    onClick = {
                        val optId = selectedOptionId
                        val optLabel = poll.options.firstOrNull { it.id == optId }?.label ?: ""
                        if (optId != null) {
                            onCastVote(optId, optLabel)
                        } else {
                            Toast.makeText(context, "Please select an option before casting your vote.", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = selectedOptionId != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("cast_vote_btn_${poll.id}"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0284C7),
                        disabledContainerColor = BentoSlate200
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "ମତ ଦିଅନ୍ତୁ (Cast Vote)" else "Cast Vote on Initiative",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF166534),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isOdia) "✓ ଆପଣଙ୍କ ମତ ଗ୍ରହଣ କରାଯାଇଛି" else "✓ Your vote is recorded for this initiative",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF166534)
                            )
                        }
                        Text(
                            text = "${poll.totalVotes} total",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }
        }
    }
}
