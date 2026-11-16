package com.example.ui.features.admin

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.fcm.FcmManager
import com.example.data.model.AlertSeverity
import com.example.data.model.AppLanguage
import com.example.data.model.BalasoreWeatherAlert
import com.example.data.model.BalasoreZone
import com.example.data.local.CommunityNewsSubmissionEntity
import com.example.data.local.NewsModerationStatus
import com.example.data.model.Hotspot
import com.example.data.model.NewsArticle
import com.example.data.model.WeatherAlertCategory
import com.example.data.model.WeatherInfo
import com.example.data.repository.CommunityNewsModerationRepository
import com.example.ui.features.news.NewsModerationStatusBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminModeratorPanelSheet(
    language: AppLanguage = AppLanguage.ENGLISH,
    newsArticles: List<NewsArticle>,
    weatherInfo: WeatherInfo,
    weatherAlerts: List<BalasoreWeatherAlert>,
    hotspots: List<Hotspot>,
    onSaveNewsArticle: (NewsArticle) -> Unit,
    onDeleteNewsArticle: (String) -> Unit,
    onUpdateWeatherInfo: (WeatherInfo) -> Unit,
    onSaveWeatherAlert: (BalasoreWeatherAlert) -> Unit,
    onDeleteWeatherAlert: (String) -> Unit,
    onSaveHotspot: (Hotspot) -> Unit,
    onDeleteHotspot: (String) -> Unit,
    onClose: () -> Unit
) {
    val isOdia = language == AppLanguage.ODIA
    var selectedTab by remember { mutableIntStateOf(0) }
    var moderatorUnlocked by remember { mutableStateOf(true) }
    var passkeyInput by remember { mutableStateOf("BLS-ADMIN-2026") }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_moderator_panel_sheet"),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = "Admin Moderator Panel",
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = if (isOdia) {
                                    "ପ୍ରଶାସନିକ ମୋଡେରେଟର ପ୍ୟାନେଲ୍"
                                } else {
                                    "Administrative Moderator Panel"
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = if (isOdia) {
                                    "ସ୍ଥାନୀୟ ଖବର, ପାଣିପାଗ ଏବଂ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ପରିଚାଳନା କରନ୍ତୁ"
                                } else {
                                    "Add, Edit & Remove Local News, Weather & Tourism Hotspots"
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
                        modifier = Modifier.testTag("close_admin_panel_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Admin Panel")
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
            // Moderator role banner
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isOdia) {
                                "ଯାଞ୍ଚকৃত ଜିଲ୍ଲା ମୋଡେରେଟର • ପରିବର୍ତ୍ତନଗୁଡ଼ିକ ତୁରନ୍ତ ସିଙ୍କ୍ ହୁଏ"
                            } else {
                                "Verified District Moderator • Changes sync immediately to Room & Live UI"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    AssistChip(
                        onClick = { moderatorUnlocked = !moderatorUnlocked },
                        label = {
                            Text(
                                text = if (moderatorUnlocked) "ACTIVE" else "LOCKED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        },
                        modifier = Modifier.testTag("admin_role_status_chip")
                    )
                }
            }

            if (!moderatorUnlocked) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Enter District Moderator Passkey",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        OutlinedTextField(
                            value = passkeyInput,
                            onValueChange = { passkeyInput = it },
                            label = { Text("Moderator Passkey") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_passkey_input")
                        )
                        Button(
                            onClick = { moderatorUnlocked = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_unlock_btn")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Unlock Administrative Controls")
                        }
                    }
                }
            } else {
                val moderationItems by ContentModerationRepository.itemsFlow.collectAsState()
                val attentionCount = remember(moderationItems) {
                    moderationItems.count { it.status == ModerationStatus.FLAGGED || it.status == ModerationStatus.PENDING }
                }

                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 8.dp
                ) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.testTag("admin_tab_content_moderation"),
                        text = {
                            Text(
                                if (isOdia) {
                                    "ମୋଡେରେସନ୍ ($attentionCount)"
                                } else {
                                    "Moderation ($attentionCount)"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = { Icon(Icons.Default.Gavel, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.testTag("admin_tab_news"),
                        text = {
                            Text(
                                if (isOdia) {
                                    "ସ୍ଥାନୀୟ ଖବର (${newsArticles.size})"
                                } else {
                                    "Local News (${newsArticles.size})"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = { Icon(Icons.Default.Newspaper, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        modifier = Modifier.testTag("admin_tab_weather"),
                        text = {
                            Text(
                                if (isOdia) {
                                    "ପାଣିପାଗ (${weatherAlerts.size})"
                                } else {
                                    "Weather (${weatherAlerts.size})"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = { Icon(Icons.Default.Thunderstorm, contentDescription = null) }
                    )
                    Tab(
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        modifier = Modifier.testTag("admin_tab_tourism"),
                        text = {
                            Text(
                                if (isOdia) {
                                    "ପର୍ଯ୍ୟଟନ (${hotspots.size})"
                                } else {
                                    "Tourism (${hotspots.size})"
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        icon = { Icon(Icons.Default.Explore, contentDescription = null) }
                    )
                }

                when (selectedTab) {
                    0 -> AdminContentModerationTab(isOdia = isOdia)
                    1 -> AdminNewsModerationTab(
                        newsArticles = newsArticles,
                        onSaveNewsArticle = onSaveNewsArticle,
                        onDeleteNewsArticle = onDeleteNewsArticle
                    )
                    2 -> AdminWeatherModerationTab(
                        weatherInfo = weatherInfo,
                        weatherAlerts = weatherAlerts,
                        onUpdateWeatherInfo = onUpdateWeatherInfo,
                        onSaveWeatherAlert = onSaveWeatherAlert,
                        onDeleteWeatherAlert = onDeleteWeatherAlert
                    )
                    3 -> AdminTourismHotspotModerationTab(
                        hotspots = hotspots,
                        onSaveHotspot = onSaveHotspot,
                        onDeleteHotspot = onDeleteHotspot
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminNewsModerationTab(
    newsArticles: List<NewsArticle>,
    onSaveNewsArticle: (NewsArticle) -> Unit,
    onDeleteNewsArticle: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val newsCategories = remember {
        listOf("Politics", "Sports", "Tourism", "Defense", "Development")
    }

    var editingArticleId by remember { mutableStateOf<String?>(null) }
    var title by remember { mutableStateOf("") }
    var odiaTitle by remember { mutableStateOf("") }
    var snippet by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var source by remember { mutableStateOf("Balasore District Bureau") }
    var selectedCategory by remember { mutableStateOf("Development") }
    var sendBreakingPush by remember { mutableStateOf(true) }

    fun resetForm() {
        editingArticleId = null
        title = ""
        odiaTitle = ""
        snippet = ""
        content = ""
        source = "Balasore District Bureau"
        selectedCategory = "Development"
    }

    val citizenSubmissions by CommunityNewsModerationRepository.submissionsStateFlow.collectAsState()
    val pendingCitizenSubmissions = remember(citizenSubmissions) {
        citizenSubmissions.filter { it.moderationStatus == NewsModerationStatus.PENDING }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_citizen_news_moderation_queue_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🛡️ Citizen-Submitted Local News Queue (${pendingCitizenSubmissions.size} Pending Review)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Review user-submitted stories before they go live on the Balasore News Feed.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (pendingCitizenSubmissions.isEmpty()) {
                        Text(
                            text = "✓ All citizen-submitted news stories have been reviewed.",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        pendingCitizenSubmissions.forEach { submission ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("admin_pending_citizen_news_${submission.id}")
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
                                        NewsModerationStatusBadge(status = submission.moderationStatus)
                                        Text(
                                            text = "${submission.category} • ${submission.locationWard}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Text(
                                        text = submission.title,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = submission.snippet,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Submitted by ${submission.authorName} (${submission.sourceVerificationType})",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Button(
                                            onClick = {
                                                scope.launch {
                                                    val result = CommunityNewsModerationRepository
                                                        .getInstance(context)
                                                        .approveAndPublishSubmission(
                                                            submissionId = submission.id,
                                                            moderatorNote = "Approved via Admin Moderator Panel"
                                                        )
                                                    if (result != null) {
                                                        onSaveNewsArticle(result.second)
                                                    }
                                                }
                                                Toast.makeText(
                                                    context,
                                                    "Approved & published citizen story to Live Feed!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("admin_approve_citizen_news_${submission.id}")
                                        ) {
                                            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text("Approve & Go Live", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                        OutlinedButton(
                                            onClick = {
                                                scope.launch {
                                                    CommunityNewsModerationRepository
                                                        .getInstance(context)
                                                        .rejectSubmission(
                                                            submissionId = submission.id,
                                                            moderatorNote = "Rejected via Admin Moderator Panel"
                                                        )
                                                }
                                                Toast.makeText(
                                                    context,
                                                    "Rejected citizen news submission",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            },
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = MaterialTheme.colorScheme.error
                                            ),
                                            modifier = Modifier
                                                .weight(0.8f)
                                                .testTag("admin_reject_citizen_news_${submission.id}")
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Reject", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
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
                        Text(
                            text = if (editingArticleId == null) {
                                "➕ Add New Local News Article"
                            } else {
                                "✏️ Edit News Article ($editingArticleId)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (editingArticleId != null) {
                            TextButton(
                                onClick = { resetForm() },
                                modifier = Modifier.testTag("admin_cancel_edit_news_btn")
                            ) {
                                Text("Cancel Edit")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Headline (English)") },
                        placeholder = { Text("e.g., Subarnarekha Flood Embankment Upgraded") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_news_title_input")
                    )

                    OutlinedTextField(
                        value = odiaTitle,
                        onValueChange = { odiaTitle = it },
                        label = { Text("Headline (Odia - Optional)") },
                        placeholder = { Text("ସୁବର୍ଣ୍ଣରେଖା ବନ୍ୟା ବନ୍ଧ ସୁଦୃଢ଼ୀକରଣ") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_news_odia_title_input")
                    )

                    OutlinedTextField(
                        value = snippet,
                        onValueChange = { snippet = it },
                        label = { Text("Summary / Breaking Snippet") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_news_snippet_input")
                    )

                    OutlinedTextField(
                        value = source,
                        onValueChange = { source = it },
                        label = { Text("Source / Bureau") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_news_source_input")
                    )

                    Text(
                        text = "Select Category:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        newsCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory.equals(cat, ignoreCase = true),
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                modifier = Modifier.testTag("admin_news_cat_${cat.lowercase()}")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Send Major Breaking News Push Alert",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Delivers instant Android notification to user devices",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = sendBreakingPush,
                            onCheckedChange = { sendBreakingPush = it },
                            modifier = Modifier.testTag("admin_news_send_push_switch")
                        )
                    }

                    Button(
                        onClick = {
                            if (title.isBlank() || snippet.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter both a headline and summary",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            val articleId = editingArticleId ?: "news_mod_${System.currentTimeMillis()}"
                            val savedArticle = NewsArticle(
                                id = articleId,
                                title = title.trim(),
                                odiaTitle = odiaTitle.trim().ifBlank { title.trim() },
                                snippet = snippet.trim(),
                                odiaSnippet = snippet.trim(),
                                category = selectedCategory,
                                timeAgo = if (editingArticleId == null) "Just now" else "Edited just now",
                                source = source.trim().ifBlank { "Balasore Moderator Desk" },
                                isBookmarked = false,
                                content = content.trim().ifBlank { snippet.trim() },
                                odiaContent = snippet.trim()
                            )
                            onSaveNewsArticle(savedArticle)
                            if (sendBreakingPush) {
                                scope.launch {
                                    FcmManager.dispatchCustomBreakingNewsPush(
                                        context = context,
                                        title = savedArticle.title,
                                        summary = savedArticle.snippet,
                                        category = savedArticle.category,
                                        source = savedArticle.source
                                    )
                                }
                            }
                            Toast.makeText(
                                context,
                                if (editingArticleId == null) "News published!" else "News updated!",
                                Toast.LENGTH_SHORT
                            ).show()
                            resetForm()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_save_news_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (editingArticleId == null) Icons.Default.Publish else Icons.Default.Save,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (editingArticleId == null) {
                                "Publish Local News"
                            } else {
                                "Save News Changes"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Published District News (${newsArticles.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }

        items(newsArticles, key = { it.id }) { article ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_news_item_${article.id}"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Text(
                                text = article.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Text(
                            text = "${article.source} • ${article.timeAgo}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = article.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = article.snippet,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    FcmManager.dispatchCustomBreakingNewsPush(
                                        context = context,
                                        title = article.title,
                                        summary = article.snippet,
                                        category = article.category,
                                        source = article.source
                                    )
                                }
                                Toast.makeText(
                                    context,
                                    "Breaking News Push Sent!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.testTag("admin_push_news_${article.id}")
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push Alert", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = {
                                editingArticleId = article.id
                                title = article.title
                                odiaTitle = article.odiaTitle
                                snippet = article.snippet
                                content = article.content
                                source = article.source
                                selectedCategory = article.category
                            },
                            modifier = Modifier.testTag("admin_edit_news_${article.id}")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = {
                                onDeleteNewsArticle(article.id)
                                Toast.makeText(
                                    context,
                                    "Removed news article",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.testTag("admin_delete_news_${article.id}")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminWeatherModerationTab(
    weatherInfo: WeatherInfo,
    weatherAlerts: List<BalasoreWeatherAlert>,
    onUpdateWeatherInfo: (WeatherInfo) -> Unit,
    onSaveWeatherAlert: (BalasoreWeatherAlert) -> Unit,
    onDeleteWeatherAlert: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Live telemetry editor state
    var tempText by remember(weatherInfo.tempCelsius) { mutableStateOf(weatherInfo.tempCelsius.toString()) }
    var conditionText by remember(weatherInfo.condition) { mutableStateOf(weatherInfo.condition) }
    var humidityText by remember(weatherInfo.humidity) { mutableStateOf(weatherInfo.humidity) }
    var windSpeedText by remember(weatherInfo.windSpeedKmh) { mutableStateOf(weatherInfo.windSpeedKmh) }
    var tideStatusText by remember(weatherInfo.tideStatus) { mutableStateOf(weatherInfo.tideStatus) }
    var seaConditionText by remember(weatherInfo.seaCondition) { mutableStateOf(weatherInfo.seaCondition) }
    var cycloneAlertText by remember(weatherInfo.cycloneAlert) { mutableStateOf(weatherInfo.cycloneAlert ?: "") }

    // Weather alert CRUD form state
    var editingAlertId by remember { mutableStateOf<String?>(null) }
    var alertTitle by remember { mutableStateOf("") }
    var alertSummary by remember { mutableStateOf("") }
    var selectedZone by remember { mutableStateOf(BalasoreZone.COASTAL_CHANDIPUR) }
    var selectedSeverity by remember { mutableStateOf(AlertSeverity.WARNING) }
    var sendUrgentWeatherPush by remember { mutableStateOf(true) }

    fun resetAlertForm() {
        editingAlertId = null
        alertTitle = ""
        alertSummary = ""
        selectedZone = BalasoreZone.COASTAL_CHANDIPUR
        selectedSeverity = AlertSeverity.WARNING
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section 1: Live Weather Conditions Updater
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🌤️ Update Live Balasore Weather Telemetry",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tempText,
                            onValueChange = { tempText = it },
                            label = { Text("Temp (°C)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_weather_temp_input")
                        )
                        OutlinedTextField(
                            value = humidityText,
                            onValueChange = { humidityText = it },
                            label = { Text("Humidity") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_weather_humidity_input")
                        )
                        OutlinedTextField(
                            value = windSpeedText,
                            onValueChange = { windSpeedText = it },
                            label = { Text("Wind") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_weather_wind_input")
                        )
                    }

                    OutlinedTextField(
                        value = conditionText,
                        onValueChange = { conditionText = it },
                        label = { Text("Sky / Weather Condition") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_weather_condition_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = tideStatusText,
                            onValueChange = { tideStatusText = it },
                            label = { Text("Chandipur Tide Status") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_weather_tide_input")
                        )
                        OutlinedTextField(
                            value = seaConditionText,
                            onValueChange = { seaConditionText = it },
                            label = { Text("Sea Condition") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_weather_sea_input")
                        )
                    }

                    OutlinedTextField(
                        value = cycloneAlertText,
                        onValueChange = { cycloneAlertText = it },
                        label = { Text("Dashboard Weather Warning Banner (Leave empty for Clear)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_weather_cyclone_banner_input")
                    )

                    Button(
                        onClick = {
                            val updatedWeather = weatherInfo.copy(
                                tempCelsius = tempText.toIntOrNull() ?: weatherInfo.tempCelsius,
                                condition = conditionText.trim().ifBlank { weatherInfo.condition },
                                humidity = humidityText.trim().ifBlank { weatherInfo.humidity },
                                windSpeedKmh = windSpeedText.trim().ifBlank { weatherInfo.windSpeedKmh },
                                tideStatus = tideStatusText.trim().ifBlank { weatherInfo.tideStatus },
                                seaCondition = seaConditionText.trim().ifBlank { weatherInfo.seaCondition },
                                cycloneAlert = cycloneAlertText.trim().ifBlank { null }
                            )
                            onUpdateWeatherInfo(updatedWeather)
                            Toast.makeText(
                                context,
                                "Live Balasore Weather Updated!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_save_weather_telemetry_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save Weather Telemetry Update", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Section 2: Add / Edit Urgent Weather Alert
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
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
                        Text(
                            text = if (editingAlertId == null) {
                                "🚨 Add Urgent Weather Alert"
                            } else {
                                "✏️ Edit Weather Alert ($editingAlertId)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (editingAlertId != null) {
                            TextButton(onClick = { resetAlertForm() }) {
                                Text("Cancel Edit")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = alertTitle,
                        onValueChange = { alertTitle = it },
                        label = { Text("Weather Alert Title") },
                        placeholder = { Text("e.g., IMD Red Alert: Squally Winds at Chandipur") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_weather_alert_title_input")
                    )

                    OutlinedTextField(
                        value = alertSummary,
                        onValueChange = { alertSummary = it },
                        label = { Text("Alert Instructions / Warning Message") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_weather_alert_summary_input")
                    )

                    Text(
                        text = "Select Affected Zone:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            BalasoreZone.COASTAL_CHANDIPUR,
                            BalasoreZone.TALASARI_SUBARNAREKHA,
                            BalasoreZone.BALASORE_TOWN,
                            BalasoreZone.DISTRICT_WIDE
                        ).forEach { zone ->
                            FilterChip(
                                selected = selectedZone == zone,
                                onClick = { selectedZone = zone },
                                label = { Text(zone.name.take(10), fontSize = 10.sp) },
                                modifier = Modifier.testTag("admin_alert_zone_${zone.name.lowercase()}")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        AlertSeverity.entries.forEach { severity ->
                            FilterChip(
                                selected = selectedSeverity == severity,
                                onClick = { selectedSeverity = severity },
                                label = { Text(severity.levelName, fontSize = 10.sp) },
                                modifier = Modifier.testTag("admin_alert_severity_${severity.name.lowercase()}")
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Send Urgent Weather Push Notification to Devices",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.weight(1f)
                        )
                        Switch(
                            checked = sendUrgentWeatherPush,
                            onCheckedChange = { sendUrgentWeatherPush = it },
                            modifier = Modifier.testTag("admin_weather_send_push_switch")
                        )
                    }

                    Button(
                        onClick = {
                            if (alertTitle.isBlank() || alertSummary.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter alert title and message",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            val id = editingAlertId ?: "IMD-MOD-${System.currentTimeMillis()}"
                            val now = System.currentTimeMillis()

                            val newAlert = BalasoreWeatherAlert(
                                id = id,
                                title = alertTitle.trim(),
                                odiaTitle = alertTitle.trim(),
                                hindiTitle = alertTitle.trim(),
                                summary = alertSummary.trim(),
                                detailedDescription = alertSummary.trim(),
                                category = WeatherAlertCategory.CYCLONE,
                                severity = selectedSeverity,
                                affectedZones = listOf(selectedZone),
                                issuingAuthority = "IMD Bhubaneswar & Balasore DEOC Moderator",
                                validFromMillis = now,
                                validUntilMillis = now + 24 * 3600_000L,
                                issuedAtMillis = now,
                                actionableInstructions = listOf(
                                    alertSummary.trim(),
                                    "Call 112 or 06782-262244 for emergency assistance."
                                ),
                                emergencyContact = "06782-262244"
                            )
                            onSaveWeatherAlert(newAlert)
                            if (sendUrgentWeatherPush) {
                                scope.launch {
                                    FcmManager.dispatchCustomWeatherAlertPush(
                                        context = context,
                                        title = newAlert.title,
                                        message = newAlert.summary,
                                        alertLevel = newAlert.severity.name
                                    )
                                }
                            }
                            Toast.makeText(
                                context,
                                "Weather Alert Saved & Dispatched!",
                                Toast.LENGTH_SHORT
                            ).show()
                            resetAlertForm()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_save_weather_alert_btn"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (editingAlertId == null) {
                                "Publish Urgent Weather Alert"
                            } else {
                                "Save Weather Alert Changes"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Active Weather Bulletins (${weatherAlerts.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }

        items(weatherAlerts, key = { it.id }) { alert ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_weather_alert_item_${alert.id}"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = alert.severity.levelName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = alert.affectedZones.joinToString(", ") { it.zoneName },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Text(
                        text = alert.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = alert.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                scope.launch {
                                    FcmManager.dispatchCustomWeatherAlertPush(
                                        context = context,
                                        title = alert.title,
                                        message = alert.summary,
                                        alertLevel = alert.severity.name
                                    )
                                }
                                Toast.makeText(
                                    context,
                                    "Urgent Weather Push Sent!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            modifier = Modifier.testTag("admin_push_weather_alert_${alert.id}")
                        ) {
                            Icon(
                                Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Push Alert", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = {
                                editingAlertId = alert.id
                                alertTitle = alert.title
                                alertSummary = alert.summary
                                selectedZone = alert.affectedZones.firstOrNull() ?: BalasoreZone.COASTAL_CHANDIPUR
                                selectedSeverity = alert.severity
                            },
                            modifier = Modifier.testTag("admin_edit_weather_alert_${alert.id}")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = {
                                onDeleteWeatherAlert(alert.id)
                                Toast.makeText(
                                    context,
                                    "Removed weather alert",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.testTag("admin_delete_weather_alert_${alert.id}")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminTourismHotspotModerationTab(
    hotspots: List<Hotspot>,
    onSaveHotspot: (Hotspot) -> Unit,
    onDeleteHotspot: (String) -> Unit
) {
    val context = LocalContext.current

    val hotspotCategories = remember {
        listOf("Beach", "Temple", "Heritage", "Nature")
    }

    var editingHotspotId by remember { mutableStateOf<String?>(null) }
    var name by remember { mutableStateOf("") }
    var odiaName by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var timing by remember { mutableStateOf("6:00 AM - 6:00 PM") }
    var distanceKm by remember { mutableStateOf("15.0") }
    var rating by remember { mutableStateOf("4.8") }
    var selectedCategory by remember { mutableStateOf("Beach") }
    var isFeatured by remember { mutableStateOf(true) }

    fun resetHotspotForm() {
        editingHotspotId = null
        name = ""
        odiaName = ""
        location = ""
        description = ""
        timing = "6:00 AM - 6:00 PM"
        distanceKm = "15.0"
        rating = "4.8"
        selectedCategory = "Beach"
        isFeatured = true
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
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
                        Text(
                            text = if (editingHotspotId == null) {
                                "🏖️ Add New Tourism Hotspot"
                            } else {
                                "✏️ Edit Tourism Hotspot ($editingHotspotId)"
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (editingHotspotId != null) {
                            TextButton(
                                onClick = { resetHotspotForm() },
                                modifier = Modifier.testTag("admin_cancel_edit_hotspot_btn")
                            ) {
                                Text("Cancel Edit")
                            }
                        }
                    }

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Hotspot Name (English)") },
                        placeholder = { Text("e.g., Bichitrapur Mangrove Sanctuary") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_hotspot_name_input")
                    )

                    OutlinedTextField(
                        value = odiaName,
                        onValueChange = { odiaName = it },
                        label = { Text("Hotspot Name (Odia - Optional)") },
                        placeholder = { Text("ବିଚିତ୍ରପୁର ହେନ୍ତାଳ ବଣ") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_hotspot_odia_name_input")
                    )

                    OutlinedTextField(
                        value = location,
                        onValueChange = { location = it },
                        label = { Text("Location / Block") },
                        placeholder = { Text("e.g., Bhograi Block, Balasore") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_hotspot_location_input")
                    )

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description & Visitor Highlights") },
                        minLines = 2,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_hotspot_description_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = timing,
                            onValueChange = { timing = it },
                            label = { Text("Timings") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1.4f)
                                .testTag("admin_hotspot_timing_input")
                        )
                        OutlinedTextField(
                            value = distanceKm,
                            onValueChange = { distanceKm = it },
                            label = { Text("Dist (km)") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("admin_hotspot_distance_input")
                        )
                        OutlinedTextField(
                            value = rating,
                            onValueChange = { rating = it },
                            label = { Text("Rating") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(0.8f)
                                .testTag("admin_hotspot_rating_input")
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        hotspotCategories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory.equals(cat, ignoreCase = true),
                                onClick = { selectedCategory = cat },
                                label = { Text(cat, fontSize = 11.sp) },
                                modifier = Modifier.testTag("admin_hotspot_cat_${cat.lowercase()}")
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (name.isBlank() || location.isBlank()) {
                                Toast.makeText(
                                    context,
                                    "Please enter hotspot name and location",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@Button
                            }
                            val spotId = editingHotspotId ?: "spot_mod_${System.currentTimeMillis()}"
                            val savedSpot = Hotspot(
                                id = spotId,
                                name = name.trim(),
                                odiaName = odiaName.trim().ifBlank { name.trim() },
                                category = selectedCategory,
                                description = description.trim().ifBlank {
                                    "Scenic destination in ${location.trim()}, Balasore."
                                },
                                location = location.trim(),
                                rating = rating.toDoubleOrNull() ?: 4.7,
                                isFeatured = isFeatured,
                                timing = timing.trim().ifBlank { "6:00 AM - 6:00 PM" },
                                distanceKm = distanceKm.toDoubleOrNull() ?: 12.0
                            )
                            onSaveHotspot(savedSpot)
                            Toast.makeText(
                                context,
                                if (editingHotspotId == null) "Tourism Hotspot Added!" else "Tourism Hotspot Updated!",
                                Toast.LENGTH_SHORT
                            ).show()
                            resetHotspotForm()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("admin_save_hotspot_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            if (editingHotspotId == null) Icons.Default.AddLocationAlt else Icons.Default.Save,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (editingHotspotId == null) {
                                "Add Tourism Hotspot"
                            } else {
                                "Save Hotspot Changes"
                            },
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Text(
                text = "Active Tourism Hotspots (${hotspots.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold
            )
        }

        items(hotspots, key = { it.id }) { spot ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_hotspot_item_${spot.id}"),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.tertiaryContainer
                        ) {
                            Text(
                                text = spot.category,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                        Text(
                            text = "★ ${spot.rating} • ${spot.distanceKm} km from Balasore HQ",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Text(
                        text = "${spot.name} (${spot.odiaName})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${spot.location} • 🕒 ${spot.timing}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = spot.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = {
                                editingHotspotId = spot.id
                                name = spot.name
                                odiaName = spot.odiaName
                                location = spot.location
                                description = spot.description
                                timing = spot.timing
                                distanceKm = spot.distanceKm.toString()
                                rating = spot.rating.toString()
                                selectedCategory = spot.category
                                isFeatured = spot.isFeatured
                            },
                            modifier = Modifier.testTag("admin_edit_hotspot_${spot.id}")
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit", fontSize = 12.sp)
                        }
                        TextButton(
                            onClick = {
                                onDeleteHotspot(spot.id)
                                Toast.makeText(
                                    context,
                                    "Removed hotspot: ${spot.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.testTag("admin_delete_hotspot_${spot.id}")
                        ) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove", fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }
}
