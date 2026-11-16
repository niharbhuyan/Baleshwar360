package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.automirrored.filled.ManageSearch
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import com.example.data.local.CommunityNewsSubmissionEntity
import com.example.data.local.SavedBookmarkEntity
import com.example.data.repository.CommunityNewsModerationRepository
import com.example.ui.components.MySavedBookmarksSection
import com.example.ui.features.news.CitizenNewsDeskBannerCard
import com.example.ui.features.news.CitizenNewsPortalSheet
import com.example.ui.components.VoiceSearchControls
import com.example.ui.components.VoiceSearchLanguage
import com.example.ui.components.VoiceSearchSection
import com.example.ui.components.matchesBilingualSearchQuery
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Water
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.NightlightRound
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Switch
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import com.example.util.ShareHelper
import androidx.compose.material3.Surface
import com.example.ui.viewmodel.ThemeMode
import com.example.ui.theme.LocalBentoPalette
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppLanguage
import com.example.data.model.CycloneShelter
import com.example.data.model.NewsArticle
import com.example.data.admob.AdMobManager
import com.example.data.model.RiverGauge
import com.example.ui.components.AdMobBannerCard
import com.example.ui.components.NewsListSkeleton
import com.example.ui.components.NewsCardSkeleton
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate400
import com.example.ui.theme.BentoSlate500
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.ui.viewmodel.UniqueFeatureSheetType
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.GraphicEq
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.util.NewsTextToSpeechHelper

