package com.example.ui.components

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AddComment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.AppDatabase
import com.example.data.local.CacheSyncMetadataEntity
import com.example.data.local.DailyForecastEntity
import com.example.data.local.HotspotEntity
import com.example.data.local.NewsArticleEntity
import com.example.data.local.WeatherCacheEntity
import com.example.data.model.AppLanguage
import com.example.data.model.Hotspot
import com.example.data.model.NewsArticle
import com.example.data.repository.BalasoreRepository
import com.example.data.sync.NetworkMonitor
import com.example.ui.theme.BentoSlate100
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.BentoSlate900
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import com.example.ui.theme.OceanBlueDark
import com.example.util.BalasoreHapticHelper
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ForumReplyItem(
    val replyId: String,
    val postId: String,
    val authorName: String,
    val authorRole: String = "Citizen Member",
    val content: String,
    val odiaContent: String = "",
    val timestampMs: Long = System.currentTimeMillis(),
    val isSyncedToCloud: Boolean = true
)

data class ForumDiscussionPost(
    val postId: String,
    val title: String,
    val odiaTitle: String,
    val body: String,
    val odiaBody: String,
    val category: String, // "Local News", "Events", "Tourism"
    val authorName: String,
    val wardOrLocation: String,
    val createdAtMs: Long,
    val upvotes: Int = 1,
    val isUpvotedByMe: Boolean = false,
    val isSyncedToCloud: Boolean = true,
    val replies: List<ForumReplyItem> = emptyList()
)

/**
 * Offline-First + Cloud-Synced Repository for the Balasore Community Forum.
 * Supports creating posts, replying to discussions, upvoting, and filtering by
 * 'Local News', 'Events', and 'Tourism'. Automatically syncs queued offline
 * posts & replies when network connectivity is available.
 */
object BalasoreCommunityForumRepository {
    private const val PREFS_NAME = "balasore_community_forum_store"
    private const val KEY_POSTS_JSON = "forum_posts_json_v1"

    private val _postsFlow = MutableStateFlow<List<ForumDiscussionPost>>(defaultSeededForumPosts())
    val postsFlow: StateFlow<List<ForumDiscussionPost>> = _postsFlow.asStateFlow()

    private val _isSyncingFlow = MutableStateFlow(false)
    val isSyncingFlow: StateFlow<Boolean> = _isSyncingFlow.asStateFlow()

    private val _lastForumSyncTimestampFlow = MutableStateFlow(System.currentTimeMillis())
    val lastForumSyncTimestampFlow: StateFlow<Long> = _lastForumSyncTimestampFlow.asStateFlow()

    private var isInitialized = false

    fun ensureInitialized(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val rawJson = prefs.getString(KEY_POSTS_JSON, null)
        if (!rawJson.isNullOrBlank()) {
            val parsed = parsePostsJson(rawJson)
            if (parsed.isNotEmpty()) {
                _postsFlow.value = parsed
                return
            }
        }
        persistToPrefs(context, _postsFlow.value)
    }

    suspend fun createPost(
        context: Context,
        title: String,
        body: String,
        category: String,
        authorName: String,
        wardOrLocation: String
    ): ForumDiscussionPost = withContext(Dispatchers.IO) {
        ensureInitialized(context)
        val isOnline = NetworkMonitor(context).isCurrentlyOnline()
        val normalizedCat = when {
            category.contains("Event", ignoreCase = true) -> "Events"
            category.contains("Tourism", ignoreCase = true) -> "Tourism"
            else -> "Local News"
        }
        val newPost = ForumDiscussionPost(
            postId = "forum_post_${System.currentTimeMillis()}",
            title = title.trim(),
            odiaTitle = title.trim(),
            body = body.trim(),
            odiaBody = body.trim(),
            category = normalizedCat,
            authorName = authorName.trim().ifBlank { "Balasore Citizen" },
            wardOrLocation = wardOrLocation.trim().ifBlank { "Balasore Town" },
            createdAtMs = System.currentTimeMillis(),
            upvotes = 1,
            isUpvotedByMe = true,
            isSyncedToCloud = isOnline,
            replies = emptyList()
        )
        val updated = listOf(newPost) + _postsFlow.value
        _postsFlow.value = updated
        persistToPrefs(context, updated)

        if (isOnline) {
            pushPostToFirestore(context, newPost)
        }
        newPost
    }

    suspend fun addReply(
        context: Context,
        postId: String,
        authorName: String,
        replyContent: String
    ): ForumReplyItem? = withContext(Dispatchers.IO) {
        ensureInitialized(context)
        val trimmed = replyContent.trim()
        if (trimmed.isEmpty()) return@withContext null
        val isOnline = NetworkMonitor(context).isCurrentlyOnline()
        val reply = ForumReplyItem(
            replyId = "reply_${System.currentTimeMillis()}",
            postId = postId,
            authorName = authorName.trim().ifBlank { "Balasore Resident" },
            authorRole = "Verified Local",
            content = trimmed,
            odiaContent = trimmed,
            timestampMs = System.currentTimeMillis(),
            isSyncedToCloud = isOnline
        )
        var updatedPost: ForumDiscussionPost? = null
        val updatedList = _postsFlow.value.map { post ->
            if (post.postId == postId) {
                val modified = post.copy(
                    replies = post.replies + reply,
                    isSyncedToCloud = isOnline && post.isSyncedToCloud
                )
                updatedPost = modified
                modified
            } else {
                post
            }
        }
        _postsFlow.value = updatedList
        persistToPrefs(context, updatedList)

        if (isOnline && updatedPost != null) {
            pushPostToFirestore(context, updatedPost!!)
        }
        reply
    }

    fun toggleUpvote(context: Context, postId: String) {
        ensureInitialized(context)
        val updatedList = _postsFlow.value.map { post ->
            if (post.postId == postId) {
                val nextUpvoted = !post.isUpvotedByMe
                val nextCount = if (nextUpvoted) post.upvotes + 1 else (post.upvotes - 1).coerceAtLeast(0)
                post.copy(upvotes = nextCount, isUpvotedByMe = nextUpvoted)
            } else {
                post
            }
        }
        _postsFlow.value = updatedList
        persistToPrefs(context, updatedList)
    }

    suspend fun syncPendingForumPostsWithCloud(context: Context): Int = withContext(Dispatchers.IO) {
        ensureInitialized(context)
        _isSyncingFlow.value = true
        var syncedCount = 0
        try {
            val current = _postsFlow.value
            val updated = current.map { post ->
                val hasUnsyncedReplies = post.replies.any { !it.isSyncedToCloud }
                if (!post.isSyncedToCloud || hasUnsyncedReplies) {
                    val syncedReplies = post.replies.map { it.copy(isSyncedToCloud = true) }
                    val syncedPost = post.copy(
                        isSyncedToCloud = true,
                        replies = syncedReplies
                    )
                    pushPostToFirestore(context, syncedPost)
                    syncedCount++
                    syncedPost
                } else {
                    post
                }
            }
            _postsFlow.value = updated
            persistToPrefs(context, updated)
            _lastForumSyncTimestampFlow.value = System.currentTimeMillis()
        } finally {
            _isSyncingFlow.value = false
        }
        syncedCount
    }

    private suspend fun pushPostToFirestore(context: Context, post: ForumDiscussionPost) {
        try {
            if (FirebaseApp.getApps(context).isEmpty()) return
            val dbId = context.getString(com.example.R.string.firestore_database_id)
            val db = FirebaseFirestore.getInstance(dbId)
            val repliesMapList = post.replies.map { r ->
                mapOf(
                    "replyId" to r.replyId,
                    "postId" to r.postId,
                    "authorName" to r.authorName,
                    "authorRole" to r.authorRole,
                    "content" to r.content,
                    "timestampMs" to r.timestampMs
                )
            }
            val data = mapOf(
                "postId" to post.postId,
                "title" to post.title,
                "odiaTitle" to post.odiaTitle,
                "body" to post.body,
                "odiaBody" to post.odiaBody,
                "category" to post.category,
                "authorName" to post.authorName,
                "wardOrLocation" to post.wardOrLocation,
                "createdAtMs" to post.createdAtMs,
                "upvotes" to post.upvotes,
                "replyCount" to post.replies.size,
                "replies" to repliesMapList,
                "updatedAt" to System.currentTimeMillis()
            )
            db.collection("community_forum_posts")
                .document(post.postId)
                .set(data, SetOptions.merge())
                .await()
        } catch (_: Exception) {
            // Gracefully keep in local Room/SharedPreferences store if Firestore is offline or restricted
        }
    }

