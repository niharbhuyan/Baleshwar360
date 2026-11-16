package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.SavedBookmarkEntity
import com.example.data.model.AppLanguage
import com.example.data.model.NewsArticle
import com.example.util.ShareHelper
import com.example.ui.theme.*

/**
 * 'My Saved' Bookmarks Interface.
 * Allows users to access bookmarked news articles and saved emergency directory contacts
 * stored persistently in the Room Database, with 1-tap emergency calling and offline reading.
 */
@Composable
fun MySavedBookmarksSection(
    savedBookmarks: List<SavedBookmarkEntity>,
    language: AppLanguage,
    onArticleClick: (NewsArticle) -> Unit,
    onRemoveBookmark: (String) -> Unit,
    onCallEmergency: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "ARTICLES", "EMERGENCY"
    val isOdia = language == AppLanguage.ODIA

    val articlesCount = remember(savedBookmarks) { savedBookmarks.count { it.itemType == "NEWS" } }
    val emergencyCount = remember(savedBookmarks) { savedBookmarks.count { it.itemType == "EMERGENCY" } }

    val filteredList = remember(savedBookmarks, selectedFilter) {
        when (selectedFilter) {
            "ARTICLES" -> savedBookmarks.filter { it.itemType == "NEWS" }
            "EMERGENCY" -> savedBookmarks.filter { it.itemType == "EMERGENCY" }
            else -> savedBookmarks
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("my_saved_bookmarks_section")
    ) {
        // Header & Metric Strip
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
            border = BorderStroke(1.dp, BentoSlate200)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = OceanBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = OceanBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (isOdia) "ମୋର ସଂରକ୍ଷିତ ତାଲିକା (ରୁମ୍ ଡାଟାବେସ୍)" else "My Saved Collection",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BentoSlate900,
                                    fontSize = 15.sp
                                )
                            )
                            Text(
                                text = if (isOdia) "ଅଫଲାଇନ୍ ଖବର ଓ ତ୍ୱରିତ ଜରୁରୀକାଳୀନ ଡାଏଲ୍" else "Offline Articles & Quick-Dial Emergency Directory",
                                style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate500)
                            )
                        }
                    }

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color(0xFFDCFCE7),
                        border = BorderStroke(1.dp, Color(0xFF86EFAC))
                    ) {
                        Text(
                            text = "${savedBookmarks.size} Saved",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF15803D),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == "ALL",
                        onClick = { selectedFilter = "ALL" },
                        label = {
                            Text(
                                text = if (isOdia) "ସମସ୍ତ (${savedBookmarks.size})" else "All (${savedBookmarks.size})",
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedFilter == "ALL") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("saved_filter_all")
                    )

                    FilterChip(
                        selected = selectedFilter == "ARTICLES",
                        onClick = { selectedFilter = "ARTICLES" },
                        label = {
                            Text(
                                text = if (isOdia) "ଖବର ($articlesCount)" else "Articles ($articlesCount)",
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedFilter == "ARTICLES") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanBlue,
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("saved_filter_articles")
                    )

                    FilterChip(
                        selected = selectedFilter == "EMERGENCY",
                        onClick = { selectedFilter = "EMERGENCY" },
                        label = {
                            Text(
                                text = if (isOdia) "ଜରୁରୀ ($emergencyCount)" else "Emergency ($emergencyCount)",
                                fontSize = 11.5.sp,
                                fontWeight = if (selectedFilter == "EMERGENCY") FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFFDC2626),
                            selectedLabelColor = Color.White
                        ),
                        modifier = Modifier.testTag("saved_filter_emergency")
                    )
                }
            }
        }

        // Content List or Empty State
        if (filteredList.isEmpty()) {
            SavedEmptyStateCard(
                filter = selectedFilter,
                isOdia = isOdia,
                modifier = Modifier.padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filteredList.forEach { bookmark ->
                    if (bookmark.itemType == "EMERGENCY") {
                        SavedEmergencyBookmarkCard(
                            bookmark = bookmark,
                            isOdia = isOdia,
                            onCall = { number -> onCallEmergency(number) },
                            onRemove = { onRemoveBookmark(bookmark.id) }
                        )
                    } else {
                        SavedNewsBookmarkCard(
                            bookmark = bookmark,
                            isOdia = isOdia,
                            onClick = {
                                val article = NewsArticle(
                                    id = bookmark.originalId,
                                    title = bookmark.title,
                                    odiaTitle = if (bookmark.odiaTitle.isNotBlank()) bookmark.odiaTitle else bookmark.title,
                                    snippet = bookmark.subtitle,
                                    odiaSnippet = if (bookmark.odiaSubtitle.isNotBlank()) bookmark.odiaSubtitle else bookmark.subtitle,
                                    category = bookmark.category,
                                    timeAgo = "Saved Offline",
                                    source = bookmark.sourceOrDepartment,
                                    isBookmarked = true,
                                    content = bookmark.content,
                                    odiaContent = bookmark.odiaContent
                                )
                                onArticleClick(article)
                            },
                            onRemove = { onRemoveBookmark(bookmark.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Emergency contact row inside 'My Saved'.
 * Features instant 1-tap call button, contact info, and remove bookmark icon.
 */
@Composable
fun SavedEmergencyBookmarkCard(
    bookmark: SavedBookmarkEntity,
    isOdia: Boolean,
    onCall: (String) -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("saved_emergency_card_${bookmark.originalId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
        border = BorderStroke(1.dp, Color(0xFFFECACA)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
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
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEE2E2)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (bookmark.category.contains("Police", ignoreCase = true)) Icons.Default.LocalPolice else Icons.Default.LocalHospital,
                        contentDescription = null,
                        tint = Color(0xFFDC2626),
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = if (isOdia && bookmark.odiaTitle.isNotBlank()) bookmark.odiaTitle else bookmark.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate900,
                            fontSize = 13.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = bookmark.subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(color = BentoSlate600),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFDC2626).copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Dial: ${bookmark.phoneNumber ?: "112"}",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFB91C1C),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val context = LocalContext.current

                // Call Button
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981))
                        .clickable { onCall(bookmark.phoneNumber ?: "112") }
                        .testTag("saved_emergency_call_btn_${bookmark.originalId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Call,
                        contentDescription = "Call Emergency Number",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Share Helpline via ShareSheet
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(OceanBlue.copy(alpha = 0.12f))
                        .clickable {
                            ShareHelper.shareEmergencyContact(
                                context = context,
                                title = if (isOdia && bookmark.odiaTitle.isNotBlank()) bookmark.odiaTitle else bookmark.title,
                                department = bookmark.sourceOrDepartment,
                                phoneNumber = bookmark.phoneNumber ?: "112",
                                isOdia = isOdia
                            )
                        }
                        .testTag("saved_emergency_share_btn_${bookmark.originalId}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Emergency Helpline via ShareSheet",
                        tint = OceanBlue,
                        modifier = Modifier.size(17.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Remove Bookmark Button
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("saved_emergency_remove_btn_${bookmark.originalId}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "Remove Bookmark",
                        tint = AmberGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * News Article card inside 'My Saved'.
 */
@Composable
fun SavedNewsBookmarkCard(
    bookmark: SavedBookmarkEntity,
    isOdia: Boolean,
    onClick: () -> Unit,
    onRemove: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("saved_news_card_${bookmark.originalId}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoSlate200),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = OceanBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = bookmark.category,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                val context = LocalContext.current

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            ShareHelper.shareNewsArticle(
                                context = context,
                                title = if (isOdia && bookmark.odiaTitle.isNotBlank()) bookmark.odiaTitle else bookmark.title,
                                snippet = if (isOdia && bookmark.odiaSubtitle.isNotBlank()) bookmark.odiaSubtitle else bookmark.subtitle,
                                source = bookmark.sourceOrDepartment,
                                category = bookmark.category,
                                odiaTitle = if (!isOdia) bookmark.odiaTitle else ""
                            )
                        },
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("saved_news_share_btn_${bookmark.originalId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share Saved News Article via ShareSheet",
                            tint = OceanBlue,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier
                            .size(28.dp)
                            .testTag("saved_news_remove_btn_${bookmark.originalId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = "Remove from bookmarks",
                            tint = AmberGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isOdia && bookmark.odiaTitle.isNotBlank()) bookmark.odiaTitle else bookmark.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate900,
                    fontSize = 14.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (bookmark.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isOdia && bookmark.odiaSubtitle.isNotBlank()) bookmark.odiaSubtitle else bookmark.subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = BentoSlate600, fontSize = 12.sp),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bookmark.sourceOrDepartment,
                    fontSize = 10.5.sp,
                    color = BentoSlate500
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val context = LocalContext.current
                    Surface(
                        onClick = {
                            ShareHelper.shareNewsArticle(
                                context = context,
                                title = if (isOdia && bookmark.odiaTitle.isNotBlank()) bookmark.odiaTitle else bookmark.title,
                                snippet = if (isOdia && bookmark.odiaSubtitle.isNotBlank()) bookmark.odiaSubtitle else bookmark.subtitle,
                                source = bookmark.sourceOrDepartment,
                                category = bookmark.category,
                                odiaTitle = if (!isOdia) bookmark.odiaTitle else ""
                            )
                        },
                        shape = RoundedCornerShape(6.dp),
                        color = OceanBlue.copy(alpha = 0.08f),
                        border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.2f)),
                        modifier = Modifier.testTag("saved_news_share_pill_${bookmark.originalId}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Share",
                                tint = OceanBlue,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (isOdia) "ସେୟାର୍" else "Share",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = OceanBlue
                            )
                        }
                    }

                    Text(
                        text = if (isOdia) "ପଢ଼ନ୍ତୁ ➔" else "Read Offline ➔",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue
                    )
                }
            }
        }
    }
}

/**
 * Helpful empty state explaining how to bookmark items.
 */
@Composable
fun SavedEmptyStateCard(
    filter: String,
    isOdia: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("saved_bookmarks_empty_state"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, BentoSlate200)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(BentoSlate100),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.BookmarkBorder,
                    contentDescription = null,
                    tint = BentoSlate400,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = when (filter) {
                    "EMERGENCY" -> if (isOdia) "କୌଣସି ଜରୁରୀ ନମ୍ବର ସଂରକ୍ଷିତ ହୋଇନାହିଁ" else "No Saved Emergency Contacts"
                    "ARTICLES" -> if (isOdia) "କୌଣସି ଖବର ସଂରକ୍ଷିତ ହୋଇନାହିଁ" else "No Saved News Articles"
                    else -> if (isOdia) "କୌଣସି ସଂରକ୍ଷିତ ଆଇଟମ୍ ନାହିଁ" else "No Saved Items Yet"
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = BentoSlate800
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (isOdia) {
                    "ଖବର କାର୍ଡ କିମ୍ବା ଜରୁରୀକାଳୀନ ନମ୍ବର ଉପରେ ବୁକମାର୍କ/ପ୍ରିୟ ଆଇକନ୍ ଟ୍ୟାପ୍ କରନ୍ତୁ, ଏହା ରୁମ୍ ଡାଟାବେସ୍ ରେ ସାଇତା ହୋଇ ଏଠାରେ ଦେଖାଯିବ।"
                } else {
                    "Tap the Bookmark icon on any news story or emergency directory contact to save it to your local Room database for rapid offline access."
                },
                style = MaterialTheme.typography.bodySmall.copy(
                    color = BentoSlate500,
                    lineHeight = 16.sp
                ),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
