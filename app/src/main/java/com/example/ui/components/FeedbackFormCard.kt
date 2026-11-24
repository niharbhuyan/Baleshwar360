package com.example.ui.components

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.firebase.FirestoreService
import com.example.data.local.AppDatabase
import com.example.data.local.ReviewEntity
import com.example.data.model.AppLanguage
import com.example.data.model.FeedbackReport
import com.example.data.model.FeedbackType
import com.example.data.repository.BalasoreRepository
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate200
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate800
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import kotlinx.coroutines.launch

enum class BalasoreFeedbackPurpose(
    val id: String,
    val labelEn: String,
    val labelOr: String,
    val emoji: String,
    val defaultFeedbackType: FeedbackType,
    val defaultCategory: String,
    val targetTypeKey: String
) {
    NEWS_ARTICLE(
        id = "news_article",
        labelEn = "News Article Feedback",
        labelOr = "ଖବର ଉପରେ ମତାମତ",
        emoji = "📰",
        defaultFeedbackType = FeedbackType.CONTENT_CORRECTION,
        defaultCategory = "App Features & Feedback",
        targetTypeKey = "NEWS_FEEDBACK"
    ),
    SUGGEST_HOTSPOT(
        id = "suggest_hotspot",
        labelEn = "Suggest Tourism Hotspot",
        labelOr = "ନୂତନ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ପ୍ରସ୍ତାବ",
        emoji = "🏖️",
        defaultFeedbackType = FeedbackType.SUGGESTION,
        defaultCategory = "Tourism & Beaches",
        targetTypeKey = "HOTSPOT_SUGGESTION"
    ),
    REPORT_ISSUE(
        id = "report_issue",
        labelEn = "Report Issue / Bug",
        labelOr = "ସମସ୍ୟା ବା ତ୍ରୁଟି ଜଣାନ୍ତୁ",
        emoji = "🛠️",
        defaultFeedbackType = FeedbackType.BUG_REPORT,
        defaultCategory = "Civic & Roads",
        targetTypeKey = "ISSUE_REPORT"
    )
}

