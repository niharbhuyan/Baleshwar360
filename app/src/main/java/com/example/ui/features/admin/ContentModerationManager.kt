package com.example.ui.features.admin

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.example.data.local.ReviewEntity
import com.example.ui.theme.AmberGold
import com.example.ui.theme.BentoSlate600
import com.example.ui.theme.BentoSlate700
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.OceanBlue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ModerationStatus(val labelEn: String, val labelOr: String) {
    PENDING("Pending Review", "ସମୀକ୍ଷା ବାକି"),
    APPROVED("Approved", "ଅନୁମୋଦିତ"),
    REJECTED("Rejected", "ପ୍ରତ୍ୟାଖ୍ୟାତ"),
    FLAGGED("Flagged by Users", "ରିପୋର୍ଟ ହୋଇଛି")
}

data class ModeratedContentItem(
    val id: String,
    val reviewDbId: Long? = null,
    val targetType: String, // "HOTSPOT", "NEWS", "APP_FEEDBACK"
    val targetId: String,
    val targetTitle: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String,
    val status: ModerationStatus = ModerationStatus.APPROVED,
    val flagCount: Int = 0,
    val flagReasons: List<String> = emptyList(),
    val flaggedByUsers: List<String> = emptyList(),
    val moderatorNote: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val moderatedAt: Long? = null
)

object ContentModerationRepository {
    private const val PREFS_NAME = "balasore_content_moderation_prefs"
    private const val KEY_ITEMS_JSON = "moderation_items_json_v1"
    private const val ROOM_CACHE_KEY = "CONTENT_MODERATION_REGISTRY_ROOM"

    val FLAG_REASON_OPTIONS = listOf(
        "Spam or Promotional Link",
        "Inappropriate or Offensive Language",
        "Misleading Local / Safety Information",
        "Harassment or Personal Attack",
        "Off-Topic / Irrelevant Content"
    )

    private val _itemsFlow = MutableStateFlow<List<ModeratedContentItem>>(defaultSeededItems())
    val itemsFlow: StateFlow<List<ModeratedContentItem>> = _itemsFlow.asStateFlow()

    private fun defaultSeededItems(): List<ModeratedContentItem> {
        val now = System.currentTimeMillis()
        return listOf(
            ModeratedContentItem(
                id = "mod_seed_1",
                targetType = "HOTSPOT",
                targetId = "1",
                targetTitle = "Chandipur-on-Sea Beach",
                userId = "usr_promo_bot",
                userName = "Coastal Dealz Promo",
                rating = 2,
                comment = "Cheap unregulated ATV rides on the seabed! Click unknown external link to book now without forest permit.",
                status = ModerationStatus.FLAGGED,
                flagCount = 3,
                flagReasons = listOf("Spam or Promotional Link", "Misleading Local / Safety Information"),
                flaggedByUsers = listOf("Subrat Mohanty", "Ananya Das", "Coastal Lifeguard"),
                moderatorNote = "Flagged by 3 citizens for unsafe intertidal vehicle promotion.",
                timestamp = now - 3600_000L * 2
            ),
            ModeratedContentItem(
                id = "mod_seed_2",
                targetType = "HOTSPOT",
                targetId = "2",
                targetTitle = "Panchalingeswar Hill Shrine",
                userId = "usr_pilgrim_09",
                userName = "Debasis Panda",
                rating = 5,
                comment = "Visited during Mangala Alati at 5:30 AM. The perennial hill stream over the five Shiva Lingams is serene and well-maintained.",
                status = ModerationStatus.PENDING,
                flagCount = 0,
                flagReasons = emptyList(),
                flaggedByUsers = emptyList(),
                moderatorNote = "Awaiting routine moderator verification.",
                timestamp = now - 3600_000L * 4
            ),
            ModeratedContentItem(
                id = "mod_seed_3",
                targetType = "NEWS",
                targetId = "1",
                targetTitle = "Subarnarekha Flood Embankment Upgraded",
                userId = "usr_anon_77",
                userName = "Unknown Commenter",
                rating = 1,
                comment = "Fake rumour: All bridges in Rajghat are closed today! Do not travel on NH-60.",
                status = ModerationStatus.FLAGGED,
                flagCount = 4,
                flagReasons = listOf("Misleading Local / Safety Information"),
                flaggedByUsers = listOf("Jaleswar Resident", "NHAI Patrol", "Block Officer"),
                moderatorNote = "Urgent review needed: unverified traffic rumour.",
                timestamp = now - 3600_000L * 5
            ),
            ModeratedContentItem(
                id = "mod_seed_4",
                targetType = "HOTSPOT",
                targetId = "3",
                targetTitle = "Khirachora Gopinatha Temple, Remuna",
                userId = "usr_devotee_12",
                userName = "Sushree Patra",
                rating = 5,
                comment = "Authentic Amruta Keli Khira Prasad available after Sandhya Dhupa. Clean queue complex for senior citizens.",
                status = ModerationStatus.APPROVED,
                flagCount = 0,
                flagReasons = emptyList(),
                flaggedByUsers = emptyList(),
                moderatorNote = "Verified helpful visitor tip.",
                timestamp = now - 3600_000L * 12,
                moderatedAt = now - 3600_000L * 10
            ),
            ModeratedContentItem(
                id = "mod_seed_5",
                targetType = "NEWS",
                targetId = "2",
                targetTitle = "Fakir Mohan University New Coastal Research Wing",
                userId = "usr_student_fmu",
                userName = "Priyanka Behera",
                rating = 5,
                comment = "Great initiative for marine biology students studying horseshoe crab conservation in Balasore!",
                status = ModerationStatus.PENDING,
                flagCount = 0,
                flagReasons = emptyList(),
                flaggedByUsers = emptyList(),
                moderatorNote = "New student comment awaiting approval.",
                timestamp = now - 1800_000L
            )
        )
    }

