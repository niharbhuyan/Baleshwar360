package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.data.fcm.FcmManager
import com.example.data.model.AppLanguage
import com.example.data.model.Hotspot
import com.example.data.model.NewsArticle
import com.example.data.recommendation.BalasoreRecommendationEngine
import com.example.data.repository.BalasoreRepository
import com.example.ui.theme.*

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PersonalizedRecommendationsCard(
    language: AppLanguage = AppLanguage.ENGLISH,
    externalSavedSpotIds: Set<String> = emptySet(),
    externalVisitedSpotIds: Set<String> = emptySet(),
    externalSavedNewsIds: Set<String> = emptySet(),
    onOpenHotspot: (Hotspot) -> Unit = {},
    onToggleSaveHotspot: (Hotspot) -> Unit = {},
    onOpenNewsArticle: (NewsArticle) -> Unit = {},
    onToggleSaveArticle: (NewsArticle) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA

    LaunchedEffect(Unit) {
        BalasoreRecommendationEngine.initialize(context)
    }

    val behaviorProfile by BalasoreRecommendationEngine.behaviorState.collectAsStateWithLifecycle()
    var selectedEngineTab by remember { mutableIntStateOf(0) } // 0: Hotspots, 1: News, 2: Weather Alerts
    var readerArticleModal by remember { mutableStateOf<NewsArticle?>(null) }
    var subscribedWeatherAlertIds by remember {
        mutableStateOf(setOf("rec_wx_chandipur_tide", "rec_wx_subarnarekha_flood"))
    }

    val recommendedSpots = remember(behaviorProfile, externalSavedSpotIds, externalVisitedSpotIds) {
        BalasoreRecommendationEngine.getRecommendedHotspots(
            context = context,
            allHotspots = BalasoreRepository.hotspots,
            externalSavedIds = externalSavedSpotIds,
            externalVisitedIds = externalVisitedSpotIds,
            limit = 4
        )
    }

    val recommendedArticles = remember(behaviorProfile, externalSavedNewsIds) {
        BalasoreRecommendationEngine.getRecommendedArticles(
            context = context,
            allArticles = BalasoreRepository.newsArticles,
            externalSavedNewsIds = externalSavedNewsIds,
            limit = 4
        )
    }

    val recommendedWeatherAlerts = remember(behaviorProfile, externalSavedSpotIds) {
        BalasoreRecommendationEngine.getRecommendedWeatherAlerts(
            context = context,
            externalSavedLocationIds = externalSavedSpotIds,
            limit = 4
        )
    }

    val topAffinityTags = remember(behaviorProfile) {
        BalasoreRecommendationEngine.getTopAffinityTags(context)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("personalized_recommendations_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, OceanBlue.copy(alpha = 0.28f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Banner
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
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(OceanBlue, Color(0xFF0D9488))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "Recommendation Engine",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (isOdia) "✨ ଆପଣଙ୍କ ପାଇଁ ବ୍ୟକ୍ତିଗତ ପରାମର୍ଶ (For You)" else "✨ Recommended For You",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.5.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOdia)
                                "କଣ୍ଟେଣ୍ଟ-ବେସଡ୍ ଫିଲ୍ଟରିଂ: ଖବର, ପାଣିପାଗ ସତର୍କତା ଓ ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ"
                            else
                                "Content-Based Filtering • Read (${behaviorProfile.viewedArticleIds.size}) • Saved (${(behaviorProfile.savedLocationIds + externalSavedSpotIds).size})",
                            fontSize = 11.sp,
                            color = BentoSlate600
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFF0FDF4),
                    border = BorderStroke(1.dp, Color(0xFF86EFAC))
                ) {
                    Text(
                        text = if (isOdia) "ସ୍ମାର୍ଟ ଇଞ୍ଜିନ୍" else "CONTENT FILTER",
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF166534),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Behavioral Affinity Tags Strip
            if (topAffinityTags.isNotEmpty()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = OceanBlue.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = if (isOdia) "ଆପଣଙ୍କ ଆଗ୍ରହ:" else "Your Taste Profile:",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                    items(topAffinityTags) { (tag, score) ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
                        ) {
                            Text(
                                text = "#$tag ($score)",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3-Tab Mode Switcher: Hotspots | News | Weather Alerts
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
                    .padding(4.dp)
            ) {
                val tabs = listOf(
                    0 to (if (isOdia) "📍 ପର୍ଯ୍ୟଟନ (${recommendedSpots.size})" else "📍 Hotspots (${recommendedSpots.size})"),
                    1 to (if (isOdia) "📰 ଖବର (${recommendedArticles.size})" else "📰 News (${recommendedArticles.size})"),
                    2 to (if (isOdia) "⛈️ ପାଣିପାଗ (${recommendedWeatherAlerts.size})" else "⛈️ Weather (${recommendedWeatherAlerts.size})")
                )
                tabs.forEach { (idx, label) ->
                    val isSelected = selectedEngineTab == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(9.dp))
                            .background(if (isSelected) OceanBlue else Color.Transparent)
                            .clickable { selectedEngineTab = idx }
                            .padding(vertical = 8.dp)
                            .testTag("recommendation_tab_$idx"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Content Area
            when (selectedEngineTab) {
                0 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recommendedSpots.forEach { rec ->
                            val isSaved = behaviorProfile.savedLocationIds.contains(rec.hotspot.id) ||
                                externalSavedSpotIds.contains(rec.hotspot.id)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.16f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        BalasoreRecommendationEngine.recordLocationViewed(context, rec.hotspot)
                                        onOpenHotspot(rec.hotspot)
                                    }
                                    .testTag("recommended_hotspot_card_${rec.hotspot.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFDCFCE7)
                                            ) {
                                                Text(
                                                    text = "${rec.matchScorePercent}% Match",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF166534),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "${rec.hotspot.category} • ⭐ ${rec.hotspot.rating}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = OceanBlue
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isOdia) rec.hotspot.odiaName else rec.hotspot.name,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = if (isOdia) rec.reasonOdia else rec.reasonEn,
                                            fontSize = 11.sp,
                                            color = BentoSlate600,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val nowSaved = !isSaved
                                            BalasoreRecommendationEngine.recordLocationSaved(
                                                context = context,
                                                spotId = rec.hotspot.id,
                                                spotName = rec.hotspot.name,
                                                category = rec.hotspot.category,
                                                location = rec.hotspot.location,
                                                isSaved = nowSaved
                                            )
                                            onToggleSaveHotspot(rec.hotspot)
                                            Toast.makeText(
                                                context,
                                                if (nowSaved) "Saved ${rec.hotspot.name}! Recommendations updated."
                                                else "Removed ${rec.hotspot.name} from saved locations",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.testTag("rec_save_spot_btn_${rec.hotspot.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = "Save Recommended Spot",
                                            tint = if (isSaved) Color(0xFFE11D48) else OceanBlue
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                1 -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recommendedArticles.forEach { rec ->
                            val isSaved = behaviorProfile.savedArticleIds.contains(rec.article.id) ||
                                externalSavedNewsIds.contains(rec.article.id)
                            val isViewed = behaviorProfile.viewedArticleIds.contains(rec.article.id)
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                                border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.16f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        BalasoreRecommendationEngine.recordArticleViewed(context, rec.article)
                                        readerArticleModal = rec.article
                                        onOpenNewsArticle(rec.article)
                                    }
                                    .testTag("recommended_article_card_${rec.article.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Color(0xFFE0F2FE)
                                            ) {
                                                Text(
                                                    text = "${rec.matchScorePercent}% Match",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = Color(0xFF0369A1),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "${rec.article.category} • ${rec.article.timeAgo}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = OceanBlue
                                            )
                                            if (isViewed) {
                                                Text(
                                                    text = "• Read",
                                                    fontSize = 10.sp,
                                                    color = EmeraldGreen,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isOdia && rec.article.odiaTitle.isNotBlank()) rec.article.odiaTitle else rec.article.title,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isOdia) rec.reasonOdia else rec.reasonEn,
                                            fontSize = 11.sp,
                                            color = BentoSlate600,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val nowSaved = !isSaved
                                            BalasoreRecommendationEngine.recordArticleSaved(
                                                context = context,
                                                articleId = rec.article.id,
                                                title = rec.article.title,
                                                category = rec.article.category,
                                                isSaved = nowSaved
                                            )
                                            onToggleSaveArticle(rec.article)
                                            Toast.makeText(
                                                context,
                                                if (nowSaved) "Article saved! Recommendations updated."
                                                else "Removed from saved articles",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.testTag("rec_save_article_btn_${rec.article.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "Bookmark Recommended Article",
                                            tint = if (isSaved) OceanBlue else BentoSlate600
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                else -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        recommendedWeatherAlerts.forEach { wx ->
                            val isAlertSubscribed = subscribedWeatherAlertIds.contains(wx.id)
                            val severityColor = when (wx.severity) {
                                "HIGH" -> Color(0xFFDC2626)
                                "MODERATE" -> Color(0xFFD97706)
                                else -> Color(0xFF0284C7)
                            }
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.32f),
                                border = BorderStroke(1.dp, severityColor.copy(alpha = 0.25f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("recommended_weather_alert_card_${wx.id}")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = severityColor.copy(alpha = 0.14f)
                                            ) {
                                                Text(
                                                    text = "${wx.matchScorePercent}% Match • ${wx.severity}",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = severityColor,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Text(
                                                text = "${wx.alertCategory} • ${wx.locality}",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = OceanBlue
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = if (isOdia) wx.titleOdia else wx.titleEn,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.5.sp
                                        )
                                        Text(
                                            text = if (isOdia) wx.advisoryOdia else wx.advisoryEn,
                                            fontSize = 11.5.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (isOdia) wx.reasonOdia else wx.reasonEn,
                                            fontSize = 10.5.sp,
                                            color = EmeraldGreen,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            val nowSubscribed = !isAlertSubscribed
                                            subscribedWeatherAlertIds = if (nowSubscribed) {
                                                subscribedWeatherAlertIds + wx.id
                                            } else {
                                                subscribedWeatherAlertIds - wx.id
                                            }
                                            if (nowSubscribed) {
                                                FcmManager.triggerSampleWeatherAlert(context)
                                            }
                                            Toast.makeText(
                                                context,
                                                if (nowSubscribed) "Subscribed to ${wx.locality} Weather Alerts!"
                                                else "Muted ${wx.locality} Weather Alert",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                        modifier = Modifier.testTag("rec_toggle_weather_alert_btn_${wx.id}")
                                    ) {
                                        Icon(
                                            imageVector = if (isAlertSubscribed) Icons.Default.NotificationsActive else Icons.Default.NotificationsNone,
                                            contentDescription = "Toggle Weather Alert",
                                            tint = if (isAlertSubscribed) severityColor else BentoSlate600
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

    // Quick Article Reader Modal when user taps a recommended article
    if (readerArticleModal != null) {
        val art = readerArticleModal!!
        AlertDialog(
            onDismissRequest = { readerArticleModal = null },
            title = {
                Column {
                    Text(
                        text = if (isOdia && art.odiaTitle.isNotBlank()) art.odiaTitle else art.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${art.category} • ${art.source} • ${art.timeAgo}",
                        fontSize = 11.5.sp,
                        color = OceanBlue
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = art.content.ifBlank { art.snippet },
                        fontSize = 13.5.sp,
                        lineHeight = 20.sp
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFF0FDF4),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "✓ Recorded in your Reading Profile — future recommendations are now tuned to ${art.category}.",
                            fontSize = 11.sp,
                            color = Color(0xFF166534),
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        BalasoreRecommendationEngine.recordArticleSaved(
                            context = context,
                            articleId = art.id,
                            title = art.title,
                            category = art.category,
                            isSaved = true
                        )
                        onToggleSaveArticle(art)
                        Toast.makeText(context, "Saved to your Profile Articles!", Toast.LENGTH_SHORT).show()
                        readerArticleModal = null
                    }
                ) {
                    Icon(Icons.Default.BookmarkAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isOdia) "ସେଭ୍ କରନ୍ତୁ" else "Save Article")
                }
            },
            dismissButton = {
                TextButton(onClick = { readerArticleModal = null }) {
                    Text(if (isOdia) "ବନ୍ଦ କରନ୍ତୁ" else "Close")
                }
            }
        )
    }
}