/**
 * Simple Feedback Form Component that allows users to submit feedback on news articles,
 * suggest new tourism hotspots, and report issues they encounter, storing them in
 * both Room (ReviewDao) and Firestore ('feedback' collection) for review and implementation.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeedbackFormCard(
    language: AppLanguage = AppLanguage.ENGLISH,
    modifier: Modifier = Modifier,
    onSubmittedSuccessfully: ((FeedbackReport) -> Unit)? = null
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firestoreService = remember { FirestoreService(context.applicationContext) }
    val db = remember { AppDatabase.getInstance(context) }
    val reviewDao = remember { db.reviewDao() }
    val storedReviews by reviewDao.getAllReviews().collectAsStateWithLifecycle(initialValue = emptyList())

    var selectedPurpose by remember { mutableStateOf(BalasoreFeedbackPurpose.SUGGEST_HOTSPOT) }
    var selectedNewsArticleTitle by remember {
        mutableStateOf(BalasoreRepository.newsArticles.firstOrNull()?.title ?: "Chandipur Marine Drive Coastal Upgrade")
    }
    var reviewFilterType by remember { mutableStateOf("ALL") }

    var feedbackType by remember { mutableStateOf(FeedbackType.SUGGESTION) }
    var selectedCategory by remember { mutableStateOf("Tourism & Beaches") }
    var titleText by remember { mutableStateOf("") }
    var descriptionText by remember { mutableStateOf("") }
    var userNameText by remember { mutableStateOf("") }
    var contactInfoText by remember { mutableStateOf("") }
    var localityText by remember { mutableStateOf("Balasore Town") }
    var ratingValue by remember { mutableIntStateOf(5) }

    var isSubmitting by remember { mutableStateOf(false) }
    var submissionSuccessId by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val categories = remember {
        listOf(
            "Civic & Roads",
            "Tourism & Beaches",
            "Sanitation & Waste",
            "Heritage & Culture",
            "Healthcare & Hospitals",
            "App Features & Feedback",
            "Disaster & Coastal Safety"
        )
    }

    val localities = remember {
        listOf(
            "Balasore Town",
            "Chandipur",
            "Remuna",
            "Bhograi",
            "Baliapal",
            "Nilagiri",
            "Jaleswar",
            "Soro",
            "Bahanaga"
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("feedback_form_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.2.dp, BentoSlate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = OceanBlue.copy(alpha = 0.12f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (feedbackType == FeedbackType.REPORT) Icons.Default.ReportProblem else Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = if (feedbackType == FeedbackType.REPORT) Color(0xFFE11D48) else OceanBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == AppLanguage.ODIA) "ମତାମତ ଓ ପରାମର୍ଶ ଫର୍ମ" else "Feedback & Suggestions",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = BentoSlate900
                            )
                        )
                        Text(
                            text = if (language == AppLanguage.ODIA) "ସିଧାସଳଖ ଫାୟାରଷ୍ଟୋର ସଂଗ୍ରହ 'feedback'ରେ ସଂରକ୍ଷିତ ହେବ" else "Synced directly to Firestore collection 'feedback'",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = BentoSlate600,
                                fontSize = 11.sp
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FIRESTORE",
                            color = EmeraldGreen,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Success Confirmation Banner
            AnimatedVisibility(visible = submissionSuccessId != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFECFDF5),
                    border = BorderStroke(1.dp, Color(0xFFA7F3D0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("feedback_success_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldGreen,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (language == AppLanguage.ODIA) "ଧନ୍ୟବାଦ! ଆପଣଙ୍କ ମତାମତ ସଫଳତାର ସହିତ ଜମା ହୋଇଛି।" else "Thank you! Your feedback has been saved to Firestore.",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                color = Color(0xFF065F46)
                            )
                            Text(
                                text = "Doc ID: ${submissionSuccessId?.take(18)}... • Collection: feedback",
                                fontSize = 10.5.sp,
                                color = Color(0xFF047857)
                            )
                        }
                        IconButton(
                            onClick = { submissionSuccessId = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Text("✕", fontSize = 12.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Error Banner
            AnimatedVisibility(visible = errorMessage != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEF2F2),
                    border = BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                ) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFF991B1B),
                        fontSize = 11.5.sp,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Feedback Type Selector Chips (Feedback, Bug Report, Suggestion, Feature Request)
            var bugSeverity by remember { mutableStateOf("Medium") }
            val recentLocalSubmissions = remember { androidx.compose.runtime.mutableStateListOf<FeedbackReport>() }

            // Dedicated 3-Mode Selector: 1) News Article Feedback, 2) Suggest Tourism Hotspot, 3) Report Issue
            Text(
                text = if (language == AppLanguage.ODIA) "ଆପଣ କ’ଣ କରିବାକୁ ଚାହୁଁଛନ୍ତି? (Select Purpose):" else "What would you like to submit?",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = BentoSlate900
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feedback_purpose_selector"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BalasoreFeedbackPurpose.values().forEach { purpose ->
                    val isPurposeSelected = selectedPurpose == purpose
                    Surface(
                        onClick = {
                            selectedPurpose = purpose
                            feedbackType = purpose.defaultFeedbackType
                            selectedCategory = purpose.defaultCategory
                            if (purpose == BalasoreFeedbackPurpose.NEWS_ARTICLE && titleText.isBlank()) {
                                titleText = "Feedback on: $selectedNewsArticleTitle"
                            }
                        },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isPurposeSelected) OceanBlueDark else BentoSlate100,
                        border = BorderStroke(1.dp, if (isPurposeSelected) OceanBlueDark else BentoSlate200),
                        modifier = Modifier.testTag("feedback_purpose_${purpose.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${purpose.emoji} ${if (language == AppLanguage.ODIA) purpose.labelOr else purpose.labelEn}",
                                color = if (isPurposeSelected) Color.White else BentoSlate800,
                                fontSize = 11.5.sp,
                                fontWeight = if (isPurposeSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Contextual News Article Selector when NEWS_ARTICLE mode is active
            AnimatedVisibility(visible = selectedPurpose == BalasoreFeedbackPurpose.NEWS_ARTICLE) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OceanBlue.copy(alpha = 0.07f),
                    border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .testTag("feedback_news_article_picker_box")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "📰 କେଉଁ ଖବର ଉପରେ ମତାମତ ଦେବାକୁ ଚାହୁଁଛନ୍ତି ବାଛନ୍ତୁ:"
                            else "📰 Select Balasore News Article to Review:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlueDark
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            BalasoreRepository.newsArticles.take(4).forEachIndexed { idx, article ->
                                val isArtSelected = selectedNewsArticleTitle == article.title
                                FilterChip(
                                    selected = isArtSelected,
                                    onClick = {
                                        selectedNewsArticleTitle = article.title
                                        titleText = "Feedback on: ${article.title.take(48)}"
                                    },
                                    label = {
                                        Text(
                                            text = article.title.take(34) + if (article.title.length > 34) "..." else "",
                                            fontSize = 10.5.sp
                                        )
                                    },
                                    modifier = Modifier.testTag("feedback_pick_news_article_$idx")
                                )
                            }
                        }
                    }
                }
            }

            // Contextual Hotspot Suggestion Helper Banner when SUGGEST_HOTSPOT mode is active
            AnimatedVisibility(visible = selectedPurpose == BalasoreFeedbackPurpose.SUGGEST_HOTSPOT) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFFBBF7D0)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .testTag("feedback_hotspot_suggestion_helper_box")
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (language == AppLanguage.ODIA) "🏖️ ନୂତନ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ପ୍ରସ୍ତାବ (Suggest a New Balasore Hotspot)"
                            else "🏖️ Suggest a New Tourism Hotspot in Balasore",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF166534)
                        )
                        Text(
                            text = if (language == AppLanguage.ODIA)
                                "ସ୍ଥାନର ନାମ, ବ୍ଲକ୍/ଅଞ୍ଚଳ ଏବଂ କାହିଁକି ପର୍ଯ୍ୟଟକମାନେ ଏଠାକୁ ଆସିବେ ତଳେ ଲେଖନ୍ତୁ।"
                            else
                                "Enter the proposed hotspot name below, select its block/locality, and describe its cultural or natural significance for review.",
                            fontSize = 10.5.sp,
                            color = Color(0xFF15803D)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (language == AppLanguage.ODIA) "ମତାମତ ପ୍ରକାର ଚୟନ କରନ୍ତୁ (Feedback / Bug Report / Suggestion):" else "Select Submission Type (Feedback, Bug Report, Suggestion):",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate700
                )
            )
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feedback_type_selector"),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FeedbackType.values().forEach { type ->
                    val isSelected = feedbackType == type
                    Surface(
                        onClick = { feedbackType = type },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) OceanBlue else BentoSlate100,
                        border = BorderStroke(1.dp, if (isSelected) OceanBlue else BentoSlate200),
                        modifier = Modifier.testTag("feedback_type_${type.name.lowercase()}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${type.iconEmoji} ${type.getDisplayName(language)}",
                                color = if (isSelected) Color.White else BentoSlate800,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }

            // Bug Severity selector when Bug Report or Issue Report is active
            AnimatedVisibility(visible = feedbackType == FeedbackType.BUG_REPORT || feedbackType == FeedbackType.REPORT) {
                Column(modifier = Modifier.padding(top = 10.dp)) {
                    Text(
                        text = if (language == AppLanguage.ODIA) "ତ୍ରୁଟି ତীব্রତା (Bug Severity):" else "Bug / Issue Severity:",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB91C1C)
                        )
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf("Low", "Medium", "High", "Critical").forEach { sev ->
                            val isSev = bugSeverity == sev
                            FilterChip(
                                selected = isSev,
                                onClick = { bugSeverity = sev },
                                label = { Text(sev, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFFFEE2E2),
                                    selectedLabelColor = Color(0xFF991B1B)
                                )
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Experience Rating (1-5 Stars)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (language == AppLanguage.ODIA) "ଆପ୍ ଅନୁଭୂତି ରେଟିଂ:" else "Overall Experience Rating:",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = BentoSlate700
                    )
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    for (star in 1..5) {
                        IconButton(
                            onClick = { ratingValue = star },
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("feedback_star_$star")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "$star Stars",
                                tint = if (star <= ratingValue) AmberGold else BentoSlate200,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Category Selector Chips
            Text(
                text = if (language == AppLanguage.ODIA) "ବର୍ଗ / ବିଭାଗ:" else "Category / App Module:",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate700
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                categories.forEach { cat ->
                    val isSelected = selectedCategory == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedCategory = cat },
                        label = { Text(cat, fontSize = 10.5.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanBlue.copy(alpha = 0.15f),
                            selectedLabelColor = OceanBlue,
                            containerColor = BentoSlate100,
                            labelColor = BentoSlate700
                        ),
                        border = BorderStroke(1.dp, if (isSelected) OceanBlue else BentoSlate200)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title Input
            OutlinedTextField(
                value = titleText,
                onValueChange = {
                    titleText = it
                    errorMessage = null
                },
                label = {
                    Text(
                        when (feedbackType) {
                            FeedbackType.BUG_REPORT -> if (language == AppLanguage.ODIA) "ତ୍ରୁଟି ଶୀର୍ଷକ (Bug Summary)" else "Bug Summary (e.g., Map marker delay)"
                            FeedbackType.SUGGESTION -> if (language == AppLanguage.ODIA) "ପରାମର୍ଶ ଶୀର୍ଷକ" else "Suggestion Title (e.g., Add Nilagiri trek route)"
                            else -> if (language == AppLanguage.ODIA) "ଶୀର୍ଷକ (ଉଦାହରଣ: ଚାନ୍ଦିପୁର ରାସ୍ତା ସଫେଇ)" else "Title / Subject"
                        },
                        fontSize = 12.sp
                    )
                },
                placeholder = { Text("Brief one-line summary...", fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feedback_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OceanBlue,
                    unfocusedBorderColor = BentoSlate200
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Detailed Description
            OutlinedTextField(
                value = descriptionText,
                onValueChange = {
                    descriptionText = it
                    errorMessage = null
                },
                label = {
                    Text(
                        when (feedbackType) {
                            FeedbackType.BUG_REPORT -> if (language == AppLanguage.ODIA) "ତ୍ରୁଟିର ବିବରଣୀ ଓ ପଦକ୍ଷେପ" else "Steps to Reproduce / Bug Details"
                            FeedbackType.SUGGESTION -> if (language == AppLanguage.ODIA) "ଆପଣଙ୍କ ପରାମର୍ଶ ବିବରଣୀ" else "Your Suggestion / Feature Idea"
                            else -> if (language == AppLanguage.ODIA) "ବିସ୍ତୃତ ବିବରଣୀ / ପରାମର୍ଶ" else "Detailed Feedback / Comments"
                        },
                        fontSize = 12.sp
                    )
                },
                placeholder = {
                    Text(
                        if (language == AppLanguage.ODIA) "ଆପଣଙ୍କ ଅଭିଜ୍ଞତା, ସମସ୍ୟାର ସ୍ଥାନ ବା ସୁଧାର ପ୍ରସ୍ତାବ ଲେଖନ୍ତୁ..."
                        else "Please provide specific details, steps, or ideas for improvement...",
                        fontSize = 12.sp
                    )
                },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feedback_description_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OceanBlue,
                    unfocusedBorderColor = BentoSlate200
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Locality & Optional Contact Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = localityText,
                    onValueChange = { localityText = it },
                    label = { Text("Locality / Block", fontSize = 11.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.LocationOn, contentDescription = null, tint = BentoSlate600, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feedback_locality_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OceanBlue,
                        unfocusedBorderColor = BentoSlate200
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                OutlinedTextField(
                    value = userNameText,
                    onValueChange = { userNameText = it },
                    label = { Text("Your Name (Optional)", fontSize = 11.sp) },
                    singleLine = true,
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = BentoSlate600, modifier = Modifier.size(16.dp))
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("feedback_name_input"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = OceanBlue,
                        unfocusedBorderColor = BentoSlate200
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Contact Email/Phone
            OutlinedTextField(
                value = contactInfoText,
                onValueChange = { contactInfoText = it },
                label = { Text("Contact Email / Phone (Optional, for status updates)", fontSize = 11.sp) },
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = BentoSlate600, modifier = Modifier.size(16.dp))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("feedback_contact_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = OceanBlue,
                    unfocusedBorderColor = BentoSlate200
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Submit Button
            Button(
                onClick = {
                    if (titleText.trim().isEmpty()) {
                        errorMessage = if (language == AppLanguage.ODIA) "ଦୟାକରି ଶୀର୍ଷକ ପ୍ରଦାନ କରନ୍ତୁ" else "Please provide a title for your feedback, bug report, or suggestion."
                        return@Button
                    }
                    if (descriptionText.trim().isEmpty()) {
                        errorMessage = if (language == AppLanguage.ODIA) "ଦୟାକରି ବିସ୍ତୃତ ବିବରଣୀ ଲେଖନ୍ତୁ" else "Please write a detailed description."
                        return@Button
                    }

                    val enrichedDesc = when {
                        feedbackType == FeedbackType.BUG_REPORT || feedbackType == FeedbackType.REPORT || selectedPurpose == BalasoreFeedbackPurpose.REPORT_ISSUE ->
                            "[Issue Severity: $bugSeverity] ${descriptionText.trim()}"
                        selectedPurpose == BalasoreFeedbackPurpose.NEWS_ARTICLE ->
                            "[News Article: $selectedNewsArticleTitle] ${descriptionText.trim()}"
                        selectedPurpose == BalasoreFeedbackPurpose.SUGGEST_HOTSPOT ->
                            "[Proposed Tourism Hotspot • Block: ${localityText.trim()}] ${descriptionText.trim()}"
                        else -> descriptionText.trim()
                    }

                    val report = FeedbackReport(
                        type = feedbackType,
                        category = selectedCategory,
                        title = titleText.trim(),
                        description = enrichedDesc,
                        userName = userNameText.trim().ifEmpty { "Balasore Citizen" },
                        contactInfo = contactInfoText.trim(),
                        locality = localityText.trim(),
                        rating = ratingValue
                    )

                    isSubmitting = true
                    errorMessage = null

                    coroutineScope.launch {
                        // 1. Store locally in Room ReviewDao for persistent offline review & implementation tracking
                        try {
                            val insertedId = reviewDao.insertReview(
                                ReviewEntity(
                                    targetType = selectedPurpose.targetTypeKey,
                                    targetId = report.id,
                                    targetTitle = report.title,
                                    userId = report.contactInfo.ifBlank { "citizen_local" },
                                    userName = report.userName,
                                    userLocality = report.locality,
                                    rating = report.rating,
                                    comment = "[${selectedCategory}] ${report.description}",
                                    timestamp = System.currentTimeMillis()
                                )
                            )
                            com.example.ui.features.admin.ContentModerationRepository.registerSubmittedReview(
                                context = context,
                                reviewDbId = insertedId,
                                targetType = selectedPurpose.targetTypeKey,
                                targetId = report.id,
                                targetTitle = report.title,
                                userId = report.contactInfo.ifBlank { "citizen_local" },
                                userName = report.userName,
                                rating = report.rating,
                                comment = "[${selectedCategory}] ${report.description}"
                            )
                        } catch (_: Exception) {
                        }

                        // 2. Store in Cloud Firestore ('feedback' collection)
                        val result = firestoreService.submitFeedback(report)
                        isSubmitting = false
                        if (result.isSuccess) {
                            submissionSuccessId = result.getOrNull() ?: report.id
                            recentLocalSubmissions.add(0, report)
                            Toast.makeText(
                                context,
                                if (language == AppLanguage.ODIA) "ମତାମତ ସଫଳତାର ସହିତ ସଂରକ୍ଷିତ!" else "${feedbackType.labelEn} submitted & saved for review!",
                                Toast.LENGTH_SHORT
                            ).show()
                            titleText = ""
                            descriptionText = ""
                            contactInfoText = ""
                            onSubmittedSuccessfully?.invoke(report)
                        } else {
                            errorMessage = result.exceptionOrNull()?.message ?: "Submission failed. Please try again."
                        }
                    }
                },
                enabled = !isSubmitting,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("feedback_submit_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = OceanBlue)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Submitting...", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(imageVector = Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == AppLanguage.ODIA) "ମତାମତ ଜମା କରନ୍ତୁ" else "Submit ${feedbackType.labelEn}",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (recentLocalSubmissions.isNotEmpty() || storedReviews.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = BentoSlate200)
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA)
                            "ସମୀକ୍ଷାଧୀନ ମତାମତ ଓ ପ୍ରସ୍ତାବ (${recentLocalSubmissions.size + storedReviews.size}):"
                        else
                            "Stored Feedback & Suggestions Under Review (${recentLocalSubmissions.size + storedReviews.size}):",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = BentoSlate900
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    listOf(
                        "ALL" to "All Stored",
                        "NEWS_FEEDBACK" to "📰 News Feedback",
                        "HOTSPOT_SUGGESTION" to "🏖️ Hotspot Suggestions",
                        "ISSUE_REPORT" to "🛠️ Reported Issues"
                    ).forEach { (filterKey, label) ->
                        FilterChip(
                            selected = reviewFilterType == filterKey,
                            onClick = { reviewFilterType = filterKey },
                            label = { Text(label, fontSize = 10.sp) },
                            modifier = Modifier.testTag("stored_feedback_filter_${filterKey.lowercase()}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("stored_feedback_review_list"),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    recentLocalSubmissions.take(3).forEach { item ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoSlate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${item.type.iconEmoji} [${item.type.labelEn}] ${item.title}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = BentoSlate900
                                    )
                                    Text(
                                        text = item.description,
                                        fontSize = 11.sp,
                                        color = BentoSlate600,
                                        maxLines = 2
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFDCFCE7)
                                ) {
                                    Text(
                                        text = "✓ ${item.status}",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF166534),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    val filteredStored = storedReviews.filter { rev ->
                        reviewFilterType == "ALL" || rev.targetType.equals(reviewFilterType, ignoreCase = true)
                    }
                    filteredStored.take(4).forEach { rev ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BentoSlate100,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "[${rev.targetType.replace("_", " ")}] ${rev.targetTitle}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.5.sp,
                                        color = BentoSlate900
                                    )
                                    Text(
                                        text = "${rev.comment} • by ${rev.userName} (${rev.userLocality})",
                                        fontSize = 10.5.sp,
                                        color = BentoSlate600,
                                        maxLines = 2
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = "UNDER REVIEW",
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