private fun formatNewsAutoUpdateCountdown(seconds: Int, language: AppLanguage): String {
    val hrs = seconds / 3600
    val mins = (seconds % 3600) / 60
    val secs = seconds % 60
    return when {
        hrs > 0 -> {
            if (language == AppLanguage.ODIA) {
                if (mins > 0) "$hrs ଘଣ୍ଟା $mins ମି" else "$hrs ଘଣ୍ଟା"
            } else {
                if (mins > 0) "${hrs}h ${mins}m" else "${hrs}h"
            }
        }
        mins > 0 -> {
            if (language == AppLanguage.ODIA) {
                if (secs > 0) "$mins ମି $secs ସେ" else "$mins ମିନିଟ୍"
            } else {
                if (secs > 0) "${mins}m ${secs}s" else "${mins}m"
            }
        }
        else -> {
            if (language == AppLanguage.ODIA) "$secs ସେକେଣ୍ଡ" else "${secs}s"
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsScreen(
    articles: List<NewsArticle>,
    riverGauges: List<RiverGauge>,
    cycloneShelters: List<CycloneShelter>,
    selectedCategory: String,
    language: AppLanguage,
    bookmarkedIds: Set<String>,
    isRefreshing: Boolean,
    onCategorySelected: (String) -> Unit,
    onToggleBookmark: (String) -> Unit,
    onRefresh: () -> Unit,
    onOpenFeatureSheet: (UniqueFeatureSheetType) -> Unit = {},
    searchQuery: String = "",
    onSearchQueryChanged: (String) -> Unit = {},
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    onToggleNightMode: () -> Unit = {},
    modifier: Modifier = Modifier,
    isAutoUpdateEnabled: Boolean = true,
    autoUpdateIntervalSeconds: Int = 3600,
    onToggleAutoUpdate: (Boolean) -> Unit = {},
    onChangeAutoUpdateInterval: (Int) -> Unit = {},
    selectedTab: Int = 0,
    onTabSelected: (Int) -> Unit = {},
    filteredArticles: List<NewsArticle>? = null,
    savedBookmarks: List<SavedBookmarkEntity> = emptyList(),
    onRemoveSavedBookmark: (String) -> Unit = {},
    onCallEmergency: (String) -> Unit = {},
    recentSearchQueries: List<String> = emptyList(),
    onSaveSearchQuery: (String) -> Unit = {},
    onDeleteSearchQuery: (String) -> Unit = {},
    onClearSearchQueries: () -> Unit = {},
    communityNewsSubmissions: List<CommunityNewsSubmissionEntity>? = null,
    onSubmitCommunityNewsStory: ((
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
    ) -> Unit)? = null,
    onApproveCommunityNewsSubmission: ((
        submissionId: String,
        moderatorNote: String,
        editedTitle: String?,
        editedOdiaTitle: String?,
        editedSnippet: String?,
        editedContent: String?,
        editedCategory: String?
    ) -> Unit)? = null,
    onRejectCommunityNewsSubmission: ((submissionId: String, moderatorNote: String) -> Unit)? = null,
    onRequestRevisionCommunityNewsSubmission: ((submissionId: String, moderatorNote: String) -> Unit)? = null,
    onResubmitCommunityNewsStory: ((
        submissionId: String,
        title: String,
        odiaTitle: String,
        snippet: String,
        content: String,
        category: String,
        locationWard: String
    ) -> Unit)? = null,
    onApproveAllPendingCommunityNews: (() -> Unit)? = null,
    onDeleteCommunityNewsSubmission: ((String) -> Unit)? = null
) {
    val context = LocalContext.current
    var autoUpdateSecondsLeft by remember { mutableStateOf(autoUpdateIntervalSeconds) }
    var selectedArticleForDetail by remember { mutableStateOf<NewsArticle?>(null) }
    var activeCitizenNewsPortalTab by remember { mutableStateOf<Int?>(null) }
    val repoSubmissions by CommunityNewsModerationRepository.submissionsStateFlow.collectAsStateWithLifecycle()
    val effectiveSubmissions = communityNewsSubmissions ?: repoSubmissions
    val syndicatedArticles by com.example.data.sync.HourlyMultiSourceNewsSyndicator.pulledArticlesFlow.collectAsStateWithLifecycle()
    val multiSourcePullSummary by com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullSummaryFlow.collectAsStateWithLifecycle()
    var firestoreTrendingArticles by remember { mutableStateOf<List<NewsArticle>>(emptyList()) }
    val lastTrendingSyncTs by com.example.data.firebase.FirestoreService.lastTrendingSyncTimestampFlow.collectAsStateWithLifecycle()
    val mergedArticles = remember(articles, syndicatedArticles, firestoreTrendingArticles, bookmarkedIds) {
        (firestoreTrendingArticles.map { art ->
            art.copy(isBookmarked = art.isBookmarked || bookmarkedIds.contains(art.id))
        } + syndicatedArticles.map { art ->
            art.copy(isBookmarked = art.isBookmarked || bookmarkedIds.contains(art.id))
        } + articles).distinctBy { "${it.id}_${it.title.trim().lowercase()}" }
    }
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()

    if (activeCitizenNewsPortalTab != null) {
        CitizenNewsPortalSheet(
            initialTab = activeCitizenNewsPortalTab ?: 0,
            submissions = effectiveSubmissions,
            language = language,
            onSubmitStory = { title, odiaTitle, snippet, content, category, ward, author, contact, sourceType, photoUri, isBreaking ->
                if (onSubmitCommunityNewsStory != null) {
                    onSubmitCommunityNewsStory(
                        title, odiaTitle, snippet, content, category, ward, author, contact, sourceType, photoUri, isBreaking
                    )
                } else {
                    coroutineScope.launch {
                        CommunityNewsModerationRepository.getInstance(context).submitNewsStory(
                            title = title,
                            odiaTitle = odiaTitle,
                            snippet = snippet,
                            content = content,
                            category = category,
                            locationWard = ward,
                            authorName = author,
                            authorContact = contact,
                            sourceVerificationType = sourceType,
                            photoUri = photoUri,
                            isBreakingCandidate = isBreaking
                        )
                    }
                }
            },
            onApproveSubmission = { subId, note, eTitle, eOdia, eSnippet, eContent, eCat ->
                if (onApproveCommunityNewsSubmission != null) {
                    onApproveCommunityNewsSubmission(subId, note, eTitle, eOdia, eSnippet, eContent, eCat)
                } else {
                    coroutineScope.launch {
                        CommunityNewsModerationRepository.getInstance(context).approveAndPublishSubmission(
                            submissionId = subId,
                            moderatorNote = note,
                            editedTitle = eTitle,
                            editedOdiaTitle = eOdia,
                            editedSnippet = eSnippet,
                            editedContent = eContent,
                            editedCategory = eCat
                        )
                    }
                }
            },
            onRejectSubmission = { subId, note ->
                if (onRejectCommunityNewsSubmission != null) {
                    onRejectCommunityNewsSubmission(subId, note)
                } else {
                    coroutineScope.launch {
                        CommunityNewsModerationRepository.getInstance(context).rejectSubmission(subId, note)
                    }
                }
            },
            onRequestRevision = { subId, note ->
                if (onRequestRevisionCommunityNewsSubmission != null) {
                    onRequestRevisionCommunityNewsSubmission(subId, note)
                } else {
                    coroutineScope.launch {
                        CommunityNewsModerationRepository.getInstance(context).requestRevision(subId, note)
                    }
                }
            },
            onUpdateAndResubmit = { subId, title, odiaTitle, snippet, content, category, ward ->
                if (onResubmitCommunityNewsStory != null) {
                    onResubmitCommunityNewsStory(subId, title, odiaTitle, snippet, content, category, ward)
                } else {
                    coroutineScope.launch {
                        CommunityNewsModerationRepository.getInstance(context)
                            .updateAndResubmitStory(subId, title, odiaTitle, snippet, content, category, ward)
                    }
                }
            },
            onApproveAllPending = {
                if (onApproveAllPendingCommunityNews != null) {
                    onApproveAllPendingCommunityNews()
                } else {
                    coroutineScope.launch {
                        CommunityNewsModerationRepository.getInstance(context).approveAllPending()
                    }
                }
            },
            onDeleteSubmission = { subId ->
                if (onDeleteCommunityNewsSubmission != null) {
                    onDeleteCommunityNewsSubmission(subId)
                } else {
                    coroutineScope.launch {
                        CommunityNewsModerationRepository.getInstance(context).deleteSubmission(subId)
                    }
                }
            },
            onClose = { activeCitizenNewsPortalTab = null }
        )
        return
    }

    // If an article is selected, display the dedicated NewsDetailScreen with full regional TTS player
    if (selectedArticleForDetail != null) {
        val currentArticle = selectedArticleForDetail!!
        LaunchedEffect(currentArticle.id) {
            com.example.data.recommendation.BalasoreRecommendationEngine.recordArticleViewed(
                context,
                currentArticle
            )
        }
        NewsDetailScreen(
            article = currentArticle,
            onNavigateBack = { selectedArticleForDetail = null },
            onToggleBookmark = {
                val nowSaved = !bookmarkedIds.contains(currentArticle.id)
                com.example.data.recommendation.BalasoreRecommendationEngine.recordArticleSaved(
                    context = context,
                    articleId = currentArticle.id,
                    category = currentArticle.category,
                    title = currentArticle.title,
                    isSaved = nowSaved
                )
                onToggleBookmark(currentArticle.id)
                selectedArticleForDetail = currentArticle.copy(
                    isBookmarked = nowSaved
                )
            },
            onToggleNightMode = onToggleNightMode
        )
        return
    }

    val isBatterySaverActive = remember(autoUpdateIntervalSeconds) {
        com.example.data.sync.SyncManager.isBatterySavingModeEnabled(context)
    }
    val effectiveIntervalSeconds = if (isBatterySaverActive) {
        maxOf(autoUpdateIntervalSeconds, 1800)
    } else {
        autoUpdateIntervalSeconds
    }

    // Auto-update countdown and automatic hourly background multi-source pull ticker
    LaunchedEffect(isAutoUpdateEnabled, effectiveIntervalSeconds) {
        try {
            com.example.data.sync.HourlyMultiSourceNewsSyndicator.scheduleHourlyAutoPull(context)
        } catch (_: Throwable) {}
        autoUpdateSecondsLeft = effectiveIntervalSeconds
        if (isAutoUpdateEnabled) {
            while (true) {
                delay(1000L)
                if (autoUpdateSecondsLeft > 1) {
                    autoUpdateSecondsLeft -= 1
                } else {
                    autoUpdateSecondsLeft = effectiveIntervalSeconds
                    try {
                        com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullAndAutoPublishAllSections(
                            context = context,
                            forceNetworkRss = true
                        )
                    } catch (_: Throwable) {}
                    onRefresh()
                }
            }
        }
    }

    val categories = listOf(
        "All",
        "Government Notices",
        "Local Events",
        "Community Highlights",
        "Trending",
        "Local Politics",
        "Health",
        "Tourism",
        "Coastal Defense",
        "Spiritual",
        "Politics",
        "Education",
        "Sports",
        "Defense",
        "Local",
        "Weather",
        "Emergency",
        "National",
        "International",
        "Saved Bookmarks"
    )

    val matchesCategoryForArticle: (NewsArticle, String) -> Boolean = { article, cat ->
        when (cat) {
            "All", "All Stories", "All News" -> true
            "Government Notices", "Government", "Official Notices", "Municipal Notices" ->
                article.category.equals("Government Notices", ignoreCase = true) ||
                article.category.equals("Politics", ignoreCase = true) ||
                article.category.equals("Local Politics", ignoreCase = true) ||
                article.category.equals("Infrastructure", ignoreCase = true) ||
                article.category.equals("Civic", ignoreCase = true) ||
                article.title.contains("Municipality", ignoreCase = true) ||
                article.title.contains("Collector", ignoreCase = true) ||
                article.title.contains("District", ignoreCase = true) ||
                article.title.contains("Notice", ignoreCase = true) ||
                article.title.contains("Advisory", ignoreCase = true) ||
                article.title.contains("Government", ignoreCase = true) ||
                article.title.contains("Parishad", ignoreCase = true) ||
                article.title.contains("Smart City", ignoreCase = true) ||
                article.title.contains("Station", ignoreCase = true) ||
                article.source.contains("DIPRO", ignoreCase = true) ||
                article.source.contains("Municipal", ignoreCase = true)
            "Community Highlights", "Community", "Citizen Stories" ->
                article.category.equals("Community Highlights", ignoreCase = true) ||
                article.category.equals("Culture", ignoreCase = true) ||
                article.category.equals("Education", ignoreCase = true) ||
                article.category.equals("Sports", ignoreCase = true) ||
                article.category.equals("Local News", ignoreCase = true) ||
                article.category.equals("Health", ignoreCase = true) ||
                article.title.contains("Community", ignoreCase = true) ||
                article.title.contains("Citizen", ignoreCase = true) ||
                article.title.contains("Artisan", ignoreCase = true) ||
                article.title.contains("University", ignoreCase = true) ||
                article.title.contains("Youth", ignoreCase = true) ||
                article.title.contains("Volunteer", ignoreCase = true) ||
                article.title.contains("Club", ignoreCase = true) ||
                article.title.contains("Heritage", ignoreCase = true) ||
                article.content.contains("citizens", ignoreCase = true) ||
                article.content.contains("community", ignoreCase = true)
            "Saved Bookmarks", "Bookmarks", "Saved", "My Saved" ->
                article.isBookmarked || bookmarkedIds.contains(article.id)
            "National", "National News" ->
                article.category.equals("National News", ignoreCase = true) ||
                article.category.equals("National", ignoreCase = true) ||
                article.title.contains("National", ignoreCase = true) ||
                article.title.contains("India", ignoreCase = true) ||
                article.title.contains("ISRO", ignoreCase = true) ||
                article.title.contains("Cabinet", ignoreCase = true) ||
                article.title.contains("Union", ignoreCase = true) ||
                article.source.contains("National", ignoreCase = true) ||
                article.source.contains("PTI", ignoreCase = true)
            "International", "International News", "World" ->
                article.category.equals("International News", ignoreCase = true) ||
                article.category.equals("International", ignoreCase = true) ||
                article.category.equals("World", ignoreCase = true) ||
                article.title.contains("Global", ignoreCase = true) ||
                article.title.contains("World", ignoreCase = true) ||
                article.title.contains("UN", ignoreCase = true) ||
                article.title.contains("International", ignoreCase = true) ||
                article.source.contains("World", ignoreCase = true) ||
                article.source.contains("Reuters", ignoreCase = true) ||
                article.source.contains("International", ignoreCase = true)
            "Local", "Local News" ->
                article.category.equals("Local News", ignoreCase = true) ||
                article.category.equals("Local", ignoreCase = true) ||
                article.category.equals("Infrastructure", ignoreCase = true) ||
                article.category.equals("Politics", ignoreCase = true) ||
                article.category.equals("Local Politics", ignoreCase = true) ||
                article.category.equals("Development", ignoreCase = true) ||
                article.category.equals("Sports", ignoreCase = true) ||
                article.category.contains("Civic", ignoreCase = true) ||
                article.category.contains("Municipal", ignoreCase = true) ||
                article.title.contains("municipality", ignoreCase = true) ||
                article.title.contains("station", ignoreCase = true) ||
                article.title.contains("parishad", ignoreCase = true) ||
                article.title.contains("drainage", ignoreCase = true) ||
                article.title.contains("smart city", ignoreCase = true)
            "Coastal Defense", "Defense", "Defense Alerts" ->
                article.category.equals("Coastal Defense", ignoreCase = true) ||
                article.category.equals("Defense Alerts", ignoreCase = true) ||
                article.category.equals("Defense", ignoreCase = true) ||
                article.category.equals("Coastal Alerts", ignoreCase = true) ||
                article.category.equals("Emergency", ignoreCase = true) ||
                article.title.contains("DRDO", ignoreCase = true) ||
                article.title.contains("Missile", ignoreCase = true) ||
                article.title.contains("ITR", ignoreCase = true) ||
                article.title.contains("Defense", ignoreCase = true) ||
                article.title.contains("Coastal", ignoreCase = true) ||
                article.title.contains("Chandipur", ignoreCase = true) ||
                article.title.contains("Alert", ignoreCase = true) ||
                article.title.contains("Storm", ignoreCase = true) ||
                article.title.contains("Flood", ignoreCase = true) ||
                article.title.contains("Siren", ignoreCase = true) ||
                article.title.contains("Warning", ignoreCase = true) ||
                article.content.contains("DRDO", ignoreCase = true) ||
                article.content.contains("missile", ignoreCase = true)
            "Local Events", "Events", "Festivals" ->
                article.category.equals("Local Events", ignoreCase = true) ||
                article.category.equals("Events", ignoreCase = true) ||
                article.category.equals("Culture", ignoreCase = true) ||
                article.category.equals("Local News", ignoreCase = true) ||
                article.title.contains("Festival", ignoreCase = true) ||
                article.title.contains("Mahotsav", ignoreCase = true) ||
                article.title.contains("Mela", ignoreCase = true) ||
                article.title.contains("Event", ignoreCase = true) ||
                article.title.contains("Exhibition", ignoreCase = true) ||
                article.title.contains("Tournament", ignoreCase = true) ||
                article.title.contains("Haat", ignoreCase = true) ||
                article.content.contains("festival", ignoreCase = true) ||
                article.content.contains("celebration", ignoreCase = true)
            "Spiritual", "Temples", "Heritage" ->
                article.category.equals("Spiritual", ignoreCase = true) ||
                article.category.equals("Culture", ignoreCase = true) ||
                article.category.equals("Heritage", ignoreCase = true) ||
                article.title.contains("Temple", ignoreCase = true) ||
                article.title.contains("Gopinath", ignoreCase = true) ||
                article.title.contains("Remuna", ignoreCase = true) ||
                article.title.contains("Panchalingeswar", ignoreCase = true) ||
                article.title.contains("Chandaneswar", ignoreCase = true) ||
                article.title.contains("Bhusandeswar", ignoreCase = true) ||
                article.title.contains("Jagannath", ignoreCase = true) ||
                article.title.contains("Darshan", ignoreCase = true) ||
                article.title.contains("Shrine", ignoreCase = true) ||
                article.content.contains("temple", ignoreCase = true) ||
                article.content.contains("devotees", ignoreCase = true)
            "Tourism", "Tourism Updates" ->
                article.category.equals("Tourism Updates", ignoreCase = true) ||
                article.category.equals("Tourism", ignoreCase = true) ||
                article.category.equals("Culture", ignoreCase = true) ||
                article.category.equals("Spiritual", ignoreCase = true) ||
                article.title.contains("Tourism", ignoreCase = true) ||
                article.title.contains("Eco-Promenade", ignoreCase = true) ||
                article.title.contains("Eco-Trek", ignoreCase = true) ||
                article.title.contains("Gazebos", ignoreCase = true) ||
                article.title.contains("Vanishing Sea", ignoreCase = true) ||
                article.title.contains("Mahotsav", ignoreCase = true) ||
                article.title.contains("Tourists", ignoreCase = true) ||
                article.title.contains("Eco-Tourism", ignoreCase = true) ||
                article.title.contains("Wildlife Sanctuary", ignoreCase = true) ||
                article.title.contains("Darshan Facilitated", ignoreCase = true) ||
                article.content.contains("tourist", ignoreCase = true) ||
                article.content.contains("visitors", ignoreCase = true)
            "Weather", "Weather Updates" ->
                article.category.equals("Weather Updates", ignoreCase = true) ||
                article.category.equals("Weather", ignoreCase = true) ||
                article.category.equals("Cyclone Alert", ignoreCase = true) ||
                article.category.equals("Meteorological", ignoreCase = true) ||
                article.title.contains("Weather", ignoreCase = true) ||
                article.title.contains("Cyclone", ignoreCase = true) ||
                article.title.contains("Tide", ignoreCase = true) ||
                article.title.contains("Rain", ignoreCase = true) ||
                article.title.contains("Heatwave", ignoreCase = true) ||
                article.title.contains("IMD", ignoreCase = true) ||
                article.title.contains("Storm", ignoreCase = true) ||
                article.snippet.contains("temperature", ignoreCase = true) ||
                article.snippet.contains("rainfall", ignoreCase = true) ||
                article.content.contains("weather", ignoreCase = true)
            "Politics", "Local Politics" ->
                article.category.equals("Politics", ignoreCase = true) ||
                article.category.equals("Local Politics", ignoreCase = true) ||
                article.title.contains("Parishad", ignoreCase = true) ||
                article.title.contains("Council", ignoreCase = true) ||
                article.title.contains("Municipal", ignoreCase = true) ||
                article.title.contains("Cabinet", ignoreCase = true) ||
                article.title.contains("Budget", ignoreCase = true) ||
                article.title.contains("Resolution", ignoreCase = true) ||
                article.content.contains("elected", ignoreCase = true) ||
                article.content.contains("council", ignoreCase = true)
            "Health", "Healthcare", "Medical" ->
                article.category.equals("Health", ignoreCase = true) ||
                article.category.equals("Healthcare", ignoreCase = true) ||
                article.category.equals("Medical", ignoreCase = true) ||
                article.title.contains("Health", ignoreCase = true) ||
                article.title.contains("Medical", ignoreCase = true) ||
                article.title.contains("Hospital", ignoreCase = true) ||
                article.title.contains("AIIMS", ignoreCase = true) ||
                article.title.contains("FM Medical", ignoreCase = true) ||
                article.title.contains("DHH", ignoreCase = true) ||
                article.title.contains("ICU", ignoreCase = true) ||
                article.title.contains("Telemedicine", ignoreCase = true) ||
                article.title.contains("Doctor", ignoreCase = true) ||
                article.content.contains("hospital", ignoreCase = true) ||
                article.content.contains("medical", ignoreCase = true) ||
                article.content.contains("patients", ignoreCase = true)
            "Sports", "Athletics" ->
                article.category.equals("Sports", ignoreCase = true) ||
                article.title.contains("Athletics", ignoreCase = true) ||
                article.title.contains("Football", ignoreCase = true) ||
                article.title.contains("Stadium", ignoreCase = true) ||
                article.title.contains("Championship", ignoreCase = true) ||
                article.title.contains("Cricket", ignoreCase = true) ||
                article.title.contains("Marathon", ignoreCase = true) ||
                article.content.contains("athletes", ignoreCase = true) ||
                article.content.contains("sprinters", ignoreCase = true)
            "Education", "Campus" ->
                article.category.equals("Education", ignoreCase = true) ||
                article.title.contains("University", ignoreCase = true) ||
                article.title.contains("FM University", ignoreCase = true) ||
                article.title.contains("College", ignoreCase = true) ||
                article.title.contains("School", ignoreCase = true) ||
                article.title.contains("Student", ignoreCase = true) ||
                article.title.contains("AIIMS", ignoreCase = true) ||
                article.content.contains("university", ignoreCase = true) ||
                article.content.contains("education", ignoreCase = true)
            "Emergency", "Emergency News" ->
                article.category.equals("Emergency", ignoreCase = true) ||
                article.category.equals("Emergency News", ignoreCase = true) ||
                article.category.equals("Disaster Alert", ignoreCase = true) ||
                article.category.equals("Coastal Alerts", ignoreCase = true) ||
                article.title.contains("Emergency", ignoreCase = true) ||
                article.title.contains("Alert", ignoreCase = true) ||
                article.title.contains("Warning", ignoreCase = true) ||
                article.title.contains("Flood", ignoreCase = true) ||
                article.title.contains("Siren", ignoreCase = true) ||
                article.title.contains("Evacuation", ignoreCase = true) ||
                article.title.contains("NDRF", ignoreCase = true) ||
                article.title.contains("ODRAF", ignoreCase = true) ||
                article.title.contains("Rescue", ignoreCase = true) ||
                article.content.contains("helpline", ignoreCase = true) ||
                article.content.contains("emergency", ignoreCase = true)
            "Trending", "Trending Stories" -> true
            else -> article.category.equals(cat, ignoreCase = true)
        }
    }

    val categoryCounts = remember(mergedArticles, bookmarkedIds) {
        categories.associateWith { cat ->
            mergedArticles.count { matchesCategoryForArticle(it, cat) }
        }
    }

    val currentNewsTab = when {
        selectedCategory.equals("All", ignoreCase = true) || selectedCategory.equals("All Stories", ignoreCase = true) || selectedCategory.isBlank() -> 0
        selectedCategory.equals("Local", ignoreCase = true) || selectedCategory.equals("Local News", ignoreCase = true) -> 1
        selectedCategory.equals("National", ignoreCase = true) || selectedCategory.equals("National News", ignoreCase = true) -> 2
        selectedCategory.equals("International", ignoreCase = true) || selectedCategory.equals("International News", ignoreCase = true) || selectedCategory.equals("World", ignoreCase = true) -> 3
        selectedCategory.equals("Defense", ignoreCase = true) || selectedCategory.equals("Defense Alerts", ignoreCase = true) -> 4
        selectedCategory.equals("Tourism", ignoreCase = true) || selectedCategory.equals("Tourism Updates", ignoreCase = true) -> 5
        selectedCategory.equals("Saved", ignoreCase = true) || selectedCategory.equals("Saved Bookmarks", ignoreCase = true) || selectedCategory.equals("My Saved", ignoreCase = true) -> 6
        selectedCategory.equals("Weather", ignoreCase = true) || selectedCategory.equals("Weather Updates", ignoreCase = true) -> 7
        selectedCategory.equals("Emergency", ignoreCase = true) || selectedCategory.equals("Emergency News", ignoreCase = true) -> 8
        selectedCategory.equals("Politics", ignoreCase = true) || selectedCategory.equals("Local Politics", ignoreCase = true) -> 9
        selectedCategory.equals("Sports", ignoreCase = true) -> 10
        selectedCategory.equals("Trending", ignoreCase = true) || selectedCategory.equals("Trending Stories", ignoreCase = true) -> 11
        selectedCategory.equals("Health", ignoreCase = true) || selectedCategory.equals("Healthcare", ignoreCase = true) || selectedCategory.equals("Medical", ignoreCase = true) -> 12
        else -> 0
    }

    var searchFieldScope by remember { mutableStateOf("ALL") } // "ALL" (Title + Snippet), "TITLE", "SNIPPET"
    val roomPreferredCategories by com.example.ui.components.RoomNewsPreferencesRepository.selectedCategoriesFlow.collectAsStateWithLifecycle()
    val enforceRoomNewsFilter by com.example.ui.components.RoomNewsPreferencesRepository.enforceRoomFilterFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        com.example.ui.components.RoomNewsPreferencesRepository.loadFromRoomDatabase(context)
    }

    val trendingFirestoreService = remember { com.example.data.firebase.FirestoreService(context) }
    val trendingViewCounts by com.example.data.firebase.FirestoreService.trendingViewCountsFlow.collectAsStateWithLifecycle()

    // Dynamic reactive filtering by category, Room News Preferences, and bilingual (English + Odia) search keyword across Title and Snippet
    val filteredArticles = remember(
        mergedArticles,
        selectedCategory,
        searchQuery,
        bookmarkedIds,
        searchFieldScope,
        roomPreferredCategories,
        enforceRoomNewsFilter,
        trendingViewCounts
    ) {
        val baseList = mergedArticles.filter { article ->
            val matchesCategory = matchesCategoryForArticle(article, selectedCategory)
            val matchesRoomPrefs = if (!enforceRoomNewsFilter || !selectedCategory.equals("All", ignoreCase = true)) {
                true
            } else {
                roomPreferredCategories.any { prefCat -> matchesCategoryForArticle(article, prefCat) }
            }
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                when (searchFieldScope) {
                    "TITLE" -> matchesBilingualSearchQuery(
                        searchQuery,
                        article.title,
                        article.odiaTitle
                    )
                    "SNIPPET" -> matchesBilingualSearchQuery(
                        searchQuery,
                        article.snippet,
                        article.odiaSnippet
                    )
                    else -> matchesBilingualSearchQuery(
                        searchQuery,
                        article.title,
                        article.odiaTitle,
                        article.snippet,
                        article.odiaSnippet,
                        article.content,
                        article.odiaContent,
                        article.category,
                        article.source
                    )
                }
            }
            matchesCategory && matchesRoomPrefs && matchesSearch
        }
        if (selectedCategory.equals("Trending", ignoreCase = true) || selectedCategory.equals("Trending Stories", ignoreCase = true)) {
            baseList.sortedByDescending {
                com.example.data.firebase.FirestoreService.getViewCountForId(it.id, trendingViewCounts)
            }
        } else {
            baseList
        }
    }

    var localPullRefreshing by remember { mutableStateOf(false) }
    var pullRefreshCycleCount by remember { mutableStateOf(0) }
    var lastServerFetchStatus by remember {
        mutableStateOf("Pull down to fetch latest trending articles from Firestore & live sources")
    }
    val effectiveIsRefreshing = isRefreshing || localPullRefreshing
    val pullToRefreshState = rememberPullToRefreshState()

    val triggerPullToRefresh: () -> Unit = {
        if (!localPullRefreshing) {
            localPullRefreshing = true
            pullRefreshCycleCount += 1
            lastServerFetchStatus = "Fetching latest news, community stories & Gemini Daily Brief from network..."
            onRefresh()
            coroutineScope.launch {
                try {
                    val cloudTrending = trendingFirestoreService.fetchLatestTrendingFromFirestore()
                    if (cloudTrending.isNotEmpty()) {
                        firestoreTrendingArticles = cloudTrending
                    }
                } catch (_: Throwable) {}
                try {
                    com.example.data.sync.HourlyMultiSourceNewsSyndicator.pullAndAutoPublishAllSections(
                        context = context,
                        forceNetworkRss = true
                    )
                } catch (_: Throwable) {}
                try {
                    CommunityNewsModerationRepository.syncWithRoom(context)
                } catch (_: Throwable) {}
                try {
                    com.example.ui.components.BalasoreCommunityForumRepository.syncPendingForumPostsWithCloud(context)
                } catch (_: Throwable) {}
                try {
                    com.example.data.notification.DailyNewsDigestManager.generateAndDispatchDigest(
                        context = context,
                        candidateArticles = articles,
                        postNotification = false
                    )
                } catch (_: Throwable) {}
                delay(700L)
                localPullRefreshing = false
                com.example.util.BalasoreHapticHelper.vibrateSuccess(context)
                val totalTracked = com.example.data.firebase.FirestoreService.trendingViewCountsFlow.value.values.sum()
                lastServerFetchStatus = "✓ Pulled latest news & community stories from network ($totalTracked views tracked) • Just now"
            }
        } else {
            onRefresh()
        }
    }

    PullToRefreshBox(
        isRefreshing = effectiveIsRefreshing,
        onRefresh = triggerPullToRefresh,
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = effectiveIsRefreshing,
                containerColor = MaterialTheme.colorScheme.surface,
                color = OceanBlue,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .testTag("news_pull_to_refresh_indicator")
            )
        },
        modifier = modifier
            .fillMaxSize()
            .testTag("news_pull_to_refresh_box")
    ) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("news_screen_list"),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        // Prominent Visual Server Loading Indicator when fetching new local stories
        if (effectiveIsRefreshing) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("news_server_fetch_loading_banner"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF0C243B) else Color(0xFFEFF6FF)
                    ),
                    border = BorderStroke(1.5.dp, OceanBlue),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .testTag("news_server_loading_spinner"),
                                    color = OceanBlue,
                                    strokeWidth = 2.8.dp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) {
                                            "ସର୍ଭରରୁ ନୂତନ ସ୍ଥାନୀୟ ଖବର ଆଣାଯାଉଛି..."
                                        } else {
                                            "Fetching new local stories from server..."
                                        },
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            color = OceanBlue
                                        )
                                    )
                                    Text(
                                        text = if (language == AppLanguage.ODIA) {
                                            "ବାଲେଶ୍ୱର ୩୬୦ ଲାଇଭ୍ ବୁଲେଟିନ୍ ଓ ରୁମ୍ କ୍ୟାশ୍ ଅପଡେଟ୍ ହେଉଛି"
                                        } else {
                                            "Synchronizing Baleshwar 360 server headlines & offline Room cache"
                                        },
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OceanBlue
                            ) {
                                Text(
                                    text = "LIVE SYNC",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .testTag("news_server_loading_progress_bar"),
                            color = OceanBlue,
                            trackColor = OceanBlue.copy(alpha = 0.2f)
                        )
                    }
                }
            }
        }

        // Section Header
        item {
            val db = remember { com.example.data.local.AppDatabase.getInstance(context) }
            val newsDao = remember { db.newsDao() }
            LaunchedEffect(articles, bookmarkedIds) {
                if (articles.isNotEmpty()) {
                    val entities = articles.mapIndexed { idx, art ->
                        com.example.data.local.NewsArticleEntity(
                            id = art.id.toLongOrNull() ?: (idx + 1).toLong(),
                            title = art.title,
                            summary = art.snippet,
                            content = art.content.ifBlank { art.snippet },
                            category = art.category,
                            source = art.source,
                            publishedAt = art.timeAgo,
                            timestamp = System.currentTimeMillis() - idx * 60000L,
                            isBreaking = idx == 0,
                            isBookmarked = art.isBookmarked || bookmarkedIds.contains(art.id)
                        )
                    }
                    newsDao.insertArticles(entities)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ସମାଚାର" else "Baleshwar Live News",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = if (language == AppLanguage.ODIA) {
                            "ସଦ୍ୟତମ ସ୍ଥାନୀୟ ଖବର • ଅଫଲାଇନ୍ ମୋଡ୍ (${articles.size} ଖବର ସେଭ୍)"
                        } else {
                            "Hyper-local updates • ${articles.size} articles cached for Offline Mode"
                        },
                        style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFECFDF5),
                        border = BorderStroke(1.dp, Color(0xFF6EE7B7)),
                        modifier = Modifier
                            .clickable { onOpenFeatureSheet(UniqueFeatureSheetType.OFFLINE_MODE_MANAGER) }
                            .testTag("news_offline_cache_badge")
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "⚡ ଅଫଲାଇନ୍" else "⚡ Offline Ready",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46),
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }

                    IconButton(
                        onClick = triggerPullToRefresh,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .testTag("refresh_news_button")
                    ) {
                        if (effectiveIsRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = OceanBlue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh News Feed",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // Interactive Pull-to-Refresh Gesture Hint & Server Fetch Status Pill
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (effectiveIsRefreshing) {
                    Color(0xFFE0F2FE)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                },
                border = BorderStroke(
                    1.dp,
                    if (effectiveIsRefreshing) OceanBlue else BentoSlate200
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable { triggerPullToRefresh() }
                    .testTag("news_pull_to_refresh_status_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (effectiveIsRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(14.dp),
                                color = OceanBlue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = OceanBlue,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (effectiveIsRefreshing) {
                                if (language == AppLanguage.ODIA) {
                                    "ସର୍ଭରରୁ ସଦ୍ୟତମ ଖବର ଲୋଡ୍ ହେଉଛି..."
                                } else {
                                    "Fetching new local stories from server..."
                                }
                            } else {
                                if (language == AppLanguage.ODIA) {
                                    "ନୂତନ ଖବର ପାଇଁ ତଳକୁ ଟାଣନ୍ତୁ (Pull-to-Refresh)"
                                } else {
                                    lastServerFetchStatus
                                }
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (effectiveIsRefreshing) OceanBlue else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            maxLines = 1
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = OceanBlue.copy(alpha = 0.14f),
                        modifier = Modifier.testTag("news_pull_refresh_action_pill")
                    ) {
                        Text(
                            text = if (effectiveIsRefreshing) {
                                "Fetching..."
                            } else if (pullRefreshCycleCount > 0) {
                                "Synced ($pullRefreshCycleCount)"
                            } else {
                                "Pull to Refresh"
                            },
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            }
        }

        // Citizen Local News Submission & Moderation Queue Card
        item {
            CitizenNewsDeskBannerCard(
                submissions = effectiveSubmissions,
                language = language,
                onOpenSubmitStory = { activeCitizenNewsPortalTab = 0 },
                onOpenMySubmissions = { activeCitizenNewsPortalTab = 1 },
                onOpenModerationQueue = { activeCitizenNewsPortalTab = 2 }
            )
        }

        // Hourly Multi-Source Auto-Syndication Card (Local, National & International without approval)
        item {
            val localSectionCount = remember(mergedArticles) {
                mergedArticles.count { matchesCategoryForArticle(it, "Local") }
            }
            val nationalSectionCount = remember(mergedArticles) {
                mergedArticles.count { matchesCategoryForArticle(it, "National") }
            }
            val internationalSectionCount = remember(mergedArticles) {
                mergedArticles.count { matchesCategoryForArticle(it, "International") }
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("hourly_multi_source_news_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF0B192C) else Color(0xFFF0F9FF)
                ),
                border = BorderStroke(1.2.dp, OceanBlue.copy(alpha = 0.55f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "⚡ ଘଣ୍ଟାକିଆ ବହୁ-ଉତ୍ସ ସମ୍ବାଦ ସିଙ୍କ୍ (Local • National • International)"
                                } else {
                                    "⚡ Hourly Multi-Source News Syndication"
                                },
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = OceanBlue
                                )
                            )
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "ଅନ୍ୟ ଉତ୍ସରୁ ପ୍ରତି ଘଣ୍ଟାରେ ବିନା ଅନୁମୋଦନରେ ସିଧାସଳଖ ପ୍ରକାଶିତ"
                                } else {
                                    "Auto-pulls external sources every hour & updates sections directly without approval"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmeraldGreen.copy(alpha = 0.16f),
                            border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ସିଧାସଳଖ • NO APPROVAL" else "DIRECT • NO APPROVAL",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = EmeraldGreen,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Quick Section Filter Pills (Local, National, International)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val sectionTriples = listOf(
                            Triple("Local", if (language == AppLanguage.ODIA) "📍 ସ୍ଥାନୀୟ ($localSectionCount)" else "📍 Local ($localSectionCount)", "hourly_section_chip_local"),
                            Triple("National", if (language == AppLanguage.ODIA) "🇮🇳 ଜାତୀୟ ($nationalSectionCount)" else "🇮🇳 National ($nationalSectionCount)", "hourly_section_chip_national"),
                            Triple("International", if (language == AppLanguage.ODIA) "🌍 ଆନ୍ତର୍ଜାତୀୟ ($internationalSectionCount)" else "🌍 World ($internationalSectionCount)", "hourly_section_chip_international")
                        )
                        sectionTriples.forEach { (catKey, chipLabel, tag) ->
                            val isSelected = selectedCategory.equals(catKey, ignoreCase = true) ||
                                selectedCategory.equals("$catKey News", ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = if (isSelected) OceanBlue else MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, if (isSelected) OceanBlue else OceanBlue.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        val vmTab = com.example.ui.viewmodel.NewsFeedTab.fromCategory(catKey)
                                        onTabSelected(vmTab.index)
                                        onCategorySelected(catKey)
                                    }
                                    .testTag(tag)
                            ) {
                                Text(
                                    text = chipLabel,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else OceanBlue,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 7.dp)
                                )
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
                                text = if (language == AppLanguage.ODIA) {
                                    "ଉତ୍ସ: ସମ୍ବାଦ, OTV, ପ୍ରଗତିବାଦୀ • PTI, DD News, PIB • Reuters, AP, BBC, UN"
                                } else {
                                    "Sources: Sambad, OTV, Pragativadi • PTI, DD News, PIB • Reuters, AP, BBC, UN"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "ଶେଷ ଘଣ୍ଟାକିଆ ସିଙ୍କ୍: ${multiSourcePullSummary.lastPulledTimeFormatted} • ଚକ୍ର #${multiSourcePullSummary.cycleNumber}"
                                } else {
                                    "Last hourly pull: ${multiSourcePullSummary.lastPulledTimeFormatted} • Cycle #${multiSourcePullSummary.cycleNumber}"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = OceanBlue
                                )
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OceanBlue,
                            modifier = Modifier
                                .clickable { triggerPullToRefresh() }
                                .testTag("pull_multi_source_news_now_btn")
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "🔄 ଏବେ ଆଣନ୍ତୁ" else "🔄 Pull Sources Now",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
                            )
                        }
                    }
                }
            }
        }

        // Material-3 TabRow Component: 'Local News', 'Defense Alerts', and 'Tourism Updates'
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("news_top_level_tabs_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF0F172A) else Color.White
                ),
                border = BorderStroke(1.dp, if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF334155) else BentoSlate200),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "ସମ୍ବାଦ ବର୍ଗ (CATEGORY)" else "NEWS CATEGORIES",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.8.sp,
                                fontSize = 10.sp,
                                color = OceanBlue
                            )
                        )
                        Text(
                            text = when (currentNewsTab) {
                                0 -> if (language == AppLanguage.ODIA) "ସମସ୍ତ ସଦ୍ୟତମ ଖବର" else "All District & National Feeds"
                                1 -> if (language == AppLanguage.ODIA) "ବାଲେଶ୍ୱର ସ୍ଥାନୀୟ ପ୍ରଗତି" else "Baleshwar Local & Civic"
                                2 -> if (language == AppLanguage.ODIA) "ଭାରତ ଓ ଜାତୀୟ ଖବର" else "National News & ISRO"
                                3 -> if (language == AppLanguage.ODIA) "ବିଶ୍ୱ ଓ ଆନ୍ତର୍ଜାତୀୟ ଖବର" else "Global & International"
                                4 -> if (language == AppLanguage.ODIA) "DRDO ଓ ଉପକୂଳ ସତର୍କତା" else "Defense & Alerts"
                                5 -> if (language == AppLanguage.ODIA) "ସଂସ୍କୃତି ଓ ପର୍ଯ୍ୟଟନ" else "Tourism & Heritage"
                                6 -> if (language == AppLanguage.ODIA) "ସଂରକ୍ଷିତ ଖବର" else "Saved Stories"
                                else -> if (language == AppLanguage.ODIA) "ସମସ୍ତ ଖବର" else "All Stories"
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontSize = 10.5.sp,
                                color = BentoSlate500
                            )
                        )
                    }

                        val newsTabPrefs = remember {
                            context.getSharedPreferences("balasore_news_tab_cache", android.content.Context.MODE_PRIVATE)
                        }
                        val firestoreService = remember { com.example.data.firebase.FirestoreService(context) }

                        LaunchedEffect(Unit) {
                            val cachedTab = newsTabPrefs.getString("last_selected_news_tab", null)
                            if (!cachedTab.isNullOrBlank() && (selectedCategory.isBlank() || selectedCategory.equals("All", ignoreCase = true))) {
                                onCategorySelected(cachedTab)
                            }
                            firestoreService.fetchTrendingViewCounts()
                        }

                        val m3Tabs = listOf(
                            Triple("All", if (language == AppLanguage.ODIA) "ସମସ୍ତ" else "All Stories", Icons.Default.Public),
                            Triple("Trending", if (language == AppLanguage.ODIA) "🔥 ଟ୍ରେଣ୍ଡିଂ" else "🔥 Trending", Icons.AutoMirrored.Filled.TrendingUp),
                            Triple("Local", if (language == AppLanguage.ODIA) "ସ୍ଥାନୀୟ" else "Local News", Icons.Default.LocationCity),
                            Triple("National", if (language == AppLanguage.ODIA) "ଜାତୀୟ" else "National", Icons.Default.Flag),
                            Triple("International", if (language == AppLanguage.ODIA) "ଆନ୍ତର୍ଜାତୀୟ" else "International", Icons.Default.Public),
                            Triple("Politics", if (language == AppLanguage.ODIA) "ରାଜନୀତି" else "Politics", Icons.Default.AccountBalance),
                            Triple("Sports", if (language == AppLanguage.ODIA) "କ୍ରୀଡ଼ା" else "Sports", Icons.Default.SportsSoccer),
                            Triple("Tourism", if (language == AppLanguage.ODIA) "ପର୍ଯ୍ୟଟନ" else "Tourism Updates", Icons.Default.Explore),
                            Triple("Defense", if (language == AppLanguage.ODIA) "ପ୍ରତିରକ୍ଷା" else "Defense Alerts", Icons.Default.Shield),
                            Triple("Saved Bookmarks", if (language == AppLanguage.ODIA) "ସାଇତା" else "Saved Stories", Icons.Default.Bookmark)
                        )
                        val selectedM3TabIdx = m3Tabs.indexOfFirst { (catId, _, _) ->
                            when (catId) {
                                "All" -> selectedCategory.equals("All", ignoreCase = true) || selectedCategory.equals("All Stories", ignoreCase = true) || selectedCategory.isBlank()
                                "Trending" -> selectedCategory.equals("Trending", ignoreCase = true) || selectedCategory.equals("Trending Stories", ignoreCase = true)
                                "Politics" -> selectedCategory.equals("Politics", ignoreCase = true) || selectedCategory.equals("Local Politics", ignoreCase = true)
                                "Sports" -> selectedCategory.equals("Sports", ignoreCase = true) || selectedCategory.equals("Athletics", ignoreCase = true)
                                "Tourism" -> selectedCategory.equals("Tourism", ignoreCase = true) || selectedCategory.equals("Tourism Updates", ignoreCase = true)
                                "Defense" -> selectedCategory.equals("Defense", ignoreCase = true) || selectedCategory.equals("Defense Alerts", ignoreCase = true)
                                "Local" -> selectedCategory.equals("Local", ignoreCase = true) || selectedCategory.equals("Local News", ignoreCase = true)
                                "National" -> selectedCategory.equals("National", ignoreCase = true) || selectedCategory.equals("National News", ignoreCase = true)
                                "International" -> selectedCategory.equals("International", ignoreCase = true) || selectedCategory.equals("International News", ignoreCase = true) || selectedCategory.equals("World", ignoreCase = true)
                                "Saved Bookmarks" -> selectedCategory.equals("Saved Bookmarks", ignoreCase = true) || selectedCategory.equals("Saved", ignoreCase = true) || selectedCategory.equals("My Saved", ignoreCase = true)
                                else -> false
                            }
                        }.coerceAtLeast(0)

                        ScrollableTabRow(
                            selectedTabIndex = selectedM3TabIdx,
                            containerColor = Color.Transparent,
                            contentColor = OceanBlue,
                            edgePadding = 4.dp,
                            indicator = { tabPositions ->
                                if (selectedM3TabIdx in tabPositions.indices) {
                                    TabRowDefaults.SecondaryIndicator(
                                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedM3TabIdx]),
                                        color = OceanBlue,
                                        height = 3.dp
                                    )
                                }
                            },
                            divider = {
                                HorizontalDivider(
                                    color = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF334155) else BentoSlate200,
                                    thickness = 1.dp
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("news_material3_tab_row")
                        ) {
                            m3Tabs.forEachIndexed { index, (catId, label, icon) ->
                                val isSelected = selectedM3TabIdx == index
                                val count = when (catId) {
                                    "All", "Trending" -> mergedArticles.size
                                    "Saved Bookmarks" -> maxOf(savedBookmarks.size, mergedArticles.count { matchesCategoryForArticle(it, catId) })
                                    else -> mergedArticles.count { matchesCategoryForArticle(it, catId) }
                                }
                                val testTagKey = when (catId) {
                                    "All" -> "news_tab_all"
                                    "Trending" -> "news_tab_trending"
                                    "Politics" -> "news_tab_politics"
                                    "Sports" -> "news_tab_sports"
                                    "Tourism" -> "news_tab_tourism"
                                    "Defense" -> "news_tab_defense"
                                    "Local" -> "news_tab_local"
                                    "National" -> "news_tab_national"
                                    "International" -> "news_tab_international"
                                    "Saved Bookmarks" -> "news_tab_saved"
                                    else -> "news_tab_extra"
                                }

                                Tab(
                                    selected = isSelected,
                                    onClick = {
                                        newsTabPrefs.edit().putString("last_selected_news_tab", catId).apply()
                                        val vmTab = com.example.ui.viewmodel.NewsFeedTab.fromCategory(catId)
                                        onTabSelected(vmTab.index)
                                        onCategorySelected(catId)
                                    },
                                    text = {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 11.5.sp,
                                                color = if (isSelected) OceanBlue else (if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF94A3B8) else BentoSlate600)
                                            ),
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    },
                                    icon = {
                                        BadgedBox(
                                            badge = {
                                                Badge(
                                                    containerColor = if (isSelected) OceanBlue else (if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF334155) else BentoSlate200),
                                                    contentColor = if (isSelected) Color.White else BentoSlate700
                                                ) {
                                                    Text(
                                                        text = "$count",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = label,
                                                tint = if (isSelected) OceanBlue else (if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF94A3B8) else BentoSlate600),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    },
                                    modifier = Modifier.testTag(testTagKey)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Breaking News FCM Push Notification Category Toggles ('Local', 'Coastal Alerts', 'Severe Weather')
                        var fcmLocalEnabled by remember { mutableStateOf(com.example.data.fcm.FcmManager.isBreakingNewsEnabled(context)) }
                        var fcmCoastalEnabled by remember { mutableStateOf(com.example.data.fcm.FcmManager.isDefenseAdvisoriesEnabled(context)) }
                        var fcmWeatherEnabled by remember { mutableStateOf(com.example.data.fcm.FcmManager.isWeatherAlertsEnabled(context)) }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF1E293B) else Color(0xFFF8FAFC))
                                .padding(horizontal = 10.dp, vertical = 8.dp)
                                .testTag("breaking_news_fcm_toggles_card"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "🔔 FCM ପୁଶ୍ ଆଲର୍ଟ:" else "🔔 FCM Push Alerts:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = OceanBlue
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                FilterChip(
                                    selected = fcmLocalEnabled,
                                    onClick = {
                                        fcmLocalEnabled = !fcmLocalEnabled
                                        com.example.data.fcm.FcmManager.setBreakingNewsEnabled(context, fcmLocalEnabled)
                                        if (fcmLocalEnabled) {
                                            com.example.data.fcm.FcmManager.triggerSampleBreakingNews(context)
                                        }
                                    },
                                    label = { Text(if (language == AppLanguage.ODIA) "ସ୍ଥାନୀୟ" else "Local", fontSize = 10.sp) },
                                    modifier = Modifier.testTag("fcm_toggle_local_chip")
                                )
                                FilterChip(
                                    selected = fcmCoastalEnabled,
                                    onClick = {
                                        fcmCoastalEnabled = !fcmCoastalEnabled
                                        com.example.data.fcm.FcmManager.setDefenseAdvisoriesEnabled(context, fcmCoastalEnabled)
                                        if (fcmCoastalEnabled) {
                                            com.example.data.fcm.FcmManager.triggerSampleWeatherAlert(context)
                                        }
                                    },
                                    label = { Text(if (language == AppLanguage.ODIA) "ଉପକୂଳ ସତର୍କତା" else "Coastal Alerts", fontSize = 10.sp) },
                                    modifier = Modifier.testTag("fcm_toggle_coastal_alerts_chip")
                                )
                                FilterChip(
                                    selected = fcmWeatherEnabled,
                                    onClick = {
                                        fcmWeatherEnabled = !fcmWeatherEnabled
                                        com.example.data.fcm.FcmManager.setWeatherAlertsEnabled(context, fcmWeatherEnabled)
                                        if (fcmWeatherEnabled) {
                                            com.example.data.fcm.FcmManager.triggerSampleWeatherAlert(context)
                                        }
                                    },
                                    label = { Text(if (language == AppLanguage.ODIA) "ପାଣିପାଗ" else "Weather", fontSize = 10.sp) },
                                    modifier = Modifier.testTag("fcm_toggle_weather_chip")
                                )
                            }
                        }
                }
            }
        }

        // Live Auto-Update Engine Telemetry & Feeds Auto-Refresh Control Bar
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("news_auto_update_control_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAutoUpdateEnabled) {
                        if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF0C243B) else Color(0xFFEFF6FF)
                    } else {
                        if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF1E293B) else BentoSlate100
                    }
                ),
                border = BorderStroke(
                    1.dp,
                    if (isAutoUpdateEnabled) OceanBlue.copy(alpha = 0.45f) else BentoSlate200
                )
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
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
                                    .background(if (isAutoUpdateEnabled) EmeraldGreen else BentoSlate400)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isAutoUpdateEnabled) {
                                    if (language == AppLanguage.ODIA) "ସ୍ୱୟଂକ୍ରିୟ ଅପଡେଟ୍: ସକ୍ରିୟ" else "LIVE AUTO-UPDATES: ACTIVE"
                                } else {
                                    if (language == AppLanguage.ODIA) "ସ୍ୱୟଂକ୍ରିୟ ଅପଡେଟ୍: ସ୍ଥଗିତ" else "AUTO-UPDATES: PAUSED"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp,
                                    color = if (isAutoUpdateEnabled) OceanBlue else BentoSlate600
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଅଟୋ ସିଙ୍କ୍" else "Auto-Sync",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Switch(
                                checked = isAutoUpdateEnabled,
                                onCheckedChange = { onToggleAutoUpdate(it) },
                                modifier = Modifier.testTag("news_auto_update_switch")
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val formattedTime = formatNewsAutoUpdateCountdown(autoUpdateSecondsLeft, language)
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isAutoUpdateEnabled) {
                                    if (autoUpdateIntervalSeconds >= 3600) {
                                        if (language == AppLanguage.ODIA) {
                                            "ଘଣ୍ଟାକିଆ ସ୍ୱୟଂକ୍ରିୟ ଅପଡେଟ୍ ସକ୍ରିୟ • ପରବର୍ତ୍ତୀ ଅପଡେଟ୍ $formattedTime ରେ"
                                        } else {
                                            "Hourly Auto-Update active • Next refresh in $formattedTime"
                                        }
                                    } else {
                                        if (language == AppLanguage.ODIA) {
                                            "ପରବର୍ତ୍ତୀ ସ୍ୱୟଂକ୍ରିୟ ଖବର ଅପଡେଟ୍ $formattedTime ରେ • ସମସ୍ତ ଫିଡ୍ ଲାଇଭ୍"
                                        } else {
                                            "Next feed auto-update in $formattedTime • Feeds synchronized live"
                                        }
                                    }
                                } else {
                                    if (language == AppLanguage.ODIA) {
                                        "ସ୍ୱୟଂକ୍ରିୟ ଖବର ଅପଡେଟ୍ ସ୍ଥଗିତ • ହାତରେ ସିଙ୍କ୍ କରିବାକୁ ରିଫ୍ରେସ୍ ଦବାନ୍ତୁ"
                                    } else {
                                        "Auto-update paused • Tap refresh for manual update"
                                    }
                                },
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    fontWeight = if (isAutoUpdateEnabled && autoUpdateIntervalSeconds >= 3600) FontWeight.SemiBold else FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                            if (isAutoUpdateEnabled && autoUpdateIntervalSeconds >= 3600) {
                                Text(
                                    text = if (language == AppLanguage.ODIA)
                                        "ଜାତୀୟ, ଆନ୍ତର୍ଜାତୀୟ ଓ ସ୍ଥାନୀୟ ସମସ୍ତ ଖବର ପ୍ରତି ଘଣ୍ଟାରେ ଅପଡେଟ୍ ହେବ"
                                    else
                                        "National, International & Local feeds auto-refresh every hour",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 10.5.sp,
                                        color = OceanBlue
                                    )
                                )
                            }
                        }

                        IconButton(
                            onClick = onRefresh,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(OceanBlue.copy(alpha = 0.12f))
                                .testTag("news_quick_auto_sync_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync now",
                                tint = OceanBlue,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Dedicated Quick Hourly Auto-Update Button
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (autoUpdateIntervalSeconds >= 3600 && isAutoUpdateEnabled) Color(0xFFEFF6FF) else (if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF1E293B) else Color(0xFFF1F5F9)),
                        border = BorderStroke(1.dp, if (autoUpdateIntervalSeconds >= 3600 && isAutoUpdateEnabled) OceanBlue else BentoSlate200),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onToggleAutoUpdate(true)
                                onChangeAutoUpdateInterval(3600)
                            }
                            .testTag("news_hourly_auto_update_feature_btn")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 7.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⏱️", fontSize = 13.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA)
                                        "ପ୍ରତି ଘଣ୍ଟାରେ ସ୍ୱୟଂକ୍ରିୟ ଅପଡେଟ୍ (Hourly Auto-Update)"
                                    else
                                        "Hourly Auto-Update Mode (60 mins)",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp,
                                        color = if (autoUpdateIntervalSeconds >= 3600 && isAutoUpdateEnabled) OceanBlueDark else BentoSlate700
                                    )
                                )
                            }
                            if (autoUpdateIntervalSeconds >= 3600 && isAutoUpdateEnabled) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = EmeraldGreen.copy(alpha = 0.15f),
                                    border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ସକ୍ରିୟ ✓" else "ACTIVE ✓",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = EmeraldGreen,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            } else {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "୧ ଘଣ୍ଟା ଚୟନ" else "Set 1h",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "ସମୟାନ୍ତର:" else "Interval:",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        val intervals = listOf(
                            60 to "1m",
                            300 to "5m",
                            900 to "15m",
                            1800 to "30m",
                            3600 to "1h Hourly"
                        )
                        intervals.forEach { (sec, label) ->
                            val isSel = autoUpdateIntervalSeconds == sec
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSel) OceanBlue else (if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF1E293B) else Color.White),
                                border = BorderStroke(1.dp, if (isSel) OceanBlue else BentoSlate200),
                                modifier = Modifier
                                    .clickable { onChangeAutoUpdateInterval(sec) }
                                    .testTag("news_auto_interval_$label")
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp,
                                        color = if (isSel) Color.White else MaterialTheme.colorScheme.onSurface
                                    ),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Global High-Contrast Night-Reading Mode Quick Toggle Bar
        item {
            val isNightMode = (themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)
            Surface(
                onClick = onToggleNightMode,
                shape = RoundedCornerShape(14.dp),
                color = if (isNightMode) Color(0xFF0F172A) else MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, if (isNightMode) Color(0xFF38BDF8) else Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("news_night_reading_toggle_bar")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (isNightMode) Color(0xFF1E293B) else MaterialTheme.colorScheme.surface,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isNightMode) Icons.Default.NightlightRound else Icons.Default.DarkMode,
                                    contentDescription = "Night reading mode",
                                    tint = if (isNightMode) Color(0xFFFBBF24) else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isNightMode) {
                                    if (language == AppLanguage.ODIA) "ନିଶାର୍ଦ୍ଧ ହାଇ-କଣ୍ଟ୍ରାଷ୍ଟ ଡାର୍କ ମୋଡ୍ (ସକ୍ରିୟ)" else "High-Contrast Night Mode (Active)"
                                } else {
                                    if (language == AppLanguage.ODIA) "ନିଶାର୍ଦ୍ଧ ଖବର ପାଠ ପାଇଁ ଡାର୍କ ମୋଡ୍" else "Night-Time News Reading Mode"
                                },
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNightMode) Color(0xFFFFFFFF) else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଚକ୍ଷୁ ଶ୍ରାନ୍ତି ଦୂର ଓ ସ୍ପଷ୍ଟ ପଠନ ପାଇଁ OLED କଣ୍ଟ୍ରାଷ୍ଟ" else "Pure black & glare-free high contrast for night reading",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    color = if (isNightMode) Color(0xFF94A3B8) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            )
                        }
                    }
                    Switch(
                        checked = isNightMode,
                        onCheckedChange = { onToggleNightMode() },
                        modifier = Modifier.testTag("news_night_mode_switch")
                    )
                }
            }
        }

        // FEATURE 4: River Water Level & Monsoon Flood Early Warning Telemetry Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("river_flood_telemetry_card"),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE0F2FE)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Water,
                                    contentDescription = "River Gauge",
                                    tint = OceanBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ନଦୀ ଜଳସ୍ତର ଓ ବନ୍ୟା ସତର୍କତା" else "River Levels & Flood Early Warning",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 15.sp
                                    )
                                )
                                Text(
                                    text = "Central Water Commission & SRC Odisha",
                                    style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFDCFCE7))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "MONITORED",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF166534),
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    riverGauges.forEach { gauge ->
                        RiverGaugeItemView(gauge = gauge, language = language)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Cyclone Shelter Directory
                    CycloneSheltersView(shelters = cycloneShelters, language = language)

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.CYCLONE_RESILIENCE) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("news_open_cyclone_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Open Offline Cyclone Shelter Map & Checklist", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Button(
                        onClick = { onOpenFeatureSheet(UniqueFeatureSheetType.CITIZEN_CIVIC_EYE) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("news_open_civic_eye_btn"),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("📸", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ଆମ ବାଲେଶ୍ୱର: ନାଗରିକ ଓ ଡ୍ରେନେଜ୍ ଅଭିଯୋଗ ଦାଖଲ" else "Citizen Civic Eye: Report Potholes & Drains",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Category Filter Chips & Local Search Field
        item {
            val popularSearchKeywords = remember {
                listOf(
                    "National",
                    "International",
                    "Gaganyaan",
                    "Amrit Bharat",
                    "DRDO Chandipur",
                    "High Tide",
                    "Global Space",
                    "UN Ocean",
                    "Blue Economy"
                )
            }

            var newsVoiceSearchLang by remember {
                mutableStateOf(
                    if (language == AppLanguage.ODIA) VoiceSearchLanguage.ODIA else VoiceSearchLanguage.ENGLISH
                )
            }

            var isRecentSearchesDropdownExpanded by remember { mutableStateOf(true) }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isBatterySaverActive) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFFEF3C7),
                        border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("news_battery_saving_mode_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) {
                                    "🔋 ବ୍ୟାଟେରୀ ସେଭିଂ ମୋଡ୍ ସକ୍ରିୟ: ଖବର ବ୍ୟାକଗ୍ରାଉଣ୍ଡ ସିଙ୍କ୍ ୩୦ ମିନିଟ୍ କୁ ହ୍ରାସ କରାଯାଇଛି"
                                } else {
                                    "🔋 Battery-Saving Mode Active: Background news sync reduced to 30m interval"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                    }
                }

                // Local Search & Voice-to-Text Input Field (English & Odia) with Recent Searches Dropdown
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { newValue ->
                        onSearchQueryChanged(newValue)
                        isRecentSearchesDropdownExpanded = true
                    },
                    placeholder = {
                        Text(
                            text = if (language == AppLanguage.ODIA) {
                                "ଇଂରାଜୀ କିମ୍ବା ଓଡ଼ିଆରେ କୁହନ୍ତୁ ବା ଖବର ଖୋଜନ୍ତୁ..."
                            } else {
                                "Search or speak in English / ଓଡ଼ିଆ (e.g., Chandipur, ବାଲେଶ୍ୱର)..."
                            },
                            fontSize = 13.sp,
                            color = BentoSlate400
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search News",
                            tint = OceanBlue,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    trailingIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(end = 4.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    isRecentSearchesDropdownExpanded = !isRecentSearchesDropdownExpanded
                                },
                                modifier = Modifier
                                    .size(32.dp)
                                    .testTag("news_recent_searches_dropdown_toggle_btn")
                            ) {
                                Icon(
                                    imageVector = if (isRecentSearchesDropdownExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.History,
                                    contentDescription = "Toggle Recent Searches Dropdown",
                                    tint = OceanBlueDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (searchQuery.isNotBlank()) {
                                IconButton(
                                    onClick = { onSearchQueryChanged("") },
                                    modifier = Modifier
                                        .size(32.dp)
                                        .testTag("news_clear_search_btn")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear Search",
                                        tint = BentoSlate500,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                            VoiceSearchControls(
                                section = VoiceSearchSection.NEWS,
                                isOdiaUi = language == AppLanguage.ODIA,
                                selectedVoiceLang = newsVoiceSearchLang,
                                onVoiceLangChanged = { newsVoiceSearchLang = it },
                                onVoiceResult = { spokenQuery ->
                                    onSearchQueryChanged(spokenQuery)
                                    if (spokenQuery.isNotBlank()) {
                                        onSaveSearchQuery(spokenQuery)
                                    }
                                },
                                tagPrefix = "news"
                            )
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            if (searchQuery.isNotBlank()) {
                                onSaveSearchQuery(searchQuery)
                            }
                        }
                    ),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OceanBlue,
                        unfocusedBorderColor = BentoSlate200,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp)
                        .testTag("news_search_text_field")
                )

                // Room Local Database 'Recent Searches' Search Bar Dropdown List
                AnimatedVisibility(visible = isRecentSearchesDropdownExpanded && (recentSearchQueries.isNotEmpty() || searchQuery.isNotBlank())) {
                    val matchingRecentQueries = remember(recentSearchQueries, searchQuery) {
                        if (searchQuery.isBlank()) {
                            recentSearchQueries.take(6)
                        } else {
                            val filtered = recentSearchQueries.filter { it.contains(searchQuery.trim(), ignoreCase = true) }
                            if (filtered.isNotEmpty()) filtered.take(6) else recentSearchQueries.take(4)
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .testTag("news_recent_searches_dropdown"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.28f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Recent Searches Icon",
                                        tint = OceanBlueDark,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ସମ୍ପ୍ରତି ଖୋଜାଯାଇଥିବା (Local DB):" else "Recent Searches (Local Database):",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = BentoSlate700
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (searchQuery.trim().length >= 2 && !recentSearchQueries.any { it.equals(searchQuery.trim(), ignoreCase = true) }) {
                                        Text(
                                            text = if (language == AppLanguage.ODIA) "+ ସେଭ୍ କରନ୍ତୁ" else "+ Save Search",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF15803D),
                                            modifier = Modifier
                                                .clickable { onSaveSearchQuery(searchQuery.trim()) }
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                .testTag("news_save_current_search_btn")
                                        )
                                    }
                                    if (recentSearchQueries.isNotEmpty()) {
                                        Text(
                                            text = if (language == AppLanguage.ODIA) "ସଫା କରନ୍ତୁ" else "Clear All",
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = OceanBlue,
                                            modifier = Modifier
                                                .clickable { onClearSearchQueries() }
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                                .testTag("clear_recent_search_queries_btn")
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            matchingRecentQueries.forEach { queryText ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onSearchQueryChanged(queryText)
                                            onSaveSearchQuery(queryText)
                                        }
                                        .padding(horizontal = 6.dp, vertical = 6.dp)
                                        .testTag("recent_search_dropdown_item_${queryText.lowercase().replace(" ", "_")}"),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = null,
                                            tint = OceanBlue,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = queryText,
                                            fontSize = 12.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteSearchQuery(queryText) },
                                        modifier = Modifier
                                            .size(26.dp)
                                            .testTag("delete_recent_search_${queryText.lowercase().replace(" ", "_")}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Clear,
                                            contentDescription = "Remove $queryText from Recent Searches",
                                            tint = BentoSlate500,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("news_recent_search_suggestions_row")
                            ) {
                                items(recentSearchQueries) { queryText ->
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = OceanBlue.copy(alpha = 0.08f),
                                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f)),
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .clickable {
                                                onSearchQueryChanged(queryText)
                                                onSaveSearchQuery(queryText)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = queryText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = OceanBlueDark
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Quick Trending Keywords Row (Shown when search is empty)
                if (searchQuery.isBlank()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ଲୋକପ୍ରିୟ:" else "Trending:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate600
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.testTag("news_trending_tags_row")
                        ) {
                            items(popularSearchKeywords) { kw ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .clickable { onSearchQueryChanged(kw) }
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = kw,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = BentoSlate700
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Active Search Summary Banner & Scope Selector (Title + Snippet / Title Only / Snippet Only)
                    val titleMatchCount = remember(filteredArticles, searchQuery) {
                        filteredArticles.count {
                            matchesBilingualSearchQuery(searchQuery, it.title, it.odiaTitle)
                        }
                    }
                    val snippetMatchCount = remember(filteredArticles, searchQuery) {
                        filteredArticles.count {
                            matchesBilingualSearchQuery(searchQuery, it.snippet, it.odiaSnippet)
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFEFF6FF))
                            .border(BorderStroke(1.dp, Color(0xFFBFDBFE)), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp)
                            .testTag("news_search_summary_banner"),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ManageSearch,
                                    contentDescription = null,
                                    tint = OceanBlue,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA)
                                        "\"${searchQuery.trim()}\" ପାଇଁ ${filteredArticles.size} ଟି ଖବର ମିଳିଲା (ଶିରୋନାମା: $titleMatchCount • ସାରାଂଶ: $snippetMatchCount)"
                                    else
                                        "Found ${filteredArticles.size} articles for \"${searchQuery.trim()}\" (Title: $titleMatchCount • Snippet: $snippetMatchCount)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlueDark
                                )
                            }

                            Text(
                                text = if (language == AppLanguage.ODIA) "ସଫା କରନ୍ତୁ ✕" else "Clear ✕",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue,
                                modifier = Modifier
                                    .clickable {
                                        onSearchQueryChanged("")
                                        searchFieldScope = "ALL"
                                    }
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            listOf(
                                "ALL" to (if (language == AppLanguage.ODIA) "ସମସ୍ତ (Title + Snippet)" else "Title & Snippet"),
                                "TITLE" to (if (language == AppLanguage.ODIA) "କେବଳ ଶିରୋନାମା ($titleMatchCount)" else "Title Only ($titleMatchCount)"),
                                "SNIPPET" to (if (language == AppLanguage.ODIA) "କେବଳ ସାରାଂଶ ($snippetMatchCount)" else "Snippet Only ($snippetMatchCount)")
                            ).forEach { (scopeKey, scopeLabel) ->
                                val isScopeSelected = searchFieldScope == scopeKey
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (isScopeSelected) OceanBlue else Color.White,
                                    border = BorderStroke(1.dp, if (isScopeSelected) OceanBlue else Color(0xFF93C5FD)),
                                    modifier = Modifier
                                        .clickable { searchFieldScope = scopeKey }
                                        .testTag("news_search_scope_${scopeKey.lowercase()}")
                                ) {
                                    Text(
                                        text = scopeLabel,
                                        fontSize = 10.sp,
                                        fontWeight = if (isScopeSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        color = if (isScopeSelected) Color.White else OceanBlueDark,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Featured Topic & Category Quick-Filter Bar ('Local Politics', 'Health', 'Tourism', 'Trending', 'All')
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("news_topic_category_quick_filter_card"),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF111C2D) else Color(0xFFF0F9FF)
                ),
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.28f))
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA)
                                "🗂️ ବିଷୟ ଅନୁସାରେ ଖବର ବାছନ୍ତୁ (Category Filter)"
                            else
                                "🗂️ Browse by Topic Category",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color.White else OceanBlueDark
                        )
                        Text(
                            text = "${filteredArticles.size} stories",
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val quickTopicFilters = listOf(
                            Triple("All", if (language == AppLanguage.ODIA) "🌐 ସମସ୍ତ" else "🌐 All", "news_quick_filter_all"),
                            Triple("Government Notices", if (language == AppLanguage.ODIA) "🏛️ ସରକାରୀ ସୂଚନା" else "🏛️ Government Notices", "news_quick_filter_government_notices"),
                            Triple("Local Events", if (language == AppLanguage.ODIA) "🎪 ସ୍ଥାନୀୟ ଉତ୍ସବ" else "🎪 Local Events", "news_quick_filter_local_events"),
                            Triple("Community Highlights", if (language == AppLanguage.ODIA) "🤝 ସମୁଦାୟ ଝଲକ" else "🤝 Community Highlights", "news_quick_filter_community_highlights")
                        )

                        quickTopicFilters.forEach { (catKey, labelText, tag) ->
                            val isActive = when (catKey) {
                                "All" -> selectedCategory.equals("All", ignoreCase = true) || selectedCategory.isBlank()
                                "Government Notices" -> selectedCategory.equals("Government Notices", ignoreCase = true)
                                "Local Events" -> selectedCategory.equals("Local Events", ignoreCase = true)
                                "Community Highlights" -> selectedCategory.equals("Community Highlights", ignoreCase = true)
                                else -> selectedCategory.equals(catKey, ignoreCase = true)
                            }
                            Surface(
                                onClick = {
                                    com.example.util.BalasoreHapticHelper.vibrateToggle(context)
                                    onCategorySelected(catKey)
                                },
                                shape = RoundedCornerShape(10.dp),
                                color = if (isActive) OceanBlue else Color.White,
                                border = BorderStroke(1.dp, if (isActive) OceanBlue else OceanBlue.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(tag)
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = labelText,
                                        fontSize = 9.5.sp,
                                        fontWeight = if (isActive) FontWeight.ExtraBold else FontWeight.Bold,
                                        color = if (isActive) Color.White else OceanBlueDark,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
                    .testTag("news_category_filter_row"),
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { cat ->
                    val isSelected = when (cat) {
                        "All" -> selectedCategory.equals("All", ignoreCase = true) || selectedCategory.isBlank()
                        "Trending" -> selectedCategory.equals("Trending", ignoreCase = true) || selectedCategory.equals("Trending Stories", ignoreCase = true)
                        "Local Politics" -> selectedCategory.equals("Local Politics", ignoreCase = true)
                        "Health" -> selectedCategory.equals("Health", ignoreCase = true) || selectedCategory.equals("Healthcare", ignoreCase = true) || selectedCategory.equals("Medical", ignoreCase = true)
                        "Coastal Defense" -> selectedCategory.equals("Coastal Defense", ignoreCase = true)
                        "Local Events" -> selectedCategory.equals("Local Events", ignoreCase = true)
                        "Spiritual" -> selectedCategory.equals("Spiritual", ignoreCase = true)
                        "Politics" -> selectedCategory.equals("Politics", ignoreCase = true)
                        "Sports" -> selectedCategory.equals("Sports", ignoreCase = true) || selectedCategory.equals("Athletics", ignoreCase = true)
                        "Local" -> selectedCategory.equals("Local", ignoreCase = true) || selectedCategory.equals("Local News", ignoreCase = true)
                        "Weather" -> selectedCategory.equals("Weather", ignoreCase = true) || selectedCategory.equals("Weather Updates", ignoreCase = true)
                        "Tourism" -> selectedCategory.equals("Tourism", ignoreCase = true) || selectedCategory.equals("Tourism Updates", ignoreCase = true)
                        "Emergency" -> selectedCategory.equals("Emergency", ignoreCase = true) || selectedCategory.equals("Emergency News", ignoreCase = true)
                        "National" -> selectedCategory.equals("National", ignoreCase = true) || selectedCategory.equals("National News", ignoreCase = true)
                        "International" -> selectedCategory.equals("International", ignoreCase = true) || selectedCategory.equals("International News", ignoreCase = true) || selectedCategory.equals("World", ignoreCase = true)
                        "Defense" -> selectedCategory.equals("Defense", ignoreCase = true) || selectedCategory.equals("Defense Alerts", ignoreCase = true)
                        "Saved Bookmarks" -> selectedCategory.equals("Saved Bookmarks", ignoreCase = true) || selectedCategory.equals("Saved", ignoreCase = true) || selectedCategory.equals("My Saved", ignoreCase = true)
                        else -> selectedCategory.equals(cat, ignoreCase = true)
                    }
                    val count = categoryCounts[cat] ?: 0
                    val emoji = when (cat) {
                        "All" -> "🌐"
                        "Government Notices" -> "🏛️"
                        "Local Events" -> "🎪"
                        "Community Highlights" -> "🤝"
                        "Trending" -> "🔥"
                        "Local Politics" -> "🏛️"
                        "Health" -> "🩺"
                        "Coastal Defense" -> "🛡️"
                        "Spiritual" -> "🛕"
                        "Politics" -> "🏛️"
                        "Sports" -> "🏆"
                        "Local" -> "📍"
                        "Weather" -> "🌦️"
                        "Tourism" -> "🏖️"
                        "Emergency" -> "🚨"
                        "National" -> "🇮🇳"
                        "International" -> "🌍"
                        "Defense" -> "🚀"
                        "Saved Bookmarks" -> "🔖"
                        else -> "📰"
                    }
                    val displayLabel = if (language == AppLanguage.ODIA) {
                        when (cat) {
                            "All" -> "ସମସ୍ତ"
                            "Government Notices" -> "ସରକାରୀ ସୂଚନା"
                            "Local Events" -> "ସ୍ଥାନୀୟ ଉତ୍ସବ"
                            "Community Highlights" -> "ସମୁଦାୟ ଝଲକ"
                            "Trending" -> "ଟ୍ରେଣ୍ଡିଂ"
                            "Local Politics" -> "ସ୍ଥାନୀୟ ରାଜନୀତି"
                            "Health" -> "ସ୍ୱାସ୍ଥ୍ୟ"
                            "Coastal Defense" -> "ଉପକୂଳ ପ୍ରତିରକ୍ଷା"
                            "Spiritual" -> "ଆଧ୍ୟାତ୍ମିକ"
                            "Politics" -> "ରାଜନୀତି"
                            "Sports" -> "କ୍ରୀଡ଼ା"
                            "Local" -> "ସ୍ଥାନୀୟ"
                            "Weather" -> "ପାଣିପାଗ"
                            "Tourism" -> "ପର୍ଯ୍ୟଟନ"
                            "Emergency" -> "ଜରୁରୀକାଳୀନ"
                            "National" -> "ଜାତୀୟ"
                            "International" -> "ଆନ୍ତର୍ଜାତୀୟ"
                            "Defense" -> "ପ୍ରତିରକ୍ଷା"
                            "Saved Bookmarks" -> "ସାଇତା"
                            else -> cat
                        }
                    } else {
                        when (cat) {
                            "All" -> "All"
                            "Government Notices" -> "Government Notices"
                            "Local Events" -> "Local Events"
                            "Community Highlights" -> "Community Highlights"
                            "Trending" -> "Trending"
                            "Local Politics" -> "Local Politics"
                            "Health" -> "Health"
                            "Coastal Defense" -> "Coastal Defense"
                            "Spiritual" -> "Spiritual"
                            "Politics" -> "Politics"
                            "Sports" -> "Sports"
                            "Local" -> "Local"
                            "Weather" -> "Weather"
                            "Tourism" -> "Tourism"
                            "Emergency" -> "Emergency"
                            "National" -> "National"
                            "International" -> "International"
                            "Defense" -> "Defense"
                            "Saved Bookmarks" -> "Saved"
                            else -> cat
                        }
                    }

                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategorySelected(cat) },
                        leadingIcon = {
                            Text(emoji, fontSize = 12.sp)
                        },
                        label = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = displayLabel,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                                    )
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(if (isSelected) Color.White.copy(alpha = 0.25f) else BentoSlate200)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "$count",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color.White else BentoSlate700
                                    )
                                }
                            }
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanBlue,
                            selectedLabelColor = Color.White,
                            containerColor = BentoSlate100,
                            labelColor = BentoSlate700
                        ),
                        border = null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("news_filter_chip_${cat.lowercase().replace(" ", "_")}")
                    )
                }
            }

            // Active category indicator and quick reset banner
            if (!selectedCategory.equals("All", ignoreCase = true) && selectedCategory.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeLabel = if (language == AppLanguage.ODIA) {
                        when {
                            selectedCategory.equals("Politics", ignoreCase = true) || selectedCategory.equals("Local Politics", ignoreCase = true) -> "ରାଜନୀତି ଖବର"
                            selectedCategory.equals("Sports", ignoreCase = true) -> "କ୍ରୀଡ଼ା ଖବର"
                            selectedCategory.equals("National", ignoreCase = true) || selectedCategory.equals("National News", ignoreCase = true) -> "ଜାତୀୟ ଖବର"
                            selectedCategory.equals("International", ignoreCase = true) || selectedCategory.equals("International News", ignoreCase = true) || selectedCategory.equals("World", ignoreCase = true) -> "ଆନ୍ତର୍ଜାତୀୟ ଖବର"
                            selectedCategory.equals("Local", ignoreCase = true) || selectedCategory.equals("Local News", ignoreCase = true) -> "ସ୍ଥାନୀୟ ଖବର"
                            selectedCategory.equals("Defense", ignoreCase = true) || selectedCategory.equals("Defense Alerts", ignoreCase = true) -> "ପ୍ରତିରକ୍ଷା ଓ ଉପକୂଳ ସତର୍କତା"
                            selectedCategory.equals("Tourism", ignoreCase = true) || selectedCategory.equals("Tourism Updates", ignoreCase = true) -> "ପର୍ଯ୍ୟଟନ ଓ ସଂସ୍କୃତି"
                            selectedCategory.equals("Saved Bookmarks", ignoreCase = true) || selectedCategory.equals("Saved", ignoreCase = true) -> "ସାଇତା ଖବର"
                            else -> selectedCategory
                        }
                    } else {
                        "Filtered by: $selectedCategory"
                    }

                    Text(
                        text = "$activeLabel (${filteredArticles.size})",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                    )

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onCategorySelected("All") }
                            .background(Color(0xFFE0F2FE))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Clear category filter",
                            tint = OceanBlue,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = if (language == AppLanguage.ODIA) "ସବୁ ଦେଖନ୍ତୁ" else "Show All",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                    }
                }
            }
        }

        // Offline News Download & Sync Banner + Community Forum Card
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                com.example.ui.components.BalasoreOfflineDownloadBannerCard(
                    mode = "NEWS",
                    language = language,
                    articles = mergedArticles,
                    onOpenOfflineManager = {
                        onOpenFeatureSheet(UniqueFeatureSheetType.OFFLINE_MODE_MANAGER)
                    }
                )

                com.example.ui.components.BalasoreCommunityForumCard(
                    language = language,
                    initialCategory = "Local News",
                    onOpenFullForumSheet = {
                        onOpenFeatureSheet(UniqueFeatureSheetType.COMMUNITY_FORUM)
                    }
                )

                com.example.ui.components.BalasoreLocalBusTrackerBannerCard(
                    language = language,
                    onOpenFullTracker = {
                        onOpenFeatureSheet(UniqueFeatureSheetType.LOCAL_BUS_TRACKER)
                    }
                )

                // Interactive Map Banner: News Locations, Weather Alert Areas & Tourist Hotspots
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            onOpenFeatureSheet(UniqueFeatureSheetType.TOURISM_INTERACTIVE_MAP)
                        }
                        .testTag("news_interactive_map_banner_card"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF0F2942) else Color(0xFFF0F9FF)
                    ),
                    border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = OceanBlue,
                                modifier = Modifier.size(38.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("🗺️", fontSize = 17.sp)
                                }
                            }
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) {
                                        "ଇଣ୍ଟରାକ୍ଟିଭ୍ ବାଲେଶ୍ୱର ମାନଚିତ୍ର (ଖବର, ପାଣିପାଗ ଓ ପର୍ଯ୍ୟଟନ)"
                                    } else {
                                        "Interactive Map: News, Weather Alerts & Hotspots"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color.White else Color(0xFF0F172A)
                                )
                                Text(
                                    text = if (language == AppLanguage.ODIA) {
                                        "${com.example.data.maps.BalasoreInteractiveMapDataSource.BALASORE_GEO_NEWS_LOCATIONS.size} ଖବର ସ୍ଥାନ • ${com.example.data.maps.BalasoreInteractiveMapDataSource.BALASORE_WEATHER_ALERT_ZONES.size} ପାଣିପାଗ ସତର୍କତା କ୍ଷେତ୍ର • 8 ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ"
                                    } else {
                                        "${com.example.data.maps.BalasoreInteractiveMapDataSource.BALASORE_GEO_NEWS_LOCATIONS.size} News Pins • ${com.example.data.maps.BalasoreInteractiveMapDataSource.BALASORE_WEATHER_ALERT_ZONES.size} Weather Alert Zones • 8 Tourist Hotspots"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFFBAE6FD) else Color(0xFF0369A1)
                                )
                            }
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = OceanBlue
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ମାନଚିତ୍ର ଖୋଲନ୍ତୁ" else "Explore Map",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Daily News Brief (Gemini API 3-Bullet Point Highlight & 8:00 AM Daily Digest)
        item {
            var isDigestEnabled by remember {
                mutableStateOf(com.example.data.notification.DailyNewsDigestManager.isDigestEnabled(context))
            }
            var digestSummary by remember {
                mutableStateOf(com.example.data.notification.DailyNewsDigestManager.getLastDigestSummary(context))
            }
            var isGeneratingDigest by remember { mutableStateOf(false) }
            var isDigestExpanded by remember { mutableStateOf(true) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("daily_news_digest_8am_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF0C243B) else Color(0xFFF0F9FF)
                ),
                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f))
            ) {
                Column(
                    modifier = Modifier
                        .padding(14.dp)
                        .testTag("daily_news_brief_card")
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
                                color = OceanBlue,
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text("✨", fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = if (language == AppLanguage.ODIA) {
                                        "ଦୈନିକ ସମ୍ବାଦ ସାରାଂଶ (Daily News Brief • Gemini AI)"
                                    } else {
                                        "Daily News Brief • Gemini AI 3-Bullet Highlight"
                                    },
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color.White else BentoSlate900
                                    )
                                )
                                Text(
                                    text = if (language == AppLanguage.ODIA) {
                                        "ବାଲେଶ୍ୱରର ଶୀର୍ଷ ୩ ଟ୍ରେଣ୍ଡିଂ ଖବରର ୩-ବୁଲେଟ୍ ହାଇଲାଇଟ୍ (Gemini AI)"
                                    } else {
                                        "Top trending Balasore news summarized into a 3-bullet highlight"
                                    },
                                    fontSize = 11.sp,
                                    color = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color.LightGray else BentoSlate600
                                )
                            }
                        }

                        androidx.compose.material3.Switch(
                            checked = isDigestEnabled,
                            onCheckedChange = { enabled ->
                                isDigestEnabled = enabled
                                com.example.data.notification.DailyNewsDigestManager.setDigestEnabled(context, enabled)
                            },
                            modifier = Modifier.testTag("daily_news_digest_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (digestSummary.isNotBlank() && isDigestExpanded) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .testTag("daily_news_brief_bullets_box")
                        ) {
                            Text(
                                text = digestSummary,
                                fontSize = 11.5.sp,
                                lineHeight = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (!isGeneratingDigest) {
                                    isGeneratingDigest = true
                                    isDigestExpanded = true
                                    coroutineScope.launch {
                                        digestSummary = com.example.data.notification.DailyNewsDigestManager
                                            .generateAndDispatchDigestNow(context, articles)
                                        isGeneratingDigest = false
                                    }
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("trigger_daily_digest_now_button")
                        ) {
                            Text(
                                text = if (isGeneratingDigest) {
                                    if (language == AppLanguage.ODIA) "AI ସାରାଂଶ ପ୍ରସ୍ତୁତ ହେଉଛି..." else "Summarizing with Gemini..."
                                } else {
                                    if (language == AppLanguage.ODIA) "✨ ଏବେ AI ଡାଇଜେଷ୍ଟ୍ ପଠାନ୍ତୁ" else "✨ Summarize Top 3 & Notify Now"
                                },
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (digestSummary.isNotBlank()) {
                            Surface(
                                onClick = { isDigestExpanded = !isDigestExpanded },
                                shape = RoundedCornerShape(10.dp),
                                color = OceanBlue.copy(alpha = 0.1f),
                                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.3f)),
                                modifier = Modifier.testTag("toggle_daily_digest_preview_button")
                            ) {
                                Text(
                                    text = if (isDigestExpanded) "Hide" else "Preview",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlue,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Loading skeleton when data is being fetched or refreshed
        if (effectiveIsRefreshing) {
            item {
                NewsListSkeleton(count = 4, showBreakingBanner = true)
            }
        } else {
            // Dedicated Offline Reading Mode Banner when viewing Saved Bookmarks
            if (selectedCategory == "Saved Bookmarks" || selectedCategory == "Bookmarks") {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("news_offline_bookmarks_banner"),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF0C243B) else Color(0xFFEFF6FF)
                        ),
                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(OceanBlue),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = "Saved Bookmarks",
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ଅଫଲାଇନ୍ ପଠନ ପାଇଁ ସାଇତା ଖବର" else "Saved Stories for Offline Reading",
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color.White else BentoSlate900
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = EmeraldGreen.copy(alpha = 0.15f),
                                        border = BorderStroke(1.dp, EmeraldGreen.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "Room DB",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = EmeraldGreen,
                                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA)
                                        "ଏହି ସମସ୍ତ ${filteredArticles.size} ଟି ଖବର ସ୍ଥାନୀୟ Room ଡାଟାବେସରେ ସାଇତା ଅଛି ଏବଂ ଇଣ୍ଟରନେଟ୍ ବିନା ମଧ୍ୟ ସମ୍ପୂର୍ଣ୍ଣ ପଢ଼ାଯାଇପାରିବ।"
                                    else
                                        "${filteredArticles.size} stories stored locally in Room database. Fully accessible without active internet connection.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF94A3B8) else BentoSlate600,
                                        fontSize = 11.5.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (filteredArticles.isEmpty()) {
                item {
                    if (selectedCategory == "Saved Bookmarks" || selectedCategory == "Bookmarks") {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .testTag("news_empty_bookmarks_card"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(OceanBlue.copy(alpha = 0.12f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkBorder,
                                        contentDescription = null,
                                        tint = OceanBlue,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(14.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "କୌଣସି ଖବର ସାଇତା ହୋଇନାହିଁ" else "No Saved Stories Yet",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA)
                                        "ଅଫଲାଇନ୍ରେ ପଢ଼ିବା ପାଇଁ ଯେକୌଣସି ଖବରର ବୁକ୍‌ମାର୍କ ଆଇକନ୍ (🔖) ଉପରେ କ୍ଲିକ୍ କରି Room ଡାଟାବେସରେ ସାଇତି ରଖନ୍ତୁ।"
                                    else
                                        "Tap the bookmark icon (🔖) on any news story to save it to your local Room database for offline reading even without internet.",
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = BentoSlate500,
                                        fontSize = 12.5.sp,
                                        lineHeight = 18.sp
                                    ),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                Button(
                                    onClick = { onCategorySelected("All") },
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(
                                        text = if (language == AppLanguage.ODIA) "ସମସ୍ତ ଖବର ଦେଖନ୍ତୁ" else "Browse All News Stories",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    } else {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                                .testTag("news_empty_search_card"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("📰", fontSize = 36.sp)
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "କୌଣସି ଖବର ମିଳିଲା ନାହିଁ" else "No News Articles Found",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA)
                                        "'$searchQuery' ପାଇଁ କୌଣସି ଆର୍ଟିକିଲ୍ ମିଳିଲା ନାହିଁ।"
                                    else
                                        "No articles match the keyword '$searchQuery' in category '$selectedCategory'.",
                                    style = MaterialTheme.typography.bodySmall.copy(
                                        color = BentoSlate600,
                                        fontSize = 12.sp
                                    )
                                )
                                Spacer(modifier = Modifier.height(14.dp))
                                Button(
                                    onClick = {
                                        onSearchQueryChanged("")
                                        onCategorySelected("All")
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("Reset Search Filters", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            } else if (currentNewsTab == 6 || selectedCategory.equals("Saved Bookmarks", ignoreCase = true) || selectedCategory.equals("Saved", ignoreCase = true) || selectedCategory.equals("My Saved", ignoreCase = true)) {
                // Dedicated 'My Saved' tab rendering bookmarked articles & emergency contacts from Room DB
                item {
                    MySavedBookmarksSection(
                        savedBookmarks = savedBookmarks,
                        language = language,
                        onArticleClick = { selectedArticleForDetail = it },
                        onRemoveBookmark = { onRemoveSavedBookmark(it) },
                        onCallEmergency = { onCallEmergency(it) }
                    )
                }
            // Dedicated Firestore 'Trending' Popular Stories Leaderboard Banner when viewing Trending tab
            if (selectedCategory.equals("Trending", ignoreCase = true) || selectedCategory.equals("Trending Stories", ignoreCase = true)) {
                item {
                    val totalTrackedViews = remember(filteredArticles, trendingViewCounts) {
                        filteredArticles.sumOf {
                            com.example.data.firebase.FirestoreService.getViewCountForId(it.id, trendingViewCounts)
                        }
                    }
                    val syncTimeFormatted = remember(lastTrendingSyncTs) {
                        java.text.SimpleDateFormat("hh:mm:ss a", java.util.Locale.getDefault())
                            .format(java.util.Date(lastTrendingSyncTs))
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                            .testTag("news_trending_firestore_banner"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFF0F172A) else Color(0xFFFFF7ED)
                        ),
                        border = BorderStroke(1.dp, Color(0xFFFB923C))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
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
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFEA580C)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.TrendingUp,
                                            contentDescription = "Trending Stories",
                                            tint = Color.White,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = if (language == AppLanguage.ODIA)
                                                "🔥 ସବୁଠାରୁ ଲୋକପ୍ରିୟ ଖବର (Firestore Trending)"
                                            else
                                                "🔥 Most Popular Stories (Firestore View Counts)",
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color.White else Color(0xFF9A3412)
                                            )
                                        )
                                        Text(
                                            text = if (language == AppLanguage.ODIA)
                                                "Firestore 'news_article_views' • ମୋଟ୍ $totalTrackedViews ଭ୍ୟୁ • ସିଙ୍କ୍: $syncTimeFormatted"
                                            else
                                                "Ranked live by Firestore • $totalTrackedViews views • Synced $syncTimeFormatted",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = if ((themeMode == ThemeMode.DARK || themeMode == ThemeMode.HIGH_CONTRAST_DARK || com.example.ui.theme.LocalAppTheme.current.isDark)) Color(0xFFCBD5E1) else Color(0xFFC2410C)
                                            )
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEA580C),
                                    modifier = Modifier
                                        .clickable { triggerPullToRefresh() }
                                        .testTag("news_trending_firestore_refresh_btn")
                                ) {
                                    Text(
                                        text = if (effectiveIsRefreshing) {
                                            if (language == AppLanguage.ODIA) "ଆଣାଯାଉଛି..." else "Fetching..."
                                        } else {
                                            if (language == AppLanguage.ODIA) "🔄 ରିଫ୍ରେସ୍" else "🔄 Pull Firestore"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Articles List with Staggered Entry Animation during offline cache recovery
            itemsIndexed(filteredArticles, key = { _, it -> it.id }) { index, article ->
                var isEntered by remember(article.id) { mutableStateOf(false) }
                LaunchedEffect(article.id) {
                    delay((index.coerceAtMost(8) * 45L))
                    isEntered = true
                }
                val alphaAnim by animateFloatAsState(
                    targetValue = if (isEntered) 1f else 0f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                    label = "news_stagger_alpha"
                )
                val translateYAnim by animateFloatAsState(
                    targetValue = if (isEntered) 0f else 24f,
                    animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
                    label = "news_stagger_translate"
                )
                val articleViewCount = com.example.data.firebase.FirestoreService.getViewCountForId(
                    article.id,
                    trendingViewCounts
                )
                val trendingRank = if (
                    (selectedCategory.equals("Trending", ignoreCase = true) || selectedCategory.equals("Trending Stories", ignoreCase = true)) &&
                    index < 3
                ) {
                    index + 1
                } else {
                    null
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = alphaAnim
                            translationY = translateYAnim
                        }
                ) {
                    NewsCard(
                        article = article,
                        language = language,
                        searchQuery = searchQuery,
                        isBookmarked = bookmarkedIds.contains(article.id),
                        viewCount = articleViewCount,
                        trendingRank = trendingRank,
                        onToggleBookmark = { onToggleBookmark(article.id) },
                        onArticleClick = {
                            coroutineScope.launch {
                                trendingFirestoreService.incrementArticleViewCount(
                                    articleId = article.id,
                                    title = article.title,
                                    category = article.category
                                )
                            }
                            selectedArticleForDetail = article
                        },
                        onShare = {
                            ShareHelper.shareNews(
                                context = context,
                                article = article,
                                isOdia = language == AppLanguage.ODIA
                            )
                        }
                    )
                }
            }
        }
    }

    // AdMob Banner Placement in News feed
    item {
        AdMobBannerCard(
            adUnitId = AdMobManager.AD_UNIT_BANNER_NEWS,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}
}
}

private fun extractSearchTokens(query: String): List<String> {
    val trimmed = query.trim()
    if (trimmed.isEmpty()) return emptyList()
    val tokens = trimmed.split(Regex("\\s+"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    return (listOf(trimmed) + tokens).distinct().sortedByDescending { it.length }
}

private fun countKeywordMatches(text: String, query: String): Int {
    val tokens = extractSearchTokens(query)
    if (tokens.isEmpty() || text.isEmpty()) return 0
    val lowerText = text.lowercase()
    var count = 0
    val occupied = BooleanArray(text.length)
    for (token in tokens) {
        val lowerToken = token.lowercase()
        if (lowerToken.isEmpty()) continue
        var startIdx = 0
        while (startIdx < lowerText.length) {
            val found = lowerText.indexOf(lowerToken, startIdx)
            if (found == -1) break
            val endIdx = found + lowerToken.length
            val overlaps = (found until endIdx).any { occupied[it] }
            if (!overlaps) {
                for (i in found until endIdx) occupied[i] = true
                count++
            }
            startIdx = found + 1
        }
    }
    return count
}

@Composable
fun HighlightedNewsText(
    text: String,
    query: String,
    style: TextStyle,
    highlightColor: Color = Color(0xFFFEF08A),
    textColor: Color = BentoSlate900,
    modifier: Modifier = Modifier
) {
    val tokens = remember(query) { extractSearchTokens(query) }
    if (tokens.isEmpty() || tokens.none { text.contains(it, ignoreCase = true) }) {
        Text(text = text, style = style, color = textColor, modifier = modifier)
        return
    }

    val annotated = remember(text, tokens, highlightColor, textColor) {
        val lowerText = text.lowercase()
        val highlightedMask = BooleanArray(text.length)
        for (token in tokens) {
            val lowerToken = token.lowercase()
            if (lowerToken.isEmpty()) continue
            var startIdx = 0
            while (startIdx < lowerText.length) {
                val matchIdx = lowerText.indexOf(lowerToken, startIdx)
                if (matchIdx == -1) break
                for (i in matchIdx until (matchIdx + lowerToken.length).coerceAtMost(text.length)) {
                    highlightedMask[i] = true
                }
                startIdx = matchIdx + 1
            }
        }

        buildAnnotatedString {
            var i = 0
            while (i < text.length) {
                val isHighlighted = highlightedMask[i]
                var j = i + 1
                while (j < text.length && highlightedMask[j] == isHighlighted) {
                    j++
                }
                if (isHighlighted) {
                    pushStyle(
                        SpanStyle(
                            background = highlightColor,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF78350F)
                        )
                    )
                    append(text.substring(i, j))
                    pop()
                } else {
                    pushStyle(SpanStyle(color = textColor))
                    append(text.substring(i, j))
                    pop()
                }
                i = j
            }
        }
    }

    Text(text = annotated, style = style, modifier = modifier)
}

@Composable
fun NewsCard(
    article: NewsArticle,
    language: AppLanguage,
    searchQuery: String = "",
    isBookmarked: Boolean,
    viewCount: Int? = null,
    trendingRank: Int? = null,
    onToggleBookmark: () -> Unit,
    onShare: () -> Unit,
    onArticleClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = androidx.compose.runtime.rememberCoroutineScope()
    val firestoreService = remember { com.example.data.firebase.FirestoreService(context) }
    val liveViewCounts by com.example.data.firebase.FirestoreService.trendingViewCountsFlow.collectAsStateWithLifecycle()
    val effectiveViewCount = viewCount ?: com.example.data.firebase.FirestoreService.getViewCountForId(article.id, liveViewCounts)
    val ttsHelper = remember { NewsTextToSpeechHelper.getInstance(context) }
    val isTtsSpeaking by ttsHelper.isSpeaking.collectAsStateWithLifecycle()
    val currentlySpeakingId by ttsHelper.currentArticleId.collectAsStateWithLifecycle()
    val isThisArticleSpeaking = isTtsSpeaking && (currentlySpeakingId == article.id)

    val query = searchQuery.trim()
    val tokens = remember(query) { extractSearchTokens(query) }

    // Select language display text, falling back to bilingual field if keyword matched in the alternate script
    val primaryTitle = if (language == AppLanguage.ODIA && article.odiaTitle.isNotBlank()) article.odiaTitle else article.title
    val secondaryTitle = if (language == AppLanguage.ODIA) article.title else article.odiaTitle
    val titleText = if (tokens.isNotEmpty() && tokens.none { primaryTitle.contains(it, ignoreCase = true) } && tokens.any { secondaryTitle.contains(it, ignoreCase = true) }) {
        secondaryTitle
    } else {
        primaryTitle
    }

    val primarySnippet = if (language == AppLanguage.ODIA && article.odiaSnippet.isNotBlank()) article.odiaSnippet else article.snippet
    val secondarySnippet = if (language == AppLanguage.ODIA) article.snippet else article.odiaSnippet
    val snippetText = if (tokens.isNotEmpty() && tokens.none { primarySnippet.contains(it, ignoreCase = true) } && tokens.any { secondarySnippet.contains(it, ignoreCase = true) }) {
        secondarySnippet
    } else {
        primarySnippet
    }

    val contentText = if (language == AppLanguage.ODIA && article.odiaContent.isNotBlank()) article.odiaContent else article.content

    val titleHits = remember(titleText, query) { countKeywordMatches(titleText, query) }
    val snippetHits = remember(snippetText, query) { countKeywordMatches(snippetText, query) }
    val isMatchedInTitle = titleHits > 0
    val isMatchedInSnippet = snippetHits > 0
    val isMatchedInContent = tokens.isNotEmpty() && tokens.any { contentText.contains(it, ignoreCase = true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(enabled = onArticleClick != null) { onArticleClick?.invoke() }
            .testTag("news_card_${article.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (trendingRank != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFEA580C),
                            modifier = Modifier
                                .padding(end = 6.dp)
                                .testTag("news_card_trending_rank_${article.id}")
                        ) {
                            Text(
                                text = "#$trendingRank TRENDING",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Category badge with distinctive styling
                    val isNational = article.category.equals("National News", ignoreCase = true) || article.category.equals("National", ignoreCase = true)
                    val isInternational = article.category.equals("International News", ignoreCase = true) || article.category.equals("International", ignoreCase = true) || article.category.equals("World", ignoreCase = true)
                    val isDefense = article.category.equals("Defense Alerts", ignoreCase = true) || article.category.equals("Defense", ignoreCase = true)
                    val isTourism = article.category.equals("Tourism Updates", ignoreCase = true) || article.category.equals("Tourism", ignoreCase = true)

                    val badgeBg = when {
                        isNational -> Color(0xFFFEF3C7)
                        isInternational -> Color(0xFFE0E7FF)
                        isDefense -> Color(0xFFFFEDD5)
                        isTourism -> Color(0xFFE0F2FE)
                        else -> OceanBlue.copy(alpha = 0.12f)
                    }
                    val badgeBorder = when {
                        isNational -> Color(0xFFFDE68A)
                        isInternational -> Color(0xFFC7D2FE)
                        isDefense -> Color(0xFFFED7AA)
                        isTourism -> Color(0xFFBAE6FD)
                        else -> Color.Transparent
                    }
                    val badgeTextColor = when {
                        isNational -> Color(0xFF92400E)
                        isInternational -> Color(0xFF3730A3)
                        isDefense -> Color(0xFF9A3412)
                        isTourism -> Color(0xFF0369A1)
                        else -> OceanBlueDark
                    }
                    val badgeLabel = when {
                        isNational -> if (language == AppLanguage.ODIA) "🇮🇳 ଜାତୀୟ ଖବର" else "🇮🇳 National News"
                        isInternational -> if (language == AppLanguage.ODIA) "🌐 ଆନ୍ତର୍ଜାତୀୟ" else "🌐 International"
                        isDefense -> if (language == AppLanguage.ODIA) "🛡️ ପ୍ରତିରକ୍ଷା" else "🛡️ Defense"
                        isTourism -> if (language == AppLanguage.ODIA) "🏖️ ପର୍ଯ୍ୟଟନ" else "🏖️ Tourism"
                        else -> article.category
                    }

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = badgeBg,
                        border = if (badgeBorder != Color.Transparent) BorderStroke(1.dp, badgeBorder) else null
                    ) {
                        Text(
                            text = badgeLabel,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = badgeTextColor
                            ),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    if (isBookmarked) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFDCFCE7),
                            border = BorderStroke(1.dp, Color(0xFF86EFAC))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    modifier = Modifier.size(10.dp),
                                    tint = Color(0xFF166534)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ଅଫଲାଇନ୍ ସାଇତା" else "Offline Ready (Room)",
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF166534)
                                )
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFFFF7ED),
                        border = BorderStroke(0.8.dp, Color(0xFFFED7AA)),
                        modifier = Modifier
                            .padding(end = 6.dp)
                            .testTag("news_card_view_count_${article.id}")
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "🔥 $effectiveViewCount ଭ୍ୟୁ" else "🔥 $effectiveViewCount views",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFC2410C),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = article.timeAgo,
                        style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Real-time Keyword Match Location & Hit Count Badge when searching
            if (query.isNotEmpty() && (isMatchedInTitle || isMatchedInSnippet || isMatchedInContent)) {
                val matchBadgeLabel = when {
                    isMatchedInTitle && isMatchedInSnippet ->
                        if (language == AppLanguage.ODIA) "🔍 ଶିରୋନାମା ଓ ସାରାଂଶରେ ମିଳିଲା (${titleHits + snippetHits})" else "🔍 Matched in Title & Snippet (${titleHits + snippetHits} hits)"
                    isMatchedInTitle ->
                        if (language == AppLanguage.ODIA) "🔍 ଶିରୋନାମାରେ ମିଳିଲା ($titleHits)" else "🔍 Matched in Title ($titleHits ${if (titleHits == 1) "hit" else "hits"})"
                    isMatchedInSnippet ->
                        if (language == AppLanguage.ODIA) "🔍 ସାରାଂଶରେ ମିଳିଲା ($snippetHits)" else "🔍 Matched in Snippet ($snippetHits ${if (snippetHits == 1) "hit" else "hits"})"
                    else ->
                        if (language == AppLanguage.ODIA) "🔍 ସମ୍ପୂର୍ଣ୍ଣ ଖବରରେ ମିଳିଲା" else "🔍 Matched in Full Article"
                }
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFFEF9C3),
                    border = BorderStroke(1.dp, Color(0xFFFDE047)),
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .testTag("news_card_match_badge_${article.id}")
                ) {
                    Text(
                        text = matchBadgeLabel,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF854D0E),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            // Highlighted Title
            HighlightedNewsText(
                text = titleText,
                query = query,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    lineHeight = 22.sp
                ),
                textColor = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.testTag("news_card_title_${article.id}")
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Highlighted Snippet
            HighlightedNewsText(
                text = snippetText,
                query = query,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 20.sp
                ),
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.testTag("news_card_snippet_${article.id}")
            )

            // Context Excerpt if keyword matched in full article text but not snippet/title
            if (isMatchedInContent && !isMatchedInTitle && !isMatchedInSnippet) {
                val matchIdx = contentText.indexOf(query, ignoreCase = true)
                val start = (matchIdx - 35).coerceAtLeast(0)
                val end = (matchIdx + query.length + 55).coerceAtMost(contentText.length)
                val excerpt = "${if (start > 0) "..." else ""}${contentText.substring(start, end)}${if (end < contentText.length) "..." else ""}"

                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFFEF3C7))
                        .border(BorderStroke(1.dp, Color(0xFFFCD34D)), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = Color(0xFFB45309),
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଆର୍ଟିକିଲ୍ ମଧ୍ୟରେ ମିଳିଥିବା ଅଂଶ:" else "Matched inside article text:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF92400E)
                            )
                        }
                        Spacer(modifier = Modifier.height(3.dp))
                        HighlightedNewsText(
                            text = excerpt,
                            query = query,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp, color = Color(0xFF78350F), lineHeight = 18.sp),
                            highlightColor = Color(0xFFFDE68A)
                        )
                    }
                }
            }

            // Expandable Full Story View
            if (contentText.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .clickable {
                            val nextExpanded = !isExpanded
                            isExpanded = nextExpanded
                            if (nextExpanded) {
                                coroutineScope.launch {
                                    firestoreService.incrementArticleViewCount(
                                        articleId = article.id,
                                        title = article.title,
                                        category = article.category
                                    )
                                }
                            }
                        }
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = OceanBlue,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isExpanded) {
                                if (language == AppLanguage.ODIA) "ସମ୍ପୂର୍ଣ୍ଣ ଖବର ବନ୍ଦ କରନ୍ତୁ" else "Collapse Full Story"
                            } else {
                                if (language == AppLanguage.ODIA) "ସମ୍ପୂର୍ଣ୍ଣ ଖବର ପଢ଼ନ୍ତୁ" else "Read Full Story"
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                    }

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = OceanBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }

                AnimatedVisibility(visible = isExpanded) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(BorderStroke(1.dp, BentoSlate100), RoundedCornerShape(10.dp))
                            .padding(12.dp)
                    ) {
                        val words = contentText.split("\\s+".toRegex()).size
                        val readingMinutes = (words / 130).coerceAtLeast(1)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ସମ୍ପୂର୍ଣ୍ଣ ବିବରଣୀ" else "Full Detailed Report",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate700
                            )
                            Text(
                                text = if (language == AppLanguage.ODIA) "$readingMinutes ମିନିଟ୍ ପଠନ ($words ଶବ୍ଦ)" else "$readingMinutes min read ($words words)",
                                fontSize = 10.sp,
                                color = BentoSlate400
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        HighlightedNewsText(
                            text = contentText,
                            query = query,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = BentoSlate800,
                                lineHeight = 22.sp
                            )
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = onShare,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.4f)),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = OceanBlue
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("share_full_news_button_${article.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଏହି ସମ୍ପୂର୍ଣ୍ଣ ଖବର ସେୟାର୍ କରନ୍ତୁ (Share)" else "Share This Full Story via ShareSheet",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = article.source,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BentoSlate400,
                        fontWeight = FontWeight.Medium
                    )
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Full Article View Button
                    if (onArticleClick != null) {
                        Surface(
                            onClick = onArticleClick,
                            shape = RoundedCornerShape(8.dp),
                            color = OceanBlue.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.2f)),
                            modifier = Modifier.testTag("open_news_article_${article.id}")
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = "Read full article",
                                    tint = OceanBlue,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = if (language == AppLanguage.ODIA) "ପଢ଼ନ୍ତୁ" else "Read",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = OceanBlue
                                    )
                                )
                            }
                        }
                    }

                    // Per-Article Offline Download Button (persisted to Room DB)
                    val downloadedNewsIds by com.example.ui.components.BalasoreOfflineAccessManager.downloadedNewsIdsFlow.collectAsStateWithLifecycle()
                    val isDownloadedOffline = downloadedNewsIds.contains(article.id)
                    Surface(
                        onClick = {
                            com.example.util.BalasoreHapticHelper.vibrateToggle(context)
                            coroutineScope.launch {
                                val savedNow = com.example.ui.components.BalasoreOfflineAccessManager.toggleDownloadNewsArticle(
                                    context = context,
                                    article = article
                                )
                                android.widget.Toast.makeText(
                                    context,
                                    if (savedNow) "Downloaded article for offline viewing!" else "Removed from offline downloads",
                                    android.widget.Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isDownloadedOffline) Color(0xFFDCFCE7) else OceanBlue.copy(alpha = 0.08f),
                        border = BorderStroke(
                            1.dp,
                            if (isDownloadedOffline) Color(0xFF86EFAC) else OceanBlue.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.testTag("news_article_offline_download_btn_${article.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (isDownloadedOffline) {
                                    if (language == AppLanguage.ODIA) "✓ ଅଫଲାଇନ୍" else "✓ Offline"
                                } else {
                                    if (language == AppLanguage.ODIA) "⬇ ଅଫଲାଇନ୍" else "⬇ Offline"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDownloadedOffline) Color(0xFF166534) else OceanBlue
                                )
                            )
                        }
                    }

                    // Dedicated Text-To-Speech Listen Button
                    Surface(
                        onClick = {
                            if (isThisArticleSpeaking) {
                                ttsHelper.stop()
                            } else {
                                val tTitle = if (language == AppLanguage.ODIA) article.odiaTitle else article.title
                                val tBody = if (language == AppLanguage.ODIA) {
                                    "${article.odiaSnippet}. ${article.odiaContent.ifBlank { article.odiaSnippet }}"
                                } else {
                                    "${article.snippet}. ${article.content.ifBlank { article.snippet }}"
                                }
                                ttsHelper.speakArticle(
                                    articleId = article.id,
                                    title = tTitle,
                                    content = tBody,
                                    language = language
                                )
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = if (isThisArticleSpeaking) OceanBlue.copy(alpha = 0.2f) else OceanBlue.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, if (isThisArticleSpeaking) OceanBlue else OceanBlue.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 44.dp)
                            .testTag("listen_news_button_${article.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isThisArticleSpeaking) Icons.Default.GraphicEq else Icons.Default.Headphones,
                                contentDescription = if (isThisArticleSpeaking) "Stop Speech" else "Listen to News Audio in ${if (language == AppLanguage.ODIA) "Odia" else "English"}",
                                tint = OceanBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isThisArticleSpeaking) {
                                    if (language == AppLanguage.ODIA) "ବନ୍ଦ (Stop)" else "Stop"
                                } else {
                                    if (language == AppLanguage.ODIA) "ଶୁଣନ୍ତୁ" else "Listen"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlue
                                )
                            )
                        }
                    }

                    // Specialized 'Share via WhatsApp' Button with Pre-Formatted Deep-Link Message
                    Surface(
                        onClick = {
                            com.example.util.ShareHelper.shareNewsViaWhatsApp(
                                context = context,
                                article = article,
                                isOdia = language == AppLanguage.ODIA
                            )
                        },
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFF22C55E)),
                        modifier = Modifier.testTag("whatsapp_share_news_button_${article.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "💬",
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "WhatsApp" else "WhatsApp",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF15803D)
                                )
                            )
                        }
                    }

                    // Dedicated Share Button with Android ShareSheet invocation
                    Surface(
                        onClick = onShare,
                        shape = RoundedCornerShape(8.dp),
                        color = OceanBlue.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.2f)),
                        modifier = Modifier.testTag("share_news_button_${article.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = OceanBlue,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == AppLanguage.ODIA) "ସେୟାର୍" else "Share",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = OceanBlue
                                )
                            )
                        }
                    }

                    // Dedicated Bookmark Button with Room database persistence
                    Surface(
                        onClick = onToggleBookmark,
                        shape = RoundedCornerShape(8.dp),
                        color = if (isBookmarked) OceanBlue.copy(alpha = 0.14f) else BentoSlate100,
                        border = BorderStroke(1.dp, if (isBookmarked) OceanBlue.copy(alpha = 0.4f) else BentoSlate200),
                        modifier = Modifier
                            .defaultMinSize(minWidth = 48.dp, minHeight = 44.dp)
                            .testTag("bookmark_news_button_${article.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (isBookmarked) "Remove from offline bookmarks" else "Save to offline bookmarks",
                                tint = if (isBookmarked) OceanBlue else BentoSlate600,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isBookmarked) {
                                    if (language == AppLanguage.ODIA) "ସାଇତା" else "Saved"
                                } else {
                                    if (language == AppLanguage.ODIA) "ବୁକ୍‌ମାର୍କ" else "Save"
                                },
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isBookmarked) OceanBlue else BentoSlate600,
                                    fontSize = 11.5.sp
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun RiverGaugeItemView(
    gauge: RiverGauge,
    language: AppLanguage
) {
    val progress = (gauge.currentLevelMeters / gauge.dangerLevelMeters).coerceIn(0.0, 1.0).toFloat()
    val statusColor = when (gauge.status.uppercase()) {
        "DANGER" -> Color(0xFFDC2626)
        "ALERT" -> Color(0xFFD97706)
        else -> Color(0xFF16A34A)
    }
    val statusBg = when (gauge.status.uppercase()) {
        "DANGER" -> Color(0xFFFEE2E2)
        "ALERT" -> Color(0xFFFEF3C7)
        else -> Color(0xFFDCFCE7)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(BorderStroke(1.dp, BentoSlate100), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (language == AppLanguage.ODIA) gauge.odiaRiverName else gauge.riverName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900
                        )
                    )
                    Text(
                        text = "Station: ${gauge.stationName}",
                        style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate500)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(statusBg)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = gauge.status,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                fontSize = 10.sp
                            )
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            ShareHelper.shareRiverGaugeAlert(
                                context = context,
                                gauge = gauge,
                                isOdia = language == AppLanguage.ODIA
                            )
                        },
                        modifier = Modifier
                            .size(30.dp)
                            .testTag("river_gauge_share_${gauge.riverName.lowercase()}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share River Flood Alert via ShareSheet",
                            tint = OceanBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = BentoSlate100
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Current: ${gauge.currentLevelMeters}m",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate800
                    )
                )
                Text(
                    text = "Warning: ${gauge.warningLevelMeters}m",
                    style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate500)
                )
                Text(
                    text = "Danger: ${gauge.dangerLevelMeters}m",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFDC2626)
                    )
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Upstream Flow: ${gauge.trend}",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = OceanBlueDark,
                    fontSize = 10.sp
                )
            )
        }
    }
}

@Composable
fun CycloneSheltersView(
    shelters: List<CycloneShelter>,
    language: AppLanguage
) {
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEFF6FF))
            .clickable { expanded = !expanded }
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Shelters",
                    tint = OceanBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (language == AppLanguage.ODIA) "ବାତ୍ୟା ଓ ବନ୍ୟା ଆଶ୍ରୟସ୍ଥଳୀ (Cyclone Shelters)" else "District Cyclone & Flood Shelters",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = OceanBlueDark
                    )
                )
            }

            Icon(
                imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = "Expand",
                tint = OceanBlue,
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(top = 10.dp)) {
                shelters.forEach { shelter ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = shelter.name,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate900
                                )
                            )
                            Text(
                                text = "${shelter.block} • Cap: ${shelter.capacityPeople} people",
                                style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate600)
                            )
                        }

                        IconButton(
                            onClick = {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${shelter.phone}"))
                                context.startActivity(intent)
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Call Shelter",
                                tint = EmeraldGreen,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