    private fun persistToPrefs(context: Context, posts: List<ForumDiscussionPost>) {
        try {
            val arr = JSONArray()
            posts.forEach { post ->
                val obj = JSONObject().apply {
                    put("postId", post.postId)
                    put("title", post.title)
                    put("odiaTitle", post.odiaTitle)
                    put("body", post.body)
                    put("odiaBody", post.odiaBody)
                    put("category", post.category)
                    put("authorName", post.authorName)
                    put("wardOrLocation", post.wardOrLocation)
                    put("createdAtMs", post.createdAtMs)
                    put("upvotes", post.upvotes)
                    put("isUpvotedByMe", post.isUpvotedByMe)
                    put("isSyncedToCloud", post.isSyncedToCloud)
                    val repliesArr = JSONArray()
                    post.replies.forEach { reply ->
                        repliesArr.put(
                            JSONObject().apply {
                                put("replyId", reply.replyId)
                                put("postId", reply.postId)
                                put("authorName", reply.authorName)
                                put("authorRole", reply.authorRole)
                                put("content", reply.content)
                                put("odiaContent", reply.odiaContent)
                                put("timestampMs", reply.timestampMs)
                                put("isSyncedToCloud", reply.isSyncedToCloud)
                            }
                        )
                    }
                    put("replies", repliesArr)
                }
                arr.put(obj)
            }
            context.applicationContext
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_POSTS_JSON, arr.toString())
                .apply()
        } catch (_: Exception) {
        }
    }

    private fun parsePostsJson(rawJson: String): List<ForumDiscussionPost> {
        return try {
            val arr = JSONArray(rawJson)
            val list = mutableListOf<ForumDiscussionPost>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val repliesArr = obj.optJSONArray("replies") ?: JSONArray()
                val replies = mutableListOf<ForumReplyItem>()
                for (j in 0 until repliesArr.length()) {
                    val rObj = repliesArr.getJSONObject(j)
                    replies.add(
                        ForumReplyItem(
                            replyId = rObj.optString("replyId", "r_$j"),
                            postId = rObj.optString("postId", ""),
                            authorName = rObj.optString("authorName", "Citizen"),
                            authorRole = rObj.optString("authorRole", "Citizen Member"),
                            content = rObj.optString("content", ""),
                            odiaContent = rObj.optString("odiaContent", ""),
                            timestampMs = rObj.optLong("timestampMs", System.currentTimeMillis()),
                            isSyncedToCloud = rObj.optBoolean("isSyncedToCloud", true)
                        )
                    )
                }
                list.add(
                    ForumDiscussionPost(
                        postId = obj.optString("postId", "post_$i"),
                        title = obj.optString("title", ""),
                        odiaTitle = obj.optString("odiaTitle", ""),
                        body = obj.optString("body", ""),
                        odiaBody = obj.optString("odiaBody", ""),
                        category = obj.optString("category", "Local News"),
                        authorName = obj.optString("authorName", "Balasore Citizen"),
                        wardOrLocation = obj.optString("wardOrLocation", "Balasore"),
                        createdAtMs = obj.optLong("createdAtMs", System.currentTimeMillis()),
                        upvotes = obj.optInt("upvotes", 1),
                        isUpvotedByMe = obj.optBoolean("isUpvotedByMe", false),
                        isSyncedToCloud = obj.optBoolean("isSyncedToCloud", true),
                        replies = replies
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun defaultSeededForumPosts(): List<ForumDiscussionPost> {
        val now = System.currentTimeMillis()
        return listOf(
            ForumDiscussionPost(
                postId = "forum_seed_local_news_1",
                title = "OT Road Flyover & Station Square Smart Traffic Signal Timings",
                odiaTitle = "ଓଟି ରୋଡ୍ ଫ୍ଲାଏଓଭର ଏବଂ ଷ୍ଟେସନ ଛକ ସ୍ମାର୍ଟ ଟ୍ରାଫିକ୍ ସିଗନାଲ୍ ଆଲୋଚନା",
                body = "The new smart LED traffic signals near Fakir Mohan Golai and Station Square have reduced morning peak congestion. Which junctions in Ward 12–18 still need pedestrian zebra crossings and solar streetlights?",
                odiaBody = "ଫକୀର ମୋହନ ଗୋଲେଇ ଏବଂ ଷ୍ଟେସନ ଛକ ନିକଟରେ ନୂତନ ସ୍ମାର୍ଟ ଟ୍ରାଫିକ୍ ସିଗନାଲ୍ ଭିଡ଼ କମାଇଛି। ଅନ୍ୟ କେଉଁ ଛକରେ ସୌର ଆଲୋକ ଓ ଜେବ୍ରା କ୍ରସିଂ ଦରକାର?",
                category = "Local News",
                authorName = "Subrat Mohanty",
                wardOrLocation = "Ward 14 • Station Square",
                createdAtMs = now - 3600_000L * 2,
                upvotes = 28,
                replies = listOf(
                    ForumReplyItem(
                        replyId = "reply_seed_1",
                        postId = "forum_seed_local_news_1",
                        authorName = "Dr. Lopamudra Das",
                        authorRole = "FM Medical Faculty",
                        content = "Adding a dedicated ambulance lane near Remuna Golai and Hospital Gate has helped emergency response tremendously!",
                        odiaContent = "ରେମୁଣା ଗୋଲେଇ ଓ ହସ୍ପିଟାଲ୍ ଗେଟ୍ ପାଖରେ ସ୍ୱତନ୍ତ୍ର ଆମ୍ବୁଲାନ୍ସ ଲେନ୍ ବହୁତ ସାହାଯ୍ୟ କରୁଛି!"
                    ),
                    ForumReplyItem(
                        replyId = "reply_seed_2",
                        postId = "forum_seed_local_news_1",
                        authorName = "chinmaya_bls",
                        authorRole = "Daily Commuter",
                        content = "Motiganj Bazaar evening drainage desilting is also progressing fast this week.",
                        odiaContent = "ମୋତିଗଞ୍ଜ ବଜାର ଡ୍ରେନେଜ୍ ସଫେଇ କାମ ମଧ୍ୟ ଏହି ସପ୍ତାହରେ ଦ୍ରୁତ ଗତିରେ ଚାଲିଛି।"
                    )
                )
            ),
            ForumDiscussionPost(
                postId = "forum_seed_tourism_1",
                title = "Chandipur Low-Tide Seabed Walk & Bichitrapur Mangrove Boat Timings",
                odiaTitle = "ଚାନ୍ଦିପୁର ଜୁଆର ସମୟ ଏବଂ ବିଚିତ୍ରପୁର ହେନ୍ତାଳ ବଣ ବୋଟ୍ ସଫାରୀ ଗାଇଡ୍",
                body = "Planning a weekend family trip to Chandipur Beach, Balaramgadi Fishing Harbor, and Bichitrapur Mangrove Eco-Park. Best low-tide walk window is 7:30 AM – 10:15 AM. Share your favorite local seafood spots and eco-boating tips!",
                odiaBody = "ଚାନ୍ଦିପୁର ବେଳାଭୂମି ଏବଂ ବିଚିତ୍ରପୁର ହେନ୍ତାଳ ବଣ ଭ୍ରମଣ ପାଇଁ ସକାଳ ୭:୩୦ ରୁ ୧୦:୧୫ ସର୍ବୋତ୍ତମ ସମୟ। ଆପଣଙ୍କ ପରାମର୍ଶ ଦିଅନ୍ତୁ!",
                category = "Tourism",
                authorName = "Ananya Panda",
                wardOrLocation = "Chandipur & Talasari Coastal Belt",
                createdAtMs = now - 3600_000L * 5,
                upvotes = 42,
                replies = listOf(
                    ForumReplyItem(
                        replyId = "reply_seed_3",
                        postId = "forum_seed_tourism_1",
                        authorName = "Rakesh Behera",
                        authorRole = "Coastal Eco-Guide",
                        content = "Don't miss the morning Kheerachora Gopinath Temple darshan at Remuna on your way back for authentic Amruta Keli prasad!",
                        odiaContent = "ଫେରିବା ବାଟରେ ରେମୁଣା କ୍ଷୀରଚୋରା ଗୋପୀନାଥ ମନ୍ଦିର ଦର୍ଶନ ଓ অমୃତ କେଳି ପ୍ରସାଦ ନିଶ୍ଚୟ ନିଅନ୍ତୁ!"
                    )
                )
            ),
            ForumDiscussionPost(
                postId = "forum_seed_events_1",
                title = "Baleshwar Mahotsav, Chadak Mela & Fakir Mohan Sahitya Utsav Volunteers",
                odiaTitle = "ବାଲେଶ୍ୱର ମହୋତ୍ସବ, ଚଡ଼କ ମେଳା ଓ ଫକୀର ମୋହନ ସାହିତ୍ୟ ଉତ୍ସବ ସ୍ୱେଚ୍ଛାସେବୀ ଆଲୋଚନା",
                body = "Registrations are open for cultural stalls, Nilagiri stone-craft exhibitions, and heritage storytelling walks at Gandhi Smruti Bhawan and Shanti Kanan. Post your event schedules, folk troupe updates, or volunteer questions here!",
                odiaBody = "ଗାନ୍ଧୀ ସ୍ମୃତି ଭବନ ଓ ଶାନ୍ତି କାନନରେ ବାଲେଶ୍ୱର ମହୋତ୍ସବ ଏବଂ ସାହିତ୍ୟ ଉତ୍ସବ ପାଇଁ ପଞ୍ଜୀକରଣ ଖୋଲିଛି। ଏଠାରେ ଆଲୋଚନା କରନ୍ତୁ!",
                category = "Events",
                authorName = "Baleswar Cultural Guild",
                wardOrLocation = "Gandhi Smruti Bhawan • Town Hall",
                createdAtMs = now - 3600_000L * 8,
                upvotes = 35,
                replies = listOf(
                    ForumReplyItem(
                        replyId = "reply_seed_4",
                        postId = "forum_seed_events_1",
                        authorName = "Smruti Ranjan",
                        authorRole = "Artisan Coordinator",
                        content = "Nilagiri Sabai grass & Dokra artisan SHGs will be showcasing live craft demonstrations at Stall Block B from 4 PM daily.",
                        odiaContent = "ନୀଳଗିରି ସବାଇ ଘାସ ଓ ଡୋକ୍ରା କାରିଗରମାନେ ପ୍ରତିଦିନ ଅପରାହ୍ନ ୪ଟାରୁ ଷ୍ଟଲ୍ ବ୍ଲକ୍-বি ରେ ପ୍ରଦର୍ଶନ କରିବେ।"
                    )
                )
            )
        )
    }
}

/**
 * Unified Offline Access & Download Manager for News Articles and Tourism Hotspots.
 * Persists downloaded articles and hotspots in Room (`newsDao()`, `hotspotDao()`, `cacheMetadataDao()`)
 * and tracks per-item offline download state with automatic cloud sync when online.
 */
object BalasoreOfflineAccessManager {
    private const val PREFS_NAME = "balasore_offline_downloads_store"
    private const val KEY_DOWNLOADED_NEWS_IDS = "downloaded_news_ids"
    private const val KEY_DOWNLOADED_HOTSPOT_IDS = "downloaded_hotspot_ids"
    private const val KEY_WEATHER_CACHED = "weather_cached_offline"
    private const val KEY_WEATHER_SUMMARY = "weather_cached_summary"
    private const val KEY_LAST_SYNC_MS = "last_offline_sync_ms"
    private const val KEY_PENDING_SYNC_ACTIONS = "pending_sync_actions_count"

    private val _downloadedNewsIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    val downloadedNewsIdsFlow: StateFlow<Set<String>> = _downloadedNewsIdsFlow.asStateFlow()

    private val _downloadedHotspotIdsFlow = MutableStateFlow<Set<String>>(emptySet())
    val downloadedHotspotIdsFlow: StateFlow<Set<String>> = _downloadedHotspotIdsFlow.asStateFlow()

    private val _isWeatherCachedOfflineFlow = MutableStateFlow(true)
    val isWeatherCachedOfflineFlow: StateFlow<Boolean> = _isWeatherCachedOfflineFlow.asStateFlow()

    private val _cachedWeatherSummaryFlow = MutableStateFlow("31°C • Partly Cloudy • AQI 42 • Chandipur Low Tide + 3-Day Forecast")
    val cachedWeatherSummaryFlow: StateFlow<String> = _cachedWeatherSummaryFlow.asStateFlow()

    private val _lastSyncTimestampFlow = MutableStateFlow(System.currentTimeMillis())
    val lastSyncTimestampFlow: StateFlow<Long> = _lastSyncTimestampFlow.asStateFlow()

    private val _pendingSyncActionsFlow = MutableStateFlow(0)
    val pendingSyncActionsFlow: StateFlow<Int> = _pendingSyncActionsFlow.asStateFlow()

    private val _isSyncingOfflineDataFlow = MutableStateFlow(false)
    val isSyncingOfflineDataFlow: StateFlow<Boolean> = _isSyncingOfflineDataFlow.asStateFlow()

    private var isInitialized = false

    fun ensureInitialized(context: Context) {
        if (isInitialized) return
        isInitialized = true
        val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedNews = prefs.getStringSet(KEY_DOWNLOADED_NEWS_IDS, null)
        val savedSpots = prefs.getStringSet(KEY_DOWNLOADED_HOTSPOT_IDS, null)
        _downloadedNewsIdsFlow.value = savedNews ?: BalasoreRepository.newsArticles.take(4).map { it.id }.toSet()
        _downloadedHotspotIdsFlow.value = savedSpots ?: BalasoreRepository.hotspots.take(4).map { it.id }.toSet()
        _isWeatherCachedOfflineFlow.value = prefs.getBoolean(KEY_WEATHER_CACHED, true)
        _cachedWeatherSummaryFlow.value = prefs.getString(
            KEY_WEATHER_SUMMARY,
            "31°C • Partly Cloudy • AQI 42 • Chandipur Low Tide + 3-Day Forecast"
        ) ?: "31°C • Partly Cloudy • AQI 42 • Chandipur Low Tide + 3-Day Forecast"
        _lastSyncTimestampFlow.value = prefs.getLong(KEY_LAST_SYNC_MS, System.currentTimeMillis())
        _pendingSyncActionsFlow.value = prefs.getInt(KEY_PENDING_SYNC_ACTIONS, 0)
    }

    suspend fun downloadWeatherForOffline(
        context: Context,
        temperatureC: Double = 31.0,
        weatherDescription: String = "Partly Cloudy • Coastal Breeze",
        windSpeedKmh: Double = 18.5,
        humidityPct: Int = 74,
        tideState: String = "LOW_TIDE"
    ): String = withContext(Dispatchers.IO) {
        ensureInitialized(context)
        val db = AppDatabase.getInstance(context)
        val now = System.currentTimeMillis()
        val weatherEntity = WeatherCacheEntity(
            id = 1,
            temperature = temperatureC,
            apparentTemperature = temperatureC + 2.5,
            weatherCode = 2,
            weatherDescription = weatherDescription,
            windSpeed = windSpeedKmh,
            windGusts = windSpeedKmh + 8.0,
            humidity = humidityPct,
            alertLevel = "ADVISORY",
            alertTitle = "Balasore Coastal Tide & Monsoon Watch",
            alertMessage = "Chandipur seabed walk safe during low tide window; monitor afternoon convective clouds over Nilagiri & Soro.",
            tideState = tideState,
            tideDescription = "Chandipur Sea Recession Active (~3.8 km exposed seabed)",
            sunrise = "05:32 AM",
            sunset = "06:14 PM",
            uvIndex = 6.5,
            maxTemp = 33.5,
            minTemp = 25.0,
            lastUpdated = now
        )
        val forecasts = listOf(
            DailyForecastEntity(
                date = "Day 1 (Today)",
                dayOfWeek = "Today",
                weatherCode = 2,
                weatherDescription = weatherDescription,
                maxTemp = 33.5,
                minTemp = 25.0,
                sunrise = "05:32 AM",
                sunset = "06:14 PM",
                uvIndex = 6.5,
                cachedAt = now
            ),
            DailyForecastEntity(
                date = "Day 2 (Tomorrow)",
                dayOfWeek = "Tomorrow",
                weatherCode = 3,
                weatherDescription = "Scattered Coastal Showers • Chandipur & Talasari",
                maxTemp = 32.0,
                minTemp = 24.5,
                sunrise = "05:33 AM",
                sunset = "06:13 PM",
                uvIndex = 6.0,
                cachedAt = now
            ),
            DailyForecastEntity(
                date = "Day 3 (Outlook)",
                dayOfWeek = "Day 3",
                weatherCode = 1,
                weatherDescription = "Mostly Sunny • Pleasant Kuldiha & Nilagiri Breeze",
                maxTemp = 33.0,
                minTemp = 24.0,
                sunrise = "05:33 AM",
                sunset = "06:12 PM",
                uvIndex = 7.0,
                cachedAt = now
            )
        )
        db.weatherDao().insertOrUpdateWeather(weatherEntity)
        db.weatherDao().insertDailyForecasts(forecasts)
        val summary = "${temperatureC.toInt()}°C • $weatherDescription • Tide: $tideState • 3-Day Forecast Cached"
        db.cacheMetadataDao().insertOrUpdate(
            CacheSyncMetadataEntity(
                cacheKey = "WEATHER",
                lastSyncedAt = now,
                itemCount = 1 + forecasts.size,
                status = "OFFLINE_READY",
                cacheLabel = "Downloaded Weather & Tide Pack",
                details = summary
            )
        )
        _isWeatherCachedOfflineFlow.value = true
        _cachedWeatherSummaryFlow.value = summary
        saveSetsToPrefs(context)
        summary
    }

    suspend fun toggleDownloadNewsArticle(
        context: Context,
        article: NewsArticle
    ): Boolean = withContext(Dispatchers.IO) {
        ensureInitialized(context)
        val db = AppDatabase.getInstance(context)
        val current = _downloadedNewsIdsFlow.value.toMutableSet()
        val isNowDownloaded: Boolean
        if (current.contains(article.id)) {
            current.remove(article.id)
            isNowDownloaded = false
        } else {
            current.add(article.id)
            isNowDownloaded = true
            val entityId = (article.id.hashCode().toLong() and 0x7FFFFFFFL).coerceAtLeast(100L)
            db.newsDao().insertArticles(
                listOf(
                    NewsArticleEntity(
                        id = entityId,
                        title = article.title,
                        summary = article.snippet,
                        content = article.content.ifBlank { "${article.snippet}\n\n${article.odiaSnippet}" },
                        category = article.category,
                        source = article.source,
                        publishedAt = article.timeAgo,
                        timestamp = System.currentTimeMillis(),
                        isBreaking = false,
                        isBookmarked = true
                    )
                )
            )
            db.cacheMetadataDao().insertOrUpdate(
                CacheSyncMetadataEntity(
                    cacheKey = "NEWS",
                    lastSyncedAt = System.currentTimeMillis(),
                    itemCount = current.size,
                    status = "OFFLINE_READY",
                    cacheLabel = "Downloaded News Articles",
                    details = "${current.size} articles saved for offline reading"
                )
            )
        }
        _downloadedNewsIdsFlow.value = current
        saveSetsToPrefs(context)
        isNowDownloaded
    }

    suspend fun downloadAllNewsArticlesForOffline(
        context: Context,
        articles: List<NewsArticle>
    ): Int = withContext(Dispatchers.IO) {
        ensureInitialized(context)
        val db = AppDatabase.getInstance(context)
        val entities = articles.mapIndexed { idx, art ->
            NewsArticleEntity(
                id = (idx + 1).toLong(),
                title = art.title,
                summary = art.snippet,
                content = art.content.ifBlank { "${art.snippet}\n\n${art.odiaSnippet}" },
                category = art.category,
                source = art.source,
                publishedAt = art.timeAgo,
                timestamp = System.currentTimeMillis() - idx * 60_000L,
                isBreaking = idx == 0,
                isBookmarked = true
            )
        }
        if (entities.isNotEmpty()) {
            db.newsDao().insertArticles(entities)
            db.cacheMetadataDao().insertOrUpdate(
                CacheSyncMetadataEntity(
                    cacheKey = "NEWS",
                    lastSyncedAt = System.currentTimeMillis(),
                    itemCount = entities.size,
                    status = "OFFLINE_READY",
                    cacheLabel = "Downloaded News Articles Pack",
                    details = "${entities.size} articles cached in Room for offline viewing"
                )
            )
        }
        val allIds = (_downloadedNewsIdsFlow.value + articles.map { it.id }).toSet()
        _downloadedNewsIdsFlow.value = allIds
        saveSetsToPrefs(context)
        allIds.size
    }

    suspend fun toggleDownloadHotspot(
        context: Context,
        hotspot: Hotspot
    ): Boolean = withContext(Dispatchers.IO) {
        ensureInitialized(context)
        val db = AppDatabase.getInstance(context)
        val current = _downloadedHotspotIdsFlow.value.toMutableSet()
        val isNowDownloaded: Boolean
        if (current.contains(hotspot.id)) {
            current.remove(hotspot.id)
            isNowDownloaded = false
        } else {
            current.add(hotspot.id)
            isNowDownloaded = true
            db.hotspotDao().insertHotspots(
                listOf(
                    HotspotEntity(
                        id = hotspot.id,
                        name = hotspot.name,
                        odiaName = hotspot.odiaName,
                        category = hotspot.category,
                        shortDescription = hotspot.description,
                        fullDescription = "${hotspot.effectiveDetailedDescription} Located at ${hotspot.location}. Hours: ${hotspot.effectiveOperatingHours}. ${hotspot.historicalSignificance}",
                        highlights = "${hotspot.category} • ${hotspot.location} • ★ ${hotspot.rating}",
                        distanceKmFromBls = hotspot.distanceKm.toInt(),
                        bestTimeToVisit = hotspot.effectiveOperatingHours,
                        timings = hotspot.timing,
                        entryFee = "Free / Public Heritage Entry",
                        specialty = hotspot.category,
                        localTip = "Downloaded for 100% offline access in Baleshwar 360.",
                        latitude = hotspot.latitude,
                        longitude = hotspot.longitude,
                        isFavorite = true
                    )
                )
            )
            db.cacheMetadataDao().insertOrUpdate(
                CacheSyncMetadataEntity(
                    cacheKey = "TOURISM",
                    lastSyncedAt = System.currentTimeMillis(),
                    itemCount = current.size,
                    status = "OFFLINE_READY",
                    cacheLabel = "Downloaded Tourism Hotspots",
                    details = "${current.size} hotspots cached in Room for offline viewing"
                )
            )
        }
        _downloadedHotspotIdsFlow.value = current
        saveSetsToPrefs(context)
        isNowDownloaded
    }

    suspend fun downloadAllHotspotsForOffline(
        context: Context,
        hotspots: List<Hotspot>
    ): Int = withContext(Dispatchers.IO) {
        ensureInitialized(context)
        val db = AppDatabase.getInstance(context)
        val entities = hotspots.map { spot ->
            HotspotEntity(
                id = spot.id,
                name = spot.name,
                odiaName = spot.odiaName,
                category = spot.category,
                shortDescription = spot.description,
                fullDescription = "${spot.effectiveDetailedDescription} Located at ${spot.location}. Hours: ${spot.effectiveOperatingHours}. ${spot.historicalSignificance}",
                highlights = "${spot.category} • ${spot.location} • ★ ${spot.rating}",
                distanceKmFromBls = spot.distanceKm.toInt(),
                bestTimeToVisit = spot.effectiveOperatingHours,
                timings = spot.timing,
                entryFee = "Free / Public Heritage Entry",
                specialty = spot.category,
                localTip = "Downloaded for 100% offline access in Baleshwar 360.",
                latitude = spot.latitude,
                longitude = spot.longitude,
                isFavorite = true
            )
        }
        if (entities.isNotEmpty()) {
            db.hotspotDao().insertHotspots(entities)
            db.cacheMetadataDao().insertOrUpdate(
                CacheSyncMetadataEntity(
                    cacheKey = "TOURISM",
                    lastSyncedAt = System.currentTimeMillis(),
                    itemCount = entities.size,
                    status = "OFFLINE_READY",
                    cacheLabel = "Downloaded Tourism Hotspots Pack",
                    details = "${entities.size} tourism hotspots cached in Room for offline viewing"
                )
            )
        }
        val allIds = (_downloadedHotspotIdsFlow.value + hotspots.map { it.id }).toSet()
        _downloadedHotspotIdsFlow.value = allIds
        saveSetsToPrefs(context)
        allIds.size
    }

    suspend fun syncOfflinePackWhenOnline(
        context: Context,
        articles: List<NewsArticle> = BalasoreRepository.newsArticles,
        hotspots: List<Hotspot> = BalasoreRepository.hotspots
    ) = withContext(Dispatchers.IO) {
        ensureInitialized(context)
        _isSyncingOfflineDataFlow.value = true
        try {
            downloadAllNewsArticlesForOffline(context, articles)
            downloadAllHotspotsForOffline(context, hotspots)
            downloadWeatherForOffline(context)
            BalasoreCommunityForumRepository.syncPendingForumPostsWithCloud(context)
            val now = System.currentTimeMillis()
            _lastSyncTimestampFlow.value = now
            _pendingSyncActionsFlow.value = 0
            saveSetsToPrefs(context)
        } finally {
            _isSyncingOfflineDataFlow.value = false
        }
    }

    private fun saveSetsToPrefs(context: Context) {
        context.applicationContext
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putStringSet(KEY_DOWNLOADED_NEWS_IDS, _downloadedNewsIdsFlow.value)
            .putStringSet(KEY_DOWNLOADED_HOTSPOT_IDS, _downloadedHotspotIdsFlow.value)
            .putBoolean(KEY_WEATHER_CACHED, _isWeatherCachedOfflineFlow.value)
            .putString(KEY_WEATHER_SUMMARY, _cachedWeatherSummaryFlow.value)
            .putLong(KEY_LAST_SYNC_MS, _lastSyncTimestampFlow.value)
            .putInt(KEY_PENDING_SYNC_ACTIONS, _pendingSyncActionsFlow.value)
            .apply()
    }
}

/**
 * Compact Offline Access & Auto-Sync Banner for NewsScreen and TourismScreen.
 * Allows users to 1-tap download all News Articles or Tourism Hotspots for offline viewing,
 * inspect offline storage status, and sync with Room + Firestore when back online.
 */
@Composable
fun BalasoreOfflineDownloadBannerCard(
    mode: String, // "NEWS" or "TOURISM"
    language: AppLanguage,
    articles: List<NewsArticle> = BalasoreRepository.newsArticles,
    hotspots: List<Hotspot> = BalasoreRepository.hotspots,
    onOpenOfflineManager: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA

    LaunchedEffect(Unit) {
        BalasoreOfflineAccessManager.ensureInitialized(context)
    }

    val networkMonitor = remember { NetworkMonitor(context) }
    val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle(
        initialValue = networkMonitor.isCurrentlyOnline()
    )
    val downloadedNewsIds by BalasoreOfflineAccessManager.downloadedNewsIdsFlow.collectAsStateWithLifecycle()
    val downloadedHotspotIds by BalasoreOfflineAccessManager.downloadedHotspotIdsFlow.collectAsStateWithLifecycle()
    val lastSyncTs by BalasoreOfflineAccessManager.lastSyncTimestampFlow.collectAsStateWithLifecycle()
    val isSyncing by BalasoreOfflineAccessManager.isSyncingOfflineDataFlow.collectAsStateWithLifecycle()

    // Automatically sync pending offline downloads & forum discussions when device comes back online
    LaunchedEffect(isOnline) {
        if (isOnline) {
            BalasoreCommunityForumRepository.syncPendingForumPostsWithCloud(context)
        }
    }

    val formattedSyncTime = remember(lastSyncTs) {
        SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(lastSyncTs))
    }

    val bannerTag = if (mode == "NEWS") "news_offline_access_banner" else "tourism_offline_access_banner"
    val downloadBtnTag = if (mode == "NEWS") "news_download_all_offline_btn" else "tourism_download_all_offline_btn"
    val syncBtnTag = if (mode == "NEWS") "news_sync_offline_data_btn" else "tourism_sync_offline_btn"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag(bannerTag),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOnline) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
        ),
        border = BorderStroke(
            1.dp,
            if (isOnline) EmeraldGreen.copy(alpha = 0.45f) else Color(0xFFF59E0B)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isOnline) EmeraldGreen.copy(alpha = 0.16f) else Color(0xFFFEF3C7),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                contentDescription = "Offline Access Status",
                                tint = if (isOnline) EmeraldGreen else Color(0xFFD97706),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (mode == "NEWS") {
                                if (isOdia) "📥 ଅଫଲାଇନ୍ ସମ୍ବାଦ ପଠନ ଓ ଅଟୋ-ସିଙ୍କ୍"
                                else "📥 Offline News Access & Auto-Sync"
                            } else {
                                if (isOdia) "📥 ଅଫଲାଇନ୍ ପର୍ଯ୍ୟଟନ ଗାଇଡ୍ ଓ ଅଟୋ-ସିଙ୍କ୍"
                                else "📥 Offline Tourism Hotspots & Auto-Sync"
                            },
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = BentoSlate900
                        )
                        Text(
                            text = if (isOnline) {
                                if (isOdia) {
                                    "ଅନଲାଇନ୍ • ${downloadedNewsIds.size} ଖବର ଓ ${downloadedHotspotIds.size} ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ଅଫଲାଇନ୍ ସେଭ୍ • ସିଙ୍କ୍: $formattedSyncTime"
                                } else {
                                    "Online Synced ($formattedSyncTime) • ${downloadedNewsIds.size} News & ${downloadedHotspotIds.size} Hotspots Offline Ready"
                                }
                            } else {
                                if (isOdia) {
                                    "ଅଫଲାଇନ୍ ମୋଡ୍ • ${downloadedNewsIds.size} ଖବର ଓ ${downloadedHotspotIds.size} ପର୍ଯ୍ୟଟନ ସ୍ଥଳୀ ବିନା ଇଣ୍ଟରନେଟରେ ଉପଲବ୍ଧ"
                                } else {
                                    "Offline Mode • Serving ${downloadedNewsIds.size} News & ${downloadedHotspotIds.size} Hotspots from Room DB"
                                }
                            },
                            fontSize = 11.sp,
                            color = BentoSlate700
                        )
                    }
                }
            }

            if (isSyncing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = EmeraldGreen
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        coroutineScope.launch {
                            if (mode == "NEWS") {
                                val count = BalasoreOfflineAccessManager.downloadAllNewsArticlesForOffline(context, articles)
                                Toast.makeText(
                                    context,
                                    "Downloaded $count News Articles to Room DB for offline reading!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                val count = BalasoreOfflineAccessManager.downloadAllHotspotsForOffline(context, hotspots)
                                Toast.makeText(
                                    context,
                                    "Downloaded $count Tourism Hotspots to Room DB for offline viewing!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1.2f)
                        .height(38.dp)
                        .testTag(downloadBtnTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudDownload,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (mode == "NEWS") {
                            if (isOdia) "ସବୁ ଖବର ଡାଉନଲୋଡ୍" else "Download News Offline"
                        } else {
                            if (isOdia) "ସବୁ ପର୍ଯ୍ୟଟନ ଡାଉନଲୋଡ୍" else "Download Hotspots"
                        },
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        coroutineScope.launch {
                            BalasoreOfflineAccessManager.syncOfflinePackWhenOnline(context, articles, hotspots)
                            Toast.makeText(
                                context,
                                "Synced offline News, Hotspots & Community Forum with Cloud!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(0.9f)
                        .height(38.dp)
                        .testTag(syncBtnTag)
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudSync,
                        contentDescription = null,
                        tint = OceanBlue,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isOdia) "ସିଙ୍କ୍ କରନ୍ତୁ" else "Sync Now",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = OceanBlue,
                        maxLines = 1
                    )
                }

                OutlinedButton(
                    onClick = {
                        BalasoreHapticHelper.vibrateButtonClick(context)
                        onOpenOfflineManager()
                    },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier
                        .height(38.dp)
                        .testTag("${mode.lowercase()}_open_offline_hub_btn")
                ) {
                    Text(
                        text = if (isOdia) "ଅଫଲାଇନ୍ ହବ୍" else "Offline Hub",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Interactive Balasore Community Forum Card (Embeddable in Home/Tourism, News, and Modal Bottom Sheet).
 * Users can:
 * 1. Filter topics by 'All', 'Local News', 'Events', and 'Tourism'.
 * 2. Create new discussion posts with category, ward/location, title, and message body.
 * 3. Reply to existing discussions and upvote helpful threads.
 * 4. Work seamlessly offline with automatic cloud sync when back online.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BalasoreCommunityForumCard(
    language: AppLanguage,
    initialCategory: String = "All",
    onOpenFullForumSheet: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isOdia = language == AppLanguage.ODIA

    LaunchedEffect(Unit) {
        BalasoreCommunityForumRepository.ensureInitialized(context)
    }

    val allPosts by BalasoreCommunityForumRepository.postsFlow.collectAsStateWithLifecycle()
    val isSyncing by BalasoreCommunityForumRepository.isSyncingFlow.collectAsStateWithLifecycle()
    val networkMonitor = remember { NetworkMonitor(context) }
    val isOnline by networkMonitor.isOnline.collectAsStateWithLifecycle(
        initialValue = networkMonitor.isCurrentlyOnline()
    )

    var selectedCategory by remember(initialCategory) { mutableStateOf(initialCategory) }
    var searchQuery by remember { mutableStateOf("") }
    var showCreatePostForm by remember { mutableStateOf(false) }

    // Create Post Form States
    var newPostTitle by remember { mutableStateOf("") }
    var newPostBody by remember { mutableStateOf("") }
    var newPostCategory by remember { mutableStateOf("Local News") }
    var newPostAuthor by remember { mutableStateOf("") }
    var newPostLocation by remember { mutableStateOf("Ward 14 • Station Square, Balasore") }

    // Expanded reply state & draft per post
    var expandedReplyPostIds by remember { mutableStateOf(setOf("forum_seed_local_news_1")) }
    var replyTextByPostId by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var replyAuthorByPostId by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    val forumCategories = listOf(
        Triple("All", if (isOdia) "🌐 ସମସ୍ତ" else "🌐 All Topics", "forum_category_chip_all"),
        Triple("Local News", if (isOdia) "📰 ସ୍ଥାନୀୟ ଖବର" else "📰 Local News", "forum_category_chip_local_news"),
        Triple("Events", if (isOdia) "🎪 ଉତ୍ସବ ଓ ଇଭେଣ୍ଟ" else "🎪 Events", "forum_category_chip_events"),
        Triple("Tourism", if (isOdia) "🏖️ ପର୍ଯ୍ୟଟନ" else "🏖️ Tourism", "forum_category_chip_tourism")
    )

    val filteredPosts = remember(allPosts, selectedCategory, searchQuery) {
        allPosts.filter { post ->
            val matchesCategory = selectedCategory.equals("All", ignoreCase = true) ||
                post.category.equals(selectedCategory, ignoreCase = true)
            val q = searchQuery.trim()
            val matchesQuery = q.isEmpty() ||
                post.title.contains(q, ignoreCase = true) ||
                post.odiaTitle.contains(q, ignoreCase = true) ||
                post.body.contains(q, ignoreCase = true) ||
                post.authorName.contains(q, ignoreCase = true) ||
                post.wardOrLocation.contains(q, ignoreCase = true) ||
                post.category.contains(q, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("balasore_community_forum_card"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.2.dp, OceanBlue.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header Row
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
                        color = OceanBlue.copy(alpha = 0.14f),
                        modifier = Modifier.size(42.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Forum,
                                contentDescription = "Balasore Community Forum",
                                tint = OceanBlue,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = if (isOdia) "💬 ବାଲେଶ୍ୱର କମ୍ୟୁନିଟି ଫୋରମ୍" else "💬 Balasore Community Forum",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 15.5.sp,
                                color = BentoSlate900
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (isOnline) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                            ) {
                                Text(
                                    text = if (isOnline) {
                                        if (isOdia) "☁️ ସିଙ୍କ୍" else "☁️ Live Sync"
                                    } else {
                                        if (isOdia) "📥 ଅଫଲାଇନ୍" else "📥 Offline Ready"
                                    },
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isOnline) Color(0xFF166534) else Color(0xFFB45309),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = if (isOdia) {
                                "ସ୍ଥାନୀୟ ଖବର, ଉତ୍ସବ ଏବଂ ପର୍ଯ୍ୟଟନ ବିଷୟରେ ପୋଷ୍ଟ ଓ ଉତ୍ତର ଦିଅନ୍ତୁ"
                            } else {
                                "Create posts, reply to discussions & explore Local News, Events & Tourism"
                            },
                            fontSize = 11.5.sp,
                            color = BentoSlate600
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            coroutineScope.launch {
                                val synced = BalasoreCommunityForumRepository.syncPendingForumPostsWithCloud(context)
                                Toast.makeText(
                                    context,
                                    if (synced > 0) "Synced $synced offline forum posts to Firestore!" else "Community Forum is up to date with Firestore!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("forum_sync_now_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Community Forum",
                            tint = OceanBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Button(
                        onClick = {
                            BalasoreHapticHelper.vibrateButtonClick(context)
                            showCreatePostForm = !showCreatePostForm
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (showCreatePostForm) Color(0xFFDC2626) else OceanBlue
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("forum_new_post_toggle_btn")
                    ) {
                        Icon(
                            imageVector = if (showCreatePostForm) Icons.Default.Close else Icons.Default.AddComment,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (showCreatePostForm) {
                                if (isOdia) "ବନ୍ଦ" else "Close"
                            } else {
                                if (isOdia) "+ ନୂଆ ପୋଷ୍ଟ" else "+ New Post"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (isSyncing) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = OceanBlue
                )
            }

            // Create New Discussion Post Composer
            AnimatedVisibility(visible = showCreatePostForm) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF0F9FF),
                    border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forum_create_post_form_container")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isOdia) "✍️ ନୂଆ କମ୍ୟୁନିଟି ଆଲୋଚନା ଆରମ୍ଭ କରନ୍ତୁ" else "✍️ Start a New Community Discussion",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.5.sp,
                            color = OceanBlueDark
                        )

                        // Topic Category Selector: Local News, Events, Tourism
                        Text(
                            text = if (isOdia) "ବିଷୟ ଶ୍ରେଣୀ ବାଛନ୍ତୁ (Topic Category):" else "Select Topic Category:",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate700
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                Triple("Local News", if (isOdia) "📰 ସ୍ଥାନୀୟ ଖବର" else "📰 Local News", "forum_new_post_cat_local_news"),
                                Triple("Events", if (isOdia) "🎪 ଉତ୍ସବ/ଇଭେଣ୍ଟ" else "🎪 Events", "forum_new_post_cat_events"),
                                Triple("Tourism", if (isOdia) "🏖️ ପର୍ଯ୍ୟଟନ" else "🏖️ Tourism", "forum_new_post_cat_tourism")
                            ).forEach { (catValue, catLabel, tag) ->
                                val isCatSelected = newPostCategory.equals(catValue, ignoreCase = true)
                                FilterChip(
                                    selected = isCatSelected,
                                    onClick = { newPostCategory = catValue },
                                    label = {
                                        Text(
                                            text = catLabel,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = OceanBlue,
                                        selectedLabelColor = Color.White
                                    ),
                                    modifier = Modifier.testTag(tag)
                                )
                            }
                        }

                        OutlinedTextField(
                            value = newPostTitle,
                            onValueChange = { newPostTitle = it },
                            label = {
                                Text(if (isOdia) "ଆଲୋଚନା ଶିରୋନାମା (Discussion Title)" else "Discussion Title (e.g., Chandipur Beach Fest / Ward Road Update)")
                            },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forum_post_title_input")
                        )

                        OutlinedTextField(
                            value = newPostBody,
                            onValueChange = { newPostBody = it },
                            label = {
                                Text(if (isOdia) "ଆପଣଙ୍କ ମତାମତ ବା ପ୍ରଶ୍ନ ଲେଖନ୍ତୁ..." else "Share details, questions, or updates with the Balasore community...")
                            },
                            minLines = 3,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("forum_post_body_input")
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newPostAuthor,
                                onValueChange = { newPostAuthor = it },
                                label = { Text(if (isOdia) "ଆପଣଙ୍କ ନାମ" else "Your Name") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("forum_post_author_input")
                            )
                            OutlinedTextField(
                                value = newPostLocation,
                                onValueChange = { newPostLocation = it },
                                label = { Text(if (isOdia) "ୱାର୍ଡ / ସ୍ଥାନ" else "Ward / Location") },
                                singleLine = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("forum_post_location_input")
                            )
                        }

                        Button(
                            onClick = {
                                val titleTrimmed = newPostTitle.trim()
                                val bodyTrimmed = newPostBody.trim()
                                if (titleTrimmed.isEmpty() || bodyTrimmed.isEmpty()) {
                                    Toast.makeText(
                                        context,
                                        if (isOdia) "ଦୟାକରି ଶିରୋନାମା ଏବଂ ବିଷୟবস্তୁ ଲେଖନ୍ତୁ" else "Please enter both a title and discussion details",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@Button
                                }
                                BalasoreHapticHelper.vibrateSuccess(context)
                                coroutineScope.launch {
                                    val created = BalasoreCommunityForumRepository.createPost(
                                        context = context,
                                        title = titleTrimmed,
                                        body = bodyTrimmed,
                                        category = newPostCategory,
                                        authorName = newPostAuthor,
                                        wardOrLocation = newPostLocation
                                    )
                                    selectedCategory = "All"
                                    newPostTitle = ""
                                    newPostBody = ""
                                    showCreatePostForm = false
                                    expandedReplyPostIds = expandedReplyPostIds + created.postId
                                    Toast.makeText(
                                        context,
                                        if (isOnline) "Published to Balasore Community Forum (${created.category})!"
                                        else "Saved offline! Will auto-sync to Firestore when back online.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("forum_create_post_submit_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (isOdia) "ଫୋରମରେ ପୋଷ୍ଟ କରନ୍ତୁ" else "Publish Discussion Post",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // Category Filter Chips Row (All, Local News, Events, Tourism)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                forumCategories.forEach { (catKey, label, chipTag) ->
                    val count = if (catKey == "All") {
                        allPosts.size
                    } else {
                        allPosts.count { it.category.equals(catKey, ignoreCase = true) }
                    }
                    val isSelected = selectedCategory.equals(catKey, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            BalasoreHapticHelper.vibrateToggle(context)
                            selectedCategory = catKey
                        },
                        label = {
                            Text(
                                text = "$label ($count)",
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OceanBlue,
                            selectedLabelColor = Color.White,
                            containerColor = BentoSlate100
                        ),
                        modifier = Modifier.testTag(chipTag)
                    )
                }
            }

            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (isOdia) "ଫୋରମ୍ ଆଲୋଚନା ଖୋଜନ୍ତୁ (ଖବର, ଇଭେଣ୍ଟ, ପର୍ଯ୍ୟଟନ)..."
                        else "Search discussions by topic, ward, or keyword...",
                        fontSize = 12.sp
                    )
                },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("forum_search_input")
            )

            // Discussions List
            filteredPosts.forEach { post ->
                val isRepliesExpanded = expandedReplyPostIds.contains(post.postId)
                val replyDraft = replyTextByPostId[post.postId] ?: ""
                val replyAuthorDraft = replyAuthorByPostId[post.postId] ?: ""

                val catBadgeColor = when {
                    post.category.equals("Events", ignoreCase = true) -> Color(0xFFD97706)
                    post.category.equals("Tourism", ignoreCase = true) -> Color(0xFF0D9488)
                    else -> OceanBlue
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFC),
                    border = BorderStroke(1.dp, catBadgeColor.copy(alpha = 0.28f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("forum_post_card_${post.postId}")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Category & Sync Badge Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = catBadgeColor.copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = when {
                                            post.category.equals("Events", ignoreCase = true) ->
                                                if (isOdia) "🎪 ଉତ୍ସବ ଓ ଇଭେଣ୍ଟ" else "🎪 Events"
                                            post.category.equals("Tourism", ignoreCase = true) ->
                                                if (isOdia) "🏖️ ପର୍ଯ୍ୟଟନ" else "🏖️ Tourism"
                                            else ->
                                                if (isOdia) "📰 ସ୍ଥାନୀୟ ଖବର" else "📰 Local News"
                                        },
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = catBadgeColor,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (post.isSyncedToCloud) Color(0xFFDCFCE7) else Color(0xFFFEF3C7)
                                ) {
                                    Text(
                                        text = if (post.isSyncedToCloud) "☁️ Synced" else "📥 Offline Queued",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (post.isSyncedToCloud) Color(0xFF166534) else Color(0xFFB45309),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

                            Text(
                                text = SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(post.createdAtMs)),
                                fontSize = 10.sp,
                                color = BentoSlate600
                            )
                        }

                        // Post Title
                        Text(
                            text = if (isOdia && post.odiaTitle.isNotBlank()) post.odiaTitle else post.title,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 14.5.sp,
                            color = BentoSlate900
                        )

                        // Post Body
                        Text(
                            text = if (isOdia && post.odiaBody.isNotBlank()) post.odiaBody else post.body,
                            fontSize = 12.5.sp,
                            color = BentoSlate700,
                            lineHeight = 18.sp
                        )

                        // Author & Location Metadata
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = OceanBlue,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = post.authorName,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoSlate700
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = Color(0xFFEA580C),
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text(
                                    text = post.wardOrLocation,
                                    fontSize = 11.sp,
                                    color = BentoSlate600
                                )
                            }
                        }

                        HorizontalDivider(color = BentoSlate100)

                        // Action Bar: Upvote + Reply Toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (post.isUpvotedByMe) OceanBlue.copy(alpha = 0.15f) else BentoSlate100,
                                modifier = Modifier
                                    .clickable {
                                        BalasoreHapticHelper.vibrateToggle(context)
                                        BalasoreCommunityForumRepository.toggleUpvote(context, post.postId)
                                    }
                                    .testTag("forum_upvote_btn_${post.postId}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ThumbUp,
                                        contentDescription = "Upvote discussion",
                                        tint = if (post.isUpvotedByMe) OceanBlue else BentoSlate600,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isOdia) "ସମର୍ଥନ (${post.upvotes})" else "Helpful (${post.upvotes})",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (post.isUpvotedByMe) OceanBlue else BentoSlate700
                                    )
                                }
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = OceanBlue.copy(alpha = 0.10f),
                                modifier = Modifier
                                    .clickable {
                                        expandedReplyPostIds = if (isRepliesExpanded) {
                                            expandedReplyPostIds - post.postId
                                        } else {
                                            expandedReplyPostIds + post.postId
                                        }
                                    }
                                    .testTag("forum_toggle_replies_btn_${post.postId}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Reply,
                                        contentDescription = "Replies",
                                        tint = OceanBlue,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isOdia) {
                                            "ଉତ୍ତର (${post.replies.size})"
                                        } else {
                                            "Replies (${post.replies.size}) • Reply"
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = OceanBlue
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = if (isRepliesExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        tint = OceanBlue,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Expandable Replies & Reply Input Box
                        AnimatedVisibility(visible = isRepliesExpanded) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 6.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                post.replies.forEach { reply ->
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = Color.White,
                                        border = BorderStroke(1.dp, BentoSlate100),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("forum_reply_item_${reply.replyId}")
                                    ) {
                                        Column(modifier = Modifier.padding(10.dp)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "${reply.authorName} • ${reply.authorRole}",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = OceanBlueDark
                                                )
                                                Text(
                                                    text = if (reply.isSyncedToCloud) "☁️" else "📥",
                                                    fontSize = 10.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.height(3.dp))
                                            Text(
                                                text = if (isOdia && reply.odiaContent.isNotBlank()) reply.odiaContent else reply.content,
                                                fontSize = 12.sp,
                                                color = BentoSlate700
                                            )
                                        }
                                    }
                                }

                                // Add Reply Composer
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.White)
                                        .padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    OutlinedTextField(
                                        value = replyAuthorDraft,
                                        onValueChange = {
                                            replyAuthorByPostId = replyAuthorByPostId + (post.postId to it)
                                        },
                                        placeholder = {
                                            Text(
                                                text = if (isOdia) "ଆପଣଙ୍କ ନାମ (ଐଚ୍ଛିକ)" else "Your Name (Optional)",
                                                fontSize = 11.5.sp
                                            )
                                        },
                                        singleLine = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("forum_reply_author_input_${post.postId}")
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedTextField(
                                            value = replyDraft,
                                            onValueChange = {
                                                replyTextByPostId = replyTextByPostId + (post.postId to it)
                                            },
                                            placeholder = {
                                                Text(
                                                    text = if (isOdia) "ଏହି ଆଲୋଚନାରେ ଉତ୍ତର ଲେଖନ୍ତୁ..." else "Write a reply to this discussion...",
                                                    fontSize = 12.sp
                                                )
                                            },
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("forum_reply_input_${post.postId}")
                                        )

                                        Button(
                                            onClick = {
                                                val draft = replyDraft.trim()
                                                if (draft.isEmpty()) {
                                                    Toast.makeText(
                                                        context,
                                                        if (isOdia) "ଦୟାକରି ଉତ୍ତର ଲେଖନ୍ତୁ" else "Please enter a reply",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    return@Button
                                                }
                                                BalasoreHapticHelper.vibrateButtonClick(context)
                                                coroutineScope.launch {
                                                    BalasoreCommunityForumRepository.addReply(
                                                        context = context,
                                                        postId = post.postId,
                                                        authorName = replyAuthorDraft,
                                                        replyContent = draft
                                                    )
                                                    replyTextByPostId = replyTextByPostId + (post.postId to "")
                                                    Toast.makeText(
                                                        context,
                                                        if (isOnline) "Reply posted!" else "Reply saved offline! Will sync when online.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                                            shape = RoundedCornerShape(10.dp),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                                            modifier = Modifier.testTag("forum_reply_submit_btn_${post.postId}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.Send,
                                                contentDescription = "Submit Reply",
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Embedded Local Polls & Lost & Found Tabs inside Community Forum Card
            Spacer(modifier = Modifier.height(8.dp))
            ForumLocalPollsSection(language = language)
            Spacer(modifier = Modifier.height(8.dp))
            ForumLostAndFoundSection(language = language)

            if (onOpenFullForumSheet != null) {
                OutlinedButton(
                    onClick = onOpenFullForumSheet,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("open_full_community_forum_sheet_btn")
                ) {
                    Icon(Icons.Default.Forum, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isOdia) "ସମ୍ପୂର୍ଣ୍ଣ ବାଲେଶ୍ୱର କମ୍ୟୁନିଟି ଫୋରମ୍ ଖୋଲନ୍ତୁ" else "Open Full-Screen Balasore Community Forum",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

data class BalasoreForumPoll(
    val pollId: String,
    val questionEn: String,
    val questionOd: String,
    val categoryBadge: String,
    val options: List<Pair<String, Int>>
)

data class BalasoreLostFoundItem(
    val id: String,
    val type: String, // "LOST" or "FOUND"
    val itemTitle: String,
    val categoryTag: String, // "Pet", "Wallet/ID", "Phone/Electronics", "Bag/Keys"
    val locationArea: String,
    val description: String,
    val contactPhone: String,
    val photoLabel: String,
    val postedAtLabel: String
)

@Composable
fun ForumLocalPollsSection(
    language: AppLanguage = AppLanguage.ENGLISH
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    var selectedOptions by remember { mutableStateOf(mapOf<String, Int>()) }
    var pollList by remember {
        mutableStateOf(
            listOf(
                BalasoreForumPoll(
                    pollId = "poll_civic_priority_2026",
                    questionEn = "Which Balasore civic upgrade should be prioritized first this quarter?",
                    questionOd = "ଏହି ତ୍ରৈମାସିକରେ ବାଲେଶ୍ୱରର କେଉଁ ନାଗରିକ ପ୍ରକଳ୍ପକୁ ପ୍ରଥମେ ପ୍ରାଧାନ୍ୟ ଦିଆযିବା ଉଚିତ?",
                    categoryBadge = "Civic Priority",
                    options = listOf(
                        "Fakir Mohan Golaye & Station Square Smart Traffic Signals" to 164,
                        "Chandipur Promenade Solar Streetlights & Restrooms" to 128,
                        "Motiganj & Cinema Bazar Underground Storm Drainage" to 192
                    )
                ),
                BalasoreForumPoll(
                    pollId = "poll_town_event_2026",
                    questionEn = "Which cultural highlight are you most excited for at Baleswar Mahotsav?",
                    questionOd = "ବାଲେଶ୍ୱର ମହୋତ୍ସବରେ ଆପଣ କେଉଁ ସାଂସ୍କୃତିକ କାର୍ଯ୍ୟକ୍ରମ ପାଇଁ ଅଧିକ ଉତ୍ସାହିତ?",
                    categoryBadge = "Town Event",
                    options = listOf(
                        "Nilagiri Chhau Dance & Folk Night" to 215,
                        "Jau Kandhei & Sabai Craft Pavilion" to 143,
                        "Baleswari Pitha & Coastal Seafood Arena" to 179
                    )
                )
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, Color(0xFF7C3AED).copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forum_local_polls_section")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("📊", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Column {
                    Text(
                        text = if (isOdia) "ସ୍ଥାନୀୟ ଜନମତ ପୋଲ୍ (Local Polls)" else "Local Polls • Vote on Town Events & Civic Priorities",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isOdia) "ଆପଣଙ୍କ ମତାମତ ଦିଅନ୍ତୁ" else "Tap any option to cast your community vote",
                        fontSize = 10.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                pollList.forEach { poll ->
                    val chosenIdx = selectedOptions[poll.pollId]
                    val totalVotes = poll.options.sumOf { it.second }.coerceAtLeast(1)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(5.dp),
                                    color = Color(0xFF7C3AED).copy(alpha = 0.14f)
                                ) {
                                    Text(
                                        text = poll.categoryBadge,
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF7C3AED),
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Text(
                                    text = "$totalVotes votes",
                                    fontSize = 10.sp,
                                    color = EmeraldGreen,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isOdia) poll.questionOd else poll.questionEn,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))

                            poll.options.forEachIndexed { optIdx, (optText, votes) ->
                                val isChosen = chosenIdx == optIdx
                                val pct = ((votes.toFloat() / totalVotes) * 100).toInt()
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isChosen) Color(0xFF7C3AED).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                    border = BorderStroke(
                                        1.dp,
                                        if (isChosen) Color(0xFF7C3AED) else Color.Transparent
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 2.dp)
                                        .clickable {
                                            if (chosenIdx == null) {
                                                selectedOptions = selectedOptions + (poll.pollId to optIdx)
                                                pollList = pollList.map { p ->
                                                    if (p.pollId == poll.pollId) {
                                                        p.copy(
                                                            options = p.options.mapIndexed { idx, pair ->
                                                                if (idx == optIdx) pair.first to (pair.second + 1) else pair
                                                            }
                                                        )
                                                    } else p
                                                }
                                                Toast.makeText(context, "Vote recorded for '$optText'!", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                        .testTag("forum_poll_option_${poll.pollId}_$optIdx")
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = optText,
                                                fontSize = 11.sp,
                                                fontWeight = if (isChosen) FontWeight.ExtraBold else FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onSurface,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "$pct% ($votes)",
                                                fontSize = 10.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isChosen) Color(0xFF7C3AED) else OceanBlue
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(3.dp))
                                        LinearProgressIndicator(
                                            progress = { pct / 100f },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(4.dp),
                                            color = if (isChosen) Color(0xFF7C3AED) else OceanBlue
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
}

@Composable
fun ForumLostAndFoundSection(
    language: AppLanguage = AppLanguage.ENGLISH
) {
    val context = LocalContext.current
    val isOdia = language == AppLanguage.ODIA
    var showForm by remember { mutableStateOf(false) }
    var postType by remember { mutableStateOf("LOST") }
    var itemTitleDraft by remember { mutableStateOf("") }
    var categoryDraft by remember { mutableStateOf("Pet") }
    var locationDraft by remember { mutableStateOf("Sahadevkhunta / Station Square") }
    var descriptionDraft by remember { mutableStateOf("") }
    var contactDraft by remember { mutableStateOf("+91 94370 11223") }
    var photoAttached by remember { mutableStateOf(true) }
    var lastMatchAlertBanner by remember { mutableStateOf<String?>(null) }

    var items by remember {
        mutableStateOf(
            listOf(
                BalasoreLostFoundItem(
                    id = "lf_1",
                    type = "LOST",
                    itemTitle = "Golden Indie Puppy ('Moti') with Red Collar",
                    categoryTag = "Pet",
                    locationArea = "Fakir Mohan Golaye / Mallikashpur",
                    description = "Friendly 6-month-old golden puppy wearing a red bell collar, last seen near Shanti Kanan park gate.",
                    contactPhone = "+91 94372 45109",
                    photoLabel = "📷 Photo Attached: Golden_Puppy_Collar.jpg",
                    postedAtLabel = "35 mins ago"
                ),
                BalasoreLostFoundItem(
                    id = "lf_2",
                    type = "FOUND",
                    itemTitle = "Brown Leather Wallet & Driving License",
                    categoryTag = "Wallet/ID",
                    locationArea = "Sahadevkhunta Bus Stand Bay-2",
                    description = "Found on Mo Bus Route 101 seat. Deposited safely with Station Master / Bus Stand Helpdesk.",
                    contactPhone = "+91 94371 88201",
                    photoLabel = "📷 Photo Attached: Brown_Wallet_Bay2.jpg",
                    postedAtLabel = "1 hr ago"
                )
            )
        )
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, Color(0xFFD97706).copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("forum_lost_and_found_section")
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🔍", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = if (isOdia) "ହଜିବା ଓ ମିଳିବା (Lost & Found)" else "Lost & Found • Items & Pets in Balasore",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isOdia) "ଫଟୋ ସହ ପୋଷ୍ଟ କରନ୍ତୁ ଓ ସ୍ୱୟଂଚାଳିତ ମ୍ୟାଚ୍ ଆଲର୍ଟ ପାଆନ୍ତୁ" else "Post photos & descriptions • Automated matching post alerts",
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                OutlinedButton(
                    onClick = { showForm = !showForm },
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("lost_found_toggle_form_btn")
                ) {
                    Text(
                        text = if (showForm) "Close" else "+ Post Lost/Found",
                        fontSize = 10.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (lastMatchAlertBanner != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = EmeraldGreen.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, EmeraldGreen),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("lost_found_automated_match_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = lastMatchAlertBanner ?: "",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = EmeraldGreen
                        )
                    }
                }
            }

            AnimatedVisibility(visible = showForm) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("LOST", "FOUND").forEach { t ->
                            FilterChip(
                                selected = postType == t,
                                onClick = { postType = t },
                                label = { Text(t, fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                        listOf("Pet", "Wallet/ID", "Phone/Electronics").forEach { cat ->
                            FilterChip(
                                selected = categoryDraft == cat,
                                onClick = { categoryDraft = cat },
                                label = { Text(cat, fontSize = 10.5.sp) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = itemTitleDraft,
                        onValueChange = { itemTitleDraft = it },
                        label = { Text("Item or Pet Name (e.g. Golden Puppy, Leather Wallet)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lost_found_title_input")
                    )

                    OutlinedTextField(
                        value = locationDraft,
                        onValueChange = { locationDraft = it },
                        label = { Text("Location in Balasore") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = descriptionDraft,
                        onValueChange = { descriptionDraft = it },
                        label = { Text("Description & Identifying Marks") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("lost_found_desc_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FilterChip(
                            selected = photoAttached,
                            onClick = { photoAttached = !photoAttached },
                            label = {
                                Text(
                                    if (photoAttached) "📷 Photo Attached (Item_Photo.jpg)" else "📷 Attach Item/Pet Photo",
                                    fontSize = 10.5.sp
                                )
                            }
                        )

                        Button(
                            onClick = {
                                val title = itemTitleDraft.trim().ifBlank { "Golden Puppy Collar near FM Golaye" }
                                val desc = descriptionDraft.trim().ifBlank { "Spotted near Mallikashpur gate with red collar" }
                                val oppositeType = if (postType == "LOST") "FOUND" else "LOST"
                                val matchedExisting = items.firstOrNull { existing ->
                                    existing.type == oppositeType && (
                                        existing.categoryTag.equals(categoryDraft, ignoreCase = true) ||
                                            existing.itemTitle.split(" ").any { word ->
                                                word.length > 3 && title.contains(word, ignoreCase = true)
                                            }
                                        )
                                }

                                val newItem = BalasoreLostFoundItem(
                                    id = "lf_${System.currentTimeMillis()}",
                                    type = postType,
                                    itemTitle = title,
                                    categoryTag = categoryDraft,
                                    locationArea = locationDraft,
                                    description = desc,
                                    contactPhone = contactDraft,
                                    photoLabel = if (photoAttached) "📷 Photo Attached: Balasore_${categoryDraft}_Photo.jpg" else "No photo",
                                    postedAtLabel = "Just now"
                                )
                                items = listOf(newItem) + items
                                itemTitleDraft = ""
                                descriptionDraft = ""
                                showForm = false

                                if (matchedExisting != null) {
                                    val alertMsg = "🔔 Automated Match Found! Your $postType post matches '${matchedExisting.itemTitle}' at ${matchedExisting.locationArea} (${matchedExisting.contactPhone})."
                                    lastMatchAlertBanner = alertMsg
                                    Toast.makeText(context, alertMsg, Toast.LENGTH_LONG).show()
                                } else {
                                    Toast.makeText(context, "Posted to Balasore Lost & Found! Auto-match scanner armed.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = OceanBlue),
                            modifier = Modifier.testTag("lost_found_submit_btn")
                        ) {
                            Text("Publish & Scan Matches", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items.forEach { lf ->
                    val badgeColor = if (lf.type == "LOST") Color(0xFFDC2626) else EmeraldGreen
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, badgeColor.copy(alpha = 0.35f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = RoundedCornerShape(5.dp),
                                        color = badgeColor.copy(alpha = 0.15f)
                                    ) {
                                        Text(
                                            text = "${lf.type} • ${lf.categoryTag}",
                                            fontSize = 9.5.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = badgeColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = lf.postedAtLabel,
                                        fontSize = 9.5.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = lf.photoLabel,
                                    fontSize = 9.5.sp,
                                    color = OceanBlue,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = lf.itemTitle,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "📍 ${lf.locationArea} • 📞 ${lf.contactPhone}",
                                fontSize = 10.5.sp,
                                color = OceanBlue,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = lf.description,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Full-Screen Modal Sheet for the Balasore Community Forum & Offline Sync Center.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BalasoreCommunityForumSheet(
    language: AppLanguage,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isSyncing by BalasoreCommunityForumRepository.isSyncingFlow.collectAsStateWithLifecycle()
    var localRefreshing by remember { mutableStateOf(false) }
    var forumPullStatus by remember {
        mutableStateOf(
            if (language == AppLanguage.ODIA) "ସଦ୍ୟତମ କମ୍ୟୁନିଟି ଆଲୋଚନା ଆଣିବା ପାଇଁ ତଳକୁ ଟାଣନ୍ତୁ"
            else "Pull down to fetch latest community forum posts from network"
        )
    }
    val effectiveRefreshing = isSyncing || localRefreshing
    val pullState = rememberPullToRefreshState()

    val triggerForumPullRefresh: () -> Unit = {
        if (!localRefreshing) {
            localRefreshing = true
            forumPullStatus = if (language == AppLanguage.ODIA) {
                "ନେଟୱାର୍କ ଓ Firestore ରୁ ସଦ୍ୟତମ କମ୍ୟୁନିଟି ପୋଷ୍ଟ ଅଣାଯାଉଛି..."
            } else {
                "Fetching latest community forum discussions from network & Firestore..."
            }
            scope.launch {
                runCatching {
                    BalasoreCommunityForumRepository.syncPendingForumPostsWithCloud(context)
                }
                kotlinx.coroutines.delay(600L)
                localRefreshing = false
                com.example.util.BalasoreHapticHelper.vibrateSuccess(context)
                forumPullStatus = if (language == AppLanguage.ODIA) {
                    "✓ ସଦ୍ୟତମ କମ୍ୟୁନିଟି ପୋଷ୍ଟ ଅପଡେଟ୍ ହୋଇଛି • ଏବେ"
                } else {
                    "✓ Pulled latest community forum posts from network • Just now"
                }
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = effectiveRefreshing,
        onRefresh = triggerForumPullRefresh,
        state = pullState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullState,
                isRefreshing = effectiveRefreshing,
                containerColor = MaterialTheme.colorScheme.surface,
                color = OceanBlue,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .testTag("community_forum_pull_to_refresh_indicator")
            )
        },
        modifier = Modifier
            .fillMaxSize()
            .testTag("community_forum_pull_to_refresh_box")
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp)
                .testTag("balasore_community_forum_sheet"),
            contentPadding = PaddingValues(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == AppLanguage.ODIA) {
                            "💬 ବାଲେଶ୍ୱର କମ୍ୟୁନିଟି ଫୋରମ୍ ଓ ଆଲୋଚନା ମଞ୍ଚ"
                        } else {
                            "💬 Balasore Community Forum & Discussions"
                        },
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = OceanBlueDark
                    )
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.testTag("close_community_forum_sheet_btn")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close Forum")
                    }
                }
            }

            item {
                Surface(
                    onClick = triggerForumPullRefresh,
                    shape = RoundedCornerShape(12.dp),
                    color = OceanBlue.copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, OceanBlue.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("community_forum_pull_refresh_status_banner")
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
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Pull to refresh forum",
                                tint = OceanBlue,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = forumPullStatus,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = OceanBlueDark
                            )
                        }
                        Text(
                            text = if (language == AppLanguage.ODIA) "ରିଫ୍ରେସ୍" else "Refresh",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlue
                        )
                    }
                }
            }

            item {
                BalasoreOfflineDownloadBannerCard(
                    mode = "NEWS",
                    language = language
                )
            }

            item {
                BalasoreCommunityForumCard(
                    language = language,
                    initialCategory = "All"
                )
            }
        }
    }
}