    suspend fun loadAndSyncWithRoom(context: Context) = withContext(Dispatchers.IO) {
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val db = AppDatabase.getInstance(context)
            val metadataDao = db.cacheMetadataDao()
            val reviewDao = db.reviewDao()

            val roomMeta = metadataDao.getMetadataSync(ROOM_CACHE_KEY)
            val jsonSource = roomMeta?.details?.takeIf { it.isNotBlank() }
                ?: prefs.getString(KEY_ITEMS_JSON, null)

            val storedList = if (!jsonSource.isNullOrBlank()) {
                parseJsonToItems(jsonSource)
            } else {
                defaultSeededItems()
            }

            // Merge any ReviewEntity rows from Room ReviewDao so every review submitted anywhere in the app is moderatable
            val roomReviews = reviewDao.getAllReviewsSyncSafe()
            val mergedMap = storedList.associateBy { it.id }.toMutableMap()

            roomReviews.forEach { rev ->
                val key = "rev_${rev.id}_${rev.targetType}_${rev.targetId}"
                val alreadyMatched = mergedMap.values.any {
                    it.reviewDbId == rev.id ||
                        (it.targetType == rev.targetType && it.targetId == rev.targetId &&
                            it.userName == rev.userName && it.comment == rev.comment)
                }
                if (!alreadyMatched) {
                    mergedMap[key] = ModeratedContentItem(
                        id = key,
                        reviewDbId = rev.id,
                        targetType = rev.targetType,
                        targetId = rev.targetId,
                        targetTitle = rev.targetTitle,
                        userId = rev.userId,
                        userName = rev.userName,
                        rating = rev.rating,
                        comment = rev.comment,
                        status = ModerationStatus.APPROVED,
                        timestamp = rev.timestamp
                    )
                }
            }

            val finalItems = mergedMap.values.sortedWith(
                compareBy<ModeratedContentItem> {
                    when (it.status) {
                        ModerationStatus.FLAGGED -> 0
                        ModerationStatus.PENDING -> 1
                        ModerationStatus.APPROVED -> 2
                        ModerationStatus.REJECTED -> 3
                    }
                }.thenByDescending { it.timestamp }
            )
            _itemsFlow.value = finalItems
            persistItems(context, finalItems)
        } catch (_: Exception) {
        }
    }

    private suspend fun ReviewEntityListHelper(context: Context): List<ReviewEntity> {
        return try {
            val db = AppDatabase.getInstance(context)
            db.reviewDao().getCount()
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private suspend fun com.example.data.local.ReviewDao.getAllReviewsSyncSafe(): List<ReviewEntity> {
        var result: List<ReviewEntity> = emptyList()
        return try {
            this.getAllReviews().collect { list ->
                result = list
                throw java.util.concurrent.CancellationException("First emission collected")
            }
            result
        } catch (_: Exception) {
            result
        }
    }

    private suspend fun persistItems(context: Context, items: List<ModeratedContentItem>) = withContext(Dispatchers.IO) {
        try {
            val json = serializeItemsToJson(items)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_ITEMS_JSON, json)
                .apply()

            val db = AppDatabase.getInstance(context)
            db.cacheMetadataDao().insertOrUpdate(
                CacheSyncMetadataEntity(
                    cacheKey = ROOM_CACHE_KEY,
                    lastSyncedAt = System.currentTimeMillis(),
                    itemCount = items.size,
                    status = "MODERATED_SYNC",
                    cacheLabel = "Content Moderation & Flag Queue",
                    details = json
                )
            )
        } catch (_: Exception) {
        }
    }

    suspend fun registerSubmittedReview(
        context: Context,
        reviewDbId: Long?,
        targetType: String,
        targetId: String,
        targetTitle: String,
        userId: String,
        userName: String,
        rating: Int,
        comment: String
    ) {
        val newItem = ModeratedContentItem(
            id = "mod_${System.currentTimeMillis()}",
            reviewDbId = reviewDbId,
            targetType = targetType,
            targetId = targetId,
            targetTitle = targetTitle,
            userId = userId,
            userName = userName,
            rating = rating,
            comment = comment,
            status = ModerationStatus.PENDING,
            moderatorNote = "Submitted by user — awaiting moderator review",
            timestamp = System.currentTimeMillis()
        )
        val updated = listOf(newItem) + _itemsFlow.value
        _itemsFlow.value = updated
        persistItems(context, updated)
    }

    suspend fun flagContent(
        context: Context,
        targetType: String,
        targetId: String,
        targetTitle: String,
        authorName: String,
        rating: Int,
        commentText: String,
        reason: String,
        customDetails: String,
        reporterName: String
    ) {
        val current = _itemsFlow.value.toMutableList()
        val existingIdx = current.indexOfFirst {
            (it.targetType == targetType && it.targetId == targetId && it.comment == commentText) ||
                (it.userName == authorName && it.comment == commentText)
        }
        val combinedReason = if (customDetails.isNotBlank()) "$reason ($customDetails)" else reason

        if (existingIdx >= 0) {
            val existing = current[existingIdx]
            val updatedItem = existing.copy(
                status = ModerationStatus.FLAGGED,
                flagCount = existing.flagCount + 1,
                flagReasons = (existing.flagReasons + combinedReason).distinct(),
                flaggedByUsers = (existing.flaggedByUsers + reporterName).distinct(),
                moderatorNote = "Flagged by $reporterName: $combinedReason"
            )
            current[existingIdx] = updatedItem
        } else {
            val flaggedNew = ModeratedContentItem(
                id = "flag_${System.currentTimeMillis()}",
                targetType = targetType,
                targetId = targetId,
                targetTitle = targetTitle,
                userId = "reported_user",
                userName = authorName,
                rating = rating,
                comment = commentText,
                status = ModerationStatus.FLAGGED,
                flagCount = 1,
                flagReasons = listOf(combinedReason),
                flaggedByUsers = listOf(reporterName),
                moderatorNote = "Flagged by $reporterName: $combinedReason",
                timestamp = System.currentTimeMillis()
            )
            current.add(0, flaggedNew)
        }

        val sorted = current.sortedWith(
            compareBy<ModeratedContentItem> {
                when (it.status) {
                    ModerationStatus.FLAGGED -> 0
                    ModerationStatus.PENDING -> 1
                    ModerationStatus.APPROVED -> 2
                    ModerationStatus.REJECTED -> 3
                }
            }.thenByDescending { it.timestamp }
        )
        _itemsFlow.value = sorted
        persistItems(context, sorted)
    }

    suspend fun updateModerationStatus(
        context: Context,
        itemId: String,
        newStatus: ModerationStatus,
        moderatorNote: String = ""
    ) {
        val updated = _itemsFlow.value.map { item ->
            if (item.id == itemId) {
                item.copy(
                    status = newStatus,
                    flagCount = if (newStatus == ModerationStatus.APPROVED) 0 else item.flagCount,
                    moderatorNote = moderatorNote.ifBlank {
                        when (newStatus) {
                            ModerationStatus.APPROVED -> "Approved by District Administrator"
                            ModerationStatus.REJECTED -> "Rejected: Violates Baleshwar 360 Community Guidelines"
                            else -> item.moderatorNote
                        }
                    },
                    moderatedAt = System.currentTimeMillis()
                )
            } else {
                item
            }
        }
        _itemsFlow.value = updated
        persistItems(context, updated)
    }

    suspend fun deleteModeratedItem(context: Context, itemId: String) {
        val updated = _itemsFlow.value.filterNot { it.id == itemId }
        _itemsFlow.value = updated
        persistItems(context, updated)
    }

    suspend fun approveAllPending(context: Context) {
        val now = System.currentTimeMillis()
        val updated = _itemsFlow.value.map { item ->
            if (item.status == ModerationStatus.PENDING) {
                item.copy(
                    status = ModerationStatus.APPROVED,
                    moderatorNote = "Batch approved by Administrator",
                    moderatedAt = now
                )
            } else {
                item
            }
        }
        _itemsFlow.value = updated
        persistItems(context, updated)
    }

    suspend fun rejectAllFlagged(context: Context) {
        val now = System.currentTimeMillis()
        val updated = _itemsFlow.value.map { item ->
            if (item.status == ModerationStatus.FLAGGED) {
                item.copy(
                    status = ModerationStatus.REJECTED,
                    moderatorNote = "Batch rejected due to community flags",
                    moderatedAt = now
                )
            } else {
                item
            }
        }
        _itemsFlow.value = updated
        persistItems(context, updated)
    }

    fun isContentRejected(targetType: String, targetId: String, commentText: String): Boolean {
        return _itemsFlow.value.any {
            it.status == ModerationStatus.REJECTED &&
                it.targetType == targetType &&
                it.targetId == targetId &&
                it.comment == commentText
        }
    }

    fun getStatusForReview(targetType: String, targetId: String, commentText: String): ModerationStatus {
        val match = _itemsFlow.value.firstOrNull {
            it.targetType == targetType && it.targetId == targetId && it.comment == commentText
        }
        return match?.status ?: ModerationStatus.APPROVED
    }

    private fun serializeItemsToJson(items: List<ModeratedContentItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                if (item.reviewDbId != null) put("reviewDbId", item.reviewDbId)
                put("targetType", item.targetType)
                put("targetId", item.targetId)
                put("targetTitle", item.targetTitle)
                put("userId", item.userId)
                put("userName", item.userName)
                put("rating", item.rating)
                put("comment", item.comment)
                put("status", item.status.name)
                put("flagCount", item.flagCount)
                put("flagReasons", JSONArray(item.flagReasons))
                put("flaggedByUsers", JSONArray(item.flaggedByUsers))
                put("moderatorNote", item.moderatorNote)
                put("timestamp", item.timestamp)
                if (item.moderatedAt != null) put("moderatedAt", item.moderatedAt)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseJsonToItems(json: String): List<ModeratedContentItem> {
        val list = mutableListOf<ModeratedContentItem>()
        val arr = JSONArray(json)
        for (i in 0 until arr.length()) {
            val obj = arr.getJSONObject(i)
            val reasonsArr = obj.optJSONArray("flagReasons")
            val reasons = mutableListOf<String>()
            if (reasonsArr != null) {
                for (j in 0 until reasonsArr.length()) {
                    reasons.add(reasonsArr.getString(j))
                }
            }
            val usersArr = obj.optJSONArray("flaggedByUsers")
            val users = mutableListOf<String>()
            if (usersArr != null) {
                for (j in 0 until usersArr.length()) {
                    users.add(usersArr.getString(j))
                }
            }
            val statusStr = obj.optString("status", ModerationStatus.APPROVED.name)
            val status = runCatching { ModerationStatus.valueOf(statusStr) }.getOrDefault(ModerationStatus.APPROVED)
            list.add(
                ModeratedContentItem(
                    id = obj.optString("id", "mod_$i"),
                    reviewDbId = if (obj.has("reviewDbId")) obj.optLong("reviewDbId") else null,
                    targetType = obj.optString("targetType", "HOTSPOT"),
                    targetId = obj.optString("targetId", "1"),
                    targetTitle = obj.optString("targetTitle", "Balasore Hotspot"),
                    userId = obj.optString("userId", "citizen"),
                    userName = obj.optString("userName", "Balasore Citizen"),
                    rating = obj.optInt("rating", 5),
                    comment = obj.optString("comment", ""),
                    status = status,
                    flagCount = obj.optInt("flagCount", 0),
                    flagReasons = reasons,
                    flaggedByUsers = users,
                    moderatorNote = obj.optString("moderatorNote", ""),
                    timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                    moderatedAt = if (obj.has("moderatedAt")) obj.optLong("moderatedAt") else null
                )
            )
        }
        return list
    }
}

/**
 * Compact badge + "🚩 Flag / Report" button attached to user reviews & comments across Hotspots & News.
 */
@Composable
fun ReviewModerationBadgeAndFlagButton(
    targetType: String,
    targetId: String,
    targetTitle: String,
    authorName: String,
    rating: Int,
    commentText: String,
    isOdia: Boolean = false,
    tagSuffix: String = "0"
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val items by ContentModerationRepository.itemsFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        ContentModerationRepository.loadAndSyncWithRoom(context)
    }

    val matchedItem = remember(items, targetType, targetId, commentText) {
        items.firstOrNull {
            it.targetType == targetType && it.targetId == targetId && it.comment == commentText
        }
    }
    val status = matchedItem?.status ?: ModerationStatus.APPROVED
    var showFlagDialog by remember { mutableStateOf(false) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = when (status) {
                ModerationStatus.APPROVED -> Color(0xFFDCFCE7)
                ModerationStatus.PENDING -> Color(0xFFFEF3C7)
                ModerationStatus.FLAGGED -> Color(0xFFFEE2E2)
                ModerationStatus.REJECTED -> Color(0xFFF1F5F9)
            }
        ) {
            Text(
                text = when (status) {
                    ModerationStatus.APPROVED -> if (isOdia) "✓ ଅନୁମୋଦିତ" else "✓ Approved"
                    ModerationStatus.PENDING -> if (isOdia) "⏳ ସମୀକ୍ଷା ବାକି" else "⏳ Pending"
                    ModerationStatus.FLAGGED -> if (isOdia) "🚩 ରିପୋର୍ଟ (${matchedItem?.flagCount ?: 1})" else "🚩 Flagged (${matchedItem?.flagCount ?: 1})"
                    ModerationStatus.REJECTED -> if (isOdia) "✕ ପ୍ରତ୍ୟାଖ୍ୟାତ" else "✕ Rejected"
                },
                fontSize = 9.5.sp,
                fontWeight = FontWeight.Bold,
                color = when (status) {
                    ModerationStatus.APPROVED -> Color(0xFF166534)
                    ModerationStatus.PENDING -> Color(0xFFB45309)
                    ModerationStatus.FLAGGED -> Color(0xFFB91C1C)
                    ModerationStatus.REJECTED -> Color(0xFF475569)
                },
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(6.dp),
            color = Color(0xFFFEF2F2),
            border = BorderStroke(1.dp, Color(0xFFFECACA)),
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable { showFlagDialog = true }
                .testTag("flag_content_btn_$tagSuffix")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = "Flag Inappropriate Content",
                    tint = Color(0xFFDC2626),
                    modifier = Modifier.size(11.dp)
                )
                Text(
                    text = if (isOdia) "ରିପୋର୍ଟ" else "Flag",
                    fontSize = 9.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFDC2626)
                )
            }
        }
    }

    if (showFlagDialog) {
        FlagContentReportDialog(
            targetTitle = targetTitle,
            authorName = authorName,
            commentText = commentText,
            isOdia = isOdia,
            onDismiss = { showFlagDialog = false },
            onSubmitFlag = { reason, customNote, reporter ->
                scope.launch {
                    ContentModerationRepository.flagContent(
                        context = context,
                        targetType = targetType,
                        targetId = targetId,
                        targetTitle = targetTitle,
                        authorName = authorName,
                        rating = rating,
                        commentText = commentText,
                        reason = reason,
                        customDetails = customNote,
                        reporterName = reporter
                    )
                    Toast.makeText(
                        context,
                        if (isOdia) "ସମୀକ୍ଷା ପାଇଁ ଆଡମିନ୍ ମୋଡେରେଟରଙ୍କୁ ରିପୋର୍ଟ କରାଗଲା!" else "Content flagged for Administrator Moderation review!",
                        Toast.LENGTH_SHORT
                    ).show()
                    showFlagDialog = false
                }
            }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FlagContentReportDialog(
    targetTitle: String,
    authorName: String,
    commentText: String,
    isOdia: Boolean,
    onDismiss: () -> Unit,
    onSubmitFlag: (reason: String, customNote: String, reporterName: String) -> Unit
) {
    var selectedReason by remember { mutableStateOf(ContentModerationRepository.FLAG_REASON_OPTIONS.first()) }
    var customNote by remember { mutableStateOf("") }
    var reporterName by remember { mutableStateOf("Balasore Citizen") }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("flag_inappropriate_content_dialog"),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Flag,
                    contentDescription = null,
                    tint = Color(0xFFDC2626)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = if (isOdia) "ଅନୁପଯୁକ୍ତ ବିଷୟবস্তୁ ରିପୋର୍ଟ କରନ୍ତୁ" else "Flag Inappropriate Content",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Target: $targetTitle • By $authorName",
                        fontSize = 11.sp,
                        color = BentoSlate600
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "\"$commentText\"",
                        fontSize = 12.sp,
                        color = BentoSlate700,
                        modifier = Modifier.padding(10.dp),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Text(
                    text = if (isOdia) "ରିପୋର୍ଟ କରିବାର କାରଣ ବାଛନ୍ତୁ:" else "Select Reason for Flagging:",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ContentModerationRepository.FLAG_REASON_OPTIONS.forEachIndexed { idx, reason ->
                        FilterChip(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason },
                            label = { Text(reason, fontSize = 11.sp) },
                            modifier = Modifier.testTag("flag_reason_chip_$idx")
                        )
                    }
                }

                OutlinedTextField(
                    value = customNote,
                    onValueChange = { customNote = it },
                    label = { Text(if (isOdia) "ଅତିରିକ୍ତ ବିବରଣୀ (ঐচ্ছିକ)" else "Additional context for Moderator (Optional)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flag_custom_note_input")
                )

                OutlinedTextField(
                    value = reporterName,
                    onValueChange = { reporterName = it },
                    label = { Text(if (isOdia) "ଆପଣଙ୍କ ନାମ" else "Reported By (Your Name)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("flag_reporter_name_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSubmitFlag(
                        selectedReason,
                        customNote.trim(),
                        reporterName.trim().ifBlank { "Anonymous Citizen" }
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                modifier = Modifier.testTag("submit_flag_report_btn")
            ) {
                Icon(Icons.Default.Flag, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(if (isOdia) "ରିପୋର୍ଟ ଦାଖଲ କରନ୍ତୁ" else "Submit Flag to Admin")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_flag_report_btn")
            ) {
                Text(if (isOdia) "ବାତିଲ୍" else "Cancel")
            }
        }
    )
}

/**
 * Administrator Content Moderation Queue Tab inside AdminModeratorPanelSheet.
 * Allows administrators to review, filter, approve, reject, or delete user-submitted reviews and comments,
 * and inspect user-submitted flags.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AdminContentModerationTab(
    isOdia: Boolean = false
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val allItems by ContentModerationRepository.itemsFlow.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        ContentModerationRepository.loadAndSyncWithRoom(context)
    }

    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "FLAGGED", "PENDING", "APPROVED", "REJECTED"
    var searchQuery by remember { mutableStateOf("") }
    var newSampleComment by remember { mutableStateOf("") }
    var newSampleTarget by remember { mutableStateOf("Chandipur-on-Sea Beach") }

    val flaggedCount = remember(allItems) { allItems.count { it.status == ModerationStatus.FLAGGED } }
    val pendingCount = remember(allItems) { allItems.count { it.status == ModerationStatus.PENDING } }
    val approvedCount = remember(allItems) { allItems.count { it.status == ModerationStatus.APPROVED } }
    val rejectedCount = remember(allItems) { allItems.count { it.status == ModerationStatus.REJECTED } }

    val filteredItems = remember(allItems, selectedFilter, searchQuery) {
        allItems.filter { item ->
            val statusMatches = when (selectedFilter) {
                "FLAGGED" -> item.status == ModerationStatus.FLAGGED
                "PENDING" -> item.status == ModerationStatus.PENDING
                "APPROVED" -> item.status == ModerationStatus.APPROVED
                "REJECTED" -> item.status == ModerationStatus.REJECTED
                else -> true
            }
            val queryMatches = searchQuery.isBlank() ||
                item.comment.contains(searchQuery, ignoreCase = true) ||
                item.userName.contains(searchQuery, ignoreCase = true) ||
                item.targetTitle.contains(searchQuery, ignoreCase = true) ||
                item.flagReasons.any { it.contains(searchQuery, ignoreCase = true) }
            statusMatches && queryMatches
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("admin_content_moderation_lazy_list"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Moderation Queue Overview & Batch Actions Card
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
                                        "🛡️ ବିଷୟବସ୍ତୁ ମୋଡେରେସନ୍ ଓ ରିପୋର୍ଟ ସମୀକ୍ଷା"
                                    } else {
                                        "🛡️ User Content Moderation & Flag Queue"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Text(
                                    text = if (isOdia) {
                                        "ୟୁଜର୍ ସମୀକ୍ଷା, ମନ୍ତବ୍ୟ ଏବଂ ରିପୋର୍ଟଗୁଡ଼ିକୁ ଅନୁମୋଦନ କିମ୍ବା ପ୍ରତ୍ୟାଖ୍ୟାନ କରନ୍ତୁ"
                                    } else {
                                        "Review, approve, or reject user-submitted reviews, comments & flagged items"
                                    },
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // 4 Status Metric Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ModerationStatPill(
                            label = "🚩 Flagged",
                            count = flaggedCount,
                            color = Color(0xFFDC2626),
                            bgColor = Color(0xFFFEE2E2),
                            modifier = Modifier.weight(1f)
                        )
                        ModerationStatPill(
                            label = "⏳ Pending",
                            count = pendingCount,
                            color = Color(0xFFD97706),
                            bgColor = Color(0xFFFEF3C7),
                            modifier = Modifier.weight(1f)
                        )
                        ModerationStatPill(
                            label = "✓ Approved",
                            count = approvedCount,
                            color = Color(0xFF15803D),
                            bgColor = Color(0xFFDCFCE7),
                            modifier = Modifier.weight(1f)
                        )
                        ModerationStatPill(
                            label = "✕ Rejected",
                            count = rejectedCount,
                            color = Color(0xFF475569),
                            bgColor = Color(0xFFF1F5F9),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Batch Administrator Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                scope.launch {
                                    ContentModerationRepository.approveAllPending(context)
                                    Toast.makeText(context, "Approved all pending user reviews!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = pendingCount > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_mod_approve_all_btn")
                        ) {
                            Icon(Icons.Default.DoneAll, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Approve Pending ($pendingCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    ContentModerationRepository.rejectAllFlagged(context)
                                    Toast.makeText(context, "Rejected all flagged items!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            enabled = flaggedCount > 0,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = BorderStroke(1.dp, Color(0xFFDC2626)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_mod_reject_all_flagged_btn")
                        ) {
                            Icon(Icons.Default.ReportProblem, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reject Flagged ($flaggedCount)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 2. Filter Chips & Search Bar
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search reviews, comments, author, or flag reason...", fontSize = 12.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_mod_search_input")
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(
                        "ALL" to "All (${allItems.size})",
                        "FLAGGED" to "🚩 Flagged ($flaggedCount)",
                        "PENDING" to "⏳ Pending ($pendingCount)",
                        "APPROVED" to "✓ Approved ($approvedCount)",
                        "REJECTED" to "✕ Rejected ($rejectedCount)"
                    ).forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            modifier = Modifier.testTag("admin_mod_filter_${key.lowercase()}")
                        )
                    }
                }
            }
        }

        // 3. Quick Simulator: Submit or Flag a User Comment to Test Moderation Queue
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isOdia) "➕ ନୂଆ ୟୁଜର୍ ମନ୍ତବ୍ୟ / ରିପୋର୍ଟ ପରୀକ୍ଷା କରନ୍ତୁ" else "➕ Submit User Review / Flagged Comment to Queue",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newSampleComment,
                            onValueChange = { newSampleComment = it },
                            placeholder = { Text("Enter user review or comment...", fontSize = 11.5.sp) },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("admin_mod_new_comment_input")
                        )
                        Button(
                            onClick = {
                                val text = newSampleComment.trim().ifBlank { "Needs clean drinking water kiosk near beach gate." }
                                scope.launch {
                                    ContentModerationRepository.registerSubmittedReview(
                                        context = context,
                                        reviewDbId = null,
                                        targetType = "HOTSPOT",
                                        targetId = "1",
                                        targetTitle = newSampleTarget,
                                        userId = "citizen_tester",
                                        userName = "Citizen Reviewer",
                                        rating = 4,
                                        comment = text
                                    )
                                    newSampleComment = ""
                                    Toast.makeText(context, "Added to Pending Moderation Queue!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("admin_mod_add_pending_btn")
                        ) {
                            Text("Add Pending", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // 4. List of Moderated Reviews & Comments
        items(filteredItems, key = { it.id }) { item ->
            ModerationQueueItemCard(
                item = item,
                isOdia = isOdia,
                onApprove = { note ->
                    scope.launch {
                        ContentModerationRepository.updateModerationStatus(
                            context = context,
                            itemId = item.id,
                            newStatus = ModerationStatus.APPROVED,
                            moderatorNote = note
                        )
                        Toast.makeText(context, "Approved content by ${item.userName}", Toast.LENGTH_SHORT).show()
                    }
                },
                onReject = { note ->
                    scope.launch {
                        ContentModerationRepository.updateModerationStatus(
                            context = context,
                            itemId = item.id,
                            newStatus = ModerationStatus.REJECTED,
                            moderatorNote = note
                        )
                        Toast.makeText(context, "Rejected content by ${item.userName}", Toast.LENGTH_SHORT).show()
                    }
                },
                onFlag = { reason, note, reporter ->
                    scope.launch {
                        ContentModerationRepository.flagContent(
                            context = context,
                            targetType = item.targetType,
                            targetId = item.targetId,
                            targetTitle = item.targetTitle,
                            authorName = item.userName,
                            rating = item.rating,
                            commentText = item.comment,
                            reason = reason,
                            customDetails = note,
                            reporterName = reporter
                        )
                        Toast.makeText(context, "Flagged content for review!", Toast.LENGTH_SHORT).show()
                    }
                },
                onDelete = {
                    scope.launch {
                        ContentModerationRepository.deleteModeratedItem(context, item.id)
                        Toast.makeText(context, "Deleted content permanently", Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }
    }
}

@Composable
private fun ModerationStatPill(
    label: String,
    count: Int,
    color: Color,
    bgColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(10.dp),
        color = bgColor
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontWeight = FontWeight.ExtraBold,
                fontSize = 15.sp,
                color = color
            )
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ModerationQueueItemCard(
    item: ModeratedContentItem,
    isOdia: Boolean,
    onApprove: (String) -> Unit,
    onReject: (String) -> Unit,
    onFlag: (reason: String, customNote: String, reporterName: String) -> Unit,
    onDelete: () -> Unit
) {
    var moderatorNoteInput by remember(item.moderatorNote) { mutableStateOf(item.moderatorNote) }
    var showFlagModal by remember { mutableStateOf(false) }
    val formattedDate = remember(item.timestamp) {
        SimpleDateFormat("dd MMM, hh:mm a", Locale.US).format(Date(item.timestamp))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_moderation_item_${item.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (item.status) {
                ModerationStatus.FLAGGED -> Color(0xFFFEF2F2)
                ModerationStatus.PENDING -> Color(0xFFFFFBEB)
                ModerationStatus.REJECTED -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                ModerationStatus.APPROVED -> MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when (item.status) {
                ModerationStatus.FLAGGED -> Color(0xFFF87171)
                ModerationStatus.PENDING -> Color(0xFFFBBF24)
                ModerationStatus.APPROVED -> Color(0xFF86EFAC)
                ModerationStatus.REJECTED -> Color(0xFFCBD5E1)
            }
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Top Row: Type badge + Target Title + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = OceanBlue.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = item.targetType,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = OceanBlue,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = item.targetTitle,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = when (item.status) {
                        ModerationStatus.FLAGGED -> Color(0xFFDC2626)
                        ModerationStatus.PENDING -> Color(0xFFD97706)
                        ModerationStatus.APPROVED -> Color(0xFF16A34A)
                        ModerationStatus.REJECTED -> Color(0xFF64748B)
                    }
                ) {
                    Text(
                        text = if (isOdia) item.status.labelOr else item.status.labelEn,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Author + Rating + Timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(OceanBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.userName.take(1).uppercase(),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = OceanBlue
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.userName,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = AmberGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${item.rating}/5",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoSlate700
                        )
                    }
                }
                Text(
                    text = formattedDate,
                    fontSize = 10.5.sp,
                    color = BentoSlate600
                )
            }

            // User Comment Body
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = Color.White.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "\"${item.comment}\"",
                    fontSize = 12.5.sp,
                    color = Color(0xFF1E293B),
                    modifier = Modifier.padding(10.dp)
                )
            }

            // Flag Details Banner (if flagged by users)
            if (item.flagCount > 0 || item.flagReasons.isNotEmpty()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFFFEE2E2),
                    border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Text(
                            text = "🚩 Flagged ${item.flagCount} time(s) • Reported by: ${item.flaggedByUsers.joinToString(", ").ifBlank { "Community Member" }}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "Reasons: ${item.flagReasons.joinToString(" • ")}",
                            fontSize = 11.sp,
                            color = Color(0xFFB91C1C)
                        )
                    }
                }
            }

            // Moderator Decision Note Field
            OutlinedTextField(
                value = moderatorNoteInput,
                onValueChange = { moderatorNoteInput = it },
                label = { Text("Moderator Decision Note", fontSize = 11.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_mod_note_input_${item.id}")
            )

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Action Buttons: Approve | Reject | Flag | Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = { onApprove(moderatorNoteInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_mod_approve_btn_${item.id}")
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Approve", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isOdia) "ଅନୁମୋଦନ" else "Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onReject(moderatorNoteInput) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("admin_mod_reject_btn_${item.id}")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Reject", modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isOdia) "ପ୍ରତ୍ୟାଖ୍ୟାନ" else "Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showFlagModal = true },
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("admin_mod_flag_btn_${item.id}")
                ) {
                    Icon(Icons.Default.Flag, contentDescription = "Flag", tint = Color(0xFFD97706), modifier = Modifier.size(15.dp))
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("admin_mod_delete_btn_${item.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Permanently",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }

    if (showFlagModal) {
        FlagContentReportDialog(
            targetTitle = item.targetTitle,
            authorName = item.userName,
            commentText = item.comment,
            isOdia = isOdia,
            onDismiss = { showFlagModal = false },
            onSubmitFlag = { reason, note, reporter ->
                onFlag(reason, note, reporter)
                showFlagModal = false
            }
        )
    }
}
