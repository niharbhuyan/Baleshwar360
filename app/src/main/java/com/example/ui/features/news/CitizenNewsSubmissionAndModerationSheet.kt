package com.example.ui.features.news

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.example.data.local.CommunityNewsSubmissionEntity
import com.example.data.local.NewsModerationStatus
import com.example.data.model.AppLanguage
import com.example.data.repository.CommunityNewsModerationRepository
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Prominent banner card displayed inside [com.example.ui.screens.NewsScreen] allowing users to:
 * 1. Submit a new local Balasore news story (which enters the moderation queue as `PENDING`).
 * 2. Track their submitted stories or open the Editorial Moderation Review Desk to approve/reject pending stories before they go live.
 */
@Composable
fun CitizenNewsDeskBannerCard(
    submissions: List<CommunityNewsSubmissionEntity>,
    language: AppLanguage,
    onOpenSubmitStory: () -> Unit,
    onOpenModerationQueue: () -> Unit,
    onOpenMySubmissions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOdia = language == AppLanguage.ODIA
    val pendingCount = remember(submissions) {
        submissions.count { it.moderationStatus == NewsModerationStatus.PENDING }
    }
    val approvedCount = remember(submissions) {
        submissions.count { it.moderationStatus == NewsModerationStatus.APPROVED }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .testTag("news_citizen_submission_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.5.dp, OceanBlue.copy(alpha = 0.38f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(OceanBlue.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = "Citizen News & Moderation Desk",
                            tint = OceanBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) {
                                "ନାଗରିକ ସାମ୍ବାଦିକତା ଓ ସମ୍ପାదକୀୟ ସମୀକ୍ଷା"
                            } else {
                                "Citizen News Desk & Moderation"
                            },
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                        Text(
                            text = if (isOdia) {
                                "ସ୍ଥାନୀୟ ଖବର ପଠାନ୍ତୁ • ମୋଡେରେଟର ଅନୁମୋଦନ ପରେ ଲାଇଭ୍ ହୁଏ"
                            } else {
                                "Submit local Balasore stories • Reviewed & approved before going live"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontSize = 11.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (pendingCount > 0) Color(0xFFFEF3C7) else Color(0xFFDCFCE7),
                    border = BorderStroke(
                        1.dp,
                        if (pendingCount > 0) Color(0xFFF59E0B) else Color(0xFF22C55E)
                    ),
                    modifier = Modifier
                        .clickable { onOpenModerationQueue() }
                        .testTag("news_pending_moderation_badge")
                ) {
                    Text(
                        text = if (pendingCount > 0) {
                            if (isOdia) "⏳ $pendingCount ସମୀକ୍ଷା ବାକି" else "⏳ $pendingCount Pending"
                        } else {
                            if (isOdia) "✓ $approvedCount ଲାଇଭ୍" else "✓ $approvedCount Verified"
                        },
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (pendingCount > 0) Color(0xFFB45309) else Color(0xFF15803D),
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                    )
                }
            }

            // Status summary pills
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenMySubmissions() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isOdia) "ମୋଟ ଦାଖଲ ଖବର" else "Submitted Stories",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${submissions.size}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlue
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFECFDF5),
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onOpenMySubmissions() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (isOdia) "ଅନୁମୋଦିତ ଓ ଲାଇଭ୍" else "Approved & Live",
                            fontSize = 11.sp,
                            color = Color(0xFF065F46),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "$approvedCount",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF047857)
                        )
                    }
                }
            }

            // Action Buttons: Submit Story + Review & Moderate Queue
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onOpenSubmitStory,
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("open_submit_news_sheet_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.RateReview,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "ଖବର ପଠାନ୍ତୁ" else "Submit Local News",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                OutlinedButton(
                    onClick = onOpenModerationQueue,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("open_news_moderation_queue_btn"),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.2.dp,
                        if (pendingCount > 0) Color(0xFFD97706) else OceanBlue
                    ),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Gavel,
                        contentDescription = null,
                        tint = if (pendingCount > 0) Color(0xFFD97706) else OceanBlue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) {
                            "ମୋଡେରେସନ୍ ($pendingCount)"
                        } else {
                            "Moderate ($pendingCount)"
                        },
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (pendingCount > 0) Color(0xFFB45309) else OceanBlue,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Full-screen sheet/portal for Citizen Local News Submission, Submission Status Tracking,
 * and the Editorial Moderation System.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CitizenNewsPortalSheet(
    initialTab: Int = 0, // 0 = Submit Story, 1 = My Submissions, 2 = Moderator Review Desk
    submissions: List<CommunityNewsSubmissionEntity>,
    language: AppLanguage = AppLanguage.ENGLISH,
    onSubmitStory: (
        title: String,
        odiaTitle: String,
        snippet: String,
        content: String,
        category: String,
        locationWard: String,
        authorName: String,
        authorContact: String,
        sourceVerificationType: String,
        photoUri: String?,
        isBreakingCandidate: Boolean
    ) -> Unit,
    onApproveSubmission: (
        submissionId: String,
        moderatorNote: String,
        editedTitle: String?,
        editedOdiaTitle: String?,
        editedSnippet: String?,
        editedContent: String?,
        editedCategory: String?
    ) -> Unit,
    onRejectSubmission: (submissionId: String, moderatorNote: String) -> Unit,
    onRequestRevision: (submissionId: String, moderatorNote: String) -> Unit,
    onUpdateAndResubmit: (
        submissionId: String,
        title: String,
        odiaTitle: String,
        snippet: String,
        content: String,
        category: String,
        locationWard: String
    ) -> Unit,
    onApproveAllPending: () -> Unit,
    onDeleteSubmission: (String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler(onBack = onClose)

    val isOdia = language == AppLanguage.ODIA
    var selectedTab by remember(initialTab) { mutableIntStateOf(initialTab.coerceIn(0, 2)) }

    val pendingCount = remember(submissions) {
        submissions.count { it.moderationStatus == NewsModerationStatus.PENDING }
    }
    val approvedCount = remember(submissions) {
        submissions.count { it.moderationStatus == NewsModerationStatus.APPROVED }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("citizen_news_portal_sheet"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FactCheck,
                            contentDescription = null,
                            tint = OceanBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isOdia) {
                                    "ବାଲେଶ୍ୱର ନାଗରିକ ସମ୍ବାଦ ଓ ମୋଡେରେସନ୍"
                                } else {
                                    "Balasore Citizen News & Moderation"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isOdia) {
                                    "ସ୍ଥାନୀୟ ଖବର ଦାଖଲ, ସ୍ଥିତି ଟ୍ରାକିଂ ଓ ସମ୍ପାଦକୀୟ ଅନୁମୋଦନ"
                                } else {
                                    "Submit local stories • Editorial review & live approval workflow"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_citizen_news_portal_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Citizen News Portal"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTab,
                edgePadding = 12.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = OceanBlue
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    modifier = Modifier.testTag("citizen_news_tab_submit"),
                    icon = { Icon(Icons.Default.RateReview, contentDescription = null) },
                    text = {
                        Text(
                            text = if (isOdia) "ଖବର ପଠାନ୍ତୁ" else "Submit Story",
                            fontWeight = if (selectedTab == 0) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    modifier = Modifier.testTag("citizen_news_tab_my_submissions"),
                    icon = {
                        BadgedBox(
                            badge = {
                                Badge(containerColor = OceanBlue, contentColor = Color.White) {
                                    Text("${submissions.size}", fontSize = 9.sp)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Article, contentDescription = null)
                        }
                    },
                    text = {
                        Text(
                            text = if (isOdia) "ଦାଖଲ ଖବର (${submissions.size})" else "Submissions (${submissions.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    modifier = Modifier.testTag("citizen_news_tab_moderation_desk"),
                    icon = {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = if (pendingCount > 0) Color(0xFFD97706) else EmeraldGreen,
                                    contentColor = Color.White
                                ) {
                                    Text("$pendingCount", fontSize = 9.sp)
                                }
                            }
                        ) {
                            Icon(Icons.Default.Gavel, contentDescription = null)
                        }
                    },
                    text = {
                        Text(
                            text = if (isOdia) {
                                "ମୋଡେରେଟର ଡେସ୍କ ($pendingCount)"
                            } else {
                                "Moderation Desk ($pendingCount)"
                            },
                            fontWeight = if (selectedTab == 2) FontWeight.ExtraBold else FontWeight.Medium
                        )
                    }
                )
            }

            when (selectedTab) {
                0 -> CitizenNewsSubmitStoryTab(
                    isOdia = isOdia,
                    onSubmitStory = { title, odiaTitle, snippet, content, category, ward, author, contact, sourceType, photoUri, isBreaking ->
                        onSubmitStory(
                            title,
                            odiaTitle,
                            snippet,
                            content,
                            category,
                            ward,
                            author,
                            contact,
                            sourceType,
                            photoUri,
                            isBreaking
                        )
                    },
                    onNavigateToMySubmissions = { selectedTab = 1 },
                    onNavigateToModerationDesk = { selectedTab = 2 }
                )
                1 -> CitizenNewsSubmissionsTrackerTab(
                    submissions = submissions,
                    isOdia = isOdia,
                    onUpdateAndResubmit = onUpdateAndResubmit,
                    onOpenModeratorDesk = { selectedTab = 2 }
                )
                2 -> CitizenNewsModeratorReviewTab(
                    submissions = submissions,
                    pendingCount = pendingCount,
                    approvedCount = approvedCount,
                    isOdia = isOdia,
                    onApproveSubmission = onApproveSubmission,
                    onRejectSubmission = onRejectSubmission,
                    onRequestRevision = onRequestRevision,
                    onApproveAllPending = onApproveAllPending,
                    onDeleteSubmission = onDeleteSubmission
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CitizenNewsSubmitStoryTab(
    isOdia: Boolean,
    onSubmitStory: (
        title: String,
        odiaTitle: String,
        snippet: String,
        content: String,
        category: String,
        locationWard: String,
        authorName: String,
        authorContact: String,
        sourceVerificationType: String,
        photoUri: String?,
        isBreakingCandidate: Boolean
    ) -> Unit,
    onNavigateToMySubmissions: () -> Unit,
    onNavigateToModerationDesk: () -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var odiaTitle by remember { mutableStateOf("") }
    var snippet by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Local") }
    var locationWard by remember { mutableStateOf("Fakir Mohan Golai, Balasore") }
    var authorName by remember { mutableStateOf("Balasore Citizen Reporter") }
    var authorContact by remember { mutableStateOf("") }
    var selectedSourceType by remember { mutableStateOf("Eyewitness Report") }
    var attachedPhotoUri by remember { mutableStateOf<String?>(null) }
    var isBreakingCandidate by remember { mutableStateOf(false) }
    var lastSubmittedHeadline by remember { mutableStateOf<String?>(null) }

    val categories = remember {
        listOf("Local", "Development", "Education", "Tourism", "Weather", "Sports", "Politics", "Emergency")
    }
    val quickWards = remember {
        listOf(
            "Fakir Mohan Golai, Balasore",
            "Chandipur Coast",
            "Remuna Block",
            "Soro Municipality",
            "Jaleswar Town",
            "Nilagiri",
            "Station Square, Ward 12"
        )
    }
    val sourceVerificationTypes = remember {
        listOf(
            "Eyewitness Report",
            "Photo Evidence Attached",
            "Ward / Municipal Notice",
            "Campus / Institutional Release"
        )
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            attachedPhotoUri = uri.toString()
            selectedSourceType = "Photo Evidence Attached"
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("citizen_news_submit_lazy_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // Editorial Moderation Policy Notice Banner
        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFFEFF6FF),
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = null,
                        tint = OceanBlue,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) {
                                "ସମ୍ପାଦକୀୟ ସମୀକ୍ଷା ନିୟମ (Moderation Policy)"
                            } else {
                                "Two-Stage Editorial Moderation Workflow"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlue
                        )
                        Text(
                            text = if (isOdia) {
                                "ଆପଣ ପଠାଇଥିବା ଖବର ପ୍ରଥମେ 'Pending Review' ତାଲିକାରେ ରହିବ। ମୋଡେରେଟର ଯାଞ୍ଚ ଓ ଅନୁମୋଦନ କରିବା ପରେ ହିଁ ଏହା ଲାଇଭ୍ ନ୍ୟୁଜ୍ ଫିଡ୍‌ରେ ପ୍ରକାଶିତ ହେବ।"
                            } else {
                                "Submitted stories enter the Moderation Review Queue (PENDING). A moderator reviews and approves each story before it goes live in the public Balasore News Feed."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            color = BentoSlate700
                        )
                    }
                }
            }
        }

        // Confirmation card after submitting a story
        if (lastSubmittedHeadline != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("citizen_news_submission_confirmation_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF3C7)),
                    border = BorderStroke(1.5.dp, Color(0xFFF59E0B))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.HourglassTop,
                                contentDescription = null,
                                tint = Color(0xFFB45309)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOdia) {
                                    "ଖବର ସମୀକ୍ଷା ପାଇଁ ଗ୍ରହଣ କରାଯାଇଛି (PENDING REVIEW)"
                                } else {
                                    "Story Queued for Moderation Review (PENDING)"
                                },
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Text(
                            text = "\"$lastSubmittedHeadline\" is now awaiting moderator verification. It will appear on the live news feed as soon as it is approved.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF78350F)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = onNavigateToMySubmissions,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("view_my_submitted_news_btn")
                            ) {
                                Text("Track Status", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = onNavigateToModerationDesk,
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("go_to_moderator_review_btn")
                            ) {
                                Text("Review & Approve Now", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Main Submission Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, BentoSlate200)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = if (isOdia) "✍️ ନୂତନ ସ୍ଥାନୀୟ ଖବର ବିବରଣୀ" else "✍️ Submit a Local Balasore News Story",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text(if (isOdia) "ଖବର ଶୀର୍ଷକ (English Headline) *" else "News Headline (English) *") },
                        placeholder = { Text("e.g., New Culvert Completed Near Remuna Golei") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_news_title_input")
                    )

                    OutlinedTextField(
                        value = odiaTitle,
                        onValueChange = { odiaTitle = it },
                        label = { Text(if (isOdia) "ଓଡ଼ିଆ ଶୀର୍ଷକ (Odia Headline - Optional)" else "Headline in Odia (Optional)") },
                        placeholder = { Text("ରେମୁଣା ଗୋଲେଇ ନିକଟରେ ନୂତନ କଲଭର୍ଟ ନିର୍ମାଣ ସମ୍ପନ୍ନ") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_news_odia_title_input")
                    )

                    Text(
                        text = if (isOdia) "ଖବର ବର୍ଗ ବାଛନ୍ତୁ (Category):" else "Select News Category:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory.equals(cat, ignoreCase = true),
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.5.sp) },
                                modifier = Modifier.testTag("submit_news_cat_${cat.lowercase()}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = locationWard,
                        onValueChange = { locationWard = it },
                        label = { Text(if (isOdia) "ସ୍ଥାନ / ୱାର୍ଡ (Ward or Locality in Balasore) *" else "Locality / Ward / Block in Balasore *") },
                        leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_news_location_input")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(quickWards) { ward ->
                            FilterChip(
                                selected = locationWard == ward,
                                onClick = { locationWard = ward },
                                label = { Text(ward, fontSize = 10.5.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = snippet,
                        onValueChange = { snippet = it },
                        label = { Text(if (isOdia) "ସଂକ୍ଷିପ୍ତ ସାରାଂଶ (Short Summary / Lead Snippet) *" else "Short Summary / Lead Snippet *") },
                        placeholder = { Text("1-2 sentences summarizing the key local update...") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_news_snippet_input")
                    )

                    OutlinedTextField(
                        value = content,
                        onValueChange = { content = it },
                        label = { Text(if (isOdia) "ସମ୍ପୂର୍ଣ୍ଣ ଖବର ବିବରଣୀ (Full News Story)" else "Full Story Details & Background") },
                        placeholder = { Text("Provide full context, timeline, who is affected, and local official/witness details...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_news_content_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = authorName,
                            onValueChange = { authorName = it },
                            label = { Text(if (isOdia) "ରିପୋର୍ଟରଙ୍କ ନାମ" else "Your Name / Byline") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("submit_news_author_input")
                        )

                        OutlinedTextField(
                            value = authorContact,
                            onValueChange = { authorContact = it },
                            label = { Text(if (isOdia) "ଫୋନ୍ / ଇମେଲ୍" else "Contact (Optional)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_news_contact_input")
                        )
                    }

                    Text(
                        text = if (isOdia) "ତଥ୍ୟର ଉତ୍ସ (Source Verification):" else "Source Verification Type:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        sourceVerificationTypes.forEachIndexed { index, sType ->
                            FilterChip(
                                selected = selectedSourceType == sType,
                                onClick = { selectedSourceType = sType },
                                label = { Text(sType, fontSize = 11.sp) },
                                modifier = Modifier.testTag("submit_news_source_type_$index")
                            )
                        }
                    }

                    // Optional Photo Evidence Section
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_news_attach_photo_btn")
                        ) {
                            Icon(Icons.Default.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (attachedPhotoUri != null) "Change Photo" else "Attach Photo",
                                fontSize = 11.5.sp
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                attachedPhotoUri = "content://com.example.balasore/citizen_news_evidence_${System.currentTimeMillis()}.jpg"
                                selectedSourceType = "Photo Evidence Attached"
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("submit_news_quick_photo_btn")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Quick Snap Evidence", fontSize = 11.5.sp)
                        }
                    }

                    if (attachedPhotoUri != null) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFECFDF5),
                            border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("submit_news_photo_attached_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "📷 Photo evidence attached for moderator verification",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF065F46),
                                    modifier = Modifier.weight(1f)
                                )
                                TextButton(onClick = { attachedPhotoUri = null }) {
                                    Text("Remove", fontSize = 11.sp, color = Color(0xFFDC2626))
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isOdia) "ଜରୁରୀ ବ୍ରେକିଂ ନ୍ୟୁଜ୍ ପ୍ରস্তାବ" else "Flag as Breaking News Candidate",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isOdia) {
                                    "ମୋଡେରେଟର ଅନୁମୋଦନ କଲେ ପୁଶ୍ ଆଲର୍ଟ ପଠାযିବ"
                                } else {
                                    "Requests priority review from the District Editorial Desk"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = isBreakingCandidate,
                            onCheckedChange = { isBreakingCandidate = it },
                            modifier = Modifier.testTag("submit_news_breaking_switch")
                        )
                    }

                    Button(
                        onClick = {
                            val cleanTitle = title.trim()
                            val cleanSnippet = snippet.trim().ifBlank { content.trim().take(140) }
                            if (cleanTitle.isBlank() || cleanSnippet.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter a news headline and summary snippet",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            onSubmitStory(
                                cleanTitle,
                                odiaTitle.trim().ifBlank { cleanTitle },
                                cleanSnippet,
                                content.trim().ifBlank { cleanSnippet },
                                selectedCategory,
                                locationWard.trim().ifBlank { "Balasore Town" },
                                authorName.trim().ifBlank { "Balasore Citizen Reporter" },
                                authorContact.trim(),
                                selectedSourceType,
                                attachedPhotoUri,
                                isBreakingCandidate
                            )
                            lastSubmittedHeadline = cleanTitle
                            title = ""
                            odiaTitle = ""
                            snippet = ""
                            content = ""
                            attachedPhotoUri = null
                            isBreakingCandidate = false
                            Toast.makeText(
                                context,
                                "News story submitted to Moderation Queue (PENDING)!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("submit_news_for_review_btn"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia) {
                                "ସମୀକ୍ଷା ପାଇଁ ଖବର ଦାଖଲ କରନ୍ତୁ (Submit for Moderation)"
                            } else {
                                "Submit Story for Moderation Review"
                            },
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CitizenNewsSubmissionsTrackerTab(
    submissions: List<CommunityNewsSubmissionEntity>,
    isOdia: Boolean,
    onUpdateAndResubmit: (
        submissionId: String,
        title: String,
        odiaTitle: String,
        snippet: String,
        content: String,
        category: String,
        locationWard: String
    ) -> Unit,
    onOpenModeratorDesk: () -> Unit
) {
    val context = LocalContext.current
    var editingSubmissionId by remember { mutableStateOf<String?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editOdiaTitle by remember { mutableStateOf("") }
    var editSnippet by remember { mutableStateOf("") }
    var editContent by remember { mutableStateOf("") }
    var editCategory by remember { mutableStateOf("Local") }
    var editWard by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("citizen_news_submissions_tracker_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isOdia) "📋 ଦାଖଲ ହୋଇଥିବା ଖବର ଓ ସ୍ଥିତି" else "📋 Citizen Submitted Stories & Status",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = if (isOdia) {
                            "କେବଳ 'Approved & Live' ଖବର ସର୍ବସାଧାରଣ ଫିଡ୍‌ରେ ଦେଖାଯାଏ"
                        } else {
                            "Only 'Approved & Live' stories appear in the public Balasore News Feed"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                TextButton(
                    onClick = onOpenModeratorDesk,
                    modifier = Modifier.testTag("tracker_open_moderator_desk_btn")
                ) {
                    Icon(Icons.Default.Gavel, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Moderator Desk", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        items(submissions, key = { it.id }) { item ->
            val status = item.moderationStatus
            val isEditingThis = editingSubmissionId == item.id

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("citizen_submission_item_${item.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    1.dp,
                    when (status) {
                        NewsModerationStatus.PENDING -> Color(0xFFF59E0B)
                        NewsModerationStatus.APPROVED -> Color(0xFF22C55E)
                        NewsModerationStatus.REVISION_REQUESTED -> OceanBlue
                        NewsModerationStatus.REJECTED -> Color(0xFFEF4444)
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        NewsModerationStatusBadge(status = status, isOdia = isOdia)
                        Text(
                            text = "${item.category} • ${item.locationWard}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = item.snippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Moderator feedback box
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Editorial Note • By ${item.moderatedBy ?: "Moderation Queue"}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                            Text(
                                text = item.moderatorNote,
                                style = MaterialTheme.typography.bodySmall,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    if (status == NewsModerationStatus.PENDING || status == NewsModerationStatus.REVISION_REQUESTED) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = {
                                    if (isEditingThis) {
                                        editingSubmissionId = null
                                    } else {
                                        editingSubmissionId = item.id
                                        editTitle = item.title
                                        editOdiaTitle = item.odiaTitle
                                        editSnippet = item.snippet
                                        editContent = item.content
                                        editCategory = item.category
                                        editWard = item.locationWard
                                    }
                                },
                                modifier = Modifier.testTag("citizen_news_edit_resubmit_btn_${item.id}")
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (isEditingThis) "Cancel Edit" else "Edit & Resubmit",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isEditingThis) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editTitle,
                                onValueChange = { editTitle = it },
                                label = { Text("Updated Headline") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("resubmit_news_title_input_${item.id}")
                            )
                            OutlinedTextField(
                                value = editSnippet,
                                onValueChange = { editSnippet = it },
                                label = { Text("Updated Summary") },
                                minLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("resubmit_news_snippet_input_${item.id}")
                            )
                            OutlinedTextField(
                                value = editWard,
                                onValueChange = { editWard = it },
                                label = { Text("Updated Ward / Location") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("resubmit_news_ward_input_${item.id}")
                            )
                            Button(
                                onClick = {
                                    onUpdateAndResubmit(
                                        item.id,
                                        editTitle,
                                        editOdiaTitle,
                                        editSnippet,
                                        editContent,
                                        editCategory,
                                        editWard
                                    )
                                    editingSubmissionId = null
                                    Toast.makeText(
                                        context,
                                        "Story updated & resubmitted for moderator review!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("confirm_resubmit_news_btn_${item.id}")
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save & Resubmit to Moderation Queue")
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Dedicated Moderator Review & Approval System Tab.
 * Allows moderators to review submitted local news stories, inspect automated fact-check pre-checks,
 * optionally refine headlines/content, and Approve (publish live), Request Revision, or Reject stories.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CitizenNewsModeratorReviewTab(
    submissions: List<CommunityNewsSubmissionEntity>,
    pendingCount: Int,
    approvedCount: Int,
    isOdia: Boolean,
    onApproveSubmission: (
        submissionId: String,
        moderatorNote: String,
        editedTitle: String?,
        editedOdiaTitle: String?,
        editedSnippet: String?,
        editedContent: String?,
        editedCategory: String?
    ) -> Unit,
    onRejectSubmission: (submissionId: String, moderatorNote: String) -> Unit,
    onRequestRevision: (submissionId: String, moderatorNote: String) -> Unit,
    onApproveAllPending: () -> Unit,
    onDeleteSubmission: (String) -> Unit
) {
    val context = LocalContext.current
    var selectedStatusFilter by remember { mutableStateOf("ALL") } // "ALL", "PENDING", "APPROVED", "REVISION_REQUESTED", "REJECTED"
    var searchQuery by remember { mutableStateOf("") }

    val revisionCount = remember(submissions) {
        submissions.count { it.moderationStatus == NewsModerationStatus.REVISION_REQUESTED }
    }
    val rejectedCount = remember(submissions) {
        submissions.count { it.moderationStatus == NewsModerationStatus.REJECTED }
    }

    val filteredSubmissions = remember(submissions, selectedStatusFilter, searchQuery) {
        submissions.filter { item ->
            val matchesStatus = when (selectedStatusFilter) {
                "PENDING" -> item.moderationStatus == NewsModerationStatus.PENDING
                "APPROVED" -> item.moderationStatus == NewsModerationStatus.APPROVED
                "REVISION_REQUESTED" -> item.moderationStatus == NewsModerationStatus.REVISION_REQUESTED
                "REJECTED" -> item.moderationStatus == NewsModerationStatus.REJECTED
                else -> true
            }
            val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.snippet.contains(searchQuery, ignoreCase = true) ||
                item.authorName.contains(searchQuery, ignoreCase = true) ||
                item.locationWard.contains(searchQuery, ignoreCase = true) ||
                item.category.contains(searchQuery, ignoreCase = true)
            matchesStatus && matchesQuery
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("citizen_news_moderator_review_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        // 1. Moderator Header & Batch Approval Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                ),
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f))
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
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Gavel,
                                contentDescription = null,
                                tint = OceanBlue
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isOdia) {
                                        "🛡️ ସମ୍ପାଦକୀୟ ସମୀକ୍ଷା ଓ ଅନୁମୋଦନ ଡେସ୍କ"
                                    } else {
                                        "🛡️ Editorial News Moderation System"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isOdia) {
                                        "ନାଗରିକ ଖବର ଯାଞ୍ଚ କରନ୍ତୁ • ଅନୁମୋଦନ କଲେ ତୁରନ୍ତ ଲାଇଭ୍ ଫିଡ୍‌କୁ ଯିବ"
                                    } else {
                                        "Review citizen stories • Approving a story publishes it live immediately"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Filter Chips Row
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val filterItems = listOf(
                            Triple("ALL", "All (${submissions.size})", "news_mod_filter_all"),
                            Triple("PENDING", "⏳ Pending ($pendingCount)", "news_mod_filter_pending"),
                            Triple("APPROVED", "✓ Live ($approvedCount)", "news_mod_filter_approved"),
                            Triple("REVISION_REQUESTED", "✏️ Revision ($revisionCount)", "news_mod_filter_revision"),
                            Triple("REJECTED", "✕ Rejected ($rejectedCount)", "news_mod_filter_rejected")
                        )
                        filterItems.forEach { (key, label, tag) ->
                            FilterChip(
                                selected = selectedStatusFilter == key,
                                onClick = { selectedStatusFilter = key },
                                label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OceanBlue.copy(alpha = 0.16f),
                                    selectedLabelColor = OceanBlue
                                ),
                                modifier = Modifier.testTag(tag)
                            )
                        }
                    }

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Search submitted headlines, wards, or citizen reporters...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("news_mod_search_input")
                    )

                    if (pendingCount > 0) {
                        Button(
                            onClick = {
                                onApproveAllPending()
                                Toast.makeText(
                                    context,
                                    "Approved & published all $pendingCount pending stories to Live Feed!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("news_mod_approve_all_pending_btn")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Approve & Publish All Pending Stories ($pendingCount)",
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // 2. Submissions List for Moderation
        items(filteredSubmissions, key = { it.id }) { item ->
            val preCheck = remember(item) {
                CommunityNewsModerationRepository.evaluateEditorialChecklist(item)
            }
            var decisionNote by remember(item.id, item.moderatorNote) {
                mutableStateOf("")
            }
            var showInlineEditor by remember(item.id) { mutableStateOf(false) }
            var editedTitle by remember(item.id, item.title) { mutableStateOf(item.title) }
            var editedOdiaTitle by remember(item.id, item.odiaTitle) { mutableStateOf(item.odiaTitle) }
            var editedSnippet by remember(item.id, item.snippet) { mutableStateOf(item.snippet) }
            var editedContent by remember(item.id, item.content) { mutableStateOf(item.content) }
            var editedCategory by remember(item.id, item.category) { mutableStateOf(item.category) }

            val status = item.moderationStatus

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("news_mod_submission_card_${item.id}"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(
                    width = if (status == NewsModerationStatus.PENDING) 1.5.dp else 1.dp,
                    color = when (status) {
                        NewsModerationStatus.PENDING -> Color(0xFFF59E0B)
                        NewsModerationStatus.APPROVED -> Color(0xFF22C55E)
                        NewsModerationStatus.REVISION_REQUESTED -> OceanBlue
                        NewsModerationStatus.REJECTED -> Color(0xFFEF4444)
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Status Badge + Category + Timestamp
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            NewsModerationStatusBadge(status = status, isOdia = isOdia)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OceanBlue.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = item.category,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }

                        Text(
                            text = dateFormatter.format(Date(item.submittedAt)),
                            style = MaterialTheme.typography.labelSmall,
                            color = BentoSlate500
                        )
                    }

                    // Automated Editorial Pre-Check Banner
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (preCheck.trustScorePercent >= 80) Color(0xFFECFDF5) else Color(0xFFFEF3C7),
                        border = BorderStroke(
                            1.dp,
                            if (preCheck.trustScorePercent >= 80) Color(0xFF6EE7B7) else Color(0xFFFCD34D)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🔍 Pre-Check: ${preCheck.summaryNote}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (preCheck.trustScorePercent >= 80) Color(0xFF065F46) else Color(0xFF92400E),
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${preCheck.trustScorePercent}% Verify",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (preCheck.trustScorePercent >= 80) Color(0xFF047857) else Color(0xFFB45309)
                            )
                        }
                    }

                    // Headline & Story Content
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (item.odiaTitle.isNotBlank() && item.odiaTitle != item.title) {
                        Text(
                            text = item.odiaTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = OceanBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = item.snippet,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (item.content.isNotBlank() && item.content != item.snippet) {
                        Text(
                            text = item.content,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Reporter & Location Metadata
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👤 ${item.authorName} • 📍 ${item.locationWard}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = BentoSlate600
                        )
                        Text(
                            text = "📋 ${item.sourceVerificationType}",
                            style = MaterialTheme.typography.labelSmall,
                            color = OceanBlue,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Optional Inline Editorial Refinement before Approving
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Current Note: ${item.moderatorNote}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        TextButton(
                            onClick = { showInlineEditor = !showInlineEditor },
                            modifier = Modifier.testTag("news_mod_toggle_edit_btn_${item.id}")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showInlineEditor) "Hide Editor" else "Edit Copy",
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    AnimatedVisibility(visible = showInlineEditor) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editedTitle,
                                onValueChange = { editedTitle = it },
                                label = { Text("Editorial Headline (English)") },
                                singleLine = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("news_mod_edit_title_${item.id}")
                            )
                            OutlinedTextField(
                                value = editedSnippet,
                                onValueChange = { editedSnippet = it },
                                label = { Text("Editorial Summary / Lead Snippet") },
                                minLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("news_mod_edit_snippet_${item.id}")
                            )
                            OutlinedTextField(
                                value = editedContent,
                                onValueChange = { editedContent = it },
                                label = { Text("Full Editorial Content") },
                                minLines = 2,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("news_mod_edit_content_${item.id}")
                            )
                        }
                    }

                    // Moderator Decision Note Field + Quick Presets
                    OutlinedTextField(
                        value = decisionNote,
                        onValueChange = { decisionNote = it },
                        label = { Text("Moderator Review Note / Approval or Rejection Reason") },
                        placeholder = { Text("e.g., Fact-checked with Balasore Ward Office & approved") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("news_mod_note_input_${item.id}")
                    )

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val presetNotes = listOf(
                            "Verified with local ward office — Approved",
                            "Confirmed eyewitness photo & details",
                            "Please specify exact street/ward location",
                            "Rejected: Unverified claim / lacks source"
                        )
                        items(presetNotes) { preset ->
                            FilterChip(
                                selected = decisionNote == preset,
                                onClick = { decisionNote = preset },
                                label = { Text(preset, fontSize = 10.sp) }
                            )
                        }
                    }

                    HorizontalDivider(color = BentoSlate200)

                    // Primary Moderator Action Buttons: Approve & Publish Live | Request Revision | Reject | Delete
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                onApproveSubmission(
                                    item.id,
                                    decisionNote.trim().ifBlank {
                                        "Verified & Approved by Baleshwar 360 Moderator — Live on News Feed"
                                    },
                                    editedTitle,
                                    editedOdiaTitle,
                                    editedSnippet,
                                    editedContent,
                                    editedCategory
                                )
                                showInlineEditor = false
                                Toast.makeText(
                                    context,
                                    "Approved! Story is now LIVE on the Balasore News Feed.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1.35f)
                                .testTag("news_mod_approve_btn_${item.id}")
                        ) {
                            Icon(Icons.Default.Publish, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (status == NewsModerationStatus.APPROVED) "Update Live" else "Approve & Go Live",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onRequestRevision(
                                    item.id,
                                    decisionNote.trim().ifBlank {
                                        "Revision requested: Please add specific location and witness details."
                                    }
                                )
                                Toast.makeText(
                                    context,
                                    "Revision requested from citizen reporter",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(0.95f)
                                .testTag("news_mod_revision_btn_${item.id}")
                        ) {
                            Text(
                                text = "Revision",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                onRejectSubmission(
                                    item.id,
                                    decisionNote.trim().ifBlank {
                                        "Rejected by Moderator: Could not verify local source accuracy."
                                    }
                                )
                                Toast.makeText(
                                    context,
                                    "Story rejected and blocked from public feed",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = BorderStroke(1.dp, Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(0.85f)
                                .testTag("news_mod_reject_btn_${item.id}")
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = "Reject",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1
                            )
                        }

                        IconButton(
                            onClick = {
                                onDeleteSubmission(item.id)
                                Toast.makeText(context, "Submission deleted", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .testTag("news_mod_delete_btn_${item.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete submission",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsModerationStatusBadge(
    status: NewsModerationStatus,
    isOdia: Boolean = false
) {
    val (bgColor, textColor, icon, text) = when (status) {
        NewsModerationStatus.PENDING -> Quadruple(
            Color(0xFFFEF3C7),
            Color(0xFFB45309),
            Icons.Default.HourglassTop,
            if (isOdia) "⏳ ସମୀକ୍ଷା ବାକି" else "⏳ PENDING REVIEW"
        )
        NewsModerationStatus.APPROVED -> Quadruple(
            Color(0xFFDCFCE7),
            Color(0xFF15803D),
            Icons.Default.Verified,
            if (isOdia) "✓ ଅନୁମୋଦିତ ଓ ଲାଇଭ୍" else "✓ APPROVED & LIVE"
        )
        NewsModerationStatus.REVISION_REQUESTED -> Quadruple(
            Color(0xFFE0F2FE),
            Color(0xFF0369A1),
            Icons.Default.Edit,
            if (isOdia) "✏️ ସଂଶୋଧନ ଆବଶ୍ୟକ" else "✏️ REVISION NEEDED"
        )
        NewsModerationStatus.REJECTED -> Quadruple(
            Color(0xFFFEE2E2),
            Color(0xFFB91C1C),
            Icons.Default.ReportProblem,
            if (isOdia) "✕ ପ୍ରତ୍ୟାଖ୍ୟାତ" else "✕ REJECTED"
        )
    }

    Surface(
        shape = RoundedCornerShape(8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
            Text(
                text = text,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
